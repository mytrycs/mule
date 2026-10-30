# Quickstart — real HTTP Listener → Logger

The complete beginner procedure and exact field values are in
[TESTING_STEP_BY_STEP.md](../TESTING_STEP_BY_STEP.md). Use the tested Linux x86-64
arrangement; the owner guide distinguishes it from untested native Mac and
Remote-SSH routes.

1. Prepare Runtime Foundation 0.1.0-preview.1, Java 17, Maven 3.9.11, and the
   Maven dependency cache. Check the Foundation's `version --json` and
   `doctor --json` output.
2. Install `zernova-studio-kernel-preview-0.2.0.vsix` in an isolated VS Code
   profile; verify `zernova-preview.zernova-studio-kernel-preview@0.2.0`.
3. Open/trust your own workspace and run **ZERNOVA: New ZERNOVA Project**.
   Choose your parent folder, `zernova-owner-test`, `io.zernova`, artifact
   `zernova-owner-test`, version `1.0.0`, and configure your real runtime/tools.
4. Confirm `src/main/mule` is empty. Run **ZERNOVA: New Integration XML** with
   filename `hello.xml`, then **New flow** with name `helloFlow`.
5. Insert Listener from the palette and select it. Use **Create HTTP Listener
   configuration** (the plus button) beside its configuration selector: name
   `HTTP_Listener_config`, HTTP, `127.0.0.1`, port `18081`, blank base path.
   Save, select that configuration, and set path `/hello`, allowed methods `GET`.
6. In Listener **Responses**, set status `200`, body
   `#['Hello from ZERNOVA']`, and headers
   `#[{'Content-Type': 'text/plain; charset=UTF-8'}]`.
7. Insert Logger: level `INFO`, category `io.zernova.acceptance`, message
   `#['ZERNOVA_TEST ' ++ (attributes.queryParams.testId default 'missing')]`.
8. Save. Open **Console**, then **Build** and **Run / Redeploy**. Deployment
   success leaves endpoint verification to the next independent request.
9. On the runtime machine, run:

   ```bash
   curl -i 'http://127.0.0.1:18081/hello?testId=ZN-OWNER-001'
   ```

   Expect HTTP 200, text content type, and `Hello from ZERNOVA`.
   Use **Refresh logs** → **Application logs** to locate
   `ZERNOVA_TEST ZN-OWNER-001` in actual application output.
10. Change Listener path to `/hello-v2`, Save, **Run / Redeploy**, and test the
    new URL with a new identifier. When finished, use **Undeploy**, then
    **Stop runtime**. Confirm the endpoint is gone.

No Set Payload or transformation processor is needed: Listener response
configuration supplies the returned body, and core Logger records the request.

See [compatibility](../COMPATIBILITY.md), [limitations](../KNOWN_LIMITATIONS.md),
and [adapter contract](RUNTIME_ADAPTER_CONTRACT.md) before changing the tested
configuration. Runtime/connector binaries and executable application JARs are
not included in this Studio delivery.
