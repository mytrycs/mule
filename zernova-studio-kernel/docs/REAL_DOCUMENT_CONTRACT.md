# Phase 2B real integration document contract

The optional custom editor is `zernovaKernelStudio.integration`. Its document
selection pattern is confined to integration XML under `src/main/mule`. The
ordinary VS Code XML editor remains available through **Reopen Editor With…**.
Message Flow, Global Elements and Configuration XML are views of the same
`vscode.TextDocument`; there is no separate saved canvas application format.

## Authoritative editing

Visual requests carry a document version and unique request ID. The host rejects
an outdated version, returns the current snapshot, and does not overwrite the
newer document. An accepted mutation is a VS Code `WorkspaceEdit`, so native
dirty state, Save, Undo, Redo and dirty-close protection apply. The host rechecks
the document version after asynchronous cross-file reference checks.

The text of an accepted edit is correlated with its request ID in snapshots.
`changeRequestId` is present only while the document text exactly matches that
pending request's expected result. External changes are not labeled as local
merely because an edit is in progress. This lets the webview preserve a conflict
instead of sending queued drafts against an unrelated newer document.

The embedded XML view writes to that same document. Invalid or incomplete XML
is retained verbatim and produces diagnostics. The host never substitutes the
last valid projection for invalid source. Filesystem changes observed by VS Code
produce new snapshots. The webview keeps a conflicting draft available for
review instead of silently overwriting a newer host document.

Incomplete property values also belong to the native document. For example,
typing a response status from `200` to `201` may temporarily produce `2` or `20`;
those values are saved as XML with semantic error diagnostics. The same applies
to incomplete paths, methods, messages and configuration references. Finishing
the value clears the relevant diagnostic. Structural and read-only guards stay
in force; build/run checks reject unresolved error diagnostics. The editor does
not keep an apparently accepted field value separate from the execution source.

Runtime actions wait for prior document edits, then execute outside the document
mutation queue. Typing and cancellation do not wait behind a build. The runtime
adapter separately checks saved inputs and rejects stale builds.

## XML preservation and supported scope

`sax` 1.6.1 validates XML; additional checks reject duplicate expanded attribute
names, multiple root elements and invalid XML characters. The source parser
retains element/attribute offsets, quote style, namespace bindings and original
text. Mutations use those spans. The implementation does not serialize a subset
of recognized fields over the complete document.

Comments, CDATA, Unicode, expressions, namespace aliases, unknown attributes and
unrelated elements remain unchanged when another supported field is edited.
Editing a response body intentionally changes that body's text; unrelated CDATA
is preserved. Namespace/schema entries are added only when the inserted or
updated supported construct requires them, and existing aliases are reused.

Supported real visual constructs are:

- A named core flow with one HTTP Listener in its first source position and
  zero or more core Loggers. Authoring incomplete flows is allowed; building
  requires a runnable application.
- HTTP Listener display name, configuration reference, path and methods,
  successful response status, body and headers.
- HTTP Listener configuration name, HTTP protocol, host, port and base path.
- Logger display name, message, TRACE/DEBUG/INFO/WARN/ERROR level and category.
- Logger insert, duplicate, reorder and delete. New components receive distinct
  documentation IDs. A supported legacy component without an ID gains one when
  necessary to retain its selection through an edit or reorder.

Multiple supported flows in one document are projected separately. A flow
containing other components remains intact and is marked read-only for visual
component editing. Unknown top-level elements are preserved. HTTPS/TLS and
advanced connection content are preserved but are outside this milestone.
Existing error-response XML is preserved; no error-response property editor is
claimed. DataWeave is executed only by the runtime when contained in supported
expression fields; there is no independent Studio evaluator.

The bounded global-configuration editor lists declarations in the current XML
document. It renames supported Listener references in that document. Renaming
or deleting a configuration referenced by another integration XML is refused;
the host does not silently change another file. Invalid sibling XML or more than
200 integration XML files also blocks automated shared-config rename/delete.
Unsupported reference kinds and referenced deletion are refused. Duplicate
names or documentation IDs disable ambiguous visual targets until corrected in
XML. General cross-document import/migration is outside the bounded editor.

DTD/entity declarations are not processed. Source over 1 MiB is outside the
bounded visual model; use the ordinary text editor to reduce it. Fields accept
bounded strings and applicable runtime placeholders/expressions. Static
validation does not claim that a DataWeave expression will execute successfully.

## Closed message surface

Preview and real-document bridges are separate. Preview cannot invoke runtime
actions. Real messages are limited to ready, versioned XML/edit requests, and a
fixed action allowlist. Requests cannot supply filesystem URIs, executables,
arbitrary commands, shell strings or working directories. The host already owns
the open document URI and selected project. Runtime configuration and workspace
trust are validated by the host adapter.

All real mutations receive `realAck` with the accepted version, or `realError`
with the request ID. Invalid schemas and oversized messages are rejected.
Native diagnostics use the source `ZERNOVA`; runtime build/deployment errors are
reported separately from static XML projection diagnostics.

## Verification entry points

`npm test` discovers `tests/xml.test.ts` alongside the preserved preview tests.
`scripts/test-document-host.mjs` installs the packaged subject VSIX into a fresh
desktop profile and runs the document lifecycle tests. It uses a separate,
development-only extension to drive public VS Code document commands. Intentional
stale/hostile requests use the actual native webview message channel inspected
in the installed VS Code build, with a passive observer in the real inner
document. The production extension does not expose a mutable testing API.
The isolated test profile uses VS Code's built-in custom dialog setting to make
the ordinary dirty-close prompt observable. Results are retained under
`evidence/phase2b/document-host-*`.
