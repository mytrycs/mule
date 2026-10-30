import * as vscode from 'vscode';
import type { Scenario } from '../shared/model';
import * as fs from 'node:fs/promises';
import path from 'node:path';
import { createHash } from 'node:crypto';
import { type Project, ProjectManager } from './projects';
import { RuntimeManager } from './runtime';

interface PreviewItem {
  id: string;
  label: string;
  icon: string;
  description?: string;
  tooltip?: string;
  scenario?: Scenario;
  children?: PreviewItem[];
}
const item = (id: string, label: string, icon: string, children?: PreviewItem[], scenario?: Scenario): PreviewItem => ({ id, label, icon, children, scenario });
const projects: PreviewItem[] = [{
  ...item('project', 'zernova-hello-integration', 'project', [
    item('src', 'src', 'folder', [
      item('main', 'main', 'folder', [
        item('zernova', 'zernova', 'folder', [item('hello.xml', 'hello.xml', 'code', undefined, 'workbench')]),
        item('java', 'java', 'folder'),
        item('resources', 'resources', 'folder', [
          item('mappings', 'mappings', 'folder', [item('hello.dwl', 'hello.dwl', 'symbol-operator', undefined, 'dataweave')]),
          { ...item('properties', 'application.properties', 'settings', undefined, 'workbench'), tooltip: 'Illustrative project resource; no file is generated or executed.' },
        ]),
      ]),
      item('test', 'test', 'folder'),
    ]),
    { ...item('pom', 'pom.xml', 'code', undefined, 'xml'), tooltip: 'Illustrative Maven project resource. Opens the bounded XML preview, not an application build.' },
    { ...item('descriptor', 'zernova-artifact.json', 'json', undefined, 'workbench'), tooltip: 'Illustrative ZERNOVA application descriptor; requiredProduct remains ZERNOVA. No descriptor is written.' },
  ]),
  description: 'Fixture',
  tooltip: 'Synthetic preview project. These tree items are not files in the current workspace.',
}];
const scenarios: PreviewItem[] = [
  item('view-workbench', '01  Workbench', 'layout', undefined, 'workbench'),
  item('view-listener', '02  HTTP Listener', 'globe', undefined, 'listener'),
  item('view-logger', '03  Logger', 'list-unordered', undefined, 'logger'),
  item('view-globals', '04  Global Elements', 'symbol-namespace', undefined, 'globals'),
  item('view-xml', '05  Configuration XML', 'code', undefined, 'xml'),
  item('view-dataweave', '06  DataWeave layout', 'split-horizontal', undefined, 'dataweave'),
  item('view-empty', 'Empty flow', 'new-file', undefined, 'empty'),
];
const runtime: PreviewItem[] = [
  { id: 'not-connected', label: 'No runtime connected', icon: 'debug-disconnect', tooltip: 'Design preview only. This extension does not discover, start or communicate with a runtime.' },
  { id: 'target', label: 'ZERNOVA 0.4.1-alpha.1', icon: 'info', description: 'Reported target' },
  { id: 'kernel', label: 'ZERNOVA Runtime 0.4.1-alpha.1', icon: 'symbol-module', description: 'Future target' },
  { id: 'dataweave', label: 'DataWeave 2.10.0', icon: 'symbol-operator', description: 'Future target' },
];

class PreviewTree implements vscode.TreeDataProvider<PreviewItem> {
  constructor(private readonly roots: PreviewItem[]) {}
  getChildren(element?: PreviewItem): PreviewItem[] { return element ? element.children ?? [] : this.roots; }
  getTreeItem(element: PreviewItem): vscode.TreeItem {
    const result = new vscode.TreeItem(element.label, element.children ? vscode.TreeItemCollapsibleState.Expanded : vscode.TreeItemCollapsibleState.None);
    result.id = `zernovaKernelStudio.${element.id}`;
    result.iconPath = new vscode.ThemeIcon(element.icon);
    result.description = element.description;
    result.tooltip = element.tooltip ?? element.label;
    result.contextValue = 'zernovaKernelStudio.fixture';
    if (element.scenario) result.command = { command: 'zernovaKernelStudio.openScenario', title: 'Open preview view', arguments: [element.scenario] };
    return result;
  }
}

export function registerNavigation(context: vscode.ExtensionContext, manager?: ProjectManager, runtimeManager?: RuntimeManager): void {
  for (const [id, roots] of [
    ['projects', projects], ['previewViews', scenarios], ['runtimeInfo', runtime],
  ] as const) {
    const provider: vscode.TreeDataProvider<PreviewItem | RealItem> = id === 'projects' && manager ? new ProjectTree(manager) : id === 'runtimeInfo' && manager && runtimeManager ? new RuntimeTree(manager, runtimeManager) : new PreviewTree(roots);
    const view = vscode.window.createTreeView(`zernovaKernelStudio.${id}`, { treeDataProvider: provider, showCollapseAll: id === 'projects' });
    context.subscriptions.push(view);
    if ('dispose' in provider) context.subscriptions.push(provider as vscode.Disposable);
  }
}

