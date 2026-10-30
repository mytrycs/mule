import { build } from 'esbuild';
import { mkdir, readdir } from 'node:fs/promises';
import { spawnSync } from 'node:child_process';
await mkdir('dist/tests', { recursive: true });
const files = (await readdir('tests')).filter(name => name.endsWith('.test.ts')).sort();
if (!files.length) throw new Error('No unit test files found.');
for (const file of files) {
  const out = `dist/tests/${file.replace(/\.ts$/, '.cjs')}`;
  await build({ entryPoints: [`tests/${file}`], outfile: out, bundle: true, format: 'cjs', platform: 'node', target: 'node22', external: ['vscode'] });
  const result = spawnSync(process.execPath, [out], { stdio: 'inherit' });
  if (result.status !== 0) process.exit(result.status ?? 1);
}
