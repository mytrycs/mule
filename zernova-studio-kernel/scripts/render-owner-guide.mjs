/** Render the complete owner Markdown guide and whole, actual desktop captures. */
import { readFile, writeFile, mkdir } from 'node:fs/promises';
import { resolve, join, basename } from 'node:path';
import { createHash } from 'node:crypto';
import { marked } from 'marked';
import sax from 'sax';

const final = process.argv.includes('--final');
const root = resolve('.'), output = join(root, 'dist');
await mkdir(output, { recursive: true });
const readJson = async path => JSON.parse(await readFile(resolve(root, path), 'utf8'));
const escape = value => String(value).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const manifest = await readJson('package.json');
let selection;
try { selection = await readJson('evidence/phase2b/final-selection.json'); } catch { /* Draft guide may precede the final acceptance run. */ }
if (final && selection?.stage !== 'final') throw new Error('Final guide requires a final evidence selection; calibration is not release evidence.');
const runs = [];
for (const part of ['create', 'persistence', 'runtime', 'themes']) {
  try {
    const pointer = final ? selection[part] : await readJson(`evidence/phase2b/latest-${part}.json`);
    const result = await readJson(pointer.results);
    if (final && (result.counts.fail || result.vsix.sha256 !== selection.vsixSha256)) throw new Error(`Final ${part} evidence differs from release artifact or contains failures`);
    runs.push({ part, result, path: pointer.results });
  } catch (error) { if (final) throw error; }
}
const headings = [];
let owner, loggerNames = [];
try {
  owner = await readJson('evidence/phase2b/owner-project.json');
  if (owner.sourceSnapshot) {
    const xml = await readFile(join(owner.sourceSnapshot, 'src/main/mule/hello.xml'), 'utf8');
    const parser = sax.parser(true, { xmlns: true });
    parser.onopentag = node => { if (node.uri === 'http://www.mulesoft.org/schema/mule/core' && node.local === 'logger') loggerNames.push(Object.values(node.attributes).find(a => a.uri === 'http://www.mulesoft.org/schema/mule/documentation' && a.local === 'name')?.value || 'Logger'); };
    parser.write(xml).close();
  }
} catch (error) { if (final) throw error; }
const releaseNotes = `

## Delivered example and recovery notes

${loggerNames.length ? `The delivered Studio-created application source contains **${loggerNames.length} Loggers**, in this execution order: ${loggerNames.map(name => '**' + name + '**').join(', ')}. The owner walkthrough first creates one Logger; the supplied source snapshot retains the second Logger inserted and reordered during the manipulation checks. Both are real Studio edits. The snapshot uses path **/hello**, before the later /hello-v2 redeployment test. It has not been hand-edited to simplify it.` : 'The final release records the exact Studio-created source snapshot and Logger order.'}

After confirming **You trust this folder** in VS Code 1.144.0, use **Close Modal Editor (Escape)** (the close button) to return from the Workspace Trust modal to Studio.

If an external change conflicts with an XML draft, choose **Copy retained draft** first. **Use current document** discards the retained draft. **Apply retained draft** deliberately replaces the entire current XML document with that draft; review before using it. For property recovery, **Copy retained edits** preserves the attempted values before **Review current document** clears the retained copy. If a configuration or flow dialog becomes stale, copy any wanted values before closing it and reopening against the current document.

The frozen VSIX contains the complete numbered testing procedure. These release notes and the actual screenshots accompany the final evidence and source delivery. They do not change the extension's executable code.
`;
const completeMarkdown = await readFile(join(root, 'TESTING_STEP_BY_STEP.md'), 'utf8') + releaseNotes;
if (final) await writeFile(join(output, 'TESTING_STEP_BY_STEP.md'), completeMarkdown);
let guide = marked.parse(completeMarkdown, { gfm: true });
guide = guide.replace(/<h([12])>([\s\S]*?)<\/h\1>/g, (_match, level, content) => {
  const title = content.replace(/<[^>]+>/g, '');
  const id = 'step-' + title.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
  if (level === '2') headings.push({ title, id });
  return `<h${level} id="${id}">${content}</h${level}>`;
});
const captions = {
  '01-project-created-empty.png': 'New project created through the installed native wizard. The integration folder starts empty.',
  '02-empty-integration-xml.png': 'Explicit New Integration XML opens one real document; no hello flow is silently generated.',
  'owner-project-after-desktop-restart.png': 'The same owner-created application and project-specific selections after a complete VS Code exit/restart.',
  '03-http-configuration.png': 'Actual shared HTTP configuration dialog, with loopback host and the selected port.',
  '04-listener-response-properties.png': 'The Listener supplies the HTTP 200 response body and text headers.',
  '05-listener-logger-workbench.png': 'The Studio-created Listener and Logger in the approved workbench.',
  '06-synchronized-xml.png': 'Editable Configuration XML is a view of the same VS Code text document.',
  '07-build-success.png': 'Actual selected-project Maven build output, including BUILD SUCCESS.',
  '08-application-deployed.png': 'Deployment state is distinct from independent endpoint verification.',
  '09-matching-real-logger-output.png': 'Actual application logs after an independent request with a fresh unique identifier.',
  '10-explicit-redeploy-logs.png': 'After explicit /hello-v2 redeployment, a fresh request appears in actual application logs.',
  '11-stopped-runtime.png': 'Owned application undeployed and project Runtime stopped.',
  'listener-dark.png': 'The real Listener editor in the optional dark theme.',
  'listener-hc-dark.png': 'The real Listener editor using Dark High Contrast host tokens.',
  'logger-hc-light.png': 'The real Logger editor using Light High Contrast host tokens.',
  'keyboard-modal-focus.png': 'Visible keyboard focus within the bounded configuration dialog.',
};
const gallery = [], screenshotManifest = [];
for (const { part, result, path } of runs) {
  for (const capture of result.screenshots) {
    if (!captions[capture.name]) continue;
    const bytes = await readFile(capture.path);
    const sha256 = createHash('sha256').update(bytes).digest('hex');
    screenshotManifest.push({ name: capture.name, sha256, evidence: path, source: 'Actual installed VS Code desktop; whole native window, no pixel edits', vsixSha256: result.vsix.sha256 });
    gallery.push(`<figure><figcaption><strong>${escape(capture.name)}</strong><br>${escape(captions[capture.name])}</figcaption><img src="data:image/png;base64,${bytes.toString('base64')}" alt="${escape(captions[capture.name])}" loading="lazy"><p class="provenance">${escape(part)} acceptance · ${escape(result.subject)} · whole native window · SHA-256 ${sha256}</p></figure>`);
  }
}
const rows = runs.map(({ part, result, path }) => `<tr><th>${escape(part)}</th><td>${result.counts.pass} PASS / ${result.counts.fail} FAIL</td><td><code>${escape(path.replace(root + '/', ''))}</code></td></tr>`).join('');
const logo = (await readFile(join(root, 'assets/zernova-mark.svg'))).toString('base64');
const status = final ? 'Release owner guide · final installed evidence' : 'Draft owner guide · calibration captures; final release evidence pending';
const html = `<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta http-equiv="Content-Security-Policy" content="default-src 'none'; img-src data:; style-src 'unsafe-inline'; base-uri 'none'"><title>ZERNOVA Studio ${escape(manifest.version)} — Test it yourself</title><style>
:root{color-scheme:light;font-family:system-ui,-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif;color:#17343d;background:#f2f6f6;font-size:16px;line-height:1.65}*{box-sizing:border-box}body{margin:0;font:inherit;color:#17343d;background:#f2f6f6}header{background:#fff;border-top:6px solid #007b73;border-bottom:1px solid #cbdadb;padding:32px max(24px,calc((100vw - 1400px)/2)) 26px}.brand{display:flex;align-items:center;gap:14px;font-size:18px;font-weight:750;letter-spacing:1.6px;color:#006d66}.brand img{width:36px;height:36px}.status{font-size:14px;color:#42636c;margin:14px 0 0}.layout{max-width:1448px;margin:auto;display:grid;grid-template-columns:260px minmax(0,1fr);gap:36px;padding:32px 24px 80px}aside{position:sticky;top:20px;align-self:start;max-height:calc(100vh - 40px);overflow:auto;font-size:13px}aside a{display:block;padding:7px 10px;text-decoration:none;border-left:2px solid #ccdedf}aside a:hover,aside a:focus{background:#dbeceb;border-left-color:#007b73}main{min-width:0;background:white;border:1px solid #d4e0e3;border-radius:6px;padding:28px 36px 48px}h1{font-size:38px;line-height:1.15;margin:0 0 22px;color:#102e36}h2{font-size:25px;line-height:1.35;margin:42px 0 16px;padding-top:14px;border-top:1px solid #d6e2e4;scroll-margin-top:20px}h3{font-size:19px;line-height:1.4;margin-top:28px}a{color:#006c7b;text-underline-offset:3px}a:focus{outline:3px solid #015fcc;outline-offset:3px}p{margin:13px 0}li{margin:7px 0}strong{font-weight:700}code{font:0.88em/1.6 ui-monospace,SFMono-Regular,Consolas,monospace;overflow-wrap:anywhere;background:#eef4f4;border-radius:3px;padding:2px 4px}pre{background:#102b33;color:#eefafa;padding:20px;border-radius:5px;overflow:auto;margin:20px 0;font-size:14px}pre code{background:none;padding:0;white-space:pre;overflow-wrap:normal}table{border-collapse:collapse;width:100%;font-size:14px;margin:20px 0;display:block;overflow:auto}th,td{text-align:left;vertical-align:top;border:1px solid #d0dfe2;padding:10px 12px}th{background:#e7f0f1}blockquote{background:#f1f7f6;border-left:4px solid #007b73;padding:8px 18px;margin:20px 0}figure{margin:30px 0;padding:18px;border:1px solid #cadde0;border-radius:6px;background:#f8fbfb;break-inside:avoid}figure img{display:block;width:100%;height:auto;border:1px solid #c0d3d7;margin:15px 0 8px}figcaption{font-size:15px}.provenance{font:11px/1.6 ui-monospace,monospace;color:#49656d;overflow-wrap:anywhere}.evidence-note{background:#e9f5f1;border:1px solid #bbd9cd;padding:16px;border-radius:5px}.footer{font-size:13px;color:#4c6770;margin-top:32px}@media(max-width:950px){.layout{grid-template-columns:1fr;padding:20px 14px;gap:20px}aside{position:static;max-height:none;display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:3px}main{padding:22px 20px}h1{font-size:30px}}@media(max-width:520px){aside{grid-template-columns:1fr}main{padding:18px 14px}table{font-size:13px}}@media print{body,main{background:white}header{padding:16px 0}.layout{display:block;padding:0}aside{display:none}main{border:0;padding:0}pre{white-space:pre-wrap;overflow:visible}figure{page-break-inside:avoid}a{color:inherit}}
</style></head><body><header><div class="brand"><img src="data:image/svg+xml;base64,${logo}" alt="ZERNOVA">ZERNOVA STUDIO · ${escape(manifest.version)}</div><p class="status">${escape(status)}</p></header><div class="layout"><aside aria-label="Guide contents">${headings.map(h => `<a href="#${h.id}">${escape(h.title)}</a>`).join('')}<a href="#actual-screenshots">Actual screenshots</a><a href="#evidence">Evidence and release scope</a></aside><main>${guide}<h2 id="actual-screenshots">Actual installed-editor screenshots</h2><p>Every image below is an uncropped capture of the installed VS Code desktop. The concept image is not used as implementation evidence. Screenshots preserve the whole native window; screenshot names and SHA-256 values link them to the machine-readable run.</p>${gallery.join('')}<h2 id="evidence">Evidence and release scope</h2><div class="evidence-note"><strong>${escape(status)}</strong><p>Tested arrangement: VS Code 1.144.0, Ubuntu 24.04.3 LTS, Linux x86-64. VS Code, extension host, application files, Maven, Runtime, and the test request run on that same Linux host. Native Mac, Windows, actual Mac-to-Linux Remote-SSH, and manual screen-reader testing are NOT RUN.</p></div><table><thead><tr><th>Workflow</th><th>Observed result</th><th>Machine-readable record</th></tr></thead><tbody>${rows}</tbody></table><p>Model, protocol, native-document, negative-runtime, preview regression, source rebuilding, and cleanup reports are delivered separately in the evidence archive. Calibration failures are retained and distinguished from final release results.</p><p>Functional HTTP Listener → Logger testing does not resolve the existing independent Kernel source-build or customer redistribution gates. No runtime/connector JAR or executable application bundle is part of this delivery.</p><p class="footer">Generated from the complete TESTING_STEP_BY_STEP.md. Images are embedded for offline reading. No analytics, external scripts, or external image requests.</p></main></div></body></html>`;
const filename = final ? 'TESTING_STEP_BY_STEP.html' : 'TESTING_STEP_BY_STEP_DRAFT.html';
await writeFile(join(output, filename), html);
await writeFile(join(output, final ? 'GUIDE_SCREENSHOT_PROVENANCE.json' : 'GUIDE_SCREENSHOT_PROVENANCE_DRAFT.json'), JSON.stringify({ final, generatedAt: new Date().toISOString(), guide: filename, images: screenshotManifest }, null, 2));
console.log(JSON.stringify({ path: join(output, filename), bytes: Buffer.byteLength(html), screenshots: screenshotManifest.length, final }));
