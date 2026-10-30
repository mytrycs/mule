/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.profiling.discovery;

import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertThat;
import static com.zernova.test.allure.AllureConstants.Profiling.PROFILING;
import static com.zernova.test.allure.AllureConstants.Profiling.ProfilingServiceStory.DEFAULT_PROFILING_SERVICE;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import com.zernova.runtime.api.profiling.ProfilingDataConsumer;
import com.zernova.runtime.api.profiling.ProfilingDataConsumerDiscoveryStrategy;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

@SmallTest
@Feature(PROFILING)
@Story(DEFAULT_PROFILING_SERVICE)
public class CompositeProfilingDataConsumerDiscoveryStrategyTestCase extends AbstractZernovaTestCase {

  private final ProfilingDataConsumer<?> profilingDataConsumerOne = mock(ProfilingDataConsumer.class);
  private final ProfilingDataConsumer<?> profilingDataConsumerTwo = mock(ProfilingDataConsumer.class);
  private final ProfilingDataConsumerDiscoveryStrategy discoveryStrategyOne = mock(ProfilingDataConsumerDiscoveryStrategy.class);
  private final ProfilingDataConsumerDiscoveryStrategy discoveryStrategyTwo = mock(ProfilingDataConsumerDiscoveryStrategy.class);
  private final Set<ProfilingDataConsumerDiscoveryStrategy> discoveryStrategies =
      new HashSet<ProfilingDataConsumerDiscoveryStrategy>() {

        {
          add(discoveryStrategyOne);
          add(discoveryStrategyTwo);
        }
      };

  @Before
  public void setUp() {
    when(discoveryStrategyOne.discover()).thenAnswer(invocation -> singleton(profilingDataConsumerOne));
    when(discoveryStrategyTwo.discover()).thenAnswer(invocation -> singleton(profilingDataConsumerTwo));
  }

  @Test
  public void testValues() {
    CompositeProfilingDataConsumerDiscoveryStrategy compositeDiscoveryStrategy =
        new CompositeProfilingDataConsumerDiscoveryStrategy(discoveryStrategies);
    assertThat(compositeDiscoveryStrategy.discover(), containsInAnyOrder(profilingDataConsumerOne, profilingDataConsumerTwo));
  }

}
