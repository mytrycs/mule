import * as vscode from 'vscode';
import * as fs from 'node:fs/promises';
import path from 'node:path';
import { createHash } from 'node:crypto';
import net from 'node:net';
import { ProjectManager, type Project, type RuntimeSettings, isInside, instancePath } from './projects';
import { parseIntegration } from '../shared/xml';
import type { RuntimeAction } from '../shared/realProtocol';
import { runProcess, type ProcessResult } from './process';
import { parser as saxParser } from 'sax';
import { assertRuntimeIdentity, parseRuntimeStatus, type RuntimeStatus } from '../shared/runtimeContract';

export interface RuntimeViewState {
  schemaVersion: 1; projectRoot: string; projectName: string; configured: boolean; phase: string;
  busy: boolean; process: string; application: string; endpoint: string; lastError: string;
  buildOutput: string; applicationLogs: string; runtimeOutput: string;
  artifact?: string; artifactSha256?: string; port?: number; instanceDirectory?: string;
  evidenceDirectory?: string; offline?: boolean; lastUpdated?: string;
}
interface Built { artifact: string; sha256: string; sourceHash: string; port: number; }
class OperationTimeoutError extends Error { override name = 'OperationTimeoutError'; }
const hash = (value: string | Buffer) => createHash('sha256').update(value).digest('hex');
const MAX_OUTPUT = 250000;
const cleanEnv = (): NodeJS.ProcessEnv => {
  const env = { ...process.env };
  for (const key of ['JAVA_TOOL_OPTIONS', 'JDK_JAVA_OPTIONS', '_JAVA_OPTIONS', 'CLASSPATH', 'MULE_HOME', 'MULE_BASE', 'ZERNOVA_WORK_DIR', 'ZERNOVA_HTTP_PORT', 'ZERNOVA_HEAP_MB', 'ZERNOVA_MAVEN_SETTINGS', 'ZERNOVA_MAVEN_REPO', 'ZERNOVA_MAVEN', 'ZERNOVA_JAVA_HOME']) delete env[key];
  return env;
};

export async function availablePort(port: number): Promise<void> {
  await new Promise<void>((resolve, reject) => {
    const server = net.createServer(); server.unref();
    server.once('error', error => reject(new Error(`Port ${port} on 127.0.0.1 is unavailable: ${error.message}. Choose another Listener port or stop the process you own; Studio will not kill it.`)));
    server.listen({ host: '127.0.0.1', port, exclusive: true }, () => server.close(error => error ? reject(error) : resolve()));
  });
}
export function assertSupportedHost(platform = process.platform, arch = process.arch): void {
  if (platform !== 'linux' || arch !== 'x64') throw new Error(`Runtime execution is supported here only on Linux x86-64; this extension host is ${platform}/${arch}. Editing and Design Preview remain usable. On a Mac use an authorized Linux extension host; a Mac-to-Linux route has not been verified by this release.`);
}

