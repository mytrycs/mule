#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
"""Expose the existing public boot-wrapper signature dependency through JPMS."""
from pathlib import Path
import hashlib,json
ROOT=Path(__file__).resolve().parents[1]
path=ROOT/'modules/boot-api/src/main/java/module-info.java'
old=path.read_bytes(); text=old.decode()
assert text.count('requires org.apache.commons.cli;')==1
text=text.replace('requires org.apache.commons.cli;',
    '// Public wrapper signatures use CommandLine; consumers must read its module.\n  requires transitive org.apache.commons.cli;',1)
# Preserve the inherited copyright block exactly. The following separate notice
# identifies this derivative-source change without replacing upstream ownership.
end=text.index('*/')+2
text=text[:end]+'\n/* ZERNOVA module metadata correction Copyright (c) 2026 ZerNova. CPAL-1.0. */'+text[end:]
path.write_text(text)
out=ROOT/'zernova-build-evidence'; out.mkdir(exist_ok=True)
(out/'boot-module-adaptation.json').write_text(json.dumps({'path':str(path.relative_to(ROOT)),
    'reason':'MuleContainerWrapper exposes commons-cli CommandLine in its signature. The released module analyzer correctly requires that dependency to be exported. Declare a transitive JPMS dependency instead of disabling the analyzer.',
    'before_sha256':hashlib.sha256(old).hexdigest(),'after_sha256':hashlib.sha256(path.read_bytes()).hexdigest(),
    'license':'CPAL-1.0','runtime_behavior_changed':False},indent=2)+'\n')
print('Corrected boot API transitive dependency; module analysis remains enabled.')
