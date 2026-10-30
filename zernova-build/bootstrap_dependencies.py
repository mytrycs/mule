#!/usr/bin/env python3
"""Prepare private dependencies using the pinned pre-migration build as input.

Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0.
The release kernel is compiled separately from tracked com.zernova sources.
"""
from pathlib import Path
import argparse
import hashlib
import json
import os
import shlex
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
BASELINE = '46cae04e9b02fe64e951791f4069089a01296695'


def run(args, **kwargs):
    subprocess.run([str(a) for a in args], check=True, **kwargs)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--prepared-repository', type=Path,
                        help='Reuse an already completed baseline Maven repository')
    args = parser.parse_args()
    mapping = ROOT / 'zernova-build/namespace-map.json'
    output = ROOT / '.zernova-m2'
    marker = output / '.zernova-namespace-relocation.json'
    digest = hashlib.sha256(mapping.read_bytes()).hexdigest()
    if marker.exists():
        if json.loads(marker.read_text())['classMapSha256'] != digest or json.loads(marker.read_text()).get('applicationFormat') != 1:
            raise SystemExit('Namespace map changed; move .zernova-m2 aside and bootstrap again')
        print('Private ZERNOVA dependency repository is ready')
        return
    if output.exists():
        raise SystemExit('Incomplete .zernova-m2; move it aside before retrying')
    repository = args.prepared_repository
    if repository is None:
        checkout = ROOT / '.zernova-bootstrap'
        repository = ROOT / '.zernova-bootstrap-m2'
        if not checkout.exists():
            run(['git', '-C', ROOT, 'worktree', 'add', '--detach', checkout, BASELINE])
        actual = subprocess.check_output(['git', '-C', str(checkout), 'rev-parse', 'HEAD'], text=True).strip()
        if actual != BASELINE:
            raise SystemExit('Unexpected bootstrap source revision')
        # Always restore our disposable pinned checkout before adapting it. A
        # failed attempt must not stack textual edits on the next invocation.
        run(['git', '-C', checkout, 'reset', '--hard', BASELINE])
        for name in ['mule-api', 'mule-extensions-api', 'mule-distributions']:
            import shutil
            shutil.rmtree(checkout / '.zernova-upstream' / name, ignore_errors=True)
        spring = checkout / 'modules/extensions-spring-support/pom.xml'
        duplicate = ('        <dependency>\n'
                     '            <groupId>com.sun.xml.bind</groupId>\n'
                     '            <artifactId>jaxb-impl</artifactId>\n'
                     '            <scope>test</scope>\n'
                     '        </dependency>')
        spring.write_text(spring.read_text().replace(duplicate, ''))
        # Independently licensed agent code is outside the CPAL engine reactor.
        pom = checkout / 'pom.xml'
        text = pom.read_text().replace('<includes><include>**/*.java</include></includes>',
                            '<includes><include>**/*.java</include></includes>'
                            '<excludes><exclude>zernova-studio-build/**</exclude></excludes>')
        pom.write_text(text)
        headers = checkout / 'zernova-build/preserve-license-headers.py'
        text = headers.read_text()
        text = text.replace("excludes=''.join(",
                            "excludes='<exclude>zernova-studio-build/**</exclude>'+''.join(")
        headers.write_text(text)
        build = checkout / 'zernova-build/build-source.sh'
        settings = shlex.quote(str(checkout / 'zernova-build/settings.xml'))
        local_repo = shlex.quote('-Dmaven.repo.local=' + str(repository))
        recipe = build.read_text().replace('mvn ', 'mvn -s ' + settings + ' ' + local_repo + ' ')
        # Prime the exact clean lifecycle too. An install-only baseline leaves
        # Maven's clean plugin absent in an otherwise empty dependency cache.
        recipe = recipe.replace(' install\n', ' clean install\n')
        build.write_text(recipe)
        run(['bash', build], cwd=checkout)
    if not repository.is_dir():
        raise SystemExit('Prepared repository does not exist')
    run([sys.executable, ROOT / 'zernova-build/refactor_namespace.py', 'repository',
         '--input', repository, '--output', output, '--application-format'])


if __name__ == '__main__':
    main()
