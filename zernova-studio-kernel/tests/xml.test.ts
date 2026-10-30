import test from 'node:test';
import assert from 'node:assert/strict';
import { applyOperation, convertLegacyIntegrationXml, createIntegrationXml, parseIntegration, findConfigurationReferences, CORE_NS, HTTP_NS, DOC_NS, MAX_XML_BYTES, type XmlOperation } from '../src/shared/xml';
import { parseRealMessage, parseXmlOperation } from '../src/shared/realProtocol';
import { parseWebviewMessage } from '../src/shared/protocol';

const config = { name: 'HTTP_Listener_config', protocol: 'HTTP' as const, host: '127.0.0.1', port: '18081', basePath: '' };
function act(xml: string, operation: XmlOperation): string { return applyOperation(xml, operation).text; }
function fixture(): string {
  let xml = createIntegrationXml();
  xml = act(xml, { kind: 'createFlow', name: 'helloFlow' });
  xml = act(xml, { kind: 'saveConfig', values: config });
  xml = act(xml, { kind: 'insert', flowId: 'flow:helloFlow', component: 'listener' });
  return act(xml, { kind: 'insert', flowId: 'flow:helloFlow', component: 'logger' });
}
function source(xml:string){const node=parseIntegration(xml).flows[0].source;assert(node?.kind==='listener');return node;}
function logger(xml: string, index = 0) { const node=parseIntegration(xml).flows[0].processors[index]; assert(node.kind === 'logger'); return node; }

test('new real document contains no flow or fixture and accepts explicit flow creation', () => {
  const initial = createIntegrationXml(), parsed = parseIntegration(initial);
  assert.equal(parsed.valid, true); assert.equal(parsed.flows.length, 0); assert.equal(parsed.configs.length, 0);
  assert.equal(parsed.diagnostics[0].code, 'empty-project');
  const next = act(initial, { kind: 'createFlow', name: 'ownerFlow' });
  assert.equal(parseIntegration(next).flows[0].name, 'ownerFlow');
  assert.equal(parseIntegration(next).flows[0].source, null);
});

test('incomplete authoring permits adding Listener before creating a configuration', () => {
  let xml = act(createIntegrationXml(), { kind: 'createFlow', name: 'helloFlow' });
  xml = act(xml, { kind: 'insert', flowId: 'flow:helloFlow', component: 'listener' });
  assert.equal(parseIntegration(xml).valid, true);
  assert.equal(source(xml).editable, true);
  assert.ok(parseIntegration(xml).diagnostics.some(d => d.code === 'config-reference'));
  xml = act(xml, { kind: 'saveConfig', values: config });
  xml = act(xml, { kind: 'updateComponent', id: source(xml).id, values: { configId: 'config:HTTP_Listener_config' } });
  assert.ok(!parseIntegration(xml).diagnostics.some(d => d.code === 'config-reference'));
});

test('Listener and core Logger are the only generated processors, with response fields on source', () => {
  const xml = fixture(), model = parseIntegration(xml);
  assert.equal(model.flows[0].processors.length, 1);
  assert.equal(source(xml).responseStatus, '200');
  assert.equal(source(xml).responseBody, "#['Hello from ZERNOVA']");
  assert.equal(source(xml).responseHeaders, "#[{'Content-Type': 'text/plain; charset=UTF-8'}]");
  assert.equal(logger(xml).message, "#['ZERNOVA_TEST ' ++ (attributes.queryParams.testId default 'missing')]");
  assert.ok(!xml.includes('set-payload') && !xml.includes('ee:transform') && !xml.includes('http:request'));
  assert.equal(model.diagnostics.filter(d => d.severity === 'error').length, 0);
});

test('one valid source position and duplicate flow/config names are enforced', () => {
  const xml = fixture();
  assert.throws(() => act(xml, { kind: 'insert', flowId: 'flow:helloFlow', component: 'listener' }), /one source/);
  assert.throws(() => act(xml, { kind: 'createFlow', name: 'helloFlow' }), /already exists/);
  assert.throws(() => act(xml, { kind: 'saveConfig', values: config }), /already exists/);
  assert.throws(() => act(xml, { kind: 'createFlow', name: '../escape' }), /Flow name/);
  assert.throws(() => act(xml, { kind: 'insert', flowId: 'flow:helloFlow', component: 'logger', index: -1 }), /position/);
});

