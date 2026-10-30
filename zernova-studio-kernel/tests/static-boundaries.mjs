/**
 * Structural security/isolation audit for the authorized Phase 2B extension.
 * This is deliberately separate from installed-host behavioral verification.
 * Run after build/package: node tests/static-boundaries.mjs
 * During editing: node tests/static-boundaries.mjs --source-only
 */
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { inflateRawSync } from 'node:zlib';
import { createHash } from 'node:crypto';
import ts from 'typescript';

const project = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const sourceOnly = process.argv.includes('--source-only');
const checks = [];
const report = (id, check) => {
  try { const details = check(); checks.push({ id, status: 'PASS', ...(details ? { details } : {}) }); }
  catch (error) { checks.push({ id, status: 'FAIL', details: error instanceof Error ? error.message : String(error) }); }
};
const read = relative => fs.readFileSync(path.join(project, relative), 'utf8');
const walk = directory => fs.readdirSync(directory, { withFileTypes: true }).flatMap(entry => entry.isDirectory() ? walk(path.join(directory, entry.name)) : [path.join(directory, entry.name)]);
const relative = filename => path.relative(project, filename).replaceAll(path.sep, '/');
const namespace = 'zernovaKernelStudio';
const manifest = JSON.parse(read('package.json'));
const expectedCommands = ['openPreview', 'resetPreview', 'resetLayout', 'openScenario', 'newProject', 'openProject', 'newIntegration', 'openIntegration', 'configureRuntime', 'diagnostics', 'build', 'run', 'status', 'logs', 'stopApplication', 'stopRuntime', 'cancel', 'selectProject'].map(name => `${namespace}.${name}`);
const allowedModules = new Set(['vscode', 'node:crypto', 'node:fs/promises', 'node:path', 'node:os', 'node:net', 'node:child_process', 'sax']);
const forbiddenCalls = new Set(['eval', 'Function', 'fetch', 'XMLHttpRequest', 'WebSocket', 'EventSource', 'Worker', 'SharedWorker', 'importScripts']);
const forbiddenMethods = new Set(['spawnSync', 'execSync', 'execFile', 'execFileSync', 'fork', 'createConnection', 'createSecureContext', 'sendBeacon']);
const forbiddenVscode = [
  'vscode.window.createTerminal', 'vscode.tasks.executeTask', 'vscode.debug.startDebugging',
  'vscode.env.openExternal',
];

