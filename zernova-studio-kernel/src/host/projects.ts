import * as vscode from 'vscode';
import { studioForm } from './forms';
import { defaultRunConfiguration, parseRunFields, type RunConfiguration } from '../../../zernova-studio-build/src/portable/run-config';
import * as fs from 'node:fs/promises';
import path from 'node:path';
import { createHash } from 'node:crypto';
import * as sax from 'sax';
import { convertLegacyIntegrationXml } from '../shared/xml';
import { RUNTIME_VERSION, walkFiles } from '../../../zernova-studio-build/src/portable/bundle';

export interface Project { root: string; name: string; groupId: string; artifactId: string; version: string; }
export interface RuntimeSettings { schemaVersion: 2; runtimeHome: string; instanceDirectory: string; offline: true; }
const PROJECTS_KEY = 'zernovaKernelStudio.projects.v1';
const SETTINGS_KEY = 'zernovaKernelStudio.runtimeProfiles.v1';
const SELECTED_KEY = 'zernovaKernelStudio.selectedProject.v1';
const artifactPattern = /^[A-Za-z][A-Za-z0-9_.-]{0,79}$/;
const groupPattern = /^[A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)+$/;
const versionPattern = /^[0-9][A-Za-z0-9_.-]{0,49}$/;
const xmlEscape = (value: string) => value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/"/g, '&quot;');

export const NEW_INTEGRATION_XML = `<?xml version="1.0" encoding="UTF-8"?>
<zernova xmlns="https://schemas.zernova.dev/core"
      xmlns:http="https://schemas.zernova.dev/http"
      xmlns:doc="https://schemas.zernova.dev/documentation"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="https://schemas.zernova.dev/core https://schemas.zernova.dev/core/current/zernova.xsd https://schemas.zernova.dev/http https://schemas.zernova.dev/http/current/zernova-http.xsd">
  <configuration-properties file="application.properties"/>
  <!-- Create a flow in ZERNOVA Studio. This document is initially empty. -->
</zernova>
`;

export function isInside(root: string, file: string): boolean {
  const relative = path.relative(root, file);
  return relative === '' || (!relative.startsWith('..' + path.sep) && relative !== '..' && !path.isAbsolute(relative));
}
/** Parse the bounded Maven coordinates without interpreting a POM as executable input. */
export function parseProjectPom(text: string): Pick<Project, 'groupId' | 'artifactId' | 'version'> {
  const parser = sax.parser(true, { xmlns: true });
  const stack: string[] = []; const values: Record<string, string> = {};
  parser.ondoctype = () => { throw new Error('POM DOCTYPE declarations are not supported.'); };
  parser.onopentag = (node: { local?: string; name: string }) => { stack.push(node.local ?? node.name); };
  parser.onclosetag = () => { stack.pop(); };
  parser.ontext = (text: string) => { const key = stack.join('/'); if (['project/groupId', 'project/artifactId', 'project/version'].includes(key)) values[key] = (values[key] ?? '') + text; };
  parser.write(text).close();
  const groupId = values['project/groupId']?.trim() ?? '', artifactId = values['project/artifactId']?.trim() ?? '', version = values['project/version']?.trim() ?? '';
  if (!groupPattern.test(groupId) || !artifactPattern.test(artifactId) || !versionPattern.test(version)) throw new Error('This milestone requires explicit valid Maven groupId, artifactId and version in the project POM (no inherited or expression coordinates).');
  return { groupId, artifactId, version };
}

