import { coreDefinition, coreElement, acceptsChild } from './coreCatalog';
/** Namespace-aware XML projection and lossless edits for the bounded real editor.
 * SAX validates well-formed XML. The lexical tree retains original source spans;
 * visual edits change those spans, never serialize a recognized-field subset.
 */
import { parser as saxParser } from 'sax';

export const CORE_NS = 'https://schemas.zernova.dev/core';
export const TRANSFORM_NS = 'https://schemas.zernova.dev/transform';
export const HTTP_NS = 'https://schemas.zernova.dev/http';
export const DOC_NS = 'https://schemas.zernova.dev/documentation';
export const XSI_NS = 'http://www.w3.org/2001/XMLSchema-instance';
export const MAX_XML_BYTES = 1024 * 1024;
export const LOG_LEVELS_REAL = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR'] as const;
export type RealLogLevel = typeof LOG_LEVELS_REAL[number];
export interface XmlDiagnostic { message: string; severity: 'error' | 'warning'; start: number; end: number; code: string }
export interface RealHttpConfig { id: string; name: string; protocol: string; host: string; port: string; basePath: string; editable: boolean }
export interface RealListener {
  id: string; kind: 'listener'; displayName: string; configId: string; path: string; allowedMethods: string; notes: string;
  errorStatus: string; errorBody: string; errorHeaders: string; responseStatus: string; responseBody: string; responseHeaders: string; editable: boolean;
}
export interface RealLogger {
  id: string; kind: 'logger'; displayName: string; message: string; level: RealLogLevel; category: string;
  expressionMode: boolean; notes: string; editable: boolean;
}
export interface RealTransform {
  id: string; kind: 'transform'; displayName: string; script: string; mimeType: string; attributesScript?: string; variablesScript?: string; payloadResource?: string; attributesResource?: string; variablesResource?: string; editable: boolean;
}
export interface RealCore {id:string;kind:'core';coreType:string;displayName:string;values:Record<string,string>;children:RealProcessor[];editable:boolean;}
export interface RealScheduler {id:string;kind:'scheduler';displayName:string;values:Record<string,string>;editable:boolean;}
export type RealProcessor = RealLogger | RealTransform | RealCore;
export type RealComponent = RealListener | RealScheduler | RealProcessor;
export interface RealErrorHandler { component?:RealCore; type: string; summary: string; editable: boolean; processors: RealProcessor[] }
export interface RealFlow { id: string; name: string; kind?: 'flow' | 'sub-flow'; source: RealListener | RealScheduler | null; processors: RealProcessor[]; editable: boolean; unsupported: string[]; errorHandler?: RealErrorHandler }
export interface RealPropertyFile { id:string; file:string; }
export interface RealGlobalProperty { id:string; name:string; value:string; }
export interface IntegrationModel { properties?:RealPropertyFile[]; globalProperties?:RealGlobalProperty[]; valid: boolean; flows: RealFlow[]; configs: RealHttpConfig[]; diagnostics: XmlDiagnostic[] }
export interface ComponentValues {
  displayName?: string; configId?: string; path?: string; allowedMethods?: string; message?: string;
  script?: string; mimeType?: string; payloadResource?: string; attributesResource?: string; variablesResource?: string; errorStatus?: string; errorBody?: string; errorHeaders?: string; attributesScript?: string; variablesScript?: string; level?: string; category?: string; responseStatus?: string; responseBody?: string; responseHeaders?: string;
}
export interface ConfigValues { name: string; protocol: 'HTTP'; host: string; port: string; basePath: string }
export type XmlOperation =
  | { kind:'savePropertyFile'; id?:string; file:string }
  | { kind:'saveGlobalProperty'; name:string; value:string }
  | { kind:'deletePropertyFile'; id:string }
  | { kind:'deleteGlobalProperty'; id:string }
  | { kind: 'createFlow'; name: string; flowKind?:'flow'|'sub-flow' }
  | { kind:'updateCore';id:string;values:Record<string,string> }
  | { kind: 'renameFlow'; flowId: string; name: string }
  | { kind: 'insert'; flowId: string; component: string; index?: number; lane?: 'error'; parentId?:string }
  | { kind: 'createErrorHandler'; flowId: string }
  | { kind: 'updateComponent'; id: string; values: ComponentValues }
  | { kind: 'deleteComponent'; id: string }
  | { kind: 'duplicateLogger' | 'duplicateProcessor'; id: string }
  | { kind: 'moveLogger' | 'moveProcessor'; id: string; direction: -1 | 1 }
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

/** Explicit legacy import. Edit only XML names and namespace/schema attributes;
 * comments, CDATA, expressions and ordinary attribute values remain untouched. */
