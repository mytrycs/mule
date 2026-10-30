/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.exception;

import static com.zernova.runtime.api.component.ComponentIdentifier.buildFromStringRepresentation;
import static com.zernova.runtime.api.message.error.matcher.ErrorTypeMatcherUtils.createErrorTypeMatcher;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.TX_CONTINUE;
import static com.zernova.runtime.api.profiling.type.RuntimeProfilingEventTypes.TX_ROLLBACK;
import static com.zernova.runtime.config.internal.error.ZernovaCoreErrorTypeRepository.MULE_CORE_ERROR_TYPE_REPOSITORY;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.REDELIVERY_EXHAUSTED;
import static com.zernova.runtime.core.api.transaction.TransactionUtils.profileTransactionAction;

import static java.util.Collections.singletonMap;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.message.error.matcher.ErrorTypeMatcher;
import com.zernova.runtime.api.profiling.ProfilingDataProducer;
import com.zernova.runtime.api.profiling.ProfilingService;
import com.zernova.runtime.api.profiling.type.context.TransactionProfilingEventContext;
import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.transaction.Transaction;
import com.zernova.runtime.core.api.transaction.TransactionCoordination;
import com.zernova.runtime.core.privileged.exception.MessagingException;
import com.zernova.runtime.core.privileged.exception.TemplateOnErrorHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import jakarta.inject.Inject;

/**
 * Handler that will propagate errors and rollback transactions. Replaces the rollback-exception-strategy from Mule 3.
 *
 * @since 4.0
 */
public class OnErrorPropagateHandler extends TemplateOnErrorHandler {

  private static final String CORE_PREFIX = "mule";
  private static final String ON_ERROR_PROPAGATE_ELEMENT_IDENTIFIER = "on-error-propagate";
  private static final String COMPONENT_IDENTIFIER = CORE_PREFIX + ":" + ON_ERROR_PROPAGATE_ELEMENT_IDENTIFIER;
  private final ErrorTypeMatcher redeliveryExhaustedMatcher;

  @Inject
  private ProfilingService profilingService;

  private ProfilingDataProducer<TransactionProfilingEventContext, Object> continueProducer;
  private ProfilingDataProducer<TransactionProfilingEventContext, Object> rollbackProducer;

  public OnErrorPropagateHandler() {
    ErrorType redeliveryExhaustedErrorType = MULE_CORE_ERROR_TYPE_REPOSITORY.getErrorType(REDELIVERY_EXHAUSTED)
        .orElseThrow(() -> new IllegalStateException("REDELIVERY_EXHAUSTED error type not found"));

    // Identifier for cases where this class is directly instantiated by the runtime core in order to implement default error
    // handling.
    setAnnotations(singletonMap(ANNOTATION_NAME, buildFromStringRepresentation(COMPONENT_IDENTIFIER)));

    redeliveryExhaustedMatcher = createErrorTypeMatcher(redeliveryExhaustedErrorType);
  }

  @Override
  protected void doInitialise() throws InitialisationException {
    super.doInitialise();
    this.continueProducer = profilingService.getProfilingDataProducer(TX_CONTINUE);
    this.rollbackProducer = profilingService.getProfilingDataProducer(TX_ROLLBACK);
  }

  @Override
  public boolean acceptsAll() {
    return errorTypeMatcher == null && !when.isPresent();
  }

  /**
   * @param errorType an {@link ErrorType}
   * @return whether this handler accepts the provided type
   */
  boolean acceptsErrorType(ErrorType errorType) {
    return acceptsAll() || (errorTypeMatcher != null && errorTypeMatcher.match(errorType));
  }

  @Override
  protected Function<CoreEvent, CoreEvent> beforeRouting() {
    return event -> {
      Exception exception = getException(event);
      event = super.beforeRouting().apply(event);
      if (!isRedeliveryExhausted(exception) && isOwnedTransaction(event, exception)) {
        profileTransactionAction(rollbackProducer, TX_ROLLBACK, getLocation());
        rollback(exception);
      } else {
        profileTransactionAction(continueProducer, TX_CONTINUE, getLocation());
      }
      return event;
    };
  }

  public void rollback(Exception ex) {
    Transaction tx = TransactionCoordination.getInstance().getTransaction();
    if (tx == null) {
      return;
    }
    try {
      tx.rollback();
    } catch (TransactionException e) {
      ex.addSuppressed(e);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @param buildFor
   */
  @Override
  public TemplateOnErrorHandler duplicateFor(ComponentLocation buildFor) {
    OnErrorPropagateHandler cpy = new OnErrorPropagateHandler();
    cpy.setFlowLocation(buildFor);
    when.ifPresent(expr -> cpy.setWhen(expr));
    cpy.setHandleException(this.handleException);
    cpy.setErrorType(this.errorType);
    cpy.setMessageProcessors(this.getMessageProcessors());
    cpy.setExceptionListener(this.getExceptionListener());
    cpy.setAnnotations(this.getAnnotations());
    return cpy;
  }

  @Override
  protected List<Processor> getOwnedMessageProcessors() {
    return new ArrayList<>(super.getOwnedMessageProcessors());
  }

  private boolean isRedeliveryExhausted(Exception exception) {
    if (exception instanceof MessagingException) {
      Optional<Error> error = ((MessagingException) exception).getEvent().getError();
      return error.map(e -> redeliveryExhaustedMatcher.match(e.getErrorType()))
          .orElse(false);
    }
    return false;
  }

}
