import * as vscode from "vscode";
import * as fs from "node:fs/promises";
import path from "node:path";
import { createHash } from "node:crypto";
import {
  PlatformClient,
  PlatformTransport,
  platformOrigin,
  type Credential,
  type Specification,
  type Organization,
} from "./client";
import { MavenResolver } from "./maven";
import {
  lockSpecification,
  addDependency,
  classifyDependencies,
  verifiedBytes,
  type Dependency,
  type LockedSpecification,
} from "./dependencies";
import type { Project, ProjectManager } from "../host/projects";
const AUTH = "zernova-platform";
const lockName = "zernova-exchange.json";
interface LockFile {
  schemaVersion: 1;
  specifications: LockedSpecification[];
}
class Item extends vscode.TreeItem {
  constructor(
    label: string,
    command?: string,
    public spec?: Specification,
  ) {
    super(label, vscode.TreeItemCollapsibleState.None);
    if (command)
      this.command = { command, title: label, arguments: spec ? [spec] : [] };
  }
}
class ExchangeTree implements vscode.TreeDataProvider<Item> {
  private readonly change = new vscode.EventEmitter<Item | undefined>();
  readonly onDidChangeTreeData = this.change.event;
  items: Item[] = [
    new Item(
      "Connect to ZERNOVA Platform",
      "zernovaKernelStudio.platform.signIn",
    ),
  ];
  getTreeItem(item: Item) {
    return item;
  }
  getChildren() {
    return this.items;
  }
  refresh() {
    this.change.fire(undefined);
  }
  dispose() {
    this.change.dispose();
  }
}
async function physical(root: string, relative: string) {
  if (
    !path.isAbsolute(root) ||
    path.isAbsolute(relative) ||
    relative.split("/").some((v) => v === ".." || v === "")
  )
    throw new Error("Invalid project path.");
  const actual = await fs.realpath(root);
  if (actual !== root)
    throw new Error(
      "Open the physical project directory before managing dependencies.",
    );
  let current = root;
  for (const part of relative.split("/")) {
    current = path.join(current, part);
    const stat = await fs.lstat(current).catch((e) => {
      if (e.code === "ENOENT") return undefined;
      throw e;
    });
    if (stat?.isSymbolicLink() || (stat?.isFile() && stat.nlink !== 1))
      throw new Error("Linked project paths are refused.");
  }
  return current;
}
async function readLocks(project: Project): Promise<LockFile> {
  const filename = await physical(project.root, lockName);
  const text = await fs.readFile(filename, "utf8").catch((e) => {
    if (e.code === "ENOENT") return undefined;
    throw e;
  });
  if (!text) return { schemaVersion: 1, specifications: [] };
  if (Buffer.byteLength(text) > 100000)
    throw new Error("Exchange lock metadata is too large.");
  const value = JSON.parse(text) as LockFile;
  if (
    value.schemaVersion !== 1 ||
    Object.keys(value).some(
      (k) => !["schemaVersion", "specifications"].includes(k),
    ) ||
    !Array.isArray(value.specifications) ||
    value.specifications.length > 32
  )
    throw new Error("Invalid Exchange lock file.");
  for (const lock of value.specifications) {
    const expected = lockSpecification(lock.platformOrigin, {
      id: lock.versionId,
      name: lock.name,
      version: lock.version,
      format: lock.format,
      sha256: lock.sha256,
      organization_id: lock.organizationId,
      workspace_id: "",
      workspace_name: "",
      maven: {
        groupId: lock.groupId,
        artifactId: lock.artifactId,
        version: lock.version,
        extension: lock.type,
        documentPath: "",
        coordinate: "",
      },
    });
    if (
      JSON.stringify(Object.entries(expected).sort()) !==
      JSON.stringify(Object.entries(lock).sort())
    )
      throw new Error(
        "Exchange lock metadata was changed or contains unsupported fields.",
      );
  }
  if (
    new Set(value.specifications.map((l) => l.groupId + ":" + l.artifactId))
      .size !== value.specifications.length
  )
    throw new Error("Duplicate Exchange lock entries are refused.");
  return value;
}
export function registerPlatformIntegration(
  context: vscode.ExtensionContext,
  projects: ProjectManager,
) {
  const changed =
    new vscode.EventEmitter<vscode.AuthenticationProviderAuthenticationSessionsChangeEvent>();
  const tree = new ExchangeTree();
  const output = vscode.window.createOutputChannel("ZERNOVA Platform");
  const status = vscode.window.createStatusBarItem(
    vscode.StatusBarAlignment.Left,
    20,
  );
  status.text = "$(account) ZERNOVA Platform";
  status.command = "zernovaKernelStudio.platform.browse";
  status.show();
  let clientReady: Promise<PlatformClient> | undefined,
    organization: Organization | undefined,
    workspace: string | undefined;
  function getClient(): Promise<PlatformClient> {
    if (!clientReady)
      clientReady = (async () => {
        const settings = vscode.workspace.getConfiguration(
          "zernovaKernelStudio",
        );
        const origin = platformOrigin(
          settings.get<string>("platformUrl") || "https://localhost:4640",
        );
        const caPath = settings.get<string>("platformCertificate");
        const ca = caPath ? await fs.readFile(caPath, "utf8") : undefined;
        const key =
          "zernova.platform.session." +
          createHash("sha256").update(origin).digest("hex");
        return new PlatformClient(
          origin,
          new PlatformTransport(origin, ca),
          async () => {
            const value = await context.secrets.get(key);
            return value ? (JSON.parse(value) as Credential) : undefined;
          },
          async (value) => {
            if (value) await context.secrets.store(key, JSON.stringify(value));
            else await context.secrets.delete(key);
          },
        );
      })().catch((error) => {
        clientReady = undefined;
        throw error;
      });
    return clientReady;
  }
  async function session(): Promise<vscode.AuthenticationSession | undefined> {
    try {
      const c = await (await getClient()).credential();
      return {
        id: c.session_id,
        accessToken: c.access_token,
        account: { id: c.account_id, label: "ZERNOVA Platform account" },
        scopes: [(await getClient()).origin],
      };
    } catch {
      return undefined;
    }
  }
  async function signIn() {
    if (!vscode.workspace.isTrusted)
      throw new Error(
        "Trust this development workspace before connecting Studio.",
      );
    const c = await getClient(),
      device = await c.begin();
    const choice = await vscode.window.showInformationMessage(
      "Studio verification code: " +
        device.user_code +
        ". Approve only this code in your Platform browser window.",
      { modal: true },
      "Open Platform sign-in",
    );
    if (choice !== "Open Platform sign-in")
      throw new Error("Studio connection canceled.");
    const target = device.verification_uri + "?code=" + device.user_code;
    if (!(await vscode.env.openExternal(vscode.Uri.parse(target))))
      throw new Error(
        "Open the Platform verification page from Identity & access and enter code " +
          device.user_code +
          ".",
      );
    await vscode.window.withProgress(
      {
        location: vscode.ProgressLocation.Notification,
        cancellable: true,
        title: "ZERNOVA Platform: waiting for approval · " + device.user_code,
      },
      async (_, cancel) => {
        const end = Date.now() + Math.min(device.expires_in, 600) * 1000;
        while (Date.now() < end) {
          if (cancel.isCancellationRequested)
            throw new Error("Studio connection canceled.");
          if ((await c.poll(device)) === "connected") return;
          await new Promise((resolve) =>
            setTimeout(resolve, Math.max(3, device.interval) * 1000),
          );
        }
        throw new Error("Studio verification expired.");
      },
    );
    const s = await session();
    if (!s) throw new Error("Studio session could not be stored securely.");
    changed.fire({ added: [s], removed: [], changed: [] });
    await refresh();
    return s;
  }
  async function signOut() {
    const s = await session();
    try {
      await (await getClient()).logout();
    } finally {
      organization = undefined;
      workspace = undefined;
      tree.items = [
        new Item(
          "Connect to ZERNOVA Platform",
          "zernovaKernelStudio.platform.signIn",
        ),
      ];
      tree.refresh();
      status.text = "$(account) ZERNOVA Platform";
      if (s) changed.fire({ added: [], removed: [s], changed: [] });
    }
  }
  async function refresh() {
    const c = await getClient();
    const account = await c.account();
    organization = account.organizations.find((o) => o.id === organization?.id);
    if (!organization) {
      const picked = await vscode.window.showQuickPick(
        account.organizations.map((o) => ({
          label: o.name,
          description: o.role,
          org: o,
        })),
        { title: "ZERNOVA Platform · choose organization" },
      );
      if (!picked) return;
      organization = picked.org;
      workspace = undefined;
    }
    const workspaces = await c.workspaces(organization.id);
    if (!workspace && workspaces.length) {
      const picked = await vscode.window.showQuickPick(
        [
          { label: "All workspaces", id: "*" },
          ...workspaces.map((w) => ({ label: w.name, id: w.id })),
        ],
        { title: "ZERNOVA Platform · choose workspace" },
      );
      if (!picked) return;
      workspace = picked.id;
    }
    const specs = await c.exchange(organization.id);
    const visible =
      workspace && workspace !== "*"
        ? specs.filter((s) => s.workspace_id === workspace)
        : specs;
    const header = new Item(organization.name + " · " + account.id.slice(0, 8));
    header.description = organization.role;
    tree.items = [
      header,
      new Item(
        "Change organization / workspace",
        "zernovaKernelStudio.platform.selectOrganization",
      ),
      new Item("Refresh Exchange", "zernovaKernelStudio.platform.browse"),
      ...visible.map((s) => {
        const i = new Item(
          s.name + " · " + s.version,
          "zernovaKernelStudio.platform.addDependency",
          s,
        );
        i.description = s.format.toUpperCase();
        i.tooltip =
          "Approved specification · " +
          s.maven.coordinate +
          "\nSHA-256 " +
          s.sha256;
        return i;
      }),
      new Item(
        "Resolve project API dependencies",
        "zernovaKernelStudio.platform.resolve",
      ),
      new Item("Sign out of Platform", "zernovaKernelStudio.platform.signOut"),
    ];
    if (!visible.length)
      tree.items.splice(
        3,
        0,
        new Item("No verified Maven publications in this selection"),
      );
    tree.refresh();
    status.text = "$(account) " + organization.name;
  }
  let activeResolver: MavenResolver | undefined,
    resolverConfig = "";
  function resolver() {
    const s = vscode.workspace.getConfiguration("zernovaKernelStudio");
    const options = {
      executable: s.get<string>("mavenExecutable") || "",
      storage: path.join(context.globalStorageUri.fsPath, "exchange-maven"),
      javaHome: s.get<string>("mavenJavaHome") || undefined,
      truststore: s.get<string>("mavenTruststore") || undefined,
    };
    const fingerprint = JSON.stringify(options);
    if (!activeResolver || fingerprint !== resolverConfig) {
      activeResolver = new MavenResolver({
        ...options,
        log: (m) => output.appendLine(m),
      });
      resolverConfig = fingerprint;
    }
    return activeResolver;
  }
  const maven = () => resolver();
  async function currentSpecification(lock: LockedSpecification) {
    const c = await getClient();
    if (c.origin !== lock.platformOrigin)
      throw new Error(
        "Select the Platform installation recorded in this project before resolving dependencies.",
      );
    const selected = (await c.exchange(lock.organizationId)).find(
      (s) => s.id === lock.versionId,
    );
    if (!selected)
      throw new Error(
        "The locked version is no longer available to this account.",
      );
    const current = lockSpecification(c.origin, selected);
    if (
      current.sha256 !== lock.sha256 ||
      current.groupId !== lock.groupId ||
      current.artifactId !== lock.artifactId ||
      current.version !== lock.version ||
      current.type !== lock.type
    )
      throw new Error(
        "Approved Exchange version no longer matches the project lock.",
      );
    return c;
  }
  async function resolveBytes(lock: LockedSpecification) {
    const c = await currentSpecification(lock);
    return maven().resolve(lock, await c.maven(lock.organizationId));
  }
  async function add(spec?: Specification) {
    if (!vscode.workspace.isTrusted)
      throw new Error("Trust this workspace before adding a dependency.");
    if (!spec) {
      await refresh();
      return;
    }
    const project = projects.current();
    if (!project) throw new Error("Select or open a ZERNOVA project first.");
    const c = await getClient(),
      lock = lockSpecification(c.origin, spec),
      saved = await readLocks(project);
    const prior = saved.specifications.find(
      (l) => l.groupId === lock.groupId && l.artifactId === lock.artifactId,
    );
    if (prior) {
      if (prior.versionId === lock.versionId && prior.sha256 === lock.sha256) {
        void vscode.window.showInformationMessage(
          "This exact API specification is already a project dependency.",
        );
        return;
      }
      throw new Error(
        "This API already has a locked version. An explicit upgrade review is required.",
      );
    }
    const pomPath = await physical(project.root, "pom.xml"),
      resource = await physical(project.root, lock.resource),
      lockPath = await physical(project.root, lockName);
    const doc = await vscode.workspace.openTextDocument(
      vscode.Uri.file(pomPath),
    );
    if (doc.isDirty)
      throw new Error(
        "Save the project POM before reviewing a dependency change.",
      );
    if (
      await fs.stat(resource).then(
        () => true,
        () => false,
      )
    )
      throw new Error(
        "The target specification resource already exists; it will not be overwritten.",
      );
    const before = doc.getText(),
      version = doc.version,
      updated = addDependency(before, lock);
    const decision = await vscode.window.showInformationMessage(
      `${project.name}\nAdd ${lock.groupId}:${lock.artifactId}:${lock.version}:${lock.type}\nSHA-256 ${lock.sha256}\nChanges: pom.xml, ${lockName}, ${lock.resource}. API specifications are non-executable resources; Runtime generation is separate.`,
      { modal: true },
      "Resolve and add dependency",
    );
    if (decision !== "Resolve and add dependency") return;
    const bytes = await resolveBytes(lock);
    await physical(project.root, lock.resource);
    await physical(project.root, lockName);
    if (
      doc.version !== version ||
      doc.isDirty ||
      doc.getText() !== before ||
      JSON.stringify(await readLocks(project)) !== JSON.stringify(saved)
    )
      throw new Error(
        "Project changed during resolution. Review the dependency again.",
      );
    const edit = new vscode.WorkspaceEdit();
    if (updated !== before)
      edit.replace(
        doc.uri,
        new vscode.Range(doc.positionAt(0), doc.positionAt(before.length)),
        updated,
      );
    const lockUri = vscode.Uri.file(lockPath);
    const lockExists = await fs.stat(lockPath).then(
      () => true,
      () => false,
    );
    if (lockExists) {
      const lockDoc = await vscode.workspace.openTextDocument(lockUri);
      if (lockDoc.isDirty)
        throw new Error(
          "Save Exchange lock metadata before adding a dependency.",
        );
      edit.replace(
        lockUri,
        new vscode.Range(
          lockDoc.positionAt(0),
          lockDoc.positionAt(lockDoc.getText().length),
        ),
        JSON.stringify(
          { ...saved, specifications: [...saved.specifications, lock] },
          null,
          2,
        ) + "\n",
      );
    } else {
      edit.createFile(lockUri, { overwrite: false });
      edit.insert(
        lockUri,
        new vscode.Position(0, 0),
        JSON.stringify({ ...saved, specifications: [lock] }, null, 2) + "\n",
      );
    }
    const uri = vscode.Uri.file(resource);
    edit.createFile(uri, { overwrite: false });
    edit.insert(uri, new vscode.Position(0, 0), bytes.toString("utf8"));
    if (!(await vscode.workspace.applyEdit(edit)))
      throw new Error(
        "The dependency edit was not applied. Recheck the project before retrying.",
      );
    for (const file of [doc.uri, lockUri, uri]) {
      const d = await vscode.workspace.openTextDocument(file);
      if (!(await d.save()))
        throw new Error(
          "A dependency edit remains unsaved. Review the native editor.",
        );
    }
    output.appendLine(
      "Added approved API dependency; downloaded bytes match SHA-256 " +
        lock.sha256,
    );
    void vscode.window.showInformationMessage(
      "API dependency added and verified. The existing offline builder can package it as a resource.",
    );
  }
  async function prepare(project: Project, deps: Dependency[]) {
    const locks = await readLocks(project);
    if (!locks.specifications.length) return undefined;
    const classified = classifyDependencies(deps, locks.specifications);
    const resolverInstance = maven();
    for (const lock of classified.specifications) {
      const file = await physical(project.root, lock.resource);
      const info = await fs.lstat(file);
      if (!info.isFile() || info.size > 128000)
        throw new Error(
          "Locked specification resource is missing or oversized.",
        );
      verifiedBytes(lock, await fs.readFile(file));
      const c = await currentSpecification(lock);
      const bytes = await resolverInstance.resolve(
        lock,
        await c.maven(lock.organizationId),
      );
      verifiedBytes(lock, bytes);
    }
    output.appendLine(
      "Authenticated Exchange dependencies verified for the selected project.",
    );
    return { http: classified.http };
  }
  const safe = (fn: () => Promise<unknown>) => async () => {
    try {
      return await fn();
    } catch (e) {
      void vscode.window.showErrorMessage(
        e instanceof Error ? e.message : "Platform operation failed.",
      );
      return undefined;
    }
  };
  context.subscriptions.push(
    changed,
    tree,
    output,
    status,
    vscode.authentication.registerAuthenticationProvider(
      AUTH,
      "ZERNOVA Platform",
      {
        onDidChangeSessions: changed.event,
        getSessions: async (scopes) => {
          const s = await session();
          return s &&
            (!scopes?.length ||
              (scopes.length === 1 && scopes[0] === s.scopes[0]))
            ? [s]
            : [];
        },
        createSession: async (scopes) => {
          if (
            scopes.length &&
            (scopes.length !== 1 || scopes[0] !== (await getClient()).origin)
          )
            throw new Error(
              "Only the configured Platform connection scope is supported.",
            );
          return signIn();
        },
        removeSession: async () => signOut(),
      },
      { supportsMultipleAccounts: false },
    ),
    vscode.window.registerTreeDataProvider(
      "zernovaKernelStudio.exchange",
      tree,
    ),
    vscode.commands.registerCommand(
      "zernovaKernelStudio.platform.signIn",
      safe(signIn),
    ),
    vscode.commands.registerCommand(
      "zernovaKernelStudio.platform.signOut",
      safe(signOut),
    ),
    vscode.commands.registerCommand(
      "zernovaKernelStudio.platform.browse",
      safe(refresh),
    ),
    vscode.commands.registerCommand(
      "zernovaKernelStudio.platform.selectOrganization",
      safe(async () => {
        organization = undefined;
        workspace = undefined;
        await refresh();
      }),
    ),
    vscode.commands.registerCommand(
      "zernovaKernelStudio.platform.addDependency",
      async (spec?: Specification) => {
        try {
          await add(spec);
        } catch (e) {
          void vscode.window.showErrorMessage(
            e instanceof Error ? e.message : "Dependency operation failed.",
          );
        }
      },
    ),
    vscode.commands.registerCommand(
      "zernovaKernelStudio.platform.resolve",
      safe(async () => {
        const project = projects.current();
        if (!project) throw new Error("Select a project first.");
        const { parseDependencies } = await import("./dependencies");
        await prepare(
          project,
          parseDependencies(
            await fs.readFile(path.join(project.root, "pom.xml"), "utf8"),
          ),
        );
        void vscode.window.showInformationMessage(
          "Project API dependencies authenticated and verified.",
        );
      }),
    ),
    vscode.workspace.onDidChangeConfiguration((e) => {
      if (
        e.affectsConfiguration("zernovaKernelStudio.platformUrl") ||
        e.affectsConfiguration("zernovaKernelStudio.platformCertificate")
      ) {
        clientReady = undefined;
        organization = undefined;
        workspace = undefined;
        tree.items = [
          new Item(
            "Connect to configured Platform",
            "zernovaKernelStudio.platform.signIn",
          ),
        ];
        tree.refresh();
      }
    }),
  );
  return { prepare };
}
