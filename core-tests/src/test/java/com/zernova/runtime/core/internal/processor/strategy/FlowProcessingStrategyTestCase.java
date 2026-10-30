/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor.strategy;

import static com.zernova.runtime.api.component.AbstractComponent.LOCATION_KEY;
import static com.zernova.runtime.core.api.construct.Flow.builder;
import static com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation.from;
import static com.zernova.tck.ZernovaTestUtils.testWithSystemProperty;
import static com.zernova.tck.util.ZernovaContextUtils.mockContextWithServices;

import static java.util.Collections.singletonMap;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.DefaultZernovaConfiguration;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.construct.Flow;
import com.zernova.runtime.core.api.construct.Flow.Builder;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategy;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategyFactory;
import com.zernova.runtime.core.privileged.registry.RegistrationException;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@SmallTest
public class FlowProcessingStrategyTestCase extends AbstractZernovaTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  protected ZernovaContext muleContext = mockContextWithServices();

  @Mock
  private ZernovaConfiguration configuration;

  private Flow flow;

  @Before
  public void before() throws RegistrationException {
    when(muleContext.getConfiguration()).thenReturn(configuration);
    createFlow(null);
  }

  @Test
  public void fixedProcessingStrategyIsHonoured() throws Exception {
    ProcessingStrategyFactory processingStrategyFactory = mock(ProcessingStrategyFactory.class);
    ProcessingStrategy processingStrategy = mock(ProcessingStrategy.class);
    when(processingStrategyFactory.create(any(), any())).thenReturn(processingStrategy);
    createFlow(processingStrategyFactory);
    flow.initialise();

    assertThat(flow.getProcessingStrategy(), is(sameInstance(processingStrategy)));
  }

  @Test
  public void defaultProcessingStrategyInConfigIsHonoured() throws Exception {
    ProcessingStrategy processingStrategy = mock(ProcessingStrategy.class);
    ProcessingStrategyFactory processingStrategyFactory = mock(ProcessingStrategyFactory.class);
    when(processingStrategyFactory.create(any(), any())).thenReturn(processingStrategy);
    when(configuration.getDefaultProcessingStrategyFactory()).thenReturn(processingStrategyFactory);

    createFlow(null);
    flow.initialise();
    assertThat(flow.getProcessingStrategy(), is(sameInstance(processingStrategy)));
  }

  @Test
  public void fixedProcessingStrategyTakesPrecedenceOverConfig() throws Exception {
    ProcessingStrategyFactory processingStrategyFactory = mock(ProcessingStrategyFactory.class);
    ProcessingStrategy processingStrategy = mock(ProcessingStrategy.class);
    when(processingStrategyFactory.create(any(), any())).thenReturn(processingStrategy);
    createFlow(processingStrategyFactory);
    flow.initialise();

    assertThat(flow.getProcessingStrategy(), is(sameInstance(processingStrategy)));
  }

  @Test
  public void createDefaultProcessingStrategyIfNoneSpecified() throws Exception {
    flow.initialise();
    assertThat(flow.getProcessingStrategy(),
               is(instanceOf(new TransactionAwareStreamEmitterProcessingStrategyFactory()
                   .getProcessingStrategyType())));
  }

  @Test
  public void processingStrategySetBySystemPropertyOverridesDefault() throws Exception {
    testWithSystemProperty(ProcessingStrategyFactory.class.getName(),
                           TestProcessingStrategyFactory.class.getName(), () -> {
                             ZernovaConfiguration muleConfiguration = new DefaultZernovaConfiguration();
                             when(muleContext.getConfiguration()).thenReturn(muleConfiguration);
                             createFlow(null);
                             assertEquals(flow.getProcessingStrategy().getClass(), TestProcessingStrategy.class);
                           });
  }


  public static class TestProcessingStrategyFactory extends AbstractProcessingStrategyFactory {

    public TestProcessingStrategyFactory() {}

    @Override
    public ProcessingStrategy create(ZernovaContext muleContext, String schedulersNamePrefix) {
      return new TestProcessingStrategy();
    }
  }

  private static class TestProcessingStrategy extends AbstractProcessingStrategy {
  }

  private void createFlow(ProcessingStrategyFactory configProcessingStrategyFactory) {
    Builder flowBuilder = builder("test", muleContext);
    if (configProcessingStrategyFactory != null) {
      flowBuilder = flowBuilder.processingStrategyFactory(configProcessingStrategyFactory);
    }

    flow = flowBuilder.build();
    flow.setAnnotations(singletonMap(LOCATION_KEY, from("flow")));
  }
}
