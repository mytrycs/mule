> Historical Phase 2A / 0.1.0 document. The approved UI continues in 0.2.0; current execution scope and owner instructions are in [Quickstart](QUICKSTART.md), [Capabilities](CAPABILITIES.md), and [TESTING_STEP_BY_STEP.md](../TESTING_STEP_BY_STEP.md).

# Future integration boundary: new Mule-Kernel runtime

## Present state

This Studio milestone provides presentation and isolated preview state only. **No runtime adapter is implemented or activated.** Opening it does not require the old Studio, the old native runtime, Java, Maven, the new Kernel runtime, or connector binaries. It does not discover installations, launch a process, bind an application port, contact a runtime, build an integration application, or evaluate DataWeave.

The future target is the new ZERNOVA Runtime only. Its supplied contract was read from `zernova-runtime-foundation-phase1/RUNTIME_COMPATIBILITY.md`, contract version **1**. The document is a reference input, not an implementation dependency. Its earlier contextual language concerning the native project does not override the owner's corrected direction that this is a separate new Studio.

## Supplied foundation: not rerun in this UI milestone

| Layer | Supplied value / result |
| --- | --- |
| ZERNOVA Runtime | `0.1.0-preview.1` |
| Build mode | `binary-based-overlay` |
| Kernel | `4.12.0` |
| Kernel build | `83aea6905e5ba8187154a8eda9195365e2dfba33` |
| Bundled DataWeave artifact versions | `2.12.0` |
| Actual reported `dw::Runtime::version()` value | `2.12` |
| Tested runtime Java | Temurin `17.0.21+9` |
| Maven | `3.9.11` |
| Mule Maven Plugin | `4.10.3` |
| Requested packaging runtime | `4.12.0` |
| Effective packaging framework | Bundle `2.1.0`, internal Mule `4.13.0` |
| HTTP / Sockets connector | `1.12.2` / `1.2.9` |
| Runtime execution host tested in Phase 1 | Linux x86-64 |
| Functional foundation | Supplied Phase 1 acceptance passed; not executed again here |
| Independent Kernel source rebuild | **BLOCKED** in supplied handoff |
| Customer redistribution | **BLOCKED** in supplied handoff |
| macOS / Apple Silicon / Windows runtime | **NOT RUN** in supplied handoff |

The requested `runtimeVersion` does not establish the effective packaging engine. The supplied contract documents the plugin's choice of its newer embedded framework. A future integration must separately test the built artifact on the actual 4.12.0 runtime. A successful Maven package or a running JVM must not be presented as a healthy application.

## Genuine application boundary to preserve

The future deployable format is a Mule-compatible XML/Maven application with `pom.xml`, `mule-artifact.json`, source under `src/main/mule`, and real `.dwl` resources. Keep upstream XML namespaces, Java packages, Maven coordinates, schema locations, classloader metadata, connector identifiers and `requiredProduct: "MULE"`.

The supplied starter descriptor includes `minMuleVersion: "4.9.0"`, `requiredProduct: "MULE"`, `supportedJavaVersions: ["17"]`, `javaSpecificationVersions: ["17"]`, and `redeploymentEnabled: false`. The declared minimum is not a test result for other runtimes. The Java specification field is the plugin compatibility correction; the preview does not invent a replacement product identity.

The Phase 1 starter uses core `set-payload` for DataWeave, including the tested external-resource expression `#[${file::dwl/external-map.dwl}]`. It does not require `ee:transform`, a ZEL replacement, or an editor-side DataWeave evaluator. The UI's synthetic `mappings/hello.dwl` is only a representative resource path, not a claim that a deployable project was generated.

## Recorded CLI contract for a later assignment

These are read-only documentation of the supplied contract. There are **no corresponding process-invoking handlers** in this UI preview.

| Recorded command | Future meaning |
| --- | --- |
| `bin/zernova version --json` | Read engine/distribution identity |
| `bin/zernova doctor --json` | Check supported platform, Java/config and integrity |
| `bin/zernova start --json` | Start an owned runtime and wait for startup |
| `bin/zernova status --json` | Process, runtime, registered apps and aggregate health |
| `bin/zernova status --app NAME --json` | Require the named application's health |
| `bin/zernova deploy JAR --name NAME --health-path /health --json` | Stage complete artifact and wait for app/endpoint readiness |
| `bin/zernova undeploy NAME --json` | Remove an adapter-owned deployment |
| `bin/zernova logs --json` | Preserved container log lines/path |
| `bin/zernova logs --app NAME --json` | Preserved application log lines/path |
| `bin/zernova stop --json` | Signal the verified owned process and wait for exit |

`--json` belongs after the subcommand. Command arguments must eventually be passed as separate arguments, not interpolated into a shell. Contract payloads differ by command; the documented `schema_version: 1` on identity/status/error surfaces is not permission to assume that every command returns the same schema. `logs --follow` streams raw text, not JSON events.

The supplied exit-code meanings are: 0 success; 2 invalid arguments/configuration or adapter I/O/data error; 3 missing/incompatible dependency, unsupported host/Java or invalid package; 4 state conflict, occupied port, stopped status, unavailable log or unowned deployment; 5 runtime/application failure or timeout. Standalone preparation and Maven build scripts have their own exit conventions.

Process, runtime and application health are separate. A failed application can coexist with `process: RUNNING`; it must remain visibly unhealthy. The optional health request must validate application identity, not just accept HTTP 200. The supplied adapter tracks process ownership with Linux PID/start ticks, a unique token and runtime base, and uses pidfd signaling. None of this establishes Mac/Windows runtime support.

## Smallest later integration, after explicit authorization

1. Agree the runtime installation location through a supported user choice. Do not discover or assume an old absolute scratch path.
2. Recheck the exact runtime command contract and platform. Surface blocked/missing conditions honestly; keep the preview runnable on other hosts.
3. Introduce a versioned, typed runtime adapter outside the presentation/fixture state modules. Never route a preview command into an old extension handler.
4. Produce the smallest genuine HTTP Listener + Logger XML/Maven project while preserving required compatibility identifiers and source notices.
5. Run its actual build, deploy to the new runtime, send a controlled synthetic request, and inspect real application-specific health/log evidence.
6. Keep build, JVM startup, runtime readiness, application deployment, endpoint readiness and shutdown distinct. Preserve engine errors with their source details.
7. Reassess attribution and redistribution before distributing any runtime or connector binaries. This UI-only VSIX does not resolve those blocked gates.

HTTP Request execution, live connector metadata, DataWeave evaluation/editor services, debugger/MUnit, cloud control planes, and enterprise features remain separate later scope. The original runtime must not be altered by this UI milestone.

## Questions for the next runtime-maintenance assignment

- Confirm whether contract version 1 and all listed versions remain current.
- Confirm supported installation/launch strategy for the owner's desired host, especially macOS/Apple Silicon.
- Confirm the currently intended solution for the packaging framework mismatch and require real 4.12.0 deployment compatibility evidence.
- Confirm source and redistribution gates, connector/transitive licenses, required notices and future GUI attribution.
- Define the smallest build/deploy cancellation and diagnostic contract without expanding this preview into a platform manager.

See [RUNTIME_AGENT_HANDOFF.md](RUNTIME_AGENT_HANDOFF.md). No external message or runtime modification is required for this handoff.
