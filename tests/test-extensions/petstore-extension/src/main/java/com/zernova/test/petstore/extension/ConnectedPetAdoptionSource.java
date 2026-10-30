/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import static java.util.Comparator.naturalOrder;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataScope;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.PollContext;

@MetadataScope(outputResolver = PollingSourceMetadataResolver.class)
@MediaType(TEXT_PLAIN)
public class ConnectedPetAdoptionSource extends PetAdoptionSource {

  @Connection
  private ConnectionProvider<PetStoreClient> fileSystemProvider;

  @Override
  protected void doStop() {}

  @Override
  public void poll(PollContext<String, Void> pollContext) {
    pollContext.setWatermarkComparator(naturalOrder());
    pets.stream()
        .map(p -> Result.<String, Void>builder().output(p).build())
        .forEach(result -> pollContext.accept(item -> {
          try {
            item.getSourceCallbackContext().bindConnection(fileSystemProvider.connect());
          } catch (Exception e) {
            // You will never get here
          }
          item.setResult(result);

          if (idempotent) {
            item.setId(result.getOutput());
          }

          if (watermark) {
            item.setWatermark(pets.indexOf(result.getOutput()));
          }
        }));
  }
}
