> Historical Phase 2A / 0.1.0 document. The approved UI continues in 0.2.0; current execution scope and owner instructions are in [Quickstart](QUICKSTART.md), [Capabilities](CAPABILITIES.md), and [TESTING_STEP_BY_STEP.md](../TESTING_STEP_BY_STEP.md).

# New ZERNOVA Studio — UI skeleton quickstart

## Identity and prerequisites

| Item | Value |
| --- | --- |
| Display name | ZERNOVA Studio |
| New extension ID | `zernova-preview.zernova-studio-kernel-preview` |
| Initial version | `0.1.0` |
| Development package | `dist/zernova-studio-kernel-preview-0.1.0.vsix` |
| Commands, views and state | `zernovaKernelStudio.*` |
| VS Code API minimum | `1.96.0` (manifest declaration; not a claim every intervening host was tested) |
| Build prerequisites | Node.js 22.14+, npm, registry access or prepared cache |
| UI review prerequisites | VS Code desktop and this VSIX only |

The actual tested host/version/platform is recorded in
`evidence/extension-host-results.json` and the screenshot/test handoff. Other
platforms must be reported **NOT RUN** until exercised. No runtime platform
result is inherited by the UI.

## Install without altering your working profile

Create a new, isolated VS Code profile using separate user-data and extension
directories. The following macOS/Linux commands use a dedicated temporary
directory; substitute a persistent review directory if you want the profile
to survive cleanup:

```sh
review_dir="${TMPDIR:-/tmp}/zernova-studio-kernel-review"
mkdir -p "$review_dir/user-data" "$review_dir/extensions" "$review_dir/shared-data"
code --user-data-dir "$review_dir/user-data" \
  --extensions-dir "$review_dir/extensions" \
  --shared-data-dir "$review_dir/shared-data" \
  --install-extension /absolute/path/zernova-studio-kernel-preview-0.1.0.vsix
code --user-data-dir "$review_dir/user-data" \
  --extensions-dir "$review_dir/extensions" \
  --shared-data-dir "$review_dir/shared-data" --new-window
```

Windows PowerShell equivalent (Windows acceptance is **NOT RUN** unless the
evidence says otherwise):

```powershell
$reviewDir = Join-Path $env:TEMP 'zernova-studio-kernel-review'
code --user-data-dir "$reviewDir/user-data" --extensions-dir "$reviewDir/extensions" --shared-data-dir "$reviewDir/shared-data" --install-extension 'C:/absolute/path/zernova-studio-kernel-preview-0.1.0.vsix'
code --user-data-dir "$reviewDir/user-data" --extensions-dir "$reviewDir/extensions" --shared-data-dir "$reviewDir/shared-data" --new-window
```

Alternatively launch that isolated window first, choose **Extensions: Install
from VSIX…**, and select the package. This is an unsigned local development
package, not a marketplace publication. Do not install it over an old extension
identity; it has its own identity and begins a new version history.

The shared-data flag is supported by the actually tested VS Code 1.144.0.
It isolates the newer host's application-shared storage as well as the usual
user-data and extension directories. The declared 1.96 API minimum is not
evidence that older host command-line flags or platforms have been tested.

## Open and navigate

Run **ZERNOVA: Open Studio Design Preview**
(`zernovaKernelStudio.openPreview`). The native ZERNOVA Activity Bar opens one
coherent area with **Projects**, **Design Preview**, and **Target Runtime**.

The project tree is synthetic. `hello.xml` opens the workbench; `hello.dwl`
opens the future DataWeave layout. No fixture tree action creates a real file.
The Design Preview list reaches all six review views plus Empty flow.

Inside the editor:

- Select **Listener** or **Logger** on the canvas to change the lower Properties
  area. Use its section navigation and the illustrative Input/Output trees.
- Use the right palette to search and insert supported sample components.
  HTTP Request is visibly deferred.
- Use **Message Flow**, **Global Elements**, and **Configuration XML** in the
  same editor. XML remains read-only, derived from current preview state,
  selectable and copyable. It is not written to a project.
- Create/edit a global HTTP configuration in the shared configuration dialog.
  Save changes preview state; Cancel discards the draft. Test Connection is
  disabled until a later runtime integration milestone.
- Open the DataWeave layout and switch **Graphics**, **Source with Trees**, and
  **Source Only**. Source and output are fixed illustrative fixtures.
- Drag pane dividers or use their keyboard interaction. Use flow/component
  selection, contextual controls and undo/redo only within the preview.

The persistent **DESIGN PREVIEW** and **No runtime connected** indicators apply
to every view. No UI edit is an integration execution, connection test, or
DataWeave evaluation.

## Themes and reset

The preview theme control changes preview presentation only. To opt into the
matching native host colors, use VS Code's **Preferences: Color Theme** and
choose **ZERNOVA Studio Light** or **ZERNOVA Studio Dark** explicitly. The
extension does not change the global theme automatically.

