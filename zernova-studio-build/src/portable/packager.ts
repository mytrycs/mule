/* Copyright (c) 2026 ZerNova. Original offline assembler; see LICENSE.txt.
 * No expression interpretation, connector replacement, Maven execution or network.
 */
import * as fs from 'node:fs/promises';
import path from 'node:path';
import { createHash, randomUUID } from 'node:crypto';
import JSZip from 'jszip';
import { parser } from 'sax';
import { RUNTIME_VERSION, verifyBundle, walkFiles, safeRelative, within, cancelled, readJson, fileSha, ordinaryDirectory } from './bundle';
const CORE = 'http://www.mulesoft.org/schema/mule/core';
const HTTP = 'http://www.mulesoft.org/schema/mule/http';
const PREFIX = 'META-INF/mule-artifact/';
const ID = /^[a-zA-Z0-9][a-zA-Z0-9_.-]{0,99}$/;
const MAX = 64 * 1024 * 1024;
export interface ApplicationMetadata { schemaVersion: 1; name: string; group: string; version: string; dependencies: string[]; configs?: string[]; }
export interface PackageResult { status: 'PACKAGED'; artifact: string; sha256: string; bytes: number; sourceHash: string; networkUsed: false; }
export function assertAppName(name: string): void { if (!ID.test(name) || name.includes('..') || !safeRelative(name)) throw new Error('Invalid application identifier.'); }
export async function applicationMetadata(project: string): Promise<ApplicationMetadata> {
  const filename = path.join(project, 'zernova-app.json');
  if ((await fs.lstat(filename)).isSymbolicLink()) throw new Error('Application metadata cannot be a symbolic link.');
  const data = await readJson<ApplicationMetadata>(filename);
  const allowed = ['schemaVersion', 'name', 'group', 'version', 'dependencies', 'configs'];
  if (!data || data.schemaVersion !== 1 || Object.keys(data).some(k => !allowed.includes(k))) throw new Error('Unsupported zernova-app.json schema or fields.');
  for (const key of ['name', 'group', 'version'] as const) { if (typeof data[key] !== 'string') throw new Error('Missing application ' + key); assertAppName(data[key]); }
  if (!Array.isArray(data.dependencies) || !(data.dependencies.length === 0 || (data.dependencies.length === 1 && data.dependencies[0] === 'http'))) throw new Error('This offline build supports core and the included HTTP profile only. It does not resolve additional connectors.');
  return data;
}
export async function sourceDirectory(project: string): Promise<string> {
  const roots = ['src/main/zernova', 'src/main/mule'];
  const present: string[] = [];
  for (const rel of roots) {
    const dir = path.join(project, rel);
    const info = await fs.lstat(dir).catch(e => { if (e.code === 'ENOENT') return undefined; throw e; });
    if (info) { await ordinaryDirectory(dir); present.push(dir); }
  }
  if (present.length === 1) return present[0];
  if (present.length === 2) {
    const nonempty = [];
    for (const dir of present) if ((await walkFiles(dir)).length) nonempty.push(dir);
    if (nonempty.length === 1) return nonempty[0];
  }
  throw new Error('Use one integration source directory: src/main/zernova (new applications) or the preserved compatibility path.');
}
export async function sourceHash(project: string): Promise<string> {
  const digest = createHash('sha256');
  const files = (await walkFiles(path.join(project, 'src'))).map(n => 'src/' + n);
  for (const file of ['zernova-app.json', 'pom.xml', 'mule-artifact.json']) if (await fs.stat(path.join(project, file)).then(() => true, () => false)) files.push(file);
  for (const file of files.sort()) { const data = await fs.readFile(path.join(project, file)); digest.update(file).update('\0').update(String(data.length)).update('\0').update(data); }
  return digest.digest('hex');
}
function verifyXml(text: string, filename: string, http: boolean): void {
  if (/<!DOCTYPE|<!ENTITY/i.test(text) || text.includes('\0')) throw new Error('DTD/entity declarations are not accepted: ' + filename);
  const p = parser(true, { xmlns: true }); let root = false;
  p.onopentag = tag => {
    if (!root) { root = true; if (tag.uri !== CORE || tag.local !== 'mule') throw new Error('The XML compatibility namespace must be preserved: ' + filename); }
    if (tag.uri === HTTP && !http) throw new Error('HTTP XML requires the included http dependency profile.');
  };
  p.write(text).close(); if (!root) throw new Error('No integration XML root: ' + filename);
}
export async function packageApplication(bundle: string, input: string, signal?: AbortSignal): Promise<PackageResult> {
  const project = await fs.realpath(input); cancelled(signal);
  const before = await sourceHash(project), meta = await applicationMetadata(project), source = await sourceDirectory(project);
  const javaDir = path.join(project, 'src/main/java');
  if (await fs.stat(javaDir).then(() => true, () => false)) if ((await walkFiles(javaDir)).length) throw new Error('This offline XML/resource packager does not compile custom Java. No Java source was silently ignored.');
  const resources = new Map<string, Buffer>(); let total = 0;
  for (const folder of [source, path.join(project, 'src/main/resources')]) {
    if (!await fs.stat(folder).then(() => true, () => false)) continue;
    if (!within(project, await fs.realpath(folder))) throw new Error('Source directory escapes this project.');
    for (const rel of await walkFiles(folder)) {
      cancelled(signal);
      if (!safeRelative(rel) || /^(META-INF|repository)\//i.test(rel) || /\.(jar|class|so|dll|dylib)$/i.test(rel) || resources.has(rel)) throw new Error('Reserved, executable or duplicate application resource: ' + rel);
      const data = await fs.readFile(path.join(folder, rel)); total += data.length;
      if (total > MAX) throw new Error('Application source exceeds 64 MiB.');
      resources.set(rel, data);
    }
  }
  const configs = meta.configs ?? (await walkFiles(source)).filter(n => n.endsWith('.xml'));
  if (!Array.isArray(configs) || !configs.length || configs.some(n => typeof n !== 'string' || !safeRelative(n) || !n.endsWith('.xml') || !resources.has(n)) || new Set(configs).size !== configs.length) throw new Error('Create an integration XML file and at least one flow before building. Configs must name distinct existing XML resources.');
  for (const config of configs) {
    const decoder = new TextDecoder('utf-8', { fatal: true });
    verifyXml(decoder.decode(resources.get(config)!), config, meta.dependencies.length > 0);
  }
  const model: Record<string, unknown> = { version: '1.2.0', artifactCoordinates: { groupId: meta.group, artifactId: meta.name, version: meta.version, type: 'jar', classifier: 'mule-application' }, dependencies: [], resources: [...resources.keys()].sort() };
  const zip = new JSZip();
  const add = (name: string, data: string | Buffer) => zip.file(name, data, { date: new Date('1980-01-01T00:00:00Z'), createFolders: false, unixPermissions: 0o100644 });
  for (const [name, data] of resources) add(name, data);
  if (meta.dependencies.length) {
    await verifyBundle(bundle, signal, ['connectors/http/']);
    const http = path.join(bundle, 'connectors/http');
    const template = await readJson<{ dependencies: unknown[] }>(path.join(http, 'classloader-template.json'));
    if (!Array.isArray(template.dependencies)) throw new Error('Bundled HTTP dependency metadata is invalid.');
    model.dependencies = template.dependencies;
    for (const name of await walkFiles(path.join(http, 'repository'))) { cancelled(signal); add('repository/' + name, await fs.readFile(path.join(http, 'repository', name))); }
  }
  const descriptor = { configs, secureProperties: [], redeploymentEnabled: false, name: `${meta.group}:${meta.name}:${meta.version}`, minMuleVersion: '4.10.0', requiredProduct: 'MULE', classLoaderModelLoaderDescriptor: { id: 'mule', attributes: { exportedPackages: [], exportedResources: [...resources.keys()].sort() } }, bundleDescriptorLoader: { id: 'mule', attributes: {} }, javaSpecificationVersions: ['17'] };
  const json = (data: unknown) => JSON.stringify(data, null, 2) + '\n';
  add(PREFIX + 'mule-artifact.json', json(descriptor)); add(PREFIX + 'classloader-model.json', json(model));
  add(`META-INF/maven/${meta.group}/${meta.name}/pom.xml`, `<?xml version="1.0" encoding="UTF-8"?>\n<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion><groupId>${meta.group}</groupId><artifactId>${meta.name}</artifactId><version>${meta.version}</version><packaging>mule-application</packaging></project>\n`);
  add(`META-INF/maven/${meta.group}/${meta.name}/pom.properties`, `groupId=${meta.group}\nartifactId=${meta.name}\nversion=${meta.version}\n`);
  add('META-INF/zernova/package.json', json({ packager: 'ZERNOVA Studio offline XML/resource assembler', version: RUNTIME_VERSION, runtimeValidation: 'DEFERRED_TO_ENGINE', expressionEvaluation: false, sourceFiles: Object.fromEntries([...resources].map(([n, b]) => [n, createHash('sha256').update(b).digest('hex')])) }));
  const target = path.join(project, 'target');
  await fs.mkdir(target, { recursive: true }); await ordinaryDirectory(target);
  if (!within(project, await fs.realpath(target))) throw new Error('Build output escapes the project.');
  const artifact = path.join(target, `${meta.name}-${meta.version}.zapp`), tmp = artifact + '.' + randomUUID() + '.tmp';
  const buffer = await zip.generateAsync({ type: 'nodebuffer', platform: 'UNIX', compression: 'DEFLATE', compressionOptions: { level: 6 }, streamFiles: true }, () => cancelled(signal));
  cancelled(signal);
  if (await sourceHash(project) !== before) throw new Error('Source changed during packaging. No artifact was replaced or deployed.');
  await fs.writeFile(tmp, buffer, { flag: 'wx' });
  try {
    const info = await fs.lstat(artifact).catch(() => undefined);
    if (info?.isSymbolicLink()) throw new Error('Application output cannot be a symlink.');
    await fs.rename(tmp, artifact);
  } finally { await fs.rm(tmp, { force: true }); }
  return { status: 'PACKAGED', artifact, sha256: await fileSha(artifact), bytes: buffer.length, sourceHash: before, networkUsed: false };
}
export async function validateArtifact(filename: string): Promise<{ sha256: string; bytes: number; name: string }> {
  const stat = await fs.lstat(filename);
  if (!stat.isFile() || stat.isSymbolicLink() || stat.size > 256 * 1024 * 1024) throw new Error('Invalid or oversized deployable application.');
  const data = await fs.readFile(filename);
  const zip = await JSZip.loadAsync(data, { checkCRC32: false });
  if (Object.keys(zip.files).length > 20000) throw new Error('Application archive has too many entries.');
  let expanded = 0;
  for (const entry of Object.values(zip.files)) {
    const raw = (entry as unknown as { unsafeOriginalName?: string }).unsafeOriginalName ?? entry.name;
    if (entry.dir) continue;
    if (!safeRelative(raw) || ((Number(entry.unixPermissions) & 0o170000) === 0o120000)) throw new Error('Unsafe entry in application archive.');
    const size = (entry as unknown as { _data?: { uncompressedSize?: number } })._data?.uncompressedSize ?? 0;
    expanded += size;
    if (expanded > 512 * 1024 * 1024) throw new Error('Expanded application exceeds the deployment limit.');
  }
  // Check decompression bounds before asking JSZip to verify every CRC.
  await JSZip.loadAsync(data, { checkCRC32: true });
  const descriptorEntry = zip.file(PREFIX + 'mule-artifact.json'), modelEntry = zip.file(PREFIX + 'classloader-model.json');
  if (!descriptorEntry || !modelEntry) throw new Error('Application deployment metadata is missing.');
  const descriptor = JSON.parse(await descriptorEntry.async('string'));
  const model = JSON.parse(await modelEntry.async('string'));
  if (descriptor.requiredProduct !== 'MULE' || !descriptor.javaSpecificationVersions?.includes('17') || !Array.isArray(descriptor.configs) || !descriptor.configs.length) throw new Error('Application does not declare the required Java 17/kernel compatibility.');
  if (typeof model.artifactCoordinates?.artifactId !== 'string') throw new Error('Application coordinates are missing.');
  assertAppName(model.artifactCoordinates.artifactId);
  for (const config of descriptor.configs) if (typeof config !== 'string' || !safeRelative(config) || !zip.file(config)) throw new Error('Application config is missing or unsafe.');
  return { sha256: createHash('sha256').update(data).digest('hex'), bytes: data.length, name: model.artifactCoordinates.artifactId };
}
