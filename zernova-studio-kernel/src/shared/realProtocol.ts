import { MAX_XML_BYTES, type IntegrationModel, type XmlOperation, type ComponentValues } from './xml';

export const RUNTIME_ACTIONS = ['configureRuntime', 'configureRun', 'configureDebug', 'debug', 'diagnostics', 'build', 'run', 'status', 'logs', 'stopApplication', 'stopRuntime', 'cancel'] as const;
export type RuntimeAction = typeof RUNTIME_ACTIONS[number];
export const REAL_ACTIONS = ['save', 'undo', 'redo', ...RUNTIME_ACTIONS] as const;
export type RealAction = typeof REAL_ACTIONS[number];
export interface RealDocumentSnapshot {
  uri: string;
  version: number;
  text: string;
  dirty: boolean;
  projectName: string;
  model: IntegrationModel;
  selectionId?: string;
  /** Present only while this exact text is the expected result of that request. */
  changeRequestId?: string;
}
export type RealWebviewMessage =
  | { type: 'ready' }
  | { type: 'realEdit'; version: number; requestId: string; operation: XmlOperation }
  | { type: 'realXml'; version: number; requestId: string; text: string }
  | { type: 'realAction'; action: RealAction };
export type RealHostMessage =
  | { type: 'realDocument'; document: RealDocumentSnapshot }
  | { type: 'realAck'; requestId: string; version: number; selectionId?: string }
  | { type: 'realError'; message: string; requestId?: string }
  | { type: 'runtimeState'; state: unknown };

const record = (v: unknown): v is Record<string, unknown> => typeof v === 'object' && v !== null && !Array.isArray(v);
const only = (v: Record<string, unknown>, required: string[], optional: string[] = []): boolean => required.every(k => Object.hasOwn(v, k)) && Object.keys(v).every(k => required.includes(k) || optional.includes(k));
const string = (v: unknown, max = 1000): v is string => typeof v === 'string' && v.length <= max && !/[\u0000-\u0008\u000b\u000c\u000e-\u001f]/.test(v);
const id = (v: unknown): v is string => string(v, 512) && v.length > 0;
const requestId = (v: unknown): v is string => typeof v === 'string' && /^[A-Za-z0-9_-]{1,100}$/.test(v);
const version = (v: unknown): v is number => Number.isSafeInteger(v) && Number(v) > 0;
const componentKeys: (keyof ComponentValues)[] = ['displayName', 'configId', 'path', 'allowedMethods', 'message', 'level', 'category', 'script', 'mimeType', 'responseStatus', 'responseBody', 'responseHeaders'];

export function parseXmlOperation(input: unknown): XmlOperation | null {
  if (!record(input) || typeof input.kind !== 'string') return null;
  const op = input;
  if (op.kind === 'createFlow' && only(op, ['kind', 'name']) && string(op.name, 100)) return { kind: op.kind, name: op.name };
  if (op.kind === 'createErrorHandler' && only(op, ['kind', 'flowId']) && id(op.flowId)) return { kind: op.kind, flowId: op.flowId };
  if (op.kind === 'renameFlow' && only(op, ['kind', 'flowId', 'name']) && id(op.flowId) && string(op.name, 100)) return { kind: op.kind, flowId: op.flowId, name: op.name };
  if (op.kind === 'insert' && only(op, ['kind', 'flowId', 'component'], ['index', 'lane']) && (op.lane === undefined || op.lane === 'error') && id(op.flowId) && (op.component === 'listener' || op.component === 'logger' || op.component === 'transform') && (op.index === undefined || Number.isSafeInteger(op.index) && Number(op.index) >= 0 && Number(op.index) <= 100)) return { kind: op.kind, flowId: op.flowId, component: op.component, ...(op.index !== undefined ? { index: Number(op.index) } : {}), ...(op.lane === 'error' ? { lane: 'error' as const } : {}) };
  if (op.kind === 'updateComponent' && only(op, ['kind', 'id', 'values']) && id(op.id) && record(op.values) && Object.keys(op.values).length > 0 && Object.keys(op.values).every(k => componentKeys.includes(k as keyof ComponentValues) && string((op.values as Record<string, unknown>)[k], 16000))) return { kind: op.kind, id: op.id, values: { ...op.values } as ComponentValues };
  if ((op.kind === 'deleteComponent' || op.kind === 'duplicateLogger' || op.kind === 'duplicateProcessor' || op.kind === 'deleteConfig') && only(op, ['kind', 'id']) && id(op.id)) return { kind: op.kind, id: op.id };
  if ((op.kind === 'moveLogger' || op.kind === 'moveProcessor') && only(op, ['kind', 'id', 'direction']) && id(op.id) && (op.direction === -1 || op.direction === 1)) return { kind: op.kind, id: op.id, direction: op.direction };
  if (op.kind === 'saveConfig' && only(op, ['kind', 'values'], ['id']) && (op.id === undefined || id(op.id)) && record(op.values)) {
    const v = op.values;
    if (only(v, ['name', 'protocol', 'host', 'port', 'basePath']) && string(v.name, 100) && v.protocol === 'HTTP' && string(v.host, 253) && string(v.port, 1000) && string(v.basePath, 1000)) return { kind: op.kind, ...(op.id !== undefined ? { id: op.id } : {}), values: { name: v.name, protocol: v.protocol, host: v.host, port: v.port, basePath: v.basePath } };
  }
  return null;
}

/** Closed, document-scoped bridge. It never accepts a URI, executable, path or command string. */
export function parseRealMessage(input: unknown): RealWebviewMessage | null {
  if (!record(input)) return null;
  try { if (new TextEncoder().encode(JSON.stringify(input)).length > MAX_XML_BYTES + 200000) return null; } catch { return null; }
  if (input.type === 'ready' && only(input, ['type'])) return { type: 'ready' };
  if (input.type === 'realAction' && only(input, ['type', 'action']) && typeof input.action === 'string' && REAL_ACTIONS.includes(input.action as RealAction)) return { type: 'realAction', action: input.action as RealAction };
  if (input.type === 'realXml' && only(input, ['type', 'version', 'requestId', 'text']) && version(input.version) && requestId(input.requestId) && string(input.text, MAX_XML_BYTES) && new TextEncoder().encode(input.text).length <= MAX_XML_BYTES) return { type: 'realXml', version: input.version, requestId: input.requestId, text: input.text };
  if (input.type === 'realEdit' && only(input, ['type', 'version', 'requestId', 'operation']) && version(input.version) && requestId(input.requestId)) {
    const operation = parseXmlOperation(input.operation);
    if (operation) return { type: 'realEdit', version: input.version, requestId: input.requestId, operation };
  }
  return null;
}
