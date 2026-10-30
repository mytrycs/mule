#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. Original Studio packaging regression preparation.
from pathlib import Path
import hashlib,json
root=Path(__file__).resolve().parent
p=root/'src/portable/runtime.ts'
old='8ba5e2646fbbe1b138564a3bec47dc07eb4867b48938d8d2b08e61769810b4bf'
new='61c1e5f6242ffbf6f62b4f3181ee8fe0114e9b89a4802c3eb1b149e8322e7541'
assert hashlib.sha256(p.read_bytes()).hexdigest()==old
text=p.read_text()
a="for (const sub of ['apps', 'logs'])"
b="// VSIX archives omit empty directories; restore the verified engine layout in private state.\n    for (const sub of ['apps', 'logs', 'domains', 'domains/default', 'lib', 'lib/user'])"
assert text.count(a)==1
p.write_text(text.replace(a,b),newline='\n')
assert hashlib.sha256(p.read_bytes()).hexdigest()==new
prep=root/'prepare-native.py';text=prep.read_text();assert old in text
text=text.replace(old,new)
anchor="unzip(engine,OUT/'engine')"
assert text.count(anchor)==1
text=text.replace(anchor,anchor+"\n# Match VSIX extraction: empty directories are not stored by the extension packager.\nfor directory in sorted((OUT/'engine').rglob('*'),key=lambda p:len(p.parts),reverse=True):\n if directory.is_dir() and not any(directory.iterdir()):directory.rmdir()\nassert not (OUT/'engine/domains').exists(), 'Packaging regression precondition must hold'\n")
prep.write_text(text,newline='\n')
(root/'vsix-layout-regression.json').write_text(json.dumps({'originalSourceSha256':old,'testedSourceSha256':new,'regression':'VSIX omits empty domains/default and lib/user directories. Native acceptance now runs with those directories absent from the immutable bundle; the adapter restores them only inside owned runtime state.'},indent=2)+'\n')
