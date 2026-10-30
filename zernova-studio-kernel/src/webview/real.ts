import { layoutFlows } from './flowLayout';
/** Real-document presentation. All file edits and runtime operations are host-owned. */
import { icon } from './icons';
import { DEFAULT_LAYOUT, HTTP_METHODS, LOG_LEVELS } from '../shared/model';
import type { RealDocumentSnapshot } from '../shared/realProtocol';
import type { XmlOperation, RealFlow, RealListener, RealLogger, RealHttpConfig } from '../shared/xml';

type VSCodeBridge = { postMessage(message: unknown): void; setState(state: unknown): void; getState(): unknown };
type Component = RealListener | RealLogger;
type EditTab = 'flow' | 'globals' | 'xml';
type LowerTab = 'properties' | 'problems' | 'console' | 'outline';
type RuntimeAction = 'configureRuntime' | 'diagnostics' | 'build' | 'run' | 'status' | 'logs' | 'stopApplication' | 'stopRuntime' | 'cancel';
type RuntimeState = { projectName?: string; configured?: boolean; phase?: string; busy?: boolean; process?: string; application?: string | Record<string, unknown>; endpoint?: string; lastError?: string; buildOutput?: string; applicationLogs?: string; [key: string]: unknown };
type ViewState = { flowId: string | null; selectedId: string | null; configId: string | null; tab: EditTab; lower: LowerTab; section: string; consoleTab: 'build' | 'logs' | 'status'; theme: 'host' | 'light' | 'dark'; zoom: number; paletteWidth: number; propertiesHeight: number };
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
  let flowDialog: { id?: string; baseVersion: number; name: string; error: string } | undefined;
  let deleteDialog: { id: string; baseVersion: number; name: string; error: string } | undefined;
  let returnFocus = '';
  let contextMenu: { id: string; x: number; y: number } | undefined;
  let drag: { kind?: 'listener' | 'logger'; id?: string } | undefined;
  let resize: { key: 'paletteWidth' | 'propertiesHeight'; start: number; value: number } | undefined;
  let applyingFromHost = false;

  const flow = (): RealFlow | undefined => document?.model.flows.find(f => f.id === view.flowId);
  const component = (): Component | undefined => flow()?.source?.id === view.selectedId ? flow()?.source ?? undefined : flow()?.processors.find(c => c.id === view.selectedId);
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
    if (['cancel', 'stopApplication', 'stopRuntime', 'status', 'logs', 'diagnostics', 'configureRuntime'].includes(name)) {
      if (name === 'logs') { view.lower = 'console'; view.consoleTab = 'logs'; }
      if (name === 'diagnostics' || name === 'status') { view.lower = 'console'; view.consoleTab = 'status'; }
      vscode.postMessage({ type: 'realAction', action: name }); render(); return;
    }
    if (xmlConflict || fieldRecovery) { announce('Review the retained draft and current document before saving or running.', true); return; }
    if (name === 'build' || name === 'run') { view.lower = 'console'; view.consoleTab = 'build'; }
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
    const focus = focusSnapshot();
    const scrolls = [...root.querySelectorAll<HTMLElement>('[data-scroll]')].map(el => [el.dataset.scroll!, el.scrollTop, el.scrollLeft] as const);
    window.document.body.dataset.previewTheme = view.theme;
    window.document.documentElement.style.setProperty('--logo-url', `url("${window.__ZERNOVA_ASSETS__.logo.replace(/"/g, '%22')}")`);
    root.innerHTML = `<main class="studio real-studio" aria-label="ZERNOVA Studio real project" style="--palette-width:${view.paletteWidth}px;--properties-height:${view.propertiesHeight}px">
      ${renderBar()}${fieldRecovery ? `<div class="real-conflict" role="alert">A document change interrupted queued edits. Current document values are shown; attempted edits are retained for copying.${button('copyFieldRecovery', 'Copy retained edits')}${button('discardFieldRecovery', 'Review current document')}<details><summary>Retained edits</summary><pre tabindex="0">${escape(fieldRecovery)}</pre></details></div>` : ''}<div class="workspace"><div class="upper-workarea"><section class="main-editor" aria-label="Integration editor"><div class="editor-content" data-scroll="editor">${view.tab === 'xml' ? renderXml() : view.tab === 'globals' ? renderGlobals() : renderFlow()}</div>${renderTabs()}</section>${split('paletteWidth', 'vertical', 'Resize palette')}${renderPalette()}</div>${split('propertiesHeight', 'horizontal', 'Resize properties area')}${renderLower()}</div>
      ${renderDialog()}${contextMenu ? renderMenu() : ''}<div class="sr-only" role="status" aria-live="polite">${escape(toast)}</div>${toast ? `<div class="toast${toastError ? ' error' : ''}" data-testid="toast">${icon(toastError ? 'warning' : 'info')}<span>${escape(toast)}</span></div>` : ''}</main>`;
    for (const [key, top, left] of scrolls) { const el = root.querySelector<HTMLElement>(`[data-scroll="${CSS.escape(key)}"]`); if (el) { el.scrollTop = top; el.scrollLeft = left; } }
    layoutFlows(root);
    restoreFocus(focus);
    if (configDialog || flowDialog || deleteDialog) {
      root.querySelector('.workspace')?.setAttribute('inert', ''); root.querySelector('.preview-bar')?.setAttribute('inert', '');
    }
  }
  function renderBar(): string {
    const busy = !!runtime.busy;
    return `<header class="preview-bar"><span class="brand"><span class="brand-mark" aria-hidden="true"></span>ZERNOVA</span><span class="preview-badge">REAL PROJECT</span><span class="connection" data-testid="runtime-phase">${icon(runtime.lastError ? 'warning' : 'info')}${escape(runtime.phase || 'Not configured')}</span><span class="bar-spacer"></span>
      ${button('runtime-configureRuntime', 'Configure runtime', 'settings', busy)}${button('runtime-diagnostics', 'Diagnostics', undefined, busy)}
      <select class="compact-select theme-select" aria-label="Editor theme" data-control="theme" data-focus="theme">${[['host', 'Host theme'], ['light', 'Light'], ['dark', 'Dark']].map(([id, name]) => `<option value="${id}" ${view.theme === id ? 'selected' : ''}>${name}</option>`).join('')}</select></header>`;
  }
  function renderTabs(): string {
    return `<nav class="editor-tabs" role="tablist" aria-label="Integration views">${[['flow', 'Message Flow'], ['globals', 'Global Elements'], ['xml', 'Configuration XML']].map(([id, name]) => `<button class="tab${view.tab === id ? ' active' : ''}" role="tab" aria-selected="${view.tab === id}" data-action="tab-${id}" data-focus="tab-${id}">${name}</button>`).join('')}</nav>`;
  }
  function toolbar(): string {
    const files = document?.uri.split('/');
    return `<div class="canvas-toolbar real-document-toolbar"><span class="hint" title="${escape(document?.uri)}">${icon('code')} ${escape(files?.at(-1) ?? 'Loading document…')}${document?.dirty ? ' • Unsaved' : ''}</span>${button('save', 'Save', undefined, !document)}${iconButton('undo', 'Undo document edit', 'undo', !document)}${iconButton('redo', 'Redo document edit', 'redo', !document)}${button('newFlow', 'New flow', 'plus', !canEdit())}<span class="toolbar-separator"></span>${iconButton('zoomOut', 'Zoom out', 'minus', view.zoom <= 60)}<span class="zoom-value">${view.zoom}%</span>${iconButton('zoomIn', 'Zoom in', 'plus', view.zoom >= 160)}</div>`;
  }
  function renderNode(node: Component): string {
    return `<button class="node${view.selectedId === node.id ? ' selected' : ''}" data-node="${escape(node.id)}" data-action="select" draggable="${node.editable}" data-focus="node-${escape(node.id)}" aria-label="${node.kind === 'listener' ? 'HTTP Listener' : 'Logger'}: ${escape(node.displayName)}" aria-pressed="${view.selectedId === node.id}" title="Select to edit ${escape(node.displayName)}. Shift+F10 for actions."><span class="node-symbol ${node.kind}">${icon(node.kind === 'listener' ? 'globe' : 'logger')}</span><span class="node-label">${escape(node.displayName || (node.kind === 'listener' ? 'HTTP Listener' : 'Logger'))}</span></button>`;
  }
  function renderFlow(): string {
    const f = flow();
    if (!document) return '<div class="empty-properties"><h2>Opening integration document…</h2></div>';
    if (!document.model.valid) return `${toolbar()}<div class="empty-properties">${icon('warning')}<h2>XML needs attention</h2><p>The source is preserved. Open Configuration XML to fix it; visual editing is paused while XML is invalid.</p>${button('tab-xml', 'Open Configuration XML')}</div>`;
    if (!f) return `${toolbar()}<div class="empty-properties">${icon('code')}<h2>Create your first flow</h2><p>This integration XML is empty. Create a flow, then add an HTTP Listener and Logger.</p>${button('newFlow', 'Create flow', 'plus', false, 'primary')}</div>`;
    return `${toolbar()}${document.model.flows.length > 1 ? `<div class="real-flow-selector"><label for="real-flow-select">Flow</label><select id="real-flow-select" data-control="flow" data-focus="flow-selector">${document.model.flows.map(x => `<option value="${escape(x.id)}" ${x.id === f.id ? 'selected' : ''}>${escape(x.name)}${x.editable ? '' : ' · unsupported'}</option>`).join('')}</select></div>` : ''}
      <div class="flow-scroll" data-scroll="canvas"><div class="flow-card${f.kind === 'sub-flow' ? ' subflow-card' : ''}" style="zoom:${view.zoom / 100}" role="group" aria-label="${escape(f.name)} integration flow"><div class="flow-heading">${icon('code')}<span class="flow-name">${escape(f.name)}</span><span class="fixture-tag">${f.editable ? 'XML document' : 'Preserved · read only'}</span>${iconButton('renameFlow', 'Rename flow', 'edit', !f.editable || !canEdit())}</div>
      ${!f.editable ? `<div class="real-unsupported"><strong>Unsupported constructs are preserved.</strong><p>${escape(f.unsupported.join(', '))}</p><p>Use Configuration XML. Visual changes to this flow are disabled.</p></div>` : ''}
      <div class="flow-lanes"><div class="source-lane${f.kind === 'sub-flow' ? ' subflow-source' : ''}" data-drop="source" role="group" aria-label="Source lane"><span class="lane-caption">Source</span>${f.source ? renderNode(f.source) : button('insert-listener', 'Add Listener', 'plus', f.kind === 'sub-flow' || !f.editable || !canEdit(), 'drop-placeholder', 'aria-label="Insert HTTP Listener in source lane"')}<span class="response-caption">Response</span></div><div class="processor-lane" data-drop="processor" role="group" aria-label="Processor lane">${f.processors.map((node, index) => `<span class="connector-arrow" aria-hidden="true"></span><span data-processor-index="${index}">${renderNode(node)}</span>`).join('')}${!f.processors.length ? button('insert-logger', 'Add Logger', 'plus', !f.editable || !canEdit(), 'drop-placeholder', 'aria-label="Insert Logger in processor lane"') : ''}</div>${f.editable && f.source && f.processors.length ? '<svg class="response-path" aria-hidden="true"><path/></svg>' : ''}</div>
      <div class="error-region">${f.editable ? f.kind === 'sub-flow' ? 'Subflow · Logger processors' : 'HTTP Listener and core Logger · response is configured on the Listener' : 'Unsupported source remains unchanged'}</div></div></div>`;
  }
  function renderPalette(): string {
    const f = flow();
    const items = [{ kind: 'listener', group: 'HTTP', name: 'Listener', glyph: 'globe' }, { kind: 'request', group: 'HTTP', name: 'Request', glyph: 'request' }, { kind: 'logger', group: 'Core', name: 'Logger', glyph: 'logger' }];
    return `<aside class="palette" aria-label="ZERNOVA Palette"><div class="pane-title">ZERNOVA Palette${icon('plus')}</div><div class="search-wrap">${icon('search')}<input type="search" data-control="palette-search" data-focus="palette-search" placeholder="Search components…" aria-label="Search components" value="${escape(query)}"></div><div class="palette-content" data-scroll="palette">${['HTTP', 'Core'].map(group => `<section class="palette-group" aria-label="${group} components"><div class="group-title">${group}</div>${items.filter(x => x.group === group && `${x.group} ${x.name}`.toLowerCase().includes(query.toLowerCase())).map(x => `<button class="palette-item" data-palette="${x.kind}" data-action="insert-${x.kind}" data-focus="palette-${x.kind}" ${x.kind === 'request' || !canEdit() || !f?.editable ? 'disabled' : 'draggable="true"'} title="${x.kind === 'request' ? 'HTTP Request is outside this milestone' : 'Add to its valid flow lane'}"><span class="node-symbol ${x.kind}">${icon(x.glyph)}</span>${x.name}${x.kind === 'request' ? '<span class="future-label">FUTURE</span>' : ''}</button>`).join('')}</section>`).join('')}</div><div class="palette-footnote">Drag or press Enter to add a component.<br>Execution uses the selected runtime.</div></aside>`;
  }
  function renderLower(): string {
    return `<section class="lower-workarea" aria-label="Properties work area"><nav class="lower-tabs" role="tablist" aria-label="Lower work area">${[['properties', 'Properties'], ['problems', 'Problems'], ['console', 'Console'], ['outline', 'Outline']].map(([id, name]) => `<button class="tab${view.lower === id ? ' active' : ''}" role="tab" aria-selected="${view.lower === id}" data-action="lower-${id}" data-focus="lower-${id}">${id === 'properties' ? icon('properties') : ''}${name}${id === 'problems' && document?.model.diagnostics.length ? ` (${document.model.diagnostics.length})` : ''}</button>`).join('')}<span class="bar-spacer"></span><span class="fixture-tag">${document?.dirty ? 'Unsaved changes' : 'Document saved'}</span></nav><div class="lower-content">${view.lower === 'properties' ? renderProperties() : view.lower === 'problems' ? renderProblems() : view.lower === 'console' ? renderConsole() : renderOutline()}</div></section>`;
  }
  function field(label: string, key: string, value: string, multiline = false): string {
    const node = component();
    const draftKey = `${node?.id}:${key}`;
    const current = drafts.get(draftKey) ?? value;
    const id = `component-${key}`;
    const attrs = `id="${id}" data-field="${key}" data-focus="${id}" ${!node?.editable || !flow()?.editable || !canEdit() ? 'disabled' : ''} maxlength="${['message', 'responseBody', 'responseHeaders'].includes(key) ? 8000 : 512}"`;
    return `<div class="form-row"><label for="${id}">${label}</label><div>${multiline ? `<textarea ${attrs} rows="2" class="expression-input">${escape(current)}</textarea>` : `<input ${attrs} value="${escape(current)}">`}</div></div>`;
  }
  function renderProperties(): string {
    const node = component();
    if (!node) return `<div class="empty-properties">${icon('settings')}<h2>Select a component to view its properties</h2><p>Choose a Listener or Logger in your flow. Values edit this integration XML document.</p></div>`;
    const sections = node.kind === 'listener' ? ['General', 'Responses', 'Help'] : ['General', 'Help'];
    if (!sections.includes(view.section)) view.section = 'General';
    const locked = !node.editable || !flow()?.editable || !canEdit();
    const index = flow()?.processors.findIndex(n => n.id === node.id) ?? -1;
    const actions = node.kind === 'logger' ? `${iconButton('duplicate', 'Duplicate Logger', 'copy', locked)}${iconButton('moveLeft', 'Move Logger earlier', 'arrowLeft', locked || index <= 0)}${iconButton('moveRight', 'Move Logger later', 'arrowRight', locked || index >= (flow()?.processors.length ?? 0) - 1)}` : '';
    return `<section class="property-main" aria-label="${node.kind === 'listener' ? 'HTTP Listener' : 'Logger'} properties"><div class="property-heading">${icon(node.kind === 'listener' ? 'globe' : 'logger')}<span>${node.kind === 'listener' ? 'HTTP Listener' : 'Logger'}</span><span class="fixture-tag">${locked ? 'Preserved · read only' : 'Real XML properties'}</span>${actions}${iconButton('deleteNode', node.kind === 'listener' ? 'Remove Listener' : 'Delete Logger', 'trash', locked)}</div><div class="property-detail"><nav class="section-nav" aria-label="Property sections">${sections.map(s => `<button class="${s === view.section ? 'active' : ''}" data-action="section" data-section="${s}" aria-current="${s === view.section ? 'page' : 'false'}" data-focus="section-${s}">${s}</button>`).join('')}</nav><div class="property-form" data-scroll="properties">${renderFields(node)}</div></div></section>`;
  }
  function renderFields(node: Component): string {
    if (view.section === 'Help') return `<div class="section-placeholder"><h3>${node.kind === 'listener' ? 'HTTP Listener' : 'Core Logger'}</h3><p>These fields edit the real XML document. Save before building; Run / Redeploy will ask how to handle unsaved documents.</p><p>Expressions run only in the bundled ZERNOVA Runtime. Static XML diagnostics cannot prove expression correctness.</p><p>${node.kind === 'listener' ? 'HTTP only. TLS, advanced settings and connection testing are unavailable. Configure the successful response under Responses.' : 'Literal text and DataWeave expressions are supported by the runtime. Open Console → Application logs to read actual Logger output.'}</p></div>`;
    if (node.kind === 'listener' && view.section === 'Responses') return `${field('Success status', 'responseStatus', node.responseStatus)}${field('Response body', 'responseBody', node.responseBody, true)}${field('Response headers', 'responseHeaders', node.responseHeaders, true)}<p class="ui-note">Body and headers accept runtime expressions. For the milestone use <code>#['Hello from ZERNOVA']</code> and <code>#[{'Content-Type': 'text/plain; charset=UTF-8'}]</code>. Error-response and TLS settings remain XML-only where already present; no replacement response processor is added.</p>`;
    if (node.kind === 'listener') return `${field('Display Name', 'displayName', node.displayName)}<div class="form-row"><label for="listener-config">Connector configuration</label><div class="field-group"><select id="listener-config" data-field="configId" data-focus="listener-config" ${!node.editable || !flow()?.editable || !canEdit() ? 'disabled' : ''}><option value="">Select a configuration…</option>${document?.model.configs.map(c => `<option value="${escape(c.id)}" ${c.id === node.configId ? 'selected' : ''}>${escape(c.name)}</option>`).join('')}</select>${iconButton('createConfig', 'Create HTTP Listener configuration', 'plus', !canEdit())}${iconButton('editListenerConfig', 'Edit HTTP Listener configuration', 'edit', !node.configId || !canEdit())}</div></div>${field('Path', 'path', node.path)}${field('Allowed methods', 'allowedMethods', node.allowedMethods)}<p class="ui-note">Methods: ${HTTP_METHODS.join(', ')}. Separate multiple methods with commas. Configure the HTTP response under Responses. A configured listener is not evidence that an endpoint is running.</p>`;
    return `${field('Display Name', 'displayName', node.displayName)}${field('Message', 'message', node.message, true)}<div class="form-row"><label for="logger-level">Level</label><select id="logger-level" data-field="level" data-focus="logger-level" ${!node.editable || !flow()?.editable || !canEdit() ? 'disabled' : ''}>${LOG_LEVELS.map(s => `<option ${node.level === s ? 'selected' : ''}>${s}</option>`).join('')}</select></div>${field('Category', 'category', node.category)}<p class="ui-note">Logger executes inside the runtime. Example: <code>#['ZERNOVA_TEST ' ++ (attributes.queryParams.testId default 'missing')]</code>.</p>`;
  }
  function renderProblems(): string {
    const errors = document?.model.diagnostics ?? [];
    return `<div class="lower-placeholder" data-scroll="problems"><h3>Document diagnostics</h3>${errors.length ? errors.map(d => `<div class="real-problem">${icon('warning')}<span>${escape(d.message)}${'line' in d ? ` · line ${escape(d.line)}` : ''}</span></div>`).join('') : '<p>No static XML issues found. Expressions and dependencies are validated by the real build/runtime.</p>'}${runtime.lastError ? `<h3>Last runtime/build failure</h3><pre class="real-error">${escape(runtime.lastError)}</pre>` : ''}</div>`;
  }
  function renderConsole(): string {
    const busy = !!runtime.busy;
    const log = view.consoleTab === 'build' ? display(runtime.buildOutput ?? '') : view.consoleTab === 'logs' ? display(runtime.applicationLogs ?? '') : JSON.stringify(runtime, (key, value) => ['buildOutput', 'applicationLogs'].includes(key) ? undefined : value, 2);
    return `<section class="real-console" aria-label="Real runtime and build output"><div class="real-console-actions">${button('runtime-build', 'Build', 'code', busy)}${button('runtime-run', 'Run / Redeploy', 'logger', busy, 'primary')}${button('runtime-status', 'Status', undefined, busy)}${button('runtime-logs', 'Refresh logs', undefined, busy)}${button('runtime-stopApplication', 'Undeploy', undefined, busy)}${button('runtime-stopRuntime', 'Stop runtime', undefined, busy)}${button('runtime-cancel', 'Cancel', undefined, !busy)}</div><nav class="real-output-tabs" role="tablist" aria-label="Output source">${[['build', 'Build output'], ['logs', 'Application logs'], ['status', 'Runtime status']].map(([id, name]) => `<button class="tab${view.consoleTab === id ? ' active' : ''}" role="tab" aria-selected="${view.consoleTab === id}" data-action="console-${id}" data-focus="console-${id}">${name}</button>`).join('')}</nav><pre class="real-output" data-scroll="console-${view.consoleTab}" tabindex="0" aria-label="${view.consoleTab === 'logs' ? 'Application runtime logs' : view.consoleTab === 'build' ? 'Actual build output' : 'Runtime status JSON'}">${escape(log || (view.consoleTab === 'logs' ? 'No application logs loaded. Run the application, send your request, then click Refresh logs.' : 'No operation output yet.'))}</pre></section>`;
  }
  function renderOutline(): string {
    return `<div class="lower-placeholder" data-scroll="outline"><h3>${escape(document?.projectName ?? 'Project')}</h3>${document?.model.flows.map(f => `<h4>${escape(f.name)}${f.editable ? '' : ' · read only'}</h4>${[...(f.source ? [f.source] : []), ...f.processors].map(n => `<button class="outline-row" data-action="outline-select" data-flow="${escape(f.id)}" data-node="${escape(n.id)}">${icon(n.kind === 'listener' ? 'globe' : 'logger')}${escape(n.displayName)}</button>`).join('')}`).join('') ?? ''}</div>`;
  }
  function renderGlobals(): string {
    const configs = document?.model.configs ?? [];
    const selected = configs.find(c => c.id === view.configId);
    return `<div class="globals"><div class="globals-toolbar">${button('createConfig', 'Create', 'plus', !canEdit())}${button('editConfig', 'Edit', 'edit', !selected?.editable || !canEdit())}${button('deleteConfig', 'Delete', 'trash', !selected?.editable || !canEdit())}<div class="globals-search"><input type="search" data-control="global-search" data-focus="global-search" aria-label="Search global configurations" placeholder="Search configurations…" value="${escape(globalQuery)}"></div></div><table class="global-table" aria-label="HTTP Listener configurations"><thead><tr><th scope="col">Type</th><th scope="col">Name</th><th scope="col">Description</th></tr></thead><tbody>${configs.filter(c => c.name.toLowerCase().includes(globalQuery.toLowerCase())).map(c => `<tr class="${c.id === view.configId ? 'selected' : ''}" data-config="${escape(c.id)}" data-action="selectConfig" tabindex="0" data-focus="config-${escape(c.id)}" aria-selected="${c.id === view.configId}"><td><span class="type-cell"><span class="node-symbol logger">${icon('globe')}</span>HTTP</span></td><td>${escape(c.name)}</td><td>${c.editable ? 'HTTP Listener configuration' : 'Preserved · unsupported settings'}</td></tr>`).join('')}</tbody></table>${!configs.length ? '<div class="no-results">No HTTP configurations. Create one and select it from your Listener.</div>' : ''}<p class="ui-note">Changes update this XML document. Referenced configurations cannot be deleted; cross-file references are checked before renaming.</p></div>`;
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
    configDialog = { id: existing?.id, baseVersion: document?.version ?? -1, values: existing ? { name: existing.name, protocol: 'HTTP', host: existing.host, port: existing.port, basePath: existing.basePath } : { name: `HTTP_Listener_config${suffix === 1 ? '' : '_' + suffix}`, protocol: 'HTTP', host: '127.0.0.1', port: '18081', basePath: '' }, error: '' };
    render(); focusDialog();
  }
  function focusDialog(): void { window.requestAnimationFrame(() => root.querySelector<HTMLElement>('.dialog input,.dialog button:not(:disabled)')?.focus()); }
  function closeDialog(): void { configDialog = undefined; flowDialog = undefined; deleteDialog = undefined; render(); restoreFocus({ key: returnFocus, start: null, end: null }); }
  function renderDialog(): string {
    if (flowDialog) return `<div class="dialog-backdrop"><section class="dialog small-dialog" role="dialog" aria-modal="true" aria-labelledby="flow-dialog-title"><div class="dialog-title"><span id="flow-dialog-title">${flowDialog.id ? 'Rename flow' : 'Create flow'}</span>${iconButton('cancelDialog', 'Close flow editor', 'close')}</div><div class="dialog-body"><label for="flow-name-input">Flow name</label><input id="flow-name-input" data-flow-name data-focus="flow-name" value="${escape(flowDialog.name)}" maxlength="100">${flowDialog.error ? `<p class="form-error" role="alert">${escape(flowDialog.error)}</p>` : ''}<p class="ui-note">Use a unique name. A new flow starts empty.</p></div><div class="dialog-actions"><span class="bar-spacer"></span>${button('cancelDialog', 'Cancel')}${button('saveFlow', flowDialog.id ? 'Rename' : 'Create flow', undefined, false, 'primary')}</div></section></div>`;
    if (deleteDialog) return `<div class="dialog-backdrop"><section class="dialog small-dialog" role="dialog" aria-modal="true" aria-labelledby="delete-title"><div class="dialog-title" id="delete-title">Delete HTTP configuration</div><div class="dialog-body"><p>Delete <strong>${escape(deleteDialog.name)}</strong> from this XML?</p><p>Deletion is refused if a project XML file references the configuration.</p>${deleteDialog.error ? `<p class="form-error" role="alert">${escape(deleteDialog.error)}</p>` : ''}</div><div class="dialog-actions"><span class="bar-spacer"></span>${button('cancelDialog', 'Cancel')}${button('confirmDeleteConfig', 'Delete', undefined, false, 'danger')}</div></section></div>`;
    if (!configDialog) return '';
    const d = configDialog.values;
    const row = (key: keyof typeof d, label: string) => `<div class="form-row"><label for="dialog-${key}">${label}</label><input id="dialog-${key}" data-draft="${key}" data-focus="dialog-${key}" value="${escape(d[key])}" maxlength="512"></div>`;
    return `<div class="dialog-backdrop"><section class="dialog" role="dialog" aria-modal="true" aria-labelledby="config-dialog-title"><div class="dialog-title"><span id="config-dialog-title">HTTP Listener configuration</span>${iconButton('cancelDialog', 'Close configuration editor', 'close')}</div><nav class="dialog-tabs" role="tablist" aria-label="HTTP configuration sections"><button class="tab active" role="tab" aria-selected="true">General</button><button class="tab" disabled title="TLS is outside this milestone">TLS</button><button class="tab" disabled title="Advanced settings are outside this milestone">Advanced</button></nav><div class="dialog-body" data-scroll="dialog">${row('name', 'Name')}<div class="form-row"><label for="dialog-protocol">Protocol</label><select id="dialog-protocol" data-draft="protocol" data-focus="dialog-protocol"><option>HTTP</option></select></div>${row('host', 'Host')}${row('port', 'Port')}${row('basePath', 'Base path')}${configDialog.error ? `<p class="form-error" role="alert">${escape(configDialog.error)}</p>` : ''}<p class="ui-note">Default local testing uses 127.0.0.1. Static fields may reference application properties; the runtime validates their resolved values.</p></div><div class="dialog-scope">Save applies changes to this document. Use document Save to write the file.</div><footer class="dialog-actions"><button disabled title="An inbound Listener is verified by deployment and an actual HTTP request">Test Connection</button><span class="test-explanation">Inbound Listener · use a real request</span><span class="bar-spacer"></span>${button('cancelDialog', 'Cancel')}${button('saveDialog', 'Save', undefined, false, 'primary')}</footer></section></div>`;
  }
  function renderMenu(): string {
    const c = component();
    if (!contextMenu || !c) return '';
    const locked = !c.editable || !flow()?.editable || !canEdit();
    return `<div class="context-menu" role="menu" aria-label="Component actions" style="left:${Math.min(contextMenu.x, window.innerWidth - 230)}px;top:${Math.min(contextMenu.y, window.innerHeight - 220)}px">${button('showProperties', 'Open Properties', 'properties', false, '', 'role="menuitem"')}${c.kind === 'logger' ? button('duplicate', 'Duplicate Logger', 'copy', locked, '', 'role="menuitem"') + button('moveLeft', 'Move earlier', 'arrowLeft', locked, '', 'role="menuitem"') + button('moveRight', 'Move later', 'arrowRight', locked, '', 'role="menuitem"') : ''}${button('deleteNode', c.kind === 'logger' ? 'Delete Logger' : 'Remove Listener', 'trash', locked, '', 'role="menuitem"')}</div>`;
  }
  function setView(fn: () => void): void { fn(); persist(); render(); }
  root.addEventListener('click', event => {
    const target = event.target as HTMLElement;
    const el = target.closest<HTMLElement>('[data-action]');
    if (!el || (el as HTMLButtonElement).disabled) { if (contextMenu) { contextMenu = undefined; render(); } return; }
    const name = el.dataset.action!;
    if (name.startsWith('runtime-')) { action(name.slice(8)); return; }
    if (name.startsWith('tab-')) { setView(() => { view.tab = name.slice(4) as EditTab; }); return; }
    if (name.startsWith('lower-')) { setView(() => { view.lower = name.slice(6) as LowerTab; }); return; }
    if (name.startsWith('console-')) { setView(() => { view.consoleTab = name.slice(8) as ViewState['consoleTab']; }); return; }
    const f = flow(), c = component();
    switch (name) {
      case 'save': case 'undo': case 'redo': action(name); break;
      case 'select': case 'outline-select': setView(() => { if (el.dataset.flow) view.flowId = el.dataset.flow; view.selectedId = el.dataset.node!; view.lower = 'properties'; view.section = 'General'; view.tab = 'flow'; contextMenu = undefined; }); break;
      case 'section': setView(() => { view.section = el.dataset.section!; }); break;
      case 'zoomIn': setView(() => { view.zoom = Math.min(160, view.zoom + 10); }); break;
      case 'zoomOut': setView(() => { view.zoom = Math.max(60, view.zoom - 10); }); break;
      case 'newFlow': case 'renameFlow': returnFocus = focusSnapshot()?.key ?? ''; flowDialog = { id: name === 'renameFlow' ? f?.id : undefined, baseVersion: document?.version ?? -1, name: name === 'renameFlow' ? f?.name ?? '' : 'helloFlow', error: '' }; render(); focusDialog(); break;
      case 'saveFlow': if (flowDialog) {
        if (!dialogIsCurrent(flowDialog)) break;
        if (!/^[A-Za-z_][A-Za-z0-9_.-]{0,99}$/.test(flowDialog.name) || document?.model.flows.some(x => x.id !== flowDialog?.id && x.name === flowDialog?.name)) { flowDialog.error = 'Use a unique flow name beginning with a letter or underscore.'; render(); break; }
        const op: XmlOperation = flowDialog.id ? { kind: 'renameFlow', flowId: flowDialog.id, name: flowDialog.name } : { kind: 'createFlow', name: flowDialog.name };
        closeDialog(); edit(op); break;
      } break;
      case 'insert-listener': case 'insert-logger': if (f) { if (name === 'insert-listener' && f.source) { announce('Only one source is allowed in a flow.', true); break; } edit({ kind: 'insert', flowId: f.id, component: name === 'insert-listener' ? 'listener' : 'logger' }); } break;
      case 'duplicate': if (c?.kind === 'logger') { contextMenu = undefined; edit({ kind: 'duplicateLogger', id: c.id }); } break;
      case 'moveLeft': case 'moveRight': if (c?.kind === 'logger') { contextMenu = undefined; edit({ kind: 'moveLogger', id: c.id, direction: name === 'moveLeft' ? -1 : 1 }); } break;
      case 'deleteNode': if (c) { contextMenu = undefined; edit({ kind: 'deleteComponent', id: c.id }); } break;
      case 'showProperties': contextMenu = undefined; setView(() => { view.lower = 'properties'; }); break;
      case 'createConfig': openConfig(); break;
      case 'editConfig': openConfig(view.configId ?? undefined); break;
      case 'editListenerConfig': openConfig(f?.source?.configId); break;
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
    const el = event.target as HTMLInputElement | HTMLTextAreaElement;
    if (el.dataset.control === 'xml') { xmlDraft = el.value; if (pending?.operation || queue.length) { xmlConflict = true; requestedAction = undefined; } persist(); sendNext(); return; }
    if (el.dataset.control === 'palette-search') { query = el.value; render(); return; }
    if (el.dataset.control === 'global-search') { globalQuery = el.value; render(); return; }
    if (el.hasAttribute('data-flow-name') && flowDialog) { flowDialog.name = el.value; return; }
    if (el.dataset.draft && configDialog && ['name', 'host', 'port', 'basePath'].includes(el.dataset.draft)) { configDialog.values[el.dataset.draft as 'name' | 'host' | 'port' | 'basePath'] = el.value; return; }
    if (el.dataset.field && el.tagName !== 'SELECT') {
      const c = component(); if (!c) return;
      const key = `${c.id}:${el.dataset.field}`; drafts.set(key, el.value);
      edit({ kind: 'updateComponent', id: c.id, values: { [el.dataset.field]: el.value } }, key, el.value);
    }
  });
  root.addEventListener('change', event => {
    const el = event.target as HTMLSelectElement;
    if (el.tagName !== 'SELECT') return;
    if (el.dataset.control === 'theme') { setView(() => { view.theme = el.value as ViewState['theme']; }); return; }
    if (el.dataset.control === 'flow') { setView(() => { view.flowId = el.value; view.selectedId = null; }); return; }
    const c = component();
    if (el.dataset.field && c) edit({ kind: 'updateComponent', id: c.id, values: { [el.dataset.field]: el.value } });
  });
  root.addEventListener('dblclick', event => { const row = (event.target as HTMLElement).closest<HTMLElement>('[data-config]'); if (row) openConfig(row.dataset.config); });
  root.addEventListener('contextmenu', event => {
    const node = (event.target as HTMLElement).closest<HTMLElement>('[data-node]'); if (!node) return;
    event.preventDefault(); view.selectedId = node.dataset.node!; contextMenu = { id: view.selectedId, x: event.clientX, y: event.clientY }; render();
    root.querySelector<HTMLElement>('.context-menu button:not(:disabled)')?.focus();
  });
  root.addEventListener('keydown', event => {
    const target = event.target as HTMLElement;
    if (event.key === 'Escape') { if (configDialog || flowDialog || deleteDialog) closeDialog(); else if (contextMenu) { contextMenu = undefined; render(); } return; }
    if ((configDialog || flowDialog || deleteDialog) && event.key === 'Tab') {
      const controls = [...root.querySelectorAll<HTMLElement>('.dialog button:not(:disabled),.dialog input,.dialog select')].filter(x => x.offsetParent !== null);
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
    if (row && ['Enter', ' '].includes(event.key)) { event.preventDefault(); view.configId = row.dataset.config!; if (event.key === 'Enter') openConfig(view.configId); else render(); return; }
    const node = target.closest<HTMLElement>('[data-node]');
    if (node && event.shiftKey && event.key === 'F10') { event.preventDefault(); view.selectedId = node.dataset.node!; const rect = node.getBoundingClientRect(); contextMenu = { id: view.selectedId, x: rect.left + 20, y: rect.bottom }; render(); root.querySelector<HTMLElement>('.context-menu button')?.focus(); return; }
    if (['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName)) return;
    const c = component();
    if (event.key === 'Delete' && c) { event.preventDefault(); edit({ kind: 'deleteComponent', id: c.id }); return; }
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'd' && c?.kind === 'logger') { event.preventDefault(); edit({ kind: 'duplicateLogger', id: c.id }); return; }
    if (event.altKey && ['ArrowLeft', 'ArrowRight'].includes(event.key) && c?.kind === 'logger') { event.preventDefault(); edit({ kind: 'moveLogger', id: c.id, direction: event.key === 'ArrowLeft' ? -1 : 1 }); return; }
    const tab = target.closest<HTMLElement>('[role="tab"]');
    if (tab && ['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) {
      event.preventDefault(); const items = [...tab.parentElement!.querySelectorAll<HTMLButtonElement>('[role="tab"]:not(:disabled)')]; const index = items.indexOf(tab as HTMLButtonElement); const next = event.key === 'Home' ? 0 : event.key === 'End' ? items.length - 1 : (index + (event.key === 'ArrowRight' ? 1 : -1) + items.length) % items.length; items[next]?.focus(); items[next]?.click();
    }
  });
  root.addEventListener('dragstart', event => { const el = (event.target as HTMLElement).closest<HTMLElement>('[draggable="true"]'); if (!el) return; drag = el.dataset.palette ? { kind: el.dataset.palette as 'listener' | 'logger' } : { id: el.dataset.node }; event.dataTransfer?.setData('application/x-zernova-component', JSON.stringify(drag)); });
  root.addEventListener('dragover', event => { const lane = (event.target as HTMLElement).closest<HTMLElement>('[data-drop]'); if (lane && drag) { event.preventDefault(); lane.classList.add('drop-valid'); } });
  root.addEventListener('dragleave', event => (event.target as HTMLElement).closest('[data-drop]')?.classList.remove('drop-valid'));
  root.addEventListener('dragend', () => { drag = undefined; root.querySelectorAll('.drop-valid').forEach(x => x.classList.remove('drop-valid')); });
  root.addEventListener('drop', event => {
    const lane = (event.target as HTMLElement).closest<HTMLElement>('[data-drop]'); const f = flow(); if (!lane || !drag || !f) return; event.preventDefault();
    const item = drag; drag = undefined; root.querySelectorAll('.drop-valid').forEach(x => x.classList.remove('drop-valid'));
    if (item.kind) { if ((item.kind === 'listener') !== (lane.dataset.drop === 'source')) { announce(item.kind === 'listener' ? 'HTTP Listener belongs in the Source lane.' : 'Logger belongs in the Processor lane.', true); return; } edit({ kind: 'insert', flowId: f.id, component: item.kind }); }
    else if (item.id && lane.dataset.drop === 'processor') { const from = f.processors.findIndex(x => x.id === item.id); const target = (event.target as HTMLElement).closest<HTMLElement>('[data-processor-index]'); const to = target ? Number(target.dataset.processorIndex) : f.processors.length - 1; if (from >= 0 && from !== to) for (let i = 0; i < Math.abs(to - from); i++) edit({ kind: 'moveLogger', id: item.id, direction: to < from ? -1 : 1 }); }
    else announce('A component can only move within its supported lane.', true);
  });
  root.addEventListener('pointerdown', event => { const el = (event.target as HTMLElement).closest<HTMLElement>('[data-resize]'); if (!el) return; event.preventDefault(); const key = el.dataset.resize as 'paletteWidth' | 'propertiesHeight'; resize = { key, start: key === 'propertiesHeight' ? event.clientY : event.clientX, value: view[key] }; el.setPointerCapture(event.pointerId); });
  root.addEventListener('pointermove', event => { if (!resize) return; const change = (resize.key === 'propertiesHeight' ? event.clientY : event.clientX) - resize.start; view[resize.key] = Math.max(resize.key === 'paletteWidth' ? 190 : 200, Math.min(resize.key === 'paletteWidth' ? 380 : 620, resize.value - change)); root.querySelector<HTMLElement>('.studio')?.style.setProperty(resize.key === 'paletteWidth' ? '--palette-width' : '--properties-height', `${view[resize.key]}px`); });
  root.addEventListener('pointerup', () => { if (resize) { resize = undefined; persist(); render(); } });
  window.addEventListener('message', event => {
    const message: unknown = event.data;
    if (!object(message) || typeof message.type !== 'string') return;
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
      pending = undefined; sendNext(); render();
    } else if (message.type === 'realError') {
      // Runtime errors are unscoped and may arrive during a document edit.
      // Only the matching mutation rejection may clear that edit's queue.
      if (typeof message.requestId === 'string' && pending?.requestId === message.requestId) {
        if (pending.text !== undefined) { xmlDraft ??= pending.text; xmlConflict = true; view.tab = 'xml'; }
        else retainFieldDrafts();
        pending = undefined; queue.length = 0; requestedAction = undefined;
      }
      announce(typeof message.message === 'string' ? message.message : 'The document operation was rejected.', true); persist();
    } else if (message.type === 'runtimeState' && object(message.state)) { runtime = message.state as RuntimeState; render(); }
    else if (message.type === 'hostTheme') render();
  });
  function selectHostId(id: string): void {
    const owner = document?.model.flows.find(f => f.id === id || f.source?.id === id || f.processors.some(p => p.id === id));
    if (owner) { view.flowId = owner.id; view.selectedId = owner.id === id ? null : id; }
    if (document?.model.configs.some(c => c.id === id)) view.configId = id;
  }
  window.addEventListener('pagehide', () => { persist(); window.clearTimeout(toastTimer); if (!applyingFromHost) sendNext(); });
  render(); vscode.postMessage({ type: 'ready' });
}
