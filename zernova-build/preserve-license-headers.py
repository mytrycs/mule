#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
"""Preserve exact inherited headers and validate separate headers for new code."""
from pathlib import Path
import hashlib,json,re,subprocess,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'zernova-build-evidence'; OUT.mkdir(exist_ok=True)
new_names=[
 'src/main/java/org/mule/runtime/http/support/internal/HttpProtocolCompatibility.java',
 'src/test/java/org/mule/runtime/http/support/internal/HttpProtocolCompatibilityTestCase.java',
 'src/main/java/org/mule/runtime/http/support/internal/message/Part.java',
 'src/main/java/org/mule/runtime/http/support/internal/message/MultipartEntityView.java',
]
plain='''Copyright (c) 2026 ZerNova.
The software in this file is published under the terms of the CPAL v1.0
license, a copy of which is included in the root LICENSE.txt file.'''
header='/*\n'+''.join(' * '+line+'\n' for line in plain.splitlines())+' */'
tracked=subprocess.check_output(['git','-C',str(ROOT),'ls-files','*.java'],text=True).splitlines()
records=[]
for rel in tracked:
    p=ROOT/rel
    raw=p.read_bytes()
    if b'ZERNOVA' not in raw[:700] or b'Copyright (c) 2026 ZerNova' not in raw[:700]:continue
    # Unmodified legacy-encoded fixtures must not be decoded or rewritten.
    text=raw.decode('utf-8')
    original=subprocess.check_output(['git','-C',str(ROOT),'show','HEAD:'+rel]).decode('utf-8')
    match=re.match(r'/\*.*?\*/',original,re.S)
    assert match and ('Copyright' in match.group(0)),rel
    inherited=match.group(0)
    current=re.match(r'/\*.*?\*/',text,re.S);assert current,rel
    additions=[line for line in current.group(0).splitlines() if 'ZERNOVA' in line and 'Copyright (c) 2026 ZerNova' in line]
    assert additions,rel
    remainder=text[current.end():]
    note='\n/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */'
    text=inherited+note+remainder
    assert text.startswith(inherited)
    p.write_text(text)
    records.append({'path':rel,'inherited_header_preserved':True,'inherited_header_sha256':hashlib.sha256(inherited.encode()).hexdigest()})
for rel in new_names:
    p=ROOT/'modules/http-support'/rel
    text=p.read_text();match=re.match(r'/\*.*?\*/',text,re.S);assert match
    previous=match.group(0)
    text=header+'\n'+previous+text[match.end():]
    p.write_text(text)
    records.append({'path':str(p.relative_to(ROOT)),'original_zernova_file':True,'license':'CPAL-1.0'})
pom=ROOT/'pom.xml';text=pom.read_text()
needle='''<groupId>com.mycila</groupId>
                <artifactId>license-maven-plugin</artifactId>'''
assert text.count(needle)==1
excludes=''.join('<exclude>**/'+name+'</exclude>' for name in new_names)
includes=''.join('<include>**/'+name+'</include>' for name in new_names)
config='''
                <configuration>
                    <licenseSets combine.self="override">
                        <licenseSet>
                            <header>${licensePath}</header>
                            <includes><include>**/*.java</include></includes>
                            <excludes>'''+excludes+'''</excludes>
                        </licenseSet>
                        <licenseSet>
                            <inlineHeader><![CDATA['''+plain+''']]></inlineHeader>
                            <includes>'''+includes+'''</includes>
                        </licenseSet>
                    </licenseSets>
                </configuration>'''
text=text.replace(needle,needle+config,1)
ET.fromstring(text);pom.write_text(text)
(OUT/'source-header-audit.json').write_text(json.dumps({'scope':'Keep inherited copyright headers exact; new covered files use ZerNova copyright and CPAL. License plugin remains enabled.','files':records},indent=2)+'\n')
print('Validated separate source ownership headers:',len(records))
