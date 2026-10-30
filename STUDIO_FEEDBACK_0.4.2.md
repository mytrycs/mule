# ZERNOVA Studio 0.4.2 — placement, diagnostics and manual save

This Studio-only iteration retains the verified ZERNOVA Runtime 0.4.1-alpha.1,
Java 17.0.21+9 and application format 1. The engine was not rebuilt.

## Changes

- Removed processor insertion placeholders and their plus buttons. Palette clicks
  arm a component. Click valid open space in the appropriate flow lane to insert
  it; drag/drop infers before/between/after positions from component geometry.
  Existing-component dragging accounts for removal before calculating its new
  position. Occupied source positions and incompatible lanes remain invalid.
- Run/build failures are retained with timestamps in Build output and the ZERNOVA
  Build output channel. Console opens on failure and includes a persistent error
  banner. Listener-only flows receive an actionable error before deployment.
- Added **Run Configuration → Redeploy on manual save → Yes/No**, default No.
  The corresponding VS Code resource setting is
  `zernovaKernelStudio.redeployOnSave`. When enabled, explicit Save rebuilds and
  redeploys only if the application is already deployed and build inputs changed.
  Integration Save buttons, the canvas/XML context Save, the native editor
  context command, Cmd/Ctrl+S, and native manual-save events are supported.
  Background XML autosaves and build-internal saves never request redeployment.
  Explicit Save still works after XML has already autosaved. Unchanged saves do
  not restart the JVM; save requests are serialized per project.

Changes are detected from the same source hash used by packaging: all project
`src` files, `pom.xml`, `zernova-app.json` and `zernova-artifact.json`. Generated
`target` output is excluded. The last deployed hash persists across reloads.
Undeployed/stopped applications are not started merely because a file is saved.

Java-file saves are recognized and attempt the build. The existing offline
builder does **not** compile custom Java; it reports this limitation in Console
and retains the previously deployed application. Arbitrary Maven goals/custom
Java support and newer-Java migration are outside this Studio-only iteration.

## Verification

- Typecheck/build, 72 unit/backend/XML tests and three flow-geometry checks passed.
- The final VSIX passed 16 installed checks locally on stable VS Code 1.144.0 in
  a fresh profile, with invalid external Java/Maven paths. These cover native
  XML autosave, opt-in/default-off save behavior, no redeploy on autosave,
  changed-versus-unchanged explicit saves, native property-file manual saves,
  Java-build limitation reporting, retained Listener-only errors, HTTP/Logger,
  replacement, source validation and clean shutdown.
- Native GUI checks on the exact final VSIX verified no placeholders, palette
  click arming, click insertion before the first processor, drag reordering
  between processors, and palette drag after the last processor.
- The new Run Configuration selector defaulted to No and persisted Yes. A Logger
  field change reached disk without restarting the JVM. Actual Cmd+S then
  replaced the JVM; localhost HTTP returned Hello from ZERNOVA and the new
  `UI042_MANUAL_SAVE` Logger message executed.
- A Listener-only Run opened Console with a persistent error. It remained visible
  after context-menu Save. The private test runtime was stopped and its test
  project restored.

Verified Studio code revision and VSIX SHA-256 are in
`verification/studio-0.4.2-devtest/release.json`; installed and native GUI results
are recorded beside it. This patch has local Mac x64 Studio qualification; it
makes no new six-platform UI qualification claim.

The VSIX was installed into the owner's main `/Applications/Visual Studio Code.app`
profile. CLI listing confirmed version 0.4.2. After the existing Stop/Debug session
finished, the window was reloaded and the new canvas/placement help verified.
