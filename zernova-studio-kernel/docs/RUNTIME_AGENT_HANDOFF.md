> Historical Phase 2A / 0.1.0 document. The approved UI continues in 0.2.0; current execution scope and owner instructions are in [Quickstart](QUICKSTART.md), [Capabilities](CAPABILITIES.md), and [TESTING_STEP_BY_STEP.md](../TESTING_STEP_BY_STEP.md).

# Coordination handoff: NEW ZERNOVA Runtime maintainer

**Status:** UI skeleton delivered for design approval. Runtime integration is deferred. This file is the coordination artifact; it is not an instruction to execute or modify the runtime now.

## Product identity

New source project: `zernova-studio-kernel`. New extension: `zernova-preview.zernova-studio-kernel-preview`, initial version `0.1.0`, commands/state under `zernovaKernelStudio.*`.

This product exclusively targets the new **Mule-Kernel-based ZERNOVA Runtime 0.1.0-preview.1**, Kernel 4.12.0, bundled DataWeave artifacts 2.12.0. It has no dependency on the old Studio or native runtime, YAML/JSON DSL, ZEL, or old runtime adapter. The XML/Maven/DataWeave format remains the application boundary.

## What the preview supplies

- A single native VS Code entry point and isolated workbench preview.
- One stable-ID sample flow with Listener/Logger UI state, shared global-configuration editing and bounded XML serialization.
- Clearly illustrative metadata, DataWeave source/output layout, disabled execution actions and truthful disconnected status.
- Namespaced preview persistence, explicit reset, tested UI interactions, and packaged install evidence reported alongside the project.

It supplies **no** executable adapter, application builder, downloader, listener, requester, runtime log stream, debugger, connection tester, or DataWeave engine. The future boundary in `FUTURE_RUNTIME_BOUNDARY.md` is documentation only.

## Required agreement before real integration

The Studio owner must first approve the UI/UX skeleton and separately authorize the next assignment. Then confirm the exact runtime contract, executable location and supported host. Do not assume paths from the original evaluation machine exist for the owner.

The supplied contract separates process, runtime and application health. A running JVM is not an application success. CLI arguments must be separate arguments, `--json` stays after the subcommand, and failures must preserve the actual structured response and exit status. Build/deploy/runtime health must remain distinguishable.

The runtime handoff reports a Maven Plugin 4.10.3 framework using internal Mule 4.13.0 while executing the app on Kernel 4.12.0. A future build success does not close that compatibility question; retain the runtime semantic check. It also reports source-build and customer-redistribution gates as blocked and only Linux x86-64 runtime execution tested. UI support on the owner's Mac cannot be presented as Mac runtime support.

## Proposed next bounded acceptance

After authorization, the smallest real workflow is HTTP Listener + Logger against the new runtime: create a genuine compatible application, build it, deploy it using the supplied contract, send synthetic loopback input, observe actual logs and response, show app-specific failures accurately, and stop the owned runtime cleanly. Preserve `requiredProduct: "MULE"`, real Mule namespaces, connector coordinates and required notices.

Real DataWeave support uses the selected DataWeave engine. Do not introduce ZEL, an editor-side evaluator, Enterprise-only Transform Message tooling, or a fallback to the old native runtime. HTTP Request and broader connector operations are later scope.

No runtime file was edited for this coordination document. No message was sent to another person or service.
