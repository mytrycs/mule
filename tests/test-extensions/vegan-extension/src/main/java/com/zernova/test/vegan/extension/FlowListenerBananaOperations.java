/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;

import com.zernova.runtime.extension.api.annotation.metadata.OutputResolver;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.runtime.operation.FlowListener;
import com.zernova.tck.testmodels.fruit.Banana;
import com.zernova.tck.testmodels.fruit.Fruit;

public class FlowListenerBananaOperations {

  @OutputResolver(output = FruitMetadataResolver.class)
  public Fruit getLunch(@Config BananaConfig config, FlowListener listener) {
    final Banana banana = new Banana();
    listener.onSuccess(message -> {
      if (message.getPayload().getValue() instanceof Banana) {
        config.onBanana();
      } else {
        config.onNotBanana();
      }
    });

    listener.onError(exception -> config.onException());
    listener.onComplete(() -> banana.peel());

    return banana;
  }

  @OutputResolver(output = FruitMetadataResolver.class)
  public Fruit SdkGetLunch(@Config BananaConfig config, com.zernova.sdk.api.runtime.operation.FlowListener listener) {
    final Banana banana = new Banana();
    listener.onSuccess(message -> {
      if (message.getPayload().getValue() instanceof Banana) {
        config.onBanana();
      } else {
        config.onNotBanana();
      }
    });

    listener.onError(exception -> config.onException());
    listener.onComplete(() -> banana.peel());

    return banana;
  }

}
