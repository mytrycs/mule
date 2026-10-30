# Studio 0.3.0 bundled-component notices

This development package includes the original Studio implementation plus the
ZERNOVA source-built runtime, Java 17 and HTTP libraries. Their original licenses
and notices remain in `runtime/engine`, `runtime/java`, `runtime/connectors` and
`runtime/notices`; corresponding runtime source is in `runtime/sources`.
The Studio development license does not replace any component's existing license.

The host bundles SAX and JSZip with its transitive JavaScript dependencies.
`licenses/npm/INVENTORY.json` records the actual bundled package versions, and
that directory preserves their license texts. JSZip's MIT licensing option is
used. VS Code itself is supplied by the installed editor and is not inside this
VSIX. Maven is not included or invoked by the offline XML/resource builder.

## Preserved notices from the earlier UI-only preview

The following scope statements describe the earlier preview before runtime
integration; the Studio 0.3.0 package scope is described above.

# Third-party and brand notices

This development VSIX contains the original ZERNOVA UI implementation, locally
bundled ZERNOVA brand assets, generated build helpers and documentation. It does
not include Mule Kernel, DataWeave, Java, Maven, connector binaries, Anypoint
Studio implementation code or Enterprise tooling. There are no production npm
dependencies; the installed extension consumes the VS Code API supplied by its
host.

## ZERNOVA brand assets

The mark and favicon originate from the owner's requested public branding
reference, `https://zernova.et/`. Exact source URLs, retrieval details and asset
derivation are recorded in `assets/NOTICE.md` and
`docs/BRAND_TOKEN_AUDIT.md`. No permissive trademark/artwork license is inferred
from public availability. The branding request authorizes this development
preview; artwork ownership is not assigned to another party.

## Development dependencies

The source project pins these development-only packages:

| Package | Version | License |
| --- | --- | --- |
| @types/node | 24.10.1 | MIT |
| @types/vscode | 1.96.0 | MIT |
| @vscode/test-electron | 3.1.0 | MIT |
| @vscode/vsce | 4.1.0 | MIT |
| axe-core | 4.11.0 | MPL-2.0 |
| esbuild | 0.28.2 | MIT |
| playwright | 1.64.1 | Apache-2.0 |
| typescript | 5.9.3 | Apache-2.0 |

Their package trees and executable tooling are excluded from the VSIX. The
source delivery preserves exact license/NOTICE copies in
`evidence/reference/licenses/`, the lockfile's transitive version/integrity
inventory, and `docs/DEPENDENCY_LICENSES.md`. That document identifies primary
publisher sources and distinguishes the toolchain's licenses. No development
dependency is represented as ZERNOVA-owned code.

VS Code, Electron and the display/browser utilities used for testing are
separate test infrastructure with their own terms. They are not included in
the ZERNOVA Studio source or VSIX deliverables. Genuine Mule identifiers in
illustrative XML retain their compatibility meaning and do not imply ownership
or affiliation.

## esbuild generated helpers

The following exact MIT license from the pinned esbuild package is preserved
for any generated helper code emitted into the bundled extension:

```text
MIT License

Copyright (c) 2020 Evan Wallace

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

These UI notices do not resolve the separate foundation's source-build or
customer runtime redistribution gates. Future inclusion of runtime or connector
artifacts requires its own completed provenance and notices.


## sax 1.6.1 — production XML parser

Source: https://github.com/isaacs/sax-js
License: BlueOak-1.0.0. The parser is bundled into the extension host; upstream license follows unchanged.

```text
# Blue Oak Model License

Version 1.0.0

## Purpose

This license gives everyone as much permission to work with
this software as possible, while protecting contributors
from liability.

## Acceptance

In order to receive this license, you must agree to its
rules.  The rules of this license are both obligations
under that agreement and conditions to your license.
You must not do anything with this software that triggers
a rule that you cannot or will not follow.

## Copyright

Each contributor licenses you to do everything with this
software that would otherwise infringe that contributor's
copyright in it.

## Notices

You must ensure that everyone who gets a copy of
any part of this software from you, with or without
changes, also gets the text of this license or a link to
<https://blueoakcouncil.org/license/1.0.0>.

## Excuse

If anyone notifies you in writing that you have not
complied with [Notices](#notices), you can keep your
license by taking all practical steps to comply within 30
days after the notice.  If you do not do so, your license
ends immediately.

## Patent

Each contributor licenses you to do everything with this
software that would otherwise infringe any patent claims
they can license or become able to license.

## Reliability

No contributor can revoke this license.

## No Liability

***As far as the law allows, this software comes as is,
without any warranty or condition, and no contributor
will be liable to anyone for any damages related to this
software or this license, under any kind of legal claim.***
```
