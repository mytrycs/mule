/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver.resolver;

import static com.zernova.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static com.zernova.runtime.api.meta.ExpressionSupport.REQUIRED;
import static com.zernova.runtime.extension.privileged.util.ComponentDeclarationUtils.isTargetParameter;
import static com.zernova.runtime.module.extension.internal.loader.java.property.stackabletypes.StackedTypesModelProperty.getStackedTypesModelProperty;
import static com.zernova.runtime.module.extension.internal.runtime.resolver.StaticValueResolver.fromUnwrapped;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.isLiteral;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.isParameterResolver;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.isTypedValue;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.toDataType;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.isExpression;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.meta.ExpressionSupport;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.core.api.util.func.CheckedFunction;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.internal.loader.java.property.ExclusiveOptionalModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.property.stackabletypes.StackedTypesModelProperty;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ConfigurationProviderValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ExpressionBasedParameterResolverValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ExpressionTypedValueValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ParameterResolverValueResolverWrapper;
import com.zernova.runtime.module.extension.internal.runtime.resolver.RequiredParameterValueResolverWrapper;
import com.zernova.runtime.module.extension.internal.runtime.resolver.StaticLiteralValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.TypeSafeExpressionValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.TypedValueValueResolverWrapper;
import com.zernova.sdk.api.runtime.parameter.Literal;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * A Factory that creates different {@link ValueResolver} instances for different parameter types.
 *
 * @since 4.2
 */
public class ValueResolverFactory {

  public <T> ValueResolver<T> of(String parameterName, MetadataType expectedType, Object value, Object defaultValue,
                                 ExpressionSupport expressionSupport, boolean required,
                                 Set<ModelProperty> modelProperties) {
    return of(parameterName, expectedType, value, defaultValue, expressionSupport, required, modelProperties, true);
  }

  public <T> ValueResolver<T> of(String parameterName, MetadataType expectedType, Object value, Object defaultValue,
                                 ExpressionSupport expressionSupport, boolean required,
                                 Set<ModelProperty> modelProperties,
                                 boolean acceptsReferences) {
    if (value instanceof ValueResolver) {
      return (ValueResolver<T>) value;
    }

    ValueResolver<T> resolver;

    final Class<?> expectedClass = ExtensionMetadataTypeUtils.getType(expectedType).orElse(Object.class);

    if (expectedClass.equals(ConfigurationProvider.class)) {
      resolver = (ValueResolver<T>) new ConfigurationProviderValueResolver((String) value);
    } else if (isExpression(value)) {
      final String expression = (String) value;
      resolver = getExpressionBasedValueResolver(expectedType, expression, modelProperties, expectedClass);
      if (required || isRequiredByExclusiveOptional(modelProperties)) {
        resolver = new RequiredParameterValueResolverWrapper<>(resolver, parameterName, expression);
      }
    } else {
      resolver = getStaticValueResolver(parameterName, expectedType, value, defaultValue, modelProperties, acceptsReferences,
                                        expectedClass);
    }

    if (resolver.isDynamic() && expressionSupport == NOT_SUPPORTED) {
      throw new IllegalArgumentException(
                                         format("An expression value was given for parameter '%s' but it doesn't support expressions",
                                                parameterName));
    }

    if (!resolver.isDynamic() && expressionSupport == REQUIRED && required) {
      throw new IllegalArgumentException(
                                         format("A fixed value was given for parameter '%s' but it only supports expressions",
                                                parameterName));
    }

    return resolver;
  }

  /**
   * Uses the {@code params} function to obtain a value for the given {@code parameterGroupModel} and {@code parameterModel}. If
   * said value is {@code not null}, then the {@link ValueResolver} obtained through the {@code resolverFunction} is returned.
   *
   * Otherwise, {@link Optional#empty()} is returned
   *
   * @param params              a {@link BiFunction} to obtain the value of a parameter in a specific group. The group <b>MUST</b>
   *                            be obtained through the {@code model}
   * @param parameterGroupModel a {@link ParameterGroupModel}
   * @param parameterModel      a {@link ParameterModel} contained in the above {@code parameterGroupModel}
   * @param resolverFunction    a function that maps a value to a {@link ValueResolver}
   * @return an optional {@link ValueResolver}
   * @since 4.5.0
   */
  public Optional<ValueResolver> ofNullableParameter(BiFunction<ParameterGroupModel, ParameterModel, Object> params,
                                                     ParameterGroupModel parameterGroupModel,
                                                     ParameterModel parameterModel,
                                                     CheckedFunction<Object, ValueResolver> resolverFunction) {

    Object value = params.apply(parameterGroupModel, parameterModel);
    return value != null ? ofNullable(resolverFunction.apply(value)) : empty();
  }