| Command Palette title | Exact command ID | Effect |
| --- | --- | --- |
| ZERNOVA: Open Studio Design Preview | `zernovaKernelStudio.openPreview` | Open/reveal the same preview panel |
| ZERNOVA: Choose Studio Preview View | `zernovaKernelStudio.openScenario` | Select a bounded fixture view |
| ZERNOVA: Reset Studio Preview | `zernovaKernelStudio.resetPreview` | Restore the initial version-1 fixture/state |
| ZERNOVA: Reset Studio Preview Layout | `zernovaKernelStudio.resetLayout` | Restore pane sizes and 100% zoom; preserve component values |

Preview state is stored only under `zernovaKernelStudio.preview.v1` in this
extension's VS Code **workspace state**. Closing/reopening or reloading the same
window restores saved preview state. A different workspace/profile has its own
state. Dialog drafts and undo history are session UI state; they are not a
project document or a native VS Code file history. Reset does not touch other
extensions, user preferences, real application files, or runtime profiles.

## Reproduce the build

From the separate source directory:

```sh
npm ci
npm run typecheck
npm test
npm run build
npm run package
```

The exact dependency tree is pinned by the lockfile. The produced file is
`dist/zernova-studio-kernel-preview-0.1.0.vsix`. Verify its SHA-256 against the
delivered checksum/evidence before installing. Packaging this UI does not
package or build a Mule application.

`npm run test:host` requires `ZERNOVA_VSCODE_BIN` pointing to an already available
desktop executable and a working display. It creates a fresh private profile
and installs the VSIX; it does not automatically download tools or reuse a
working profile. See the README for the command and the evidence directory for
the observed test results.

## Reproduce installed-UI interaction and screenshot checks

The included `npm run test:ui` suite automates the **installed VSIX in a real
VS Code desktop**, using its loopback Chromium debugging endpoint. It is not
a standalone browser render. The current native resize helper is deliberately
Linux/X11-specific; it is not a tested macOS/Windows automation method.

Use a private X11 display (for example an already available Xvfb display),
Python 3, libX11 and a VS Code desktop executable. Keep only one review window
whose title contains `ZERNOVA Studio` on that display. The bounded resize
helper refuses zero or ambiguous matches and sizes the actual native window;
it does not crop screenshots or emulate a browser viewport.

After installing the VSIX into the isolated directories above, create this
settings file inside **that test profile only**:

```sh
mkdir -p "$review_dir/user-data/User" "$review_dir/workspace"
cat > "$review_dir/user-data/User/settings.json" <<'JSON'
{
  "telemetry.telemetryLevel": "off",
  "update.mode": "none",
  "extensions.autoCheckUpdates": false,
  "extensions.autoUpdate": "off",
  "workbench.startupEditor": "none",
  "workbench.colorTheme": "ZERNOVA Studio Light",
  "workbench.tips.enabled": false,
  "security.workspace.trust.enabled": false,
  "chat.disableAIFeatures": true,
  "window.title": "ZERNOVA Studio · UI Review",
  "window.commandCenter": true,
  "workbench.editor.showTabs": "multiple",
  "workbench.sideBar.location": "left",
  "window.zoomLevel": 0,
  "extensions.ignoreRecommendations": true,
  "workbench.editor.empty.hint": "hidden"
}
JSON

DISPLAY=:99 /absolute/path/to/VSCode-linux-x64/code \
  --user-data-dir "$review_dir/user-data" \
  --extensions-dir "$review_dir/extensions" \
  --shared-data-dir "$review_dir/shared-data" \
  --remote-debugging-address=127.0.0.1 --remote-debugging-port=9223 \
  --skip-welcome --skip-release-notes --disable-workspace-trust \
  --disable-updates --new-window "$review_dir/workspace"
```

Keep that desktop running, then from this source project run:

```sh
DISPLAY=:99 ZERNOVA_UI_CDP=http://127.0.0.1:9223 npm run test:ui
```

For the root-owned container used in the recorded test, the native executable
also required `--no-sandbox`; that was a test-container launch option, not a
change to the extension or a requirement for ordinary user installations.
The test machine used locally extracted display libraries through a scoped
`LD_LIBRARY_PATH`, recorded in `evidence/host-provisioning.json`.

The script changes only its isolated review state/theme/window, performs the
recorded fixture interactions, writes exact-size screenshots in
`evidence/screenshots/`, and retains results under `evidence/ui-runs/` with
`evidence/latest-ui.json` as the current pointer. Do not run it against a
working profile: reset, theme selection and window resizing are intentional
test operations. Keep the debugging endpoint on loopback and close this test
host when finished.

The actual observed host was VS Code 1.144.0 / Electron 43.8.3 /
Chromium 150.0.7871.250 / Linux x64. This is separate from the future runtime's
reported Linux/Java 17 baseline. The UI itself requires no Java, Maven, old
Studio or runtime installation.

## Approval boundary

Review placement, density, interactions, resizing, labels, and themes. Record
the design changes you want before approving the skeleton. Approval of the
visual design alone does not authorize release, runtime operations, connector
execution, or full Studio parity claims.
