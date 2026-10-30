import test from 'node:test';
import assert from 'node:assert/strict';
import {CORE_CATALOG,acceptsChild} from '../src/shared/coreCatalog';
import {applyOperation,createIntegrationXml,parseIntegration,processorTree,type RealCore} from '../src/shared/xml';
import {componentLocation} from '../src/shared/breakpoints';
const base=()=>applyOperation(createIntegrationXml(),{kind:'createFlow',name:'test'}).text;
test('every catalog entry projects and round-trips editable native XML in a valid parent',()=>{
 for(const definition of CORE_CATALOG){let xml=base();let parentId:string|undefined;
 const parent=definition.key==='when'||definition.key==='otherwise'?'choice':definition.key==='route'?'scatter-gather':definition.key.startsWith('on-error-')?'error-handler':definition.key==='batch-step'||definition.key==='batch-on-complete'?'batch-job':definition.key==='batch-aggregator'?'batch-step':undefined;
 if(parent){if(parent==='batch-step'){xml=applyOperation(xml,{kind:'insert',flowId:'flow:test',component:'batch-job'}).text;parentId=(parseIntegration(xml).flows[0].processors[0] as RealCore).children[0].id;}else{xml=applyOperation(xml,{kind:'insert',flowId:'flow:test',component:parent}).text;parentId=parseIntegration(xml).flows[0].processors[0]?.id??parseIntegration(xml).flows[0].errorHandler?.component?.id;}
 if(definition.key==='otherwise')continue;
 }
 const result=applyOperation(xml,{kind:'insert',flowId:'flow:test',component:definition.key,...(parentId?{parentId}:{})});const model=parseIntegration(result.text);assert(model.valid,definition.key);const node=processorTree(model.flows[0]).find(p=>p.id===result.selectionId);assert(node?.kind==='core',definition.key);assert.equal(node.coreType,definition.key);assert(node.editable,definition.key);
 }
});
test('nested edits preserve enclosing XML, create unique descendant IDs and map breakpoint locations',()=>{
 let xml=applyOperation(base(),{kind:'insert',flowId:'flow:test',component:'choice'}).text;
 const choice=parseIntegration(xml).flows[0].processors[0] as RealCore,branch=choice.children[0] as RealCore;
 const inserted=applyOperation(xml,{kind:'insert',flowId:'flow:test',component:'try',parentId:branch.id});xml=inserted.text;
 const scope=processorTree(parseIntegration(xml).flows[0]).find(n=>n.id===inserted.selectionId) as RealCore;
 assert.equal(componentLocation(parseIntegration(xml),scope.children[0].id),'test/processors/0/route/0/processors/1/processors/0');
 xml=applyOperation(xml,{kind:'duplicateProcessor',id:scope.id}).text;
 const nodes=processorTree(parseIntegration(xml).flows[0]);assert.equal(new Set(nodes.map(n=>n.id)).size,nodes.length);
 assert.throws(()=>applyOperation(xml,{kind:'insert',flowId:'flow:test',component:'when'}),/matching scope/);
 assert(!acceptsChild('choice','logger'));assert(!acceptsChild('flow','batch-step'));
});
test('external transform targets, listener errors and environment configuration round-trip',()=>{
 let xml=applyOperation(base(),{kind:'insert',flowId:'flow:test',component:'listener'}).text;
 let model=parseIntegration(xml),listener=model.flows[0].source!;
 xml=applyOperation(xml,{kind:'updateComponent',id:listener.id,values:{errorStatus:'503',errorBody:"#[{error: error.description}]",errorHeaders:"#[{'X-Error':'yes'}]"}}).text;
 const inserted=applyOperation(xml,{kind:'insert',flowId:'flow:test',component:'transform'});xml=inserted.text;
 xml=applyOperation(xml,{kind:'updateComponent',id:inserted.selectionId!,values:{script:'',variablesResource:'dwl/variables.dwl'}}).text;
 xml=applyOperation(xml,{kind:'saveGlobalProperty',name:'zernova.env',value:'development'}).text;
 xml=applyOperation(xml,{kind:'savePropertyFile',file:'config/application-${zernova.env}.properties'}).text;
 model=parseIntegration(xml);const node=model.flows[0].processors[0];assert(node.kind==='transform');assert.equal(node.script,'');assert.equal(node.variablesResource,'dwl/variables.dwl');assert(!model.diagnostics.some(d=>d.code==='transform-script'));
 assert(model.flows[0].source?.kind==='listener');assert.equal(model.flows[0].source.errorStatus,'503');assert.equal(model.properties?.[0].file,'config/application-${zernova.env}.properties');assert.equal(model.globalProperties?.[0].value,'development');
 const invalid=applyOperation(xml,{kind:'updateComponent',id:node.id,values:{payloadResource:'../outside.dwl'}}).text;assert(parseIntegration(invalid).diagnostics.some(d=>d.code==='transform-resource'));
});
test('Scheduler editing preserves cron time zone and comments',()=>{
 let xml=applyOperation(base(),{kind:'insert',flowId:'flow:test',component:'scheduler'}).text;let source=parseIntegration(xml).flows[0].source!;
 xml=applyOperation(xml,{kind:'updateCore',id:source.id,values:{cron:'0 0 * * * ?',timeZone:'UTC'}}).text.replace('<cron ','<!--keep-->\n<cron ');
 source=parseIntegration(xml).flows[0].source!;xml=applyOperation(xml,{kind:'updateCore',id:source.id,values:{displayName:'Hourly'}}).text;
 assert(xml.includes('<!--keep-->'));assert(xml.includes('timeZone="UTC"'));assert.equal(parseIntegration(xml).flows[0].source?.displayName,'Hourly');
});
