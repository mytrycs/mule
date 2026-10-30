import { isValidPreviewState, jsonByteLength, MAX_STATE_BYTES, PreviewState, Scenario, SCENARIOS } from './model';

/** A closed message surface: no command, filesystem, network or shell bridge. */
export type WebviewMessage =
  | { type: 'ready' }
  | { type: 'saveState'; state: PreviewState }
  | { type: 'resetPreview' }
  | { type: 'resetLayout' };

export type HostMessage =
  | { type: 'state'; state: PreviewState }
  | { type: 'scenario'; scenario: Scenario }
  | { type: 'hostTheme'; kind: 'light' | 'dark' | 'highContrast' | 'highContrastLight' };

export function parseWebviewMessage(input: unknown): WebviewMessage | null {
  if (typeof input !== 'object' || input === null || Array.isArray(input)) return null;
  const message = input as Record<string, unknown>;
  try { if (jsonByteLength(input) > MAX_STATE_BYTES + 100) return null; } catch { return null; }
  const keys = Object.keys(message);
  if (message.type === 'ready' || message.type === 'resetPreview' || message.type === 'resetLayout') {
    return keys.length === 1 ? { type: message.type } : null;
  }
  if (message.type === 'saveState' && keys.length === 2 && keys.includes('state') && isValidPreviewState(message.state)) {
    return { type: 'saveState', state: message.state };
  }
  return null;
}

export function isScenario(value: unknown): value is Scenario {
  return typeof value === 'string' && SCENARIOS.includes(value as Scenario);
}
