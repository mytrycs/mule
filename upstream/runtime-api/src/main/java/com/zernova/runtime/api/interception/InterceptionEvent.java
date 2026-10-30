/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.interception;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.api.el.ZernovaExpressionLanguage;
import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.DataType;

import java.util.Map;

/**
 * Provides access to the attributes of the input event of a component and allows to mutate them.
 *
 * @since 1.0
 */
@NoImplement
public interface InterceptionEvent extends Event {

  /**
   * Set the {@link Message} to construct the target Event with.
   *
   * @param message the message instance.
   * @return the builder instance
   */
  InterceptionEvent message(Message message);

  /**
   * Set a map of variables. Any existing variables added to the builder will be removed.
   *
   * @param variables variables to be set.
   * @return the builder instance
   */
  InterceptionEvent variables(Map<String, Object> variables);

  /**
   * Add a variable.
   *
   * @param key       the key of the variable to add.
   * @param value     the value of the variable to add. {@code null} values are supported.
   * @param mediaType additional metadata about the {@code value} type.
   * @return the builder instance.
   */
  InterceptionEvent addVariable(String key, Object value, DataType mediaType);

  /**
   * Add a variable.
   *
   * @param key   the key of the variable to add.
   * @param value the value of the variable to add. {@code null} values are supported.
   * @return the builder instance
   */
  InterceptionEvent addVariable(String key, Object value);

  /**
   * Remove a variable.
   *
   * @param key the variable key.
   * @return the builder instance
   */
  InterceptionEvent removeVariable(String key);

  /**
   * Creates a {@link BindingContext} for the target event to use with a {@link ZernovaExpressionLanguage}.
   *
   * @return a {@link BindingContext} representing the data of the target event.
   */
  BindingContext asBindingContext();

}
