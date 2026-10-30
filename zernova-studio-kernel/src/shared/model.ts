/**
 * In-memory, bounded design-preview state. This is not an application format,
 * a runtime adapter, a compiler or a replacement for Mule XML/Maven projects.
 */
export const PREVIEW_SCHEMA_VERSION = 1 as const;
export const PREVIEW_STATE_KEY = 'zernovaKernelStudio.preview.v1';
export const MAX_PROCESSORS = 20;
export const MAX_CONFIGS = 12;
// Covers the bounded maximum of 20 processors with long Unicode messages/notes.
export const MAX_STATE_BYTES = 1024 * 1024;

export type EditorTab = 'flow' | 'globals' | 'xml' | 'dataweave';
export type LowerTab = 'properties' | 'problems' | 'console' | 'outline';
export type PreviewTheme = 'host' | 'light' | 'dark';
export type Scenario = 'workbench' | 'listener' | 'logger' | 'globals' | 'xml' | 'dataweave' | 'empty';
export const SCENARIOS: readonly Scenario[] = ['workbench', 'listener', 'logger', 'globals', 'xml', 'dataweave', 'empty'];
export const LOG_LEVELS = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR'] as const;
export type LogLevel = typeof LOG_LEVELS[number];
export const HTTP_METHODS = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS'] as const;

export interface Listener {
  id: string;
  kind: 'listener';
  displayName: string;
  configId: string;
  path: string;
  allowedMethods: string;
  notes: string;
}
export interface Logger {
  id: string;
  kind: 'logger';
  displayName: string;
  message: string;
  level: LogLevel;
  category: string;
  expressionMode: boolean;
  notes: string;
}
export type Component = Listener | Logger;
export interface HttpConfig {
  id: string;
  name: string;
  protocol: 'HTTP' | 'HTTPS';
  host: string;
  port: string;
  basePath: string;
}
export interface PreviewLayout {
  paletteWidth: number;
  propertiesHeight: number;
  metadataWidth: number;
  dwInputWidth: number;
  dwOutputWidth: number;
  dwSourceWidth: number;
}
export const DEFAULT_LAYOUT: Readonly<PreviewLayout> = Object.freeze({
  paletteWidth: 224,
  propertiesHeight: 310,
  metadataWidth: 244,
  dwInputWidth: 185,
  dwOutputWidth: 175,
  dwSourceWidth: 310,
});
export interface PreviewState {
  schemaVersion: typeof PREVIEW_SCHEMA_VERSION;
  projectId: 'zernova-hello-integration';
  flow: {
    id: 'hello-flow';
    name: string;
    collapsed: boolean;
    errorExpanded: boolean;
    source: Listener | null;
    processors: Logger[];
  };
  selectedId: string | null;
  editorTab: EditorTab;
  lowerTab: LowerTab;
  propertySection: string;
  metadataTab: 'input' | 'output';
  expandedMetadata: string[];
  globalConfigs: HttpConfig[];
  selectedConfigId: string | null;
  layout: PreviewLayout;
  theme: PreviewTheme;
  dwMode: 'graphics' | 'trees' | 'source';
  zoom: number;
  nextId: number;
}
export interface ConfigDraft {
  id: string;
  sourceConfigId: string | null;
  value: HttpConfig;
  section: 'general' | 'tls' | 'advanced';
}
export type ValidationErrors = Record<string, string>;

export function createInitialState(empty = false): PreviewState {
  return {
    schemaVersion: PREVIEW_SCHEMA_VERSION,
    projectId: 'zernova-hello-integration',
    flow: {
      id: 'hello-flow', name: 'helloFlow', collapsed: false, errorExpanded: false,
      source: empty ? null : { id: 'listener-1', kind: 'listener', displayName: 'Listener', configId: 'http-config-1', path: '/hello', allowedMethods: 'GET', notes: '' },
      processors: empty ? [] : [{ id: 'logger-1', kind: 'logger', displayName: 'Log request', message: '#["Request received"]', level: 'INFO', category: 'io.zernova.demo', expressionMode: true, notes: '' }],
    },
    selectedId: null, editorTab: 'flow', lowerTab: 'properties', propertySection: 'General', metadataTab: 'input',
    expandedMetadata: ['input-payload', 'input-attributes'],
    globalConfigs: [{ id: 'http-config-1', name: 'HTTP_Listener_config', protocol: 'HTTP', host: '127.0.0.1', port: '18081', basePath: '' }],
    selectedConfigId: 'http-config-1', layout: { ...DEFAULT_LAYOUT }, theme: 'host', dwMode: 'trees', zoom: 100, nextId: 2,
  };
}

