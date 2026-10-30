import * as fs from "node:fs/promises";
import path from "node:path";
import { spawn } from "node:child_process";
import { randomUUID, createHash } from "node:crypto";
import type { MavenCredential } from "./client";
import { verifiedBytes, type LockedSpecification } from "./dependencies";
const xml = (s: string) =>
  s.replace(
    /[&<>"']/g,
    (c) =>
      ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&apos;",
      })[c]!,
  );
export function mavenSettings(url: string, authenticated: boolean) {
  const u = new URL(url);
  if (u.protocol !== "https:" || u.username || u.password || u.search || u.hash)
    throw new Error("Maven requires a fixed trusted HTTPS repository.");
  return `<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0">${authenticated ? "<servers><server><id>zernova-exchange</id><username>zernova</username><password>${env.ZERNOVA_MAVEN_TOKEN}</password></server></servers>" : ""}<mirrors><mirror><id>zernova-exchange</id><mirrorOf>*</mirrorOf><url>${xml(url)}</url></mirror></mirrors></settings>\n`;
}
export interface ResolverOptions {
  executable: string;
  storage: string;
  javaHome?: string;
  truststore?: string;
  truststorePassword?: string;
  log?: (message: string) => void;
}
async function privateDir(p: string) {
  await fs.mkdir(p, { recursive: true, mode: 0o700 });
  const stat = await fs.lstat(p);
  if (!stat.isDirectory() || stat.isSymbolicLink())
    throw new Error("Private Maven storage is not a physical directory.");
  await fs.chmod(p, 0o700);
}
export class MavenResolver {
  private queue: Promise<unknown> = Promise.resolve();
  constructor(private readonly options: ResolverOptions) {}
  resolve(
    lock: LockedSpecification,
    credential: MavenCredential,
  ): Promise<Buffer> {
    const task = this.queue.then(() => this.execute(lock, credential));
    this.queue = task.catch(() => {});
    return task;
  }
  private async run(args: string[], cwd: string, token?: string) {
    if (!path.isAbsolute(this.options.executable))
      throw new Error("Configure an absolute Maven executable path.");
    const env = { ...process.env };
    for (const name of [
      "MAVEN_OPTS",
      "MAVEN_ARGS",
      "MAVEN_CONFIG",
      "MAVEN_PROJECTBASEDIR",
      "M2_HOME",
      "MAVEN_HOME",
      "JAVA_TOOL_OPTIONS",
      "JDK_JAVA_OPTIONS",
      "_JAVA_OPTIONS",
      "ZERNOVA_MAVEN_TOKEN",
    ])
      delete env[name];
    env.MAVEN_SKIP_RC = "1";
    if (this.options.javaHome) env.JAVA_HOME = this.options.javaHome;
    if (this.options.truststore) {
      if (
        !path.isAbsolute(this.options.truststore) ||
        /[\r\n"]/.test(this.options.truststore)
      )
        throw new Error("Invalid private Java truststore path.");
      // Java parses quotes in JDK_JAVA_OPTIONS; Maven's shell expands MAVEN_OPTS
      // as words and cannot preserve a truststore path containing spaces.
      env.JDK_JAVA_OPTIONS = `-Djavax.net.ssl.trustStore="${this.options.truststore}" -Djavax.net.ssl.trustStorePassword="${this.options.truststorePassword || "changeit"}"`;
    }
    if (token) env.ZERNOVA_MAVEN_TOKEN = token;
    await new Promise<void>((resolve, reject) => {
      const child = spawn(this.options.executable, args, {
        cwd,
        env,
        stdio: ["ignore", "pipe", "pipe"],
        shell: false,
      });
      let bytes = 0;
      let done = false;
      const captured: Buffer[] = [];
      const timer = setTimeout(() => {
        child.kill("SIGTERM");
        setTimeout(() => child.kill("SIGKILL"), 1500).unref();
      }, 120_000);
      timer.unref();
      const observe = (data: Buffer) => {
        bytes += data.length;
        if (bytes <= 2_000_000) captured.push(Buffer.from(data));
        if (bytes > 2_000_000) child.kill("SIGTERM");
      };
      child.stdout.on("data", observe);
      child.stderr.on("data", observe);
      child.once("error", () => {
        clearTimeout(timer);
        if (!done) {
          done = true;
          reject(
            new Error(
              "Maven could not be started. Verify the selected toolchain.",
            ),
          );
        }
      });
      child.once("close", async (code) => {
        clearTimeout(timer);
        if (done) return;
        done = true;
        let log = Buffer.concat(captured).toString("utf8");
        if (token)
          for (const value of [
            token,
            Buffer.from("zernova:" + token).toString("base64"),
          ])
            log = log.split(value).join("[redacted]");
        log = log.replace(/(Authorization:\s*)[^\r\n]*/gi, "$1[redacted]");
        await fs
          .writeFile(path.join(cwd, "maven-" + randomUUID() + ".log"), log, {
            mode: 0o600,
            flag: "wx",
          })
          .catch(() => {});
        if (code === 0 && bytes <= 2_000_000) resolve();
        else
          reject(
            new Error(
              "Maven resolution failed. No specification was admitted; check authentication, repository availability and the pinned public plugin cache.",
            ),
          );
      });
    });
  }
  private async execute(
    lock: LockedSpecification,
    credential: MavenCredential,
  ): Promise<Buffer> {
    if (
      credential.repository_url !==
        lock.platformOrigin + "/maven/" + lock.organizationId ||
      credential.username !== "zernova" ||
      credential.permission !== "read-only" ||
      credential.expires_at <= Date.now()
    )
      throw new Error("Maven credential does not match this specification.");
    const root = path.resolve(this.options.storage);
    await privateDir(root);
    const cache = path.join(root, "repository");
    await privateDir(cache);
    const job = path.join(root, "jobs", randomUUID());
    await privateDir(job);
    const global = path.join(job, "global.xml"),
      settings = path.join(job, "settings.xml"),
      pom = path.join(job, "pom.xml");
    await fs.writeFile(global, "<settings/>\n", { mode: 0o600, flag: "wx" });
    await fs.writeFile(
      pom,
      '<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion><groupId>com.zernova.tools</groupId><artifactId>exchange-resolver</artifactId><version>1.0.0</version><packaging>pom</packaging></project>\n',
      { mode: 0o600, flag: "wx" },
    );
    const args = [
      "-B",
      "-ntp",
      "-C",
      "--global-settings",
      global,
      "--settings",
      settings,
      "-Dmaven.repo.local=" + cache,
      "-f",
      pom,
    ];
    const marker = path.join(root, "public-plugin-cache.json");
    if (
      !(await fs.stat(marker).then(
        () => true,
        () => false,
      ))
    ) {
      this.options.log?.(
        "Preparing the pinned public Maven resolver plugin. No project coordinates or credentials are sent.",
      );
      await fs.writeFile(
        settings,
        mavenSettings("https://repo.maven.apache.org/maven2", false),
        { mode: 0o600 },
      );
      await this.run(
        [
          ...args,
          "org.apache.maven.plugins:maven-dependency-plugin:3.8.1:help",
          "-Ddetail=false",
        ],
        job,
      );
      const entries: Record<string, string> = {};
      async function walk(dir: string) {
        for (const entry of await fs.readdir(dir, { withFileTypes: true })) {
          const file = path.join(dir, entry.name);
          if (entry.isSymbolicLink())
            throw new Error("Maven tool cache contains a symbolic link.");
          if (entry.isDirectory()) await walk(file);
          else if (/\.(jar|pom)$/.test(entry.name))
            entries[path.relative(cache, file)] = createHash("sha256")
              .update(await fs.readFile(file))
              .digest("hex");
        }
      }
      await walk(cache);
      await fs.writeFile(marker, JSON.stringify(entries, null, 2) + "\n", {
        mode: 0o600,
        flag: "wx",
      });
    }
    const publicFiles = JSON.parse(await fs.readFile(marker, "utf8")) as Record<
      string,
      string
    >;
    for (const [relative, expected] of Object.entries(publicFiles)) {
      if (path.isAbsolute(relative) || relative.split(path.sep).includes(".."))
        throw new Error("Invalid Maven tool cache manifest.");
      const file = path.join(cache, relative);
      if (
        (await fs.lstat(file)).isSymbolicLink() ||
        createHash("sha256")
          .update(await fs.readFile(file))
          .digest("hex") !== expected
      )
        throw new Error("Pinned Maven tool cache integrity failed.");
    }
    // The private phase mirrors every repository to the authenticated gateway.
    // Private coordinates never fall back to Maven Central or inherited global settings.
    const privateCache = path.join(job, "repository");
    await fs.cp(cache, privateCache, {
      recursive: true,
      errorOnExist: true,
      force: false,
    });
    const privateArgs = args.map((a) =>
      a.startsWith("-Dmaven.repo.local=")
        ? "-Dmaven.repo.local=" + privateCache
        : a,
    );
    await fs.writeFile(
      settings,
      mavenSettings(credential.repository_url, true),
      { mode: 0o600 },
    );
    this.options.log?.(
      "Resolving the selected immutable specification through authenticated Exchange Maven access.",
    );
    await this.run(
      [
        ...privateArgs,
        "org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy",
        `-Dartifact=${lock.groupId}:${lock.artifactId}:${lock.version}:${lock.type}`,
        "-Dtransitive=false",
        "-DoutputDirectory=" + path.join(job, "download"),
      ],
      job,
      credential.password,
    );
    const file = path.join(
      job,
      "download",
      `${lock.artifactId}-${lock.version}.${lock.type}`,
    );
    const info = await fs.lstat(file);
    if (
      !info.isFile() ||
      info.isSymbolicLink() ||
      info.nlink !== 1 ||
      info.size > 128000
    )
      throw new Error(
        "Downloaded specification is not an ordinary bounded file.",
      );
    return verifiedBytes(lock, await fs.readFile(file));
  }
}
