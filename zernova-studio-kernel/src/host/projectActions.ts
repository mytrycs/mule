import * as vscode from 'vscode';
import * as fs from 'node:fs/promises';
import path from 'node:path';
import {ProjectManager,type Project,isInside} from './projects';
import type {StudioForm} from './forms';
import type {RealItem} from './navigation';
import {DependencyManager} from './dependencies';
import {RuntimeManager} from './runtime';
export class ProjectActions {
 constructor(private readonly projects:ProjectManager,private readonly runtime:RuntimeManager,private readonly dependencies:DependencyManager,private readonly form:(project:Project)=>StudioForm){}
 private async target(candidate:unknown):Promise<{uri:vscode.Uri;project:Project;directory:boolean}>{
  const item=candidate as RealItem|undefined;const uri=candidate instanceof vscode.Uri?candidate:item?.uri??(this.projects.current()?vscode.Uri.file(this.projects.current()!.root):undefined);const project=uri?this.projects.projectForUri(uri):undefined;
  if(!uri||!project||uri.scheme!=='file')throw new Error('Select a file or folder in an opened ZERNOVA project.');
  const real=await fs.realpath(uri.fsPath);if(!isInside(project.root,real))throw new Error('The selected path resolves outside the project.');const stat=await fs.lstat(uri.fsPath);if(stat.isSymbolicLink())throw new Error('Use the actual project file, not a symbolic link.');return {uri,project,directory:stat.isDirectory()};
 }
 async execute(action:string,candidate:unknown):Promise<unknown>{
  const item=candidate as RealItem|undefined;
  if(action==='inspectArtifact'){if(!item?.artifact||!item.project)throw new Error('Select an artifact.');return this.dependencies.inspect(item.artifact,this.form(item.project));}
  if(action==='revealArtifact'){if(!item?.artifact?.file)throw new Error('This managed artifact has no separate file yet.');await fs.access(item.artifact.file);return vscode.commands.executeCommand('revealFileInOS',vscode.Uri.file(item.artifact.file));}
  const {uri,project,directory}=await this.target(candidate);const form=this.form(project);
  if(action==='openFile')return vscode.commands.executeCommand('vscode.open',uri,{preview:false});
  if(action==='openText')return vscode.commands.executeCommand('vscode.openWith',uri,'default',{preview:false});
  if(action==='openToSide')return vscode.commands.executeCommand('vscode.open',uri,{viewColumn:vscode.ViewColumn.Beside,preview:false});
  if(action==='openPom')return vscode.commands.executeCommand('vscode.openWith',vscode.Uri.file(path.join(project.root,'pom.xml')),'default',{preview:false});
  if(action==='copyPath')return vscode.env.clipboard.writeText(uri.fsPath);
  if(action==='copyRelativePath')return vscode.env.clipboard.writeText(path.relative(project.root,uri.fsPath).split(path.sep).join('/')||'.');
  if(action==='revealFile')return vscode.commands.executeCommand('revealFileInOS',uri);
  if(action==='revealExplorer')return vscode.commands.executeCommand('revealInExplorer',uri);
  if(action==='findInFolder')return vscode.commands.executeCommand('workbench.action.findInFiles',{filesToInclude:(directory?uri.fsPath:path.dirname(uri.fsPath)).replace(/\\/g,'/')});
  if(action==='refreshProject'){this.projects.refresh();return;}
  if(action==='manageDependencies'||action==='removeDependency')return this.dependencies.manage(project,form,action==='removeDependency'?item?.artifact?.dependencyId:undefined);
  if(!vscode.workspace.isTrusted)throw new Error('Workspace trust is required to modify project files.');
  if(action==='cleanProject'){
   if(this.runtime.state(project).busy)throw new Error('Wait for the current build/runtime operation to finish.');const target=path.join(project.root,'target'),stat=await fs.lstat(target).catch(e=>{if(e.code==='ENOENT')return undefined;throw e;});if(!stat)return;if(stat.isSymbolicLink()||!stat.isDirectory())throw new Error('Build output must be an ordinary target directory.');await vscode.workspace.fs.delete(vscode.Uri.file(target),{recursive:true,useTrash:true});this.runtime.buildCleaned(project);this.projects.refresh();return;
  }
  if(action==='removeProject'){
   await this.runtime.reconcile(project);if(this.runtime.state(project).busy||this.runtime.state(project).process==='RUNNING')throw new Error('Stop the project runtime before removing this project from Studio.');
   return this.projects.remove(project);
  }
  if(action==='newFile'||action==='newFolder'){
   const parent=directory?uri.fsPath:path.dirname(uri.fsPath);return form(action==='newFile'?'New Project File':'New Project Folder',[{key:'name',label:action==='newFile'?'Filename':'Folder name',value:action==='newFile'?'application.properties':'folder'}],async fields=>{
    const name=this.name(fields.name),destination=vscode.Uri.file(path.join(parent,name));if(await fs.lstat(destination.fsPath).then(()=>true,e=>{if(e.code==='ENOENT')return false;throw e;}))throw new Error('That name already exists.');
    const canonical=await fs.realpath(parent);if(!isInside(project.root,canonical))throw new Error('Destination resolves outside the project.');if(action==='newFolder')await fs.mkdir(destination.fsPath);else{const edit=new vscode.WorkspaceEdit();edit.createFile(destination,{overwrite:false});if(!await vscode.workspace.applyEdit(edit))throw new Error('File creation failed.');await vscode.commands.executeCommand('vscode.open',destination,{preview:false});}this.projects.refresh();return destination;
   });
  }
  if(uri.fsPath===project.root)throw new Error('Use project-level actions on the project root.');
  if(action==='renameFile'||action==='moveFile')return form(action==='renameFile'?'Rename Project Resource':'Move Project Resource',[{key:'name',label:action==='renameFile'?'New name':'Destination relative to the project',value:action==='renameFile'?path.basename(uri.fsPath):path.relative(project.root,uri.fsPath).split(path.sep).join('/')}],async fields=>{
   const destination=action==='renameFile'?path.join(path.dirname(uri.fsPath),this.name(fields.name)):path.resolve(project.root,fields.name);if(!isInside(project.root,destination)||destination===project.root)throw new Error('Choose a destination inside the project.');if(destination===uri.fsPath)return;
   if(directory&&isInside(uri.fsPath,destination))throw new Error('A folder cannot be moved inside itself.');const canonical=await fs.realpath(path.dirname(destination));if(!isInside(project.root,canonical))throw new Error('Destination resolves outside the project.');
   const change=new vscode.WorkspaceEdit();change.renameFile(uri,vscode.Uri.file(destination),{overwrite:false});if(!await vscode.workspace.applyEdit(change))throw new Error('Rename/move failed. The destination may exist.');this.projects.refresh();return true;
  },'Updates the project resource path. Review XML/DataWeave/resource references to this name before the next build; semantic references are not automatically rewritten.');
  if(action==='deleteFile')return form('Move to Trash',[],async()=>{await vscode.workspace.fs.delete(uri,{recursive:directory,useTrash:true});this.projects.refresh();return true;},`Move ${path.relative(project.root,uri.fsPath)}${directory?' and its contents':''} to the operating system Trash? Review references before rebuilding.`);
  throw new Error('Unsupported project action: '+action);
 }
 private name(value:string):string{if(!value.trim()||value!==value.trim()||/[\\/\0\r\n]/.test(value)||['.','..','.git'].includes(value)||value.length>120)throw new Error('Use one valid filename or folder name without directory segments.');return value;}
}
export const PROJECT_ACTIONS=['cleanProject','inspectArtifact','revealArtifact','openFile','openText','openToSide','openPom','copyPath','copyRelativePath','revealFile','revealExplorer','findInFolder','refreshProject','manageDependencies','removeDependency','removeProject','newFile','newFolder','renameFile','moveFile','deleteFile']as const;
