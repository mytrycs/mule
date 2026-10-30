import test from 'node:test';
import assert from 'node:assert/strict';
import {parseDependencies,addDependency,removeDependency,isBundledHttp} from '../src/shared/projectArtifacts';
const pom=`<?xml version="1.0"?>\n<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion><groupId>com.zernova.example</groupId><artifactId>test</artifactId><version>1.0.0</version><!--preserve--><properties><http.version>1.12.2</http.version></properties><dependencyManagement><dependencies><dependency><groupId>managed.example</groupId><artifactId>bom</artifactId><version>1</version></dependency></dependencies></dependencyManagement><build><plugins><!--keep plugins--></plugins></build></project>`;
test('dependency edits preserve unrelated POM source and distinguish direct from managed dependencies',()=>{
 assert.equal(parseDependencies(pom).dependencies.length,0);const added=addDependency(pom,{groupId:'com.zernova.connectors',artifactId:'zernova-http-connector',version:'${http.version}',classifier:'zernova-plugin'});const parsed=parseDependencies(added);assert.equal(parsed.dependencies.length,1);assert(isBundledHttp(parsed.dependencies[0]));assert.equal(parsed.dependencies[0].resolved?.version,'1.12.2');assert(added.includes('<!--keep plugins-->'));assert(added.includes('<!--preserve-->'));
 const removed=removeDependency(added,parsed.dependencies[0].id);assert.equal(parseDependencies(removed).dependencies.length,0);assert(removed.includes('<dependencyManagement>'));assert(removed.includes('<build><plugins><!--keep plugins--></plugins></build>'));
 assert.throws(()=>addDependency(added,{groupId:'com.zernova.connectors',artifactId:'zernova-http-connector',version:'1.12.2',classifier:'zernova-plugin'}),/already declared/);
});
test('prefixed/self-closing dependency containers are edited safely and unsupported declarations stay visible',()=>{
 const xml='<m:project xmlns:m="http://maven.apache.org/POM/4.0.0"><m:dependencies /></m:project>';const added=addDependency(xml,{groupId:'example.library',artifactId:'example',version:'1.0',scope:'test'});const parsed=parseDependencies(added);assert.equal(parsed.dependencies[0].artifactId,'example');assert.equal(parsed.dependencies[0].scope,'test');assert(!isBundledHttp(parsed.dependencies[0]));const removed=removeDependency(added,parsed.dependencies[0].id);assert.equal(parseDependencies(removed).dependencies.length,0);assert(removed.includes('</m:dependencies>'));
 assert.throws(()=>parseDependencies('<!DOCTYPE project SYSTEM "file:///tmp/private"><project/>'),/DOCTYPE/);
});
