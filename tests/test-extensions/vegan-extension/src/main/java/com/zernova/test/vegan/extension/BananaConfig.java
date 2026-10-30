/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.vegan.extension;

import static com.zernova.test.vegan.extension.VeganExtension.BANANA;
import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.Sources;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.metadata.OutputResolver;
import com.zernova.runtime.extension.api.annotation.param.NullSafe;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration(name = BANANA)
@Operations({EatBananaOperation.class, SpreadVeganismOperation.class, FlowListenerBananaOperations.class,
    FruitOperationsWithConfigOverride.class})
@ConnectionProviders(VeganBananaConnectionProvider.class)
@Sources(PaulMcCartneySource.class)
public class BananaConfig extends EasyToEatConfig {

  private final AtomicInteger bananasCount = new AtomicInteger(0);
  private final AtomicInteger nonBananasCount = new AtomicInteger(0);
  private final AtomicInteger exceptionCount = new AtomicInteger(0);

  @Parameter
  @Optional
  @NullSafe
  private HealthyFood healthyFood;

  @OutputResolver(output = FruitMetadataResolver.class)
  public HealthyFood getHealthyFood() {
    return healthyFood;
  }

  public int getBananasCount() {
    return bananasCount.get();
  }

  public int onBanana() {
    return bananasCount.incrementAndGet();
  }

  public int getNonBananasCount() {
    return nonBananasCount.get();
  }

  public int onNotBanana() {
    return nonBananasCount.incrementAndGet();
  }

  public int getExceptionCount() {
    return exceptionCount.get();
  }

  public int onException() {
    return exceptionCount.incrementAndGet();
  }

}
