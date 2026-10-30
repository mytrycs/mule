/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.util.IOUtils;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.execution.OnError;
import com.zernova.runtime.extension.api.annotation.execution.OnSuccess;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataScope;
import com.zernova.runtime.extension.api.annotation.metadata.fixed.InputJsonType;
import com.zernova.runtime.extension.api.annotation.metadata.fixed.InputXmlType;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.source.EmitsResponse;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;
import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;
import com.zernova.tck.message.StringAttributes;
import com.zernova.test.metadata.extension.resolver.JavaOutputStaticTypeResolver;

import java.io.InputStream;

@Alias("custom-static-metadata")
@EmitsResponse
@MetadataScope(outputResolver = JavaOutputStaticTypeResolver.class)
@MediaType(value = "application/java", strict = false)
public class CustomStaticMetadataSource extends Source<Object, StringAttributes> {

  public static String onSuccessResult;
  public static String onErrorResult;

  @Override
  public void onStart(SourceCallback sourceCallback) throws ZernovaException {
    sourceCallback.handle(Result.builder().output("something").build());
  }

  @OnError
  public void onError(@InputJsonType(schema = "person-schema.json") InputStream person, SourceCallbackContext cc) {
    onErrorResult = IOUtils.toString(person);
  }

  @OnSuccess
  public void onSuccess(@InputXmlType(schema = "order.xsd", qname = "shiporder") InputStream order, SourceCallbackContext cc) {
    onSuccessResult = IOUtils.toString(order);
  }

  @Override
  public void onStop() {

  }
}
