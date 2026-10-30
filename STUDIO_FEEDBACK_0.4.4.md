# ZERNOVA Studio 0.4.4 — source routes, full Transform outputs and component debugging

The approved source presentation is implemented: blue incoming globe above a gray
outgoing response icon. The forward SVG connects measured icon edges at a common
height, independent of the source's taller card. The return starts at the last
processor's output port.

Transform Message now has independent DataWeave payload, attributes and variables
scripts. Blank scripts preserve that target. All scripts evaluate against the
same incoming event; named-variable outputs merge into existing variables. Invalid
variable output fails before a changed event is emitted. Existing core set-payload
XML remains readable and upgrades on edit without losing its ID, name or script.

New transforms use `zt:message` in `https://schemas.zernova.dev/transform`. An
original Java 17 processor, schema, DSL provider and extension declaration are
compiled from `zernova-studio-build/transform-support/` and embedded in the portable
launcher. It installs a verified support JAR into the owned private runtime before
startup. Existing engine files and the immutable engine archive are unchanged.
The base engine remains Runtime 0.4.1-alpha.1; the support module is 0.4.4. Older
bare engines do not provide this new transform component.

Right-click a component or select it and press F9 to toggle its breakpoint.
Breakpoints are project-local and use component IDs. Debug connects directly over
loopback JDWP to the qualified engine's pre-component execution hook. It suspends
the executing event thread before a matching processor runs, not the whole VM.
Listener breakpoints mean flow entry after HTTP acceptance. Continue resumes;
Next component follows the current execution thread; Disconnect disposes JDWP and
resumes pending work. Run does not connect a debugger or pause on these points.

Debug output and controls are in Studio Console → Debug console. No VS Code Java
debug session is started, and the Java debugger extension is not required. This
iteration does not add conditional breakpoints, expression watches, Java source
stepping or an asynchronous-flow stepping contract for future connectors.

The step transition clears the previous UI pause before resuming and snapshots
paused threads before sending resume commands, so a new breakpoint cannot be
removed or accidentally resumed by that earlier transition.

Validation and installation evidence is recorded under
`verification/studio-0.4.4-devtest/`. Local Mac x64 qualification is separate from
future installed-Studio qualification on the other five targets.
