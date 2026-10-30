import * as vscode from 'vscode';
import * as fs from 'node:fs/promises';
import path from 'node:path';
import {ProjectManager,type Project,isInside} from './projects';
import type {StudioForm} from './forms';
import {saveAutomatically} from './saves';
import {parseDependencies,addDependency,removeDependency,isBundledHttp} from '../shared/projectArtifacts';
export interface ArtifactEntry {label:string;description:string;file?:string;tooltip?:string;children?:ArtifactEntry[];dependencyId?:string;}
export class DependencyManager {
 constructor(private readonly context:vscode.ExtensionContext,private readonly projects:ProjectManager){}
 async projectArtifacts(project:Project):Promise<ArtifactEntry[]>{
  const pom=path.join(project.root,'pom.xml'),parsed=parseDependencies(await fs.readFile(pom,'utf8'));
  return Promise.all(parsed.dependencies.map(async d=>{const supported=isBundledHttp(d);const profile=path.join(this.context.extensionPath,'runtime/connectors/http/repository');const all=supported?await this.repositoryArtifacts(profile):[];return {label:`${d.groupId}:${d.artifactId}:${d.version||'(managed version)'}`,description:supported?'Bundled · resolved':'Declared · unavailable in offline build',file:pom,dependencyId:d.id,tooltip:`${d.type}${d.classifier?' / '+d.classifier:''} · scope ${d.scope}\n${supported?'Provided by the bundled HTTP profile.':'The current offline builder cannot package this declaration. Open POM or remove it to restore a supported build.'}`,children:supported?all:undefined};}));
 }
 private async repositoryArtifacts(root:string):Promise<ArtifactEntry[]>{
  const output:ArtifactEntry[]=[];const visit=async(dir:string)=>{for(const item of await fs.readdir(dir,{withFileTypes:true})){if(item.isSymbolicLink())continue;const file=path.join(dir,item.name);if(item.isDirectory())await visit(file);else if(item.name.endsWith('.jar')){const parts=path.relative(root,file).split(path.sep),version=parts.at(-2)!,artifact=parts.at(-3)!;const group=parts.slice(0,-3).join('.');output.push({label:`${group}:${artifact.startsWith('mule-')?'zernova-'+artifact.slice(5):artifact}:${version}`,description:'Bundled JAR',file,tooltip:`Bundled Maven artifact\nPhysical file: ${file}\nOriginal artifact ID: ${artifact}`});}}};await visit(root);return output.sort((a,b)=>a.label.localeCompare(b.label));
 }
 async runtimeArtifacts(project:Project):Promise<ArtifactEntry[]>{
  const root=path.join(this.context.extensionPath,'runtime'),bundle=JSON.parse(await fs.readFile(path.join(root,'bundle.json'),'utf8'));const lib=path.join(root,'engine/lib/mule');const jars=(await fs.readdir(lib)).filter(n=>n.endsWith('.jar'));
  return [{label:'ZERNOVA Runtime '+bundle.version,description:bundle.target,file:path.join(root,'bundle.json')},{label:'Java '+bundle.javaVersion,description:bundle.javaVendor,file:path.join(root,bundle.javaHome,'release')},{label:'DataWeave '+bundle.dataweaveVersion,description:'Bundled language engine'},{label:'ZERNOVA Core Support 0.5.0',description:'Managed core module',tooltip:'Transform, Dynamic Evaluate, business events, Cache and Batch. Installed in the private runtime on launch.'},{label:'Runtime libraries',description:String(jars.length)+' JARs',children:jars.sort().map(name=>({label:name.replace(/^mule-/,'zernova-'),description:'Runtime JAR',file:path.join(lib,name),tooltip:'Physical runtime artifact: '+path.join(lib,name)}))}];
 }
 async inspect(entry:ArtifactEntry,form:StudioForm):Promise<void>{await form('Artifact Details',[{key:'artifact',label:'Artifact',value:entry.label},{key:'status',label:'Status',value:entry.description},{key:'path',label:'Physical file',value:entry.file??'Managed by the bundled runtime'}],async()=>true,entry.tooltip??'Artifact information. This form does not modify the artifact.');}
 async manage(project:Project,form:StudioForm,removeId?:string):Promise<void>{
  if(!vscode.workspace.isTrusted)throw new Error('Workspace trust is required to manage dependencies.');const uri=vscode.Uri.file(path.join(project.root,'pom.xml'));const document=await vscode.workspace.openTextDocument(uri),version=document.version,before=document.getText(),pom=parseDependencies(before);
  await form('Manage Maven Dependencies',[
   {key:'action',label:'Action',value:removeId?'remove':'add',options:[{value:'add',label:'Add declared dependency'},{value:'remove',label:'Remove declared dependency'}]},
   {key:'existing',label:'Dependency to remove',value:removeId??pom.dependencies[0]?.id??'',options:pom.dependencies.length?pom.dependencies.map(d=>({value:d.id,label:`${d.groupId}:${d.artifactId}:${d.version}`})):[{value:'',label:'No dependencies declared'}]},
   {key:'groupId',label:'Group ID',value:'com.zernova.connectors'},{key:'artifactId',label:'Artifact ID',value:'zernova-http-connector'},{key:'version',label:'Version',value:'1.12.2'},{key:'classifier',label:'Classifier',value:'zernova-plugin'},
   {key:'type',label:'Type',value:'jar',options:[{value:'jar',label:'JAR'},{value:'pom',label:'POM'}]},
   {key:'scope',label:'Scope',value:'compile',options:['compile','runtime','provided','test'].map(value=>({value,label:value}))},
  ],async fields=>{
   if(document.version!==version||document.getText()!==before)throw new Error('POM changed while this dialog was open. Reopen the dependency manager.');
   const text=fields.action==='add'?addDependency(before,fields as any):fields.action==='remove'?removeDependency(before,fields.existing):(()=>{throw new Error('Choose Add or Remove.');})();
   const metadataUri=vscode.Uri.file(path.join(project.root,'zernova-app.json'));const existing=await fs.lstat(metadataUri.fsPath).catch(error=>{if(error.code==='ENOENT')return undefined;throw error;});if(existing?.isSymbolicLink())throw new Error('Application metadata must be an ordinary project file.');
   const metadataDocument=existing?await vscode.workspace.openTextDocument(metadataUri):undefined;const metadataBefore=metadataDocument?.getText();const metadata=metadataBefore?JSON.parse(metadataBefore):{schemaVersion:1,name:project.artifactId,group:project.groupId,version:project.version};metadata.dependencies=parseDependencies(text).dependencies.some(isBundledHttp)?['http']:[];
   const edit=new vscode.WorkspaceEdit();edit.replace(uri,new vscode.Range(document.positionAt(0),document.positionAt(before.length)),text);if(metadataDocument)edit.replace(metadataUri,new vscode.Range(metadataDocument.positionAt(0),metadataDocument.positionAt(metadataBefore!.length)),JSON.stringify(metadata,null,2)+'\n');else{edit.createFile(metadataUri,{overwrite:false});edit.insert(metadataUri,new vscode.Position(0,0),JSON.stringify(metadata,null,2)+'\n');}
   if(document.version!==version||document.getText()!==before)throw new Error('POM changed; dependency edit cancelled.');if(!await vscode.workspace.applyEdit(edit))throw new Error('The dependency edit could not be applied.');if(!await saveAutomatically(document)||!await saveAutomatically(await vscode.workspace.openTextDocument(metadataUri)))throw new Error('Dependency files could not be saved.');this.projects.refresh();return true;
  },'Declared dependencies are saved in pom.xml. The current offline builder resolves core plus the bundled HTTP 1.12.2 profile. Other coordinates remain visibly unresolved and fail build validation. Inherited/profile-dependent Maven resolution and arbitrary connector downloads are not enabled. Removing a dependency does not rewrite its usages in application XML.');
 }
}
