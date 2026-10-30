# Studio 0.2.0 ↔ Runtime Foundation contract

This document describes the adapter that is implemented in `src/host/runtime.ts`, `src/host/process.ts`, `src/shared/runtimeContract.ts`, and `src/host/projects.ts`. The runtime contract was read from the delivered Runtime Foundation `zernova_runtime/cli.py`, `scripts/build-app.sh`, `QUICKSTART.md`, and `RUNTIME_COMPATIBILITY.md`. Studio does not modify those files or replace the HTTP connector or core Logger.

For the owner’s installation and complete UI walkthrough, use [TESTING_STEP_BY_STEP.md](../TESTING_STEP_BY_STEP.md). This document explains the execution boundary, commands, and state meanings.

## Supported arrangement

The adapter executes on the **VS Code workspace extension host**. This release admits execution only when that host reports **Linux x64**. Java, Maven, Bash, Python, the prepared Runtime Foundation, Maven cache, project directory, and runtime instance must be accessible on that same host.

A Mac may display VS Code while an independently configured remote workspace extension host runs on Linux. Installing the VSIX in a local Mac extension host does not make the Linux-tested runtime run natively on macOS. Remote SSH/container setup is outside the tested arrangement. The fresh acceptance evidence uses a Linux desktop, Linux extension host, Linux runtime, and HTTP request on that same Linux machine. Visual/XML editing remains available without executing the runtime.

Build, diagnostics, status, application logs, deployment, and stop require Workspace Trust. Configuration records selected paths but does not execute them. Workspace execution is blocked before starting tools in Restricted Mode. A configured project also requires the actual prepared Runtime Foundation artifacts; this VSIX does not contain a runtime or bootstrap one.

## Pinned identity and prerequisites

| Item | Required/used value |
| --- | --- |
| Extension | `zernova-preview.zernova-studio-kernel-preview`, version `0.2.0` |
| Runtime JSON schema | `schema_version: 1` |
| Runtime product | `ZERNOVA Runtime` |
| Runtime distribution | `0.1.0-preview.1` |
| Runtime kernel | `4.12.0` |
| Runtime DataWeave | `2.12.0` |
| Runtime build mode | `binary-based-overlay` |
| Java | JDK 17; tested prepared toolchain Temurin 17.0.21+9 |
| Maven | Apache Maven `3.9.11` |
| Python | Python 3.10+ with working Linux `os.pidfd_open` and `signal.pidfd_send_signal` |
| Shell | GNU Bash |
| New-project HTTP dependency | `org.mule.connectors:mule-http-connector:1.12.2:mule-plugin` |
| New-project packaging plugin | `org.mule.tools.maven:mule-maven-plugin:4.10.3` |
| New-project descriptor | `requiredProduct: MULE`, `minMuleVersion: 4.9.0`, Java specification `17` |

The actual `version --json` response must match the Runtime identity above. A different runtime does not silently become a supported one. The Runtime’s own `doctor` checks Java and the prepared upstream integrity.

**Build-framework distinction:** the pinned Mule Maven Plugin 4.10.3 resolves its internal build-framework dependency at 4.13.0 in the prepared cache. The application POM requests runtime 4.12.0, and the executed runtime identifies as Kernel 4.12.0. Studio does not rewrite vendor JARs or mislabel that internal dependency as the running kernel. A successful application test does not complete the separate source-build, distribution, trademark, or customer-release gates recorded by Runtime Foundation.

## Project and machine configuration

A new project has a real Maven POM, Mule application descriptor, `src/main/mule/`, `src/main/resources/application.properties`, and an application Log4j2 configuration. The initial integration XML has a valid Mule root and an `application.properties` declaration; it contains no flow or components until the owner creates them in Studio.

Machine selections are stored per canonical project root in VS Code extension storage:

- Runtime Foundation directory;
- Java JDK directory;
- Maven directory;
- Maven dependency-cache directory;
- offline or online Maven mode;
- derived private runtime-instance directory.

These absolute paths are not emitted into the project POM, XML, application descriptor, or other project source. Projects can be opened again, and the tool selections are recovered in that VS Code profile. A separate machine/profile must configure its own tools. A running owned instance must be stopped before its configuration changes.

For each project, the instance directory is derived as `os.tmpdir()/zernova-studio-<uid>-<first16 SHA256 of canonical project root>`. It is outside the project and is not the Runtime Foundation’s default shared work directory. `studio-owner.json` binds that directory to the canonical project and selected runtime. A directory that belongs to another project/user, is a symlink, or lacks the expected ownership marker is not silently claimed.

