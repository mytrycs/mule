# ZERNOVA Studio 0.5.0 local qualification

Implementation commit: a4cb85fcb5. Target: Mac x64. Base runtime:
0.4.1-alpha.1, patched Java 17, original managed support module 0.5.0.

- TypeScript check passed; 84 unit tests passed.
- Native runtime suite: 23/23 passed.
- Final installed VSIX in isolated VS Code 1.144.0: 20/20 passed, including real
  HTTP/Logger, autosave/manual-save redeploy, external transforms, environment
  properties, nested core scope and embedded component debugging.
- Core acceptance: 16 behavior checks, including native routers/scopes, Scheduler,
  business-event contents, Dynamic Evaluate and ordinary/JSON stream cache hits.
- Resource acceptance: all three external DW outputs, property precedence,
  Unicode/continuations/interpolation and actual configured HTTP 503 error response.
- Batch acceptance: record/group/completion execution, failed-record isolation,
  ONLY_FAILURES, empty input and a pending 10,000-record job resumed after a JVM
  restart. At-least-once single-instance behavior; see known limitations.
- Known 0.4.4 support module archived/upgraded; unknown bytes refused unchanged.
- Manual installed UI: nested placement/collapse/delete, optional outputs,
  draggable external script wizard, file creation/linking, HTTP error-response
  fields and QA environment wizard followed by actual HTTP response.
- Default owner VS Code updated and visibly active. Existing owner app returned
  HTTP 200 on its configured port 8081 with matching test-ID application log.
  Eight source/config files remained unchanged. Runtime left stopped.
- An obsolete, separately authenticated Studio-owned 0.4.0 runtime for the same
  project was gracefully stopped, releasing port 18081 for the supplied sample.

The first installed-harness attempt used response.json() with a helper that only
exposes text(); the harness was corrected to JSON.parse(await response.text()).
The final run used the final VSIX and completed all 20 checks successfully.

VSIX identity is in vsix.json. Raw check results and local timings are alongside
this file. PERFORMANCE.md explains the limits of the lifecycle timing probe.
No new six-target installed-Studio or overall engine throughput claim is made.
