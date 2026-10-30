# ZERNOVA Studio development test

Studio extension 0.4.0 · ZERNOVA Runtime 0.4.0-alpha.1 · bundled Java 17.

## Install on this Mac

1. In VS Code, open **Extensions**, choose **… → Install from VSIX…**, and select the supplied `zernova-studio-kernel-preview-0.4.0-darwin-x64.vsix`.
2. Reload VS Code when prompted. This package is for Intel/x64 macOS.
3. Open the supplied `http-logger-devtest` folder as your workspace.
4. Open `src/main/zernova/hello.xml` and run **ZERNOVA: Open Integration in Studio** from the Command Palette.
5. Trust this project to enable local execution. No external Java, Maven or runtime path is required.

## Run the sample

Open the designer's **Console** tab. Click **Build**, then **Run / Redeploy**. The first startup may take a little time; wait for deployed status.

Open this URL in a browser:

`http://127.0.0.1:8081/hello?testId=dev-test-1`

Expected response: `Hello from ZERNOVA`.

Click **Refresh logs**, then **Application logs**. Look for `ZERNOVA_TEST dev-test-1`. Change the query test ID to identify another request.

Add a second Logger from the palette. The flow should grow wider, with its return line starting at that final Logger. Delete the Logger and the flow should shrink. Edit the remaining Logger's message, save, and **Run / Redeploy** to test your change. Use **Stop runtime** when done.

If port 8081 is occupied, edit the HTTP Listener connection in **Global Elements**, then use the changed port in the URL. Runtime diagnostics report missing or damaged bundled files rather than silently selecting an external runtime.

## Build your own small app

Run **ZERNOVA: New ZERNOVA Project**, select a destination and the HTTP dependency. Add/configure an HTTP Listener and a Logger in the designer. Configure a successful response on the Listener. Save, open Console and Run / Redeploy. New projects use `<zernova>` and bundled ZERNOVA schemas.

## Feedback to send

Include the action you took, what you expected, what happened, a screenshot if visual, and the Console build/application logs if execution failed. Please test create/open, edit/save/reopen, Run, HTTP response, Logger output, add/delete sizing and Stop.

## Scope

This is an internal development alpha delivered as a VS Code extension with its runtime, Java, HTTP dependencies, notices and corresponding runtime source inside the VSIX. The engine is derived from the 4.10 source baseline; the ZERNOVA product version starts independently at 0.4.0-alpha.1. Historical and legally required upstream attribution remains.

The supported authoring/build slice is core + HTTP Listener/Logger. Runtime acceptance also exercises transformation/validation, redeployment and failure recovery. YAML, arbitrary extra connectors and custom Java builds remain future work. Use explicit legacy XML import for supported older projects. This is not a production release.

Run / Redeploy restarts this project's private runtime when replacing an application. Expect a short interruption and a new runtime run ID. Existing client connections are closed; seamless live redeployment is deferred. The runtime is dedicated to this project, and restart uses authenticated shutdown. Acceptance requests are never automatically retried.
