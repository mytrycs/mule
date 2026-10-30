import * as vscode from 'vscode';
import * as fs from 'node:fs/promises';
import path from 'node:path';
import { createHash } from 'node:crypto';
import { tmpdir } from 'node:os';
import * as sax from 'sax';

export interface Project { root: string; name: string; groupId: string; artifactId: string; version: string; }
export interface RuntimeSettings {
  schemaVersion: 1; runtimeHome: string; javaHome: string; mavenHome: string; mavenRepository: string;
  offline: boolean; instanceDirectory: string;
}
const PROJECTS_KEY = 'zernovaKernelStudio.projects.v1';
const SETTINGS_KEY = 'zernovaKernelStudio.runtimeProfiles.v1';
const SELECTED_KEY = 'zernovaKernelStudio.selectedProject.v1';
const artifactPattern = /^[A-Za-z][A-Za-z0-9_.-]{0,79}$/;
const groupPattern = /^[A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)+$/;
const versionPattern = /^[0-9][A-Za-z0-9_.-]{0,49}$/;
const xmlEscape = (value: string) => value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/"/g, '&quot;');

export const NEW_INTEGRATION_XML = `<?xml version="1.0" encoding="UTF-8"?>
<mule xmlns="http://www.mulesoft.org/schema/mule/core"
      xmlns:http="http://www.mulesoft.org/schema/mule/http"
      xmlns:doc="http://www.mulesoft.org/schema/mule/documentation"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd http://www.mulesoft.org/schema/mule/http http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd">
  <configuration-properties file="application.properties"/>
  <!-- Create a flow in ZERNOVA Studio. This document is initially empty. -->
</mule>
`;

export function isInside(root: string, file: string): boolean {
  const relative = path.relative(root, file);
  return relative === '' || (!relative.startsWith('..' + path.sep) && relative !== '..' && !path.isAbsolute(relative));
}
export function instancePath(root: string): string {
  return path.join(tmpdir(), `zernova-studio-${process.getuid?.() ?? 'user'}-${createHash('sha256').update(root).digest('hex').slice(0, 16)}`);
}

/** Parse the bounded Maven coordinates without interpreting a POM as executable input. */
export function parseProjectPom(text: string): Pick<Project, 'groupId' | 'artifactId' | 'version'> {
  const parser = sax.parser(true, { xmlns: true });
  const stack: string[] = []; const values: Record<string, string> = {};
  parser.ondoctype = () => { throw new Error('POM DOCTYPE declarations are not supported.'); };
  parser.onopentag = (node: { local?: string; name: string }) => { stack.push(node.local ?? node.name); };
  parser.onclosetag = () => { stack.pop(); };
  parser.ontext = (text: string) => { const key = stack.join('/'); if (['project/groupId', 'project/artifactId', 'project/version'].includes(key)) values[key] = (values[key] ?? '') + text; };
  parser.write(text).close();
  const groupId = values['project/groupId']?.trim() ?? '', artifactId = values['project/artifactId']?.trim() ?? '', version = values['project/version']?.trim() ?? '';
  if (!groupPattern.test(groupId) || !artifactPattern.test(artifactId) || !versionPattern.test(version)) throw new Error('This milestone requires explicit valid Maven groupId, artifactId and version in the project POM (no inherited or expression coordinates).');
  return { groupId, artifactId, version };
}

async function directory(value: string, label: string): Promise<string | undefined> {
  if (!path.isAbsolute(value) || value.includes('\0')) return `${label} must be an absolute path on this extension host.`;
  try { if (!(await fs.stat(value)).isDirectory()) return `${label} is not a directory.`; } catch { return `${label} does not exist on this extension host.`; }
  return undefined;
}
async function runtimeDirectory(value: string): Promise<string | undefined> {
  const invalid = await directory(value, 'Runtime Foundation directory'); if (invalid) return invalid;
  for (const rel of ['bin/zernova', 'scripts/build-app.sh', 'var/prepared.json', 'var/prepared-manifest.json']) {
    try { if (!(await fs.stat(path.join(value, rel))).isFile()) throw new Error(); }
    catch { return `Missing ${rel}. Extract and prepare ZERNOVA Runtime Foundation first; no runtime binaries are included in the VSIX.`; }
  }
  return undefined;
}

