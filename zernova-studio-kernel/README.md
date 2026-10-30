# ZERNOVA Studio

**Mule-Kernel Studio · 0.2.0 · HTTP Listener → Logger milestone**

This release continues the approved ZERNOVA Studio interface and extension
identity, `zernova-preview.zernova-studio-kernel-preview`. Its commands, views,
and storage remain under `zernovaKernelStudio.*`. It does not depend on the
older native Studio or native Runtime.

The real-project editor creates Maven/XML application source, synchronizes
visual properties with the same VS Code text document, and builds/deploys that
application through the existing ZERNOVA Runtime Foundation contract. It uses
the Runtime's HTTP connector and core Logger. The separately labeled Design
Preview remains an isolated fixture workbench.

## Start here

Read `TESTING_STEP_BY_STEP.md` (TESTING_STEP_BY_STEP.md) for the complete owner
procedure: prerequisites, VSIX installation, exact wizard fields, Listener and
Logger values, real requests, matching logs, redeployment, failures, and cleanup.
The delivered HTML edition embeds actual installed-editor screenshots.

The verified execution arrangement is **VS Code desktop, extension host,
workspace, Java/Maven, Runtime, and the HTTP test request on the same Linux
x86-64 machine**. Native macOS/Apple Silicon and Windows runtime execution are
not verified. A Mac-to-Linux Remote-SSH route is documented as **NOT RUN**.

1. Prepare ZERNOVA Runtime `0.1.0-preview.1`, Java 17, Maven 3.9.11, and the
   application dependency cache on Linux.
2. Install `dist/zernova-studio-kernel-preview-0.2.0.vsix` in an isolated VS Code
   profile. Open and trust your own test workspace.
3. Run **ZERNOVA: New ZERNOVA Project**, then **ZERNOVA: New Integration XML**.
   The new project's `src/main/mule` directory starts empty.
4. Create a flow, configure a loopback HTTP Listener and core Logger, and Save.
5. Use **Build**, then **Run / Redeploy** in the editor's Console. Send the guide's
   `curl` request and use **Refresh logs** to find its unique request identifier.
6. Use **Undeploy**, then **Stop runtime** to clean up the private instance.

## Release documentation

- `docs/QUICKSTART.md` (Quickstart)
- `TESTING_STEP_BY_STEP.md` (Full owner testing guide)
- `docs/CAPABILITIES.md` (Implemented and preview capabilities)
- `COMPATIBILITY.md` (Compatibility matrix)
- `KNOWN_LIMITATIONS.md` (Known limitations)
- `docs/RUNTIME_ADAPTER_CONTRACT.md` (Runtime adapter command and JSON contract)
- `CHANGELOG.md` (Changelog)
- `THIRD_PARTY_NOTICES.md` (Third-party notices)

The Phase 2A documents under `docs/UI_SKELETON_*`, `NEW_STUDIO_SCOPE.md`,
`HOST_IMPLEMENTATION.md`, `FUTURE_RUNTIME_BOUNDARY.md`, and
`RUNTIME_AGENT_HANDOFF.md` record the previous UI-only milestone. Their old
execution exclusions are historical. This README and the Phase 2B documents
define the current release.

## Build the extension from source

Use Node.js 22.14 or newer and npm. The delivered source and lockfile pin the
dependencies. These commands build the extension, not an integration app:

```bash
npm ci
npm run typecheck
npm test
npm run build
npm run package
node tests/static-boundaries.mjs
```

`npm ci` requires a registry connection or an already prepared npm cache. The
installed webview uses local resources. Real applications are built with the
project's selected Maven installation/cache; no first-time offline download is
promised. The VSIX contains no JDK, Maven, Kernel, or connector executable.

## Verification and replay

Current release evidence lives under `evidence/phase2b/`, with separate fresh
model/host/static reports. Machine-readable reports bind installed tests to
the tested VSIX SHA-256. Historical reports are not counted as new results.

For installed preview host regression, provide an existing VS Code desktop
binary and Linux display:

```bash
ZERNOVA_VSCODE_BIN="/absolute/path/VSCode-linux-x64/code" \
  DISPLAY=:99 npm run test:host
```

The runner installs the subject VSIX in its own temporary profile; a separate
test-only harness observes the public VS Code API. It does not replace the
installed subject with a development-path extension. Real owner-workflow
automation uses `scripts/phase2b-desktop.mjs` and
`scripts/test-phase2b-ui.mjs`; see the testing guide for exact replay setup.

## Release boundaries

Functional readiness, independent Kernel source-build reproducibility, and
customer redistribution are distinct. This milestone supplies the functional
Listener → Logger development workflow for evaluation. It does not clear the
Runtime Foundation's independent source-build or redistribution gates, publish
to Marketplace, claim complete Anypoint parity, or begin the next feature stage.
