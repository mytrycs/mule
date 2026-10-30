#!/usr/bin/env bash
# Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
# Historical preparation scripts are bootstrap recipes; do not apply them to
# this refactored working tree. Compile the tracked, renamed sources below.
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p zernova-build-evidence
rm -f zernova-build-evidence/{api-build,extensions-api-build,source-build,distribution-build}-exit-code.txt
export PATH="$(python3 zernova-build/toolchain.py):$PATH"
mvn -version > zernova-build-evidence/toolchain.txt
python3 zernova-build/bootstrap_dependencies.py "$@"
python3 -m unittest discover -s zernova-build/tests -v
python3 zernova-build/audit_namespace.py --output zernova-build-evidence/source-audit.json
python3 - <<'PY'
from pathlib import Path
import json, subprocess
record = {'sourceRevision': subprocess.check_output(['git', 'rev-parse', 'HEAD'], text=True).strip(),
          'dirty': bool(subprocess.check_output(['git', 'status', '--porcelain'], text=True).strip())}
Path('zernova-build-evidence/source-build-revision.json').write_text(json.dumps(record, indent=2) + '\n')
PY
COMMON=(-o -B -s zernova-build/settings.xml "-Dmaven.repo.local=$PWD/.zernova-m2"
        -DskipTests -DskipVerifications=true -DskipRevapiCheck=true -Drevapi.skip=true)
stage() {
  local name="$1"; shift
  set +e
  "$@" 2>&1 | tee "zernova-build-evidence/$name.log"
  local result=${PIPESTATUS[0]}
  set -e
  printf '%s\n' "$result" > "zernova-build-evidence/$name-exit-code.txt"
  return "$result"
}
stage api-build mvn "${COMMON[@]}" -f upstream/runtime-api/pom.xml clean install
stage extensions-api-build mvn "${COMMON[@]}" -f upstream/extensions-api/pom.xml clean install
stage source-build mvn "${COMMON[@]}" clean install
stage distribution-build mvn "${COMMON[@]}" -f upstream/distributions/pom.xml -pl standalone -am clean install
printf 'Renamed APIs, complete kernel reactor and standalone distribution compiled from tracked com.zernova sources. Tests compiled; runtime acceptance and selected unit tests are separate gates.\n' > zernova-build-evidence/SCOPE.txt
