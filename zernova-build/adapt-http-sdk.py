#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
"""Adapt the archived HTTP bridge to the available SDK 0.12.0 contract."""
from pathlib import Path
import hashlib,json,re
ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'modules/http-support'
OUT=ROOT/'zernova-build-evidence'; OUT.mkdir(exist_ok=True)
renames={
 'org.mule.sdk.api.http.client.HttpClientConfig':'HttpClientConfigurer',
 'org.mule.sdk.api.http.server.HttpServerConfig':'HttpServerConfigurer',
 'org.mule.sdk.api.http.client.auth.HttpAuthenticationConfig':'HttpAuthenticationConfigurer',
 'org.mule.sdk.api.http.client.proxy.ProxyConfig':'ProxyConfigurer',
 'org.mule.sdk.api.http.client.HttpRequestOptionsConfig':'HttpRequestOptionsConfigurer',
 'org.mule.sdk.api.http.sse.client.SseSourceConfig':'SseSourceConfigurer',
 'org.mule.sdk.api.http.sse.server.SseClientConfig':'SseClientConfigurer',
}
records=[]
def save(path,text,reason):
    old=path.read_bytes() if path.exists() else b''
    data=text.encode()
    if old==data:return
    path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(data)
    records.append({'path':str(path.relative_to(ROOT)),'reason':reason,'before_sha256':hashlib.sha256(old).hexdigest(),'after_sha256':hashlib.sha256(data).hexdigest(),'license':'CPAL-1.0'})
header=' * ZERNOVA compatibility adaptation Copyright (c) 2026 ZerNova. CPAL-1.0.\n'
for path in BASE.rglob('*.java'):
    if 'target' in path.relative_to(BASE).parts:continue
    text=path.read_text();old=text
    for qualified,new in renames.items():
        if qualified not in text:continue
        simple=qualified.rsplit('.',1)[1]
        text=text.replace(qualified,qualified.rsplit('.',1)[0]+'.'+new)
        # Leave differently qualified engine types such as runtime.http...ProxyConfig intact.
        text=re.sub(r'(?<![\w.])'+simple+r'\b',new,text)
    if text!=old:
        if header not in text:text=text.replace('/*\n','/*\n'+header,1)
        save(path,text,'Use published SDK Configurer names; behavior and runtime API types preserved')
for side in ['client','server']:
    name='Http'+side.title()
    path=BASE/('src/main/java/org/mule/runtime/http/support/internal/'+side+'/'+name+'ConfigToBuilder.java')
    text=path.read_text()
    if 'configHttp1(' in text:raise SystemExit('Protocol methods already exist; review rather than overwrite')
    methods='''
  @Override
  public TYPE configHttp1(java.util.function.Consumer<org.mule.sdk.api.http.protocol.Http1ProtocolConfigurer> callback) {
    org.mule.runtime.http.support.internal.HttpProtocolCompatibility.http1(callback);
    return this;
  }

  @Override
  public TYPE configHttp2(java.util.function.Consumer<org.mule.sdk.api.http.protocol.Http2ProtocolConfigurer> callback) {
    org.mule.runtime.http.support.internal.HttpProtocolCompatibility.http2(callback);
    return this;
  }
'''.replace('TYPE',name+'Configurer')
    idx=text.rfind('}');text=text[:idx]+methods+text[idx:]
    if header not in text:text=text.replace('/*\n','/*\n'+header,1)
    save(path,text,'Preserve HTTP/1.1; fail explicitly for HTTP/2 absent from this engine baseline')
