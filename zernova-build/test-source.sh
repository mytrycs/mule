#!/usr/bin/env bash
# Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
set -euo pipefail
cd "$(dirname "$0")/.."
COMMON=(-B -s zernova-build/settings.xml "-Dmaven.repo.local=$PWD/.zernova-m2"
        -DskipTests=false -DskipVerifications=true -DskipRevapiCheck=true -Drevapi.skip=true)
mvn "${COMMON[@]}" -pl manifest,modules/boot-api,modules/container,modules/launcher,modules/http-support test
mvn "${COMMON[@]}" -f upstream/runtime-api/pom.xml '-Dtest=MediaType*TestCase' test
mvn "${COMMON[@]}" -f upstream/extensions-api/mule-extensions-api/pom.xml -Dtest=ExtensionModelUtilsTestCase test
