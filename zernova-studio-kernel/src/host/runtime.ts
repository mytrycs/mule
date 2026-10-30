import net from 'node:net';
import { deploymentBanner, stageLine, deploymentConsole } from '../shared/deploymentLog';
import { RUNTIME_VERSION } from '../../../zernova-studio-build/src/portable/bundle';
import { projectProperties } from './propertyResolution';
import { componentLocation } from '../shared/breakpoints';
import { FlowDebugger } from './flowDebug';
import * as vscode from 'vscode';
import { saveAutomatically } from './saves';
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
  deploymentOutput?:string; runtimeConsole?:string; debugOutput?: string; debugConnected?: boolean; debugPaused?: string; debugBreakpoints?: {uri:string;id:string;path:string}[];
  buildOutput: string; applicationLogs: string; runtimeOutput: string;
  artifact?: string; artifactSha256?: string; port?: number; instanceDirectory?: string;
  evidenceDirectory?: string; offline?: boolean; lastUpdated?: string;
}
const MAX_OUTPUT = 250000;
export class RuntimeManager implements vscode.Disposable {
  private readonly changed = new vscode.EventEmitter<string>();
  readonly onDidChange = this.changed.event;
  private readonly debuggers = new Map<string,FlowDebugger>();
  private readonly states = new Map<string, RuntimeViewState>();
  private readonly logTimers = new Map<string, ReturnType<typeof setInterval>>();
  private readonly logReading = new Set<string>();
  private readonly pendingManualSaves = new Set<string>();
  private readonly manualSaveTasks = new Map<string, Promise<void>>();
  private readonly operations = new Map<string, AbortController>();
  private readonly buildChannel = vscode.window.createOutputChannel('ZERNOVA Build');
  private readonly runtimeChannel = vscode.window.createOutputChannel('ZERNOVA Runtime');
  private readonly logChannel = vscode.window.createOutputChannel('ZERNOVA Application Logs');
  private readonly diagnostics = vscode.languages.createDiagnosticCollection('zernovaKernelStudio.build');
  constructor(private readonly context: vscode.ExtensionContext, private readonly projects: ProjectManager) {}
  dispose(): void { for(const debugger_ of this.debuggers.values()) void debugger_.close(); for (const timer of this.logTimers.values()) clearInterval(timer); for (const op of this.operations.values()) op.abort(); this.changed.dispose(); this.buildChannel.dispose(); this.runtimeChannel.dispose(); this.logChannel.dispose(); this.diagnostics.dispose(); }
  state(project: Project): RuntimeViewState {
    let value = this.states.get(project.root);
    if (!value) {
      value = { schemaVersion: 1, projectRoot: project.root, projectName: project.name, configured: true,
        phase: 'Bundled runtime ready for diagnostics', busy: false, process: 'UNKNOWN', application: 'NOT_CHECKED', endpoint: 'UNVERIFIED', lastError: '', debugOutput:'', debugBreakpoints:this.context.workspaceState.get('zernova.breakpoints.'+project.root,[]), buildOutput: '', applicationLogs: '', runtimeOutput: '', offline: true,
        instanceDirectory: this.projects.getSettings(project).instanceDirectory };
      this.states.set(project.root, value);
    }
    return { ...value };
  }
  stateForUri(uri: vscode.Uri): RuntimeViewState | undefined { const project = this.projects.projectForUri(uri); return project ? this.state(project) : undefined; }
  private update(project: Project, patch: Partial<RuntimeViewState>): void { this.states.set(project.root, { ...this.state(project), ...patch, lastUpdated: new Date().toISOString() }); this.changed.fire(project.root); }
  private append(project: Project, stream: 'buildOutput' | 'runtimeOutput', text: string): void { this.update(project, { [stream]: (this.state(project)[stream] + text).slice(-MAX_OUTPUT) }); (stream === 'buildOutput' ? this.buildChannel : this.runtimeChannel).append(text); }
  private deploymentStage(project:Project,stage:string,message:string,elapsedMs?:number):void{this.update(project,{deploymentOutput:((this.state(project).deploymentOutput??'')+stageLine(stage,message,elapsedMs)).slice(-MAX_OUTPUT)});}
  reportError(project: Project, error: unknown): void {
    this.deploymentStage(project,'FAILED',error instanceof Error?error.message:String(error));
    const text = error instanceof Error ? error.message : String(error);
    if (this.state(project).lastError !== text) this.append(project, 'buildOutput', `\n[${new Date().toISOString()}] ERROR: ${text}\n`);
    this.update(project, { lastError: text, ...(!this.operations.has(project.root) ? {phase:'Failed'} : {}) });
  }
  async manualSaved(project: Project, uri: vscode.Uri): Promise<void> {
    const relative = path.relative(project.root, uri.fsPath).split(path.sep).join('/');
    if (!(relative.startsWith('src/') || ['pom.xml', 'zernova-app.json', 'zernova-artifact.json'].includes(relative)) || !this.projects.getRunConfiguration(project).redeployOnSave) return;
    const previous = this.manualSaveTasks.get(project.root) ?? Promise.resolve();
    const task = previous.then(async () => {
      if (!this.projects.getRunConfiguration(project).redeployOnSave) return;
      if (this.operations.has(project.root)) { if (this.state(project).application === 'DEPLOYED') this.pendingManualSaves.add(project.root); return; }
      this.trust();
      const status = await this.backend(project).status();
      if (status.applications[project.artifactId]?.state !== 'DEPLOYED') return;
      const hash = await sourceHash(project.root);
      if (hash === this.context.workspaceState.get<string>('zernova.deployedHash.' + project.root)) return;
      this.append(project, 'buildOutput', `\nManual save: rebuilding and redeploying ${path.basename(uri.fsPath)}.\n`);
      await this.execute(this.context.workspaceState.get<string>('zernova.launchMode.' + project.root) === 'debug' ? 'debug' : 'run', project);
    }).catch(error => { this.reportError(project, error); });
    this.manualSaveTasks.set(project.root, task);
    try { await task; } finally { if (this.manualSaveTasks.get(project.root) === task) this.manualSaveTasks.delete(project.root); }
  }
  async toggleBreakpoint(project: Project, uri: vscode.Uri, id: string): Promise<void> {
    const text=(await vscode.workspace.openTextDocument(uri)).getText(), model=parseIntegration(text);
    const location=componentLocation(model,id);
    if(!location)throw new Error('Select an executable component.');
    if(this.state(project).debugConnected && await sourceHash(project.root)!==this.context.workspaceState.get<string>('zernova.deployedHash.'+project.root))throw new Error('Source changed since deployment. Debug/redeploy again before changing breakpoints.');
    const current=this.state(project).debugBreakpoints??[], key=uri.toString();
    const next=current.some(b=>b.uri===key&&b.id===id)?current.filter(b=>b.uri!==key||b.id!==id):[...current,{uri:key,id,path:location}];
    await this.context.workspaceState.update('zernova.breakpoints.'+project.root,next);
    this.update(project,{debugBreakpoints:next});this.debuggers.get(project.root)?.setBreakpoints(next.map(b=>b.path));
  }
  private async attachFlowDebugger(project: Project): Promise<void> {
    const refreshed=[];
    for(const bp of this.state(project).debugBreakpoints??[]){
      const file=vscode.Uri.parse(bp.uri);if(!isInside(project.root,file.fsPath))continue;
      try{const model=parseIntegration(await fs.readFile(file.fsPath,'utf8')),location=componentLocation(model,bp.id);if(location)refreshed.push({...bp,path:location});}catch{/* Removed files cannot retain a live breakpoint. */}
    }
    await this.context.workspaceState.update('zernova.breakpoints.'+project.root,refreshed);this.update(project,{debugBreakpoints:refreshed});
    const debugger_=new FlowDebugger(stop=>this.update(project,{debugPaused:stop.path}),text=>this.update(project,{debugOutput:((this.state(project).debugOutput??'')+text).slice(-MAX_OUTPUT)}),()=>this.update(project,{debugConnected:false,debugPaused:undefined}));
    debugger_.setBreakpoints((this.state(project).debugBreakpoints??[]).map(b=>b.path));
    this.debuggers.set(project.root,debugger_);
    try{await debugger_.connect(this.projects.getRunConfiguration(project,true).debugPort);this.update(project,{debugConnected:true});}catch(error){await debugger_.close();this.debuggers.delete(project.root);throw error;}
  }
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
      const backend=this.backend(project);const [logs,consoleLog]=await Promise.all([backend.logs(),backend.consoleLog()]);
      const presented=deploymentConsole(consoleLog.text);if(presented!==this.state(project).runtimeConsole)this.update(project,{runtimeConsole:presented});
      const text = logs.lines.join('\n').slice(-MAX_OUTPUT);
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
    const choice = await studioForm('Save Project Documents', [], async () => true, `Save ${dirty.length} unsaved project document(s) before building the current application?`);
    if (!choice) throw new Error('Build cancelled: unsaved documents were not saved. No existing artifact was deployed.');
    for (const doc of dirty) if (!await saveAutomatically(doc)) throw new Error(`Could not save ${doc.uri.fsPath}. No application was built or deployed.`);
  }
  private async projectPort(project: Project, debug = false): Promise<number> {
    const source = path.join(project.root, 'src/main/zernova');
    const files = (await fs.readdir(source)).filter(name => name.endsWith('.xml'));
    if (!files.length) throw new Error('src/main/zernova is empty. Create Integration XML, create a flow, then add and configure an HTTP Listener and Logger before building.');
    const run = this.projects.getRunConfiguration(project,debug);
    const models:ReturnType<typeof parseIntegration>[]=[];
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
      models.push(model);
      flowCount += model.flows.length;
      for (const config of model.configs) { if (configs.has(config.id)) throw new Error(`Duplicate HTTP configuration across files: ${config.name}`); configs.set(config.id, config); }
      for (const flow of model.flows) if (flow.source) { if (!flow.processors.length) throw new Error(`Flow ${flow.name} has an HTTP Listener but no processors. Add a processor, such as Logger, before running this application.`); if(flow.source.kind==='listener')refs.push(flow.source.configId); }
    }
    const {resolve:resolved}=await projectProperties(project.root,models,run);
    if(!refs.length&&models.some(m=>m.flows.some(f=>f.source?.kind==='scheduler'))){const socket=net.createServer();await new Promise<void>(resolve=>socket.listen(0,'127.0.0.1',resolve));const port=(socket.address() as net.AddressInfo).port;await new Promise<void>(resolve=>socket.close(()=>resolve()));return port;}
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
    if (['debugContinue','debugStep','debugDisconnect'].includes(action)) { this.trust(); const debugger_=this.debuggers.get(project.root); if(!debugger_)throw new Error('Start Debug first.'); if(action==='debugDisconnect'){await debugger_.close();this.debuggers.delete(project.root);}else{this.update(project,{debugPaused:undefined});await debugger_.resume(action==='debugStep');} return this.state(project); }
    if (action === 'cancel') { this.operations.get(project.root)?.abort(); return this.state(project); }
    if (this.operations.has(project.root)) throw new Error('A project operation is already in progress.');
    this.trust(); assertSupportedHost();
    if (action === 'configureRun' || action === 'configureDebug') { await this.projects.configureRun(project,action === 'configureDebug'); return this.state(project); }
    if (['debug','run','stopRuntime','stopApplication'].includes(action)) { await this.debuggers.get(project.root)?.close(); this.debuggers.delete(project.root); }
    const debug = action === 'debug'; if (debug) action = 'run';
    if (action === 'configureRuntime') { await this.projects.configure(project); action = 'diagnostics'; }
    const controller = new AbortController(); this.operations.set(project.root, controller);
    const backend = debug ? new BundledRuntime({ bundle: this.projects.getSettings(project).runtimeHome, instance: this.projects.getSettings(project).instanceDirectory, project: project.root, application: project.artifactId, runConfiguration: this.projects.getRunConfiguration(project,true), debug: true, onOutput: text => this.append(project, 'runtimeOutput', text) }) : this.backend(project), signal = controller.signal;
    const evidence = path.join(this.context.globalStorageUri.fsPath, 'runtime-evidence', createHash('sha256').update(project.root).digest('hex').slice(0, 16), new Date().toISOString().replace(/[:.]/g, '-') + '-' + action);
    await fs.mkdir(evidence, { recursive: true }); this.update(project, { busy: true, lastError: '', evidenceDirectory: evidence, phase: 'Checking bundled runtime' });
    let mutated = false;const started=Date.now();
    if(action==='build'||action==='run')this.update(project,{deploymentOutput:deploymentBanner(project.artifactId,this.projects.getRunConfiguration(project,debug).environment,String(this.context.extension.packageJSON.version),RUNTIME_VERSION),runtimeConsole:''});
    try {
      await vscode.window.withProgress({ location: vscode.ProgressLocation.Notification, title: `ZERNOVA · ${action} · ${project.name}`, cancellable: true }, async (_progress, token) => {
        const subscription = token.onCancellationRequested(() => controller.abort());
        try {
          if (action === 'diagnostics') { const result = await backend.doctor(signal); this.append(project, 'runtimeOutput', JSON.stringify(result, null, 2) + '\n'); this.update(project, { phase: 'Diagnostics passed' }); }
          else if (action === 'build' || action === 'run') {
            await this.saveProjectDocuments(project); Object.assign(project, await this.projects.load(project.root));
            const port = await this.projectPort(project,debug); if (debug && port === this.projects.getRunConfiguration(project,true).debugPort) throw new Error('Listener and debugger ports must be different.'); await this.metadata(project);
            this.update(project, { phase: 'Building offline', port, artifact: undefined, artifactSha256: undefined, buildOutput: '' });
            this.deploymentStage(project,'BUILD','Building the current sources offline');const buildStarted=Date.now();
            const artifact = await backend.package(project.root, signal);
            this.deploymentStage(project,'BUILD','SUCCEEDED · '+path.basename(artifact.artifact),Date.now()-buildStarted);
            this.append(project, 'buildOutput', JSON.stringify(artifact, null, 2) + '\n');
            this.update(project, { artifact: artifact.artifact, artifactSha256: artifact.sha256, phase: 'Build succeeded · not deployed' });
            if (action === 'run') {
              this.deploymentStage(project,'DEPLOY','Starting application deployment');
              const before = await backend.status();
              if (before.applications[project.artifactId]) { mutated = true; await backend.undeploy(project.artifactId, signal); }
              if (before.process === 'RUNNING') { mutated=true; await backend.stop(signal); }
              if (await sourceHash(project.root) !== artifact.sourceHash || vscode.workspace.textDocuments.some(d => d.isDirty && isInside(project.root, d.uri.fsPath))) throw new Error('Source changed after building. No stale artifact was deployed.');
              this.update(project, { phase: 'Starting runtime' });this.deploymentStage(project,'RUNTIME','Starting the owned runtime and compatible Java');this.followLogs(project,true); mutated = true; await backend.start(port, signal);
              this.deploymentStage(project,'RUNTIME','READY');
              this.update(project, { phase: 'Deploying' });this.deploymentStage(project,'DEPLOY','Deploying '+project.artifactId); await backend.deploy(artifact.artifact, project.artifactId, signal);
              await this.context.workspaceState.update('zernova.deployedHash.' + project.root, artifact.sourceHash);
              await this.context.workspaceState.update('zernova.launchMode.' + project.root,debug ? 'debug' : 'run');
              this.update(project, { phase: 'Deployed · endpoint unverified' });this.deploymentStage(project,'DEPLOY','SUCCEEDED · Send a request to test the endpoint',Date.now()-started);
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
      await this.refreshLogs(project).catch(() => {});
      this.reportError(project, error); this.update(project, { phase: signal.aborted ? 'Cancelled' : 'Failed' }); throw error;
    } finally { this.operations.delete(project.root); this.update(project, { busy: false }); if (this.pendingManualSaves.delete(project.root)) void this.manualSaved(project, vscode.Uri.file(path.join(project.root, 'pom.xml'))); await fs.writeFile(path.join(evidence, 'state.json'), JSON.stringify(this.state(project), null, 2)); }
    if (debug) await this.attachFlowDebugger(project);
    return this.state(project);
  }
}