test('a literal path edit changes only its original attribute span', () => {
  const xml = fixture(), before = source(xml);
  const result = applyOperation(xml, { kind: 'updateComponent', id: before.id, values: { path: '/hello-v2' } });
  assert.equal(result.text, xml.replace('path="/hello"', 'path="/hello-v2"'));
  assert.equal(source(result.text).id, before.id);
  assert.equal(result.selectionId, before.id);
  assert.equal(result.edits.length, 1);
});

test('intermediate property typing persists safely with semantic errors that clear on completion', () => {
  let xml = fixture();
  const listenerId = source(xml).id, loggerId = logger(xml).id;
  xml = act(xml, { kind: 'updateComponent', id: listenerId, values: { path: '', allowedMethods: 'P', responseStatus: '2', configId: '' } });
  xml = act(xml, { kind: 'updateComponent', id: loggerId, values: { displayName: '', message: '' } });
  const partial = parseIntegration(xml);
  assert.equal(partial.valid, true); assert.equal(partial.flows[0].editable, true);
  assert.equal(source(xml).path, ''); assert.equal(source(xml).allowedMethods, 'P'); assert.equal(source(xml).responseStatus, '2');
  assert.equal(logger(xml).message, ''); assert.equal(logger(xml).displayName, '');
  for (const code of ['listener-path', 'listener-methods', 'response-status', 'config-reference', 'logger-message']) assert.ok(partial.diagnostics.some(d => d.code === code && d.severity === 'error'), code);
  xml = act(xml, { kind: 'updateComponent', id: listenerId, values: { path: 'hello', allowedMethods: 'PO', responseStatus: '20', configId: 'config:missing' } });
  assert.equal(source(xml).configId, 'config:missing');
  assert.ok(parseIntegration(xml).diagnostics.some(d => d.code === 'response-status'));
  xml = act(xml, { kind: 'updateComponent', id: listenerId, values: { path: '/hello', allowedMethods: 'POST', responseStatus: '201', configId: 'config:HTTP_Listener_config' } });
  xml = act(xml, { kind: 'updateComponent', id: loggerId, values: { displayName: 'Log owner request', message: 'Owner <typed> & completed' } });
  assert.equal(source(xml).id, listenerId); assert.equal(logger(xml).id, loggerId);
  assert.equal(logger(xml).message, 'Owner <typed> & completed'); assert.ok(xml.includes('Owner &lt;typed> &amp; completed'));
  assert.equal(parseIntegration(xml).diagnostics.filter(d => d.severity === 'error').length, 0);
});

test('namespace aliases are resolved by URI and retained through edits', () => {
  let xml = fixture().replaceAll('<zernova ', '<m:zernova ').replaceAll('</zernova>', '</m:zernova>').replaceAll('xmlns="' + CORE_NS + '"', 'xmlns:m="' + CORE_NS + '"').replaceAll('<flow ', '<m:flow ').replaceAll('</flow>', '</m:flow>').replaceAll('<logger ', '<m:logger ').replaceAll('xmlns:http=', 'xmlns:h=').replaceAll('http:', 'h:').replaceAll('xmlns:doc=', 'xmlns:d=').replaceAll('doc:', 'd:');
  // Restore namespace URIs after replacing element prefixes.
  xml = xml.replaceAll('h://', 'http://');
  assert.equal(parseIntegration(xml).valid, true);
  xml = act(xml, { kind: 'updateComponent', id: logger(xml).id, values: { message: '#["alias"]' } });
  assert.ok(xml.includes('<m:logger') && xml.includes('d:id=') && xml.includes('<h:listener'));
  assert.ok(!xml.includes('xmlns:http=') && !xml.includes('xmlns:doc='));
  assert.equal(logger(xml).message, '#["alias"]');
});