export function cloneState(state: PreviewState): PreviewState {
  return JSON.parse(JSON.stringify(state)) as PreviewState;
}

const isRecord = (value: unknown): value is Record<string, unknown> => typeof value === 'object' && value !== null && !Array.isArray(value);
const safeString = (value: unknown, fallback: string, limit = 200): string => typeof value === 'string' ? value.slice(0, limit).replace(/[\u0000-\u0008\u000b\u000c\u000e-\u001f]/g, '') : fallback;
const safeId = (value: unknown, fallback: string): string => typeof value === 'string' && /^[a-z][a-z0-9-]{0,63}$/.test(value) ? value : fallback;
const choose = <T extends string>(value: unknown, values: readonly T[], fallback: T): T => typeof value === 'string' && values.includes(value as T) ? value as T : fallback;
const finite = (value: unknown, fallback: number, min: number, max: number): number => typeof value === 'number' && Number.isFinite(value) ? Math.round(Math.max(min, Math.min(max, value))) : fallback;
export const jsonByteLength = (value: unknown): number => new TextEncoder().encode(JSON.stringify(value)).length;

/** Drop incompatible versions and reconstruct a fresh object from an allowlist. */
export function sanitizeState(input: unknown): PreviewState {
  const fallback = createInitialState();
  if (!isRecord(input) || input.schemaVersion !== PREVIEW_SCHEMA_VERSION) return fallback;
  try { if (jsonByteLength(input) > MAX_STATE_BYTES) return fallback; } catch { return fallback; }
  const state = createInitialState();
  const flow = isRecord(input.flow) ? input.flow : {};
  state.flow.name = safeString(flow.name, state.flow.name, 100);
  state.flow.collapsed = flow.collapsed === true;
  state.flow.errorExpanded = flow.errorExpanded === true;
  const configIds = new Set<string>();
  const configNames = new Set<string>();
  if (Array.isArray(input.globalConfigs)) {
    state.globalConfigs = input.globalConfigs.slice(0, MAX_CONFIGS).filter(isRecord).map((c, index): HttpConfig => ({
      id: safeId(c.id, `http-config-${index + 1}`), name: safeString(c.name, `HTTP_Listener_config_${index + 1}`, 100),
      protocol: choose(c.protocol, ['HTTP', 'HTTPS'], 'HTTP'), host: safeString(c.host, '127.0.0.1', 253),
      port: safeString(c.port, '18081', 5), basePath: safeString(c.basePath, '', 512),
    })).filter(c => {
      if (configIds.has(c.id) || configNames.has(c.name)) return false;
      configIds.add(c.id); configNames.add(c.name); return true;
    });
  } else {
    state.globalConfigs.forEach(c => { configIds.add(c.id); configNames.add(c.name); });
  }
  const componentIds = new Set<string>();
  if (flow.source === null) state.flow.source = null;
  else if (isRecord(flow.source) && flow.source.kind === 'listener') {
    const source = flow.source;
    const configId = typeof source.configId === 'string' && configIds.has(source.configId) ? source.configId : state.globalConfigs[0]?.id;
    state.flow.source = configId ? {
      id: safeId(source.id, 'listener-1'), kind: 'listener', displayName: safeString(source.displayName, 'Listener', 100),
      configId, path: safeString(source.path, '/hello', 512), allowedMethods: safeString(source.allowedMethods, 'GET', 80), notes: safeString(source.notes, '', 4000),
    } : null;
  } else if (!state.globalConfigs.some(c => c.id === state.flow.source?.configId)) {
    state.flow.source = state.globalConfigs[0] ? { ...state.flow.source!, configId: state.globalConfigs[0].id } : null;
  }
  if (state.flow.source) componentIds.add(state.flow.source.id);
  if (Array.isArray(flow.processors)) {
    state.flow.processors = flow.processors.slice(0, MAX_PROCESSORS).filter(isRecord).filter(p => p.kind === 'logger').map((p, index): Logger => ({
      id: safeId(p.id, `logger-${index + 1}`), kind: 'logger', displayName: safeString(p.displayName, 'Log request', 100),
      message: safeString(p.message, '#["Request received"]', 8000), level: choose(p.level, LOG_LEVELS, 'INFO'),
      category: safeString(p.category, 'io.zernova.demo', 200), expressionMode: p.expressionMode !== false, notes: safeString(p.notes, '', 4000),
    })).filter(p => {
      if (componentIds.has(p.id)) return false;
      componentIds.add(p.id); return true;
    });
  } else state.flow.processors.forEach(p => componentIds.add(p.id));
  state.selectedId = typeof input.selectedId === 'string' && componentIds.has(input.selectedId) ? input.selectedId : null;
  state.selectedConfigId = typeof input.selectedConfigId === 'string' && configIds.has(input.selectedConfigId) ? input.selectedConfigId : state.globalConfigs[0]?.id ?? null;
  state.editorTab = choose(input.editorTab, ['flow', 'globals', 'xml', 'dataweave'], 'flow');
  state.lowerTab = choose(input.lowerTab, ['properties', 'problems', 'console', 'outline'], 'properties');
  state.propertySection = choose(input.propertySection, ['General', 'MIME Type', 'Redelivery', 'Responses', 'Metadata', 'Notes', 'Help'], 'General');
  state.metadataTab = choose(input.metadataTab, ['input', 'output'], 'input');
  state.expandedMetadata = Array.isArray(input.expandedMetadata) ? [...new Set(input.expandedMetadata.filter((s): s is string => typeof s === 'string' && /^[a-zA-Z0-9_-]{1,80}$/.test(s)))].slice(0, 40) : state.expandedMetadata;
  const layout = isRecord(input.layout) ? input.layout : {};
  state.layout = {
    paletteWidth: finite(layout.paletteWidth, DEFAULT_LAYOUT.paletteWidth, 190, 380),
    propertiesHeight: finite(layout.propertiesHeight, DEFAULT_LAYOUT.propertiesHeight, 230, 620),
    metadataWidth: finite(layout.metadataWidth, DEFAULT_LAYOUT.metadataWidth, 200, 420),
    dwInputWidth: finite(layout.dwInputWidth, DEFAULT_LAYOUT.dwInputWidth, 140, 400),
    dwOutputWidth: finite(layout.dwOutputWidth, DEFAULT_LAYOUT.dwOutputWidth, 140, 400),
    dwSourceWidth: finite(layout.dwSourceWidth, DEFAULT_LAYOUT.dwSourceWidth, 200, 600),
  };
  state.theme = choose(input.theme, ['host', 'light', 'dark'], 'host');
  state.dwMode = choose(input.dwMode, ['graphics', 'trees', 'source'], 'trees');
  state.zoom = finite(input.zoom, 100, 60, 160);
  state.nextId = finite(input.nextId, 2, 2, 1000000);
  return state;
}

