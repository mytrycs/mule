import * as vscode from 'vscode';
import { saveAutomatically } from './saves';
import { applyOperation, findConfigurationReferences, parseIntegration, XmlEditError, type XmlOperation } from '../shared/xml';
import { parseRealMessage, type RealDocumentSnapshot, type RuntimeAction, type ResourceAction, type TransformTarget } from '../shared/realProtocol';

export const REAL_EDITOR_TYPE = 'zernovaKernelStudio.integration';
export interface IntegrationEditorOptions {
  render: (webview: vscode.Webview, initial: RealDocumentSnapshot) => string;
  projectName: (uri: vscode.Uri) => string;
  projectRoot?: (uri: vscode.Uri) => vscode.Uri | undefined;
  manualSave?: (document: vscode.TextDocument) => Promise<void>;
  resourceAction?: (uri:vscode.Uri,action:ResourceAction,id?:string,target?:TransformTarget)=>Promise<void>;
  breakpoint?: (uri: vscode.Uri, id: string) => Promise<void>;
  runtimeState?: (uri: vscode.Uri) => unknown;
  runtimeAction?: (action: RuntimeAction, uri: vscode.Uri) => Promise<void>;
}
interface OpenEditor { document: vscode.TextDocument; panel: vscode.WebviewPanel; selectionId?: string; expectedChange?: { requestId: string; text: string }; queue: Promise<void>; disposed: boolean }

/** A CustomTextEditor owns no secondary file state: TextDocument is authoritative. */
export class IntegrationEditorProvider implements vscode.CustomTextEditorProvider, vscode.Disposable {
  private readonly editors = new Map<string, OpenEditor>();
  private readonly diagnostics = vscode.languages.createDiagnosticCollection('zernovaKernelStudio');
  private readonly activeEmitter = new vscode.EventEmitter<vscode.Uri | undefined>();
  readonly onDidChangeActiveDocument = this.activeEmitter.event;
  private activeUri: vscode.Uri | undefined;
  private readonly subscriptions: vscode.Disposable[];
  private rejectedMessages = 0;

  constructor(private readonly context: vscode.ExtensionContext, private readonly options: IntegrationEditorOptions) {
    this.subscriptions = [
      vscode.workspace.onDidChangeTextDocument(event => { const editor = this.editors.get(event.document.uri.toString()); if (editor) void this.postDocument(editor); }),
      vscode.workspace.onDidSaveTextDocument(document => { const editor = this.editors.get(document.uri.toString()); if (editor) void this.postDocument(editor); }),
      vscode.workspace.onDidCloseTextDocument(document => { if (!this.editors.has(document.uri.toString())) this.diagnostics.delete(document.uri); }),
      vscode.window.onDidChangeActiveColorTheme(() => { for (const editor of this.editors.values()) void editor.panel.webview.postMessage({ type: 'hostTheme', kind: this.hostTheme() }); }),
    ];
  }

  private hostTheme(): string {
    return ({ [vscode.ColorThemeKind.Light]: 'light', [vscode.ColorThemeKind.Dark]: 'dark', [vscode.ColorThemeKind.HighContrast]: 'highContrast', [vscode.ColorThemeKind.HighContrastLight]: 'highContrastLight' })[vscode.window.activeColorTheme.kind];
  }
  activeDocumentUri(): vscode.Uri | undefined { return this.activeUri; }
  async open(uri: vscode.Uri): Promise<void> { await vscode.commands.executeCommand('vscode.openWith', uri, REAL_EDITOR_TYPE); }
  getDiagnostics(): { documents: string[]; rejectedMessages: number } { return { documents: [...this.editors.keys()], rejectedMessages: this.rejectedMessages }; }

  private snapshot(editor: OpenEditor): RealDocumentSnapshot {
    const document = editor.document, text = document.getText();
    const model = parseIntegration(text);
    this.diagnostics.set(document.uri, model.diagnostics.map(problem => {
      const diagnostic = new vscode.Diagnostic(new vscode.Range(document.positionAt(problem.start), document.positionAt(problem.end)), problem.message,
        problem.severity === 'error' ? vscode.DiagnosticSeverity.Error : vscode.DiagnosticSeverity.Warning);
      diagnostic.source = 'ZERNOVA'; diagnostic.code = problem.code; return diagnostic;
    }));
    return { uri: document.uri.toString(), version: document.version, text, dirty: document.isDirty, projectName: this.options.projectName(document.uri), model,
      ...(editor.selectionId ? { selectionId: editor.selectionId } : {}),
      ...(editor.expectedChange?.text === text ? { changeRequestId: editor.expectedChange.requestId } : {}) };
  }
  private async postDocument(editor: OpenEditor): Promise<void> {
    if (!editor.disposed) await editor.panel.webview.postMessage({ type: 'realDocument', document: this.snapshot(editor) });
  }
  async refreshRuntime(uri?: vscode.Uri): Promise<void> {
    for (const editor of this.editors.values()) {
      if (uri && uri.toString() !== editor.document.uri.toString() && this.options.projectRoot?.(editor.document.uri)?.toString() !== uri.toString()) continue;
      await editor.panel.webview.postMessage({ type: 'runtimeState', state: this.options.runtimeState?.(editor.document.uri) ?? { status: 'not configured' } });
    }
  }

