# Changelog

## 0.2.0 — 2026-10-29

Continues the same approved NEW Studio identity and visual design.

- Added a native project wizard with Maven coordinates, explicit project-specific
  tool/runtime selection, cancellation, and destination validation. New projects
  contain an empty `src/main/mule` directory until the owner creates XML.
- Added the optional ZERNOVA Integration custom text editor for project XML,
  with Message Flow, Global Elements, and editable Configuration XML sharing one
  authoritative VS Code document. Includes save, undo/redo, version checks,
  invalid-XML diagnostics, external-change handling, and preservation of
  unsupported source constructs.
- Added real HTTP Listener/global configuration and core Logger editing,
  including bounded Listener success responses and Logger ordering,
  duplicate/delete, levels, category, and runtime expressions.
- Added a versioned adapter to the existing Runtime Foundation CLI: diagnostics,
  actual Maven build, explicit run/redeploy, status, real application logs,
  undeploy, safe stop, cancellation, and bounded operation timeouts.
- Kept design fixtures and future DataWeave layout separate from real source and
  execution. Preserved optional themes and keyboard/accessibility regressions.
- Added owner instructions, actual installed-editor evidence, application source,
  compatibility/limitations documentation, and release checksums.

The adapter retains Kernel 4.12.0, Java 17, HTTP connector 1.12.2, and the supplied
Maven Plugin 4.10.3 contract. Upstream runtime/connector implementations are not
modified. Native Mac and Windows runtime support are not claimed.

## 0.1.0 — 2026-10-29

Initial six-screen interactive UI design preview in this repository and extension
identity. The owner approved its design before authorizing Phase 2B. Historical
UI-only reports remain labeled as that milestone's evidence.
