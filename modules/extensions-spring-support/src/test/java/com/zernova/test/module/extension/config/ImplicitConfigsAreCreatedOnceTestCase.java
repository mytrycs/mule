/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.config;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import org.junit.Test;

import java.util.Collection;
import java.util.List;

public class ImplicitConfigsAreCreatedOnceTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "implicit-configs-are-created-once.xml";
  }

  @Test
  public void tooManyConfigsTestCase() throws Exception {
    Integer value = (Integer) flowRunner("implicitConfig").run().getMessage().getPayload().getValue();
    assertThat(value, is(5));
    Collection<ConfigurationProvider> configs = registry.lookupAllByType(ConfigurationProvider.class);

    assertThat(configs, hasSize(5));
    List<String> names = configs.stream().map(ConfigurationProvider::getName).collect(toList());
    assertThat(names, hasItems("implicitExclusive-bleconf-implicit",
                               "implicitExclusive-blaconf-implicit",
                               "blaexplicit",
                               "blaexplicit2",
                               "bleexplicit"));
  }
}
