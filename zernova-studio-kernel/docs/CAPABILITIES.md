# Implemented versus preview — 0.2.0

| Capability | Real project mode | Design preview / future scope |
| --- | --- | --- |
| Project tree and creation | Actual filesystem, Maven coordinates, explicit empty integration folder, open/select project | Fixture state remains in isolated design editor |
| Create integration XML and flow | Explicit owner actions; actual source document | Illustrative preloaded flow |
| Flow/property/Global Elements/XML views | One authoritative VS Code text document; same native editor tab | One synthetic preview model |
| Save, dirty state, undo/redo | VS Code document edits/save/undo facilities; version guarded requests | Local preview persistence and bounded history |
| HTTP Listener | Real insertion, source constraints, display name, reference, path, methods | Preview interaction only |
| HTTP Listener configuration | Name, HTTP, host, port, base path, local validation | Other configuration layouts illustrative |
| Listener success response | Status, body expression, headers for the accepted workflow | Error-response controls deferred; existing XML retained |
| Core Logger | Display name, message, supported level, category; reorder/duplicate/delete | Preview never executes Logger |
| XML preservation | Comments, CDATA, namespace aliases, unknown attributes/elements preserved by bounded source edits | Derived preview XML is read-only |
| Unknown components | Preserved and visibly unsupported/read-only where needed | No execution support added |
| Build/run/redeploy | Selected project's actual Maven artifact and existing Runtime CLI | No executable preview bridge |
| Status/logs | Genuine build output, runtime/app status, application log file | Fixture views labeled illustrative |
| HTTP endpoint verification | Owner/acceptance makes independent request and matches unique log identifier | No fake Test Connection button |
| Undeploy/stop | Owned app/instance lifecycle, explicit controls | No shared runtime management |
| Light/dark/high contrast and keyboard | Retains approved workbench controls and host-theme handling | Original six-screen regressions retained |
| HTTP Request | Disabled | Future |
| DataWeave | Existing runtime expressions used by Listener and Logger | Visual mapper/sample output remain design-only |
| Debug, enterprise, accounts, other connectors | Not implemented | Awaiting separate approval |

Use **ZERNOVA: Open Studio Design Preview** only to inspect fixtures/future
layouts. Use **ZERNOVA: New ZERNOVA Project** or **Open ZERNOVA Project** to edit
and execute real source. The two modes have separate host message contracts.
