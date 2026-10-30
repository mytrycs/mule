#!/usr/bin/env python3
"""Reproducible source/dependency relocation for the ZERNOVA Java namespace.

Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0.
Never run dependency relocation against a shared Maven repository in place.
Class files use modified UTF-8: only ASCII byte sequences are replaced, without
decoding/re-encoding the other bytes. Every constant-pool length is rebuilt.
"""
import argparse
import hashlib
import io
import json
import os
from concurrent.futures import ProcessPoolExecutor
import re
import shutil
import struct
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TOKEN = re.compile(r'[A-Za-z_][A-Za-z_0-9]*')
SKIP = {'.git', 'target', 'node_modules', '__pycache__'}
TEXT = {'.java', '.xml', '.pom', '.properties', '.json', '.txt', '.MF', '.xsd', '.dtd',
        '.yaml', '.yml', '.conf', '.config', '.ts', '.js', '.cjs', '.mjs', '.html',
        '.groovy', '.sh', '.bat', '.cmd', '.md', '.vm', '.ftl', '.raml', '.wsdl'}


def application_text(value):
    """ZERNOVA format v1: executable XML and deployment contract identifiers.

    Maven engine coordinates and upstream licenses are separate from this
    contract. Apply the same mapping to source, connector bytecode and metadata.
    """
    if not any(word in value for word in ('mule', 'Mule', 'MULE')):
        return value
    value = value.replace('http://www.mulesoft.org/schema/mule', 'https://schemas.zernova.dev')
    value = value.replace('http\\://www.mulesoft.org/schema/mule', 'https\\://schemas.zernova.dev')
    for old, new in [('mule-artifact', 'zernova-artifact'), ('mule-application', 'zernova-application'),
                     ('mule-plugin', 'zernova-plugin'), ('mule-domain', 'zernova-domain'),
                     ('mule-src', 'zernova-src'), ('minMuleVersion', 'minZernovaVersion'),
                     ('MinMuleVersion', 'MinZernovaVersion')]:
        value = value.replace(old, new)
    value = re.sub(r'mule(?=(?:-[A-Za-z0-9_-]+)?\.xsd|\.schemas)', 'zernova', value)
    value = value.replace('mule-%s', 'zernova-%s')
    value = re.sub(r'\bMULE_EE\b', 'ZERNOVA_EE', value)
    value = re.sub(r'\bMULE\b(?!-\d)', 'ZERNOVA', value)
    value = re.sub(r'([\"\x27])mule\1', lambda m: m[1] + 'zernova' + m[1], value)
    value = re.sub(r'(<\/?(?:[\w.-]+:)?)mule(?=[\s/:>])', r'\1zernova', value)
    value = value.replace('xmlns:mule=', 'xmlns:zernova=').replace('mule:', 'zernova:')
    if value == 'mule':
        value = 'zernova'
    return value


