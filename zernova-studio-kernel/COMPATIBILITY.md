# Compatibility — Studio 0.2.0

This matrix separates declared requirements from observed release coverage.
The final release report provides exact test timestamps, hashes, and results.

| Component or route | Contract / selection | Coverage |
| --- | --- | --- |
| Extension identity | `zernova-preview.zernova-studio-kernel-preview@0.2.0` | Installed VSIX tests |
| VS Code | Manifest minimum 1.96.0; test host 1.144.0 | Linux desktop tested; minimum-version host NOT RUN |
| Extension host | Linux x86-64, Ubuntu 24.04.3 LTS | Verified local execution arrangement |
| Runtime Foundation | 0.1.0-preview.1, existing CLI/JSON contract | Read from actual files and used by adapter |
| Mule Kernel | 4.12.0 official pinned distribution | Actual application execution |
| DataWeave artifacts | Bundled 2.12.0 | Runtime expressions used by Listener/Logger; no visual mapper |
| Java | Java 17 JDK, Temurin 17.0.21+9 in test environment | Actual selected JDK |
| Maven | Apache Maven 3.9.11 | Actual selected Maven/cache |
| Application plugin | Mule Maven Plugin 4.10.3 | Target 4.12.0; internal packaging framework reports 4.13.0 |
| HTTP connector | 1.12.2; Sockets transitive 1.2.9 | Normal Maven resolution and runtime execution |
| Python / shell | Python 3.10+, Bash, Linux pidfd support | Required by existing Foundation launcher |
| Node/npm | Node >=22.14.0 for extension build; test environment Node 24.22.1 | Extension source build; not an app runtime dependency |
| Offline application build | Selected Maven cache must already contain required artifacts | Prepared-cache offline workflow tested |
| First-time offline preparation | No cache or downloaded Runtime | Unsupported; missing dependencies need authorized downloads |
| Native macOS / Apple Silicon runtime | No established Foundation launcher support | NOT RUN; do not assume Linux instructions work natively |
| Native Windows runtime | No established Foundation launcher support | NOT RUN |
| Mac VS Code → Linux Remote-SSH extension host | Linux workspace/tools/runtime; optional loopback forwarding | Documented option; NOT RUN for this release |
| Browser-only VS Code / virtual workspace | Requires accessible filesystem and host processes | Not supported for real execution |
| Screen reader | No manual assistive-technology session performed | NOT RUN; automated accessibility checks are not certification |

## Build-tool discrepancy

The supplied Maven Plugin 4.10.3 requests runtime target 4.12.0 but uses an
internal framework associated with 4.13.0. This discrepancy is retained and
documented. Actual deployment tests against Kernel 4.12.0 establish this bounded
application's observed compatibility; they do not prove compatibility for all
components or configurations. No silent dependency upgrade or vendor patch is
used to conceal it.

## Independent release gates

| Gate | Meaning |
| --- | --- |
| Functional HTTP Listener → Logger milestone | Assessed by current installed-Studio, HTTP, log, restart, and cleanup evidence |
| Independent source rebuild of the upstream Kernel | Existing blocked gate; not resolved by building this TypeScript extension |
| Customer redistribution of runtime/connector/application binaries | Existing blocked gate; requires its separate obligations review |

Only Studio extension/source, application **source**, documentation, and test
evidence belong to this delivery. No Kernel, connector, or app JAR is included.