/** A bridge message may contain only a complete, already-valid state snapshot. */
export function isValidPreviewState(input: unknown): input is PreviewState {
  if (!isRecord(input)) return false;
  const ordered = (value: unknown): unknown => Array.isArray(value)
    ? value.map(ordered)
    : isRecord(value) ? Object.fromEntries(Object.keys(value).sort().map(key => [key, ordered(value[key])])) : value;
  try {
    return jsonByteLength(input) <= MAX_STATE_BYTES && JSON.stringify(ordered(input)) === JSON.stringify(ordered(sanitizeState(input)));
  } catch { return false; }
}

export function validateHttpConfig(config: HttpConfig, otherConfigs: readonly HttpConfig[] = []): ValidationErrors {
  const errors: ValidationErrors = {};
  if (!/^[A-Za-z_][A-Za-z0-9_.-]{0,99}$/.test(config.name)) errors.name = 'Use a name beginning with a letter or underscore; letters, numbers, dots and hyphens are supported.';
  else if (otherConfigs.some(c => c.id !== config.id && c.name === config.name)) errors.name = 'A preview configuration with this name already exists.';
  if (config.protocol !== 'HTTP' && config.protocol !== 'HTTPS') errors.protocol = 'Choose HTTP or HTTPS.';
  if (!config.host.trim()) errors.host = 'Enter a sample host.';
  else if (/\s/.test(config.host)) errors.host = 'A sample host cannot contain whitespace.';
  if (!/^\d{1,5}$/.test(config.port) || Number(config.port) < 1 || Number(config.port) > 65535) errors.port = 'Enter a sample port between 1 and 65535.';
  if (config.basePath !== '' && !config.basePath.startsWith('/')) errors.basePath = 'Base path must be empty or begin with /.';
  return errors;
}

