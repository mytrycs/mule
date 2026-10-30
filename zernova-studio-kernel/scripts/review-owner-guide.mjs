/** Render the standalone guide in Chromium through a private VS Code test window.
 * The HTML bytes are unchanged; a test-only webview displays the local artifact.
 */
import { spawn } from 'node:child_process';
import { mkdtemp, mkdir, readFile, writeFile } from 'node:fs/promises';
import { resolve, join } from 'node:path';
import { tmpdir } from 'node:os';
import { createHash } from 'node:crypto';
import { chromium } from 'playwright';
import assert from 'node:assert/strict';

const guide = resolve(process.argv[2] || 'dist/TESTING_STEP_BY_STEP.html');
const bytes = await readFile(guide);
const root = await mkdtemp(join(tmpdir(), 'zernova-guide-review-'));
const evidence = resolve('evidence/phase2b/guide-review');
const executable = process.env.ZERNOVA_VSCODE_BIN || '/tmp/zernova-studio-tools/VSCode-linux-x64/code';
const xvfb = process.env.ZERNOVA_XVFB_BIN || '/tmp/zernova-studio-tools/Xvfb-local';
const displayName = process.env.ZERNOVA_GUIDE_DISPLAY || ':104';
const cdpPort = Number(process.env.ZERNOVA_GUIDE_CDP_PORT || 9227);
const profile = join(root, 'profile'), harness = join(root, 'harness');
const env = { ...process.env, DISPLAY: displayName, LD_LIBRARY_PATH: process.env.ZERNOVA_TEST_LIBS || '/tmp/zernova-studio-tools/xvfb/usr/lib/x86_64-linux-gnu', ZERNOVA_GUIDE_FILE: guide };
delete env.ELECTRON_RUN_AS_NODE;
for (const path of [join(profile, 'User'), harness, evidence]) await mkdir(path, { recursive: true });
await writeFile(join(profile, 'User/settings.json'), JSON.stringify({ 'telemetry.telemetryLevel': 'off', 'update.mode': 'none', 'extensions.autoUpdate': false, 'workbench.startupEditor': 'none', 'security.workspace.trust.enabled': false, 'chat.disableAIFeatures': true, 'window.title': 'ZERNOVA Studio Guide Review' }));
await writeFile(join(harness, 'package.json'), JSON.stringify({ name: 'zernova-guide-review-harness', publisher: 'local-tests', version: '0.0.1', engines: { vscode: '^1.96.0' }, activationEvents: ['onStartupFinished'], main: 'index.cjs' }));
await writeFile(join(harness, 'index.cjs'), `const vscode = require('vscode'); const fs = require('node:fs');
exports.activate = async () => { await vscode.commands.executeCommand('workbench.action.closeSidebar'); await vscode.commands.executeCommand('workbench.action.closeAuxiliaryBar'); const panel = vscode.window.createWebviewPanel('localTests.zernovaGuide', 'Owner guide review', vscode.ViewColumn.One, { enableScripts: false }); panel.webview.html = fs.readFileSync(process.env.ZERNOVA_GUIDE_FILE, 'utf8'); };
`);
const result = { status: 'RUNNING', guide, sha256: createHash('sha256').update(bytes).digest('hex'), renderer: 'Chromium in a private VS Code desktop webview; unchanged complete standalone HTML', checks: [], screenshots: [] };
let display, desktop, browser;
const wait = ms => new Promise(ok => setTimeout(ok, ms));
async function until(fn, label, timeout = 20000) { const start = Date.now(); while (Date.now() - start < timeout) { try { const value = await fn(); if (value) return value; } catch {} await wait(100); } throw new Error('Timed out: ' + label); }
try {
  display = spawn(xvfb, [displayName, '-screen', '0', '1600x1000x24', '-ac', '-nolisten', 'tcp'], { env, stdio: 'ignore' }); await wait(300);
  desktop = spawn(executable, ['--no-sandbox', '--disable-gpu', '--user-data-dir', profile, '--extensions-dir', join(root, 'extensions'), '--shared-data-dir', join(root, 'shared'), '--extensionDevelopmentPath', harness, '--skip-welcome', '--skip-release-notes', '--disable-updates', '--remote-debugging-address=127.0.0.1', `--remote-debugging-port=${cdpPort}`], { env, stdio: 'ignore' });
  browser = await until(async () => chromium.connectOverCDP(`http://127.0.0.1:${cdpPort}`), 'private review desktop');
  const context = browser.contexts()[0];
  const page = await until(() => context.pages().find(p => p.url().startsWith('vscode-file:')), 'desktop window');
  const frame = await until(async () => { for (const f of page.frames()) if ((await f.locator('h1').allTextContents()).some(t => t.includes('Test ZERNOVA Studio'))) return f; }, 'rendered guide');
  const structure = await frame.evaluate(() => ({ title: document.querySelector('h1')?.textContent, headings: document.querySelectorAll('main h2').length, steps: document.querySelectorAll('aside a').length, bodyOverflow: document.documentElement.scrollWidth > document.documentElement.clientWidth, bodyColor: getComputedStyle(document.body).color, bodyFontSize: getComputedStyle(document.body).fontSize, images: document.querySelectorAll('figure img').length }));
  assert.equal(structure.bodyOverflow, false); assert.equal(structure.bodyColor, 'rgb(23, 52, 61)'); assert.equal(structure.bodyFontSize, '16px'); assert(structure.headings >= 17); assert(structure.images >= 11); result.checks.push({ name: 'Complete guide, numbered sections, images, and no document-width overflow', status: 'PASS', detail: structure });
  await page.screenshot({ path: join(evidence, 'guide-top.png') }); result.screenshots.push('guide-top.png');
  const gallery = frame.locator('#actual-screenshots'); await gallery.scrollIntoViewIfNeeded(); await wait(300);
  const first = frame.locator('figure img').first(); await first.scrollIntoViewIfNeeded(); await until(() => first.evaluate(img => img.complete && img.naturalWidth > 0), 'embedded screenshot decoding');
  await page.screenshot({ path: join(evidence, 'guide-screenshots.png') }); result.screenshots.push('guide-screenshots.png');
  const anchors = await frame.evaluate(() => [...document.querySelectorAll('aside a')].every(a => document.querySelector(a.getAttribute('href'))));
  assert(anchors); result.checks.push({ name: 'Contents links resolve and actual embedded screenshot decodes', status: 'PASS' });
  result.status = 'PASS';
} catch (error) { result.status = 'FAIL'; result.error = String(error); throw error; }
finally {
  if (browser) await browser.close().catch(() => {});
  if (desktop && desktop.exitCode === null) desktop.kill('SIGTERM');
  if (display && display.exitCode === null) display.kill('SIGTERM');
  result.completedAt = new Date().toISOString(); await writeFile(join(evidence, 'results.json'), JSON.stringify(result, null, 2)); console.log(JSON.stringify(result));
}
