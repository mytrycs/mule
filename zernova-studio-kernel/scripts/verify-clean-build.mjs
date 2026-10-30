/** Rebuild the separate source export in a fresh directory, without old projects. */
import { mkdtemp, readdir, mkdir, copyFile, readFile, writeFile } from 'node:fs/promises';
import { spawnSync } from 'node:child_process';
import { join, resolve, relative } from 'node:path';
import { tmpdir } from 'node:os';
import { createHash } from 'node:crypto';

const source = process.cwd();
const manifest = JSON.parse(await readFile(join(source, 'package.json'), 'utf8'));
const vsixName = `${manifest.name}-${manifest.version}.vsix`;
const extractionRoot = await mkdtemp(join(tmpdir(), 'zernova-studio-clean-build-'));
let destination = extractionRoot;
const excluded = new Set(['node_modules', 'dist', '.cache', '.git', 'evidence', 'screenshots', 'references']);
async function copyTree(from, to, depth = 0) {
  await mkdir(to, { recursive: true });
  for (const entry of await readdir(from, { withFileTypes: true })) {
    if (depth === 0 && excluded.has(entry.name)) continue;
    const file = join(from, entry.name);
    const rel = relative(source, file).replaceAll('\\', '/');
    if (rel === 'media/studio.js' || rel === 'media/studio.css') continue;
    if (entry.isDirectory()) await copyTree(file, join(to, entry.name), depth + 1);
    else if (entry.isFile()) await copyFile(file, join(to, entry.name));
    else throw new Error(`Unexpected source symlink/special file: ${rel}`);
  }
}
if (process.env.ZERNOVA_SOURCE_ARCHIVE) {
  const extraction = spawnSync('python3', ['-c', `from pathlib import Path
import sys,zipfile
root=Path(sys.argv[2]).resolve()
with zipfile.ZipFile(sys.argv[1]) as archive:
 for entry in archive.infolist():
  target=(root/entry.filename).resolve()
  if not target.is_relative_to(root): raise RuntimeError('Unsafe archive path')
 archive.extractall(root)
`, resolve(process.env.ZERNOVA_SOURCE_ARCHIVE), extractionRoot], { encoding: 'utf8' });
  if (extraction.status !== 0) throw new Error('Source archive extraction failed: ' + extraction.stderr);
  destination = join(extractionRoot, 'zernova-studio-kernel');
} else await copyTree(source, destination);
const results = [];
const install = ['ci', '--no-audit', '--no-fund'];
if (process.env.ZERNOVA_NPM_CACHE) install.push('--cache', resolve(process.env.ZERNOVA_NPM_CACHE), '--offline');
const commands = [install, ['run', 'typecheck'], ['test'], ['run', 'build'], ['run', 'package']];
await mkdir('evidence/clean-build', { recursive: true });
for (const [index, args] of commands.entries()) {
  const result = spawnSync(process.platform === 'win32' ? 'npm.cmd' : 'npm', args, { cwd: destination, encoding: 'utf8', timeout: 240000 });
  const log = `evidence/clean-build/${index + 1}-${args[0]}.log`;
  await writeFile(log, `${result.stdout ?? ''}\n${result.stderr ?? ''}`);
  results.push({ command: ['npm', ...args], exitCode: result.status, log, error: result.error?.message });
  if (result.status !== 0) {
    await writeFile('evidence/clean-build/results.json', JSON.stringify({ status: 'FAIL', destination, results }, null, 2));
    throw new Error(`Clean-source command failed: npm ${args.join(' ')}; see ${log}`);
  }
}
const hash = async file => createHash('sha256').update(await readFile(file)).digest('hex');
const bundles = [];
for (const file of ['dist/extension.js', 'media/studio.js', 'media/studio.css']) {
  const original = await hash(join(source, file));
  const rebuilt = await hash(join(destination, file));
  bundles.push({ file, original, rebuilt, match: original === rebuilt });
}
const status = bundles.every(bundle => bundle.match) ? 'PASS' : 'FAIL';
const evidence = { status, completedAt: new Date().toISOString(), node: process.version, platform: process.platform, architecture: process.arch, destination, copiedSourceOnly: true, sourceMode: process.env.ZERNOVA_SOURCE_ARCHIVE ? 'Fresh extraction of delivered source archive' : 'Fresh source-only directory copy', sourceArchive: process.env.ZERNOVA_SOURCE_ARCHIVE || null, sourceArchiveSha256: process.env.ZERNOVA_SOURCE_ARCHIVE ? await hash(resolve(process.env.ZERNOVA_SOURCE_ARCHIVE)) : null, priorProjectsAccessibleAsDependencies: false, cleanDependencyInstallation: true, registryTransport: process.env.ZERNOVA_NPM_CACHE ? 'offline pinned local npm cache' : 'registry', results, bundles, vsix: join(destination, 'dist', vsixName), vsixSha256: await hash(join(destination, 'dist', vsixName)) };
await writeFile('evidence/clean-build/results.json', JSON.stringify(evidence, null, 2));
console.log(JSON.stringify(evidence, null, 2));
if (status !== 'PASS') process.exitCode = 1;
