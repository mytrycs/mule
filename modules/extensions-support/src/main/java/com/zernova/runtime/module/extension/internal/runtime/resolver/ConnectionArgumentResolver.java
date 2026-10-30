/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static java.lang.String.format;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.extension.api.util.NameUtils.getComponentModelTypeName;
import static com.zernova.runtime.module.extension.internal.ExtensionProperties.CONNECTION_PARAM;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionHandler;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.extension.api.runtime.operation.ExecutionContext;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.ExtensionProperties;

/**
 * Returns the value of the {@link ExtensionProperties#CONNECTION_PARAM} variable, which is expected to have been previously set
 * on the supplied {@link ExecutionContext}.
 * <p>
 * Notice that for this to work, the {@link ExecutionContext} has to be an instance of {@link ExecutionContextAdapter}
 *
 * @since 4.0
 */
public class ConnectionArgumentResolver implements ArgumentResolver<Object> {

  /**
   * Returns the connection previously set on the {@code executionContext} under the key
   * {@link ExtensionProperties#CONNECTION_PARAM}
   *
   * @param executionContext an {@link ExecutionContext}
   * @return the connection
   * @throws IllegalArgumentException if the connection was not set
   * @throws ClassCastException       if {@code executionContext} is not an {@link ExecutionContextAdapter}
   */
  @Override
  public Object resolve(ExecutionContext executionContext) {
    ConnectionHandler connectionHandler =
        ((ExecutionContextAdapter<ComponentModel>) executionContext).getVariable(CONNECTION_PARAM);
    if (connectionHandler == null) {
      throw new IllegalArgumentException(format("No connection was provided for the component [%s]",
                                                executionContext.getComponentModel().getName()));
    }

    try {
      return connectionHandler.getConnection();
    } catch (ConnectionException e) {
      throw new ZernovaRuntimeException(
                                     createStaticMessage(format("Error was found trying to obtain a connection to execute %s '%s' of extension '%s'",
                                                                getComponentModelTypeName(executionContext.getComponentModel()),
                                                                executionContext.getComponentModel().getName(),
                                                                executionContext.getExtensionModel().getName())),
                                     e);
    }
  }
}
