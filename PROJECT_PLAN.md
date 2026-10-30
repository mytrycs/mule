# ZERNOVA migration and product delivery plan

Updated October 30, 2026. Active product source: this Mule-derived repository,
including `zernova-studio-kernel/`, `zernova-studio-build/` and the Java runtime.

## Latest Studio-only iteration

Studio 0.4.2 retains Runtime 0.4.1-alpha.1. It removes processor placeholders,
uses open-canvas click/drag placement, retains run failures in Console, and adds
opt-in redeploy on explicit manual save while preserving XML autosave. Local
Mac x64 qualification includes 72 unit tests, three geometry checks, 16 installed
cases and native GUI interaction. See `STUDIO_FEEDBACK_0.4.2.md` and its evidence.

## Current position

The active feedback candidate pairs Studio 0.4.2 with independently versioned
ZERNOVA Runtime 0.4.1-alpha.1 and bundled, security-patched Java 17. Application
format 1 retains its 0.4.0 minimum language/runtime floor. New applications execute
ZERNOVA XML directly: a `zernova` root, ZERNOVA namespaces, application descriptors
and offline packaging. The engine uses `com.zernova` Java packages. The original
4.10 baseline remains explicitly identified as engine provenance/SDK compatibility.

Local tests cover the installed stable VS Code extension, HTTP Listener/Logger,
Orders JSON transformation/validation, redeployment, shutdown and crash recovery.
The visual flow response starts at the final processor, and flow/subflow cards
resize as processors are added or removed. A six-platform CI matrix builds and
tests native runtime and installed Studio packages; final results are recorded in
`STUDIO_FEEDBACK_0.4.1.md` and `verification/studio-0.4.1-devtest/`. The earlier
0.4.0 qualification remains in `DEV_TEST_VERIFICATION.md`.

The immediate delivery is an internal development alpha for owner feedback.
Custom Java, arbitrary connector dependencies and YAML remain deferred. Required
upstream legal attribution and historical provenance remain intact. This is not
a production release or a claim of compatibility with every legacy application.

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
then qualify YAML against the same behavioral fixtures. The XML/version contract is implemented in the 0.4.0 candidate. YAML remains
future work and is not part of this delivery.

## Implementation status

The owner authorized all six phases and requested a bundled HTTP Listener/Logger
dev-test delivery, plus intrinsic flow widths and last-component response lines.
The initial 0.4.0 six-phase development-test slice was implemented and verified on
the migration branch. Its clean CI run passed source builds, all six native
runtime suites and all six installed-Studio suites. The owner delivery includes an Intel
Mac VSIX, bundled runtime/Java, HTTP/Logger sample and verification notes. YAML is
deferred. See `DEV_TEST_VERIFICATION.md` for the tested scope and evidence.

## Owner feedback iteration: Studio 0.4.1

The next development-test iteration adds deliberate valid-slot component
placement and pointer dragging, keyboard deletion, XML autosave, collapsible
flows/error lanes, draggable Studio forms, global/XML context actions, visible
Run/Java Debug controls, per-project runtime settings, localhost and automatic
application-log following. Error-handler Loggers execute in the native runtime.

The Debug control exposes Java JDWP attachment; XML flow stepping remains future
work. Maven-style `-D` properties and environment variables are supported by the
offline builder; arbitrary Maven goals and external dependencies are deferred.

Java 25 and 21 were actually probed. Extension/connector compatibility checks
reject the supported HTTP application on those versions. The feedback bundle
therefore retains the latest pinned Java 17 patch. A newer Java baseline needs
connector/SDK migration and independent qualification before release.

The final feedback artifact and exact source revision are recorded in
`STUDIO_FEEDBACK_0.4.1.md`. The Mac Intel artifact passed an additional fresh
local installed-VSIX suite. Five targets passed the 0.4.1 CI suites; Mac ARM64
could not acquire a hosted runner and is not qualified for this build. Its
retry was requested. Once the owner tests this iteration, prioritize any
remaining basic authoring/lifecycle defects before expanding connector coverage
or beginning the newer-Java migration. YAML remains deferred.

## Immediate milestone: reproducible internal release

The same committed Studio/runtime pair must build on a clean machine, install
with its dependencies, and pass the supported workflows without a prepared
developer environment.

### Clean-build foundation

The historical bootstrap failure is repaired. Maven 3.9.11 is checksum-pinned,
duplicate JAXB declarations are corrected, private bootstrap copies are excluded
from the main-source license scan, and initial clean builds provision required
plugins. Empty-cache CI source builds passed. Later repeat builds use cached
adapted dependencies while recompiling the tracked engine from source.

Native Java and HTTP inputs now come from durable checksum-pinned downloads.
Assembly verifies source-build success gates and the exact engine SHA, records
clean/dirty/resumed provenance, and includes corresponding source and notices.
See `DEV_TEST_VERIFICATION.md` for the verified code revision, final matrix and
owner-test package.

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
