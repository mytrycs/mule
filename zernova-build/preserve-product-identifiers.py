#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
"""Keep product-compatibility identifiers while using ZERNOVA presentation metadata."""
from pathlib import Path
import hashlib,json,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'zernova-build-evidence'; OUT.mkdir(exist_ok=True)
records=[]
def write(path,text,why):
    old=path.read_bytes(); ET.fromstring(text);path.write_text(text)
    records.append({'path':str(path.relative_to(ROOT)),'reason':why,'before_sha256':hashlib.sha256(old).hexdigest(),'after_sha256':hashlib.sha256(path.read_bytes()).hexdigest()})
p=ROOT/'pom.xml';text=p.read_text()
assert '<zernovaProductVersion>0.3.0-dev.1</zernovaProductVersion>' in text
text=text.replace('<zernovaProductVersion>0.3.0-dev.1</zernovaProductVersion>',
 '<zernovaProductVersion>0.3.0-dev.1</zernovaProductVersion>\n        <zernovaCompatibilityTitle>${project.name}</zernovaCompatibilityTitle>',1)
anchor='<Zernova-Product-Version>${zernovaProductVersion}</Zernova-Product-Version>'
assert anchor in text
text=text.replace(anchor,anchor+'\n                                <Zernova-Product-Name>ZERNOVA Runtime</Zernova-Product-Name>\n                                <Implementation-Title>${zernovaCompatibilityTitle}</Implementation-Title>',1)
write(p,text,'Separate product display fields from the Implementation-Title consumed by Product.getProductByName.')
for rel,value in [('core/pom.xml','Mule Core'),('runtime-extension-model/pom.xml','Mule 4 Runtime Extension model'),('manifest/pom.xml','Mule Manifest')]:
    p=ROOT/rel;text=p.read_text();assert '<properties>' in text
    text=text.replace('<properties>','<properties>\n        <!-- Technical product identity; the visible project name remains ZERNOVA. -->\n        <zernovaCompatibilityTitle>'+value+'</zernovaCompatibilityTitle>',1)
    write(p,text,'Preserve the exact community-engine identifier; do not alter enterprise compatibility checks.')
(OUT/'product-identifier-audit.json').write_text(json.dumps({'changes':records,'engine_product':'MULE','enterprise_product_checks':'UNCHANGED','reason':'Runtime startup established that three JAR Implementation-Title values are protocol identifiers, not merely display strings. ZERNOVA module names, organization, banner and custom product metadata remain branded.'},indent=2)+'\n')
print('Preserved three technical product identifiers; ZERNOVA display branding retained.')
