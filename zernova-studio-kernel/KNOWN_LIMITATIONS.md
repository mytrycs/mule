# Known limitations — Studio 0.2.0

## Supported milestone

Real editing and execution are bounded to HTTP Listener and core Logger on the
existing Linux x86-64 ZERNOVA Runtime Foundation. The implementation keeps the
approved interface and separate design preview. It is an evaluation milestone,
not a complete Anypoint Studio replacement.

- The success response is configured on the Listener. Basic error-response
  editing is not implemented; existing error-response XML is preserved.
- Only HTTP is editable. HTTPS/TLS, authentication, advanced transport options,
  HTTP Request, and other connectors are disabled or outside the current editor.
- Supported flows contain a Listener and zero or more Loggers while being edited;
  runnable applications require complete valid sources/configuration. Multiple
  supported flows in one XML can be represented. Unsupported flow constructs
  remain intact and make affected visual editing read-only.
- Global configuration selection is scoped to the current XML. Supported renames
  update local references. Referenced or uncertain cross-file configurations
  cannot be renamed/deleted blindly; Studio refuses unsafe changes.
- XML editing is limited to 1 MiB. DTD declarations are not accepted. Runtime
  expression syntax is ultimately validated by the actual runtime, not evaluated
  inside the webview.
- TRACE/DEBUG Logger visibility also depends on the application's logging
  thresholds. Generated projects use INFO root logging; selecting DEBUG does
  not lower that threshold automatically.
- Build/run require a saved, trusted, configured project. Runtime configuration
  is stored per project on the extension host, not committed as machine paths in
  application source. Reopening source on another host requires new selections.
- Runtime state is reconciled from the CLI after reload. A deployed application
  remains marked **endpoint unverified** in Studio. Independent curl responses
  and matching application logs verify behavior externally; they do not
  automatically change this UI status. A running JVM or deployment marker
  alone is not endpoint-health evidence.
- Only a private, owned instance is stopped. No shared-instance manager,
  debugger, automatic hot deployment, API platform, or multi-environment manager
  is implemented. Build/run/redeploy are explicit actions.
- Offline use requires legitimate prior preparation of the Runtime and Maven
  cache. Missing dependencies fail with retained output.

## Platform and test limits

The verified route places VS Code, the extension host, project, Runtime, and
request on one Linux x86-64 machine. Native macOS/Apple Silicon, Windows, and an
actual Mac-to-Linux Remote-SSH session are **NOT RUN**. Runtime execution is
explicitly blocked on unsupported extension hosts. Manual screen-reader testing
and comprehensive accessibility certification have not been performed.

Test-only displays, profiles, fixtures, and HTTP identifiers are disposable.
Their absolute paths in evidence describe the verification machine, not required
installation locations for the owner.

## Future work awaiting separate approval

HTTP Request and additional connectors; broader import/migration; transformations
and visual DataWeave mapping; metadata services; debugging/MUnit; enterprise
accounts/cloud control planes; authentication/TLS; commercial licensing and
distribution. This milestone stops before those features.
