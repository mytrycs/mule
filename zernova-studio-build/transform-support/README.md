# ZERNOVA managed core support 0.5.0

Original Java 17 processor and extension integration for payload, attributes and
named-variable DataWeave outputs, external resources, Dynamic Evaluate, business
event logging, bounded local Cache and durable local Batch. The qualified engine's public APIs and extension
loader are used; engine bytecode is unchanged. The custom-building model property
is reused through its public ModelProperty interface from the core declaration.

Build with a JDK 17+ compiler and a prepared Studio runtime:

```
JAVAC=/path/to/javac python3 zernova-studio-build/transform-support/build.py
npm run build:portable
```

The build emits the ignored support JAR and the tracked generated
`src/portable/transformSupport.ts`. Portable launch installs these exact bytes in
the private instance's lib/mule directory; the known 0.4.4 predecessor is archived before upgrade. Unknown mismatching files
or symlinks are refused, not overwritten. The JAR includes its extension model, DSL and namespace
providers, local schema and schema mapping. It declares Java 17 compatibility.

All outputs evaluate against the input event before a new event is built.
Variables must be a Java object/map of valid variable names; entries merge into
existing variables. Blank targets preserve their input. Native/installed tests
exercise actual event changes and HTTP serialization.

This is a managed runtime support module, not an additional user-supplied connector
or permission to execute arbitrary Java sources from application projects.

External scripts are loaded once at deployment. Batch stores serialized record
checkpoints in the private runtime data directory, resumes matching application
artifacts after restart, and uses at-least-once semantics. Completed/failed spool
files are retained. See Studio KNOWN_LIMITATIONS.md for acceptance policies,
aggregation, callback failures, retention and concurrency restrictions.

Native acceptance scripts are `tests/studio-050-{catalog,resources,batch,batch-recovery}.cjs`
in zernova-studio-build. The performance probe measures lifecycle latency; it is
not an overall engine throughput benchmark.
