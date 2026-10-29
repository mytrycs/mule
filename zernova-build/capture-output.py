#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
"""Capture compilation outputs with their exact sources; no execution claim."""
from pathlib import Path
import gzip
import hashlib
import io
import json
import tarfile
import zipfile
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'zernova-build-evidence'
gate=OUT/'source-build-exit-code.txt'
if not gate.exists() or gate.read_text().strip()!='0':
    raise SystemExit('Refusing to label a failed compilation as source-build output')
DEST=ROOT/'zernova-build-output'; DEST.mkdir(exist_ok=True)
entries={}
ignored={'.git','target','__pycache__','.zernova-upstream','zernova-build-evidence','zernova-build-output'}
fonts={'.ttf','.otf','.woff','.woff2','.eot','.ttc'}
bases=[('kernel-source',ROOT)]
upstream=ROOT/'.zernova-upstream'
if upstream.exists():
    bases += [(p.name+'-source',p) for p in sorted(upstream.iterdir()) if p.is_dir()]
for label,base in bases:
    for path in sorted(base.rglob('*')):
        rel=path.relative_to(base)
        if any(part in ignored for part in rel.parts) or path.is_dir(): continue
        if path.is_symlink() or not path.resolve().is_relative_to(base.resolve()):
            raise SystemExit('Unsafe source path: '+str(rel))
        if path.suffix.lower() in fonts:
            raise SystemExit('Font file is not permitted in delivery: '+str(rel))
        entries[label+'/'+rel.as_posix()]=path
for path in sorted(ROOT.rglob('*.jar')):
    rel=path.relative_to(ROOT)
    if 'target' in rel.parts and '.git' not in rel.parts:
        if path.is_symlink(): raise SystemExit('Unexpected linked build output')
        entries['compiled/'+rel.as_posix()]=path
assembly_gate=OUT/'distribution-build-exit-code.txt'
assembly_pass=assembly_gate.exists() and assembly_gate.read_text().strip()=='0'
if assembly_pass:
    archives=list((upstream/'mule-distributions/standalone/target').glob('mule-standalone-*.zip'))
    if len(archives)!=1: raise SystemExit('Expected one actual standalone ZIP')
    with zipfile.ZipFile(archives[0]) as z:
        if any(Path(n).suffix.lower() in fonts for n in z.namelist()):
            raise SystemExit('Unexpected font in standalone distribution')
    entries['standalone-assembly/'+archives[0].name]=archives[0]
for path in sorted(OUT.rglob('*')):
    if path.is_file() and not path.is_symlink():
        entries['evidence/'+path.relative_to(OUT).as_posix()]=path
manifest={name:{'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'bytes':path.stat().st_size} for name,path in entries.items()}
archive=DEST/'ZERNOVA_4.10_Compiled_Source_Checkpoint.tar.gz'
with archive.open('wb') as raw,gzip.GzipFile(fileobj=raw,mode='wb',mtime=0,filename='') as gz,tarfile.open(fileobj=gz,mode='w') as tar:
    for name,path in sorted(entries.items()):
        info=tar.gettarinfo(str(path),arcname=name); info.uid=info.gid=0; info.uname=info.gname=''; info.mtime=0
        with path.open('rb') as source: tar.addfile(info,source)
    data=(json.dumps({'scope':'Source compilation checkpoint; live runtime execution is not established here','standalone_assembly_passed':assembly_pass,'files':manifest},indent=2)+'\n').encode()
    info=tarfile.TarInfo('SOURCE_BUILD_MANIFEST.json'); info.size=len(data); info.mode=0o644; info.mtime=0
    tar.addfile(info,io.BytesIO(data))
sha=hashlib.sha256(archive.read_bytes()).hexdigest()
(DEST/'SHA256SUMS.txt').write_text(sha+'  '+archive.name+'\n')
print(json.dumps({'archive':str(archive),'sha256':sha,'files':len(entries),'standalone_assembly_passed':assembly_pass,'scope':'Source/assembly checkpoint, not execution readiness'},indent=2))
