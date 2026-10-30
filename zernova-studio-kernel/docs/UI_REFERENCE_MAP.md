# Six-screen UI reference map

## Authority and reference status

The corrected owner brief dated **29 October 2026** is the implementation scope. The supplied six-panel image sets placement, density, hierarchy, and visual direction; it contains generated-text mistakes and legacy navigation that the corrected brief explicitly removes.

The 56-area inventory, `Pasted text(20261029-162541).txt`, was located and read in full. Its opening statement says a complete Anypoint Studio audit has **not** been completed. A copy of the owner-supplied inventory is retained at `evidence/reference/anypoint-audit-inventory.txt`; the numbers below refer to that inventory. They are traceability IDs, not a claim that whole audit areas were implemented or verified.

Public official documentation was consulted on **29 October 2026**. No authorized local Anypoint Studio installation was used for this milestone, and no Studio build-level visual comparison is claimed. Studio documentation is the unversioned public documentation at the URLs below; the HTTP documentation explicitly identifies Connector **1.12**. Actual VS Code version/platform and installed-host tests are reported separately with implementation evidence.

## Six bounded views

| View and required screenshot | Reference placement and interactions | Inventory areas | Primary reference | Deliberate scope boundary |
| --- | --- | --- | --- | --- |
| 01 Workbench / `01-workbench-light.png` | Native left project navigation; center `helloFlow`; compact Listener and Logger; right palette; empty lower Properties state; flow tabs | 4 Workbench, 7 Explorer, 11 Palette, 14 Canvas, 15 Movement, 16 Selection, 17 Context menus | [Studio workbench](https://docs.mulesoft.com/studio/) | One synthetic project and bounded fixture editing, not project generation or arbitrary Mule editing |
| 02 HTTP Listener / `02-http-listener-dark.png` | Selected source; lower General sheet; shared config editor; input/output metadata fixtures | 13 Connector forms, 21 Properties, 22 Globals, 24 Sources, 26 Metadata | [HTTP in Studio 1.12](https://docs.mulesoft.com/http-connector/latest/http-connector-studio), [Listener source](https://docs.mulesoft.com/http-connector/latest/http-listener-ref) | Local form validation only; no HTTP listener, live metadata, TLS, or connection testing |
| 03 Logger / `03-logger-light.png` | Selected processor; Display Name, Message expression affordance, Level, Category, Notes; below-canvas editing | 14 Canvas, 16 Selection, 18 Core catalog, 21 Properties | [Logger reference](https://docs.mulesoft.com/mule-runtime/4.3/logger-component-reference), [Studio workbench](https://docs.mulesoft.com/studio/) | Logger sample state only; no emitted runtime log or expression evaluation |
| 04 Global Elements / `04-global-elements-light.png` | Searchable Type/Name/Description table; Create/Edit/Delete; shared bounded HTTP configuration dialog; Save/Cancel | 21 Properties, 22 Global Elements, 23 Connections | [Global elements](https://docs.mulesoft.com/mule-runtime/latest/global-elements), [HTTP in Studio](https://docs.mulesoft.com/http-connector/latest/http-connector-studio) | Reference-safe preview CRUD; TLS/Advanced structure reserved; Test Connection disabled |
| 05 Configuration XML / `05-configuration-xml-dark.png` | Same editor, embedded read-only XML with line numbers, scroll, selection, copy; persistent flow tabs | 31 XML/document synchronization, 55 VS Code coexistence | [Studio editor tabs](https://docs.mulesoft.com/studio/), [Mule configuration](https://docs.mulesoft.com/mule-runtime/latest/about-mule-configuration) | Bounded fixture serializer only; no arbitrary XML parse/round trip or real file write |
| 06 DataWeave / `06-dataweave-future-layout-dark.png` | Graphics, Source with Trees, Source Only; synthetic metadata, mapping lines, source and sample output; resizable panes | 26 Metadata, 28 DataWeave editing, 29 Graphical editor, 30 Outputs/layout | [Transform Message modes](https://docs.mulesoft.com/studio/transform-message-component-concept-studio) | Read-only future-layout fixture; no engine, metadata inference, compiler, or Enterprise component |

The inherited inventory refers to complete connector coverage, execution, metadata propagation, debugging, MUnit, import/export, enterprise/platform workflows, and full project round trips. Those broader cases remain **deferred**. Passing a bounded UI case does not qualify its entire inventory area.

## Cross-view phase matrix

| Phase requirement | Inventory link | Intended bounded evidence |
| --- | --- | --- |
| One new extension, command namespace, Activity Bar contribution, isolated profile | 2 Installation; 55 Coexistence | Manifest/package inspection and actual installed-host open/reset/reload checks |
| Light/dark surfaces, sizing, pane resize, layout reset | 4 Workbench; 51 Preferences; 52 Accessibility | Six views in both themes, 1920×1080 / 1440×900 / 1280×800, enlarged text and high contrast |
| Palette search, empty result, permitted inserts, invalid placement rejection | 11 Palette; 14 Canvas; 15 Movement | Real interactions against shared fixture state; keyboard equivalent to supported movement |
| Selection, duplicate/delete/reorder, undo/redo | 16 Selection; 17 Context menus | Stable IDs, selected-node persistence, independent field values, one-step undo/redo |
| Component form edits and globals Save/Cancel | 21 Properties; 22 Globals | Local validation, draft discard, same dialog from both entry points, reference warning |
| Metadata expansion and input/output switching | 26 Metadata | Tree interaction only, clearly illustrative |
| XML derives from fixture, no duplicate native editor | 31 XML; 55 Coexistence | Same-panel navigation, valid escaping, host tab count and unchanged project files |
| DataWeave mode transitions and explicit illustrative output | 29 Graphical editor; 30 Samples/layout | Fixture mode/state/layout checks, no evaluation claim |
| Persistence, reset, malformed state recovery, reopen/reload | 51 Preferences; 54 Data integrity | Namespaced versioned preview state only; no global settings mutation |
| Keyboard focus, modal containment/restoration, Escape | 52 Accessibility | Keyboard interaction evidence and explicit accessible controls |
| No runtime, network, downloader, credentials or application build | 23 Connections; 32 Build; 33 Runtime; 34 Lifecycle; 55 Host safety | Static boundary inspection plus observed negative checks in available host |

Actual outcomes are recorded in [UI_SKELETON_SCOPE_AND_GAPS.md](UI_SKELETON_SCOPE_AND_GAPS.md) and [`evidence/latest-ui.json`](../evidence/latest-ui.json): the installed VS Code desktop suite passed 73 checks with 0 failures on Linux x86-64. All six views were exercised in light/dark and both native high-contrast themes. The 24 axe scans reported zero violations while retaining 8 states with incomplete/manual-review contrast items. The earlier lane ARIA-role finding was corrected and is absent from the final run. This table defines bounded traceability; it does not qualify entire audit areas. Manual screen-reader testing, an actual Mac/Windows host, and a proprietary Studio installation remain NOT RUN.

## Documented differences from the concept and Studio

1. **Native host chrome.** VS Code supplies the outer workbench. No second fake title bar, Activity Bar, or operating-system frame is drawn in the preview. Native UI varies by VS Code version and platform.
2. **One supported webview editor.** The integration canvas, palette, lower work area, global dialog, XML, and DataWeave layout stay inside a single dedicated design preview. It is not a modified Eclipse workbench or a default XML editor association.
3. **Focused navigation.** The concept's API Flow, Jira, Git & CI/CD, AI & MCP, deployment and broad platform sections are omitted as expressly required. Projects and design-review scenarios remain.
4. **Corrected labels and source.** `Protocol` is spelled correctly; XML uses genuine Mule namespaces and valid escaping; the configured base path starts empty as required by the corrected brief, even though the concept image suggests `/api`.
5. **Allowed methods placement.** The bounded preview puts Allowed methods in General because the corrected brief requires it there. The consulted HTTP Listener documentation demonstrates it in Studio's Advanced section. No claim of exact version-specific form parity is made.
6. **Truthful lower surfaces.** Problems and Console are explicitly preview/empty surfaces. A future native `DiagnosticCollection` and `OutputChannel` mapping can be evaluated when real diagnostics exist; this milestone does not manufacture runtime results.
7. **DataWeave layout choice.** Official documented Graphics mode includes mapping lines; Source with Trees omits those lines; Source Only retains an input/source-oriented structure. The implemented preview uses a fixed input/output mapping diagram plus source/sample in Graphics; input/output trees plus source/sample in Source with Trees; and input tree plus source/sample in Source Only. Keeping the illustrative sample pane in every mode is an explicit design-review choice. All source/mapping/output is read-only and illustrative; the concept's inconsistent labeling does not authorize a real Transform Message implementation or establish exact proprietary editor parity.
8. **Brand evidence.** The existing ZERNOVA Z geometry is reused. The website currently uses green/lime; Studio teal is a provisional choice following the supplied concept, documented in `BRAND_TOKEN_AUDIT.md`.
9. **Bounded customization.** Splitters and preview controls resize internal panes. Native arbitrary docking/detaching and Eclipse perspective behavior are not promised by this supported VS Code architecture.

## VS Code primary references

- [Extension capabilities overview](https://code.visualstudio.com/api/extension-capabilities/overview)
- [Extending the workbench](https://code.visualstudio.com/api/extension-capabilities/extending-workbench)
- [Webview UX](https://code.visualstudio.com/api/ux-guidelines/webviews)
- [Webview API, resources, theme integration, lifecycle and security](https://code.visualstudio.com/api/extension-guides/webview)
- [Theme color reference](https://code.visualstudio.com/api/references/theme-color)

These references guide the supported API boundary. Third-party reference artwork and implementation code are not included in the ZERNOVA UI.
