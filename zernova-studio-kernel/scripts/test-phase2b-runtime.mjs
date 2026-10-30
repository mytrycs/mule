/** Installed desktop negative Runtime tests, isolated from the owner UI app.
 * All Studio operations use visible native commands and webview controls.
 * Fixture changes affect only a copied owner-created source project/runtime config.
 */
import assert from 'node:assert/strict';
import { readFile, writeFile, mkdir, rename, cp, readdir } from 'node:fs/promises';
import { join, resolve } from 'node:path';
import { request as httpRequest, createServer } from 'node:http';
import { connectHost, webviewFrame } from './ui-utils.mjs';

process.env.ZERNOVA_UI_CDP ||= 'http://127.0.0.1:9226';
process.env.DISPLAY ||= ':102';
const home = '/tmp/zernova-studio-phase2b-runtime-review';
const fixture = JSON.parse(await readFile(join(home, 'fixture.json'), 'utf8'));
const environment = JSON.parse(await readFile(join(home, 'desktop.json'), 'utf8'));
const prepared = JSON.parse(await readFile(join(fixture.runtime, 'var/prepared.json'), 'utf8'));
const part = process.argv[2] || 'tooling';
const runId = new Date().toISOString().replace(/[:.]/g, '-');
const out = resolve('evidence/phase2b-runtime-host/runs', runId + '-' + part);
await mkdir(out, { recursive: true });
const { browser, page } = await connectHost();
let frame;
const checks = [], states = [], shots = [];
const record = () => ({ schemaVersion: 1, runId, part, createdAt: new Date().toISOString(), subject: environment.install,
  arrangement: 'Isolated installed VS Code desktop, Runtime, and HTTP test request on the same Linux x64 host',
  fixture: { project: fixture.project, runtime: fixture.runtime, port: 18088, source: fixture.source }, checks, states, shots,
  counts: { pass: checks.filter(c => c.status === 'PASS').length, fail: checks.filter(c => c.status === 'FAIL').length } });
