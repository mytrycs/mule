/** Declared Maven dependencies and lossless edits; no Maven goals or downloads run here. */
import * as sax from 'sax';
export interface MavenDependency {id:string;groupId:string;artifactId:string;version:string;classifier:string;type:string;scope:string;start:number;end:number;resolved?:{groupId:string;artifactId:string;version:string};}
interface Node {name:string;local:string;uri:string;start:number;openEnd:number;closeStart:number;end:number;selfClosing:boolean;text:string;children:Node[];}
export interface PomDependencies {dependencies:MavenDependency[];properties:Record<string,string>;root:Node;container?:Node;}
export function parseDependencies(text:string):PomDependencies{
 if(Buffer.byteLength(text)>1024*1024)throw new Error('POM exceeds the 1 MiB editor limit.');
 const parser=sax.parser(true,{xmlns:true}),stack:Node[]=[];let root:Node|undefined;
 parser.ondoctype=()=>{throw new Error('POM DOCTYPE declarations are not supported.');};
 parser.onopentag=(tag:any)=>{const node:Node={name:tag.name,local:tag.local,uri:tag.uri,start:parser.startTagPosition-1,openEnd:parser.position,closeStart:parser.position,end:parser.position,selfClosing:tag.isSelfClosing,text:'',children:[]};if(stack.length)stack.at(-1)!.children.push(node);else{if(root)throw new Error('Use one Maven project root.');root=node;}stack.push(node);};
 parser.ontext=parser.oncdata=(value:string)=>{if(stack.length)stack.at(-1)!.text+=value;};
 parser.onclosetag=()=>{const node=stack.pop()!;node.end=parser.position;node.closeStart=node.selfClosing?node.openEnd-2:text.lastIndexOf('</',node.end);};
 parser.write(text).close();if(!root||root.local!=='project'||!['','http://maven.apache.org/POM/4.0.0'].includes(root.uri))throw new Error('Expected a Maven project POM.');
 const same=(n:Node,name:string)=>n.uri===root!.uri&&n.local===name;const containers=root.children.filter(n=>same(n,'dependencies'));if(containers.length>1)throw new Error('Duplicate direct dependencies sections in POM.');
 const properties:Record<string,string>=Object.create(null);for(const property of root.children.find(n=>same(n,'properties'))?.children??[])properties[property.local]=property.text.trim();
 for(const name of ['groupId','artifactId','version']){const value=root.children.find(n=>same(n,name))?.text.trim();if(value)properties['project.'+name]=value;}
 const expand=(value:string,seen:string[]=[]):string=>value.replace(/\$\{([^{}]+)\}/g,(_,key:string)=>{if(seen.includes(key)||seen.length>32||!Object.hasOwn(properties,key))throw new Error('Unresolved Maven property '+key);return expand(properties[key],[...seen,key]);});
 const dependencies=(containers[0]?.children??[]).filter(n=>same(n,'dependency')).map((node):MavenDependency=>{const value=(name:string,fallback='')=>node.children.find(n=>same(n,name))?.text.trim()??fallback;const d:MavenDependency={id:'dependency:'+node.start,groupId:value('groupId'),artifactId:value('artifactId'),version:value('version'),classifier:value('classifier'),type:value('type','jar'),scope:value('scope','compile'),start:node.start,end:node.end};try{d.resolved={groupId:expand(d.groupId),artifactId:expand(d.artifactId),version:expand(d.version)};}catch{/* Unresolved declarations remain visible and editable. */}return d;});
 return {dependencies,properties,root,container:containers[0]};
}
export function isBundledHttp(d:MavenDependency):boolean{return d.resolved?.groupId==='com.zernova.connectors'&&d.resolved.artifactId==='zernova-http-connector'&&d.resolved.version==='1.12.2'&&d.classifier==='zernova-plugin'&&d.type==='jar'&&['compile','runtime'].includes(d.scope);}
const escape=(s:string)=>s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/"/g,'&quot;');
export function addDependency(text:string,fields:{groupId:string;artifactId:string;version:string;classifier?:string;scope?:string;type?:string}):string{
 const pom=parseDependencies(text);for(const key of ['groupId','artifactId','version']as const)if(!fields[key]||!/^([A-Za-z0-9_.+-]+|\$\{[A-Za-z0-9_.-]+\})$/.test(fields[key]))throw new Error('Enter a valid Maven '+key+'.');
 if(fields.classifier&&!/^[A-Za-z0-9_.-]+$/.test(fields.classifier))throw new Error('Invalid classifier.');if(!['compile','runtime','test','provided'].includes(fields.scope??'compile'))throw new Error('Choose a Maven scope.');if(!['jar','pom'].includes(fields.type??'jar'))throw new Error('Choose jar or pom.');
 if(pom.dependencies.some(d=>d.groupId===fields.groupId&&d.artifactId===fields.artifactId&&d.classifier===(fields.classifier??'')))throw new Error('That dependency is already declared. Remove it before replacing its coordinates.');
 const prefix=pom.root.name.includes(':')?pom.root.name.split(':')[0]+':':'',tag=(name:string,value:string)=>`<${prefix}${name}>${escape(value)}</${prefix}${name}>`;const eol=text.includes('\r\n')?'\r\n':'\n';
 const entry=`    <${prefix}dependency>${tag('groupId',fields.groupId)}${tag('artifactId',fields.artifactId)}${tag('version',fields.version)}${fields.classifier?tag('classifier',fields.classifier):''}${fields.type&&fields.type!=='jar'?tag('type',fields.type):''}${fields.scope&&fields.scope!=='compile'?tag('scope',fields.scope):''}</${prefix}dependency>`;
 const container=pom.container;if(container?.selfClosing)return text.slice(0,container.openEnd-2)+'>'+eol+entry+eol+'  </'+container.name+'>'+text.slice(container.end);
 const at=container?.closeStart??pom.root.closeStart,insert=container?eol+entry+eol+'  ':`  <${prefix}dependencies>${eol}${entry}${eol}  </${prefix}dependencies>${eol}`;return text.slice(0,at)+insert+text.slice(at);
}
export function removeDependency(text:string,id:string):string{const node=parseDependencies(text).dependencies.find(d=>d.id===id);if(!node)throw new Error('Dependency changed. Reopen the dependency manager.');return text.slice(0,node.start)+text.slice(node.end);}
