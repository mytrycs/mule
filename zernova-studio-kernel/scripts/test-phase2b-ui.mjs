/** Phase 2B acceptance through an INSTALLED extension in real VS Code desktop.
 * Project creation/editing/build/run uses visible commands and controls.
 * HTTP is independently checked by the test process on the same Linux host.
 */
import assert from 'node:assert/strict';
import { execFile } from 'node:child_process';
import { promisify } from 'node:util';
const runFile = promisify(execFile);
import { readFile, writeFile, mkdir, readdir, cp, stat } from 'node:fs/promises';
import { resolve, join } from 'node:path';
import { createHash } from 'node:crypto';
import { request as httpRequest } from 'node:http';
import { createServer } from 'node:net';
import axe from 'axe-core';
import { connectHost, command, webviewFrame, resizeHost, hostTheme } from './ui-utils.mjs';

process.env.ZERNOVA_UI_CDP ||= 'http://127.0.0.1:9224';
process.env.DISPLAY ||= ':100';
const part = process.argv[2] || 'create';
const base = resolve('evidence/phase2b');
const runId = new Date().toISOString().replace(/[:.]/g, '-');
const runDir = join(base, 'runs', `${runId}-${part}`);
const shots = join(runDir, 'screenshots');
await mkdir(shots, { recursive: true });
const env = JSON.parse(await readFile(join(base, 'environment.json'), 'utf8'));
const manifest = JSON.parse(await readFile('package.json', 'utf8'));
const projectName = process.env.ZERNOVA_ACCEPTANCE_PROJECT || 'zernova-owner-test';
const projectRoot = join(env.workspace, projectName);
const xmlPath = join(projectRoot, 'src/main/mule/hello.xml');
const runtimeHome = process.env.ZERNOVA_RUNTIME_HOME || resolve('../zernova-runtime-foundation-phase1');
const prepared = JSON.parse(await readFile(join(runtimeHome, 'var/prepared.json'), 'utf8'));
const cache = join(env.root, 'maven-repository');
const { browser, context, page } = await connectHost();
let frame;
const checks = [], screenshots = [], http = [], a11y = [], pageErrors = [];
page.on('pageerror', error => pageErrors.push(String(error)));
const report = () => ({ schemaVersion: 1, runId, part, generatedAt: new Date().toISOString(),
  subject: `${manifest.publisher}.${manifest.name}@${manifest.version}`, vsix: env.install,
  host: { platform: process.platform, architecture: process.arch, environment: join(base, 'environment.json'),
    arrangement: 'Installed VS Code desktop, extension host, project, Maven, Runtime, and test request all on the same Linux host' },
  projectRoot, xmlPath, checks, counts: { pass: checks.filter(x => x.status === 'PASS').length, fail: checks.filter(x => x.status === 'FAIL').length },
  screenshots, http, a11y, pageErrors });
