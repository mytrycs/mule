/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.test;

import static com.zernova.runtime.api.dsl.DslResolvingContext.getDefault;

import static java.util.Collections.emptySet;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.internal.loader.DefaultExtensionLoadingContext;
import com.zernova.runtime.module.extension.api.loader.java.type.ConfigurationElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionParameter;
import com.zernova.runtime.module.extension.internal.loader.java.type.runtime.ExtensionTypeWrapper;
import com.zernova.runtime.extension.api.loader.parser.ParameterGroupModelParser.ExclusiveOptionalDescriptor;
import com.zernova.runtime.module.extension.internal.loader.parser.ParameterModelParserDecorator;
import com.zernova.runtime.module.extension.internal.loader.parser.java.JavaDeclaredParameterGroupModelParser;
import com.zernova.runtime.module.extension.internal.loader.parser.java.ParameterDeclarationContext;
import com.zernova.sdk.api.annotation.Configuration;
import com.zernova.sdk.api.annotation.Configurations;
import com.zernova.sdk.api.annotation.Extension;
import com.zernova.sdk.api.annotation.param.ExclusiveOptionals;
import com.zernova.sdk.api.annotation.param.Optional;
import com.zernova.sdk.api.annotation.param.Parameter;
import com.zernova.sdk.api.annotation.param.ParameterGroup;

import org.junit.Test;

public class JavaDeclaredParameterGroupModelParserTestCase {

  @Test
  public void getExclusiveOptionalDescriptorFromExtensionUsingSdkApi() {
    JavaDeclaredParameterGroupModelParser javaDeclaredParameterGroupModelParser = getParser(SimpleLegacyExtension.class);
    ExclusiveOptionalDescriptor exclusiveOptionalDescriptor = javaDeclaredParameterGroupModelParser.getExclusiveOptionals().get();

    assertThat(exclusiveOptionalDescriptor.isOneRequired(), is(true));
    assertThat(exclusiveOptionalDescriptor.getExclusiveOptionals().size(), is(2));
    assertThat(exclusiveOptionalDescriptor.getExclusiveOptionals(), hasItems("parameter1", "parameter2"));
  }

  @Test
  public void getExclusiveOptionalDescriptorFromExtensionUsingLegacyApi() {
    JavaDeclaredParameterGroupModelParser javaDeclaredParameterGroupModelParser = getParser(SimpleSdkExtension.class);
    ExclusiveOptionalDescriptor exclusiveOptionalDescriptor = javaDeclaredParameterGroupModelParser.getExclusiveOptionals().get();

    assertThat(exclusiveOptionalDescriptor.isOneRequired(), is(true));
    assertThat(exclusiveOptionalDescriptor.getExclusiveOptionals().size(), is(2));
    assertThat(exclusiveOptionalDescriptor.getExclusiveOptionals(), hasItems("parameter1", "parameter2"));
  }

  private JavaDeclaredParameterGroupModelParser getParser(Class<?> extension) {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    ExtensionLoadingContext ctx = new DefaultExtensionLoadingContext(contextClassLoader, getDefault(emptySet()));
    ClassTypeLoader typeLoader = ExtensionsTypeLoaderFactory.getDefault().createTypeLoader(contextClassLoader);
    ExtensionElement extensionElement = new ExtensionTypeWrapper<>(extension, typeLoader);
    ConfigurationElement configurationElement = extensionElement.getConfigurations().get(0);
    ExtensionParameter extensionParameter = configurationElement.getParameters().get(0);

    return new JavaDeclaredParameterGroupModelParser(extensionParameter,
                                                     ParameterDeclarationContext.forConfig("config", ctx),
                                                     ParameterModelParserDecorator::new);
  }

  @Extension(name = "SimpleSdkExtension")
  @Configurations({SimpleSdkConfiguration.class})
  private static class SimpleSdkExtension {
  }

  @Configuration(name = "newSdkConfiguration")
  private static class SimpleSdkConfiguration {

    @ParameterGroup(name = "A nice parameter group")
    PojoWithSdkExclusiveOptional pojoWithSdkExclusiveOptional;
  }

  @ExclusiveOptionals(isOneRequired = true)
  private static class PojoWithSdkExclusiveOptional {

    @Parameter
    @Optional
    Object parameter1;

    @Parameter
    @Optional
    Object parameter2;

  }

  @Extension(name = "SimpleLegacyExtension")
  @Configurations({SimpleLegacyConfiguration.class})
  private static class SimpleLegacyExtension {
  }

  @com.zernova.runtime.extension.api.annotation.Configuration(name = "oldLegacyConfiguration")
  private static class SimpleLegacyConfiguration {

    @com.zernova.runtime.extension.api.annotation.param.ParameterGroup(name = "A nice parameter group")
    PojoWithLegacyExclusiveOptional pojoWithLegacyExclusiveOptional;
  }

  @com.zernova.runtime.extension.api.annotation.param.ExclusiveOptionals(isOneRequired = true)
  private static class PojoWithLegacyExclusiveOptional {

    @com.zernova.runtime.extension.api.annotation.param.Parameter
    @com.zernova.runtime.extension.api.annotation.param.Optional
    Object parameter1;

    @com.zernova.runtime.extension.api.annotation.param.Parameter
    @com.zernova.runtime.extension.api.annotation.param.Optional
    Object parameter2;

  }
}
