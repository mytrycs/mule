/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.processor;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.api.component.location.ConfigurationComponentLocator.REGISTRY_KEY;
import static com.zernova.runtime.api.message.Message.of;
import static com.zernova.runtime.core.api.event.CoreEvent.builder;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.internal.event.DefaultEventContext.child;
import static com.zernova.runtime.core.api.config.ZernovaProperties.INTERCEPTOR_MANAGER_REGISTRY_KEY;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.newChain;
import static com.zernova.tck.ZernovaTestUtils.APPLE_FLOW;
import static com.zernova.tck.ZernovaTestUtils.createAndRegisterFlow;
import static com.zernova.test.allure.AllureConstants.RoutersFeature.ROUTERS;
import static com.zernova.test.allure.AllureConstants.RoutersFeature.ProcessorChainRouterStory.PROCESSOR_CHAIN_ROUTER;

import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.core.api.processor.ReactiveProcessor;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategy;
import com.zernova.runtime.core.internal.interception.InterceptorManager;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

// TODO MULE-13550 Improve CompositeProcessorChainRouter unit tests to cover scenario that was previously causing deadlock with flow-ref
@Feature(ROUTERS)
@Story(PROCESSOR_CHAIN_ROUTER)
public class CompositeProcessorChainRouterTestCase extends AbstractZernovaContextTestCase {

  private CompositeProcessorChainRouter chainRouter;
  private Scheduler scheduler;

  @Rule
  public ExpectedException expected = none();

  @Before
  public void setup() throws ZernovaException {
    scheduler = muleContext.getSchedulerService().ioScheduler();
    createAndRegisterFlow(muleContext, APPLE_FLOW, componentLocator);
  }

  @Override
  protected Map<String, Object> getStartUpRegistryObjects() {
    final Map<String, Object> objects = new HashMap<>();
    objects.put(REGISTRY_KEY, componentLocator);
    objects.put(INTERCEPTOR_MANAGER_REGISTRY_KEY, mock(InterceptorManager.class));
    return objects;
  }

  @After
  public void tearDown() throws ZernovaException {
    chainRouter.stop();
    chainRouter.dispose();
    scheduler.stop();
  }

  @Test
  @Description("Ensure that with simple chains both chains are executed consecutively with the result of the first chain being used for the second chain.")
  public void simpleChain() throws Exception {
    Message chain1Out = of(1);
    Message chain2Out = of(2);

    AtomicReference<Message> chain1In = new AtomicReference<>();
    AtomicReference<Message> chain2In = new AtomicReference<>();

    MessageProcessorChain chain1 = newChain(empty(), event -> {
      chain1In.set(event.getMessage());
      return builder(event).message(chain1Out).build();
    });
    MessageProcessorChain chain2 = newChain(empty(), event -> {
      chain2In.set(event.getMessage());
      return builder(event).message(chain2Out).build();
    });
    chainRouter = createCompositeProcessorChainRouter(chain1, chain2);

    Message result = chainRouter.execute(testEvent()).get().getMessage();

    assertThat(chain1In.get(), equalTo(testEvent().getMessage()));
    assertThat(chain2In.get(), equalTo(chain1Out));
    assertThat(result, equalTo(chain2Out));
  }

  @Test
  @Description("Ensure that when a child context is created as part of the execution of one of the composite chains then the chain does not complete and the next chains is not executed until the child context completes.")
  public void childContextChain() throws Exception {
    Latch latch = new Latch();
    AtomicReference<BaseEventContext> childEventContext = new AtomicReference<>();

    MessageProcessorChain chainUsingChildContext = newChain(empty(), event -> {
      latch.release();
      childEventContext.set(child((BaseEventContext) event.getContext(), empty()));
      return event;
    });
    chainRouter = createCompositeProcessorChainRouter(chainUsingChildContext, newChain(empty(), event -> event));

    // CompletableFuture is not returned immediately because simply invoking CompositeProcessorChainRouter there is no async
    // hand-off and so this blocks until child context completes.
    Future<CompletableFuture<Event>> future = scheduler.submit(() -> chainRouter.execute(testEvent()));

    latch.await();

    expected.expect(TimeoutException.class);
    try {
      future.get(BLOCK_TIMEOUT, MILLISECONDS);
    } finally {
      childEventContext.get().success();

      assertThat(future.get(BLOCK_TIMEOUT, MILLISECONDS).get().getMessage(), equalTo(testEvent().getMessage()));
    }
  }

  @Test
  @Description("Ensure that processing strategies can be applied to router chains")
  public void applyProcessingStrategyToChain() throws Exception {
    Message chainOut = of(1);
    ProcessingStrategy processingStrategyMock = mock(ProcessingStrategy.class);

    AtomicReference<Message> chainIn = new AtomicReference<>();

    MessageProcessorChain chain = newChain(empty(), event -> {
      chainIn.set(event.getMessage());
      return builder(event).message(chainOut).build();
    });
    when(processingStrategyMock.onPipeline(chain)).thenReturn(chain);

    chainRouter = new ProcessingStrategyChainRouter(processingStrategyMock);
    chainRouter.setProcessorChains(singletonList(chain));
    initialiseIfNeeded(chainRouter, muleContext);

    Message result = chainRouter.execute(testEvent()).get().getMessage();

    verify(processingStrategyMock).onPipeline(chain);
    assertThat(result, equalTo(chainOut));
  }

  private CompositeProcessorChainRouter createCompositeProcessorChainRouter(MessageProcessorChain chain1,
                                                                            MessageProcessorChain chain2)
      throws InitialisationException {
    CompositeProcessorChainRouter chainRouter = new CompositeProcessorChainRouter();
    chainRouter.setProcessorChains(asList(chain1, chain2));
    chainRouter.setMuleContext(muleContext);
    chainRouter.initialise();
    return chainRouter;
  }

  private static class ProcessingStrategyChainRouter extends CompositeProcessorChainRouter {

    private final ProcessingStrategy processingStrategy;

    ProcessingStrategyChainRouter(ProcessingStrategy processingStrategy) {
      this.processingStrategy = processingStrategy;
    }

    @Override
    protected List<? extends ReactiveProcessor> processorChainsToExecute(List<MessageProcessorChain> processorChains) {
      return processorChains.stream().map(chain -> processingStrategy.onPipeline(chain)).collect(toList());
    }
  }

}
