/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.persistence;

import com.zernova.runtime.api.meta.ZernovaVersion;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * {@link TypeAdapter} implementation which serializes {@link ZernovaVersion} instances
 *
 * @since 1.0
 */
public class ZernovaVersionTypeAdapter extends TypeAdapter<ZernovaVersion> {

  @Override
  public void write(JsonWriter out, ZernovaVersion muleVersion) throws IOException {
    out.value(muleVersion.toCompleteNumericVersion());
  }

  @Override
  public ZernovaVersion read(JsonReader in) throws IOException {
    return new ZernovaVersion(in.nextString());
  }
}
