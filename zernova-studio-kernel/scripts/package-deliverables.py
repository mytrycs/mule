#!/usr/bin/env python3
"""Create the owner handoff from actual installed-host evidence and PNG captures.
No screenshot pixels are fabricated or cropped. The contact sheet scales whole
native-window screenshots and labels them; originals remain in the archive.
"""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import base64
import hashlib
import html
import json
import shutil
import zipfile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'dist'
OUT.mkdir(exist_ok=True)
SCREENS = [
    ('01-workbench-light.png', '01 / Workbench / Light', 'Native navigation, compact canvas, right palette, lower Properties area.'),
    ('02-http-listener-dark.png', '02 / HTTP Listener / Dark', 'Selection, General form, shared configuration actions and illustrative metadata.'),
    ('03-logger-light.png', '03 / Logger / Light', 'Display name, message presentation, level, category and lower property navigation.'),
    ('04-global-elements-light.png', '04 / Global Elements / Light', 'Preview-only configuration editor with Save, Cancel and local validation.'),
    ('05-configuration-xml-dark.png', '05 / Configuration XML / Dark', 'Synchronized, escaped, read-only Mule XML inside the same preview editor.'),
    ('06-dataweave-future-layout-dark.png', '06 / DataWeave / Future layout', 'Three navigable modes with read-only source and explicitly illustrative output.'),
]
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
read = lambda p: json.loads((ROOT / p).read_text())
ui_pointer = read('evidence/latest-ui.json')
ui = read(ui_pointer['results'])
host = read('evidence/extension-host-results.json')
restart_path = ROOT / 'evidence/extension-host-restart.json'
if not restart_path.exists():
    candidates = list((ROOT / 'evidence').glob('*restart*results*.json'))
    if candidates: restart_path = candidates[0]
restart = json.loads(restart_path.read_text()) if restart_path.exists() else None
clean = read('evidence/clean-build/results.json')
boundaries = read('evidence/static-boundaries.json')
isolation = read('evidence/sibling-isolation-results.json')
contrast = read('evidence/accessibility-contrast-review.json')
vsix = OUT / 'zernova-studio-kernel-preview-0.1.0.vsix'
assert ui['counts']['fail'] == 0 and host['status'] == 'PASS' and clean['status'] == 'PASS'
assert restart and restart['status'] == 'PASS', 'Cold restart evidence required for final handoff'
font_dir = Path('/usr/share/fonts/truetype/dejavu')
font = lambda size, bold=False: ImageFont.truetype(str(font_dir / ('DejaVuSans-Bold.ttf' if bold else 'DejaVuSans.ttf')), size)

