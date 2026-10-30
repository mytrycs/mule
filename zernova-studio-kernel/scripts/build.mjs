import { build } from 'esbuild';
import { mkdir } from 'node:fs/promises';
await mkdir('dist', { recursive: true });
await mkdir('media', { recursive: true });
await build({ entryPoints: ['src/extension.ts'], bundle: true, platform: 'node', format: 'cjs', target: 'node20', external: ['vscode'], outfile: 'dist/extension.js', sourcemap: false, legalComments: 'eof' });
await build({ entryPoints: ['src/webview/main.ts'], bundle: true, platform: 'browser', format: 'iife', target: 'es2022', outfile: 'media/studio.js', sourcemap: false, legalComments: 'eof', minify: false });
console.log('Built new ZERNOVA Studio host and offline webview.');
