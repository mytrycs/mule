#!/usr/bin/env python3
"""Package the final, hash-bound Studio milestone; never include runtime JARs."""
from pathlib import Path
import hashlib
import json
import shutil
import sys
import subprocess
import zipfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'dist'
OUT.mkdir(exist_ok=True)
read = lambda path: json.loads((ROOT / path).read_text())
digest = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()
manifest = read('package.json')
version = manifest['version']
def archive(path, files):
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as zipped:
        for source, relative in files:
            assert source.is_file() and not source.is_symlink(), source
            zipped.write(source, relative)
    with zipfile.ZipFile(path) as zipped:
        assert zipped.testzip() is None

# Keep complete source, tests and notices, but avoid build outputs/caches and
# duplicate large evidence. Current verification has its own dedicated archive.
source_zip = OUT / f'ZERNOVA_Studio_Kernel_{version}_Source.zip'
excluded = {'node_modules', 'dist', '.git', '.cache', 'evidence', 'screenshots'}
sources = [p for p in sorted(ROOT.rglob('*')) if p.is_file() and p.relative_to(ROOT).parts[0] not in excluded and '__pycache__' not in p.parts and p.suffix != '.pyc']
for path in sources:
    assert path.suffix.lower() not in {'.jar', '.class', '.so', '.dll', '.dylib', '.exe'}, path
source_entries = [(p, 'zernova-studio-kernel/' + p.relative_to(ROOT).as_posix()) for p in sources]
source_manifest = [{'path': p.relative_to(ROOT).as_posix(), 'bytes': p.stat().st_size, 'sha256': digest(p)} for p in sources]
source_record = ROOT / 'evidence/phase2b/source-archive.json'
if source_zip.exists() and source_record.exists():
    prior = json.loads(source_record.read_text())
    assert digest(source_zip) == prior['sha256'], 'Frozen source archive bytes changed'
    with zipfile.ZipFile(source_zip) as zipped:
        stored = json.loads(zipped.read('zernova-studio-kernel/SOURCE_FILE_MANIFEST.json'))
    assert stored == source_manifest, 'Source files changed after archive freeze; review before issuing another release archive'
else:
    archive(source_zip, source_entries)
    with zipfile.ZipFile(source_zip, 'a', zipfile.ZIP_DEFLATED, compresslevel=9) as zipped:
        zipped.writestr('zernova-studio-kernel/SOURCE_FILE_MANIFEST.json', json.dumps(source_manifest, indent=2))
    source_record.parent.mkdir(parents=True, exist_ok=True)
    source_record.write_text(json.dumps({'file': str(source_zip), 'sha256': digest(source_zip), 'files': len(source_manifest), 'frozen': True}, indent=2))
if '--source-only' in sys.argv:
    print(source_record.read_text())
    sys.exit(0)


selection = read('evidence/phase2b/final-selection.json')
assert selection['stage'] == 'final', 'Calibration evidence cannot authorize a final release package'
vsix = OUT / f"{manifest['name']}-{version}.vsix"
assert digest(vsix) == selection['vsixSha256'], 'The VSIX changed after final evidence selection'

screens = []
workflow = []
for part in ['create', 'persistence', 'runtime', 'themes']:
    result = read(selection[part]['results'])
    assert result['counts']['fail'] == 0 and result['vsix']['sha256'] == selection['vsixSha256']
    workflow.append({'part': part, 'counts': result['counts'], 'evidence': selection[part]['results']})
    screens.extend(result['screenshots'])

subprocess.run(['node', 'scripts/render-owner-guide.mjs', '--final'], cwd=ROOT, check=True)
for name in ['COMPATIBILITY.md', 'KNOWN_LIMITATIONS.md', 'CHANGELOG.md']:
    shutil.copyfile(ROOT / name, OUT / name)

owner = read('evidence/phase2b/owner-project.json')
example_root = Path(owner['sourceSnapshot'])
assert example_root.is_dir(), 'Example must come from the saved native-wizard/UI source snapshot'
example_files = [p for p in sorted(example_root.rglob('*')) if p.is_file()]
loggers = ET.parse(example_root / 'src/main/mule/hello.xml').getroot().findall('.//{http://www.mulesoft.org/schema/mule/core}logger')
logger_names = [node.attrib.get('{http://www.mulesoft.org/schema/mule/documentation}name', 'Logger') for node in loggers]
for path in example_files:
    assert path.suffix.lower() not in {'.jar', '.class', '.zip', '.so', '.dll', '.dylib', '.exe'}, path
    assert 'target' not in path.relative_to(example_root).parts
