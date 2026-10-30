import test from 'node:test';
import assert from 'node:assert/strict';
import { runProcess } from '../src/host/process';
import { assertRuntimeIdentity, parseRuntimeStatus } from '../src/shared/runtimeContract';

const identity = { schema_version: 2, application_format_version: 1, product: 'ZERNOVA Runtime', distribution_version: '0.4.0-alpha.1', kernel_version: '0.4.0-alpha.1', dataweave_version: '2.10.0', build_mode: 'source-built-development' };
const state = () => ({ ...identity, process: 'RUNNING', runtime: 'READY', healthy: true, work_directory: '/tmp/zernova-studio-test', applications: { owner: { name: 'owner', state: 'DEPLOYED', healthy: true, deployment: 'DEPLOYED', endpoint: 'NOT_CHECKED' } } });

test('observed runtime identity is pinned separately from the plugin build framework', () => {
  assert.doesNotThrow(() => assertRuntimeIdentity(identity));
  assert.throws(() => assertRuntimeIdentity({ ...identity, kernel_version: '4.13.0' }), /Unexpected/);
  assert.throws(() => assertRuntimeIdentity({ ...identity, schema_version: 1 }), /Unexpected/);
});
test('process running never upgrades failed app health', () => {
  const value = state(); value.healthy = false; value.applications.owner = { name: 'owner', state: 'UNHEALTHY', healthy: false, deployment: 'FAILED', endpoint: 'NOT_CHECKED' };
  const parsed = parseRuntimeStatus(value, value.work_directory);
  assert.equal(parsed.process, 'RUNNING'); assert.equal(parsed.applications.owner.healthy, false);
  assert.equal(parsed.applications.owner.deployment, 'FAILED');
});
test('deployment success without a health request preserves NOT_CHECKED endpoint', () => {
  const value = state(); const parsed = parseRuntimeStatus(value, value.work_directory);
  assert.equal(parsed.applications.owner.endpoint, 'NOT_CHECKED');
});
test('status for a different private directory is rejected', () => {
  assert.throws(() => parseRuntimeStatus(state(), '/tmp/someone-else'), /contract/);
});
test('malformed and contradictory status fields cannot produce healthy badges', () => {
  const value = state();
  assert.throws(() => parseRuntimeStatus({ ...value, healthy: 'true' }, value.work_directory), /contract/);
  assert.throws(() => parseRuntimeStatus({ ...value, process: 'STOPPED' }, value.work_directory), /Stopped/);
  value.applications.owner.state = 'UNHEALTHY';
  assert.throws(() => parseRuntimeStatus(value, value.work_directory), /Contradictory/);
});
test('process arguments remain literal data, without shell expansion', async () => {
  const arg = 'literal $(printf unsafe); `printf unsafe` && secret';
  const result = await runProcess({ executable: process.execPath, args: ['-e', 'process.stdout.write(process.argv[1])', arg], cwd: process.cwd(), env: { ...process.env }, timeoutMs: 5000 });
  assert.equal(result.exitCode, 0); assert.equal(result.stdout, arg);
});
test('process result retains real nonzero exit and stderr', async () => {
  const result = await runProcess({ executable: process.execPath, args: ['-e', 'process.stderr.write("dependency missing");process.exit(17)'], cwd: process.cwd(), env: { ...process.env }, timeoutMs: 5000 });
  assert.equal(result.exitCode, 17); assert.equal(result.stderr, 'dependency missing');
});
test('cancellation terminates the spawned process group within a bound', async () => {
  const controller = new AbortController();
  const result = await runProcess({ executable: process.execPath, args: ['-e', 'console.log("ready");setInterval(()=>{},1000)'], cwd: process.cwd(), env: { ...process.env }, timeoutMs: 10000, signal: controller.signal, onOutput: text => { if (text.includes('ready')) controller.abort(); } });
  assert.equal(result.cancelled, true); assert.equal(result.timedOut, false); assert.ok(result.milliseconds < 7000); assert.notEqual(result.exitCode, 0);
});
test('bounded timeout is distinguished from cancellation and success', async () => {
  const result = await runProcess({ executable: process.execPath, args: ['-e', 'setInterval(()=>{},1000)'], cwd: process.cwd(), env: { ...process.env }, timeoutMs: 200 });
  assert.equal(result.timedOut, true); assert.equal(result.cancelled, false); assert.ok(result.milliseconds < 7000); assert.notEqual(result.exitCode, 0);
});
