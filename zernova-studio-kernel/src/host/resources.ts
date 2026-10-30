/* Copyright (c) 2026 ZerNova. Project-scoped resource authoring. */
import * as vscode from 'vscode';
import * as fs from 'node:fs/promises';
import path from 'node:path';
import { studioForm } from './forms';
import { saveAutomatically } from './saves';
import { ProjectManager, isInside, type Project } from './projects';
import { applyOperation, parseIntegration, processorTree, type RealTransform, type ComponentValues } from '../shared/xml';
import type { ResourceAction, TransformTarget } from '../shared/realProtocol';
import { walkFiles } from '../../../zernova-studio-build/src/portable/bundle';

async function directory(project:Project,relative:string):Promise<string>{
 let current=project.root;
 for(const part of relative.split('/')){
  current=path.join(current,part);const stat=await fs.lstat(current).catch(error=>{if(error.code==='ENOENT')return undefined;throw error;});
  if(stat&&(stat.isSymbolicLink()||!stat.isDirectory()))throw new Error('Resource directories must be ordinary project folders.');
  if(!stat)await fs.mkdir(current);
 }
 return current;
}
async function resource(project:Project,name:string):Promise<string>{
 if(!name||path.isAbsolute(name)||name.includes('\\')||name.split('/').some(p=>!p||p==='.'||p==='..'))throw new Error('Use a relative project resource path.');
 const base=path.join(project.root,'src/main/resources'),file=path.join(base,name),real=await fs.realpath(file);
 if(!isInside(base,real)||!isInside(await fs.realpath(project.root),real))throw new Error('Resource resolves outside the project.');
 if((await fs.lstat(file)).isSymbolicLink())throw new Error('Symlink resource files are not supported.');
 return file;
}
export class ResourceManager {
 constructor(private readonly projects:ProjectManager){}
 async action(uri:vscode.Uri,action:ResourceAction,id?:string,target?:TransformTarget):Promise<void>{
  if(!vscode.workspace.isTrusted)throw new Error('Workspace trust is required for resource authoring.');
  const project=this.projects.projectForUri(uri);if(!project)throw new Error('Open this integration through its ZERNOVA project.');
  const doc=await vscode.workspace.openTextDocument(uri),version=doc.version,before=doc.getText(),model=parseIntegration(before);
  const commit=async(text:string,added?:{file:string;text:string})=>{
   if(doc.version!==version||doc.getText()!==before)throw new Error('The XML changed while the dialog was open. Close and reopen to review it.');
   const change=new vscode.WorkspaceEdit();
   if(added){const file=vscode.Uri.file(added.file);change.createFile(file,{overwrite:false});change.insert(file,new vscode.Position(0,0),added.text);}
   change.replace(uri,new vscode.Range(doc.positionAt(0),doc.positionAt(before.length)),text);
   if(!await vscode.workspace.applyEdit(change))throw new Error('The resource/document edit could not be applied.');
   if(added&&!await saveAutomatically(await vscode.workspace.openTextDocument(vscode.Uri.file(added.file))))throw new Error('The resource could not be saved.');
   if(!await saveAutomatically(doc))throw new Error('The XML could not be saved.');
  };
  if(action==='configureScript'||action==='openScript'){
   if(!id||!target)throw new Error('Choose a Transform output.');
   const node=model.flows.flatMap(processorTree).find(p=>p.id===id);
   if(node?.kind!=='transform')throw new Error('Transform Message no longer exists.');
   const scriptKey=({payload:'script',attributes:'attributesScript',variables:'variablesScript'} as const)[target],resourceKey=({payload:'payloadResource',attributes:'attributesResource',variables:'variablesResource'} as const)[target];
   const linked=node[resourceKey]??'';
   if(action==='openScript'){if(!linked)throw new Error('This output has no linked file.');await vscode.commands.executeCommand('vscode.open',vscode.Uri.file(await resource(project,linked)));return;}
   const base=await directory(project,'src/main/resources/dwl'),files=(await walkFiles(base)).filter(n=>n.endsWith('.dwl')).map(n=>'dwl/'+n);
   const initial=linked?await fs.readFile(await resource(project,linked),'utf8'):node[scriptKey]??'';
   if(Buffer.byteLength(initial)>1024*1024)throw new Error('Open large scripts in the file editor; this dialog supports up to 1 MiB.');
   await studioForm('ZERNOVA Transform '+target+' source',[
    {key:'mode',label:'Script source',value:linked?'existing':'inline',options:[{value:'inline',label:'Inline script'},{value:'existing',label:'Use an existing external file'},{value:'new',label:'Save script as a new external file'}]},
    {key:'existing',label:'Existing DataWeave file',value:linked||files[0]||'',options:files.length?files.map(file=>({value:file,label:file})):[{value:'',label:'No DataWeave files yet'}]},
    {key:'filename',label:'New external filename',value:target+'-'+node.id.replace(/[^A-Za-z0-9_-]/g,'').slice(0,12)+'.dwl',hint:'Saved under src/main/resources/dwl. Existing files are never overwritten.'},
    {key:'script',label:'Inline / new file contents',value:initial,multiline:true,maxLength:1024*1024,hint:'Existing-file mode uses the selected file as-is. Use Open external file to edit it in the code editor.'},
   ],async fields=>{
    const values:ComponentValues={};let added:{file:string;text:string}|undefined;
    if(fields.mode==='inline'){if(Buffer.byteLength(fields.script)>16000)throw new Error('Use an external file for scripts over 16000 bytes.');values[scriptKey]=fields.script;values[resourceKey]='';}
    else if(fields.mode==='existing'){if(!files.includes(fields.existing))throw new Error('Choose an existing DataWeave file.');await resource(project,fields.existing);values[scriptKey]='';values[resourceKey]=fields.existing;}
    else if(fields.mode==='new'){
     if(!/^[A-Za-z_][A-Za-z0-9_.-]*\.dwl$/.test(fields.filename))throw new Error('Use a filename such as mapping.dwl.');
     const file=path.join(base,fields.filename);if(await fs.lstat(file).then(()=>true,error=>{if(error.code==='ENOENT')return false;throw error;}))throw new Error('That file already exists. Choose it in existing-file mode or use another name.');
     added={file,text:fields.script};values[scriptKey]='';values[resourceKey]='dwl/'+fields.filename;
    }else throw new Error('Choose a script source.');
    const result=applyOperation(before,{kind:'updateComponent',id,values});
    const transform=parseIntegration(result.text).flows.flatMap(processorTree).find(n=>n.id===(result.selectionId??id)) as RealTransform|undefined;
    if(transform&&![transform.script,transform.attributesScript,transform.variablesScript,transform.payloadResource,transform.attributesResource,transform.variablesResource].some(v=>v?.trim()))throw new Error('Keep at least one Transform output.');
    await commit(result.text,added);return true;
   },'Outputs evaluate against the incoming event. Resource files are read and cached when the application deploys. Manual file saves follow your redeploy-on-save setting.');
   return;
  }
  if(action==='addGlobalProperty'){
   const existing=model.globalProperties?.find(p=>p.id===id);
   await studioForm('ZERNOVA Global Property',[{key:'name',label:'Property name',value:existing?.name??''},{key:'value',label:'Default value',value:existing?.value??''}],async fields=>{
    if(existing&&fields.name!==existing.name)throw new Error('Create a new property to rename it; existing references are not rewritten blindly.');
    await commit(applyOperation(before,{kind:'saveGlobalProperty',name:fields.name,value:fields.value}).text);return true;
   },'Global defaults are overridden by application files, environment variables and Java/deployment properties.');return;
  }
  if(action==='addPropertyFile'){
   const existing=model.properties?.find(p=>p.id===id);
   await studioForm('ZERNOVA Configuration Properties',[{key:'file',label:'Resource path / environment pattern',value:existing?.file??'config/application-${zernova.env}.properties'}],async fields=>{await commit(applyOperation(before,{kind:'savePropertyFile',file:fields.file,...(existing?{id:existing.id}:{})}).text);return true;},'Paths are relative to src/main/resources. For example config/application-${zernova.env}.properties.');return;
  }
  if(action==='configureEnvironment'){
   const current=this.projects.getRunConfiguration(project),pattern=model.properties?.[0]?.file??'config/application-${zernova.env}.properties';
   await studioForm('ZERNOVA Environment & Properties',[
    {key:'environment',label:'Active environment',value:current.environment,hint:'Sets the XML global default and the Run/Debug profile environment. Individual launch profiles can override it afterward.'},
    {key:'pattern',label:'Environment property-file pattern',value:pattern},
    {key:'defaults',label:'Initial contents for a missing environment file',value:'http.host=localhost\nhttp.port=18081\napp.message=Hello from ZERNOVA\n',multiline:true,hint:'Existing files are left unchanged. Edit them from the project tree.'},
   ],async fields=>{
    if(!/^[A-Za-z0-9_.-]{1,80}$/.test(fields.environment))throw new Error('Use a valid environment name.');
    let text=applyOperation(before,{kind:'saveGlobalProperty',name:'zernova.env',value:fields.environment}).text;
    text=applyOperation(text,{kind:'savePropertyFile',file:fields.pattern,...(model.properties?.[0]?{id:model.properties[0].id}:{})}).text;
    const resolved=fields.pattern.replaceAll('${zernova.env}',fields.environment);
    if(!/^[A-Za-z0-9_./-]+\.properties$/.test(resolved)||resolved.split('/').some(v=>!v||v==='.'||v==='..'))throw new Error('Use an ordinary relative .properties pattern with ${zernova.env}.');
    const parent=path.posix.dirname(resolved),base=await directory(project,'src/main/resources'+(parent==='.'?'':'/'+parent));
    const file=path.join(base,path.posix.basename(resolved)),exists=await fs.lstat(file).then(()=>true,error=>{if(error.code==='ENOENT')return false;throw error;});
    if(exists)await resource(project,resolved);
    await commit(text,exists?undefined:{file,text:fields.defaults});
    await this.projects.setEnvironment(project,fields.environment);return true;
   },'XML placeholders use ${name}. DataWeave reads properties with p(\'name\'); $(p(\'name\')) interpolates the value inside a string.');
  }
 }
}
