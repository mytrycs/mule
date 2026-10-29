#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
"""Capture successful compilation outputs and corresponding source, not a runtime release."""
from pathlib import Path
import gzip
import hashlib
import io
import json
import tarfile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT/'zernova-build-evidence'
if (OUT/'build-exit-code.txt').read_text().strip() != '0':
    raise SystemExit('Refusing to label a failed compilation as source-build output')
DEST = ROOT/'zernova-build-output'
DEST.mkdir(exist_ok=True)
entries = {}
ignored = {'.git','target','__pycache__','.zernova-upstream','zernova-build-evidence','zernova-build-output'}
fonts = {'.ttf','.otf','.woff','.woff2','.eot','.ttc'}
for label, base in [('kernel-source',ROOT),('api-source',ROOT/'.zernova-upstream/mule-api')]:
    for path in sorted(base.rglob('*')):
        rel = path.relative_to(base)
        if any(part in ignored for part in rel.parts) or path.is_dir():
            continue
        if path.is_symlink() or not path.resolve().is_relative_to(base.resolve()):
            raise SystemExit('Unsafe source path: '+str(rel))
        if path.suffix.lower() in fonts:
            raise SystemExit('Font file is not permitted in a delivery: '+str(rel))
        entries[label+'/'+rel.as_posix()] = path
for path in sorted(ROOT.rglob('*.jar')):
    rel = path.relative_to(ROOT)
    if 'target' in rel.parts and '.git' not in rel.parts:
        if path.is_symlink():
            raise SystemExit('Unexpected linked build output')
        entries['compiled/'+rel.as_posix()] = path
for path in sorted(OUT.rglob('*')):
    if path.is_file() and not path.is_symlink():
        entries['evidence/'+path.relative_to(OUT).as_posix()] = path
manifest = {name: {'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'bytes':path.stat().st_size}
            for name,path in entries.items()}
archive = DEST/'ZERNOVA_4.10_Compiled_Source_Checkpoint.tar.gz'
with archive.open('wb') as raw, gzip.GzipFile(fileobj=raw,mode='wb',mtime=0,filename='') as gz, tarfile.open(fileobj=gz,mode='w') as tar:
    for name,path in sorted(entries.items()):
        info = tar.gettarinfo(str(path),arcname=name)
        info.uid=info.gid=0
        info.uname=info.gname=''
        info.mtime=0
        with path.open('rb') as source:
            tar.addfile(info,source)
    data = (json.dumps({'scope':'Compiled kernel/API plus exact sources; NOT an assembled or execution-tested runtime','files':manifest},indent=2)+'\n').encode()
    info=tarfile.TarInfo('SOURCE_BUILD_MANIFEST.json')
    info.size=len(data);info.mode=0o644;info.mtime=0
    tar.addfile(info,io.BytesIO(data))
sha=hashlib.sha256(archive.read_bytes()).hexdigest()
(DEST/'SHA256SUMS.txt').write_text(sha+'  '+archive.name+'\n')
print(json.dumps({'archive':str(archive),'sha256':sha,'files':len(entries),'scope':'Compilation checkpoint, not runtime readiness'},indent=2))
