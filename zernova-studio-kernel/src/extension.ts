import * as vscode from 'vscode';
import { saveAutomatically, isAutomaticSave } from './host/saves';
import { createInitialState, sanitizeState, cloneState, PREVIEW_STATE_KEY, DEFAULT_LAYOUT, type PreviewState, type Scenario } from './shared/model';
import { parseWebviewMessage, isScenario } from './shared/protocol';
import { registerNavigation } from './host/navigation';
import { currentHostTheme, renderWebview } from './host/webview';
import { ProjectManager, isInside } from './host/projects';
import { RuntimeManager } from './host/runtime';
import { IntegrationEditorProvider, REAL_EDITOR_TYPE } from './host/documents';
import { RUNTIME_ACTIONS, type RuntimeAction } from './shared/realProtocol';
import * as fs from 'node:fs/promises';
import path from 'node:path';

const VIEW_TYPE = 'zernovaKernelStudio.preview';
let activePanel: vscode.WebviewPanel | undefined;

/** Real project host and isolated design preview share the approved workbench. */
export function activate(context: vscode.ExtensionContext) {
  const projects = new ProjectManager(context);
  const runtime = new RuntimeManager(context, projects);
  const documents = new IntegrationEditorProvider(context, {
    render: webview => renderWebview(webview, context.extensionUri, createInitialState(true), true),
    projectName: uri => projects.nameForUri(uri), projectRoot: uri => projects.rootForUri(uri),
    manualSave: async doc => { if (!await saveAutomatically(doc)) throw new Error('The document could not be saved.'); const project = projects.projectForUri(doc.uri); if (project) await runtime.manualSaved(project, doc.uri); },
    breakpoint: async (uri,id) => { const project=projects.projectForUri(uri); if(!project) throw new Error('Open a ZERNOVA project.'); await runtime.toggleBreakpoint(project,uri,id); },
    runtimeState: uri => runtime.stateForUri(uri),
    runtimeAction: async (action, uri) => {
      const project = projects.projectForUri(uri);
      if (!project) throw new Error('Open this file through ZERNOVA: Open ZERNOVA Project before executing it.');
      try { await runtime.execute(action, project); } catch (error) { runtime.reportError(project, error); throw error; }
    },
  });
  context.subscriptions.push(projects, runtime, documents,
    vscode.window.registerCustomEditorProvider(REAL_EDITOR_TYPE, documents, { webviewOptions: { retainContextWhenHidden: true }, supportsMultipleEditorsPerDocument: false }),
    runtime.onDidChange(root => { void documents.refreshRuntime(vscode.Uri.file(root)); }),
    projects.onDidChange(() => { void documents.refreshRuntime(); }),
    documents.onDidChangeActiveDocument(uri => { const project = uri ? projects.projectForUri(uri) : undefined; if (project) { void projects.select(uri); void runtime.reconcile(project); } }),
  );
  const xmlSaves = new Map<string, ReturnType<typeof setTimeout>>();
  context.subscriptions.push(vscode.workspace.onDidChangeTextDocument(event => {
    const doc = event.document, project = projects.projectForUri(doc.uri);
    if (!project || !doc.uri.fsPath.endsWith('.xml') || !isInside(path.join(project.root, 'src/main/zernova'), doc.uri.fsPath)) return;
    const key = doc.uri.toString(); clearTimeout(xmlSaves.get(key));
    xmlSaves.set(key, setTimeout(() => { xmlSaves.delete(key); if (!doc.isClosed && doc.isDirty) void saveAutomatically(doc); }, 500));
  }), { dispose: () => { for (const timer of xmlSaves.values()) clearTimeout(timer); } });
  const nativeManualSaves = new Set<string>();
  context.subscriptions.push(vscode.workspace.onWillSaveTextDocument(event => {
    if (event.reason === vscode.TextDocumentSaveReason.Manual && !isAutomaticSave(event.document.uri)) nativeManualSaves.add(event.document.uri.toString());
  }), vscode.workspace.onDidSaveTextDocument(doc => {
    const explicit = nativeManualSaves.delete(doc.uri.toString());
    const project = projects.projectForUri(doc.uri);
    if (explicit && !isAutomaticSave(doc.uri) && project) void runtime.manualSaved(project, doc.uri);
  }));
  const initialized = projects.initialize().then(async () => { for (const project of projects.list()) await runtime.reconcile(project); });
  let state = sanitizeState(context.workspaceState.get<unknown>(PREVIEW_STATE_KEY));
  let ready = false;
  let pendingScenario: Scenario | undefined;
  let panelsCreated = 0;
  let messagesRejected = 0;
  let stateWrites = Promise.resolve();
  let panelListeners: vscode.Disposable[] = [];

  const save = (next: PreviewState): Promise<void> => {
    state = cloneState(next);
    stateWrites = stateWrites.then(() => context.workspaceState.update(PREVIEW_STATE_KEY, next));
    return stateWrites;
  };
  const postState = () => activePanel?.webview.postMessage({ type: 'state', state });
  const resetPreview = async () => {
    await save(createInitialState());
    if (!activePanel) await openPreview();
    else await postState();
  };
  const resetLayout = async () => {
    await save({ ...state, layout: { ...DEFAULT_LAYOUT }, zoom: 100 });
    if (!activePanel) await openPreview();
    else await postState();
  };

  const adoptPanel = (panel: vscode.WebviewPanel): void => {
    if (activePanel && activePanel !== panel) { panel.dispose(); activePanel.reveal(); return; }
    activePanel = panel;
    ready = false;
    panelsCreated++;
    panel.webview.options = {
      enableScripts: true,
      localResourceRoots: [vscode.Uri.joinPath(context.extensionUri, 'media'), vscode.Uri.joinPath(context.extensionUri, 'assets')],
    };
    panel.iconPath = vscode.Uri.joinPath(context.extensionUri, 'assets', 'zernova-mark.svg');
    panelListeners = [
      panel.onDidDispose(() => {
        if (activePanel === panel) activePanel = undefined;
        ready = false;
        for (const disposable of panelListeners.splice(0)) disposable.dispose();
      }),
      panel.webview.onDidReceiveMessage(async (unknownMessage: unknown) => {
        const message = parseWebviewMessage(unknownMessage);
        if (!message) { messagesRejected++; return; }
        if (message.type === 'ready') {
          ready = true;
          if (pendingScenario) {
            const scenario = pendingScenario;
            pendingScenario = undefined;
            await panel.webview.postMessage({ type: 'scenario', scenario });
          }
        } else if (message.type === 'saveState') await save(message.state);
        else if (message.type === 'resetPreview') await resetPreview();
        else if (message.type === 'resetLayout') await resetLayout();
      }),
    ];
    panel.webview.html = renderWebview(panel.webview, context.extensionUri, state);
  };

  async function openPreview(scenario?: Scenario): Promise<void> {
    if (scenario) pendingScenario = scenario;
    if (activePanel) {
      activePanel.reveal(vscode.ViewColumn.One);
      if (ready && pendingScenario) {
        const next = pendingScenario;
        pendingScenario = undefined;
        await activePanel.webview.postMessage({ type: 'scenario', scenario: next });
      }
      return;
    }
    adoptPanel(vscode.window.createWebviewPanel(VIEW_TYPE, 'ZERNOVA Studio · Design Preview', vscode.ViewColumn.One, { enableScripts: true, retainContextWhenHidden: true }));
    await vscode.commands.executeCommand('workbench.view.extension.zernovaKernelStudio');
  }

  registerNavigation(context, projects, runtime);
  const explicitUri = (candidate?: unknown): vscode.Uri | undefined => candidate instanceof vscode.Uri ? candidate
    : candidate && typeof candidate === 'object' && 'uri' in candidate && (candidate as { uri: unknown }).uri instanceof vscode.Uri ? (candidate as { uri: vscode.Uri }).uri
    : undefined;
  const commandUri = (candidate?: unknown): vscode.Uri | undefined => explicitUri(candidate) ?? vscode.window.activeTextEditor?.document.uri ?? documents.activeDocumentUri();
  const guarded = (callback: (...args: unknown[]) => Promise<unknown>) => async (...args: unknown[]) => {
    try { await initialized; return await callback(...args); }
    catch (error) { void vscode.window.showErrorMessage('ZERNOVA: ' + (error instanceof Error ? error.message : String(error))); return undefined; }
  };
  context.subscriptions.push(
    vscode.commands.registerCommand('zernovaKernelStudio.manualSave', guarded(async candidate => {
      const uri = commandUri(candidate); if (!uri) return;
      const doc = await vscode.workspace.openTextDocument(uri);
      if (!await saveAutomatically(doc)) throw new Error('The document could not be saved.');
      const project = projects.projectForUri(uri); if (project) await runtime.manualSaved(project, uri);
    })),
    vscode.commands.registerCommand('zernovaKernelStudio.newProject', guarded(async () => { const project = await projects.createProject(); if (project) { await vscode.commands.executeCommand('workbench.view.extension.zernovaKernelStudio'); const files = (await fs.readdir(path.join(project.root, 'src/main/zernova'))).filter(f => f.endsWith('.xml')); if (files[0]) await documents.open(vscode.Uri.file(path.join(project.root, 'src/main/zernova', files[0]))); } return project?.root; })),
    vscode.commands.registerCommand('zernovaKernelStudio.importLegacyProject', guarded(async () => { const project = await projects.importLegacyProject(); if (project) await vscode.commands.executeCommand('workbench.view.extension.zernovaKernelStudio'); return project?.root; })),
    vscode.commands.registerCommand('zernovaKernelStudio.openProject', guarded(async () => { const project = await projects.openProject(); if (project) { await runtime.reconcile(project); await vscode.commands.executeCommand('workbench.view.extension.zernovaKernelStudio'); } return project?.root; })),
    vscode.commands.registerCommand('zernovaKernelStudio.selectProject', guarded(async candidate => { const uri = explicitUri(candidate); const project = await projects.select(uri, !uri); if (project) { projects.refresh(); await runtime.reconcile(project); } })),
    vscode.commands.registerCommand('zernovaKernelStudio.newIntegration', guarded(async candidate => {
      const project = await projects.select(explicitUri(candidate) ?? (projects.current() ? vscode.Uri.file(projects.current()!.root) : commandUri())); if (!project) return;
      const uri = await projects.newIntegration(project); if (uri) await documents.open(uri); return uri?.toString();
    })),
    vscode.commands.registerCommand('zernovaKernelStudio.openIntegration', guarded(async candidate => {
      let uri = commandUri(candidate);
      if (!uri || !uri.fsPath.endsWith('.xml')) {
        const project = await projects.select(uri); if (!project) return;
        const files = (await fs.readdir(path.join(project.root, 'src/main/zernova'))).filter(name => name.endsWith('.xml'));
        const choice = await vscode.window.showQuickPick(files, { title: 'Open Integration in Studio', placeHolder: files.length ? 'Select an integration XML document' : 'No XML files exist. Use New Integration XML.' });
        if (!choice) return; uri = vscode.Uri.file(path.join(project.root, 'src/main/zernova', choice));
      }
      if (!projects.projectForUri(uri)) throw new Error('Open the containing ZERNOVA project first.');
      await documents.open(uri);
    })),
    vscode.commands.registerCommand('zernovaKernelStudio.toggleComponentBreakpoint', guarded(async (uri,id) => { if(!(uri instanceof vscode.Uri)||typeof id!=='string')throw new Error('Use a Studio component and its ID.'); const project=projects.projectForUri(uri);if(!project)throw new Error('Open a ZERNOVA project.');await runtime.toggleBreakpoint(project,uri,id); })),
    ...RUNTIME_ACTIONS.map(action => vscode.commands.registerCommand('zernovaKernelStudio.' + action, guarded(async candidate => {
      const project = await projects.select(commandUri(candidate)); if (project) { try { return await runtime.execute(action as RuntimeAction, project); } catch (error) { runtime.reportError(project, error); return runtime.state(project); } }
    }))),
  );
  context.subscriptions.push(
    vscode.commands.registerCommand('zernovaKernelStudio.openPreview', () => openPreview()),
    vscode.commands.registerCommand('zernovaKernelStudio.resetPreview', resetPreview),
    vscode.commands.registerCommand('zernovaKernelStudio.resetLayout', resetLayout),
    vscode.commands.registerCommand('zernovaKernelStudio.openScenario', async (candidate: unknown) => {
      let scenario = candidate;
      if (!isScenario(scenario)) {
        const selected = await vscode.window.showQuickPick([
          { label: '01 Workbench', scenario: 'workbench' }, { label: '02 HTTP Listener', scenario: 'listener' },
          { label: '03 Logger', scenario: 'logger' }, { label: '04 Global Elements', scenario: 'globals' },
          { label: '05 Configuration XML', scenario: 'xml' }, { label: '06 DataWeave layout', scenario: 'dataweave' },
          { label: 'Empty flow', scenario: 'empty' },
        ], { title: 'ZERNOVA Studio design preview', placeHolder: 'Choose a fixture view; no runtime is connected' });
        scenario = selected?.scenario;
      }
      if (isScenario(scenario)) await openPreview(scenario);
    }),
    vscode.window.onDidChangeActiveColorTheme(() => activePanel?.webview.postMessage({ type: 'hostTheme', kind: currentHostTheme() })),
    vscode.window.registerWebviewPanelSerializer(VIEW_TYPE, {
      deserializeWebviewPanel: async panel => { adoptPanel(panel); },
    }),
    { dispose: () => { activePanel?.dispose(); activePanel = undefined; } },
  );
  const status = vscode.window.createStatusBarItem('zernovaKernelStudio.status', vscode.StatusBarAlignment.Left, 20);
  status.name = 'ZERNOVA Studio';
  status.text = '$(beaker) ZERNOVA Studio';
  status.tooltip = 'Open the isolated Design Preview. Real projects are available in ZERNOVA Projects.';
  status.command = 'zernovaKernelStudio.openPreview';
  status.show();
  context.subscriptions.push(status);

  // Read-only diagnostics for the actual extension-host acceptance test.
  return Object.freeze({
    getPreviewDiagnostics: () => ({ extensionId: context.extension.id, version: context.extension.packageJSON.version as string, panelCount: activePanel ? 1 : 0, panelsCreated, ready, messagesRejected, state: cloneState(state), stateKey: PREVIEW_STATE_KEY }),
    whenStateSaved: () => stateWrites,
    whenInitialized: () => initialized,
    getProjectDiagnostics: () => ({ projects: projects.list().map(p => ({ ...p, configured: !!projects.getSettings(p) })), selected: projects.current()?.root, documents: documents.getDiagnostics() }),
    getRuntimeDiagnostics: () => projects.list().map(p => runtime.state(p)),
  });
}

export function deactivate(): void { activePanel?.dispose(); activePanel = undefined; }
