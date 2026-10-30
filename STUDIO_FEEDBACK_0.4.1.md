# ZERNOVA Studio 0.4.1 owner feedback build

Studio 0.4.1 pairs with ZERNOVA Runtime 0.4.1-alpha.1. Application format remains
1, with a 0.4.0 minimum language/runtime floor. The 4.10 engine version remains
upstream provenance. YAML remains deferred.

## Feedback addressed

| Owner feedback | Result |
| --- | --- |
| New Project wizard | Draggable Studio form with location/Browse, folder, coordinates and initial XML. Creates and opens the actual integration. |
| Collapse flows | Expand/collapse control on flows and subflows. |
| Error-handler section | Independent collapse control. Creates native On Error Propagate with Logger; supported error-handler Loggers can be inserted, edited, moved and deleted. Other handler XML is preserved. |
| Deliberate placement | Palette clicks select a component. Choose a valid empty slot before or after selecting a component, or drag to a valid location. Invalid lanes reject insertion. |
| Delete key | Delete and Mac Backspace/Delete remove the selected editable component; text fields retain normal text deletion. |
| XML autosave | Designer changes save through the native document. Text-editor XML changes debounce to disk. Concurrent external-file changes are protected by VS Code. |
| Movable global config | Title-bar dragging retains draft values. |
| XML context actions | Run, Java Debug, Run Configuration, Build, Logs and Stop on empty designer/XML space and the native text-editor context menu. |
| Visible Run/Debug | Top-bar Run/Deploy, Debug, Run Configuration, Logs and Stop. Console controls remain available. |
| Run configurations | Draggable environment, variables, application `-D` properties and debugger-port form. Settings reach the actual JVM and property preflight. |
| Global context actions | Right-click Open, Delete and New; shared-reference checks protect in-use configurations. |
| Draggable dialogs | Shared dragging applies to Studio-owned forms, configuration/flow/delete dialogs, previews and debugger notices. Native VS Code/system UI remains host-owned. |
| localhost | Listener preflight accepts localhost and 127.0.0.1. Actual localhost HTTP execution is tested. |
| Successful HTTP app | Retained; the supplied sample has HTTP Listener and two Loggers. |
| Missing Console logs | Existing owner log file contained both entries. Studio now loads and follows logs automatically, switches to Application Logs after Run, follows the tail and offers Follow latest; manual scrollback is retained. |

## Defined limits

The Debug icon starts a real loopback-only JDWP Java debug target. If VS Code's
Debugger for Java extension is installed, Studio requests attachment. Otherwise,
a draggable notice provides the port for an external Java debugger. This is not
XML flow breakpoints, message inspection or DataWeave stepping.

The offline builder does not execute Maven goals or shell commands. Maven-style
`-Dname=value` lines (and boolean `-Dname`) become literal application JVM system
properties. Environment variables are passed to the child runtime. The
Environment field supplies `zernova.env`. Runtime/bootstrap injection settings
are rejected. Run settings are local workspace data, not a production secret
store. Replacements restart the project-owned JVM.

## Java compatibility finding

The bundle retains the current pinned Java 17 security release: Temurin
17.0.21+9, and Microsoft 17.0.21.0.2 on Windows. The vendor latest-release API was
checked on October 30, 2026 and still identified Temurin 17.0.21+9.

Actual macOS x64 probes used Temurin 25.0.5+8 and 21.0.13+9:

- Java 25 starts the VM but the default domain fails: an extension declares
  support only through Java 21.
- Java 21 starts the VM and default domain, but HTTP app deployment fails because
  the Sockets connector declares Java 8/11/17 support.

Compatibility checks were retained. A JVM readiness banner alone is no longer
accepted when the default domain failed. Java 25 requires connector/SDK migration
and qualification. No claim that one JDK is universally fastest is made.

Evidence: `verification/studio-0.4.1-devtest/java-compatibility.json`.
Primary references: [Java support roadmap](https://www.oracle.com/technetwork/java/javase/tech/eol-135779.html),
[Temurin 17 release](https://github.com/adoptium/temurin17-binaries/releases/tag/jdk-17.0.21%2B9),
[Sockets compatibility](https://docs.mulesoft.com/release-notes/connector/connector-sockets).

## Verification

Typecheck/build, 67 unit/backend/XML tests and three geometry checks passed
locally. All 22 native runtime acceptance cases passed, including real JDWP
handshake, environment/property resolution, native error-handler execution,
HTTP/Logger, transformation/validation, redeployment and crash recovery.

Native UI checks created a project through the new wizard, physically moved the
project/global/run forms with values intact, exercised both click-placement
orders, palette dragging, Delete, flow/error collapse, native error-handler
creation, global and XML context menus, and native text-editor autosave.

A GUI-created localhost app with two Loggers returned Hello from ZERNOVA. The
live Console showed `UI041 qa works qa` and `ZERNOVA_TEST UI041-LOCALHOST` without
Refresh. This proves the saved environment and properties reached actual flow
execution. The app was stopped via the XML context menu.

The final package was built from clean source revision
`a8578a44a7a48d8ef01fdef58261ee37315b2022`, with no resumed build. The source CI
suite reports 429 selected tests: 428 passed and one inherited skip; all 11
namespace-relocation checks passed. The source engine SHA-256 is
`15181b8fe0657880029a4b2fc83bcd7cedcf7b5eec7809e4211d5051ec5958a0`.

[CI run 41086574445](https://github.com/mytrycs/mule/actions/runs/41086574445)
passed 22 native runtime cases and nine installed-Studio cases on each of Mac
Intel, Linux x64/ARM64 and Windows x64/ARM64. macOS ARM64 did not acquire a
hosted runner; GitHub reported capacity constraints and canceled that job before
any steps ran. Its retry was requested. This build does not claim macOS ARM64
qualification or an entirely green six-target run.

The exact downloaded Mac Intel VSIX then passed all nine installed checks locally
on stable VS Code 1.144.0 in a fresh profile, with deliberately invalid external
Java/Maven paths. Checks include actual custom-editor resolution, localhost HTTP,
two automatic fresh Logger entries, native XML autosave, route replacement,
invalid-source protection and clean shutdown.

Native GUI testing of that exact package confirmed the fresh messages
`UI041 qa works qa` and `ZERNOVA_TEST UI041-FINAL-AUTOTAIL` visible at the live
console tail without Refresh or scrolling. An invalid physical Logger drop onto
the HTTP source was rejected and the XML SHA remained unchanged. The GUI runtime
was stopped and the isolated test app closed.

The Mac Intel VSIX SHA-256 is
`5713bf1adfc52ec9e45c00007924b7e0c5172d9b2ab8e2377bd1a08dcb6e34f2`.
Evidence is in `verification/studio-0.4.1-devtest/`. Development dirty/resumed
bundles were excluded from delivery. Later documentation/evidence commits do not
change this verified binary revision.
