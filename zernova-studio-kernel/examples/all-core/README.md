# All-core ZERNOVA app

Open this folder as a ZERNOVA project and Run. Default HTTP port: 18081.
All flows/subflows live in one XML configuration. The default retry resource exists;
the native acceptance test overrides its URL to exercise a real transient failure.
Batch is asynchronous, single-instance and at least once. Cache is application-local.

The Scheduler writes a log every second. /propagated intentionally returns HTTP 418.
/batch accepts five records, rejects record 3, aggregates successes in groups of two,
processes failed records in another step and emits a completion report.

Run the repository all-core acceptance test for response, router, retry, cache
hit/eviction/TTL, async, error, external transform and Batch assertions.