On reload, Studio asks the actual CLI for status. It does not restore a persisted “Running” UI flag as proof of a live or healthy application.

## Environment passed to tools

The adapter removes inherited `ZERNOVA_*` overrides and Java injection variables before setting its explicit profile values. It passes:

```text
JAVA_HOME=<selected JDK>
ZERNOVA_JAVA_HOME=<selected JDK>
ZERNOVA_MAVEN=<selected Maven>/bin/mvn
ZERNOVA_MAVEN_REPO=<selected cache>
ZERNOVA_WORK_DIR=<derived private instance>
PYTHONDONTWRITEBYTECODE=1
```

The subprocess working directory is the selected project. Executables and arguments are supplied as arrays with `shell: false`; shell metacharacters in a path are not evaluated. Bash is invoked explicitly to run the existing packaging script. This release does not expose a Studio field for Maven settings or credentials. The existing Maven installation may still use its ordinary user/global settings. Offline mode requires a complete prepared cache; the extension does not claim that every connector/version is available offline.

## Actual invocations

`<runtime>` below is the chosen Runtime Foundation directory, not a command guessed from `PATH`. Each CLI operation is launched as `python3 <runtime>/bin/zernova ... --json`.

| Purpose | Arguments passed after the CLI path |
| --- | --- |
| Identify the selected runtime | `version --json` |
| Check prepared runtime/toolchain | `doctor --port <resolved Listener port> --json` |
| Inspect real state | `status --json` |
| Start private runtime | `start --port <resolved Listener port> --json` |
| Deploy fresh packaged artifact | `deploy <absolute project JAR> --name <Maven artifactId> --json` |
| Read actual application log | `logs --app <Maven artifactId> --lines 1000 --json` |
| Undeploy selected application | `undeploy <Maven artifactId> --json` |
| Stop verified private runtime | `stop --json` |

Before build/run/diagnostics, Studio checks selected executables, Python/pidfd support, GNU Bash, Maven’s reported version, and runtime identity. Java 17 validation also occurs in the existing `doctor`/build script.

Packaging is the existing, inspected command:

```text
bash <runtime>/scripts/build-app.sh --project <absolute project directory> [--offline]
```

`--offline` is included only when the owner chose offline mode. The script invokes Maven’s `package` goal and the selected cache. Studio does not use the Phase 1 starter directory as a hidden build input. The expected deployable artifact is the selected project’s `target/<artifactId>-<version>-mule-application.jar`.

No arbitrary shell command, `--force` stop, invented JSON field, replacement connector, execution debugger, or HTTP Request component is introduced. The CLI’s optional `--health-path` is deliberately omitted: its contract validates an application-identity JSON response, whereas the owner’s Listener returns the configured application response, such as plain text.

## What Build and Run guarantees

Build prompts to save every dirty document inside the selected project. Declining leaves the operation unbuilt and undeployed. It loads the current POM metadata, validates integration XML, and requires a supported HTTP Listener flow.

The bounded preview supports HTTP on `127.0.0.1`, one port per project, and an integer port from 1024 through 65535. Basic `${property}` references resolve from relative `.properties` resources explicitly declared by `configuration-properties`. Unknown/dynamic values, conflicting property values, escaping paths, unsupported hosts/protocols, and mixed ports receive actionable errors. This release does not infer a runtime port by substituting a hidden default for an unresolved Listener value. Runtime Diagnostics may use 18081 only as a prerequisite-doctor port when the current project is incomplete; passing Diagnostics is not a runnable-application claim.

A build records a source fingerprint over the POM, descriptor, and `src/` contents. A successful Maven exit must produce the expected fresh JAR. The source fingerprint and open-document dirty state are rechecked after packaging and immediately before deployment. If source changes during the build or before deployment, Studio refuses that stale artifact. A failed build does not deploy an older JAR left in `target/`.

Run builds first. Only after a valid fresh artifact exists does it inspect the instance, refuse unexpected other applications, explicitly undeploy its previous application, and start/reuse its private runtime. If the selected Listener port changes, the owned runtime is stopped and restarted with the new port. The adapter preflights the loopback port without killing its occupier. It deploys the actual selected-project JAR under that project’s Maven artifactId.

## JSON state and health semantics

Studio validates the following real status fields:

