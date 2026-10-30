# Dependency versions, licenses and packaging provenance

Recorded **29 October 2026** against the exact `package.json`, `package-lock.json`, and installed package metadata/license files in this new project. This is an engineering provenance inventory, not a blanket legal clearance or a conclusion about the separate Kernel/connector redistribution gates.

## Small implementation stack

The extension uses TypeScript, the supported VS Code extension API, and a locally bundled vanilla TypeScript/CSS webview. There is no production `dependencies` entry and no React, graph framework, runtime SDK or third-party integration engine. The host supplies `vscode`; it is external to the esbuild host bundle. The installed webview is bundled for offline use.

Build and test dependencies are exact version pins. `package-lock.json` retains resolved package integrity values. The build uses esbuild's `legalComments: 'eof'` so retained legal comments are not silently removed from generated bundles.

## Direct development dependencies

| Package | Exact version | Declared license | Role | Publisher primary source |
| --- | --- | --- | --- | --- |
| `@types/node` | `24.10.1` | MIT | Node typings for build/test/host compilation | [DefinitelyTyped](https://github.com/DefinitelyTyped/DefinitelyTyped/blob/master/LICENSE) |
| `@types/vscode` | `1.96.0` | MIT | VS Code extension API typings | [DefinitelyTyped](https://github.com/DefinitelyTyped/DefinitelyTyped/blob/master/LICENSE) |
| `@vscode/test-electron` | `3.1.0` | MIT | Isolated actual extension-host testing | [Microsoft vscode-test](https://github.com/microsoft/vscode-test/blob/main/LICENSE) |
| `@vscode/vsce` | `4.1.0` | MIT | Local VSIX packaging | [Microsoft vscode-vsce](https://github.com/microsoft/vscode-vsce/blob/main/LICENSE) |
| `axe-core` | `4.11.0` | MPL-2.0 | Development accessibility checks | [Deque axe-core](https://github.com/dequelabs/axe-core/blob/develop/LICENSE) |
| `esbuild` | `0.28.2` | MIT | TypeScript bundle generation | [Evan Wallace esbuild](https://github.com/evanw/esbuild/blob/main/LICENSE.md) |
| `playwright` | `1.64.1` | Apache-2.0 | Development UI automation | [Microsoft Playwright](https://github.com/microsoft/playwright/blob/main/LICENSE) |
| `typescript` | `5.9.3` | Apache-2.0 | Type checking | [Microsoft TypeScript](https://github.com/microsoft/TypeScript/blob/main/LICENSE.txt) |

Installed license texts were read and copied without modification to `evidence/reference/licenses/`, including axe-core's third-party license file and Playwright's NOTICE. Exact file paths and SHA-256 values appear in `evidence/reference/dependency-license-inventory.json`. The local pinned package content, rather than a moving default branch alone, grounds the version-specific declarations above.

MPL-2.0 and Apache-2.0 packages are explicitly distinguished from MIT packages. They are test/build tooling, not rebranded ZERNOVA-owned code. No third-party package was modified as part of the UI implementation.

## Transitive inventory and delivery boundary

The snapshot records **203** non-root lockfile package entries, including optional platform packages. It preserves each entry's declared license, version, integrity, development/optional flag, and locally installed metadata when available. Some platform-specific entries are naturally absent from this host. Declared labels include MIT, ISC, Apache-2.0, MPL-2.0, BSD variants, Artistic-2.0, BlueOak-1.0.0, compound expressions and `SEE LICENSE IN LICENSE.txt`; no single license is inferred for the whole toolchain.

The VSIX packaging excludes `node_modules`, source test scripts, browser/VS Code downloads, evidence, reference artwork and caches, and uses `vsce package --no-dependencies`. Final archive inspection must confirm the actual payload; a manifest's intent alone is not evidence of package contents. The source delivery retains the lockfile and provenance but should not embed downloaded test hosts or dependency trees.

VS Code, Electron and browser binaries used to run tests have their own distribution terms. They are testing infrastructure and are not included as a ZERNOVA product runtime. Installing a development VSIX is not a marketplace publication.

## Original code and brand assets

The new extension's original implementation is governed by the project's `LICENSE.txt`; this document does not grant a different license. ZERNOVA mark provenance is in `assets/NOTICE.md`. The website's brand artwork was reused under the owner's requested branding workflow; no public permissive trademark/artwork license was invented.

Official Anypoint documentation was consulted for behavior and layout. No proprietary Studio implementation, vendor artwork, Mule runtime, Java, Maven, connector binaries or Enterprise tooling is bundled in this UI skeleton. Genuine Mule names in the illustrative XML remain compatibility identifiers, not a transfer of ownership or an assertion of affiliation.

The supplied runtime source-build and customer-redistribution blockers remain separate and unchanged. Future runtime or connector bundling needs its own resolved provenance/notice review; this dependency inventory does not clear it.