async function save() { await writeFile(join(out, 'results.json'), JSON.stringify(record(), null, 2)); }
async function check(name, fn) {
  const began = Date.now();
  try { const detail = await fn(); checks.push({ name, status: 'PASS', milliseconds: Date.now() - began, detail }); console.log('PASS', name); }
  catch (error) { checks.push({ name, status: 'FAIL', milliseconds: Date.now() - began, error: String(error) }); await screenshot('failure.png'); await save(); throw error; }
  await save();
}
async function until(fn, timeout = 15000, label = 'condition') {
  const end = Date.now() + timeout; let last;
  while (Date.now() < end) { try { const value = await fn(); if (value) return value; } catch (e) { last = e; } await page.waitForTimeout(100); }
  throw new Error('Timed out: ' + label + (last ? ' (' + String(last) + ')' : ''));
}
async function command(page, name) {
  await page.keyboard.press('Escape'); await page.waitForTimeout(200); await page.keyboard.press('Control+Shift+P');
  const input = page.locator('.quick-input-widget input'); await input.waitFor({ state: 'visible' }); await input.fill('>' + name);
  const option = await until(async () => { for (const item of await page.locator('.quick-input-list [role="option"]').all()) { const label = await item.getAttribute('aria-label'); if (label === name || label?.startsWith(name + ',')) return item; } return null; }, 10000, name);
  await option.click(); await page.waitForTimeout(400);
}
async function screenshot(name) { const file = join(out, name); await page.screenshot({ path: file }); shots.push({ name, file, source: 'Actual installed VS Code desktop screenshot' }); }
async function step(title, value) { const box = page.locator('.quick-input-widget'); await until(async () => (await box.innerText()).includes(title), 10000, title); await box.locator('input').fill(value); await page.keyboard.press('Enter'); }
async function pick(label) { const option = page.locator('.quick-input-list [role="option"]').filter({ hasText: label }).first(); await option.waitFor(); await option.click(); }
async function click(action) { await page.keyboard.press('Escape'); await page.mouse.move(45, 1020); await page.waitForTimeout(80); await frame.locator(`[data-action="${action}"]`).first().click(); }
async function state() { await click('lower-console'); await click('console-status'); return JSON.parse(await frame.locator('.real-output').innerText()); }
async function operation(action, timeout = 180000) {
  const before = await state(); await click('runtime-' + action);
  await until(async () => { const s = await state(); return s.busy || s.evidenceDirectory && s.evidenceDirectory !== before.evidenceDirectory; }, 15000, 'operation start');
  await until(async () => !(await state()).busy, timeout, 'operation finish');
  const after = await state(); states.push({ action, state: after }); await save(); return after;
}
async function setXml(text) {
  await click('tab-xml'); await frame.locator('[data-control="xml"]').fill(text);
  await until(async () => !(await frame.locator('.code-bar').innerText()).includes('Applying edit'), 15000, 'XML edit acknowledgement');
  await page.waitForTimeout(180); await click('save');
  await until(async () => (await readFile(join(fixture.project, 'src/main/mule/hello.xml'), 'utf8')) === text, 15000, 'saved XML');
}
async function configure(javaHome = prepared.java_home, mavenHome = prepared.maven_home) {
  await command(page, 'ZERNOVA: Configure Project Runtime and Tools');
  await step('Configure ZERNOVA Runtime (1/5)', fixture.runtime);
  await step('Configure ZERNOVA Runtime (2/5)', javaHome);
  await step('Configure ZERNOVA Runtime (3/5)', mavenHome);
  await step('Configure ZERNOVA Runtime (4/5)', join(home, 'maven-repository'));
  await pick('Offline — use prepared cache'); await page.waitForTimeout(350);
}
async function open() {
  await command(page, 'ZERNOVA: Open ZERNOVA Project'); await step('Open ZERNOVA Project', fixture.project); await page.waitForTimeout(600);
  await command(page, 'ZERNOVA: Open Integration in Studio');
  await page.waitForTimeout(400);
  if (await page.locator('.quick-input-list [role="option"]').filter({ hasText: 'hello.xml' }).first().isVisible().catch(() => false)) await pick('hello.xml');
  frame = await webviewFrame(page); await frame.locator('main.real-studio').waitFor();
}
async function trust() {
  if ((await page.locator('body').innerText()).includes('Restricted Mode')) {
    await command(page, 'Workspaces: Manage Workspace Trust'); await page.getByRole('button', { name: 'Trust', exact: true }).click();
    await until(async () => (await page.locator('body').innerText()).includes('You trust this folder'));
    await command(page, 'ZERNOVA: Open Integration in Studio'); frame = await webviewFrame(page);
  }
}
async function request(route = '/hello-v2') {
  return new Promise((res, rej) => { const req = httpRequest({ hostname: '127.0.0.1', port: 18088, path: route + '?testId=ZN-NEG-' + runId, timeout: 10000 }, response => {
    const chunks = []; response.on('data', data => chunks.push(data)); response.on('end', () => res({ status: response.statusCode, body: Buffer.concat(chunks).toString('utf8') }));
  }); req.on('error', rej); req.on('timeout', () => req.destroy(new Error('HTTP timeout'))); req.end(); });
}
async function stopped() { const value = await operation('stopRuntime'); assert.equal(value.process, 'STOPPED'); return value; }
async function copyHistory() {
  for (const item of states) if (item.state.evidenceDirectory) {
    try { await cp(item.state.evidenceDirectory, join(out, 'command-history', item.state.evidenceDirectory.split('/').at(-1)), { recursive: true }); } catch { /* A failed prerequisite may have no persisted command. */ }
  }
}

