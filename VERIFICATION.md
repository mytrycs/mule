# ZERNOVA namespace refactor verification

Validated locally on **October 30, 2026**, on **macOS x64**, using Java 17 and
Node 22.23.3. Runtime version: **0.3.0-dev.2**; engine source baseline:
**4.10.0-SNAPSHOT**.

## Passed

- Matching runtime API and extension API compiled from tracked `com.zernova`
  sources, including regenerated ZERNOVA DSQL lexer/parser classes.
- All **123 kernel reactor modules** built successfully; test sources compiled.
- Standalone distribution assembled successfully. Its engine JARs come from the
  renamed source build, rather than post-build relocation of the old core.
- **424 selected unit tests reported: 423 passed, one inherited skip**, with no
  failures or errors. Coverage includes manifest, boot, container, launcher,
  HTTP support, API media types, all five migrated serialization fixtures, and
  extension route-model boundaries.
- **Eight relocation regression tests passed**, including actual JVM linkage,
  reflection, modified UTF-8, serialization, nested archives, service/Sisu/Spring
  metadata, Maven POMs, signature handling, and URL preservation.
- Recursive audit passed for **7,272 Java source files**, **372 runtime archives**
  and **90,079 class entries**, with no old Java namespace or Mule-named runtime
  classes under `com.zernova`.
- **All 16 native application acceptance checks passed** against the freshly
  source-built engine and bundled Java: offline deterministic packaging, XML
  rejection, occupied-port handling, startup, authenticated control, HTTP
  Listener and correlated Logger output, ownership reconciliation, route
  redeployment, real expression/configuration failures, shutdown and restart.
- Standalone CLI verified in separate processes: doctor, build/run, status,
  real HTTP 200 response, logs, authenticated stop and final `STOPPED` state.
- At the original import checkpoint, all **79 Studio files** matched the supplied
  0.2.0 ZIP byte-for-byte. Subsequent 0.3.0 host integration is documented
  separately below. The root engine license remains unchanged.

The inherited skipped container test is
`actualContainerClassLoaderNotAccessibleThroughParents`, disabled upstream for
`W-13829761, W-13205329, W-13829740`. No additional tests were disabled.

Machine-readable results are in
[`verification/namespace-0.3.0-dev.2/local.json`](verification/namespace-0.3.0-dev.2/local.json).
Detailed local logs remain in the ignored `zernova-build-evidence/` and
`zernova-studio-build/evidence/` directories. Tokens are excluded from committed
evidence. The local native bundle is `zernova-studio-build/runtime/`.

## Reproduce

```sh
bash zernova-build/build-source.sh
bash zernova-build/test-source.sh
npm ci
npm run build:portable
# Prepare matching native Java/HTTP inputs if not already present, then:
python3 zernova-studio-build/prepare-native.py darwin-x64
cd zernova-studio-build
node tests/native-runtime.cjs
```

See [NAMESPACE_MIGRATION.md](NAMESPACE_MIGRATION.md) for input provisioning and
standalone CLI commands. Historical native input recovery uses retained GitHub
artifacts; preserve the downloaded inputs when their upstream retention expires.
The final runtime itself operates offline with bundled Java and HTTP libraries.

## Scope

The complete upstream test suite was not executed. The **new namespace** has
local native acceptance on macOS x64; earlier six-platform results apply to the
previous namespace. `.github/workflows/zernova-namespace.yml` builds this source
and schedules fresh acceptance on all six platforms after push; its results are
separate from these local results.

The bundle records its source archive checksum and source-tree commit. Local
compilation occurred before the source commits were finalized, so its build
provenance explicitly records `sourceBuildDirty: true`; it does not claim that
the original baseline commit contains the renamed source.

XML/deployment compatibility identifiers, legacy method/field names and required
upstream attribution remain intentionally preserved. Subsequent local Mac Studio integration is recorded in
`zernova-studio-kernel/STUDIO_LOCAL_VERIFICATION.md`. Cross-platform installed
Studio and customer release packaging remain separate gates.