async function save() { await writeFile(join(runDir, 'results.json'), JSON.stringify(report(), null, 2)); }
async function check(name, fn) {
  const began = Date.now();
  try { const detail = await fn(); checks.push({ name, status: 'PASS', milliseconds: Date.now() - began, ...(detail ? { detail } : {}) }); console.log('PASS', name); }
  catch (error) { checks.push({ name, status: 'FAIL', milliseconds: Date.now() - began, error: String(error) }); console.error('FAIL', name, error); await page.screenshot({ path: join(runDir, 'failure.png') }).catch(() => {}); await save(); throw error; }
  await save();
}
async function until(fn, timeout = 15000, label = 'condition') {
  const deadline = Date.now() + timeout; let last;
  while (Date.now() < deadline) { try { const value = await fn(); if (value) return value; } catch (error) { last = error; } await page.waitForTimeout(100); }
  throw new Error(`Timed out waiting for ${label}${last ? ': ' + last : ''}`);
}
async function screenshot(name) {
  if (await page.locator('.part.auxiliarybar').isVisible().catch(() => false)) {
    await page.keyboard.press('Control+Alt+b');
    await until(async () => !(await page.locator('.part.auxiliarybar').isVisible()), 5000, 'secondary sidebar closed');
  }
  if (/matching-real-logger|explicit-redeploy-logs/.test(name)) {
    await frame.locator('.real-output').evaluate(el => { el.scrollTop = el.scrollHeight; });
    await page.waitForTimeout(500);
  }
  const path = join(shots, name);
  await page.screenshot({ path });
  screenshots.push({ name, path, source: 'Uncropped screenshot of actual installed VS Code desktop', size: await page.evaluate(() => ({ width: innerWidth, height: innerHeight })) });
}
async function inputStep(title, value) {
  const widget = page.locator('.quick-input-widget');
  await until(async () => (await widget.innerText()).includes(title), 10000, title);
  const input = widget.locator('input'); await input.fill(value); await page.keyboard.press('Enter');
}
async function pick(label) {
  const option = page.locator('.quick-input-list [role="option"]').filter({ hasText: label }).first();
  await option.waitFor(); await option.click();
}
async function trustOwnWorkspace() {
  const buttons = page.getByRole('button').filter({ hasText: /^Yes, I trust the authors/ });
  if (await buttons.count()) await buttons.first().click();
  if (await page.getByRole('button', { name: ' Restricted Mode', exact: true }).count() || (await page.locator('.monaco-workbench').innerText()).includes('Restricted Mode is intended')) {
    await command(page, 'Workspaces: Manage Workspace Trust');
    await page.getByRole('button', { name: 'Trust', exact: true }).click();
    await until(async () => (await page.locator('body').innerText()).includes('You trust this folder'), 10000, 'private workspace trust');
    await page.keyboard.press('Escape');
  }
  const modal = page.locator('.monaco-modal-editor-block');
  if (await modal.isVisible().catch(() => false) && (await modal.innerText()).includes('Workspace Trust')) {
    await modal.getByRole('button', { name: 'Close Modal Editor (Escape)', exact: true }).click();
    await until(async () => !(await modal.isVisible()), 5000, 'Workspace Trust modal closed');
  }
}
async function realFrame() {
  frame = await webviewFrame(page, 'main.real-studio');
  await frame.locator('main.real-studio').waitFor(); return frame;
}
async function click(action) { await frame.locator(`[data-action="${action}"]`).first().click(); }
async function settle() { await until(async () => !((await frame.locator('.code-bar').allTextContents()).join(' ')).includes('Applying edit'), 10000, 'document acknowledgement'); await page.waitForTimeout(120); }
async function field(key, value) { const element = frame.locator(`[data-field="${key}"]`); await element.fill(value); await settle(); }
async function xmlText() { await click('tab-xml'); await settle(); return frame.locator('[data-control="xml"]').inputValue(); }
async function setXml(text) { await click('tab-xml'); await frame.locator('[data-control="xml"]').fill(text); await settle(); }
async function saveDoc() { await click('save'); await until(async () => !(await frame.locator('.lower-tabs').innerText()).includes('Unsaved changes'), 10000, 'saved document'); }
async function select(kind) { await click('tab-flow'); await frame.locator(`.node:has(.node-symbol.${kind})`).first().click(); }
let operationBaseline;
async function consoleAction(action) { operationBaseline = await runtimeState(); await click('runtime-' + action); }
async function runtimeState() { await click('lower-console'); await click('console-status'); const raw = await frame.locator('.real-output').innerText(); return JSON.parse(raw); }
async function waitIdle(timeout = 180000) {
  const before = operationBaseline;
  await until(async () => { const state = await runtimeState(); return state.busy || state.evidenceDirectory && state.evidenceDirectory !== before?.evidenceDirectory; }, 15000, 'new runtime operation start/evidence acknowledgement');
  await until(async () => !(await runtimeState()).busy, timeout, 'runtime operation completion');
  const completed = await runtimeState();
  if (completed.evidenceDirectory) await cp(completed.evidenceDirectory, join(runDir, 'command-history', completed.evidenceDirectory.split('/').at(-1)), { recursive: true });
  return completed;
}
function request(path) {
  return new Promise((resolveRequest, reject) => {
    const req = httpRequest({ hostname: '127.0.0.1', port: 18081, path, method: 'GET', timeout: 15000 }, response => {
      const chunks = []; response.on('data', c => chunks.push(c)); response.on('end', () => resolveRequest({ url: `http://127.0.0.1:18081${path}`, status: response.statusCode, headers: response.headers, body: Buffer.concat(chunks).toString('utf8'), at: new Date().toISOString() }));
    }); req.on('error', reject); req.on('timeout', () => req.destroy(new Error('HTTP timeout'))); req.end();
  });
}
async function scan(label) {
  await frame.evaluate(axe.source);
  const result = await frame.evaluate(() => window.axe.run(document, { runOnly: { type: 'tag', values: ['wcag2a', 'wcag2aa', 'wcag21aa'] } }));
  const record = { label, violations: result.violations.map(v => ({ id: v.id, impact: v.impact, nodes: v.nodes.map(n => ({ target: n.target, failureSummary: n.failureSummary })) })), incomplete: result.incomplete.map(v => ({ id: v.id, nodes: v.nodes.length })) };
  a11y.push(record); assert.deepEqual(record.violations, [], `${label}: ${JSON.stringify(record.violations)}`);
}

