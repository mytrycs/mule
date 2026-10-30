/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java;

import static com.zernova.runtime.api.metadata.DataType.fromType;

import static java.lang.String.format;

import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterDeclarer;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.extension.api.exception.IllegalParameterModelDefinitionException;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.runtime.parameter.Literal;
import com.zernova.runtime.extension.api.runtime.parameter.ParameterResolver;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionParameter;
import com.zernova.runtime.module.extension.api.loader.java.type.Type;
import com.zernova.runtime.module.extension.api.loader.java.type.TypeGeneric;
import com.zernova.runtime.module.extension.internal.loader.java.property.stackabletypes.StackableType;
import com.zernova.runtime.module.extension.internal.loader.java.property.stackabletypes.StackedTypesModelProperty;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ExpressionBasedParameterResolverValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ExpressionTypedValueValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ParameterResolverValueResolverWrapper;
import com.zernova.runtime.module.extension.internal.runtime.resolver.StaticLiteralValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.StaticParameterResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.StaticValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.TypedValueValueResolverWrapper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves the {@link ModelProperty model properties} used to manage parameters defined as {@link StackableType wrapper types}.
 * <p>
 * This class is based on what used to be the {@code StackableTypesParameterContributor} in the 4.1 to 4.4.x version range.
 *
 * @since 4.5
 */
class StackableTypesModelPropertyResolver {

  public static StackableTypesModelPropertyResolver newInstance(ExtensionLoadingContext loadingContext) {
    return StackableTypesModelPropertyResolver.builder()
        .addType(StackableType
            .builder(ParameterResolver.class, loadingContext.getTypeLoader())
            .setStaticResolverFactory(value -> new StaticValueResolver<>(new StaticParameterResolver<>(value)))
            .setDelegateResolverFactory(resolver -> new ParameterResolverValueResolverWrapper(resolver))
            .setExpressionBasedResolverFactory((value,
                                                expectedType) -> new ExpressionBasedParameterResolverValueResolver(value,
                                                                                                                   expectedType,
                                                                                                                   fromType(expectedType)))
            .build())
        .addType(StackableType
            .builder(TypedValue.class, loadingContext.getTypeLoader())
            .setStaticResolverFactory(value -> new StaticValueResolver<>(new TypedValue<>(value, DataType.fromObject(value))))
            .setDelegateResolverFactory(valueResolver -> new TypedValueValueResolverWrapper(valueResolver))
            .setExpressionBasedResolverFactory((expression, expectedType) -> new ExpressionTypedValueValueResolver(expression,
                                                                                                                   expectedType))
            .build())
        .addType(StackableType
            .builder(Literal.class, loadingContext.getTypeLoader())
            .setExpressionBasedResolverFactory((expression, expectedType) -> new StaticLiteralValueResolver(expression,
                                                                                                            expectedType))
            .setStaticResolverFactory((value) -> new StaticLiteralValueResolver(value.toString(), value.getClass()))
            .build())
        .addType(StackableType
            .builder(com.zernova.sdk.api.runtime.parameter.ParameterResolver.class, loadingContext.getTypeLoader())
            .setStaticResolverFactory(value -> new StaticValueResolver<>(new StaticParameterResolver<>(value)))
            .setDelegateResolverFactory(resolver -> new ParameterResolverValueResolverWrapper(resolver))
            .setExpressionBasedResolverFactory((value,
                                                expectedType) -> new ExpressionBasedParameterResolverValueResolver(value,
                                                                                                                   expectedType,
                                                                                                                   fromType(expectedType)))
            .build())
        .addType(StackableType
            .builder(com.zernova.sdk.api.runtime.parameter.Literal.class, loadingContext.getTypeLoader())
            .setExpressionBasedResolverFactory((expression, expectedType) -> new StaticLiteralValueResolver(expression,
                                                                                                            expectedType))
            .setStaticResolverFactory((value) -> new StaticLiteralValueResolver(value.toString(), value.getClass()))
            .build())
        .build();
  }

  private final Map<Type, StackableType> stackableTypes;

  private StackableTypesModelPropertyResolver(Map<Type, StackableType> stackableTypes) {
    this.stackableTypes = stackableTypes;
  }

  /**
   * Contributes to a {@link ParameterDeclarer} if the type of the given parameter is one of the registered as
   * {@link StackableType wrapper types}
   *
   * @param parameter          {@link ExtensionParameter} with introspected information of the Java parameter
   * @param declarationContext context of the parameter to be declared
   */
  public List<ModelProperty> resolveStackableProperties(ExtensionParameter parameter,
                                                        ParameterDeclarationContext declarationContext) {
    List<ModelProperty> properties = new LinkedList<>();
    LazyValue<StackedTypesModelProperty.Builder> stackedTypesModelPropertyBuilder =
        new LazyValue<>(StackedTypesModelProperty::builder);

    doResolve(parameter, declarationContext, parameter.getType(), stackedTypesModelPropertyBuilder);
    stackedTypesModelPropertyBuilder.ifComputed(builder -> properties.add(builder.build()));

    return properties;
  }

  private void doResolve(ExtensionParameter extensionParameter,
                         ParameterDeclarationContext declarationContext,
                         Type resolvableType,
                         LazyValue<StackedTypesModelProperty.Builder> builder) {
    getStackableType(resolvableType)
        .ifPresent(stackableType -> {
          List<TypeGeneric> generics = resolvableType.getGenerics();
          if (!generics.isEmpty()) {
            builder.get().addType(stackableType);
            doResolve(extensionParameter, declarationContext, generics.get(0).getConcreteType(), builder);
          } else {
            throw new IllegalParameterModelDefinitionException(
                                                               format(
                                                                      "The parameter [%s] from the %s [%s] doesn't specify the %s parameterized type",
                                                                      extensionParameter.getName(),
                                                                      declarationContext.getComponentType(),
                                                                      declarationContext.getComponentName(),
                                                                      extensionParameter.getType()));
          }
        });

  }

  private Optional<StackableType> getStackableType(Type type) {
    return stackableTypes.entrySet().stream().filter(entry -> entry.getKey().isSameType(type)).map(Map.Entry::getValue)
        .findFirst();
  }

  private static Builder builder() {
    return new Builder();
  }

  private static class Builder {

    private final Map<Type, StackableType> stackableTypes = new HashMap<>();

    public Builder addType(StackableType stackableType) {
      stackableTypes.put(stackableType.getType(), stackableType);
      return this;
    }

    public StackableTypesModelPropertyResolver build() {
      return new StackableTypesModelPropertyResolver(stackableTypes);
    }
  }

}
