import test from 'node:test';
import assert from 'node:assert/strict';
import {
  applyAction, applyScenario, cloneState, createInitialState, DATAWEAVE_INPUT, DATAWEAVE_NOTICE,
  DATAWEAVE_OUTPUT, DATAWEAVE_SOURCE, DEFAULT_LAYOUT, escapeXmlAttribute, getComponent,
  isValidPreviewState, MAX_CONFIGS, MAX_PROCESSORS, MAX_STATE_BYTES, PreviewHistory,
  sanitizeState, serializeXml, validateComponent, validateHttpConfig,
} from '../src/shared/model';
import { isScenario, parseWebviewMessage } from '../src/shared/protocol';

test('fixture identity targets only the new Mule-compatible presentation model', () => {
  const state = createInitialState();
  assert.equal(state.projectId, 'zernova-hello-integration');
  assert.equal(state.schemaVersion, 1);
  assert.equal(state.flow.name, 'helloFlow');
  assert.equal(state.flow.source?.path, '/hello');
  assert.equal(state.flow.processors[0].message, '#["Request received"]');
  assert.equal(state.globalConfigs[0].basePath, '');
  assert.equal(state.globalConfigs[0].port, '18081');
  assert.equal(state.selectedId, null);
  assert.ok(isValidPreviewState(state));
});

test('initial states, cloned states and frozen layout defaults share no mutable state', () => {
  const one = createInitialState(); const two = createInitialState(); const copy = cloneState(one);
  one.flow.processors[0].message = 'edited'; one.layout.paletteWidth = 300;
  assert.equal(two.flow.processors[0].message, '#["Request received"]');
  assert.equal(copy.flow.processors[0].message, '#["Request received"]');
  assert.equal(DEFAULT_LAYOUT.paletteWidth, 224);
  assert.ok(Object.isFrozen(DEFAULT_LAYOUT));
});

test('palette source lane rejects a second source and wrong placements without mutation', () => {
  const state = createInitialState(); const before = JSON.stringify(state);
  for (const action of [
    { type: 'insert', kind: 'listener', lane: 'source' },
    { type: 'insert', kind: 'listener', lane: 'processor' },
    { type: 'insert', kind: 'logger', lane: 'source' },
  ] as const) {
    const result = applyAction(state, action);
    assert.ok(result.error); assert.equal(result.state, state);
  }
  assert.equal(JSON.stringify(state), before);
});

test('empty fixture accepts a Listener and Logger with stable IDs and selection', () => {
  let state = createInitialState(true);
  assert.equal(state.flow.source, null); assert.equal(state.flow.processors.length, 0);
  state = applyAction(state, { type: 'insert', kind: 'listener', lane: 'source' }).state;
  assert.equal(state.selectedId, state.flow.source?.id);
  assert.equal(state.flow.source?.configId, state.globalConfigs[0].id);
  state = applyAction(state, { type: 'insert', kind: 'logger', lane: 'processor' }).state;
  assert.equal(state.selectedId, state.flow.processors[0].id);
  assert.notEqual(state.flow.source?.id, state.flow.processors[0].id);
  assert.ok(isValidPreviewState(state));
});

test('inserting a Listener with no configuration gives an explicit error', () => {
  const state = createInitialState(true); state.globalConfigs = []; state.selectedConfigId = null;
  const result = applyAction(state, { type: 'insert', kind: 'listener', lane: 'source' });
  assert.match(result.error ?? '', /configuration/); assert.equal(result.state, state);
});

test('duplicate, reorder and delete preserve moved Logger properties and selection', () => {
  let state = createInitialState();
  state.flow.processors[0].message = '#["A & B"]';
  state.flow.processors[0].notes = 'Longer\nmultiline notes';
  state = applyAction(state, { type: 'duplicate', id: 'logger-1' }).state;
  const copy = state.flow.processors[1];
  assert.equal(copy.message, '#["A & B"]'); assert.equal(copy.notes, 'Longer\nmultiline notes');
  assert.notEqual(copy.id, 'logger-1'); assert.equal(state.selectedId, copy.id);
  state = applyAction(state, { type: 'move', id: copy.id, index: 0 }).state;
  assert.equal(state.flow.processors[0].id, copy.id); assert.equal(state.selectedId, copy.id);
  assert.equal(state.flow.processors[0].message, '#["A & B"]');
  state = applyAction(state, { type: 'delete', id: copy.id }).state;
  assert.equal(state.selectedId, null); assert.deepEqual(state.flow.processors.map(p => p.id), ['logger-1']);
});