class Relocation:
    def __init__(self, names, application_format=False):
        self.names = dict(sorted(names.items()))
        self.application_format = application_format

    def text(self, value):
        if self.application_format:
            value = application_text(value)
        if not any(s in value for s in ('org.mule', 'org/mule', 'Mule', 'io.zernova.studio', 'io/zernova/studio')):
            return value
        # Source Unicode escapes can immediately precede a qualified class
        # name (for example, \\u0027org.mule...); do not rely on a word boundary.
        value = re.sub(r'org\.mule(?=\.|\b)', 'com.zernova', value)
        # JVM descriptors permit Lorg/mule..., while website domains ending in
        # .org/mule are URLs and must remain intact.
        value = re.sub(r'(?:(?<![A-Za-z0-9_.])|(?<=L))org/mule(?=/|\b)', 'com/zernova', value)
        value = value.replace('io.zernova.studio', 'com.zernova.studio')
        value = value.replace('io/zernova/studio', 'com/zernova/studio')
        return TOKEN.sub(lambda m: self.names.get(m[0], m[0]), value)

    def classfile(self, data):
        if data[:4] != b'\xca\xfe\xba\xbe':
            raise ValueError('Invalid class file')
        candidates = (b'org/mule', b'org.mule', b'Mule', b'io/zernova/studio', b'io.zernova.studio')
        if self.application_format:
            candidates += (b'mule', b'MULE')
        if not any(s in data for s in candidates):
            return data
        count = struct.unpack_from('>H', data, 8)[0]
        output, offset, index = bytearray(data[:10]), 10, 1
        sizes = {3: 4, 4: 4, 5: 8, 6: 8, 7: 2, 8: 2, 9: 4, 10: 4, 11: 4,
                 12: 4, 15: 3, 16: 2, 17: 4, 18: 4, 19: 2, 20: 2}
        while index < count:
            tag = data[offset]
            offset += 1
            output.append(tag)
            if tag == 1:
                length = struct.unpack_from('>H', data, offset)[0]
                offset += 2
                raw = data[offset:offset + length]
                if len(raw) != length:
                    raise ValueError('Truncated UTF-8 constant')
                raw = self.text(raw.decode('latin1')).encode('latin1')
                output.extend(struct.pack('>H', len(raw)))
                output.extend(raw)
                offset += length
            else:
                if tag not in sizes:
                    raise ValueError('Unknown constant-pool tag: ' + str(tag))
                length = sizes[tag]
                output.extend(data[offset:offset + length])
                offset += length
                if tag in (5, 6):
                    index += 1
            index += 1
        output.extend(data[offset:])
        return bytes(output)

    def manifest(self, data, strip_digests=False):
        # Unfold before replacing names; module and main-class names may wrap.
        text = data.replace(b'\r\n', b'\n').replace(b'\n ', b'').decode('latin1')
        relocated = self.text(text)
        if relocated == text and not strip_digests:
            return data
        lines = []
        for line in relocated.rstrip('\n').splitlines():
            if re.match(r'[^:]*-Digest(?:-[^:]*)?:', line, re.I):
                continue
            raw = line.encode('latin1')
            while len(raw) > 70:
                end = 70
                while end and raw[end] & 0xc0 == 0x80:
                    end -= 1
                lines.append(raw[:end])
                raw = b' ' + raw[end:]
            lines.append(raw)
        return b'\r\n'.join(lines) + b'\r\n\r\n'

    def content(self, name, data):
        if name.endswith('.class'):
            return self.classfile(data)
        if data[:4] == b'PK\x03\x04' and Path(name).suffix.lower() in {'.jar', '.zip', '.mule'}:
            return self.archive(data)
        if name.upper().endswith('META-INF/MANIFEST.MF'):
            return self.manifest(data)
        if name.endswith('.ser') and data.startswith(b'\xac\xed\x00\x05'):
            return self.serialized_fixture(data)
        # Notices are immutable, including embedded notices in third-party JARs.
        if re.match(r'^(LICENSES?|NOTICE|COPYING|COPYRIGHT)(?:[._-].*)?$', Path(name).name, re.I) and Path(name).suffix not in {'.java', '.pom'}:
            return data
        if Path(name).suffix in TEXT or '/META-INF/services/' in '/' + name or Path(name).suffix == '':
            # ASCII-only edits preserve both UTF-8 and legacy fixture encodings.
            return self.text(data.decode('latin1')).encode('latin1')
        # Sisu indexes (javax.inject.Named), Spring indexes and other provider
        # metadata often have nonstandard suffixes. Relocate arbitrary UTF-8
        # text resources too; binary resources and legal notices remain intact.
        if b'\0' not in data:
            try:
                return self.text(data.decode('utf-8')).encode('utf-8')
            except UnicodeDecodeError:
                pass
        return data

    def serialized_fixture(self, data):
        """Relocate length-prefixed class descriptors in historical test fixtures.

        Only exact ASCII class/signature UTF fields are accepted. This is not a
        general-purpose migration tool for arbitrary application serialized data.
        """
        edits = {}
        for match in re.finditer(rb'org[./]mule[./]', data):
            position = match.start()
            found = False
            for start in [position, position - 1, position - 2, position - 3]:
                if start < 2:
                    continue
                length = struct.unpack('>H', data[start - 2:start])[0]
                raw = data[start:start + length]
                if len(raw) != length or not re.fullmatch(rb'(?:\[+)?L?org[./]mule[./][A-Za-z0-9_.$/;]+', raw):
                    continue
                new = self.text(raw.decode('ascii')).encode('ascii')
                edits[start - 2] = (start + length, struct.pack('>H', len(new)) + new)
                found = True
                break
            if not found:
                raise ValueError('Unsupported namespace field in serialized fixture')
        for start, (end, replacement) in sorted(edits.items(), reverse=True):
            data = data[:start] + replacement + data[end:]
        return data

    def archive(self, data):
        entries, changed = [], False
        with zipfile.ZipFile(io.BytesIO(data)) as source:
            names = set()
            for info in source.infolist():
                name = self.text(info.filename)
                if name in names:
                    raise ValueError('Relocation collides at ' + name)
                names.add(name)
                original = source.read(info)
                content = self.content(info.filename, original)
                changed |= name != info.filename or content != original
                entries.append((info, name, content))
        if not changed:
            return data
        result = io.BytesIO()
        with zipfile.ZipFile(result, 'w', zipfile.ZIP_DEFLATED) as target:
            for info, name, content in entries:
                if re.match(r'^META-INF/[^/]+\.(SF|RSA|DSA|EC)$', name, re.I) or name == 'META-INF/INDEX.LIST':
                    continue
                if name.upper() == 'META-INF/MANIFEST.MF':
                    content = self.manifest(content, strip_digests=True)
                info.filename = name
                info.compress_type = zipfile.ZIP_DEFLATED
                target.writestr(info, content)
        return result.getvalue()