export function convertLegacyIntegrationXml(text: string): string {
  if (byteLength(text) > MAX_XML_BYTES || /<!DOCTYPE|<!ENTITY/i.test(text) || !validXmlCharacters(text)) throw new XmlEditError('Legacy XML exceeds the supported size or uses unsupported declarations.');
  saxParser(true, { xmlns: true, strictEntities: true }).write(text).close();
  const tree = lexicalTree(text), root = tree.root;
  const legacy = 'http://www.mulesoft.org/schema/mule';
  if (!root || tree.rootCount !== 1 || root.uri !== legacy + '/core' || root.local !== 'mule') throw new XmlEditError('Expected one legacy Mule XML root.');
  const edits: SourceEdit[] = [];
  const prefix = root.name.includes(':') ? root.name.slice(0, root.name.indexOf(':') + 1) : '';
  edits.push({ start: root.start + 1, end: root.start + 1 + root.name.length, text: prefix + 'zernova' });
  if (!root.selfClosing) edits.push({ start: root.closeStart + 2, end: root.closeStart + 2 + root.name.length, text: prefix + 'zernova' });
  for (const node of tree.all) for (const attr of node.attributes) {
    if (attr.name === 'xmlns' || attr.name.startsWith('xmlns:') || attr.name.endsWith(':schemaLocation')) {
      let next = attr.value.split(legacy).join('https://schemas.zernova.dev');
      next = next.replace(/\/mule((?:-[\w-]+)?\.xsd)/g, '/zernova$1');
      if (next !== attr.value) edits.push({ start: attr.valueStart, end: attr.valueEnd, text: escapedAttribute(next, attr.quote) });
    }
  }
  for (const edit of edits.sort((a, b) => b.start - a.start)) text = text.slice(0, edit.start) + edit.text + text.slice(edit.end);
  if (!parseIntegration(text).valid) throw new XmlEditError('Converted XML did not pass ZERNOVA parsing.');
  return text;
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
  if (!tree.root || !is(tree.root, CORE_NS, 'zernova')) { issue('The root must be zernova in the ZERNOVA core namespace.'); return parsed; }
  model.valid = true;
  const docIds = new Set<string>();
  for (const n of tree.all) { const id = namespacedAttribute(n, DOC_NS, 'id')?.value; if (id && docIds.has(id)) issue('Duplicate documentation ID: ' + id, n.start, n.openEnd, 'duplicate-id'); if (id) docIds.add(id); }
  model.properties=[];model.globalProperties=[];
  for(const [index,n] of tree.root.children.filter(n=>is(n,CORE_NS,'configuration-properties')).entries()){
    const id='property-file:'+index;model.properties.push({id,file:value(n,'file')});parsed.configNodes.set(id,n);
  }
  for(const n of tree.root.children.filter(n=>is(n,CORE_NS,'global-property'))){
    const name=value(n,'name'),id='global-property:'+name;model.globalProperties.push({id,name,value:value(n,'value')});parsed.configNodes.set(id,n);
  }
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
  for (const [flowIndex, n] of tree.root.children.filter(n => is(n, CORE_NS, 'flow') || is(n, CORE_NS, 'sub-flow')).entries()) {
    const name = value(n, 'name'), id = 'flow:' + name;
    const sources = n.children.filter(c => is(c, HTTP_NS, 'listener')||is(c,CORE_NS,'scheduler'));
    const unsupported = n.children.filter(c => !is(c, HTTP_NS, 'listener') && !is(c, CORE_NS, 'logger') && !is(c, CORE_NS, 'set-payload') && !is(c, TRANSFORM_NS, 'message') && !is(c, CORE_NS, 'error-handler')&&!is(c,CORE_NS,'scheduler')&&!coreElement(c.uri,c.local)).map(c => c.name);
    const flow: RealFlow = { id, name, kind: n.local as 'flow' | 'sub-flow', source: null, processors: [], editable: unsupported.length === 0 && sources.length <= 1 && (sources.length === 0 || n.children[0] === sources[0]) && (n.local !== 'sub-flow' || sources.length === 0), unsupported };
    model.flows.push(flow); parsed.flowNodes.set(id, n);
    if (!name || flowNames.has(name)) issue('Flow names must be present and unique: ' + name, n.start, n.openEnd, 'flow-name');
    flowNames.add(name);
    if (sources.length > 1 || (sources.length === 1 && n.children[0] !== sources[0])) issue('A flow supports one Listener in its first source position.', n.start, n.openEnd, 'source-position');
    if (unsupported.length) issue('Unsupported flow content is preserved; visual component edits for this flow are disabled: ' + unsupported.join(', '), n.start, n.openEnd, 'unsupported', 'warning');
    const handler = child(n, CORE_NS, 'error-handler'), onError = handler?.children[0];
    if (handler) flow.errorHandler = { type: onError?.local ?? 'custom', summary: handler.children.map(c => c.name).join(', '), editable: handler.children.length === 1 && !!onError && [CORE_NS].includes(onError.uri) && ['on-error-propagate', 'on-error-continue'].includes(onError.local) && onError.children.every(c => is(c, CORE_NS, 'logger') || is(c, CORE_NS, 'set-payload') || is(c, TRANSFORM_NS, 'message')), processors: [] };
    const components = [...n.children, ...(flow.errorHandler?.editable ? onError!.children : [])];
    for (const [componentIndex, c] of components.entries()) {
      const componentId = namespacedAttribute(c, DOC_NS, 'id')?.value ?? `legacy:${flowIndex}:${componentIndex}:${c.local}`;
      if(is(c,CORE_NS,'scheduler')){
        const scheduling=child(c,CORE_NS,'scheduling-strategy');const strategy=scheduling?.children[0];
        const schedulerSafe=c.children.length===1&&scheduling?.children.length===1&&strategy?.uri===CORE_NS&&['fixed-frequency','cron'].includes(strategy.local)&&strategy.children.length===0&&strategy.attributes.every(a=>a.name==='xmlns'||a.name.startsWith('xmlns:')||['frequency','timeUnit','startDelay','expression','timeZone'].includes(a.name)); 
        flow.source={id:componentId,kind:'scheduler',displayName:namespacedAttribute(c,DOC_NS,'name')?.value??'Scheduler',values:{frequency:strategy?value(strategy,'frequency','1000'):'1000',timeUnit:strategy?value(strategy,'timeUnit','MILLISECONDS'):'MILLISECONDS',startDelay:strategy?value(strategy,'startDelay','0'):'0',cron:strategy?.local==='cron'?value(strategy,'expression'):'',timeZone:strategy?.local==='cron'?value(strategy,'timeZone'):'',disallowConcurrentExecution:value(c,'disallowConcurrentExecution','true')},editable:flow.editable&&!!schedulerSafe};parsed.nodes.set(componentId,c);
      }else if (is(c, HTTP_NS, 'listener')) {
        const response = child(c, HTTP_NS, 'response'), errorResponse=child(c,HTTP_NS,'error-response');
        const safeChildren = c.children.filter(v=>is(v,HTTP_NS,'error-response')).length<=1 && c.children.every(v => is(v, HTTP_NS, 'response') || is(v, HTTP_NS, 'error-response')) && c.children.filter(v => is(v, HTTP_NS, 'response')).length <= 1;
        const listener: RealListener = { id: componentId, kind: 'listener', displayName: namespacedAttribute(c, DOC_NS, 'name')?.value ?? 'HTTP Listener', configId: 'config:' + value(c, 'config-ref'), path: value(c, 'path'), allowedMethods: value(c, 'allowedMethods', 'GET'), notes: '', errorStatus:errorResponse?value(errorResponse,'statusCode','500'):'500',errorBody:content(text,errorResponse&&child(errorResponse,HTTP_NS,'body')),errorHeaders:content(text,errorResponse&&child(errorResponse,HTTP_NS,'headers')), responseStatus: response ? value(response, 'statusCode', '200') : '200', responseBody: content(text, response && child(response, HTTP_NS, 'body')), responseHeaders: content(text, response && child(response, HTTP_NS, 'headers')), editable: flow.editable && safeChildren && (!errorResponse || errorResponse.children.every(v=>is(v,HTTP_NS,'body')||is(v,HTTP_NS,'headers')) && errorResponse.children.filter(v=>is(v,HTTP_NS,'body')).length<=1 && errorResponse.children.filter(v=>is(v,HTTP_NS,'headers')).length<=1) && (!response || response.children.every(v => is(v, HTTP_NS, 'body') || is(v, HTTP_NS, 'headers')) && response.children.filter(v => is(v, HTTP_NS, 'body')).length <= 1 && response.children.filter(v => is(v, HTTP_NS, 'headers')).length <= 1) };
        flow.source ??= listener; parsed.nodes.set(componentId, c);
        if (!model.configs.some(cfg => cfg.id === listener.configId)) issue('Listener references a missing HTTP configuration: ' + value(c, 'config-ref'), c.start, c.openEnd, 'config-reference');
        if (!listener.path || !dynamic(listener.path) && !listener.path.startsWith('/')) issue('Listener path must begin with / or use a runtime property/expression.', c.start, c.openEnd, 'listener-path');
        if (!dynamic(listener.allowedMethods) && !listener.allowedMethods.split(',').every(method => ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS'].includes(method.trim()))) issue('Use valid comma-separated HTTP methods.', c.start, c.openEnd, 'listener-methods');
        if (!dynamic(listener.responseStatus) && (!/^\d{3}$/.test(listener.responseStatus) || Number(listener.responseStatus) < 100 || Number(listener.responseStatus) > 599)) issue('Response status must be an HTTP code from 100 to 599 or a runtime expression/property.', response?.start ?? c.start, response?.openEnd ?? c.openEnd, 'response-status');
      } else if (is(c, CORE_NS, 'set-payload') && (namespacedAttribute(c,DOC_NS,'name')?.value==='Transform Message'||value(c,'value').startsWith('#[%dw')) || is(c, TRANSFORM_NS, 'message')) {
        const raw = value(c,c.uri === TRANSFORM_NS ? 'payload' : 'value');
        const transform: RealTransform = { id: componentId, kind:'transform', displayName:namespacedAttribute(c,DOC_NS,'name')?.value ?? 'Transform Message', payloadResource:value(c,'payloadResource'),attributesResource:value(c,'attributesResource'),variablesResource:value(c,'variablesResource'),attributesScript:value(c,'attributes'), variablesScript:value(c,'variables'), script:c.uri === TRANSFORM_NS ? raw : raw.startsWith('#[') && raw.endsWith(']') ? raw.slice(2,-1) : JSON.stringify(raw), mimeType:value(c,'mimeType','application/java'), editable:flow.editable && c.children.length === 0 };
        if (c.parent === onError) flow.errorHandler!.processors.push(transform); else flow.processors.push(transform);
        parsed.nodes.set(componentId,c);
        if (![transform.script,transform.attributesScript,transform.variablesScript,transform.payloadResource,transform.attributesResource,transform.variablesResource].some(v=>v?.trim())) issue('Transform Message requires a DataWeave payload script.',c.start,c.openEnd,'transform-script');
        for(const [script,resource] of [[transform.script,transform.payloadResource],[transform.attributesScript,transform.attributesResource],[transform.variablesScript,transform.variablesResource]]) {
          if(resource && (!/^dwl\/[A-Za-z0-9_./-]+\.dwl$/.test(resource)||resource.split('/').some(v=>v==='..'||v==='.'||!v)))issue('DataWeave resources must name a .dwl file under src/main/resources/dwl.',c.start,c.openEnd,'transform-resource');
          if(resource && script?.trim())issue('Choose inline script or external resource for each Transform output.',c.start,c.openEnd,'transform-source');
        }
        if (!transform.mimeType.trim()) issue('Transform Message requires an output MIME type.',c.start,c.openEnd,'transform-mime');
      } else if (is(c, CORE_NS, 'logger')) {
        const rawLevel = value(c, 'level', 'INFO');
        const logger: RealLogger = { id: componentId, kind: 'logger', displayName: namespacedAttribute(c, DOC_NS, 'name')?.value ?? 'Logger', message: value(c, 'message'), level: rawLevel as RealLogLevel, category: value(c, 'category'), expressionMode: value(c, 'message').startsWith('#['), notes: '', editable: flow.editable && c.children.length === 0 };
        if (c.parent === onError) flow.errorHandler!.processors.push(logger); else flow.processors.push(logger);
        parsed.nodes.set(componentId, c);
        if (!LOG_LEVELS_REAL.includes(rawLevel as RealLogLevel)) issue('Unsupported Logger level: ' + rawLevel, c.start, c.openEnd, 'logger-level');
        if (!logger.message.trim()) issue('Logger message is required.', c.start, c.openEnd, 'logger-message');
      } else if(coreElement(c.uri,c.local)&&!is(c,CORE_NS,'error-handler')){const processor=projectProcessor(c,parsed,flow.editable,flowIndex,issue);if(processor){if(c.parent===onError)flow.errorHandler!.processors.push(processor);else flow.processors.push(processor);}}
    }
    if(handler){const projected=projectProcessor(handler,parsed,true,flowIndex,issue);if(projected?.kind==='core'){flow.errorHandler={component:projected,type:projected.children.map(c=>c.kind==='core'?c.coreType:c.kind).join(', '),summary:'Local error handler',editable:projected.editable,processors:projected.children[0]?.kind==='core'?projected.children[0].children:[]};}}
  }
  if (!model.flows.length) issue('Create a flow, then add an HTTP Listener and Logger before building.', tree.root.start, tree.root.openEnd, 'empty-project', 'warning');
  if (model.diagnostics.some(d => ['duplicate-id', 'flow-name', 'config-name'].includes(d.code))) {
    // A stable selector must be unambiguous before any visual source mutation.
    for (const flow of model.flows) { flow.editable = false; if (flow.source) flow.source.editable = false; for (const logger of flow.processors) logger.editable = false; }
    for (const config of model.configs) config.editable = false;
  }
  return parsed;
}

export function processorTree(flow:RealFlow):RealProcessor[]{
 const out:RealProcessor[]=[];
 const visit=(node:RealProcessor)=>{out.push(node);if(node.kind==='core')node.children.forEach(visit);};
 flow.processors.forEach(visit);
 if(flow.errorHandler?.component)visit(flow.errorHandler.component);else flow.errorHandler?.processors.forEach(visit);
 return out;
}
function projectProcessor(node:Element,parsed:Parsed,editable:boolean,flowIndex:number,issue:(message:string,start?:number,end?:number,code?:string,severity?:'error'|'warning')=>void):RealProcessor|undefined{
 const id=namespacedAttribute(node,DOC_NS,'id')?.value??`legacy:${flowIndex}:nested:${node.start}:${node.local}`;
 const name=namespacedAttribute(node,DOC_NS,'name')?.value;
 parsed.nodes.set(id,node);
 if(is(node,CORE_NS,'logger')){
  return {id,kind:'logger',displayName:name??'Logger',message:value(node,'message'),level:value(node,'level','INFO') as RealLogLevel,category:value(node,'category'),expressionMode:value(node,'message').startsWith('#['),notes:'',editable:editable&&!node.children.length};
 }
 if(is(node,TRANSFORM_NS,'message')||is(node,CORE_NS,'set-payload')&&(name==='Transform Message'||value(node,'value').startsWith('#[%dw'))){
  const raw=value(node,node.uri===TRANSFORM_NS?'payload':'value');
  const outputs=[[raw,value(node,'payloadResource')],[value(node,'attributes'),value(node,'attributesResource')],[value(node,'variables'),value(node,'variablesResource')]];
  if(!outputs.some(pair=>pair.some(v=>v.trim())))issue('Transform Message needs at least one output.',node.start,node.openEnd,'transform-script');
  for(const [script,resource]of outputs){if(resource&&(!/^dwl\/[A-Za-z0-9_./-]+\.dwl$/.test(resource)||resource.split('/').some(v=>!v||v==='.'||v==='..')))issue('DataWeave resources must be under src/main/resources/dwl.',node.start,node.openEnd,'transform-resource');if(resource&&script.trim())issue('Choose inline script or external resource for each Transform output.',node.start,node.openEnd,'transform-source');}
  return {id,kind:'transform',displayName:name??'Transform Message',script:node.uri===TRANSFORM_NS?raw:raw.startsWith('#[')&&raw.endsWith(']')?raw.slice(2,-1):JSON.stringify(raw),mimeType:value(node,'mimeType','application/java'),attributesScript:value(node,'attributes'),variablesScript:value(node,'variables'),payloadResource:value(node,'payloadResource'),attributesResource:value(node,'attributesResource'),variablesResource:value(node,'variablesResource'),editable:editable&&!node.children.length};
 }
 const definition=coreElement(node.uri,node.local);if(!definition)return undefined;
 const values=Object.fromEntries(definition.fields.map(field=>[field.key,value(node,field.key,field.initial)]));
 const children=node.children.map(c=>projectProcessor(c,parsed,editable,flowIndex,issue)).filter((c):c is RealProcessor=>!!c);
 const safe=node.children.length===children.length&&children.every(c=>c.editable);
 const component:RealCore={id,kind:'core',coreType:definition.key,displayName:name??definition.name,values,children,editable:editable&&safe};
 if(!safe)issue('Unsupported child content is preserved in '+definition.name+'.',node.start,node.openEnd,'unsupported','warning');
 for(const field of definition.fields)if(field.required&&!values[field.key]?.trim())issue(definition.name+' requires '+field.label+'.',node.start,node.openEnd,'component-field');
 if(definition.minimum&&children.filter(c=>c.kind!=='core'||c.coreType!=='error-handler').length<definition.minimum)issue(definition.name+' needs at least '+definition.minimum+' child component(s).',node.start,node.openEnd,'component-children');
 return component;
}
function catalogMarkup(key:string,node:Element,id:string):string{
 const definition=coreDefinition(key);if(!definition)throw new XmlEditError('Unknown core component.');
 const name=qname(node,definition.namespace==='core'?CORE_NS:TRANSFORM_NS,definition.local),docName=qname(node,DOC_NS,'name'),docId=qname(node,DOC_NS,'id');
 const attrs=definition.fields.filter(f=>f.initial!=='').map(f=>` ${f.key}="${escapedAttribute(f.initial)}"`).join('');
 const logger=()=>`<${qname(node,CORE_NS,'logger')} ${docName}="Logger" ${docId}="${newId()}" level="INFO" message="Processing ${escapedAttribute(definition.name)}"/>`;
 let body='';
 if(definition.children==='choice')body=catalogMarkup('when',node,newId())+catalogMarkup('otherwise',node,newId());
 else if(definition.children==='routes')body=catalogMarkup('route',node,newId())+catalogMarkup('route',node,newId());
 else if(definition.children==='handlers')body=catalogMarkup('on-error-propagate',node,newId());
 else if(definition.children==='batch')body=catalogMarkup('batch-step',node,newId());
 else if(definition.children)body=logger()+(['first-successful','round-robin'].includes(key)?logger():'');
 return `<${name} ${docName}="${escapedAttribute(definition.name)}" ${docId}="${id}"${attrs}${body?'>'+body+'</'+name+'>':'/>'}`;
}

export function parseIntegration(text: string): IntegrationModel { return parseInternal(text).model; }
export function createIntegrationXml(): string {
  return `<?xml version="1.0" encoding="UTF-8"?>\n<zernova xmlns="${CORE_NS}"\n      xmlns:http="${HTTP_NS}"\n      xmlns:doc="${DOC_NS}"\n      xmlns:xsi="${XSI_NS}"\n      xsi:schemaLocation="${CORE_NS} ${CORE_NS}/current/zernova.xsd ${HTTP_NS} ${HTTP_NS}/current/zernova-http.xsd">\n</zernova>\n`;
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
function removeAttribute(node:Element,name:string,edits:SourceEdit[]):void{const existing=attribute(node,name);if(existing)edits.push({start:existing.start,end:existing.end,text:''});}
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
  const docNeeded = ['insert', 'updateComponent', 'updateCore', 'duplicateLogger', 'moveLogger', 'duplicateProcessor', 'moveProcessor', 'createErrorHandler'].includes(operation.kind);
  const transformNeeded = operation.kind === 'insert' && (operation.component === 'transform'||coreDefinition(operation.component)?.namespace==='support') || operation.kind === 'updateComponent' && parsed.model.flows.some(f => processorTree(f).some(p => p.id === operation.id && p.kind === 'transform'));
  const httpNeeded = operation.kind === 'saveConfig' || operation.kind === 'insert' && operation.component === 'listener';
  const namespaces: [string, string][] = [...(transformNeeded ? [[TRANSFORM_NS,'zt'] as [string,string], [XSI_NS,'xsi'] as [string,string]] : []),...(docNeeded ? [[DOC_NS, 'doc'] as [string,string]] : []), ...(httpNeeded ? [[HTTP_NS, 'http'] as [string,string], [XSI_NS, 'xsi'] as [string,string]] : [])];
  for (const [uri, preferred] of namespaces) {
    if (prefixFor(root, uri) !== undefined) continue;
    let prefix = preferred, suffix = 2;
    while (root.namespaces.has(prefix)) prefix = preferred + suffix++;
    setAttribute(root, 'xmlns:' + prefix, uri, edits);
    root.namespaces.set(prefix, uri);
  }
  if (httpNeeded || transformNeeded) {
    const schema = namespacedAttribute(root, XSI_NS, 'schemaLocation'), pairs = (schema?.value ?? '').trim().split(/\s+/).filter(Boolean);
    if (pairs.length % 2) throw new XmlEditError('schemaLocation must contain namespace/location pairs. Repair it in XML first.');
    const namespacesInSchema = new Set(pairs.filter((_, index) => index % 2 === 0));
    if (!namespacesInSchema.has(CORE_NS)) pairs.push(CORE_NS, CORE_NS + '/current/zernova.xsd');
    if (transformNeeded && !namespacesInSchema.has(TRANSFORM_NS)) pairs.push(TRANSFORM_NS,TRANSFORM_NS+'/current/transform.xsd');
    if (httpNeeded && !namespacesInSchema.has(HTTP_NS)) pairs.push(HTTP_NS, HTTP_NS + '/current/zernova-http.xsd');
    if (!schema || pairs.length !== schema.value.trim().split(/\s+/).length) setAttribute(root, schema?.name ?? qname(root, XSI_NS, 'schemaLocation'), pairs.join(' '), edits);
  }
  let result=applyEdits(text,edits);
  if(transformNeeded && operation.kind === 'updateComponent') {
    const current=parseInternal(result), node=current.nodes.get(operation.id);
    if(node && is(node,CORE_NS,'set-payload')) {
      const component=current.model.flows.flatMap(processorTree).find(p=>p.id===operation.id) as RealTransform;
      const changes:SourceEdit[]=[{start:node.start+1,end:node.start+1+node.name.length,text:qname(node,TRANSFORM_NS,'message')}];
      const old=attribute(node,'value');
      if(old) changes.push({start:old.start,end:old.end,text:' payload="'+escapedAttribute(component.script)+'"'});
      else setAttribute(node,'payload',component.script,changes);
      if(!node.selfClosing) changes.push({start:node.closeStart+2,end:node.closeStart+2+node.name.length,text:qname(node,TRANSFORM_NS,'message')});
      result=applyEdits(result,changes);
    }
  }
  return result;
}
function componentAndFlow(parsed: Parsed, id: string): { component: RealComponent; node: Element; flow: RealFlow } {
  for (const flow of parsed.model.flows) { const component = flow.source?.id === id ? flow.source : processorTree(flow).find(p=>p.id===id); if (component) { const node = parsed.nodes.get(id)!; if (!component.editable || !flow.editable) throw new XmlEditError('This component/flow contains unsupported constructs. Edit its XML directly; source is preserved.'); return { component, node, flow }; } }
  throw new XmlEditError('Component no longer exists. Refresh the document and try again.');
}
function supportedFlow(parsed: Parsed, id: string): { flow: RealFlow; node: Element } {
  const flow = parsed.model.flows.find(f => f.id === id), node = parsed.flowNodes.get(id);
  if (!flow || !node) throw new XmlEditError('Select an existing flow.');
  if (!flow.editable) throw new XmlEditError('This flow contains unsupported constructs. Its XML is preserved; visual edits are disabled.');
  return { flow, node };
}
function updateResponse(text: string, listener: Element, values: ComponentValues, edits: SourceEdit[], error=false): void {
  if (values.responseStatus === undefined && values.responseBody === undefined && values.responseHeaders === undefined) return;
  const responseName=error?'error-response':'response';
  const response = child(listener, HTTP_NS, responseName), newline = eol(text), indent = indentation(text, listener);
  if (!response) {
    const body = values.responseBody ?? (error?"#[error.description default 'Internal Server Error']":'#[payload]'), headers = values.responseHeaders ?? "#[{'Content-Type': 'text/plain; charset=UTF-8'}]";
    const markup = `<${qname(listener, HTTP_NS, responseName)} statusCode="${escapedAttribute(values.responseStatus ?? (error?'500':'200'))}">${newline}${indent}    <${qname(listener, HTTP_NS, 'body')}>${escapedText(body)}</${qname(listener, HTTP_NS, 'body')}>${newline}${indent}    <${qname(listener, HTTP_NS, 'headers')}>${escapedText(headers)}</${qname(listener, HTTP_NS, 'headers')}>${newline}${indent}  </${qname(listener, HTTP_NS, responseName)}>`;
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
  if(operation.kind==='saveGlobalProperty'){
    if(!/^[A-Za-z_][A-Za-z0-9_.-]{0,119}$/.test(operation.name))throw new XmlEditError('Use a valid property name.');
    const existing=(parsed.model.globalProperties??[]).find(v=>v.name===operation.name);
    if(existing)setAttribute(parsed.configNodes.get(existing.id)!,'value',operation.value,edits);
    else insertChildren(text,root,`<${qname(root,CORE_NS,'global-property')} name="${escapedAttribute(operation.name)}" value="${escapedAttribute(operation.value)}"/>`,edits);
  }else if(operation.kind==='savePropertyFile'){
    if(!operation.file.trim()||operation.file.length>512||operation.file.startsWith('/')||operation.file.includes('\\')||operation.file.split('/').some(p=>p==='..'||p==='.')||!operation.file.endsWith('.properties'))throw new XmlEditError('Use a relative .properties resource path.');
    const node=operation.id?parsed.configNodes.get(operation.id):undefined;
    if(operation.id&&!node)throw new XmlEditError('Property-file configuration no longer exists.');
    if(node)setAttribute(node,'file',operation.file,edits);
    else insertChildren(text,root,`<${qname(root,CORE_NS,'configuration-properties')} file="${escapedAttribute(operation.file)}"/>`,edits);
  }else if(operation.kind==='deletePropertyFile'||operation.kind==='deleteGlobalProperty'){
    const node=parsed.configNodes.get(operation.id);
    if(!node||node.local!==(operation.kind==='deletePropertyFile'?'configuration-properties':'global-property'))throw new XmlEditError('Configuration no longer exists.');
    edits.push({start:node.start,end:node.end,text:''});
  }else if (operation.kind === 'createFlow') {
    requireName(operation.name, 'Flow name');
    if (parsed.model.flows.some(f => f.name === operation.name)) throw new XmlEditError('A flow with this name already exists.');
    insertChildren(text, root, `<${qname(root, CORE_NS, operation.flowKind??'flow')} name="${escapedAttribute(operation.name)}">${eol(text)}  </${qname(root, CORE_NS, operation.flowKind??'flow')}>`, edits);
    selectionId = 'flow:' + operation.name;
  } else if (operation.kind === 'renameFlow') {
    const { node } = supportedFlow(parsed, operation.flowId);
    requireName(operation.name, 'Flow name');
    if (parsed.model.flows.some(f => f.id !== operation.flowId && f.name === operation.name)) throw new XmlEditError('A flow with this name already exists.');
    if (parsed.all.some(n => is(n, CORE_NS, 'flow-ref') && value(n, 'name') === value(node, 'name'))) throw new XmlEditError('This flow is referenced. Rename it in XML with all references reviewed.');
    setAttribute(node, 'name', operation.name, edits); selectionId = 'flow:' + operation.name;
  } else if (operation.kind === 'createErrorHandler') {
    const { node, flow } = supportedFlow(parsed, operation.flowId);
    if (flow.kind === 'sub-flow' || flow.errorHandler) throw new XmlEditError('Use a flow without an existing error handler. Subflows inherit their caller error handling.');
    const q = (name: string) => qname(node, CORE_NS, name); selectionId = newId();
    insertChildren(text, node, `<${q('error-handler')}><${q('on-error-propagate')} type="ANY"><${q('logger')} ${qname(node, DOC_NS, 'id')}="${selectionId}" ${qname(node, DOC_NS, 'name')}="Log error" level="ERROR" message="#[error.description]"/></${q('on-error-propagate')}></${q('error-handler')}>`, edits);
  } else if (operation.kind === 'insert') {
    const { node, flow } = supportedFlow(parsed, operation.flowId);
    if (operation.lane === 'error' && (operation.component === 'listener' || !flow.errorHandler?.editable)) throw new XmlEditError('Select a supported error-handler processor slot.');
    if (flow.kind === 'sub-flow' && operation.component === 'listener') throw new XmlEditError('A subflow cannot contain an HTTP Listener. Add the Listener to its calling flow.');
    selectionId = newId();
    const docName = qname(node, DOC_NS, 'name'), docId = qname(node, DOC_NS, 'id');
    if(operation.component==='scheduler'){
      if(flow.source||flow.kind==='sub-flow')throw new XmlEditError('Scheduler needs an empty flow source.');
      const q=(name:string)=>qname(node,CORE_NS,name);insertChildren(text,node,`<${q('scheduler')} ${docName}="Scheduler" ${docId}="${selectionId}" disallowConcurrentExecution="true"><${q('scheduling-strategy')}><${q('fixed-frequency')} frequency="1000" timeUnit="MILLISECONDS"/></${q('scheduling-strategy')}></${q('scheduler')}>`,edits,node.children[0]);
    }else if (operation.component === 'listener') {
      if(flow.kind==='sub-flow')throw new XmlEditError('Subflows do not accept sources.');
      if (flow.source) throw new XmlEditError('This flow already has a Listener. A flow accepts one source.');
      const cfg = parsed.model.configs.find(c => c.editable && c.protocol === 'HTTP');
      const newline = eol(text), indent = indentation(text, node) + '  ', q = (name: string) => qname(node, HTTP_NS, name);
      const markup = `<${q('listener')} config-ref="${escapedAttribute(cfg?.name ?? '')}" path="/hello" allowedMethods="GET" ${docName}="HTTP Listener" ${docId}="${selectionId}">${newline}${indent}  <${q('response')} statusCode="200">${newline}${indent}    <${q('body')}>${escapedText("#['Hello from ZERNOVA']")}</${q('body')}>${newline}${indent}    <${q('headers')}>${escapedText("#[{'Content-Type': 'text/plain; charset=UTF-8'}]")}</${q('headers')}>${newline}${indent}  </${q('response')}>${newline}${indent}</${q('listener')}>`;
      insertChildren(text, node, markup, edits, node.children[0]);
    } else {
      if(operation.component==='error-handler'&&!operation.parentId&&flow.errorHandler)throw new XmlEditError('This flow already has an error handler.');
      const container=operation.parentId?componentAndFlow(parsed,operation.parentId):undefined;
      if(container&&(container.flow.id!==flow.id||container.component.kind!=='core'||!acceptsChild(container.component.coreType,operation.component)))throw new XmlEditError('Choose a valid component container.');
      if(!container&&!acceptsChild('flow',operation.component))throw new XmlEditError('This component needs a matching scope or route.');
      const processors = container&&container.component.kind==='core'?container.component.children:operation.lane === 'error' ? flow.errorHandler!.processors : flow.processors;
      const parent = container?container.node:operation.lane === 'error' ? child(node, CORE_NS, 'error-handler')!.children[0] : node;
      if (processors.length >= 100) throw new XmlEditError('This milestone supports up to 100 processors per lane.');
      let markup = coreDefinition(operation.component)?catalogMarkup(operation.component,node,selectionId):operation.component === 'transform'
        ? `<${qname(node, TRANSFORM_NS, 'message')} ${docName}="Transform Message" ${docId}="${selectionId}" mimeType="application/json" payload="${escapedAttribute('%dw 2.0\noutput application/json\n---\npayload')}"/>`
        : `<${qname(node, CORE_NS, 'logger')} ${docName}="Logger" ${docId}="${selectionId}" level="INFO" category="io.zernova.acceptance" message="${escapedAttribute("#['ZERNOVA_TEST ' ++ (attributes.queryParams.testId default 'missing')]")}"/>`;
      let index = operation.index ?? processors.length;
      const unique=['otherwise','error-handler','batch-on-complete','batch-aggregator'];
      if(unique.includes(operation.component)&&processors.some(p=>p.kind==='core'&&p.coreType===operation.component))throw new XmlEditError('This container already has '+operation.component+'.');
      if(operation.component==='batch-step'){const used=new Set(processors.filter(p=>p.kind==='core').map(p=>(p as RealCore).values.name));let step='step',suffix=2;while(used.has(step))step='step'+suffix++;markup=markup.replace('name="step"','name="'+step+'"');}
      const terminalIndex=processors.findIndex(p=>p.kind==='core'&&['otherwise','error-handler','batch-on-complete','batch-aggregator'].includes(p.coreType));
      if(terminalIndex>=0&&!unique.includes(operation.component))index=Math.min(index,terminalIndex);
      if(unique.includes(operation.component))index=processors.length;
      if (!Number.isInteger(index) || index < 0 || index > processors.length) throw new XmlEditError('Invalid processor insertion position.');
      insertChildren(text, parent, markup, edits, processors[index] ? parsed.nodes.get(processors[index].id) : operation.lane === 'error' ? undefined : child(parent, CORE_NS, 'error-handler'));
    }
  }else if(operation.kind==='updateCore'){
    const {component,node}=componentAndFlow(parsed,operation.id);
    if(component.kind==='scheduler'){
      const allowed=['displayName','frequency','timeUnit','startDelay','cron','timeZone','disallowConcurrentExecution'];if(Object.keys(operation.values).some(k=>!allowed.includes(k)))throw new XmlEditError('Unsupported Scheduler field.');
      const values={...component.values,...operation.values};if(operation.values.displayName!==undefined)setDocAttribute(node,'name',operation.values.displayName,edits);
      setAttribute(node,'disallowConcurrentExecution',values.disallowConcurrentExecution,edits);
      const strategy=child(node,CORE_NS,'scheduling-strategy');const existingStrategy=strategy?.children[0];const q=(local:string)=>qname(node,CORE_NS,local);const inner=values.cron?`<${q('cron')} expression="${escapedAttribute(values.cron)}"${values.timeZone?' timeZone="'+escapedAttribute(values.timeZone)+'"':''}/>`:`<${q('fixed-frequency')} frequency="${escapedAttribute(values.frequency)}" timeUnit="${escapedAttribute(values.timeUnit)}" startDelay="${escapedAttribute(values.startDelay)}"/>`;
      if(existingStrategy&&existingStrategy.local===(values.cron?'cron':'fixed-frequency')){for(const key of(values.cron?['expression','timeZone']:['frequency','timeUnit','startDelay'])){const val=key==='expression'?values.cron:values[key];if(key==='timeZone'&&!val)removeAttribute(existingStrategy,key,edits);else setAttribute(existingStrategy,key,val??'',edits);}}else if(strategy)edits.push({start:strategy.start,end:strategy.end,text:`<${q('scheduling-strategy')}>${inner}</${q('scheduling-strategy')}>`});else insertChildren(text,node,`<${q('scheduling-strategy')}>${inner}</${q('scheduling-strategy')}>`,edits);
    }else if(component.kind==='core'){
      const definition=coreDefinition(component.coreType)!;if(Object.keys(operation.values).some(k=>k!=='displayName'&&!definition.fields.some(f=>f.key===k)))throw new XmlEditError('Unsupported component field.');
      for(const [key,value] of Object.entries(operation.values))if(key==='displayName')setDocAttribute(node,'name',value,edits);else setAttribute(node,key,value,edits);
    }else throw new XmlEditError('Choose a core component.');
    selectionId=namespacedAttribute(node,DOC_NS,'id')?.value??newId();setDocAttribute(node,'id',selectionId,edits);
  } else if (operation.kind === 'updateComponent') {
    const { component, node } = componentAndFlow(parsed, operation.id), next = operation.values;
    const allowed = component.kind === 'listener' ? ['displayName', 'configId', 'path', 'allowedMethods', 'responseStatus', 'responseBody', 'responseHeaders', 'errorStatus', 'errorBody', 'errorHeaders'] : component.kind === 'transform' ? ['displayName', 'script', 'mimeType', 'attributesScript', 'variablesScript', 'payloadResource', 'attributesResource', 'variablesResource'] : ['displayName', 'message', 'level', 'category'];
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
      updateResponse(text,node,{responseStatus:next.errorStatus,responseBody:next.errorBody,responseHeaders:next.errorHeaders},edits,true);
    } else if (component.kind === 'transform') {
      if (next.script !== undefined) setAttribute(node,'payload',next.script,edits);
      if (next.attributesScript !== undefined) setAttribute(node,'attributes',next.attributesScript,edits);
      if (next.variablesScript !== undefined) setAttribute(node,'variables',next.variablesScript,edits);
      for(const key of ['payloadResource','attributesResource','variablesResource'] as const)if(next[key]!==undefined)setAttribute(node,key,next[key]!,edits);
      if (next.mimeType !== undefined) setAttribute(node,'mimeType',next.mimeType,edits);
    } else {
      if (next.message !== undefined) setAttribute(node, 'message', next.message, edits);
      if (next.level !== undefined) { if (!LOG_LEVELS_REAL.includes(next.level as RealLogLevel)) throw new XmlEditError('Choose TRACE, DEBUG, INFO, WARN or ERROR.'); setAttribute(node, 'level', next.level, edits); }
      if (next.category !== undefined) setAttribute(node, 'category', next.category, edits);
    }
  } else if (operation.kind === 'deleteComponent') {
    const { node } = componentAndFlow(parsed, operation.id); edits.push({ start: node.start, end: node.end, text: '' });
  } else if ((operation.kind === 'duplicateLogger' || operation.kind === 'duplicateProcessor')) {
    const { component, node, flow } = componentAndFlow(parsed, operation.id);
    if(component.kind==='core'&&['error-handler','otherwise','batch-on-complete','batch-aggregator'].includes(component.coreType))throw new XmlEditError('This container allows only one '+component.displayName+'.');
    if (component.kind === 'listener'||component.kind==='scheduler') throw new XmlEditError('Only processors can be duplicated.');
    const lane = flow.processors.some(p=>p.id === component.id) ? flow.processors : flow.errorHandler?.processors ?? [];
    if (lane.length >= 100) throw new XmlEditError('This milestone supports up to 100 processors per lane.');
    selectionId = newId();
    const local: SourceEdit[] = [];
    setDocAttribute(node, 'id', selectionId, local);
    for(const descendant of parsed.all.filter(n=>n!==node&&n.start>node.start&&n.end<=node.end))if(namespacedAttribute(descendant,DOC_NS,'id'))setDocAttribute(descendant,'id',newId(),local);
    if(component.kind==='core'&&component.coreType==='batch-step'){let suffix=2;const used=new Set(processorTree(flow).filter(p=>p.kind==='core'&&p.coreType==='batch-step').map(p=>(p as RealCore).values.name));let name=component.values.name+'Copy';while(used.has(name))name=component.values.name+'Copy'+suffix++;setAttribute(node,'name',name,local);}
    const snippet = applyEdits(text.slice(node.start, node.end), local.map(e => ({ ...e, start: e.start - node.start, end: e.end - node.start })));
    edits.push({ start: node.end, end: node.end, text: eol(text) + indentation(text, node) + snippet });
  } else if ((operation.kind === 'moveLogger' || operation.kind === 'moveProcessor')) {
    const { component, node, flow } = componentAndFlow(parsed, operation.id);
    if (component.kind === 'listener'||component.kind==='scheduler') throw new XmlEditError('Only processors can be reordered.');
    if(component.kind==='core'&&['error-handler','otherwise','batch-on-complete','batch-aggregator'].includes(component.coreType))throw new XmlEditError('This element stays at the end of its container.');
    const processors=processorTree(flow).filter(p=>parsed.nodes.get(p.id)?.parent===node.parent&&!(p.kind==='core'&&['error-handler','otherwise','batch-on-complete','batch-aggregator'].includes(p.coreType)));
    const index = processors.findIndex(p => p.id === component.id), adjacent = processors[index + operation.direction];
    if (!adjacent) throw new XmlEditError('The component is already at that edge of the processor lane.');
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
