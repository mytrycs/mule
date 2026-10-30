import * as vscode from 'vscode';
import { randomBytes } from 'node:crypto';
import { installDialogDragging } from '../webview/dialogDrag';
import type {FormField} from '../shared/studioForms';
export type {FormField,StudioForm} from '../shared/studioForms';
const escape = (value: string) => value.replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]!));
/** A draggable, validated Studio form. Values are data, never executable commands. */
export function studioForm<T>(title: string, fields: FormField[], submit: (values: Record<string, string>) => Promise<T>, help = ''): Promise<T | undefined> {
  const panel = vscode.window.createWebviewPanel('zernovaKernelStudio.wizard', title, vscode.ViewColumn.Active, { enableScripts: true, retainContextWhenHidden: true });
  const sections = [...new Set(fields.map(f => f.section ?? 'General'))];
  const fieldHtml = (f: FormField) => `<label for="${f.key}">${escape(f.label)}</label><div class="field">${f.options ? `<select id="${f.key}" name="${f.key}">${f.options.map(o => `<option value="${escape(o.value)}" ${o.value === f.value ? 'selected' : ''}>${escape(o.label)}</option>`).join('')}</select>` : f.multiline ? `<textarea id="${f.key}" name="${f.key}">${escape(f.value)}</textarea>` : `<input id="${f.key}" name="${f.key}" value="${escape(f.value)}">`}${f.browse ? `<button type="button" data-browse="${f.key}">Browse…</button>` : ''}</div>${f.hint ? `<small>${escape(f.hint)}</small>` : ''}`;
  const nonce = randomBytes(20).toString('hex'); let busy = false;
  return new Promise(resolve => {
    let resolved = false;
    const finish = (value?: T) => { if (!resolved) { resolved = true; resolve(value); } };
    panel.onDidDispose(() => finish());
    panel.webview.onDidReceiveMessage(async message => {
      if (busy || !message || typeof message !== 'object') return;
      if (message.type === 'cancel') { panel.dispose(); return; }
      if (message.type === 'browse' && fields.some(f => f.key === message.key && f.browse)) {
        const result = await vscode.window.showOpenDialog({ canSelectFiles: false, canSelectFolders: true, canSelectMany: false, title: 'Choose project parent directory' });
        if (result?.[0]) await panel.webview.postMessage({ type: 'field', key: message.key, value: result[0].fsPath }); return;
      }
      if (message.type !== 'submit' || !message.values || typeof message.values !== 'object' || Object.keys(message.values).length !== fields.length || !fields.every(f => typeof message.values[f.key] === 'string' && message.values[f.key].length <= (f.maxLength??16384))) return;
      busy = true;
      try { const result = await submit(message.values); finish(result); panel.dispose(); }
      catch (error) { await panel.webview.postMessage({ type: 'error', message: error instanceof Error ? error.message : String(error) }); }
      finally { busy = false; }
    });
    panel.webview.html = `<!doctype html><html><head><meta charset="utf-8"><meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src 'unsafe-inline'; script-src 'nonce-${nonce}';"><style>
    *{box-sizing:border-box}body{font-family:var(--vscode-font-family);color:var(--vscode-foreground);background:var(--vscode-editor-background);margin:0}.dialog-backdrop{position:fixed;inset:0;display:flex;align-items:center;justify-content:center;background:#102b3020}.dialog{width:min(640px,calc(100vw - 32px));max-height:calc(100vh - 32px);display:flex;flex-direction:column;background:var(--vscode-editor-background);border:1px solid var(--vscode-panel-border,#69918e);border-radius:6px;box-shadow:0 12px 45px #0004}.dialog-title{cursor:grab;touch-action:none;display:flex;justify-content:space-between;align-items:center;padding:16px;font-weight:600;border-bottom:1px solid var(--vscode-panel-border,#69918e)}.body{padding:16px;overflow:auto}label{display:block;margin:12px 0 5px}input,textarea,select{width:100%;font:inherit;padding:8px;background:var(--vscode-input-background);color:var(--vscode-input-foreground);border:1px solid var(--vscode-input-border,#849e9b)}textarea{min-height:80px;resize:vertical}button{font:inherit;cursor:pointer;padding:7px 13px;background:var(--vscode-button-secondaryBackground,#e6efee);color:var(--vscode-button-secondaryForeground);border:1px solid var(--vscode-panel-border,#69918e);border-radius:3px}.primary{background:var(--vscode-button-background,#00897b);color:var(--vscode-button-foreground,#fff)}footer{padding:12px 16px;display:flex;justify-content:flex-end;gap:8px;border-top:1px solid var(--vscode-panel-border,#69918e)}small,p{line-height:1.5;color:var(--vscode-descriptionForeground)}.form-tabs{display:flex;gap:5px;flex-wrap:wrap}.form-tabs [aria-selected=true]{border-color:var(--vscode-focusBorder)}[hidden]{display:none!important}.field{display:flex;gap:8px}.error{color:var(--vscode-errorForeground)}:focus-visible{outline:2px solid var(--vscode-focusBorder);outline-offset:2px}
    </style></head><body><div class="dialog-backdrop"><form class="dialog" role="dialog" aria-modal="true" aria-labelledby="wizard-title"><div class="dialog-title"><span id="wizard-title">${escape(title)}</span><button type="button" id="close" aria-label="Close wizard">×</button></div><div class="body">${help ? `<p>${escape(help)}</p>` : ''}${sections.length > 1 ? `<nav class="form-tabs" role="tablist" aria-label="Configuration sections">${sections.map((section,i)=>`<button type="button" role="tab" data-section-tab="${escape(section)}" aria-selected="${i===0}">${escape(section)}</button>`).join('')}</nav>` : ''}${sections.map((section,i)=>`<section data-form-section="${escape(section)}" ${i ? 'hidden' : ''}>${fields.filter(f=>(f.section ?? 'General')===section).map(fieldHtml).join('')}</section>`).join('')}<p id="error" class="error" role="alert"></p></div><footer><button type="button" id="cancel">Cancel</button><button type="submit" class="primary">${title === 'New ZERNOVA Project' ? 'Create project' : title === 'New Integration XML' ? 'Create XML' : title.startsWith('Import') ? 'Import' : 'Save'}</button></footer></form></div><script nonce="${nonce}">
    const vscode=acquireVsCodeApi();(${installDialogDragging.toString()})(document.body);
    const form=document.querySelector('form'),error=document.getElementById('error');
    for(const id of ['cancel','close'])document.getElementById(id).onclick=()=>vscode.postMessage({type:'cancel'});
    document.querySelectorAll('[data-browse]').forEach(b=>b.onclick=()=>vscode.postMessage({type:'browse',key:b.dataset.browse}));
    document.querySelectorAll('[data-section-tab]').forEach(b=>b.onclick=()=>{document.querySelectorAll('[data-section-tab]').forEach(t=>t.setAttribute('aria-selected',String(t===b)));document.querySelectorAll('[data-form-section]').forEach(s=>s.hidden=s.dataset.formSection!==b.dataset.sectionTab);});
    form.onsubmit=e=>{e.preventDefault();error.textContent='';form.querySelector('[type=submit]').disabled=true;vscode.postMessage({type:'submit',values:Object.fromEntries(new FormData(form))});};
    window.addEventListener('message',e=>{const m=e.data;if(m.type==='error'){error.textContent=m.message;form.querySelector('[type=submit]').disabled=false;}if(m.type==='field'){const f=document.getElementById(m.key);if(f)f.value=m.value;}});
    document.addEventListener('keydown',e=>{if(e.key==='Escape'){e.preventDefault();vscode.postMessage({type:'cancel'});}if(e.key==='Tab'){const items=[...form.querySelectorAll('button:not(:disabled),input,textarea,select,[tabindex="0"]')];const visible=items.filter(x=>x.offsetParent!==null),first=visible[0],last=visible.at(-1);if(e.shiftKey&&document.activeElement===first){e.preventDefault();last.focus();}else if(!e.shiftKey&&document.activeElement===last){e.preventDefault();first.focus();}}});
    document.querySelector('input,textarea')?.focus();
    </script></body></html>`;
  });
}
