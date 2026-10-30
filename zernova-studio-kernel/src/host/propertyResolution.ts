/* Copyright (c) 2026 ZerNova. Java-properties-compatible local preflight. */
import * as fs from 'node:fs/promises';
import path from 'node:path';
import type { IntegrationModel } from '../shared/xml';
import type { RunConfiguration } from '../../../zernova-studio-build/src/portable/run-config';
import { launchEnvironment } from '../../../zernova-studio-build/src/portable/runtime';
import { within } from '../../../zernova-studio-build/src/portable/bundle';

export function parseProperties(text:string):Record<string,string>{
 const result:Record<string,string>=Object.create(null);
 const unescape=(s:string)=>{let result='';for(let i=0;i<s.length;i++){if(s[i]!=='\\'){result+=s[i];continue;}const c=s[++i];if(c==='u'){const digits=s.slice(i+1,i+5);if(!/^[0-9a-fA-F]{4}$/.test(digits))throw new Error('Malformed Unicode escape in property file.');result+=String.fromCharCode(parseInt(digits,16));i+=4;}else if(c!==undefined)result+=({t:'\t',n:'\n',r:'\r',f:'\f'}[c]??c);}return result;};
 let pending='';
 for(const physical of text.replace(/^\uFEFF/,'').split(/\r\n|\n|\r/)){
  let line=pending?pending+physical.replace(/^[ \t\f]+/,''):physical;pending='';
  const slashes=/\\+$/.exec(line)?.[0].length??0;if(slashes%2){pending=line.slice(0,-1);continue;}
  line=line.replace(/^[ \t\f]+/,'');if(!line||/^[#!]/.test(line))continue;
  let end=0,escaped=false;
  while(end<line.length){const c=line[end];if(!escaped&&/[=:\t\f ]/.test(c))break;if(c==='\\')escaped=!escaped;else escaped=false;end++;}
  let valueStart=end;while(/[ \t\f]/.test(line[valueStart]??'!'))valueStart++;if(line[valueStart]==='='||line[valueStart]===':')valueStart++;while(/[ \t\f]/.test(line[valueStart]??'!'))valueStart++;
  const key=unescape(line.slice(0,end)),value=unescape(line.slice(valueStart));result[key]=value;
 }
 if(pending)Object.assign(result,parseProperties(pending));
 return result;
}
export function expandProperties(value:string,lookup:(name:string)=>string|undefined,chain:string[]=[]):string{
 return value.replace(/\$\{([^{}]+)\}/g,(_,name:string)=>{
  if(chain.includes(name)||chain.length>=64)throw new Error('Cyclic configuration property: '+[...chain,name].join(' → '));
  const next=lookup(name);if(next===undefined)throw new Error('Unresolved configuration property: '+name);
  return expandProperties(next,lookup,[...chain,name]);
 });
}
export async function projectProperties(project:string,models:IntegrationModel[],run:RunConfiguration):Promise<{resolve:(value:string)=>string;files:string[]}>{
 const globals:Record<string,string>=Object.create(null),application:Record<string,string>=Object.create(null);
 for(const property of models.flatMap(m=>m.globalProperties??[])){
  if(Object.hasOwn(globals,property.name))throw new Error('Duplicate global property: '+property.name);globals[property.name]=property.value;
 }
 const environment=launchEnvironment(run),system={...run.properties,'zernova.env':run.environment};
 const rootLookup=(name:string)=>Object.hasOwn(system,name)?system[name as keyof typeof system]:environment[name]??globals[name];
 const canonicalProject=await fs.realpath(project),resources=await fs.realpath(path.join(canonicalProject,'src/main/resources')),files:string[]=[];if(!within(canonicalProject,resources))throw new Error('Resources resolve outside the project.');
 for(const item of models.flatMap(m=>m.properties??[])){
  const name=expandProperties(item.file,rootLookup);
  if(!/^[A-Za-z0-9_./-]+\.properties$/.test(name)||path.isAbsolute(name)||name.split('/').some(p=>!p||p==='..'||p==='.'))throw new Error('Property files must resolve to relative .properties resources.');
  const real=await fs.realpath(path.join(resources,name));if(!within(resources,real)||!within(canonicalProject,real))throw new Error('Property resource resolves outside the project.');
  const values=parseProperties(await fs.readFile(real,'utf8'));
  for(const [key,value] of Object.entries(values)){if(Object.hasOwn(application,key)&&application[key]!==value)throw new Error('Conflicting application-property files define '+key+'.');application[key]=value;}
  files.push(name);
 }
 const lookup=(name:string)=>Object.hasOwn(system,name)?system[name as keyof typeof system]:environment[name]??application[name]??globals[name];
 return {resolve:value=>expandProperties(value,lookup),files};
}
