import type {StudioFormView} from '../shared/studioForms';
import { CORE_CATALOG, coreDefinition, acceptsChild } from '../shared/coreCatalog';
import { processorTree, type RealComponent, type RealCore } from '../shared/xml';
import { componentLocation } from '../shared/breakpoints';
import { insertionAt, moveToInsertion } from '../shared/placement';
import { layoutFlows } from './flowLayout';
/** Real-document presentation. All file edits and runtime operations are host-owned. */
import { icon } from './icons';
import { DEFAULT_LAYOUT, HTTP_METHODS, LOG_LEVELS } from '../shared/model';
import type { RealDocumentSnapshot } from '../shared/realProtocol';
import type { XmlOperation, RealFlow, RealListener, RealLogger, RealTransform, RealHttpConfig } from '../shared/xml';

type VSCodeBridge = { postMessage(message: unknown): void; setState(state: unknown): void; getState(): unknown };
type Component = RealComponent;
type EditTab = 'flow' | 'globals' | 'xml';
type LowerTab = 'properties' | 'problems' | 'console' | 'outline';
type RuntimeAction = 'debugContinue' | 'debugStep' | 'debugDisconnect' | 'debug' | 'configureRun' | 'configureDebug' | 'configureRuntime' | 'diagnostics' | 'build' | 'run' | 'status' | 'logs' | 'stopApplication' | 'stopRuntime' | 'cancel';
type RuntimeState = { debugOutput?:string; debugConnected?:boolean; debugPaused?:string; debugBreakpoints?:{uri:string;id:string;path:string}[]; projectName?: string; configured?: boolean; phase?: string; busy?: boolean; process?: string; application?: string | Record<string, unknown>; endpoint?: string; lastError?: string; buildOutput?: string; applicationLogs?: string; [key: string]: unknown };
type ViewState = { flowId: string | null; selectedId: string | null; configId: string | null; tab: EditTab; lower: LowerTab; section: string; consoleTab: 'deployment' | 'build' | 'logs' | 'runtime' | 'status' | 'debug'; theme: 'host' | 'light' | 'dark'; zoom: number; paletteWidth: number; propertiesHeight: number };
type Pending = { requestId: string; version: number; text?: string; operation?: XmlOperation; draftKey?: string; draftValue?: string };
const escape = (value: unknown): string => String(value ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]!));
const object = (v: unknown): v is Record<string, unknown> => !!v && typeof v === 'object' && !Array.isArray(v);

