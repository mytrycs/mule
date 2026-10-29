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
stage source-build mvn "${COMMON[@]}" install
cp "$EVIDENCE/source-build-exit-code.txt" "$EVIDENCE/build-exit-code.txt"
printf 'Kernel/API compilation passed; tests compiled but not executed in that gate. Historical API comparisons skipped. Full assembly and real execution remain separate.\n' > "$EVIDENCE/SCOPE.txt"
stage http-bridge-tests mvn -B -V -e -f modules/http-support/pom.xml -DskipTests=false -DskipVerifications=true -DskipRevapiCheck=true -Drevapi.skip=true test
stage distribution-preparation python3 zernova-build/prepare-distribution.py
stage distribution-build mvn "${COMMON[@]}" -f .zernova-upstream/mule-distributions/pom.xml -pl standalone -am install
printf 'Kernel/API compilation, HTTP bridge unit tests, and standalone assembly passed. Live integration execution and broader conformance remain separate.\n' > "$EVIDENCE/SCOPE.txt"