test('source cannot be duplicated or moved into processors', () => {
  const state = createInitialState();
  assert.match(applyAction(state, { type: 'duplicate', id: 'listener-1' }).error ?? '', /Only Logger/);
  assert.match(applyAction(state, { type: 'move', id: 'listener-1', index: 0 }).error ?? '', /Only Logger/);
});

test('bounded insertion caps the processor list', () => {
  let state = createInitialState();
  for (let i = 1; i < MAX_PROCESSORS; i++) state = applyAction(state, { type: 'insert', kind: 'logger', lane: 'processor' }).state;
  assert.equal(state.flow.processors.length, MAX_PROCESSORS);
  const result = applyAction(state, { type: 'duplicate', id: 'logger-1' });
  assert.match(result.error ?? '', /20/); assert.equal(result.state, state);
});

test('one-step UI undo/redo restores fixtures without sharing mutable snapshots', () => {
  const history = new PreviewHistory(); const initial = createInitialState();
  let state = history.commit(initial, applyAction(initial, { type: 'delete', id: 'logger-1' }).state);
  assert.equal(state.flow.processors.length, 0); assert.ok(history.canUndo); assert.ok(!history.canRedo);
  state = history.undo(state); assert.equal(state.flow.processors.length, 1); assert.ok(history.canRedo); assert.ok(!history.canUndo);
  state.flow.processors[0].message = 'changed after undo';
  state = history.redo(state); assert.equal(state.flow.processors.length, 0);
  state = history.undo(state); assert.equal(state.flow.processors[0].message, 'changed after undo');
  history.clear(); assert.ok(!history.canUndo); assert.ok(!history.canRedo);
});

test('new edit after undo invalidates the redo snapshot', () => {
  const history = new PreviewHistory(); const initial = createInitialState();
  let state = history.commit(initial, applyAction(initial, { type: 'duplicate', id: 'logger-1' }).state);
  state = history.undo(state);
  state = history.commit(state, applyAction(state, { type: 'delete', id: 'logger-1' }).state);
  assert.ok(!history.canRedo); assert.equal(state.flow.processors.length, 0);
});

test('configuration draft cancellation is independent; saved edits keep references stable', () => {
  const state = createInitialState(); const draft = { ...state.globalConfigs[0] };
  draft.port = '19000'; draft.name = 'Renamed_config';
  assert.equal(state.globalConfigs[0].port, '18081'); // discard draft = Cancel
  const saved = applyAction(state, { type: 'saveConfig', config: draft });
  assert.equal(saved.error, undefined); assert.equal(saved.state.globalConfigs[0].port, '19000');
  assert.equal(saved.state.flow.source?.configId, draft.id);
  assert.match(serializeXml(saved.state), /config-ref="Renamed_config"/);
});

test('referenced global configuration cannot be silently deleted', () => {
  let state = createInitialState();
  const blocked = applyAction(state, { type: 'deleteConfig', id: 'http-config-1' });
  assert.match(blocked.error ?? '', /used by Listener/); assert.equal(blocked.state, state);
  state = applyAction(state, { type: 'delete', id: 'listener-1' }).state;
  state = applyAction(state, { type: 'deleteConfig', id: 'http-config-1' }).state;
  assert.equal(state.globalConfigs.length, 0); assert.equal(state.selectedConfigId, null);
});

test('configuration save validates duplicate names and caps new configurations', () => {
  let state = createInitialState(); const initial = state.globalConfigs[0];
  assert.match(applyAction(state, { type: 'saveConfig', config: { ...initial, id: 'new-config' } }).error ?? '', /already exists/);
  for (let i = 1; i < MAX_CONFIGS; i++) {
    const result = applyAction(state, { type: 'saveConfig', config: { ...initial, id: `draft-${i}`, name: `Config_${i}` } });
    assert.equal(result.error, undefined); state = result.state;
  }
  assert.equal(state.globalConfigs.length, MAX_CONFIGS);
  assert.match(applyAction(state, { type: 'saveConfig', config: { ...initial, id: 'another', name: 'Another' } }).error ?? '', /12/);
});