- Runtime identity fields above;
- `process`: `RUNNING` or `STOPPED`;
- `runtime`: `READY`, `STARTING`, or `STOPPED`;
- `work_directory`: exactly the project’s derived instance directory;
- top-level `healthy`: boolean;
- `applications`: map keyed by application name, with each entry’s boolean `healthy` and state;
- application `state`: `NOT_DEPLOYED`, `STOPPED`, `DEPLOYING`, `DEPLOYED`, `READY`, or `UNHEALTHY`;
- optional application `deployment`: `NOT_RUNNING`, `PENDING`, `DEPLOYED`, or `FAILED`;
- optional application `endpoint`: `NOT_CHECKED`, `NOT_READY`, or `READY`.

The actual CLI can return exit 4 for a stopped runtime and exit 5 for unhealthy application state. Those are accepted for a **status** query so the UI can show the truthful result. Other command failures remain failures. Contradictory “stopped but healthy” or “failed application but healthy” status is rejected. Malformed JSON is rejected and raw output is retained.

A live JVM alone is not successful deployment. A failed app can display process `RUNNING` and application `UNHEALTHY` while its runtime is available for diagnosis. Successful deploy requires the CLI’s application result to report `deployment: DEPLOYED` and `healthy: true`.

Because Studio supplies no optional health path, deployment readiness does not prove the owner’s endpoint response. The UI intentionally says **“Deployed · endpoint unverified”** and exposes endpoint `UNVERIFIED`. The owner must send the specified `curl` request on the Linux host, inspect its status/body, and match its unique test token in Logger output. Studio does not invent a successful HTTP result or change this label merely because an app has an anchor file.

## Output and retained evidence

The Output selector exposes three channels:

- **ZERNOVA Build:** actual packaging command and Maven output;
- **ZERNOVA Runtime:** prerequisites, lifecycle/status JSON, errors, and cleanup actions;
- **ZERNOVA Application Logs:** actual CLI-selected application log and its returned lines.

The real editor’s Console also shows these streams and a readable Studio status record. That record is an adapter UI model, not a new Runtime Foundation API contract. Native commands and webview buttons both use the same host adapter. Logs are refreshed by the owner’s Logs action; there is no simulated log feed.

Each operation retains command records in the extension’s global-storage `runtime-evidence/<project hash>/<timestamp-action>/`. Records include executable, explicit arguments, working directory, stdout, stderr, exit/signal, cancellation/timeout flags, elapsed time, and final Studio state. The status model’s `evidenceDirectory` identifies that operation. Paths can contain user/project names; review retained logs before sharing them.

## Cancellation, timeouts, and ownership

A project has one operation at a time. Progress notifications and **Cancel Current Operation** share one cancellation controller. Spawned tool processes use their own process group: cancellation/timeouts first send SIGTERM to that owned group, then SIGKILL only if the group has not closed after three seconds. Runtime shutdown itself uses the existing CLI’s verified ownership contract; Studio does not kill an arbitrary Java PID.

Bounds are 240 seconds for application packaging, normally 120 seconds around CLI startup/deploy, and 45 seconds around stop/undeploy. The actual Foundation configuration has its own startup/deployment deadlines (90 seconds in the delivered configuration) and stop deadline (30 seconds). Tool probes have shorter bounds. A timeout returned as the CLI’s schema-1 error JSON is handled as a timeout as well as an outer process timeout.

Cancellation during the build stage preserves a previously deployed working application. If cancellation or timeout occurs after this Run begins lifecycle changes, Studio reconciles and cleans the owned private instance through `stop`/`undeploy`. Cleanup failures are retained as explicit “Cleanup requires attention” output; they are not reported as successful cleanup. An ordinary deployment failure is kept available for real status/log inspection, and the owner can use Stop Project Runtime.

**Stop Application (Undeploy)** removes only the selected project’s application; the runtime may remain running. **Stop Project Runtime** refuses an instance containing unexpected other applications, removes this project’s application, and stops its verified instance. A pending failed deployment may require the CLI to stop the instance before removing deployment files; Studio follows that contract and records the fallback. Repeating Stop Project Runtime against a stopped private instance is safe.

## Scope limits

This contract implements one HTTP Listener → core Logger milestone. Future connectors, HTTP Request, visual DataWeave mapping, debugging, enterprise functions, commercial licensing, and runtime source-build/release gates remain outside it. Unsupported XML can be preserved and edited as source, but the visual editor and execution preflight do not promise all Mule XML constructs or arbitrary dynamic configurations. Imported cross-file configurations are not a supported general project model in this preview; keep this milestone’s Listener and its configuration in one integration XML.
