/** Namespace-aware XML projection and lossless edits for the bounded real editor.
 * SAX validates well-formed XML. The lexical tree retains original source spans;
 * visual edits change those spans, never serialize a recognized-field subset.
 */
import { parser as saxParser } from 'sax';

export const CORE_NS = 'http://www.mulesoft.org/schema/mule/core';
export const HTTP_NS = 'http://www.mulesoft.org/schema/mule/http';
export const DOC_NS = 'http://www.mulesoft.org/schema/mule/documentation';
export const XSI_NS = 'http://www.w3.org/2001/XMLSchema-instance';
export const MAX_XML_BYTES = 1024 * 1024;
export const LOG_LEVELS_REAL = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR'] as const;
export type RealLogLevel = typeof LOG_LEVELS_REAL[number];
export interface XmlDiagnostic { message: string; severity: 'error' | 'warning'; start: number; end: number; code: string }
export interface RealHttpConfig { id: string; name: string; protocol: string; host: string; port: string; basePath: string; editable: boolean }
export interface RealListener {
  id: string; kind: 'listener'; displayName: string; configId: string; path: string; allowedMethods: string; notes: string;
  responseStatus: string; responseBody: string; responseHeaders: string; editable: boolean;
}
export interface RealLogger {
  id: string; kind: 'logger'; displayName: string; message: string; level: RealLogLevel; category: string;
  expressionMode: boolean; notes: string; editable: boolean;
}
export type RealComponent = RealListener | RealLogger;
export interface RealFlow { id: string; name: string; source: RealListener | null; processors: RealLogger[]; editable: boolean; unsupported: string[] }
export interface IntegrationModel { valid: boolean; flows: RealFlow[]; configs: RealHttpConfig[]; diagnostics: XmlDiagnostic[] }
export interface ComponentValues {
  displayName?: string; configId?: string; path?: string; allowedMethods?: string; message?: string;
  level?: string; category?: string; responseStatus?: string; responseBody?: string; responseHeaders?: string;
}
export interface ConfigValues { name: string; protocol: 'HTTP'; host: string; port: string; basePath: string }
export type XmlOperation =
  | { kind: 'createFlow'; name: string }
  | { kind: 'renameFlow'; flowId: string; name: string }
  | { kind: 'insert'; flowId: string; component: 'listener' | 'logger'; index?: number }
  | { kind: 'updateComponent'; id: string; values: ComponentValues }
  | { kind: 'deleteComponent'; id: string }
  | { kind: 'duplicateLogger'; id: string }
  | { kind: 'moveLogger'; id: string; direction: -1 | 1 }
  | { kind: 'saveConfig'; id?: string; values: ConfigValues }
  | { kind: 'deleteConfig'; id: string };
