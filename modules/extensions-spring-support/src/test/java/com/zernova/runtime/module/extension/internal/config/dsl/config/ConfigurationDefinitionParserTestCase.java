/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.config;

import static com.zernova.runtime.api.dsl.DslResolvingContext.getDefault;
import static com.zernova.runtime.core.api.util.ClassUtils.setContextClassLoader;
import static com.zernova.test.module.extension.internal.util.ExtensionDeclarationTestUtils.declarerFor;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Optional.of;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;

import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.time.Time;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition.Builder;
import com.zernova.runtime.dsl.api.component.TypeConverter;
import com.zernova.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.internal.loader.DefaultExtensionLoadingContext;
import com.zernova.runtime.extension.internal.loader.ExtensionModelFactory;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionParsingContext;
import com.zernova.runtime.module.extension.internal.config.dsl.config.extension.SimpleExtension;
import com.zernova.tck.classlaoder.TestClassLoader;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

public class ConfigurationDefinitionParserTestCase extends AbstractZernovaTestCase {

  @Test
  public void typeConverterUsesTheClassloaderOfTheParser() throws Exception {
    List<ComponentBuildingDefinition> componentBuildingDefinitions;

    Thread thread = Thread.currentThread();
    ClassLoader currentClassLoader = thread.getContextClassLoader();
    TestClassLoader classLoader = getTestClassLoader();
    setContextClassLoader(thread, currentClassLoader, classLoader);
    try {
      Builder<?> definitionBuilder = new Builder<>().withIdentifier("test").withNamespace("namespace");

      ExtensionLoadingContext ctx = new DefaultExtensionLoadingContext(classLoader, getDefault(emptySet()));
      declarerFor(SimpleExtension.class, "1.0.0-dev", ctx);
      ExtensionModelFactory factory = new ExtensionModelFactory();
      ExtensionModel extensionModel = factory.create(ctx);
      ConfigurationModel configurationModel = extensionModel.getConfigurationModels().get(0);

      DslResolvingContext dslResolvingContext = getDefault(singleton(extensionModel));
      DslSyntaxResolver dslSyntaxResolver = DslSyntaxResolver.getDefault(extensionModel, dslResolvingContext);
      ExtensionParsingContext extensionParsingContext = new ExtensionParsingContext(extensionModel);

      ConfigurationDefinitionParser configurationDefinitionParser =
          new ConfigurationDefinitionParser(definitionBuilder, extensionModel, configurationModel, dslSyntaxResolver,
                                            extensionParsingContext,
                                            of(ExtensionsTypeLoaderFactory.getDefault().createTypeLoader()));

      componentBuildingDefinitions = configurationDefinitionParser.parse();
    } finally {
      setContextClassLoader(thread, classLoader, currentClassLoader);
    }

    Optional<TypeConverter<?, ?>> typeConverter = componentBuildingDefinitions.get(1).getTypeConverter();

    int testClassLoaderInvocations = classLoader.getInvocations().size();
    typeConverter.get().convert(null);
    List<Pair<String, String>> invocations = classLoader.getInvocations();
    String invocationArg = (invocations.get(invocations.size() - 1)).getSecond();

    assertThat(invocations, iterableWithSize(testClassLoaderInvocations + 2));
    assertThat(invocationArg, is("com.zernova.runtime.api.time.Time"));
  }

  private TestClassLoader getTestClassLoader() {
    TestClassLoader classLoader = new TestClassLoader(null);
    classLoader.addClass("java.util.Map", Map.class);
    classLoader.addClass("com.zernova.runtime.api.time.Time", Time.class);
    return classLoader;
  }

}
