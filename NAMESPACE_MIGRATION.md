> The 0.4.0 XML migration is described in APPLICATION_FORMAT.md. The evidence
> below records the previous Java namespace milestone; it does not qualify the
> current XML/deployment-contract build. See the new dev-test evidence when
> generated.

# ZERNOVA Java namespace migration — 0.3.0-dev.2

This branch starts from `zernova/studio-bundle-0.3.0` at
`46cae04e9b02fe64e951791f4069089a01296695`. That commit already contains the
4.10 source-build and runtime-recovery branches. The approved Studio 0.2.0 ZIP
is imported into `zernova-studio-kernel/` with its approved UI retained. Subsequent local Studio/runtime integration is recorded in `zernova-studio-kernel/STUDIO_LOCAL_VERIFICATION.md`.

## What changed

Java package roots and Maven group IDs move from `org.mule` to `com.zernova`.
Java classes containing `Mule` use `Zernova`, including constructors, type
references, reflective names, service provider files, JPMS descriptors and
dependency bytecode. `namespace-map.json` records the explicit class mapping.
The lifecycle agent moves from `io.zernova.studio` to `com.zernova.studio`.

The matching API and distribution sources are tracked under `upstream/` so
rebuilding uses the renamed sources directly. Their pinned origins are in
`upstream/SOURCES.json`. Original copyright blocks and licenses remain intact;
modified Java files carry a separate ZERNOVA modification notice.

The kernel and APIs are compiled from these sources. Dependencies outside this
repository are relocated in a **private** Maven repository, `.zernova-m2`.
Constant-pool lengths, descriptors, reflection strings, module names, nested
service archives, manifests and Maven metadata are rewritten together. Modified
signed archives lose their invalid signatures and digest entries; unrelated
archives and their signatures remain unchanged. Relocation records retain input
and output hashes. The shared Maven cache never receives renamed binaries.

## Compatibility boundary

XML schema URLs and XML element names, DataWeave syntax, `META-INF/mule-artifact`,
`mule-artifact.json`, `minMuleVersion`, artifact IDs, classifiers such as
`mule-application`, `MULE` product protocol values, launch properties such as
`mule.home`, and legacy method/field names remain compatible with existing
applications. These are not Java package or class declarations. External
libraries keep their own package roots except for references into the relocated
API. Required upstream attribution remains visible in the startup banner.

This is a breaking Java API namespace change: custom Java applications and
connectors must be recompiled against `com.zernova`, or have their full dependency
closure relocated with the same mapping. Unmodified vendor connectors are not
interchangeable with the renamed APIs. The included HTTP dependency profile is
relocated and tested as a unit.

## Build

Prerequisites for developers: Git, Python 3, Java 17 JDK, Maven 3.9+, and Node 22
for the portable control/acceptance tooling. GitHub authentication is needed to
recover the pinned native Java input artifacts. Runtime users receive bundled
Java and dependencies; they do not run the bootstrap.

```sh
bash zernova-build/build-source.sh
bash zernova-build/test-source.sh
npm ci
npm run build:portable
```

On a first build, the bootstrap uses a detached checkout of the pinned baseline
to resolve and build historical dependencies, then relocates them privately.
It does not supply the final kernel binaries. Once the private repository is
prepared, the renamed API, extension API, all kernel modules/test sources and
standalone assembly build offline. An existing completed baseline cache can be
used with `bash zernova-build/build-source.sh --prepared-repository /path/to/cache`.

Do not run the historical `brand-source.py`, `stabilize-build.py` or API
preparation scripts against this renamed tree. Those recipes belong to the
baseline checkout. Do not substitute the old compiled checkpoint for the new
kernel. Compilation and application acceptance are separate gates.

To prepare native Java and connector inputs on a fresh machine:

```sh
python3 zernova-studio-build/create-fixture.py
# Set GH_TOKEN through your authenticated environment, then:
python3 zernova-studio-build/prepare-bootstrap-native.py darwin-x64
python3 zernova-studio-build/prepare-native.py darwin-x64
```

Use the host's actual target (`darwin-x64`, `darwin-arm64`, `linux-x64`,
`linux-arm64`, `win32-x64`, or `win32-arm64`). The final assembler requires a
successful renamed source build and uses its standalone ZIP. It compiles the
current lifecycle agent, bundles Java and relocated HTTP libraries, includes
corresponding sources and notices, regenerates integrity records, and audits
the Java namespace recursively. It refuses to replace an existing output.

For an already downloaded input bundle, use
`prepare-native.py TARGET --inputs PATH --output NEW_PATH`.

## Sample application and independent runtime

`zernova-studio-build/tests/fixtures/native-hello` is a real HTTP/Logger app.
The following commands use the same backend independently of VS Code:

```sh
node zernova-studio-build/cli.cjs run \
  --project zernova-studio-build/tests/fixtures/native-hello \
  --instance "$HOME/.zernova/sample-instance" --app hello-zernova --port 18081
curl 'http://127.0.0.1:18081/hello?testId=manual-test'
```

Replace `run` with `status`, `logs`, `undeploy` or `stop`, retaining the same
project, instance and app arguments. Use `--bundle PATH` for a nondefault bundle.

The full acceptance harness builds, deploys, requests, checks fresh correlated
logs, redeploys, checks expression and configuration errors, and stops twice:

```sh
cd zernova-studio-build
node tests/native-runtime.cjs
```

Evidence is written to `zernova-build-evidence/` and
`zernova-studio-build/evidence/`. `VERIFICATION.md` records what actually ran.
Earlier six-platform results describe the previous namespace; they do not
establish six-platform acceptance for this refactor. The Studio source import
also does not establish a finished installed-Studio release.
