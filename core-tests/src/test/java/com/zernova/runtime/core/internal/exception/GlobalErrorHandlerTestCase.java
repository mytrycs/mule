/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.exception;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.tck.util.ZernovaContextUtils.mockContextWithServices;
import static com.zernova.test.allure.AllureConstants.ErrorHandlingFeature.ERROR_HANDLING;
import static com.zernova.test.allure.AllureConstants.ErrorHandlingFeature.ErrorHandlingStory.GLOBAL_ERROR_HANDLER;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.exception.ErrorHandlerTestCase.DefaultMessagingExceptionHandlerAcceptor;
import com.zernova.runtime.core.privileged.exception.TemplateOnErrorHandler;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@SmallTest
@Issue("W-11117613")
@Feature(ERROR_HANDLING)
@Story(GLOBAL_ERROR_HANDLER)
public class GlobalErrorHandlerTestCase extends AbstractZernovaTestCase {

  private GlobalErrorHandler globalErrorHandler;

  private final TemplateOnErrorHandler onErrorHandler = mock(TemplateOnErrorHandler.class);

  private final DefaultMessagingExceptionHandlerAcceptor defaultMessagingExceptionHandler =
      spy(new DefaultMessagingExceptionHandlerAcceptor());

  private final ZernovaContext mockMuleContext = mockContextWithServices();

  @Before
  public void setUp() throws Exception {
    globalErrorHandler = new GlobalErrorHandler();
    when(onErrorHandler.isInitialised()).thenReturn(true);
    globalErrorHandler.setExceptionListeners(new ArrayList<>(asList(onErrorHandler)));
    when(mockMuleContext.getDefaultErrorHandler(empty())).thenReturn(defaultMessagingExceptionHandler);
    globalErrorHandler.setMuleContext(mockMuleContext);
    globalErrorHandler.setRootContainerName("root");
  }

  @Test
  public void globalErrorHandlerInitialiseOnce() throws Exception {
    initialiseIfNeeded(globalErrorHandler, mockMuleContext);
    initialiseIfNeeded(globalErrorHandler, mockMuleContext);

    verify(onErrorHandler, times(1)).initialise();
  }

  @Test
  public void globalErrorHandlerStartOnce() throws Exception {
    startIfNeeded(globalErrorHandler);
    startIfNeeded(globalErrorHandler);

    verify(onErrorHandler, times(1)).start();
  }

  @Test
  public void globalErrorHandlerStopOnce() throws Exception {
    startIfNeeded(globalErrorHandler);
    startIfNeeded(globalErrorHandler);
    stopIfNeeded(globalErrorHandler);
    stopIfNeeded(globalErrorHandler);

    verify(onErrorHandler, times(1)).stop();
  }

  @Test
  public void globalErrorHandlerDisposeOnce() throws InitialisationException {
    initialiseIfNeeded(globalErrorHandler, mockMuleContext);
    disposeIfNeeded(globalErrorHandler, null);
    disposeIfNeeded(globalErrorHandler, null);

    verify(onErrorHandler, times(1)).dispose();
  }

  @Test
  public void sameRouterForChain() {
    MessageProcessorChain chain = mock(MessageProcessorChain.class);
    Supplier<Consumer<Exception>> errorRouterSupplier = () -> mock(ExceptionRouter.class);

    Consumer<Exception> errorRouter = globalErrorHandler.routerForChain(chain, errorRouterSupplier);
    Consumer<Exception> anotherErrorRouter = globalErrorHandler.routerForChain(chain, errorRouterSupplier);

    assertThat(errorRouter, is(sameInstance(anotherErrorRouter)));
  }

  @Test
  public void differentRouterForChains() {
    MessageProcessorChain chain = mock(MessageProcessorChain.class);
    MessageProcessorChain anotherChain = mock(MessageProcessorChain.class);
    Supplier<Consumer<Exception>> errorRouterSupplier = () -> mock(ExceptionRouter.class);

    Consumer<Exception> errorRouter = globalErrorHandler.routerForChain(chain, errorRouterSupplier);
    Consumer<Exception> anotherErrorRouter = globalErrorHandler.routerForChain(anotherChain, errorRouterSupplier);

    assertThat(errorRouter, is(not(sameInstance(anotherErrorRouter))));
  }

  @Test
  public void newRouterForChainIfCleared() {
    MessageProcessorChain chain = mock(MessageProcessorChain.class);
    Supplier<Consumer<Exception>> errorRouterSupplier = () -> mock(ExceptionRouter.class);

    Consumer<Exception> errorRouter = globalErrorHandler.routerForChain(chain, errorRouterSupplier);
    globalErrorHandler.clearRouterForChain(chain);
    Consumer<Exception> anotherErrorRouter = globalErrorHandler.routerForChain(chain, errorRouterSupplier);

    assertThat(errorRouter, is(not(sameInstance(anotherErrorRouter))));
  }

}
