import './styles.css';
import { applyAction, cloneState, createInitialState, DATAWEAVE_INPUT, DATAWEAVE_OUTPUT, DATAWEAVE_SOURCE, DEFAULT_LAYOUT, getComponent, HTTP_METHODS, LOG_LEVELS, PreviewHistory, sanitizeState, serializeXml, validateComponent, validateHttpConfig } from '../shared/model';
import type { Component, HttpConfig, PreviewAction, PreviewLayout, PreviewState, Scenario, ValidationErrors } from '../shared/model';
import { isScenario } from '../shared/protocol';
import { icon } from './icons';
import { mountReal } from './real';

declare const acquireVsCodeApi: () => { postMessage(message: unknown): void; setState(state: unknown): void; getState(): unknown };
declare global { interface Window { __ZERNOVA_INITIAL__: unknown; __ZERNOVA_ASSETS__: { logo: string }; __ZERNOVA_HOST_THEME__: string; __ZERNOVA_REAL__?: boolean } }
const vscode = acquireVsCodeApi();
const root = document.getElementById('app')!;
if (window.__ZERNOVA_REAL__) {
  mountReal(root, vscode);
} else {
let state = sanitizeState(window.__ZERNOVA_INITIAL__);
const history = new PreviewHistory();
let paletteQuery = '';
let globalQuery = '';
let metadataQuery = '';
let currentScenario: Scenario = 'workbench';
let toast: { text: string; error: boolean } | null = null;
let toastTimer = 0;
let saveTimer = 0;
let menu: { id: string; x: number; y: number } | null = null;
let infoOpen = false;
let confirmDialog: { kind: 'deleteConfig' | 'reset'; id?: string } | null = null;
let dialog: { draft: HttpConfig; mode: 'create' | 'edit'; tab: 'general' | 'tls' | 'advanced'; attempted: boolean } | null = null;
let returnFocus = '';
let activeEditGroup = '';
let dragPayload: { kind?: 'listener' | 'logger'; id?: string } | null = null;
let dragRejection: string | null = null;
let resizing: { key: keyof PreviewLayout; start: number; value: number; before: PreviewState } | null = null;
const e = (value: unknown): string => String(value ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]!));
const isInput = (el: Element | null): el is HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement => !!el && ['INPUT', 'TEXTAREA', 'SELECT'].includes(el.tagName);
const button = (action: string, title: string, glyph: string, disabled = false, extra = '') => `<button class="icon-button" type="button" data-action="${action}" aria-label="${e(title)}" title="${e(title)}" ${disabled ? 'disabled' : ''} ${extra}>${icon(glyph)}</button>`;
const split = (key: keyof PreviewLayout, orientation: 'vertical' | 'horizontal', label: string) => `<div class="resize-${orientation === 'vertical' ? 'v' : 'h'}" data-resize="${key}" role="separator" tabindex="0" aria-orientation="${orientation}" aria-label="${label}" aria-valuenow="${state.layout[key]}" title="Drag to resize. Arrow keys resize; Home restores the default." data-focus="split-${key}"></div>`;
function persist(): void { vscode.setState(state); vscode.postMessage({ type: 'saveState', state }); }
function update(next: PreviewState, record = false, group = ''): void {
  if (record && (!group || group !== activeEditGroup)) history.commit(state, next);
  state = next; activeEditGroup = group; persist(); render();
}
function mutate(fn: (next: PreviewState) => void, record = false, group = ''): void { const next = cloneState(state); fn(next); update(next, record, group); }
function act(action: PreviewAction, record = true): boolean {
  const result = applyAction(state, action);
  if (result.error) { announce(result.error, true); return false; }
  update(result.state, record); return true;
}
function announce(text: string, error = false): void {
  toast = { text, error }; window.clearTimeout(toastTimer); render();
  toastTimer = window.setTimeout(() => { toast = null; render(); }, 5500);
}
function captureFocus(): { key: string; start: number | null; end: number | null } | null {
  const el = document.activeElement as HTMLElement | null;
  const key = el?.dataset.focus;
  if (!key) return null;
  return { key, start: el instanceof HTMLInputElement || el instanceof HTMLTextAreaElement ? el.selectionStart : null, end: el instanceof HTMLInputElement || el instanceof HTMLTextAreaElement ? el.selectionEnd : null };
}
function restoreFocus(saved: ReturnType<typeof captureFocus>): void {
  if (!saved) return;
  const el = root.querySelector<HTMLElement>(`[data-focus="${CSS.escape(saved.key)}"]`);
  el?.focus({ preventScroll: true });
  if (el instanceof HTMLInputElement || el instanceof HTMLTextAreaElement) { try { if (saved.start !== null) el.setSelectionRange(saved.start, saved.end); } catch { /* number/select controls */ } }
}
function render(): void {
  const focus = captureFocus();
  const selected = getComponent(state);
  currentScenario = state.editorTab === 'dataweave' ? 'dataweave' : state.editorTab === 'xml' ? 'xml' : state.editorTab === 'globals' ? 'globals' : selected?.kind === 'listener' ? 'listener' : selected?.kind === 'logger' ? 'logger' : !state.flow.source && !state.flow.processors.length ? 'empty' : 'workbench';
  const scrolls = [...root.querySelectorAll<HTMLElement>('[data-scroll]')].map(el => [el.dataset.scroll!, el.scrollTop, el.scrollLeft] as const);
  document.body.dataset.previewTheme = state.theme;
  document.documentElement.style.setProperty('--logo-url', `url("${window.__ZERNOVA_ASSETS__.logo.replace(/"/g, '%22')}")`);
  const layout = state.layout;
  root.innerHTML = `<main class="studio" aria-label="ZERNOVA Studio design preview" style="--palette-width:${layout.paletteWidth}px;--properties-height:${layout.propertiesHeight}px;--metadata-width:${layout.metadataWidth}px;--dw-input-width:${layout.dwInputWidth}px;--dw-output-width:${layout.dwOutputWidth}px;--dw-source-width:${layout.dwSourceWidth}px">
    ${renderBar()}<div class="workspace"><div class="upper-workarea"><section class="main-editor" aria-label="Integration editor"><div class="editor-content" data-scroll="editor">${renderEditor()}</div>${renderEditorTabs()}</section>${state.editorTab !== 'dataweave' ? `${split('paletteWidth', 'vertical', 'Resize palette')}${renderPalette()}` : ''}</div>
    ${split('propertiesHeight', 'horizontal', 'Resize properties area')}${renderLower()}</div>
    ${dialog ? renderDialog() : ''}${infoOpen ? renderInfo() : ''}${confirmDialog ? renderConfirm() : ''}${menu ? renderMenu() : ''}
    <div class="sr-only" role="status" aria-live="polite">${toast ? e(toast.text) : ''}</div>${toast ? `<div class="toast${toast.error ? ' error' : ''}" data-testid="toast">${icon(toast.error ? 'warning' : 'info')}<span>${e(toast.text)}</span></div>` : ''}
  </main>`;
  for (const [key, top, left] of scrolls) { const el = root.querySelector<HTMLElement>(`[data-scroll="${CSS.escape(key)}"]`); if (el) { el.scrollTop = top; el.scrollLeft = left; } }
  restoreFocus(focus);
  if (dialog || infoOpen || confirmDialog) {
    root.querySelector('.workspace')?.setAttribute('inert', ''); root.querySelector('.preview-bar')?.setAttribute('inert', '');
  }
}
function renderBar(): string {
  const scenarios: [Scenario, string][] = [['workbench', '01 · Workbench'], ['listener', '02 · HTTP Listener'], ['logger', '03 · Logger'], ['globals', '04 · Global Elements'], ['xml', '05 · Configuration XML'], ['dataweave', '06 · DataWeave'], ['empty', 'Empty flow']];
  return `<header class="preview-bar"><span class="brand"><span class="brand-mark" aria-hidden="true"></span>ZERNOVA</span><span class="preview-badge">DESIGN PREVIEW</span><span class="connection">${icon('disconnect')}No runtime connected</span><span class="bar-spacer"></span>
    <label class="sr-only" for="scenario-selector">Preview scenario</label><select id="scenario-selector" class="compact-select" data-control="scenario" data-focus="scenario" aria-label="Preview scenario">${scenarios.map(([id, label]) => `<option value="${id}" ${currentScenario === id ? 'selected' : ''}>${label}</option>`).join('')}</select>
    <label class="sr-only" for="theme-selector">Preview theme</label><select id="theme-selector" class="compact-select theme-select" data-control="theme" data-focus="theme" aria-label="Preview theme">${[['host','Host theme'],['light','Light'],['dark','Dark']].map(([id, label]) => `<option value="${id}" ${state.theme === id ? 'selected' : ''}>${label}</option>`).join('')}</select>
    ${button('resetLayout','Reset preview layout','fit',false,'data-focus="reset-layout"')}${button('resetPreview','Reset preview state','reset',false,'data-focus="reset-preview"')}${button('info','Target runtime information — not connected','info',false,'data-focus="runtime-info"')}</header>`;
}
function renderEditorTabs(): string {
  if (state.editorTab === 'dataweave') return `<nav class="editor-tabs" role="tablist" aria-label="Preview resources"><button class="tab" role="tab" aria-selected="false" data-action="tab-flow" data-focus="tab-flow">${icon('code')}hello.xml</button><button class="tab active" role="tab" aria-selected="true" data-action="tab-dataweave" data-focus="tab-dataweave">${icon('file')}hello.dwl</button></nav>`;
  return `<nav class="editor-tabs" role="tablist" aria-label="Integration views">${[['flow','Message Flow'],['globals','Global Elements'],['xml','Configuration XML']].map(([id,label])=>`<button class="tab${state.editorTab===id?' active':''}" role="tab" aria-selected="${state.editorTab===id}" data-action="tab-${id}" data-focus="tab-${id}">${label}</button>`).join('')}</nav>`;
}
function renderEditor(): string { return state.editorTab === 'globals' ? renderGlobals() : state.editorTab === 'xml' ? renderXml() : state.editorTab === 'dataweave' ? renderDataweave() : renderFlow(); }
function renderNode(node: Component): string {
  const label = node.kind === 'listener' && node.displayName === 'Listener' ? 'HTTP Listener' : node.kind === 'logger' && node.displayName === 'Log request' ? 'Logger' : node.displayName || (node.kind === 'listener' ? 'HTTP Listener' : 'Logger');
  return `<button class="node${state.selectedId===node.id?' selected':''}" data-node="${e(node.id)}" data-action="select" draggable="true" data-focus="node-${e(node.id)}" aria-label="${node.kind === 'listener' ? 'HTTP Listener' : 'Logger'}: ${e(node.displayName || 'Unnamed')}" aria-pressed="${state.selectedId===node.id}" title="${e(node.displayName)} · Select to edit properties. Shift+F10 for actions."><span class="node-symbol ${node.kind}">${icon(node.kind==='listener'?'globe':'logger')}</span><span class="node-label">${e(label)}</span></button>`;
}
function renderFlow(): string {
  return `<div class="canvas-toolbar"><span class="hint">${icon('code')} &nbsp;hello.xml <span class="fixture-tag">· preview fixture</span></span>${button('undo','Undo preview edit','undo',!history.canUndo,'data-focus="undo"')}${button('redo','Redo preview edit','redo',!history.canRedo,'data-focus="redo"')}<span class="toolbar-separator"></span>${button('zoomOut','Zoom out','minus',state.zoom<=60)}<span class="zoom-value">${state.zoom}%</span>${button('zoomIn','Zoom in','plus',state.zoom>=160)}${button('zoomReset','Reset canvas zoom','fit')}</div>
    <div class="flow-scroll" data-action="deselect" data-scroll="canvas"><div class="flow-card" style="zoom:${state.zoom/100}" role="group" aria-label="helloFlow integration fixture"><div class="flow-heading">${button('toggleFlow',state.flow.collapsed?'Expand helloFlow':'Collapse helloFlow',state.flow.collapsed?'chevron':'down',false,'data-focus="flow-collapse"')}<span class="flow-name">${e(state.flow.name)}</span><span class="fixture-tag">Sample flow</span>${button('flowMenu','Flow preview actions','more',false,'data-focus="flow-menu"')}</div>
      ${!state.flow.collapsed ? `<div class="flow-lanes"><div class="source-lane" data-drop="source" role="group" aria-label="Source lane"><span class="lane-caption">Source</span>${state.flow.source ? renderNode(state.flow.source) : `<button class="drop-placeholder" data-action="insert-listener" aria-label="Insert HTTP Listener in source lane">${icon('plus')}Add Listener</button>`}<span class="response-caption">Response</span></div>
      <div class="processor-lane" data-drop="processor" role="group" aria-label="Processor lane">${state.flow.processors.length ? state.flow.processors.map((node,index) => `<span class="connector-arrow" aria-hidden="true"></span><span data-processor-index="${index}">${renderNode(node)}</span>`).join('') : `<button class="drop-placeholder" data-action="insert-logger" aria-label="Insert Logger in processor lane">${icon('plus')}Add Logger</button>`}</div>${state.flow.source && state.flow.processors.length ? '<div class="response-path" aria-hidden="true"></div>' : ''}</div>
      <button class="error-toggle" data-action="toggleError" aria-expanded="${state.flow.errorExpanded}" data-focus="error-toggle">${icon(state.flow.errorExpanded?'down':'chevron')}Error handling</button>${state.flow.errorExpanded?'<div class="error-region">Error handling layout preview · no handlers configured or executed.</div>':''}` : ''}
    </div></div>`;
}
function renderPalette(): string {
  const query = paletteQuery.trim().toLowerCase();
  const items = [{kind:'listener',name:'Listener',group:'HTTP',glyph:'globe'},{kind:'request',name:'Request',group:'HTTP',glyph:'request'},{kind:'logger',name:'Logger',group:'Core',glyph:'logger'}];
  const matches = items.filter(item=>`${item.group} ${item.name}`.toLowerCase().includes(query));
  return `<aside class="palette" aria-label="ZERNOVA Palette"><div class="pane-title"><span>ZERNOVA Palette</span>${icon('plus')}</div><div class="search-wrap">${icon('search')}<input type="search" placeholder="Search components…" aria-label="Search components" value="${e(paletteQuery)}" data-control="palette-search" data-focus="palette-search"/></div><div class="palette-content" data-scroll="palette">${['HTTP','Core'].map(group=>{
    const groupItems=matches.filter(item=>item.group===group); return groupItems.length?`<section class="palette-group" aria-label="${group} components"><div class="group-title">${group}</div>${groupItems.map(item=>`<button class="palette-item" ${item.kind==='request'?'disabled aria-disabled="true" title="HTTP Request — available in a later milestone"':`draggable="true" data-palette="${item.kind}" data-action="insert-${item.kind}" title="Add ${item.name} to its valid lane. Drag or press Enter."`} data-focus="palette-${item.kind}"><span class="node-symbol ${item.kind}">${icon(item.glyph)}</span><span>${item.name}</span>${item.kind==='request'?'<span class="future-label">FUTURE</span>':''}</button>`).join('')}</section>`:'';
  }).join('')}${!matches.length?'<div class="no-results" role="status">No components match this search.</div>':''}</div><div class="palette-footnote">Drag or press Enter to add a fixture.<br>HTTP Request is reserved for later.</div></aside>`;
}
function renderLower(): string {
  return `<section class="lower-workarea" aria-label="Properties work area"><nav class="lower-tabs" role="tablist" aria-label="Lower work area">${[['properties','Properties','properties'],['problems','Problems','warning'],['console','Console','code'],['outline','Outline','file']].map(([id,label,glyph])=>`<button class="tab${state.lowerTab===id?' active':''}" role="tab" aria-selected="${state.lowerTab===id}" data-action="lower-${id}" data-focus="lower-${id}">${id==='properties'?icon(glyph):''}${label}</button>`).join('')}<span class="bar-spacer"></span><span class="fixture-tag">UI preview only</span></nav><div class="lower-content">${renderLowerContent()}</div></section>`;
}
function renderLowerContent(): string {
  if (state.lowerTab === 'problems') return '<div class="lower-placeholder"><h3>Problems preview</h3><p>No runtime diagnostics are available. Inline messages validate sample forms only.</p><p>This area is reserved for future mapping to VS Code diagnostics.</p></div>';
  if (state.lowerTab === 'console') return '<div class="lower-placeholder"><h3>Console preview · no execution</h3><p>No runtime is connected. This preview does not run Logger components or produce runtime logs.</p><p>Future runtime output will use a dedicated native VS Code output channel.</p></div>';
  if (state.lowerTab === 'outline') return `<div class="lower-placeholder"><h3>Preview outline</h3><p>${e(state.flow.name)} · ${state.flow.source ? '1 source' : 'no source'} · ${state.flow.processors.length} processor${state.flow.processors.length===1?'':'s'}</p>${[...(state.flow.source?[state.flow.source]:[]),...state.flow.processors].map(node=>`<button class="outline-row" data-action="select" data-node="${node.id}" data-focus="outline-${node.id}">${icon(node.kind==='listener'?'globe':'logger')}${e(node.displayName)}<span class="type-label">${node.kind==='listener'?'HTTP Listener':'Logger'}</span></button>`).join('')}</div>`;
  const node = getComponent(state);
  if (!node || state.editorTab === 'dataweave') return `<div class="empty-properties">${icon('settings')}<h2>Select a component to view its properties</h2><p>${state.editorTab==='dataweave'?'DataWeave is a future layout preview. Its source and sample output are read-only.':'Click a component in the flow to edit its sample properties, or add a fixture from the palette.'}</p></div>`;
  const sections = node.kind==='listener' ? ['General','MIME Type','Redelivery','Responses','Metadata','Notes','Help'] : ['General','Metadata','Notes','Help'];
  const section = sections.includes(state.propertySection) ? state.propertySection : 'General';
  return `<section class="property-main" aria-label="${node.kind==='listener'?'HTTP Listener':'Logger'} properties"><div class="property-heading">${icon(node.kind==='listener'?'globe':'logger')}<span>${node.kind==='listener'?'HTTP Listener':'Logger'}</span><span class="fixture-tag">Sample values</span>${button('componentMenu','Selected component actions','more',false,'data-focus="component-menu"')}</div><div class="property-detail"><nav class="section-nav" aria-label="Property sections">${sections.map(name=>`<button class="${name===section?'active':''}" data-action="section" data-section="${name}" aria-current="${name===section?'page':'false'}" data-focus="section-${name}">${name}</button>`).join('')}</nav><div class="property-form" data-scroll="properties">${renderPropertyFields(node,section)}</div></div></section>${split('metadataWidth','vertical','Resize metadata area')}${renderMetadata()}`;
}
function field(label: string, name: string, value: string, options: { required?: boolean; errors?: ValidationErrors; multiline?: boolean; cls?: string; scope?: string; after?: string } = {}): string {
  const id = `${options.scope??'component'}-${name}`;
  const err = options.errors?.[name];
  const limits:Record<string,number>={message:8000,notes:4000,port:5,displayName:100,name:100,category:200,host:253};
  const attrs=`id="${e(id)}" data-${options.scope==='dialog'?'draft':'field'}="${name}" data-focus="${e(id)}" ${options.required?'aria-required="true"':''} ${err?`aria-invalid="true" aria-describedby="${id}-error"`:''} class="${options.cls??''}" maxlength="${limits[name]??512}"`;
  const input=options.multiline?`<textarea ${attrs} rows="${name==='notes'?5:2}">${e(value)}</textarea>`:`<input ${attrs} value="${e(value)}" ${name==='port'?'inputmode="numeric"':''}/>`;
  return `<div class="form-row"><label for="${e(id)}">${label}${options.required?'<span class="required" aria-hidden="true">*</span>':''}</label><div>${options.after?`<div class="field-group">${input}${options.after}</div>`:input}${err?`<div class="form-error" id="${id}-error">${e(err)}</div>`:''}</div></div>`;
}
function renderPropertyFields(node: Component, section: string): string {
  const errors=validateComponent(node,state.globalConfigs);
  if(section==='Notes') return `${field('Notes','notes',node.notes,{multiline:true})}<p class="ui-note">Notes are saved only in this design preview.</p>`;
  if(section==='Help') return `<div class="section-placeholder"><h3>${node.kind==='listener'?'HTTP Listener':'Logger'} preview</h3><p>Use General to edit the fixture. All values remain in namespaced preview state. No application files or runtime processes are created.</p><p>Keyboard: Enter selects; Shift+F10 opens component actions.${node.kind==='logger'?' Alt+Left/Right reorders; Delete removes the selected Logger.':''}</p><p>Ctrl/Cmd+Z undoes the last preview edit outside text fields.</p></div>`;
  if(section==='Metadata') return '<div class="section-placeholder"><h3>Illustrative metadata</h3><p>The Input / Output trees show synthetic payload, attributes and variables. No connector has resolved or inferred these types.</p></div>';
  if(section!=='General') return `<div class="section-placeholder"><h3>${e(section)}</h3><p>Layout preview · reserved for later implementation.</p><p>No ${e(section.toLowerCase())} behavior is configured or executed in this milestone.</p><button disabled title="Available after runtime integration">Configure ${e(section.toLowerCase())}</button></div>`;
  if(node.kind==='listener') return `${field('Display Name','displayName',node.displayName,{required:true,errors})}<div class="form-row"><label for="listener-config">Connector configuration<span class="required" aria-hidden="true">*</span></label><div><div class="field-group"><select id="listener-config" data-field="configId" data-focus="listener-config" aria-required="true">${state.globalConfigs.map(c=>`<option value="${e(c.id)}" ${node.configId===c.id?'selected':''}>${e(c.name)}</option>`).join('')}</select><button class="small-action" data-action="createConfig" aria-label="Create HTTP Listener configuration" title="Create configuration" data-focus="create-config">${icon('plus')}</button><button class="small-action" data-action="editListenerConfig" aria-label="Edit HTTP Listener configuration" title="Edit configuration" data-focus="edit-listener-config">${icon('edit')}</button></div></div></div>${field('Path','path',node.path,{required:true,errors})}<div class="form-row"><label for="allowed-methods">Allowed methods</label><select id="allowed-methods" data-field="allowedMethods" data-focus="allowed-methods">${HTTP_METHODS.map(m=>`<option ${node.allowedMethods===m?'selected':''}>${m}</option>`).join('')}</select></div><p class="ui-note">Local form validation only. No listener is running.</p>`;
  return `${field('Display Name','displayName',node.displayName,{required:true,errors})}<div class="form-row"><label for="logger-message">Message<span class="required" aria-hidden="true">*</span></label><div><div class="field-group"><button class="expression-button" data-action="toggleExpression" aria-pressed="${node.expressionMode}" aria-label="${node.expressionMode?'Switch to literal editing presentation':'Switch to expression editing presentation'}" title="${node.expressionMode?'Expression presentation — no evaluation':'Literal presentation — no evaluation'}" data-focus="expression-toggle">${node.expressionMode?'ƒx':'Aa'}</button><textarea id="logger-message" class="${node.expressionMode?'expression-input':''}" rows="2" data-field="message" data-focus="logger-message" maxlength="8000" aria-required="true" ${errors.message?'aria-invalid="true" aria-describedby="message-error"':''}>${e(node.message)}</textarea></div>${errors.message?`<div class="form-error" id="message-error">${e(errors.message)}</div>`:''}</div></div><div class="form-row"><label for="logger-level">Level</label><select id="logger-level" data-field="level" data-focus="logger-level">${LOG_LEVELS.map(level=>`<option ${node.level===level?'selected':''}>${level}</option>`).join('')}</select></div>${field('Category','category',node.category)}<p class="ui-note">${node.expressionMode?'Expression':'Literal'} editing presentation only. No DataWeave evaluation or logging.</p>`;
}
function treeGroup(id: string, label: string, type: string, children: string): string {
  return `<details data-tree="${id}" ${state.expandedMetadata.includes(id)?'open':''}><summary data-focus="tree-${id}">${icon('object')}<span>${label}</span><span class="type-label">${type}</span></summary><div class="tree-children">${children}</div></details>`;
}
const leaf=(label:string,type:string)=>`<div class="tree-leaf"><span>${e(label)}</span><span class="type-label">${type}</span></div>`;
function renderMetadata(): string {
  const dir=state.metadataTab;
  return `<aside class="metadata" aria-label="Illustrative metadata"><nav class="metadata-tabs" role="tablist" aria-label="Metadata direction">${['input','output'].map(id=>`<button class="tab${dir===id?' active':''}" data-action="metadata-${id}" role="tab" aria-selected="${dir===id}" data-focus="metadata-${id}">${id==='input'?'Input':'Output'}</button>`).join('')}</nav><div class="metadata-content tree" data-scroll="metadata">${treeGroup(`${dir}-payload`,'Payload','Object',leaf('message','String'))}${treeGroup(`${dir}-attributes`,'Attributes','Object',leaf('method','String')+leaf('uri','String')+treeGroup(`${dir}-headers`,'headers','Object',leaf('content-type','String'))+treeGroup(`${dir}-query`,'queryParams','Object',leaf('name','String')))}${treeGroup(`${dir}-variables`,'Variables','Object',leaf('requestId','String'))}</div><div class="metadata-footnote">Illustrative metadata · not inferred</div></aside>`;
}
function renderGlobals(): string {
  const selected=state.globalConfigs.find(c=>c.id===state.selectedConfigId);
  const rows=state.globalConfigs.filter(c=>`${c.name} HTTP Listener configuration`.toLowerCase().includes(globalQuery.toLowerCase()));
  return `<div class="globals"><div class="globals-toolbar"><button data-action="createConfig" data-focus="global-create">${icon('plus')}Create</button><button data-action="editConfig" ${selected?'':'disabled'} data-focus="global-edit">${icon('edit')}Edit</button><button data-action="deleteConfig" ${selected?'':'disabled'} data-focus="global-delete">${icon('trash')}Delete</button><div class="globals-search"><input type="search" aria-label="Search global configurations" placeholder="Search configurations…" value="${e(globalQuery)}" data-control="globals-search" data-focus="globals-search"/></div></div><table class="global-table" aria-label="Global configuration fixtures"><thead><tr><th scope="col">Type</th><th scope="col">Name</th><th scope="col">Description</th></tr></thead><tbody>${rows.map(c=>`<tr class="${c.id===state.selectedConfigId?'selected':''}" data-config="${e(c.id)}" data-action="selectConfig" tabindex="0" aria-selected="${c.id===state.selectedConfigId}" data-focus="config-${e(c.id)}"><td><span class="type-cell"><span class="node-symbol logger">${icon('globe')}</span>HTTP</span></td><td>${e(c.name)}</td><td>HTTP Listener configuration</td></tr>`).join('')}</tbody></table>${rows.length?'':'<div class="no-results">No global configurations match this search.</div>'}<p class="ui-note">Saved configurations belong to this preview only. Nothing is written to a project.</p></div>`;
}
function highlightXml(line: string): string {
  const rx=/<!--.*?-->|<\/?[\w:-]+|\/?\>|[\w:-]+(?=\s*=)|"[^"]*"/g;
  let out='',last=0;
  for(const match of line.matchAll(rx)){
    const index=match.index!;out+=e(line.slice(last,index));
    const token=match[0];const cls=token.startsWith('<!--')?'comment':token.startsWith('"')?'string':token.startsWith('<')||token.endsWith('>')?'tag':'attribute';
    out+=`<span class="syn-${cls}">${e(token)}</span>`;last=index+token.length;
  }
  return out+e(line.slice(last));
}
function codeLines(text: string, xml=true): string {
  return `<div class="code-lines" tabindex="0" role="textbox" aria-label="${xml?'Read-only preview XML':'Read-only DataWeave source fixture'}" aria-readonly="true" data-focus="${xml?'xml-source':'dw-source'}">${text.split('\n').map((line,index)=>`<div class="code-line"><span class="line-number" aria-hidden="true">${index+1}</span><span class="line-source">${xml?highlightXml(line):e(line).replace(/(output|application\/json|%dw 2\.0)/g,'<span class="syn-keyword">$1</span>')}</span></div>`).join('')}</div>`;
}
function renderXml(): string {
  return `<section class="xml-view" aria-label="Configuration XML preview"><div class="code-bar">${icon('lock')}<span>Preview XML — not saved to a project</span><button data-action="copyXml" data-focus="copy-xml">${icon('copy')}Copy XML</button></div><div class="code-scroll" data-scroll="xml">${codeLines(serializeXml(state))}</div></section>`;
}
const dwSource=DATAWEAVE_SOURCE;
function dwInput(): string {
  return `<section class="dw-pane input-pane" aria-label="DataWeave illustrative input"><div class="pane-title">Input<span class="fixture-tag">Sample JSON</span></div><div class="search-wrap">${icon('search')}<input type="search" placeholder="Search input…" aria-label="Search illustrative input" data-control="metadata-search" data-focus="metadata-search" value="${e(metadataQuery)}"/></div><div class="metadata-content tree" data-scroll="dw-input">${treeGroup('dw-payload','payload','Object',[['firstName','String'],['lastName','String']].filter(([name])=>name.toLowerCase().includes(metadataQuery.toLowerCase())).map(([name,type])=>leaf(name,type)).join('')||'<div class="no-results">No fields match.</div>')}${treeGroup('dw-attributes','attributes','Object',leaf('method','String'))}${treeGroup('dw-variables','variables','Object',leaf('example','String'))}</div><div class="metadata-footnote">Synthetic fixture</div></section>`;
}
function dwOutput(): string {
  return `<section class="dw-pane output-pane" aria-label="DataWeave illustrative output"><div class="pane-title">Output<span class="fixture-tag">Schema fixture</span></div><div class="metadata-content tree">${treeGroup('dw-output','payload','Object',leaf('fullName','String'))}</div><div class="metadata-footnote">Illustrative · not inferred</div></section>`;
}
function dwCode(): string {
  return `<section class="dw-pane source-pane" aria-label="DataWeave source"><div class="pane-title"><span>${icon('code')} DataWeave Source</span><span class="fixture-tag">Read only</span></div><div class="code-scroll" data-scroll="dw-code">${codeLines(dwSource,false)}</div></section>`;
}
function dwSample(): string {
  return `<section class="dw-pane sample-pane" aria-label="Illustrative sample output"><div class="pane-title">Sample Output<span class="fixture-tag">JSON</span></div><pre class="sample-code">{\n  <span class="syn-tag">"fullName"</span>:\n    <span class="syn-string">"${e(DATAWEAVE_OUTPUT.fullName)}"</span>\n}</pre><div class="dw-illustration">Illustrative — not executed</div></section>`;
}
function mapping(): string {
  return `<section class="dw-graphics" aria-label="Read-only graphical mapping fixture"><svg class="mapping-diagram" viewBox="0 0 650 230" role="img" aria-label="Illustrative mapping of firstName and lastName to fullName"><defs><marker id="mapArrow" markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto"><path d="M0 0L6 3L0 6" fill="var(--z-accent)"/></marker></defs><text x="22" y="27">Input</text><text x="474" y="27">Output</text><rect class="box" x="20" y="49" width="182" height="40" rx="3"/><rect class="box" x="20" y="118" width="182" height="40" rx="3"/><rect class="box" x="451" y="82" width="177" height="42" rx="3"/><text x="32" y="73">payload.firstName</text><text x="32" y="142">payload.lastName</text><text x="464" y="108">fullName</text><path class="line" d="M202 69C315 69 340 103 446 103" marker-end="url(#mapArrow)"/><path class="line" d="M202 138C315 138 340 103 446 103" marker-end="url(#mapArrow)"/><text class="small" x="32" y="184">${e(DATAWEAVE_INPUT.firstName)} + space + ${e(DATAWEAVE_INPUT.lastName)}</text><text class="small" x="464" y="148">String · fixture</text></svg><p class="mapping-note">Read-only mapping illustration. No code generation or evaluation.</p></section>`;
}
function renderDataweave(): string {
  return `<section class="dataweave" aria-label="DataWeave future layout"><div class="dw-breadcrumb"><span>src › main › resources › mappings ›</span><strong>hello.dwl</strong><span class="future-label">Future layout — design only</span></div><nav class="dw-tabs" role="tablist" aria-label="DataWeave layout modes">${[['graphics','Graphics'],['trees','Source with Trees'],['source','Source Only']].map(([id,label])=>`<button class="tab${state.dwMode===id?' active':''}" role="tab" aria-selected="${state.dwMode===id}" data-action="dw-${id}" data-focus="dw-${id}">${label}</button>`).join('')}</nav><div class="dw-panes" data-scroll="dw-panes">${state.dwMode==='graphics'?`${mapping()}${split('dwSourceWidth','vertical','Resize DataWeave source')}${dwCode()}${dwSample()}`:state.dwMode==='source'?`${dwInput()}${split('dwInputWidth','vertical','Resize DataWeave input')}${dwCode()}${dwSample()}`:`${dwInput()}${split('dwInputWidth','vertical','Resize DataWeave input')}${dwOutput()}${split('dwOutputWidth','vertical','Resize DataWeave output')}${dwCode()}${split('dwSourceWidth','vertical','Resize DataWeave source')}${dwSample()}`}</div></section>`;
}
function renderDialog(): string {
  if(!dialog)return '';
  const c=dialog.draft; const errors=dialog.attempted?validateHttpConfig(c,state.globalConfigs):{};
  return `<div class="dialog-backdrop"><section class="dialog" role="dialog" aria-modal="true" aria-labelledby="config-dialog-title" aria-describedby="config-dialog-scope"><div class="dialog-title"><span id="config-dialog-title">HTTP Listener configuration</span>${button('cancelDialog','Close configuration editor','close',false,'data-focus="dialog-close"')}</div><nav class="dialog-tabs" role="tablist" aria-label="HTTP configuration sections">${[['general','General'],['tls','TLS'],['advanced','Advanced']].map(([id,label])=>`<button class="tab${dialog!.tab===id?' active':''}" role="tab" aria-selected="${dialog!.tab===id}" data-action="dialog-${id}" data-focus="dialog-tab-${id}">${label}</button>`).join('')}</nav><div class="dialog-body" data-scroll="dialog">${dialog.tab==='general'?`${field('Name','name',c.name,{scope:'dialog',required:true,errors})}<div class="form-row"><label for="dialog-protocol">Protocol</label><select id="dialog-protocol" data-draft="protocol" data-focus="dialog-protocol"><option ${c.protocol==='HTTP'?'selected':''}>HTTP</option><option ${c.protocol==='HTTPS'?'selected':''}>HTTPS</option></select></div>${field('Host','host',c.host,{scope:'dialog',required:true,errors})}${field('Port','port',c.port,{scope:'dialog',required:true,errors})}${field('Base path','basePath',c.basePath,{scope:'dialog',errors})}<p class="ui-note">Validation checks sample form values only.</p>`:`<div class="section-placeholder"><h3>${dialog.tab==='tls'?'TLS':'Advanced'} configuration</h3><p>Reserved layout · no ${dialog.tab==='tls'?'certificates, credentials or TLS configuration':'connection pools, retries or security settings'} are implemented.</p><button disabled title="Available after runtime integration">${dialog.tab==='tls'?'Configure TLS context':'Configure advanced options'}</button></div>`}</div><div class="dialog-scope" id="config-dialog-scope">Preview state only. Save does not create or update project files.</div><footer class="dialog-actions"><button disabled aria-describedby="test-connection-explanation" title="Available after runtime integration">Test Connection</button><span id="test-connection-explanation" class="test-explanation">Available after runtime integration</span><span class="bar-spacer"></span><button data-action="cancelDialog" data-focus="dialog-cancel">Cancel</button><button class="primary" data-action="saveDialog" data-focus="dialog-save">Save</button></footer></section></div>`;
}
function renderInfo(): string {
  return `<div class="dialog-backdrop"><section class="dialog" role="dialog" aria-modal="true" aria-labelledby="runtime-info-title"><div class="dialog-title"><span id="runtime-info-title">Target runtime · not connected</span>${button('closeInfo','Close runtime information','close',false,'data-focus="info-close"')}</div><div class="dialog-body"><p class="form-help">Reported Phase 1 foundation. This UI does not discover, connect to or execute a runtime.</p><dl class="info-grid"><dt>ZERNOVA Runtime</dt><dd>0.1.0-preview.1</dd><dt>Engine</dt><dd>Mule Kernel 4.12.0</dd><dt>Transformation</dt><dd>DataWeave 2.12.0</dd><dt>Project structure</dt><dd>XML / Maven / mule-artifact.json</dd><dt>Connection</dt><dd>No runtime connected</dd></dl><div class="runtime-actions"><button disabled title="Available after runtime integration">Run</button><button disabled title="Available after runtime integration">Debug</button><button disabled title="Available after runtime integration">Deploy</button></div><p class="ui-note">Source-build and customer-redistribution gates remain blocked in the supplied runtime handoff. They are not evaluated or cleared by this UI.</p></div><div class="dialog-actions"><span class="bar-spacer"></span><button class="primary" data-action="closeInfo" data-focus="info-done">Done</button></div></section></div>`;
}
function renderConfirm(): string {
  if(!confirmDialog)return '';
  const config=state.globalConfigs.find(c=>c.id===confirmDialog!.id);
  const referenced=config?.id===state.flow.source?.configId;
  const isReset=confirmDialog.kind==='reset';
  return `<div class="dialog-backdrop"><section class="dialog small-dialog" role="dialog" aria-modal="true" aria-labelledby="confirm-title"><div class="dialog-title"><span id="confirm-title">${isReset?'Reset preview state?':referenced?'Configuration is in use':'Delete preview configuration?'}</span>${button('cancelConfirm','Close confirmation','close',false,'data-focus="confirm-close"')}</div><div class="dialog-body">${isReset?'<p>Restore the sample flow, properties, configurations and layout to their defaults?</p><p>This affects only this extension’s namespaced preview state.</p>':referenced?`<p><strong>${e(config?.name)}</strong> is referenced by ${e(state.flow.source?.displayName)}.</p><p>Choose another saved configuration for the Listener, or remove the Listener, before deleting this configuration. The reference will remain intact.</p>`:`<p>Delete <strong>${e(config?.name)}</strong> from the preview?</p><p>This does not modify a project file. Undo is available.</p>`}</div><div class="dialog-actions"><span class="bar-spacer"></span><button data-action="cancelConfirm" data-focus="confirm-cancel">${referenced&&!isReset?'Close':'Cancel'}</button>${!referenced||isReset?`<button class="${isReset?'primary':'danger'}" data-action="confirmAction" data-focus="confirm-action">${isReset?'Reset Preview':'Delete'}</button>`:''}</div></section></div>`;
}
function renderMenu(): string {
  if(!menu)return '';
  const node=getComponent(state,menu.id);
  const index=state.flow.processors.findIndex(p=>p.id===menu!.id);
  const menuButton=(action:string,label:string,glyph:string,disabled=false,shortcut='')=>`<button role="menuitem" data-action="${action}" ${disabled?'disabled':''} title="${disabled?'Available after runtime integration':label}">${icon(glyph)}${label}${shortcut?`<span class="shortcut">${shortcut}</span>`:''}</button>`;
  return `<div class="context-menu" role="menu" aria-label="${node?`${node.kind==='logger'?'Logger':'Listener'} actions`:'Flow actions'}" style="left:${Math.min(menu.x,window.innerWidth-224)}px;top:${Math.min(menu.y,window.innerHeight-(node?.kind==='logger'?292:204))}px">${node?`${menuButton('menuProperties','Open Properties','properties')}${node.kind==='logger'?`${menuButton('duplicate','Duplicate Logger','copy')}${menuButton('moveLeft','Move earlier','arrowLeft',index<=0,'Alt+←')}${menuButton('moveRight','Move later','arrowRight',index>=state.flow.processors.length-1,'Alt+→')}${menuButton('deleteNode','Delete Logger','trash',false,'Delete')}`:menuButton('deleteNode','Remove Listener','trash')}<div class="menu-separator"></div>${menuButton('unavailable','Run component','logger',true)}${menuButton('unavailable','Debug component','settings',true)}${menuButton('unavailable','Test component','check',true)}`:`${menuButton('insert-listener','Add HTTP Listener','globe',!!state.flow.source)}${menuButton('insert-logger','Add Logger','logger')}${menuButton('populateFlow','Restore sample flow','reset')}<div class="menu-separator"></div>${menuButton('unavailable','Run flow','logger',true)}${menuButton('unavailable','Deploy application','globe',true)}`}</div>`;
}
function focusDialog(): void { window.requestAnimationFrame(()=>root.querySelector<HTMLElement>('.dialog input,.dialog button:not(:disabled)')?.focus()); }
function rememberFocus(): void { returnFocus=captureFocus()?.key??''; }
function closeOverlay(): void { dialog=null;infoOpen=false;confirmDialog=null;render(); if(returnFocus)restoreFocus({key:returnFocus,start:null,end:null}); }
function openConfig(mode:'create'|'edit',id?:string): void {
  rememberFocus();
  const existing=state.globalConfigs.find(c=>c.id===id);
  if(mode==='edit'&&!existing){announce('Select a saved preview configuration.',true);return;}
  dialog={mode,tab:'general',attempted:false,draft:existing?{...existing}:{id:`http-config-${state.nextId}`,name:`HTTP_Listener_config_${state.nextId}`,protocol:'HTTP',host:'127.0.0.1',port:'18081',basePath:''}};
  render();focusDialog();
}
function setScenario(scenario:Scenario): void {
  currentScenario=scenario;menu=null;dialog=null;infoOpen=false;confirmDialog=null;
  act({type:'setScenario',scenario},scenario==='empty');
  if(scenario==='dataweave') mutate(s=>{s.expandedMetadata=[...new Set([...s.expandedMetadata,'dw-payload','dw-output'])];});
}
function showMenu(id:string,x:number,y:number):void { menu={id,x:Math.max(6,x),y:Math.max(6,y)};render();window.requestAnimationFrame(()=>root.querySelector<HTMLButtonElement>('.context-menu button:not(:disabled)')?.focus()); }
async function copyXml():Promise<void>{
  const text=serializeXml(state);
  try{await navigator.clipboard.writeText(text);announce('Preview XML copied. No project file was changed.');}
  catch{const area=document.createElement('textarea');area.value=text;area.style.position='fixed';area.style.left='-10000px';document.body.append(area);area.select();const ok=document.execCommand('copy');area.remove();announce(ok?'Preview XML copied. No project file was changed.':'Clipboard unavailable. Select and copy the read-only XML.',!ok);}
}
root.addEventListener('click',(event)=>{
  const target=event.target as HTMLElement;
  const el=target.closest<HTMLElement>('[data-action]');
  if(!el){if(menu&&!target.closest('.context-menu')){menu=null;render();}return;}
  if((el as HTMLButtonElement).disabled)return;
  const action=el.dataset.action!;
  if(action.startsWith('tab-')){
    const tab=action.slice(4) as PreviewState['editorTab'];
    mutate(s=>{s.editorTab=tab;});currentScenario=tab==='globals'?'globals':tab==='xml'?'xml':tab==='dataweave'?'dataweave':'workbench';render();return;
  }
  if(action.startsWith('lower-')){mutate(s=>{s.lowerTab=action.slice(6) as PreviewState['lowerTab'];});return;}
  if(action.startsWith('metadata-')){mutate(s=>{s.metadataTab=action.slice(9) as PreviewState['metadataTab'];});return;}
  if(action.startsWith('dw-')){mutate(s=>{s.dwMode=action.slice(3) as PreviewState['dwMode'];});return;}
  if(action.startsWith('dialog-')){if(dialog){dialog.tab=action.slice(7) as typeof dialog.tab;render();}return;}
  const selected=getComponent(state);
  switch(action){
    case 'select': currentScenario=getComponent(state,el.dataset.node!)?.kind==='listener'?'listener':'logger';act({type:'select',id:el.dataset.node!},false);mutate(s=>{s.lowerTab='properties';s.propertySection='General';if(s.editorTab!=='flow')s.editorTab='flow';});break;
    case 'deselect': if(!target.closest('.node')&&!target.closest('.flow-heading'))act({type:'select',id:null},false);break;
    case 'section': mutate(s=>{s.propertySection=el.dataset.section!;});break;
    case 'toggleFlow': mutate(s=>{s.flow.collapsed=!s.flow.collapsed;});break;
    case 'toggleError': mutate(s=>{s.flow.errorExpanded=!s.flow.errorExpanded;});break;
    case 'insert-listener': menu=null;if(act({type:'insert',kind:'listener',lane:'source'})){mutate(s=>{s.editorTab='flow';s.flow.collapsed=false;s.lowerTab='properties';s.propertySection='General';});announce('HTTP Listener fixture added. No listener is running.');}break;
    case 'insert-logger': menu=null;if(act({type:'insert',kind:'logger',lane:'processor'})){mutate(s=>{s.editorTab='flow';s.flow.collapsed=false;s.lowerTab='properties';s.propertySection='General';});announce('Logger fixture added. No logging is executed.');}break;
    case 'undo': state=history.undo(state);activeEditGroup='';persist();render();announce('Last preview edit undone.');break;
    case 'redo': state=history.redo(state);activeEditGroup='';persist();render();announce('Preview edit restored.');break;
    case 'zoomOut': mutate(s=>{s.zoom=Math.max(60,s.zoom-10);});break;
    case 'zoomIn': mutate(s=>{s.zoom=Math.min(160,s.zoom+10);});break;
    case 'zoomReset': mutate(s=>{s.zoom=100;});break;
    case 'resetLayout': act({type:'resetLayout'},false);announce('Preview layout restored.');break;
    case 'resetPreview': rememberFocus();confirmDialog={kind:'reset'};render();focusDialog();break;
    case 'info': rememberFocus();infoOpen=true;render();focusDialog();break;
    case 'closeInfo': closeOverlay();break;
    case 'selectConfig': mutate(s=>{s.selectedConfigId=el.dataset.config!;});break;
    case 'createConfig': openConfig('create');break;
    case 'editConfig': openConfig('edit',state.selectedConfigId??undefined);break;
    case 'editListenerConfig': openConfig('edit',state.flow.source?.configId);break;
    case 'deleteConfig': rememberFocus();confirmDialog={kind:'deleteConfig',id:state.selectedConfigId??undefined};render();focusDialog();break;
    case 'cancelDialog': case 'cancelConfirm': closeOverlay();break;
    case 'saveDialog': {
      if(!dialog)break;dialog.attempted=true;
      const errors=validateHttpConfig(dialog.draft,state.globalConfigs);
      if(Object.keys(errors).length){dialog.tab='general';render();root.querySelector<HTMLElement>('.dialog [aria-invalid="true"]')?.focus();break;}
      const draft={...dialog.draft};const result=applyAction(state,{type:'saveConfig',config:draft});
      if(result.error){announce(result.error,true);break;}
      if(dialog.mode==='create')result.state.nextId=Math.max(result.state.nextId,state.nextId+1);
      dialog=null;update(result.state,true);restoreFocus({key:returnFocus,start:null,end:null});announce('Configuration saved in preview state only.');break;
    }
    case 'confirmAction':{
      const confirmation=confirmDialog;confirmDialog=null;
      if(confirmation?.kind==='reset'){state=createInitialState();history.clear();currentScenario='workbench';paletteQuery='';globalQuery='';metadataQuery='';vscode.postMessage({type:'resetPreview'});persist();render();announce('Preview reset to its original sample state.');}
      else if(confirmation?.id){act({type:'deleteConfig',id:confirmation.id});}
      break;
    }
    case 'toggleExpression': if(selected?.kind==='logger')act({type:'updateComponent',id:selected.id,changes:{expressionMode:!selected.expressionMode}});break;
    case 'copyXml': void copyXml();break;
    case 'componentMenu':{const rect=el.getBoundingClientRect();if(state.selectedId)showMenu(state.selectedId,rect.left-180,rect.bottom+2);break;}
    case 'flowMenu':{const rect=el.getBoundingClientRect();showMenu('flow',rect.left-175,rect.bottom+2);break;}
    case 'menuProperties':menu=null;mutate(s=>{s.lowerTab='properties';});break;
    case 'duplicate':{const id=menu?.id??state.selectedId;menu=null;if(id)act({type:'duplicate',id});break;}
    case 'deleteNode':{const id=menu?.id??state.selectedId;menu=null;if(id)act({type:'delete',id});break;}
    case 'moveLeft':case 'moveRight':{const id=menu?.id??state.selectedId;menu=null;const i=state.flow.processors.findIndex(n=>n.id===id);if(id)act({type:'move',id,index:i+(action==='moveLeft'?-1:1)});break;}
    case 'populateFlow':{menu=null;const next=createInitialState();next.theme=state.theme;next.layout={...state.layout};update(next,true);announce('Original populated sample restored in preview state.');break;}
    default:break;
  }
});
function onValueChange(event:Event):void{
  const el=event.target as HTMLInputElement|HTMLTextAreaElement|HTMLSelectElement;
  if(!isInput(el))return;
  if(el.dataset.control){
    switch(el.dataset.control){
      case 'palette-search':paletteQuery=el.value;render();return;
      case 'globals-search':globalQuery=el.value;render();return;
      case 'metadata-search':metadataQuery=el.value;render();return;
      case 'scenario':if(isScenario(el.value))setScenario(el.value);return;
      case 'theme':mutate(s=>{s.theme=el.value as PreviewState['theme'];});return;
    }
  }
  if(el.dataset.draft&&dialog){
    const name=el.dataset.draft as keyof HttpConfig;
    if(['name','protocol','host','port','basePath'].includes(name)){
      dialog.draft={...dialog.draft,[name]:el.value};render();
    }
    return;
  }
  const node=getComponent(state);
  if(el.dataset.field&&node){
    const name=el.dataset.field;
    const result=applyAction(state,{type:'updateComponent',id:node.id,changes:{[name]:el.value}});
    if(result.error){announce(result.error,true);return;}
    update(result.state,true,`${node.id}-${name}`);
  }
}
root.addEventListener('input',onValueChange);
root.addEventListener('change',event=>{if((event.target as HTMLElement).tagName==='SELECT')onValueChange(event);});
root.addEventListener('dblclick',event=>{const row=(event.target as HTMLElement).closest<HTMLElement>('[data-config]');if(row)openConfig('edit',row.dataset.config);});
root.addEventListener('toggle',event=>{
  const el=event.target as HTMLDetailsElement;
  if(!el.dataset.tree)return;
  const id=el.dataset.tree;const expanded=state.expandedMetadata.includes(id);
  if(el.open===expanded)return;
  if(el.open)state.expandedMetadata.push(id);else state.expandedMetadata=state.expandedMetadata.filter(k=>k!==id);
  persist();
},true);
root.addEventListener('contextmenu',event=>{
  const el=(event.target as HTMLElement).closest<HTMLElement>('[data-node]');
  if(!el)return;event.preventDefault();act({type:'select',id:el.dataset.node!},false);showMenu(el.dataset.node!,(event as MouseEvent).clientX,(event as MouseEvent).clientY);
});
root.addEventListener('keydown',event=>{
  const target=event.target as HTMLElement;
  if(event.key==='Escape'){
    if(dialog||infoOpen||confirmDialog){event.preventDefault();closeOverlay();return;}
    if(menu){const id=menu.id;menu=null;render();restoreFocus({key:`node-${id}`,start:null,end:null});event.preventDefault();return;}
    if(!isInput(target)&&state.selectedId){act({type:'select',id:null},false);event.preventDefault();}return;
  }
  if((dialog||infoOpen||confirmDialog)&&event.key==='Tab'){
    const controls=[...root.querySelectorAll<HTMLElement>('.dialog button:not(:disabled),.dialog input,.dialog select,.dialog textarea,[role="dialog"] [tabindex="0"]')].filter(el=>el.offsetParent!==null);
    const first=controls[0],last=controls.at(-1);
    if(event.shiftKey&&target===first){event.preventDefault();last?.focus();}else if(!event.shiftKey&&target===last){event.preventDefault();first?.focus();}return;
  }
  if(menu&&['ArrowDown','ArrowUp','Home','End'].includes(event.key)){
    event.preventDefault();const buttons=[...root.querySelectorAll<HTMLButtonElement>('.context-menu button:not(:disabled)')];const i=buttons.indexOf(target as HTMLButtonElement);const next=event.key==='Home'?0:event.key==='End'?buttons.length-1:(i+(event.key==='ArrowDown'?1:-1)+buttons.length)%buttons.length;buttons[next]?.focus();return;
  }
  if(target.dataset.resize){
    const key=target.dataset.resize as keyof PreviewLayout;
    const horizontal=key==='propertiesHeight';
    if(event.key==='Home'){event.preventDefault();act({type:'setLayout',layout:{[key]:DEFAULT_LAYOUT[key]}},false);return;}
    const direction=event.key===(horizontal?'ArrowUp':'ArrowLeft')?1:event.key===(horizontal?'ArrowDown':'ArrowRight')?-1:0;
    if(direction){event.preventDefault();const sign=key.startsWith('dw')?-1:1;act({type:'setLayout',layout:{[key]:state.layout[key]+direction*sign*(event.shiftKey?30:10)}},false);}return;
  }
  const row=target.closest<HTMLElement>('[data-config]');
  if(row&&(event.key==='Enter'||event.key===' ')){event.preventDefault();mutate(s=>{s.selectedConfigId=row.dataset.config!;});if(event.key==='Enter')openConfig('edit',row.dataset.config);return;}
  const node=target.closest<HTMLElement>('[data-node]');
  if(node&&event.shiftKey&&event.key==='F10'){event.preventDefault();const rect=node.getBoundingClientRect();act({type:'select',id:node.dataset.node!},false);showMenu(node.dataset.node!,rect.left+30,rect.bottom);return;}
  if(isInput(target))return;
  if((event.ctrlKey||event.metaKey)&&event.key.toLowerCase()==='z'){event.preventDefault();state=event.shiftKey?history.redo(state):history.undo(state);persist();render();return;}
  if((event.ctrlKey||event.metaKey)&&event.key.toLowerCase()==='y'){event.preventDefault();state=history.redo(state);persist();render();return;}
  if((event.ctrlKey||event.metaKey)&&event.key.toLowerCase()==='d'&&state.selectedId){event.preventDefault();act({type:'duplicate',id:state.selectedId});return;}
  if(event.key==='Delete'&&state.selectedId){event.preventDefault();act({type:'delete',id:state.selectedId});return;}
  if(event.altKey&&['ArrowLeft','ArrowRight'].includes(event.key)&&state.selectedId){event.preventDefault();const i=state.flow.processors.findIndex(n=>n.id===state.selectedId);if(i>=0)act({type:'move',id:state.selectedId,index:i+(event.key==='ArrowLeft'?-1:1)});return;}
  const tab=target.closest<HTMLElement>('[role="tab"]');
  if(tab&&['ArrowLeft','ArrowRight','Home','End'].includes(event.key)){
    event.preventDefault();const siblings=[...tab.parentElement!.querySelectorAll<HTMLButtonElement>('[role="tab"]')];const i=siblings.indexOf(tab as HTMLButtonElement);const next=event.key==='Home'?0:event.key==='End'?siblings.length-1:(i+(event.key==='ArrowRight'?1:-1)+siblings.length)%siblings.length;siblings[next]?.focus();siblings[next]?.click();
  }
});
root.addEventListener('dragstart',event=>{
  const el=(event.target as HTMLElement).closest<HTMLElement>('[draggable="true"]');if(!el)return;
  dragPayload=el.dataset.palette?{kind:el.dataset.palette as 'listener'|'logger'}:{id:el.dataset.node};
  dragRejection=null;
  event.dataTransfer?.setData('application/x-zernova-preview',JSON.stringify(dragPayload));if(event.dataTransfer)event.dataTransfer.effectAllowed=dragPayload.id?'move':'copy';
});
root.addEventListener('dragover',event=>{
  const lane=(event.target as HTMLElement).closest<HTMLElement>('[data-drop]');if(!lane||!dragPayload)return;event.preventDefault();
  const source=dragPayload.kind==='listener'||(dragPayload.id&&state.flow.source?.id===dragPayload.id);
  const valid=source?lane.dataset.drop==='source'&&(!state.flow.source||state.flow.source.id===dragPayload.id):lane.dataset.drop==='processor';
  dragRejection=valid?null:source?(lane.dataset.drop==='source'?'This flow already has a source. Only one HTTP Listener is allowed.':'HTTP Listener belongs in the Source lane.'):'Logger belongs in the Processor lane.';
  root.querySelectorAll('.drop-valid,.drop-invalid').forEach(el=>el.classList.remove('drop-valid','drop-invalid'));lane.classList.add(valid?'drop-valid':'drop-invalid');
  if(event.dataTransfer)event.dataTransfer.dropEffect=valid?(dragPayload.id?'move':'copy'):'none';
});
root.addEventListener('dragleave',event=>{const lane=(event.target as HTMLElement).closest<HTMLElement>('[data-drop]');lane?.classList.remove('drop-valid','drop-invalid');});
root.addEventListener('drop',event=>{
  const lane=(event.target as HTMLElement).closest<HTMLElement>('[data-drop]');if(!lane||!dragPayload)return;event.preventDefault();
  const payload=dragPayload;dragPayload=null;const destination=lane.dataset.drop as 'source'|'processor';
  if(payload.kind){act({type:'insert',kind:payload.kind,lane:destination});return;}
  if(payload.id){
    if(state.flow.source?.id===payload.id){if(destination!=='source')announce('HTTP Listener belongs in the source lane.',true);else act({type:'select',id:payload.id},false);return;}
    if(destination!=='processor'){announce('Logger belongs in the processor lane.',true);return;}
    const target=(event.target as HTMLElement).closest<HTMLElement>('[data-processor-index]');const index=target?Number(target.dataset.processorIndex):state.flow.processors.length-1;act({type:'move',id:payload.id,index});
  }
});
root.addEventListener('dragend',()=>{dragPayload=null;root.querySelectorAll('.drop-valid,.drop-invalid').forEach(el=>el.classList.remove('drop-valid','drop-invalid'));if(dragRejection){const reason=dragRejection;dragRejection=null;announce(reason,true);}});
root.addEventListener('pointerdown',event=>{
  const el=(event.target as HTMLElement).closest<HTMLElement>('[data-resize]');if(!el)return;
  event.preventDefault();const key=el.dataset.resize as keyof PreviewLayout;
  resizing={key,start:key==='propertiesHeight'?event.clientY:event.clientX,value:state.layout[key],before:cloneState(state)};
  el.setPointerCapture(event.pointerId);el.classList.add('resizing');
});
root.addEventListener('pointermove',event=>{
  if(!resizing)return;const {key,start,value}=resizing;
  const distance=(key==='propertiesHeight'?event.clientY:event.clientX)-start;
  const next=applyAction(state,{type:'setLayout',layout:{[key]:value+(key.startsWith('dw')?distance:-distance)}}).state;
  state.layout=next.layout;
  const property=key.replace(/[A-Z]/g,l=>`-${l.toLowerCase()}`);root.querySelector<HTMLElement>('.studio')?.style.setProperty(`--${property}`,`${state.layout[key]}px`);
});
root.addEventListener('pointerup',()=>{if(!resizing)return;resizing=null;persist();render();});
window.addEventListener('message',event=>{
  const message=event.data;
  if(!message||typeof message!=='object')return;
  if(message.type==='state'){state=sanitizeState(message.state);history.clear();dialog=null;menu=null;confirmDialog=null;infoOpen=false;currentScenario=state.editorTab==='dataweave'?'dataweave':state.editorTab==='xml'?'xml':state.editorTab==='globals'?'globals':'workbench';render();}
  else if(message.type==='scenario'&&isScenario(message.scenario))setScenario(message.scenario);
  else if(message.type==='hostTheme'&&['light','dark','highContrast','highContrastLight'].includes(message.kind)){render();}
});
window.addEventListener('pagehide',()=>{window.clearTimeout(saveTimer);window.clearTimeout(toastTimer);vscode.postMessage({type:'saveState',state});});
render();
vscode.postMessage({type:'ready'});
}
