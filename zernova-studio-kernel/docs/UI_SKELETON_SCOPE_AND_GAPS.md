> Historical Phase 2A / 0.1.0 document. The approved UI continues in 0.2.0; current execution scope and owner instructions are in [Quickstart](QUICKSTART.md), [Capabilities](CAPABILITIES.md), and [TESTING_STEP_BY_STEP.md](../TESTING_STEP_BY_STEP.md).

# UI skeleton: implemented boundary, fixtures, gaps and verification

## Milestone and evidence vocabulary

This is the first, separate UI design milestone for `zernova-preview.zernova-studio-kernel-preview` **0.1.0**. It exclusively targets the new Mule-Kernel-based ZERNOVA Runtime for future integration. No old Studio or native-runtime dependency is present by design.

The prototype has one shared versioned preview model. It is not six independent screenshot pages and is not a deployable application format. The six-screen reference is reproduced through selection, tabs, sample forms, and future-layout navigation inside one supported VS Code webview editor, surrounded by genuine native host contributions.

Use these classifications when reading the evidence:

- **UI implemented and tested:** an actual test result is cited for the bounded interaction. This never means its underlying connector/runtime function exists.
- **UI implemented but not tested:** code is present, but the required interaction/platform has not been exercised in the reported evidence.
- **Fixture only:** synthetic project resources, metadata, transformation source/output, diagram paths, and runtime target information.
- **Deferred functionality:** real integration execution and every capability expressly excluded from this milestone.
- **Blocked / NOT RUN:** unavailable environments or reference comparisons, with no inferred pass.

The actual installed preview passed **73 UI checks, 0 failures** in run `2026-10-29T18-43-46-916Z`. The suite exercised **VS Code desktop 1.144.0 on Ubuntu 24.04.3 LTS, Linux x86-64**, using isolated user-data and extension directories. These are installed-host results, not a standalone browser simulation. The authoritative pointer is [`evidence/latest-ui.json`](../evidence/latest-ui.json), with the individual cases in [`results.json`](../evidence/ui-runs/2026-10-29T18-43-46-916Z/results.json).

The shared model/protocol suite passed **25 tests, 0 failures**; an independent Python XML parser accepted **3 escaping/round-trip fixtures** with no injected elements. The source/bundle/VSIX boundary audit recorded **19 checks, 0 failures**. The separate extension-host installation suite and final clean-build/reinstall evidence are retained alongside the UI results; their final observed counts should be read from those files, rather than inferred from the 73 UI cases.

| Evidence | Observed result |
| --- | --- |
| [Installed UI suite](../evidence/ui-runs/2026-10-29T18-43-46-916Z/results.json) | 73 PASS / 0 FAIL |
| [Theme/accessibility scans](../evidence/ui-runs/2026-10-29T18-43-46-916Z/accessibility.json) | 24 scans, zero reported violations; manual-review/incomplete items retained |
| [Model/protocol test output](../evidence/model/unit-test-output.txt) | 25 PASS / 0 FAIL |
| [Independent XML parser](../evidence/model/xml-parser-results.json) | 3 cases PASS |
| [Source/bundle/VSIX boundary audit](../evidence/static-boundaries.json) | 19 PASS / 0 FAIL |
| [Installed extension-host results](../evidence/extension-host-results.json) | Actual installed VSIX identity, native behavior, persistence and settings assertions; consult final result count |
| [Sibling integrity comparison](../evidence/sibling-isolation-results.json) | Available runtime/evaluation sibling contents unchanged across the recorded test interval |

Evidence files are part of the companion source/evidence delivery; development-only evidence is excluded from the small installed VSIX.

## Six-screen scope matrix

