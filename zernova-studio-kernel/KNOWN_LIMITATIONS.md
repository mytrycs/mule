# Known limitations — Studio 0.4.4

Studio supports local ZERNOVA XML projects with HTTP Listener, Logger and Transform
Message. Transform can set payload, attributes and named variables. The base engine
is Runtime 0.4.1-alpha.1; the portable launcher installs the original ZERNOVA
Transform 0.4.4 support module into the private runtime. New `zt:message` XML needs
that support module; an older bare runtime does not provide it.

- Local HTTP listeners bind to localhost/127.0.0.1. HTTP Request, HTTPS/TLS and
  additional connectors are outside the current palette/build profile.
- The offline builder packages supported XML/resources and bundled dependencies.
  It does not compile user Java or execute arbitrary Maven goals.
- Transform outputs evaluate against the same input. A blank script preserves
  that target. Variables must return an application/java object keyed by variable
  names; its entries set/replace those variables and preserve other variables.
  No separate metadata/schema mapping GUI is included.
- Component breakpoints pause before processors execute. A Listener breakpoint
  pauses at flow entry, after HTTP acceptance and before its first processor.
  Next component follows the current execution thread through the supported
  synchronous processors. Debugging is local and intended for development.
- Studio has an embedded debug-event console with Continue, Next component and
  Disconnect. It does not start a VS Code Java debug session. Conditional
  breakpoints, watch-expression evaluation and Java source stepping are future
  work. HTTP clients may time out while processing is intentionally paused.
- Source changes require redeployment before live breakpoint changes. Component
  IDs keep breakpoints associated with visual nodes; legacy position-based IDs
  should be replaced by stable documentation IDs before rearranging such flows.
- Run/Debug profiles expose environment, variables, Java properties, heap, VM
  options, working directory, timeouts and debug port. The JRE/bootstrap/classpaths
  remain managed. Runtime replacement restarts this project's private instance.
- Redeploy on manual save is opt-in; background XML autosaves do not redeploy.
- Supported XML edits preserve unrelated source. Unsupported constructs remain
  in XML and can make the affected visual flow read-only. XML is limited to 1 MiB;
  DTD/entity declarations are not accepted.
- Listener error-response editing and advanced transport settings remain limited.
  Logger DEBUG/TRACE output depends on the application's logging thresholds.
- Studio retains endpoint-unverified status until separately tested with a real
  request. Deployment markers and JVM banners alone are not HTTP health evidence.
- This iteration is qualified on local Mac x64. Earlier runtime portability tests
  do not constitute complete installed-Studio qualification on all six targets.
- Java remains the compatibility-tested patched Java 17 bundle. Java 21/25 probes
  encountered extension/connector compatibility failures; Java 25 is not enabled.
