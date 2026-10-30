# ZERNOVA migration and product delivery plan

Updated October 30, 2026. Active product source: this Mule-derived repository,
including `zernova-studio-kernel/`, `zernova-studio-build/` and the Java runtime.

## Current position

We have an integrated development alpha. The source-built 4.10-derived runtime
uses `com.zernova` Java packages and renamed classes. Studio 0.3.0 contains Runtime
0.3.0-dev.2, Java 17 and the HTTP profile in a Mac x64 VSIX. Local installed-host
tests and visible UI checks demonstrate creation, editing, offline packaging,
deployment, real HTTP responses, logs, redeployment and shutdown.

This establishes a working bounded product slice. It does not establish a
production release, full Mule application/connector compatibility, stable-editor
acceptance or six-platform installed-Studio support. Custom Java and additional
dependencies are currently rejected by the offline builder. XML/deployment
compatibility names remain in this alpha and are migration work; required
upstream notices must remain accurate.

Evidence: `VERIFICATION.md`, `zernova-studio-kernel/STUDIO_LOCAL_VERIFICATION.md`,
and `verification/`.

## Product identity and application language direction

The owner has requested ZERNOVA XML namespaces and independent ZERNOVA runtime
versioning, and asked about YAML application definitions. Recommended direction:

- Use `0.4.0-alpha.1` for the next application-format migration release, with
  independent ZERNOVA semantic versions. Keep `4.10` only as upstream provenance
  or an explicitly named legacy compatibility value while consumers migrate.
  Do not label the product production-ready or 1.0 before its acceptance gates.
- New XML applications use a `zernova` root and ZERNOVA-owned core, HTTP and
  documentation namespace identifiers. Final namespace URIs must be selected
  deliberately and resolved from bundled schemas without network access.
- Change the runtime parsers, schema mappings, component identifiers, generated
  connector metadata, packager and Studio together. Renaming XML strings alone
  does not establish an executable ZERNOVA application format.
- Treat legacy Mule XML as an explicit import/migration format, with diagnostics
  and fixtures. New project output and application-facing contracts must use
  ZERNOVA names. Preserve legally required upstream attribution and historical
  source provenance rather than falsifying it.
- Evaluate YAML after the XML contract executes end to end. Prefer XML and YAML
  frontends feeding the same validated application model and execution semantics.
  Define expression quoting, typing, flow references, source locations, unknown
  components and deterministic round trips before offering YAML authoring.
  A YAML prototype for the supported core/HTTP subset is distinct from a complete
  editor, schema and connector language implementation.

Sequence: repair clean builds; establish the independent version contract and
ZERNOVA XML slice; prove the reference application in Studio and standalone;
then qualify YAML against the same behavioral fixtures. These changes are
planned work, not capabilities of the current 0.3.0 bundle.

## Implementation status

The owner authorized all six phases and requested a bundled HTTP Listener/Logger
dev-test delivery, plus intrinsic flow widths and last-component response lines.
Implementation is in progress on the existing migration branch. YAML is deferred.
No phase is complete until its actual build/installed/runtime evidence is recorded.

## Immediate milestone: reproducible internal release

The same committed Studio/runtime pair must build on a clean machine, install
with its dependencies, and pass the supported workflows without a prepared
developer environment.

### First implementation task: close the clean-build failure

The latest checked CI run, for commit `08c8178b1c9bc794b92a06b09091b167df60f5ff`,
failed in the historical dependency bootstrap. Its runner used Maven 3.10.0,
which rejected the duplicate `com.sun.xml.bind:jaxb-impl` declaration in
`modules/extensions-spring-support/pom.xml`. Native jobs were skipped.

Run: https://github.com/mytrycs/mule/actions/runs/41057298180

Work in order:

1. Pin and record the build toolchain. Correct duplicate dependency declarations
   in tracked source and the bootstrap adaptation without weakening validation.
2. Complete the source build and selected tests in a fresh checkout with empty
   dependency caches. Treat further failures as blockers until this path passes.
3. Make preparation, failure recovery and repeat builds predictable. Document
   which inputs are original, adapted, relocated or compiled from our source.
4. Replace expiring workflow-artifact input recovery with durable, checksum-pinned
   inputs or repeatable vendor downloads. Record dependency sources and licenses.
5. Produce artifacts from a clean committed source tree, with exact versions,
   dependency inventory, source archive, checksums and build/test provenance.

### Acceptance gates

- Green clean source build and selected source/relocation tests in CI.
- Fresh native acceptance of the renamed runtime on Linux, macOS and Windows,
  each on x64 and ARM64; previous-namespace results do not satisfy this gate.
- Installed VSIX validation on a pinned stable VS Code release, starting with
  Mac x64 and then each supported target. Test installation, restart, runtime
  state reconciliation, build/run/logs/stop and missing/damaged-bundle errors.
- Offline operation of installed core/HTTP projects with no external Java,
  Maven, runtime directory or dependency cache required.
- Failed deployment, cancellation, occupied ports and crashes preserve truthful
  state and do not stop unrelated processes. Stop/reopen/upgrade behavior has
  explicit tests and recovery instructions.
- Packaging fails if the matching runtime, Java, sources or required notices
  are absent; an incomplete VSIX cannot be presented as a full package.

## Following milestone: representative application acceptance

Create a small versioned reference application suite shared by runtime and Studio
tests. Start with an Orders API using supported HTTP/core features:

- POST JSON, a meaningful DataWeave response transformation and correlation logs.
- Valid and invalid inputs with defined response bodies/status codes.
- Configuration from project resources, route edits and repeat redeployment.
- Import/save/reopen/build behavior, including preservation of unsupported XML.
- The same expected responses and lifecycle results through Studio and standalone
  runtime control.

Add a failure/recovery application and an import compatibility fixture. Record
feature gaps discovered by these applications and prioritize the ones that
prevent a complete supported workflow.

## Then: establish the extension and compatibility contract

Version the application format, runtime-control API, Studio/runtime pairing,
Java baseline and dependency profile together. Define the supported import subset,
namespace migration rules and actionable diagnostics for unsupported inputs.

Design and verify namespace-aware connector/SDK packaging and custom Java builds
before enabling arbitrary additional dependencies in Studio. Extend visual
editing alongside the engine capabilities that representative applications need.
Retain a clear separation between engine execution, portable control/build
services and Studio document/UI behavior.

## Then: operational readiness and enterprise capabilities

Complete supported-platform packaging, upgrade/recovery, configuration/secrets,
diagnostics, resource/performance testing and distribution/license review before
external release. Build enterprise capabilities in the order required by real
applications, with tests and supported contracts for each increment.

Product priorities such as environments, deployment management, access control,
audit history, observability and connector coverage should be selected against
actual customer workflows after the internal-release gates pass.

## Project ownership

Engineering ownership covers the runtime fork, dependency adaptations, shared
contracts, Studio integration, fixtures, CI, packaging and release evidence.
Routine implementation decisions follow the agreed supported scope. Product
scope, commercial licensing and business priorities remain explicit owner
decisions when they materially change the product.

Every milestone closes with a reviewable source commit, built artifacts, actual
test results, known limitations and reproducible instructions. Future work uses
this plan and the committed evidence as the current baseline.
