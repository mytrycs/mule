/** Development-only public VS Code API driver for a separately installed VSIX.
 * Private file IPC is confined to this runner's temporary workspace; the subject
 * extension has no mutable testing API and receives genuine webview messages.
 */
const vscode = require('vscode');
const fs = require('node:fs');
const path = require('node:path');
exports.activate = async context => {
  const home = process.env.ZERNOVA_DOCUMENT_TEST_HOME;
  if (!home) throw new Error('Missing isolated document-test directory');
  const ipc = path.join(home, 'ipc');
  fs.mkdirSync(ipc, { recursive: true });
  const file = vscode.Uri.file(path.join(home, 'workspace', 'src', 'main', 'zernova', 'hello.xml'));
  let lastId = '', busy = false;
  const subject = vscode.extensions.getExtension('zernova-preview.zernova-studio-kernel-preview');
  if (!subject) throw new Error('The subject VSIX must be installed independently.');
  await subject.activate();
  const document = () => vscode.workspace.openTextDocument(file);
  const info = async () => {
    const doc = await document();
    return { uri: file.toString(), version: doc.version, text: doc.getText(), dirty: doc.isDirty,
      hostVersion: vscode.version, extensionVersion: subject.packageJSON.version, extensionPath: subject.extensionPath,
      diagnostics: vscode.languages.getDiagnostics(file).map(d => ({ message: d.message, code: d.code, severity: d.severity })),
      tabs: vscode.window.tabGroups.all.flatMap(group => group.tabs).map(tab => ({ label: tab.label, dirty: tab.isDirty,
        viewType: tab.input instanceof vscode.TabInputCustom ? tab.input.viewType : null, uri: tab.input.uri?.toString() ?? null })) };
  };
  const handle = async request => {
    if (request.operation === 'info') return info();
    if (request.operation === 'clipboard') return vscode.env.clipboard.readText();
    if (request.operation === 'open') { await vscode.commands.executeCommand('vscode.openWith', file, 'zernovaKernelStudio.integration'); return info(); }
    if (request.operation === 'replace') {
      if (typeof request.text !== 'string' || request.text.length > 1024 * 1024) throw new Error('Invalid fixture text');
      const doc = await document(), edit = new vscode.WorkspaceEdit();
      edit.replace(file, new vscode.Range(doc.positionAt(0), doc.positionAt(doc.getText().length)), request.text);
      if (!await vscode.workspace.applyEdit(edit)) throw new Error('Public WorkspaceEdit failed');
      return info();
    }
    if (request.operation === 'save') { await (await document()).save(); return info(); }
    if (request.operation === 'undo' || request.operation === 'redo') { await vscode.commands.executeCommand(request.operation); return info(); }
    if (request.operation === 'close') { void vscode.commands.executeCommand('workbench.action.closeActiveEditor'); return { requested: true }; }
    if (request.operation === 'quit') { setTimeout(() => { void vscode.commands.executeCommand('workbench.action.quit'); }, 100); return { requested: true }; }
    if (request.operation === 'reload') { setTimeout(() => { void vscode.commands.executeCommand('workbench.action.reloadWindow'); }, 100); return { requested: true }; }
    throw new Error('Unknown document-test operation');
  };
  const timer = setInterval(async () => {
    if (busy) return;
    let request;
    try { request = JSON.parse(fs.readFileSync(path.join(ipc, 'request.json'), 'utf8')); } catch { return; }
    if (typeof request.id !== 'string' || request.id === lastId) return;
    busy = true; lastId = request.id;
    let response;
    try { response = { id: request.id, result: await handle(request) }; }
    catch (error) { response = { id: request.id, error: String(error.stack || error) }; }
    const temporary = path.join(ipc, 'response.tmp');
    fs.writeFileSync(temporary, JSON.stringify(response)); fs.renameSync(temporary, path.join(ipc, 'response.json'));
    busy = false;
  }, 30);
  context.subscriptions.push({ dispose: () => clearInterval(timer) });
  fs.writeFileSync(path.join(ipc, 'ready.json'), JSON.stringify({ version: subject.packageJSON.version, extensionPath: subject.extensionPath, vscode: vscode.version, pid: process.pid }));
};
