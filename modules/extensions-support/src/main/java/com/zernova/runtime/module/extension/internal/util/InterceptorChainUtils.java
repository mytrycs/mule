/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.util;

import static com.zernova.runtime.extension.privileged.util.ComponentDeclarationUtils.isConnectionProvisioningRequired;
import static com.zernova.runtime.module.extension.internal.util.ReconnectionUtils.addCursorResetInterceptorsIfRequired;

import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.ConnectableComponentModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.ConnectionInterceptor;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.ExtensionConnectionSupplier;
import com.zernova.runtime.module.extension.internal.runtime.execution.interceptor.InterceptorChain;
import com.zernova.runtime.tracer.api.component.ComponentTracer;
import com.zernova.runtime.tracer.api.span.info.InitialSpanInfo;

/**
 * Utilities related with the creation of interception chains.
 *
 * @since 4.5.0
 */
public class InterceptorChainUtils {

  private InterceptorChainUtils() {}

  /**
   * Creates an {@link InterceptorChain} with the interceptors necessary for connectable components.
   *
   * @param extensionModel     the {@link ExtensionModel}
   * @param componentModel     the {@link ComponentModel}
   * @param connectionSupplier the connection supplier
   * @param reflectionCache    a {@link ReflectionCache}
   * @param initialSpanInfo    a {@link InitialSpanInfo} for the span representing the get-connection.
   * @return a new {@link InterceptorChain}
   * @since 4.5.0
   */
  public static InterceptorChain createConnectionInterceptorsChain(ExtensionModel extensionModel,
                                                                   ComponentModel componentModel,
                                                                   ExtensionConnectionSupplier connectionSupplier,
                                                                   ReflectionCache reflectionCache,
                                                                   ComponentTracer<CoreEvent> connectionComponentTracer) {
    InterceptorChain.Builder chainBuilder = InterceptorChain.builder();

    if (requiresConnectionInterceptors(extensionModel, componentModel)) {
      addConnectionInterceptors(chainBuilder, connectionSupplier, connectionComponentTracer);
    }

    addCursorResetInterceptorsIfRequired(chainBuilder, extensionModel, componentModel, reflectionCache);

    return chainBuilder.build();
  }

  private static boolean requiresConnectionInterceptors(ExtensionModel extensionModel, ComponentModel componentModel) {
    // Only connectable components that require a connection to be provided beforehand should add the connection interceptors
    if (componentModel instanceof ConnectableComponentModel) {
      return ((ConnectableComponentModel) componentModel).requiresConnection()
          && isConnectionProvisioningRequired((ConnectableComponentModel) componentModel);
    }

    return false;
  }

  private static void addConnectionInterceptors(InterceptorChain.Builder chainBuilder,
                                                ExtensionConnectionSupplier connectionSupplier,
                                                ComponentTracer<CoreEvent> connectionComponentTracer) {
    chainBuilder.addInterceptor(new ConnectionInterceptor(connectionSupplier, connectionComponentTracer));
  }
}