example_zip = OUT / f'ZERNOVA_Studio_Created_Example_{version}_Source.zip'
archive(example_zip, [(p, owner['projectName'] + '/' + p.relative_to(example_root).as_posix()) for p in example_files])
with zipfile.ZipFile(example_zip, 'a', zipfile.ZIP_DEFLATED, compresslevel=9) as zipped:
    zipped.writestr('APPLICATION_SOURCE_PROVENANCE.json', json.dumps({
        'createdBy': owner['createdBy'], 'creationEvidence': owner['creationEvidence'],
        'snapshot': 'Saved source after the installed UI creation phase, before path redeployment changes',
        'defaultRoute': '/hello', 'loggerCount': len(loggers), 'loggerExecutionOrder': logger_names, 'loggerNote': 'Owner tutorial begins with one Logger; saved source retains the real second Logger from insertion/reorder/duplicate/delete acceptance. No manual XML simplification.', 'binaries': 'Excluded; build the application locally using the owner guide',
        'files': [{'path': p.relative_to(example_root).as_posix(), 'sha256': digest(p)} for p in example_files],
    }, indent=2))

screens_zip = OUT / f'ZERNOVA_Studio_{version}_Actual_Screenshots.zip'
archive(screens_zip, [(Path(item['path']), Path(item['path']).parent.parent.name + '/' + item['name']) for item in screens])
with zipfile.ZipFile(screens_zip, 'a', zipfile.ZIP_DEFLATED, compresslevel=9) as zipped:
    zipped.writestr('SCREENSHOT_PROVENANCE.json', json.dumps({
        'vsixSha256': selection['vsixSha256'],
        'source': 'Actual installed VS Code desktop; complete native window; no pixel edits',
        'screenshots': [{'name': x['name'], 'sha256': digest(Path(x['path']))} for x in screens],
    }, indent=2))

evidence_zip = OUT / f'ZERNOVA_Studio_{version}_Test_Evidence.zip'
evidence_files = [p for p in sorted((ROOT / 'evidence').rglob('*')) if p.is_file()
                  and (p.relative_to(ROOT / 'evidence').parts[0].startswith('phase2b')
                       or p.relative_to(ROOT / 'evidence').parts[0] in {'clean-build'}
                       or p.name in {'extension-host-results.json', 'extension-host-primary.json', 'extension-host-restart.json',
                                     'host-test-environment.json', 'host-install.log', 'static-boundaries.json', 'latest-ui.json'})]
# Include the selected fresh preview run, not prior milestone screenshot counts.
if selection.get('preview'):
    preview_record = read(selection['preview']['results'])
    preview_dir = (ROOT / selection['preview']['results']).parent
    evidence_files.extend(p for p in preview_dir.rglob('*') if p.is_file())
    evidence_files.extend(ROOT / 'evidence/screenshots' / x['name'] for x in preview_record['screenshots'])
evidence_files = sorted(set(evidence_files))
for path in evidence_files:
    assert path.suffix.lower() not in {'.jar', '.class', '.so', '.dll', '.dylib', '.exe'}, path
archive(evidence_zip, [(p, p.relative_to(ROOT).as_posix()) for p in evidence_files])

deliverables = [vsix, source_zip, example_zip, OUT / 'TESTING_STEP_BY_STEP.md', OUT / 'TESTING_STEP_BY_STEP.html',
                screens_zip, evidence_zip, OUT / 'COMPATIBILITY.md', OUT / 'KNOWN_LIMITATIONS.md', OUT / 'CHANGELOG.md',
                OUT / 'GUIDE_SCREENSHOT_PROVENANCE.json']
release = {'schemaVersion': 1, 'product': 'ZERNOVA Studio', 'version': version,
           'extensionId': manifest['publisher'] + '.' + manifest['name'], 'stage': 'HTTP Listener → Logger owner acceptance',
           'workflowEvidence': workflow, 'finalSelection': selection,
           'sourceFiles': len(source_manifest), 'applicationFiles': len(example_files),
           'deliverables': [{'name': p.name, 'bytes': p.stat().st_size, 'sha256': digest(p)} for p in deliverables]}
(OUT / 'DELIVERY_MANIFEST.json').write_text(json.dumps(release, indent=2) + '\n')
deliverables.append(OUT / 'DELIVERY_MANIFEST.json')
(OUT / 'SHA256SUMS.txt').write_text(''.join(f'{digest(p)}  {p.name}\n' for p in deliverables))
owner_delivery = OUT / f'ZERNOVA_Studio_{version}_Owner_Delivery.zip'
archive(owner_delivery, [(p, p.name) for p in deliverables + [OUT / 'SHA256SUMS.txt']])
print(json.dumps({'ownerDelivery': owner_delivery.name, 'bytes': owner_delivery.stat().st_size, 'sha256': digest(owner_delivery)}, indent=2))
print(json.dumps({'sourceFiles': len(source_manifest), 'applicationFiles': len(example_files),
                  'artifacts': [{'name': p.name, 'bytes': p.stat().st_size} for p in deliverables]}, indent=2))
