import * as vscode from 'vscode';
import { randomBytes } from 'node:crypto';
import type { PreviewState } from '../shared/model';

export type HostTheme = 'light' | 'dark' | 'highContrast' | 'highContrastLight';
export function currentHostTheme(): HostTheme {
  switch (vscode.window.activeColorTheme.kind) {
    case vscode.ColorThemeKind.Light: return 'light';
    case vscode.ColorThemeKind.HighContrast: return 'highContrast';
    case vscode.ColorThemeKind.HighContrastLight: return 'highContrastLight';
    default: return 'dark';
  }
}
const attribute = (text: string): string => text.replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
const inlineJson = (value: unknown): string => JSON.stringify(value).replace(/</g, '\\u003c').replace(/>/g, '\\u003e').replace(/&/g, '\\u0026').replace(/\u2028/g, '\\u2028').replace(/\u2029/g, '\\u2029');

export function renderWebview(webview: vscode.Webview, extensionUri: vscode.Uri, state: PreviewState, realMode = false): string {
  const uri = (path: string): string => webview.asWebviewUri(vscode.Uri.joinPath(extensionUri, path)).toString();
  const nonce = randomBytes(24).toString('base64');
  const policy = `default-src 'none'; img-src ${webview.cspSource}; style-src ${webview.cspSource} 'unsafe-inline'; script-src 'nonce-${nonce}'; connect-src 'none'; font-src ${webview.cspSource}; frame-src 'none'; object-src 'none'; base-uri 'none'; form-action 'none';`;
  return `<!DOCTYPE html>
<html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta http-equiv="Content-Security-Policy" content="${attribute(policy)}">
<link rel="stylesheet" href="${attribute(uri('media/studio.css'))}"><title>ZERNOVA Studio</title>
</head><body><div id="app"></div>
<script nonce="${nonce}">window.__ZERNOVA_REAL__=${inlineJson(realMode)};window.__ZERNOVA_INITIAL__=${inlineJson(state)};window.__ZERNOVA_ASSETS__=${inlineJson({ logo: uri('assets/zernova-mark.svg') })};window.__ZERNOVA_HOST_THEME__=${inlineJson(currentHostTheme())};</script>
<script nonce="${nonce}" src="${attribute(uri('media/studio.js'))}"></script></body></html>`;
}
