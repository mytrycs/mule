/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.processor.chain.test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.api.component.AbstractComponent.LOCATION_KEY;
import static com.zernova.runtime.api.component.location.ConfigurationComponentLocator.REGISTRY_KEY;
import static com.zernova.runtime.api.message.Message.of;
import static com.zernova.runtime.core.api.construct.Flow.builder;
import static com.zernova.runtime.core.api.event.EventContextFactory.create;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.processor.ReactiveProcessor.ProcessingType.CPU_INTENSIVE;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.newChain;
import static com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation.from;
import static com.zernova.tck.ZernovaTestUtils.APPLE_FLOW;
import static com.zernova.tck.ZernovaTestUtils.createAndRegisterFlow;
import static com.zernova.tck.junit4.AbstractReactiveProcessorTestCase.Mode.BLOCKING;
import static com.zernova.tck.junit4.AbstractReactiveProcessorTestCase.Mode.NON_BLOCKING;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.construct.Flow;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategyFactory;
import com.zernova.runtime.core.api.util.ObjectUtils;
import com.zernova.runtime.core.internal.processor.strategy.BlockingProcessingStrategyFactory;
import com.zernova.runtime.core.internal.processor.strategy.DirectProcessingStrategyFactory;
import com.zernova.runtime.core.internal.processor.strategy.ProactorStreamEmitterProcessingStrategyFactory;
import com.zernova.runtime.core.internal.processor.strategy.StreamEmitterProcessingStrategyFactory;
import com.zernova.runtime.core.internal.processor.strategy.TransactionAwareProactorStreamEmitterProcessingStrategyFactory;
import com.zernova.runtime.core.internal.processor.strategy.TransactionAwareStreamEmitterProcessingStrategyFactory;
import com.zernova.runtime.core.internal.profiling.DummyComponentTracerFactory;
import com.zernova.runtime.core.internal.routing.ChoiceRouter;
import com.zernova.runtime.core.internal.routing.ScatterGatherRouter;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.junit4.AbstractReactiveProcessorTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@SmallTest
@SuppressWarnings("deprecation")
public class DefaultMessageProcessorChainTestCase extends AbstractReactiveProcessorTestCase {

  private static final Logger LOGGER = getLogger(DefaultMessageProcessorChainTestCase.class);

  protected ZernovaContext muleContext;

  private final AtomicInteger nonBlockingProcessorsExecuted = new AtomicInteger(0);
  private final ProcessingStrategyFactory processingStrategyFactory;

  @Rule
  public ExpectedException expectedException = none();

  @Parameterized.Parameters(name = "{0}, {2}")
  public static Collection<Object[]> parameters() {
    return asList(new Object[][] {
        {"TransactionAwareStreamEmitterProcessingStrategyFactory",
            new TransactionAwareStreamEmitterProcessingStrategyFactory(), BLOCKING},
        {"TransactionAwareProactorStreamEmitterProcessingStrategyFactory",
            new TransactionAwareProactorStreamEmitterProcessingStrategyFactory(), BLOCKING},
        {"StreamEmitterProcessingStrategyFactory",
            new StreamEmitterProcessingStrategyFactory(), BLOCKING},
        {"ProactorStreamEmitterProcessingStrategyFactory",
            new ProactorStreamEmitterProcessingStrategyFactory(), BLOCKING},
        {"BlockingProcessingStrategyFactory",
            new BlockingProcessingStrategyFactory(), BLOCKING},
        {"DirectProcessingStrategyFactory",
            new DirectProcessingStrategyFactory(), BLOCKING},
        {"TransactionAwareStreamEmitterProcessingStrategyFactory",
            new TransactionAwareStreamEmitterProcessingStrategyFactory(), NON_BLOCKING},
        {"TransactionAwareProactorStreamEmitterProcessingStrategyFactory",
            new TransactionAwareProactorStreamEmitterProcessingStrategyFactory(), NON_BLOCKING},
        {"StreamEmitterProcessingStrategyFactory",
            new StreamEmitterProcessingStrategyFactory(), NON_BLOCKING},
        {"ProactorStreamEmitterProcessingStrategyFactory",
            new ProactorStreamEmitterProcessingStrategyFactory(), NON_BLOCKING},
        {"BlockingProcessingStrategyFactory",
            new BlockingProcessingStrategyFactory(), NON_BLOCKING},
        {"DirectProcessingStrategyFactory",
            new DirectProcessingStrategyFactory(), NON_BLOCKING}});
  }

  private Flow flow;

  // psName exists only for showing the friendly name of the test parameter
  public DefaultMessageProcessorChainTestCase(String psName, ProcessingStrategyFactory processingStrategyFactory, Mode mode) {
    super(mode);
    this.processingStrategyFactory = processingStrategyFactory;
  }

  @Before
  public void before() throws ZernovaException {
    nonBlockingProcessorsExecuted.set(0);
    muleContext = spy(AbstractZernovaContextTestCase.muleContext);
    ZernovaConfiguration muleConfiguration = mock(ZernovaConfiguration.class);
    when(muleConfiguration.isContainerMode()).thenReturn(false);
    when(muleConfiguration.getId()).thenReturn(randomNumeric(3));
    when(muleConfiguration.getShutdownTimeout()).thenReturn(1000L);
    when(muleContext.getConfiguration()).thenReturn(muleConfiguration);

    flow = builder("flow", muleContext).processingStrategyFactory(processingStrategyFactory).build();
    flow.setAnnotations(singletonMap(LOCATION_KEY, from("flow")));
    flow.initialise();
    flow.start();
  }