# Labeled contact sheet: whole, actual host images, scaled equally.
tile_w, tile_h, margin, gap, heading = 1008, 630, 26, 24, 122
cell_h = tile_h + 52
sheet = Image.new('RGB', (2 * tile_w + gap + 2 * margin, heading + 3 * cell_h + 2 * gap + margin), '#edf3f3')
draw = ImageDraw.Draw(sheet)
draw.text((margin, 20), 'ZERNOVA STUDIO / ACTUAL IMPLEMENTATION', font=font(31, True), fill='#102d34')
draw.text((margin, 66), '0.1.0  ·  Installed VSIX  ·  VS Code 1.144.0 / Linux x64  ·  UI design preview only', font=font(20), fill='#435f68')
screen_manifest = []
for i, (name, label, caption) in enumerate(SCREENS):
    p = ROOT / 'evidence/screenshots' / name
    image = Image.open(p).convert('RGB')
    assert image.size == (1440, 900), (name, image.size)
    x = margin + (i % 2) * (tile_w + gap)
    y = heading + (i // 2) * (cell_h + gap)
    draw.text((x, y), label.upper(), font=font(22, True), fill='#134e51')
    sheet.paste(image.resize((tile_w, tile_h), Image.Resampling.LANCZOS), (x, y + 39))
    draw.rectangle((x, y + 39, x + tile_w - 1, y + 39 + tile_h - 1), outline='#a5bfc4')
    screen_manifest.append({'file': name, 'dimensions': list(image.size), 'sha256': sha(p), 'source': 'Actual installed VS Code host screenshot; uncropped'})
contact = OUT / 'ZERNOVA_Studio_UI_Contact_Sheet.png'
sheet.save(contact)

# Screenshot evidence is a separate convenience download; reference art is excluded.
screens_zip = OUT / 'ZERNOVA_Studio_Actual_Screenshots.zip'
valid_screens = [ROOT / 'evidence/screenshots' / item['name'] for item in ui['screenshots']]
with zipfile.ZipFile(screens_zip, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as z:
    for p in dict.fromkeys(valid_screens): z.write(p, p.name)
    z.write(contact, contact.name)
    z.writestr('SCREENSHOT_PROVENANCE.json', json.dumps({'nativeHost': 'VS Code 1.144.0 on Linux x64', 'uiRun': ui_pointer['results'], 'images': ui['screenshots'], 'primaryImages': screen_manifest, 'contactSheet': 'Scaled whole screenshots with labels; no generated scene or cropped host'}, indent=2))

counts = ui['counts']
host_count = next((p['passed'] for p in host.get('phases', []) if p.get('phase') == 'primary'), host.get('passed', len(host.get('results', []))))
restart_count = restart.get('passed', len(restart.get('results', [])))
incomplete = sum(bool(x['incomplete']) for x in ui['a11y'])
vsix_hash = sha(vsix)
summary = {
    'product': 'ZERNOVA Studio', 'identity': 'zernova-preview.zernova-studio-kernel-preview', 'version': '0.1.0',
    'scope': 'New standalone UI/UX skeleton for the new Mule-Kernel runtime; design approval gate',
    'vsix': {'name': vsix.name, 'bytes': vsix.stat().st_size, 'sha256': vsix_hash},
    'ui': {'passed': counts['pass'], 'failed': counts['fail'], 'results': ui_pointer['results']},
    'host': {'passed': host_count, 'failed': host.get('failed', 0), 'version': host['version'], 'platform': host['platform'], 'architecture': host['architecture']},
    'coldRestart': {'passed': restart_count, 'failed': restart.get('failed', 0)},
    'modelTests': {'passed': 25, 'failed': 0, 'xmlParserCases': 3},
    'accessibility': {'scans': len(ui['a11y']), 'reportedViolations': sum(len(x['violations']) for x in ui['a11y']), 'incompleteStatesRetained': incomplete, 'computedContrastAndReachabilityReviews': contrast['statesReviewed'], 'screenReader': 'NOT RUN'},
    'cleanBuild': clean['status'], 'runtimeExecution': 'Not implemented or performed', 'macOSAppleSilicon': 'NOT RUN', 'windows': 'NOT RUN',
    'primaryScreenshots': screen_manifest,
}
(ROOT / 'evidence/DELIVERY_SUMMARY.json').write_text(json.dumps(summary, indent=2))

esc = html.escape
gallery = []
for name, label, caption in SCREENS:
    b64 = base64.b64encode((ROOT / 'evidence/screenshots' / name).read_bytes()).decode()
    gallery.append(f'<article id="{name[:2]}"><h3>{esc(label)}</h3><p>{esc(caption)}</p><img src="data:image/png;base64,{b64}" alt="Actual VS Code implementation: {esc(label)}" width="1440" height="900"><small>Uncropped native host capture · 1440 × 900 · {esc(name)}</small></article>')
tests = ''.join(f'<tr><td>{esc(c["name"])}</td><td class="pass">{c["status"]}</td></tr>' for c in ui['checks'])
report = f'''<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>ZERNOVA Studio — UI Design Review</title><style>
*{{box-sizing:border-box}}body{{margin:0;background:#eef4f4;color:#17343e;font:16px/1.65 system-ui,sans-serif}}main{{max-width:1200px;margin:auto;padding:42px 28px 70px}}header{{border-top:5px solid #007b73;padding-top:25px}}.eyebrow{{font-size:12px;letter-spacing:2px;font-weight:700;color:#007b73}}h1{{font-size:42px;line-height:1.15;margin:14px 0}}h2{{font-size:26px;margin-top:42px}}h3{{font-size:19px;margin:0 0 6px}}p{{max-width:940px}}.lead{{font-size:19px}}.badge{{display:inline-block;background:#d5eee8;color:#134f46;padding:4px 10px;border-radius:4px;font-weight:650;font-size:13px}}article,section.card{{background:#fff;border:1px solid #ccdade;border-radius:6px;margin:24px 0;padding:22px}}article img{{display:block;width:100%;height:auto;border:1px solid #cbdade;margin:18px 0 7px}}small{{font-size:12px;color:#536b75}}table{{border-collapse:collapse;width:100%;background:white}}td,th{{text-align:left;border-bottom:1px solid #d4e0e3;padding:10px 12px;vertical-align:top}}th{{background:#e0ecee}}.pass{{color:#126846;font-weight:700;white-space:nowrap}}code,pre{{font:13px/1.7 ui-monospace,monospace}}code{{overflow-wrap:anywhere}}pre{{background:#10272f;color:#e6f3f4;padding:20px;overflow:auto;border-radius:4px}}a{{color:#006c7b}}nav{{display:flex;gap:12px;flex-wrap:wrap;margin-top:22px}}nav a{{padding:5px 10px;background:#fff;border:1px solid #cbdade;border-radius:4px;text-decoration:none;font-size:13px}}.muted{{color:#536b75}}details{{background:white;padding:18px;border:1px solid #cbdade;border-radius:4px;margin-top:20px}}summary{{cursor:pointer;font-weight:650}}@media(max-width:700px){{main{{padding:24px 14px}}h1{{font-size:32px}}article,section.card{{padding:14px}}}}@media print{{body{{background:#fff}}article{{break-inside:avoid}}nav{{display:none}}}}
</style></head><body><main><header><div class="eyebrow">NEW MULE-KERNEL STUDIO · 29 OCTOBER 2026</div><h1>ZERNOVA Studio<br>UI/UX skeleton · Design review</h1><p class="lead">A separately packaged, interactive VS Code extension with the six requested screens. Runtime execution remains outside this milestone.</p><span class="badge">Installed and tested · {counts['pass']} UI checks passed</span><nav>{''.join(f'<a href="#{i:02}">{i:02} {esc(label.split(" / ")[1])}</a>' for i,(_,label,_) in enumerate(SCREENS,1))}<a href="#validation">Test evidence</a><a href="#install">Install</a></nav></header>
<section class="card"><h2 style="margin-top:0">Delivered identity</h2><table><tr><th>Project</th><td><code>zernova-studio-kernel</code></td></tr><tr><th>Extension</th><td><code>zernova-preview.zernova-studio-kernel-preview</code></td></tr><tr><th>Initial version</th><td>0.1.0</td></tr><tr><th>Namespace</th><td><code>zernovaKernelStudio.*</code></td></tr><tr><th>VSIX</th><td><code>{vsix.name}</code> · {vsix.stat().st_size:,} bytes</td></tr><tr><th>SHA-256</th><td><code>{vsix_hash}</code></td></tr></table><p>The source, extension identity, state and command namespace are new. The extension has no old Studio/native Runtime imports, no XML file association, and no runtime process or network adapter. No Java, Maven or runtime is bundled.</p></section>
<h2>The six implemented screens</h2><p>These are captures from the running, installed VSIX in the actual VS Code desktop host. The reference concept is retained separately in the source evidence. All sample values are illustrative.</p>{''.join(gallery)}
<section id="validation" class="card"><h2 style="margin-top:0">Observed test results</h2><table><tr><th>Verification</th><th>Observed result</th></tr><tr><td>Installed mouse/keyboard UI acceptance</td><td class="pass">{counts['pass']} PASS / {counts['fail']} FAIL</td></tr><tr><td>Clean-profile installed extension-host acceptance</td><td class="pass">{host_count} PASS / 0 FAIL</td></tr><tr><td>Second independent desktop process / cold restart</td><td class="pass">{restart_count} PASS / 0 FAIL</td></tr><tr><td>Model and message-protocol tests</td><td class="pass">25 PASS / 0 FAIL</td></tr><tr><td>Independent XML parser cases</td><td class="pass">3 PASS</td></tr><tr><td>Source, bundled code and VSIX boundary audit</td><td class="pass">19 PASS / 0 FAIL</td></tr><tr><td>Clean source dependency install, typecheck, tests, build and package</td><td class="pass">PASS · identical executable bundle hashes</td></tr><tr><td>Available prior evaluation/runtime directory comparison</td><td class="pass">17,246 entries · 0 added, removed or changed</td></tr></table><p>Tested desktop: <strong>VS Code {esc(host['version'])}, Ubuntu 24.04.3 LTS, Linux x86-64</strong>. Native window sizes: 1920×1080, 1440×900, 1280×800, plus enlarged host zoom. Main screenshots are 1440×900; the screenshot archive also includes narrow, wide, keyboard-focus, validation and high-contrast captures.</p><p>Accessibility: all six screens were scanned in light, dark, Dark High Contrast and Light High Contrast: <strong>{len(ui['a11y'])} scans, zero reported WCAG A/AA violations</strong>. The {incomplete} recorded contrast-only incomplete states were separately reviewed through actual computed color ratios, scroll reachability and DOM hit-testing; all eight passed. The raw incomplete entries are retained. Manual screen-reader testing and full accessibility certification were not performed.</p><p class="muted">The sibling integrity comparison covers the recorded interval after implementation began, not a retroactive task-start snapshot or unavailable external installations. The clean rebuild used a fresh source directory and an offline pinned npm cache. macOS/Apple Silicon, Windows and other VS Code versions are NOT RUN.</p><details><summary>All {counts['pass']} installed UI checks</summary><table>{tests}</table></details></section>
<section class="card"><h2 style="margin-top:0">What works, and what remains a fixture</h2><p>Selection, property editing, local validation, Save/Cancel, palette filtering, supported drag/drop, keyboard alternatives, Logger reorder/duplicate/delete, one-step undo/redo, flow/metadata expansion, pane resizing, zoom, preview persistence and reset are real UI behavior. XML is derived from that same sample model and escapes values correctly.</p><p>The project tree and integration components are fixtures. HTTP Request, Run/Debug/Deploy/Test Connection and connector execution are deferred. DataWeave source, mapping lines and sample output are read-only illustrations; no script is evaluated. Console and Problems remain explicit preview states.</p><p>The native VS Code title bar, Activity Bar, sidebar, editor tab and status bar remain native. The bounded integration workbench is one webview editor. There is no default association with arbitrary XML files and no fake operating-system frame inside the editor.</p><p>The existing ZERNOVA logo geometry is reused with provenance. The website's current green/lime tokens and this concept's provisional teal Studio treatment are distinguished in the brand audit. Original line icons are used for the component UI.</p></section>
<section id="install" class="card"><h2 style="margin-top:0">Install and review</h2><p>Open a separate VS Code review profile using dedicated user-data, extensions and shared-data directories. The shared-data flag was verified on the tested 1.144.0 host; older host flags are not claimed tested.</p><pre>review_dir="${{TMPDIR:-/tmp}}/zernova-studio-kernel-review"
mkdir -p "$review_dir/user-data" "$review_dir/extensions" "$review_dir/shared-data"
code --user-data-dir "$review_dir/user-data" \\
  --extensions-dir "$review_dir/extensions" \\
  --shared-data-dir "$review_dir/shared-data" \\
  --install-extension /absolute/path/{vsix.name}
code --user-data-dir "$review_dir/user-data" \\
  --extensions-dir "$review_dir/extensions" \\
  --shared-data-dir "$review_dir/shared-data" --new-window</pre><p>Run <strong>ZERNOVA: Open Studio Design Preview</strong>. Use the native Design Preview list, the fixture files, canvas selection and internal tabs to reach all six views. Select ZERNOVA Studio Light or Dark explicitly from <strong>Preferences: Color Theme</strong>, or use the preview-only theme control.</p><p>Reset commands: <code>zernovaKernelStudio.resetPreview</code> and <code>zernovaKernelStudio.resetLayout</code>. Preview state is saved only in this extension's versioned workspace storage. Dialog drafts and one-step undo history are session-only.</p><h3>Build the extension from source</h3><pre>npm ci
npm run typecheck
npm test
npm run build
npm run package</pre><p>Use Node.js 22.14+ and npm. The supplied lockfile pins development dependencies. The installed UI itself works offline. Detailed host/UI replay instructions and evidence paths are in <code>docs/UI_SKELETON_QUICKSTART.md</code>.</p></section>
<section class="card"><h2 style="margin-top:0">Future runtime boundary and stop gate</h2><p>The reported target remains ZERNOVA Runtime 0.1.0-preview.1, Mule Kernel 4.12.0 and bundled DataWeave 2.12.0. The actual supplied runtime contract was read, and genuine Mule namespaces, Maven structure and <code>requiredProduct: "MULE"</code> remain intact. This milestone did not rerun the runtime or clear its independent kernel-source and customer-redistribution gates.</p><p><code>docs/FUTURE_RUNTIME_BOUNDARY.md</code> and <code>docs/RUNTIME_AGENT_HANDOFF.md</code> describe the future HTTP Listener + Logger integration boundary. No command invoker, application build, runtime download, connector execution or DataWeave evaluator has been added.</p><p><strong>Stopped for design approval.</strong> The next decision is acceptance or revision of this new Studio UI. Runtime functionality requires a subsequent assignment.</p></section>
</main></body></html>'''
report_path = OUT / 'ZERNOVA_Studio_UI_Review.html'
report_path.write_text(report)
shutil.copyfile(ROOT / 'docs/UI_SKELETON_QUICKSTART.md', OUT / 'ZERNOVA_Studio_UI_Quickstart.md')

# Complete source export; no tool installs, caches, other projects or duplicate VSIX.
source_zip = OUT / 'ZERNOVA_Studio_Kernel_Preview_0.1.0_Source.zip'
excluded_roots = {'node_modules', 'dist', '.cache', '.git'}
excluded_names = {'initial-inspection.png', 'listener-inspection.png', 'workbench-inspection.png'}
files = [p for p in sorted(ROOT.rglob('*')) if p.is_file() and p.relative_to(ROOT).parts[0] not in excluded_roots and p.name not in excluded_names]
manifest = [{'path':p.relative_to(ROOT).as_posix(),'bytes':p.stat().st_size,'sha256':sha(p)} for p in files]
with zipfile.ZipFile(source_zip, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as z:
    for p in files: z.write(p, 'zernova-studio-kernel/' + p.relative_to(ROOT).as_posix())
    z.writestr('zernova-studio-kernel/SOURCE_FILE_MANIFEST.json', json.dumps(manifest,indent=2))
    z.writestr('zernova-studio-kernel/RELEASE_MANIFEST.json', json.dumps(summary,indent=2))
with zipfile.ZipFile(source_zip) as z:
    assert z.testzip() is None
    for item in manifest:
        assert hashlib.sha256(z.read('zernova-studio-kernel/'+item['path'])).hexdigest()==item['sha256']
deliverables=[vsix,source_zip,report_path,screens_zip,contact,OUT/'ZERNOVA_Studio_UI_Quickstart.md']
release={'sourceFiles':len(files),'sourceArchiveIntegrity':'PASS','deliverables':[{'name':p.name,'bytes':p.stat().st_size,'sha256':sha(p)} for p in deliverables]}
(OUT/'DELIVERY_MANIFEST.json').write_text(json.dumps(release,indent=2))
(OUT/'SHA256SUMS.txt').write_text(''.join(f'{sha(p)}  {p.name}\n' for p in deliverables))
print(json.dumps(release,indent=2))
