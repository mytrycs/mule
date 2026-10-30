/* Copyright (c) 2026 ZerNova. Original runtime portability test harness.
 * Tests the exact portable backend used by Studio, not a mock execution engine.
 */
const fs=require('node:fs/promises'),path=require('node:path'),os=require('node:os'),net=require('node:net');
const assert=require('node:assert/strict');const {randomUUID,createHash}=require('node:crypto');
const {BundledRuntime,checkPort}=require('../dist/portable-runtime.cjs');
const {validateArtifact}=require('../dist/portable-packager.cjs');
(async()=>{
 const target=`${process.platform}-${process.arch}`;
 const base=path.resolve(process.env.ZN_NATIVE_WORK||path.join(os.homedir(),'.zernova-native-tests',randomUUID()));
 const project=path.join(base,'Project with spaces'),instance=path.join(base,'private instance');
 const bundle=path.resolve(process.env.ZN_NATIVE_BUNDLE||'runtime');
 const output=path.resolve(process.env.ZN_NATIVE_EVIDENCE||'evidence/native-'+target);
 await fs.mkdir(output,{recursive:true});await fs.mkdir(project,{recursive:true});
 await fs.cp(path.resolve('tests/fixtures/native-hello'),project,{recursive:true});
 const app='hello-zernova',xml=path.join(project,'src/main/zernova/hello.xml');
 const initial=await fs.readFile(xml,'utf8');
 const r=new BundledRuntime({bundle,instance,project,application:app,onOutput:t=>process.stdout.write(t)});
 const port=18281,rows=[];let artifact;
 const persist=async()=>fs.writeFile(path.join(output,'results.json'),JSON.stringify({target,platform:process.platform,architecture:process.arch,node:process.version,os:os.release(),bundle,instance,project,completed:rows.length,passed:rows.filter(x=>x.status==='PASS').length,failed:rows.filter(x=>x.status==='FAIL').length,cases:rows},null,2)+'\n');
 async function test(name,fn){try{const detail=await fn();rows.push({name,status:'PASS',detail});console.log('PASS '+name)}catch(e){rows.push({name,status:'FAIL',error:String(e.stack||e)});throw e}finally{await persist()}}
 async function request(route='/hello'){
   const id='ZN-'+randomUUID();const res=await fetch(`http://127.0.0.1:${port}${route}?testId=${id}`,{signal:AbortSignal.timeout(10000)}),body=await res.text();
   assert.equal(res.status,200);assert.equal(body,'Hello from ZERNOVA');let logs=[];
   for(let i=0;i<60;i++){logs=(await r.logs()).lines.filter(l=>l.includes('ZERNOVA_TEST '+id));if(logs.length)break;await new Promise(r=>setTimeout(r,100))}
   assert(logs.length,'Fresh correlation Logger entry missing');return{status:res.status,body,testId:id,matchingLogs:logs};
 }
 try{
   await test('Native bundled Java architecture and complete integrity',()=>r.doctor());
   await test('Offline build in path containing spaces',async()=>{artifact=await r.package();assert.equal(artifact.networkUsed,false);return artifact});
   await test('Deterministic repackage',async()=>{const repeated=await r.package();assert.equal(artifact.sha256,repeated.sha256);return{sha256:repeated.sha256}});
   await test('Unsafe XML does not replace last valid artifact',async()=>{await fs.writeFile(xml,initial.replace('<zernova ', '<!DOCTYPE mule [<!ENTITY x SYSTEM "file:///etc/passwd">]>\n<zernova '));await assert.rejects(r.package(),/DTD|entity/i);assert.equal((await validateArtifact(artifact.artifact)).sha256,artifact.sha256);await fs.writeFile(xml,initial)});
   await test('Occupied port is rejected without killing its owner',async()=>{const server=net.createServer();await new Promise((ok,no)=>{server.once('error',no);server.listen(port,'127.0.0.1',ok)});try{await assert.rejects(r.start(port),/unavailable/i);assert(server.listening)}finally{await new Promise(ok=>server.close(ok))}});
   await test('Real source-built engine startup',()=>r.start(port));
   await test('Authenticated control rejects wrong token',async()=>{await assert.rejects(r.controlRequest('STOP','0'.repeat(64)),/reject/i);assert.equal((await r.status()).process,'RUNNING')});
   await test('Real HTTP Listener and Logger deployment',()=>r.deploy(artifact.artifact));
   await test('Actual HTTP response plus matching fresh Logger',()=>request());
   await test('A new manager reconciles the live runtime',async()=>{const next=new BundledRuntime({bundle,instance,project,application:app});const status=await next.status();assert.equal(status.process,'RUNNING');assert.equal(status.applications[app].state,'DEPLOYED');return status});
   await test('Refuse unrelated application ownership',async()=>{const other=new BundledRuntime({bundle,instance,project:project+'-other',application:'other-app'});await assert.rejects(other.stop(),/different|unrecognized|another/i);assert.equal((await r.status()).process,'RUNNING')});
   await test('Edit package and redeploy actual changed route',async()=>{await r.undeploy();await fs.writeFile(xml,initial.replace('path="/hello"','path="/hello-v2"'));artifact=await r.package();await r.deploy(artifact.artifact);const result=await request('/hello-v2');const old=await fetch(`http://127.0.0.1:${port}/hello`,{signal:AbortSignal.timeout(5000)});assert.equal(old.status,404);return{...result,oldRouteStatus:old.status}});
   await test('Runtime expression errors produce a real HTTP failure',async()=>{await r.undeploy();await fs.writeFile(xml,initial.replace("#['Hello from ZERNOVA']","#[1 + ]"));const invalid=await r.package();await r.deploy(invalid.artifact);const response=await fetch(`http://127.0.0.1:${port}/hello?testId=ZN-INVALID-EXPRESSION`,{signal:AbortSignal.timeout(10000)});const body=await response.text();assert.equal(response.status,500);return{status:response.status,body:body.slice(0,2000),note:'Response expressions compile on execution; successful deployment is not endpoint validation.'}});
   await test('Invalid configuration fails deployment rather than reporting healthy',async()=>{await r.undeploy();await fs.writeFile(xml,initial.replace('config-ref="Local_HTTP"','config-ref="Missing_Configuration"'));const invalid=await r.package();await assert.rejects(r.deploy(invalid.artifact),/failed|timed out/i);const status=await r.status();assert.notEqual(status.applications[app].healthy,true);return status});
   await test('Graceful shutdown and failed application cleanup',async()=>{const stopped=await r.stop();await r.undeploy();await checkPort(port);assert.equal((await r.status()).process,'STOPPED');return stopped});
   await test('Second cycle after complete stop',async()=>{await fs.writeFile(xml,initial);artifact=await r.package();await r.start(port);await r.deploy(artifact.artifact);const result=await request();await r.undeploy();const stop=await r.stop();await checkPort(port);return{...result,stop}});
 }finally{
   try{await r.stop();await r.undeploy()}catch(e){rows.push({name:'Final cleanup',status:'FAIL',error:String(e)});console.error(e)}
   await persist();
   await fs.cp(path.join(instance,'runs'),path.join(output,'runs'),{recursive:true}).catch(()=>{});
   await fs.copyFile(path.join(instance,'state.json'),path.join(output,'final-state.json')).catch(()=>{});
   await fs.writeFile(path.join(output,'source-hashes.json'),JSON.stringify(Object.fromEntries(await Promise.all(['src/portable/bundle.ts','src/portable/packager.ts','src/portable/runtime.ts','runtime-support/java/com/zernova/studio/RuntimeControlAgent.java'].map(async f=>[f,createHash('sha256').update(await fs.readFile(f)).digest('hex')]))),null,2)+'\n');
   // Never retain auth tokens in evidence. Only public launch arguments and logs.
   if(rows.some(x=>x.status==='FAIL'))process.exitCode=1;
 }
})().catch(e=>{console.error(e);process.exitCode=1});
