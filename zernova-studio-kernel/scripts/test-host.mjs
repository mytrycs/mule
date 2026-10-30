/** Actual VS Code desktop host test, using the packaged/installed subject VSIX. */
import { resolveCliPathFromVSCodeExecutablePath } from '@vscode/test-electron';
import { mkdtemp, mkdir, writeFile, copyFile, readFile } from 'node:fs/promises';
import { spawnSync, spawn } from 'node:child_process';
import { resolve, join } from 'node:path';
import { tmpdir } from 'node:os';
import { createHash } from 'node:crypto';

const evidenceRoot = resolve(process.env.ZERNOVA_HOST_EVIDENCE_DIR || 'evidence');
const executable = process.env.ZERNOVA_VSCODE_BIN;
if (!executable) throw new Error('Set ZERNOVA_VSCODE_BIN to a real VS Code desktop executable. No test host is downloaded automatically.');
const cli = process.env.ZERNOVA_VSCODE_CLI || resolveCliPathFromVSCodeExecutablePath(executable);
const root = await mkdtemp(join(tmpdir(), 'zernova-studio-host-test-'));
const profile = join(root, 'profile');
const extensions = join(root, 'extensions');
const sharedData = join(root, 'shared-data');
const workspace = join(root, 'isolated-fixture-workspace');
const harness = join(root, 'harness');
await Promise.all([mkdir(join(profile, 'User'), { recursive: true }), mkdir(extensions), mkdir(workspace), mkdir(harness), mkdir(evidenceRoot, { recursive: true })]);
await writeFile(join(profile, 'User', 'settings.json'), JSON.stringify({
  'telemetry.telemetryLevel': 'off', 'update.mode': 'none', 'extensions.autoCheckUpdates': false, 'extensions.autoUpdate': false,
  'workbench.startupEditor': 'none', 'workbench.colorTheme': 'ZERNOVA Studio Light', 'workbench.tips.enabled': false,
  'security.workspace.trust.enabled': false, 'chat.disableAIFeatures': true,
}, null, 2));
await writeFile(join(workspace, 'ordinary.xml'), '<ordinary><unchanged /></ordinary>\n');
await writeFile(join(harness, 'package.json'), JSON.stringify({ name: 'zernova-ui-test-harness', publisher: 'local-tests', version: '0.0.1', engines: { vscode: '^1.96.0' }, activationEvents: ['onStartupFinished'], main: 'index.js' }));
await writeFile(join(harness, 'index.js'), `const vscode = require('vscode');
exports.activate = () => { setTimeout(async () => {
  try { await require('./tests.cjs').run(); } catch (error) { console.error('ZERNOVA host acceptance failed', error); }
  await vscode.commands.executeCommand('workbench.action.quit');
}, 350); };
`);
await copyFile(resolve('tests/extension-host.cjs'), join(harness, 'tests.cjs'));
const manifest = JSON.parse(await readFile('package.json', 'utf8'));
const vsix = process.env.ZERNOVA_TEST_VSIX ? resolve(process.env.ZERNOVA_TEST_VSIX) : resolve(`dist/${manifest.name}-${manifest.version}.vsix`);
const vsixSha256 = createHash('sha256').update(await readFile(vsix)).digest('hex');
const installArgs = ['--no-sandbox', '--user-data-dir', profile, '--extensions-dir', extensions, '--shared-data-dir', sharedData, '--install-extension', vsix, '--force'];
const installed = spawnSync(cli, installArgs, { encoding: 'utf8', timeout: 60000 });
await writeFile(join(evidenceRoot, 'host-install.log'), JSON.stringify({ command: [cli, ...installArgs], exitCode: installed.status, stdout: installed.stdout, stderr: installed.stderr }, null, 2));
if (installed.status !== 0) throw new Error(`VSIX installation failed: ${installed.stderr || installed.stdout}`);
await writeFile(join(evidenceRoot, 'host-test-environment.json'), JSON.stringify({ generatedAt: new Date().toISOString(), executable, profile, extensions, sharedData, workspace, subjectVsix: vsix, vsixSha256, display: process.env.DISPLAY || null, subjectMode: 'installed VSIX; only test harness is development extension' }, null, 2));
const phases = [];
await writeFile(join(evidenceRoot, 'extension-host-results.json'), JSON.stringify({ status: 'RUNNING', subjectVsix: vsix, vsixSha256 }, null, 2));
try {
for (const phase of ['primary', 'restart']) {
const evidence = join(evidenceRoot, `extension-host-${phase}.json`);
const args = [workspace, '--no-sandbox', '--disable-gpu', '--user-data-dir', profile, '--extensions-dir', extensions, '--shared-data-dir', sharedData, '--extensionDevelopmentPath', harness, '--skip-welcome', '--skip-release-notes', '--disable-workspace-trust', '--disable-updates', '--disable-background-networking', '--host-rules=MAP * ~NOTFOUND'];
const childEnv = { ...process.env, ZERNOVA_HOST_EVIDENCE: evidence, ZERNOVA_HOST_PHASE: phase, ZERNOVA_SUBJECT_EXTENSIONS: extensions, JAVA_HOME: '/nonexistent-zernova-ui-test-java', ZERNOVA_JAVA_HOME: '/nonexistent-zernova-ui-test-java', ZERNOVA_MAVEN: '/nonexistent-zernova-ui-test-maven', ZERNOVA_WORK_DIR: '/nonexistent-zernova-ui-test-runtime' };
delete childEnv.ELECTRON_RUN_AS_NODE;
await new Promise((resolveExit, reject) => {
  const child = spawn(executable, args, { env: childEnv, stdio: 'inherit' });
  const timeout = setTimeout(() => { child.kill('SIGTERM'); reject(new Error(`Host phase ${phase} exceeded 90 seconds`)); }, 90000);
  child.on('error', error => { clearTimeout(timeout); reject(error); });
  child.on('close', (code, signal) => { clearTimeout(timeout); code === 0 ? resolveExit() : reject(new Error(`Host phase ${phase} exit ${code}, signal ${signal}`)); });
});
const result = JSON.parse(await readFile(evidence, 'utf8'));
phases.push(result);
console.log(JSON.stringify(result, null, 2));
if (result.status !== 'PASS') throw new Error(`Host acceptance phase ${phase} failed; inspect ${evidence}`);
}
} catch (error) {
  await writeFile(join(evidenceRoot, 'extension-host-results.json'), JSON.stringify({ status: 'FAIL', subjectVsix: vsix, vsixSha256, phasesCompleted: phases, error: String(error), completedAt: new Date().toISOString() }, null, 2));
  throw error;
}
const unchangedVsix = createHash('sha256').update(await readFile(vsix)).digest('hex') === vsixSha256;
const combined = { status: phases.every(phase => phase.status === 'PASS') && unchangedVsix ? 'PASS' : 'FAIL', version: phases[0].version, platform: phases[0].platform, architecture: phases[0].architecture, subjectVsix: vsix, vsixSha256, unchangedVsixDuringTest: unchangedVsix, passed: phases.reduce((sum, phase) => sum + phase.passed, 0), failed: phases.reduce((sum, phase) => sum + phase.failed, 0), phases: phases.map(phase => ({ phase: phase.phase, passed: phase.passed, failed: phase.failed, evidence: `extension-host-${phase.phase}.json` })), completedAt: new Date().toISOString() };
await writeFile(join(evidenceRoot, 'extension-host-results.json'), JSON.stringify(combined, null, 2));
console.log(JSON.stringify(combined, null, 2));
if (combined.status !== 'PASS') process.exitCode = 1;
