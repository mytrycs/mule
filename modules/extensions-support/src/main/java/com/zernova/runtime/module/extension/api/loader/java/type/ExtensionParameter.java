/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.api.loader.java.type;

import static java.lang.String.format;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.ZernovaExtensionAnnotationParser.mapReduceAnnotation;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getDefaultValue;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.api.meta.NamedObject;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.metadata.MediaType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.api.tls.TlsContextFactory;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.DefaultEncoding;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.client.ExtensionsClient;
import com.zernova.runtime.extension.api.exception.IllegalParameterModelDefinitionException;
import com.zernova.runtime.extension.api.notification.NotificationEmitter;
import com.zernova.runtime.extension.api.runtime.operation.FlowListener;
import com.zernova.runtime.extension.api.runtime.parameter.CorrelationInfo;
import com.zernova.runtime.extension.api.runtime.process.CompletionCallback;
import com.zernova.runtime.extension.api.runtime.process.VoidCompletionCallback;
import com.zernova.runtime.extension.api.runtime.route.Chain;
import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;
import com.zernova.runtime.extension.api.runtime.source.SourceCompletionCallback;
import com.zernova.runtime.extension.api.runtime.source.SourceResult;
import com.zernova.runtime.extension.api.runtime.streaming.StreamingHelper;
import com.zernova.runtime.extension.api.security.AuthenticationHandler;
import com.zernova.sdk.api.connectivity.oauth.AuthCodeRequest;
import com.zernova.sdk.api.runtime.parameter.Literal;
import com.zernova.sdk.api.runtime.parameter.ParameterResolver;
import com.zernova.sdk.api.runtime.source.DistributedTraceContextManager;

import java.lang.reflect.AnnotatedElement;
import java.util.Set;

import javax.lang.model.element.VariableElement;

import com.google.common.collect.ImmutableSet;

/**
 * A contract for any kind of component from which an extension's parameter can be derived
 *
 * @since 4.0
 */
@NoImplement
public interface ExtensionParameter extends WithType, WithAnnotations, NamedObject, WithAlias, WithOwner, WithElement {

  Set<Class<?>> IMPLICIT_ARGUMENT_TYPES = ImmutableSet.<Class<?>>builder()
      .add(Error.class)
      .add(SourceCallbackContext.class)
      .add(com.zernova.sdk.api.runtime.source.SourceCallbackContext.class)
      .add(CompletionCallback.class)
      .add(com.zernova.sdk.api.runtime.process.CompletionCallback.class)
      .add(VoidCompletionCallback.class)
      .add(com.zernova.sdk.api.runtime.process.VoidCompletionCallback.class)
      .add(SourceCompletionCallback.class)
      .add(com.zernova.sdk.api.runtime.source.SourceCompletionCallback.class)
      .add(MediaType.class)
      .add(AuthenticationHandler.class)
      .add(com.zernova.sdk.api.security.AuthenticationHandler.class)
      .add(FlowListener.class)
      .add(com.zernova.sdk.api.runtime.operation.FlowListener.class)
      .add(StreamingHelper.class)
      .add(com.zernova.sdk.api.runtime.streaming.StreamingHelper.class)
      .add(SourceResult.class)
      .add(com.zernova.sdk.api.runtime.source.SourceResult.class)
      .add(ComponentLocation.class)
      .add(Chain.class)
      .add(com.zernova.sdk.api.runtime.route.Chain.class)
      .add(CorrelationInfo.class)
      .add(DistributedTraceContextManager.class)
      .add(com.zernova.sdk.api.runtime.parameter.CorrelationInfo.class)
      .add(NotificationEmitter.class)
      .add(com.zernova.sdk.api.notification.NotificationEmitter.class)
      .add(ExtensionsClient.class)
      .add(com.zernova.sdk.api.client.ExtensionsClient.class)
      .add(RetryPolicyTemplate.class)
      .build();

  Set<String> IMPLICIT_ARGUMENT_PACKAGES = ImmutableSet.<String>builder()
      .add("com.zernova.sdk.api")
      .build();

  Set<Class<?>> EXPLICIT_MULE_ARGUMENT_TYPES = ImmutableSet.<Class<?>>builder()
      .add(TypedValue.class)
      .add(ParameterResolver.class)
      .add(com.zernova.runtime.extension.api.runtime.parameter.ParameterResolver.class)
      .add(com.zernova.runtime.extension.api.runtime.parameter.Literal.class)
      .add(Literal.class)
      .add(TlsContextFactory.class)
      .add(ObjectStore.class)
      .add(AuthCodeRequest.class)
      .build();

  /**
   * @return A {@code boolean} indicating whether the parameter should be advertised and added as a {@link ParameterModel} in the
   *         {@link ExtensionModel}
   */
  default boolean shouldBeAdvertised() {
    return !(IMPLICIT_ARGUMENT_TYPES.stream().anyMatch(aClass -> getType().isAssignableTo(aClass))
        || isAnnotatedWith(Config.class) || isAnnotatedWith(com.zernova.sdk.api.annotation.param.Config.class)
        || isAnnotatedWith(com.zernova.sdk.api.annotation.param.Connection.class) || isAnnotatedWith(Connection.class)
        || isAnnotatedWith(DefaultEncoding.class) || isAnnotatedWith(com.zernova.sdk.api.annotation.param.DefaultEncoding.class))
        && (!IMPLICIT_ARGUMENT_PACKAGES.stream().anyMatch(packageName -> getType().getTypeName().startsWith(packageName))
            || EXPLICIT_MULE_ARGUMENT_TYPES.stream().anyMatch(aClass -> getType().isAssignableTo(aClass)));
  }

  /**
   * @return A {@code boolean} indicating whether the parameter is a required or not
   */
  default boolean isRequired() {
    return !(isAnnotatedWith(Optional.class) || isAnnotatedWith(com.zernova.sdk.api.annotation.param.Optional.class));
  }

  /**
   * @return The {@link java.util.Optional} default value of the operation
   */
  default java.util.Optional<String> defaultValue() {
    return mapReduceAnnotation(this, Optional.class, com.zernova.sdk.api.annotation.param.Optional.class,
                               legacyAnnotationValueFetcher -> getDefaultValue(legacyAnnotationValueFetcher
                                   .getStringValue(Optional::defaultValue)),
                               sdkAnnotationValueFetcher -> getDefaultValue(sdkAnnotationValueFetcher
                                   .getStringValue(com.zernova.sdk.api.annotation.param.Optional::defaultValue)),
                               () -> new IllegalParameterModelDefinitionException(format("Parameter '%s' is annotated with '@%s' and '@%s' at the same time",
                                                                                         getName(),
                                                                                         Optional.class.getName(),
                                                                                         com.zernova.sdk.api.annotation.param.Optional.class
                                                                                             .getName())));
  }

  /**
   * @return The {@link AnnotatedElement} from which {@code this} instance was derived
   */
  java.util.Optional<? extends AnnotatedElement> getDeclaringElement();

  @Override
  java.util.Optional<VariableElement> getElement();
}
