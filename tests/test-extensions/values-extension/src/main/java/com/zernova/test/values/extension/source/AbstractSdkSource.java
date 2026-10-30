/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension.source;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.sdk.api.runtime.source.Source;
import com.zernova.sdk.api.runtime.source.SourceCallback;

public class AbstractSdkSource extends Source<String, String> {

  @Override
  public void onStart(SourceCallback<String, String> sourceCallback) throws ZernovaException {

  }

  @Override
  public void onStop() {

  }
}
