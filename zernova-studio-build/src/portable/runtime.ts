/* Copyright (c) 2026 ZerNova. Original Studio lifecycle tooling; see LICENSE.txt.
 * Adapter v2: portable bundled-runtime support. The existing engine alone runs
 * flows/connectors/expressions. Authenticated JVM self-shutdown replaces Linux
 * PID signaling; it does not replace the integration runtime.
 */
import * as fs from 'node:fs/promises';
import path from 'node:path';
import net from 'node:net';
import { createHash, randomBytes, randomUUID } from 'node:crypto';
import { spawn, type ChildProcess } from 'node:child_process';
import { setTimeout as sleep } from 'node:timers/promises';
import { bundleInfo, verifyBundle, javaExecutable, cleanEnvironment, readJson, writeJson, fileSha, safeRelative, within, ordinaryDirectory, cancelled, RUNTIME_VERSION, ENGINE_VERSION } from './bundle';
import { packageApplication, validateArtifact, assertAppName } from './packager';

interface AgentEndpoint { schemaVersion: 1; product: 'ZERNOVA Runtime Control'; port: number; pid: number; runId: string; stopping?: boolean; }
interface RuntimeState { schema_version: 2; pid: number; run_id: string; console_log: string; runtime_ready: boolean; deployment_attempted?: boolean; started_utc: string; config: { http_host: '127.0.0.1'; http_port: number; heap_mb: number }; }
interface AppRecord { sha256: string; run_id: string; log_offset: number; deployed_utc: string; }
export interface AppStatus { name: string; state: 'NOT_DEPLOYED' | 'STOPPED' | 'DEPLOYING' | 'DEPLOYED' | 'UNHEALTHY'; healthy: boolean; deployment: 'NOT_RUNNING' | 'PENDING' | 'DEPLOYED' | 'FAILED'; endpoint: 'NOT_CHECKED'; }
export interface PortableStatus { schema_version: 2; product: 'ZERNOVA Runtime'; distribution_version: string; application_format_version: 1; kernel_version: string; upstream_baseline?: string; dataweave_version: string; build_mode: string; process: 'RUNNING' | 'STOPPED'; runtime: 'READY' | 'STARTING' | 'STOPPED'; pid?: number; run_id?: string; work_directory: string; healthy: boolean; applications: Record<string, AppStatus>; }
export interface RuntimeOptions { bundle: string; instance: string; project: string; application: string; onOutput?: (text: string) => void; startupTimeoutMs?: number; deploymentTimeoutMs?: number; stopTimeoutMs?: number; }
const children = new Map<string, ChildProcess>();
const MAX_LOG = 8 * 1024 * 1024;
const errorCode = (e: unknown): string | undefined => (e as NodeJS.ErrnoException)?.code;
const exists = async (p: string): Promise<boolean> => fs.lstat(p).then(() => true, e => { if (e.code === 'ENOENT') return false; throw e; });
async function optionalJson<T>(file: string, fallback: T): Promise<T> { try { return await readJson<T>(file); } catch (e) { if (errorCode(e) === 'ENOENT') return fallback; throw e; } }
async function wait(ms: number, signal?: AbortSignal): Promise<void> { cancelled(signal); await sleep(ms, undefined, { signal }).catch(e => { if (signal?.aborted) throw new Error('Operation cancelled.'); throw e; }); }
export async function checkPort(port: number): Promise<void> {
  if (!Number.isInteger(port) || port < 1024 || port > 65535) throw new Error('Use an unprivileged listener port from 1024 to 65535.');
  await new Promise<void>((resolve, reject) => {
    const server = net.createServer(); server.unref();
    server.once('error', e => reject(new Error(`Port ${port} on 127.0.0.1 is unavailable: ${e.message}. Choose another port or stop only a process you own.`)));
    server.listen({ host: '127.0.0.1', port, exclusive: true }, () => server.close(e => e ? reject(e) : resolve()));
  });
}
export function javaArguments(bundle: string, runtime: string, control: string, agent: string, port: number, heap = 768): string[] {
  const args = [`-Xms${Math.min(heap, 256)}m`, `-Xmx${heap}m`,
    `-javaagent:${path.join(bundle, agent)}=${control}`,
    `-Dmule.home=${runtime}`, `-Dzernova.home=${runtime}`, `-Dmule.base=${runtime}`, `-Dzernova.distribution.version=${RUNTIME_VERSION}`,
    '-Dzernova.http.host=127.0.0.1', `-Dzernova.http.port=${port}`, '-Dfile.encoding=UTF-8', '-Djava.net.preferIPv4Stack=TRUE',
    '-Dorg.glassfish.grizzly.nio.transport.TCPNIOTransport.max-receive-buffer-size=1048576',
    '-Dorg.glassfish.grizzly.nio.transport.TCPNIOTransport.max-send-buffer-size=1048576',
    '-XX:MaxMetaspaceSize=256m', '-XX:MetaspaceSize=128m', '-XX:+HeapDumpOnOutOfMemoryError',
    '-XX:NewRatio=1', '-XX:MaxTenuringThreshold=8', '-Dorg.quartz.scheduler.skipUpdateCheck=true',
    '-Dmule.metadata.cache.entryTtl.minutes=10', '-Dmule.metadata.cache.expirationInterval.millis=5000',
    '-Djava.locale.providers=COMPAT,CLDR,SPI', '-Dlog4j2.disable.jmx=true',
    '-Dlog4j2.Script.enableLanguages=nashorn,js,javascript,ecmascript,groovy',
    '-Dmule.bootstrap.container.wrapper.class=com.zernova.runtime.module.boot.internal.ZernovaContainerBasicWrapper',
    `--module-path=${path.join(runtime, 'lib/boot')}`,
    '--add-modules=java.se,com.zernova.runtime.boot.log4j,com.zernova.runtime.logging,com.zernova.runtime.jpms.utils,com.fasterxml.jackson.core'];
  for (const target of ['java.base/java.lang', 'java.base/java.lang.reflect', 'java.base/java.lang.invoke', 'java.sql/java.sql']) args.push(`--add-opens=${target}=com.zernova.runtime.jpms.utils`);
  args.push('--module=com.zernova.boot/com.zernova.runtime.module.reboot.ZernovaContainerBootstrap');
  return args;
}
async function pidExists(pid: number): Promise<boolean> {
  if (!Number.isSafeInteger(pid) || pid <= 0) return false;
  try {
    process.kill(pid, 0); // Existence probe ONLY. Never a signal to a remembered PID.
    if (process.platform === 'linux') {
      const stat = await fs.readFile(`/proc/${pid}/stat`, 'utf8').catch(() => '');
      if (stat.slice(stat.lastIndexOf(')') + 2).startsWith('Z ')) return false;
    }
    return true;
  } catch (e) { if (errorCode(e) === 'ESRCH') return false; return true; }
}
export class BundledRuntime {
  readonly bundle: string; readonly instance: string; readonly project: string; readonly application: string;
  readonly runtime: string; readonly control: string;
  private readonly output: (text: string) => void;
  private readonly startupMs: number; private readonly deployMs: number; private readonly stopMs: number;
  constructor(options: RuntimeOptions) {
    this.bundle = path.resolve(options.bundle); this.instance = path.resolve(options.instance); this.project = path.resolve(options.project);
    assertAppName(options.application); this.application = options.application;
    if (within(this.project, this.instance) || within(this.bundle, this.instance)) throw new Error('Live runtime state must be outside the project and immutable extension package.');
    this.runtime = path.join(this.instance, 'runtime'); this.control = path.join(this.instance, 'control');
    this.output = options.onOutput ?? (() => {});
    this.startupMs = options.startupTimeoutMs ?? 120000; this.deployMs = options.deploymentTimeoutMs ?? 120000; this.stopMs = options.stopTimeoutMs ?? 45000;
    for (const value of [this.startupMs, this.deployMs, this.stopMs]) if (!Number.isInteger(value) || value < 500 || value > 300000) throw new Error('Invalid lifecycle timeout.');
  }
  identity(): Omit<PortableStatus, 'process' | 'runtime' | 'work_directory' | 'healthy' | 'applications'> {
    return { schema_version: 2, product: 'ZERNOVA Runtime', application_format_version: 1, distribution_version: RUNTIME_VERSION, kernel_version: RUNTIME_VERSION, upstream_baseline: ENGINE_VERSION, dataweave_version: '2.10.0', build_mode: 'source-built-development' };
  }
  async ownership(): Promise<void> {
    // Check every existing ancestor for redirection before creating private state.
    let ancestor = this.instance;
    while (ancestor !== path.dirname(ancestor)) {
      const stat = await fs.lstat(ancestor).catch(e => { if (e.code === 'ENOENT') return undefined; throw e; });
      if (stat && (stat.isSymbolicLink() || !stat.isDirectory())) throw new Error('Runtime state has an unsafe ancestor: ' + ancestor);
      ancestor = path.dirname(ancestor);
    }
    await fs.mkdir(path.dirname(this.instance), { recursive: true, mode: 0o700 });
    const marker = path.join(this.instance, 'studio-owner.json');
    if (!await exists(this.instance)) {
      try { await fs.mkdir(this.instance, { mode: 0o700 }); }
      catch (e) { if (errorCode(e) !== 'EEXIST') throw e; }
      try { await fs.writeFile(marker, JSON.stringify({ schemaVersion: 2, product: 'ZERNOVA Studio', projectRoot: this.project, application: this.application, engineVersion: ENGINE_VERSION, runtimeVersion: RUNTIME_VERSION, applicationFormat: 1 }) + '\n', { flag: 'wx', mode: 0o600 }); }
      catch (e) { if (errorCode(e) !== 'EEXIST') throw e; }
    }
    await ordinaryDirectory(this.instance);
    const stat = await fs.stat(this.instance);
    if (process.getuid && stat.uid !== process.getuid()) throw new Error('The runtime state directory belongs to another user.');
    const owner = await optionalJson<Record<string, unknown>>(marker, {});
    if (owner.schemaVersion !== 2 || owner.projectRoot !== this.project || owner.application !== this.application || owner.engineVersion !== ENGINE_VERSION) throw new Error('This runtime state directory is unrecognized or belongs to another project. It will not be reused or stopped.');
    if (owner.runtimeVersion !== RUNTIME_VERSION || owner.applicationFormat !== 1) throw new Error('This instance belongs to a different ZERNOVA runtime/application version. Stop it using its original Studio/runtime before upgrading; use a new private instance for this version.');
    if (process.platform !== 'win32') await fs.chmod(this.instance, 0o700);
  }
  private async mutate<T>(work: () => Promise<T>): Promise<T> {
    await this.ownership();
    const dir = path.join(this.instance, '.operation.lock'), id = randomUUID();
    try { await fs.mkdir(dir, { mode: 0o700 }); }
    catch (e) {
      if (errorCode(e) !== 'EEXIST') throw e;
      await ordinaryDirectory(dir);
      const prior = await optionalJson<{ pid?: number; id?: string }>(path.join(dir, 'owner.json'), {});
      if (!Number.isSafeInteger(prior.pid) || typeof prior.id !== 'string' || await pidExists(prior.pid!)) throw new Error('Another Studio window/operation owns this runtime action. Retry after it completes; no process was killed.');
      await fs.rm(dir, { recursive: true }); await fs.mkdir(dir, { mode: 0o700 });
    }
    await writeJson(path.join(dir, 'owner.json'), { pid: process.pid, id });
    try { return await work(); }
    finally { const current = await optionalJson<{ id?: string }>(path.join(dir, 'owner.json'), {}); if (current.id === id) await fs.rm(dir, { recursive: true }); }
  }
  private async state(): Promise<RuntimeState | undefined> { return optionalJson<RuntimeState | undefined>(path.join(this.instance, 'state.json'), undefined); }
  private async registry(): Promise<Record<string, AppRecord>> { return optionalJson<Record<string, AppRecord>>(path.join(this.instance, 'applications.json'), {}); }
  private async writeRegistry(data: Record<string, AppRecord>): Promise<void> { await writeJson(path.join(this.instance, 'applications.json'), data); }
  async controlRequest(command: 'STATUS' | 'STOP', overrideToken?: string): Promise<AgentEndpoint | undefined> {
    const endpoint = await optionalJson<AgentEndpoint | undefined>(path.join(this.control, 'endpoint.json'), undefined);
    if (!endpoint) return undefined;
    const state = await this.state();
    if (endpoint.schemaVersion !== 1 || endpoint.product !== 'ZERNOVA Runtime Control' || endpoint.runId !== state?.run_id || endpoint.pid !== state.pid || !Number.isInteger(endpoint.port) || endpoint.port < 1 || endpoint.port > 65535) throw new Error('The runtime control identity does not match this owned instance.');
    const token = overrideToken ?? (await fs.readFile(path.join(this.control, 'auth.token'), 'ascii')).trim();
    if (!/^[a-f0-9]{64}$/.test(token)) throw new Error('Invalid private runtime control token.');
    return new Promise<AgentEndpoint | undefined>((resolve, reject) => {
      const socket = net.createConnection({ host: '127.0.0.1', port: endpoint.port });
      let data = '', finished = false;
      const end = (error?: Error, result?: AgentEndpoint) => { if (finished) return; finished = true; socket.destroy(); error ? reject(error) : resolve(result); };
      socket.setTimeout(2500, () => end(new Error('Runtime control request timed out; no PID signal was sent.')));
      socket.on('connect', () => socket.write(`${command} ${token}\n`));
      socket.setEncoding('utf8');
      socket.on('data', text => {
        data += text;
        if (data.length > 4096) return end(new Error('Runtime control response exceeds its limit.'));
        if (!data.includes('\n')) return;
        try {
          const response = JSON.parse(data.slice(0, data.indexOf('\n'))) as AgentEndpoint & { error?: string };
          if (response.error) return end(new Error('Runtime control rejected the request: ' + response.error));
          if (response.pid !== endpoint.pid || response.runId !== endpoint.runId || response.product !== endpoint.product || response.schemaVersion !== 1) return end(new Error('Unrelated runtime control response was rejected.'));
          end(undefined, response);
        } catch { end(new Error('Invalid runtime control response.')); }
      });
      socket.on('error', e => { if (['ECONNREFUSED', 'ECONNRESET', 'EPIPE'].includes(errorCode(e) ?? '')) end(); else end(e); });
      socket.on('end', () => { if (!finished) end(); });
    });
  }
  private async alive(): Promise<boolean> {
    const state = await this.state(); if (!state) return false;
    const child = children.get(this.instance);
    if (child && child.pid === state.pid && (child.exitCode !== null || child.signalCode !== null)) return false;
    const response = await this.controlRequest('STATUS');
    if (response) return true;
    // A killed JVM can reset a connected socket before its exit event is
    // delivered. Allow that event to settle, but never infer process death
    // solely from a broken control connection.
    for (let attempt = 0; attempt < 10; attempt++) {
      if (child && child.pid === state.pid && (child.exitCode !== null || child.signalCode !== null)) return false;
      if (!await pidExists(state.pid)) return false;
      await sleep(25);
    }
    throw new Error('A recorded process may still be alive, but its authenticated control endpoint is unavailable. Studio will not signal that PID or start a duplicate. Inspect the retained runtime log.');
  }
  private async consoleText(state: RuntimeState, offset = 0): Promise<string> {
    if (!within(path.join(this.instance, 'runs'), state.console_log)) throw new Error('Runtime log path escaped its owned instance.');
    const f = await fs.open(state.console_log, 'r').catch(e => { if (e.code === 'ENOENT') return undefined; throw e; });
    if (!f) return '';
    try { const size = (await f.stat()).size; const start = Math.max(offset, size - MAX_LOG); const data = Buffer.alloc(Math.max(0, size - start)); await f.read(data, 0, data.length, start); return data.toString('utf8'); }
    finally { await f.close(); }
  }
  async status(): Promise<PortableStatus> {
    if (await exists(this.instance)) await this.ownership();
    const state = await this.state(), alive = await this.alive(), records = await this.registry();
    const applications: Record<string, AppStatus> = {};
    for (const [name, record] of Object.entries(records)) {
      assertAppName(name);
      const app: AppStatus = { name, state: 'STOPPED', healthy: false, deployment: 'NOT_RUNNING', endpoint: 'NOT_CHECKED' };
      if (alive && state) {
        const log = await this.consoleText(state, record.run_id === state.run_id ? record.log_offset : 0);
        const escaped = name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        const failures = [...log.matchAll(new RegExp("Failed to deploy artifact\\s*:?\\s*['\\\"\\[]?" + escaped + "(?:['\\\"\\]\\s,]|$)", 'g'))];
        const failure = failures.at(-1)?.index ?? -1, started = log.lastIndexOf("Started app '" + name + "'");
        const stopped = Math.max(log.lastIndexOf("Stopping application '" + name + "'"), log.lastIndexOf("Undeployed artifact '" + name + "'"));
        const anchor = await exists(path.join(this.runtime, 'apps', name + '-anchor.txt'));
        if (failure > started) Object.assign(app, { state: 'UNHEALTHY', deployment: 'FAILED' });
        else if (anchor && started >= 0 && stopped < started) Object.assign(app, { state: 'DEPLOYED', deployment: 'DEPLOYED', healthy: true });
        else Object.assign(app, { state: 'DEPLOYING', deployment: 'PENDING' });
      }
      applications[name] = app;
    }
    return { ...this.identity(), process: alive ? 'RUNNING' : 'STOPPED', runtime: alive ? (state?.runtime_ready ? 'READY' : 'STARTING') : 'STOPPED', pid: alive ? state?.pid : undefined, run_id: state?.run_id, work_directory: this.instance, healthy: alive && !!state?.runtime_ready && Object.values(applications).every(a => a.healthy), applications };
  }
  async doctor(signal?: AbortSignal): Promise<Record<string, unknown>> {
    const info = await bundleInfo(this.bundle); const integrity = await verifyBundle(this.bundle, signal); const java = await javaExecutable(this.bundle, signal);
    return { ...this.identity(), dependency_check: 'PASS', target: info.target, java_version: java.version, java_bundled: true, engine_bundled: true, network_used: false, integrity, adapter_version: 2, customer_release: 'NOT_CLEARED' };
  }
  private async ensureEngine(signal?: AbortSignal): Promise<void> {
    await verifyBundle(this.bundle, signal);
    if (!await exists(this.runtime)) {
      const tmp = path.join(this.instance, 'runtime-preparing-' + randomUUID());
      try { await fs.cp(path.join(this.bundle, 'engine'), tmp, { recursive: true, errorOnExist: true, force: false }); cancelled(signal); await fs.rename(tmp, this.runtime); }
      finally { await fs.rm(tmp, { recursive: true, force: true }); }
    }
    await ordinaryDirectory(this.runtime);
    const manifest = await readJson<{ files: Record<string, { sha256: string }> }>(path.join(this.bundle, 'checksums.json'));
    for (const [name, record] of Object.entries(manifest.files)) {
      if (!name.startsWith('engine/')) continue;
      cancelled(signal); const target = path.join(this.runtime, name.slice(7));
      const stat = await fs.lstat(target);
      if (stat.isSymbolicLink() || !stat.isFile() || await fileSha(target) !== record.sha256) throw new Error('Live instance has an altered engine file: ' + name + '. Studio will not replace or execute it silently.');
    }
    for (const sub of ['apps', 'logs', 'domains', 'domains/default', 'lib', 'lib/user']) { await fs.mkdir(path.join(this.runtime, sub), { recursive: true }); await ordinaryDirectory(path.join(this.runtime, sub)); }
  }
  async start(port: number, signal?: AbortSignal): Promise<PortableStatus> {
    return this.mutate(() => this.startOwned(port, signal));
  }
  private async startOwned(port: number, signal?: AbortSignal): Promise<PortableStatus> {
    cancelled(signal);
    if (await this.alive()) { const state = (await this.state())!; if (state.config.http_port !== port) throw new Error('Stop the project runtime before changing its port.'); return this.status(); }
    await checkPort(port); const info = await bundleInfo(this.bundle); await this.ensureEngine(signal); const java = await javaExecutable(this.bundle, signal);
    await fs.mkdir(this.control, { recursive: true, mode: 0o700 }); await ordinaryDirectory(this.control);
    const runId = randomUUID(), token = randomBytes(32).toString('hex');
    await fs.writeFile(path.join(this.control, 'auth.token'), token, { mode: 0o600 });
    await fs.writeFile(path.join(this.control, 'run-id.txt'), runId, { mode: 0o600 });
    await fs.rm(path.join(this.control, 'endpoint.json'), { force: true });
    const logdir = path.join(this.instance, 'runs', runId); await fs.mkdir(logdir, { recursive: true, mode: 0o700 });
    const logfile = path.join(logdir, 'console.log'), output = await fs.open(logfile, 'wx', 0o600);
    await output.write(`ZERNOVA Runtime ${RUNTIME_VERSION} — bundled source-built development engine\nCopyright (c) 2003-2015 MuleSoft Inc.\nPowered by Mule. MuleSoft is Open for Integration. http://www.MuleSoft.com\n`);
    const args = javaArguments(this.bundle, this.runtime, this.control, info.controlAgent, port);
    await writeJson(path.join(logdir, 'launch.json'), { executable: java.executable, args, target: info.target, javaVersion: java.version, networkUsed: false });
    let spawnError: Error | undefined;
    const child = spawn(java.executable, args, { cwd: this.runtime, env: cleanEnvironment(), shell: false, detached: true, windowsHide: true, stdio: ['ignore', output.fd, output.fd] });
    child.on('error', e => { spawnError = e; });
    await new Promise<void>((resolve, reject) => { child.once('spawn', resolve); child.once('error', reject); }).finally(() => output.close());
    if (!child.pid) throw new Error('The bundled runtime did not receive a process identity.');
    children.set(this.instance, child); child.unref();
    const state: RuntimeState = { schema_version: 2, pid: child.pid, run_id: runId, console_log: logfile, runtime_ready: false, started_utc: new Date().toISOString(), config: { http_host: '127.0.0.1', http_port: port, heap_mb: 768 } };
    await writeJson(path.join(this.instance, 'state.json'), state);
    this.output(`Starting bundled ZERNOVA Runtime on ${info.target}; console: ${logfile}\n`);
    try {
      const deadline = Date.now() + this.startupMs;
      while (Date.now() < deadline) {
        cancelled(signal);
        if (spawnError || child.exitCode !== null || child.signalCode !== null) throw new Error(`Runtime exited during startup (${child.exitCode ?? child.signalCode ?? spawnError?.message}). Inspect ${logfile}\n${(await this.consoleText(state)).slice(-5000)}`);
        const text = await this.consoleText(state);
        if (text.includes('ZERNOVA Runtime is ready (deployment scan every ') && await this.controlRequest('STATUS')) {
          state.runtime_ready = true; await writeJson(path.join(this.instance, 'state.json'), state); return this.status();
        }
        await wait(250, signal);
      }
      throw new Error(`Runtime startup timed out. Inspect ${logfile}`);
    } catch (error) {
      try { await this.stopOwned(); } catch (cleanup) { this.output('Startup cleanup requires attention: ' + String(cleanup) + '\n'); }
      throw error;
    }
  }
  private async foreignApplications(): Promise<string[]> {
    const names = new Set(Object.keys(await this.registry()));
    const directory = path.join(this.runtime, 'apps');
    if (await exists(directory)) {
      for (const entry of await fs.readdir(directory, { withFileTypes: true })) {
        if (entry.name.startsWith('.')) continue;
        if (entry.isDirectory()) names.add(entry.name);
        else if (entry.name.endsWith('-anchor.txt')) names.add(entry.name.slice(0, -11));
        else if (entry.name.endsWith('.jar')) names.add(entry.name.slice(0, -4));
      }
    }
    return [...names].filter(n => n !== this.application);
  }
  async deploy(filename: string, name = this.application, signal?: AbortSignal): Promise<{ application: AppStatus; artifact_sha256: string }> {
    if (name !== this.application) throw new Error('A Studio runtime instance may deploy only its own project application.');
    return this.mutate(async () => {
      cancelled(signal); let state = await this.state();
      if (!state?.runtime_ready || !await this.alive()) throw new Error('Start the bundled runtime before deployment.');
      const foreign = await this.foreignApplications(); if (foreign.length) throw new Error('Unexpected applications in this private instance: ' + foreign.join(', '));
      const artifact = await validateArtifact(filename); if (artifact.name !== name) throw new Error('The artifact belongs to a different application.');
      const destination = path.join(this.runtime, 'apps', name + '.jar'), anchor = path.join(this.runtime, 'apps', name + '-anchor.txt'), appdir = path.join(this.runtime, 'apps', name);
      if (await exists(anchor) || await exists(destination) || await exists(appdir)) throw new Error('Undeploy the previous application before redeploying. No files were overwritten.');
      // One app owns this JVM. A replacement gets fresh shared HTTP services:
      // native Windows acceptance exposed handlers missing after live undeploy.
      // Hold the same mutation lock across authenticated stop/start; never retry
      // application requests or claim that a deployed banner proves HTTP health.
      if (state.deployment_attempted) {
        const port = state.config.http_port;
        this.output('Restarting the private project runtime before replacement deployment.\n');
        await this.stopOwned();
        await this.startOwned(port, signal);
        state = (await this.state())!;
      }
      const tmp = path.join(this.instance, 'deploying-' + randomUUID() + '.tmp');
      try {
        await fs.copyFile(filename, tmp, fs.constants.COPYFILE_EXCL);
        if (await fileSha(tmp) !== artifact.sha256) throw new Error('Artifact changed during deployment copy.');
        cancelled(signal);
        const logOffset = (await fs.stat(state.console_log)).size;
        state.deployment_attempted = true;
        await writeJson(path.join(this.instance, 'state.json'), state);
        const registry = await this.registry(); registry[name] = { sha256: artifact.sha256, run_id: state.run_id, log_offset: logOffset, deployed_utc: new Date().toISOString() };
        await this.writeRegistry(registry); await fs.rename(tmp, destination);
        const deadline = Date.now() + this.deployMs;
        while (Date.now() < deadline) {
          cancelled(signal); const current = await this.status(), app = current.applications[name];
          if (current.process !== 'RUNNING') throw new Error('Runtime stopped during application deployment.');
          if (app.state === 'UNHEALTHY') throw new Error('Application deployment failed; a running JVM is not application success. Inspect application/runtime logs.\n' + (await this.consoleText(state, logOffset)).slice(-6000));
          if (app.deployment === 'DEPLOYED' && app.healthy) return { application: app, artifact_sha256: artifact.sha256 };
          await wait(300, signal);
        }
        throw new Error('Application deployment timed out; status and logs are retained. Stop this project runtime before removing a failed deployment.');
      } finally { await fs.rm(tmp, { force: true }); }
    });
  }
  async undeploy(name = this.application, signal?: AbortSignal): Promise<Record<string, unknown>> {
    if (name !== this.application) throw new Error('This instance can undeploy only its own project application.');
    return this.mutate(async () => {
      cancelled(signal); const alive = await this.alive(); const anchor = path.join(this.runtime, 'apps', name + '-anchor.txt'), appdir = path.join(this.runtime, 'apps', name), archive = path.join(this.runtime, 'apps', name + '.jar');
      if (alive && !await exists(anchor) && (await exists(appdir) || await exists(archive))) throw new Error('This deployment has no successful anchor. Stop the owned runtime before cleaning the failed or pending application.');
      if (alive && await exists(anchor)) {
        await fs.unlink(anchor); const deadline = Date.now() + 35000;
        while (Date.now() < deadline && (await exists(appdir) || await exists(archive))) await wait(250, signal);
        if (await exists(appdir) || await exists(archive)) throw new Error('Undeployment did not complete. No live deployment files were forcibly deleted.');
      }
      if (!alive) {
        for (const filename of [anchor, archive, appdir]) {
          if (!within(path.join(this.runtime, 'apps'), filename) || (await fs.lstat(filename).catch(() => undefined))?.isSymbolicLink()) throw new Error('Unsafe deployment cleanup path.');
          await fs.rm(filename, { recursive: true, force: true });
        }
        // The engine's private application working directory is owned by this app.
        const work = path.join(this.runtime, '.mule', name);
        if ((await fs.lstat(work).catch(() => undefined))?.isSymbolicLink()) throw new Error('Unsafe application working directory.');
        await fs.rm(work, { recursive: true, force: true });
      }
      const registry = await this.registry(); delete registry[name]; await this.writeRegistry(registry);
      return { application: name, state: 'NOT_DEPLOYED', process_stopped: !alive };
    });
  }
  private async stopOwned(): Promise<Record<string, unknown>> {
    const state = await this.state(); if (!state) return { process: 'STOPPED', forced: false, signal_sent: false };
    const child = children.get(this.instance);
    if (child && child.pid === state.pid && (child.exitCode !== null || child.signalCode !== null)) return { process: 'STOPPED', forced: false, signal_sent: false };
    const response = await this.controlRequest('STOP');
    if (!response) {
      if (await pidExists(state.pid)) throw new Error('The recorded JVM cannot be authenticated. No signal was sent to its PID. Inspect its runtime log before recovery.');
      return { process: 'STOPPED', forced: false, signal_sent: false };
    }
    const deadline = Date.now() + this.stopMs;
    while (Date.now() < deadline) {
      if ((child && child.pid === state.pid && (child.exitCode !== null || child.signalCode !== null)) || !await pidExists(state.pid)) {
        state.runtime_ready = false; await writeJson(path.join(this.instance, 'state.json'), state);
        return { process: 'STOPPED', forced: false, signal_sent: false, shutdown: 'authenticated JVM self-shutdown', pid: state.pid };
      }
      await sleep(250);
    }
    throw new Error('The authenticated JVM did not finish graceful shutdown within the limit. Studio did not kill an unverified PID. The instance remains observable.');
  }
  async stop(signal?: AbortSignal): Promise<Record<string, unknown>> {
    return this.mutate(async () => { cancelled(signal); const foreign = await this.foreignApplications(); if (foreign.length) throw new Error('Studio will not stop an instance containing other applications: ' + foreign.join(', ')); return this.stopOwned(); });
  }
  async logs(name = this.application, lines = 1000): Promise<{ log: string; lines: string[] }> {
    if (name !== this.application || !Number.isInteger(lines) || lines < 1 || lines > 10000) throw new Error('Invalid application log request.');
    const zernovaLog = path.join(this.runtime, 'logs', 'zernova-app-' + name + '.log');
    const logfile = await exists(zernovaLog) ? zernovaLog : path.join(this.runtime, 'logs', 'mule-app-' + name + '.log');
    if ((await fs.lstat(logfile).catch(() => undefined))?.isSymbolicLink()) throw new Error('Application log is an unexpected symlink.');
    const file = await fs.open(logfile, 'r').catch(e => { if (e.code === 'ENOENT') return undefined; throw e; });
    if (!file) return { log: logfile, lines: [] };
    try { const size = (await file.stat()).size, start = Math.max(0, size - MAX_LOG), data = Buffer.alloc(size - start); await file.read(data, 0, data.length, start); return { log: logfile, lines: data.toString('utf8').split(/\r?\n/).slice(-lines) }; }
    finally { await file.close(); }
  }
  /** Internal typed adapter only. No shell, arbitrary paths/flags, or process API is exposed to webviews. */
  async command(args: string[], signal?: AbortSignal): Promise<Record<string, unknown>> {
    const [command, ...rest] = args;
    cancelled(signal);
    if (command === 'version' && !rest.length) { await bundleInfo(this.bundle); return this.identity(); }
    if (command === 'doctor' && (rest.length === 0 || (rest.length === 2 && rest[0] === '--port'))) return this.doctor(signal);
    if (command === 'status' && !rest.length) return this.status() as unknown as Record<string, unknown>;
    if (command === 'start' && rest.length === 2 && rest[0] === '--port') return this.start(Number(rest[1]), signal) as unknown as Record<string, unknown>;
    if (command === 'stop' && !rest.length) return this.stop(signal);
    if (command === 'undeploy' && rest.length === 1) return this.undeploy(rest[0], signal);
    if (command === 'deploy' && rest.length === 3 && rest[1] === '--name') return this.deploy(rest[0], rest[2], signal) as unknown as Record<string, unknown>;
    if (command === 'logs' && rest.length === 4 && rest[0] === '--app' && rest[2] === '--lines') return this.logs(rest[1], Number(rest[3]));
    throw new Error('Unsupported bundled-runtime adapter operation.');
  }
  package(project = this.project, signal?: AbortSignal) { return packageApplication(this.bundle, project, signal); }
}
