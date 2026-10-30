import test from 'node:test';
import assert from 'node:assert/strict';
import * as fs from 'node:fs/promises';
import path from 'node:path';
import os from 'node:os';
import net from 'node:net';
import { spawn } from 'node:child_process';
import { BundledRuntime } from '../../zernova-studio-build/src/portable/runtime';

test('a reset control socket reports a dead JVM and refuses an unauthenticated live PID', async () => {
  const root = await fs.realpath(await fs.mkdtemp(path.join(os.tmpdir(), 'zernova-control-')));
  const server = net.createServer(socket => socket.resetAndDestroy());
  await new Promise<void>(resolve => server.listen(0, '127.0.0.1', resolve));
  const port = (server.address() as net.AddressInfo).port;
  const runtime = new BundledRuntime({ bundle: path.join(root, 'bundle'), project: path.join(root, 'project'), instance: path.join(root, 'instance'), application: 'control-test' });
  const child = spawn(process.execPath, ['-e', 'process.exit(0)']);
  const deadPid = child.pid!;
  await new Promise<void>(resolve => child.once('exit', () => resolve()));
  try {
    await runtime.ownership();
    const control = path.join(root, 'instance/control'); await fs.mkdir(control);
    await fs.writeFile(path.join(control, 'auth.token'), 'a'.repeat(64));
    const state = async (pid: number) => {
      await fs.writeFile(path.join(root, 'instance/state.json'), JSON.stringify({ pid, run_id: 'owned-run', runtime_ready: true }));
      await fs.writeFile(path.join(control, 'endpoint.json'), JSON.stringify({ schemaVersion: 1, product: 'ZERNOVA Runtime Control', pid, runId: 'owned-run', port }));
    };
    await state(deadPid);
    assert.equal((await runtime.status()).process, 'STOPPED');
    await state(process.pid);
    await assert.rejects(runtime.status(), /authenticated control endpoint is unavailable/);
    assert.equal(process.kill(process.pid, 0), true);
  } finally {
    await new Promise<void>(resolve => server.close(() => resolve()));
    await fs.rm(root, { recursive: true, force: true });
  }
});
