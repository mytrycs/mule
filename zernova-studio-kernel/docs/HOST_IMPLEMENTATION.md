> Historical Phase 2A / 0.1.0 document. The approved UI continues in 0.2.0; current execution scope and owner instructions are in [Quickstart](QUICKSTART.md), [Capabilities](CAPABILITIES.md), and [TESTING_STEP_BY_STEP.md](../TESTING_STEP_BY_STEP.md).

# Supported VS Code host implementation

## New native surface

The manifest contributes one `zernovaKernelStudio` Activity Bar container and
three native Tree Views beneath it: Projects, Design Preview and Target Runtime.
The fixture explorer is implemented with `TreeDataProvider`; it does not scan
the user's workspace or register a filesystem provider. Ordinary XML files
retain their usual editor behavior. There is no custom-editor association,
language override, task provider, debug adapter, authentication provider, or
extension dependency on the previous Studio.

Four commands are registered by this extension:
`zernovaKernelStudio.openPreview`, `openScenario`, `resetPreview`, and
`resetLayout`. VS Code additionally creates its normal focus/open/move/hide
commands for the native Tree Views; those are host navigation, not integration
execution.

A singleton `WebviewPanel` owns the integration-specific UI. Message Flow,
Global Elements, Configuration XML and the DataWeave layout all share this
panel. The native sidebar can queue a scenario before the webview is ready;
the host dispatches it only after the ready handshake. Repeated invocations
reveal the existing panel. Closing it disposes message listeners; a registered
serializer restores one panel after host reload.

The native title bar, command center, editor tab, Activity Bar, window controls
and status bar remain VS Code surfaces. The extension never patches workbench
internals or applies CSS to the native host. Optional Light and Dark themes
are contributions a user must explicitly select.

## Security and persistence boundary

The webview receives only locally bundled CSS, JavaScript and brand assets.
Its resource roots contain `media/` and `assets/` only. The CSP blocks network
connections, frames, objects and form submission. Executable scripts require
a fresh nonce. Inline **styles** are permitted for the UI's dynamic pane sizes;
inline executable event handlers are not. JSON embedded into the nonce-bound
initialization script escapes angle brackets, ampersands and line separators.

The host accepts exactly four message types: ready, saveState, resetPreview and
resetLayout. Unknown fields, oversized payloads, malformed values, unsupported
schema versions and runtime-like command messages are rejected by the shared
protocol validator. No arbitrary command, path, filesystem, process or network
bridge exists. The only Node built-in used by production host source is crypto
for the CSP nonce.

Preview state is reconstructed from allowed fields and saved serially under
`zernovaKernelStudio.preview.v1` in the extension's `workspaceState`. The
extension does not update global settings, workspace configuration files,
real application documents or runtime profiles. The exported diagnostics API
used by the acceptance harness is read-only; it exposes identity, panel counts,
ready status, rejected-message count and a copied fixture snapshot.

## Actual-host test method

The subject is packaged into its own VSIX, installed with the native VS Code
CLI into a fresh extension directory, and then activated in the real desktop
host. A separate small development harness performs assertions via the public
VS Code API. The subject itself is **not** loaded through
`extensionDevelopmentPath`.

For the two-process persistence check, that harness activates normally on
`onStartupFinished` and quits its private host gracefully after assertions.
It deliberately does not use `--extensionTestsPath`: this host's automated
extension-test mode uses in-memory workspace storage, which cannot establish
persistence across separate processes. The development-only runner records
that calibration failure and then tests the ordinary disk-backed profile.

`scripts/test-host.mjs` accepts an existing desktop executable in
`ZERNOVA_VSCODE_BIN`. It creates separate user-data, extensions, shared-data and
fixture-workspace directories. Settings that disable telemetry/update checks
and select the test theme are confined to that temporary test profile. No host
is downloaded by the installed extension or by opening its preview.

During this task, the official Linux x64 desktop archive was downloaded as
development tooling and verified against the SHA-256 in Microsoft's release
metadata. An extracted Ubuntu virtual display and two supporting libraries were
used without installing system packages. The virtual display's sole local
helper-directory adaptation is recorded in `evidence/display-provisioning.json`;
these development tools are not part of the source/VSIX deliverables. See
`evidence/host-provisioning.json` and `evidence/vscode-release.json` for identity.

The observed host is VS Code **1.144.0**, commit
`2c33b50b16c472efe58357d1d2a8a5414bc23fbf`, Linux x64 on Ubuntu 24.04.3 LTS.
The executable reports Electron **43.8.3**, Chromium **150.0.7871.250** and
Node **24.22.1**. These are this test environment's observed versions, not
extension runtime prerequisites. macOS, Apple Silicon and Windows are **NOT RUN**.

VS Code 1.144 supports an application-shared storage directory in addition to
user data. `--shared-data-dir` is therefore passed explicitly in the review
and test commands. The test host runs with background networking disabled;
this does not convert generic upstream host diagnostics into extension activity.
Any negative network/process claims are scoped to the extension's closed
capability boundary and recorded UI tests, not every internal VS Code service.

## Official primary references consulted

Retrieved 29 October 2026:

- [Webview API](https://code.visualstudio.com/api/extension-guides/webview):
  local resources, message passing, lifecycle, CSP and persistence.
- [Contribution Points](https://code.visualstudio.com/api/references/contribution-points):
  commands, views, Activity Bar containers and optional themes.
- [Testing extensions](https://code.visualstudio.com/api/working-with-extensions/testing-extension):
  desktop extension-host test architecture. The initial retrieval timed out;
  the pinned official `@vscode/test-electron` package supplies CLI resolution;
  the final cold-restart harness uses ordinary activation as described above.
- [Official release metadata](https://update.code.visualstudio.com/api/update/linux-x64/stable/latest):
  exact test-host release identity and checksum, retained in evidence.

Dependency licenses and source locations are documented separately in
`DEPENDENCY_LICENSES.md`. None of these references implies complete Studio
workflow parity or a proprietary Studio implementation dependency.
