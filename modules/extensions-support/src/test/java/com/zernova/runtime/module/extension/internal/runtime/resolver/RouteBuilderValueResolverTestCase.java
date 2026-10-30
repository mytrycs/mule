/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.module.extension.internal.runtime.objectbuilder.ObjectBuilder;
import com.zernova.tck.size.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class RouteBuilderValueResolverTestCase {

  @Mock
  private ObjectBuilder objectBuilder;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ZernovaContext muleContext;

  @Mock
  private MessageProcessorChain chain;

  @Test
  public void routeInitialiseIsPropagatedToItsChain() throws ZernovaException {
    RouteBuilderValueResolver routeBuilder = new RouteBuilderValueResolver(objectBuilder, muleContext, chain);
    routeBuilder.initialise();
    verify(chain, times(1)).initialise();
  }

  @Test
  public void routeStartIsPropagatedToItsChain() throws ZernovaException {
    RouteBuilderValueResolver routeBuilder = new RouteBuilderValueResolver(objectBuilder, muleContext, chain);
    routeBuilder.start();
    verify(chain, times(1)).start();
  }

  @Test
  public void routeStopIsPropagatedToItsChain() throws ZernovaException {
    RouteBuilderValueResolver routeBuilder = new RouteBuilderValueResolver(objectBuilder, muleContext, chain);
    routeBuilder.stop();
    verify(chain, times(1)).stop();
  }

  @Test
  public void routeDisposeIsPropagatedToItsChain() {
    RouteBuilderValueResolver routeBuilder = new RouteBuilderValueResolver(objectBuilder, muleContext, chain);
    routeBuilder.dispose();
    verify(chain, times(1)).dispose();
  }
}