export function validateComponent(component: Component, configs?: readonly HttpConfig[]): ValidationErrors {
  const errors: ValidationErrors = {};
  if (!component.displayName.trim()) errors.displayName = 'Enter a display name.';
  if (component.kind === 'listener') {
    if (!component.path.startsWith('/')) errors.path = 'Path must begin with /.';
    if (!component.configId || (configs && !configs.some(c => c.id === component.configId))) errors.configId = 'Choose a saved preview configuration.';
    const methods = component.allowedMethods.split(',').map(m => m.trim());
    if (!methods.length || methods.some(m => !HTTP_METHODS.includes(m as typeof HTTP_METHODS[number]))) errors.allowedMethods = 'Choose valid HTTP methods, separated by commas.';
  } else {
    if (!component.message.trim()) errors.message = 'Enter a sample message. Expressions are not evaluated.';
    if (!LOG_LEVELS.includes(component.level)) errors.level = 'Choose a supported sample log level.';
  }
  return errors;
}

export function getComponent(state: PreviewState, id: string | null = state.selectedId): Component | undefined {
  return state.flow.source?.id === id ? state.flow.source : state.flow.processors.find(p => p.id === id);
}

export type PreviewAction =
  | { type: 'select'; id: string | null }
  | { type: 'insert'; kind: 'listener' | 'logger'; lane: 'source' | 'processor'; index?: number }
  | { type: 'delete'; id: string }
  | { type: 'duplicate'; id: string }
  | { type: 'move'; id: string; index: number }
  | { type: 'updateComponent'; id: string; changes: Partial<Omit<Listener, 'id' | 'kind'>> | Partial<Omit<Logger, 'id' | 'kind'>> }
  | { type: 'saveConfig'; config: HttpConfig }
  | { type: 'deleteConfig'; id: string }
  | { type: 'setScenario'; scenario: Scenario }
  | { type: 'setLayout'; layout: Partial<PreviewLayout> }
  | { type: 'resetLayout' };
export interface ActionResult { state: PreviewState; error?: string }

function newId(state: PreviewState, prefix: string): string {
  const existing = new Set([state.flow.source?.id, ...state.flow.processors.map(p => p.id), ...state.globalConfigs.map(c => c.id)]);
  let id: string;
  do { id = `${prefix}-${state.nextId++}`; } while (existing.has(id));
  return id;
}

