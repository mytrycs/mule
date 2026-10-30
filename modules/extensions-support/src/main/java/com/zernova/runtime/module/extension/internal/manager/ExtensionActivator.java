/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.manager;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.metadata.DataType.fromFunction;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static com.zernova.runtime.core.privileged.registry.LegacyRegistryUtils.registerObject;
import static com.zernova.runtime.module.extension.internal.lifecycle.ExtensionOnZernovaContextDisposedNotificationListener.registerLifecycleListenerForOnContextDisposed;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getParameterClasses;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getSubtypeClasses;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getClassLoader;

import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.api.el.ExpressionModule;
import com.zernova.runtime.api.el.ModuleNamespace;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.function.FunctionModel;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.internal.el.DefaultBindingContextBuilder;
import com.zernova.runtime.core.internal.el.DefaultExpressionModuleBuilder;
import com.zernova.runtime.core.internal.transformer.simple.StringToEnum;
import com.zernova.runtime.core.privileged.el.GlobalBindingContextProvider;
import com.zernova.runtime.extension.api.runtime.function.FunctionExecutor;
import com.zernova.runtime.extension.api.runtime.function.FunctionParameterDefaultValueResolverFactory;
import com.zernova.runtime.module.extension.internal.loader.java.property.FunctionExecutorModelProperty;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Activator implementation for registered {@link ExtensionModel extensions}
 *
 * @since 4.0
 */
public final class ExtensionActivator implements Startable, Stoppable {

  private final ZernovaContext muleContext;
  private final Set<Class<? extends Enum>> enumTypes = new HashSet<>();
  private final List<Object> lifecycleAwareElements = new LinkedList<>();

  ExtensionActivator(ZernovaContext muleContext) {
    this.muleContext = muleContext;
  }

  void activateExtension(ExtensionModel extensionModel) {
    registerEnumTransformers(extensionModel);
    registerAsModuleDefinition(extensionModel);
    registerLifecycleListenerForOnContextDisposed(muleContext.getNotificationManager(),
                                                  muleContext.getExecutionClassLoader(),
                                                  extensionModel);
  }

  private void registerEnumTransformers(ExtensionModel extensionModel) {
    ClassLoader classLoader = getClassLoader(extensionModel);
    Set<Class<?>> parameterClasses = new HashSet<>();

    parameterClasses.addAll(getParameterClasses(extensionModel, classLoader));
    parameterClasses.addAll(getSubtypeClasses(extensionModel, classLoader));

    parameterClasses.stream()
        .filter(type -> Enum.class.isAssignableFrom(type))
        .forEach(type -> {
          final Class<Enum> enumClass = (Class<Enum>) type;
          if (enumTypes.add(enumClass)) {
            try {
              StringToEnum stringToEnum = new StringToEnum(enumClass);
              registerObject(muleContext, stringToEnum.getName(), stringToEnum);
            } catch (ZernovaException e) {
              throw new ZernovaRuntimeException(createStaticMessage("Could not register transformer for enum "
                  + enumClass.getName()), e);
            }
          }
        });
  }

  private void registerAsModuleDefinition(ExtensionModel extensionModel) {
    if (extensionModel.getFunctionModels().isEmpty() && extensionModel.getTypes().isEmpty()) {
      return;
    }

    ExpressionModule expressionModule = extensionAsModuleDefinition(extensionModel);
    try {
      final BindingContext bindingContext = new DefaultBindingContextBuilder().addModule(expressionModule).build();
      registerObject(muleContext, extensionModel.getName() + "GlobalBindingContextProvider",
                     (GlobalBindingContextProvider) () -> bindingContext);
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage(e.getMessage()), e);
    }
  }

  private ExpressionModule extensionAsModuleDefinition(ExtensionModel extensionModel) {
    ModuleNamespace namespace = new ModuleNamespace(extensionModel.getXmlDslModel().getPrefix());
    ExpressionModule.Builder moduleBuilder = new DefaultExpressionModuleBuilder(namespace);

    if (!extensionModel.getFunctionModels().isEmpty()) {
      addExtensionFunctions(extensionModel.getFunctionModels().stream(), moduleBuilder);
    }
    if (!extensionModel.getTypes().isEmpty()) {
      addExtensionTypes(extensionModel.getTypes().stream(), moduleBuilder);
    }

    return moduleBuilder.build();
  }

  private void addExtensionTypes(Stream<ObjectType> objectTypes, ExpressionModule.Builder module) {
    objectTypes.forEach(module::addType);
  }

  private void addExtensionFunctions(Stream<FunctionModel> functions, ExpressionModule.Builder module) {
    final FunctionParameterDefaultValueResolverFactory valueResolverFactory = (defaultValue, type) -> context -> {
      ExtendedExpressionManager em = muleContext.getExpressionManager();
      String value = String.valueOf(defaultValue);
      return em.isExpression(value) ? em.evaluate(value, type, context) : new TypedValue<>(defaultValue, type);
    };

    functions.forEach(function -> function.getModelProperty(FunctionExecutorModelProperty.class).ifPresent(mp -> {
      FunctionExecutor executor = mp.getExecutorFactory().createExecutor(function, valueResolverFactory);
      lifecycleAwareElements.add(executor);
      module.addBinding(function.getName(), new TypedValue<>(executor, fromFunction(executor)));
    }));
  }

  @Override
  public void start() throws ZernovaException {
    for (Object element : lifecycleAwareElements) {
      initialiseIfNeeded(element, muleContext);
      startIfNeeded(element);
    }
  }

  @Override
  public void stop() throws ZernovaException {
    for (Object element : lifecycleAwareElements) {
      stopIfNeeded(element);
    }
    lifecycleAwareElements.clear();
    enumTypes.clear();
  }

  public Set<Class<? extends Enum>> getEnumTypes() {
    return enumTypes;
  }
}
