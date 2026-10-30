# ZERNOVA XML application format 1

The 0.4.0 alpha introduces an independent executable XML/deployment contract.
YAML is deferred. Runtime product version: `0.4.0-alpha.1`; Studio extension
version: `0.4.0`. The inherited engine Maven/source baseline remains explicitly
`4.10.0-SNAPSHOT`; it is provenance and connector SDK compatibility, not the
ZERNOVA product version.

New integration sources live in `src/main/zernova/`. Their root is `<zernova>`.
Core, HTTP and documentation identifiers are respectively
`https://schemas.zernova.dev/core`, `/http` and `/documentation`. Schema locations
use `current/zernova.xsd` and `current/zernova-http.xsd`. These identifiers resolve
from bundled runtime/connector resources; execution must not fetch schemas online.

The application descriptor is `zernova-artifact.json`, with `requiredProduct:
ZERNOVA` and `minZernovaVersion: 0.4.0`. Packaged descriptors live under
`META-INF/zernova-artifact/`; classifiers/loaders use ZERNOVA identifiers.
Application minimum versions are compared against the independent product
version; adapted third-party connector compatibility floors retain their upstream
SDK baseline. `zernova-app.json` schema version 1 defines the offline build input.

Studio builds XML/resources with the included HTTP 1.12.2 dependency profile.
Its POM is project metadata; no external Maven plugin, Java installation or
runtime location is needed. Custom Java/additional dependencies remain rejected.
Connector artifact coordinates inside the private bundled dependency closure
still document their upstream origins. Namespace migration does not grant
compatibility with arbitrary unmodified third-party connectors.

`ZERNOVA: Import Legacy XML Project` converts supported projects into a new
separate destination. It changes XML contract names/namespace attributes, not
application expressions or text. Existing Maven build plugins/tests are not
copied or executed; HTTP is bound to the bundled profile. Review the generated
migration report, XML diagnostics and real application behavior. This is a
bounded migration tool, not a compatibility guarantee for all Mule applications.

The visual designer edits HTTP Listener/core Logger flows and Logger subflows.
Unsupported XML is retained and made read-only in the visual projection. Cards
use intrinsic content width and return paths attach to the last actual processor
component boundary. A subflow has no HTTP source and does not draw a fictitious Listener.

Required upstream licenses, authorship and historical issue references remain
accurate. Internal inherited engine Maven module names/launch compatibility
properties are distinct from the new XML/application contract.

Runtime state is separated by product version in Studio. The standalone control
API rejects state owned by another runtime/application-format version instead
of labelling an older JVM as current. Stop the previous version before upgrade;
retain its package if needed to stop its existing instance.
