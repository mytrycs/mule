#!/usr/bin/env python3
# Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
"""Retain engine multipart behavior across the narrower published HTTP SDK."""
from pathlib import Path
import hashlib,json,re
ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'modules/http-support'
OUT=ROOT/'zernova-build-evidence'; OUT.mkdir(exist_ok=True)
PKG='org.mule.runtime.http.support.internal.message'
records=[]
header=' * ZERNOVA compatibility adaptation Copyright (c) 2026 ZerNova. CPAL-1.0.\n'
def save(path,text,why):
    old=path.read_bytes() if path.exists() else b''
    if old==text.encode():return
    if old and text.startswith('/*\n') and header not in text:text=text.replace('/*\n','/*\n'+header,1)
    path.parent.mkdir(parents=True,exist_ok=True);path.write_text(text)
    records.append({'path':str(path.relative_to(ROOT)),'reason':why,'before_sha256':hashlib.sha256(old).hexdigest(),'after_sha256':hashlib.sha256(text.encode()).hexdigest(),'license':'CPAL-1.0'})
for path in BASE.rglob('*.java'):
    if 'target' in path.relative_to(BASE).parts:continue
    text=path.read_text()
    text=text.replace('org.mule.sdk.api.http.domain.entity.multipart.Part',PKG+'.Part')
    if 'org.mule.sdk.api.http.server.EndpointAvailabilityHandler' in text:
        text=text.replace('org.mule.sdk.api.http.server.EndpointAvailabilityHandler','org.mule.sdk.api.http.server.EndpointLifecycleHandler')
        text=re.sub(r'(?<![\w.])EndpointAvailabilityHandler\b','EndpointLifecycleHandler',text)
    save(path,text,'Use released SDK lifecycle name; keep multipart view as an internal engine interface, not a forged SDK type')
part='''/*
 * Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
 * Internal bridge view. This is not an implementation of a nonexistent SDK API.
 */
package org.mule.runtime.http.support.internal.message;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
public interface Part {
  InputStream getInputStream() throws IOException;
  String getContentType();
  String getName();
  long getSize();
  String getHeader(String name);
  Collection<String> getHeaders(String name);
  Collection<String> getHeaderNames();
  String getFileName();
}
'''
save(BASE/'src/main/java/org/mule/runtime/http/support/internal/message/Part.java',part,'Retain full original multipart accessor contract within the engine bridge')
view='''/*
 * Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
 * The published SDK exposes bytes/streams; this internal extension preserves
 * structured parts when an engine entity makes a round trip through that SDK.
 */
package org.mule.runtime.http.support.internal.message;
import java.io.IOException;
import java.util.Collection;
public interface MultipartEntityView extends org.mule.sdk.api.http.domain.entity.HttpEntity {
  boolean isComposed();
  Collection<Part> getParts() throws IOException;
}
'''
save(BASE/'src/main/java/org/mule/runtime/http/support/internal/message/MultipartEntityView.java',view,'Preserve structured engine multipart data without pretending the SDK still exposes parts')
p=BASE/'src/main/java/org/mule/runtime/http/support/internal/message/muletosdk/HttpEntityWrapper.java'
t=p.read_text().replace('implements HttpEntity {','implements '+PKG+'.MultipartEntityView {')
save(p,t,'Expose engine multipart view alongside the actual published SDK interface')
p=BASE/'src/main/java/org/mule/runtime/http/support/internal/message/sdktomule/HttpEntityWrapper.java'
t=p.read_text().replace('return sdkEntity.isComposed();','return sdkEntity instanceof '+PKG+'.MultipartEntityView view && view.isComposed();')
old='return sdkEntity.getParts().stream().map(HttpPartWrapper::new).collect(toUnmodifiableList());'
assert old in t
t=t.replace(old,'''if (sdkEntity instanceof '''+PKG+'''.MultipartEntityView view) {
      return view.getParts().stream().map(HttpPartWrapper::new).collect(toUnmodifiableList());
    }
    // The published SDK has no structured multipart accessors; retain its raw
    // entity content unchanged. Non-composed engine entities return no parts.
    return java.util.List.of();''')
