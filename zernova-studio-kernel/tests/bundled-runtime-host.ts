import * as vscode from 'vscode';
import * as fs from 'node:fs/promises';
import path from 'node:path';
import assert from 'node:assert/strict';
import net from 'node:net';
import { randomUUID } from 'node:crypto';
import { projectFiles } from '../src/host/projects';
import { createIntegrationXml, applyOperation, parseIntegration } from '../src/shared/xml';

export async function run() {
  const root = process.env.ZN_STUDIO_TEST_ROOT!;
  const workspace = path.join(root, 'workspace');
  const rows: { name: string; status: string; detail?: unknown; error?: string }[] = [];
  const persist = () => fs.writeFile(path.join(root, 'results.json'), JSON.stringify({ platform: process.platform, arch: process.arch, vscode: vscode.version, rows }, null, 2));
  async function check(name: string, test: () => Promise<unknown>) {
    try { rows.push({ name, status: 'PASS', detail: await test() }); }
    catch (error) { rows.push({ name, status: 'FAIL', error: String(error instanceof Error ? error.stack : error) }); throw error; }
    finally { await persist(); }
  }
  const server = net.createServer();
  await new Promise<void>(resolve => server.listen(0, '127.0.0.1', resolve));
  const port = (server.address() as net.AddressInfo).port;
  await new Promise<void>(resolve => server.close(() => resolve()));
  await fs.mkdir(path.join(workspace, 'src/main/mule'), { recursive: true });
  await fs.mkdir(path.join(workspace, 'src/main/resources'), { recursive: true });
  for (const [rel, text] of Object.entries(projectFiles({ root: workspace, name: 'studio-local-test', groupId: 'com.zernova.example', artifactId: 'studio-local-test', version: '1.0.0' }))) {
    await fs.writeFile(path.join(workspace, rel), text);
  }
  let xml = createIntegrationXml();
  xml = applyOperation(xml, { kind: 'createFlow', name: 'studioFlow' }).text;
  xml = applyOperation(xml, { kind: 'saveConfig', values: { name: 'HTTP_Listener_config', protocol: 'HTTP', host: '127.0.0.1', port: String(port), basePath: '' } }).text;
  xml = applyOperation(xml, { kind: 'insert', flowId: 'flow:studioFlow', component: 'listener' }).text;
  xml = applyOperation(xml, { kind: 'insert', flowId: 'flow:studioFlow', component: 'logger' }).text;
  const file = vscode.Uri.file(path.join(workspace, 'src/main/mule/hello.xml'));
  await fs.writeFile(file.fsPath, xml);
  const subject = vscode.extensions.getExtension('zernova-preview.zernova-studio-kernel-preview')!;
  const api = await subject.activate(); await api.whenInitialized();
  await fs.writeFile(path.join(root, 'ready.json'), JSON.stringify({ workspace, port, file: file.fsPath, extensionPath: subject.extensionPath }));
  if (!vscode.workspace.isTrusted) await new Promise<void>(resolve => { const event = vscode.workspace.onDidGrantWorkspaceTrust(() => { event.dispose(); resolve(); }); });
  const command = async (action: string) => { await vscode.commands.executeCommand('zernovaKernelStudio.' + action, file); return api.getRuntimeDiagnostics()[0]; };
  const edit = async (text: string) => {
    const doc = await vscode.workspace.openTextDocument(file); const change = new vscode.WorkspaceEdit();
    change.replace(file, new vscode.Range(doc.positionAt(0), doc.positionAt(doc.getText().length)), text);
    assert(await vscode.workspace.applyEdit(change)); assert(await doc.save());
  };
  const request = async (route = '/hello') => {
    const id = 'STUDIO-' + randomUUID(); const response = await fetch(`http://127.0.0.1:${port}${route}?testId=${id}`);
    const body = await response.text(); assert.equal(response.status, 200); assert.equal(body, 'Hello from ZERNOVA');
    await command('logs'); const logs = api.getRuntimeDiagnostics()[0].applicationLogs;
    assert(logs.includes(id), 'Fresh Logger correlation is visible through Studio');
    return { status: response.status, body, correlatedLogger: true };
  };
  try {
    await check('Installed VSIX and actual custom editor', async () => {
      assert(subject.extensionPath.startsWith(path.join(root, 'extensions'))); assert.equal(subject.packageJSON.version, '0.3.0');
      await vscode.commands.executeCommand('zernovaKernelStudio.openIntegration', file);
      return { extensionPath: subject.extensionPath, model: parseIntegration(xml).flows[0].name };
    });
    await check('Bundled Java/runtime diagnostics with invalid external tool paths', async () => { const state = await command('diagnostics'); assert.equal(state.phase, 'Diagnostics passed'); assert(state.runtimeOutput.includes('0.3.0-dev.2')); return { phase: state.phase, offline: state.offline }; });
    await check('Studio offline build produces a fresh application', async () => { const state = await command('build'); assert.equal(state.phase, 'Build succeeded · not deployed'); assert(state.artifact.endsWith('.zapp')); return { artifact: state.artifact, sha256: state.artifactSha256 }; });
    await check('Studio Run deploys to the renamed source-built engine', async () => { const state = await command('run'); assert.equal(state.application, 'DEPLOYED'); assert.equal(state.process, 'RUNNING'); return request(); });
    await check('XML edits save and redeploy without the old route', async () => { await edit(xml.replace('path="/hello"', 'path="/studio-v2"')); const state = await command('run'); assert.equal(state.application, 'DEPLOYED'); const result = await request('/studio-v2'); assert.equal((await fetch(`http://127.0.0.1:${port}/hello`)).status, 404); return result; });
    await check('Stop Application undeploys and retains the private runtime', async () => { const state = await command('stopApplication'); assert.equal(state.process, 'RUNNING'); assert.equal(state.application, 'NOT_DEPLOYED'); });
    await check('Stop Runtime shuts down the owned engine', async () => { const state = await command('stopRuntime'); assert.equal(state.process, 'STOPPED'); });
    await check('Invalid XML is diagnosed and cannot deploy a stale artifact', async () => { await edit(xml.replace('</mule>', '</broken>')); const state = await command('run'); assert.equal(state.phase, 'Failed'); assert.equal(state.process, 'STOPPED'); assert(state.lastError); });
    await check('Restored document runs again and stops cleanly', async () => { await edit(xml); const state = await command('run'); assert.equal(state.application, 'DEPLOYED'); const result = await request(); assert.equal((await command('stopRuntime')).process, 'STOPPED'); return result; });
  } finally {
    await command('stopRuntime'); await edit(xml); await persist();
    await fs.writeFile(path.join(root, 'complete.json'), JSON.stringify({ passed: rows.filter(r => r.status === 'PASS').length, failed: rows.filter(r => r.status === 'FAIL').length }));
  }
}