test('comments, CDATA, Unicode, unknown attributes and unrelated formatting survive visual changes', () => {
  const untouched = '<!-- KEEP ሰላም <tag> & raw -->\n  <custom:thing xmlns:custom="urn:custom" attr = \'A &amp; B\'><![CDATA[one < two && three > four; <!DOCTYPE literal>]]></custom:thing>';
  let xml = fixture().replace('</zernova>', '  ' + untouched + '\n</zernova>').replace('level="INFO"', "odd = 'preserve' level = 'INFO'");
  const before = xml;
  xml = act(xml, { kind: 'updateComponent', id: logger(xml).id, values: { level: 'WARN' } });
  assert.equal(xml, before.replace("level = 'INFO'", "level = 'WARN'"));
  assert.ok(xml.includes(untouched));
});

test('unsupported constructs make their flow read-only but remain intact while another flow is edited', () => {
  const other = '<flow name="other"><future-processor value="#[{x:1}]"/><logger message="old"/></flow>';
  let xml = fixture().replace('</zernova>', '  ' + other + '\n</zernova>');
  const model = parseIntegration(xml);
  assert.equal(model.flows[1].editable, false);
  assert.deepEqual(model.flows[1].unsupported, ['future-processor']);
  assert.throws(() => act(xml, { kind: 'insert', flowId: 'flow:other', component: 'logger' }), /unsupported/);
  xml = act(xml, { kind: 'updateComponent', id: logger(xml).id, values: { category: 'io.owner' } });
  assert.ok(xml.includes(other));
});

test('invalid XML is diagnosed and visual edits never replace it with a valid model', () => {
  const invalid = fixture().replace('</zernova>', '</wrong>');
  assert.equal(parseIntegration(invalid).valid, false);
  assert.throws(() => act(invalid, { kind: 'createFlow', name: 'replace' }), /Repair XML syntax/);
  assert.equal(invalid.endsWith('</wrong>\n'), true);
  const accepted = parseRealMessage({ type: 'realXml', requestId: 'xml-1', version: 1, text: invalid });
  assert.equal(accepted?.type, 'realXml');
});

test('well-formedness supplements reject duplicate attrs, expanded aliases, multiple roots and illegal characters', () => {
  const head = `<zernova xmlns="${CORE_NS}"`;
  for (const xml of [head + ' a="1" a="2"/>', head + ' xmlns:x="urn:same" xmlns:y="urn:same" x:a="1" y:a="2"/>', head + '/><x/>', head + '>\u0000</zernova>', head + '> &#0; </zernova>', head + ' a="&#0;"/>', head + '>\ud800</zernova>']) {
    assert.equal(parseIntegration(xml).valid, false, JSON.stringify(xml));
  }
});

test('DOCTYPE/entities are rejected without fetching external resources', () => {
  const xml = '<!DOCTYPE mule SYSTEM "https://example.invalid/never.dtd"><zernova xmlns="' + CORE_NS + '"/>';
  assert.equal(parseIntegration(xml).valid, false);
  assert.match(parseIntegration(xml).diagnostics[0].message, /DTD/);
});

test('oversized UTF-8 files are rejected consistently by model and bridge', () => {
  const text = '<zernova xmlns="' + CORE_NS + '">' + 'ሰ'.repeat(MAX_XML_BYTES / 2) + '</zernova>';
  assert.equal(parseIntegration(text).valid, false);
  assert.match(parseIntegration(text).diagnostics[0].message, /1 MiB/);
  assert.equal(parseRealMessage({ type: 'realXml', version: 1, requestId: 'a', text }), null);
});

test('Logger literal/expression values preserve Unicode, newlines and XML characters', () => {
  let xml = fixture();
  const message = 'ሰላም & <tag> "quoted"\nsecond\tline';
  xml = act(xml, { kind: 'updateComponent', id: logger(xml).id, values: { message } });
  assert.equal(logger(xml).message, message);
  assert.ok(xml.includes('&amp;') && xml.includes('&lt;') && xml.includes('&#10;') && xml.includes('&#9;'));
  const expression = "#['message ' ++ attributes.queryParams.testId]";
  xml = act(xml, { kind: 'updateComponent', id: logger(xml).id, values: { message: expression } });
  assert.equal(logger(xml).message, expression);
});

