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
      assert(subject.extensionPath.startsWith(path.join(root, 'extensions'))); assert.equal(subject.packageJSON.version, '0.6.0');
      await vscode.commands.executeCommand('zernovaKernelStudio.openIntegration', file);
      for (let i = 0; i < 50 && !api.getProjectDiagnostics().documents.documents.includes(file.toString()); i++) await new Promise(resolve => setTimeout(resolve, 100));
      assert(api.getProjectDiagnostics().documents.documents.includes(file.toString()), 'The actual custom editor must resolve this project, including Windows drive-letter casing');
      return { extensionPath: subject.extensionPath, model: parseIntegration(xml).flows[0].name };
    });
    await check('Explorer exposes applicable resource, execution and dependency menus',async()=>{
      const commands=await vscode.commands.getCommands(true);for(const name of ['newFile','newFolder','renameFile','moveFile','deleteFile','manageDependencies','inspectArtifact','openPom','cleanProject','configureRun','configureDebug'])assert(commands.includes('zernovaKernelStudio.'+name),name);
      const menus=subject.packageJSON.contributes.menus;assert(menus['view/item/context'].some((e:any)=>e.command==='zernovaKernelStudio.renameFile'));assert(menus['explorer/context'].some((e:any)=>e.submenu==='zernovaKernelStudio.explorerProject'));
      const artifacts=await api.getArtifactDiagnostics(file);assert(artifacts.dependencies.some((d:any)=>d.label.includes('zernova-http-connector')&&d.description.includes('resolved')));const jars=artifacts.dependencies.flatMap((d:any)=>d.children??[]);assert(jars.length>=9);for(const jar of jars)assert((await fs.stat(jar.file)).isFile());assert(artifacts.runtime.some((a:any)=>a.label.includes('ZERNOVA Runtime 0.4.1')));return {bundledMavenJars:jars.length,menus:true};
    });
    await check('New Integration dialog stays in the current Studio tab and cancellation creates no file',async()=>{
      const tabs=vscode.window.tabGroups.all.flatMap(g=>g.tabs).length;const before=await fs.readFile(file.fsPath,'utf8');const filesBefore=(await fs.readdir(path.dirname(file.fsPath))).sort();
      const pending=vscode.commands.executeCommand('zernovaKernelStudio.newIntegration',file);for(let i=0;i<100&&!api.getProjectDiagnostics().documents.forms.length;i++)await new Promise(resolve=>setTimeout(resolve,50));
      const forms=api.getProjectDiagnostics().documents.forms;assert.equal(forms.length,1);assert.equal(forms[0].uri,file.toString());assert.equal(forms[0].title,'New Integration XML');assert.equal(vscode.window.tabGroups.all.flatMap(g=>g.tabs).length,tabs);
      await vscode.commands.executeCommand('workbench.action.closeActiveEditor');assert.equal(await pending,undefined);assert.equal(await fs.readFile(file.fsPath,'utf8'),before);assert.deepEqual((await fs.readdir(path.dirname(file.fsPath))).sort(),filesBefore);await vscode.commands.executeCommand('zernovaKernelStudio.openIntegration',file);return {inline:true,extraTabs:0,originalUnchanged:true};
    });
    await check('Bundled Java/runtime diagnostics with invalid external tool paths', async () => { const state = await command('diagnostics'); assert.equal(state.phase, 'Diagnostics passed'); assert(state.runtimeOutput.includes('0.4.1-alpha.1')); return { phase: state.phase, offline: state.offline }; });
    await check('Studio offline build produces a fresh application', async () => { const state = await command('build'); assert.equal(state.phase, 'Build succeeded · not deployed'); assert(state.artifact.endsWith('.zapp')); return { artifact: state.artifact, sha256: state.artifactSha256 }; });
    await check('Studio Run deploys to the renamed source-built engine', async () => { const state = await command('run'); assert.equal(state.application, 'DEPLOYED'); assert.equal(state.process, 'RUNNING'); assert(state.deploymentOutput.includes('Z E R N O V A'));assert(state.deploymentOutput.includes('BUILD'));assert(state.deploymentOutput.includes('DEPLOY'));return request(); });
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
    await check('Installed Studio deploys an external transform with environment properties and nested core scope',async()=>{
      const resources=path.join(workspace,'src/main/resources');await fs.mkdir(path.join(resources,'dwl'),{recursive:true});await fs.mkdir(path.join(resources,'config'),{recursive:true});
      await fs.writeFile(path.join(resources,'dwl/message.dwl'),'%dw 2.0\noutput application/json\n---\n{message:p("app.message"),environment:p("zernova.env"),value:vars.coreValue}');
      await fs.writeFile(path.join(resources,'config/development.properties'),'app.message=External ZERNOVA mapping\n');
      let text=applyOperation(xml,{kind:'saveGlobalProperty',name:'zernova.env',value:'development'}).text;
      text=applyOperation(text,{kind:'savePropertyFile',file:'config/${zernova.env}.properties'}).text;
      const scope=applyOperation(text,{kind:'insert',flowId:'flow:studioFlow',component:'try',index:0});text=scope.text;
      const variable=applyOperation(text,{kind:'insert',flowId:'flow:studioFlow',component:'set-variable',parentId:scope.selectionId});text=variable.text;
      text=applyOperation(text,{kind:'updateCore',id:variable.selectionId!,values:{variableName:'coreValue',value:'native scope'}}).text;
      const transform=applyOperation(text,{kind:'insert',flowId:'flow:studioFlow',component:'transform'});text=transform.text;
      text=applyOperation(text,{kind:'updateComponent',id:transform.selectionId!,values:{script:'',payloadResource:'dwl/message.dwl',mimeType:'application/json'}}).text;
      text=applyOperation(text,{kind:'updateComponent',id:parseIntegration(text).flows[0].source!.id,values:{responseBody:'#[payload]',responseHeaders:"#[{'Content-Type':'application/json'}]",errorStatus:'503',errorBody:"#['ZERNOVA error']"}}).text;
      await edit(text);const state=await command('run');assert.equal(state.application,'DEPLOYED');await waitForHttpReady(`http://localhost:${port}/hello`);
      const response=await fetch(`http://localhost:${port}/hello`);assert.equal(response.status,200);const body=JSON.parse(await response.text());assert.deepEqual(body,{message:'External ZERNOVA mapping',environment:'development',value:'native scope'});return body;
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
    await check('Transform sets payload, attributes and named variables from the same input',async()=>{
      let inserted=applyOperation(xml,{kind:'insert',flowId:'flow:studioFlow',component:'transform',index:1});
      let text=applyOperation(inserted.text,{kind:'updateComponent',id:inserted.selectionId!,values:{script:'{message: "Three outputs"}',mimeType:'application/java',attributesScript:'{correlation: attributes.queryParams.testId}',variablesScript:'{savedId: attributes.queryParams.testId}'}}).text;
      text=applyOperation(text,{kind:'updateComponent',id:parseIntegration(text).flows[0].source!.id,values:{responseBody:'#[%dw 2.0\noutput application/json\n---\n{body: payload, attributes: attributes, savedId: vars.savedId}]',responseHeaders:"#[{'Content-Type': 'application/json'}]"}}).text;
      await edit(text);assert.equal((await command('run')).application,'DEPLOYED');
      const id='ALL-'+randomUUID(),response=await fetch(`http://localhost:${port}/hello?testId=${id}`),body=JSON.parse(await response.text());assert.equal(response.status,200);assert.deepEqual(body,{body:{message:'Three outputs'},attributes:{correlation:id},savedId:id});
      return {sameInput:true,body};
    });
    await check('Embedded component debugger pauses HTTP, steps, continues and disconnects without VS Code debug session',async()=>{
      const current=parseIntegration((await vscode.workspace.openTextDocument(file)).getText()),first=current.flows[0].processors[0];
      await vscode.commands.executeCommand('zernovaKernelStudio.toggleComponentBreakpoint',file,first.id);
      const connected=await command('debug');assert.equal(connected.debugConnected,true);assert.equal(vscode.debug.activeDebugSession,undefined);
      const waitPaused=async(location:string)=>{for(let attempt=0;attempt<150;attempt++){if(api.getRuntimeDiagnostics()[0].debugPaused===location)return;await new Promise(resolve=>setTimeout(resolve,100));}throw new Error('Debugger did not pause at '+location);};
      let completed=false;const response=fetch(`http://localhost:${port}/hello?testId=DEBUG-044`,{signal:AbortSignal.timeout(30000)});response.then(()=>completed=true,()=>completed=true);
      await waitPaused('studioFlow/processors/0');await new Promise(resolve=>setTimeout(resolve,200));assert.equal(completed,false);
      await command('debugStep');await waitPaused('studioFlow/processors/1');assert.equal(completed,false);
      await command('debugContinue');assert.equal((await response).status,200);
      const detached=fetch(`http://localhost:${port}/hello?testId=DETACH-044`,{signal:AbortSignal.timeout(30000)});await waitPaused('studioFlow/processors/0');await command('debugDisconnect');assert.equal((await detached).status,200);assert.equal(api.getRuntimeDiagnostics()[0].debugConnected,false);
      await vscode.commands.executeCommand('zernovaKernelStudio.toggleComponentBreakpoint',file,first.id);
      return {heldBeforeExecution:true,step:true,continue:true,disconnectResumes:true,defaultDebugSessionOpened:false};
    });
    await check('Installed Studio builds and runs the shipped ALL-core app as one XML',async()=>{
      await config.update('redeployOnSave',false,vscode.ConfigurationTarget.WorkspaceFolder);const example=path.join(subject.extensionPath,'examples/all-core');const resources=path.join(workspace,'src/main/resources');
      for(const folder of ['config','dwl','templates'])await fs.cp(path.join(example,'src/main/resources',folder),path.join(resources,folder),{recursive:true});await fs.copyFile(path.join(example,'src/main/resources/retry-ready.txt'),path.join(resources,'retry-ready.txt'));
      const properties=path.join(resources,'config/application-development.properties');await fs.writeFile(properties,(await fs.readFile(properties,'utf8')).replace('http.port=18081','http.port='+port));
      const text=await fs.readFile(path.join(example,'src/main/zernova/all-core.xml'),'utf8');await edit(text);const model=parseIntegration(text);assert.equal(model.flows.length,21);const state=await command('run');assert.equal(state.application,'DEPLOYED');await waitForHttpReady(`http://localhost:${port}/hello`);
      let response=await fetch(`http://localhost:${port}/hello?testId=INSTALLED-ALL-CORE`);assert.equal(response.status,200);const body=JSON.parse(await response.text());assert.equal(body.payload.message,'Hello from ZERNOVA');assert.equal(body.marker,'external-attributes');assert.equal(body.variables.transformed,true);
      response=await fetch(`http://localhost:${port}/batch`);assert.equal(response.status,200);assert.equal(JSON.parse(await response.text()).payload.accepted,true);let logs='';for(let i=0;i<300;i++){logs=api.getRuntimeDiagnostics()[0].applicationLogs;if(logs.includes('name=allCoreBatch status=COMPLETED'))break;await new Promise(resolve=>setTimeout(resolve,100));}assert(logs.includes('total=5 failed=1 processed=5'));return {flows:21,externalOutputs:true,batchCompleted:true};
    });
    await check('Final shutdown', async () => { assert.equal((await command('stopRuntime')).process,'STOPPED'); });
  } finally {
    await command('stopRuntime'); await edit(xml); await persist();
    await fs.writeFile(path.join(root, 'complete.json'), JSON.stringify({ passed: rows.filter(r => r.status === 'PASS').length, failed: rows.filter(r => r.status === 'FAIL').length }));
  }
}