function examineModule(text, filename, source = true) {
  const ast = ts.createSourceFile(filename, text, ts.ScriptTarget.Latest, true, filename.endsWith('.ts') ? ts.ScriptKind.TS : ts.ScriptKind.JS);
  const imports = [];
  const errors = [];
  const isHost = filename.startsWith('src/host/') || filename === 'src/extension.ts' || filename.endsWith('dist/extension.js');
  const at = node => {
    const pos = ast.getLineAndCharacterOfPosition(node.getStart(ast));
    return `${filename}:${pos.line + 1}:${pos.character + 1}`;
  };
  const moduleName = (value, node) => {
    imports.push(value);
    if (value.startsWith('.')) {
      if (!source) errors.push(`${at(node)} unbundled relative runtime import ${value}`);
      else {
        const resolved = path.resolve(project, path.dirname(filename), value);
        if (!resolved.startsWith(path.join(project, 'src') + path.sep)) errors.push(`${at(node)} relative import escapes the new source tree: ${value}`);
      }
    } else if (value === 'stream' && !source && filename.endsWith('dist/extension.js')) { /* sax 1.6.1 optional SAXStream compatibility import; no network transport */ }
    else if (!allowedModules.has(value)) errors.push(`${at(node)} unexpected executable dependency ${value}`);
    else if (!isHost && value !== 'sax') errors.push(`${at(node)} privileged host import in shared/webview source ${value}`);
    if (source && value === 'node:child_process' && filename !== 'src/host/process.ts') errors.push(`${at(node)} process capability outside the reviewed process adapter`);
    if (source && value === 'node:net' && filename !== 'src/host/runtime.ts') errors.push(`${at(node)} socket capability outside the loopback port preflight`);
  };
  const visit = node => {
    if ((ts.isImportDeclaration(node) || ts.isExportDeclaration(node)) && node.moduleSpecifier && ts.isStringLiteral(node.moduleSpecifier)) {
      moduleName(node.moduleSpecifier.text, node);
      if (filename === 'src/host/webview.ts' && node.moduleSpecifier.text === 'node:crypto' && ts.isImportDeclaration(node)) {
        const bindings = node.importClause?.namedBindings;
        if (!bindings || !ts.isNamedImports(bindings) || bindings.elements.some(part => (part.propertyName?.text ?? part.name.text) !== 'randomBytes')) errors.push(`${at(node)} only nonce generation with randomBytes is allowed from crypto`);
      }
    }
    if (ts.isImportEqualsDeclaration(node)) errors.push(`${at(node)} import-equals requires explicit review`);
    if (ts.isCallExpression(node) || ts.isNewExpression(node)) {
      const expression = node.expression;
      const call = expression.getText(ast);
      if (forbiddenCalls.has(call)) errors.push(`${at(node)} forbidden network/code/process constructor ${call}`);
      if (['spawn', 'createServer', 'listen'].includes(call) && !isHost) errors.push(`${at(node)} host capability in presentation/shared source ${call}`);
      if (ts.isPropertyAccessExpression(expression) && forbiddenMethods.has(expression.name.text)) errors.push(`${at(node)} forbidden executable/network method ${expression.name.text}`);
      if (ts.isPropertyAccessExpression(expression) && ['spawn', 'createServer', 'listen'].includes(expression.name.text) && !isHost) errors.push(`${at(node)} host capability in presentation/shared source ${expression.name.text}`);
      if (forbiddenVscode.some(prefix => call === prefix || call.startsWith(prefix))) errors.push(`${at(node)} forbidden VS Code integration API ${call}`);
      if (call === 'window.open' || call === 'location.assign' || call === 'location.replace') errors.push(`${at(node)} navigation/network surface is not permitted`);
      if (call === 'vscode.commands.executeCommand') {
        const first = node.arguments?.[0];
        const literal = first && ts.isStringLiteral(first) ? first.text : undefined;
        const approvedLiteral = ['workbench.view.extension.zernovaKernelStudio', 'vscode.openWith', 'undo', 'redo'].includes(literal);
        const validatedUndoRedo = filename === 'src/host/documents.ts' && first?.getText(ast) === 'message.action';
        if (!approvedLiteral && !validatedUndoRedo) errors.push(`${at(node)} command dispatch is outside the reviewed sidebar/custom-editor/undo-redo boundary`);
      }
      if (call === 'require' || expression.kind === ts.SyntaxKind.ImportKeyword) {
        const first = node.arguments?.[0];
        if (!first || !ts.isStringLiteral(first)) errors.push(`${at(node)} dynamic executable import`);
        else moduleName(first.text, node);
      }
      if ((call === 'setTimeout' || call === 'setInterval' || call === 'window.setTimeout' || call === 'window.setInterval') && node.arguments?.[0] && ts.isStringLiteral(node.arguments[0])) errors.push(`${at(node)} string-based timer execution`);
      if (call.includes('getConfiguration') && call.endsWith('.update')) errors.push(`${at(node)} global/workspace configuration write`);
    }
    ts.forEachChild(node, visit);
  };
  visit(ast);
  assert.deepEqual(errors, [], errors.join('\n'));
  return { imports: [...new Set(imports)].sort() };
}