async function directory(value: string, label: string): Promise<string | undefined> {
  if (!path.isAbsolute(value) || value.includes('\0')) return `${label} must be an absolute path on this extension host.`;
  try { if (!(await fs.stat(value)).isDirectory()) return `${label} is not a directory.`; } catch { return `${label} does not exist on this extension host.`; }
  return undefined;
}
export class ProjectManager implements vscode.Disposable {
  private readonly changed = new vscode.EventEmitter<void>();
  readonly onDidChange = this.changed.event;
  private projects = new Map<string, Project>();
  private watchers = new Map<string, vscode.FileSystemWatcher>();
  private selected: string | undefined;
  constructor(private readonly context: vscode.ExtensionContext) { this.selected = context.workspaceState.get<string>(SELECTED_KEY); }
  dispose(): void { for (const watcher of this.watchers.values()) watcher.dispose(); this.changed.dispose(); }
  list(): Project[] { return [...this.projects.values()]; }
  current(): Project | undefined { return this.selected ? this.projects.get(this.selected) : undefined; }
  rootForUri(uri: vscode.Uri): vscode.Uri | undefined {
    if (uri.scheme !== 'file') return undefined;
    const candidates = this.list().filter(p => isInside(p.root, uri.fsPath)).sort((a, b) => b.root.length - a.root.length);
    return candidates[0] ? vscode.Uri.file(candidates[0].root) : undefined;
  }
  projectForUri(uri?: vscode.Uri): Project | undefined { const root = uri ? this.rootForUri(uri)?.fsPath : undefined; return root ? this.projects.get(root) : undefined; }
  nameForUri(uri: vscode.Uri): string { return this.projectForUri(uri)?.name ?? path.basename(path.dirname(uri.fsPath)); }
  getSettings(project: Project): RuntimeSettings {
    return { schemaVersion: 2, runtimeHome: path.join(this.context.extensionUri.fsPath, 'runtime'),
      instanceDirectory: path.join(this.context.globalStorageUri.fsPath, 'instances', RUNTIME_VERSION, createHash('sha256').update(project.root).digest('hex').slice(0, 20)), offline: true };
  }
  async saveSettings(_project: Project, _settings: RuntimeSettings): Promise<void> { this.changed.fire(); }
  async initialize(): Promise<void> {
    const paths = new Set<string>(this.context.globalState.get<string[]>(PROJECTS_KEY, []));
    for (const folder of vscode.workspace.workspaceFolders ?? []) if (folder.uri.scheme === 'file') paths.add(folder.uri.fsPath);
    for (const candidate of paths) { try { await this.load(candidate, false); } catch { /* Non-project and unavailable previous paths stay out of navigation. */ } }
    if (!this.current()) this.selected = this.list()[0]?.root;
    this.changed.fire();
  }
  async load(candidate: string, select = true): Promise<Project> {
    const root = await fs.realpath(candidate);
    const coords = parseProjectPom(await fs.readFile(path.join(root, 'pom.xml'), 'utf8'));
    const descriptor = JSON.parse(await fs.readFile(path.join(root, 'zernova-artifact.json'), 'utf8'));
    if (descriptor.requiredProduct !== 'ZERNOVA') throw new Error('Expected zernova-artifact.json with requiredProduct: ZERNOVA. The old native Runtime format is not supported.');
    if (!(await fs.stat(path.join(root, 'src/main/zernova'))).isDirectory()) throw new Error('Expected src/main/zernova directory.');
    const project = { root, name: path.basename(root), ...coords };
    this.projects.set(root, project);
    if (!this.watchers.has(root)) {
      const watcher = vscode.workspace.createFileSystemWatcher(new vscode.RelativePattern(vscode.Uri.file(root), 'src/main/zernova/*.xml'));
      watcher.onDidCreate(() => this.changed.fire()); watcher.onDidDelete(() => this.changed.fire()); this.watchers.set(root, watcher);
    }
    if (select) { this.selected = root; await this.context.workspaceState.update(SELECTED_KEY, root); }
    await this.context.globalState.update(PROJECTS_KEY, this.list().map(p => p.root)); this.changed.fire();
    return project;
  }
  async select(uri?: vscode.Uri, forcePick = false): Promise<Project | undefined> {
    const contextual = uri ? this.projectForUri(uri) : undefined;
    if (contextual) { this.selected = contextual.root; await this.context.workspaceState.update(SELECTED_KEY, this.selected); this.changed.fire(); return contextual; }
    const active = this.current(); if (active && !forcePick) return active;
    const selected = await vscode.window.showQuickPick(this.list().map(project => ({ label: project.name, description: project.root, project })), { title: 'Select ZERNOVA Project' });
    if (selected) { this.selected = selected.project.root; await this.context.workspaceState.update(SELECTED_KEY, this.selected); this.changed.fire(); }
    else if (!this.list().length) void vscode.window.showInformationMessage('Create or open a ZERNOVA project first.');
    return selected?.project;
  }
  async openProject(): Promise<Project | undefined> {
    const selected = await vscode.window.showOpenDialog({ canSelectFiles: false, canSelectFolders: true, canSelectMany: false, title: 'Open ZERNOVA Project' });
    return selected?.[0] ? this.load(selected[0].fsPath) : undefined;
  }
  async importLegacyProject(): Promise<Project | undefined> {
    return studioForm('Import Legacy XML Project', [
      { key: 'source', label: 'Source project directory', value: '', browse: true },
      { key: 'destination', label: 'New destination directory (must not exist)', value: '' },
    ], async ({ source, destination }) => { await importLegacyProject(source, destination); return this.load(destination); }, 'Creates a separate ZERNOVA project and preserves the original source.');
  }
  async configure(_project: Project): Promise<boolean> {
    void vscode.window.showInformationMessage('Studio uses its bundled ZERNOVA Runtime and Java. No external tools are required for core and HTTP projects.');
    return true;
  }
  getRunConfiguration(project: Project): RunConfiguration {
    return this.context.workspaceState.get<RunConfiguration>('zernova.run.' + project.root) ?? defaultRunConfiguration();
  }
  async configureRun(project: Project): Promise<boolean> {
    const current = this.getRunConfiguration(project);
    return await studioForm('ZERNOVA Run Configuration', [
      { key: 'environment', label: 'Environment', value: current.environment, hint: 'Exposed as the zernova.env system property.' },
      { key: 'variables', label: 'Environment variables', value: Object.entries(current.variables).map(([k,v]) => k + '=' + v).join('\n'), multiline: true, hint: 'One NAME=value per line. Stored in this workspace profile; do not put production secrets here.' },
      { key: 'properties', label: 'Application properties (Maven-style -D)', value: Object.entries(current.properties).map(([k,v]) => '-D' + k + '=' + v).join('\n'), multiline: true, hint: 'One -Dname=value per line. Passed to the runtime JVM and listener preflight. This offline builder does not execute Maven goals.' },
      { key: 'debugPort', label: 'Java debugger port (localhost only)', value: String(current.debugPort) },
    ], async fields => { await this.context.workspaceState.update('zernova.run.' + project.root, parseRunFields(fields)); this.changed.fire(); return true; }) ?? false;
  }
  async createProject(): Promise<Project | undefined> {
    return studioForm('New ZERNOVA Project', [
      { key: 'parent', label: 'Project location', value: vscode.workspace.workspaceFolders?.find(f => f.uri.scheme === 'file')?.uri.fsPath ?? '', browse: true },
      { key: 'name', label: 'Project folder', value: 'zernova-app' },
      { key: 'groupId', label: 'Group ID', value: 'com.zernova' },
      { key: 'artifactId', label: 'Artifact ID', value: 'zernova-app' },
      { key: 'version', label: 'Version', value: '1.0.0' },
      { key: 'integration', label: 'Initial integration XML', value: 'hello.xml' },
    ], async values => {
      const { parent, name, groupId, artifactId, version, integration } = values;
      const error = await directory(parent, 'Project location'); if (error) throw new Error(error);
      if (!artifactPattern.test(name) || name.includes('..')) throw new Error('Use a valid project folder name without parent-path segments.');
      if (!groupPattern.test(groupId) || !artifactPattern.test(artifactId) || artifactId.includes('..') || !versionPattern.test(version)) throw new Error('Enter valid Group ID, Artifact ID and Version coordinates.');
      if (!/^[A-Za-z][A-Za-z0-9_.-]{0,100}\.xml$/.test(integration) || integration.includes('..')) throw new Error('Use an XML filename such as hello.xml.');
      const canonicalParent = await fs.realpath(parent), root = path.join(canonicalParent, name);
      await fs.access(canonicalParent, fs.constants.W_OK); await fs.mkdir(root);
      try {
        for (const dir of ['src/main/zernova', 'src/main/java', 'src/main/resources', 'src/test']) await fs.mkdir(path.join(root, dir), { recursive: true });
        for (const [relative, content] of Object.entries(projectFiles({ root, name, groupId, artifactId, version }))) await fs.writeFile(path.join(root, relative), content, { flag: 'wx' });
        await fs.writeFile(path.join(root, 'src/main/zernova', integration), NEW_INTEGRATION_XML.replace('  <!-- Create a flow in ZERNOVA Studio. This document is initially empty. -->', '  <flow name="helloFlow">\n  </flow>'), { flag: 'wx' });
      } catch (error) { await fs.rm(root, { recursive: true, force: true }); throw error; }
      return this.load(root);
    }, 'Create an integration project using bundled ZERNOVA Runtime and Java. Drag this title bar to move the wizard.');
  }
  async newIntegration(project: Project): Promise<vscode.Uri | undefined> {
    return studioForm('New Integration XML', [{ key: 'filename', label: 'XML filename', value: 'hello.xml' }], async ({ filename }) => {
      if (!/^[A-Za-z][A-Za-z0-9_.-]{0,100}\.xml$/.test(filename) || filename.includes('..')) throw new Error('Use an XML filename such as hello.xml without directories.');
      const directory = await fs.realpath(path.join(project.root, 'src/main/zernova'));
      if (!isInside(project.root, directory)) throw new Error('Source directory resolves outside the project.');
      const uri = vscode.Uri.file(path.join(directory, filename));
      await fs.writeFile(uri.fsPath, NEW_INTEGRATION_XML, { flag: 'wx' }); this.changed.fire(); return uri;
    });
  }
  refresh(): void { this.changed.fire(); }
}

