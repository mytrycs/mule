/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.http.support.internal.message.muletosdk;

import static java.util.stream.Collectors.toUnmodifiableList;

import com.zernova.sdk.api.http.domain.entity.HttpEntity;
import com.zernova.runtime.http.support.internal.message.Part;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.OptionalLong;

public class HttpEntityWrapper implements com.zernova.runtime.http.support.internal.message.MultipartEntityView {

  private final com.zernova.runtime.http.api.domain.entity.HttpEntity muleEntity;

  public HttpEntityWrapper(com.zernova.runtime.http.api.domain.entity.HttpEntity muleEntity) {
    this.muleEntity = muleEntity;
  }

  @Override
  public boolean isStreaming() {
    return muleEntity.isStreaming();
  }

  @Override
  public boolean isComposed() {
    return muleEntity.isComposed();
  }

  @Override
  public InputStream getContent() {
    return muleEntity.getContent();
  }

  @Override
  public byte[] getBytes() throws IOException {
    return muleEntity.getBytes();
  }

  @Override
  public Collection<Part> getParts() throws IOException {
    return muleEntity.getParts().stream().map(HttpPartWrapper::new).collect(toUnmodifiableList());
  }

  @Override
  public OptionalLong getBytesLength() {
    return muleEntity.getBytesLength();
  }
}
