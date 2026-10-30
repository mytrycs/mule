/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.implicit.exclusive.config.extension.extension;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.ConfigOverride;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

public class OperationWithConfigOverride implements Initialisable {

  private String optionalParameterValueAtInitialize;

  @Parameter
  @ConfigOverride
  private String optionalWithStaticDefault = "Some random default!";

  @Override
  public void initialise() throws InitialisationException {
    if (optionalWithStaticDefault == null) {
      throw new InitialisationException(createStaticMessage("optionalWithStaticDefault is not permitted to be null!"), this);
    }
    optionalParameterValueAtInitialize = optionalWithStaticDefault;
  }

  @MediaType(TEXT_PLAIN)
  public String getString(@Config ImplicitConfigWithOptionalParameter config) {
    return optionalParameterValueAtInitialize;
  }

}