export function projectFiles(p: Project): Record<string, string> {
  return {
    'pom.xml': `<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>${xmlEscape(p.groupId)}</groupId>
  <artifactId>${xmlEscape(p.artifactId)}</artifactId>
  <version>${xmlEscape(p.version)}</version>
  <packaging>zernova-application</packaging>
  <name>${xmlEscape(p.name)}</name>
  <properties><project.build.sourceEncoding>UTF-8</project.build.sourceEncoding><maven.compiler.source>17</maven.compiler.source><maven.compiler.target>17</maven.compiler.target></properties>
  <!-- Built offline by ZERNOVA Studio; no external Maven plugin required. -->
  <dependencies><dependency><groupId>com.zernova.connectors</groupId><artifactId>zernova-http-connector</artifactId><version>1.12.2</version><classifier>zernova-plugin</classifier></dependency></dependencies>
</project>
`,
    'zernova-app.json': JSON.stringify({ schemaVersion: 1, name: p.artifactId, group: p.groupId, version: p.version, dependencies: ['http'] }, null, 2) + '\n',
    'zernova-artifact.json': JSON.stringify({ minZernovaVersion: '0.4.0', requiredProduct: 'ZERNOVA', javaSpecificationVersions: ['17'], redeploymentEnabled: false }, null, 2) + '\n',
    'src/main/resources/application.properties': '# Project resource defaults. Listener configuration is edited in the integration XML.\n',
    'src/main/resources/log4j2.xml': `<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
  <Appenders>
    <RollingFile name="ApplicationFile" fileName="\${sys:zernova.home}/logs/zernova-app-${xmlEscape(p.artifactId)}.log" filePattern="\${sys:zernova.home}/logs/zernova-app-${xmlEscape(p.artifactId)}.%i.log">
      <PatternLayout pattern="%d{ISO8601} %-5p [%t] %c: %m%n"/>
      <Policies><SizeBasedTriggeringPolicy size="10 MB"/></Policies><DefaultRolloverStrategy max="5"/>
    </RollingFile>
  </Appenders>
  <Loggers><AsyncRoot level="INFO"><AppenderRef ref="ApplicationFile"/></AsyncRoot></Loggers>
</Configuration>
`,
    '.gitignore': 'target/\n*.log\n.DS_Store\n',
    'README.md': `# ${p.name}\n\nCreated by ZERNOVA Studio 0.4.1 with bundled Runtime 0.4.1-alpha.1 (4.10 source baseline, Java 17).\n\nCreate Integration XML, create a flow and add an HTTP Listener and Logger. Save and click Run. The offline builder supports core/XML/resources and the bundled HTTP profile; it rejects custom Java and additional dependencies. Runtime state stays outside the project.\n`,
  };
}

