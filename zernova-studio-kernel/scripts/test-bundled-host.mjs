import { build } from 'esbuild';
import { mkdir, writeFile, rm } from 'node:fs/promises';
import { spawnSync, spawn } from 'node:child_process';
import path from 'node:path';
import os from 'node:os';

const root = process.env.ZN_STUDIO_TEST_HOME || path.join(os.homedir(), '.zernova-studio-tests', new Date().toISOString().replace(/[:.]/g, '-'));
for (const rel of ['workspace', 'extensions', 'profile/User', 'harness']) await mkdir(path.join(root, rel), { recursive: true });
for (const name of ['results.json', 'ready.json', 'fatal.txt', 'complete.json']) await rm(path.join(root, name), { force: true });
await writeFile(path.join(root, 'profile/User/settings.json'), JSON.stringify({
  'telemetry.telemetryLevel': 'off', 'update.mode': 'none', 'extensions.autoUpdate': false,
  'extensions.autoCheckUpdates': false, 'workbench.startupEditor': 'none', 'workbench.colorTheme': 'ZERNOVA Studio Light',
  'workbench.tips.enabled': false, 'chat.disableAIFeatures': true,
}, null, 2));
await writeFile(path.join(root, 'harness/package.json'), JSON.stringify({ name: 'zernova-bundled-host-test', publisher: 'local-tests', version: '0.0.1', engines: { vscode: '^1.96.0' }, activationEvents: ['onStartupFinished'], main: 'index.cjs' }));
await build({ entryPoints: ['tests/bundled-runtime-host.ts'], bundle: true, platform: 'node', format: 'cjs', external: ['vscode'], outfile: path.join(root, 'harness/tests.cjs') });
await writeFile(path.join(root, 'harness/index.cjs'), `exports.activate = async () => { try { await require('./tests.cjs').run(); } catch (error) { require('node:fs').writeFileSync(${JSON.stringify(path.join(root, 'fatal.txt'))}, String(error.stack || error)); } };`);
const cli = process.env.ZERNOVA_VSCODE_CLI || '/Applications/Visual Studio Code.app/Contents/Resources/app/bin/code';
const vsix = path.resolve('dist/zernova-studio-kernel-preview-0.3.0-darwin-x64.vsix');
const options = ['--user-data-dir', path.join(root, 'profile'), '--extensions-dir', path.join(root, 'extensions')];
const installed = spawnSync(cli, [...options, '--install-extension', vsix, '--force'], { encoding: 'utf8', timeout: 120000 });
await writeFile(path.join(root, 'install.json'), JSON.stringify({ exitCode: installed.status, stdout: installed.stdout, stderr: installed.stderr }, null, 2));
if (installed.status !== 0) throw new Error('VSIX installation failed: ' + installed.stderr);
await mkdir('evidence', { recursive: true });
await writeFile('evidence/active-bundled-host.json', JSON.stringify({ root, vsix }, null, 2));
const child = spawn(cli, [...options, '--new-window', '--extensionDevelopmentPath', path.join(root, 'harness'), '--skip-welcome', '--skip-release-notes', path.join(root, 'workspace')], {
  env: { ...process.env, ZN_STUDIO_TEST_ROOT: root, JAVA_HOME: '/nonexistent-studio-test-java', ZERNOVA_JAVA_HOME: '/nonexistent-studio-test-java', ZERNOVA_MAVEN: '/nonexistent-studio-test-maven' },
  stdio: 'ignore', detached: true,
});
child.unref();
console.log(JSON.stringify({ root, vsix, pid: child.pid }, null, 2));
