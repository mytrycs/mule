import type * as vscode from 'vscode';
// TextDocument.save() reports a manual save too. Mark our background/build saves
// so they cannot be mistaken for an explicit owner request to redeploy.
const automatic = new Map<string, number>();
export function isAutomaticSave(uri: vscode.Uri): boolean { return (automatic.get(uri.toString()) ?? 0) > 0; }
export async function saveAutomatically(document: vscode.TextDocument): Promise<boolean> {
  const key = document.uri.toString(); automatic.set(key, (automatic.get(key) ?? 0) + 1);
  try { return await document.save(); }
  finally { const depth = (automatic.get(key) ?? 1) - 1; if (depth) automatic.set(key, depth); else automatic.delete(key); }
}
