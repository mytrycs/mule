/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.execution;

import static com.zernova.runtime.api.exception.ExceptionHelper.getExceptionsAsList;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.event.CoreEvent.builder;
import static com.zernova.runtime.module.extension.internal.runtime.execution.SdkInternalContext.from;
import static java.lang.String.format;
import static java.util.Optional.empty;

import com.zernova.runtime.api.component.execution.ComponentExecutionException;
import com.zernova.runtime.api.exception.TypedException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.registry.DefaultRegistry;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutor;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.mule.internal.construct.Operation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.inject.Inject;

/**
 * {@link CompletableComponentExecutor} implemenetation that executes {@link Operation} instances.
 *
 * @since 4.5.0
 */
public class ZernovaOperationExecutor implements CompletableComponentExecutor<ComponentModel>, Initialisable {

  private static final Message NULL_MESSAGE = Message.builder().nullValue().build();

  private final ComponentModel operationModel;
  private Operation operation;

  @Inject
  private ZernovaContext muleContext;

  public ZernovaOperationExecutor(ComponentModel operationModel) {
    this.operationModel = operationModel;
  }

  @Override
  public void initialise() throws InitialisationException {
    operation = (Operation) new DefaultRegistry(muleContext).lookupByName(operationModel.getName())
        .orElseThrow(() -> new InitialisationException(createStaticMessage(
                                                                           format("Operation '%s' not found in registry",
                                                                                  operationModel.getName())),
                                                       this));
  }

  @Override
  public void execute(ExecutionContext<ComponentModel> executionContext, ExecutorCallback callback) {
    ExecutionContextAdapter<ComponentModel> ctx = (ExecutionContextAdapter<ComponentModel>) executionContext;
    final CoreEvent inputEvent = ctx.getEvent();

    CoreEvent executionEvent = builder(inputEvent.getContext())
        .parameters(buildOperationParameters(inputEvent, ctx))
        .message(NULL_MESSAGE)
        .build();

    operation.execute(executionEvent).whenComplete((resultEvent, exception) -> {
      if (exception != null) {
        callback.error(tryCreateTypedExceptionFrom(exception));
      } else {
        callback.complete(builder(inputEvent)
            .message(resultEvent.getMessage())
            .build());
      }
    });
  }

  private Map<String, ?> buildOperationParameters(CoreEvent inputEvent, ExecutionContextAdapter<ComponentModel> ctx) {
    Map<String, Object> parameters = new HashMap<>();
    from(inputEvent)
        .getOperationExecutionParams(ctx.getComponent().getLocation(), inputEvent.getContext().getId())
        .parameters()
        .forEach((key, value) -> parameters.put(key, mapParameterValue(value)));
    return parameters;
  }

  private Object mapParameterValue(Object parameterValue) {
    // We don't want to expose the ConfigurationProvider to the inside of the operation, only its name.
    return parameterValue instanceof ConfigurationProvider ? ((ConfigurationProvider) parameterValue).getName() : parameterValue;
  }

  private static Throwable tryCreateTypedExceptionFrom(Throwable exception) {
    // Wrapping the caught exception into a TypedException allows us to explicit the error type,
    // which we are extracting from a ComponentExecutionException.
    // Other way to implement it would be to change the method MessagingExceptionResolver#errorTypeFromException
    // to handle instances of ComponentExecutionException, but having this code in this module will allow us to
    // do custom mappings without modifying the error handling mechanism.
    return getErrorType(exception).map(errorType -> wrapInTyped(exception, errorType)).orElse(exception);
  }

  private static Optional<ErrorType> getErrorType(Throwable exception) {
    List<Throwable> exceptionsAsList = getExceptionsAsList(exception);
    for (Throwable e : exceptionsAsList) {
      // The ZernovaOperation has a chain which is an AbstractExecutableComponent, so
      // given the logic of AbstractExecutableComponent#doProcess, some exception in this list
      // must be an instance of ComponentExecutionException, and the event within it should have
      // the event with the actual error.
      if (e instanceof ComponentExecutionException) {
        Optional<Error> optionalError = ((ComponentExecutionException) e).getEvent().getError();
        if (optionalError.isPresent()) {
          return optionalError.map(Error::getErrorType);
        }
      }
    }

    return empty();
  }

  private static Throwable wrapInTyped(Throwable throwable, ErrorType errorType) {
    if (throwable instanceof TypedException) {
      return throwable;
    }
    return new TypedException(throwable, errorType);
  }
}
