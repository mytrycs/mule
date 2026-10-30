# Test ZERNOVA Studio 0.2.0 yourself

This guide is for the **new Mule-Kernel-based Studio**, extension
`zernova-preview.zernova-studio-kernel-preview`. It continues the approved 0.1.0
interface. It does not replace the older native ZERNOVA Studio.

The milestone is one real **HTTP Listener → Logger** application: create its
project and XML in Studio, build it, deploy it to the new Runtime, send a request,
find the corresponding Logger line, change the app, redeploy, and clean up.

The machine-readable results and actual screenshots for this release are in
`evidence/phase2b/`. The HTML edition of this guide embeds the final screenshots.
The earlier `evidence/ui-runs/` and 0.1.0 reports are historical evidence, not the
verification counts for this release.

## 1. Choose the right machine

The directly tested arrangement runs **all four parts on one Linux x86-64
machine**:

| Part | Where it runs in the tested arrangement |
| --- | --- |
| VS Code desktop and this extension | Linux x86-64 |
| Project files and Maven build | The same Linux machine |
| ZERNOVA Runtime, Java, and app listener | The same Linux machine |
| `curl` test request | A terminal on that Linux machine |

The application listens on `127.0.0.1`, so it is reachable from that machine.
`127.0.0.1` on your Mac is your Mac, not a remote Linux machine.

**Native Mac/Apple Silicon and Windows runtime execution are NOT RUN.** The
Runtime Foundation launcher currently checks for Linux x86-64 and Linux pidfd
support. Do not install the VSIX locally on your Mac and assume it can start
this Linux runtime there. File editing and the design preview do not require
the runtime.

### Optional Mac-to-Linux arrangement — NOT RUN for this release

If you already have an authorized Linux x86-64 SSH host, VS Code's Remote-SSH
extension can put the workspace and extension host on Linux while the desktop
window stays on your Mac. Prerequisites are an SSH client on the Mac, an
accessible SSH server/account on Linux, VS Code with Microsoft's Remote-SSH
extension, and the Runtime/toolchain files on Linux. Use **Remote-SSH: Connect
to Host...**, open the Linux workspace, and install ZERNOVA in the SSH window's
remote extension host. Check that the lower-left host indicator identifies the
Linux host. Use the remote integrated terminal for the commands below.

If you send requests from the Mac instead, forward the listener port with the
Remote-SSH **Ports** view, or use an authorized SSH tunnel such as
`ssh -N -L 127.0.0.1:18081:127.0.0.1:18081 user@your-linux-host`. The left port is the Mac
port; the destination is Linux loopback. Change only the left port if it is
already occupied on the Mac. This route is a documented setup option, not a
verified ZERNOVA result. No hypervisor, paid host, public listener, or host
firewall change is part of this delivery.

