/* Copyright (c) 2026 ZerNova. Original build tooling. */
const path = require('node:path');
for (const name of ['runtime', 'packager']) {
  require('esbuild').buildSync({entryPoints:[path.join(__dirname, 'src/portable/' + name + '.ts')],bundle:true,platform:'node',format:'cjs',target:'node20',outfile:path.join(__dirname, 'dist/portable-' + name + '.cjs')});
}
