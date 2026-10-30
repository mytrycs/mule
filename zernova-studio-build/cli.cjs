#!/usr/bin/env node
/* Copyright (c) 2026 ZerNova. Original runtime control tooling; see LICENSE.txt. */
const path = require('node:path');
const { BundledRuntime } = require('./dist/portable-runtime.cjs');

async function main() {
  const [command, ...tokens] = process.argv.slice(2);
  if (!command || command === '--help') {
    console.log('ZERNOVA Runtime: doctor | build | run | status | logs | undeploy | stop\n' +
      'Required: --project PATH --instance PATH --app NAME\n' +
      'Optional: --bundle PATH (default: bundled runtime) --port NUMBER (default: 18081)');
    return;
  }
  const options = {};
  for (let i = 0; i < tokens.length; i += 2) {
    if (!/^--(project|instance|app|bundle|port)$/.test(tokens[i]) || !tokens[i + 1] || tokens[i + 1].startsWith('--')) {
      throw new Error('Invalid option; use --help');
    }
    if (options[tokens[i]]) throw new Error('Duplicate option: ' + tokens[i]);
    options[tokens[i]] = tokens[i + 1];
  }
  for (const required of ['project', 'instance', 'app']) {
    if (!options['--' + required]) throw new Error('Missing --' + required);
  }
  const runtime = new BundledRuntime({
    bundle: options['--bundle'] || path.join(__dirname, 'runtime'),
    project: options['--project'], instance: options['--instance'], application: options['--app'],
    onOutput: text => process.stderr.write(text),
  });
  let result;
  switch (command) {
    case 'doctor': result = await runtime.doctor(); break;
    case 'build': result = await runtime.package(); break;
    case 'run': {
      const artifact = await runtime.package();
      await runtime.start(Number(options['--port'] || 18081));
      try { result = await runtime.deploy(artifact.artifact); }
      catch (error) { await runtime.stop(); throw error; }
      break;
    }
    case 'status': result = await runtime.status(); break;
    case 'logs': result = await runtime.logs(); break;
    case 'undeploy': result = await runtime.undeploy(); break;
    case 'stop': result = await runtime.stop(); break;
    default: throw new Error('Unknown command; use --help');
  }
  console.log(JSON.stringify(result ?? { success: true }, null, 2));
}
main().catch(error => { console.error(error.message); process.exitCode = 1; });
