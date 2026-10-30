/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataScope;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;

@MetadataScope(outputResolver = PetstoreMetadataResolver.class)
public class SentientSource extends Source<ComponentLocation, Void> {

  public static ComponentLocation capturedLocation;

  private ComponentLocation location;

  @Override
  public void onStart(SourceCallback<ComponentLocation, Void> sourceCallback) throws ZernovaException {
    capturedLocation = location;
  }

  @Override
  public void onStop() {
    capturedLocation = null;
  }
}
