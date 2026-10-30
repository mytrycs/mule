# ZERNOVA 0.4.0 internal development-test delivery

This records the bounded owner-feedback release, not production qualification.
Studio VSIX version: **0.4.0**. Bundled ZERNOVA Runtime: **0.4.0-alpha.1**.
Product application format: **1**. Engine provenance/SDK baseline: **4.10**.
YAML is deferred.

## Implemented

- Pinned toolchain, repaired clean source bootstrap and durable checksum-pinned
  Java/HTTP inputs. Clean CI builds the renamed Java engine from source.
- `com.zernova` Java identity, native `<zernova>` XML and bundled ZERNOVA schema
  resolution. Studio generation, descriptors, offline packaging and runtime agree
  on the format. No deploy-time legacy-XML translation is needed.
- Independent runtime version, Studio/runtime pairing, compatible-instance guards
  and explicit legacy import for the supported subset.
- Native Java + engine + HTTP dependencies + lifecycle agent + corresponding
  source + checksums + notices packaged inside the platform-specific VSIX.
- Return paths start at the final processor, end at the Listener and clear labels.
  Flow/subflow cards grow and shrink with their actual components. Subflows do
  not invent a Listener or HTTP response path.
- Versioned HTTP/Logger owner-test sample and representative Orders/failure/
  recovery runtime fixtures.

## Local verification

- 11 relocator tests passed, including JVM linkage/metadata/serialization and
  derived packaging enum names.
- 428 selected Java tests reported: 427 passed, one inherited container skip,
  zero failures/errors. This is a selected suite, not the entire upstream suite.
- Studio typecheck/build and 62 unit/backend/XML tests plus three geometry checks
  passed.
- All 20 real-runtime cases passed on this Intel Mac, including native Java
  integrity, offline packaging, actual HTTP/Logger, JSON transformation/validation,
  route redeployment, invalid expressions/configuration, cancellation, ownership,
  occupied ports, shutdown/restart and JVM crash/recovery.
- Installed VSIX on stable VS Code 1.144.0: nine acceptance cases passed without
  external Java/Maven/runtime configuration. VS Code Insiders: nine also passed.
- A normally installed extension was separately inspected through the actual UI:
  main flow one/two/one Logger widths approximately 375/520/375 px; subflow widths
  210/344/210 px; final-component return anchors and narrow-window alignment pass.
- The supplied simple sample returned `Hello from ZERNOVA` at port 8081 and logged
  `ZERNOVA_TEST dev-test-delivery-040`. Authenticated shutdown completed gracefully.

Local source builds were used during diagnosis and honestly carry dirty/resumed
provenance. The delivery uses a clean CI artifact; its provenance and final matrix
results are recorded below.

Evidence: `verification/studio-0.4.0-devtest/`.
Instructions: `zernova-studio-kernel/DEV_TEST_START_HERE.md`.

## Limits

This is a VS Code extension, not a standalone IDE installer. The supported
visual/offline-build slice is core plus HTTP Listener/Logger. Arbitrary connector
dependencies, custom Java and YAML remain deferred. Required upstream notices,
historical identifiers and inherited internal coordinates remain where needed;
this is not a promise that every historical occurrence of “Mule” is gone.

Windows acceptance reproduced HTTP 503 after in-process application replacement,
even using a fresh TCP connection. That disproved the initial pooled-connection
explanation. The development lifecycle now restarts the project's private JVM
before replacement deployment, using authenticated shutdown under the same
mutation lock. Acceptance explicitly requires a changed runtime run ID and real
HTTP/Logger success. Requests are never retried. This alpha offers reliable
restart-based redeployment; seamless in-process replacement remains future work.

## Final clean CI delivery

Verified code revision: `e098822d2f4600f417b611e4d71cda2885301f5d`.
[Final CI run](https://github.com/mytrycs/mule/actions/runs/41072634257): **success**.
All four source-build gates passed; the source tree was clean and the build was
not resumed. Engine SHA-256:
`33ece1c37c001e293652194802d10573ad0c5f08f50a27e08e4229d200786864`.

| Native target | Real runtime acceptance | Installed Studio on stable VS Code |
| --- | --- | --- |
| macOS x64 | 20 passed | 9 passed |
| macOS ARM64 | 20 passed | 9 passed |
| Windows x64 | 20 passed | 9 passed |
| Windows ARM64 | 20 passed | 9 passed |
| Linux x64 | 20 passed | 9 passed |
| Linux ARM64 | 20 passed | 9 passed |

Each job also passed Studio typecheck, unit tests, package integrity checks and
installation of its native VSIX. This qualifies the tested development slice on
these targets, not arbitrary applications or production operation. CI artifacts
for the other five targets remain available from the linked run.

The owner delivery is the Intel Mac VSIX plus `http-logger-devtest`, quickstart,
checksums and verification notes. The runtime source archive, Java and dependency
notices are inside the VSIX. `delivered-bundle.json`, `vsix-sha256.txt`, the matrix
job record and native result files are retained under the evidence directory.
Documentation/evidence-only commits may follow the verified code revision.

The exact downloaded Intel Mac CI VSIX was installed again locally in an isolated
stable VS Code 1.144.0 profile. All nine cases passed, including actual HTTP and
fresh Logger output, XML edit/redeploy, undeploy, shutdown, invalid XML rejection
and restored-document recovery. The installed bundle's clean source revision
and VSIX checksum were checked against this delivery. The runtime stopped cleanly.
