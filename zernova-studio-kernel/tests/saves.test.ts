import test from 'node:test';
import assert from 'node:assert/strict';
import type * as vscode from 'vscode';
import { saveAutomatically, isAutomaticSave } from '../src/host/saves';
test('Programmatic autosaves remain marked through the save event and clear afterwards', async () => {
  const uri = { toString: () => 'file:///project/hello.xml' } as vscode.Uri;
  let finish!: () => void; const waiting = new Promise<void>(resolve => { finish = resolve; });
  const doc = { uri, save: async () => { assert(isAutomaticSave(uri)); await waiting; assert(isAutomaticSave(uri)); return true; } } as unknown as vscode.TextDocument;
  const saved = saveAutomatically(doc); assert(isAutomaticSave(uri)); finish(); assert(await saved); assert(!isAutomaticSave(uri));
});
test('Failed programmatic saves cannot suppress a later owner save', async () => {
  const uri = { toString: () => 'file:///project/hello.xml' } as vscode.Uri;
  const doc = { uri, save: async () => { throw new Error('disk failed'); } } as unknown as vscode.TextDocument;
  await assert.rejects(saveAutomatically(doc), /disk failed/); assert(!isAutomaticSave(uri));
});
