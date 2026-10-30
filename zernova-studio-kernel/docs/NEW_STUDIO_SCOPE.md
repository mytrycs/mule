> Historical Phase 2A / 0.1.0 document. The approved UI continues in 0.2.0; current execution scope and owner instructions are in [Quickstart](QUICKSTART.md), [Capabilities](CAPABILITIES.md), and [TESTING_STEP_BY_STEP.md](../TESTING_STEP_BY_STEP.md).

# New ZERNOVA Studio: product boundary

## This initiative

This is a new Studio codebase for the new **Mule-Kernel-based ZERNOVA Runtime**. It is not an upgrade, migration, integration, or rebranding of an earlier ZERNOVA Studio or independently developed native runtime. The owner's corrected brief dated **29 October 2026** replaces the previous Phase 2A prompt in full.

| Identity | Value |
| --- | --- |
| Separate source directory | `zernova-studio-kernel` |
| Product display name | `ZERNOVA Studio` |
| Extension publisher | `zernova-preview` |
| Extension/package name | `zernova-studio-kernel-preview` |
| Full extension identifier | `zernova-preview.zernova-studio-kernel-preview` |
| Initial version | `0.1.0` |
| Command/view/state namespace | `zernovaKernelStudio.*` |
| Milestone | Six-screen interactive UI/UX design preview |
| Publication | Locally packaged development VSIX; no marketplace/customer release |

The version starts a fresh history for this product. An earlier extension's version and identity are not reused. This extension contributes one ZERNOVA Activity Bar container. It does not remove or rename any contribution from another extension.

## Supported design boundary

The native VS Code host owns its title bar, Activity Bar, sidebar, editor tabs, command palette, and status bar. One scoped webview panel provides the integration canvas, right palette, lower Properties area, metadata fixtures, global-configuration dialog, embedded XML preview, and future DataWeave layout. It is not registered as the default editor for XML files.

All editable sample values belong to a versioned, namespaced in-memory/persisted **preview model**. That model is an internal UI state structure, not a new application DSL, compiler, or on-disk integration format. Preview XML is a bounded serializer of the fixture state. Preview save operations do not create, modify, or build real integration projects.

The required project tree uses `src/main/mule`, `src/main/java`, `src/main/resources`, `src/test`, `pom.xml`, `mule-artifact.json`, and `.dwl` resources. Genuine Mule namespaces and compatibility identities are preserved. `requiredProduct` remains `MULE` wherever the future descriptor is discussed.

## Only future runtime target

The read-only supplied contract is `zernova-runtime-foundation-phase1/RUNTIME_COMPATIBILITY.md`, contract version 1. Its reported foundation is ZERNOVA Runtime **0.1.0-preview.1**, Mule Kernel **4.12.0**, and bundled DataWeave artifacts **2.12.0**. The runtime is a binary evaluation overlay around unchanged upstream files. Its reported execution tests used Linux x86-64 and Java 17.

These are supplied Phase 1 findings, not runtime tests performed by this UI milestone. Independent Kernel rebuild and customer redistribution remain blocked in that handoff. The preview neither requires nor changes those gates. See [FUTURE_RUNTIME_BOUNDARY.md](FUTURE_RUNTIME_BOUNDARY.md).

## Explicit exclusions

This extension does not depend on or import the old Studio, old runtime adapters, native YAML/JSON flow model, ZEL engine, Java 25 contract, legacy licensing, API Flow, Jira, Git/CI/CD product modules, AI/MCP modules, or enterprise management services. The ordinary VS Code host can still provide its own unrelated built-in navigation.

This milestone includes no runtime invocation, process launch, Java/Maven download, integration application build, deployment, debugger, HTTP server/client, Logger execution, DataWeave evaluation, live metadata, connection testing, connector installation, Exchange, authentication, or licensing workflow. It bundles no Mule runtime, Java, Maven, executable connectors, proprietary Studio implementation, or Enterprise tooling.

The preview remains explicitly labeled `DESIGN PREVIEW` and `No runtime connected`. Metadata and sample output are illustrative. Disabled future actions do not report successful runtime behavior.

## Isolation and approval gate

Development and installation evidence must use separate VS Code user-data and extensions directories. Only new-project files and those isolated test profiles are writable targets. Existing projects, primary VS Code settings, editor associations, and installed legacy extensions must remain unchanged.

Source provenance and actual installation evidence are delivered with this project. A browser render alone does not establish an installed VS Code result. Untested operating systems and architectures remain `NOT RUN`.

After delivery, **stop for design approval**. The next possible assignment is the smallest real HTTP Listener + Logger workflow against the new runtime's verified contract. Design approval does not automatically authorize that implementation; execution work must be separately requested.

## Sources

- Owner's corrected attached brief, `Pasted markdown(20261029-180756).md`, 29 October 2026.
- Owner's supplied six-panel concept, retained unchanged in `evidence/reference/supplied-six-panel-concept.png`.
- Supplied Runtime contract v1, read without executing or modifying the runtime.
- [VS Code workbench extension boundaries](https://code.visualstudio.com/api/extension-capabilities/extending-workbench), consulted 29 October 2026.