test('response status/body/headers edit children without changing other response settings', () => {
  let xml = fixture().replace('<http:response statusCode="200">', '<http:response statusCode="200" custom="KEEP">');
  const headerBefore = xml.slice(xml.indexOf('<http:headers>'), xml.indexOf('</http:headers>') + '</http:headers>'.length);
  xml = act(xml, { kind: 'updateComponent', id: source(xml).id, values: { responseStatus: '201', responseBody: "#['ሰላም <&>']" } });
  assert.equal(source(xml).responseStatus, '201'); assert.equal(source(xml).responseBody, "#['ሰላም <&>']");
  assert.ok(xml.includes('custom="KEEP"') && xml.includes(headerBefore));
  xml = act(xml, { kind: 'updateComponent', id: source(xml).id, values: { responseStatus: '999' } });
  assert.equal(source(xml).responseStatus, '999');
  assert.ok(parseIntegration(xml).diagnostics.some(d => d.code === 'response-status'));
});

test('duplicate, reorder and delete retain Logger data and unique stable doc IDs', () => {
  let xml = fixture(); const first = logger(xml).id;
  const duplicate = applyOperation(xml, { kind: 'duplicateLogger', id: first }); xml = duplicate.text;
  const second = logger(xml, 1).id; assert.notEqual(first, second); assert.equal(second, duplicate.selectionId);
  xml = act(xml, { kind: 'updateComponent', id: second, values: { message: 'second message' } });
  xml = act(xml, { kind: 'moveLogger', id: second, direction: -1 });
  assert.equal(logger(xml).id, second); assert.equal(logger(xml).message, 'second message'); assert.equal(logger(xml, 1).id, first);
  xml = act(xml, { kind: 'deleteComponent', id: second });
  assert.equal(parseIntegration(xml).flows[0].processors.length, 1); assert.equal(logger(xml).id, first);
});

test('legacy Logger acquires doc:id for future stable selection while preserving its original attributes', () => {
  const xml = `<zernova xmlns="${CORE_NS}"><flow name="legacy"><logger message='one' odd='keep'/><logger message='two'/></flow></zernova>`;
  const result = applyOperation(xml, { kind: 'moveLogger', id: logger(xml).id, direction: 1 });
  assert.equal(logger(result.text).message, 'two'); assert.equal(logger(result.text, 1).message, 'one');
  assert.equal(logger(result.text, 1).id, result.selectionId); assert.ok(result.text.includes("odd='keep'"));
});

test('Listener cannot be duplicated/moved and editing a field from another kind is refused', () => {
  const xml = fixture(), id = source(xml).id;
  assert.throws(() => act(xml, { kind: 'duplicateLogger', id }), /Only processors/);
  assert.throws(() => act(xml, { kind: 'moveLogger', id, direction: 1 }), /Only processors/);
  assert.throws(() => act(xml, { kind: 'updateComponent', id, values: { message: 'not a Listener field' } }), /does not belong/);
  assert.throws(() => act(xml, { kind: 'updateComponent', id: logger(xml).id, values: { level: 'INVALID' } }), /Choose TRACE/);
});

test('configuration rename updates supported same-document references and preserves unknown attributes', () => {
  let xml = fixture().replace('name="HTTP_Listener_config"', 'name="HTTP_Listener_config" custom="KEEP"');
  xml = act(xml, { kind: 'saveConfig', id: 'config:HTTP_Listener_config', values: { ...config, name: 'renamed_config', port: '18083' } });
  assert.equal(source(xml).configId, 'config:renamed_config'); assert.equal(parseIntegration(xml).configs[0].port, '18083');
  assert.ok(xml.includes('custom="KEEP"'));
  assert.equal(findConfigurationReferences(xml, 'HTTP_Listener_config').references, 0);
  assert.equal(findConfigurationReferences(xml, 'renamed_config').references, 1);
});

test('configuration deletion refuses references and rename refuses unsupported reference types', () => {
  const xml = fixture();
  assert.throws(() => act(xml, { kind: 'deleteConfig', id: 'config:HTTP_Listener_config' }), /referenced/);
  const unknown = xml.replace('</zernova>', '<thing xmlns="urn:custom" config-ref="HTTP_Listener_config"/></zernova>');
  assert.throws(() => act(unknown, { kind: 'saveConfig', id: 'config:HTTP_Listener_config', values: { ...config, name: 'newName' } }), /outside the supported/);
  const noListener = act(xml, { kind: 'deleteComponent', id: source(xml).id });
  assert.equal(parseIntegration(act(noListener, { kind: 'deleteConfig', id: 'config:HTTP_Listener_config' })).configs.length, 0);
});