helper='''/*
 * Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
 * Compatibility addition to the CPAL-covered HTTP bridge. See LICENSE.txt.
 */
package org.mule.runtime.http.support.internal;

import java.util.Objects;
import java.util.function.Consumer;
import org.mule.sdk.api.http.protocol.Http1ProtocolConfigurer;
import org.mule.sdk.api.http.protocol.Http2ProtocolConfigurer;

/** Explicit capabilities of this HTTP/1.1 engine; never silently downgrade HTTP/2. */
public final class HttpProtocolCompatibility {
  private HttpProtocolCompatibility() {}

  public static void http1(Consumer<Http1ProtocolConfigurer> callback) {
    Objects.requireNonNull(callback, "HTTP/1 configuration callback").accept(new Http1());
  }

  public static void http2(Consumer<Http2ProtocolConfigurer> callback) {
    Objects.requireNonNull(callback, "HTTP/2 configuration callback").accept(new Http2());
  }

  private static UnsupportedOperationException unavailable() {
    return new UnsupportedOperationException("ZERNOVA 4.10 source baseline supports HTTP/1.1, not HTTP/2. No protocol downgrade was performed.");
  }

  private static final class Http1 implements Http1ProtocolConfigurer {
    @Override
    public Http1ProtocolConfigurer setEnabled(boolean enabled) {
      if (!enabled) {
        throw new IllegalArgumentException("HTTP/1.1 cannot be disabled: it is the supported protocol in this ZERNOVA baseline.");
      }
      return this;
    }
  }

  private static final class Http2 implements Http2ProtocolConfigurer {
    @Override
    public Http2ProtocolConfigurer setEnabled(boolean enabled) {
      if (enabled) { throw unavailable(); }
      return this;
    }
    @Override public Http2ProtocolConfigurer setHeaderTableSize(Long value) { throw unavailable(); }
    @Override public Http2ProtocolConfigurer setMaxConcurrentStreams(Long value) { throw unavailable(); }
    @Override public Http2ProtocolConfigurer setInitialWindowSize(Integer value) { throw unavailable(); }
    @Override public Http2ProtocolConfigurer setMaxFrameSize(Integer value) { throw unavailable(); }
    @Override public Http2ProtocolConfigurer setMaxHeaderListSize(Long value) { throw unavailable(); }
  }
}
'''
save(BASE/'src/main/java/org/mule/runtime/http/support/internal/HttpProtocolCompatibility.java',helper,'Explicit fail-fast protocol capability adapter; no fake HTTP/2 implementation')
test='''/*
 * Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
 */
package org.mule.runtime.http.support.internal;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class HttpProtocolCompatibilityTestCase {
  @Test void http1CallbackIsInvoked() {
    AtomicBoolean invoked = new AtomicBoolean();
    HttpProtocolCompatibility.http1(config -> { config.setEnabled(true); invoked.set(true); });
    assertTrue(invoked.get());
  }
  @Test void onlySupportedProtocolCannotBeDisabled() {
    assertThrows(IllegalArgumentException.class, () -> HttpProtocolCompatibility.http1(config -> config.setEnabled(false)));
  }
  @Test void explicitHttp2DisabledIsSupported() {
    AtomicBoolean invoked = new AtomicBoolean();
    HttpProtocolCompatibility.http2(config -> { config.setEnabled(false); invoked.set(true); });
    assertTrue(invoked.get());
  }
  @Test void http2DoesNotSilentlyFallBack() {
    assertThrows(UnsupportedOperationException.class, () -> HttpProtocolCompatibility.http2(config -> config.setEnabled(true)));
  }
  @Test void unsupportedHttp2SettingsAreNotIgnored() {
    assertThrows(UnsupportedOperationException.class, () -> HttpProtocolCompatibility.http2(config -> config.setHeaderTableSize(1L)));
    assertThrows(UnsupportedOperationException.class, () -> HttpProtocolCompatibility.http2(config -> config.setMaxConcurrentStreams(1L)));
    assertThrows(UnsupportedOperationException.class, () -> HttpProtocolCompatibility.http2(config -> config.setInitialWindowSize(1)));
    assertThrows(UnsupportedOperationException.class, () -> HttpProtocolCompatibility.http2(config -> config.setMaxFrameSize(1)));
    assertThrows(UnsupportedOperationException.class, () -> HttpProtocolCompatibility.http2(config -> config.setMaxHeaderListSize(1L)));
  }
  @Test void nullCallbacksFail() {
    assertThrows(NullPointerException.class, () -> HttpProtocolCompatibility.http1(null));
    assertThrows(NullPointerException.class, () -> HttpProtocolCompatibility.http2(null));
  }
}
'''
save(BASE/'src/test/java/org/mule/runtime/http/support/internal/HttpProtocolCompatibilityTestCase.java',test,'Protocol capability regression assertions')
(OUT/'http-sdk-adaptation.json').write_text(json.dumps({'sdk':'org.mule.sdk:mule-sdk-api:0.12.0','sdk_jar_sha256':'d75a183fbac1558d8b094e2d4d4f7564401529e9bc7ffde4165d88fa971a846f','reason':'Exact old snapshot artifacts returned 404; released SDK renamed configuration interfaces and added protocol switches. Source HTTP API supports up to HTTP/1.1. Preserve it and reject new unsupported protocols explicitly.','changes':records,'test_execution':'NOT YET RUN'},indent=2)+'\n')
print('HTTP compatibility changes:',len(records))