  @Override
  protected Map<String, Object> getStartUpRegistryObjects() {
    return singletonMap(REGISTRY_KEY, componentLocator);
  }

  @After
  public void after() throws ZernovaException {
    flow.stop();
    flow.dispose();
  }

  @Test
  public void all() throws Exception {
    createAndRegisterFlow(muleContext, APPLE_FLOW, componentLocator);
    ScatterGatherRouter scatterGatherRouter = new ScatterGatherRouter();
    scatterGatherRouter.setAnnotations(getAppleFlowComponentLocationAnnotations());
    scatterGatherRouter
        .setRoutes(asList(newChain(empty(), getAppendingMP("1")), newChain(empty(), getAppendingMP("2")),
                          newChain(empty(), getAppendingMP("3"))));

    CoreEvent event = getTestEventUsingFlow("0");
    final MessageProcessorChain chain = newChain(empty(), singletonList(scatterGatherRouter));
    Message result = process(chain, CoreEvent.builder(event).message(event.getMessage()).build()).getMessage();
    assertThat(result.getPayload().getValue(), instanceOf(Map.class));
    Map<String, Message> resultMessage = (Map<String, Message>) result.getPayload().getValue();
    assertThat(resultMessage.values().stream().map(msg -> msg.getPayload().getValue()).collect(toList()).toArray(),
               is(equalTo(new String[] {"01", "02", "03"})));

    scatterGatherRouter.stop();
    scatterGatherRouter.dispose();
    disposeIfNeeded(chain, LOGGER);
  }

  @Test
  public void choice() throws Exception {
    ChoiceRouter choiceRouter = new ChoiceRouter(new DummyComponentTracerFactory());
    choiceRouter.setAnnotations(getAppleFlowComponentLocationAnnotations());
    choiceRouter.setAnnotations(singletonMap(LOCATION_KEY, TEST_CONNECTOR_LOCATION));
    choiceRouter.addRoute("true", newChain(empty(), getAppendingMP("1")));
    choiceRouter.addRoute("true", newChain(empty(), getAppendingMP("2")));
    choiceRouter.addRoute("true", newChain(empty(), getAppendingMP("3")));
    initialiseIfNeeded(choiceRouter, muleContext);

    Processor chain = newChain(empty(), choiceRouter);
    try {
      assertThat(process(chain, getTestEventUsingFlow("0")).getMessage().getPayload().getValue(),
                 equalTo("01"));
    } finally {
      disposeIfNeeded(choiceRouter, LOGGER);
      disposeIfNeeded(chain, LOGGER);
    }
  }

  @Override
  protected CoreEvent process(Processor messageProcessor, CoreEvent event) throws Exception {
    initialiseIfNeeded(messageProcessor, muleContext);
    startIfNeeded(messageProcessor);

    return super.process(messageProcessor, event);
  }

  private AppendingMP getAppendingMP(String append) {
    return new NonBlockingAppendingMP(append);
  }

  class NonBlockingAppendingMP extends AppendingMP {

    /**
     * Force the proactor to change the thread.
     */
    @Override
    public ProcessingType getProcessingType() {
      return CPU_INTENSIVE;
    }

    public NonBlockingAppendingMP(String append) {
      super(append);
    }

    @Override
    public CoreEvent process(CoreEvent event) throws ZernovaException {
      nonBlockingProcessorsExecuted.incrementAndGet();
      return super.process(event);
    }
  }

  class AppendingMP extends AbstractComponent implements Processor, Lifecycle, ZernovaContextAware {

    String appendString;
    boolean muleContextInjected;
    boolean initialised;
    boolean started;
    boolean stopped;
    boolean disposed;
    CoreEvent event;
    CoreEvent resultEvent;

    public AppendingMP(String append) {
      this.appendString = append;
    }

    @Override
    public CoreEvent process(final CoreEvent event) throws ZernovaException {
      return innerProcess(event);
    }

    private CoreEvent innerProcess(CoreEvent event) {
      this.event = event;
      CoreEvent result =
          CoreEvent.builder(event).message(of(event.getMessage().getPayload().getValue() + appendString)).build();
      this.resultEvent = result;
      return result;
    }

    @Override
    public void initialise() throws InitialisationException {
      initialised = true;
    }

    @Override
    public void start() throws ZernovaException {
      started = true;
    }

    @Override
    public void stop() throws ZernovaException {
      stopped = true;
    }

    @Override
    public void dispose() {
      disposed = true;
    }

    @Override
    public String toString() {
      return ObjectUtils.toString(this);
    }

    @Override
    public void setMuleContext(ZernovaContext context) {
      this.muleContextInjected = true;
    }

    @Override
    public ComponentLocation getLocation() {
      return mock(ComponentLocation.class);
    }
  }

  protected CoreEvent getTestEventUsingFlow(Object data) throws ZernovaException {
    return CoreEvent.builder(create(flow, TEST_CONNECTOR_LOCATION)).message(of(data)).build();
  }

}
