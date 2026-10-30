# HTTP + Logger development test

Open this folder in VS Code with ZERNOVA Studio 0.4.0 installed. The native VSIX includes ZERNOVA Runtime 0.4.0-alpha.1, Java 17, HTTP dependencies and offline application packaging. No external runtime, Java or Maven configuration is needed for this example.

1. Open `src/main/zernova/hello.xml`. Run **ZERNOVA: Open Integration in Studio** if it opens as text.
2. Trust this project when prompted. Open **Console**, then **Build** and **Run / Redeploy**.
3. Visit `http://127.0.0.1:8081/hello?testId=dev-test-1`. Expect `Hello from ZERNOVA`.
4. Click **Refresh logs → Application logs**. Find `ZERNOVA_TEST dev-test-1` from the Logger.
5. Add a second Logger from the palette. The flow should widen and its response line should begin at the final Logger. Delete it: the flow should shrink again.
6. Save, rebuild and run to test your changes. Stop the runtime when finished.

The Listener binds only to this computer. If port 8081 is occupied, change the Listener connection port in Global Elements before running. This is an internal development alpha; YAML, arbitrary connector dependencies and custom Java builds are deferred.
