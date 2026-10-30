/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.config;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_TIME_SUPPLIER;
import static com.zernova.tck.ZernovaTestUtils.spyInjector;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.module.extension.internal.runtime.DefaultExecutionContext;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.tck.util.TestTimeSupplier;

import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractConfigurationProviderTestCase<T> extends AbstractZernovaContextTestCase {

  protected static final String CONFIG_NAME = "config";

  @Mock(lenient = true)
  protected ExtensionModel extensionModel;

  @Mock(answer = RETURNS_DEEP_STUBS, lenient = true)
  protected ConfigurationModel configurationModel;

  @Mock(answer = RETURNS_DEEP_STUBS, lenient = true)
  protected DefaultExecutionContext operationContext;

  @Mock(lenient = true)
  protected CoreEvent event;

  protected TestTimeSupplier timeSupplier = new TestTimeSupplier(System.currentTimeMillis());
  protected LifecycleAwareConfigurationProvider provider;

  @Override
  protected Map<String, Object> getStartUpRegistryObjects() {
    return singletonMap(OBJECT_TIME_SUPPLIER, timeSupplier);
  }

  @Before
  public void before() throws Exception {
    muleContext.getInjector().inject(provider);
    spyInjector(muleContext);
  }

  @Test
  public void getName() {
    assertThat(provider.getName(), is(CONFIG_NAME));
  }

  @Test
  public void getConfigurationModel() {
    assertThat(provider.getConfigurationModel(), is(CoreMatchers.sameInstance(configurationModel)));
  }

  protected void assertSameInstancesResolved() throws Exception {
    final int count = 10;
    Object config = provider.get(event);

    for (int i = 1; i < count; i++) {
      assertThat(provider.get(event), is(sameInstance(config)));
    }
  }
}
