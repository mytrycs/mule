# Studio 0.6.0 local qualification

Implementation commit: 63773615b8. Target: Mac x64. Engine 0.4.1-alpha.1,
managed core support 0.5.0 and bundled patched Java 17 are unchanged.

- TypeScript check passed; 88 unit tests passed.
- All-core native acceptance: 23/23 checks passed. One application covers all
  catalog components, 21 flows/subflows and 116 stable component IDs.
- Final installed VSIX in isolated VS Code 1.144.0: 23/23 passed, including
  artifact inventory (9 physical bundled Maven JARs), inline New XML cancellation,
  actual HTTP/Logger deployment, save/redeploy, external transforms, environment
  properties, nested scopes, embedded debugging, and the shipped all-core app's
  transform outputs and asynchronous Batch completion. Final shutdown passed.
- Manual installed UI: shared 21-flow canvas; append two flows and a subflow;
  draggable inline XML creation and separate tab; empty For Each insert/delete;
  distinct icons; project/file/dependency menus; add/remove dependency declaration
  with explicit unavailable status and a real offline build failure. The final
  VSIX also passed shared-canvas/name, artifact ordering, inline creation and POM
  context-menu checks. See manual-ui.json for preliminary/final distinction.
- Normal Default VS Code profile updated. Installed code/webview/runtime manifest
  and package metadata match the final VSIX. Existing owner project stayed stopped;
  all eight non-build files remain byte-for-byte unchanged.

VSIX identity is in vsix.json. Raw results/logs are alongside this file. The handoff
folder includes the VSIX, All_Core_App, its ZIP, test guide and known limitations.
Other Maven coordinates can be declared and inspected but cannot be resolved or
built by the current offline profile. This is not full Eclipse/Maven parity.
Rename/move changes paths and does not rewrite semantic XML/resource references.
Qualification here is local Mac x64, not installed-Studio testing on six targets.

During native sample qualification, the fixture used reserved app.name and a Java
map response without JSON serialization. The sample now uses example.name and an
explicit JSON DataWeave error body. The final 23 native checks passed. No engine
change was required for those fixture corrections.
