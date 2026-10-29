#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. CPAL-1.0, see ../LICENSE.txt.
# This is an explicit build adaptation of the uploaded 4.10.0-SNAPSHOT source,
# not a claim that it is the official 4.10.0 release source.
from pathlib import Path
import hashlib
import json
import re
import urllib.request
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'zernova-build-evidence'
OUT.mkdir(exist_ok=True)
POM = ROOT / 'pom.xml'
text = POM.read_text()
ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
model = ET.fromstring(text)
assert model.findtext('m:version', namespaces=ns) == '4.10.0-SNAPSHOT'
assert model.findtext('m:parent/m:artifactId', namespaces=ns) == 'mule-plugin-mgmt-parent-pom'

# Obtain and record the actual release parent, without inventing a replacement POM.
url = 'https://repository.mulesoft.org/releases/org/mule/mule-plugin-mgmt-parent-pom/4.10.0/mule-plugin-mgmt-parent-pom-4.10.0.pom'
with urllib.request.urlopen(url, timeout=60) as response:
    parent = response.read()
parent_model = ET.fromstring(parent)
assert parent_model.findtext('m:artifactId', namespaces=ns) == 'mule-plugin-mgmt-parent-pom'
assert parent_model.findtext('m:version', namespaces=ns) == '4.10.0'
(OUT / 'released-parent-4.10.0.pom').write_bytes(parent)
changes = [{'location': 'parent', 'from': '4.10.0-SNAPSHOT', 'to': '4.10.0', 'origin': url, 'sha256': hashlib.sha256(parent).hexdigest()}]
block = re.search(r'<parent>.*?</parent>', text, re.S).group(0)
assert block.count('<version>4.10.0-SNAPSHOT</version>') == 1
text = text.replace(block, block.replace('<version>4.10.0-SNAPSHOT</version>', '<version>4.10.0</version>'), 1)

# Keep the reactor/source version intact. Only these external dependency/plugin
# properties are stabilized; every change is retained as machine-readable evidence.
keys = ['muleBomVersion', 'muleMavenClientBomVersion', 'mulePropertiesApiVersion',
        'muleExtensionsApiVersion', 'muleConnectivityApiVersion', 'muleHttpPolicyApiVersion',
        'mulePolicyApiVersion', 'muleApiVersion', 'muleMetadataModelVersion',
        'muleAnnotationsApiVersion', 'mule.module.maven.plugin.version',
        'mule.extensions.maven.plugin.version', 'mule.app.plugins.maven.plugin.version',
        'mule.jmh.elasticsearch.maven.plugin.version']
for key in keys:
    old = model.findtext('m:properties/m:' + key, namespaces=ns)
    if old and old.endswith('-SNAPSHOT'):
        new = old.removesuffix('-SNAPSHOT')
        before = '<' + key + '>' + old + '</' + key + '>'
        after = '<' + key + '>' + new + '</' + key + '>'
        assert text.count(before) == 1, key
        text = text.replace(before, after, 1)
        changes.append({'location': 'properties/' + key, 'from': old, 'to': new})
ET.fromstring(text)
POM.write_text(text)
(OUT / 'dependency-adaptation.json').write_text(json.dumps({'source_version': '4.10.0-SNAPSHOT', 'changes': changes, 'compatibility': 'UNVERIFIED until compilation and runtime tests pass'}, indent=2) + '\n')
print(json.dumps(changes, indent=2))