def discover(paths):
    names = set()
    for root in paths:
        for p in root.rglob('*'):
            if not p.is_file() or '.git' in p.parts or 'node_modules' in p.parts:
                continue
            if p.suffix == '.java':
                names.add(p.stem)
                text = p.read_bytes().decode('latin1')
                text = re.sub(r'/\*.*?\*/|//[^\n]*|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'', '', text, flags=re.S)
                names.update(re.findall(r'\b(?:class|interface|enum|record)\s+(\w+)', text))
            elif p.suffix == '.jar':
                with zipfile.ZipFile(p) as z:
                    for n in z.namelist():
                        if n.endswith('.class') and ('org/mule/' in n or 'io/zernova/' in n):
                            names.update(Path(n).stem.split('$'))
    return {n: n.replace('Mule', 'Zernova') for n in sorted(names) if 'Mule' in n and 'MuleSoft' not in n}


def relocate_tree(source, dest, relocation, in_place=False):
    records, pending = [], []
    for path in sorted(source.rglob('*')):
        rel = path.relative_to(source)
        if any(p in SKIP for p in rel.parts) or not path.is_file():
            continue
        if path.is_symlink():
            raise ValueError('Unexpected linked input: ' + str(path))
        if path.suffix in {'.sha1', '.md5', '.sha256', '.sha512', '.lastUpdated'}:
            if not in_place:
                continue
        new_rel = Path(relocation.text(rel.as_posix()))
        target = dest / new_rel
        old = path.read_bytes()
        content = relocation.content(rel.as_posix(), old)
        if not in_place or old != content or rel != new_rel:
            if in_place and target != path and target.exists():
                raise ValueError('Existing relocation target: ' + str(target))
            if in_place:
                pending.append((path, target, content))
            else:
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(content)
                shutil.copymode(path, target)
            if old != content or rel != new_rel:
                records.append({'from': rel.as_posix(), 'to': new_rel.as_posix(),
                                'inputSha256': hashlib.sha256(old).hexdigest(),
                                'outputSha256': hashlib.sha256(content).hexdigest()})
    for path, target, content in pending:
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(content)
        shutil.copymode(path, target)
        if in_place and target != path:
            path.unlink()
    if in_place:
        for p in sorted(source.rglob('*'), key=lambda p: len(p.parts), reverse=True):
            if p.is_dir() and not any(x in SKIP for x in p.relative_to(source).parts) and not any(p.iterdir()):
                p.rmdir()
    return records


def repository_worker_init(source, dest, names, application_format):
    global WORKER_SOURCE, WORKER_DEST, WORKER_RELOCATION
    WORKER_SOURCE, WORKER_DEST = Path(source), Path(dest)
    WORKER_RELOCATION = Relocation(names, application_format)


def repository_worker(name):
    path = WORKER_SOURCE / name
    new_name = WORKER_RELOCATION.text(name)
    target = WORKER_DEST / new_name
    old = path.read_bytes()
    content = WORKER_RELOCATION.content(name, old)
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(content)
    shutil.copymode(path, target)
    if old != content or name != new_name:
        return {'from': name, 'to': new_name, 'inputSha256': hashlib.sha256(old).hexdigest(),
                'outputSha256': hashlib.sha256(content).hexdigest()}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('operation', choices=['discover', 'tree', 'repository'])
    parser.add_argument('--input', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--map', type=Path, default=ROOT / 'zernova-build/namespace-map.json')
    parser.add_argument('--extra', type=Path, action='append', default=[])
    parser.add_argument('--application-format', action='store_true')
    args = parser.parse_args()
    if args.operation == 'discover':
        mapping = discover([args.input] + args.extra)
        args.output.write_text(json.dumps(mapping, indent=2) + '\n')
        print('Class renames:', len(mapping))
        return
    relocation = Relocation(json.loads(args.map.read_text()), args.application_format)
    if args.operation == 'repository' and args.input.resolve() == args.output.resolve():
        raise ValueError('Dependency inputs must remain immutable; use a separate output repository')
    if args.operation == 'repository' and args.output.exists():
        raise ValueError('Output repository already exists')
    if args.operation == 'repository':
        paths = [p.relative_to(args.input).as_posix() for p in sorted(args.input.rglob('*'))
                 if p.is_file() and not any(part in SKIP for part in p.relative_to(args.input).parts)
                 and p.suffix not in {'.sha1', '.md5', '.sha256', '.sha512', '.lastUpdated'}
                 and p.name != '.zernova-namespace-relocation.json']
        destinations = [relocation.text(name) for name in paths]
        if len(set(destinations)) != len(destinations):
            raise ValueError('Dependency relocation path collision')
        with ProcessPoolExecutor(max_workers=int(os.environ.get('ZN_RELOCATION_JOBS', '4')),
                                 initializer=repository_worker_init,
                                 initargs=(str(args.input), str(args.output), relocation.names, args.application_format)) as pool:
            records = [record for record in pool.map(repository_worker, paths, chunksize=8) if record]
    else:
        records = relocate_tree(args.input, args.output, relocation,
                                in_place=args.input.resolve() == args.output.resolve())
    report = args.output / '.zernova-namespace-relocation.json'
    report.write_text(json.dumps({'schemaVersion': 1, 'namespace': 'com.zernova',
                                 'applicationFormat': 1 if args.application_format else 0,
                                 'classMapSha256': hashlib.sha256(args.map.read_bytes()).hexdigest(),
                                 'changes': records}, indent=2) + '\n')
    print('Relocated files:', len(records))


if __name__ == '__main__':
    main()
