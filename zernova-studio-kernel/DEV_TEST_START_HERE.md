# Studio 0.6.0 owner development test

The update is installed in the normal VS Code profile. A Mac x64 VSIX, the
all-core sample and verification evidence are also supplied in the handoff folder.
Runtime 0.4.1-alpha.1, core support 0.5.0 and patched Java 17 are bundled.

1. Open your existing XML in ZERNOVA Studio. Every flow/subflow is on the same
   canvas. Add a flow/subflow with New flow or the palette; scroll to see all cards.
   Original flows remain in the same XML and stay available on the canvas.
2. From the project context menu choose New → New Integration XML. The draggable
   popup appears over the current Studio document. Enter a filename and Create XML
   (or press Enter). The original stays open and the new XML opens in its own tab.
3. Right-click project folders, source XML, properties, POM and other resources.
   Open/edit, New, Run As, Debug As, Build, Maven/Dependencies, Find, Rename/Move,
   Trash, path copying and Show In appear where applicable.
4. Expand ZERNOVA Artifacts, Maven Dependencies and Project Artifacts. Expand the
   HTTP dependency to inspect bundled JARs. Right-click Maven Dependencies → Maven /
   Dependencies → Manage Maven Dependencies to add/remove declarations. Unsupported
   coordinates are marked unavailable and prevent a misleading successful build.
5. Add For Each, Try, Until Successful or Cache. Each starts as an empty framed drop
   area with its own icon. Place processors inside; Delete/Backspace removes a
   selected processor. Removing the last child leaves the scope frame empty.
6. Open the supplied All_Core_App folder with Open ZERNOVA Project, open
   src/main/zernova/all-core.xml and Run. Default port is 18081. The whole app has
   21 flows/subflows and uses every catalog component.

Useful sample requests:

| Path | Expected result |
|---|---|
| /hello | JSON greeting, external attributes marker and variables |
| /variables | Kept variable present; removed variable absent |
| /reference | from-subflow |
| /template | Hello ZERNOVA |
| /choice?pick=a or pick=b | A or B |
| /first | recovered after the first route fails |
| /round | one, two, one on successive calls |
| /scatter | left and right route messages |
| /foreach | Sum 6 |
| /tryscope | handled |
| /retry | RETRY_READY; native acceptance also tests a transient failure |
| /asyncscope | main, with separate async Logger entry |
| /cachetest?key=a | Repeated key returns the same UUID while cached |
| /cachettl | Cached UUID expires after one second |
| /dynamic | HELLO and current environment |
| /business | Business-event entry in application logs |
| /continued | Normal response from On Error Continue |
| /propagated | Intentional HTTP 418 from the Listener error response |
| /batch | Accepted job ID; logs show groups, failed record 3 and completion |

The Scheduler logs every second. Use Console → Application logs to inspect these
entries. Error/retry/batch rejection messages in this sample are intentional test
cases. Stop Runtime when finished. Known limitations describes local Batch/Cache
semantics and the current offline dependency boundary.
