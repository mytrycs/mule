import test from 'node:test';
import assert from 'node:assert/strict';
import {parseProperties,expandProperties,projectProperties} from '../src/host/propertyResolution';
import {mkdtemp,mkdir,writeFile,rm} from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import {parseIntegration,createIntegrationXml,applyOperation} from '../src/shared/xml';
import {validateRunConfiguration} from '../../zernova-studio-build/src/portable/run-config';
test('Java properties parsing handles escaped delimiters, Unicode, continuations and rejects malformed Unicode',()=>{
 const values=parseProperties('#comment\nkey\\:name : Caf\\u00e9\\\n  example\nescaped\\ key=one\\ttwo\n');assert.equal(values['key:name'],'Caféexample');assert.equal(values['escaped key'],'one\ttwo');assert.throws(()=>parseProperties('x=\\u00zz'),/Unicode/);
 assert.throws(()=>expandProperties('${a}',k=>({a:'${b}',b:'${a}'}[k])),/Cyclic/);assert.throws(()=>expandProperties('${absent}',()=>undefined),/Unresolved/);
});
test('environment file selection and system/environment/application/global precedence agree',async()=>{
 const root=await mkdtemp(path.join(os.tmpdir(),'zn-properties-'));try{
 await mkdir(path.join(root,'src/main/resources/config'),{recursive:true});await writeFile(path.join(root,'src/main/resources/config/qa.properties'),'message=qa\nfileWins=file\nenvWins=file\nsystemWins=file\n');
 let xml=createIntegrationXml();for(const [name,value]of Object.entries({'zernova.env':'dev',fileWins:'global',globalOnly:'default'}))xml=applyOperation(xml,{kind:'saveGlobalProperty',name,value}).text;
 xml=applyOperation(xml,{kind:'savePropertyFile',file:'config/${zernova.env}.properties'}).text;
 const run=validateRunConfiguration({environment:'qa',variables:{envWins:'environment'},debugPort:5005,properties:{systemWins:'system'}});
 const resolver=await projectProperties(root,[parseIntegration(xml)],run);assert.equal(resolver.resolve('${message}/${fileWins}/${envWins}/${systemWins}/${globalOnly}'),'qa/file/environment/system/default');assert.deepEqual(resolver.files,['config/qa.properties']);
 }finally{await rm(root,{recursive:true,force:true});}
});