export interface SourceEdit { start: number; end: number; text: string }
export interface XmlEditResult { text: string; edits: SourceEdit[]; selectionId?: string }
interface Attribute { name: string; value: string; start: number; end: number; valueStart: number; valueEnd: number; quote: string }
interface Element {
  name: string; local: string; uri: string; start: number; openEnd: number; closeStart: number; end: number;
  selfClosing: boolean; attributes: Attribute[]; namespaces: Map<string, string>; children: Element[]; parent?: Element;
}
interface Parsed { model: IntegrationModel; root?: Element; all: Element[]; nodes: Map<string, Element>; flowNodes: Map<string, Element>; configNodes: Map<string, Element> }
const space = (c: string | undefined): boolean => c === ' ' || c === '\t' || c === '\r' || c === '\n';
const byteLength = (s: string): number => new TextEncoder().encode(s).length;
const escapedAttribute = (s: string, quote = '"'): string => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/\r/g, '&#13;').replace(/\n/g, '&#10;').replace(/\t/g, '&#9;').replace(quote === '"' ? /"/g : /'/g, quote === '"' ? '&quot;' : '&apos;');
const escapedText = (s: string): string => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
function decode(s: string): string {
  return s.replace(/&(#x[0-9a-fA-F]+|#[0-9]+|amp|lt|gt|quot|apos);/g, (_, entity: string) => {
    if (entity.startsWith('#x')) return String.fromCodePoint(parseInt(entity.slice(2), 16));
    if (entity.startsWith('#')) return String.fromCodePoint(parseInt(entity.slice(1), 10));
    return ({ amp: '&', lt: '<', gt: '>', quot: '"', apos: "'" } as Record<string, string>)[entity];
  });
}
function attribute(node: Element, name: string): Attribute | undefined { return node.attributes.find(a => a.name === name); }
function value(node: Element, name: string, fallback = ''): string { return attribute(node, name)?.value ?? fallback; }
function namespacedAttribute(node: Element, ns: string, local: string): Attribute | undefined {
  return node.attributes.find(a => { const colon = a.name.indexOf(':'); return colon > 0 && a.name.slice(colon + 1) === local && node.namespaces.get(a.name.slice(0, colon)) === ns; });
}
function child(node: Element, ns: string, local: string): Element | undefined { return node.children.find(n => n.uri === ns && n.local === local); }
function is(node: Element, ns: string, local: string): boolean { return node.uri === ns && node.local === local; }
function content(text: string, node: Element | undefined): string {
  if (!node || node.selfClosing) return '';
  let out = '', i = node.openEnd;
  while (i < node.closeStart) {
    if (text.startsWith('<![CDATA[', i)) { const end = text.indexOf(']]>', i + 9); out += text.slice(i + 9, end); i = end + 3; }
    else if (text.startsWith('<!--', i)) { i = text.indexOf('-->', i + 4) + 3; }
    else { const next = text.indexOf('<', i); const end = next < 0 ? node.closeStart : Math.min(next, node.closeStart); if (end === i) break; out += decode(text.slice(i, end)); i = end; }
  }
  return out.trim();
}

/** The scanner is a lexical parser, not regular-expression XML replacement. */
function lexicalTree(text: string): { root?: Element; all: Element[]; rootCount: number } {
  const stack: Element[] = [], all: Element[] = [];
  let root: Element | undefined, rootCount = 0, i = 0;
  while (i < text.length) {
    if (text[i] !== '<') { i++; continue; }
    if (text.startsWith('<!--', i)) { i = text.indexOf('-->', i + 4) + 3; continue; }
    if (text.startsWith('<![CDATA[', i)) { i = text.indexOf(']]>', i + 9) + 3; continue; }
    if (text.startsWith('<?', i)) { i = text.indexOf('?>', i + 2) + 2; continue; }
    if (text.startsWith('</', i)) { const node = stack.pop()!; node.closeStart = i; node.end = text.indexOf('>', i + 2) + 1; i = node.end; continue; }
    const start = i++;
    const nameStart = i;
    while (i < text.length && !space(text[i]) && text[i] !== '/' && text[i] !== '>') i++;
    const name = text.slice(nameStart, i), attributes: Attribute[] = [];
    while (i < text.length) {
      const attrStart = i;
      while (space(text[i])) i++;
      if (text[i] === '/' || text[i] === '>') break;
      const a = i;
      while (i < text.length && !space(text[i]) && text[i] !== '=') i++;
      const attrName = text.slice(a, i);
      while (space(text[i])) i++;
      i++; while (space(text[i])) i++;
      const quote = text[i++], valueStart = i;
      while (i < text.length && text[i] !== quote) i++;
      const valueEnd = i;
      i++;
      attributes.push({ name: attrName, value: decode(text.slice(valueStart, valueEnd).replace(/[\t\r\n]/g, ' ')), start: attrStart, end: i, valueStart, valueEnd, quote });
    }
    const selfClosing = text[i] === '/';
    i += selfClosing ? 2 : 1;
    const parent = stack[stack.length - 1], namespaces = new Map(parent?.namespaces ?? [['xml', 'http://www.w3.org/XML/1998/namespace']]);
    for (const a of attributes) { if (a.name === 'xmlns') namespaces.set('', a.value); else if (a.name.startsWith('xmlns:')) namespaces.set(a.name.slice(6), a.value); }
    const colon = name.indexOf(':'), prefix = colon < 0 ? '' : name.slice(0, colon), local = colon < 0 ? name : name.slice(colon + 1);
    const node: Element = { name, local, uri: namespaces.get(prefix) ?? '', start, openEnd: i, closeStart: selfClosing ? i - 2 : -1, end: selfClosing ? i : -1, selfClosing, attributes, namespaces, children: [], parent };
    if (parent) parent.children.push(node); else { root = node; rootCount++; }
    all.push(node);
    if (!selfClosing) stack.push(node);
  }
  return { root, all, rootCount };
}

function validXmlCharacters(text: string): boolean {
  for (const character of text) { const c = character.codePointAt(0)!; if (!(c === 9 || c === 10 || c === 13 || c >= 0x20 && c <= 0xd7ff || c >= 0xe000 && c <= 0xfffd || c >= 0x10000 && c <= 0x10ffff)) return false; }
  return true;
}

function parseInternal(text: string): Parsed {
  const model: IntegrationModel = { valid: false, flows: [], configs: [], diagnostics: [] };
  const parsed: Parsed = { model, all: [], nodes: new Map(), flowNodes: new Map(), configNodes: new Map() };
  const issue = (message: string, start = 0, end = start + 1, code = 'xml', severity: 'error' | 'warning' = 'error') => model.diagnostics.push({ message, start, end: Math.min(text.length, end), code, severity });
  if (byteLength(text) > MAX_XML_BYTES) { issue('This integration exceeds the 1 MiB editor limit. Use the ordinary text editor to reduce it.'); return parsed; }
  if (!validXmlCharacters(text)) { issue('XML contains an invalid control character or unpaired surrogate.'); return parsed; }
  const validator = saxParser(true, { xmlns: true, position: true, strictEntities: true });
  let failure: Error | undefined;
  validator.ondoctype = () => { throw new Error('DTD/entity declarations are outside this editor’s supported scope. Source is preserved; visual edits and execution are disabled.'); };
  validator.ontext = value => { if (!validXmlCharacters(value)) throw new Error('XML character reference resolves to an invalid control character.'); };
  validator.onerror = error => { failure = error; throw error; };
  try { validator.write(text).close(); } catch (error) { failure = error as Error; }
  if (failure) { const pos = Math.max(0, Math.min(text.length - 1, validator.position - 1)); issue(failure.message.split('\n')[0], pos); return parsed; }
  let tree: ReturnType<typeof lexicalTree>;
  try { tree = lexicalTree(text); } catch { issue('Invalid XML character reference or source token.'); return parsed; }
  if (tree.rootCount !== 1) { issue('An XML document must contain exactly one root element.'); return parsed; }
  for (const node of tree.all) {
    const seen = new Set<string>();
    for (const attr of node.attributes) {
      const colon = attr.name.indexOf(':'), expanded = colon < 0 ? attr.name : (node.namespaces.get(attr.name.slice(0, colon)) ?? attr.name.slice(0, colon)) + ':' + attr.name.slice(colon + 1);
      if (seen.has(expanded)) { issue('Duplicate XML attribute: ' + attr.name, attr.start, attr.end); return parsed; }
      if (!validXmlCharacters(attr.value)) { issue('Invalid XML character reference in attribute.', attr.valueStart, attr.valueEnd); return parsed; }
      seen.add(expanded);
    }
  }
  parsed.root = tree.root; parsed.all = tree.all;
  if (!tree.root || !is(tree.root, CORE_NS, 'mule')) { issue('The root must be mule in the Mule core namespace.'); return parsed; }
  model.valid = true;
  const docIds = new Set<string>();
  for (const n of tree.all) { const id = namespacedAttribute(n, DOC_NS, 'id')?.value; if (id && docIds.has(id)) issue('Duplicate documentation ID: ' + id, n.start, n.openEnd, 'duplicate-id'); if (id) docIds.add(id); }
  const configNames = new Set<string>();
  for (const n of tree.root.children.filter(n => is(n, HTTP_NS, 'listener-config'))) {
    const name = value(n, 'name'), id = 'config:' + name, connection = child(n, HTTP_NS, 'listener-connection');
    const editable = n.children.length === 1 && !!connection && connection.children.length === 0;
    const config: RealHttpConfig = { id, name, protocol: connection ? value(connection, 'protocol', 'HTTP') : 'HTTP', host: connection ? value(connection, 'host') : '', port: connection ? value(connection, 'port') : '', basePath: value(n, 'basePath'), editable };
    model.configs.push(config); parsed.configNodes.set(id, n);
    if (!name || configNames.has(name)) issue('HTTP configuration names must be present and unique: ' + name, n.start, n.openEnd, 'config-name');
    configNames.add(name);
    if (!connection) issue('HTTP Listener configuration is missing its listener-connection.', n.start, n.openEnd, 'config-connection');
    if (config.protocol !== 'HTTP') { config.editable = false; issue('HTTPS/TLS is preserved but outside this milestone.', n.start, n.openEnd, 'unsupported', 'warning'); }
  }
  const flowNames = new Set<string>();
  for (const [flowIndex, n] of tree.root.children.filter(n => is(n, CORE_NS, 'flow')).entries()) {
    const name = value(n, 'name'), id = 'flow:' + name;
    const sources = n.children.filter(c => is(c, HTTP_NS, 'listener'));
    const unsupported = n.children.filter(c => !is(c, HTTP_NS, 'listener') && !is(c, CORE_NS, 'logger')).map(c => c.name);
    const flow: RealFlow = { id, name, source: null, processors: [], editable: unsupported.length === 0 && sources.length <= 1 && (sources.length === 0 || n.children[0] === sources[0]), unsupported };
    model.flows.push(flow); parsed.flowNodes.set(id, n);
    if (!name || flowNames.has(name)) issue('Flow names must be present and unique: ' + name, n.start, n.openEnd, 'flow-name');
    flowNames.add(name);
    if (sources.length > 1 || (sources.length === 1 && n.children[0] !== sources[0])) issue('A flow supports one Listener in its first source position.', n.start, n.openEnd, 'source-position');
    if (unsupported.length) issue('Unsupported flow content is preserved; visual component edits for this flow are disabled: ' + unsupported.join(', '), n.start, n.openEnd, 'unsupported', 'warning');
    for (const [componentIndex, c] of n.children.entries()) {
      const componentId = namespacedAttribute(c, DOC_NS, 'id')?.value ?? `legacy:${flowIndex}:${componentIndex}:${c.local}`;
      if (is(c, HTTP_NS, 'listener')) {
        const response = child(c, HTTP_NS, 'response');
        const safeChildren = c.children.every(v => is(v, HTTP_NS, 'response') || is(v, HTTP_NS, 'error-response')) && c.children.filter(v => is(v, HTTP_NS, 'response')).length <= 1;
        const listener: RealListener = { id: componentId, kind: 'listener', displayName: namespacedAttribute(c, DOC_NS, 'name')?.value ?? 'HTTP Listener', configId: 'config:' + value(c, 'config-ref'), path: value(c, 'path'), allowedMethods: value(c, 'allowedMethods', 'GET'), notes: '', responseStatus: response ? value(response, 'statusCode', '200') : '200', responseBody: content(text, response && child(response, HTTP_NS, 'body')), responseHeaders: content(text, response && child(response, HTTP_NS, 'headers')), editable: flow.editable && safeChildren && (!response || response.children.every(v => is(v, HTTP_NS, 'body') || is(v, HTTP_NS, 'headers')) && response.children.filter(v => is(v, HTTP_NS, 'body')).length <= 1 && response.children.filter(v => is(v, HTTP_NS, 'headers')).length <= 1) };
        flow.source ??= listener; parsed.nodes.set(componentId, c);
        if (!model.configs.some(cfg => cfg.id === listener.configId)) issue('Listener references a missing HTTP configuration: ' + value(c, 'config-ref'), c.start, c.openEnd, 'config-reference');
        if (!listener.path || !dynamic(listener.path) && !listener.path.startsWith('/')) issue('Listener path must begin with / or use a runtime property/expression.', c.start, c.openEnd, 'listener-path');
        if (!dynamic(listener.allowedMethods) && !listener.allowedMethods.split(',').every(method => ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS'].includes(method.trim()))) issue('Use valid comma-separated HTTP methods.', c.start, c.openEnd, 'listener-methods');
        if (!dynamic(listener.responseStatus) && (!/^\d{3}$/.test(listener.responseStatus) || Number(listener.responseStatus) < 100 || Number(listener.responseStatus) > 599)) issue('Response status must be an HTTP code from 100 to 599 or a runtime expression/property.', response?.start ?? c.start, response?.openEnd ?? c.openEnd, 'response-status');
      } else if (is(c, CORE_NS, 'logger')) {
        const rawLevel = value(c, 'level', 'INFO');
        const logger: RealLogger = { id: componentId, kind: 'logger', displayName: namespacedAttribute(c, DOC_NS, 'name')?.value ?? 'Logger', message: value(c, 'message'), level: rawLevel as RealLogLevel, category: value(c, 'category'), expressionMode: value(c, 'message').startsWith('#['), notes: '', editable: flow.editable && c.children.length === 0 };
        flow.processors.push(logger); parsed.nodes.set(componentId, c);
        if (!LOG_LEVELS_REAL.includes(rawLevel as RealLogLevel)) issue('Unsupported Logger level: ' + rawLevel, c.start, c.openEnd, 'logger-level');
        if (!logger.message.trim()) issue('Logger message is required.', c.start, c.openEnd, 'logger-message');
      }
    }
  }
  if (!model.flows.length) issue('Create a flow, then add an HTTP Listener and Logger before building.', tree.root.start, tree.root.openEnd, 'empty-project', 'warning');
  if (model.diagnostics.some(d => ['duplicate-id', 'flow-name', 'config-name'].includes(d.code))) {
    // A stable selector must be unambiguous before any visual source mutation.
    for (const flow of model.flows) { flow.editable = false; if (flow.source) flow.source.editable = false; for (const logger of flow.processors) logger.editable = false; }
    for (const config of model.configs) config.editable = false;
  }
  return parsed;
}

export function parseIntegration(text: string): IntegrationModel { return parseInternal(text).model; }
export function createIntegrationXml(): string {
  return `<?xml version="1.0" encoding="UTF-8"?>\n<mule xmlns="${CORE_NS}"\n      xmlns:http="${HTTP_NS}"\n      xmlns:doc="${DOC_NS}"\n      xmlns:xsi="${XSI_NS}"\n      xsi:schemaLocation="${CORE_NS} ${CORE_NS}/current/mule.xsd ${HTTP_NS} ${HTTP_NS}/current/mule-http.xsd">\n</mule>\n`;
}

export class XmlEditError extends Error { constructor(message: string) { super(message); this.name = 'XmlEditError'; } }
function requireName(name: string, label: string): void { if (!/^[A-Za-z_][A-Za-z0-9_.-]{0,99}$/.test(name)) throw new XmlEditError(label + ' must begin with a letter/underscore and contain only letters, digits, underscores, dots or hyphens (100 characters maximum).'); }
function nonEmpty(s: string, label: string): void { if (!s.trim()) throw new XmlEditError(label + ' is required.'); }
function dynamic(s: string): boolean { return s.includes('${') || s.startsWith('#['); }
function newId(): string { return globalThis.crypto.randomUUID(); }
function prefixFor(node: Element, uri: string): string | undefined { for (const [prefix, ns] of node.namespaces) if (ns === uri && (![DOC_NS, XSI_NS].includes(uri) || prefix !== '')) return prefix; return undefined; }
function qname(node: Element, uri: string, local: string): string { const prefix = prefixFor(node, uri); if (prefix === undefined) throw new XmlEditError('Required namespace is not declared: ' + uri); return prefix ? prefix + ':' + local : local; }
function indentation(text: string, node: Element): string { const line = text.lastIndexOf('\n', node.start - 1) + 1; const lead = text.slice(line, node.start); return /^[\t ]*$/.test(lead) ? lead : ''; }
function eol(text: string): string { return text.includes('\r\n') ? '\r\n' : '\n'; }
function applyEdits(text: string, edits: SourceEdit[]): string {
  const ordered = edits.map((edit, index) => ({ ...edit, index })).sort((a, b) => b.start - a.start || b.end - a.end || b.index - a.index);
  let previous = text.length + 1;
  for (const edit of ordered) { if (edit.start < 0 || edit.end < edit.start || edit.end > text.length || edit.end > previous) throw new XmlEditError('Overlapping source edit refused.'); text = text.slice(0, edit.start) + edit.text + text.slice(edit.end); previous = edit.start; }
  return text;
}
function setAttribute(node: Element, name: string, next: string, edits: SourceEdit[]): void {
  const old = attribute(node, name);
  if (old) { if (old.value !== next) edits.push({ start: old.valueStart, end: old.valueEnd, text: escapedAttribute(next, old.quote) }); }
  else edits.push({ start: node.openEnd - (node.selfClosing ? 2 : 1), end: node.openEnd - (node.selfClosing ? 2 : 1), text: ` ${name}="${escapedAttribute(next)}"` });
}
function setDocAttribute(node: Element, local: string, next: string, edits: SourceEdit[]): void {
  const old = namespacedAttribute(node, DOC_NS, local);
  setAttribute(node, old?.name ?? qname(node, DOC_NS, local), next, edits);
}
function insertChildren(text: string, parent: Element, markup: string, edits: SourceEdit[], before?: Element): void {
  const newline = eol(text), indent = indentation(text, parent), childIndent = indent + '  ';
  if (parent.selfClosing) edits.push({ start: parent.openEnd - 2, end: parent.openEnd, text: '>' + newline + childIndent + markup + newline + indent + '</' + parent.name + '>' });
  else if (before) edits.push({ start: before.start, end: before.start, text: markup + newline + indentation(text, before) });
  else {
    const trailingStart = text.lastIndexOf('\n', parent.closeStart - 1) + 1;
    const ownLine = /^[\t ]*$/.test(text.slice(trailingStart, parent.closeStart));
    edits.push({ start: ownLine ? trailingStart : parent.closeStart, end: ownLine ? trailingStart : parent.closeStart, text: (ownLine ? '' : newline) + childIndent + markup + newline + (ownLine ? '' : indent) });
  }
}
function minimalEdit(original: string, next: string): SourceEdit[] {
  if (original === next) return [];
  let start = 0, before = original.length, after = next.length;
  while (start < before && start < after && original[start] === next[start]) start++;
  while (before > start && after > start && original[before - 1] === next[after - 1]) { before--; after--; }
  return [{ start, end: before, text: next.slice(start, after) }];
}

function ensureNamespaces(text: string, operation: XmlOperation): string {
  const parsed = parseInternal(text), root = parsed.root;
  if (!parsed.model.valid || !root) throw new XmlEditError('Repair XML syntax before visual editing. Source has been preserved.');
  const edits: SourceEdit[] = [];
  const docNeeded = ['insert', 'updateComponent', 'duplicateLogger', 'moveLogger'].includes(operation.kind);
  const httpNeeded = operation.kind === 'saveConfig' || operation.kind === 'insert' && operation.component === 'listener';
  const namespaces: [string, string][] = [...(docNeeded ? [[DOC_NS, 'doc'] as [string,string]] : []), ...(httpNeeded ? [[HTTP_NS, 'http'] as [string,string], [XSI_NS, 'xsi'] as [string,string]] : [])];
  for (const [uri, preferred] of namespaces) {
    if (prefixFor(root, uri) !== undefined) continue;
    let prefix = preferred, suffix = 2;
    while (root.namespaces.has(prefix)) prefix = preferred + suffix++;
    setAttribute(root, 'xmlns:' + prefix, uri, edits);
    root.namespaces.set(prefix, uri);
  }
  if (httpNeeded) {
    const schema = namespacedAttribute(root, XSI_NS, 'schemaLocation'), pairs = (schema?.value ?? '').trim().split(/\s+/).filter(Boolean);
    if (pairs.length % 2) throw new XmlEditError('schemaLocation must contain namespace/location pairs. Repair it in XML first.');
    const namespacesInSchema = new Set(pairs.filter((_, index) => index % 2 === 0));
    if (!namespacesInSchema.has(CORE_NS)) pairs.push(CORE_NS, CORE_NS + '/current/mule.xsd');
    if (!namespacesInSchema.has(HTTP_NS)) pairs.push(HTTP_NS, HTTP_NS + '/current/mule-http.xsd');
    if (!schema || pairs.length !== schema.value.trim().split(/\s+/).length) setAttribute(root, schema?.name ?? qname(root, XSI_NS, 'schemaLocation'), pairs.join(' '), edits);
  }
  return applyEdits(text, edits);
}
function componentAndFlow(parsed: Parsed, id: string): { component: RealComponent; node: Element; flow: RealFlow } {
  for (const flow of parsed.model.flows) { const component = flow.source?.id === id ? flow.source : flow.processors.find(p => p.id === id); if (component) { const node = parsed.nodes.get(id)!; if (!component.editable || !flow.editable) throw new XmlEditError('This component/flow contains unsupported constructs. Edit its XML directly; source is preserved.'); return { component, node, flow }; } }
  throw new XmlEditError('Component no longer exists. Refresh the document and try again.');
}
function supportedFlow(parsed: Parsed, id: string): { flow: RealFlow; node: Element } {
  const flow = parsed.model.flows.find(f => f.id === id), node = parsed.flowNodes.get(id);
  if (!flow || !node) throw new XmlEditError('Select an existing flow.');
  if (!flow.editable) throw new XmlEditError('This flow contains unsupported constructs. Its XML is preserved; visual edits are disabled.');
  return { flow, node };
}
function updateResponse(text: string, listener: Element, values: ComponentValues, edits: SourceEdit[]): void {
  if (values.responseStatus === undefined && values.responseBody === undefined && values.responseHeaders === undefined) return;
  const response = child(listener, HTTP_NS, 'response'), newline = eol(text), indent = indentation(text, listener);
  if (!response) {
    const body = values.responseBody ?? '#[payload]', headers = values.responseHeaders ?? "#[{'Content-Type': 'text/plain; charset=UTF-8'}]";
    const markup = `<${qname(listener, HTTP_NS, 'response')} statusCode="${escapedAttribute(values.responseStatus ?? '200')}">${newline}${indent}    <${qname(listener, HTTP_NS, 'body')}>${escapedText(body)}</${qname(listener, HTTP_NS, 'body')}>${newline}${indent}    <${qname(listener, HTTP_NS, 'headers')}>${escapedText(headers)}</${qname(listener, HTTP_NS, 'headers')}>${newline}${indent}  </${qname(listener, HTTP_NS, 'response')}>`;
    insertChildren(text, listener, markup, edits); return;
  }
  if (values.responseStatus !== undefined) setAttribute(response, 'statusCode', values.responseStatus, edits);
  for (const [field, local] of [['responseBody', 'body'], ['responseHeaders', 'headers']] as const) {
    const next = values[field]; if (next === undefined) continue;
    const node = child(response, HTTP_NS, local);
    if (node && !node.selfClosing) edits.push({ start: node.openEnd, end: node.closeStart, text: escapedText(next) });
    else if (node) edits.push({ start: node.openEnd - 2, end: node.end, text: '>' + escapedText(next) + '</' + node.name + '>' });
    else insertChildren(text, response, `<${qname(response, HTTP_NS, local)}>${escapedText(next)}</${qname(response, HTTP_NS, local)}>`, edits);
  }
}

export function applyOperation(original: string, operation: XmlOperation): XmlEditResult {
  let text = ensureNamespaces(original, operation);
  const parsed = parseInternal(text), root = parsed.root!, edits: SourceEdit[] = [];
  if (parsed.model.diagnostics.some(d => ['duplicate-id', 'flow-name', 'config-name'].includes(d.code))) throw new XmlEditError('Repair duplicate or missing names/documentation IDs in XML before visual editing. Ambiguous source targets are never changed.');
  let selectionId: string | undefined;
  if (operation.kind === 'createFlow') {
    requireName(operation.name, 'Flow name');
    if (parsed.model.flows.some(f => f.name === operation.name)) throw new XmlEditError('A flow with this name already exists.');
    insertChildren(text, root, `<${qname(root, CORE_NS, 'flow')} name="${escapedAttribute(operation.name)}">${eol(text)}  </${qname(root, CORE_NS, 'flow')}>`, edits);
    selectionId = 'flow:' + operation.name;
  } else if (operation.kind === 'renameFlow') {
    const { node } = supportedFlow(parsed, operation.flowId);
    requireName(operation.name, 'Flow name');
    if (parsed.model.flows.some(f => f.id !== operation.flowId && f.name === operation.name)) throw new XmlEditError('A flow with this name already exists.');
    if (parsed.all.some(n => is(n, CORE_NS, 'flow-ref') && value(n, 'name') === value(node, 'name'))) throw new XmlEditError('This flow is referenced. Rename it in XML with all references reviewed.');
    setAttribute(node, 'name', operation.name, edits); selectionId = 'flow:' + operation.name;
  } else if (operation.kind === 'insert') {
    const { node, flow } = supportedFlow(parsed, operation.flowId);
    selectionId = newId();
    const docName = qname(node, DOC_NS, 'name'), docId = qname(node, DOC_NS, 'id');
    if (operation.component === 'listener') {
      if (flow.source) throw new XmlEditError('This flow already has a Listener. A flow accepts one source.');
      const cfg = parsed.model.configs.find(c => c.editable && c.protocol === 'HTTP');
      const newline = eol(text), indent = indentation(text, node) + '  ', q = (name: string) => qname(node, HTTP_NS, name);
      const markup = `<${q('listener')} config-ref="${escapedAttribute(cfg?.name ?? '')}" path="/hello" allowedMethods="GET" ${docName}="HTTP Listener" ${docId}="${selectionId}">${newline}${indent}  <${q('response')} statusCode="200">${newline}${indent}    <${q('body')}>${escapedText("#['Hello from ZERNOVA']")}</${q('body')}>${newline}${indent}    <${q('headers')}>${escapedText("#[{'Content-Type': 'text/plain; charset=UTF-8'}]")}</${q('headers')}>${newline}${indent}  </${q('response')}>${newline}${indent}</${q('listener')}>`;
      insertChildren(text, node, markup, edits, node.children[0]);
    } else {
      if (flow.processors.length >= 100) throw new XmlEditError('This milestone supports up to 100 Loggers per flow.');
      const markup = `<${qname(node, CORE_NS, 'logger')} ${docName}="Logger" ${docId}="${selectionId}" level="INFO" category="io.zernova.acceptance" message="${escapedAttribute("#['ZERNOVA_TEST ' ++ (attributes.queryParams.testId default 'missing')]")}"/>`;
      const index = operation.index ?? flow.processors.length;
      if (!Number.isInteger(index) || index < 0 || index > flow.processors.length) throw new XmlEditError('Invalid Logger insertion position.');
      insertChildren(text, node, markup, edits, flow.processors[index] ? parsed.nodes.get(flow.processors[index].id) : undefined);
    }
  } else if (operation.kind === 'updateComponent') {
    const { component, node } = componentAndFlow(parsed, operation.id), next = operation.values;
    const allowed = component.kind === 'listener' ? ['displayName', 'configId', 'path', 'allowedMethods', 'responseStatus', 'responseBody', 'responseHeaders'] : ['displayName', 'message', 'level', 'category'];
    if (Object.keys(next).some(k => !allowed.includes(k))) throw new XmlEditError('This field does not belong to the selected component.');
    selectionId = namespacedAttribute(node, DOC_NS, 'id')?.value ?? newId();
    setDocAttribute(node, 'id', selectionId, edits);
    // A native text document also owns incomplete form input. Semantic issues
    // are diagnostics; rejecting normal intermediate keystrokes would leave a
    // visible draft that does not match the XML used by Save/Build/Run.
    if (next.displayName !== undefined) setDocAttribute(node, 'name', next.displayName, edits);
    if (component.kind === 'listener') {
      if (next.configId !== undefined) { if (next.configId !== '' && !next.configId.startsWith('config:')) throw new XmlEditError('Select an HTTP Listener configuration.'); setAttribute(node, 'config-ref', next.configId.slice('config:'.length), edits); }
      if (next.path !== undefined) setAttribute(node, 'path', next.path, edits);
      if (next.allowedMethods !== undefined) setAttribute(node, 'allowedMethods', next.allowedMethods, edits);
      updateResponse(text, node, next, edits);
    } else {
      if (next.message !== undefined) setAttribute(node, 'message', next.message, edits);
      if (next.level !== undefined) { if (!LOG_LEVELS_REAL.includes(next.level as RealLogLevel)) throw new XmlEditError('Choose TRACE, DEBUG, INFO, WARN or ERROR.'); setAttribute(node, 'level', next.level, edits); }
      if (next.category !== undefined) setAttribute(node, 'category', next.category, edits);
    }
  } else if (operation.kind === 'deleteComponent') {
    const { node } = componentAndFlow(parsed, operation.id); edits.push({ start: node.start, end: node.end, text: '' });
  } else if (operation.kind === 'duplicateLogger') {
    const { component, node, flow } = componentAndFlow(parsed, operation.id);
    if (component.kind !== 'logger') throw new XmlEditError('Only a Logger can be duplicated.');
    if (flow.processors.length >= 100) throw new XmlEditError('This milestone supports up to 100 Loggers per flow.');
    selectionId = newId();
    const local: SourceEdit[] = [];
    setDocAttribute(node, 'id', selectionId, local);
    const snippet = applyEdits(text.slice(node.start, node.end), local.map(e => ({ ...e, start: e.start - node.start, end: e.end - node.start })));
    edits.push({ start: node.end, end: node.end, text: eol(text) + indentation(text, node) + snippet });
  } else if (operation.kind === 'moveLogger') {
    const { component, node, flow } = componentAndFlow(parsed, operation.id);
    if (component.kind !== 'logger') throw new XmlEditError('Only a Logger can be reordered.');
    const index = flow.processors.findIndex(p => p.id === component.id), adjacent = flow.processors[index + operation.direction];
    if (!adjacent) throw new XmlEditError('The Logger is already at that edge of the processor lane.');
    const other = parsed.nodes.get(adjacent.id)!;
    const stabilized = (target: Element, id: string): string => { const changes: SourceEdit[] = []; setDocAttribute(target, 'id', id, changes); return applyEdits(text.slice(target.start, target.end), changes.map(e => ({ ...e, start: e.start - target.start, end: e.end - target.start }))); };
    selectionId = namespacedAttribute(node, DOC_NS, 'id')?.value ?? newId();
    const otherId = namespacedAttribute(other, DOC_NS, 'id')?.value ?? newId();
    edits.push({ start: node.start, end: node.end, text: stabilized(other, otherId) }, { start: other.start, end: other.end, text: stabilized(node, selectionId) });
  } else if (operation.kind === 'saveConfig') {
    const cfg = operation.values;
    requireName(cfg.name, 'Configuration name'); nonEmpty(cfg.host, 'Host'); nonEmpty(cfg.port, 'Port');
    if (cfg.protocol !== 'HTTP') throw new XmlEditError('Only HTTP is supported in this milestone.');
    if (/\s/.test(cfg.host)) throw new XmlEditError('Host cannot contain whitespace.');
    if (!dynamic(cfg.port) && (!/^\d+$/.test(cfg.port) || Number(cfg.port) < 1024 || Number(cfg.port) > 65535)) throw new XmlEditError('Use an unprivileged port from 1024 to 65535 or a runtime property.');
    if (cfg.basePath && !dynamic(cfg.basePath) && !cfg.basePath.startsWith('/')) throw new XmlEditError('Base path must be empty or begin with /.');
    if (parsed.model.configs.some(c => c.id !== operation.id && c.name === cfg.name)) throw new XmlEditError('A configuration with this name already exists.');
    selectionId = 'config:' + cfg.name;
    if (operation.id) {
      const config = parsed.model.configs.find(c => c.id === operation.id), node = parsed.configNodes.get(operation.id);
      if (!config || !node || !config.editable) throw new XmlEditError('Configuration is missing or contains unsupported settings; use XML for those settings.');
      const connection = child(node, HTTP_NS, 'listener-connection')!;
      if (config.name !== cfg.name) {
        const references = parsed.all.filter(n => value(n, 'config-ref') === config.name);
        if (references.some(n => !is(n, HTTP_NS, 'listener'))) throw new XmlEditError('Configuration has references outside the supported Listener scope; rename refused.');
        for (const reference of references) setAttribute(reference, 'config-ref', cfg.name, edits);
      }
      setAttribute(node, 'name', cfg.name, edits); setAttribute(node, 'basePath', cfg.basePath, edits);
      setAttribute(connection, 'protocol', cfg.protocol, edits); setAttribute(connection, 'host', cfg.host, edits); setAttribute(connection, 'port', cfg.port, edits);
    } else {
      const q = (name: string) => qname(root, HTTP_NS, name), newline = eol(text);
      insertChildren(text, root, `<${q('listener-config')} name="${escapedAttribute(cfg.name)}" basePath="${escapedAttribute(cfg.basePath)}">${newline}    <${q('listener-connection')} protocol="HTTP" host="${escapedAttribute(cfg.host)}" port="${escapedAttribute(cfg.port)}"/>${newline}  </${q('listener-config')}>`, edits, root.children.find(n => is(n, CORE_NS, 'flow')));
    }
  } else if (operation.kind === 'deleteConfig') {
    const cfg = parsed.model.configs.find(c => c.id === operation.id), node = parsed.configNodes.get(operation.id);
    if (!cfg || !node) throw new XmlEditError('Configuration no longer exists.');
    if (!cfg.editable) throw new XmlEditError('Unsupported configuration is preserved. Delete it explicitly in XML after reviewing its references.');
    if (parsed.all.some(n => value(n, 'config-ref') === cfg.name)) throw new XmlEditError('Configuration is referenced. Select another configuration or remove its Listeners first.');
    edits.push({ start: node.start, end: node.end, text: '' });
  }
  text = applyEdits(text, edits);
  const check = parseIntegration(text);
  if (!check.valid) throw new XmlEditError('The operation would produce invalid XML; no source was changed.');
  return { text, edits: minimalEdit(original, text), selectionId };
}

/** Conservative references used by the host’s project-wide rename/delete guard. */
export function findConfigurationReferences(text: string, name: string): { valid: boolean; references: number } {
  const parsed = parseInternal(text);
  return { valid: parsed.model.valid, references: parsed.all.filter(node => value(node, 'config-ref') === name).length };
}