report('new-extension-identity-and-version', () => {
  assert.equal(manifest.name, 'zernova-studio-kernel-preview');
  assert.equal(manifest.publisher, 'zernova-preview');
  assert.equal(manifest.version, '0.2.0');
  assert.equal(manifest.displayName, 'ZERNOVA Studio');
  assert.equal(manifest.private, true);
  assert.equal(manifest.main, './dist/extension.js');
  return { extensionId: `${manifest.publisher}.${manifest.name}`, version: manifest.version, project };
});
report('one-native-activity-bar-contribution', () => {
  const entries = manifest.contributes.viewsContainers.activitybar;
  assert.equal(entries.length, 1); assert.equal(entries[0].id, namespace);
  for (const [container, views] of Object.entries(manifest.contributes.views)) {
    assert.equal(container, namespace);
    for (const view of views) assert.ok(view.id.startsWith(`${namespace}.`));
  }
  return { count: entries.length, viewIds: Object.values(manifest.contributes.views).flat().map(view => view.id) };
});
report('all-preview-and-real-commands-use-established-namespace', () => {
  assert.deepEqual(manifest.contributes.commands.map(command => command.command).sort(), [...expectedCommands].sort());
  for (const menu of Object.values(manifest.contributes.menus ?? {})) for (const item of menu) assert.ok(expectedCommands.includes(item.command));
  for (const key of Object.keys(manifest.contributes.configuration?.properties ?? {})) assert.ok(key.startsWith(`${namespace}.`));
  return expectedCommands;
});
report('optional-scoped-integration-editor-without-settings-overrides', () => {
  assert.deepEqual(manifest.contributes.customEditors, [{ viewType: 'zernovaKernelStudio.integration', displayName: 'ZERNOVA Integration', selector: [{ filenamePattern: '**/src/main/mule/*.xml' }], priority: 'option' }]);
  for (const key of ['languages', 'configurationDefaults', 'jsonValidation', 'debuggers', 'taskDefinitions', 'terminal', 'terminalProfiles']) assert.equal(manifest.contributes[key], undefined, `Unexpected contribution ${key}`);
  assert.equal(manifest.extensionPack, undefined); assert.equal(manifest.extensionDependencies, undefined);
  assert.deepEqual(manifest.activationEvents, ['onWebviewPanel:zernovaKernelStudio.preview']);
});
report('no-runtime-or-legacy-production-dependencies', () => {
  assert.deepEqual(manifest.dependencies ?? {}, { sax: '1.6.1' });
  assert.deepEqual(manifest.optionalDependencies ?? {}, {});
  const allowedDev = ['@types/node', '@types/vscode', '@vscode/test-electron', '@vscode/vsce', 'axe-core', 'esbuild', 'playwright', 'typescript', 'marked'];
  assert.deepEqual(Object.keys(manifest.devDependencies).sort(), allowedDev.sort());
  for (const version of Object.values(manifest.devDependencies)) assert.match(version, /^\d+\.\d+\.\d+$/);
  const lock = JSON.parse(read('package-lock.json'));
  assert.equal(lock.packages[''].name, manifest.name);
  assert.deepEqual(lock.packages[''].dependencies ?? {}, { sax: '1.6.1' });
  assert.deepEqual(lock.packages[''].devDependencies, manifest.devDependencies);
  for (const [name, entry] of Object.entries(lock.packages)) {
    if (name) assert.ok(name.startsWith('node_modules/'), `Lockfile links an external project: ${name}`);
    assert.notEqual(entry.link, true, `Linked local dependency: ${name}`);
  }
  return { productionDependencyCount: 1, productionDependency: 'Bundled XML syntax parser sax; no execution engine or connector', pinnedDevelopmentDependencyCount: allowedDev.length };
});