save(p,t,'Round-trip composed entities intact; ordinary SDK entities preserve their raw content')
# Existing structured-part tests continue against the actual internal view.
p=BASE/'src/test/java/org/mule/runtime/http/support/internal/message/sdktomule/HttpEntityWrapperTestCase.java'
t=p.read_text().replace('import org.mule.sdk.api.http.domain.entity.HttpEntity;', 'import '+PKG+'.MultipartEntityView;')
t=t.replace('private HttpEntity sdkEntity;', 'private MultipartEntityView sdkEntity;')
extra='''
  @Test
  void publishedSdkEntityRetainsRawContentWithoutInventedParts() throws IOException {
    org.mule.sdk.api.http.domain.entity.HttpEntity plain = mock(org.mule.sdk.api.http.domain.entity.HttpEntity.class);
    byte[] content = "raw multipart or ordinary body".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    when(plain.getBytes()).thenReturn(content);
    HttpEntityWrapper actual = new HttpEntityWrapper(plain);
    assertThat(actual.isComposed(), is(false));
    assertThat(actual.getParts(), hasSize(0));
    assertThat(actual.getBytes(), is(content));
  }
'''
i=t.rfind('}');t=t[:i]+extra+t[i:]
save(p,t,'Retain multipart regression assertions and add published-SDK raw-content preservation test')
# Keep old convenience methods and implement the new overloaded factory methods.
p=BASE/'src/main/java/org/mule/runtime/http/support/internal/message/HttpEntityFactoryImpl.java';t=p.read_text()
for method in ['fromByteArray','fromString','fromInputStream']:
    t=re.sub(r'  @Override\n(  public HttpEntity '+method+r'\()',r'\1',t)
extra='''
  @Override public HttpEntity from(byte[] content) { return fromByteArray(content); }
  @Override public HttpEntity from(String content, Charset charset) { return fromString(content, charset); }
  @Override public HttpEntity from(InputStream content) { return fromInputStream(content); }
  @Override public HttpEntity from(InputStream content, Long length) { return fromInputStream(content, length); }
'''
i=t.rfind('}');t=t[:i]+extra+t[i:]
save(p,t,'Preserve old helpers and implement released SDK factory overloads by delegation')
p=BASE/'src/main/java/org/mule/runtime/http/support/internal/server/HttpServerWrapper.java';t=p.read_text()
old='public void start() throws IOException {\n    delegate.start();\n  }';assert old in t
t=t.replace(old,'''public void start() throws org.mule.runtime.api.exception.MuleException {
    try {
      delegate.start();
    } catch (IOException failure) {
      throw new org.mule.runtime.api.exception.DefaultMuleException("ZERNOVA HTTP server could not start", failure);
    }
  }''')
save(p,t,'Conform to SDK lifecycle exception while preserving the actual start failure as cause')
p=BASE/'src/test/java/org/mule/runtime/http/support/internal/server/HttpServerWrapperTestCase.java';t=p.read_text().replace('void start() throws IOException {','void start() throws org.mule.runtime.api.exception.MuleException, IOException {')
extra='''
  @Test
  void startupFailureRetainsIoCause() throws Exception {
    IOException failure = new IOException("listener unavailable");
    org.mockito.Mockito.doThrow(failure).when(delegateServer).start();
    org.mule.runtime.api.exception.MuleException error = org.junit.jupiter.api.Assertions.assertThrows(
        org.mule.runtime.api.exception.MuleException.class, () -> wrapper.start());
    org.junit.jupiter.api.Assertions.assertSame(failure, error.getCause());
  }
'''
i=t.rfind('}');t=t[:i]+extra+t[i:]
save(p,t,'Test released lifecycle contract and preserved I/O error cause')
(OUT/'http-entity-adaptation.json').write_text(json.dumps({'changes':records,'scope':'Full native multipart and HTTP/1.1 retained. New SDK exposes fewer structured-body methods; internal view preserves native round trips. No vendor API stubs or engine feature deletion.','tests':'NOT YET EXECUTED'},indent=2)+'\n')
print('Entity/lifecycle compatibility changes:',len(records))
