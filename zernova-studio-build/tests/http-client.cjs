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