| Screen | Real UI behavior within the bounded preview | Fixture content | Deferred functionality | Audit area links |
| --- | --- | --- | --- | --- |
| Workbench | **UI implemented and tested:** native project-tree navigation; preview scenarios; tab switching; palette filtering; component selection; flow/error expansion; zoom; pane resizing | `zernova-hello-integration` tree, `helloFlow`, Listener/Logger, response path | Project generation/import, application build, arbitrary canvas/connector catalog, actual error handlers | 4, 7, 11, 14, 15, 16, 17 |
| HTTP Listener | **UI implemented and tested:** lower Properties sheet; section navigation; editable display name/path/method fields; local validation; config selection and shared dialog | Payload/Attributes/Variables trees; reserved MIME/Redelivery/Responses/Metadata structure | HTTP server, connector execution, live metadata, runtime validation, source lifecycle | 13, 21, 22, 24, 26 |
| Logger | **UI implemented and tested:** lower Properties sheet; editable display name/message/level/category; expression presentation; notes; selection preserved across bounded edits | Initial `#["Request received"]` message and illustrative metadata | Logging, DataWeave expression evaluation, runtime console output | 14, 16, 18, 21 |
| Global Elements | **UI implemented and tested:** search and row selection; shared configuration editor; Save/Cancel drafts; required/invalid-value feedback; reference-safe delete handling | HTTP Listener config and reserved TLS/Advanced structures | Connections, certificates, security, pooling, dynamic connector configuration | 21, 22, 23 |
| Configuration XML | **UI implemented and tested:** embedded read-only highlighted XML with line numbers, selection, scrolling/copy; bounded fixture serializer updates from shared state | Mule-compatible sample XML marked not saved | Arbitrary parsing/round trip, completion, compiler, dirty-file history, application file writes | 31, 55 |
| DataWeave future layout | **UI implemented and tested:** Graphics / Source with Trees / Source Only navigation; metadata-tree expansion; split panes | Read-only firstName/lastName → fullName source, mapping lines, sample output | DataWeave engine, mapping compilation, code synchronization, metadata inference, real preview evaluation | 26, 28, 29, 30 |

The DataWeave source, mapping and sample output remain explicitly read-only and illustrative. Its visible `Future layout — design only` and `Illustrative — not executed` labels were tested. Graphics shows the fixed mapping illustration, source, and sample output; Source with Trees shows input/output trees, source, and sample; Source Only shows the illustrative input tree, source, and sample. Retaining sample output in all modes is an explicit review-layout choice, not a claim of exact proprietary editor parity. The HTTP Request palette item is a future item, not an installed or working connector.

## Cross-screen engineering matrix

| Requirement | Implementation boundary | Actual bounded verification |
| --- | --- | --- |
| Separate project/identity/version | New directory, publisher/name, initial 0.1.0 history and `zernovaKernelStudio.*` namespace | **PASS:** manifest/bundle/package boundary checks and installed identity |
| One native Activity Bar contribution | One contribution from this extension; other products are not removed | **PASS:** manifest and native host assertion |
| One coherent preview editor | Message Flow, Global Elements, XML and DataWeave views share a panel; repeated open reveals it | **PASS:** open, immediate close/reopen and Reload Window cases |
| Bounded movement | Listener only in source lane; Logger only in processor lane; second source rejected; Logger reorder/duplicate/delete | **PASS:** actual pointer drag, invalid-target drag, keyboard insertion/reorder and model assertions |
| Undo/redo | Preview edits only; stable component IDs and sample values retained | **PASS:** one-step restoration of the exact edited Logger; no native document history claim |
| Save/Cancel | HTTP configuration drafts save to preview state or discard; referenced delete cannot silently dangle | **PASS:** valid Save, invalid draft rejection, Cancel discard, referenced-delete warning and unreferenced-delete undo |
| Persistence/reset | Namespaced schema-versioned VS Code workspace preview state; explicit Reset Preview and Reset Layout | **PASS:** immediate close/reopen, window reload, reset/Cancel and malformed-state tests |
| Light/dark/high contrast | Webview semantic tokens and opt-in native themes; no silent global theme change | **PASS:** six views in light/dark plus six in each native high-contrast theme |
| Responsive layout | Internal scrolling/resizing with minimum pane sizes | **PASS:** native 1920×1080, 1440×900, 1280×800, enlarged native text/zoom, keyboard and pointer splitters |
| Accessibility | Names, labels, focus outlines, keyboard movement, splitters, modal containment/restoration and Escape | **PASS for tested keyboard interactions and zero reported axe violations; manual review still required** |
| Host safety | Validated messages, restrictive CSP, constrained local resources, listener disposal; no execution bridge | **PASS for scoped source/bundle/VSIX audit and negative UI checks**; not a formal security proof |
| Offline UI | Locally bundled scripts/styles/assets; no runtime/connector download | **PASS:** UI opened without Java/Maven/runtime prerequisites; 9 observed local-resource requests, 0 unexpected webview requests |
| Ordinary XML coexistence | No default/custom editor association for all XML | **PASS:** manifest and installed-host ordinary XML behavior; embedded XML creates no second text document |
| Clean source build | Exact direct pins plus lockfile; documented Node/npm prerequisites | Separate final clean-build/repackage result is reported with host packaging evidence; not inferred from the UI test count |

## Accessibility evidence and its limits

The UI suite ran **24 axe-core scans**: all six views in light, dark, native Dark High Contrast, and native Light High Contrast. All returned zero reported violations. The initial calibration had genuine light-theme contrast failures; those are retained in `evidence/accessibility-calibration.json`, and the final stylesheet uses a darker faint-text token. The final passing run supersedes that calibration for the delivered UI.

