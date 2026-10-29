#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
"""Apply reviewable product presentation changes without rewriting engine APIs."""
from pathlib import Path
import argparse
import hashlib
import json
import re
import xml.etree.ElementTree as ET

VERSION = '0.3.0-dev.1'
LICENSE_SHA = '1c7645351a5374addf38c27b8f9ed9968a58bd18617668005f20125ed1479202'
NS = {'m': 'http://maven.apache.org/POM/4.0.0'}


def require(condition, message):
    if not condition:
        raise ValueError(message)


def apply(root, output):
    root = root.resolve()
    output.mkdir(parents=True, exist_ok=True)
    require(hashlib.sha256((root/'LICENSE.txt').read_bytes()).hexdigest() == LICENSE_SHA,
            'Unexpected engine license; review before branding')
    records = []

    def write(rel, text, reason):
        path = root/rel
        old = path.read_text()
        if text == old:
            return
        path.write_text(text)
        records.append({'path': rel, 'reason': reason, 'license': 'CPAL-1.0',
                        'before_sha256': hashlib.sha256(old.encode()).hexdigest(),
                        'after_sha256': hashlib.sha256(text.encode()).hexdigest()})

    text = (root/'pom.xml').read_text()
    model = ET.fromstring(text)
    require(model.findtext('m:version', namespaces=NS) == '4.10.0-SNAPSHOT',
            'This recipe supports only the supplied 4.10.0-SNAPSHOT reactor')
    if model.findtext('m:name', namespaces=NS) == 'Mule Kernel':
        old = '<organization>\n        <name>MuleSoft, Inc.</name>\n        <url>http://www.mulesoft.com</url>\n    </organization>'
        new = '<organization>\n        <name>ZerNova</name>\n        <url>https://zernova.et</url>\n    </organization>'
        require(old in text, 'Expected product organization is missing')
        text = text.replace(old, new, 1)
        text = text.replace('<name>Mule Kernel</name>', '<name>ZERNOVA Kernel</name>', 1)
        a = text.index('    <description>')
        b = text.index('    </description>', a) + len('    </description>')
        text = text[:a] + '''    <description>
        ZERNOVA integration runtime, a CPAL-covered fork of the Mule Kernel
        4.10 development source. Original upstream copyrights and licenses are
        retained. ZERNOVA modifications are Copyright 2026 ZerNova.
    </description>''' + text[b:]
        text = text.replace('<url>https://developer.mulesoft.com/</url>', '<url>https://zernova.et/</url>', 1)
        text = text.replace('<productUrl>https://developer.mulesoft.com/</productUrl>',
                            '<productUrl>https://zernova.et/</productUrl>\n        <zernovaProductVersion>'+VERSION+'</zernovaProductVersion>')
        text = text.replace('<Description>Mule Runtime and Integration Platform</Description>',
                            '<Description>ZERNOVA Integration Runtime</Description>\n                                <Zernova-Product-Version>${zernovaProductVersion}</Zernova-Product-Version>\n                                <Upstream-Lineage>Mule Kernel 4.10.0-SNAPSHOT</Upstream-Lineage>')
        text = text.replace('<Support>Get commercial support: ${project.organization.url}/support</Support>',
                            '<Support>Development preview; see the bundled ZERNOVA documentation</Support>')
        text = text.replace('scm:git:git://github.com/mulesoft/mule.git', 'scm:git:https://github.com/mytrycs/mule.git')
        text = text.replace('scm:git:git@github.com:mulesoft/mule.git', 'scm:git:git@github.com:mytrycs/mule.git')
        text = text.replace('<url>https://github.com/mulesoft/mule</url>', '<url>https://github.com/mytrycs/mule</url>')
        ET.fromstring(text)
        write('pom.xml', text, 'Product display, source provenance and distinct product version; technical identifiers and license retained.')
    else:
        require(model.findtext('m:name', namespaces=NS) == 'ZERNOVA Kernel', 'Unexpected root product')
        require(model.findtext('m:properties/m:zernovaProductVersion', namespaces=NS) == VERSION,
                'A different ZERNOVA version is already present')

    for path in sorted(root.rglob('pom.xml')):
        parts = path.relative_to(root).parts
        if 'src' in parts or 'target' in parts or '.zernova-upstream' in parts or path == root/'pom.xml':
            continue
        text = path.read_text()
        model = ET.fromstring(text)
        name = model.findtext('m:name', namespaces=NS)
        if name and re.search(r'\bMule\b', name):
            new_name = re.sub(r'\bMule\b', 'ZERNOVA', name)
            require('<name>'+name+'</name>' in text, 'Unexpected display-name formatting: '+str(path))
            text = text.replace('<name>'+name+'</name>', '<name>'+new_name+'</name>', 1)
        text = text.replace('<description>Mule Runtime Manifest</description>',
                            '<description>ZERNOVA Runtime Manifest</description>')
        ET.fromstring(text)
        write(str(path.relative_to(root)), text, 'Human-readable module title only.')

    banner = '''
  Z E R N O V A
  INTEGRATION RUNTIME

  ZERNOVA source fork: '''+VERSION+'''
  Engine lineage: Mule Kernel 4.10.0-SNAPSHOT
  ZERNOVA modifications: Copyright (c) 2026 ZerNova.

  Required upstream attribution:
  Copyright (c) 2003-2015 MuleSoft Inc.
  Powered by Mule. MuleSoft is Open for Integration.
  http://www.MuleSoft.com
  Covered engine source remains under CPAL-1.0. See LICENSE.txt.

'''
    write('modules/launcher/src/main/resources/banner.txt', banner,
          'Original product text artwork with required attribution and accurate source lineage.')
    replacements = {
        'modules/boot-api/src/main/java/org/mule/runtime/module/boot/internal/AbstractMuleContainerWrapper.java':
            [('Starting the Mule Container...', 'Starting ZERNOVA Runtime...')],
        'modules/launcher/src/main/java/org/mule/runtime/module/launcher/DefaultMuleContainer.java':
            [('Mule container shutting down due to normal shutdown request', 'ZERNOVA Runtime shutting down due to normal shutdown request')],
        'modules/launcher/src/main/java/org/mule/runtime/module/launcher/splash/MuleContainerStartupSplashScreen.java':
            [('Mule server plugins:', 'ZERNOVA server plugins:'), ('Mule services:', 'ZERNOVA services:'),
             ('Mule system properties:', 'Engine compatibility system properties:')],
        'modules/deployment/src/main/java/org/mule/runtime/module/deployment/internal/DeploymentDirectoryWatcher.java':
            [('Mule is up and kicking (every %dms)', 'ZERNOVA Runtime is ready (deployment scan every %dms)')],
    }
    copyright_line = ' * ZERNOVA presentation modifications Copyright (c) 2026 ZerNova. CPAL-1.0.\n'
    for rel, pairs in replacements.items():
        text = (root/rel).read_text()
        for old, new in pairs:
            if old in text:
                require(text.count(old) == 1, 'Ambiguous presentation string in '+rel)
                text = text.replace(old, new, 1)
            else:
                require(new in text, 'Expected presentation string missing in '+rel)
        if copyright_line not in text:
            text = text.replace('/*\n', '/*\n'+copyright_line, 1)
        write(rel, text, 'User-facing presentation strings only; original CPAL notice retained.')

    rel = 'modules/launcher/src/test/java/org/mule/runtime/module/launcher/MuleContainerStartupSplashScreenTestCase.java'
    text = (root/rel).read_text()
    for old, new in [('Mule services:', 'ZERNOVA services:'), ('Mule system properties:', 'Engine compatibility system properties:')]:
        pattern = r'\* '+re.escape(old)+r' +\*'
        match = re.search(pattern, text)
        if match:
            prefix = '* '+new
            updated = prefix + ' '*(len(match.group(0))-len(prefix)-1) + '*'
            require(len(updated) == len(match.group(0)), 'Changed splash-frame width')
            text = text[:match.start()]+updated+text[match.end():]
        else:
            require(new in text, 'Missing branded test expectation')
    if copyright_line not in text:
        text = text.replace('/*\n', '/*\n'+copyright_line, 1)
    write(rel, text, 'Update splash label expectations while retaining all upstream tests and fixed-width assertions.')

    readme = root/'README.md'
    if not readme.read_text().startswith('# ZERNOVA Kernel'):
        intro = '''# ZERNOVA Kernel — 4.10 source fork

Product source version: **'''+VERSION+'''**. Underlying source baseline:
**4.10.0-SNAPSHOT**. This is derived from Mule Kernel, originally developed by
MuleSoft/Salesforce. It is not the official released 4.10.0 source.

Compilation, distribution assembly and application execution are separate gates.
See the current build evidence. A branded banner is not proof of a working engine.

## License and identity

Inherited engine code and changes to covered files remain **CPAL-1.0**.
Original notices remain in `LICENSE.txt` and source headers. ZERNOVA's original-
components license applies only to explicitly listed independently written files,
not to the covered engine. No vendor endorsement is implied.

Java packages, Maven coordinates, XML namespaces and deployment identifiers
retain their compatibility names. Studio and older native products are untouched.

## Preserved upstream README

'''
        write('README.md', intro+readme.read_text(), 'ZERNOVA source entry point with preserved upstream documentation.')
    require(hashlib.sha256((root/'LICENSE.txt').read_bytes()).hexdigest() == LICENSE_SHA, 'License changed')
    result = {'product_version': VERSION, 'underlying_source_version': '4.10.0-SNAPSHOT',
              'license_sha256': LICENSE_SHA, 'changes': records,
              'source_build_claim': 'None: this script edits source only'}
    (output/'branding-changes.json').write_text(json.dumps(result, indent=2)+'\n')
    return result


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument('--output', type=Path)
    args = parser.parse_args()
    try:
        result = apply(args.root, args.output or args.root/'zernova-build-evidence')
    except (ValueError, OSError, ET.ParseError) as error:
        raise SystemExit(str(error))
    print('Source presentation changes:', len(result['changes']))