try {
  await open();
  if (part.startsWith('tooling')) {
    if (part === 'tooling') await check('Untrusted workspace blocks build without starting tools', async () => {
      assert((await page.locator('body').innerText()).includes('Restricted Mode'));
      await configure(); const before = await state(); await click('runtime-build');
      await until(async () => (await frame.locator('body').innerText()).includes('Workspace trust is required'));
      const after = await state(); assert.equal(after.evidenceDirectory, before.evidenceDirectory); await screenshot('01-trust-blocked.png');
    });
    await trust();
    await check('Missing Java executable produces an actionable failure', async () => {
      await configure(join(home, 'missing-java'));
      const s = await operation('diagnostics'); assert.equal(s.phase, 'Failed'); assert.match(s.lastError, /Java.*missing or not executable/); assert.equal(s.process, 'STOPPED'); return { error: s.lastError };
    });
    await check('Missing Maven executable produces an actionable failure', async () => {
      await configure(prepared.java_home, join(home, 'missing-maven'));
      const s = await operation('diagnostics'); assert.equal(s.phase, 'Failed'); assert.match(s.lastError, /Maven.*missing or not executable/); return { error: s.lastError };
    });
    await check('Missing selected Runtime CLI fails without another launcher fallback', async () => {
      await configure();
      const bin = join(fixture.runtime, 'bin/zernova'); await rename(bin, bin + '.test-unavailable');
      try { const s = await operation('diagnostics'); assert.equal(s.phase, 'Failed'); assert.match(s.lastError, /ENOENT|no such file|cannot open file/); return { error: s.lastError }; }
      finally { await rename(bin + '.test-unavailable', bin); }
    });
    await check('Restored genuine toolchain diagnostics pass', async () => {
      const s = await operation('diagnostics'); assert.equal(s.phase, 'Diagnostics passed'); assert.equal(s.lastError, ''); return { offline: s.offline, instance: s.instanceDirectory };
    });
    await check('Invalid XML is retained, diagnosed, and prevents build/deploy', async () => {
      const invalid = fixture.baseXml.replace('</mule>', '</mule-broken>'); await setXml(invalid);
      const s = await operation('run'); assert.equal(s.phase, 'Failed'); assert.match(s.lastError, /Fix hello.xml|Invalid integration XML/); assert.equal(s.process, 'STOPPED'); assert.equal(await readFile(join(fixture.project, 'src/main/mule/hello.xml'), 'utf8'), invalid);
      await screenshot('02-invalid-xml-blocked.png'); await setXml(fixture.baseXml); return { error: s.lastError };
    });
    await check('Invalid configuration reference prevents build/deploy', async () => {
      const invalid = fixture.baseXml.replace(/config-ref="[^"]+"/, 'config-ref="Missing_Config"'); await setXml(invalid);
      const s = await operation('run'); assert.equal(s.phase, 'Failed'); assert.match(s.lastError, /configuration|config|reference/i); assert.equal(s.process, 'STOPPED'); await setXml(fixture.baseXml); return { error: s.lastError };
    });
    await check('Unresolved dependency in offline build cannot deploy an old JAR', async () => {
      await writeFile(join(fixture.project, 'pom.xml'), fixture.basePom.replace('<version>1.12.2</version>', '<version>99.99.99-zernova-missing</version>'));
      try { const s = await operation('run'); assert.equal(s.phase, 'Failed'); assert.match(s.lastError, /offline|resolve|99.99.99/i); assert.equal(s.application, 'NOT_DEPLOYED'); assert.equal(s.artifact, undefined); return { error: s.lastError }; }
      finally { await writeFile(join(fixture.project, 'pom.xml'), fixture.basePom); }
    });
  } else if (part === 'execution') {
    await trust(); await configure(); await setXml(fixture.baseXml);
    await check('Invalid runtime expression returns HTTP500 and actual error logs', async () => {
      const invalid = fixture.baseXml.replace("#['ZERNOVA_TEST ' ++ (attributes.queryParams.testId default 'missing')]", '#[definitelyMissingFunction()]');
      assert.notEqual(invalid, fixture.baseXml); await setXml(invalid);
      const s = await operation('run'); assert.equal(s.phase, 'Deployed · endpoint unverified'); assert.equal(s.endpoint, 'UNVERIFIED');
      const reply = await request(); assert.equal(reply.status, 500); assert.match(reply.body, /Unable to resolve reference.*definitelyMissingFunction/s);
      await operation('logs'); await click('console-logs'); const logText = await frame.locator('.real-output').innerText(); assert.match(logText, /definitelyMissingFunction/); assert(logText.includes('ZN-NEG-' + runId)); await writeFile(join(out, 'invalid-expression.log'), logText); await screenshot('03-invalid-expression-runtime-log.png');
      await stopped(); await setXml(fixture.baseXml); return { phase: s.phase, process: s.process, application: s.application, reply };
    });
    await check('Unresolved Listener property fails deployment while the JVM remains running', async () => {
      const invalid = fixture.baseXml.replace('path="/hello-v2"', 'path="${zernova.missing.path}"'); assert.notEqual(invalid, fixture.baseXml); await setXml(invalid);
      const s = await operation('run'); assert.equal(s.phase, 'Failed'); assert.match(s.lastError, /failed to deploy|did not become ready/); assert.equal(s.process, 'RUNNING'); assert.equal(s.application, 'UNHEALTHY');
      await screenshot('04-failed-app-live-jvm.png'); await stopped(); await setXml(fixture.baseXml); return { phase: s.phase, process: s.process, application: s.application, error: s.lastError };
    });
    await check('Corrected app deploys from the selected project and serves real HTTP', async () => {
      const s = await operation('run'); assert.equal(s.phase, 'Deployed · endpoint unverified'); const reply = await request(); assert.equal(reply.status, 200); assert.equal(reply.body, 'Hello from ZERNOVA'); return { artifact: s.artifact, sha256: s.artifactSha256, reply };
    });
    await check('Offline dependency failure leaves old JAR undeployed and preserves the existing running app', async () => {
      const before = await state(); assert(before.artifact); const oldJar = await readFile(before.artifact);
      await writeFile(join(fixture.project, 'pom.xml'), fixture.basePom.replace('<version>1.12.2</version>', '<version>99.99.99-zernova-missing</version>'));
      try {
        const s = await operation('run'); assert.equal(s.phase, 'Failed'); assert.equal(s.artifact, undefined); assert.equal(s.process, 'RUNNING'); assert.equal(s.application, 'DEPLOYED'); assert.match(s.lastError, /offline|resolve|99.99.99/i);
        assert(oldJar.equals(await readFile(before.artifact))); const reply = await request(); assert.equal(reply.status, 200); return { error: s.lastError, oldJarStillExists: true, noArtifactSelectedForDeployment: true, reply };
      } finally { await writeFile(join(fixture.project, 'pom.xml'), fixture.basePom); }
    });
    await check('Cancellation during build preserves the existing deployed application', async () => {
      await click('runtime-run'); await until(async () => (await state()).phase === 'Building', 30000, 'build stage'); await click('runtime-cancel');
      await until(async () => !(await state()).busy, 30000, 'cancel completion'); const s = await state(); states.push({ action: 'cancel-run', state: s });
      assert.equal(s.phase, 'Cancelled'); assert.equal(s.process, 'RUNNING'); assert.equal(s.application, 'DEPLOYED'); const reply = await request(); assert.equal(reply.status, 200); return { phase: s.phase, reply };
    });
    await check('Source changes while building refuse stale-artifact deployment', async () => {
      await click('runtime-run'); await until(async () => (await state()).phase === 'Building', 30000, 'build stage');
      await setXml(fixture.baseXml.replace('Log owner request', 'Changed during build'));
      await until(async () => !(await state()).busy, 60000, 'stale build completion'); const s = await state(); states.push({ action: 'source-changed-build', state: s });
      assert.equal(s.phase, 'Failed'); assert.match(s.lastError, /source changed|Source changed/i); assert.equal(s.process, 'RUNNING'); assert.equal(s.application, 'DEPLOYED');
      await setXml(fixture.baseXml); return { error: s.lastError, reply: await request() };
    });
    await stopped();
    await check('Occupied loopback port is reported without stopping an unrelated owned test server', async () => {
      const token = 'ZN-PORT-HOLDER-' + runId;
      const holder = createServer((_req, res) => { res.writeHead(200, { 'Content-Type': 'text/plain' }); res.end(token); });
      await new Promise((resolve, reject) => { holder.once('error', reject); holder.listen({ host: '127.0.0.1', port: 18088, exclusive: true }, resolve); });
      try {
        const s = await operation('run'); assert.equal(s.phase, 'Failed'); assert.match(s.lastError, /Port 18088.*unavailable/); assert.equal(s.port, 18088); assert.equal(s.process, 'STOPPED'); assert.equal(s.application, 'NOT_DEPLOYED');
        const records = await Promise.all((await readdir(s.evidenceDirectory)).filter(f => f !== 'state.json').map(async f => JSON.parse(await readFile(join(s.evidenceDirectory, f), 'utf8'))));
        assert(!records.some(r => r.args?.[1] === 'start'), 'Runtime start must not be invoked after a port conflict');
        const reply = await request(); assert.equal(reply.status, 200); assert.equal(reply.body, token); await screenshot('05-port-conflict-holder-preserved.png'); return { error: s.lastError, runtimeStartNotInvoked: true, holderStillResponds: reply };
      } finally { await new Promise(resolve => holder.close(resolve)); }
    });
    await check('Runtime startup timeout cleans up the owned partial instance', async () => {
      const configFile = join(fixture.runtime, 'config/runtime.json'), original = await readFile(configFile, 'utf8');
      await writeFile(configFile, JSON.stringify({ ...JSON.parse(original), startup_timeout_seconds: 1 }, null, 2));
      try {
        const s = await operation('run'); assert.equal(s.phase, 'Failed'); assert.match(s.lastError, /timed out|timeout/i); assert.equal(s.process, 'STOPPED'); assert.equal(s.application, 'NOT_DEPLOYED');
        await assert.rejects(request(), /ECONNREFUSED/); await screenshot('06-timeout-owned-cleanup.png'); return { error: s.lastError, process: s.process, application: s.application };
      } finally { await writeFile(configFile, original); }
    });
    await check('Final Stop Project Runtime is safe and idempotent', async () => { const s = await stopped(); await assert.rejects(request(), /ECONNREFUSED/); return { process: s.process, application: s.application }; });
  } else throw new Error('Use tooling or execution');
} finally {
  await copyHistory(); await save(); await browser.close(); console.log(JSON.stringify({ out, counts: record().counts }));
}