export function mountReal(root: HTMLElement, vscode: VSCodeBridge): void {
  let document: RealDocumentSnapshot | undefined;
  let runtime: RuntimeState = { phase: 'Not configured', configured: false };
  const saved = vscode.getState();
  const restored = object(saved) && saved.mode === 'real' && object(saved.view) ? saved.view : {};
  const view: ViewState = {
    flowId: typeof restored.flowId === 'string' ? restored.flowId : null,
    selectedId: typeof restored.selectedId === 'string' ? restored.selectedId : null,
    configId: typeof restored.configId === 'string' ? restored.configId : null,
    tab: ['flow', 'globals', 'xml'].includes(String(restored.tab)) ? restored.tab as EditTab : 'flow',
    lower: ['properties', 'problems', 'console', 'outline'].includes(String(restored.lower)) ? restored.lower as LowerTab : 'properties',
    section: typeof restored.section === 'string' ? restored.section : 'General',
    consoleTab: 'build', theme: ['host', 'light', 'dark'].includes(String(restored.theme)) ? restored.theme as ViewState['theme'] : 'host',
    zoom: 100, paletteWidth: DEFAULT_LAYOUT.paletteWidth, propertiesHeight: DEFAULT_LAYOUT.propertiesHeight,
  };
  let query = '';
  let globalQuery = '';
  let toast = '';
  let toastError = false;
  let toastTimer = 0;
  let nextRequest = 0;
  let pending: Pending | undefined;
  const queue: Array<{ operation: XmlOperation; draftKey?: string; draftValue?: string }> = [];
  const drafts = new Map<string, string>();
  let xmlDraft: string | undefined = object(saved) && saved.mode === 'real' && typeof saved.xmlDraft === 'string' ? saved.xmlDraft : undefined;
  let xmlConflict = false;
  let fieldRecovery = object(saved) && saved.mode === 'real' && typeof saved.fieldRecovery === 'string' ? saved.fieldRecovery.slice(0, 131072) : '';
  let requestedAction: string | undefined;
  let configDialog: { id?: string; baseVersion: number; values: { name: string; protocol: 'HTTP'; host: string; port: string; basePath: string }; error: string } | undefined;
  let flowDialog: { id?: string; baseVersion: number; name: string; error: string; flowKind?:'flow'|'sub-flow' } | undefined;
  let deleteDialog: { id: string; baseVersion: number; name: string; error: string; flowKind?:'flow'|'sub-flow' } | undefined;
  let hostForm:StudioFormView|undefined;
  let returnFocus = '';
  const collapsedFlows = new Set<string>(); const openErrors = new Set<string>();
  let selectedPalette: string | undefined;
  let showLogsAfterRun = false;
  let contextMenu: { id: string; kind?: 'component' | 'canvas' | 'config' | 'run' | 'debug'; x: number; y: number } | undefined;
  let drag: { kind?: string; id?: string } | undefined;
  let pointerPlacement: { item: { kind?: string; id?: string }; element: HTMLElement; startX: number; startY: number; active: boolean } | undefined;
  let suppressPlacementClick = false;
  let resize: { key: 'paletteWidth' | 'propertiesHeight'; start: number; value: number } | undefined;
  let applyingFromHost = false;

  const suggestedFlowName=(kind='flow')=>{const base=kind==='sub-flow'?'newSubflow':'newFlow';let name=base,index=2;while(document?.model.flows.some(f=>f.name===name))name=base+index++;return name;};
  const flow = (): RealFlow | undefined => document?.model.flows.find(f => f.id === view.flowId);
  const component = (): Component | undefined => flow()?.source?.id===view.selectedId?flow()?.source??undefined:flow()?processorTree(flow()!).find(c=>c.id===view.selectedId):undefined;
  const stateText = (value: unknown): string => typeof value === 'string' ? value : object(value) ? String(value.state ?? value.deployment ?? 'Unknown') : 'Unknown';
  const display = (value: unknown): string => typeof value === 'string' ? value : JSON.stringify(value, null, 2);
  const canEdit = (): boolean => !!document?.model.valid && !xmlConflict && !fieldRecovery && xmlDraft === undefined;
  const persist = (): void => { vscode.setState({ mode: 'real', uri: document?.uri, view, xmlDraft, xmlConflict, fieldRecovery }); };
  const button = (action: string, text: string, glyph?: string, disabled = false, cls = '', attrs = ''): string => `<button type="button" data-action="${action}" data-focus="${action}" ${disabled ? 'disabled' : ''} class="${cls}" ${attrs}>${glyph ? icon(glyph) : ''}${escape(text)}</button>`;
  const iconButton = (action: string, text: string, glyph: string, disabled = false): string => button(action, '', glyph, disabled, 'icon-button', `aria-label="${escape(text)}" title="${escape(text)}"`);
  const split = (key: 'paletteWidth' | 'propertiesHeight', orientation: 'vertical' | 'horizontal', text: string): string => `<div class="resize-${orientation === 'vertical' ? 'v' : 'h'}" data-resize="${key}" role="separator" tabindex="0" aria-orientation="${orientation}" aria-label="${text}" aria-valuenow="${view[key]}" data-focus="split-${key}"></div>`;
  function announce(text: string, error = false): void {
    toast = text; toastError = error; window.clearTimeout(toastTimer); render();
    toastTimer = window.setTimeout(() => { toast = ''; render(); }, 7000);
  }
  function sendNext(): void {
    persist();
    if (!document || pending || xmlConflict || fieldRecovery) return;
    if (xmlDraft !== undefined && xmlDraft !== document.text) {
      pending = { requestId: `edit-${++nextRequest}`, version: document.version, text: xmlDraft };
      vscode.postMessage({ type: 'realXml', requestId: pending.requestId, version: pending.version, text: pending.text });
      return;
    }
    if (xmlDraft === document.text) xmlDraft = undefined;
    const next = queue.shift();
    if (next) {
      pending = { ...next, requestId: `edit-${++nextRequest}`, version: document.version };
      vscode.postMessage({ type: 'realEdit', requestId: pending.requestId, version: pending.version, operation: next.operation });
    } else if (requestedAction) {
      const action = requestedAction; requestedAction = undefined;
      vscode.postMessage({ type: 'realAction', action });
    }
  }
  function edit(operation: XmlOperation, draftKey?: string, draftValue?: string): void {
    if (!canEdit()) { announce('Resolve invalid XML or the retained XML draft before visual editing.', true); return; }
    if (draftKey) {
      const existing = queue.findIndex(x => x.draftKey === draftKey);
      if (existing >= 0) queue.splice(existing, 1);
    }
    queue.push({ operation, draftKey, draftValue }); sendNext();
  }
  function action(name: string): void {
    // Inspection and lifecycle cleanup must remain available while an editor
    // draft is conflicted. They do not commit or execute the draft.
    contextMenu = undefined;
    if (['configureRun', 'configureDebug', 'cancel', 'stopApplication', 'stopRuntime', 'status', 'logs', 'diagnostics', 'configureRuntime'].includes(name)) {
      if (name === 'logs') { view.lower = 'console'; view.consoleTab = 'logs'; }
      if (name === 'diagnostics' || name === 'status') { view.lower = 'console'; view.consoleTab = 'status'; }
      vscode.postMessage({ type: 'realAction', action: name }); render(); return;
    }
    if (xmlConflict || fieldRecovery) { announce('Review the retained draft and current document before saving or running.', true); return; }
    if (name === 'run' || name === 'debug') showLogsAfterRun = true;
    if (name === 'build' || name === 'run' || name === 'debug') { view.lower = 'console'; view.consoleTab = 'build'; }
    if (name === 'logs') { view.lower = 'console'; view.consoleTab = 'logs'; }
    if (name === 'diagnostics' || name === 'status') { view.lower = 'console'; view.consoleTab = 'status'; }
    requestedAction = name; sendNext(); render();
  }
  function retainFieldDrafts(): void {
    if (drafts.size || queue.length) fieldRecovery = JSON.stringify({
      message: 'These attempted edits were not confirmed. The editor now shows the authoritative document. Review before reapplying.',
      fields: Object.fromEntries(drafts), queuedOperations: queue.map(item => item.operation),
    }, null, 2);
    drafts.clear(); queue.length = 0; requestedAction = undefined;
  }
  function dialogIsCurrent(dialog: { baseVersion: number; error: string }): boolean {
    if (document?.version === dialog.baseVersion && !pending && !queue.length) return true;
    dialog.error = 'The document changed while this dialog was open. Your draft is retained here. Close and reopen the dialog to review the current values before applying changes.';
    render(); return false;
  }
  function focusSnapshot(): { key: string; start: number | null; end: number | null } | undefined {
    const el = window.document.activeElement as HTMLElement | null;
    if (!el?.dataset.focus) return;
    return { key: el.dataset.focus, start: el instanceof HTMLInputElement || el instanceof HTMLTextAreaElement ? el.selectionStart : null, end: el instanceof HTMLInputElement || el instanceof HTMLTextAreaElement ? el.selectionEnd : null };
  }
  function restoreFocus(savedFocus: ReturnType<typeof focusSnapshot>): void {
    if (!savedFocus) return;
    const el = root.querySelector<HTMLElement>(`[data-focus="${CSS.escape(savedFocus.key)}"]`);
    el?.focus({ preventScroll: true });
    if ((el instanceof HTMLInputElement || el instanceof HTMLTextAreaElement) && savedFocus.start !== null) {
      try { el.setSelectionRange(savedFocus.start, savedFocus.end); } catch { /* number inputs */ }
    }
  }
  function render(): void {
    if (pointerPlacement?.active) return;
    const focus = focusSnapshot();
    const oldLogs = root.querySelector<HTMLElement>('[data-scroll="console-logs"]');
    const followLogTail = !oldLogs || oldLogs.scrollHeight - oldLogs.clientHeight - oldLogs.scrollTop < 40;
    const scrolls = [...root.querySelectorAll<HTMLElement>('[data-scroll]')].map(el => [el.dataset.scroll!, el.scrollTop, el.scrollLeft] as const);
    window.document.body.dataset.previewTheme = view.theme;
    window.document.documentElement.style.setProperty('--logo-url', `url("${window.__ZERNOVA_ASSETS__.logo.replace(/"/g, '%22')}")`);
    root.innerHTML = `<main class="studio real-studio" aria-label="ZERNOVA Studio real project" style="--palette-width:${view.paletteWidth}px;--properties-height:${view.propertiesHeight}px">
      ${renderBar()}${fieldRecovery ? `<div class="real-conflict" role="alert">A document change interrupted queued edits. Current document values are shown; attempted edits are retained for copying.${button('copyFieldRecovery', 'Copy retained edits')}${button('discardFieldRecovery', 'Review current document')}<details><summary>Retained edits</summary><pre tabindex="0">${escape(fieldRecovery)}</pre></details></div>` : ''}<div class="workspace"><div class="upper-workarea"><section class="main-editor" aria-label="Integration editor"><div class="editor-content" data-scroll="editor">${view.tab === 'xml' ? renderXml() : view.tab === 'globals' ? renderGlobals() : renderFlow()}</div>${renderTabs()}</section>${split('paletteWidth', 'vertical', 'Resize palette')}${renderPalette()}</div>${split('propertiesHeight', 'horizontal', 'Resize properties area')}${renderLower()}</div>
      ${renderDialog()}${contextMenu ? renderMenu() : ''}<div class="sr-only" role="status" aria-live="polite">${escape(toast)}</div>${toast ? `<div class="toast${toastError ? ' error' : ''}" data-testid="toast">${icon(toastError ? 'warning' : 'info')}<span>${escape(toast)}</span></div>` : ''}</main>`;
    for (const [key, top, left] of scrolls) { const el = root.querySelector<HTMLElement>(`[data-scroll="${CSS.escape(key)}"]`); if (el) { el.scrollTop = top; el.scrollLeft = left; } }
    layoutFlows(root);
    if (view.lower === 'console' && view.consoleTab === 'logs' && followLogTail) { const logs = root.querySelector<HTMLElement>('[data-scroll="console-logs"]'); if (logs) logs.scrollTop = logs.scrollHeight; }
    restoreFocus(focus);
    if (hostForm || configDialog || flowDialog || deleteDialog) {
      root.querySelector('.workspace')?.setAttribute('inert', ''); root.querySelector('.preview-bar')?.setAttribute('inert', '');
    }
  }
  function renderBar(): string {
    const busy = !!runtime.busy;
    return `<header class="preview-bar"><span class="brand"><span class="brand-mark" aria-hidden="true"></span>ZERNOVA</span><span class="connection" data-testid="runtime-phase">${icon(runtime.lastError ? 'warning' : 'info')}${escape(runtime.phase || 'Not configured')}</span><span class="bar-spacer"></span>
      <div class="launch-control" role="group" aria-label="Run controls">${button('runtime-run','Run / Deploy','play',busy,'primary')}${button('runMenu','', 'down',busy,'launch-caret',`aria-label="Run options" aria-haspopup="menu" aria-expanded="${contextMenu?.kind === 'run'}"`)}</div><div class="launch-control" role="group" aria-label="Debug controls">${iconButton('runtime-debug','Debug application (component breakpoints)','debug',busy)}${button('debugMenu','', 'down',busy,'launch-caret',`aria-label="Debug options" aria-haspopup="menu" aria-expanded="${contextMenu?.kind === 'debug'}"`)}</div>${iconButton('runtime-logs', 'Application logs', 'logger', busy)}${iconButton('runtime-stopRuntime', 'Stop runtime', 'stop', busy)}${button('runtime-diagnostics', 'Diagnostics', undefined, busy)}
      <select class="compact-select theme-select" aria-label="Editor theme" data-control="theme" data-focus="theme">${[['host', 'Host theme'], ['light', 'Light'], ['dark', 'Dark']].map(([id, name]) => `<option value="${id}" ${view.theme === id ? 'selected' : ''}>${name}</option>`).join('')}</select></header>`;
  }
  function renderTabs(): string {
    return `<nav class="editor-tabs" role="tablist" aria-label="Integration views">${[['flow', 'Message Flow'], ['globals', 'Global Elements'], ['xml', 'Configuration XML']].map(([id, name]) => `<button class="tab${view.tab === id ? ' active' : ''}" role="tab" aria-selected="${view.tab === id}" data-action="tab-${id}" data-focus="tab-${id}">${name}</button>`).join('')}</nav>`;
  }
  function toolbar(): string {
    const files = document?.uri.split('/');
    return `<div class="canvas-toolbar real-document-toolbar"><span class="hint" title="${escape(document?.uri)}">${icon('code')} ${escape(files?.at(-1) ?? 'Loading document…')}${document?.dirty ? ' • Unsaved' : ''}</span>${button('save', 'Save', undefined, !document)}${iconButton('undo', 'Undo document edit', 'undo', !document)}${iconButton('redo', 'Redo document edit', 'redo', !document)}${button('newFlow', 'New flow', 'plus', !canEdit())}<span class="toolbar-separator"></span>${iconButton('zoomOut', 'Zoom out', 'minus', view.zoom <= 60)}<span class="zoom-value">${view.zoom}%</span>${iconButton('zoomIn', 'Zoom in', 'plus', view.zoom >= 160)}</div>`;
  }
  const componentName = (node: Component) => node.kind === 'listener' ? 'HTTP Listener' : node.kind==='scheduler'?'Scheduler':node.kind==='core'?coreDefinition(node.coreType)?.name??node.coreType:node.kind === 'transform' ? 'Transform Message' : 'Logger';
  const componentIcon = (node: Component) => node.kind === 'listener' ? 'globe' : node.kind==='scheduler'?'clock':node.kind==='core'?node.coreType:node.kind === 'transform' ? 'transform' : 'logger';
  const collapsedScopes=new Set<string>();
  function renderNode(node: Component,owner:RealFlow): string {
    if(node.kind==='core'&&coreDefinition(node.coreType)?.children){
      const last=owner.processors.at(-1)?.id===node.id;
      return `<section class="processor-scope" data-flow-component data-scope="${escape(node.id)}"><header>${button('toggleScope',collapsedScopes.has(node.id)?'▸':'▾',undefined,false,'icon-button',`data-scope="${escape(node.id)}" aria-label="${collapsedScopes.has(node.id)?'Expand':'Collapse'} ${escape(node.displayName)}"`)}${renderSimpleNode(node,false,owner)}</header>${collapsedScopes.has(node.id)?'':`<div class="scope-children" data-drop="processor" data-parent="${escape(node.id)}" role="group" aria-label="${escape(node.displayName)} children">${node.children.map((child,index)=>`<span data-processor-index="${index}">${renderNode(child,owner)}</span>`).join('')}${node.children.length?'':'<span class="empty-scope-hint">Place a valid component here</span>'}</div>`}${last?'<span class="node-port response-port" data-anchor="response-origin" aria-label="Response output from final scope">'+icon('arrowDown')+'</span>':''}</section>`;
    }
    return renderSimpleNode(node,true,owner);
  }
  function renderSimpleNode(node:Component,portsEnabled:boolean,owner:RealFlow):string {
    const isBreakpoint=runtime.debugBreakpoints?.some(b=>b.uri===document?.uri&&b.id===node.id);
    const location=document?componentLocation(document.model,node.id):undefined;
    const paused=runtime.debugPaused===location && (node.kind!=='listener'||isBreakpoint);
    const last = portsEnabled&&owner.processors.at(-1)?.id === node.id;
    const ports = node.kind === 'listener' ? `<span class="node-port request-port" data-anchor="request" role="img" aria-label="Request into flow" title="Request into flow">${icon('globe')}</span><span class="node-port response-port" data-anchor="response" role="img" aria-label="Response to caller" title="Response to caller">${icon('sourceResponse')}</span>` : last ? `<span class="node-port response-port" data-anchor="response-origin" role="img" aria-label="Response output from last processor" title="Response output from last processor">${icon('arrowDown')}</span>` : '';
    return `<button class="node ${portsEnabled?'':'scope-heading-node '}${node.kind}-node${isBreakpoint?' has-breakpoint':''}${paused?' debug-paused':''}${view.selectedId === node.id ? ' selected' : ''}" data-flow-component data-node="${escape(node.id)}" data-action="select" draggable="false" data-focus="node-${escape(node.id)}" aria-label="${componentName(node)}: ${escape(node.displayName)}" aria-pressed="${view.selectedId === node.id}" title="Select to edit ${escape(node.displayName)}. Shift+F10 for actions.">${node.kind === 'listener' ? '' : `<span class="node-symbol ${node.kind}">${icon(componentIcon(node))}</span>`}<span class="node-label">${escape(node.displayName || componentName(node))}</span>${ports}${isBreakpoint?'<span class="breakpoint-dot" role="img" aria-label="Breakpoint enabled"></span>':''}${paused?'<span class="debug-paused-label">Paused</span>':''}</button>`;
  }
  function validPlacement(kind: string, lane: string, parentId?:string,flowId?:string): boolean {
    const f=flowId?document?.model.flows.find(f=>f.id===flowId):flow();if(!f?.editable||!canEdit())return false;
    if(kind==='listener'||kind==='scheduler')return lane==='source'&&!f.source&&f.kind!=='sub-flow';
    const parent=parentId?processorTree(f).find(p=>p.id===parentId):undefined;
    return (lane==='processor'||lane==='error')&&(parent?.kind==='core'?acceptsChild(parent.coreType,kind):lane==='error'?!!f.errorHandler?.editable:acceptsChild('flow',kind));
  }
  function place(kind: string, target: { lane: 'source' | 'processor' | 'error'; index: number; parentId?:string }): void {
    const f = flow(); if (!f || !validPlacement(kind,target.lane,target.parentId)) { announce('Choose a valid ' + (kind === 'listener' ? 'empty Source' : 'Processor or Error Handler') + ' location.', true); return; }
    selectedPalette = undefined;
    edit({ kind: 'insert', flowId: f.id, component: kind, ...(target.parentId?{parentId:target.parentId}:{}), ...(kind !== 'listener'&&kind!=='scheduler' ? { index: target.index, ...(target.lane === 'error' ? { lane: 'error' as const } : {}) } : {}) });
  }
  function renderFlow(): string {
    const f = flow();
    if (!document) return '<div class="empty-properties"><h2>Opening integration document…</h2></div>';
    if (!document.model.valid) return `${toolbar()}<div class="empty-properties">${icon('warning')}<h2>XML needs attention</h2><p>The source is preserved. Open Configuration XML to fix it; visual editing is paused while XML is invalid.</p>${button('tab-xml', 'Open Configuration XML')}</div>`;
    if (!f) return `${toolbar()}<div class="empty-properties">${icon('code')}<h2>Create your first flow</h2><p>This integration XML is empty. Create a flow, then add an HTTP Listener and Logger.</p>${button('newFlow', 'Create flow', 'plus', false, 'primary')}</div>`;
    return `${toolbar()}<div class="flow-scroll" data-scroll="canvas" role="region" aria-label="All flows in this XML">${document.model.flows.map(f=>renderFlowCard(f)).join('')}</div>`;
  }
  function renderFlowCard(f:RealFlow):string{
    return `<div data-flow-id="${escape(f.id)}" class="flow-card${f.kind === 'sub-flow' ? ' subflow-card' : ''}" style="zoom:${view.zoom / 100};min-width:${Math.max(f.kind==='sub-flow'?210:375,f.name.length*7+95)}px" role="group" aria-label="${escape(f.name)} integration flow"><div class="flow-heading">${button('toggleFlow', collapsedFlows.has(f.id) ? '▸' : '▾', undefined, false, 'icon-button', `aria-label="${collapsedFlows.has(f.id) ? 'Expand' : 'Collapse'} flow ${escape(f.name)}" aria-expanded="${!collapsedFlows.has(f.id)}"`)}${icon(f.kind==='sub-flow'?'sub-flow':'flow')}<span class="flow-name">${escape(f.name)}</span>${f.editable?'':'<span class="fixture-tag">Read only</span>'}${iconButton('renameFlow', 'Rename flow', 'edit', !f.editable || !canEdit())}</div>
      ${!collapsedFlows.has(f.id) ? `${!f.editable ? `<div class="real-unsupported"><strong>Unsupported constructs are preserved.</strong><p>${escape(f.unsupported.join(', '))}</p><p>Use Configuration XML. Visual changes to this flow are disabled.</p></div>` : ''}
      <div class="flow-lanes"><div class="source-lane${f.kind === 'sub-flow' ? ' subflow-source' : ''}" data-drop="source" role="group" aria-label="Source lane"><span class="lane-caption">Source</span>${f.source ? renderNode(f.source,f) : f.kind === 'sub-flow' ? '' : ''}${f.source?.kind==='listener'?'<span class="response-caption">Response</span>':''}</div><div class="processor-lane" data-drop="processor" role="group" aria-label="Processor lane">${f.processors.map((node, index) => `<span class="connector-arrow${index === 0 && f.source ? ' source-request-connector' : ''}" aria-hidden="true"></span><span data-processor-index="${index}">${renderNode(node,f)}</span>`).join('')}</div>${f.editable && f.source && f.processors.length ? '<svg class="request-path" aria-hidden="true"><path/></svg>'+(f.source.kind==='listener'?'<svg class="response-path" aria-hidden="true"><path/></svg>':'') : ''}</div>
      <section class="error-section"><button class="error-toggle" data-action="toggleError" aria-expanded="${openErrors.has(f.id)}">${openErrors.has(f.id) ? '▾' : '▸'} Error handler</button>${openErrors.has(f.id) ? `<div class="error-region">${f.errorHandler ? `<strong>${escape(f.errorHandler.type)}</strong>${f.errorHandler.editable ? `<div class="error-processors" data-drop="error">${f.errorHandler.component?renderNode(f.errorHandler.component,f):f.errorHandler.processors.map((n,i) => `${renderNode(n,f)}`).join('')}</div>` : `<p>${escape(f.errorHandler.summary)}</p>${button('tab-xml', 'Open handler XML', 'code')}`}` : `<p>No local error handler. Runtime default error propagation applies.</p>${button('addErrorHandler', 'Add On Error Propagate + Logger', 'plus', !f.editable || f.kind === 'sub-flow' || !canEdit())}`}</div>` : ''}</section>` : ''}</div>`.replace(/data-focus="([^"]+)"/g,(_,key:string)=>`data-focus="${escape(f.id)}:${key}"`);
  }

  function renderPalette(): string {
    const f = flow();
    const items = [{kind:'flow',group:'Scopes',name:'Flow',glyph:'flow'},{kind:'sub-flow',group:'Scopes',name:'Subflow',glyph:'sub-flow'},{kind:'scheduler',group:'Sources',name:'Scheduler',glyph:'clock'},...CORE_CATALOG.map(d=>({kind:d.key,group:d.group,name:d.name,glyph:d.key})),{ kind: 'listener', group: 'HTTP', name: 'Listener', glyph: 'globe' }, { kind: 'request', group: 'HTTP', name: 'Request', glyph: 'request' }, { kind: 'logger', group: 'Core', name: 'Logger', glyph: 'logger' }, { kind:'transform', group:'Core', name:'Transform Message', glyph:'transform' }];
    return `<aside class="palette" aria-label="ZERNOVA Palette"><div class="pane-title">ZERNOVA Palette</div><div class="search-wrap">${icon('search')}<input type="search" data-control="palette-search" data-focus="palette-search" placeholder="Search components…" aria-label="Search components" value="${escape(query)}"></div><div class="palette-content" data-scroll="palette">${[...new Set(items.map(x=>x.group))].map(group => `<section class="palette-group" aria-label="${group} components"><div class="group-title">${group}</div>${items.filter(x => x.group === group && `${x.group} ${x.name}`.toLowerCase().includes(query.toLowerCase())).map(x => `<button class="palette-item${selectedPalette === x.kind ? ' selected' : ''}" aria-pressed="${selectedPalette === x.kind}" data-palette="${x.kind}" data-action="insert-${x.kind}" data-focus="palette-${x.kind}" ${x.kind === 'request' || !canEdit() || (!['flow','sub-flow'].includes(x.kind)&&!f?.editable) ? 'disabled' : 'draggable="false"'} title="${x.kind === 'request' ? 'HTTP Request is outside this milestone' : 'Select, then click a valid empty flow location'}"><span class="node-symbol ${x.kind}">${icon(x.glyph)}</span>${x.name}${x.kind === 'request' ? '<span class="future-label">FUTURE</span>' : ''}</button>`).join('')}</section>`).join('')}</div><div class="palette-footnote">Drag into the flow, or select a component and click a valid empty flow location.<br>Execution uses the selected runtime.</div></aside>`;
  }
  function renderLower(): string {
    return `<section class="lower-workarea" aria-label="Properties work area"><nav class="lower-tabs" role="tablist" aria-label="Lower work area">${[['properties', 'Properties'], ['problems', 'Problems'], ['console', 'Console'], ['outline', 'Outline']].map(([id, name]) => `<button class="tab${view.lower === id ? ' active' : ''}" role="tab" aria-selected="${view.lower === id}" data-action="lower-${id}" data-focus="lower-${id}">${id === 'properties' ? icon('properties') : ''}${name}${id === 'problems' && document?.model.diagnostics.length ? ` (${document.model.diagnostics.length})` : ''}</button>`).join('')}<span class="bar-spacer"></span><span class="fixture-tag">${document?.dirty ? 'Unsaved changes' : 'Document saved'}</span></nav><div class="lower-content">${view.lower === 'properties' ? renderProperties() : view.lower === 'problems' ? renderProblems() : view.lower === 'console' ? renderConsole() : renderOutline()}</div></section>`;
  }
  function field(label: string, key: string, value: string, multiline = false): string {
    const node = component();
    const draftKey = `${node?.id}:${key}`;
    const current = drafts.get(draftKey) ?? value;
    const id = `component-${key}`;
    const attrs = `id="${id}" data-field="${key}" data-focus="${id}" ${!node?.editable || !flow()?.editable || !canEdit() ? 'disabled' : ''} maxlength="${multiline ? 16000 : 512}"`;
    return `<div class="form-row"><label for="${id}">${label}</label><div>${multiline ? `<textarea ${attrs} rows="2" class="expression-input">${escape(current)}</textarea>` : `<input ${attrs} value="${escape(current)}">`}</div></div>`;
  }
  function renderTransformOutputs(node:RealTransform):string{
    const targets=['payload','attributes','variables'] as const;
    const keys={payload:'script',attributes:'attributesScript',variables:'variablesScript'} as const;
    const resources={payload:'payloadResource',attributes:'attributesResource',variables:'variablesResource'} as const;
    const active=targets.filter(target=>!!(node[keys[target]]?.trim()||node[resources[target]]?.trim()));
    const targetButton=(action:string,label:string,target:string,glyph:string,disabled=false)=>button(action,label,glyph,disabled,'',`data-target="${target}"`);
    return `${field('Display Name','displayName',node.displayName)}<div class="transform-outputs">${active.map(target=>`<section class="transform-output"><header><strong>${target[0].toUpperCase()+target.slice(1)}</strong>${targetButton('removeTransformOutput','Remove '+target,target,'trash',active.length<=1)}</header>${node[resources[target]]?`<p>External: <code>${escape(node[resources[target]])}</code></p>${targetButton('openTransformScript','Open external file',target,'code')}`:field(target==='variables'?'Variables DataWeave object script':target==='attributes'?'Attributes DataWeave script':'Payload DataWeave script',keys[target],node[keys[target]]??'',true)}${target==='payload'?field('Output MIME type','mimeType',node.mimeType):''}${targetButton('configureTransformScript','Configure script source…',target,'settings')}</section>`).join('')}</div><div class="transform-add-outputs">${targets.filter(t=>!active.includes(t)).map(target=>targetButton('addTransformOutput','Add '+target,target,'plus')).join('')}</div><p class="ui-note">Payload is the default output. Add attributes or variables when needed; keep at least one output. Blank outputs preserve input. Variables scripts return an application/java object of named variables. Scripts share the same input event. Configure HTTP success and error responses on the HTTP Listener.</p>`;
  }
  function renderProperties(): string {
    const node = component();
    if (!node) return `<div class="empty-properties">${icon('settings')}<h2>Select a component to view its properties</h2><p>Choose a Listener, Logger or Transform Message in your flow. Values edit this integration XML document.</p></div>`;
    const sections = node.kind === 'listener' ? ['General', 'Responses', 'Error Responses', 'Help'] : node.kind === 'transform' ? ['General'] : ['General', 'Help'];
    if (!sections.includes(view.section)) view.section = 'General';
    const locked = !node.editable || !flow()?.editable || !canEdit();
    const index = flow()?.processors.findIndex(n => n.id === node.id) ?? -1;
    const actions = node.kind !== 'listener' ? `${iconButton('duplicate', 'Duplicate processor', 'copy', locked)}${iconButton('moveLeft', 'Move processor earlier', 'arrowLeft', locked || index <= 0)}${iconButton('moveRight', 'Move processor later', 'arrowRight', locked || index >= (flow()?.processors.length ?? 0) - 1)}` : '';
    return `<section class="property-main" aria-label="${componentName(node)} properties"><div class="property-heading">${icon(componentIcon(node))}<span>${componentName(node)}</span><span class="fixture-tag">${locked ? 'Preserved · read only' : 'Real XML properties'}</span>${actions}${iconButton('deleteNode', node.kind === 'listener' ? 'Remove Listener' : 'Delete processor', 'trash', locked)}</div><div class="property-detail"><nav class="section-nav" aria-label="Property sections">${sections.map(s => `<button class="${s === view.section ? 'active' : ''}" data-action="section" data-section="${s}" aria-current="${s === view.section ? 'page' : 'false'}" data-focus="section-${s}">${s}</button>`).join('')}</nav><div class="property-form" data-scroll="properties">${renderFields(node)}</div></div></section>`;
  }
  function renderFields(node: Component): string {
    if(node.kind==='core'||node.kind==='scheduler'){
      const fields=node.kind==='core'?coreDefinition(node.coreType)?.fields??[]:[{key:'frequency',label:'Frequency',initial:'1000'},{key:'timeUnit',label:'Time unit',initial:'MILLISECONDS'},{key:'startDelay',label:'Initial delay',initial:'0'},{key:'cron',label:'Cron expression (overrides frequency)',initial:''},{key:'timeZone',label:'Cron time zone',initial:''},{key:'disallowConcurrentExecution',label:'Disallow concurrent executions',initial:'true'}];
      return `${field('Display Name','displayName',node.displayName)}${fields.map(f=>field(f.label,f.key,node.values[f.key]??f.initial,'multiline' in f&&!!f.multiline)).join('')}<p class="ui-note">${escape(node.kind==='core'?coreDefinition(node.coreType)?.help:'Starts a flow on a fixed schedule or cron expression.')}</p>`;
    }
    if (node.kind === 'transform') return renderTransformOutputs(node);
    if (view.section === 'Help') return `<div class="section-placeholder"><h3>${componentName(node)}</h3><p>These fields edit the real XML document. Save before building; Run / Redeploy will ask how to handle unsaved documents.</p><p>Expressions run only in the bundled ZERNOVA Runtime. Static XML diagnostics cannot prove expression correctness.</p><p>${node.kind === 'listener' ? 'HTTP only. TLS, advanced settings and connection testing are unavailable. Configure the successful response under Responses.' : 'Literal text and DataWeave expressions are supported by the runtime. Open Console → Application logs to read actual Logger output.'}</p></div>`;
    if (node.kind === 'listener' && view.section === 'Responses') return `${field('Success status', 'responseStatus', node.responseStatus)}${field('Response body', 'responseBody', node.responseBody, true)}${field('Response headers', 'responseHeaders', node.responseHeaders, true)}<p class="ui-note">Body and headers accept runtime expressions. For the milestone use <code>#['Hello from ZERNOVA']</code> and <code>#[{'Content-Type': 'text/plain; charset=UTF-8'}]</code>. Configure failures separately under Error Responses. The Listener owns HTTP response serialization and headers.</p>`;
    if(node.kind==='listener'&&view.section==='Error Responses')return `${field('Error status','errorStatus',node.errorStatus)}${field('Error response body','errorBody',node.errorBody,true)}${field('Error response headers','errorHeaders',node.errorHeaders,true)}<p class="ui-note">Used for propagated flow errors. For example #[error.description] and status 500. An On Error Continue route normally produces the success response instead.</p>`;
    if (node.kind === 'listener') return `${field('Display Name', 'displayName', node.displayName)}<div class="form-row"><label for="listener-config">Connector configuration</label><div class="field-group"><select id="listener-config" data-field="configId" data-focus="listener-config" ${!node.editable || !flow()?.editable || !canEdit() ? 'disabled' : ''}><option value="">Select a configuration…</option>${document?.model.configs.map(c => `<option value="${escape(c.id)}" ${c.id === node.configId ? 'selected' : ''}>${escape(c.name)}</option>`).join('')}</select>${iconButton('createConfig', 'Create HTTP Listener configuration', 'plus', !canEdit())}${iconButton('editListenerConfig', 'Edit HTTP Listener configuration', 'edit', !node.configId || !canEdit())}</div></div>${field('Path', 'path', node.path)}${field('Allowed methods', 'allowedMethods', node.allowedMethods)}<p class="ui-note">Methods: ${HTTP_METHODS.join(', ')}. Separate multiple methods with commas. Configure the HTTP response under Responses. A configured listener is not evidence that an endpoint is running.</p>`;
    return `${field('Display Name', 'displayName', node.displayName)}${field('Message', 'message', node.message, true)}<div class="form-row"><label for="logger-level">Level</label><select id="logger-level" data-field="level" data-focus="logger-level" ${!node.editable || !flow()?.editable || !canEdit() ? 'disabled' : ''}>${LOG_LEVELS.map(s => `<option ${node.level === s ? 'selected' : ''}>${s}</option>`).join('')}</select></div>${field('Category', 'category', node.category)}<p class="ui-note">Logger executes inside the runtime. Example: <code>#['ZERNOVA_TEST ' ++ (attributes.queryParams.testId default 'missing')]</code>.</p>`;
  }
  function renderProblems(): string {
    const errors = document?.model.diagnostics ?? [];
    return `<div class="lower-placeholder" data-scroll="problems"><h3>Document diagnostics</h3>${errors.length ? errors.map(d => `<div class="real-problem">${icon('warning')}<span>${escape(d.message)}${'line' in d ? ` · line ${escape(d.line)}` : ''}</span></div>`).join('') : '<p>No static XML issues found. Expressions and dependencies are validated by the real build/runtime.</p>'}${runtime.lastError ? `<h3>Last runtime/build failure</h3><pre class="real-error">${escape(runtime.lastError)}</pre>` : ''}</div>`;
  }
  function renderConsole(): string {
    const busy = !!runtime.busy;
    const log = view.consoleTab === 'deployment' ? String(runtime.deploymentOutput??'')+'\n'+String(runtime.runtimeConsole??'')+'\n'+String(runtime.applicationLogs??'') : view.consoleTab === 'debug' ? (runtime.debugOutput??'Start Debug, then send an HTTP request. Right-click a component → Toggle breakpoint, or select it and press F9.') : view.consoleTab === 'build' ? display(runtime.buildOutput ?? '') : view.consoleTab === 'logs' ? display(runtime.applicationLogs ?? '') : view.consoleTab === 'runtime' ? display(String(runtime.runtimeOutput??'')) : JSON.stringify(runtime, (key, value) => ['buildOutput', 'applicationLogs'].includes(key) ? undefined : value, 2);
    return `<section class="real-console" aria-label="Real runtime and build output"><div class="real-console-actions">${button('runtime-build', 'Build', 'code', busy)}${button('runtime-run', 'Run / Redeploy', 'logger', busy, 'primary')}${button('runtime-status', 'Status', undefined, busy)}${button('runtime-logs', 'Refresh logs', undefined, busy)}${button('followLogs', 'Follow latest')}${button('runtime-stopApplication', 'Undeploy', undefined, busy)}${button('runtime-stopRuntime', 'Stop runtime', undefined, busy)}${button('runtime-cancel', 'Cancel', undefined, !busy)}</div>${view.consoleTab === 'debug' ? `<div class="debug-controls"><strong>${runtime.debugPaused ? 'Paused before '+escape(runtime.debugPaused) : runtime.debugConnected ? 'Debugger connected · Running' : 'Debugger disconnected'}</strong>${button('runtime-debugContinue','Continue','play',!runtime.debugPaused)}${button('runtime-debugStep','Next component','arrowRight',!runtime.debugPaused)}${button('runtime-debugDisconnect','Disconnect','stop',!runtime.debugConnected)}</div>` : ''}${runtime.lastError ? `<div class="real-conflict" role="alert"><strong>Operation failed</strong><p>${escape(runtime.lastError)}</p></div>` : ''}<nav class="real-output-tabs" role="tablist" aria-label="Output source">${[['deployment','Deployment log'], ['build', 'Build output'], ['logs', 'Application logs · live'], ['runtime', 'Runtime output'], ['debug','Debug console'], ['status', 'Runtime status']].map(([id, name]) => `<button class="tab${view.consoleTab === id ? ' active' : ''}" role="tab" aria-selected="${view.consoleTab === id}" data-action="console-${id}" data-focus="console-${id}">${name}</button>`).join('')}</nav><pre class="real-output" data-scroll="console-${view.consoleTab}" tabindex="0" aria-label="${view.consoleTab === 'debug' ? 'Studio Debug console' : view.consoleTab === 'logs' ? 'Application runtime logs' : view.consoleTab === 'build' ? 'Actual build output' : view.consoleTab==='deployment'?'Deployment lifecycle log':'Runtime status JSON'}">${escape(log || (view.consoleTab === 'logs' ? 'Following application logs automatically. Send a request to execute your Logger; no entries have been written yet.' : 'No operation output yet.'))}</pre></section>`;
  }
  function renderOutline(): string {
    return `<div class="lower-placeholder" data-scroll="outline"><h3>${escape(document?.projectName ?? 'Project')}</h3>${document?.model.flows.map(f => `<h4>${escape(f.name)}${f.editable ? '' : ' · read only'}</h4>${[...(f.source ? [f.source] : []), ...f.processors].map(n => `<button class="outline-row" data-action="outline-select" data-flow="${escape(f.id)}" data-node="${escape(n.id)}">${icon(componentIcon(n))}${escape(n.displayName)}</button>`).join('')}`).join('') ?? ''}</div>`;
  }
  function propertyGlobals():string {
    const rows=[...(document?.model.properties??[]).map(p=>({id:p.id,name:p.file,type:'Configuration properties',value:''})),...(document?.model.globalProperties??[]).map(p=>({id:p.id,name:p.name,type:'Global property',value:p.value}))];
    return `<section class="property-globals"><div class="global-toolbar">${button('configureEnvironment','Environment & properties…','settings')}${button('addPropertyFile','Add property file','plus')}${button('addGlobalProperty','Add global property','plus')}</div>${rows.length?`<table class="global-table"><thead><tr><th>Type</th><th>Name / file</th><th>Value</th><th>Actions</th></tr></thead><tbody>${rows.map(p=>`<tr data-config="${escape(p.id)}" data-action="selectConfig" tabindex="0"><td>${escape(p.type)}</td><td>${escape(p.name)}</td><td>${escape(p.value)}</td><td>${button('editProperty','Open','edit',false,'',`data-configid="${escape(p.id)}"`)}${button('deleteProperty','Delete','trash',false,'',`data-configid="${escape(p.id)}"`)}</td></tr>`).join('')}</tbody></table>`:'<p>No configuration-property files or global defaults declared.</p>'}</section>`;
  }
  function renderGlobals(): string {
    const configs = document?.model.configs ?? [];
    const selected = configs.find(c => c.id === view.configId);
    return `<div class="globals"><div class="globals-toolbar">${button('createConfig', 'Create', 'plus', !canEdit())}${button('editConfig', 'Edit', 'edit', !selected?.editable || !canEdit())}${button('deleteConfig', 'Delete', 'trash', !selected?.editable || !canEdit())}<div class="globals-search"><input type="search" data-control="global-search" data-focus="global-search" aria-label="Search global configurations" placeholder="Search configurations…" value="${escape(globalQuery)}"></div></div><table class="global-table" aria-label="HTTP Listener configurations"><thead><tr><th scope="col">Type</th><th scope="col">Name</th><th scope="col">Description</th></tr></thead><tbody>${configs.filter(c => c.name.toLowerCase().includes(globalQuery.toLowerCase())).map(c => `<tr class="${c.id === view.configId ? 'selected' : ''}" data-config="${escape(c.id)}" data-action="selectConfig" tabindex="0" data-focus="config-${escape(c.id)}" aria-selected="${c.id === view.configId}"><td><span class="type-cell"><span class="node-symbol logger">${icon('globe')}</span>HTTP</span></td><td>${escape(c.name)}</td><td>${c.editable ? 'HTTP Listener configuration' : 'Preserved · unsupported settings'}</td></tr>`).join('')}</tbody></table>${!configs.length ? '<div class="no-results">No HTTP configurations. Create one and select it from your Listener.</div>' : ''}${propertyGlobals()}<p class="ui-note">Changes update this XML document. Referenced configurations cannot be deleted; cross-file references are checked before renaming.</p></div>`;
  }
  function renderXml(): string {
    const text = xmlDraft ?? document?.text ?? '';
    return `<section class="xml-view" aria-label="Configuration XML"><div class="code-bar">${icon('code')}<span>Configuration XML${document?.dirty ? ' · Unsaved' : ''}${pending ? ' · Applying edit…' : ''}</span>${button('save', 'Save')}${button('copyXml', 'Copy XML', 'copy')}</div>${xmlConflict ? `<div class="real-conflict" role="alert">The document changed while an XML draft was pending. Your draft is retained below; the document was not overwritten.${button('copyXml', 'Copy retained draft')}${button('discardDraft', 'Use current document')}${button('applyDraft', 'Apply retained draft')}</div>` : ''}<div class="real-xml-editor"><pre class="real-line-numbers" aria-hidden="true">${text.split('\n').map((_, i) => i + 1).join('\n')}</pre><textarea class="real-xml-source" spellcheck="false" wrap="off" data-control="xml" data-focus="xml-source" data-scroll="xml-source" aria-label="Configuration XML" maxlength="1048576" ${pending?.operation || queue.length ? 'readonly' : ''}>${escape(text)}</textarea></div></section>`;
  }
  function openConfig(id?: string): void {
    const existing = document?.model.configs.find(c => c.id === id);
    if (existing && !existing.editable) { announce('This configuration has unsupported settings. Edit its preserved XML directly.', true); return; }
    returnFocus = focusSnapshot()?.key ?? '';
    let suffix = 1;
    while (document?.model.configs.some(c => c.name === `HTTP_Listener_config${suffix === 1 ? '' : '_' + suffix}`)) suffix++;
    configDialog = { id: existing?.id, baseVersion: document?.version ?? -1, values: existing ? { name: existing.name, protocol: 'HTTP', host: existing.host, port: existing.port, basePath: existing.basePath } : { name: `HTTP_Listener_config${suffix === 1 ? '' : '_' + suffix}`, protocol: 'HTTP', host: 'localhost', port: '18081', basePath: '' }, error: '' };
    render(); focusDialog();
  }
  function focusDialog(): void { window.requestAnimationFrame(() => root.querySelector<HTMLElement>('.dialog input,.dialog button:not(:disabled)')?.focus()); }
  function closeDialog(): void { if(hostForm){if(!hostForm.busy)vscode.postMessage({type:'realFormCancel',id:hostForm.id});return;}configDialog = undefined; flowDialog = undefined; deleteDialog = undefined; render(); restoreFocus({ key: returnFocus, start: null, end: null }); }
  function renderDialog(): string {
    if(hostForm)return `<div class="dialog-backdrop"><section class="dialog studio-host-form${hostForm.fields.length<=2?' small-dialog':''}" role="dialog" aria-modal="true" aria-labelledby="host-form-title"><div class="dialog-title"><span id="host-form-title">${escape(hostForm.title)}</span>${button('hostFormCancel','×',undefined,!!hostForm.busy,'icon-button','aria-label="Close wizard"')}</div><div class="dialog-body">${hostForm.help?`<p class="ui-note">${escape(hostForm.help)}</p>`:''}${hostForm.fields.map(f=>`<label for="host-${escape(f.key)}">${escape(f.label)}</label>${f.options?`<select id="host-${escape(f.key)}" data-host-field="${f.key}" data-focus="host-${f.key}">${f.options.map(o=>`<option value="${escape(o.value)}" ${o.value===f.value?'selected':''}>${escape(o.label)}</option>`).join('')}</select>`:f.multiline?`<textarea id="host-${escape(f.key)}" data-host-field="${f.key}" data-focus="host-${f.key}" maxlength="${f.maxLength??16000}">${escape(f.value)}</textarea>`:`<input id="host-${escape(f.key)}" data-host-field="${f.key}" data-focus="host-${f.key}" value="${escape(f.value)}" maxlength="${f.maxLength??16000}">`}${f.hint?`<p class="ui-note">${escape(f.hint)}</p>`:''}`).join('')}${hostForm.error?`<p class="form-error" role="alert">${escape(hostForm.error)}</p>`:''}</div><footer class="dialog-actions"><span class="bar-spacer"></span>${button('hostFormCancel','Cancel',undefined,!!hostForm.busy)}${button('hostFormSubmit',hostForm.busy?'Working…':hostForm.title==='New Integration XML'?'Create XML':'Save',undefined,!!hostForm.busy,'primary')}</footer></section></div>`;

    if (flowDialog) return `<div class="dialog-backdrop"><section class="dialog small-dialog" role="dialog" aria-modal="true" aria-labelledby="flow-dialog-title"><div class="dialog-title"><span id="flow-dialog-title">${flowDialog.id ? 'Rename flow' : 'Create flow'}</span>${iconButton('cancelDialog', 'Close flow editor', 'close')}</div><div class="dialog-body"><label for="flow-name-input">Flow name</label><input id="flow-name-input" data-flow-name data-focus="flow-name" value="${escape(flowDialog.name)}" maxlength="100">${!flowDialog.id?`<label for="flow-kind">Type</label><select id="flow-kind" data-flow-kind><option value="flow" ${flowDialog.flowKind!=='sub-flow'?'selected':''}>Flow</option><option value="sub-flow" ${flowDialog.flowKind==='sub-flow'?'selected':''}>Subflow</option></select>`:''}${flowDialog.error ? `<p class="form-error" role="alert">${escape(flowDialog.error)}</p>` : ''}<p class="ui-note">Use a unique name. A new flow starts empty.</p></div><div class="dialog-actions"><span class="bar-spacer"></span>${button('cancelDialog', 'Cancel')}${button('saveFlow', flowDialog.id ? 'Rename' : 'Create flow', undefined, false, 'primary')}</div></section></div>`;
    if (deleteDialog) return `<div class="dialog-backdrop"><section class="dialog small-dialog" role="dialog" aria-modal="true" aria-labelledby="delete-title"><div class="dialog-title" id="delete-title">Delete HTTP configuration</div><div class="dialog-body"><p>Delete <strong>${escape(deleteDialog.name)}</strong> from this XML?</p><p>Deletion is refused if a project XML file references the configuration.</p>${deleteDialog.error ? `<p class="form-error" role="alert">${escape(deleteDialog.error)}</p>` : ''}</div><div class="dialog-actions"><span class="bar-spacer"></span>${button('cancelDialog', 'Cancel')}${button('confirmDeleteConfig', 'Delete', undefined, false, 'danger')}</div></section></div>`;
    if (!configDialog) return '';
    const d = configDialog.values;
    const row = (key: keyof typeof d, label: string) => `<div class="form-row"><label for="dialog-${key}">${label}</label><input id="dialog-${key}" data-draft="${key}" data-focus="dialog-${key}" value="${escape(d[key])}" maxlength="512"></div>`;
    return `<div class="dialog-backdrop"><section class="dialog" role="dialog" aria-modal="true" aria-labelledby="config-dialog-title"><div class="dialog-title"><span id="config-dialog-title">HTTP Listener configuration</span>${iconButton('cancelDialog', 'Close configuration editor', 'close')}</div><nav class="dialog-tabs" role="tablist" aria-label="HTTP configuration sections"><button class="tab active" role="tab" aria-selected="true">General</button><button class="tab" disabled title="TLS is outside this milestone">TLS</button><button class="tab" disabled title="Advanced settings are outside this milestone">Advanced</button></nav><div class="dialog-body" data-scroll="dialog">${row('name', 'Name')}<div class="form-row"><label for="dialog-protocol">Protocol</label><select id="dialog-protocol" data-draft="protocol" data-focus="dialog-protocol"><option>HTTP</option></select></div>${row('host', 'Host')}${row('port', 'Port')}${row('basePath', 'Base path')}${configDialog.error ? `<p class="form-error" role="alert">${escape(configDialog.error)}</p>` : ''}<p class="ui-note">Local testing supports localhost and 127.0.0.1. Static fields may reference application properties; the runtime validates their resolved values.</p></div><div class="dialog-scope">Save applies changes to this document. XML changes are autosaved.</div><footer class="dialog-actions"><button disabled title="An inbound Listener is verified by deployment and an actual HTTP request">Test Connection</button><span class="test-explanation">Inbound Listener · use a real request</span><span class="bar-spacer"></span>${button('cancelDialog', 'Cancel')}${button('saveDialog', 'Save', undefined, false, 'primary')}</footer></section></div>`;
  }
  const menuIcons: Record<string,string> = { save:'save','runtime-run':'play','runtime-debug':'debug','runtime-configureRun':'settings','runtime-configureDebug':'settings','runtime-build':'build','runtime-logs':'logger','runtime-stopRuntime':'stop','tab-flow':'fit','tab-xml':'code' };
  function renderMenu(): string {
    if (contextMenu?.kind === 'run' || contextMenu?.kind === 'debug') {
      const debug = contextMenu.kind === 'debug';
      return `<div class="context-menu" role="menu" aria-label="${debug ? 'Debug' : 'Run'} options" style="left:${Math.max(0,Math.min(contextMenu.x,window.innerWidth-250))}px;top:${contextMenu.y}px">${button(debug ? 'runtime-debug' : 'runtime-run',debug ? 'Debug application' : 'Run / Deploy',debug ? 'debug' : 'play',!!runtime.busy,'','role="menuitem"')}${button(debug ? 'runtime-configureDebug' : 'runtime-configureRun',debug ? 'Debug configuration…' : 'Run configuration…','settings',!!runtime.busy,'','role="menuitem"')}</div>`;
    }
    if (contextMenu?.kind === 'canvas') return `<div class="context-menu" role="menu" aria-label="Integration actions" style="left:${Math.max(0,Math.min(contextMenu.x, window.innerWidth - 260))}px;top:${Math.max(0,Math.min(contextMenu.y, window.innerHeight - 370))}px">${[['save','Save'],['runtime-run','Run / Deploy'],['runtime-debug','Debug application'],['runtime-configureRun','Run configuration…'],['runtime-configureDebug','Debug configuration…'],['runtime-build','Build application'],['runtime-logs','Application logs'],['runtime-stopRuntime','Stop runtime'],['tab-flow','Message Flow'],['tab-xml','Configuration XML']].map(([a,n]) => button(a,n,menuIcons[a],!!runtime.busy && a.startsWith('runtime-'),'','role="menuitem"')).join('')}</div>`;
    if(contextMenu?.kind==='config'&&(contextMenu.id.startsWith('property-file:')||contextMenu.id.startsWith('global-property:')))return `<div class="context-menu" role="menu" aria-label="Property configuration actions" style="left:${Math.max(0,Math.min(contextMenu.x,window.innerWidth-230))}px;top:${contextMenu.y}px">${button('editProperty','Open','edit',false,'','role="menuitem"')}${button('deleteProperty','Delete','trash',false,'','role="menuitem"')}</div>`;
    if (contextMenu?.kind === 'config') return `<div class="context-menu" role="menu" aria-label="Global configuration actions" style="left:${Math.max(0,Math.min(contextMenu.x,window.innerWidth-230))}px;top:${Math.max(0,Math.min(contextMenu.y,window.innerHeight-180))}px">${button('editConfig','Open configuration','edit',false,'','role="menuitem"')}${button('deleteConfig','Delete configuration','trash',!canEdit(),'','role="menuitem"')}${button('createConfig','New configuration','plus',!canEdit(),'','role="menuitem"')}</div>`;
    const c = component();
    if (!contextMenu || !c) return '';
    const locked = !c.editable || !flow()?.editable || !canEdit();
    return `<div class="context-menu" role="menu" aria-label="Component actions" style="left:${Math.min(contextMenu.x, window.innerWidth - 230)}px;top:${Math.min(contextMenu.y, window.innerHeight - 220)}px">${button('toggleBreakpoint',runtime.debugBreakpoints?.some(b=>b.uri===document?.uri&&b.id===c.id)?'Remove breakpoint (F9)':'Add breakpoint (F9)','debug',false,'','role="menuitem"')}${button('showProperties', 'Open Properties', 'properties', false, '', 'role="menuitem"')}${c.kind !== 'listener'&&c.kind!=='scheduler' ? button('duplicate', 'Duplicate processor', 'copy', locked, '', 'role="menuitem"') + button('moveLeft', 'Move earlier', 'arrowLeft', locked, '', 'role="menuitem"') + button('moveRight', 'Move later', 'arrowRight', locked, '', 'role="menuitem"') : ''}${button('deleteNode', c.kind !== 'listener'&&c.kind!=='scheduler' ? 'Delete processor' : 'Remove Listener', 'trash', locked, '', 'role="menuitem"')}</div>`;
  }
  function setView(fn: () => void): void { fn(); persist(); render(); }
  root.addEventListener('click', event => {
    if (suppressPlacementClick) { event.preventDefault(); event.stopPropagation(); suppressPlacementClick = false; return; }
    const target = event.target as HTMLElement;
    const ownerChanged=activateFlowAt(target);
    const el = target.closest<HTMLElement>('[data-action]');
    if (!el && selectedPalette && view.tab === 'flow' && !target.closest('[data-node]')) { const lane = target.closest<HTMLElement>('[data-drop]'); if (lane && validPlacement(selectedPalette,lane.dataset.drop!,lane.dataset.parent)) { dropPlacement({kind:selectedPalette},lane,target,event.clientX); return; } }
    if (!el || (el as HTMLButtonElement).disabled) { if (contextMenu) { contextMenu = undefined; render(); } else if(ownerChanged)render();return; }
    const name = el.dataset.action!;
    if (name === 'runMenu' || name === 'debugMenu') { const kind=name === 'runMenu' ? 'run' : 'debug'; const rect=el.getBoundingClientRect(); contextMenu=contextMenu?.kind === kind ? undefined : {id:'',kind,x:rect.left,y:rect.bottom+5}; render(); root.querySelector<HTMLElement>('.context-menu [role=menuitem]')?.focus(); return; }
    if (name !== 'selectConfig' && !name.startsWith('insert-')) contextMenu = undefined;
    if (name.startsWith('runtime-')) { action(name.slice(8)); return; }
    if (name.startsWith('tab-')) { setView(() => { view.tab = name.slice(4) as EditTab; }); return; }
    if (name.startsWith('lower-')) { setView(() => { view.lower = name.slice(6) as LowerTab; }); return; }
    if (name.startsWith('console-')) { setView(() => { view.consoleTab = name.slice(8) as ViewState['consoleTab']; }); return; }
    const f = flow(), c = component();
    if(name.startsWith('insert-')&&name!=='insert-request'){const kind=name.slice(7);if(kind==='flow'||kind==='sub-flow'){flowDialog={baseVersion:document?.version??-1,name:suggestedFlowName(kind),flowKind:kind,error:''};render();focusDialog();return;}if(kind==='error-handler')openErrors.add(flow()?.id??'');setView(()=>{selectedPalette=selectedPalette===kind?undefined:kind;});return;}
    switch (name) {
      case 'hostFormCancel':closeDialog();break;
      case 'hostFormSubmit':if(hostForm&&!hostForm.busy){hostForm.busy=true;vscode.postMessage({type:'realFormSubmit',id:hostForm.id,values:Object.fromEntries(hostForm.fields.map(f=>[f.key,f.value]))});render();}break;
      case 'toggleScope':{const id=el.dataset.scope;if(id){collapsedScopes.has(id)?collapsedScopes.delete(id):collapsedScopes.add(id);render();}break;}
      case 'toggleBreakpoint': if(view.selectedId){vscode.postMessage({type:'realBreakpoint',id:view.selectedId});contextMenu=undefined;render();} break;
      case 'configureTransformScript':case 'openTransformScript':if(c?.kind==='transform')vscode.postMessage({type:'realResource',action:name==='configureTransformScript'?'configureScript':'openScript',id:c.id,target:el.dataset.target});break;
      case 'addTransformOutput':case 'removeTransformOutput':if(c?.kind==='transform'){
        const target=el.dataset.target as 'payload'|'attributes'|'variables';if(!['payload','attributes','variables'].includes(target))break;
        const scriptKey=({payload:'script',attributes:'attributesScript',variables:'variablesScript'} as const)[target],resourceKey=({payload:'payloadResource',attributes:'attributesResource',variables:'variablesResource'} as const)[target];
        const values=name==='removeTransformOutput'?{[scriptKey]:'',[resourceKey]:''}:{[scriptKey]:target==='payload'?'payload':target==='attributes'?'attributes':'{}',[resourceKey]:''};
        edit({kind:'updateComponent',id:c.id,values});
      }break;
      case 'configureEnvironment':vscode.postMessage({type:'realResource',action:'configureEnvironment'});break;
      case 'addGlobalProperty':case 'addPropertyFile':vscode.postMessage({type:'realResource',action:name});break;
      case 'editProperty':{const id=el.dataset.configid??view.configId;if(id)vscode.postMessage({type:'realResource',action:id.startsWith('property-file:')?'addPropertyFile':'addGlobalProperty',id});break;}
      case 'deleteProperty':{const id=el.dataset.configid??view.configId;if(id)edit({kind:id.startsWith('property-file:')?'deletePropertyFile':'deleteGlobalProperty',id});break;}

      case 'followLogs': view.lower = 'console'; view.consoleTab = 'logs'; render(); { const logs = root.querySelector<HTMLElement>('[data-scroll="console-logs"]'); if (logs) logs.scrollTop = logs.scrollHeight; } break;
      case 'toggleFlow': if (f) setView(() => { collapsedFlows.has(f.id) ? collapsedFlows.delete(f.id) : collapsedFlows.add(f.id); }); break;
      case 'toggleError': if (f) setView(() => { openErrors.has(f.id) ? openErrors.delete(f.id) : openErrors.add(f.id); }); break;
      case 'addErrorHandler': if (f) edit({ kind: 'createErrorHandler', flowId: f.id }); break;
      case 'save': case 'undo': case 'redo': action(name); break;
      case 'select': case 'outline-select': setView(() => { if (el.dataset.flow) view.flowId = el.dataset.flow; view.selectedId = el.dataset.node!; view.lower = 'properties'; view.section = 'General'; view.tab = 'flow'; contextMenu = undefined; }); break;
      case 'section': setView(() => { view.section = el.dataset.section!; }); break;
      case 'zoomIn': setView(() => { view.zoom = Math.min(160, view.zoom + 10); }); break;
      case 'zoomOut': setView(() => { view.zoom = Math.max(60, view.zoom - 10); }); break;
      case 'newFlow': case 'renameFlow': returnFocus = focusSnapshot()?.key ?? ''; flowDialog = { id: name === 'renameFlow' ? f?.id : undefined, baseVersion: document?.version ?? -1, name: name === 'renameFlow' ? f?.name ?? '' : suggestedFlowName(), error: '' }; render(); focusDialog(); break;
      case 'saveFlow': if (flowDialog) {
        if (!dialogIsCurrent(flowDialog)) break;
        if (!/^[A-Za-z_][A-Za-z0-9_.-]{0,99}$/.test(flowDialog.name) || document?.model.flows.some(x => x.id !== flowDialog?.id && x.name === flowDialog?.name)) { flowDialog.error = 'Use a unique flow name beginning with a letter or underscore.'; render(); break; }
        const op: XmlOperation = flowDialog.id ? { kind: 'renameFlow', flowId: flowDialog.id, name: flowDialog.name } : {kind:'createFlow',name:flowDialog.name,flowKind:flowDialog.flowKind??'flow'};
        closeDialog(); edit(op); break;
      } break;
      case 'insert-listener': case 'insert-logger': case 'insert-transform': { const kind = name === 'insert-listener' ? 'listener' : name === 'insert-transform' ? 'transform' : 'logger'; setView(() => { selectedPalette = selectedPalette === kind ? undefined : kind; }); break; }
      case 'duplicate': if (c && c.kind !== 'listener'&&c.kind!=='scheduler') { contextMenu = undefined; edit({ kind: 'duplicateProcessor', id: c.id }); } break;
      case 'moveLeft': case 'moveRight': if (c && c.kind !== 'listener'&&c.kind!=='scheduler') { contextMenu = undefined; edit({ kind: 'moveProcessor', id: c.id, direction: name === 'moveLeft' ? -1 : 1 }); } break;
      case 'deleteNode': if (c) { contextMenu = undefined; edit({ kind: 'deleteComponent', id: c.id }); } break;
      case 'showProperties': contextMenu = undefined; setView(() => { view.lower = 'properties'; }); break;
      case 'createConfig': openConfig(); break;
      case 'editConfig': openConfig(view.configId ?? undefined); break;
      case 'editListenerConfig': openConfig(f?.source?.kind==='listener'?f.source.configId:undefined); break;
      case 'selectConfig': setView(() => { view.configId = el.dataset.config!; }); break;
      case 'deleteConfig': { const cfg = document?.model.configs.find(x => x.id === view.configId); if (cfg) { returnFocus = focusSnapshot()?.key ?? ''; deleteDialog = { id: cfg.id, baseVersion: document?.version ?? -1, name: cfg.name, error: '' }; render(); focusDialog(); } break; }
      case 'confirmDeleteConfig': if (deleteDialog) { if (!dialogIsCurrent(deleteDialog)) break; const id = deleteDialog.id; closeDialog(); edit({ kind: 'deleteConfig', id }); } break;
      case 'saveDialog': if (configDialog) {
        if (!dialogIsCurrent(configDialog)) break;
        const values = configDialog.values;
        let error = '';
        if (!/^[A-Za-z_][A-Za-z0-9_.-]{0,99}$/.test(values.name)) error = 'Enter a valid configuration name.';
        else if (document?.model.configs.some(x => x.id !== configDialog?.id && x.name === values.name)) error = 'A configuration with this name already exists.';
        else if (!values.host.trim() || /\s/.test(values.host)) error = 'Enter a host without whitespace.';
        else if (!/^\$\{[^{}]+\}$/.test(values.port) && (!/^\d+$/.test(values.port) || Number(values.port) < 1024 || Number(values.port) > 65535)) error = 'Use a local port from 1024 to 65535, or an application property placeholder.';
        else if (values.basePath && !values.basePath.startsWith('/') && !values.basePath.startsWith('${')) error = 'Base path must begin with / or reference an application property.';
        if (error) { configDialog.error = error; render(); break; }
        const id = configDialog.id; closeDialog(); edit({ kind: 'saveConfig', ...(id ? { id } : {}), values });
      } break;
      case 'cancelDialog': closeDialog(); break;
      case 'copyFieldRecovery': void navigator.clipboard.writeText(fieldRecovery).then(() => announce('Retained edits copied.'), () => announce('Clipboard access was unavailable. The retained edits remain in this editor state.', true)); break;
      case 'discardFieldRecovery': fieldRecovery = ''; persist(); render(); break;
      case 'copyXml': void navigator.clipboard.writeText(xmlDraft ?? document?.text ?? '').then(() => announce('XML copied.'), () => announce('Select the XML and copy it using the keyboard.', true)); break;
      case 'discardDraft': xmlDraft = undefined; xmlConflict = false; queue.length = 0; persist(); render(); break;
      case 'applyDraft': xmlConflict = false; sendNext(); render(); break;
    }
  });
  root.addEventListener('input', event => {
    const hostField=(event.target as HTMLElement).dataset.hostField;if(hostField&&hostForm){const field=hostForm.fields.find(f=>f.key===hostField);if(field)field.value=(event.target as HTMLInputElement).value;return;}
    const el = event.target as HTMLInputElement | HTMLTextAreaElement;
    if (el.dataset.control === 'xml') { xmlDraft = el.value; if (pending?.operation || queue.length) { xmlConflict = true; requestedAction = undefined; } persist(); sendNext(); return; }
    if (el.dataset.control === 'palette-search') { query = el.value; render(); return; }
    if (el.dataset.control === 'global-search') { globalQuery = el.value; render(); return; }
    if (el.hasAttribute('data-flow-name') && flowDialog) { flowDialog.name = el.value; return; }
    if (el.dataset.draft && configDialog && ['name', 'host', 'port', 'basePath'].includes(el.dataset.draft)) { configDialog.values[el.dataset.draft as 'name' | 'host' | 'port' | 'basePath'] = el.value; return; }
    if (el.dataset.field && el.tagName !== 'SELECT') {
      const c = component(); if (!c) return;
      const key = `${c.id}:${el.dataset.field}`; drafts.set(key, el.value);
      edit({ kind: c.kind==='core'||c.kind==='scheduler'?'updateCore':'updateComponent', id: c.id, values: { [el.dataset.field]: el.value } }, key, el.value);
    }
  });
  root.addEventListener('change', event => {
    const hostField=(event.target as HTMLElement).dataset.hostField;if(hostField&&hostForm){const field=hostForm.fields.find(f=>f.key===hostField);if(field)field.value=(event.target as HTMLInputElement).value;return;}
    const el = event.target as HTMLSelectElement;
    if (el.tagName !== 'SELECT') return;
    if(el.hasAttribute('data-flow-kind')&&flowDialog){flowDialog.flowKind=el.value==='sub-flow'?'sub-flow':'flow';return;}
    if (el.dataset.control === 'theme') { setView(() => { view.theme = el.value as ViewState['theme']; }); return; }
    const c = component();
    if (el.dataset.field && c) edit({ kind: c.kind==='core'||c.kind==='scheduler'?'updateCore':'updateComponent', id: c.id, values: { [el.dataset.field]: el.value } });
  });
  root.addEventListener('dblclick', event => { const row = (event.target as HTMLElement).closest<HTMLElement>('[data-config]'); if (row) {view.configId=row.dataset.config!;if(/^(property-file:|global-property:)/.test(view.configId))action('editProperty');else openConfig(view.configId);} });
  root.addEventListener('contextmenu', event => {
    const target = event.target as HTMLElement;activateFlowAt(target);const node = target.closest<HTMLElement>('[data-node]'), config = target.closest<HTMLElement>('[data-config]');
    if (target.closest('.dialog') || !node && !config && !target.closest('.editor-content')) return;
    event.preventDefault();
    if (node) view.selectedId = node.dataset.node!;
    if (config) view.configId = config.dataset.config!;
    contextMenu = { id: node?.dataset.node ?? config?.dataset.config ?? '', kind: node ? 'component' : config ? 'config' : 'canvas', x: event.clientX, y: event.clientY }; render();
    root.querySelector<HTMLElement>('.context-menu button:not(:disabled)')?.focus();
  });
  root.addEventListener('keydown', event => {
    if(hostForm&&event.key==='Enter'&&(event.target as HTMLElement).tagName==='INPUT'){event.preventDefault();root.querySelector<HTMLButtonElement>('[data-action=hostFormSubmit]')?.click();return;}
    if(event.key==='F9' && view.selectedId && !(event.target as HTMLElement).closest('input,textarea,select')){event.preventDefault();vscode.postMessage({type:'realBreakpoint',id:view.selectedId});return;}
    const target = event.target as HTMLElement;
    if (event.key === 'Escape') { if (hostForm || configDialog || flowDialog || deleteDialog) closeDialog(); else if (contextMenu) { contextMenu = undefined; render(); } return; }
    if ((hostForm || configDialog || flowDialog || deleteDialog) && event.key === 'Tab') {
      const controls = [...root.querySelectorAll<HTMLElement>('.dialog button:not(:disabled),.dialog input,.dialog textarea,.dialog select')].filter(x => x.offsetParent !== null);
      if (event.shiftKey && target === controls[0]) { event.preventDefault(); controls.at(-1)?.focus(); }
      else if (!event.shiftKey && target === controls.at(-1)) { event.preventDefault(); controls[0]?.focus(); } return;
    }
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 's') { event.preventDefault(); action('save'); return; }
    if ((event.ctrlKey || event.metaKey) && ['z', 'y'].includes(event.key.toLowerCase()) && !configDialog && !flowDialog) { event.preventDefault(); action(event.key.toLowerCase() === 'y' || event.shiftKey ? 'redo' : 'undo'); return; }
    if (target.dataset.resize) {
      const key = target.dataset.resize as 'paletteWidth' | 'propertiesHeight';
      if (event.key === 'Home') { event.preventDefault(); view[key] = DEFAULT_LAYOUT[key]; render(); return; }
      const delta = ['ArrowLeft', 'ArrowUp'].includes(event.key) ? 10 : ['ArrowRight', 'ArrowDown'].includes(event.key) ? -10 : 0;
      if (delta) { event.preventDefault(); view[key] = Math.max(key === 'paletteWidth' ? 190 : 200, Math.min(key === 'paletteWidth' ? 380 : 620, view[key] + delta)); render(); } return;
    }
    if (contextMenu && ['ArrowUp', 'ArrowDown'].includes(event.key)) { event.preventDefault(); const items = [...root.querySelectorAll<HTMLButtonElement>('.context-menu button:not(:disabled)')]; items[(items.indexOf(target as HTMLButtonElement) + (event.key === 'ArrowDown' ? 1 : -1) + items.length) % items.length]?.focus(); return; }
    const row = target.closest<HTMLElement>('[data-config]');
    if (row && ['Enter', ' '].includes(event.key)) { event.preventDefault(); view.configId = row.dataset.config!; if (event.key === 'Enter') {if(/^(property-file:|global-property:)/.test(view.configId))action('editProperty');else openConfig(view.configId);} else render(); return; }
    const node = target.closest<HTMLElement>('[data-node]');
    if (node && event.shiftKey && event.key === 'F10') { event.preventDefault(); view.selectedId = node.dataset.node!; const rect = node.getBoundingClientRect(); contextMenu = { id: view.selectedId, x: rect.left + 20, y: rect.bottom }; render(); root.querySelector<HTMLElement>('.context-menu button')?.focus(); return; }
    if (hostForm || configDialog || flowDialog || deleteDialog || ['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName)) return;
    const c = component();
    if (['Delete', 'Backspace'].includes(event.key) && view.tab === 'flow' && c && c.editable && flow()?.editable) { event.preventDefault(); edit({ kind: 'deleteComponent', id: c.id }); return; }
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'd' && c && c.kind !== 'listener'&&c.kind!=='scheduler') { event.preventDefault(); edit({ kind: 'duplicateProcessor', id: c.id }); return; }
    if (event.altKey && ['ArrowLeft', 'ArrowRight'].includes(event.key) && c && c.kind !== 'listener'&&c.kind!=='scheduler') { event.preventDefault(); edit({ kind: 'moveProcessor', id: c.id, direction: event.key === 'ArrowLeft' ? -1 : 1 }); return; }
    const tab = target.closest<HTMLElement>('[role="tab"]');
    if (tab && ['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) {
      event.preventDefault(); const items = [...tab.parentElement!.querySelectorAll<HTMLButtonElement>('[role="tab"]:not(:disabled)')]; const index = items.indexOf(tab as HTMLButtonElement); const next = event.key === 'Home' ? 0 : event.key === 'End' ? items.length - 1 : (index + (event.key === 'ArrowRight' ? 1 : -1) + items.length) % items.length; items[next]?.focus(); items[next]?.click();
    }
  });
  // Pointer capture gives the same drag behavior in installed desktop webviews,
  // using open lane geometry and no dependency on native HTML drag timing.
  function clearPlacement(): void {
    pointerPlacement = undefined;
    root.querySelector('.placement-ghost')?.remove(); root.classList.remove('pointer-placing');
    root.querySelectorAll('.drop-valid').forEach(x => x.classList.remove('drop-valid'));
  }
  function dropPlacement(item: { kind?: string; id?: string }, lane: HTMLElement, target: HTMLElement, clientX: number): void {
    activateFlowAt(lane);const f = flow(); if (!f) return;
    const name = lane.dataset.drop as 'source' | 'processor' | 'error';
    const parent=lane.dataset.parent?processorTree(f).find(p=>p.id===lane.dataset.parent):undefined;
    const list=parent?.kind==='core'?parent.children:name==='error'?f.errorHandler?.processors??[]:f.processors;
    const node = target.closest<HTMLElement>('[data-node]');
    const nodes=[...lane.querySelectorAll<HTMLElement>(':scope > [data-processor-index]')];
    const index = insertionAt(nodes.map(n => n.getBoundingClientRect().left + n.getBoundingClientRect().width / 2), clientX);
    if (item.kind) place(item.kind,{lane:name,index,parentId:lane.dataset.parent});
    else if (item.id && name !== 'source') {
      const from = list.findIndex(x => x.id === item.id), to = moveToInsertion(from,index,list.length);
      if (from < 0) announce('Move components within their existing processor/error-handler lane.', true);
      else if (from !== to) for (let i=0;i<Math.abs(to-from);i++) edit({kind:'moveProcessor',id:item.id,direction:to<from?-1:1});
    }
  }
  root.addEventListener('pointerdown', event => {
    const element = (event.target as HTMLElement).closest<HTMLElement>('[data-palette],[data-node]');
    if (!element || event.button !== 0 || (element as HTMLButtonElement).disabled || !canEdit()) return;
    if(element.dataset.node)activateFlowAt(element);
    if (element.dataset.node && !componentById(element.dataset.node)?.editable) return;
    pointerPlacement = { item: element.dataset.palette ? { kind: element.dataset.palette as string } : {id:element.dataset.node}, element, startX:event.clientX,startY:event.clientY,active:false };
    element.setPointerCapture(event.pointerId);
  });
  root.addEventListener('pointermove', event => {
    const drag = pointerPlacement; if (!drag) return;
    if (!drag.active && Math.hypot(event.clientX-drag.startX,event.clientY-drag.startY)<6) return;
    event.preventDefault(); drag.active=true; root.classList.add('pointer-placing');
    let ghost=root.querySelector<HTMLElement>('.placement-ghost');
    if(!ghost){ghost=window.document.createElement('div');ghost.className='placement-ghost';ghost.textContent=drag.item.kind?coreDefinition(drag.item.kind)?.name??({listener:'HTTP Listener',logger:'Logger',transform:'Transform Message',flow:'Flow','sub-flow':'Subflow',scheduler:'Scheduler'}[drag.item.kind]??drag.item.kind):'Move component';root.append(ghost);}
    ghost.style.left=(event.clientX+14)+'px';ghost.style.top=(event.clientY+14)+'px';
    root.querySelectorAll('.drop-valid').forEach(x=>x.classList.remove('drop-valid'));
    const lane=window.document.elementFromPoint(event.clientX,event.clientY)?.closest<HTMLElement>('[data-drop]');
    if(drag.item.kind&&['flow','sub-flow'].includes(drag.item.kind)&&window.document.elementFromPoint(event.clientX,event.clientY)?.closest('.editor-content'))root.querySelector('.flow-scroll')?.classList.add('drop-valid');
    if(lane && (drag.item.kind ? validPlacement(drag.item.kind,lane.dataset.drop!,lane.dataset.parent,laneFlowId(lane)) : lane.dataset.drop!=='source')) lane.classList.add('drop-valid');
  });
  root.addEventListener('pointerup', event => {
    const drag=pointerPlacement;if(!drag)return;
    const target=window.document.elementFromPoint(event.clientX,event.clientY) as HTMLElement|null;
    const lane=target?.closest<HTMLElement>('[data-drop]');const active=drag.active;clearPlacement();
    if(!active)return;
    event.preventDefault();suppressPlacementClick=true;window.setTimeout(()=>{suppressPlacementClick=false;},0);
    if((drag.item.kind==='flow'||drag.item.kind==='sub-flow')&&target?.closest('.editor-content')&&view.tab==='flow'){flowDialog={baseVersion:document?.version??-1,name:suggestedFlowName(drag.item.kind),flowKind:drag.item.kind,error:''};render();focusDialog();return;}
    if(lane && target) dropPlacement(drag.item,lane,target,event.clientX);else announce('Drop in a valid Source, Processor or Error Handler location.',true);
  });
  root.addEventListener('pointercancel',clearPlacement);
  function componentById(id:string):Component|undefined{return document?.model.flows.flatMap(f=>[...(f.source?[f.source]:[]),...processorTree(f)]).find(n=>n.id===id);}
  function activateFlowAt(target:HTMLElement):boolean{const id=target.closest<HTMLElement>('[data-flow-id]')?.dataset.flowId;if(id&&id!==view.flowId&&document?.model.flows.some(f=>f.id===id)){view.flowId=id;persist();return true;}return false;}
  const laneFlowId=(lane:HTMLElement)=>lane.closest<HTMLElement>('[data-flow-id]')?.dataset.flowId;
  root.addEventListener('dragstart', event => { const el = (event.target as HTMLElement).closest<HTMLElement>('[draggable="true"]'); if (!el) return; drag = el.dataset.palette ? { kind: el.dataset.palette as string } : { id: el.dataset.node }; event.dataTransfer?.setData('application/x-zernova-component', JSON.stringify(drag)); });
  root.addEventListener('dragover', event => {
    const lane = (event.target as HTMLElement).closest<HTMLElement>('[data-drop]');
    if (!lane || !drag) return;
    const valid = drag.kind ? validPlacement(drag.kind,lane.dataset.drop!,lane.dataset.parent,laneFlowId(lane)) : lane.dataset.drop !== 'source';
    if (valid) { event.preventDefault(); lane.classList.add('drop-valid'); if (event.dataTransfer) event.dataTransfer.dropEffect = drag.kind ? 'copy' : 'move'; }
  });
  root.addEventListener('dragleave', event => (event.target as HTMLElement).closest('[data-drop]')?.classList.remove('drop-valid'));
  root.addEventListener('dragend', () => { drag = undefined; root.querySelectorAll('.drop-valid').forEach(x => x.classList.remove('drop-valid')); });
  root.addEventListener('drop', event => {
    const lane = (event.target as HTMLElement).closest<HTMLElement>('[data-drop]');if(lane)activateFlowAt(lane);const f=flow(); if (!lane || !drag || !f) return;
    event.preventDefault(); const item = drag; drag = undefined; root.querySelectorAll('.drop-valid').forEach(x => x.classList.remove('drop-valid'));
    const name = lane.dataset.drop as 'source' | 'processor' | 'error';
    const parent=lane.dataset.parent?processorTree(f).find(p=>p.id===lane.dataset.parent):undefined;
    const list=parent?.kind==='core'?parent.children:name==='error'?f.errorHandler?.processors??[]:f.processors;
    const index = lane.dataset.index === undefined ? list.length : Number(lane.dataset.index);
    if (item.kind) place(item.kind,{lane:name,index,parentId:lane.dataset.parent});
    else if (item.id && name !== 'source') {
      const from = list.findIndex(x => x.id === item.id); const node = (event.target as HTMLElement).closest<HTMLElement>('[data-node]');
      const to = Math.min(list.length - 1, node ? list.findIndex(x => x.id === node.dataset.node) : index);
      if (from < 0) announce('Move components within their existing processor/error-handler lane.', true);
      else if (from !== to) for (let i = 0; i < Math.abs(to - from); i++) edit({ kind: 'moveProcessor', id: item.id, direction: to < from ? -1 : 1 });
    }
  });
  root.addEventListener('pointerdown', event => { const el = (event.target as HTMLElement).closest<HTMLElement>('[data-resize]'); if (!el) return; event.preventDefault(); const key = el.dataset.resize as 'paletteWidth' | 'propertiesHeight'; resize = { key, start: key === 'propertiesHeight' ? event.clientY : event.clientX, value: view[key] }; el.setPointerCapture(event.pointerId); });
  root.addEventListener('pointermove', event => { if (!resize) return; const change = (resize.key === 'propertiesHeight' ? event.clientY : event.clientX) - resize.start; view[resize.key] = Math.max(resize.key === 'paletteWidth' ? 190 : 200, Math.min(resize.key === 'paletteWidth' ? 380 : 620, resize.value - change)); root.querySelector<HTMLElement>('.studio')?.style.setProperty(resize.key === 'paletteWidth' ? '--palette-width' : '--properties-height', `${view[resize.key]}px`); });
  root.addEventListener('pointerup', () => { if (resize) { resize = undefined; persist(); render(); } });
  window.addEventListener('message', event => {
    const message: unknown = event.data;
    if (!object(message) || typeof message.type !== 'string') return;
    if(message.type==='realForm'){hostForm=object(message.form)?message.form as unknown as StudioFormView:undefined;render();if(hostForm)window.requestAnimationFrame(()=>root.querySelector<HTMLElement>('.dialog [data-host-field]')?.focus());return;}
    if (message.type === 'realDocument' && object(message.document)) {
      const next = message.document as unknown as RealDocumentSnapshot;
      if (document && next.uri !== document.uri) return;
      if (!document && object(saved) && saved.uri !== next.uri) { xmlDraft = undefined; fieldRecovery = ''; }
      if (!document && xmlDraft !== undefined) {
        // A recovered unacknowledged draft must never silently replace a newer
        // on-disk/native document after restart or external modification.
        if (object(saved) && saved.uri !== next.uri) xmlDraft = undefined;
        else if (xmlDraft !== next.text) { xmlConflict = true; view.tab = 'xml'; }
      }
      const changed = !!document && next.version !== document.version;
      if (changed && pointerPlacement) clearPlacement();
      const ownSnapshot = !!pending && next.changeRequestId === pending.requestId;
      if (changed && xmlDraft !== undefined && next.text !== xmlDraft && !(ownSnapshot && pending?.text === next.text)) {
        xmlConflict = true; requestedAction = undefined; view.tab = 'xml';
      }
      if (changed && !ownSnapshot && (pending?.operation || queue.length)) retainFieldDrafts();
      for (const dialog of [configDialog, flowDialog, deleteDialog]) {
        if (dialog && next.version !== dialog.baseVersion) dialog.error = 'The document changed while this dialog was open. Your draft is retained; close and reopen to review the current values.';
      }
      document = next;
      if (!pending && !xmlConflict && xmlDraft === next.text) xmlDraft = undefined;
      if (!next.model.flows.some(f => f.id === view.flowId)) view.flowId = next.model.flows[0]?.id ?? null;
      if (next.selectionId) selectHostId(next.selectionId);
      if (!next.model.configs.some(c => c.id === view.configId)) view.configId = next.model.configs[0]?.id ?? null;
      applyingFromHost = true; render(); applyingFromHost = false; persist();
    } else if (message.type === 'realAck' && pending && message.requestId === pending.requestId) {
      if (pending.text !== undefined && !xmlConflict && xmlDraft === pending.text) xmlDraft = undefined;
      if (pending.operation?.kind === 'updateComponent' && pending.operation.id.startsWith('legacy:') && typeof message.selectionId === 'string' && message.selectionId !== pending.operation.id) {
        const oldId = pending.operation.id, newId = message.selectionId;
        for (const item of queue) {
          if ('id' in item.operation && item.operation.id === oldId) item.operation = { ...item.operation, id: newId };
          if (item.draftKey?.startsWith(oldId + ':')) item.draftKey = newId + item.draftKey.slice(oldId.length);
        }
        for (const [key, value] of [...drafts]) if (key.startsWith(oldId + ':')) {
          const newKey = newId + key.slice(oldId.length);
          if (!drafts.has(newKey)) drafts.set(newKey, value);
          drafts.delete(key);
        }
        if (pending.draftKey?.startsWith(oldId + ':')) pending.draftKey = newId + pending.draftKey.slice(oldId.length);
      }
      if (pending.draftKey && drafts.get(pending.draftKey) === pending.draftValue) drafts.delete(pending.draftKey);
      if (typeof message.selectionId === 'string') selectHostId(message.selectionId);
      const createdFlow=pending.operation?.kind==='createFlow'?view.flowId:undefined;pending = undefined; sendNext(); render();if(createdFlow)window.requestAnimationFrame(()=>root.querySelector<HTMLElement>(`[data-flow-id="${CSS.escape(createdFlow)}"]`)?.scrollIntoView({block:'nearest',inline:'nearest'}));
    } else if (message.type === 'realError') {
      // Runtime errors are unscoped and may arrive during a document edit.
      // Only the matching mutation rejection may clear that edit's queue.
      if (typeof message.requestId === 'string' && pending?.requestId === message.requestId) {
        if (pending.text !== undefined) { xmlDraft ??= pending.text; xmlConflict = true; view.tab = 'xml'; }
        else retainFieldDrafts();
        pending = undefined; queue.length = 0; requestedAction = undefined;
      }
      if (typeof message.requestId !== 'string') { const text = typeof message.message === 'string' ? message.message : 'Operation failed'; if (!runtime.buildOutput?.includes(text)) runtime.buildOutput = (runtime.buildOutput || '') + '\nERROR: ' + text; view.lower = 'console'; view.consoleTab = 'build'; render(); } else announce(typeof message.message === 'string' ? message.message : 'The document operation was rejected.', true); persist();
    } else if (message.type === 'runtimeState' && object(message.state)) { const wasDebugConnected=runtime.debugConnected,previousError=runtime.lastError; runtime = message.state as RuntimeState; if(runtime.debugConnected&&!wasDebugConnected){view.lower='console';view.consoleTab='debug';} if(showLogsAfterRun&&['Starting runtime','Deploying'].includes(String(runtime.phase))){view.lower='console';view.consoleTab='deployment';} if(runtime.debugPaused){view.lower='console';view.consoleTab='debug';} if (runtime.lastError&&runtime.lastError!==previousError) { view.lower = 'console'; view.consoleTab = 'build'; showLogsAfterRun = false; } if (showLogsAfterRun && !runtime.busy) { showLogsAfterRun = false; if (runtime.application === 'DEPLOYED') { view.lower = 'console'; view.consoleTab = runtime.debugConnected?'debug':'deployment'; } } if (!hostForm && !configDialog && !flowDialog && !deleteDialog) render(); }
    else if (message.type === 'hostTheme') render();
  });
  function selectHostId(id: string): void {
    const owner = document?.model.flows.find(f => f.id === id || f.source?.id === id || processorTree(f).some(p=>p.id===id));
    if (owner) { view.flowId = owner.id; view.selectedId = owner.id === id ? null : id; }
    if (document?.model.configs.some(c => c.id === id)) view.configId = id;
  }
  window.addEventListener('pagehide', () => { persist(); window.clearTimeout(toastTimer); if (!applyingFromHost) sendNext(); });
  render(); vscode.postMessage({ type: 'ready' });
}
