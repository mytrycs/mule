import { spawnSync } from 'node:child_process';
import { readFile } from 'node:fs/promises';
const pkg = JSON.parse(await readFile('package.json', 'utf8'));
const target = process.env.ZN_TARGET || `${process.platform}-${process.arch}`;
if (!['darwin-x64','darwin-arm64','linux-x64','linux-arm64','win32-x64','win32-arm64'].includes(target)) throw new Error('Unsupported package target');
const bundle = JSON.parse(await readFile('runtime/bundle.json', 'utf8'));
if (bundle.target !== target) throw new Error('Runtime target differs from VSIX target');
const result = spawnSync(process.execPath, ['node_modules/@vscode/vsce/vsce', 'package', '--target', target, '--no-dependencies', '--allow-missing-repository', '--out', `dist/zernova-studio-kernel-preview-${pkg.version}-${target}.vsix`], { stdio: 'inherit' });
process.exit(result.status ?? 1);
