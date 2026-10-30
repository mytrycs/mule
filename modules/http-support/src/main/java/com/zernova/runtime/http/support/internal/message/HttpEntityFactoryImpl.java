/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.http.support.internal.message;

import static java.util.OptionalLong.empty;
import static java.util.OptionalLong.of;

import com.zernova.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import com.zernova.runtime.http.api.domain.entity.EmptyHttpEntity;
import com.zernova.runtime.http.api.domain.entity.InputStreamHttpEntity;
import com.zernova.runtime.http.support.internal.message.muletosdk.HttpEntityWrapper;
import com.zernova.sdk.api.http.domain.entity.HttpEntity;
import com.zernova.sdk.api.http.domain.entity.HttpEntityFactory;

import java.io.InputStream;
import java.nio.charset.Charset;

public class HttpEntityFactoryImpl implements HttpEntityFactory {

  public HttpEntity fromByteArray(byte[] content) {
    return new HttpEntityWrapper(new ByteArrayHttpEntity(content));
  }

  public HttpEntity fromString(String content, Charset charset) {
    return new HttpEntityWrapper(new ByteArrayHttpEntity(content.getBytes(charset)));
  }

  public HttpEntity fromInputStream(InputStream content) {
    return new HttpEntityWrapper(new InputStreamHttpEntity(content));
  }

  public HttpEntity fromInputStream(InputStream inputStream, Long contentLength) {
    return new HttpEntityWrapper(new InputStreamHttpEntity(inputStream, contentLength == null ? empty() : of(contentLength)));
  }

  @Override
  public HttpEntity emptyEntity() {
    return new HttpEntityWrapper(new EmptyHttpEntity());
  }

  @Override public HttpEntity from(byte[] content) { return fromByteArray(content); }
  @Override public HttpEntity from(String content, Charset charset) { return fromString(content, charset); }
  @Override public HttpEntity from(InputStream content) { return fromInputStream(content); }
  @Override public HttpEntity from(InputStream content, Long length) { return fromInputStream(content, length); }
}