References: [VS Code Remote-SSH](https://code.visualstudio.com/docs/remote/ssh)
and [VS Code command-line isolation](https://code.visualstudio.com/docs/configure/command-line).

## 2. Gather the files and prerequisites

You need the delivered VSIX `zernova-studio-kernel-preview-0.2.0.vsix`, the new
[Runtime Foundation source/packaging ZIP](https://chatgpt.com/library?file_id=libfile_93e1a88a825881919a4d53d21db398f5&account_id=personal), and a Linux x86-64 machine with a GUI
or an appropriately configured remote extension host. Keep the separate
0.1.0 VSIX if you want a UI-only rollback.

| Dependency | Required selection |
| --- | --- |
| VS Code | Manifest minimum 1.96.0; tested VS Code 1.144.0 on Ubuntu 24.04.3 LTS |
| Python | 3.10 or newer, with working Linux pidfd support |
| Shell | Bash |
| Java | Java 17 JDK; tested baseline Temurin 17.0.21+9 |
| Maven | Apache Maven 3.9.11 |
| Runtime Foundation | ZERNOVA 0.1.0-preview.1 |
| Actual execution kernel | Mule Kernel 4.12.0 |
| Bundled DataWeave artifacts | 2.12.0 |
| HTTP connector | 1.12.2; resolved Sockets dependency 1.2.9 |
| Application build plugin | Mule Maven Plugin 4.10.3 |
| Test utility | `curl` |

Allow space for the Runtime, Java, Maven, dependency cache, and private live
instance. The Runtime's default heap is 1024 MiB, plus native memory. Node/npm
are needed only if you rebuild the Studio extension from source.

The VSIX contains **no Kernel, JDK, Maven, or connector executable**. The example
deliverable contains application source, not a generated application JAR.
Initial preparation and initial Maven dependency resolution need internet
access. Once the dependencies are legitimately cached, use the tested offline
build mode. Offline mode does not download missing dependencies.

## 3. Prepare the Runtime on Linux

Extract `ZERNOVA_Runtime_Foundation_0.1.0-preview.1_Source.zip` into a new folder.
In a Linux terminal, use your actual extraction path:

```bash
cd "/absolute/path/zernova-runtime-foundation-phase1"
python3 scripts/prepare.py --download-toolchain
./bin/zernova version --json
./bin/zernova doctor --json
```

Preparation downloads checksum-pinned dependencies with TLS verification.
If you already have the exact official Runtime ZIP and supported tools, the
documented alternative is:

```bash
python3 scripts/prepare.py \
  --runtime-zip "/absolute/path/mule-standalone-4.12.0.zip" \
  --jdk-home "/absolute/path/jdk-17" \
  --maven-home "/absolute/path/apache-maven-3.9.11"
```

Expected identity: ZERNOVA `0.1.0-preview.1`, Kernel `4.12.0`, DataWeave `2.12.0`.
Expected diagnostic result: `dependency_check: "PASS"`. A stopped runtime is
normal before you run an app. Keep the Runtime's legal notices and original
Mule compatibility identifiers. Its `customer_release: "BLOCKED"` field is an
existing release gate; it is not an application execution failure.

If preparation fails, fix the reported missing tool, checksum, repository,
proxy, or trusted-CA issue before continuing. Do not disable TLS verification.
Record the prepared Java and Maven locations from `var/prepared.json`.

## 4. Install in an isolated VS Code instance

The following Linux terminal recipe keeps the old Studio's profile and
extensions separate. Replace only `ZN_DELIVERY` with your real delivery folder.
Use a new `ZN_TEST_HOME` folder if that folder already belongs to another test.

```bash
ZN_DELIVERY="/absolute/path/to/the/delivery"
ZN_TEST_HOME="$HOME/zernova-phase2b-owner-test"
mkdir -p "$ZN_TEST_HOME/workspace"

code --user-data-dir "$ZN_TEST_HOME/profile" \
  --extensions-dir "$ZN_TEST_HOME/extensions" \
  --install-extension "$ZN_DELIVERY/zernova-studio-kernel-preview-0.2.0.vsix" \
  --force

code --user-data-dir "$ZN_TEST_HOME/profile" \
  --extensions-dir "$ZN_TEST_HOME/extensions" \
  --list-extensions --show-versions

code --new-window --user-data-dir "$ZN_TEST_HOME/profile" \
  --extensions-dir "$ZN_TEST_HOME/extensions" \
  "$ZN_TEST_HOME/workspace"
```

The extension list must contain
`zernova-preview.zernova-studio-kernel-preview@0.2.0`. If using the UI installer,
run **Extensions: Install from VSIX...** in this isolated window and choose the
same file. Trust only the test workspace you just created. If the title/banner
says **Restricted Mode**, run **Workspaces: Manage Workspace Trust**, choose
**Trust** for this private workspace, and confirm **You trust this folder**.
Builds and runtime execution are blocked in an untrusted workspace.

Open the Command Palette with **Ctrl+Shift+P** on Linux. The original
**ZERNOVA: Open Studio Design Preview** command remains available, but use the
real-project commands in the following steps.

## 5. Create your own empty project

Run **ZERNOVA: New ZERNOVA Project**. Enter these example values:

| Wizard step | Example |
| --- | --- |
| Parent directory (absolute path) | The full path to `$ZN_TEST_HOME/workspace` |
| Project folder name | `zernova-owner-test` |
| Maven groupId | `io.zernova` |
| Maven artifactId | `zernova-owner-test` |
| Maven version | `1.0.0` |
| Project runtime and tools | **Configure runtime now** |

The configuration sequence asks for:

1. **Runtime Foundation directory:** the extracted folder containing
   `bin/zernova`, `scripts/build-app.sh`, and `var/prepared.json`.
2. **Java 17 JDK directory:** its root, not the `bin/java` file.
3. **Maven 3.9.11 directory:** its root, not the `bin/mvn` file.
4. **Maven dependency cache directory:** use the prepared cache, normally
   `<runtime-folder>/var/maven-repository`.
5. **Maven dependency mode:** use **Online — allow Maven downloads** for a
   fresh cache, or **Offline — use prepared cache** when the needed artifacts
   already exist.

You can choose **Configure later — edit only** to create/edit without tools,
then run **ZERNOVA: Configure Project Runtime and Tools** later. Configuration
belongs to the selected project and is stored in this extension's local state;
opening another project does not silently reuse this project's settings.

Inspect the real project tree. You should see `pom.xml`, `mule-artifact.json`,
resources, and **an empty `src/main/mule` directory**. No hello-world flow is
generated. This is intentionally not runnable yet. A duplicate destination or
invalid identifier must be reported, not overwritten. Escape cancels a wizard.

Run **ZERNOVA: Runtime Diagnostics** after configuring tools. Read the actual
version/dependency output; a running JVM is not required for diagnostics.

## 6. Create `hello.xml` and a flow

Run **ZERNOVA: New Integration XML**, enter `hello.xml`, and accept. The file
opens in the ZERNOVA integration editor. It contains the real Mule XML root and
namespaces but no flow.

Choose **New flow**, enter `helloFlow`, and create it. The source and processor
lanes are initially empty. **Message Flow**, **Global Elements**, and
**Configuration XML** are views of this one document. Selecting XML must stay
inside the same integration editor.

## 7. Add and configure the HTTP Listener

Choose **Listener** in the right-hand palette and select the new source node.
Its empty configuration reference is diagnosed until you select a configuration.
Choose **Create HTTP Listener configuration** (the plus button) beside that selector to open the shared configuration editor
(the same editor is available through **Global Elements → Create**).

| Configuration field | Value |
| --- | --- |
| Name | `HTTP_Listener_config` |
| Protocol | `HTTP` |
| Host | `127.0.0.1` |
| Port | `18081` |
| Base path | Empty |

Save the configuration. Select the Listener again if needed, choose
`HTTP_Listener_config` in Properties, and enter the fields below. Only one source
belongs in this flow. **Edit HTTP Listener configuration** (the pencil button) beside the selector reopens this configuration.

| Listener field | Value |
| --- | --- |
| Display Name | `HTTP Listener` |
| Configuration reference | `HTTP_Listener_config` |
| Path | `/hello` |
| Allowed methods | `GET` |

In the Listener's **Responses** section, enter:

| Success response field | Value |
| --- | --- |
| Status | `200` |
| Body expression | `#['Hello from ZERNOVA']` |
| Headers expression | `#[{'Content-Type': 'text/plain; charset=UTF-8'}]` |

The Listener itself supplies the response after the flow finishes. No Set
Payload, transformation processor, or substitute HTTP component is needed.
These expressions run in the actual runtime, not the webview. A listener has
no useful inbound **Test Connection** operation; that control is disabled.
HTTPS/TLS and advanced listener features remain outside this milestone.

## 8. Add and configure the Logger

Choose **Logger** in the palette and select the new processor. Enter:

| Logger field | Value |
| --- | --- |
| Display Name | `Log request` |
| Message | `#['ZERNOVA_TEST ' ++ (attributes.queryParams.testId default 'missing')]` |
| Level | `INFO` |
| Category | `io.zernova.acceptance` |

Your flow now has a Listener in the source lane and one Logger in the processor
lane. The Logger records the request identifier but does not change the
response body. Unknown processors in an imported project remain preserved and
visually unsupported; this milestone does not execute replacements for them.

## 9. Check synchronization, save, and reopen

Change the Logger's Display Name visually to `Log owner request`. Open
**Configuration XML** and find the corresponding documentation name. Change
the Listener path in the embedded XML to `/hello-review`,
then return to **Message Flow** and select the Listener. The Path must show
`/hello-review`. XML edits are applied to the same VS Code document as you type;
wait for the change to appear before the next operation. Restore `/hello`
before the first run.

Use the editor's **Undo** and **Redo** controls and inspect the resulting XML.
These are document edits, not changes to the old preview fixture. Use **Save**
or **Ctrl+S** to save the actual XML. A dirty indicator means the editor differs
from disk. If you close while dirty, respect the normal Save/Don't Save/Cancel
protection. Do not discard your work accidentally.

Close and reopen `hello.xml` from Projects. For ordinary XML that opens in a
text editor, use **ZERNOVA: Open Integration in Studio**. Saved values should
remain. A second project has its own runtime configuration. Unsupported XML,
comments, CDATA, namespaces, expressions, and Unicode are preserved during
unrelated supported edits; invalid XML remains available for correction.

## 10. Build and run through Studio

Save the document, then choose **Build** or **ZERNOVA: Build Application**.
Inspect Build output. The build runs Maven against this project's actual POM.
For the example coordinates, the generated artifact is:

```text
zernova-owner-test/target/zernova-owner-test-1.0.0-mule-application.jar
```

Choose **Run / Redeploy** or **ZERNOVA: Build and Run Application**. It builds the current
project, starts/reuses its designated private runtime instance, and explicitly
deploys the completed artifact. Resolve a save prompt before continuing; a
cancelled or failed build must not deploy an old JAR as a fresh success.

Keep the status meanings separate:

| Status | What it establishes |
| --- | --- |
| Build succeeded | Maven produced the selected project's application artifact |
| Runtime ready | The owned JVM completed the runtime's startup sequence |
| Application deployed | The engine accepted and started this application |
| Endpoint unverified | No fresh HTTP verification has been established yet |
| Failed | Inspect the specific build/deployment/diagnostic output |

The `/hello` endpoint returns plain text, not the starter's JSON `/health`
contract. Studio does not invent a JSON health response. The next request and
matching log line establish the behavior that matters.

## 11. Send a request and find its real Logger line

On the **same Linux machine** as the runtime, open a terminal and run:

```bash
curl --noproxy '*' -i \
  'http://127.0.0.1:18081/hello?testId=ZN-OWNER-001'
```

Expect HTTP **200**, a text content type, and body:

```text
Hello from ZERNOVA
```

Choose **Refresh logs** or **ZERNOVA: Show Application Logs**. Find a fresh line with:

```text
ZERNOVA_TEST ZN-OWNER-001
```

Check its timestamp, Logger category, and application identity. Use a new
identifier such as `ZN-OWNER-002` for each request so an old log cannot satisfy
your check. These are original application runtime logs, separate from Maven
Build output and extension diagnostics. Refresh logs after sending the request.

If port `18081` is occupied, choose another free port in the configuration and
save/redeploy; update the request URL to the same port. Do not change the host
to `0.0.0.0` for this test. If using an unverified remote setup, send curl from
the Linux terminal or use the forwarded Mac port described in step 1.

## 12. Change and redeploy the app

Visually change the Listener Path to `/hello-v2`. Save and choose **Run / Redeploy** again.
This is an explicit build/redeploy; editing alone does not hot-redeploy.

```bash
curl --noproxy '*' -i \
  'http://127.0.0.1:18081/hello-v2?testId=ZN-OWNER-002'
curl --noproxy '*' -i \
  'http://127.0.0.1:18081/hello?testId=ZN-OLD-ROUTE'
```

The new route must return the expected text and produce the new Logger line.
The old route must no longer behave as the active `/hello` flow; the recorded
acceptance evidence gives its exact observed HTTP result. Do not count an old
Logger line as proof that the new build ran.

Optionally insert a second Logger with a different display name/message.
Use its context menu or the editor controls to move, duplicate, delete, and
undo/redo it. Rebuild/redeploy when you want to verify its actual log order.
Return to the one-Logger sample after the exercise if you want a simple final
project.

## 13. Try two controlled failures and recover

### Invalid XML

Save a valid copy first. In **Configuration XML**, remove the final `>` from
`</mule>`. Problems must identify invalid XML, and visual
editing must not replace your source with a previous valid document. A build
must not report a fresh successful deployment. Undo the malformed edit,
restore valid XML, and save before continuing.

### Occupied port

First choose **Undeploy** and **Stop runtime** so
your test app is not using the port. In a separate Linux terminal, start this
temporary, loopback-only holder:

```bash
python3 -m http.server 18081 --bind 127.0.0.1
```

Try **Run / Redeploy** in Studio. It must report the port conflict and remain unsuccessful;
it must not kill the holder or silently select a different port. Return to the
holder's own terminal and press **Ctrl+C**. Then run your app again. Never kill
an arbitrary Java/process ID to resolve a port conflict.

Other failure evidence covers missing tools/runtime, unresolved dependencies,
invalid expressions/configuration references, stale-build protection,
cancellation, and bounded timeouts. See the release's test records for which
cases use a real runtime and which use a controlled adapter/extension-host test.

## 14. Clean up and check persistence

Choose **Undeploy** (Command Palette: **ZERNOVA: Stop Application (Undeploy)**) to remove only the selected application's
deployment. Send another curl request: it must no longer return that app's
successful response. The runtime process may still be running.

Choose **Stop runtime** (Command Palette: **ZERNOVA: Stop Project Runtime**) to stop the project's designated runtime
instance. Any active/foreign application protection must be resolved explicitly;
do not stop a shared instance silently. The selected instance should become
stopped and release its listener ports. **Status** (Command Palette: **ZERNOVA: Refresh Runtime Status**) obtains
current process/application state; a stale persisted “Running” label is not
evidence of a live instance.

Close the isolated VS Code window, reopen it with the same command/profile,
and reopen the project/XML. Saved files and project-specific selections should
remain. The runtime must not silently restart merely because an editor opens.
Live state stays outside the synchronized project source folder; preserve the
reported state location if diagnosing a failed cleanup.

## 15. Automated checks and troubleshooting

To rebuild Studio itself from the source delivery, use Node.js 22.14+ and npm:

```bash
cd "/absolute/path/zernova-studio-kernel"
npm ci
npm run typecheck
npm test
npm run build
npm run package
```

Expected: typecheck succeeds, every model/protocol/host-adapter test reports
PASS, and package creates the 0.2.0 VSIX. Run the source/bundle/package boundary
checks after packaging:

```bash
node tests/static-boundaries.mjs
```

For the installed preview regression, set an existing VS Code executable and
private Linux display. The runner creates its own profile and installs the VSIX:

```bash
ZERNOVA_VSCODE_BIN="/absolute/path/VSCode-linux-x64/code" \
  DISPLAY=:99 npm run test:host
```

For the actual owner UI automation, first install Xvfb through your normal
approved Linux package-management process if it is not available. This example
uses an Ubuntu Linux tool layout, a disposable profile, and display `:100`.
Choose a different free display if yours is occupied. Do not point these tests
at a primary VS Code profile. Set the actual Runtime Foundation folder:

```bash
export ZERNOVA_VSCODE_BIN="/absolute/path/VSCode-linux-x64/code"
export ZERNOVA_VSCODE_CLI="/absolute/path/VSCode-linux-x64/bin/code"
export ZERNOVA_XVFB_BIN="/usr/bin/Xvfb"
export ZERNOVA_TEST_LIBS="/usr/lib/x86_64-linux-gnu"
export ZERNOVA_PHASE2B_HOME="/tmp/zernova-phase2b-replay"
export ZERNOVA_RUNTIME_HOME="/absolute/path/zernova-runtime-foundation-phase1"
export DISPLAY=:100
export ZERNOVA_UI_CDP="http://127.0.0.1:9224"
export ZERNOVA_ACCEPTANCE_PROJECT="zernova-owner-test"
node scripts/phase2b-desktop.mjs start
node scripts/test-phase2b-ui.mjs create
node scripts/phase2b-desktop.mjs stop
node scripts/phase2b-desktop.mjs start
node scripts/test-phase2b-ui.mjs persistence
node scripts/test-phase2b-ui.mjs runtime
node scripts/test-phase2b-ui.mjs themes
node scripts/test-ui.mjs
node scripts/phase2b-desktop.mjs stop
```

The Runtime must already be prepared and its Maven cache populated. The create
phase copies that cache into its own test folder and drives the native wizard.
The runtime phase builds, runs, requests, redeploys, undeploys, and stops; the
last desktop command closes the test VS Code/Xvfb processes only. If a runtime
phase fails before cleanup, follow section 14 before removing its test profile.
Use a fresh test folder/project name for a new create run.

Expected: each step exits zero with PASS checks. Results are written under
`evidence/phase2b/runs/`; `latest-create.json`, `latest-runtime.json`, and
`latest-themes.json` identify the successful runs. Preview results use
`evidence/latest-ui.json`; installed preview host results use
`evidence/extension-host-results.json`. Final release evidence records exact
hashes, timestamps, observed totals, and any calibration failures. Separate
native-document and runtime-failure harnesses are included in `scripts/` with
their environment setup and result paths in the release report. Model tests
alone do not substitute for the installed workflow test.

| Symptom | What to check |
| --- | --- |
| Unsupported host / wrong extension host | Confirm the executing extension host is Linux x86-64. A Mac window alone does not establish a Linux extension host. |
| Runtime/JDK/Maven missing | Re-run **Configure Project Runtime and Tools**; select directory roots and exact supported versions. Run **Runtime Diagnostics**. |
| Build cannot resolve a dependency | Use Online mode for the first authorized resolution, or populate the exact selected cache. Inspect Maven repository/proxy/CA errors. |
| Build fails but an old JAR exists | Fix the build first. A prior artifact is not the current build; Studio must not deploy it as fresh success. |
| App failed while Java is running | Inspect application deployment errors and source locations, not only process status. |
| Connection refused | Confirm app deployment, Linux versus Mac loopback, current path/port, and any explicitly configured forwarding. |
| HTTP 404 | Verify the latest Listener path/base path; `/hello` changes to `/hello-v2` only after save and redeploy. |
| Missing Logger output | Send a new unique testId, refresh **Show Application Logs**, and inspect the right application's INFO/category setting. |
| XML is invalid or unsupported | Fix XML in the embedded editor or undo. Preserve unsupported source; don't expect every Mule component to be visually editable. |
| Stop refuses an unexpected/shared instance | Inspect status and ownership. Do not kill unrelated processes or remove unrelated deployments. |
| Need to return to the design-only release | Stop/undeploy first, back up project files, and install the separate 0.1.0 VSIX in the isolated profile. It cannot build/run or edit these projects visually. |

When reporting a problem, include the VSIX version/hash, OS/architecture,
selected command, fresh testId, relevant error and runtime status, and a
minimal redacted XML/POM example. Do not include credentials, private Maven
settings, tokens, full environment dumps, or customer data.

### Owner checklist

| Check | Pass / Fail | Observation |
| --- | --- | --- |
| Correct extension ID and 0.2.0 installed in isolated profile | | |
| Linux runtime/tools diagnostics succeed | | |
| New project's `src/main/mule` starts empty | | |
| XML + flow + Listener + Logger created through Studio | | |
| Visual/XML synchronization and save/reopen work | | |
| `/hello` returns 200 and `Hello from ZERNOVA` | | |
| Matching fresh Logger testId found | | |
| `/hello-v2` change/redeploy works; old route checked | | |
| Invalid XML and occupied-port failures are honest/recoverable | | |
| App undeployed; runtime stopped; port released | | |

This milestone stops at HTTP Listener → Logger. DataWeave visual mapping,
HTTP Request, other connectors, debugging, enterprise features, and commercial
licensing remain preview-only, disabled, or absent. Functional testing does not
clear the existing independent-kernel-source-build and redistribution gates.
