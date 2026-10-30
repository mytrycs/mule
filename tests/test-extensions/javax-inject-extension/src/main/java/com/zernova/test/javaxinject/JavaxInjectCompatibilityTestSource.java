/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.javaxinject;

import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.sdk.api.annotation.Alias;
import com.zernova.sdk.api.runtime.operation.Result;
import com.zernova.sdk.api.runtime.source.Source;
import com.zernova.sdk.api.runtime.source.SourceCallback;

import javax.inject.Inject;

@Alias("Source")
public class JavaxInjectCompatibilityTestSource extends Source<String, String> {

  @Config
  private JavaxInjectCompatibilityTestConfiguration config;

  @Inject
  private ArtifactEncoding encoding;

  @Override
  public void onStart(SourceCallback<String, String> sourceCallback) throws ZernovaException {
    sourceCallback.handle(Result.<String, String>builder()
        .output(encoding.getDefaultEncoding().name())
        .attributes(config.getEncoding().getDefaultEncoding().name())
        .build());

  }

  @Override
  public void onStop() {
    // nothing to do
  }

}
