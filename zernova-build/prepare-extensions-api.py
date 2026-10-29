#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
"""Build the connector API source that matches the supplied kernel snapshot."""
from pathlib import Path
import hashlib
import json
import re
import subprocess
import xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'zernova-build-evidence'; OUT.mkdir(exist_ok=True)
DEST=ROOT/'.zernova-upstream/mule-extensions-api'
COMMIT='be8ee76e1a359c263861baf539c5359eaa89e9f1'
VERSION='1.10.0-zernova.1'
NS={'m':'http://maven.apache.org/POM/4.0.0'}
if DEST.exists(): raise SystemExit('Refusing to overwrite existing extensions API sources')
DEST.mkdir(parents=True)
for command in [['git','init',str(DEST)],['git','-C',str(DEST),'remote','add','origin','https://github.com/mulesoft/mule-extensions-api.git'],['git','-C',str(DEST),'fetch','--depth=1','origin',COMMIT],['git','-C',str(DEST),'checkout','--detach','FETCH_HEAD']]:
    subprocess.run(command,check=True,timeout=120)
assert subprocess.check_output(['git','-C',str(DEST),'rev-parse','HEAD'],text=True).strip()==COMMIT
source=(DEST/'mule-extensions-api/src/main/java/org/mule/runtime/extension/api/loader/parser/SourceModelParser.java').read_text()
assert 'getSourceClusterSupportModelProperty()' in source and 'getBackPressureStrategyModelProperty()' in source
artifacts=['mule-extensions-mime-types','mule-extensions-api','mule-extensions-api-persistence','mule-extensions-api-dsql']
changes=[]
def managed(artifact):
    return '            <dependency><groupId>org.mule.runtime</groupId><artifactId>'+artifact+'</artifactId><version>'+VERSION+'</version></dependency>\n'
for path in [DEST/'pom.xml']+[DEST/a/'pom.xml' for a in artifacts]:
    text=path.read_text(); model=ET.fromstring(text)
    parent=re.search(r'<parent>.*?</parent>',text,re.S).group(0)
    if path==DEST/'pom.xml':
        assert model.findtext('m:version',namespaces=NS)=='1.10.0-SNAPSHOT'
        text=text.replace(parent,parent.replace('<version>4.10.0-SNAPSHOT</version>','<version>4.10.0</version>'),1)
        text=text.replace('<version>1.10.0-SNAPSHOT</version>','<version>'+VERSION+'</version>',1)
        for key,new in {'muleApiVersion':VERSION,'muleConnectivityApiVersion':'1.1.0','metadataModelApiVersion':'1.10.0','muleModuleMavenPluginVersion':'1.10.0','muleBomVersion':'4.10.0'}.items():
            old=model.findtext('m:properties/m:'+key,namespaces=NS)
            assert old, key
            text=text.replace('<'+key+'>'+old+'</'+key+'>','<'+key+'>'+new+'</'+key+'>',1)
            changes.append({'property':key,'from':old,'to':new})
        anchor=re.search(r'<dependencyManagement>\s*<dependencies>',text).group(0)
        overrides='\n'+''.join(managed(a) for a in ['mule-api']+artifacts)
        text=text.replace(anchor,anchor+overrides,1)
    else:
        assert model.findtext('m:parent/m:artifactId',namespaces=NS)=='mule-extensions-api-parent'
        text=text.replace(parent,parent.replace('<version>1.10.0-SNAPSHOT</version>','<version>'+VERSION+'</version>'),1)
        direct=model.findtext('m:version',namespaces=NS)
        if direct:
            assert direct=='1.10.0-SNAPSHOT',(path,direct)
            text=text.replace('<version>1.10.0-SNAPSHOT</version>','<version>'+VERSION+'</version>',1)
    name=model.findtext('m:name',namespaces=NS)
    if name and 'Mule' in name:
        text=text.replace('<name>'+name+'</name>','<name>'+re.sub(r'\bMule\b','ZERNOVA',name)+'</name>',1)
    check=ET.fromstring(text)
    assert (check.findtext('m:version',namespaces=NS) or check.findtext('m:parent/m:version',namespaces=NS))==VERSION
    path.write_text(text)
(OUT/'extensions-api-source.patch').write_bytes(subprocess.check_output(['git','-C',str(DEST),'diff']))
main=ROOT/'pom.xml'; text=main.read_text()
assert '<muleExtensionsApiVersion>1.10.0</muleExtensionsApiVersion>' in text
text=text.replace('<muleExtensionsApiVersion>1.10.0</muleExtensionsApiVersion>','<muleExtensionsApiVersion>'+VERSION+'</muleExtensionsApiVersion>',1)
anchor=re.search(r'<dependencyManagement>\s*<dependencies>',text).group(0)
overrides='\n            <!-- Source-built matching connector API family. CPAL-1.0. -->\n'+''.join(managed(a) for a in artifacts)
text=text.replace(anchor,anchor+overrides,1)
ET.fromstring(text); main.write_text(text)
record={'repository':'https://github.com/mulesoft/mule-extensions-api.git','commit':COMMIT,'source_version':'1.10.0-SNAPSHOT','built_version':VERSION,'artifacts':artifacts,'dependency_changes':changes,'java_sources_changed':False,'reason':'Snapshot core and released 1.10.0 extension-parser contracts differ. Restore matching source rather than delete or stub engine behavior.','license':'CPAL-1.0','license_sha256':hashlib.sha256((DEST/'LICENSE.txt').read_bytes()).hexdigest()}
(OUT/'extensions-api-source.json').write_text(json.dumps(record,indent=2)+'\n')
print(json.dumps(record,indent=2))