test('HTTP settings validate static values and preserve runtime properties as unevaluated text', () => {
  let xml = fixture();
  assert.throws(() => act(xml, { kind: 'saveConfig', id: 'config:HTTP_Listener_config', values: { ...config, port: '22' } }), /1024/);
  xml = act(xml, { kind: 'saveConfig', id: 'config:HTTP_Listener_config', values: { ...config, port: '${http.port}' } });
  assert.equal(parseIntegration(xml).configs[0].port, '${http.port}');
  xml = act(xml, { kind: 'updateComponent', id: source(xml).id, values: { path: '${http.path}', responseStatus: '#[vars.httpStatus default 200]' } });
  assert.equal(source(xml).path, '${http.path}');
});

test('missing HTTP/doc namespaces and schema entries are added once without replacing existing declarations', () => {
  let xml = `<m:zernova xmlns:m="${CORE_NS}" xmlns:http="urn:not-http"><m:flow name="helloFlow"/></m:zernova>`;
  xml = act(xml, { kind: 'saveConfig', values: config });
  xml = act(xml, { kind: 'insert', flowId: 'flow:helloFlow', component: 'listener' });
  assert.ok(xml.includes('xmlns:http="urn:not-http"'));
  assert.ok(xml.includes('xmlns:http2="' + HTTP_NS + '"'));
  assert.equal((xml.match(/xmlns:http2=/g) ?? []).length, 1);
  assert.equal((xml.match(/xmlns:doc=/g) ?? []).length, 1);
  assert.equal((xml.match(/current\/zernova-http.xsd/g) ?? []).length, 1);
  assert.equal(parseIntegration(xml).valid, true);
});

test('non-HTTP config, unknown Listener children and duplicate source positions are read-only', () => {
  const xml = fixture();
  assert.equal(parseIntegration(xml.replace('protocol="HTTP"', 'protocol="HTTPS"')).configs[0].editable, false);
  assert.equal(source(xml.replace('</http:listener>', '<x xmlns="urn:x"/></http:listener>')).editable, false);
  const listenerText = xml.slice(xml.indexOf('<http:listener '), xml.indexOf('</http:listener>') + '</http:listener>'.length);
  const malformed = xml.replace('</flow>', listenerText + '</flow>');
  assert.equal(parseIntegration(malformed).flows[0].editable, false);
  assert.ok(parseIntegration(malformed).diagnostics.some(d => d.code === 'source-position'));
});

test('real protocol rejects arbitrary command/path payloads, unknown fields and stale-version shapes', () => {
  assert.equal(parseRealMessage({ type: 'realAction', action: 'run', executable: '/bin/sh' }), null);
  assert.equal(parseRealMessage({ type: 'realAction', action: 'executeCommand' }), null);
  assert.equal(parseRealMessage({ type: 'realXml', requestId: 'a', version: -1, text: '<zernova/>' }), null);
  assert.equal(parseRealMessage({ type: 'realXml', requestId: 'a', version: 1, text: '', uri: 'file:///tmp/elsewhere' }), null);
  assert.equal(parseRealMessage({ type: 'realXml', version: 1, text: '' }), null);
  assert.equal(parseXmlOperation({ kind: 'updateComponent', id: 'a', values: { shell: 'anything' } }), null);
  assert.equal(parseXmlOperation({ kind: 'saveConfig', values: { ...config, protocol: 'HTTPS' } }), null);
  assert.equal(parseRealMessage({ type: 'realEdit', requestId: 'r1', version: 1, operation: { kind: 'createFlow', name: 'hello' } })?.type, 'realEdit');
});

test('preview and real protocols remain isolated capability surfaces', () => {
  assert.equal(parseWebviewMessage({ type: 'realAction', action: 'run' }), null);
  assert.equal(parseWebviewMessage({ type: 'realXml', requestId: 'x', version: 1, text: '' }), null);
  assert.equal(parseRealMessage({ type: 'saveState', state: {} }), null);
  assert.equal(parseRealMessage({ type: 'resetPreview' }), null);
});

