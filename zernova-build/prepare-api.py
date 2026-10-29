#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. CPAL-1.0; see ../LICENSE.txt.
# Source-build dependency restoration, not a binary patch or compatibility bypass.
from pathlib import Path
import hashlib
import json
import re
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'zernova-build-evidence'
OUT.mkdir(exist_ok=True)
DEST = ROOT / '.zernova-upstream' / 'mule-api'
COMMIT = 'acf394e596fb41df1649ae00b2d5c7e4377078b8'
VERSION = '1.10.0-zernova.1'
NS = {'m': 'http://maven.apache.org/POM/4.0.0'}
if DEST.exists():
    raise SystemExit('Refusing to replace an existing API source directory')
DEST.mkdir(parents=True)
for args in [ ['git','init',str(DEST)],
              ['git','-C',str(DEST),'remote','add','origin','https://github.com/mulesoft/mule-api.git'],
              ['git','-C',str(DEST),'fetch','--depth=1','origin',COMMIT],
              ['git','-C',str(DEST),'checkout','--detach','FETCH_HEAD'] ]:
    subprocess.run(args, check=True, timeout=120)
actual = subprocess.check_output(['git','-C',str(DEST),'rev-parse','HEAD'], text=True).strip()
assert actual == COMMIT
pom = DEST / 'pom.xml'
text = pom.read_text()
model = ET.fromstring(text)
assert model.findtext('m:version', namespaces=NS) == '1.10.0-SNAPSHOT'
assert 'SEPARATE_CLASSLOADER_FOR_POLICY_ISOLATION(' in (DEST/'src/main/java/org/mule/runtime/api/config/MuleRuntimeFeature.java').read_text()
parent = re.search(r'<parent>.*?</parent>',text,re.S).group(0)
text = text.replace(parent,parent.replace('<version>4.10.0-SNAPSHOT</version>','<version>4.10.0</version>'),1)
# A distinct artifact version avoids representing patched/source-built API bytes
# as the vendor's released 1.10.0 binary. Original package/class names are retained.
text = text.replace('<version>1.10.0-SNAPSHOT</version>', '<version>'+VERSION+'</version>', 1)
changes = []
for key in ['artifactDeclarationVersion','muleModuleMavenPluginVersion','muleApiAnnotationsVersion','muleBomVersion']:
    old = model.findtext('m:properties/m:'+key,namespaces=NS)
    assert old and old.endswith('-SNAPSHOT'), key
    new = old.removesuffix('-SNAPSHOT')
    text = text.replace('<'+key+'>'+old+'</'+key+'>','<'+key+'>'+new+'</'+key+'>',1)
    changes.append({'property':key,'from':old,'to':new})
text = text.replace('<name>Mule API</name>','<name>ZERNOVA Runtime API</name>',1)
ET.fromstring(text)
pom.write_text(text)
(OUT/'api-source.patch').write_bytes(subprocess.check_output(['git','-C',str(DEST),'diff','--','pom.xml']))

main = ROOT/'pom.xml'
text = main.read_text()
anchor = re.search(r'<dependencyManagement>\s*<dependencies>',text).group(0)
override = '\n            <!-- ZERNOVA: source-built API matching this development snapshot. CPAL-1.0. -->\n            <dependency>\n                <groupId>org.mule.runtime</groupId>\n                <artifactId>mule-api</artifactId>\n                <version>'+VERSION+'</version>\n            </dependency>\n'
assert '<version>'+VERSION+'</version>' not in text
text = text.replace(anchor,anchor+override,1)
ET.fromstring(text)
main.write_text(text)
record = {'repository':'https://github.com/mulesoft/mule-api.git','commit':COMMIT,
          'source_version':'1.10.0-SNAPSHOT','built_artifact_version':VERSION,
          'reason':'The supplied core references SEPARATE_CLASSLOADER_FOR_POLICY_ISOLATION, absent from the released API 1.10.0. Build matching source instead of removing behavior.',
          'parent':'org.mule:mule-plugin-mgmt-parent-pom:4.10.0','property_changes':changes,
          'license':'CPAL-1.0','license_sha256':hashlib.sha256((DEST/'LICENSE.txt').read_bytes()).hexdigest(),
          'source_java_modified':False,'compilation':'NOT_YET_EXECUTED'}
(OUT/'api-source.json').write_text(json.dumps(record,indent=2)+'\n')
print(json.dumps(record,indent=2))
