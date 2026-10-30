#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. Original test fixture, not an alternate engine.
from pathlib import Path
import json
root=Path(__file__).resolve().parent
(root/'LICENSE.txt').write_text('''ZERNOVA Original Components — Development License 0.3
Copyright (c) 2026 ZerNova. All rights reserved.

The original portability adapter, packager, lifecycle agent and test infrastructure
in this directory are supplied to the project owner for evaluation and continued
development. Public visibility does not grant a public redistribution license.
No customer release or production warranty is granted by this development license.
This license does not apply to inherited engine source, engine modifications,
Java or third-party dependencies. Their existing licenses and required notices
continue to apply independently and must not be removed or restricted by this file.
''',encoding='utf-8')
project=root/'tests/fixtures/native-hello'
(project/'src/main/zernova').mkdir(parents=True,exist_ok=True)
(project/'src/main/resources').mkdir(parents=True,exist_ok=True)
(project/'zernova-app.json').write_text(json.dumps({'schemaVersion':1,'name':'hello-zernova','group':'com.zernova.example','version':'1.0.0','dependencies':['http']},indent=2)+'\n',encoding='utf-8')
(project/'src/main/zernova/hello.xml').write_text('''<?xml version="1.0" encoding="UTF-8"?>
<!-- ZERNOVA application. Namespaces below are engine compatibility identifiers. -->
<zernova xmlns="https://schemas.zernova.dev/core"
 xmlns:http="https://schemas.zernova.dev/http"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="https://schemas.zernova.dev/core https://schemas.zernova.dev/core/current/zernova.xsd https://schemas.zernova.dev/http https://schemas.zernova.dev/http/current/zernova-http.xsd">
  <http:listener-config name="Local_HTTP">
    <http:listener-connection host="127.0.0.1" port="${zernova.http.port}"/>
  </http:listener-config>
  <flow name="helloFlow">
    <http:listener config-ref="Local_HTTP" path="/hello" allowedMethods="GET">
      <http:response statusCode="200">
        <http:body>#['Hello from ZERNOVA']</http:body>
        <http:headers>#[{'Content-Type': 'text/plain; charset=UTF-8'}]</http:headers>
      </http:response>
    </http:listener>
    <logger level="INFO" category="io.zernova.acceptance"
      message="#['ZERNOVA_TEST ' ++ (attributes.queryParams.testId default 'missing')]"/>
  </flow>
</zernova>
''',encoding='utf-8')
(project/'src/main/resources/log4j2.xml').write_text('''<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (c) 2026 ZerNova. Original application configuration. -->
<Configuration status="WARN">
  <!-- The runtime adds its application-specific rolling file appender. -->
  <Loggers><AsyncRoot level="INFO"/></Loggers>
</Configuration>
''',encoding='utf-8')
