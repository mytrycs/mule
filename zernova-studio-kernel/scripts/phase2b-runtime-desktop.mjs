/** Isolated real desktop for Phase 2B runtime negative tests. No primary profile is used. */
import { spawn, spawnSync } from 'node:child_process';
import { readFile, writeFile, mkdir, access } from 'node:fs/promises';
import { join, resolve } from 'node:path';
import { createHash } from 'node:crypto';

const root = process.env.ZERNOVA_PHASE2B_HOME || '/tmp/zernova-studio-phase2b-runtime-review';
const executable = process.env.ZERNOVA_VSCODE_BIN || '/tmp/zernova-studio-tools/VSCode-linux-x64/code';
const cli = process.env.ZERNOVA_VSCODE_CLI || join(executable, '..', 'bin', 'code');
const xvfb = process.env.ZERNOVA_XVFB_BIN || '/tmp/zernova-studio-tools/Xvfb-local';
const display = process.env.DISPLAY || ':102';
const cdpPort = Number(process.env.ZERNOVA_PHASE2B_CDP_PORT || 9226);
const profile = join(root, 'profile'), extensions = join(root, 'extensions'), shared = join(root, 'shared-data');
const workspace = join(root, 'workspace');
const statePath = join(root, 'desktop.json');
const evidence = resolve('evidence/phase2b-runtime-host');
const env = { ...process.env, DISPLAY: display,
  LD_LIBRARY_PATH: process.env.ZERNOVA_TEST_LIBS || '/tmp/zernova-studio-tools/xvfb/usr/lib/x86_64-linux-gnu' };
delete env.ELECTRON_RUN_AS_NODE;

async function readState() { try { return JSON.parse(await readFile(statePath, 'utf8')); } catch { return {}; } }
async function processInfo(pid) {
  if (!Number.isInteger(pid) || pid < 1) return null;
  try { return { command: (await readFile(`/proc/${pid}/cmdline`, 'utf8')).split('\0').filter(Boolean),
    stat: (await readFile(`/proc/${pid}/stat`, 'utf8')).split(') ')[1].split(' ') }; } catch { return null; }
}
async function launch(binary, args, logPath) {
  const { open } = await import('node:fs/promises');
  const log = await open(logPath, 'a');
  const child = spawn(binary, args, { env, detached: true, stdio: ['ignore', log.fd, log.fd] });
  await new Promise((ok, fail) => { child.once('spawn', ok); child.once('error', fail); });
  child.unref(); await log.close();
  const info = await processInfo(child.pid);
  return { pid: child.pid, startTicks: info?.stat[19], command: [binary, ...args] };
}
async function stopOwned(record, required) {
  if (!record) return { stopped: true, signal: false };
  const info = await processInfo(record.pid);
  if (!info || info.stat[0] === 'Z') return { stopped: true, signal: false };
  if (info.stat[19] !== record.startTicks || !info.command.some(v => v === required)) throw new Error('Refusing to stop a process whose identity differs');
  process.kill(record.pid, 'SIGTERM');
  for (let i = 0; i < 100; i++) { const next = await processInfo(record.pid); if (!next || next.stat[0] === 'Z') return { stopped: true, signal: 'SIGTERM' }; await new Promise(r => setTimeout(r, 100)); }
  throw new Error('Owned desktop process did not stop; no forced termination requested');
}

await mkdir(root, { recursive: true }); await mkdir(evidence, { recursive: true });
const mode = process.argv[2] || 'start';
const manifest = JSON.parse(await readFile('package.json', 'utf8'));
const vsix = resolve(`dist/${manifest.name}-${manifest.version}.vsix`);
if (mode === 'stop') {
  const state = await readState();
  const result = { desktop: await stopOwned(state.desktop, profile), display: await stopOwned(state.display, display), time: new Date().toISOString() };
  await writeFile(join(evidence, 'desktop-cleanup.json'), JSON.stringify(result, null, 2)); console.log(JSON.stringify(result));
} else if (mode === 'status') {
  const state = await readState(); console.log(JSON.stringify({ ...state, desktopAlive: !!(await processInfo(state.desktop?.pid)), displayAlive: !!(await processInfo(state.display?.pid)) }, null, 2));
} else if (mode === 'start' || mode === 'install') {
  for (const path of [executable, cli, xvfb, vsix]) await access(path);
  await Promise.all([mkdir(join(profile, 'User'), { recursive: true }), mkdir(extensions, { recursive: true }), mkdir(shared, { recursive: true }), mkdir(workspace, { recursive: true })]);
  const previous = await readState();
  if (previous.desktop && (await processInfo(previous.desktop.pid))?.stat[0] !== 'Z' && await processInfo(previous.desktop.pid)) throw new Error('Desktop already running; close it before installing/restarting');
  const settings = {
    'telemetry.telemetryLevel': 'off', 'update.mode': 'none', 'extensions.autoUpdate': false, 'extensions.autoCheckUpdates': false,
    'workbench.startupEditor': 'none', 'workbench.colorTheme': 'ZERNOVA Studio Light', 'workbench.tips.enabled': false,
    'security.workspace.trust.enabled': true, 'chat.disableAIFeatures': true,
    'window.title': '${activeEditorShort}${separator}ZERNOVA Runtime Negative Tests${separator}${appName}',
    'files.autoSave': 'off', 'window.restoreWindows': 'all',
  };
  await writeFile(join(profile, 'User', 'settings.json'), JSON.stringify(settings, null, 2));
  const installed = spawnSync(cli, ['--no-sandbox', '--user-data-dir', profile, '--extensions-dir', extensions, '--shared-data-dir', shared,
    '--install-extension', vsix, '--force'], { env, encoding: 'utf8', timeout: 60000 });
  const installRecord = { version: manifest.version, extensionId: manifest.publisher + '.' + manifest.name, vsix,
    sha256: createHash('sha256').update(await readFile(vsix)).digest('hex'), exitCode: installed.status, stdout: installed.stdout, stderr: installed.stderr };
  await writeFile(join(evidence, 'install.json'), JSON.stringify(installRecord, null, 2));
  if (installed.status !== 0) throw new Error('VSIX installation failed; see evidence/phase2b-runtime-host/install.json');
  if (mode === 'install') { console.log(JSON.stringify(installRecord, null, 2)); process.exit(0); }
  let displayRecord = previous.display;
  if (!displayRecord || !(await processInfo(displayRecord.pid)) || (await processInfo(displayRecord.pid)).stat[0] === 'Z') {
    displayRecord = await launch(xvfb, [display, '-screen', '0', '1920x1080x24', '-ac', '-nolisten', 'tcp'], join(evidence, 'display.log'));
    await new Promise(r => setTimeout(r, 300));
  }
  const desktop = await launch(executable, [workspace, '--no-sandbox', '--disable-gpu', '--user-data-dir', profile, '--extensions-dir', extensions,
    '--shared-data-dir', shared, '--skip-welcome', '--skip-release-notes', '--disable-updates', '--disable-background-networking',
    '--remote-debugging-address=127.0.0.1', `--remote-debugging-port=${cdpPort}`], join(evidence, 'desktop.log'));
  const state = { root, profile, extensions, shared, workspace, displayName: display, cdpPort, desktop, display: displayRecord, install: installRecord,
    startedAt: new Date().toISOString(), platform: process.platform, architecture: process.arch, executable };
  await writeFile(statePath, JSON.stringify(state, null, 2)); await writeFile(join(evidence, 'environment.json'), JSON.stringify(state, null, 2));
  console.log(JSON.stringify(state, null, 2));
} else throw new Error('Usage: node scripts/phase2b-runtime-desktop.mjs start|install|status|stop');