test('legacy import changes XML contracts without rewriting application data', () => {
  const old = `<mule xmlns="http://www.mulesoft.org/schema/mule/core"><!-- <mule> stays history --><flow name="legacy"><logger message="mule"/><set-payload><![CDATA[<mule xmlns="http://www.mulesoft.org/schema/mule/core">]]></set-payload></flow></mule>`;
  const converted = convertLegacyIntegrationXml(old);
  assert.ok(converted.startsWith('<zernova xmlns="' + CORE_NS + '">'));
  assert.ok(converted.endsWith('</zernova>'));
  assert.ok(converted.includes('<!-- <mule> stays history -->'));
  assert.ok(converted.includes('message="mule"'));
  assert.ok(converted.includes('<![CDATA[<mule xmlns="http://www.mulesoft.org/schema/mule/core">]]>'));
  assert.equal(parseIntegration(converted).valid, true);
});
test('subflows grow the same processor model and reject listener insertion', () => {
  let xml = createIntegrationXml().replace('</zernova>', '<sub-flow name="logging"><logger message="one"/></sub-flow></zernova>');
  assert.equal(parseIntegration(xml).flows[0].kind, 'sub-flow');
  xml = act(xml, { kind: 'insert', flowId: 'flow:logging', component: 'logger' });
  assert.equal(parseIntegration(xml).flows[0].processors.length, 2);
  assert.throws(() => act(xml, { kind: 'insert', flowId: 'flow:logging', component: 'listener' }), /subflow cannot contain/);
});

test('Error handler round-trip keeps main and error processors separate and inserts before handler', () => {
  let xml = act(fixture(), { kind: 'createErrorHandler', flowId: 'flow:helloFlow' });
  let f = parseIntegration(xml).flows[0];
  assert(f.editable && f.errorHandler?.editable); assert.equal(f.errorHandler.processors.length, 1);
  assert.equal(f.processors.length, 1);
  xml = act(xml, { kind: 'insert', flowId: f.id, component: 'logger' });
  assert.equal(parseIntegration(xml).flows[0].processors.length, 2);
  assert(xml.indexOf(parseIntegration(xml).flows[0].processors[1].id) < xml.indexOf('<error-handler'));
  xml = act(xml, { kind: 'insert', flowId: f.id, component: 'logger', lane: 'error', index: 0 });
  f = parseIntegration(xml).flows[0]; assert.equal(f.processors.length, 2); assert.equal(f.errorHandler!.processors.length, 2);
  const errorLogger = f.errorHandler!.processors[0];
  xml = act(xml, { kind: 'updateComponent', id: errorLogger.id, values: { message: 'Changed error logger' } });
  xml = act(xml, { kind: 'moveLogger', id: errorLogger.id, direction: 1 });
  const changedError=parseIntegration(xml).flows[0].errorHandler!.processors[1]; assert(changedError.kind === 'logger'); assert.equal(changedError.message, 'Changed error logger');
  xml = act(xml, { kind: 'deleteComponent', id: errorLogger.id });
  assert.equal(parseIntegration(xml).flows[0].errorHandler!.processors.length, 1);
});
test('Invalid error destinations and duplicate handlers are rejected without losing XML', () => {
  const xml = act(fixture(), { kind: 'createErrorHandler', flowId: 'flow:helloFlow' });
  assert.throws(() => act(xml, { kind: 'createErrorHandler', flowId: 'flow:helloFlow' }));
  assert.throws(() => act(xml, { kind: 'insert', flowId: 'flow:helloFlow', component: 'listener', lane: 'error' }));
  assert.equal(parseXmlOperation({ kind: 'insert', flowId: 'flow:helloFlow', component: 'logger', lane: 'arbitrary' }), null);
  assert(parseXmlOperation({ kind: 'insert', flowId: 'flow:helloFlow', component: 'logger', lane: 'error', index: 0 }));
});
test('Extended error handlers keep variable processors editable and preserve their XML', () => {
  const xml = fixture().replace('</flow>', '<error-handler><on-error-propagate type="ANY"><set-variable variableName="owner" value="original"/></on-error-propagate></error-handler></flow>');
  const f = parseIntegration(xml).flows[0]; assert(f.editable); assert.equal(f.errorHandler!.editable, true);
  const next = act(xml, { kind: 'insert', flowId: f.id, component: 'logger' });
  assert(next.includes('<set-variable variableName="owner" value="original"/>'));
});

