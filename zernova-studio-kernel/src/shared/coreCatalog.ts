/* Copyright (c) 2026 ZerNova. ZERNOVA authoring catalog; runtime XML stays native. */
export interface CoreField { key:string; label:string; initial:string; multiline?:boolean; required?:boolean; }
export interface CoreDefinition { key:string; name:string; group:string; local:string; namespace:'core'|'support'; fields:CoreField[]; children?:'processors'|'choice'|'routes'|'handlers'|'batch'; minimum?:number; help:string; }
const field=(key:string,label:string,initial='',multiline=false,required=false):CoreField=>({key,label,initial,multiline,required});
export const CORE_CATALOG:CoreDefinition[]=[
 {key:'set-payload',name:'Set Payload',group:'Transformers',local:'set-payload',namespace:'core',fields:[field('value','Value','#[payload]',true,true),field('mimeType','MIME type','application/java')],help:'Replaces the payload. HTTP response settings belong to the Listener.'},
 {key:'set-variable',name:'Set Variable',group:'Transformers',local:'set-variable',namespace:'core',fields:[field('variableName','Variable name','value',false,true),field('value','Value','#[payload]',true,true),field('mimeType','MIME type','application/java')],help:'Sets a named event variable; use vars.name in DataWeave.'},
 {key:'remove-variable',name:'Remove Variable',group:'Transformers',local:'remove-variable',namespace:'core',fields:[field('variableName','Variable name','value',false,true)],help:'Removes a named event variable.'},
 {key:'flow-ref',name:'Flow Reference',group:'Components',local:'flow-ref',namespace:'core',fields:[field('name','Flow / subflow name','',false,true),field('target','Target variable'),field('targetValue','Target value','#[payload]',true)],help:'Calls another flow/subflow and returns to this flow. An optional target stores its result.'},
 {key:'parse-template',name:'Parse Template',group:'Components',local:'parse-template',namespace:'core',fields:[field('location','Template resource path','templates/message.txt',false,true)],help:'Reads a template resource and evaluates its embedded expressions.'},
 {key:'dynamic-evaluate',name:'Dynamic Evaluate',group:'Components',local:'evaluate',namespace:'support',fields:[field('expression','Expression returning a DataWeave script','#[vars.script]',true,true)],help:'Evaluates a script selected by a DataWeave expression and sets the payload.'},
 {key:'business-event',name:'Custom Business Event',group:'Components',local:'business-event',namespace:'support',fields:[field('name','Event name','application.event',false,true),field('data','Event fields','#[{correlationId: correlationId}]',true)],help:'Emits a structured ZERNOVA business-event log entry with the event correlation ID.'},
 {key:'choice',name:'Choice',group:'Flow Control',local:'choice',namespace:'core',fields:[],children:'choice',minimum:1,help:'Evaluates When conditions in order and selects one branch; Otherwise is the fallback.'},
 {key:'when',name:'When',group:'Routes',local:'when',namespace:'core',fields:[field('expression','Condition','#[true]',true,true)],children:'processors',minimum:1,help:'A condition branch inside Choice.'},
 {key:'otherwise',name:'Otherwise',group:'Routes',local:'otherwise',namespace:'core',fields:[],children:'processors',minimum:1,help:'The fallback branch inside Choice.'},
 {key:'first-successful',name:'First Successful',group:'Flow Control',local:'first-successful',namespace:'core',fields:[],children:'routes',minimum:1,help:'Tries routes in order until one succeeds.'},
 {key:'round-robin',name:'Round Robin',group:'Flow Control',local:'round-robin',namespace:'core',fields:[],children:'routes',minimum:1,help:'Selects the next route for each event.'},
 {key:'scatter-gather',name:'Scatter-Gather',group:'Flow Control',local:'scatter-gather',namespace:'core',fields:[field('timeout','Timeout (ms)'),field('maxConcurrency','Maximum concurrency')],children:'routes',minimum:2,help:'Runs routes independently and collects their messages into a result object.'},
 {key:'route',name:'Route',group:'Routes',local:'route',namespace:'core',fields:[],children:'processors',minimum:1,help:'A child route inside Scatter-Gather.'},
 {key:'async',name:'Async',group:'Scopes',local:'async',namespace:'core',fields:[field('maxConcurrency','Maximum concurrency')],children:'processors',minimum:1,help:'Runs its child chain asynchronously. Results do not replace the main event.'},
 {key:'foreach',name:'For Each',group:'Scopes',local:'foreach',namespace:'core',fields:[field('collection','Collection','#[payload]',true,true),field('batchSize','Batch size','1'),field('counterVariableName','Counter variable','counter'),field('rootMessageVariableName','Root message variable','rootMessage')],children:'processors',minimum:1,help:'Processes collection items in sequence; the original payload is restored after iteration.'},
 {key:'try',name:'Try',group:'Scopes',local:'try',namespace:'core',fields:[field('transactionalAction','Transactional action','INDIFFERENT')],children:'processors',minimum:1,help:'Groups processors and can have its own local error handler.'},
 {key:'until-successful',name:'Until Successful',group:'Scopes',local:'until-successful',namespace:'core',fields:[field('maxRetries','Maximum retries','5'),field('millisBetweenRetries','Delay between retries (ms)','1000')],children:'processors',minimum:1,help:'Retries the child chain after an error, up to the configured retry limit.'},
 {key:'cache',name:'Cache',group:'Scopes',local:'cache',namespace:'support',fields:[field('key','Cache-key expression','#[payload]',true,true),field('ttlMillis','Time to live (ms)','60000'),field('maxEntries','Maximum entries','1000')],children:'processors',minimum:1,help:'Caches completed child-chain messages in this application with TTL and a bounded entry limit.'},
 {key:'raise-error',name:'Raise Error',group:'Error Handling',local:'raise-error',namespace:'core',fields:[field('type','Error type','APP:ERROR',false,true),field('description','Description','An application error occurred',true)],help:'Raises a typed application error for a local or default error handler.'},
 {key:'error-handler',name:'Error Handler',group:'Error Handling',local:'error-handler',namespace:'core',fields:[],children:'handlers',minimum:1,help:'Contains ordered On Error branches. Valid on a flow or Try scope.'},
 {key:'on-error-continue',name:'On Error Continue',group:'Error Handling',local:'on-error-continue',namespace:'core',fields:[field('type','Error types','ANY'),field('when','Condition','',true),field('logException','Log exception','true')],children:'processors',minimum:1,help:'Handles the error and produces a normal result for the owning flow/scope.'},
 {key:'on-error-propagate',name:'On Error Propagate',group:'Error Handling',local:'on-error-propagate',namespace:'core',fields:[field('type','Error types','ANY'),field('when','Condition','',true),field('logException','Log exception','true')],children:'processors',minimum:1,help:'Runs its child chain and propagates the error to the caller.'},
 {key:'batch-job',name:'Batch Job',group:'Batch',local:'batch-job',namespace:'support',fields:[field('name','Job name','batchJob',false,true),field('blockSize','Record block size','100'),field('maxFailedRecords','Maximum failed records','-1')],children:'batch',minimum:1,help:'Runs record-processing steps with per-record outcomes and a completion report.'},
 {key:'batch-step',name:'Batch Step',group:'Batch',local:'batch-step',namespace:'support',fields:[field('name','Step name','step',false,true),field('acceptExpression','Accept record condition','#[true]',true),field('acceptPolicy','Accept policy','NO_FAILURES')],children:'processors',minimum:1,help:'Processes accepted records in a Batch Job.'},
 {key:'batch-on-complete',name:'Batch On Complete',group:'Batch',local:'batch-on-complete',namespace:'support',fields:[],children:'processors',minimum:1,help:'Receives the completion report after record processing.'},
 {key:'batch-aggregator',name:'Batch Aggregator',group:'Batch',local:'batch-aggregator',namespace:'support',fields:[field('size','Aggregation size','100')],children:'processors',minimum:1,help:'Processes groups of records within a Batch Step.'},
];
export type PaletteKind='listener'|'logger'|'transform'|'scheduler'|'flow'|'sub-flow'|string;
export const coreDefinition=(key:string)=>CORE_CATALOG.find(d=>d.key===key);
export const coreElement=(namespace:string,local:string)=>CORE_CATALOG.find(d=>d.local===local&&(d.namespace==='core'?namespace==='https://schemas.zernova.dev/core':namespace==='https://schemas.zernova.dev/transform'));
export function acceptsChild(parent:string,child:string):boolean{
 if(['listener','scheduler','flow','sub-flow'].includes(child))return false;
 if(child==='error-handler')return parent==='flow'||parent==='try';
 if(child.startsWith('on-error-'))return parent==='error-handler';
 if(parent==='error-handler')return child.startsWith('on-error-');
 if(child==='when'||child==='otherwise')return parent==='choice';
 if(parent==='choice')return child==='when'||child==='otherwise';
 if(child==='route')return ['scatter-gather','first-successful','round-robin'].includes(parent);
 if(['scatter-gather','first-successful','round-robin'].includes(parent))return child==='route';
 if(child==='batch-step'||child==='batch-on-complete')return parent==='batch-job';
 if(parent==='batch-job')return child==='batch-step'||child==='batch-on-complete';
 if(child==='batch-aggregator')return parent==='batch-step';
 return parent==='flow'||!!coreDefinition(parent)?.children;
}
