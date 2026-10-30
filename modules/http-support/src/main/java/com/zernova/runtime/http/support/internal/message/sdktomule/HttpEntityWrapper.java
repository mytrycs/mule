/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.http.support.internal.message.sdktomule;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toUnmodifiableList;

import com.zernova.runtime.http.api.domain.entity.HttpEntity;
import com.zernova.runtime.http.api.domain.entity.multipart.HttpPart;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.OptionalLong;

public class HttpEntityWrapper implements HttpEntity {

  private final com.zernova.sdk.api.http.domain.entity.HttpEntity sdkEntity;

  public HttpEntityWrapper(com.zernova.sdk.api.http.domain.entity.HttpEntity sdkEntity) {
    this.sdkEntity = sdkEntity;
  }

  @Override
  public boolean isStreaming() {
    return sdkEntity.isStreaming();
  }

  @Override
  public boolean isComposed() {
    return sdkEntity instanceof com.zernova.runtime.http.support.internal.message.MultipartEntityView view && view.isComposed();
  }

  @Override
  public InputStream getContent() {
    return sdkEntity.getContent();
  }

  @Override
  public byte[] getBytes() throws IOException {
    return sdkEntity.getBytes();
  }

  @Override
  public Collection<HttpPart> getParts() throws IOException {
    if (sdkEntity instanceof com.zernova.runtime.http.support.internal.message.MultipartEntityView view) {
      return view.getParts().stream().map(HttpPartWrapper::new).collect(toUnmodifiableList());
    }
    // The published SDK has no structured multipart accessors; retain its raw
    // entity content unchanged. Non-composed engine entities return no parts.
    return java.util.List.of();
  }

  @Override
  public Optional<Long> getLength() {
    OptionalLong result = sdkEntity.getBytesLength();
    return result.isPresent() ? of(result.getAsLong()) : empty();
  }

  @Override
  public OptionalLong getBytesLength() {
    return sdkEntity.getBytesLength();
  }
}