/** Bounded conversion into a new directory. The source project is never edited. */
export async function importLegacyProject(sourceInput: string, destination: string): Promise<void> {
  const source = await fs.realpath(sourceInput);
  if (!path.isAbsolute(destination) || destination.includes('\0')) throw new Error('Use an absolute destination directory.');
  await fs.access(destination).then(() => { throw new Error('Import destination already exists.'); }, error => { if (error.code !== 'ENOENT') throw error; });
  const coords = parseProjectPom(await fs.readFile(path.join(source, 'pom.xml'), 'utf8'));
  const descriptor = JSON.parse(await fs.readFile(path.join(source, 'mule-artifact.json'), 'utf8'));
  if (descriptor.requiredProduct !== 'MULE') throw new Error('Expected a legacy Mule application descriptor.');
  const java = path.join(source, 'src/main/java');
  if (await fs.stat(java).then(() => true, () => false)) if ((await walkFiles(java)).length) throw new Error('Import currently supports XML/resources and HTTP only. Custom Java needs a separate migration.');
  const pom = await fs.readFile(path.join(source, 'pom.xml'), 'utf8');
  const parser = sax.parser(true, { xmlns: true }); const stack: string[] = []; const dependencies: Record<string, string>[] = []; let dep: Record<string, string> | undefined;
  parser.ondoctype = () => { throw new Error('POM DTD is not supported.'); };
  parser.onopentag = tag => { stack.push(tag.local); if (stack.join('/') === 'project/dependencies/dependency') dep = {}; };
  parser.ontext = text => { if (dep && stack.length === 4) dep[stack[3]] = (dep[stack[3]] ?? '') + text; };
  parser.onclosetag = () => { if (stack.join('/') === 'project/dependencies/dependency' && dep) { dependencies.push(dep); dep = undefined; } stack.pop(); };
  parser.write(pom).close();
  if (dependencies.some(d => d.artifactId?.trim() !== 'mule-http-connector' || !['org.mule.connectors', 'com.zernova.connectors'].includes(d.groupId?.trim()))) throw new Error('Import supports the bundled HTTP profile only; additional dependencies require migration.');
  const files = new Map<string, string | Buffer>(Object.entries(projectFiles({ root: destination, name: path.basename(destination), ...coords })));
  const converted: string[] = [];
  const integrations = path.join(source, 'src/main/mule');
  for (const name of await walkFiles(integrations)) {
    if (!name.endsWith('.xml')) throw new Error('Unexpected integration source: ' + name);
    files.set('src/main/zernova/' + name, convertLegacyIntegrationXml(await fs.readFile(path.join(integrations, name), 'utf8'))); converted.push(name);
  }
  if (!converted.length) throw new Error('No legacy integration XML found.');
  const resources = path.join(source, 'src/main/resources');
  if (await fs.stat(resources).then(() => true, () => false)) for (const name of await walkFiles(resources)) {
    if (/\.(jar|class|dll|so|dylib)$/i.test(name) || name.startsWith('META-INF/')) throw new Error('Unsupported executable/reserved resource: ' + name);
    const bytes = await fs.readFile(path.join(resources, name));
    files.set('src/main/resources/' + name, name === 'log4j2.xml' ? bytes.toString('utf8').replaceAll('sys:mule.home', 'sys:zernova.home') : bytes);
  }
  files.set('migration-report.json', JSON.stringify({ formatVersion: 1, source, converted, runtime: '0.4.1-alpha.1', dependencyProfile: 'bundled HTTP 1.12.2', notes: ['Maven plugins and tests are not executed or copied.', 'Unsupported XML is preserved; review diagnostics and test behavior.'] }, null, 2) + '\n');
  await fs.mkdir(destination);
  for (const [name, data] of files) { const target = path.join(destination, name); await fs.mkdir(path.dirname(target), { recursive: true }); await fs.writeFile(target, data, { flag: 'wx' }); }
}
