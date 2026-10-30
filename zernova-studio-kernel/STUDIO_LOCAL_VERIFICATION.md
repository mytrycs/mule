# Studio 0.3.0 with ZERNOVA Runtime — local verification

**Date:** October 30, 2026. **Host:** macOS x64.
**Editor tested:** VS Code 1.145.0-insider, actual installed VSIX.
**Runtime:** ZERNOVA 0.3.0-dev.2, compiled from the renamed 4.10 source baseline.

## Result

Passed: **59 unit tests**, TypeScript checking, **nine installed-extension
integration checks**, and direct interaction with the visible Studio interface.
The final package targets `darwin-x64` and contains the runtime, Java 17, HTTP
dependencies, integrity manifest, source archive and license notices.

The original 0.2.0 host required a Linux-only external runtime and separate Java
and Maven settings. Studio now calls the tested portable backend directly,
uses its installed bundle, and keeps each project's live instance in private
extension storage outside the application source. The approved visual interface
and XML editing implementation remain in place.

## Installed-extension checks

1. Installed VSIX activates and opens the actual custom integration editor.
2. Bundled Java/runtime diagnostics pass with deliberately invalid external
   `JAVA_HOME`, `ZERNOVA_JAVA_HOME` and `ZERNOVA_MAVEN` paths.
3. Studio Build produces a fresh offline `.zapp` artifact.
4. Studio Run deploys to the renamed engine; HTTP returns **200** and
   **Hello from ZERNOVA**, with the request's fresh correlation ID in logs.
5. Editing XML, saving and redeploying changes the route; the old route returns
   404 and the new one returns 200.
6. Stop Application undeploys the application while retaining the owned JVM.
7. Stop Runtime shuts down that JVM.
8. Invalid XML is diagnosed and cannot deploy an old artifact.
9. Restoring valid XML permits another complete run/request/log/stop cycle.

These checks execute the installed extension's public commands in the real
VS Code extension host. The Studio runtime and HTTP server are real; no runtime
or editor API mocks substitute for them.

## Visible UI checks

The Listener's Path was edited in Properties, saved, and confirmed in the
Configuration XML view. Clicking **Run / Redeploy** in Studio deployed the edited
`/studio-ui` route. A real request returned 200 with **Hello from ZERNOVA**.
Clicking **Refresh logs** exposed the fresh request correlation in Studio's
application log state. **Stop runtime** then stopped the owned engine.

The complete **New ZERNOVA Project** wizard also created a separate project
using **Use bundled runtime · offline**, with `com.zernova` coordinates and
`zernova-app.json`. It did not request external runtime, Java or Maven paths.

All test runtimes were stopped. The owner's existing editor profile, extensions
and projects were not replaced. Testing used an isolated profile and generated
projects beneath `~/.zernova-studio-tests/`.

## Reproduce

After preparing the matching runtime in this extension's `runtime/` directory:

```sh
npm ci
npm run typecheck
npm test
npm run package
node scripts/test-bundled-host.mjs
```

The last command installs the VSIX in an isolated profile and launches the real
editor. Trust the generated test workspace to allow execution. It writes
`ready.json`, `results.json`, and `complete.json` under the printed test directory.
`ZERNOVA_VSCODE_CLI` can select a particular editor executable.

## Scope and evidence

This is a **local Mac x64 development test**, not six-platform installed-Studio
release clearance. Stable VS Code 1.144.0 acceptance was not completed and is
not counted in these results. The offline builder supports core XML/resources
and the bundled HTTP profile. Custom Java and additional dependencies are
rejected; Maven plugins are not executed by this builder.

The UI continues to label endpoints unverified until a separate request is
performed; deployment alone is not an HTTP health check. The requests above
were performed and verified separately by the test harness.

Detailed logs and JSON results are in `evidence/`. A compact report is also
retained in the repository's `verification/studio-0.3.0-local/` directory.
The package is `dist/zernova-studio-kernel-preview-0.3.0-darwin-x64.vsix`.
