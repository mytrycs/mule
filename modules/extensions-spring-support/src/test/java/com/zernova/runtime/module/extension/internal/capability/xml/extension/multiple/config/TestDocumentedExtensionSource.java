/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.capability.xml.extension.multiple.config;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.execution.OnError;
import com.zernova.runtime.extension.api.annotation.execution.OnSuccess;
import com.zernova.runtime.extension.api.annotation.execution.OnTerminate;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;

/**
 * TestDocumentedExtensionSource javadoc documentation
 */
@MediaType("application/json")
public class TestDocumentedExtensionSource extends Source<String, String> {

  /**
   * Source parameter 1
   */
  @Parameter
  private String source1;

  /**
   * Source Parameter group
   */
  @ParameterGroup(name = "Source group")
  private TestDocumentedParameterGroup group;

  @Override
  public void onStart(SourceCallback<String, String> sourceCallback) throws ZernovaException {

  }

  @Override
  public void onStop() {

  }

  /**
   * @param responseGroup Callback Doc of responseGroup
   */
  @OnSuccess
  public void onSuccess(@com.zernova.sdk.api.annotation.param.ParameterGroup(name = "Response group",
      showInDsl = true) TestDocumentedParameterGroup responseGroup) {

  }

  /**
   * @param errorParam Callback Doc of errorParam
   */
  @OnError
  public void onError(String errorParam) {

  }

  @OnTerminate
  public void doNothing() {}

}
