/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.PollContext;
import com.zernova.runtime.extension.api.runtime.source.PollingSource;
import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

@Alias("pet-filter-source")
@MediaType(TEXT_PLAIN)
public class PetFilterPollingSource extends PollingSource<String, Void> {

  private static final String TIGER = "tiger";
  private static final String WHALE = "whale";
  private static final String DINOSAUR = "dinosaur";

  @Parameter
  private String filter;

  @Override
  protected void doStart() {}

  @Override
  protected void doStop() {}

  @Override
  public void poll(PollContext<String, Void> pollContext) {
    if (TIGER.equalsIgnoreCase(filter)) {
      throw new RuntimeException(new ConnectionException("A tiger cannot be petted."));
    }
    if (WHALE.equalsIgnoreCase(filter)) {
      throw new RuntimeException("Why do you want to pet a whale?");
    }
    if (DINOSAUR.equalsIgnoreCase(filter)) {
      pollContext.onConnectionException(new ConnectionException("Dinosaurs no longer exist."));
    }
  }

  @Override
  public void onRejectedItem(Result result, SourceCallbackContext callbackContext) {}
}