export class ProjectManager implements vscode.Disposable {
  private readonly changed = new vscode.EventEmitter<void>();
  readonly onDidChange = this.changed.event;
  private projects = new Map<string, Project>();
  private watchers = new Map<string, vscode.FileSystemWatcher>();
  private selected: string | undefined;
  constructor(private readonly context: vscode.ExtensionContext) { this.selected = context.workspaceState.get<string>(SELECTED_KEY); }
  dispose(): void { for (const watcher of this.watchers.values()) watcher.dispose(); this.changed.dispose(); }
  list(): Project[] { return [...this.projects.values()]; }
  current(): Project | undefined { return this.selected ? this.projects.get(this.selected) : undefined; }
  rootForUri(uri: vscode.Uri): vscode.Uri | undefined {
    if (uri.scheme !== 'file') return undefined;
    const candidates = this.list().filter(p => isInside(p.root, uri.fsPath)).sort((a, b) => b.root.length - a.root.length);
    return candidates[0] ? vscode.Uri.file(candidates[0].root) : undefined;
  }
  projectForUri(uri?: vscode.Uri): Project | undefined { const root = uri ? this.rootForUri(uri)?.fsPath : undefined; return root ? this.projects.get(root) : undefined; }
  nameForUri(uri: vscode.Uri): string { return this.projectForUri(uri)?.name ?? path.basename(path.dirname(uri.fsPath)); }
  getSettings(project: Project): RuntimeSettings | undefined {
    return this.context.globalState.get<Record<string, RuntimeSettings>>(SETTINGS_KEY, {})[project.root];
  }
  async saveSettings(project: Project, settings: RuntimeSettings): Promise<void> {
    const all = this.context.globalState.get<Record<string, RuntimeSettings>>(SETTINGS_KEY, {});
    await this.context.globalState.update(SETTINGS_KEY, { ...all, [project.root]: settings }); this.changed.fire();
  }
  async initialize(): Promise<void> {
    const paths = new Set<string>(this.context.globalState.get<string[]>(PROJECTS_KEY, []));
    for (const folder of vscode.workspace.workspaceFolders ?? []) if (folder.uri.scheme === 'file') paths.add(folder.uri.fsPath);
    for (const candidate of paths) { try { await this.load(candidate, false); } catch { /* Non-project and unavailable previous paths stay out of navigation. */ } }
    if (!this.current()) this.selected = this.list()[0]?.root;
    this.changed.fire();
  }
  async load(candidate: string, select = true): Promise<Project> {
    const root = await fs.realpath(candidate);
    const coords = parseProjectPom(await fs.readFile(path.join(root, 'pom.xml'), 'utf8'));
    const descriptor = JSON.parse(await fs.readFile(path.join(root, 'mule-artifact.json'), 'utf8'));
    if (descriptor.requiredProduct !== 'MULE') throw new Error('Expected mule-artifact.json with requiredProduct: MULE. The old native Runtime format is not supported.');
    if (!(await fs.stat(path.join(root, 'src/main/mule'))).isDirectory()) throw new Error('Expected src/main/mule directory.');
    const project = { root, name: path.basename(root), ...coords };
    this.projects.set(root, project);
    if (!this.watchers.has(root)) {
      const watcher = vscode.workspace.createFileSystemWatcher(new vscode.RelativePattern(vscode.Uri.file(root), 'src/main/mule/*.xml'));
      watcher.onDidCreate(() => this.changed.fire()); watcher.onDidDelete(() => this.changed.fire()); this.watchers.set(root, watcher);
    }
    if (select) { this.selected = root; await this.context.workspaceState.update(SELECTED_KEY, root); }
    await this.context.globalState.update(PROJECTS_KEY, this.list().map(p => p.root)); this.changed.fire();
    return project;
  }
  async select(uri?: vscode.Uri, forcePick = false): Promise<Project | undefined> {
    const contextual = uri ? this.projectForUri(uri) : undefined;
    if (contextual) { this.selected = contextual.root; await this.context.workspaceState.update(SELECTED_KEY, this.selected); this.changed.fire(); return contextual; }
    const active = this.current(); if (active && !forcePick) return active;
    const selected = await vscode.window.showQuickPick(this.list().map(project => ({ label: project.name, description: project.root, project })), { title: 'Select ZERNOVA Project' });
    if (selected) { this.selected = selected.project.root; await this.context.workspaceState.update(SELECTED_KEY, this.selected); this.changed.fire(); }
    else if (!this.list().length) void vscode.window.showInformationMessage('Create or open a ZERNOVA project first.');
    return selected?.project;
  }
  async openProject(): Promise<Project | undefined> {
    const root = await vscode.window.showInputBox({ title: 'Open ZERNOVA Project', prompt: 'Project directory containing pom.xml and mule-artifact.json', ignoreFocusOut: true, validateInput: value => directory(value, 'Project directory') });
    return root === undefined ? undefined : this.load(root);
  }
  async promptSettings(projectRoot: string, previous?: RuntimeSettings): Promise<RuntimeSettings | undefined> {
    const runtimeHome = await vscode.window.showInputBox({ title: 'Configure ZERNOVA Runtime (1/5)', prompt: 'Runtime Foundation directory on this extension host', value: previous?.runtimeHome, ignoreFocusOut: true, validateInput: runtimeDirectory });
    if (runtimeHome === undefined) return;
    const prepared = JSON.parse(await fs.readFile(path.join(runtimeHome, 'var/prepared.json'), 'utf8')) as Record<string, unknown>;
    const javaHome = await vscode.window.showInputBox({ title: 'Configure ZERNOVA Runtime (2/5)', prompt: 'Java 17 JDK directory', value: previous?.javaHome ?? (typeof prepared.java_home === 'string' ? prepared.java_home : ''), ignoreFocusOut: true, validateInput: value => directory(value, 'JDK directory') });
    if (javaHome === undefined) return;
    const mavenHome = await vscode.window.showInputBox({ title: 'Configure ZERNOVA Runtime (3/5)', prompt: 'Apache Maven 3.9.11 directory', value: previous?.mavenHome ?? (typeof prepared.maven_home === 'string' ? prepared.maven_home : ''), ignoreFocusOut: true, validateInput: value => directory(value, 'Maven directory') });
    if (mavenHome === undefined) return;
    const mavenRepository = await vscode.window.showInputBox({ title: 'Configure ZERNOVA Runtime (4/5)', prompt: 'Maven dependency cache directory', value: previous?.mavenRepository ?? path.join(runtimeHome, 'var/maven-repository'), ignoreFocusOut: true, validateInput: value => !path.isAbsolute(value) || value.includes('\0') ? 'Use an absolute cache path on this extension host.' : undefined });
    if (mavenRepository === undefined) return;
    const mode = await vscode.window.showQuickPick([{ label: 'Offline — use prepared cache', offline: true }, { label: 'Online — allow Maven downloads', offline: false }], { title: 'Configure ZERNOVA Runtime (5/5)', placeHolder: 'Maven dependency mode', ignoreFocusOut: true });
    if (!mode) return;
    return { schemaVersion: 1, runtimeHome: await fs.realpath(runtimeHome), javaHome: await fs.realpath(javaHome), mavenHome: await fs.realpath(mavenHome), mavenRepository: path.resolve(mavenRepository), offline: mode.offline, instanceDirectory: instancePath(projectRoot) };
  }
  async configure(project: Project): Promise<boolean> {
    const previous = this.getSettings(project);
    const settings = await this.promptSettings(project.root, previous);
    if (!settings) return false;
    if (previous && JSON.stringify(previous) !== JSON.stringify(settings)) {
      // Existing instances must first be stopped using the settings that own them.
      try {
        const live = JSON.parse(await fs.readFile(path.join(previous.instanceDirectory, 'state.json'), 'utf8'));
        if (live.pid) {
          const raw = await fs.readFile(`/proc/${Number(live.pid)}/stat`, 'utf8').catch(() => '');
          const fields = raw.slice(raw.lastIndexOf(')') + 2).split(' ');
          const command = (await fs.readFile(`/proc/${Number(live.pid)}/cmdline`).catch(() => Buffer.alloc(0))).toString().split('\0');
          if (fields[19] === live.start_ticks && fields[0] !== 'Z' && command.includes(`-Dzernova.instance.token=${live.token}`) && command.includes(`-Dmule.base=${path.join(previous.instanceDirectory, 'runtime')}`)) throw new Error('Stop this project runtime before changing its tool/runtime selections.');
        }
      } catch (error) { if (!(error instanceof Error && 'code' in error && (error as NodeJS.ErrnoException).code === 'ENOENT')) throw error; }
      if (previous.runtimeHome !== settings.runtimeHome) {
        const marker = path.join(previous.instanceDirectory, 'studio-owner.json');
        try {
          const owner = JSON.parse(await fs.readFile(marker, 'utf8'));
          if (owner.schemaVersion !== 1 || owner.projectRoot !== project.root || owner.runtimeHome !== previous.runtimeHome) throw new Error('Existing runtime ownership marker does not match this project.');
          await fs.writeFile(marker, JSON.stringify({ ...owner, runtimeHome: settings.runtimeHome }, null, 2) + '\n');
        } catch (error) { if ((error as NodeJS.ErrnoException).code !== 'ENOENT') throw error; }
      }
    }
    await this.saveSettings(project, settings); return true;
  }
  async createProject(): Promise<Project | undefined> {
    const parent = await vscode.window.showInputBox({ title: 'New ZERNOVA Project (1/6)', prompt: 'Parent directory (absolute path)', value: vscode.workspace.workspaceFolders?.find(f => f.uri.scheme === 'file')?.uri.fsPath, ignoreFocusOut: true, validateInput: value => directory(value, 'Parent directory') });
    if (parent === undefined) return;
    const name = await vscode.window.showInputBox({ title: 'New ZERNOVA Project (2/6)', prompt: 'Project folder name', value: 'zernova-owner-test', ignoreFocusOut: true, validateInput: async value => {
      if (!artifactPattern.test(value) || value.includes('..')) return 'Use a letter followed by letters, numbers, dots, hyphens or underscores; no parent-path segments.';
      try { await fs.lstat(path.join(parent, value)); return 'Destination already exists. Choose a new folder; existing content will not be overwritten.'; } catch (error) { if ((error as NodeJS.ErrnoException).code !== 'ENOENT') return 'Destination cannot be checked.'; }
      return undefined;
    } });
    if (name === undefined) return;
    const groupId = await vscode.window.showInputBox({ title: 'New ZERNOVA Project (3/6)', prompt: 'Maven groupId', value: 'io.zernova', ignoreFocusOut: true, validateInput: v => groupPattern.test(v) ? undefined : 'Use a dotted Maven group such as io.zernova.' });
    if (groupId === undefined) return;
    const artifactId = await vscode.window.showInputBox({ title: 'New ZERNOVA Project (4/6)', prompt: 'Maven artifactId', value: name, ignoreFocusOut: true, validateInput: v => artifactPattern.test(v) && !v.includes('..') ? undefined : 'Use a Maven artifact name without spaces or parent-path segments.' });
    if (artifactId === undefined) return;
    const version = await vscode.window.showInputBox({ title: 'New ZERNOVA Project (5/6)', prompt: 'Maven version', value: '1.0.0', ignoreFocusOut: true, validateInput: v => versionPattern.test(v) ? undefined : 'Use an explicit version such as 1.0.0 or 1.0.0-SNAPSHOT.' });
    if (version === undefined) return;
    const configure = await vscode.window.showQuickPick([{ label: 'Configure runtime now', now: true }, { label: 'Configure later — edit only', now: false }], { title: 'New ZERNOVA Project (6/6)', placeHolder: 'Project runtime and tools', ignoreFocusOut: true });
    if (!configure) return;
    const canonicalParent = await fs.realpath(parent), root = path.join(canonicalParent, name);
    const settings = configure.now ? await this.promptSettings(root) : undefined;
    if (configure.now && !settings) return;
    await fs.access(canonicalParent, fs.constants.W_OK);
    await fs.mkdir(root); // Exclusive creation: EEXIST is never overwritten.
    try {
      for (const dir of ['src/main/mule', 'src/main/java', 'src/main/resources', 'src/test']) await fs.mkdir(path.join(root, dir), { recursive: true });
      const files = projectFiles({ root, name, groupId, artifactId, version });
      for (const [relative, content] of Object.entries(files)) await fs.writeFile(path.join(root, relative), content, { flag: 'wx' });
    } catch (error) { await fs.rm(root, { recursive: true, force: true }); throw error; }
    const project = await this.load(root); if (settings) await this.saveSettings(project, settings);
    void vscode.window.showInformationMessage(`Created ${name}. src/main/mule is empty. Use ZERNOVA: New Integration XML, then Create Flow before building.`);
    return project;
  }
  async newIntegration(project: Project): Promise<vscode.Uri | undefined> {
    const filename = await vscode.window.showInputBox({ title: 'New Integration XML', prompt: 'XML filename inside src/main/mule', value: 'hello.xml', ignoreFocusOut: true, validateInput: async value => {
      if (!/^[A-Za-z][A-Za-z0-9_.-]{0,100}\.xml$/.test(value) || value.includes('..')) return 'Use a filename such as hello.xml; directories and parent-path segments are not allowed.';
      try { await fs.lstat(path.join(project.root, 'src/main/mule', value)); return 'This file already exists. Open it from Projects.'; } catch (error) { return (error as NodeJS.ErrnoException).code === 'ENOENT' ? undefined : 'Unable to check destination.'; }
    } });
    if (filename === undefined) return;
    const directory = await fs.realpath(path.join(project.root, 'src/main/mule'));
    if (!isInside(project.root, directory)) throw new Error('The Mule source directory resolves outside this project.');
    const uri = vscode.Uri.file(path.join(directory, filename));
    await fs.writeFile(uri.fsPath, NEW_INTEGRATION_XML, { flag: 'wx' }); this.changed.fire(); return uri;
  }
  refresh(): void { this.changed.fire(); }
}