export class RuntimeManager implements vscode.Disposable {
  private readonly changed = new vscode.EventEmitter<string>();
  readonly onDidChange = this.changed.event;
  private readonly states = new Map<string, RuntimeViewState>();
  private readonly operations = new Map<string, AbortController>();
  private readonly runMutations = new Set<string>();
  private readonly buildChannel = vscode.window.createOutputChannel('ZERNOVA Build');
  private readonly runtimeChannel = vscode.window.createOutputChannel('ZERNOVA Runtime');
  private readonly logChannel = vscode.window.createOutputChannel('ZERNOVA Application Logs');
  private readonly diagnostics = vscode.languages.createDiagnosticCollection('zernovaKernelStudio.build');
  private readonly records = new Map<string, string>();
  constructor(private readonly context: vscode.ExtensionContext, private readonly projects: ProjectManager) {}
  dispose(): void { for (const op of this.operations.values()) op.abort(); this.changed.dispose(); this.buildChannel.dispose(); this.runtimeChannel.dispose(); this.logChannel.dispose(); this.diagnostics.dispose(); }
  state(project: Project): RuntimeViewState {
    const settings = this.projects.getSettings(project);
    let state = this.states.get(project.root);
    if (!state) {
      state = { schemaVersion: 1, projectRoot: project.root, projectName: project.name, configured: !!settings,
        phase: settings ? 'Status not checked' : 'Not configured', busy: false, process: 'UNKNOWN', application: 'NOT_CHECKED', endpoint: 'UNVERIFIED', lastError: '', buildOutput: '', applicationLogs: '', runtimeOutput: '' };
      this.states.set(project.root, state);
    }
    state.configured = !!settings; state.offline = settings?.offline; state.instanceDirectory = settings?.instanceDirectory;
    return { ...state };
  }
  stateForUri(uri: vscode.Uri): RuntimeViewState | undefined { const project = this.projects.projectForUri(uri); return project ? this.state(project) : undefined; }
  private update(project: Project, patch: Partial<RuntimeViewState>): void {
    this.states.set(project.root, { ...this.state(project), ...patch, lastUpdated: new Date().toISOString() }); this.changed.fire(project.root);
  }
  private append(project: Project, stream: 'buildOutput' | 'runtimeOutput', text: string): void {
    const prior = this.state(project)[stream]; this.update(project, { [stream]: (prior + text).slice(-MAX_OUTPUT) });
    (stream === 'buildOutput' ? this.buildChannel : this.runtimeChannel).append(text);
  }
  private environment(settings: RuntimeSettings): NodeJS.ProcessEnv {
    return { ...cleanEnv(), JAVA_HOME: settings.javaHome, ZERNOVA_JAVA_HOME: settings.javaHome,
      ZERNOVA_MAVEN: path.join(settings.mavenHome, 'bin/mvn'), ZERNOVA_MAVEN_REPO: settings.mavenRepository,
      ZERNOVA_WORK_DIR: settings.instanceDirectory, PYTHONDONTWRITEBYTECODE: '1' };
  }
  private settings(project: Project): RuntimeSettings {
    const settings = this.projects.getSettings(project);
    if (!settings) throw new Error('Runtime not configured for this project. Run ZERNOVA: Configure Project Runtime and Tools.');
    if (settings.schemaVersion !== 1 || settings.instanceDirectory !== instancePath(project.root) || isInside(project.root, settings.instanceDirectory)) throw new Error('Invalid project runtime profile. Configure this project again; runtime state must stay outside the source folder.');
    return settings;
  }
  private trust(): void { if (!vscode.workspace.isTrusted) throw new Error('Workspace trust is required before executing tools, building or running applications. Review and trust this workspace first.'); }
  private async ownership(project: Project, settings: RuntimeSettings): Promise<void> {
    const directory = settings.instanceDirectory;
    const marker = path.join(directory, 'studio-owner.json');
    try {
      const info = await fs.lstat(directory);
      if (!info.isDirectory() || info.isSymbolicLink()) throw new Error('Runtime instance directory is not a regular owned directory.');
      if (process.getuid && info.uid !== process.getuid()) throw new Error('Runtime instance directory belongs to another user.');
      const owner = JSON.parse(await fs.readFile(marker, 'utf8'));
      if (owner.schemaVersion !== 1 || owner.projectRoot !== project.root || owner.runtimeHome !== settings.runtimeHome) throw new Error('This instance belongs to a different project/runtime. Studio will not reuse or stop it.');
    } catch (error) {
      if ((error as NodeJS.ErrnoException).code !== 'ENOENT') throw error;
      const exists = await fs.stat(directory).then(() => true, () => false);
      if (exists) throw new Error('An unrecognized runtime directory already exists. Studio will not claim its processes. Move only your stopped private instance or use a separate project.');
      await fs.mkdir(directory, { mode: 0o700 });
      await fs.writeFile(marker, JSON.stringify({ schemaVersion: 1, projectRoot: project.root, runtimeHome: settings.runtimeHome }, null, 2) + '\n', { flag: 'wx', mode: 0o600 });
    }
  }
  private async executable(file: string, label: string): Promise<void> {
    try { if (!(await fs.stat(file)).isFile()) throw new Error('Not a file'); await fs.access(file, fs.constants.X_OK); }
    catch { throw new Error(`${label} is missing or not executable: ${file}. Configure this project's tools again.`); }
  }
  private async command(project: Project, settings: RuntimeSettings, executable: string, args: string[], timeoutMs: number, stream: 'buildOutput' | 'runtimeOutput', signal?: AbortSignal, allowedCodes = [0]): Promise<ProcessResult> {
    this.append(project, stream, '\n> ' + JSON.stringify({ executable, args, cwd: project.root }) + '\n');
    const result = await runProcess({ executable, args, cwd: project.root, env: this.environment(settings), timeoutMs, signal, onOutput: text => this.append(project, stream, text) });
    const recordDirectory = this.records.get(project.root);
    if (recordDirectory) {
      const filename = `${Date.now()}-${Math.random().toString(16).slice(2, 8)}.json`;
      await fs.writeFile(path.join(recordDirectory, filename), JSON.stringify(result, null, 2) + '\n');
    }
    if (result.cancelled) throw new Error('Operation cancelled. Its tool process group was terminated; runtime cleanup is reported separately.');
    if (result.timedOut) throw new OperationTimeoutError(`Operation timed out after ${Math.round(timeoutMs / 1000)} seconds. Inspect ZERNOVA ${stream === 'buildOutput' ? 'Build' : 'Runtime'} output.`);
    if (result.exitCode === null || !allowedCodes.includes(result.exitCode)) {
      let detail = (result.stderr || result.stdout).trim().slice(-5000);
      let runtimeTimedOut = false;
      try { const parsed = JSON.parse(result.stdout); if (typeof parsed.error === 'string') { detail = parsed.error; runtimeTimedOut = parsed.status === 'ERROR' && /timed out|timeout/i.test(parsed.error); } } catch { /* Maven text retains its diagnostic. */ }
      if (runtimeTimedOut) throw new OperationTimeoutError(`Runtime command exited ${result.exitCode}: ${detail}`);
      throw new Error(`Command exited ${result.exitCode ?? result.signal}: ${detail || 'No diagnostic output.'}`);
    }
    return result;
  }
  private async cli(project: Project, settings: RuntimeSettings, args: string[], signal?: AbortSignal, allowedCodes = [0], timeoutMs = 120000): Promise<Record<string, unknown>> {
    const result = await this.command(project, settings, 'python3', [path.join(settings.runtimeHome, 'bin/zernova'), ...args, '--json'], timeoutMs, 'runtimeOutput', signal, allowedCodes);
    try { const parsed = JSON.parse(result.stdout); if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) throw new Error(); return parsed as Record<string, unknown>; }
    catch { throw new Error('Runtime returned malformed JSON. Verify the selected Runtime Foundation contract; raw output is retained.'); }
  }
  private async validateTools(project: Project, settings: RuntimeSettings, signal?: AbortSignal): Promise<void> {
    assertSupportedHost(); this.trust();
    for (const [file, label] of [[path.join(settings.javaHome, 'bin/java'), 'Java'], [path.join(settings.javaHome, 'bin/javac'), 'JDK compiler'], [path.join(settings.mavenHome, 'bin/mvn'), 'Maven']] as const) await this.executable(file, label);
    for (const file of ['bin/zernova', 'scripts/build-app.sh', 'var/prepared.json']) await fs.access(path.join(settings.runtimeHome, file));
    await this.ownership(project, settings);
    const python = await this.command(project, settings, 'python3', ['-c', 'import sys,os,signal,json; assert sys.version_info >= (3,10), "Python 3.10+ required"; fd=os.pidfd_open(os.getpid()); os.close(fd); assert hasattr(signal,"pidfd_send_signal"); print(json.dumps({"python":sys.version,"pidfd":True}))'], 15000, 'runtimeOutput', signal);
    if (!JSON.parse(python.stdout).pidfd) throw new Error('Linux Python pidfd support is required.');
    const bash = await this.command(project, settings, 'bash', ['--version'], 10000, 'runtimeOutput', signal);
    if (!bash.stdout.includes('GNU bash')) throw new Error('A working Bash installation is required.');
    const mvn = await this.command(project, settings, path.join(settings.mavenHome, 'bin/mvn'), ['-version'], 15000, 'runtimeOutput', signal);
    if (!/Apache Maven 3\.9\.11(?:\s|$)/.test(mvn.stdout + mvn.stderr)) throw new Error('This preview requires Apache Maven 3.9.11; configure the verified toolchain.');
    const identity = await this.cli(project, settings, ['version'], signal);
    assertRuntimeIdentity(identity);
  }
  private async saveProjectDocuments(project: Project): Promise<void> {
    const dirty = vscode.workspace.textDocuments.filter(doc => doc.uri.scheme === 'file' && isInside(project.root, doc.uri.fsPath) && doc.isDirty);
    if (!dirty.length) return;
    const choice = await vscode.window.showWarningMessage(`Save ${dirty.length} unsaved project document(s) before building the current application?`, { modal: true }, 'Save and Continue');
    if (choice !== 'Save and Continue') throw new Error('Build cancelled: unsaved documents were not saved. No existing artifact was deployed.');
    for (const doc of dirty) if (!await doc.save()) throw new Error(`Could not save ${doc.uri.fsPath}. No application was built or deployed.`);
  }
  private async sourceHash(project: Project): Promise<string> {
    const digest = createHash('sha256');
    const walk = async (directory: string) => {
      for (const entry of (await fs.readdir(directory, { withFileTypes: true })).sort((a, b) => a.name.localeCompare(b.name))) {
        const file = path.join(directory, entry.name);
        if (entry.isSymbolicLink()) throw new Error('Source symlinks are outside this milestone’s reproducible build scope: ' + file);
        if (entry.isDirectory()) await walk(file); else if (entry.isFile()) { digest.update(path.relative(project.root, file)); digest.update(await fs.readFile(file)); }
      }
    };
    for (const name of ['pom.xml', 'mule-artifact.json']) { digest.update(name); digest.update(await fs.readFile(path.join(project.root, name))); }
    await walk(path.join(project.root, 'src')); return digest.digest('hex');
  }
  private async projectPort(project: Project): Promise<number> {
    const source = path.join(project.root, 'src/main/mule');
    const files = (await fs.readdir(source)).filter(name => name.endsWith('.xml'));
    if (!files.length) throw new Error('src/main/mule is empty. Create Integration XML, create a flow, then add and configure an HTTP Listener and Logger before building.');
    const properties: Record<string, string> = {};
    const propertyFiles = new Set<string>();
    const resources = path.join(project.root, 'src/main/resources');
    const resolved = (value: string): string => value.replace(/\$\{([^}]+)\}/g, (_, name: string) => {
      if (!(name in properties)) throw new Error(`Unresolved listener property ${name}. Use a literal or a root .properties resource for this milestone.`); return properties[name];
    });
    const configs = new Map<string, { host: string; port: string; protocol: string }>();
    const refs: string[] = []; let flowCount = 0;
    for (const file of files) {
      const text = await fs.readFile(path.join(source, file), 'utf8'), model = parseIntegration(text);
      const issues = model.diagnostics.filter(d => d.severity === 'error');
      this.diagnostics.set(vscode.Uri.file(path.join(source, file)), issues.map(issue => {
        const before = text.slice(0, issue.start).split('\n'); const start = new vscode.Position(before.length - 1, before[before.length - 1].length);
        const d = new vscode.Diagnostic(new vscode.Range(start, start.translate(0, 1)), issue.message, vscode.DiagnosticSeverity.Error); d.source = 'ZERNOVA build'; return d;
      }));
      if (!model.valid || issues.length) throw new Error(`Fix ${file} before building: ${issues[0]?.message ?? 'Invalid integration XML.'}`);
      const propertyParser = saxParser(true, { xmlns: true });
      propertyParser.onopentag = tag => {
        if (tag.uri !== 'http://www.mulesoft.org/schema/mule/core' || tag.local !== 'configuration-properties') return;
        const attr = tag.attributes.file as { value?: string } | undefined;
        if (typeof attr?.value !== 'string' || !/^[A-Za-z0-9_.\/-]+\.properties$/.test(attr.value) || path.isAbsolute(attr.value) || attr.value.split('/').includes('..')) throw new Error('Runtime port preflight supports explicitly declared relative .properties resources only.');
        propertyFiles.add(attr.value);
      };
      propertyParser.write(text).close();
      flowCount += model.flows.length;
      for (const config of model.configs) { if (configs.has(config.id)) throw new Error(`Duplicate HTTP configuration across files: ${config.name}`); configs.set(config.id, config); }
      for (const flow of model.flows) if (flow.source) refs.push(flow.source.configId);
    }
    for (const file of propertyFiles) {
      const resolvedFile = await fs.realpath(path.join(resources, file));
      if (!isInside(resources, resolvedFile)) throw new Error('Property resource resolves outside this project.');
      for (const line of (await fs.readFile(resolvedFile, 'utf8')).split(/\r?\n/)) {
        const match = /^\s*([^#!\s][^=:]*?)\s*[=:]\s*(.*?)\s*$/.exec(line);
        if (match) {
          if (Object.hasOwn(properties, match[1]) && properties[match[1]] !== match[2]) throw new Error('Conflicting property definitions prevent an unambiguous listener preflight: ' + match[1]);
          properties[match[1]] = match[2];
        }
      }
    }
    if (!flowCount || !refs.length) throw new Error('No runnable HTTP flow exists. Create a flow and add a configured HTTP Listener source.');
    const ports = new Set<number>();
    for (const reference of refs) {
      const config = configs.get(reference); if (!config) throw new Error(`Missing HTTP Listener configuration ${reference}.`);
      if (config.protocol !== 'HTTP' || resolved(config.host) !== '127.0.0.1') throw new Error('This local milestone executes HTTP listeners bound explicitly to 127.0.0.1 only. HTTPS and public bindings are not enabled.');
      const literal = resolved(config.port);
      if (!/^\d+$/.test(literal) || Number(literal) < 1024 || Number(literal) > 65535) throw new Error('Listener port must resolve to an unprivileged integer from 1024 to 65535.');
      ports.add(Number(literal));
    }
    if (ports.size !== 1) throw new Error('This milestone runs one project listener port per instance. Use a shared Listener configuration/port.');
    return [...ports][0];
  }
  private async queryStatus(project: Project, settings: RuntimeSettings, signal?: AbortSignal): Promise<RuntimeStatus> {
    const data = await this.cli(project, settings, ['status'], signal, [0, 4, 5]);
    const status = parseRuntimeStatus(data, settings.instanceDirectory);
    const app = status.applications[project.artifactId];
    this.update(project, { process: status.process, application: app?.state ?? 'NOT_DEPLOYED', endpoint: 'UNVERIFIED',
      ...(!this.state(project).busy ? { phase: status.process === 'STOPPED' ? 'Stopped' : app?.state === 'DEPLOYED' ? 'Deployed · endpoint unverified' : app?.state === 'UNHEALTHY' ? 'Failed' : 'Runtime ready · application not deployed' } : {}) });
    return status;
  }
  private async build(project: Project, settings: RuntimeSettings, signal: AbortSignal): Promise<Built> {
    await this.saveProjectDocuments(project);
    const fresh = await this.projects.load(project.root); Object.assign(project, fresh);
    const port = await this.projectPort(project);
    this.update(project, { port, artifact: undefined, artifactSha256: undefined, phase: 'Building', buildOutput: '' });
    const before = await this.sourceHash(project), started = Date.now();
    await this.cli(project, settings, ['doctor', '--port', String(port)], signal);
    await this.command(project, settings, 'bash', [path.join(settings.runtimeHome, 'scripts/build-app.sh'), '--project', project.root, ...(settings.offline ? ['--offline'] : [])], 240000, 'buildOutput', signal);
    const artifact = path.join(project.root, 'target', `${project.artifactId}-${project.version}-mule-application.jar`);
    const info = await fs.stat(artifact).catch(() => { throw new Error('Build returned success but the expected deployable application JAR was not produced: ' + artifact); });
    if (!info.isFile() || info.mtimeMs < started - 1000) throw new Error('Build did not produce a fresh application artifact. An existing JAR will not be deployed.');
    if (before !== await this.sourceHash(project) || vscode.workspace.textDocuments.some(d => d.isDirty && d.uri.scheme === 'file' && isInside(project.root, d.uri.fsPath))) throw new Error('Project source changed while building. Save and build again; the artifact was not deployed.');
    const sha256 = hash(await fs.readFile(artifact));
    this.update(project, { artifact, artifactSha256: sha256, phase: 'Build succeeded · not deployed' });
    return { artifact, sha256, sourceHash: before, port };
  }
  private async appLogs(project: Project, settings: RuntimeSettings, signal?: AbortSignal): Promise<void> {
    const data = await this.cli(project, settings, ['logs', '--app', project.artifactId, '--lines', '1000'], signal);
    if (!Array.isArray(data.lines) || !data.lines.every(l => typeof l === 'string')) throw new Error('Application log response does not match the runtime contract.');
    const text = data.lines.join('\n'); this.update(project, { applicationLogs: text.slice(-MAX_OUTPUT) });
    this.logChannel.clear(); this.logChannel.appendLine(String(data.log ?? 'Application log')); this.logChannel.appendLine(text);
  }
  private async stopApplication(project: Project, settings: RuntimeSettings, signal?: AbortSignal): Promise<void> {
    const status = await this.queryStatus(project, settings, signal);
    if (status.applications[project.artifactId]) await this.cli(project, settings, ['undeploy', project.artifactId], signal, [0], 45000);
    await this.queryStatus(project, settings, signal);
  }
  private async stopRuntime(project: Project, settings: RuntimeSettings, signal?: AbortSignal): Promise<void> {
    const status = await this.queryStatus(project, settings, signal);
    const other = Object.keys(status.applications).filter(name => name !== project.artifactId);
    if (other.length) throw new Error('This runtime also contains other applications (' + other.join(', ') + '). Studio will not stop a shared instance. Stop only your application or manage that instance explicitly.');
    if (status.applications[project.artifactId]) {
      try { await this.stopApplication(project, settings, signal); }
      catch (error) {
        if (signal?.aborted) throw error;
        // A pending failed deployment may not yet have an anchor. The CLI
        // deliberately requires stopping the owned instance before removing it.
        this.append(project, 'runtimeOutput', '\nUndeploy before stop could not complete; stopping this private instance before retrying its deployment removal.\n');
        await this.cli(project, settings, ['stop'], signal, [0], 45000);
        await this.cli(project, settings, ['undeploy', project.artifactId], signal, [0], 45000);
      }
    }
    await this.cli(project, settings, ['stop'], signal, [0], 45000); await this.queryStatus(project, settings, signal);
  }
  private async run(project: Project, settings: RuntimeSettings, signal: AbortSignal): Promise<void> {
    const built = await this.build(project, settings, signal);
    let status = await this.queryStatus(project, settings, signal);
    const foreign = Object.keys(status.applications).filter(name => name !== project.artifactId);
    if (foreign.length) throw new Error('Unexpected other applications in this project instance. No deployment or stop was attempted: ' + foreign.join(', '));
    if (status.applications[project.artifactId]) { this.runMutations.add(project.root); this.update(project, { phase: 'Undeploying previous application' }); await this.stopApplication(project, settings, signal); status = await this.queryStatus(project, settings, signal); }
    if (status.process === 'RUNNING') {
      const instanceState = JSON.parse(await fs.readFile(path.join(settings.instanceDirectory, 'state.json'), 'utf8'));
      if (instanceState.config?.http_port !== built.port) { this.runMutations.add(project.root); await this.stopRuntime(project, settings, signal); status = await this.queryStatus(project, settings, signal); }
    }
    await availablePort(built.port);
    if (status.process === 'STOPPED') {
      this.runMutations.add(project.root);
      this.update(project, { phase: 'Starting runtime' }); await this.cli(project, settings, ['start', '--port', String(built.port)], signal);
    }
    if (built.sourceHash !== await this.sourceHash(project) || vscode.workspace.textDocuments.some(d => d.isDirty && d.uri.scheme === 'file' && isInside(project.root, d.uri.fsPath))) throw new Error('Source changed before deployment. Build and run again; no stale artifact was deployed.');
    this.update(project, { phase: 'Deploying', application: 'DEPLOYING', endpoint: 'UNVERIFIED' });
    this.runMutations.add(project.root);
    const result = await this.cli(project, settings, ['deploy', built.artifact, '--name', project.artifactId], signal);
    const app = result.application as Record<string, unknown> | undefined;
    if (!app || app.deployment !== 'DEPLOYED' || app.healthy !== true) throw new Error('The runtime did not confirm successful application deployment.');
    await this.queryStatus(project, settings, signal);
    this.update(project, { phase: 'Deployed · endpoint unverified', application: 'DEPLOYED', endpoint: 'UNVERIFIED' });
    await this.appLogs(project, settings, signal).catch(error => this.append(project, 'runtimeOutput', '\nLog read: ' + String(error) + '\n'));
  }
  async reconcile(project: Project): Promise<void> {
    if (!vscode.workspace.isTrusted || process.platform !== 'linux' || process.arch !== 'x64' || !this.projects.getSettings(project) || this.operations.has(project.root)) return;
    try { const settings = this.settings(project); await this.ownership(project, settings); await this.queryStatus(project, settings); }
    catch (error) { this.update(project, { phase: 'Status unavailable', lastError: error instanceof Error ? error.message : String(error), process: 'UNKNOWN', application: 'UNKNOWN' }); }
  }
  async execute(action: RuntimeAction, project: Project): Promise<void> {
    if (action === 'cancel') { this.operations.get(project.root)?.abort(); return; }
    if (this.operations.has(project.root)) throw new Error('A project operation is already in progress. Wait or use Cancel Current Operation.');
    if (action === 'configureRuntime') {
      if (await this.projects.configure(project)) { this.update(project, { phase: 'Configured · run diagnostics', lastError: '', process: 'UNKNOWN', application: 'NOT_CHECKED' }); await this.reconcile(project); }
      return;
    }
    this.trust(); assertSupportedHost();
    const settings = this.settings(project), controller = new AbortController(); this.operations.set(project.root, controller);
    this.update(project, { busy: true, lastError: '', phase: action === 'logs' ? 'Reading application logs' : 'Checking runtime' });
    const recordDirectory = path.join(this.context.globalStorageUri.fsPath, 'runtime-evidence', hash(project.root).slice(0, 16), new Date().toISOString().replace(/[:.]/g, '-') + '-' + action);
    await fs.mkdir(recordDirectory, { recursive: true }); this.records.set(project.root, recordDirectory); this.update(project, { evidenceDirectory: recordDirectory });
    try {
      await vscode.window.withProgress({ location: vscode.ProgressLocation.Notification, title: `ZERNOVA · ${action} · ${project.name}`, cancellable: true }, async (_progress, token) => {
        const cancellation = token.onCancellationRequested(() => controller.abort());
        try {
          if (['build', 'run', 'diagnostics'].includes(action)) await this.validateTools(project, settings, controller.signal);
          else { await this.ownership(project, settings); }
          if (action === 'diagnostics') {
            const port = await this.projectPort(project).catch(() => 18081);
            await this.cli(project, settings, ['doctor', '--port', String(port)], controller.signal);
            this.update(project, { phase: 'Diagnostics passed', port });
          } else if (action === 'build') await this.build(project, settings, controller.signal);
          else if (action === 'run') await this.run(project, settings, controller.signal);
          else if (action === 'status') await this.queryStatus(project, settings, controller.signal);
          else if (action === 'logs') { await this.appLogs(project, settings, controller.signal); await this.queryStatus(project, settings, controller.signal); }
          else if (action === 'stopApplication') await this.stopApplication(project, settings, controller.signal);
          else if (action === 'stopRuntime') await this.stopRuntime(project, settings, controller.signal);
        } finally { cancellation.dispose(); }
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : String(error);
      if ((controller.signal.aborted || error instanceof OperationTimeoutError) && action === 'run' && this.runMutations.has(project.root)) {
        this.append(project, 'runtimeOutput', '\nCancellation/timeout after lifecycle changes: reconciling this private instance and removing only its deployment.\n');
        try { await this.stopRuntime(project, settings); }
        catch (cleanupError) { this.append(project, 'runtimeOutput', '\nCleanup requires attention: ' + String(cleanupError) + '\n'); }
      }
      try { await this.queryStatus(project, settings); } catch { /* Keep last verified state and explicit failure. */ }
      this.update(project, { phase: controller.signal.aborted ? 'Cancelled' : 'Failed', lastError: message });
      this.append(project, action === 'build' ? 'buildOutput' : 'runtimeOutput', '\nERROR: ' + message + '\n');
      throw error;
    } finally {
      this.operations.delete(project.root); this.runMutations.delete(project.root); this.update(project, { busy: false });
      if (['status', 'logs', 'stopApplication', 'stopRuntime'].includes(action) && !this.state(project).lastError) {
        const state = this.state(project); this.update(project, { phase: state.process === 'STOPPED' ? 'Stopped' : state.application === 'DEPLOYED' ? 'Deployed · endpoint unverified' : state.application === 'UNHEALTHY' ? 'Failed' : 'Runtime ready · application not deployed' });
      }
      await fs.writeFile(path.join(recordDirectory, 'state.json'), JSON.stringify(this.state(project), null, 2) + '\n'); this.records.delete(project.root);
    }
  }
}
