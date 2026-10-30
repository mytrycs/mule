import * as vscode from 'vscode';
import * as fs from 'node:fs/promises';
import path from 'node:path';
import { createHash } from 'node:crypto';
import { studioForm } from './forms';
import { ProjectManager, type Project, isInside } from './projects';
import { parseIntegration } from '../shared/xml';
import type { RuntimeAction } from '../shared/realProtocol';
import { parser as saxParser } from 'sax';
import { BundledRuntime, type PortableStatus } from '../../../zernova-studio-build/src/portable/runtime';
import { sourceHash } from '../../../zernova-studio-build/src/portable/packager';
import { parseRuntimeStatus } from '../shared/runtimeContract';
import { targetForHost } from '../../../zernova-studio-build/src/portable/bundle';
export { checkPort as availablePort } from '../../../zernova-studio-build/src/portable/runtime';
export function assertSupportedHost(platform = process.platform, arch = process.arch): void { targetForHost(platform, arch); }
export interface RuntimeViewState {
  schemaVersion: 1; projectRoot: string; projectName: string; configured: boolean; phase: string;
  busy: boolean; process: string; application: string; endpoint: string; lastError: string;
  buildOutput: string; applicationLogs: string; runtimeOutput: string;
  artifact?: string; artifactSha256?: string; port?: number; instanceDirectory?: string;
  evidenceDirectory?: string; offline?: boolean; lastUpdated?: string;
}
const MAX_OUTPUT = 250000;
export class RuntimeManager implements vscode.Disposable {
  private readonly changed = new vscode.EventEmitter<string>();
  readonly onDidChange = this.changed.event;
  private readonly states = new Map<string, RuntimeViewState>();
  private readonly logTimers = new Map<string, ReturnType<typeof setInterval>>();
  private readonly logReading = new Set<string>();
  private readonly operations = new Map<string, AbortController>();
  private readonly buildChannel = vscode.window.createOutputChannel('ZERNOVA Build');
  private readonly runtimeChannel = vscode.window.createOutputChannel('ZERNOVA Runtime');
  private readonly logChannel = vscode.window.createOutputChannel('ZERNOVA Application Logs');
  private readonly diagnostics = vscode.languages.createDiagnosticCollection('zernovaKernelStudio.build');
  constructor(private readonly context: vscode.ExtensionContext, private readonly projects: ProjectManager) {}
  dispose(): void { for (const timer of this.logTimers.values()) clearInterval(timer); for (const op of this.operations.values()) op.abort(); this.changed.dispose(); this.buildChannel.dispose(); this.runtimeChannel.dispose(); this.logChannel.dispose(); this.diagnostics.dispose(); }
  state(project: Project): RuntimeViewState {
    let value = this.states.get(project.root);
    if (!value) {
      value = { schemaVersion: 1, projectRoot: project.root, projectName: project.name, configured: true,
        phase: 'Bundled runtime ready for diagnostics', busy: false, process: 'UNKNOWN', application: 'NOT_CHECKED', endpoint: 'UNVERIFIED', lastError: '', buildOutput: '', applicationLogs: '', runtimeOutput: '', offline: true,
        instanceDirectory: this.projects.getSettings(project).instanceDirectory };
      this.states.set(project.root, value);
    }
    return { ...value };
  }
  stateForUri(uri: vscode.Uri): RuntimeViewState | undefined { const project = this.projects.projectForUri(uri); return project ? this.state(project) : undefined; }
  private update(project: Project, patch: Partial<RuntimeViewState>): void { this.states.set(project.root, { ...this.state(project), ...patch, lastUpdated: new Date().toISOString() }); this.changed.fire(project.root); }
  private append(project: Project, stream: 'buildOutput' | 'runtimeOutput', text: string): void { this.update(project, { [stream]: (this.state(project)[stream] + text).slice(-MAX_OUTPUT) }); (stream === 'buildOutput' ? this.buildChannel : this.runtimeChannel).append(text); }
  private trust(): void { if (!vscode.workspace.isTrusted) throw new Error('Workspace trust is required before building or running applications.'); }
  private backend(project: Project): BundledRuntime {
    const settings = this.projects.getSettings(project);
    return new BundledRuntime({ bundle: settings.runtimeHome, instance: settings.instanceDirectory, project: project.root, application: project.artifactId, runConfiguration: this.projects.getRunConfiguration(project),
      onOutput: text => this.append(project, 'runtimeOutput', text) });
  }
  private async refreshLogs(project: Project): Promise<void> {
    if (this.logReading.has(project.root)) return;
    this.logReading.add(project.root);
    try {
      const logs = await this.backend(project).logs(); const text = logs.lines.join('\n').slice(-MAX_OUTPUT);
      if (text !== this.state(project).applicationLogs) { this.update(project, { applicationLogs: text }); this.logChannel.clear(); this.logChannel.appendLine(text); }
    } catch (error) { if (!this.operations.has(project.root)) this.update(project, { lastError: 'Log refresh failed: ' + String(error) }); }
    finally { this.logReading.delete(project.root); }
  }
  private followLogs(project: Project, running: boolean): void {
    if (!running) { const timer = this.logTimers.get(project.root); if (timer) clearInterval(timer); this.logTimers.delete(project.root); return; }
    if (!this.logTimers.has(project.root)) { const timer = setInterval(() => { void this.refreshLogs(project); }, 1000); timer.unref(); this.logTimers.set(project.root, timer); }
  }
  private reflect(project: Project, status: PortableStatus): void {
    parseRuntimeStatus(status, this.projects.getSettings(project).instanceDirectory);
    this.followLogs(project, status.process === 'RUNNING');
    const app = status.applications[project.artifactId];
    this.update(project, { process: status.process, application: app?.state ?? 'NOT_DEPLOYED', endpoint: 'UNVERIFIED' });
  }
  private async metadata(project: Project): Promise<void> {
    // Refuse unsupported Maven dependencies rather than silently omitting them.
    const parser = saxParser(true, { xmlns: true }); const stack: string[] = []; const deps: Record<string, string>[] = []; let dep: Record<string, string> | undefined;
    parser.ondoctype = () => { throw new Error('POM DOCTYPE is not supported.'); };
    parser.onopentag = tag => { stack.push(tag.local); if (stack.join('/') === 'project/dependencies/dependency') dep = {}; };
    parser.ontext = text => { if (dep && stack.length === 4) dep[stack[3]] = (dep[stack[3]] ?? '') + text; };
    parser.onclosetag = () => { if (stack.join('/') === 'project/dependencies/dependency' && dep) { deps.push(dep); dep = undefined; } stack.pop(); };
    parser.write(await fs.readFile(path.join(project.root, 'pom.xml'), 'utf8')).close();
    if (deps.some(d => !['com.zernova.connectors'].includes(d.groupId?.trim()) || d.artifactId?.trim() !== 'zernova-http-connector' || d.version?.trim() !== '1.12.2' || d.classifier?.trim() !== 'zernova-plugin')) throw new Error('The bundled offline builder supports core and HTTP 1.12.2 only. Additional dependencies are not silently ignored.');
    const metadata = { schemaVersion: 1, name: project.artifactId, group: project.groupId, version: project.version, dependencies: deps.length ? ['http'] : [] };
    const file = path.join(project.root, 'zernova-app.json');
    const existing = await fs.readFile(file, 'utf8').catch(e => { if (e.code === 'ENOENT') return undefined; throw e; });
    if (existing) {
      const parsed = JSON.parse(existing);
      if (parsed.name !== metadata.name || parsed.group !== metadata.group || parsed.version !== metadata.version || JSON.stringify(parsed.dependencies) !== JSON.stringify(metadata.dependencies)) throw new Error('zernova-app.json and the Studio POM do not match. Align the coordinates and supported dependencies before building.');
    } else await fs.writeFile(file, JSON.stringify(metadata, null, 2) + '\n', { flag: 'wx' });
  }
  private async saveProjectDocuments(project: Project): Promise<void> {
    const dirty = vscode.workspace.textDocuments.filter(doc => doc.uri.scheme === 'file' && isInside(project.root, doc.uri.fsPath) && doc.isDirty);
    if (!dirty.length) return;
    const choice = await vscode.window.showWarningMessage(`Save ${dirty.length} unsaved project document(s) before building the current application?`, { modal: true }, 'Save and Continue');
    if (choice !== 'Save and Continue') throw new Error('Build cancelled: unsaved documents were not saved. No existing artifact was deployed.');
    for (const doc of dirty) if (!await doc.save()) throw new Error(`Could not save ${doc.uri.fsPath}. No application was built or deployed.`);
  }
  private async projectPort(project: Project): Promise<number> {
    const source = path.join(project.root, 'src/main/zernova');
    const files = (await fs.readdir(source)).filter(name => name.endsWith('.xml'));
    if (!files.length) throw new Error('src/main/zernova is empty. Create Integration XML, create a flow, then add and configure an HTTP Listener and Logger before building.');
    const run = this.projects.getRunConfiguration(project);
    const properties: Record<string, string> = { ...run.variables, ...run.properties, 'zernova.env': run.environment };
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
        if (tag.uri !== 'https://schemas.zernova.dev/core' || tag.local !== 'configuration-properties') return;
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
          if (Object.hasOwn(run.properties, match[1]) || Object.hasOwn(run.variables, match[1])) continue;
          if (Object.hasOwn(properties, match[1]) && properties[match[1]] !== match[2]) throw new Error('Conflicting property definitions prevent an unambiguous listener preflight: ' + match[1]);
          properties[match[1]] = match[2];
        }
      }
    }
    if (!flowCount || !refs.length) throw new Error('No runnable HTTP flow exists. Create a flow and add a configured HTTP Listener source.');
    const ports = new Set<number>();
    for (const reference of refs) {
      const config = configs.get(reference); if (!config) throw new Error(`Missing HTTP Listener configuration ${reference}.`);
      if (config.protocol !== 'HTTP' || !['127.0.0.1', 'localhost'].includes(resolved(config.host).toLowerCase())) throw new Error('This local milestone executes HTTP listeners bound to localhost or 127.0.0.1 only. HTTPS and public bindings are not enabled.');
      const literal = resolved(config.port);
      if (!/^\d+$/.test(literal) || Number(literal) < 1024 || Number(literal) > 65535) throw new Error('Listener port must resolve to an unprivileged integer from 1024 to 65535.');
      ports.add(Number(literal));
    }
    if (ports.size !== 1) throw new Error('This milestone runs one project listener port per instance. Use a shared Listener configuration/port.');
    return [...ports][0];
  }
  async reconcile(project: Project): Promise<void> {
    if (!vscode.workspace.isTrusted || this.operations.has(project.root)) return;
    try { this.reflect(project, await this.backend(project).status()); }
    catch (error) { this.update(project, { lastError: String(error), phase: 'Status unavailable' }); }
  }
  async execute(action: RuntimeAction, project: Project): Promise<RuntimeViewState> {
    if (action === 'cancel') { this.operations.get(project.root)?.abort(); return this.state(project); }
    if (this.operations.has(project.root)) throw new Error('A project operation is already in progress.');
    this.trust(); assertSupportedHost();
    if (action === 'configureRun') { await this.projects.configureRun(project); return this.state(project); }
    const debug = action === 'debug'; if (debug) action = 'run';
    if (action === 'configureRuntime') { await this.projects.configure(project); action = 'diagnostics'; }
    const controller = new AbortController(); this.operations.set(project.root, controller);
    const backend = debug ? new BundledRuntime({ bundle: this.projects.getSettings(project).runtimeHome, instance: this.projects.getSettings(project).instanceDirectory, project: project.root, application: project.artifactId, runConfiguration: this.projects.getRunConfiguration(project), debug: true, onOutput: text => this.append(project, 'runtimeOutput', text) }) : this.backend(project), signal = controller.signal;
    const evidence = path.join(this.context.globalStorageUri.fsPath, 'runtime-evidence', createHash('sha256').update(project.root).digest('hex').slice(0, 16), new Date().toISOString().replace(/[:.]/g, '-') + '-' + action);
    await fs.mkdir(evidence, { recursive: true }); this.update(project, { busy: true, lastError: '', evidenceDirectory: evidence, phase: 'Checking bundled runtime' });
    let mutated = false;
    try {
      await vscode.window.withProgress({ location: vscode.ProgressLocation.Notification, title: `ZERNOVA · ${action} · ${project.name}`, cancellable: true }, async (_progress, token) => {
        const subscription = token.onCancellationRequested(() => controller.abort());
        try {
          if (action === 'diagnostics') { const result = await backend.doctor(signal); this.append(project, 'runtimeOutput', JSON.stringify(result, null, 2) + '\n'); this.update(project, { phase: 'Diagnostics passed' }); }
          else if (action === 'build' || action === 'run') {
            await this.saveProjectDocuments(project); Object.assign(project, await this.projects.load(project.root));
            const port = await this.projectPort(project); if (debug && port === this.projects.getRunConfiguration(project).debugPort) throw new Error('Listener and debugger ports must be different.'); await this.metadata(project);
            this.update(project, { phase: 'Building offline', port, artifact: undefined, artifactSha256: undefined, buildOutput: '' });
            const artifact = await backend.package(project.root, signal);
            this.append(project, 'buildOutput', JSON.stringify(artifact, null, 2) + '\n');
            this.update(project, { artifact: artifact.artifact, artifactSha256: artifact.sha256, phase: 'Build succeeded · not deployed' });
            if (action === 'run') {
              const before = await backend.status();
              if (before.applications[project.artifactId]) { mutated = true; await backend.undeploy(project.artifactId, signal); }
              if (before.process === 'RUNNING') {
                const state = JSON.parse(await fs.readFile(path.join(backend.instance, 'state.json'), 'utf8'));
                if (state.config.http_port !== port) { mutated = true; await backend.stop(signal); }
              }
              if (await sourceHash(project.root) !== artifact.sourceHash || vscode.workspace.textDocuments.some(d => d.isDirty && isInside(project.root, d.uri.fsPath))) throw new Error('Source changed after building. No stale artifact was deployed.');
              this.update(project, { phase: 'Starting runtime' }); mutated = true; await backend.start(port, signal);
              this.update(project, { phase: 'Deploying' }); await backend.deploy(artifact.artifact, project.artifactId, signal);
              this.update(project, { phase: 'Deployed · endpoint unverified' });
            }
          } else if (action === 'stopApplication') await backend.undeploy(project.artifactId, signal);
          else if (action === 'stopRuntime') { await backend.stop(signal); await backend.undeploy(project.artifactId, signal); }
          if (action === 'logs' || action === 'run' || action === 'stopRuntime') await this.refreshLogs(project);
          this.reflect(project, await backend.status());
          if (['status', 'logs', 'stopApplication', 'stopRuntime'].includes(action)) this.update(project, { phase: this.state(project).process === 'STOPPED' ? 'Stopped' : this.state(project).application === 'DEPLOYED' ? 'Deployed · endpoint unverified' : 'Runtime ready · application not deployed' });
        } finally { subscription.dispose(); }
      });
    } catch (error) {
      if (signal.aborted && mutated) { try { await backend.stop(); await backend.undeploy(); } catch (cleanup) { this.append(project, 'runtimeOutput', 'Cleanup requires attention: ' + String(cleanup)); } }
      try { this.reflect(project, await backend.status()); } catch { /* Keep the last verified state. */ }
      this.update(project, { phase: signal.aborted ? 'Cancelled' : 'Failed', lastError: error instanceof Error ? error.message : String(error) }); throw error;
    } finally { this.operations.delete(project.root); this.update(project, { busy: false }); await fs.writeFile(path.join(evidence, 'state.json'), JSON.stringify(this.state(project), null, 2)); }
    if (debug) {
      const port = this.projects.getRunConfiguration(project).debugPort;
      if (vscode.extensions.getExtension('vscjava.vscode-java-debug')) {
        await vscode.debug.startDebugging(vscode.workspace.getWorkspaceFolder(vscode.Uri.file(project.root)), { type: 'java', name: 'ZERNOVA · ' + project.name, request: 'attach', hostName: '127.0.0.1', port });
      } else await studioForm('ZERNOVA Java Debugger', [], async () => true, `The runtime is running with JDWP on localhost:${port}. Attach a Java debugger to this port. Install the VS Code Debugger for Java extension to attach from Studio. This supports Java breakpoints; XML flow stepping is not yet implemented.`);
    }
    return this.state(project);
  }
}