export function projectFiles(p: Project): Record<string, string> {
  return {
    'pom.xml': `<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>${xmlEscape(p.groupId)}</groupId>
  <artifactId>${xmlEscape(p.artifactId)}</artifactId>
  <version>${xmlEscape(p.version)}</version>
  <packaging>mule-application</packaging>
  <name>${xmlEscape(p.name)}</name>
  <properties><project.build.sourceEncoding>UTF-8</project.build.sourceEncoding><maven.compiler.source>17</maven.compiler.source><maven.compiler.target>17</maven.compiler.target></properties>
  <build><plugins><plugin><groupId>org.mule.tools.maven</groupId><artifactId>mule-maven-plugin</artifactId><version>4.10.3</version><extensions>true</extensions><configuration><classifier>mule-application</classifier><runtimeVersion>4.12.0</runtimeVersion></configuration></plugin></plugins></build>
  <dependencies><dependency><groupId>org.mule.connectors</groupId><artifactId>mule-http-connector</artifactId><version>1.12.2</version><classifier>mule-plugin</classifier></dependency></dependencies>
  <repositories><repository><id>mulesoft-public</id><url>https://repository.mulesoft.org/nexus/content/repositories/public/</url></repository></repositories>
  <pluginRepositories><pluginRepository><id>mulesoft-public</id><url>https://repository.mulesoft.org/nexus/content/repositories/public/</url></pluginRepository></pluginRepositories>
</project>
`,
    'mule-artifact.json': JSON.stringify({ minMuleVersion: '4.9.0', requiredProduct: 'MULE', javaSpecificationVersions: ['17'], redeploymentEnabled: false }, null, 2) + '\n',
    'src/main/resources/application.properties': '# Project resource defaults. Listener configuration is edited in the integration XML.\n',
    'src/main/resources/log4j2.xml': `<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
  <Appenders>
    <RollingFile name="ApplicationFile" fileName="\${sys:mule.home}/logs/mule-app-${xmlEscape(p.artifactId)}.log" filePattern="\${sys:mule.home}/logs/mule-app-${xmlEscape(p.artifactId)}.%i.log">
      <PatternLayout pattern="%d{ISO8601} %-5p [%t] %c: %m%n"/>
      <Policies><SizeBasedTriggeringPolicy size="10 MB"/></Policies><DefaultRolloverStrategy max="5"/>
    </RollingFile>
  </Appenders>
  <Loggers><AsyncRoot level="INFO"><AppenderRef ref="ApplicationFile"/></AsyncRoot></Loggers>
</Configuration>
`,
    '.gitignore': 'target/\n*.log\n.DS_Store\n',
    'README.md': `# ${p.name}\n\nCreated by ZERNOVA Studio 0.2.0 for ZERNOVA Runtime 0.1.0-preview.1 (Mule Kernel 4.12.0, Java 17).\n\nThe src/main/mule directory is intentionally empty. Use **ZERNOVA: New Integration XML**, then **Create Flow** and add HTTP Listener plus core Logger. Save, configure the project runtime/tools, then use **ZERNOVA: Build and Run Application**.\n\nRuntime paths and credentials are not committed here. Configure each project separately on its extension host. Runtime execution is currently supported only on Linux x86-64.\n\nMule Maven Plugin 4.10.3 requests target 4.12.0; the Phase 1 contract records its internal framework as 4.13.0. Real execution must still be checked against Kernel 4.12.0. HTTP connector 1.12.2 is resolved normally by Maven.\n\nApplication logs use the runtime app log path and INFO root threshold. TRACE/DEBUG Logger messages are subject to that logging threshold. No runtime or connector binaries are included in this source project.\n`,
  };
}
