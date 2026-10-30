# ZERNOVA brand token and asset audit

Retrieved **29 October 2026** from the owner's specified public branding reference, [https://zernova.et/](https://zernova.et/). Source bytes and hashes are retained in `evidence/reference/brand-retrieval.json` and the corresponding local evidence files.

## Verified website sources

| Source | Saved evidence | Observed content |
| --- | --- | --- |
| `https://zernova.et/` | `evidence/reference/zernova-home.html` | Header uses an inline angular Z SVG and the text ZERNOVA |
| `https://zernova.et/_astro/SiteLayout.9JU6cvW7.css` | `evidence/reference/zernova-site.css` | Explicit dark and light CSS custom properties |
| `https://zernova.et/favicon.svg` | `assets/zernova-favicon.svg` | Existing lime-square/angular-Z favicon, downloaded without modification |
| Owner's six-panel concept | `evidence/reference/supplied-six-panel-concept.png` | Teal Studio layout direction; illustrative mockup, not implementation evidence |

The website currently uses **green/lime**, while the supplied Studio concept uses **teal**. The Studio's teal choices are deliberately provisional design tokens following the owner's concept. They are not falsely attributed to the website stylesheet. The website's older product copy does not change the new XML/Maven/DataWeave scope.

## Exact website color tokens

These values were extracted from the CSS, not sampled from a screenshot.

| CSS property | `:root` (dark) | `:root[data-theme=light]` |
| --- | --- | --- |
| `--bg` | `#101713` | `#f7f8f1` |
| `--surface` | `#18211b` | `#edf0e5` |
| `--surface-2` | `#202c23` | `#e3e9da` |
| `--ink` | `#f2f3e9` | `#18251c` |
| `--muted` | `#b1bcb2` | `#536355` |
| `--line` | `#344137` | `#cbd3c2` |
| `--accent` | `#c7f272` | `#41651d` |
| `--accent-ink` | `#14200c` | `#fff` |
| `--paper` | `#f0f1e8` | `#e9eddf` |
| `--paper-ink` | `#19251d` | `#18251c` |
| `--paper-muted` | `#4c5f50` | `#475b4a` |

Other verified values: `--radius: 8px`; `--spacing: .25rem`; the sans stack is `-apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif`; the mono stack is `ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace`. No remote font download is required.

## Existing logo assets

The header logo's original viewBox is `0 0 40 40`. Its unchanged path is:

```text
M5 6h30v8L16 26h19v8H5v-8l19-12H5z
```

`assets/zernova-mark.svg` retains this geometry and `currentColor`. The standalone file adds the SVG XML namespace and removes fixed HTML display dimensions; it does not invent or redraw the mark. `assets/zernova-logo.svg` is a byte-identical alias for UI use. Native VS Code can tint the monochrome mark using the active host theme.

`assets/zernova-favicon.svg` is byte-identical to the public website file. `assets/zernova-icon.png` is a 128×128 raster rendering of that SVG using Inkscape. The favicon's fill values remain `#c7f272` and `#101713`. No AI-generated logo or MuleSoft artwork is included.

These are existing ZERNOVA brand assets reused under the owner's explicit instruction to use the supplied ZERNOVA branding reference. No public open-source license for the brand artwork was identified or invented. The project does not grant ownership of the mark to third parties. See `assets/NOTICE.md` for provenance.

## Studio semantic design tokens

The implementation centralizes its semantic tokens in the webview stylesheet and optional native theme JSON. They cover brand/accent, focus, selection, hover, pressed state, editor/canvas, palette, Properties, metadata, dialog/input/code surfaces, text hierarchy, borders, disabled states, component colors, paths, drop targets, typography, control density, radii, and icon sizes.

The selected webview direction uses provisional teal `#007b73` in light mode and `#4ddbc9` in dark mode, with dark editor surface `#102127`. These were engineering/design choices from the supplied concept and contrast needs. They were verified against `src/webview/styles.css`; the style files are authoritative for the full implemented values and any additional semantic distinctions.

| Implemented semantic token | Light | Dark |
| --- | --- | --- |
| `--z-brand` | `#007b73` | `#4ddbc9` |
| `--z-accent` | `#006f91` | `#72cbef` |
| `--z-focus` | `#007d89` | `#5ce4d5` |
| `--z-editor` | `#ffffff` | `#102127` |
| `--z-canvas` | `#fcfefe` | `#11262d` |
| `--z-palette` | `#f6f8f8` | `#13292f` |
| `--z-properties` | `#ffffff` | `#13272d` |
| `--z-dialog` | `#ffffff` | `#182e35` |
| `--z-text` | `#1c2e37` | `#e3ecee` |
| `--z-muted` | `#536770` | `#b0c3c9` |
| `--z-faint` | `#596e78` | `#99b2ba` |
| `--z-border` | `#c9d6da` | `#39525b` |
| `--z-http` | `#3262d9` | `#5e9dff` |
| `--z-log` | `#008b7e` | `#3ac9af` |
| `--z-node-ink` | `#ffffff` | `#0c262d` |

HTTP components use a separate blue family; Logger uses a green/teal component family; status is also conveyed by text and symbols. The compact spacing scale is 4/8/12/16 px, nominal control height 30 px, corner radius 3 px, and ordinary icon size 16 px. These are Studio choices, not website token claims. Typography follows the VS Code host font variables with local system-font fallbacks.

Native themes are offered as explicit choices. The extension does not change the user's global color theme or settings. The webview can follow the host theme or preview a light/dark presentation independently. High-contrast presentation uses host contrast tokens, visible outlines, readable text, and non-color labels.

The initial automated contrast calibration identified light-theme faint-text contrast failures. `--z-faint` was darkened to `#596e78`, and the dark component glyph token is `#0c262d`. The final installed-host run covered all six views in light/dark and both native high-contrast themes: 24 axe scans reported zero violations. Eight states retain incomplete/manual-review contrast items for partially obscured/clipped code or form content, so this does not claim complete accessibility acceptance. The earlier lane ARIA-role finding was corrected before the final passing run. See `docs/UI_SKELETON_SCOPE_AND_GAPS.md` and the final `evidence/latest-ui.json` pointer.

## Asset and reference restrictions

All assets used by the installed preview are bundled locally. No CDN fonts, remote scripts, remote SVG, runtime documentation fetch, or customer service contact is required to open it. Website HTML/CSS and the supplied concept are reference evidence; they are not executable dependencies. Third-party Studio pages were consulted for layout/behavior only. Their proprietary implementation, icons, screenshots, and product artwork are not repackaged as ZERNOVA UI resources.
