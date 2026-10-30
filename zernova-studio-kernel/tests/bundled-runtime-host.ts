import * as vscode from 'vscode';
import * as fs from 'node:fs/promises';
import path from 'node:path';
import assert from 'node:assert/strict';
import net from 'node:net';
import { randomUUID } from 'node:crypto';
import { projectFiles } from '../src/host/projects';
import { createIntegrationXml, applyOperation, parseIntegration } from '../src/shared/xml';
import { freshFetch as fetch, waitForHttpReady } from '../../zernova-studio-build/tests/http-client.cjs';

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
  await fs.mkdir(path.join(workspace, 'src/main/zernova'), { recursive: true });
  await fs.mkdir(path.join(workspace, 'src/main/resources'), { recursive: true });
  for (const [rel, text] of Object.entries(projectFiles({ root: workspace, name: 'studio-local-test', groupId: 'com.zernova.example', artifactId: 'studio-local-test', version: '1.0.0' }))) {
    await fs.writeFile(path.join(workspace, rel), text);
  }
  let xml = createIntegrationXml();
  xml = applyOperation(xml, { kind: 'createFlow', name: 'studioFlow' }).text;
  xml = applyOperation(xml, { kind: 'saveConfig', values: { name: 'HTTP_Listener_config', protocol: 'HTTP', host: 'localhost', port: String(port), basePath: '' } }).text;
  xml = applyOperation(xml, { kind: 'insert', flowId: 'flow:studioFlow', component: 'listener' }).text;
  xml = applyOperation(xml, { kind: 'insert', flowId: 'flow:studioFlow', component: 'logger' }).text;
  xml = applyOperation(xml, { kind: 'insert', flowId: 'flow:studioFlow', component: 'logger' }).text;
  const file = vscode.Uri.file(path.join(workspace, 'src/main/zernova/hello.xml'));
  await fs.writeFile(file.fsPath, xml);
  const subject = vscode.extensions.getExtension('zernova-preview.zernova-studio-kernel-preview')!;
  const api = await subject.activate(); await api.whenInitialized();
  await fs.writeFile(path.join(root, 'ready.json'), JSON.stringify({ workspace, port, file: file.fsPath, extensionPath: subject.extensionPath }));
  if (!vscode.workspace.isTrusted) await new Promise<void>(resolve => { const event = vscode.workspace.onDidGrantWorkspaceTrust(() => { event.dispose(); resolve(); }); });
  const command = async (action: string) => { await vscode.commands.executeCommand('zernovaKernelStudio.' + action, file); return api.getRuntimeDiagnostics()[0]; };
  const edit = async (text: string) => {
    const doc = await vscode.workspace.openTextDocument(file); const change = new vscode.WorkspaceEdit();
    change.replace(file, new vscode.Range(doc.positionAt(0), doc.positionAt(doc.getText().length)), text);
    assert(await vscode.workspace.applyEdit(change));
    for (let i = 0; i < 60 && doc.isDirty; i++) await new Promise(resolve => setTimeout(resolve, 50));
    assert(!doc.isDirty, 'Native XML edits must autosave without explicit Save');
    assert.equal(await fs.readFile(file.fsPath, 'utf8'), text);
  };
  const request = async (route = '/hello') => {
    await waitForHttpReady(`http://localhost:${port}${route}`);
    const id = 'STUDIO-' + randomUUID(); const response = await fetch(`http://localhost:${port}${route}?testId=${id}`);
    const body = await response.text(); assert.equal(response.status, 200); assert.equal(body, 'Hello from ZERNOVA');
    let logs = '';
    for (let attempt = 0; attempt < 60; attempt++) { logs = api.getRuntimeDiagnostics()[0].applicationLogs; if (logs.split('\n').filter((line: string) => line.includes(id)).length >= 2) break; await new Promise(resolve => setTimeout(resolve, 100)); }
    assert(logs.split('\n').filter((line: string) => line.includes(id)).length >= 2, 'Both fresh Logger entries must appear automatically without Refresh');
    return { status: response.status, body, correlatedLogger: true, automaticLogRefresh: true, loggerCount: 2 };
  };
  try {
    await check('Installed VSIX and actual custom editor', async () => {
      assert(subject.extensionPath.startsWith(path.join(root, 'extensions'))); assert.equal(subject.packageJSON.version, '0.4.3');
      await vscode.commands.executeCommand('zernovaKernelStudio.openIntegration', file);
      for (let i = 0; i < 50 && !api.getProjectDiagnostics().documents.documents.includes(file.toString()); i++) await new Promise(resolve => setTimeout(resolve, 100));
      assert(api.getProjectDiagnostics().documents.documents.includes(file.toString()), 'The actual custom editor must resolve this project, including Windows drive-letter casing');
      return { extensionPath: subject.extensionPath, model: parseIntegration(xml).flows[0].name };
    });
    await check('Bundled Java/runtime diagnostics with invalid external tool paths', async () => { const state = await command('diagnostics'); assert.equal(state.phase, 'Diagnostics passed'); assert(state.runtimeOutput.includes('0.4.1-alpha.1')); return { phase: state.phase, offline: state.offline }; });
    await check('Studio offline build produces a fresh application', async () => { const state = await command('build'); assert.equal(state.phase, 'Build succeeded · not deployed'); assert(state.artifact.endsWith('.zapp')); return { artifact: state.artifact, sha256: state.artifactSha256 }; });
    await check('Studio Run deploys to the renamed source-built engine', async () => { const state = await command('run'); assert.equal(state.application, 'DEPLOYED'); assert.equal(state.process, 'RUNNING'); return request(); });
    await check('XML edits save and redeploy without the old route', async () => { await edit(xml.replace('path="/hello"', 'path="/studio-v2"')); const state = await command('run'); assert.equal(state.application, 'DEPLOYED'); const result = await request('/studio-v2'); assert.equal((await fetch(`http://localhost:${port}/hello`)).status, 404); return result; });
    await check('Stop Application undeploys and retains the private runtime', async () => { const state = await command('stopApplication'); assert.equal(state.process, 'RUNNING'); assert.equal(state.application, 'NOT_DEPLOYED'); });
    await check('Stop Runtime shuts down the owned engine', async () => { const state = await command('stopRuntime'); assert.equal(state.process, 'STOPPED'); });
    await check('Invalid XML is diagnosed and cannot deploy a stale artifact', async () => { await edit(xml.replace('</zernova>', '</broken>')); const state = await command('run'); assert.equal(state.phase, 'Failed'); assert.equal(state.process, 'STOPPED'); assert(state.lastError); });
    await check('Restored document runs again', async () => { await edit(xml); const state = await command('run'); assert.equal(state.application, 'DEPLOYED'); return request(); });
    const config = vscode.workspace.getConfiguration('zernovaKernelStudio', file);
    const runId = async () => JSON.parse(await fs.readFile(path.join(api.getRuntimeDiagnostics()[0].instanceDirectory, 'state.json'), 'utf8')).run_id;
    await check('Redeploy setting defaults off; manual save retains the deployed JVM', async () => {
      assert.equal(config.get('redeployOnSave'), false); const before = await runId();
      await edit(xml + '\n<!-- changed with setting off -->\n'); await vscode.commands.executeCommand('zernovaKernelStudio.manualSave', file);
      assert.equal(await runId(), before); return {defaultOff:true};
    });
    await check('Background XML autosave never redeploys when the setting is on', async () => {
      await config.update('redeployOnSave', true, vscode.ConfigurationTarget.WorkspaceFolder); const before = await runId();
      await edit(xml + '\n<!-- autosave only -->\n'); await new Promise(resolve => setTimeout(resolve, 1200));
      assert.equal(await runId(), before); return {autosaved:true,redeployed:false};
    });
    await check('Explicit save of already autosaved XML redeploys once; unchanged save does not', async () => {
      const before = await runId(); await vscode.commands.executeCommand('zernovaKernelStudio.manualSave', file);
      const after = await runId(); assert.notEqual(after, before); await request();
      await vscode.commands.executeCommand('zernovaKernelStudio.manualSave', file); assert.equal(await runId(),after);
      return {changedSaveRedeployed:true,unchangedSaveRedeployed:false};
    });
    await check('Manual property-file save redeploys the running app', async () => {
      const uri=vscode.Uri.file(path.join(workspace,'src/main/resources/application.properties')); const doc=await vscode.workspace.openTextDocument(uri);
      const before=await runId(); const change=new vscode.WorkspaceEdit(); change.insert(uri,new vscode.Position(0,0),'# manual property save\n'); assert(await vscode.workspace.applyEdit(change));
      assert(await doc.save());
      for(let i=0;i<1800;i++){const state=api.getRuntimeDiagnostics()[0];if(await runId()!==before && !state.busy && state.application==='DEPLOYED') break; await new Promise(resolve=>setTimeout(resolve,100));}
      assert.notEqual(await runId(),before); assert.equal(api.getRuntimeDiagnostics()[0].phase,'Deployed · endpoint unverified'); return request();
    });
    await check('Transform Message creates native DataWeave XML and returns actual JSON', async () => {
      await config.update('redeployOnSave',false,vscode.ConfigurationTarget.WorkspaceFolder);
      let transformed=applyOperation(xml,{kind:'insert',flowId:'flow:studioFlow',component:'transform',index:1});
      const script='%dw 2.0\noutput application/json\n---\n{message: "Hello from Transform", testId: attributes.queryParams.testId, environment: p("zernova.env")}';
      let text=applyOperation(transformed.text,{kind:'updateComponent',id:transformed.selectionId!,values:{script,mimeType:'application/json'}}).text;
      text=applyOperation(text,{kind:'updateComponent',id:parseIntegration(text).flows[0].source!.id,values:{responseBody:'#[payload]',responseHeaders:"#[{'Content-Type': 'application/json'}]"}}).text;
      await edit(text);const state=await command('run');assert.equal(state.application,'DEPLOYED');
      await waitForHttpReady(`http://localhost:${port}/hello`);const id='TRANSFORM-'+randomUUID();const response=await fetch(`http://localhost:${port}/hello?testId=${id}`);const body=JSON.parse(await response.text());
      assert.equal(response.status,200);assert.deepEqual(body,{message:'Hello from Transform',testId:id,environment:'development'});
      return {nativeDataWeave:true,status:response.status,body};
    });
    await check('Listener-only run failure stays in Console and retains the deployed app', async () => {
      await config.update('redeployOnSave',false,vscode.ConfigurationTarget.WorkspaceFolder);
      const only=xml.replace(/\s*<logger\b[^>]*\/>/g,''); assert.notEqual(only,xml); await edit(only);
      const state=await command('run'); assert.equal(state.phase,'Failed'); assert.match(state.buildOutput,/HTTP Listener but no processors/); assert.match(state.lastError,/Add a processor/);
      assert.equal(state.application,'DEPLOYED'); return {persistentBuildError:true,existingAppRetained:true};
    });
    await check('Manual Java save attempts a build and retains its unsupported-build error', async () => {
      await edit(xml); await config.update('redeployOnSave',true,vscode.ConfigurationTarget.WorkspaceFolder);
      const folder=path.join(workspace,'src/main/java');await fs.mkdir(folder,{recursive:true}); const uri=vscode.Uri.file(path.join(folder,'SaveProbe.java'));await fs.writeFile(uri.fsPath,'');
      const doc=await vscode.workspace.openTextDocument(uri);const change=new vscode.WorkspaceEdit();change.insert(uri,new vscode.Position(0,0),'class SaveProbe {}\n');assert(await vscode.workspace.applyEdit(change));
      const before=await runId();await vscode.commands.executeCommand('zernovaKernelStudio.manualSave',uri);
      const state=api.getRuntimeDiagnostics()[0];assert.match(state.buildOutput,/does not compile custom Java/);assert.equal(state.phase,'Failed');assert.equal(await runId(),before);
      await fs.rm(uri.fsPath);await config.update('redeployOnSave',false,vscode.ConfigurationTarget.WorkspaceFolder);return {javaChangeDetected:true,unsupportedBuildReported:true,existingAppRetained:true};
    });
    await check('Final shutdown', async () => { assert.equal((await command('stopRuntime')).process,'STOPPED'); });
  } finally {
    await command('stopRuntime'); await edit(xml); await persist();
    await fs.writeFile(path.join(root, 'complete.json'), JSON.stringify({ passed: rows.filter(r => r.status === 'PASS').length, failed: rows.filter(r => r.status === 'FAIL').length }));
  }
}
