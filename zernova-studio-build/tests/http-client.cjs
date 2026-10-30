/* Copyright (c) 2026 ZerNova. Original acceptance tooling; see ../LICENSE.txt. */
const http = require('node:http');

// A request after redeployment must reach the new server, not a pooled channel
// belonging to the application that was just undeployed. Never retry a POST or
// hide a failed response; use one fresh TCP connection for each assertion.
exports.freshFetch = (url, options = {}) => new Promise((resolve, reject) => {
  const headers = { ...options.headers, Connection: 'close' };
  if (options.body !== undefined) headers['Content-Length'] = Buffer.byteLength(options.body);
  const request = http.request(url, { method: options.method || 'GET', headers, agent: false, signal: options.signal }, response => {
    const chunks = []; let size = 0;
    response.on('data', chunk => {
      size += chunk.length;
      if (size > 2 * 1024 * 1024) request.destroy(new Error('Acceptance response exceeds 2 MiB'));
      else chunks.push(chunk);
    });
    response.on('error', reject);
    response.on('end', () => {
      const body = Buffer.concat(chunks).toString('utf8');
      resolve({ status: response.statusCode, text: async () => body });
    });
  });
  request.setTimeout(10000, () => request.destroy(new Error('HTTP acceptance request timed out')));
  request.on('error', reject);
  if (options.body !== undefined) request.write(options.body);
  request.end();
});

// The deployment banner can precede HTTP listener activation. Probe separately
// with GET before the functional assertion; never retry the asserted request or
// a POST. Only connection refusal and startup 503 are transient here.
exports.waitForHttpReady = async url => {
  const deadline = Date.now() + 15000;
  let last = 'not contacted';
  while (Date.now() < deadline) {
    try {
      const response = await exports.freshFetch(url, { signal: AbortSignal.timeout(2000) });
      last = `HTTP ${response.status}`;
      if (response.status !== 503) return;
    } catch (error) {
      if (error.code !== 'ECONNREFUSED') throw error;
      last = error.code;
    }
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  throw new Error(`HTTP listener did not become ready within 15 seconds: ${last}`);
};
