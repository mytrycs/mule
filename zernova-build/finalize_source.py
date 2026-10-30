#!/usr/bin/env python3
"""Bind successful compilation gates to the exact assembled engine archive.
Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0.
"""
from pathlib import Path
import hashlib
import json
import subprocess

ROOT = Path(__file__).resolve().parents[1]
EVIDENCE = ROOT / 'zernova-build-evidence'
for name in ['api-build', 'extensions-api-build', 'source-build', 'distribution-build']:
    if (EVIDENCE / (name + '-exit-code.txt')).read_text().strip() != '0':
        raise SystemExit('Source gate has not passed: ' + name)
engine = ROOT / 'upstream/distributions/standalone/target/mule-standalone-4.10.0-SNAPSHOT.zip'
record = EVIDENCE / 'source-build-revision.json'
info = json.loads(record.read_text())
info['engineSha256'] = hashlib.sha256(engine.read_bytes()).hexdigest()
info['completionRevision'] = subprocess.check_output(['git', '-C', str(ROOT), 'rev-parse', 'HEAD'], text=True).strip()
info['treeChangedDuringBuild'] = info['completionRevision'] != info['sourceRevision'] or bool(subprocess.check_output(['git', '-C', str(ROOT), 'status', '--porcelain'], text=True).strip())
info['resumedLocalBuild'] = (EVIDENCE / 'source-build-resumed.json').exists()
record.write_text(json.dumps(info, indent=2) + '\n')
print(json.dumps(info, indent=2))
