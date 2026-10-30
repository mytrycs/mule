# Transform and component-debug development test

Requires Studio 0.4.4 and its managed ZERNOVA Transform support module. The base
engine remains ZERNOVA Runtime 0.4.1-alpha.1, using bundled Java 17.

Open hello.xml in Studio. Run and call
`http://localhost:18083/transform?testId=OWNER-044`.
Expect JSON with payload.message `Hello from ZERNOVA`, attributes.correlation
`OWNER-044`, attributes.source `ZERNOVA`, and variables.savedId `OWNER-044`.
Application logs show INBOUND and OUTBOUND correlation lines.

Right-click Before Transform and add a breakpoint (or select it and press F9).
Click Debug and wait for Debugger connected in Studio Console → Debug console.
Send a fresh HTTP request: it remains pending until you Continue. Next component
moves to Transform Message, then After Transform. Run ignores breakpoints.
Source breakpoints pause at flow entry after HTTP acceptance, before processor 0.
Disconnect resumes pending processing. Stop runtime when finished.

Blank Transform outputs preserve the input. Variables scripts produce an
application/java object of named variables to set/replace; other variables remain.
All outputs evaluate against the same input. This release has no conditional
breakpoints, watch-expression REPL, Java source stepping, or visual schema mapper.
