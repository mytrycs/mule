/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.el.function;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_MAP;
import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static com.zernova.runtime.api.el.BindingContextUtils.ERROR;
import static com.zernova.runtime.api.el.BindingContextUtils.MESSAGE;
import static com.zernova.runtime.api.el.BindingContextUtils.VARS;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.metadata.DataType.NUMBER;
import static com.zernova.runtime.api.metadata.DataType.OBJECT;
import static com.zernova.runtime.api.metadata.DataType.STRING;
import static com.zernova.runtime.api.metadata.DataType.fromType;
import static com.zernova.runtime.api.metadata.MediaType.APPLICATION_JAVA;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.execution.ComponentExecutionException;
import com.zernova.runtime.api.component.execution.ExecutableComponent;
import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.api.el.ExpressionFunction;
import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.FunctionParameter;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.core.api.construct.Flow;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.privileged.event.PrivilegedEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


/**
 * Implementation of lookup("myFlow", payload), a function which executes the desired flow with the specified payload and returns
 * its result. For now, only Java payloads will be supported.
 *
 * @since 4.0
 */
public class LookupFunction implements ExpressionFunction {

  private static final DataType TYPED_VALUE = fromType(TypedValue.class);

  private final ConfigurationComponentLocator componentLocator;

  private final SchedulerService schedulerService;

  public LookupFunction(ConfigurationComponentLocator componentLocator, SchedulerService schedulerService) {
    this.componentLocator = componentLocator;
    this.schedulerService = schedulerService;
  }

  @Override
  public Object call(Object[] parameters, BindingContext context) {
    String flowName = (String) parameters[0];
    Object payload = parameters[1];
    Integer timeout = (Integer) parameters[2];

    Location componentLocation = Location.builder().globalName(flowName).build();
    Component component = componentLocator.find(componentLocation)
        .orElseThrow(() -> new IllegalArgumentException(format("There is no component named '%s'.", flowName)));

    if (component instanceof Flow) {
      Message incomingMessage = lookupValue(context, MESSAGE, Message.builder().nullValue().build());
      Map<String, ?> incomingVariables = lookupValue(context, VARS, EMPTY_MAP);
      Error incomingError = lookupValue(context, ERROR, null);

      Message message = Message.builder(incomingMessage).value(payload).mediaType(APPLICATION_JAVA).build();
      CoreEvent event = CoreEvent.builder(PrivilegedEvent.getCurrentEvent().getContext())
          .variables(incomingVariables)
          .error(incomingError)
          .message(message)
          .build();

      CompletableFuture<Event> lookupResultFuture = null;
      try {
        lookupResultFuture = ((ExecutableComponent) component).execute(event);
        return lookupResultFuture
            // If this timeout is hit, the thread that is executing the flow is interrupted
            .get(timeout, MILLISECONDS)
            .getMessage().getPayload();
      } catch (ExecutionException e) {
        ComponentExecutionException componentExecutionException = (ComponentExecutionException) e.getCause();
        Error error = componentExecutionException.getEvent().getError().get();
        throw new ZernovaRuntimeException(createStaticMessage(format("Flow '%s' has failed with error '%s' (%s)",
                                                                  flowName,
                                                                  error.getErrorType(),
                                                                  error.getDescription())),
                                       error.getCause());
      } catch (InterruptedException e) {
        currentThread().interrupt();
        if (lookupResultFuture != null) {
          lookupResultFuture.cancel(true);
        }
        throw new ZernovaRuntimeException(e);
      } catch (TimeoutException e) {
        if (lookupResultFuture != null) {
          lookupResultFuture.cancel(true);
        }
        throw new ZernovaRuntimeException(createStaticMessage(format("Flow '%s' has timed out after %d millis",
                                                                  flowName,
                                                                  timeout)),
                                       e);
      }
    } else {
      throw new IllegalArgumentException(format("Component '%s' is not a flow.", flowName));
    }
  }

  @Override
  public Optional<DataType> returnType() {
    return of(TYPED_VALUE);
  }

  @Override
  public List<FunctionParameter> parameters() {
    return asList(new FunctionParameter("flowName", STRING),
                  new FunctionParameter("payload", OBJECT),
                  new FunctionParameter("timeoutMillis", NUMBER, context -> {
                    if (schedulerService.isCurrentThreadForCpuWork()) {
                      return 2000;
                    } else {
                      return (int) SECONDS.toMillis(60);
                    }
                  }));
  }

  private <T> T lookupValue(BindingContext context, String binding, T fallback) {
    return context.lookup(binding).map(typedValue -> (T) typedValue.getValue()).orElse(fallback);
  }

}
