#!/usr/bin/env python3
"""Provision the checksum-pinned Maven used by local and CI source builds.
Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0.
"""
from pathlib import Path
import hashlib
import io
import tarfile
import urllib.request

VERSION = '3.9.11'
SHA512 = 'bcfe4fe305c962ace56ac7b5fc7a08b87d5abd8b7e89027ab251069faebee516b0ded8961445d6d91ec1985dfe30f8153268843c89aa392733d1a3ec956c9978'
ROOT = Path(__file__).resolve().parents[1]

def provision():
    home = ROOT / '.zernova-tools' / ('apache-maven-' + VERSION)
    if not (home / 'bin/mvn').exists():
        url = f'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/{VERSION}/apache-maven-{VERSION}-bin.tar.gz'
        with urllib.request.urlopen(url, timeout=120) as response:
            data = response.read()
        if hashlib.sha512(data).hexdigest() != SHA512:
            raise RuntimeError('Maven download checksum mismatch')
        home.parent.mkdir(exist_ok=True)
        with tarfile.open(fileobj=io.BytesIO(data)) as archive:
            for entry in archive.getmembers():
                target = (home.parent / entry.name).resolve()
                if home.parent.resolve() not in target.parents or not (entry.isdir() or entry.isfile()):
                    raise RuntimeError('Unexpected Maven archive entry')
            archive.extractall(home.parent)
    return home / 'bin'

if __name__ == '__main__':
    print(provision())
