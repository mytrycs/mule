/* Copyright (c) 2026 ZerNova. Original Studio tooling; see LICENSE.txt. */
import * as fs from 'node:fs/promises';
import { createReadStream } from 'node:fs';
import path from 'node:path';
import { createHash, randomUUID } from 'node:crypto';
import { spawn } from 'node:child_process';

export const STUDIO_VERSION = '0.4.0';
export const RUNTIME_VERSION = '0.4.0-alpha.1';
export const APPLICATION_FORMAT_VERSION = 1;
export const ENGINE_VERSION = '4.10.0-SNAPSHOT';
export const TARGETS = ['linux-x64', 'linux-arm64', 'darwin-x64', 'darwin-arm64', 'win32-x64', 'win32-arm64'] as const;
export interface BundleInfo {
  schemaVersion: 1; product: 'ZERNOVA Runtime'; version: string; engineVersion: string; applicationFormat: number;
  dataweaveVersion: string; sourceBuildCommit: string; target: string; javaHome: string;
  javaVendor: string; javaVersion: string; javaArchitecture: string; controlAgent: string;
  sourceArchive: string; distributionMode: 'source-built-development';
}
export interface ChecksumRecord { sha256: string; bytes: number; executable?: boolean; }
export interface ChecksumManifest { schemaVersion: 1; files: Record<string, ChecksumRecord>; }
export function within(root: string, target: string): boolean {
  const r = path.relative(root, target); return r === '' || (!path.isAbsolute(r) && r !== '..' && !r.startsWith('..' + path.sep));
}
export function safeRelative(name: string): boolean {
  if (!name || name.includes('\\') || name.includes(':') || /[\x00-\x1f]/.test(name) || name.startsWith('/')) return false;
  return name.split('/').every(p => p !== '' && p !== '.' && p !== '..' && !/[. ]$/.test(p) && !/^(con|prn|aux|nul|com[1-9]|lpt[1-9])(?:\.|$)/i.test(p));
}
export function cancelled(signal?: AbortSignal): void { if (signal?.aborted) throw new Error('Operation cancelled.'); }
export async function fileSha(filename: string): Promise<string> {
  const digest = createHash('sha256');
  for await (const data of createReadStream(filename)) digest.update(data);
  return digest.digest('hex');
}
export async function readJson<T>(filename: string): Promise<T> { return JSON.parse(await fs.readFile(filename, 'utf8')) as T; }
export async function writeJson(filename: string, value: unknown): Promise<void> {
  const tmp = filename + '.' + randomUUID() + '.tmp';
  await fs.writeFile(tmp, JSON.stringify(value, null, 2) + '\n', { mode: 0o600, flag: 'wx' });
  try { await fs.rename(tmp, filename); } finally { await fs.rm(tmp, { force: true }); }
}
export async function ordinaryDirectory(directory: string): Promise<void> {
  const info = await fs.lstat(directory);
  if (!info.isDirectory() || info.isSymbolicLink()) throw new Error('Expected an ordinary directory, not a symlink: ' + directory);
}
export async function walkFiles(root: string): Promise<string[]> {
  await ordinaryDirectory(root);
  const result: string[] = [];
  async function walk(dir: string): Promise<void> {
    for (const entry of (await fs.readdir(dir, { withFileTypes: true })).sort((a, b) => a.name.localeCompare(b.name))) {
      const absolute = path.join(dir, entry.name);
      if (entry.isSymbolicLink()) throw new Error('Symbolic links are not accepted in packaged runtime or application inputs: ' + absolute);
      if (entry.isDirectory()) await walk(absolute);
      else if (entry.isFile()) result.push(path.relative(root, absolute).split(path.sep).join('/'));
      else throw new Error('Special filesystem entry is not accepted: ' + absolute);
    }
  }
  await walk(root); return result;
}
export function targetForHost(platform = process.platform, arch = process.arch): string {
  const target = `${platform}-${arch}`;
  if (!(TARGETS as readonly string[]).includes(target)) throw new Error(`Bundled runtime execution does not support this host: ${target}. Install a matching desktop package; browser, 32-bit and other CPU targets are not execution targets.`);
  if (platform === 'linux' && process.platform === 'linux') {
    const report = process.report?.getReport() as { header?: { glibcVersionRuntime?: string } } | undefined;
    if (!report?.header?.glibcVersionRuntime) throw new Error('This Linux bundle requires glibc. Alpine/musl is not silently treated as a supported target.');
  }
  return target;
}
export async function bundleInfo(root: string, checkHost = true): Promise<BundleInfo> {
  const info = await readJson<BundleInfo>(path.join(root, 'bundle.json')).catch(() => { throw new Error('The bundled ZERNOVA Runtime is missing or damaged. Reinstall this platform’s full VSIX; no external runtime configuration is required.'); });
  if (info.schemaVersion !== 1 || info.product !== 'ZERNOVA Runtime' || info.version !== RUNTIME_VERSION || info.applicationFormat !== APPLICATION_FORMAT_VERSION || info.engineVersion !== ENGINE_VERSION || info.dataweaveVersion !== '2.10.0' || info.distributionMode !== 'source-built-development') throw new Error(`Unexpected bundled runtime identity. This Studio requires ZERNOVA Runtime ${RUNTIME_VERSION}, application format ${APPLICATION_FORMAT_VERSION}.`);
  for (const field of [info.javaHome, info.controlAgent, info.sourceArchive]) if (!safeRelative(field)) throw new Error('Unsafe packaged runtime path.');
  if (checkHost && info.target !== targetForHost()) throw new Error(`This VSIX contains ${info.target}, but the extension host is ${targetForHost()}. Install the matching platform VSIX in the host where the project executes.`);
  return info;
}
export async function verifyBundle(root: string, signal?: AbortSignal, prefixes?: string[]): Promise<{ files: number; bytes: number }> {
  const checksums = await readJson<ChecksumManifest>(path.join(root, 'checksums.json'));
  if (checksums.schemaVersion !== 1 || !checksums.files || typeof checksums.files !== 'object') throw new Error('Invalid bundled integrity manifest.');
  const names = Object.keys(checksums.files).filter(n => !prefixes || prefixes.some(p => n.startsWith(p)));
  if (!names.length) throw new Error('The requested bundled component has no integrity records.');
  let bytes = 0;
  for (const name of names) {
    cancelled(signal);
    const record = checksums.files[name];
    if (!safeRelative(name) || !/^[a-f0-9]{64}$/.test(record.sha256) || !Number.isSafeInteger(record.bytes) || record.bytes < 0) throw new Error('Invalid checksum record.');
    const filename = path.join(root, name), stat = await fs.lstat(filename);
    const canonical = await fs.realpath(filename);
    if (!stat.isFile() || stat.isSymbolicLink() || !within(await fs.realpath(root), canonical) || stat.size !== record.bytes || await fileSha(filename) !== record.sha256) throw new Error('Bundled file is missing, changed, or escaping its package: ' + name);
    bytes += stat.size;
  }
  // Reject additional executable/library inputs as well as missing/changed ones.
  for (const folder of ['engine', 'connectors/http', 'java', 'control', 'sources']) {
    if (prefixes && !prefixes.some(p => (folder + '/').startsWith(p) || p.startsWith(folder + '/'))) continue;
    const observed = await walkFiles(path.join(root, folder));
    const expected = new Set(Object.keys(checksums.files).filter(n => n.startsWith(folder + '/')));
    for (const file of observed) if (!expected.delete(folder + '/' + file)) throw new Error('Unexpected bundled file: ' + folder + '/' + file);
    if (expected.size) throw new Error('Bundled component is incomplete.');
  }
  return { files: names.length, bytes };
}
export async function javaExecutable(root: string, signal?: AbortSignal): Promise<{ executable: string; version: string }> {
  const info = await bundleInfo(root);
  const executable = path.join(root, info.javaHome, 'bin', process.platform === 'win32' ? 'java.exe' : 'java');
  // VSIX extraction on some hosts loses executable bits. Repair only this known,
  // already-hash-verified bundled executable; never chmod an arbitrary user path.
  const manifest = await readJson<ChecksumManifest>(path.join(root, 'checksums.json'));
  const record = manifest.files[path.relative(root, executable).split(path.sep).join('/')];
  if (!record || await fileSha(executable) !== record.sha256) throw new Error('Bundled Java integrity check failed.');
  if (process.platform !== 'win32') await fs.chmod(executable, 0o755);
  const version = await new Promise<string>((resolve, reject) => {
    cancelled(signal);
    const child = spawn(executable, ['-XshowSettings:properties', '-version'], { shell: false, windowsHide: true, env: cleanEnvironment(), stdio: ['ignore', 'pipe', 'pipe'] });
    let output = '';
    const abort = () => child.kill();
    const timer = setTimeout(() => child.kill(), 15000);
    signal?.addEventListener('abort', abort, { once: true });
    const done = () => { clearTimeout(timer); signal?.removeEventListener('abort', abort); };
    child.stdout.on('data', d => { output = (output + d).slice(-16000); });
    child.stderr.on('data', d => { output = (output + d).slice(-16000); });
    child.on('error', e => { done(); reject(new Error('Bundled Java could not start: ' + e.message)); });
    child.on('close', code => { done(); if (code === 0 && /(?:openjdk|java) version "17\./.test(output)) { const nativeArch = /os\.arch = (\S+)/.exec(output)?.[1]; const expected = info.javaArchitecture === 'arm64' ? ['aarch64', 'arm64'] : ['amd64', 'x86_64', 'x64']; if (!nativeArch || !expected.includes(nativeArch)) return reject(new Error('Bundled Java architecture does not match this platform: '+nativeArch)); resolve(output.split(/\r?\n/).filter(line => /os\.arch =|version "17\.|Runtime Environment|Server VM/.test(line)).join('\n').trim()); } else reject(new Error('Bundled Java 17 is unavailable or incompatible: ' + output)); });
  });
  cancelled(signal); return { executable, version };
}
export function cleanEnvironment(): NodeJS.ProcessEnv {
  const env = { ...process.env };
  for (const k of Object.keys(env)) if (/^(JAVA_TOOL_OPTIONS|JDK_JAVA_OPTIONS|_JAVA_OPTIONS|CLASSPATH|MULE_HOME|MULE_BASE|ZERNOVA_.*|LD_PRELOAD|LD_LIBRARY_PATH|DYLD_.*)$/i.test(k)) delete env[k];
  return env;
}
