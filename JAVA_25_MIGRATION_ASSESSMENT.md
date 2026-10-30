# Java 25 migration assessment

Java 25 is a separate runtime compatibility project with substantial engineering
effort and high release risk in the current dependency stack. It is not a bundled
JRE path/version change. No Java 25 migration was performed for Studio 0.4.4.

The actual prior Mac x64 probes are recorded in
`verification/studio-0.4.1-devtest/java-compatibility.json`:

- Java 25 starts the VM but default-domain deployment fails because an extension
  declares compatibility only with Java 8, 11, 17 and 21.
- Java 21 starts the VM and default domain but HTTP application deployment fails:
  Sockets declares compatibility only with Java 8, 11 and 17.

These are observed release blockers, not proof that changing the declared version
lists would make the libraries compatible. Keep those checks and the patched
Java 17 release while doing the migration on a separate branch.

## Migration gates

1. Inventory engine, SDK, DataWeave, HTTP/Sockets, native/network/TLS libraries,
   bootstrap, agents, JPMS configuration and developer tools. Establish Java 25
   support or source ownership for every required dependency.
2. Upgrade or rebuild the required libraries and fix removed-API, reflection,
   module, native-access and bytecode-tooling issues. Rebuild the full engine.
3. Pass default-domain and application deployment, HTTP/logging, transformations,
   errors, breakpoints/step/disconnect, replacement, stop and crash recovery.
4. Qualify Intel/x64 and ARM64 on Mac, Windows and Linux, including installed
   Studio packages and independent runtime operation.
5. Compare throughput, p95/p99 latency, startup, memory and long-running behavior
   against the Java 17 baseline before selecting the new default and rollback.

The work should be planned in weeks rather than treated as an hours-long swap;
a reliable schedule needs the dependency audit first. Java 25 alone does not
establish a performance improvement or eliminate third-party security exposure.

Primary migration reference: Oracle's
[Java 25 migration guide](https://docs.oracle.com/en/java/javase/25/migrate/getting-started.html).
The guide calls for updating third-party libraries, compiling/testing and reviewing
removed APIs/features and behavioral changes.
