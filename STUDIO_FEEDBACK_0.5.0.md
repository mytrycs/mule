# ZERNOVA Studio 0.5.0 — core catalog and resource authoring

This release implements the full category list in the supplied core-components
reference as native engine components or original ZERNOVA support components.
It adds nested scopes/routes, transformers and Scheduler authoring; a deployment
console with the Z logo and build/deploy stages; optional Transform outputs with
external DW resources; HTTP error responses; and global/environment properties.

The engine is still 0.4.1-alpha.1 and the support module is 0.5.0. The known 0.4.4
support JAR can be archived and upgraded inside a private runtime instance. Unknown
modified JARs are refused. This release does not silently replace the base engine.

Performance changes target deployment waiting (configurable 1000 ms scan versus
5000 ms legacy default) and deployment-time caching of external scripts. They do
not establish a general throughput improvement. See release evidence for measured
local deployment timings, and KNOWN_LIMITATIONS.md for exact component semantics.

Reference: https://github.com/mulesoft/docs-mule-runtime/blob/v4.13/modules/ROOT/pages/about-components.adoc
Properties: https://docs.mulesoft.com/mule-runtime/4.10/configuring-properties
DataWeave: https://docs.mulesoft.com/dataweave/latest/dataweave-language-guide

The test handoff contains a simple sample on localhost:18081 and additional core
and Batch examples. The local qualification target is Mac x64; other installed
Studio targets are not newly qualified in this iteration.
