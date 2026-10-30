/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.api.processor;

import static com.zernova.runtime.api.component.ComponentIdentifier.builder;
import static com.zernova.runtime.api.metadata.DataType.fromType;
import static com.zernova.runtime.module.extension.mule.internal.error.ThrowableError.wrap;
import static com.zernova.runtime.module.extension.mule.internal.loader.parser.ZernovaSdkApplicationExtensionModelParser.APP_LOCAL_EXTENSION_NAMESPACE;

import com.zernova.api.annotation.Experimental;
import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.exception.DefaultZernovaException;
import com.zernova.runtime.api.exception.TypedException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.AbstractRaiseErrorProcessor;
import com.zernova.runtime.core.internal.util.attribute.AttributeEvaluator;

/**
 * Processor capable of raising errors within a Mule Operation's body on demand, given a type and optionally a message.
 *
 * @since 4.5
 */
@Experimental
public final class ZernovaSdkRaiseErrorProcessor extends AbstractRaiseErrorProcessor {

  private static final DataType ERROR_DATA_TYPE = fromType(Error.class);

  private AttributeEvaluator causeEvaluator = new AttributeEvaluator("#[error]", ERROR_DATA_TYPE);

  @Override
  protected ComponentIdentifier calculateErrorIdentifier(String typeId) {
    return builder().namespace(APP_LOCAL_EXTENSION_NAMESPACE).name(typeId).build();
  }

  @Override
  public void initialise() throws InitialisationException {
    super.initialise();
    causeEvaluator.initialize(expressionManager);
  }

  @Override
  protected TypedException getException(ErrorType type, String message, CoreEvent event) {
    return new TypedException(calculateCause(event, message), type);
  }

  private Throwable calculateCause(CoreEvent event, String message) {
    Error causeError = causeEvaluator.<Error>resolveTypedValue(event).getValue();
    if (causeError == null) {
      return new DefaultZernovaException(message);
    } else {
      return wrap(causeError);
    }
  }

  public void setCause(String cause) {
    this.causeEvaluator = new AttributeEvaluator(cause, ERROR_DATA_TYPE);
  }
}
