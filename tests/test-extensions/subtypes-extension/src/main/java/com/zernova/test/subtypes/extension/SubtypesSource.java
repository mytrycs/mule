/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.subtypes.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;
import com.zernova.tck.message.StringAttributes;

@MediaType(TEXT_PLAIN)
public class SubtypesSource extends Source<String, StringAttributes> {

  @Parameter
  public Door doorParam;

  @Override
  public void onStart(SourceCallback<String, StringAttributes> sourceCallback) throws ZernovaException {
    if (doorParam == null) {
      throw new RuntimeException("Door was null");
    }
    if (!(doorParam instanceof HouseDoor)) {
      throw new RuntimeException("Expected HouseDoor");
    }
    if (((HouseDoor) doorParam).isLocked()) {
      throw new RuntimeException("Expected unlocked door");
    }
  }

  @Override
  public void onStop() {

  }
}
