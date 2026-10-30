# Local lifecycle latency probe

Mac x64, bundled Java 17 and source-built engine 0.4.1-alpha.1. Same simple HTTP
Listener/Logger/Set Payload app; deployment scan interval was the changed setting.
Other desktop acceptance tests were active, so these small samples are not a
controlled benchmark and do not establish an overall engine throughput gain.

| Measurement | 5000 ms scan | 1000 ms scan |
|---|---:|---:|
| Offline build | 0.370 s | 0.370 s |
| Initial runtime start | 6.44 s | 6.26 s |
| First deployment after runtime ready | 14.90 s | 8.65 s |
| First replacement, including private JVM restart | 34.36 s | 17.79 s |
| Second replacement, including private JVM restart | 20.04 s | 19.40 s |

The launcher now defaults to 1000 ms instead of the engine's legacy 5000 ms scan.
Run/Debug configuration allows 100–60000 ms. This reduces the maximum idle scan
wait by four seconds per scan, while actual deployment includes initialization,
verification and server startup. Replacements still restart the private JVM for
cross-platform HTTP reliability. Do not interpret timings as steady-state RPS,
latency percentiles, GC performance or a comparison against another product.

External DW scripts are read and cached at deployment instead of loading files
per request. The optional bounded Cache scope can reuse completed messages; its
TTL and side effects are application decisions. No blanket request caching was
introduced and no general throughput improvement is claimed.

Next performance work: capture representative HTTP/JSON/large-payload workloads,
measure warm p50/p95/p99, throughput, allocations, CPU and GC; profile the largest
cost; optimize and rerun identical tests. Investigate reliable live replacement
of the HTTP connector before removing the mandatory private-JVM restart. Java 25
is a separate compatibility migration, not a guaranteed performance shortcut.

Raw measurements and reproducible probe are in performance.log and
zernova-studio-build/tests/studio-050-performance.cjs.