/** Immutable bounded edits: unsuccessful edits preserve the original snapshot. */
export function applyAction(current: PreviewState, action: PreviewAction): ActionResult {
  const state = cloneState(current);
  const fail = (error: string): ActionResult => ({ state: current, error });
  switch (action.type) {
    case 'select':
      if (action.id !== null && !getComponent(state, action.id)) return fail('Choose a component in this preview flow.');
      state.selectedId = action.id; state.propertySection = 'General'; state.lowerTab = 'properties'; break;
    case 'insert': {
      if (action.kind === 'listener') {
        if (action.lane !== 'source') return fail('HTTP Listener belongs in the Source lane.');
        if (state.flow.source) return fail('This flow already has a source. Only one HTTP Listener is allowed.');
        if (!state.globalConfigs.length) return fail('Create an HTTP Listener configuration before adding a source.');
        state.flow.source = { id: newId(state, 'listener'), kind: 'listener', displayName: 'Listener', configId: state.globalConfigs[0].id, path: '/hello', allowedMethods: 'GET', notes: '' };
        state.selectedId = state.flow.source.id;
      } else {
        if (action.lane !== 'processor') return fail('Logger belongs in the Processor lane.');
        if (state.flow.processors.length >= MAX_PROCESSORS) return fail(`This bounded preview supports up to ${MAX_PROCESSORS} Logger components.`);
        const logger: Logger = { id: newId(state, 'logger'), kind: 'logger', displayName: 'Logger', message: '#["Request received"]', level: 'INFO', category: 'io.zernova.demo', expressionMode: true, notes: '' };
        const index = finite(action.index, state.flow.processors.length, 0, state.flow.processors.length);
        state.flow.processors.splice(index, 0, logger); state.selectedId = logger.id;
      }
      state.flow.collapsed = false; state.editorTab = 'flow'; state.lowerTab = 'properties'; state.propertySection = 'General'; break;
    }
    case 'delete': {
      if (!getComponent(state, action.id)) return fail('The selected component is no longer in this preview.');
      if (state.flow.source?.id === action.id) state.flow.source = null;
      else state.flow.processors = state.flow.processors.filter(p => p.id !== action.id);
      if (state.selectedId === action.id) state.selectedId = null;
      break;
    }
    case 'duplicate': {
      const index = state.flow.processors.findIndex(p => p.id === action.id);
      if (index < 0) return fail('Only Logger processors can be duplicated; a flow has one source.');
      if (state.flow.processors.length >= MAX_PROCESSORS) return fail(`This bounded preview supports up to ${MAX_PROCESSORS} Logger components.`);
      const copy = { ...state.flow.processors[index], id: newId(state, 'logger'), displayName: `${state.flow.processors[index].displayName} copy`.slice(0, 100) };
      state.flow.processors.splice(index + 1, 0, copy); state.selectedId = copy.id; break;
    }
    case 'move': {
      const index = state.flow.processors.findIndex(p => p.id === action.id);
      if (index < 0) return fail('Only Logger processors can be reordered in the Processor lane.');
      const [moved] = state.flow.processors.splice(index, 1);
      state.flow.processors.splice(finite(action.index, index, 0, state.flow.processors.length), 0, moved);
      state.selectedId = moved.id; break;
    }
    case 'updateComponent': {
      const component = getComponent(state, action.id);
      if (!component) return fail('The component is no longer in this preview.');
      const id = component.id; const kind = component.kind;
      Object.assign(component, action.changes, { id, kind });
      // Reconstruct only known fields; drafts can be invalid but cannot add capabilities.
      const sanitized = sanitizeState(state);
      return { state: sanitized };
    }
    case 'saveConfig': {
      const errors = validateHttpConfig(action.config, state.globalConfigs);
      if (Object.keys(errors).length) return fail(Object.values(errors).join(' '));
      const index = state.globalConfigs.findIndex(c => c.id === action.config.id);
      if (index < 0) {
        if (state.globalConfigs.length >= MAX_CONFIGS) return fail(`This bounded preview supports up to ${MAX_CONFIGS} global configurations.`);
        state.globalConfigs.push({ ...action.config, id: newId(state, 'http-config') });
        state.selectedConfigId = state.globalConfigs[state.globalConfigs.length - 1].id;
      } else {
        state.globalConfigs[index] = { ...action.config }; state.selectedConfigId = action.config.id;
      }
      break;
    }
    case 'deleteConfig':
      if (!state.globalConfigs.some(c => c.id === action.id)) return fail('This preview configuration no longer exists.');
      if (state.flow.source?.configId === action.id) return fail(`Configuration is used by ${state.flow.source.displayName || 'HTTP Listener'}. Select another configuration or remove the Listener before deleting it.`);
      state.globalConfigs = state.globalConfigs.filter(c => c.id !== action.id);
      if (state.selectedConfigId === action.id) state.selectedConfigId = state.globalConfigs[0]?.id ?? null;
      break;
    case 'setScenario': return { state: applyScenario(state, action.scenario) };
    case 'setLayout': state.layout = sanitizeState({ ...state, layout: { ...state.layout, ...action.layout } }).layout; break;
    case 'resetLayout': state.layout = { ...DEFAULT_LAYOUT }; state.zoom = 100; break;
  }
  return { state };
}

/** View navigation preserves sample edits; Empty explicitly replaces the flow. */
export function applyScenario(current: PreviewState, scenario: Scenario): PreviewState {
  const state = cloneState(current);
  state.lowerTab = 'properties'; state.propertySection = 'General';
  if (scenario === 'empty') {
    state.flow.source = null; state.flow.processors = []; state.flow.collapsed = false; state.selectedId = null; state.editorTab = 'flow'; return state;
  }
  if (scenario === 'listener' || scenario === 'logger' || scenario === 'workbench') {
    state.editorTab = 'flow'; state.flow.collapsed = false;
    state.selectedId = scenario === 'listener' ? state.flow.source?.id ?? null : scenario === 'logger' ? state.flow.processors[0]?.id ?? null : null;
  } else { state.editorTab = scenario; state.selectedId = null; }
  return state;
}