interface RealItem { id: string; label: string; icon: string; uri?: vscode.Uri; project?: Project; directory?: boolean; command?: string; description?: string; children?: RealItem[] }
class ProjectTree implements vscode.TreeDataProvider<RealItem>, vscode.Disposable {
  private readonly changed = new vscode.EventEmitter<void>(); readonly onDidChangeTreeData = this.changed.event;
  private readonly subscription: vscode.Disposable;
  constructor(private readonly manager: ProjectManager) { this.subscription = manager.onDidChange(() => this.changed.fire()); }
  dispose(): void { this.subscription.dispose(); this.changed.dispose(); }
  async getChildren(element?: RealItem): Promise<RealItem[]> {
    if (!element) return [
      { id: 'new-real-project', label: 'New ZERNOVA Project', icon: 'new-folder', command: 'zernovaKernelStudio.newProject' },
      { id: 'open-real-project', label: 'Open ZERNOVA Project', icon: 'folder-opened', command: 'zernovaKernelStudio.openProject' },
      ...this.manager.list().map(project => ({ id: 'project:' + project.root, label: project.name, icon: 'project', project, uri: vscode.Uri.file(project.root), directory: true, description: 'Real project' })),
    ];
    if (!element.uri || !element.directory) return [];
    let entries: import('node:fs').Dirent[];
    try { entries = await fs.readdir(element.uri.fsPath, { withFileTypes: true }); } catch { return []; }
    return entries.filter(e => !e.name.startsWith('.') && e.name !== 'target' && !e.isSymbolicLink()).sort((a, b) => Number(b.isDirectory()) - Number(a.isDirectory()) || a.name.localeCompare(b.name)).map(e => ({
      id: path.join(element.uri!.fsPath, e.name), label: e.name, icon: e.isDirectory() ? 'folder' : e.name.endsWith('.xml') ? 'code' : e.name.endsWith('.json') ? 'json' : 'file',
      uri: vscode.Uri.file(path.join(element.uri!.fsPath, e.name)), project: element.project, directory: e.isDirectory(),
    }));
  }
  getTreeItem(element: RealItem): vscode.TreeItem {
    const result = new vscode.TreeItem(element.label, element.directory ? vscode.TreeItemCollapsibleState.Expanded : vscode.TreeItemCollapsibleState.None);
    result.id = 'zernovaKernelStudio.real.' + createHash('sha256').update(element.id).digest('hex').slice(0, 20);
    result.iconPath = new vscode.ThemeIcon(element.icon); result.description = element.description;
    result.resourceUri = element.uri; result.tooltip = element.uri?.fsPath ?? element.label;
    result.contextValue = element.uri?.fsPath === element.project?.root ? 'zernovaKernelStudio.project' : 'zernovaKernelStudio.file';
    if (element.command) result.command = { command: element.command, title: element.label };
    else if (element.uri?.fsPath === element.project?.root) result.command = { command: 'zernovaKernelStudio.selectProject', title: 'Select Project', arguments: [element.uri] };
    else if (!element.directory && element.uri) {
      const integration = element.project && path.dirname(element.uri.fsPath) === path.join(element.project.root, 'src/main/zernova') && element.uri.fsPath.endsWith('.xml');
      result.command = { command: integration ? 'zernovaKernelStudio.openIntegration' : 'vscode.open', title: 'Open ' + element.label, arguments: [element.uri] };
    }
    return result;
  }
}
class RuntimeTree implements vscode.TreeDataProvider<RealItem>, vscode.Disposable {
  private readonly changed = new vscode.EventEmitter<void>(); readonly onDidChangeTreeData = this.changed.event;
  private readonly subscriptions: vscode.Disposable[];
  constructor(private readonly projects: ProjectManager, private readonly runtime: RuntimeManager) { this.subscriptions = [projects.onDidChange(() => this.changed.fire()), runtime.onDidChange(() => this.changed.fire())]; }
  dispose(): void { for (const s of this.subscriptions) s.dispose(); this.changed.dispose(); }
  getChildren(): RealItem[] {
    const project = this.projects.current();
    if (!project) return [{ id: 'runtime-none', label: 'Open a real project to configure runtime', icon: 'info' }, { id: 'runtime-preview', label: 'Design Preview is isolated', icon: 'beaker' }];
    const state = this.runtime.state(project);
    return [{ id: 'runtime-project', label: project.name, icon: 'project' }, { id: 'runtime-phase', label: state.phase, icon: state.lastError ? 'error' : 'info' },
      { id: 'runtime-configure', label: 'Bundled Runtime and Java', icon: 'settings-gear', command: 'zernovaKernelStudio.configureRuntime' },
      { id: 'runtime-doctor', label: 'Runtime Diagnostics', icon: 'checklist', command: 'zernovaKernelStudio.diagnostics' },
      { id: 'runtime-status', label: 'Refresh Runtime Status', icon: 'refresh', command: 'zernovaKernelStudio.status' },
      { id: 'runtime-logs', label: 'Show Application Logs', icon: 'output', command: 'zernovaKernelStudio.logs' },
      { id: 'runtime-stop-app', label: 'Stop Application (Undeploy)', icon: 'debug-stop', command: 'zernovaKernelStudio.stopApplication' },
      { id: 'runtime-stop', label: 'Stop Project Runtime', icon: 'stop-circle', command: 'zernovaKernelStudio.stopRuntime' }];
  }
  getTreeItem(element: RealItem): vscode.TreeItem {
    const item = new vscode.TreeItem(element.label); item.id = 'zernovaKernelStudio.' + element.id; item.iconPath = new vscode.ThemeIcon(element.icon);
    if (element.command) item.command = { command: element.command, title: element.label, arguments: this.projects.current() ? [vscode.Uri.file(this.projects.current()!.root)] : [] };
    return item;
  }
}
