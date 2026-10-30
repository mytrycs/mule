# ZERNOVA Studio 0.5.0 development release

Studio bundles the source-built ZERNOVA Runtime 0.4.1-alpha.1, patched Java 17,
the HTTP dependency profile and original ZERNOVA support module 0.5.0. Projects
use ZERNOVA namespaces and `src/main/zernova`; supported applications need no
external Java, Maven or runtime configuration.

This version adds the core component/transformer catalog, nested scopes/routes,
Scheduler, external DataWeave Transform outputs, HTTP error responses,
environment/global properties and a Z-branded deployment console with lifecycle
stages. Use **ZERNOVA: New ZERNOVA Project**, create integration XML and a flow,
then add/configure components and click Run. Palette selection requires a valid
placement, or use drag and drop. New Transform components default to Payload;
Attributes and Variables are optional.

Read [DEV_TEST_START_HERE.md](https://github.com/mytrycs/mule/blob/codex/studio-core-catalog-0.5.0/zernova-studio-kernel/DEV_TEST_START_HERE.md) for the owner test and
[KNOWN_LIMITATIONS.md](https://github.com/mytrycs/mule/blob/codex/studio-core-catalog-0.5.0/zernova-studio-kernel/KNOWN_LIMITATIONS.md) for exact Batch/Cache semantics,
debugging, offline build and platform scope. Batch and Cache are original local
ZERNOVA implementations; they do not claim Mule Enterprise feature parity.
Qualification for this iteration is Mac x64. YAML and Java 25 remain future work.

The following sections retain earlier implementation history and verification;
their versions/results are not evidence for this release.

---

# ZERNOVA Studio 0.4.0 alpha

This development package contains ZERNOVA Runtime 0.4.0-alpha.1, Java 17 and the
HTTP dependency profile. New projects use ZERNOVA XML and `src/main/zernova`.
Use **ZERNOVA: New ZERNOVA Project**, then **New Integration XML**, create a flow,
add an HTTP Listener and Logger, configure the Listener, save, and click Run.
No external Java, Maven or runtime path is needed for this supported slice.

The flow width follows its components. The response line starts at the last
processor and returns to the Listener. Legacy XML import creates a separate
converted project. YAML, custom Java and arbitrary additional connectors are
outside this alpha. See the repository `APPLICATION_FORMAT.md` for the contract.

The sections below describe earlier implementation/verification history; their
versions and results must not be interpreted as new 0.4.0 verification evidence.

---

# ZERNOVA Studio 0.3.0 — local runtime integration

This Mac x64 development package contains the ZERNOVA Runtime 0.3.0-dev.2,
Java 17 and the HTTP profile. Install the VSIX, open or create a project, design
an HTTP Listener/Logger flow, save and run. External Java, Maven and runtime
paths are not required for the supported offline builder.

Use the integration editor's Console tab for Build, Run, logs, undeploy and stop.
The builder supports core XML/resources and the bundled HTTP connector. Custom
Java or additional Maven dependencies are rejected rather than silently omitted.
Runtime state is private and stored outside the application project.

Local installed-host validation is recorded in STUDIO_LOCAL_VERIFICATION.md.
This package is for local development testing; cross-platform installed-Studio
release verification remains separate. The original 0.2.0 interface and document
editing behavior are retained.

## Preserved 0.2.0 documentation

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
