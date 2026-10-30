# HTTP + Logger development test

Open this folder in VS Code with ZERNOVA Studio 0.4.2 installed. The native VSIX includes ZERNOVA Runtime 0.4.1-alpha.1, Java 17, HTTP dependencies and offline application packaging. No external runtime, Java or Maven configuration is needed for this example.

1. Open `src/main/zernova/hello.xml`. Run **ZERNOVA: Open Integration in Studio** if it opens as text.
2. Trust this project when prompted. Click **Run / Deploy** at the top right. Wait for application deployment.
3. Visit `http://localhost:18081/hello?testId=dev-test-1`. Expect `Hello from ZERNOVA`.
4. Open **Console → Application logs**. `ZERNOVA_TEST dev-test-1` and `Second Logger reached` appear automatically; **Follow latest** returns to the tail after scrolling back.
5. Select another Logger in the palette and click a valid open flow location, or drag it into a valid flow location. The flow should widen and its response line should begin at the final Logger. Select it and press Delete: the flow should shrink again.
6. XML autosaves. Run again to build and deploy your changes. Stop the runtime when finished.

The Listener binds only to this computer. If port 18081 is occupied, change the Listener connection port in Global Elements before running. This is an internal development alpha; YAML, arbitrary connector dependencies and custom Java builds are deferred.
