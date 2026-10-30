#!/usr/bin/env python3
"""Fail on legacy Java declarations and legacy classes in nested runtime archives."""
import argparse
import io
import json
from pathlib import Path
import re
import zipfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE_DIRS = ['core', 'core-tests', 'errors', 'manifest', 'modules',
               'runtime-extension-model', 'tests', 'upstream']


def audit_sources(root):
    errors, count = [], 0
    for folder in SOURCE_DIRS:
        for path in (root / folder).rglob('*.java'):
            if 'target' in path.parts:
                continue
            count += 1
            text = path.read_bytes().decode('latin1')
            package = re.search(r'^package\s+([\w.]+)\s*;', text, re.M)
            if package:
                if package[1].startswith('org.mule'):
                    errors.append('Legacy package: ' + str(path))
                suffix = package[1].replace('.', '/') + '/' + path.name
                compiled_source = '/src/main/java/' in path.as_posix() or '/src/test/java/' in path.as_posix()
                if compiled_source and not path.as_posix().endswith(suffix):
                    errors.append('Package/path mismatch: ' + str(path))
            if 'Mule' in path.stem and package and package[1].startswith('com.zernova'):
                errors.append('Legacy class name: ' + str(path))
            declarations = re.sub(r'/\*.*?\*/|//[^\n]*|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'', '', text, flags=re.S)
            if any('Mule' in n for n in re.findall(r'\b(?:class|interface|enum|record)\s+(\w+)', declarations)):
                errors.append('Legacy nested/type declaration: ' + str(path))
            if re.search(r'\b(?:import|requires(?:\s+transitive|\s+static)?|exports|opens|uses|provides)\s+org\.mule\b', text):
                errors.append('Legacy Java/module reference: ' + str(path))
    return count, errors


def audit_archive(data, name, counts, errors):
    with zipfile.ZipFile(io.BytesIO(data)) as archive:
        for entry in archive.infolist():
            value = archive.read(entry)
            label = name + '!/' + entry.filename
            if entry.filename.endswith('.class'):
                counts['classes'] += 1
                if 'org/mule/' in entry.filename or b'org/mule/' in value or b'org.mule.' in value:
                    errors.append('Legacy Java namespace: ' + label)
                if '/com/zernova/' in '/' + entry.filename and 'Mule' in Path(entry.filename).stem:
                    errors.append('Legacy class name: ' + label)
            elif value[:4] == b'PK\x03\x04' and Path(entry.filename).suffix in {'.jar', '.zip', '.mule'}:
                counts['archives'] += 1
                audit_archive(value, label, counts, errors)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', type=Path, default=ROOT)
    parser.add_argument('--runtime', type=Path)
    parser.add_argument('--output', type=Path)
    args = parser.parse_args()
    count, errors = audit_sources(args.root)
    counts = {'sources': count, 'archives': 0, 'classes': 0}
    if args.runtime:
        for path in args.runtime.rglob('*'):
            if path.suffix in {'.jar', '.mule'} and 'sources' not in path.parts:
                counts['archives'] += 1
                audit_archive(path.read_bytes(), str(path), counts, errors)
    result = {'schemaVersion': 1, 'counts': counts, 'passed': not errors, 'errors': errors}
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(result, indent=2) + '\n')
    print(json.dumps(result, indent=2))
    raise SystemExit(bool(errors))


if __name__ == '__main__':
    main()
