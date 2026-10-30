/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.routing;

import static com.zernova.runtime.api.exception.ExceptionHelper.getRootMuleException;
import static com.zernova.runtime.api.message.Message.of;

import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

import com.zernova.runtime.api.exception.ComposedErrorException;
import com.zernova.runtime.api.exception.ErrorMessageAwareException;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.api.i18n.I18nMessageFactory;
import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.core.privileged.processor.Router;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ZernovaException} used to aggregate exceptions thrown by several routes in the context of a single {@link Router}. This
 * exception implements {@link ComposedErrorException} so that a composite {@link Error} is created and also implements
 * {@link ErrorMessageAwareException} to provide an error message using {@link RoutingResult} that provides access to all route
 * results for use in error handlers.
 *
 * @since 4.6, moved from {@link com.zernova.runtime.core.internal.routing.result.CompositeRoutingException}
 * @see RoutingResult
 */
public class CompositeRoutingException extends ZernovaException implements ComposedErrorException, ErrorMessageAwareException {

  private static final String MESSAGE_TITLE = "Error(s) were found for route(s):";
  private static final String MESSAGE_SUB_TITLE = "Detailed Error(s) for route(s):";
  private static final String LEGACY_MESSAGE_TITLE = "Exception(s) were found for route(s): ";

  private static final long serialVersionUID = -4421728527040579605L;

  private final RoutingResult routingResult;
  private static final Logger LOGGER = LoggerFactory.getLogger(CompositeRoutingException.class);

  /**
   * Constructs a new {@link CompositeRoutingException}
   *
   * @param routingResult routing result object containing the results from all routes.
   */
  public CompositeRoutingException(RoutingResult routingResult) {
    super(buildExceptionMessage(routingResult));
    this.routingResult = routingResult;
  }

  @Override
  public String getDetailedMessage() {
    Map<String, Pair<Error, ZernovaException>> detailedFailures = getDetailedFailures();

    if (detailedFailures.isEmpty()) {
      return getLegacyDetailedMessage();
    } else {
      StringBuilder builder = new StringBuilder();
      // provide information about the composite exception itself
      builder.append(super.getDetailedMessage());
      // get detailed information about exceptions that make up composite exception
      builder.append(lineSeparator()).append(MESSAGE_SUB_TITLE).append(lineSeparator());
      for (Entry<String, Pair<Error, ZernovaException>> entry : detailedFailures.entrySet()) {
        ZernovaException muleException = entry.getValue().getSecond();
        Throwable exception = entry.getValue().getFirst().getCause();
        appendMessageForExceptions(builder, entry.getKey(), exception, muleException);
      }
      return builder.toString();
    }
  }

  private String getLegacyDetailedMessage() {
    StringBuilder builder = new StringBuilder();
    builder.append(LEGACY_MESSAGE_TITLE).append(lineSeparator());
    for (Entry<String, Error> entry : routingResult.getFailures().entrySet()) {
      ZernovaException muleException = getRootMuleException(entry.getValue().getCause());
      Throwable exception = entry.getValue().getCause();
      appendMessageForExceptions(builder, entry.getKey(), exception, muleException);
    }
    return builder.toString();
  }

  private void appendMessageForExceptions(StringBuilder builder, String route, Throwable exception, ZernovaException muleException) {
    String routeSubtitle = String.format("Route %s: ", route);
    builder.append(lineSeparator());
    if (muleException != null) {
      builder.append(routeSubtitle).append(muleException.getDetailedMessage());
    } else {
      builder.append(routeSubtitle).append("Caught exception in Exception Strategy: ").append(exception.getMessage());
    }
  }

  private Map<String, Pair<Error, ZernovaException>> getDetailedFailures() {
    Method getDetailedFailuresMethod = null;
    Map<String, Pair<Error, ZernovaException>> detailedFailures = emptyMap();
    try {
      getDetailedFailuresMethod = RoutingResult.class.getMethod("getFailuresWithExceptionInfo");
      detailedFailures =
          (Map<String, Pair<Error, ZernovaException>>) getDetailedFailuresMethod.invoke(routingResult);
    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
      LOGGER.warn("Invalid Invocation, Expected method getFailuresWithExceptionInfo doesn't exist");
    }
    return detailedFailures;
  }

  private static I18nMessage buildExceptionMessage(RoutingResult routingResult) {
    StringBuilder builder = new StringBuilder();
    for (Entry<String, Error> routeResult : routingResult.getFailures().entrySet()) {
      Throwable routeException = routeResult.getValue().getCause();
      builder.append(lineSeparator()).append("\t").append("Route ").append(routeResult.getKey()).append(": ")
          .append(routeException.getClass().getName())
          .append(": ").append(routeException.getMessage());
    }
    if (!routingResult.getFailuresWithExceptionInfo().isEmpty()) {
      builder.insert(0, MESSAGE_TITLE);
    } else {
      builder.insert(0, LEGACY_MESSAGE_TITLE);
    }
    return I18nMessageFactory.createStaticMessage(builder.toString());
  }

  @Override
  public List<Error> getErrors() {
    if (!routingResult.getFailures().isEmpty()) {
      return new ArrayList<>(routingResult.getFailures().values());
    } else {
      return routingResult.getFailuresWithExceptionInfo().values().stream().map(Pair::getFirst).collect(toList());
    }
  }

  @Override
  public Message getErrorMessage() {
    return of(routingResult);
  }

}
