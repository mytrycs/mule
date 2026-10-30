import { spawn } from 'node:child_process';

export interface ProcessResult {
  executable: string; args: string[]; cwd: string; exitCode: number | null;
  signal: NodeJS.Signals | null; stdout: string; stderr: string; milliseconds: number;
  cancelled: boolean; timedOut: boolean;
}
export interface ProcessOptions {
  executable: string; args: string[]; cwd: string; env: NodeJS.ProcessEnv;
  timeoutMs: number; signal?: AbortSignal; onOutput?: (text: string) => void;
}

/** Explicit executable/argv only. Cancellation targets this spawned process group. */
export function runProcess(options: ProcessOptions): Promise<ProcessResult> {
  return new Promise((resolve, reject) => {
    if (options.signal?.aborted) { reject(new Error('Operation cancelled before launch.')); return; }
    const began = Date.now();
    const child = spawn(options.executable, options.args, {
      cwd: options.cwd, env: options.env, shell: false, detached: process.platform === 'linux',
      stdio: ['ignore', 'pipe', 'pipe'], windowsHide: true,
    });
    let stdout = '', stderr = '', cancelled = false, timedOut = false, closed = false;
    let forceTimer: ReturnType<typeof setTimeout> | undefined;
    const terminate = () => {
      if (closed || !child.pid) return;
      try { process.platform === 'linux' ? process.kill(-child.pid, 'SIGTERM') : child.kill('SIGTERM'); } catch { /* It may have exited. */ }
      forceTimer = setTimeout(() => {
        if (closed || !child.pid) return;
        try { process.platform === 'linux' ? process.kill(-child.pid, 'SIGKILL') : child.kill('SIGKILL'); } catch { /* Already exited. */ }
      }, 3000);
    };
    const abort = () => { cancelled = true; terminate(); };
    options.signal?.addEventListener('abort', abort, { once: true });
    const timeout = setTimeout(() => { timedOut = true; terminate(); }, options.timeoutMs);
    const cap = 4 * 1024 * 1024;
    child.stdout.setEncoding('utf8'); child.stderr.setEncoding('utf8');
    child.stdout.on('data', (text: string) => { stdout = (stdout + text).slice(-cap); options.onOutput?.(text); });
    child.stderr.on('data', (text: string) => { stderr = (stderr + text).slice(-cap); options.onOutput?.(text); });
    const clear = () => { closed = true; clearTimeout(timeout); if (forceTimer) clearTimeout(forceTimer); options.signal?.removeEventListener('abort', abort); };
    child.once('error', error => { clear(); reject(error); });
    child.once('close', (exitCode, signal) => {
      clear(); resolve({ executable: options.executable, args: [...options.args], cwd: options.cwd,
        exitCode, signal, stdout, stderr, milliseconds: Date.now() - began, cancelled, timedOut });
    });
  });
}