test('local form validation rejects missing values, bad path and out-of-range port', () => {
  const state = createInitialState(); const config = { ...state.globalConfigs[0], name: '', port: '99999', host: '', basePath: 'api' };
  assert.deepEqual(Object.keys(validateHttpConfig(config)).sort(), ['basePath', 'host', 'name', 'port']);
  for (const port of ['0', '-1', '65536', '1x', '', ' 80']) assert.ok(validateHttpConfig({ ...state.globalConfigs[0], port }).port);
  for (const port of ['1', '80', '65535']) assert.equal(validateHttpConfig({ ...state.globalConfigs[0], port }).port, undefined);
  const source = { ...state.flow.source!, displayName: '', path: 'hello', allowedMethods: 'EXEC', configId: 'missing' };
  assert.deepEqual(Object.keys(validateComponent(source, state.globalConfigs)).sort(), ['allowedMethods', 'configId', 'displayName', 'path']);
  assert.ok(validateComponent({ ...state.flow.processors[0], message: '' }).message);
});

test('XML is derived from current fields and uses ZERNOVA application identifiers', () => {
  const state = createInitialState(); state.flow.source!.path = '/changed'; state.flow.processors[0].level = 'WARN';
  const xml = serializeXml(state);
  assert.match(xml, /path="\/changed"/); assert.match(xml, /level="WARN"/);
  assert.match(xml, /https:\/\/schemas\.zernova\.dev\/core/);
  assert.match(xml, /xmlns:http="https:\/\/schemas\.zernova\.dev\/http"/);
  assert.match(xml, /doc:name="Log request"/);
  assert.doesNotMatch(xml, /ee:transform|zernova\.org\/schema|ZEL/);
  assert.match(xml, /DESIGN PREVIEW/);
});