  async resolveCustomTextEditor(document: vscode.TextDocument, panel: vscode.WebviewPanel): Promise<void> {
    const key = document.uri.toString(), old = this.editors.get(key);
    if (old && !old.disposed) { panel.dispose(); old.panel.reveal(); return; }
    const editor: OpenEditor = { document, panel, queue: Promise.resolve(), disposed: false };
    this.editors.set(key, editor); this.activeUri = document.uri; this.activeEmitter.fire(document.uri);
    panel.webview.options = { enableScripts: true, localResourceRoots: [vscode.Uri.joinPath(this.context.extensionUri, 'media'), vscode.Uri.joinPath(this.context.extensionUri, 'assets')] };
    panel.iconPath = vscode.Uri.joinPath(this.context.extensionUri, 'assets', 'zernova-mark.svg');
    const subscriptions = [
      panel.onDidChangeViewState(event => { if (event.webviewPanel.active) { this.activeUri = document.uri; this.activeEmitter.fire(document.uri); } }),
      panel.webview.onDidReceiveMessage((input: unknown) => {
        const message = parseRealMessage(input);
        if (!message) {
          this.rejectedMessages++;
          // A bounded, well-formed request identity is safe to echo even when
          // the rest of a mutation is oversized/invalid. Otherwise the UI could
          // wait forever, unable to distinguish rejection from a runtime error.
          const rejected = input && typeof input === 'object' ? input as Record<string, unknown> : undefined;
          const requestId = rejected && (rejected.type === 'realEdit' || rejected.type === 'realXml') && typeof rejected.requestId === 'string' && /^[A-Za-z0-9_-]{1,100}$/.test(rejected.requestId) ? rejected.requestId : undefined;
          void panel.webview.postMessage({ type: 'realError', message: 'Unsupported or oversized editor request was rejected.', ...(requestId ? { requestId } : {}) }); return;
        }
        if(message.type==='realResource'){void editor.queue.then(()=>this.options.resourceAction?.(document.uri,message.action,message.id,message.target)).then(()=>this.postDocument(editor)).catch(error=>panel.webview.postMessage({type:'realError',message:String(error)}));return;}
        if (message.type === 'realBreakpoint') { void this.options.breakpoint?.(document.uri,message.id).catch(error => panel.webview.postMessage({type:'realError',message:String(error)})); return; }
        // Cancellation must not wait behind the long-running build/run it cancels.
        if (message.type === 'realAction' && message.action === 'cancel') {
          void Promise.resolve(this.options.runtimeAction?.('cancel', document.uri)).catch(error => panel.webview.postMessage({ type: 'realError', message: String(error) }));
          return;
        }
        if (message.type === 'realAction' && !['save', 'undo', 'redo'].includes(message.action)) {
          // Wait for preceding document edits, without making later edits wait
          // behind a long build/deploy. The adapter rechecks dirty/version state.
          void editor.queue.then(async () => {
            if (!this.options.runtimeAction) throw new Error('Runtime integration is not available.');
            await this.options.runtimeAction(message.action as RuntimeAction, document.uri);
            await this.refreshRuntime(document.uri);
          }).catch(error => panel.webview.postMessage({ type: 'realError', message: error instanceof Error ? error.message : String(error) }));
          return;
        }
        editor.queue = editor.queue.then(async () => {
          if (editor.disposed) return;
          try {
            if (message.type === 'ready') { await this.postDocument(editor); await this.refreshRuntime(document.uri); return; }
            if (message.type === 'realAction') {
              if (message.action === 'save') { if (this.options.manualSave) await this.options.manualSave(document); else if (!await saveAutomatically(document)) throw new Error('The document could not be saved. Check permissions and try again.'); await this.postDocument(editor); }
              else if (message.action === 'undo' || message.action === 'redo') {
                if (!panel.active) throw new Error('Focus this integration editor before using Undo or Redo.');
                await vscode.commands.executeCommand(message.action); await this.postDocument(editor);
              } else {
                if (!this.options.runtimeAction) throw new Error('Runtime integration is not available.');
                await this.options.runtimeAction(message.action, document.uri); await this.refreshRuntime(document.uri);
              }
              return;
            }
            if (document.version !== message.version) throw new Error('The document changed outside this request. Your draft was not applied. Review the current XML and reapply the edit.');
            const before = document.getText();
            let next = before, selectionId: string | undefined;
            let edits: { start: number; end: number; text: string }[];
            if (message.type === 'realEdit') {
              await this.guardProjectReferences(document, message.operation);
              if (document.version !== message.version || document.getText() !== before) throw new Error('The document changed while references were checked. Refresh and try the edit again.');
              const result = applyOperation(before, message.operation);
              next = result.text; selectionId = result.selectionId; edits = result.edits;
            } else edits = [{ start: 0, end: before.length, text: message.text }], next = message.text;
            if (document.version !== message.version) throw new Error('A newer document version exists. The stale edit was not applied.');
            editor.selectionId = selectionId;
            // A request being active is not evidence that a subsequent native
            // document change came from it. Tag only the exact expected text.
            editor.expectedChange = { requestId: message.requestId, text: next };
            if (next !== before) {
              const edit = new vscode.WorkspaceEdit();
              edit.set(document.uri, edits.map(change => vscode.TextEdit.replace(new vscode.Range(document.positionAt(change.start), document.positionAt(change.end)), change.text)));
              if (!await vscode.workspace.applyEdit(edit)) throw new Error('VS Code refused the edit because the document changed or is read-only.');
            }
            await this.postDocument(editor);
            if (document.isDirty && !await saveAutomatically(document)) throw new Error('Autosave failed. Check file permissions.');
            await this.postDocument(editor);
            await panel.webview.postMessage({ type: 'realAck', requestId: message.requestId, version: document.version, ...(selectionId ? { selectionId } : {}) });
          } catch (error) {
            editor.expectedChange = undefined;
            const text = error instanceof Error ? error.message : String(error);
            await panel.webview.postMessage({ type: 'realError', message: text, ...('requestId' in message ? { requestId: message.requestId } : {}) });
            await this.postDocument(editor);
          } finally { editor.expectedChange = undefined; }
        }).catch(error => { void panel.webview.postMessage({ type: 'realError', message: 'Editor request failed: ' + String(error) }); });
      }),
      panel.onDidDispose(() => {
        editor.disposed = true; this.editors.delete(key);
        if (this.activeUri?.toString() === key) { this.activeUri = undefined; this.activeEmitter.fire(undefined); }
        for (const disposable of subscriptions) disposable.dispose();
      }),
    ];
    panel.webview.html = this.options.render(panel.webview, this.snapshot(editor));
  }

