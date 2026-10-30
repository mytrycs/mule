# Platform / Exchange integration candidate

This isolated branch continues Studio 0.5.0 at `a4cb85fcb5`; candidate version is `0.5.0-platform-lab.3`. It must be reviewed/merged by the Studio owner later. The active Studio/Runtime branch and normal desktop installation are not changed by this candidate.

## User journey

1. Configure the customer Platform's exact HTTPS origin in machine settings. Use the operator-approved CA PEM and a private Java truststore for the local Docker lab; certificate checks remain enabled. Never configure these paths or credentials through an untrusted project.
2. Open **ZERNOVA → Platform & Exchange → Connect to ZERNOVA Platform**, or use **ZERNOVA: Sign in to Platform**. For a browser-hosted IDE, copy the sign-in link into a normal browser tab; it keeps HTTPS and the exact customer origin outside IDE port proxies. Desktop Studio opens that browser directly. Sign in through the customer's configured provider in the browser, compare the displayed code and approve the read-only Studio session.
3. Select an organization and workspace. Browse approved Exchange API specifications. Use **Add Exchange API Dependency** and review the immutable GAV, version and SHA-256 before changing the project.
4. Studio resolves real Maven bytes using a short-lived organization-scoped credential, checks integrity, then adds the POM dependency, a secret-free `zernova-exchange.json` lock and the exact YAML/RAML file in `src/main/resources/exchange/`.
5. Building rechecks the live publication, permission/licence and file hashes before using that data resource. Additional executable Runtime dependencies remain unsupported. Existing core/HTTP build behavior is preserved.
6. Sign out or revoke the Studio session in Platform Identity & access. Both native API access and derived Maven grants stop working. A new login is required after the bounded session expires.

The authentication provider uses VS Code SecretStorage keyed by installation origin. No provider client secret or repository publisher credential is embedded in Studio. Storage encryption depends on the host: a browser-hosted IDE is not automatically backed by the Mac keychain. Protect its account, private profile and machine. Do not put tokens in settings, POMs, lockfiles, logs or source control.

## Local verification and limits

The companion Platform worktree contains Docker Compose, local CA generation, private Reposilite and Keycloak, an isolated code-server helper and the actual Maven protocol harness. It uses separate ports/volumes/profiles and a snapshot of the already accepted Mac Runtime bundle. Google is disabled by the user's current choice. Real-provider/browser/native UI qualification is separate from synthetic protocol tests; consult the companion final evidence instead of inferring a PASS from unit tests.

`npm test`, `npm run typecheck`, `npm run package` exercise the candidate in this worktree. The package validator also checks the unchanged accepted Runtime bundle. A fresh audit updated packaging-only transitive Azure identity dependencies; the application does not gain an Azure dependency or cloud service.

This work does not implement Runtime-side licence enforcement, arbitrary executable Maven connectors, generated API implementations, commercial licence issuance or a production/HA install. It does not install into the normal VS Code profile. No commercial release approval or extension Marketplace publication is implied.
