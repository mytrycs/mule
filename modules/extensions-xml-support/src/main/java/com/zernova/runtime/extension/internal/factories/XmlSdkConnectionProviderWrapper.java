/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.factories;

import static com.zernova.metadata.java.api.utils.JavaTypeUtils.getType;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.internal.event.NullEventFactory.getNullEvent;

import static org.apache.commons.beanutils.BeanUtils.getProperty;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.ComponentParameterAst;
import com.zernova.runtime.ast.api.MetadataTypeAdapter;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.connection.ConnectionUtils;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.module.extension.api.runtime.config.ConnectionProviderObjectBuilder;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.config.dsl.connection.ConnectionProviderObjectFactory;
import com.zernova.runtime.module.extension.internal.config.dsl.parameter.TopLevelParameterObjectFactory;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ConnectionProviderResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.StaticValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.TypeSafeValueResolverWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;

import jakarta.inject.Inject;

/**
 * Base class responsible of instantiating and managing lifecycle of the actual connection provider (the {@code delegate}) within
 * the XML-SDK connector.
 *
 * @param <C> the actual type of connection of the {@code delegate}.
 *
 * @since 4.7
 */
public abstract class XmlSdkConnectionProviderWrapper<C> implements ConnectionProvider<C>, Lifecycle {

  private static final Logger LOGGER = getLogger(XmlSdkConnectionProviderWrapper.class);

  private final ComponentAst innerConnectionProviderComponent;

  private final Function<ComponentParameterAst, Optional<String>> propertyUsage;

  private ConnectionProvider<C> delegate;

  @Inject
  private ZernovaContext muleContext;

  @Inject
  private ExpressionManager expressionManager;

  public XmlSdkConnectionProviderWrapper(ComponentAst innerConnectionProviderComponent,
                                         Function<ComponentParameterAst, Optional<String>> propertyUsage) {
    this.innerConnectionProviderComponent = innerConnectionProviderComponent;
    this.propertyUsage = propertyUsage;
  }

  private ConnectionProvider<C> buildAndPopulateDelegate() throws Exception {
    final ConnectionProviderObjectFactory connectionProviderObjectFactory =
        new ConnectionProviderObjectFactory(innerConnectionProviderComponent.getModel(ConnectionProviderModel.class).get(),
                                            innerConnectionProviderComponent.getExtensionModel(),
                                            null,
                                            null,
                                            null,
                                            muleContext);
    initialiseIfNeeded(connectionProviderObjectFactory, true, muleContext);
    connectionProviderObjectFactory.setParameters(resolveParameters(innerConnectionProviderComponent));
    ConnectionProviderResolver<C> connectionProviderResolver = connectionProviderObjectFactory.doGetObject();

    initialiseIfNeeded(connectionProviderResolver, true, muleContext);
    ConnectionProviderObjectBuilder<C> objectBuilder =
        connectionProviderResolver.getObjectBuilder().get();

    ResolverSetResult resolvedResolverSet =
        resolveResolverSet(connectionProviderResolver.getResolverSet().get());
    Pair<ConnectionProvider<C>, ResolverSetResult> built = objectBuilder.build(resolvedResolverSet);
    return built.getFirst();
  }

  private Map<String, Object> resolveParameters(final ComponentAst connectionProviderComponent)
      throws Exception {
    Map<String, Object> parameters = new HashMap<>();

    for (ComponentParameterAst p : connectionProviderComponent.getParameters()) {
      if (p.getRawValue() == null) {
        if (p.getValue().isRight() && p.getValue().getRight() instanceof ComponentAst) {
          ComponentAst complexParamValue = (ComponentAst) p.getValue().getRight();

          final MetadataType type = complexParamValue.getModel(MetadataTypeAdapter.class).get().getType();
          parameters.put(p.getModel().getName(),
                         new StaticValueResolver<>(buildComplexParam(type,
                                                                     resolveParameters(complexParamValue))));
        }
        continue;
      }

      final Optional<String> propertyNameOpt = propertyUsage.apply(p);
      // do not use the functional idiom so we don clutter the code with handling the exception from BeanUtils.getProperty
      if (propertyNameOpt.isPresent()) {
        String propertyName = propertyNameOpt.get();
        parameters.put(p.getModel().getName(),
                       paramValueResolver(getProperty(this, propertyName),
                                          p.getModel().getType(),
                                          p.getModel().getDefaultValue()));
      }
    }

    return parameters;
  }

  private Object buildComplexParam(MetadataType type, Map<String, Object> parameters) throws Exception {
    final TopLevelParameterObjectFactory topLevelParameterObjectFactory =
        new TopLevelParameterObjectFactory((ObjectType) type, muleContext.getExecutionClassLoader(), muleContext);
    initialiseIfNeeded(topLevelParameterObjectFactory, true, muleContext);

    topLevelParameterObjectFactory.setParameters(parameters);

    return topLevelParameterObjectFactory.doGetObject();
  }

  private ValueResolver paramValueResolver(Object paramValue, final MetadataType type, Object defaultValue) {
    final StaticValueResolver staticDelegate = new StaticValueResolver<>(paramValue != null
        ? paramValue
        : defaultValue);
    return new TypeSafeValueResolverWrapper<>(staticDelegate, getType(type));
  }

  private ResolverSetResult resolveResolverSet(ResolverSet resolverSet)
      throws ZernovaException {
    ResolverSetResult result;
    CoreEvent initializerEvent = getNullEvent();
    try (ValueResolvingContext ctx = ValueResolvingContext.builder(initializerEvent, expressionManager).build()) {
      result = resolverSet.resolve(ctx);
    } finally {
      ((BaseEventContext) initializerEvent.getContext()).success();
    }
    return result;
  }

  @Override
  public void initialise() throws InitialisationException {
    // delegate has to be built here, it cannot be received at construction since it needs to be populated before initialization
    try {
      this.delegate = buildAndPopulateDelegate();
    } catch (Exception e) {
      throw new InitialisationException(e, this);
    }
    initialiseIfNeeded(delegate, true, muleContext);
  }

  @Override
  public void start() throws ZernovaException {
    startIfNeeded(delegate);
  }

  @Override
  public void stop() throws ZernovaException {
    stopIfNeeded(delegate);
  }

  @Override
  public void dispose() {
    disposeIfNeeded(delegate, LOGGER);
    this.delegate = null;
  }

  @Override
  public C connect() throws ConnectionException {
    return ConnectionUtils.connect(delegate);
  }

  @Override
  public ConnectionValidationResult validate(C connection) {
    return delegate.validate(connection);
  }

  @Override
  public void disconnect(C connection) {
    delegate.disconnect(connection);
  }

}