const sourceFiles = walk(path.join(project, 'src')).filter(file => file.endsWith('.ts'));
for (const filename of sourceFiles) report(`source-api-boundary:${relative(filename)}`, () => examineModule(fs.readFileSync(filename, 'utf8'), relative(filename)));
report('closed-webview-message-type', () => {
  const ast = ts.createSourceFile('protocol.ts', read('src/shared/protocol.ts'), ts.ScriptTarget.Latest, true, ts.ScriptKind.TS);
  const alias = ast.statements.find(node => ts.isTypeAliasDeclaration(node) && node.name.text === 'WebviewMessage');
  assert.ok(alias && ts.isUnionTypeNode(alias.type));
  const variants = alias.type.types.map(type => {
    assert.ok(ts.isTypeLiteralNode(type));
    const discriminator = type.members.find(member => ts.isPropertySignature(member) && member.name.getText(ast) === 'type');
    assert.ok(discriminator?.type && ts.isLiteralTypeNode(discriminator.type) && ts.isStringLiteral(discriminator.type.literal));
    return discriminator.type.literal.text;
  });
  assert.deepEqual(variants.sort(), ['ready', 'resetLayout', 'resetPreview', 'saveState']);
  const host = read('src/extension.ts');
  assert.match(host, /parseWebviewMessage\(unknownMessage\)/);
  assert.match(host, /if\s*\(!message\)\s*\{[^}]*return;/);
  return { inboundVariants: variants, note: 'Behavioral rejection tests are in tests/model.test.ts.' };
});
report('local-resources-and-restrictive-csp', () => {
  const html = read('src/host/webview.ts');
  for (const directive of ["default-src 'none'", "connect-src 'none'", "frame-src 'none'", "object-src 'none'", "base-uri 'none'", "form-action 'none'", "script-src 'nonce-"]) assert.ok(html.includes(directive), `Missing CSP directive ${directive}`);
  assert.ok(!html.includes("'unsafe-eval'"));
  assert.match(html, /randomBytes\(24\)/);
  assert.match(html, /Content-Security-Policy/);
  assert.match(html, /inlineJson\(state\)/);
  const host = read('src/extension.ts');
  const roots = host.match(/localResourceRoots:\s*\[([^\]]+)\]/)?.[1] ?? '';
  assert.match(roots, /context\.extensionUri, 'media'/);
  assert.match(roots, /context\.extensionUri, 'assets'/);
  assert.equal((roots.match(/context\.extensionUri/g) ?? []).length, 2);
  assert.doesNotMatch(roots, /workspace|fsPath|file\(/);
  return { scriptPolicy: 'nonce', connectionPolicy: 'none', roots: ['media', 'assets'], inlineStyles: 'Permitted for controlled pane sizes; no inline scripts without nonce.' };
});
report('namespaced-state-and-disposal', () => {
  assert.match(read('src/shared/model.ts'), /PREVIEW_STATE_KEY = 'zernovaKernelStudio\.preview\.v1'/);
  const host = read('src/extension.ts');
  assert.match(host, /workspaceState\.update\(PREVIEW_STATE_KEY/);
  assert.match(host, /context\.subscriptions\.push/);
  assert.match(host, /panel\.onDidDispose/);
  assert.match(host, /panelListeners\.splice\(0\)/);
  return { persistence: 'Preview remains under the versioned fixture key; per-project real settings and document state use separate host-owned storage', note: 'Installed-host reload/singleton behavior is verified separately.' };
});
report('preview-bridge-stays-non-executable', () => {
  const source = read('src/shared/protocol.ts');
  assert.doesNotMatch(source, /realAction|realEdit|realXml|RuntimeAction/);
  const host = read('src/extension.ts');
  const adopt = host.slice(host.indexOf('const adoptPanel'), host.indexOf('async function openPreview'));
  assert.match(adopt, /parseWebviewMessage/);
  assert.doesNotMatch(adopt, /runtime\.action|runtime\.execute|runtime\.run|parseRealMessage/);
  return { previewInbound: 'ready/saveState/resetPreview/resetLayout only', realEditor: 'Separate validated custom-editor message handler' };
});
report('process-adapter-explicit-argv-no-shell-and-loopback-preflight', () => {
  const process = read('src/host/process.ts');
  assert.match(process, /spawn\(options\.executable, options\.args/);
  assert.match(process, /shell:\s*false/);
  assert.doesNotMatch(process, /execSync|execFile|shell:\s*true/);
  const runtime = read('src/host/runtime.ts');
  assert.match(runtime, /host:\s*'127\.0\.0\.1'/);
  assert.match(runtime, /isTrusted/);
  return { processes: 'Reviewed host adapter with executable/argv separation', socketUse: 'Bounded loopback port availability check' };
});
report('offline-css-and-vector-assets', () => {
  const assets = [...walk(path.join(project, 'assets')), ...walk(path.join(project, 'src/webview'))].filter(file => /\.(svg|css)$/.test(file));
  for (const file of assets) {
    const text = fs.readFileSync(file, 'utf8');
    assert.doesNotMatch(text, /url\(\s*['"]?(?:https?:|\/\/)/i, `${relative(file)} contains remote CSS resource`);
    if (file.endsWith('.svg')) {
      assert.doesNotMatch(text, /<\s*(?:script|foreignObject)\b/i);
      assert.doesNotMatch(text, /\son[a-z]+\s*=/i);
      assert.doesNotMatch(text, /(?:href|xlink:href)\s*=\s*['"](?:https?:|\/\/|data:text\/html)/i);
    }
  }
  return { inspectedFiles: assets.map(relative) };
});

// A bounded ZIP reader for the local VSIX: no external commands or unzip tool required.
function readZip(buffer) {
  let end = -1;
  for (let i = buffer.length - 22; i >= Math.max(0, buffer.length - 65557); i--) if (buffer.readUInt32LE(i) === 0x06054b50) { end = i; break; }
  assert.ok(end >= 0, 'ZIP end directory missing');
  const count = buffer.readUInt16LE(end + 10);
  assert.ok(count > 0 && count < 2000, 'Unexpected ZIP entry count');
  let cursor = buffer.readUInt32LE(end + 16);
  const entries = new Map();
  for (let i = 0; i < count; i++) {
    assert.equal(buffer.readUInt32LE(cursor), 0x02014b50, 'Invalid central directory');
    const flags = buffer.readUInt16LE(cursor + 8), method = buffer.readUInt16LE(cursor + 10);
    const packed = buffer.readUInt32LE(cursor + 20), size = buffer.readUInt32LE(cursor + 24);
    const nameLength = buffer.readUInt16LE(cursor + 28), extraLength = buffer.readUInt16LE(cursor + 30), commentLength = buffer.readUInt16LE(cursor + 32);
    const offset = buffer.readUInt32LE(cursor + 42);
    const name = buffer.subarray(cursor + 46, cursor + 46 + nameLength).toString('utf8');
    assert.ok(!name.startsWith('/') && !name.includes('..') && !name.includes('\\'), `Unsafe ZIP path ${name}`);
    assert.ok(!entries.has(name), `Duplicate ZIP path ${name}`);
    assert.equal(flags & 1, 0, 'Encrypted ZIP entry');
    assert.ok(size <= 10 * 1024 * 1024 && packed <= 10 * 1024 * 1024, 'Oversized VSIX entry');
    assert.equal(buffer.readUInt32LE(offset), 0x04034b50, 'Missing local ZIP header');
    const start = offset + 30 + buffer.readUInt16LE(offset + 26) + buffer.readUInt16LE(offset + 28);
    const data = buffer.subarray(start, start + packed);
    const unpacked = method === 0 ? data : method === 8 ? inflateRawSync(data, { maxOutputLength: 10 * 1024 * 1024 }) : null;
    assert.ok(unpacked, `Unsupported ZIP method ${method}`); assert.equal(unpacked.length, size, `Size mismatch ${name}`);
    entries.set(name, unpacked);
    cursor += 46 + nameLength + extraLength + commentLength;
  }
  return entries;
}

if (!sourceOnly) {
  for (const filename of ['dist/extension.js', 'media/studio.js']) report(`built-bundle-api-boundary:${filename}`, () => examineModule(read(filename), filename, false));
  report('vsix-isolated-package-and-executable-content', () => {
    const filename = `dist/${manifest.name}-${manifest.version}.vsix`;
    const buffer = fs.readFileSync(path.join(project, filename));
    const entries = readZip(buffer);
    for (const required of ['[Content_Types].xml', 'extension.vsixmanifest', 'extension/package.json', 'extension/dist/extension.js', 'extension/media/studio.js', 'extension/media/studio.css']) assert.ok(entries.has(required), `Missing ${required}`);
    const packagedManifest = JSON.parse(entries.get('extension/package.json').toString('utf8'));
    assert.equal(packagedManifest.name, manifest.name); assert.equal(packagedManifest.publisher, manifest.publisher); assert.equal(packagedManifest.version, manifest.version);
    assert.deepEqual(packagedManifest.contributes, manifest.contributes);
    for (const [name] of entries) {
      assert.doesNotMatch(name, /\.(?:jar|war|ear|class|so|dylib|dll|exe|node|wasm|zip)$/i, `Runtime/native/archive binary in VSIX: ${name}`);
      assert.doesNotMatch(name, /(?:^|\/)(?:node_modules|\.git|\.cache|src|tests|evidence|scripts)\//, `Development/runtime payload in VSIX: ${name}`);
      assert.doesNotMatch(name, /(?:^|\/)(?:java|javac|mvn|mvnw|mule)(?:\.cmd|\.bat|\.sh)?$/i, `Runtime tool in VSIX: ${name}`);
    }
    for (const filename of ['dist/extension.js', 'media/studio.js']) {
      const packaged = entries.get(`extension/${filename}`);
      assert.deepEqual(packaged, fs.readFileSync(path.join(project, filename)), `VSIX contains stale ${filename}`);
      examineModule(packaged.toString('utf8'), `extension/${filename}`, false);
    }
    return { filename, bytes: buffer.length, entries: entries.size, sha256: createHash('sha256').update(buffer).digest('hex'), executableModules: ['extension/dist/extension.js', 'extension/media/studio.js'], bundledRuntimeArtifacts: 0 };
  });
}

const failed = checks.filter(check => check.status === 'FAIL');
console.log(JSON.stringify({
  status: failed.length ? 'FAIL' : 'PASS', mode: sourceOnly ? 'source-only' : 'source-bundle-vsix',
  generatedAt: new Date().toISOString(), platform: process.platform, architecture: process.arch, node: process.version,
  passed: checks.length - failed.length, failed: failed.length, checks,
  limitations: [
    'This audit inspects executable imports, AST calls, manifest contributions, CSP/resource boundaries and packaged bytes. It does not claim to prove every possible JavaScript behavior.',
    'Installed-host tests and interaction/network observations are separate evidence.',
    'No claims about old installations are inferred from this source audit; source/import/package isolation is verified here.',
    ...(sourceOnly ? ['Built bundles and VSIX were not inspected in this source-only invocation.'] : []),
  ],
}, null, 2));
process.exitCode = failed.length ? 1 : 0;
