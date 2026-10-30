/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.getMuleVersion;
import static com.zernova.runtime.core.internal.util.FunctionalUtils.withNullEvent;
import static com.zernova.runtime.extension.api.ExtensionConstants.POLLING_SOURCE_LIMIT_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.ExtensionConstants.SCHEDULING_STRATEGY_PARAMETER_NAME;
import static com.zernova.runtime.module.extension.internal.runtime.source.legacy.SourceTransactionalActionUtils.toLegacy;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.injectComponentLocation;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.injectDefaultEncoding;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.injectRefName;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.injectRuntimeVersion;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.api.scheduler.SchedulingStrategy;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.source.scheduler.FixedFrequencyScheduler;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.loader.ParameterGroupDescriptor;
import com.zernova.runtime.module.extension.internal.runtime.objectbuilder.ResolverSetBasedObjectBuilder;
import com.zernova.runtime.module.extension.internal.runtime.source.legacy.SdkSourceAdapterFactory;
import com.zernova.runtime.module.extension.internal.runtime.source.poll.PollingSourceWrapper;
import com.zernova.sdk.api.runtime.source.PollingSource;
import com.zernova.sdk.api.runtime.source.Source;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves and injects the values of a {@link Source} that has fields annotated with {@link Parameter} or
 * {@link ParameterGroupDescriptor}
 *
 * @since 4.0
 */
public final class SourceConfigurer {

  private final SourceModel model;
  private final ResolverSet resolverSet;
  private final ComponentLocation componentLocation;
  private final ExpressionManager expressionManager;
  private final ZernovaContext muleContext;
  private final boolean restarting;

  /**
   * Create a new instance
   *
   * @param model             the {@link SourceModel} which describes the instances that the {@link #configure(Source, Optional)}
   *                          method will accept
   * @param resolverSet       the {@link ResolverSet} used to resolve the parameters
   * @param expressionManager the {@link ExpressionManager} used to create a session used to evaluate the attributes.
   * @param muleContext       the current {@link ZernovaContext}
   */
  public SourceConfigurer(SourceModel model, ComponentLocation componentLocation, ResolverSet resolverSet,
                          ExpressionManager expressionManager, ZernovaContext muleContext) {
    this(model, componentLocation, resolverSet, expressionManager, muleContext, false);
  }

  /**
   * Create a new instance
   *
   * @param model             the {@link SourceModel} which describes the instances that the {@link #configure(Source, Optional)}
   *                          method will accept
   * @param resolverSet       the {@link ResolverSet} used to resolve the parameters
   * @param expressionManager the {@link ExpressionManager} used to create a session used to evaluate the attributes.
   * @param properties        deployment configuration properties
   * @param muleContext       the current {@link ZernovaContext}
   * @param restarting        indicates if the source is being created after a restart or not.
   */
  public SourceConfigurer(SourceModel model, ComponentLocation componentLocation, ResolverSet resolverSet,
                          ExpressionManager expressionManager, ZernovaContext muleContext,
                          boolean restarting) {
    this.model = model;
    this.resolverSet = resolverSet;
    this.componentLocation = componentLocation;
    this.expressionManager = expressionManager;
    this.muleContext = muleContext;
    this.restarting = restarting;
  }

  /**
   * Performs the configuration of the given {@code source} and returns the result
   *
   * @param source a {@link Source}
   * @param config the {@link ConfigurationInstance config instance} associated to {@code this} source object.
   * @return the configured instance
   * @throws ZernovaException
   */
  public Source configure(Object source, Optional<ConfigurationInstance> config) {
    ResolverSetBasedObjectBuilder<Object> builder =
        new ResolverSetBasedObjectBuilder<Object>(source.getClass(), model, resolverSet, expressionManager, muleContext) {

          @Override
          protected Object instantiateObject() {
            return source;
          }

          @Override
          public Object build(ValueResolvingContext context) throws ZernovaException {
            Object builtSource = build(buildResolverSetResult(source, context));
            injectDefaultEncoding(model, builtSource, muleContext.getConfiguration().getDefaultEncoding());
            injectRuntimeVersion(model, builtSource, getMuleVersion());
            injectComponentLocation(builtSource, componentLocation);
            config.ifPresent(c -> injectRefName(builtSource, c.getName(), getReflectionCache()));
            return builtSource;
          }

        };

    return withNullEvent(event -> {
      try (ValueResolvingContext context = ValueResolvingContext.builder(event, expressionManager).withConfig(config).build()) {
        Object configuredSource = builder.build(context);

        Source sdkSource = SdkSourceAdapterFactory.createAdapter(configuredSource);

        if (sdkSource instanceof PollingSource) {
          SchedulingStrategy scheduler;
          ValueResolver<?> valueResolver = resolverSet.getResolvers().get(SCHEDULING_STRATEGY_PARAMETER_NAME);
          if (valueResolver == null) {
            scheduler = new FixedFrequencyScheduler(60000, 0, MILLISECONDS);
          } else {
            scheduler = (SchedulingStrategy) valueResolver.resolve(context);
          }
          sdkSource = new PollingSourceWrapper<>((PollingSource) sdkSource, scheduler,
                                                 resolverMaxItemsPerPoll(resolverSet, context, event),
                                                 muleContext.getExceptionListener());
        }

        return sdkSource;
      } catch (Exception e) {
        throw new ZernovaRuntimeException(createStaticMessage("Exception was found trying to configure source of type "
            + source.getClass().getName()), e);
      }
    });
  }

  private int resolverMaxItemsPerPoll(ResolverSet resolverSet, ValueResolvingContext context, CoreEvent event)
      throws ZernovaException {
    ValueResolver<?> valueResolver = resolverSet.getResolvers().get(POLLING_SOURCE_LIMIT_PARAMETER_NAME);
    if (valueResolver == null) {
      return Integer.MAX_VALUE;
    } else {
      int maxItemsPerPoll = (Integer) valueResolver.resolve(context);
      if (maxItemsPerPoll < 1) {
        throw new IllegalArgumentException(format("The %s parameter must have a value greater than 1",
                                                  POLLING_SOURCE_LIMIT_PARAMETER_NAME));
      }
      return maxItemsPerPoll;
    }
  }

  private ResolverSetResult buildResolverSetResult(Object source, ValueResolvingContext context) throws ZernovaException {
    ResolverSetResult resolverSetResult = resolverSet.resolve(context);
    Optional<Field> field = Arrays.stream(source.getClass().getDeclaredFields())
        .filter(f -> f.getType().getName().equals(com.zernova.runtime.extension.api.tx.SourceTransactionalAction.class.getName()))
        .findFirst();
    if (field.isPresent() && resolverSet.getResolvers().get(field.get().getName()) != null) {
      return overwriteResolverResult(field.get().getName(), resolverSetResult);
    } else {
      return resolverSetResult;
    }
  }

  private ResolverSetResult overwriteResolverResult(String fieldName, ResolverSetResult resolverSetResult) {
    ResolverSetResult.Builder builder = ResolverSetResult.newBuilder();
    for (Map.Entry<String, Object> entry : resolverSetResult.asMap().entrySet()) {
      if (entry.getKey().equals(fieldName)) {
        builder.add(entry.getKey(), toLegacy(entry.getValue()));
      } else {
        builder.add(entry.getKey(), entry.getValue());
      }
    }
    return builder.build();
  }
}