try {
  await trustOwnWorkspace();
  if (part === 'create') {
    await check('Installed subject identity and empty owner workspace', async () => {
      assert.equal(env.install.extensionId, 'zernova-preview.zernova-studio-kernel-preview'); assert.equal(env.install.version, manifest.version);
      assert.equal(env.install.sha256, createHash('sha256').update(await readFile(env.install.vsix)).digest('hex'));
      assert(!(await readdir(env.workspace)).includes(projectName));
      return { extensionId: env.install.extensionId, version: env.install.version, workspace: env.workspace };
    });
    await check('Owner creates a real empty project with native wizard and per-project tools', async () => {
      try { await stat(cache); } catch { await cp(join(runtimeHome, 'var/maven-repository'), cache, { recursive: true }); }
      await command(page, 'ZERNOVA: New ZERNOVA Project');
      await inputStep('New ZERNOVA Project (1/6)', env.workspace);
      await inputStep('New ZERNOVA Project (2/6)', projectName);
      await inputStep('New ZERNOVA Project (3/6)', 'io.zernova');
      await inputStep('New ZERNOVA Project (4/6)', projectName);
      await inputStep('New ZERNOVA Project (5/6)', '1.0.0');
      await pick('Configure runtime now');
      await inputStep('Configure ZERNOVA Runtime (1/5)', runtimeHome);
      await inputStep('Configure ZERNOVA Runtime (2/5)', prepared.java_home);
      await inputStep('Configure ZERNOVA Runtime (3/5)', prepared.maven_home);
      await inputStep('Configure ZERNOVA Runtime (4/5)', cache);
      await pick('Offline — use prepared cache');
      await until(async () => { try { await stat(join(projectRoot, 'mule-artifact.json')); await stat(join(projectRoot, 'pom.xml')); return (await readdir(join(projectRoot, 'src/main/mule'))).length === 0; } catch { return false; } }, 15000, 'complete project and empty real mule directory');
      for (const name of ['pom.xml', 'mule-artifact.json']) await stat(join(projectRoot, name));
      await screenshot('01-project-created-empty.png');
      return { projectRoot, initialMuleFiles: await readdir(join(projectRoot, 'src/main/mule')), runtimeHome, javaHome: prepared.java_home, mavenHome: prepared.maven_home, mavenRepository: cache, offline: true };
    });
    await check('Owner explicitly creates hello.xml through installed command', async () => {
      await command(page, 'ZERNOVA: New Integration XML'); await inputStep('New Integration XML', 'hello.xml'); await realFrame();
      const text = await readFile(xmlPath, 'utf8'); assert(!text.includes('<flow'));
      await resizeHost(page, 1440, 900); await screenshot('02-empty-integration-xml.png');
    });
    await check('Owner creates helloFlow using workbench dialog', async () => {
      await click('newFlow'); await frame.locator('[data-flow-name]').fill('helloFlow'); await click('saveFlow');
      await until(async () => (await frame.locator('.flow-name').innerText()) === 'helloFlow');
      assert.equal(await frame.locator('.node').count(), 0);
    });
    await check('Owner inserts Listener first and creates its real global configuration', async () => {
      await click('insert-listener'); await until(async () => await frame.locator('.source-lane .node').count() === 1); await select('listener');
      assert.equal(await frame.locator('[data-field="configId"]').inputValue(), '');
      await click('createConfig');
      for (const [name, value] of Object.entries({ name: 'HTTP_Listener_config', host: '127.0.0.1', port: '18081', basePath: '' })) await frame.locator(`[data-draft="${name}"]`).fill(value);
      await screenshot('03-http-configuration.png'); await click('saveDialog');
      await select('listener'); await frame.locator('[data-field="configId"]').selectOption({ label: 'HTTP_Listener_config' }); await settle();
    });
    await check('Owner inserts and configures HTTP Listener with response only', async () => {
      await click('tab-flow'); await select('listener');
      await field('displayName', 'HTTP Listener'); await field('path', '/hello'); await field('allowedMethods', 'GET');
      await frame.locator('[data-section="Responses"]').click();
      await field('responseStatus', '200'); await field('responseBody', "#['Hello from ZERNOVA']"); await field('responseHeaders', "#[{'Content-Type': 'text/plain; charset=UTF-8'}]");
      await screenshot('04-listener-response-properties.png');
    });
    await check('Owner can type complete Listener values through intermediate diagnostics', async () => {
      await frame.locator('[data-section="General"]').click();
      for (const [key, value] of [['allowedMethods', 'GET'], ['path', '/hello']]) {
        await frame.locator(`[data-field="${key}"]`).fill('');
        await frame.locator(`[data-field="${key}"]`).pressSequentially(value, { delay: 65 }); await settle();
        await until(async () => await frame.locator(`[data-field="${key}"]`).inputValue() === value, 10000, `complete typed ${key}`);
      }
      await frame.locator('[data-section="Responses"]').click();
      await frame.locator('[data-field="responseStatus"]').fill('');
      await frame.locator('[data-field="responseStatus"]').pressSequentially('200', { delay: 65 }); await settle();
      assert.equal(await frame.locator('[data-field="responseStatus"]').inputValue(), '200');
      const typed = await xmlText(); assert(typed.includes('allowedMethods="GET"')); assert(typed.includes('path="/hello"')); assert(typed.includes('statusCode="200"'));
      await click('tab-flow');
    });
    await check('Owner inserts and configures actual core Logger', async () => {
      await click('insert-logger'); await until(async () => await frame.locator('.processor-lane .node').count() === 1); await select('logger');
      await field('displayName', 'Log request'); await field('message', "#['ZERNOVA_TEST ' ++ (attributes.queryParams.testId default 'missing')]");
      await frame.locator('[data-field="level"]').selectOption('INFO'); await settle(); await field('category', 'io.zernova.acceptance');
      await screenshot('05-listener-logger-workbench.png'); await saveDoc();
      const xml = await readFile(xmlPath, 'utf8'); assert(xml.includes('ZERNOVA_TEST')); assert(xml.includes('Hello from ZERNOVA')); assert(!xml.includes('set-payload')); assert(!xml.includes('ee:transform'));
      await writeFile(join(runDir, 'studio-created-initial.xml'), xml);
    });
    await check('Single authoritative editor and visual to XML synchronization', async () => {
      await select('logger'); await field('displayName', 'Log owner request'); const before = await page.locator('.part.editor [role="tab"]').count();
      const xml = await xmlText(); assert(xml.includes('Log owner request')); assert.equal(await page.locator('.part.editor [role="tab"]').count(), before);
      await screenshot('06-synchronized-xml.png');
    });
    await check('Embedded XML to visual synchronization and native undo redo', async () => {
      const baseline = await xmlText(); await setXml(baseline.replace('path="/hello"', 'path="/hello-review"'));
      await select('listener'); assert.equal(await frame.locator('[data-field="path"]').inputValue(), '/hello-review');
      await click('undo'); await until(async () => await frame.locator('[data-field="path"]').inputValue() === '/hello');
      await click('redo'); await until(async () => await frame.locator('[data-field="path"]').inputValue() === '/hello-review');
      await field('path', '/hello'); await saveDoc();
    });
    await check('Second Logger insert reorder duplicate delete undo redo preserve document', async () => {
      await click('insert-logger'); await until(async () => await frame.locator('.processor-lane .node').count() === 2);
      await frame.locator('.processor-lane .node').last().click(); await field('displayName', 'Second Logger'); await field('message', "#['ZERNOVA_SECOND ' ++ (attributes.queryParams.testId default 'missing')]");
      await click('moveLeft'); await until(async () => (await frame.locator('.processor-lane .node').first().innerText()).includes('Second Logger'));
      await click('duplicate'); await until(async () => await frame.locator('.processor-lane .node').count() === 3);
      await click('deleteNode'); await until(async () => await frame.locator('.processor-lane .node').count() === 2);
      await click('undo'); await until(async () => await frame.locator('.processor-lane .node').count() === 3);
      await click('redo'); await until(async () => await frame.locator('.processor-lane .node').count() === 2);
      await saveDoc(); await writeFile(join(runDir, 'studio-created-two-loggers.xml'), await readFile(xmlPath, 'utf8'));
    });
    await check('Native wizard cancellation and duplicate destination preserve existing files', async () => {
      const before = await readFile(xmlPath, 'utf8');
      await command(page, 'ZERNOVA: New ZERNOVA Project'); await inputStep('New ZERNOVA Project (1/6)', env.workspace);
      const widget = page.locator('.quick-input-widget'); await until(async () => (await widget.innerText()).includes('New ZERNOVA Project (2/6)'));
      await widget.locator('input').fill(projectName); await page.keyboard.press('Enter');
      await until(async () => (await widget.innerText()).includes('Destination already exists'), 10000, 'duplicate destination validation'); await page.keyboard.press('Escape');
      assert.equal(await readFile(xmlPath, 'utf8'), before);
      await command(page, 'ZERNOVA: New ZERNOVA Project'); await inputStep('New ZERNOVA Project (1/6)', env.workspace);
      await inputStep('New ZERNOVA Project (2/6)', 'zernova-cancelled-project'); await page.keyboard.press('Escape');
      assert(!(await readdir(env.workspace)).includes('zernova-cancelled-project'));
    });
    await check('Second project keeps runtime settings and new XML destination separate', async () => {
      const otherName = projectName + '-unconfigured', otherRoot = join(env.workspace, otherName);
      await command(page, 'ZERNOVA: New ZERNOVA Project');
      await inputStep('New ZERNOVA Project (1/6)', env.workspace); await inputStep('New ZERNOVA Project (2/6)', otherName);
      await inputStep('New ZERNOVA Project (3/6)', 'io.zernova'); await inputStep('New ZERNOVA Project (4/6)', otherName);
      await inputStep('New ZERNOVA Project (5/6)', '1.0.0'); await pick('Configure later — edit only');
      await until(async () => { try { await stat(join(otherRoot, 'mule-artifact.json')); return true; } catch { return false; } });
      assert.deepEqual(await readdir(join(otherRoot, 'src/main/mule')), []);
      await until(async () => (await page.locator('body').innerText()).includes('Created ' + otherName + '. src/main/mule is empty.'), 15000, 'second-project creation completed');
      await page.waitForTimeout(300);
      await command(page, 'ZERNOVA: New Integration XML'); await inputStep('New Integration XML', 'isolated.xml'); await realFrame();
      await until(async () => { try { await stat(join(otherRoot, 'src/main/mule/isolated.xml')); return true; } catch { return false; } });
      assert(!(await readdir(join(projectRoot, 'src/main/mule'))).includes('isolated.xml'));
      const other = await runtimeState(); assert.equal(other.configured, false); assert.equal(other.projectRoot, otherRoot); assert.equal(other.phase, 'Not configured');
      await screenshot('second-project-not-configured.png'); await page.keyboard.press('Control+w'); await realFrame();
      const owner = await runtimeState(); assert.equal(owner.projectRoot, projectRoot); assert.equal(owner.configured, true);
      return { firstProject: projectRoot, secondProject: otherRoot, secondConfigured: false, secondXml: join(otherRoot, 'src/main/mule/isolated.xml') };
    });
    const sourceSnapshot = join(runDir, 'application-source');
    await cp(projectRoot, sourceSnapshot, { recursive: true, filter: path => !path.split('/').includes('target') });
    await writeFile(join(base, 'owner-project.json'), JSON.stringify({ sourceSnapshot, projectRoot, projectName, xmlPath, createdBy: 'Native installed extension wizard and real workbench UI', creationEvidence: join(runDir, 'results.json'), runtimeHome, cache }, null, 2));
  } else {
    await realFrame();
  }

  if (part === 'persistence') {
    await check('Full desktop restart retains the Studio-created source and project-specific selections', async () => {
      const owner = JSON.parse(await readFile(join(base, 'owner-project.json'), 'utf8'));
      assert.equal(await readFile(xmlPath, 'utf8'), await readFile(join(owner.sourceSnapshot, 'src/main/mule/hello.xml'), 'utf8'));
      const state = await runtimeState(); assert.equal(state.projectRoot, projectRoot); assert.equal(state.configured, true); assert.equal(state.offline, true);
      await select('listener'); assert.equal(await frame.locator('[data-field="path"]').inputValue(), '/hello');
      assert.equal(await frame.locator('.processor-lane .node').count(), 2);
      await screenshot('owner-project-after-desktop-restart.png'); return state;
    });
    await check('Owner closes and reopens the saved integration without source loss', async () => {
      const before = await readFile(xmlPath, 'utf8'); await page.keyboard.press('Control+w');
      await command(page, 'ZERNOVA: Open Integration in Studio');
      const option = page.locator('.quick-input-list [role="option"]').filter({ hasText: 'hello.xml' }).first();
      if (await option.isVisible().catch(() => false)) await option.click(); await realFrame();
      assert.equal(await readFile(xmlPath, 'utf8'), before); await select('listener'); assert.equal(await frame.locator('[data-field="path"]').inputValue(), '/hello');
    });
  }

  if (part === 'runtime') {
    await check('Trusted workspace runs real Runtime diagnostics through installed Studio', async () => { await consoleAction('diagnostics'); const state = await waitIdle(30000); assert(!state.lastError, state.lastError); assert.equal(state.phase, 'Diagnostics passed'); await writeFile(join(runDir, 'diagnostics-state.json'), JSON.stringify(state, null, 2)); return state; });
    await check('Owner builds actual selected application offline', async () => {
      await saveDoc(); await consoleAction('build'); const state = await waitIdle(180000); assert(!state.lastError, state.lastError);
      await click('console-build'); const output = await frame.locator('.real-output').innerText(); assert(output.includes('BUILD SUCCESS')); await writeFile(join(runDir, 'actual-build-output.txt'), output);
      const artifact = join(projectRoot, 'target', `${projectName}-1.0.0-mule-application.jar`); const bytes = await readFile(artifact);
      await screenshot('07-build-success.png'); return { artifact, bytes: bytes.length, sha256: createHash('sha256').update(bytes).digest('hex') };
    });
    await check('Owner runs actual project through installed Studio adapter', async () => {
      await consoleAction('run'); const state = await waitIdle(180000); assert(!state.lastError, state.lastError);
      await writeFile(join(runDir, 'deployed-state.json'), JSON.stringify(state, null, 2)); await screenshot('08-application-deployed.png'); return state;
    });
    await check('Fresh HTTP request returns real 200 body and matching Logger lines', async () => {
      const testId = 'ZN-ACCEPT-' + Date.now(); const result = await request('/hello?testId=' + testId); http.push({ ...result, testId });
      assert.equal(result.status, 200); assert.equal(result.body, 'Hello from ZERNOVA'); assert(String(result.headers['content-type']).startsWith('text/plain'));
      const curlId = 'ZN-OWNER-CURL-' + Date.now(), curlUrl = 'http://127.0.0.1:18081/hello?testId=' + curlId;
      const curl = await runFile('curl', ['-i', '--max-time', '15', curlUrl], { encoding: 'utf8', timeout: 20000 });
      assert(curl.stdout.includes('200')); assert(curl.stdout.includes('Hello from ZERNOVA'));
      await writeFile(join(runDir, 'actual-curl-response.txt'), `$ curl -i --max-time 15 '${curlUrl}'\n${curl.stdout}`);
      http.push({ tool: 'curl', url: curlUrl, testId: curlId, stdout: curl.stdout, stderr: curl.stderr, at: new Date().toISOString() });
      await consoleAction('logs'); await waitIdle(30000); await click('console-logs');
      const logs = await frame.locator('.real-output').innerText(); assert(logs.includes('ZERNOVA_TEST ' + testId)); assert(logs.includes('ZERNOVA_TEST ' + curlId)); assert(logs.includes('ZERNOVA_SECOND ' + testId));
      assert(logs.indexOf('ZERNOVA_SECOND ' + testId) < logs.indexOf('ZERNOVA_TEST ' + testId));
      await writeFile(join(runDir, 'actual-matching-application-logs.txt'), logs); await screenshot('09-matching-real-logger-output.png'); return { testId, curlId, status: result.status, body: result.body };
    });
    await check('Owner changes path visually then saves rebuilds redeploys', async () => {
      await select('listener'); await field('path', '/hello-v2'); await saveDoc(); await consoleAction('run'); const state = await waitIdle(180000); assert(!state.lastError, state.lastError);
      const testId = 'ZN-REDEPLOY-' + Date.now(); const result = await request('/hello-v2?testId=' + testId), old = await request('/hello?testId=ZN-OLD-' + Date.now()); http.push({ ...result, testId }, { ...old, purpose: 'old route after explicit redeploy' });
      assert.equal(result.status, 200); assert.equal(result.body, 'Hello from ZERNOVA'); assert.notEqual(old.status, 200);
      await consoleAction('logs'); await waitIdle(30000); await click('console-logs'); assert((await frame.locator('.real-output').innerText()).includes('ZERNOVA_TEST ' + testId));
      await screenshot('10-explicit-redeploy-logs.png'); return { newRouteStatus: result.status, oldRouteStatus: old.status, oldRouteBody: old.body };
    });
    await check('Stop Application undeploys without treating process as endpoint health', async () => {
      await consoleAction('stopApplication'); const state = await waitIdle(30000); assert(!state.lastError, state.lastError);
      let result; try { result = await request('/hello-v2?testId=ZN-STOPPED'); } catch (error) { result = { error: String(error) }; } http.push({ purpose: 'after undeploy', ...result });
      assert(!('status' in result && result.status === 200 && result.body === 'Hello from ZERNOVA'));
      return state;
    });
    await check('Stop Runtime safely releases the owned process and port', async () => {
      await consoleAction('stopRuntime'); const state = await waitIdle(45000); assert(!state.lastError, state.lastError);
      assert.equal(state.process, 'STOPPED');
      await new Promise((ok, fail) => { const server = createServer(); server.once('error', fail); server.listen({ host: '127.0.0.1', port: 18081, exclusive: true }, () => server.close(error => error ? fail(error) : ok())); });
      await writeFile(join(runDir, 'stopped-state.json'), JSON.stringify(state, null, 2)); await screenshot('11-stopped-runtime.png'); return state;
    });
  }

  if (part === 'themes') {
    for (const [name, code] of [['ZERNOVA Studio Light', 'light'], ['ZERNOVA Studio Dark', 'dark'], ['Dark High Contrast', 'hc-dark'], ['Light High Contrast', 'hc-light']]) {
      await hostTheme(page, name); await realFrame();
      for (const view of ['listener', 'logger', 'xml', 'globals']) await check(`Real ${view} accessibility / ${name}`, async () => {
        if (view === 'listener' || view === 'logger') await select(view); else await click('tab-' + view);
        await scan(`${view}-${code}`); await screenshot(`${view}-${code}.png`);
      });
    }
    await check('Keyboard focus and modal traversal remain usable', async () => {
      await click('tab-globals'); await click('createConfig'); await frame.locator('[data-action="saveDialog"]').focus(); await page.keyboard.press('Tab');
      const focus = await frame.evaluate(() => ({ tag: document.activeElement?.tagName, focus: document.activeElement?.getAttribute('data-focus'), outline: getComputedStyle(document.activeElement).outlineStyle }));
      assert.equal(focus.focus, 'cancelDialog'); assert.notEqual(focus.outline, 'none'); await screenshot('keyboard-modal-focus.png'); await page.keyboard.press('Escape');
    });
    await hostTheme(page, 'ZERNOVA Studio Light'); await realFrame();
  }
  await check('No uncaught page errors in this installed interaction run', async () => { assert.deepEqual(pageErrors, []); });
  await writeFile(join(base, `latest-${part}.json`), JSON.stringify({ runId, results: join(runDir, 'results.json'), status: 'PASS' }, null, 2));
} finally { await save(); await browser.close(); }
