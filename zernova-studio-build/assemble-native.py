#!/usr/bin/env python3
"""Bundle the compiled ZERNOVA engine, Java and relocated connector dependencies.

Copyright (c) 2026 ZerNova. Original runtime development packaging tooling.
Engine modifications retain CPAL-1.0; independently written Studio files retain
their existing license. Java and dependencies retain their own notices.
"""
import argparse
import hashlib
import importlib.util
import json
from pathlib import Path, PurePosixPath
import shutil
import subprocess
import tempfile
import zipfile

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
SPEC = importlib.util.spec_from_file_location('relocation', ROOT / 'zernova-build/refactor_namespace.py')
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('target', choices=['darwin-x64', 'darwin-arm64', 'linux-x64', 'linux-arm64', 'win32-x64', 'win32-arm64'])
    parser.add_argument('--inputs', type=Path, default=HERE / 'runtime-baseline')
    parser.add_argument('--engine', type=Path, default=ROOT / 'upstream/distributions/standalone/target/mule-standalone-4.10.0-SNAPSHOT.zip')
    parser.add_argument('--output', type=Path, default=HERE / 'runtime')
    args = parser.parse_args()
    args.output = args.output.resolve()
    if args.output.exists():
        raise SystemExit('Refusing to replace existing runtime output: ' + str(args.output))
    baseline = json.loads((args.inputs / 'bundle.json').read_text())
    if baseline['target'] != args.target:
        raise SystemExit('Input Java bundle does not match target')
    # Compilation success is required; never label a patched vendor core as a
    # fresh source build. The engine archive itself is audited below.
    gate = ROOT / 'zernova-build-evidence/source-build-exit-code.txt'
    if not gate.exists() or gate.read_text().strip() != '0':
        raise SystemExit('The renamed source-build gate must pass first')
    args.output.mkdir(parents=True)
    for folder in ['java', 'connectors', 'notices']:
        shutil.copytree(args.inputs / folder, args.output / folder)
    engine = args.output / 'engine'
    engine.mkdir()
    with zipfile.ZipFile(args.engine) as z:
        prefixes = {PurePosixPath(n).parts[0] for n in z.namelist()}
        if len(prefixes) != 1:
            raise ValueError('Expected a single standalone distribution root')
        for info in z.infolist():
            name = PurePosixPath(info.filename)
            if name.is_absolute() or '..' in name.parts or '\\' in info.filename:
                raise ValueError('Unsafe standalone archive entry')
            rel = PurePosixPath(*name.parts[1:])
            if not rel.parts:
                continue
            dest = engine / str(rel)
            if info.is_dir():
                dest.mkdir(parents=True, exist_ok=True)
            else:
                dest.parent.mkdir(parents=True, exist_ok=True)
                dest.write_bytes(z.read(info))
                dest.chmod(0o755 if (info.external_attr >> 16) & 0o111 else 0o644)
    relocation = MODULE.Relocation(json.loads((ROOT / 'zernova-build/namespace-map.json').read_text()))
    MODULE.relocate_tree(args.output / 'connectors', args.output / 'connectors', relocation, True)
    # Compile the current lifecycle agent; no engine bytecode is relocated here.
    control = args.output / 'control'
    control.mkdir()
    agent_source = HERE / 'runtime-support/java/com/zernova/studio/RuntimeControlAgent.java'
    agent_source.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(HERE / 'RuntimeControlAgent.java', agent_source)
    with tempfile.TemporaryDirectory() as temp:
        subprocess.run(['javac', '--release', '17', '-d', temp, str(agent_source)], check=True)
        with zipfile.ZipFile(control / 'runtime-control.jar', 'w', zipfile.ZIP_DEFLATED) as z:
            z.writestr('META-INF/MANIFEST.MF', 'Manifest-Version: 1.0\r\nPremain-Class: com.zernova.studio.RuntimeControlAgent\r\n\r\n')
            for path in sorted(Path(temp).rglob('*.class')):
                z.write(path, path.relative_to(temp).as_posix())
    sources = args.output / 'sources'
    sources.mkdir()
    source_archive = sources / 'ZERNOVA_Runtime_Corresponding_Source.zip'
    excluded = {'.git', 'target', 'node_modules', '__pycache__'}
    with zipfile.ZipFile(source_archive, 'w', zipfile.ZIP_DEFLATED) as z:
        for folder in ['buildtools', 'core', 'core-tests', 'errors', 'manifest', 'modules',
                       'runtime-extension-model', 'tests', 'upstream', 'zernova-build', 'zernova-studio-build/src']:
            for path in sorted((ROOT / folder).rglob('*')):
                if path.is_file() and not any(p in excluded or p.startswith('.zernova-') for p in path.relative_to(ROOT).parts):
                    z.write(path, path.relative_to(ROOT).as_posix())
        for name in ['pom.xml', 'LICENSE.txt', 'README.md', 'NAMESPACE_MIGRATION.md',
                     'package.json', 'package-lock.json',
                     'zernova-studio-build/RuntimeControlAgent.java', 'zernova-studio-build/LICENSE.txt',
                     'zernova-studio-build/assemble-native.py', 'zernova-studio-build/prepare-native.py',
                     'zernova-studio-build/prepare-bootstrap-native.py', 'zernova-studio-build/cli.cjs',
                     'zernova-studio-build/create-fixture.py', 'zernova-studio-build/build-portable.cjs',
                     'zernova-studio-build/tests/native-runtime.cjs']:
            if (ROOT / name).is_file():
                z.write(ROOT / name, name)
    revision = json.loads((ROOT / 'zernova-build-evidence/source-build-revision.json').read_text())
    info = {**baseline, 'version': '0.3.0-dev.2', 'javaNamespace': 'com.zernova',
            'sourceBuildCommit': revision['sourceRevision'], 'sourceBuildDirty': revision['dirty'],
            'sourceTreeCommit': subprocess.check_output(['git', '-C', str(ROOT), 'rev-parse', 'HEAD'], text=True).strip(),
            'inputEngineSha256': sha(args.engine), 'sourceArchive': source_archive.relative_to(args.output).as_posix(),
            'sourceArchiveSha256': sha(source_archive), 'engineBuild': 'compiled-from-renamed-source'}
    (args.output / 'bundle.json').write_text(json.dumps(info, indent=2) + '\n')
    files = {p.relative_to(args.output).as_posix(): {'sha256': sha(p), 'bytes': p.stat().st_size,
             'executable': bool(p.stat().st_mode & 0o111)} for p in sorted(args.output.rglob('*')) if p.is_file()}
    (args.output / 'checksums.json').write_text(json.dumps({'schemaVersion': 1, 'files': files}, indent=2) + '\n')
    subprocess.run(['python3', str(ROOT / 'zernova-build/audit_namespace.py'), '--runtime', str(args.output),
                    '--output', str(ROOT / 'zernova-build-evidence/runtime-audit.json')], check=True)
    print(json.dumps({'bundle': str(args.output), 'files': len(files), 'engineBuild': info['engineBuild']}, indent=2))


if __name__ == '__main__':
    main()
