/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.message.error.matcher.ErrorTypeMatcherUtils.createErrorTypeMatcher;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.ANY;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.CORE_PREFIX;

import static java.lang.String.format;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.message.error.matcher.ErrorTypeMatcher;
import com.zernova.runtime.core.internal.exception.EnrichedErrorMapping;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;

import java.util.Optional;

import jakarta.inject.Inject;

import org.slf4j.Logger;


public class EnrichedErrorMappingsFactoryBean extends AbstractComponentFactory<EnrichedErrorMapping> {

  private static final Logger LOGGER = getLogger(EnrichedErrorMappingsFactoryBean.class);

  public static final String CORE_ERROR_NS = CORE_PREFIX.toUpperCase();

  @Inject
  private ErrorTypeRepository errorTypeRepository;

  private String source;
  private String target;

  @Override
  public EnrichedErrorMapping doGetObject() throws Exception {
    ErrorTypeMatcher errorTypeMatcher;
    try {
      errorTypeMatcher = source != null ? createErrorTypeMatcher(errorTypeRepository, source)
          : createErrorTypeMatcher(errorTypeRepository.lookupErrorType(ANY).get());
    } catch (IllegalArgumentException e) {
      throw new ZernovaRuntimeException(e);
    }
    ErrorType targetValue = resolveErrorType(target);
    return new EnrichedErrorMapping(errorTypeMatcher, targetValue);
  }

  private ErrorType resolveErrorType(String representation) {
    ComponentIdentifier errorIdentifier = parserErrorType(representation);
    final Optional<ErrorType> lookupErrorType = errorTypeRepository.lookupErrorType(errorIdentifier);
    if (CORE_ERROR_NS.equals(errorIdentifier.getNamespace())) {
      return lookupErrorType
          .orElseThrow(() -> new ZernovaRuntimeException(createStaticMessage(format("There's no ZERNOVA error named '%s'.",
                                                                                 errorIdentifier.getName()))));
    }

    if (lookupErrorType.isPresent()) {
      return lookupErrorType.get();
    } else {
      throw new ZernovaRuntimeException(createStaticMessage("Could not find synthetic error '%s' in registry",
                                                         errorIdentifier));
    }
  }

  public static ComponentIdentifier parserErrorType(String representation) {
    int separator = representation.indexOf(':');
    String namespace;
    String identifier;
    if (separator > 0) {
      namespace = representation.substring(0, separator).toUpperCase();
      identifier = representation.substring(separator + 1).toUpperCase();
    } else {
      namespace = CORE_ERROR_NS;
      identifier = representation.toUpperCase();
    }

    return ComponentIdentifier.builder().namespace(namespace).name(identifier).build();
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

}