  /**
   * Generates the {@link ValueResolver} for expression based values
   */
  private ValueResolver getExpressionBasedValueResolver(MetadataType expectedType, String value,
                                                        Set<ModelProperty> modelProperties,
                                                        Class<?> expectedClass) {
    ValueResolver resolver;
    Optional<StackedTypesModelProperty> stackedTypesModelProperty = getStackedTypesModelProperty(modelProperties);
    if (stackedTypesModelProperty.isPresent()) {
      resolver = stackedTypesModelProperty.get().getValueResolverFactory().getExpressionBasedValueResolver(value, expectedClass);
      // TODO MULE-13518: Add support for stacked value resolvers for @Parameter inside pojos // The following "IFs" should be
      // removed once implemented
    } else if (isParameterResolver(expectedType)) {
      resolver = new ExpressionBasedParameterResolverValueResolver<>(value, expectedClass, toDataType(expectedType));
    } else if (isTypedValue(expectedType)) {
      resolver = new ExpressionTypedValueValueResolver<>(value, expectedClass);
    } else if (isLiteral(expectedType) || isTargetParameter(modelProperties)) {
      resolver = new StaticLiteralValueResolver<>(value, expectedClass);
    } else {
      resolver = new TypeSafeExpressionValueResolver<>(value, expectedClass, toDataType(expectedType));
    }
    return resolver;
  }

  /**
   * Generates the {@link ValueResolver} for non expression based values
   */
  private ValueResolver getStaticValueResolver(String parameterName, MetadataType expectedType, Object value, Object defaultValue,
                                               Set<ModelProperty> modelProperties, boolean acceptsReferences,
                                               Class<?> expectedClass) {

    Optional<StackedTypesModelProperty> optionalStackedTypeModelProperty = getStackedTypesModelProperty(modelProperties);

    if (optionalStackedTypeModelProperty.isPresent()) {
      StackedTypesModelProperty property = optionalStackedTypeModelProperty.get();
      Optional<ValueResolver> optionalResolver =
          property.getValueResolverFactory().getStaticValueResolver(value, Literal.class);
      if (optionalResolver.isPresent()) {
        return optionalResolver.get();
      }
    }

    if (isLiteral(expectedType)) {
      return new StaticLiteralValueResolver<>(value != null ? value.toString() : null, expectedClass);
    }

    ValueResolver resolver;
    resolver = value != null
        ? getValueResolverFromMetadataType(parameterName, expectedType, value, defaultValue, acceptsReferences, expectedClass)
        : fromUnwrapped(defaultValue);

    if (optionalStackedTypeModelProperty.isPresent()) {
      resolver = optionalStackedTypeModelProperty.get().getValueResolverFactory().getWrapperValueResolver(resolver);
    } else if (isParameterResolver(expectedType)) {
      resolver = new ParameterResolverValueResolverWrapper(resolver);
    } else if (isTypedValue(expectedType)) {
      resolver = new TypedValueValueResolverWrapper(resolver);
    }

    return resolver;
  }

  private boolean isRequiredByExclusiveOptional(Set<ModelProperty> modelProperties) {
    return modelProperties.stream().anyMatch(modelProperty -> modelProperty instanceof ExclusiveOptionalModelProperty
        && ((ExclusiveOptionalModelProperty) modelProperty).isOneRequired());
  }

  private ValueResolver getValueResolverFromMetadataType(String paramName, MetadataType expected, Object value,
                                                         Object defaultValue, boolean acceptsReferences, Class<?> expectedClass) {
    ValueResolverFactoryTypeVisitor visitor =
        new ValueResolverFactoryTypeVisitor(paramName, value, defaultValue, acceptsReferences,
                                            expectedClass);
    expected.accept(visitor);
    return visitor.getResolver();
  }
}