test('Transform Message scripts round-trip without disturbing unrelated XML, and participate in processor operations', () => {
  let xml=fixture().replace('</zernova>','<!-- keep owner comment -->\n</zernova>');
  const inserted=applyOperation(xml,{kind:'insert',flowId:'flow:helloFlow',component:'transform',index:0});xml=inserted.text;
  const script='%dw 2.0\noutput application/json\n---\n{message: "hello & <world>", incoming: payload}';
  const operation={kind:'updateComponent' as const,id:inserted.selectionId!,values:{script,mimeType:'application/json'}};
  assert(parseXmlOperation(operation));xml=act(xml,operation);
  let node=parseIntegration(xml).flows[0].processors[0];assert(node.kind==='transform');assert.equal(node.script,script);assert.equal(node.mimeType,'application/json');assert(xml.includes('keep owner comment'));
  xml=act(xml,{kind:'duplicateProcessor',id:node.id});assert.equal(parseIntegration(xml).flows[0].processors.length,3);
  xml=act(xml,{kind:'moveProcessor',id:node.id,direction:1});assert.equal(parseIntegration(xml).flows[0].processors[1].id,node.id);
  xml=act(xml,{kind:'deleteComponent',id:node.id});assert.equal(parseIntegration(xml).flows[0].processors.length,2);
});
test('Existing native set-payload and handler transforms remain executable editable XML', () => {
  let xml=act(fixture(),{kind:'createErrorHandler',flowId:'flow:helloFlow'});
  xml=act(xml,{kind:'insert',flowId:'flow:helloFlow',component:'transform',lane:'error'});
  const model=parseIntegration(xml);assert(model.flows[0].editable);assert(model.flows[0].errorHandler!.editable);assert.equal(model.flows[0].errorHandler!.processors[1].kind,'transform');
  const node=model.flows[0].errorHandler!.processors[1];xml=act(xml,{kind:'updateComponent',id:node.id,values:{script:''}});assert(parseIntegration(xml).diagnostics.some(d=>d.code==='transform-script'));
});

test('Transform preserves independent payload, attributes and named-variable scripts including blank outputs',()=>{
  let xml=act(fixture(),{kind:'insert',flowId:'flow:helloFlow',component:'transform'});
  const id=parseIntegration(xml).flows[0].processors.at(-1)!.id;
  xml=act(xml,{kind:'updateComponent',id,values:{script:'',attributesScript:'%dw 2.0\noutput application/java\n---\n{requestId: attributes.queryParams.id}',variablesScript:'{customer: payload.id, label: "A & B"}'}});
  const node=parseIntegration(xml).flows[0].processors.at(-1)!;
  assert(node.kind==='transform');assert.equal(node.script,'');assert.equal(node.variablesScript,'{customer: payload.id, label: "A & B"}');assert.match(node.attributesScript!,/requestId/);
  assert(!parseIntegration(xml).diagnostics.some(d=>d.code==='transform-script'));
});
test('Editing a legacy payload transform upgrades it without losing the script, ID, label, comment or other XML',()=>{
 let xml=fixture().replace('</flow>','<!-- original -->\n<set-payload xmlns:doc="https://schemas.zernova.dev/documentation" doc:id="old-transform" doc:name="Keep name" value="#[%dw 2.0&#10;output application/java&#10;---&#10;{answer: 42}]" mimeType="application/java"/>\n</flow>');
 assert(parseIntegration(xml).flows[0].processors.some(p=>p.id==='old-transform'));
 xml=act(xml,{kind:'updateComponent',id:'old-transform',values:{variablesScript:'{saved: payload}'}});
 const node=parseIntegration(xml).flows[0].processors.find(p=>p.id==='old-transform')!;
 assert(node.kind==='transform');assert.equal(node.script,'%dw 2.0\noutput application/java\n---\n{answer: 42}');assert.equal(node.displayName,'Keep name');assert.equal(node.variablesScript,'{saved: payload}');assert(xml.includes('<!-- original -->'));
});
