const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vscode = require('vscode');
const results = [];
const waitFor = async (predicate, message, timeout = 15000) => {
  const started = Date.now();
  while (!predicate()) { if (Date.now() - started > timeout) throw new Error(message); await new Promise(resolve => setTimeout(resolve, 100)); }
};
const check = async (name, fn) => { try { await fn(); results.push({ name, status: 'PASS' }); } catch (error) { results.push({ name, status: 'FAIL', error: String(error.stack || error) }); throw error; } };
exports.run = async () => {
  const evidence = { startedAt: new Date().toISOString(), phase: process.env.ZERNOVA_HOST_PHASE || 'primary', version: vscode.version, platform: process.platform, architecture: process.arch, node: process.version, uiKind: vscode.env.uiKind, results };
  const originalSettings = JSON.stringify({ theme: vscode.workspace.getConfiguration('workbench').get('colorTheme'), associations: vscode.workspace.getConfiguration('files').get('associations') });
  try {
    const subject = vscode.extensions.getExtension('zernova-preview.zernova-studio-kernel-preview');
    assert.ok(subject, 'Packaged extension is installed');
    evidence.extensionPath = subject.extensionPath;
    evidence.installedExtensions = vscode.extensions.all.filter(ext => !ext.packageJSON.isBuiltin && ext.extensionPath.startsWith(process.env.ZERNOVA_SUBJECT_EXTENSIONS)).map(ext => ext.id);
    if (process.env.ZERNOVA_HOST_PHASE === 'restart') {
      const api = await subject.activate();
      await check('non-default preview state survives a complete desktop host exit/restart', () => {
        assert.equal(api.getPreviewDiagnostics().state.editorTab, 'dataweave');
      });
      await check('restarted installed preview completes its webview handshake', async () => {
        await vscode.commands.executeCommand('zernovaKernelStudio.openPreview');
        await waitFor(() => api.getPreviewDiagnostics().ready, 'Restarted preview not ready');
        assert.equal(api.getPreviewDiagnostics().panelCount, 1);
      });
      await check('restored webview and repeated opens create only one actual native editor tab', async () => {
        for (let i = 0; i < 5; i++) await vscode.commands.executeCommand('zernovaKernelStudio.openPreview');
        const tabs = vscode.window.tabGroups.all.flatMap(group => group.tabs).filter(tab => tab.input instanceof vscode.TabInputWebview && tab.input.viewType.includes('zernovaKernelStudio.preview'));
        assert.equal(tabs.length, 1);
        assert.equal(api.getPreviewDiagnostics().panelCount, 1);
      });
      await check('Reset Preview after restart clears only the namespaced fixture state', async () => {
        await vscode.commands.executeCommand('zernovaKernelStudio.resetPreview');
        await api.whenStateSaved();
        assert.equal(api.getPreviewDiagnostics().state.editorTab, 'flow');
        assert.equal(api.getPreviewDiagnostics().state.selectedId, null);
        assert.equal(JSON.stringify({ theme: vscode.workspace.getConfiguration('workbench').get('colorTheme'), associations: vscode.workspace.getConfiguration('files').get('associations') }), originalSettings);
      });
      evidence.status = 'PASS';
      return;
    }
    await check('subject is the independently installed VSIX, not a development-path replacement', () => {
      assert.ok(subject.extensionPath.startsWith(process.env.ZERNOVA_SUBJECT_EXTENSIONS));
      assert.equal(subject.packageJSON.version, '0.2.0');
      assert.equal(subject.packageJSON.name, 'zernova-studio-kernel-preview');
      assert.deepEqual(evidence.installedExtensions, ['zernova-preview.zernova-studio-kernel-preview']);
    });
    await check('native contributions contain exactly one new ZERNOVA Activity Bar entry', () => {
      assert.equal(subject.packageJSON.contributes.viewsContainers.activitybar.length, 1);
      assert.equal(subject.packageJSON.contributes.viewsContainers.activitybar[0].id, 'zernovaKernelStudio');
      assert.deepEqual(subject.packageJSON.contributes.customEditors, [{ viewType: 'zernovaKernelStudio.integration', displayName: 'ZERNOVA Integration', selector: [{ filenamePattern: '**/src/main/mule/*.xml' }], priority: 'option' }]);
      assert.equal(subject.packageJSON.contributes.configurationDefaults, undefined);
    });
    const api = await subject.activate();
    await check('all own commands use the established namespace and only the approved milestone actions', async () => {
      const commands = (await vscode.commands.getCommands(true)).filter(command => command.startsWith('zernovaKernelStudio.')).sort();
      const own = subject.packageJSON.contributes.commands.map(command => command.command).sort();
      assert.deepEqual(own, ['openPreview','openScenario','resetLayout','resetPreview','newProject','openProject','newIntegration','openIntegration','configureRuntime','diagnostics','build','run','status','logs','stopApplication','stopRuntime','cancel','selectProject'].map(name => 'zernovaKernelStudio.' + name).sort());
      for (const command of own) assert.ok(commands.includes(command));
      for (const command of commands) assert.ok(own.includes(command) || /^zernovaKernelStudio\.(projects|previewViews|runtimeInfo)\.(focus|open|removeView|resetViewLocation|toggleVisibility)$/.test(command), `Unexpected command ${command}`);
    });
    await check('opening the installed UI reaches a ready webview without Java/Maven/runtime prerequisites', async () => {
      await vscode.commands.executeCommand('zernovaKernelStudio.openPreview');
      await waitFor(() => api.getPreviewDiagnostics().ready, 'Webview did not complete ready handshake');
      assert.equal(api.getPreviewDiagnostics().panelCount, 1);
      assert.ok(!fs.existsSync(process.env.JAVA_HOME));
      assert.ok(!fs.existsSync(process.env.ZERNOVA_MAVEN));
      assert.ok(!fs.existsSync(process.env.ZERNOVA_WORK_DIR));
    });
    await check('repeated preview invocation keeps exactly one editor panel', async () => {
      const created = api.getPreviewDiagnostics().panelsCreated;
      for (let i = 0; i < 10; i++) await vscode.commands.executeCommand('zernovaKernelStudio.openPreview');
      assert.equal(api.getPreviewDiagnostics().panelCount, 1);
      assert.equal(api.getPreviewDiagnostics().panelsCreated, created);
    });
    await check('all six native scenario commands navigate the shared installed webview', async () => {
      for (const [scenario, expected] of [['workbench', 'flow'], ['listener', 'flow'], ['logger', 'flow'], ['globals', 'globals'], ['xml', 'xml'], ['dataweave', 'dataweave']]) {
        await vscode.commands.executeCommand('zernovaKernelStudio.openScenario', scenario);
        await waitFor(() => api.getPreviewDiagnostics().state.editorTab === expected, `Scenario ${scenario} not applied`);
      }
      assert.equal(api.getPreviewDiagnostics().panelCount, 1);
    });
    await check('embedded XML keeps one preview editor and creates no text document', async () => {
      const documentCount = vscode.workspace.textDocuments.length;
      await vscode.commands.executeCommand('zernovaKernelStudio.openScenario', 'xml');
      await waitFor(() => api.getPreviewDiagnostics().state.editorTab === 'xml', 'XML preview not applied');
      assert.equal(vscode.workspace.textDocuments.length, documentCount);
      assert.equal(api.getPreviewDiagnostics().panelCount, 1);
    });
    await check('ordinary XML still opens in the ordinary text editor unchanged', async () => {
      const file = vscode.Uri.joinPath(vscode.workspace.workspaceFolders[0].uri, 'ordinary.xml');
      const doc = await vscode.workspace.openTextDocument(file);
      await vscode.window.showTextDocument(doc);
      assert.equal(vscode.window.activeTextEditor.document.uri.toString(), file.toString());
      assert.equal(doc.getText(), '<ordinary><unchanged /></ordinary>\n');
      assert.equal(doc.isDirty, false);
      await vscode.commands.executeCommand('workbench.action.closeActiveEditor');
      await vscode.commands.executeCommand('zernovaKernelStudio.openPreview');
    });
    await check('preview persistence survives closing and reopening the editor', async () => {
      await vscode.commands.executeCommand('zernovaKernelStudio.openScenario', 'dataweave');
      await waitFor(() => api.getPreviewDiagnostics().state.editorTab === 'dataweave', 'DataWeave preview not applied');
      await api.whenStateSaved();
      await vscode.commands.executeCommand('workbench.action.closeActiveEditor');
      await waitFor(() => api.getPreviewDiagnostics().panelCount === 0, 'Preview did not close');
      await vscode.commands.executeCommand('zernovaKernelStudio.openPreview');
      await waitFor(() => api.getPreviewDiagnostics().ready, 'Reopened preview not ready');
      assert.equal(api.getPreviewDiagnostics().state.editorTab, 'dataweave');
      assert.equal(api.getPreviewDiagnostics().panelCount, 1);
    });
    await check('explicit Reset Preview restores the versioned fixture only', async () => {
      await vscode.commands.executeCommand('zernovaKernelStudio.resetPreview');
      await api.whenStateSaved();
      const state = api.getPreviewDiagnostics().state;
      assert.equal(state.schemaVersion, 1);
      assert.equal(state.editorTab, 'flow');
      assert.equal(state.selectedId, null);
      assert.equal(state.flow.processors[0].displayName, 'Log request');
      assert.equal(api.getPreviewDiagnostics().stateKey, 'zernovaKernelStudio.preview.v1');
    });
    await check('global theme and file associations remain unchanged', () => {
      assert.equal(JSON.stringify({ theme: vscode.workspace.getConfiguration('workbench').get('colorTheme'), associations: vscode.workspace.getConfiguration('files').get('associations') }), originalSettings);
    });
    await check('non-default preview state is saved before the cold-restart phase', async () => {
      await vscode.commands.executeCommand('zernovaKernelStudio.openScenario', 'dataweave');
      await waitFor(() => api.getPreviewDiagnostics().state.editorTab === 'dataweave', 'Pre-restart view not applied');
      await api.whenStateSaved();
    });
    evidence.status = 'PASS';
  } catch (error) { evidence.status = 'FAIL'; throw error; }
  finally {
    evidence.completedAt = new Date().toISOString();
    evidence.passed = results.filter(r => r.status === 'PASS').length;
    evidence.failed = results.filter(r => r.status === 'FAIL').length;
    fs.writeFileSync(process.env.ZERNOVA_HOST_EVIDENCE, JSON.stringify(evidence, null, 2));
  }
};
