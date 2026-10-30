# Known limitations — Studio 0.5.0

Studio 0.5.0 adds authoring for every core component and transformer category in the
referenced Mule runtime 4.13 core-components list. Runtime compatibility is based
on the source-built ZERNOVA 0.4.1-alpha.1 engine and Java 17. The original managed
ZERNOVA support module is now 0.5.0. This is a local development release.

- Native engine components include Logger, Scheduler, Flow/Subflow, Flow Reference,
  Parse Template, Choice, First Successful, Round Robin, Scatter-Gather, Async,
  For Each, Try, Until Successful, error handlers, Set Payload, Set Variable,
  Remove Variable and Raise Error. Nested scopes/routes can be collapsed and edited.
- ZERNOVA provides original Transform Message, Dynamic Evaluate, Custom Business
  Event, Cache and Batch components in `https://schemas.zernova.dev/transform`.
  These require the support module, including when running independently of Studio.
  A bare older runtime does not implement these elements.
- Custom Business Event emits structured application logs; it does not include a
  hosted business-event analytics service. Dynamic Evaluate sets the payload.
- Cache is application-local, in-memory and message-only, with TTL, LRU eviction,
  at most 64 MiB retained serialized data and at most 8 MiB per message. It does
  not cache variables or errors, coalesce simultaneous misses or provide clustering.
  Unserializable messages bypass storage. Keys are bounded to 1 MiB of JSON text.
- Batch is original, single-instance, sequential and asynchronous after submission
  to local disk. Checkpoints resume after a JVM restart using the same packaged
  application definition. At-least-once processing can repeat external side effects;
  use idempotent writes. There is no distributed scheduler or exactly-once guarantee.
  Input must be a collection whose records/variables can be serialized. One final
  Aggregator per Step is supported; On Complete receives a report, not all records.
  `blockSize` controls progress reporting, not worker concurrency. Pending jobs with
  a changed application definition are quarantined. Failed completion callbacks
  mark the job failed and are not automatically retried. Completed/failed job files
  are retained under private runtime `data/zernova-batch`; disk retention management
  and operational recovery UI are future work. No power-loss durability claim is made.
- Transform outputs evaluate against the same incoming event. Missing outputs are
  preserved. Variables return an object keyed by variable names; existing variables
  remain. External UTF-8 `.dwl` resources under `src/main/resources/dwl` are loaded
  at deployment (maximum 1 MiB each). Inline scripts are limited to 16,000 characters
  in configuration forms. A metadata/schema mapping GUI is not included.
- HTTP success/error status, body and headers belong to the Listener. Local HTTP
  is supported; HTTP Request, HTTPS/TLS and additional connectors are outside this
  palette/build profile. Logger DEBUG/TRACE requires suitable logging thresholds.
- Environment properties use `.properties` files, global defaults and launch
  profiles. `${name}` is property substitution; `p('name')` reads a property in
  DataWeave; `$(...)` interpolates a DataWeave string. Property preflight intentionally
  rejects conflicting file values and unsafe paths. YAML/secure-property modules
  and remote property providers are not included.
- The offline builder packages XML/resources and bundled dependencies. It does not
  compile user Java or execute arbitrary Maven goals. Build/deploy does not validate
  every expression until that expression is actually executed.
- Local component debugging pauses before processors. Listener breakpoints stop
  at flow entry. Embedded controls include Continue, Next component and Disconnect.
  Java source stepping, watches and conditional breakpoints are future work.
  Source edits require redeployment before live breakpoint changes. Stable component
  IDs preserve breakpoint association; old position-based IDs can change on editing.
- Run/Debug profiles expose environment, variables, Java properties, heap, VM options,
  working directory and timeouts. The managed deployment scan interval defaults to
  1000 ms. Replacement deployment restarts the private project JVM for cross-platform
  HTTP reliability; reducing scan delay does not remove JVM startup cost.
- Redeploy on manual save is opt-in; background XML autosaves do not redeploy.
  The deployment console adds ZERNOVA branding/stages and retains raw runtime logs
  separately, including required third-party notices and original failure details.
- XML edits preserve unrelated source. Unsupported constructs can make a visual
  component/flow read-only. XML is limited to 1 MiB; DTD/entity declarations are refused.
- Qualification for this release is local Mac x64. Prior runtime portability tests
  do not establish installed-Studio qualification on six targets. Java remains the
  tested patched Java 17 bundle. Java 25 migration is a separate compatibility project.
