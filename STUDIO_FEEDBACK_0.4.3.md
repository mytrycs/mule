# ZERNOVA Studio 0.4.3 — launch profiles and Transform Message

Studio 0.4.3 retains Runtime 0.4.1-alpha.1, Java 17.0.21+9 and application format 1.
The Java engine and immutable runtime bundle were not rebuilt. The shared portable
launcher/controller now applies the new Studio launch settings.

## Feedback delivered

| Request | Result |
| --- | --- |
| Separate Run/Debug dropdowns and floating configuration | Split controls have launch and configuration actions. Run and Debug retain independent project profiles; both use draggable, tabbed forms. |
| Source request/response indicators and final-component route | The Listener has request and response port icons. The final processor has a response-output icon. Request/response SVGs use measured port centers, including zoom and variable component sizes. |
| Context-menu icons | All Studio canvas, component, global and launch-menu actions have appropriate line icons; native command contributions also declare icons. |
| Remove REAL PROJECT badge | Removed from the real editor's logo/header. |
| Transform Message | Palette component with DataWeave payload-script editor, output MIME type, insert/drag/move/duplicate/delete, error-lane support and lossless native XML editing. |

## Launch configuration contract

One Run profile and one Debug profile are stored per project. A new Debug profile
starts from Run values until it is saved independently. Settings apply on the
next launch/redeployment; manual-save redeployment remembers the last launch mode.

| Section | Working controls |
| --- | --- |
| General | Environment (`zernova.env`), working directory, shared redeploy-on-manual-save preference |
| Environment | Explicit `NAME=value` variables and inheritance of host variables |
| Java VM | Initial/maximum heap MB, literal application `-D` properties, additional JVM options |
| Lifecycle | Runtime startup, application deployment and shutdown timeouts |
| Debug | Loopback debugger port, in the Debug configuration dialog |

Working directories may be existing relative/absolute paths; `${project}` and
`${workspaceFolder}` mean the ZERNOVA project root. Environment inheritance No
keeps essential OS variables and explicit application variables. Heap values and
timeouts are validated. Additional JVM options accept GC, diagnostics, assertion,
stack and module-opening settings, one literal option per line; values are never
executed through a shell.

This is a ZERNOVA runtime launch configuration, not an arbitrary Java-main launcher.
The compatible bundled JRE, engine entry point, module/class paths and control agent
remain managed. External agents/bootstrap overrides, arbitrary Maven goals and
custom Java compilation remain outside the current offline builder. Debug is Java
JDWP attachment after startup, without startup suspension or XML flow stepping.

## Transform Message contract

Transform Message executes a DataWeave expression or full `%dw 2.0` payload script
through native ZERNOVA core `set-payload` XML and the bundled DataWeave engine.
Write the script without the outer `#[ ]`; Studio writes the expression safely.
It adds no EE transform connector dependency. Current visual scope is payload
transformation; separate variable/attribute transforms and a schema-mapping GUI
are future work.

An existing Listener response body can override the transformed payload. The
property button **Use payload for HTTP response** sets that body to `#[payload]`.
Response headers remain under Listener Responses. The included minimal sample
already returns payload as application/json.

## Verification

- Typecheck/build, 75 unit/backend/XML tests and four request/response geometry checks passed.
- All 23 native runtime cases passed using the clean bundled engine. New coverage
  verified real configured heap sizes, JVM options and working directory, plus
  JDWP handshake. HTTP, transformation, environment/properties, error handling,
  replacement, ownership, stop and crash recovery remain covered.
- All 17 installed-Studio cases passed on stable VS Code 1.144.0 in a fresh profile
  with invalid external tool paths. Transform Message generated real native XML
  and returned the expected correlated JSON over localhost HTTP.
- Native GUI checks exercised both dropdowns, draggable/tabbed configuration,
  independent Debug values, context-menu icons, badge removal and measured port
  routing. A GUI-authored script returned JSON with environment `qa` and the
  configured project working directory. Both a two-Logger + Transform chain and
  the minimal Listener + Transform chain executed successfully.
- The GUI-saved heap 128/1024 MB and `-ea` were verified in actual JVM launch args.
  Test runtimes were stopped; the private GUI test XML was restored.
- The final VSIX adds the sample after initial qualification. All 691 executable,
  webview and runtime files compare byte-for-byte with the installed/UI-tested
  package. ZIP CRC and bundle provenance checks passed; the sample packaged offline.

Studio 0.4.3 is installed and active in the owner’s main VS Code Default profile.
A graceful restart recovered a blank window after reload. The restored editor
shows the new controls and Transform palette; the owner project was not modified.

This iteration is qualified locally on Mac x64; it does not claim a new six-target
Studio qualification. Exact code revision, artifact hash, results and installation
record are in `verification/studio-0.4.3-devtest/`.
