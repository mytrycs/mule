# ZERNOVA Studio 0.4.4 development test

This Mac x64 VSIX includes Runtime 0.4.1-alpha.1, compatible Java 17 and HTTP
libraries. The portable launcher adds ZERNOVA Transform support 0.4.4 without
replacing engine bytecode. Install the supplied VSIX and reload/restart VS Code.

## Transform and debugging sample

Open `examples/transform-debug-devtest`, then `src/main/zernova/hello.xml` in Studio.
Run and call `http://localhost:18083/transform?testId=OWNER-044`.
Expect JSON containing payload.message `Hello from ZERNOVA`, attributes.correlation
`OWNER-044`, and variables.savedId `OWNER-044`. Application logs show INBOUND and
OUTBOUND lines. The source has a blue incoming globe above its gray response icon;
the forward route is straight and the return begins at the last Logger.

Select Transform Message to edit independent Payload, Attributes and Variables
DataWeave scripts. Enter expressions or full `%dw 2.0` scripts without outer `#[ ]`.
Blank scripts preserve existing values. Variables must produce an application/java
object such as `{customerId: payload.id}`. All scripts read the same input event.

Right-click a processor → Add breakpoint, or select it and press F9. Click Debug,
wait for Debugger connected, then send an HTTP request. Studio Console → Debug
console shows the pause, Continue, Next component and Disconnect. The request
stays pending while paused; Continue finishes it. A Listener breakpoint stops
at flow entry after accepting the request and before the first processor.

The debugger stays within Studio and does not open VS Code's Java debug console.
It currently has no watch-expression REPL, conditional breakpoints or Java source
stepping. Stop runtime when done.

## Existing projects

Run/Debug have separate dropdowns and draggable configuration windows. Configure
environment, variables, Java properties, heap, JVM options, working directory,
timeouts and debugger port. Java/bootstrap/classpaths remain managed.

XML autosaves. Redeploy on manual save defaults to No; enabling it makes explicit
Save/Command-S/Control-S rebuild a changed deployed project. Runtime replacement
restarts the private instance and closes existing connections.

Run errors stay in Studio Console. New `zt:message` applications require the
Transform support supplied by this version's portable launcher. See
KNOWN_LIMITATIONS.md for the current development scope.