The final automated scans returned **incomplete/manual-review items in 8 of the 24 states**, all `color-contrast` items in Logger/XML states. Detailed review identified partially obscured/clipped text or form content for which axe could not determine the background; these items are retained in `accessibility.json` and `evidence/accessibility-manual-review-code-and-form.json`. That detailed review file predates the final role correction and also preserves the historical ARIA finding. The source and processor lanes were corrected to use `role="group"`; the final run contains no `aria-prohibited-attr` items. Remaining incomplete items are not silently converted into passing human assessments. Keyboard focus, modal containment/Escape/restoration, insertion/reorder and pane resizing were exercised independently. **Manual screen-reader testing is NOT RUN**, and no full accessibility certification is claimed.

The enlarged-text screenshot uses a 1280×800 native window with host zoom applied; its reported CSS viewport is correspondingly smaller. The screenshots preserve the entire actual host. Sizing results are bounded to the tested UI states and sizes, not every possible monitor or display scaling configuration.

The request observation assertion covers the preview's webview only. Native VS Code background traffic is outside that network observation; the production source/bundle audit separately checks that this extension exposes no runtime/network/process bridge.

## Mandatory truthful states

`DESIGN PREVIEW` and `No runtime connected` remain visible. A sample target version is not a discovered installation. Problems/Console contain labeled preview or empty states, not fabricated live diagnostics. Disabled Run, Debug, Deploy, Test and Test Connection actions must explain their deferred availability and cannot dispatch runtime or network work.

Sample project resources are representative tree entries, not proof of generated project files. The XML view says `Preview XML — not saved to a project`. Preview saving changes only namespaced UI state. No Java, Maven, Kernel, executable connector, proprietary Studio tooling, old native runtime or ZEL engine is bundled.

## Known gaps and unavailable comparisons

| Area | Status and implication |
| --- | --- |
| Complete Anypoint Studio audit/parity | **Not completed.** The supplied inventory explicitly says this. Only its relevant bounded areas are traced here. |
| Proprietary Anypoint Studio build comparison | **NOT RUN.** Official public documentation was consulted; no local Studio build was inspected. |
| Real HTTP/Logger/DataWeave behavior | **Deferred by scope.** UI success is not execution success. |
| Runtime integration on Mac/Apple Silicon/Windows | **NOT RUN here.** The supplied Phase 1 runtime evidence covers Linux x86-64 only. |
| UI on macOS/Apple Silicon/Windows and other untested hosts | **NOT RUN.** Actual testing used VS Code 1.144.0 on Linux x86-64. A Linux test does not prove Mac/Windows support. |
| Manual assistive-technology testing | **NOT RUN.** Automated accessibility/keyboard checks do not imply screen-reader acceptance; incomplete axe items remain recorded for human review. |
| Broad connector/schema forms and metadata | **Deferred.** HTTP Listener and Logger are hand-bounded UI fixtures, not a generalized SDK form system. |
| Production XML round trip and DataWeave tooling | **Deferred.** The bounded serializer and read-only transformation layout do not implement those capabilities. |
| Performance/soak/security certification | **Not claimed.** Small bounded tests are not complete audits or production qualification. |
| Runtime independent rebuild / redistribution | **BLOCKED in supplied Phase 1 handoff.** This UI-only VSIX neither retests nor clears them. |

## Reference coverage and deviations

The original 56-area inventory was located and read completely (655 lines) on 29 October 2026; its disclaimer is retained. The supplied image is separately labeled reference artwork. The six required screenshots and narrow/focus/validation/zoom/high-contrast examples were captured from the running installed implementation and retain the native VS Code host plus disconnected indicators. They are in `evidence/screenshots/`.

The available `mule-kernel-evaluation` and `zernova-runtime-foundation-phase1` siblings had identical file/symlink inventories and hashes before and after the recorded host-test interval. No earlier Studio/native-runtime source directory was present in the initial top-level inventory. This is evidence of source/import/package isolation and unchanged available siblings across that interval; it is not a historical audit of inaccessible old repositories or the owner's primary installations.

[UI_REFERENCE_MAP.md](UI_REFERENCE_MAP.md) records exact audit area numbers, the official sources and bounded differences: supported VS Code chrome, focused navigation, corrected labels, General placement of Allowed methods per this brief, and preview-only lower surfaces. [BRAND_TOKEN_AUDIT.md](BRAND_TOKEN_AUDIT.md) distinguishes the website's verified green/lime palette from the provisional teal Studio treatment.

## Stop gate

Deliver the development VSIX, source, actual screenshots and test evidence, then stop for design approval. A subsequent explicitly authorized assignment may implement the smallest real HTTP Listener + Logger workflow against the new runtime contract. This milestone does not activate that integration automatically.
