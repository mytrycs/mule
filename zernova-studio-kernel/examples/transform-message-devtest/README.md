# HTTP + Transform Message dev test

Open this folder with Studio 0.4.3. Open `src/main/zernova/hello.xml` in ZERNOVA
Studio and click Run / Deploy. Call:

http://localhost:18082/transform?testId=OWNER-043

Expect JSON containing message `Hello from ZERNOVA Transform`, environment
`development` (unless your Run profile selects another environment), and testId
`OWNER-043`.

Select Transform Message to edit its DataWeave payload script and output MIME
type. Do not include the surrounding `#[ ]` in the script editor. XML autosaves.
The component uses native ZERNOVA core `set-payload` XML and the bundled DataWeave
engine; it adds no enterprise-only connector dependency.

The Listener returns `#[payload]` and application/json. For other existing apps,
use the Transform property button **Use payload for HTTP response** or configure
the Listener response body yourself, and set its response headers as needed.
Run again, or enable redeploy on manual save and press Cmd/Ctrl+S while deployed.
Stop the runtime after testing. No external Java/Maven/runtime is needed.