  private async guardProjectReferences(document: vscode.TextDocument, operation: XmlOperation): Promise<void> {
    if (operation.kind !== 'deleteConfig' && operation.kind !== 'saveConfig') return;
    if (operation.kind === 'saveConfig' && !operation.id) return;
    const model = parseIntegration(document.getText()), old = model.configs.find(config => config.id === operation.id);
    if (!old || operation.kind === 'saveConfig' && old.name === operation.values.name) return;
    const project = this.options.projectRoot?.(document.uri);
    if (!project) throw new XmlEditError('Open this integration as a ZERNOVA project before renaming or deleting a shared configuration.');
    const files = await vscode.workspace.findFiles(new vscode.RelativePattern(project, 'src/main/zernova/**/*.xml'), '**/target/**', 201);
    if (files.length > 200) throw new XmlEditError('Project has more than 200 integration documents. Automatic shared-configuration rename/delete is disabled.');
    for (const uri of files) {
      if (uri.toString() === document.uri.toString()) continue;
      const other = await vscode.workspace.openTextDocument(uri), references = findConfigurationReferences(other.getText(), old.name);
      if (!references.valid) throw new XmlEditError('Repair invalid XML in ' + vscode.workspace.asRelativePath(uri) + ' before renaming or deleting a shared configuration.');
      if (references.references) throw new XmlEditError('Configuration is referenced by ' + vscode.workspace.asRelativePath(uri) + '. Cross-file rename/delete is intentionally refused; review and update those references explicitly.');
    }
  }

  dispose(): void {
    for (const editor of this.editors.values()) editor.panel.dispose();
    this.editors.clear();
    for (const disposable of this.subscriptions) disposable.dispose();
    this.diagnostics.dispose(); this.activeEmitter.dispose();
  }
}
