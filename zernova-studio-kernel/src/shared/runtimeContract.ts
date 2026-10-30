/** Bundled source-built runtime contract; never accepted as webview input. */
export interface RuntimeStatus {
  process: 'RUNNING' | 'STOPPED'; runtime: 'READY' | 'STARTING' | 'STOPPED'; pid?: number; run_id?: string;
  work_directory: string; healthy: boolean;
  applications: Record<string, { name?: string; state: string; healthy: boolean; deployment?: string; endpoint?: string }>;
}
const record = (value: unknown): value is Record<string, unknown> => !!value && typeof value === 'object' && !Array.isArray(value);
export function assertRuntimeIdentity(value: unknown): asserts value is Record<string, unknown> {
  if (!record(value) || value.schema_version !== 2 || value.application_format_version !== 1 || value.product !== 'ZERNOVA Runtime' || value.distribution_version !== '0.4.0-alpha.1' || value.kernel_version !== '0.4.0-alpha.1' || value.dataweave_version !== '2.10.0' || value.build_mode !== 'source-built-development') {
    throw new Error('Unexpected bundled runtime identity. Studio requires ZERNOVA 0.4.0-alpha.1 / the source-built 4.10 baseline / DataWeave 2.10.0.');
  }
}
export function parseRuntimeStatus(value: unknown, expectedDirectory: string): RuntimeStatus {
  assertRuntimeIdentity(value);
  if (!['RUNNING', 'STOPPED'].includes(String(value.process)) || !['READY', 'STARTING', 'STOPPED'].includes(String(value.runtime)) || value.work_directory !== expectedDirectory || typeof value.healthy !== 'boolean' || !record(value.applications)) throw new Error('Runtime status does not match the documented instance/status contract.');
  for (const [name, app] of Object.entries(value.applications)) {
    if (!/^[A-Za-z0-9][A-Za-z0-9_.-]{0,99}$/.test(name) || !record(app) || typeof app.healthy !== 'boolean' || !['NOT_DEPLOYED', 'STOPPED', 'DEPLOYING', 'DEPLOYED', 'READY', 'UNHEALTHY'].includes(String(app.state))) throw new Error('Runtime returned an invalid application status.');
    if (app.deployment !== undefined && !['NOT_RUNNING', 'PENDING', 'DEPLOYED', 'FAILED'].includes(String(app.deployment))) throw new Error('Runtime returned an invalid deployment status.');
    if (app.endpoint !== undefined && !['NOT_CHECKED', 'NOT_READY', 'READY'].includes(String(app.endpoint))) throw new Error('Runtime returned an invalid endpoint status.');
    if ((app.state === 'UNHEALTHY' || app.deployment === 'FAILED') && app.healthy) throw new Error('Contradictory runtime health response was rejected.');
  }
  if (value.process === 'STOPPED' && value.healthy) throw new Error('Stopped runtime cannot be healthy.');
  return value as unknown as RuntimeStatus;
}