test('XML attribute escaping contains malicious tags, ampersands, quotes and multiline expressions', () => {
  const state = createInitialState();
  state.flow.processors[0].message = '#["a < b & c > d"]\n\t\'quote\'"/><evil />\u0000';
  const xml = serializeXml(state);
  assert.doesNotMatch(xml, /<evil/); assert.doesNotMatch(xml, /\u0000/);
  assert.match(xml, /&lt;evil \/&gt;/); assert.match(xml, /&amp;/); assert.match(xml, /&#10;&#9;/);
  assert.match(xml, /&apos;quote&apos;/);
  assert.equal(escapeXmlAttribute('ሰላም 😀'), 'ሰላም 😀');
  assert.equal(escapeXmlAttribute('\ud800X\ufffe'), 'X');
});

test('missing, old-version, oversized and cyclic persisted values reset safely', () => {
  for (const input of [undefined, null, [], {}, { schemaVersion: 2 }, { schemaVersion: 1, extra: 'x'.repeat(MAX_STATE_BYTES + 1) }]) {
    assert.deepEqual(sanitizeState(input), createInitialState());
  }
  const cycle: Record<string, unknown> = { schemaVersion: 1 }; cycle.self = cycle;
  assert.deepEqual(sanitizeState(cycle), createInitialState()); assert.equal(isValidPreviewState(cycle), false);
});

test('persisted duplicate IDs, orphaned selection and unsafe fields are dropped', () => {
  const state = createInitialState();
  const unsafe = { ...state, command: 'spawn', selectedId: 'missing', flow: { ...state.flow, processors: [state.flow.processors[0], { ...state.flow.processors[0], message: 'duplicate' }] } };
  const clean = sanitizeState(unsafe);
  assert.equal(clean.flow.processors.length, 1); assert.equal(clean.selectedId, null);
  assert.equal((clean as unknown as Record<string, unknown>).command, undefined);
  assert.equal(isValidPreviewState(unsafe), false); assert.ok(isValidPreviewState(clean));
});

test('invalid persisted configuration reference resolves safely or removes orphan source', () => {
  const state = createInitialState(); state.flow.source!.configId = 'missing';
  assert.equal(sanitizeState(state).flow.source?.configId, 'http-config-1');
  state.globalConfigs = [];
  assert.equal(sanitizeState(state).flow.source, null);
});

test('layout, zoom, string and processor limits survive hostile saved values', () => {
  const state = createInitialState();
  const input = { ...state, zoom: Infinity, layout: { ...state.layout, paletteWidth: -20, propertiesHeight: 999999, metadataWidth: NaN }, flow: { ...state.flow, name: 'x'.repeat(150), processors: Array.from({ length: 25 }, (_, i) => ({ ...state.flow.processors[0], id: `logger-${i}` })) } };
  const clean = sanitizeState(input);
  assert.equal(clean.zoom, 100); assert.equal(clean.layout.paletteWidth, 190);
  assert.equal(clean.layout.propertiesHeight, 620); assert.equal(clean.layout.metadataWidth, DEFAULT_LAYOUT.metadataWidth);
  assert.equal(clean.flow.name.length, 100); assert.equal(clean.flow.processors.length, MAX_PROCESSORS);
});

test('state validator is order-independent but rejects additional capability fields', () => {
  const state = createInitialState(); const reordered = Object.fromEntries(Object.entries(state).reverse());
  assert.ok(isValidPreviewState(reordered));
  assert.equal(isValidPreviewState({ ...state, runtimeCommand: 'start' }), false);
});

test('bounded maximum long-Unicode fixture remains persistable and over-limit Unicode is rejected', () => {
  let state = createInitialState();
  state.flow.processors[0].message = 'ሀ'.repeat(8000);
  state.flow.processors[0].notes = 'መ'.repeat(4000);
  for (let i = 1; i < MAX_PROCESSORS; i++) state = applyAction(state, { type: 'duplicate', id: 'logger-1' }).state;
  assert.ok(isValidPreviewState(state));
  assert.equal(parseWebviewMessage({ type: 'saveState', state })?.type, 'saveState');
  const oversized = { schemaVersion: 1, extra: 'ሀ'.repeat(Math.ceil(MAX_STATE_BYTES / 3)) };
  assert.equal(parseWebviewMessage({ type: 'saveState', state: oversized }), null);
  assert.deepEqual(sanitizeState(oversized), createInitialState());
});

test('closed webview protocol accepts only complete permitted messages', () => {
  for (const type of ['ready', 'resetPreview', 'resetLayout']) assert.deepEqual(parseWebviewMessage({ type }), { type });
  const saved = parseWebviewMessage({ type: 'saveState', state: createInitialState() });
  assert.equal(saved?.type, 'saveState');
  for (const value of [null, [], 'ready', { type: 'execute', command: 'java' }, { type: 'fetch', url: 'https://customer.example' }, { type: 'ready', command: 'start' }, { type: 'saveState', state: {} }, { type: 'saveState', state: createInitialState(), extra: true }]) {
    assert.equal(parseWebviewMessage(value), null);
  }
});

test('all bounded scenarios navigate the same state, preserving edited component properties', () => {
  const state = createInitialState(); state.flow.processors[0].category = 'io.zernova.changed';
  for (const scenario of ['workbench', 'listener', 'logger', 'globals', 'xml', 'dataweave'] as const) {
    assert.ok(isScenario(scenario)); const next = applyScenario(state, scenario);
    assert.equal(next.flow.processors[0].category, 'io.zernova.changed');
  }
  assert.equal(applyScenario(state, 'logger').selectedId, 'logger-1');
  assert.equal(applyScenario(state, 'listener').selectedId, 'listener-1');
  assert.equal(applyScenario(state, 'xml').editorTab, 'xml');
  assert.equal(applyScenario(state, 'empty').flow.processors.length, 0);
  assert.equal(isScenario('deploy'), false); assert.equal(getComponent(state, 'unknown'), undefined);
});

test('DataWeave presentation is explicitly a fixed, illustrative fixture', () => {
  assert.match(DATAWEAVE_SOURCE, /%dw 2\.0/);
  assert.equal(DATAWEAVE_INPUT.firstName, 'Samira');
  assert.equal(DATAWEAVE_OUTPUT.fullName, 'Samira Tesfaye');
  assert.equal(DATAWEAVE_NOTICE, 'Illustrative — not executed');
});
