/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.DefaultEncoding;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;
import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Alias("pet-source-stream")
@MediaType(value = TEXT_PLAIN, strict = false)
public class PetStoreStreamSource extends Source<InputStream, Object> {

  @ParameterGroup(name = "Breeder")
  private ExclusivePetBreeder breeder;

  @com.zernova.sdk.api.annotation.param.DefaultEncoding
  private String encoding;

  private int counter = 0;

  @Override
  public void onStart(SourceCallback<InputStream, Object> sourceCallback) throws ZernovaException {
    SourceCallbackContext context = sourceCallback.createContext();
    context.setCorrelationId(breeder.getBirds());
    sourceCallback.handle(Result.<InputStream, Object>builder().output(new ByteArrayInputStream(encoding.getBytes())).build(),
                          context);
  }

  @Override
  public void onStop() {}
}
