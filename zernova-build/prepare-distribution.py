#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
"""Prepare the actual standalone assembly; never substitute released core binaries."""
from pathlib import Path
import hashlib
import json
import re
import subprocess
import urllib.request
import xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'zernova-build-evidence'; OUT.mkdir(exist_ok=True)
DEST=ROOT/'.zernova-upstream/mule-distributions'
COMMIT='fa3255511c83e0a9aac5768d71ac4c51b32ffcce'
NS={'m':'http://maven.apache.org/POM/4.0.0'}
if DEST.exists(): raise SystemExit('Refusing to overwrite existing distribution sources')
DEST.mkdir(parents=True)
for cmd in [['git','init',str(DEST)],['git','-C',str(DEST),'remote','add','origin','https://github.com/mulesoft/mule-distributions.git'],['git','-C',str(DEST),'fetch','--depth=1','origin',COMMIT],['git','-C',str(DEST),'checkout','--detach','FETCH_HEAD']]:
    subprocess.run(cmd,check=True,timeout=120)
assert subprocess.check_output(['git','-C',str(DEST),'rev-parse','HEAD'],text=True).strip()==COMMIT
url='https://repository.mulesoft.org/releases/org/mule/distributions/mule-distributions/4.10.0/mule-distributions-4.10.0.pom'
with urllib.request.urlopen(url,timeout=60) as response: released=response.read()
assert hashlib.sha256(released).hexdigest()=='b99e51d87bdcdb95fed157382a828e1465550e9ca2f716010ac46238308908ad'
(OUT/'released-distribution-4.10.0.pom').write_bytes(released)
release=ET.fromstring(released)
text=(DEST/'pom.xml').read_text(); model=ET.fromstring(text)
assert model.findtext('m:version',namespaces=NS)=='4.10.0-SNAPSHOT'
changes=[]
for key in ['muleEmbeddedApiVersion','muleDependenciesVersion']:
    old=model.findtext('m:properties/m:'+key,namespaces=NS)
    new=release.findtext('m:properties/m:'+key,namespaces=NS)
    assert old and new and 'SNAPSHOT' not in new
    text=text.replace('<'+key+'>'+old+'</'+key+'>','<'+key+'>'+new+'</'+key+'>',1)
    changes.append({'property':key,'from':old,'to':new})
text=text.replace('<muleBomVersion>${project.version}</muleBomVersion>','<muleBomVersion>4.10.0</muleBomVersion>',1)
changes.append({'property':'muleBomVersion','from':'${project.version}','to':'4.10.0'})

# Maven child BOM imports can otherwise select released transitive kernel bytes.
# Enumerate the actual source reactor and give every produced artifact an explicit
# dependency-management entry at its real source version.
coordinates=[]
seen=set()
def visit(path, inherited_group=None, inherited_version=None):
    path=path.resolve()
    if path in seen: return
    seen.add(path)
    model=ET.parse(path).getroot()
    group=model.findtext('m:groupId',namespaces=NS) or inherited_group or model.findtext('m:parent/m:groupId',namespaces=NS)
    version=model.findtext('m:version',namespaces=NS) or inherited_version or model.findtext('m:parent/m:version',namespaces=NS)
    artifact=model.findtext('m:artifactId',namespaces=NS)
    packaging=model.findtext('m:packaging',namespaces=NS) or 'jar'
    assert group and artifact and version and '${' not in group+artifact+version,(path,group,artifact,version)
    assert version=='4.10.0-SNAPSHOT',(path,version)
    coordinates.append({'groupId':group,'artifactId':artifact,'version':version,'type':packaging,'source_pom':str(path.relative_to(ROOT))})
    for module in model.findall('m:modules/m:module',NS):
        visit(path.parent/module.text/'pom.xml',group,version)
visit(ROOT/'pom.xml')
for artifact in ['mule-api','mule-extensions-mime-types','mule-extensions-api','mule-extensions-api-persistence','mule-extensions-api-dsql']:
    coordinates.append({'groupId':'org.mule.runtime','artifactId':artifact,'version':'1.10.0-zernova.1','type':'jar','source_pom':'pinned API source'})
anchor=re.search(r'<dependencyManagement>\s*<dependencies>',text).group(0)
overrides='\n            <!-- ZERNOVA: use exact source-built kernel and API artifacts, not upstream binary substitutes. -->\n'
for item in coordinates:
    overrides+='            <dependency><groupId>'+item['groupId']+'</groupId><artifactId>'+item['artifactId']+'</artifactId><version>'+item['version']+'</version><type>'+item['type']+'</type></dependency>\n'
text=text.replace(anchor,anchor+overrides,1)
text=text.replace('<name>Mule Distributions</name>','<name>ZERNOVA Runtime Distributions</name>',1)
ET.fromstring(text); (DEST/'pom.xml').write_text(text)
for path in DEST.rglob('pom.xml'):
    rel=path.relative_to(DEST)
    if path==DEST/'pom.xml' or 'src' in rel.parts: continue
    text=path.read_text(); model=ET.fromstring(text); name=model.findtext('m:name',namespaces=NS)
    if name and 'Mule' in name:
        text=text.replace('<name>'+name+'</name>','<name>'+re.sub(r'\bMule\b','ZERNOVA',name)+'</name>',1)
        ET.fromstring(text); path.write_text(text)
(OUT/'distribution-source.patch').write_bytes(subprocess.check_output(['git','-C',str(DEST),'diff']))
record={'repository':'https://github.com/mulesoft/mule-distributions.git','commit':COMMIT,'source_version':'4.10.0-SNAPSHOT','external_property_pins':changes,'source_artifact_overrides':coordinates,'assembly':'Original standalone assembly descriptor and full runtime dependency sets retained','execution_tests':'NOT YET RUN'}
(OUT/'distribution-source.json').write_text(json.dumps(record,indent=2)+'\n')
print('Prepared standalone assembly with',len(coordinates),'source artifact overrides')