/** One-step in-memory UI undo/redo. No VS Code document/history access. */
export class PreviewHistory {
  private previous: PreviewState | null = null;
  private future: PreviewState | null = null;
  get canUndo(): boolean { return this.previous !== null; }
  get canRedo(): boolean { return this.future !== null; }
  commit(before: PreviewState, after: PreviewState): PreviewState {
    if (JSON.stringify(before) !== JSON.stringify(after)) { this.previous = cloneState(before); this.future = null; }
    return cloneState(after);
  }
  undo(current: PreviewState): PreviewState {
    if (!this.previous) return current;
    const restored = this.previous; this.previous = null; this.future = cloneState(current); return cloneState(restored);
  }
  redo(current: PreviewState): PreviewState {
    if (!this.future) return current;
    const restored = this.future; this.future = null; this.previous = cloneState(current); return cloneState(restored);
  }
  clear(): void { this.previous = null; this.future = null; }
}

/** XML 1.0 attributes: preserve newlines explicitly and never inject markup. */
export function escapeXmlAttribute(value: string): string {
  return value.replace(/[^\u0009\u000a\u000d\u0020-\ud7ff\ue000-\ufffd\u{10000}-\u{10ffff}]/gu, '')
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;').replace(/\r/g, '&#13;').replace(/\n/g, '&#10;').replace(/\t/g, '&#9;');
}

/** Tiny serializer for this fixture only; output is read-only and never saved. */
export function serializeXml(state: PreviewState): string {
  const x = escapeXmlAttribute;
  const lines = [
    '<?xml version="1.0" encoding="UTF-8"?>',
    '<!-- DESIGN PREVIEW: sample state only; not saved to a project. -->',
    '<mule xmlns="http://www.mulesoft.org/schema/mule/core"',
    '      xmlns:http="http://www.mulesoft.org/schema/mule/http"',
    '      xmlns:doc="http://www.mulesoft.org/schema/mule/documentation"',
    '      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"',
    '      xsi:schemaLocation="',
    '        http://www.mulesoft.org/schema/mule/core',
    '        http://www.mulesoft.org/schema/mule/core/current/mule.xsd',
    '        http://www.mulesoft.org/schema/mule/http',
    '        http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd">',
    '',
  ];
  for (const config of state.globalConfigs) {
    const basePath = config.basePath ? ` basePath="${x(config.basePath)}"` : '';
    lines.push(`  <http:listener-config name="${x(config.name)}"${basePath}>`);
    lines.push(`    <http:listener-connection protocol="${x(config.protocol)}"`);
    lines.push(`                              host="${x(config.host)}" port="${x(config.port)}" />`);
    lines.push('  </http:listener-config>', '');
  }
  lines.push(`  <flow name="${x(state.flow.name)}">`);
  if (state.flow.source) {
    const source = state.flow.source;
    const config = state.globalConfigs.find(c => c.id === source.configId);
    if (config) {
      lines.push(`    <http:listener doc:name="${x(source.displayName)}" config-ref="${x(config.name)}"`);
      lines.push(`                   path="${x(source.path)}" allowedMethods="${x(source.allowedMethods)}" />`);
    }
  }
  for (const logger of state.flow.processors) {
    lines.push(`    <logger doc:name="${x(logger.displayName)}" level="${x(logger.level)}"`);
    lines.push(`            message="${x(logger.message)}" category="${x(logger.category)}" />`);
  }
  if (!state.flow.source && !state.flow.processors.length) lines.push('    <!-- Empty fixture flow. Add a source and processors in Message Flow. -->');
  lines.push('  </flow>', '</mule>');
  return lines.join('\n');
}

export const DATAWEAVE_SOURCE = `%dw 2.0\noutput application/json\n---\n{\n  fullName: payload.firstName\n    ++ " " ++ payload.lastName\n}`;
export const DATAWEAVE_INPUT = { firstName: 'Samira', lastName: 'Tesfaye' };
export const DATAWEAVE_OUTPUT = { fullName: 'Samira Tesfaye' };
export const DATAWEAVE_NOTICE = 'Illustrative — not executed';
