#!/usr/bin/env bash
# Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p zernova-build-evidence
EVIDENCE="$PWD/zernova-build-evidence"
COMMON=(-B -V -e -DskipTests -DskipVerifications=true -DskipRevapiCheck=true -Drevapi.skip=true)
stage() {
  local name="$1"; shift
  set +e
  "$@" 2>&1 | tee "$EVIDENCE/$name.log"
  local result=${PIPESTATUS[0]}
  set -e
  printf '%s\n' "$result" > "$EVIDENCE/$name-exit-code.txt"
  return "$result"
}
stage stabilization python3 zernova-build/stabilize-build.py
stage api-preparation python3 zernova-build/prepare-api.py
stage extensions-api-preparation python3 zernova-build/prepare-extensions-api.py
stage route-multiplicity python3 zernova-build/restore-route-multiplicity.py
git diff -- pom.xml > "$EVIDENCE/dependency-adaptation.patch"
stage branding python3 zernova-build/brand-source.py
stage http-sdk-adaptation python3 zernova-build/adapt-http-sdk.py
stage http-entity-adaptation python3 zernova-build/adapt-http-entities.py
stage source-license-headers python3 zernova-build/preserve-license-headers.py
stage boot-module-adaptation python3 zernova-build/adapt-boot-module.py
stage product-identifiers python3 zernova-build/preserve-product-identifiers.py
git diff > "$EVIDENCE/complete-source-changes.patch"
sha256sum LICENSE.txt > "$EVIDENCE/preserved-license.sha256"
mvn --version > "$EVIDENCE/maven.txt"
for component in mule-api mule-extensions-api; do
  stage "$component-build" mvn "${COMMON[@]}" -f ".zernova-upstream/$component/pom.xml" install
done
# This API intentionally depends on Mockito-inline 4.11 for Java 8 compatibility.
# Do not inject the newer parent's Mockito 5 agent into that test JVM. Mockito 4's
# own inline instrumentation remains active; no assertions or tests are skipped.
printf 'Extensions API tests use their declared Mockito-inline 4.11.0. The incompatible inherited Mockito 5 javaagent argument is omitted; inline mocking and all selected assertions execute.\n' > "$EVIDENCE/route-test-instrumentation.txt"
stage route-model-tests mvn -B -V -e -f .zernova-upstream/mule-extensions-api/mule-extensions-api/pom.xml -Dtest=ExtensionModelUtilsTestCase -DskipTests=false -Dsurefire.args.mockito= -DskipVerifications=true -DskipRevapiCheck=true -Drevapi.skip=true test
stage source-build mvn "${COMMON[@]}" install
cp "$EVIDENCE/source-build-exit-code.txt" "$EVIDENCE/build-exit-code.txt"
printf 'Kernel/API compilation passed; all reactor tests compiled. Route-model tests executed; other tests not executed in compilation gate. Historical API comparisons skipped. Full assembly and real execution remain separate.\n' > "$EVIDENCE/SCOPE.txt"
stage http-bridge-tests mvn -B -V -e -f modules/http-support/pom.xml -DskipTests=false -DskipVerifications=true -DskipRevapiCheck=true -Drevapi.skip=true test
stage distribution-preparation python3 zernova-build/prepare-distribution.py
stage distribution-build mvn "${COMMON[@]}" -f .zernova-upstream/mule-distributions/pom.xml -pl standalone -am install
printf 'Kernel/API compilation, route-model and HTTP bridge unit tests, and full standalone assembly passed. Live integration execution and broader conformance remain separate.\n' > "$EVIDENCE/SCOPE.txt"
