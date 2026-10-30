/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.processor;

import static com.zernova.runtime.api.el.BindingContextUtils.NULL_BINDING_CONTEXT;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.MULE_LOGGING_BLOCKING_CATEGORIES;
import static com.zernova.runtime.core.api.processor.ReactiveProcessor.ProcessingType.BLOCKING;
import static com.zernova.runtime.core.api.processor.ReactiveProcessor.ProcessingType.CPU_LITE;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.runtime.core.api.util.StringUtils.EMPTY;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.util.StringUtils;
import com.zernova.runtime.core.internal.interception.HasParamsAsTemplateProcessor;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

/**
 * MessageProcessor implementation that logs the current element of a value evaluated from it using an expression evaluator. By
 * default the current messages is logged using the {@link LogLevel#INFO} level to the
 * 'com.zernova.runtime.core.internal.processor.LoggerMessageProcessor' category. The level and category can both be configured to
 * suit your needs.
 */
public class LoggerMessageProcessor extends AbstractComponent
    implements HasParamsAsTemplateProcessor, Processor, Initialisable {

  // TODO - MULE-16446: Logger execution type should be defined according to the appender used
  private static final String BLOCKING_CATEGORIES_PROPERTY = System.getProperty(MULE_LOGGING_BLOCKING_CATEGORIES, "");
  private static final Set<String> BLOCKING_CATEGORIES = new HashSet<>(asList(BLOCKING_CATEGORIES_PROPERTY.split(",")));
  private static final String WILDCARD = "*";

  protected transient Logger logger;

  protected String message;
  protected String category;
  protected String level = "INFO";

  private ExtendedExpressionManager expressionManager;

  private volatile ProcessingType processingType;
  private transient ClassLoader loggerExecutionClassloader;

  @Override
  public void initialise() throws InitialisationException {
    initLogger();
    initProcessingTypeIfPossible();
  }

  protected void initLogger() {
    this.loggerExecutionClassloader = currentThread().getContextClassLoader();
    if (category != null) {
      logger = LoggerFactory.getLogger(category);
    } else {
      logger = LoggerFactory.getLogger(LoggerMessageProcessor.class);
    }
  }

  protected void initProcessingTypeIfPossible() {
    if (getBlockingCategories().size() == 1 && getBlockingCategories().contains(EMPTY)) {
      processingType = CPU_LITE;
    } else if (getBlockingCategories().contains(WILDCARD)) {
      processingType = BLOCKING;
    }
  }

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    log(event);
    return event;
  }

  @Override
  public ProcessingType getProcessingType() {
    if (processingType == null) {
      synchronized (this) {
        if (processingType == null) {
          processingType = isBlocking(category) ? BLOCKING : CPU_LITE;
        }
      }
    }
    return processingType;
  }

  private boolean isBlocking(String category) {
    return getBlockingCategories().stream().anyMatch(blockingCategory -> blockingCategory.equals(category) ||
        (category != null && category.startsWith(blockingCategory + ".")));
  }

  protected void log(CoreEvent event) {
    if (loggerExecutionClassloader != null) {
      // The logger should be the one the log4j was initialized with.
      // This guarantees that the logger is always is the one from the appropriate artifact
      // independently of the TCCL.
      withContextClassLoader(loggerExecutionClassloader, () -> doLog(event));
    } else {
      doLog(event);
    }
  }

  private void doLog(CoreEvent event) {
    if (event == null) {
      logWithLevel(null);
    } else {
      if (StringUtils.isEmpty(message)) {
        logWithLevel(event.getMessage());
      } else {
        LogLevel logLevel = LogLevel.valueOf(level);
        if (logLevel.isEnabled(logger)) {
          logLevel.log(logger, expressionManager.parseLogTemplate(message, event, getLocation(), NULL_BINDING_CONTEXT));
        }
      }
    }
  }

  protected void logWithLevel(Object object) {
    LogLevel logLevel = LogLevel.valueOf(level);
    if (logLevel.isEnabled(logger)) {
      logLevel.log(logger, object);
    }
  }

  @Inject
  public void setExpressionManager(ExtendedExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public void setLevel(String level) {
    this.level = level.toUpperCase();
  }

  public enum LogLevel {

    ERROR {

      @Override
      public void log(Logger logger, Object object) {
        logger.error(object == null ? null : object.toString());
      }

      @Override
      public boolean isEnabled(Logger logger) {
        return logger.isErrorEnabled();
      }
    },
    WARN {

      @Override
      public void log(Logger logger, Object object) {
        logger.warn(object == null ? null : object.toString());
      }

      @Override
      public boolean isEnabled(Logger logger) {
        return logger.isWarnEnabled();
      }
    },
    INFO {

      @Override
      public void log(Logger logger, Object object) {
        logger.info(object == null ? null : object.toString());
      }

      @Override
      public boolean isEnabled(Logger logger) {
        return logger.isInfoEnabled();
      }
    },
    DEBUG {

      @Override
      public void log(Logger logger, Object object) {
        logger.debug(object == null ? null : object.toString());
      }

      @Override
      public boolean isEnabled(Logger logger) {
        return logger.isDebugEnabled();
      }
    },
    TRACE {

      @Override
      public void log(Logger logger, Object object) {
        logger.trace(object == null ? null : object.toString());
      }

      @Override
      public boolean isEnabled(Logger logger) {
        return logger.isTraceEnabled();
      }
    };

    public abstract void log(Logger logger, Object object);

    public abstract boolean isEnabled(Logger logger);
  }

  protected Set<String> getBlockingCategories() {
    return BLOCKING_CATEGORIES;
  }
}
