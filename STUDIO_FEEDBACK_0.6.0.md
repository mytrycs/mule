# ZERNOVA Studio 0.6.0 — shared canvas and project workbench

This iteration addresses the five owner feedback items: all flows/subflows in one
configuration are visible together; new XML is created through an inline draggable
popup; project/resource context actions are expanded; declared and bundled artifacts
are visible; scopes begin empty and have distinct original icons.

The included all-core sample is one application with 21 flows/subflows and 116
stable component IDs. Its acceptance test asserts coverage against the live core
catalog, so missing catalog components fail the test. Functional checks include
real retry after a resource failure, Cache reuse/LRU/TTL, routing, error handling,
external transform targets, Scheduler and asynchronous Batch completion.

Core/runtime versions are unchanged (0.5.0 support / 0.4.1-alpha.1 engine / Java 17).
This is an editor/explorer update; Maven declarations beyond the bundled profile
remain unavailable for offline execution. Details are in the packaged test guide
and known limitations. Qualification is local Mac x64.
