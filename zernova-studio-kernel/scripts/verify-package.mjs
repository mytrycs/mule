/* Copyright (c) 2026 ZerNova. See LICENSE.txt. */
import { build } from 'esbuild';
import { createRequire } from 'node:module';
import path from 'node:path';
import { access } from 'node:fs/promises';
await build({ entryPoints: ['../zernova-studio-build/src/portable/bundle.ts'], bundle: true, platform: 'node', format: 'cjs', outfile: 'dist/package-bundle.cjs' });
const { bundleInfo, verifyBundle } = createRequire(import.meta.url)('../dist/package-bundle.cjs');
const root = path.resolve('runtime');
const info = await bundleInfo(root, false);
const integrity = await verifyBundle(root);
for (const name of [info.sourceArchive, info.controlAgent, 'notices/ZERNOVA_ORIGINAL_COMPONENTS.txt', 'connectors/http/classloader-template.json', `${info.javaHome}/bin/${info.target.startsWith('win32') ? 'java.exe' : 'java'}`]) await access(path.join(root, name));
console.log(JSON.stringify({ packagedRuntime: info.version, target: info.target, ...integrity }));
