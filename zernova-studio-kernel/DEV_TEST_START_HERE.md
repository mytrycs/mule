# Studio 0.5.0 development test

Install the Mac x64 VSIX, then reload VS Code. Open your existing ZERNOVA project
or the supplied `ZERNOVA_0.5.0_Sample` folder, and open `src/main/zernova/hello.xml`
with ZERNOVA Studio. Runtime and Java are bundled.

1. Click Run. The embedded Console has a Deployment tab showing the Z logo,
   application/environment/version details, Build succeeded and deployment stages.
   Application logs remain available in their own tab.
2. Request `http://localhost:18081/hello` in the supplied sample. Expect JSON with
   a greeting and active environment. The Logger writes a matching application entry.
3. Select Transform Message. Payload is the default output. Add Attributes or
   Variables if needed; remove Payload after another output exists. Configure source
   opens a draggable form for inline script, an existing `dwl` file or a new file.
   Open external file edits the linked resource in the code editor.
4. Select the HTTP Listener to configure success and error responses. Transform
   Message does not own HTTP response settings.
5. Open Global Elements → Environment & properties. Choose an environment and a
   property file pattern. The supplied sample has development and test files.
   Run/Debug profiles can subsequently override the environment and other settings.
6. Search the palette for core components. Drag a processor into a valid scope/route,
   or select it in the palette then click a valid empty location. Scope headers expand
   and collapse. When/Otherwise belong to Choice, Route belongs to routers, Batch Step
   belongs to Batch Job and Batch Aggregator belongs to a Step.
7. Review KNOWN_LIMITATIONS.md before testing Batch, Cache or advanced scenarios.
   Extra `Core_Catalog_Sample` and `Batch_Sample` folders contain runtime-tested examples.

Property examples:

```xml
<global-property name="zernova.env" value="development"/>
<configuration-properties file="config/application-${zernova.env}.properties"/>
```

```dataweave
%dw 2.0
output application/json
---
{message: p('app.message'), label: "Environment $(p('zernova.env'))"}
```

Enable redeploy on manual save in Run configuration if desired. XML autosaves keep
editing safe; explicit Save triggers redeployment only when this option is enabled.
Manual saves of linked `.dwl` and property files also participate. Test endpoints
with real requests: a successful deployment alone is not an endpoint health test.
