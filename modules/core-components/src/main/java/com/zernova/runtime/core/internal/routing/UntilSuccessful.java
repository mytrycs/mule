/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.SUPPRESS_ERRORS;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.buildNewChainWithListOfProcessors;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.createDefaultProcessingStrategyFactory;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.getProcessingStrategy;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.processToApply;

import static java.util.Collections.singletonList;

import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.scheduler.SchedulerConfig;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.expression.ExpressionRuntimeException;
import com.zernova.runtime.core.api.processor.AbstractZernovaObjectOwner;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategy;
import com.zernova.runtime.core.internal.routing.UntilSuccessfulRouter.RetryContextInitializationException;
import com.zernova.runtime.core.privileged.processor.Scope;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import jakarta.inject.Inject;

import org.reactivestreams.Publisher;

/**
 * UntilSuccessful attempts to route a message to the message processor it contains. Routing is considered successful if no
 * exception has been raised and, optionally, if the response matches an expression.
 */
public class UntilSuccessful extends AbstractZernovaObjectOwner implements Scope {

  private static final String DEFAULT_MILLIS_BETWEEN_RETRIES = "60000";
  private static final String DEFAULT_RETRIES = "5";
  public static final String UNTIL_SUCCESSFUL_ATTEMPT_SPAN_NAME_SUFIX = ":attempt";

  @Inject
  private SchedulerService schedulerService;

  @Inject
  private ExtendedExpressionManager expressionManager;

  @Inject
  private ConfigurationComponentLocator componentLocator;

  @Inject
  private FeatureFlaggingService featureFlaggingService;

  @Inject
  private ComponentTracerFactory componentTracerFactory;

  private String maxRetries = DEFAULT_RETRIES;
  private String millisBetweenRetries = DEFAULT_MILLIS_BETWEEN_RETRIES;
  private MessageProcessorChain nestedChain;
  private boolean suppressErrors;
  private Predicate<CoreEvent> shouldRetry;
  private Scheduler timer;
  private List<Processor> processors;
  private ProcessingStrategy processingStrategy;

  @Override
  public void initialise() throws InitialisationException {
    if (processors == null) {
      throw new InitialisationException(createStaticMessage("One message processor must be configured within 'until-successful'."),
                                        this);
    }

    this.nestedChain =
        buildNewChainWithListOfProcessors(getProcessingStrategy(locator, this), processors,
                                          componentTracerFactory.fromComponent(this, UNTIL_SUCCESSFUL_ATTEMPT_SPAN_NAME_SUFIX));

    super.initialise();

    timer = schedulerService.cpuLightScheduler(SchedulerConfig.config()
        .withName(this.getClass().getName() + ".timer - " + getLocation().getLocation()));
    suppressErrors = featureFlaggingService.isEnabled(SUPPRESS_ERRORS);
    shouldRetry = event -> event.getError().isPresent();

    final Optional<ProcessingStrategy> processingStrategyFromRootContainer = getProcessingStrategy(componentLocator, this);

    processingStrategy = processingStrategyFromRootContainer
        .orElseGet(() -> createDefaultProcessingStrategyFactory().create(muleContext, getLocation().getLocation() + ".ps"));
  }

  @Override
  public void dispose() {
    super.dispose();
    timer.stop();
  }

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    try {
      return processToApply(event, this);
    } catch (Exception error) {
      Throwable cause = error.getCause();
      if (cause != null && cause instanceof RetryContextInitializationException &&
          cause.getCause() instanceof ExpressionRuntimeException) {
        // Runtime exception caused by Retry Ctx initialization, propagating
        throw ((ExpressionRuntimeException) cause.getCause());
      } else {
        // Not caused by context initialization. Throwing as raised.
        throw error;
      }
    }
  }

  @Override
  public Publisher<CoreEvent> apply(Publisher<CoreEvent> publisher) {
    return new UntilSuccessfulRouter(this, publisher, nestedChain, processingStrategy, expressionManager, shouldRetry, timer,
                                     maxRetries, millisBetweenRetries, suppressErrors)
                                         .getDownstreamPublisher();
  }


  /**
   * @return the number of times the scope will retry before failing. Default value is 5.
   */
  public String getMaxRetries() {
    return maxRetries;
  }

  /**
   *
   * @param maxRetries the number of times the scope will retry before failing. Default value is 5.
   */
  public void setMaxRetries(final String maxRetries) {
    this.maxRetries = maxRetries;
  }

  /**
   * @return the number of milliseconds between retries. Default value is 60000.
   */
  public String getMillisBetweenRetries() {
    return millisBetweenRetries;
  }

  /**
   * @param millisBetweenRetries the number of milliseconds between retries. Default value is 60000.
   */
  public void setMillisBetweenRetries(String millisBetweenRetries) {
    this.millisBetweenRetries = millisBetweenRetries;
  }

  /**
   * Configure the nested {@link Processor}'s that error handling and transactional behaviour should be applied to.
   *
   * @param processors
   */
  public void setMessageProcessors(List<Processor> processors) {
    this.processors = processors;
  }

  @Override
  protected List<Object> getOwnedObjects() {
    return singletonList(nestedChain);
  }
}

