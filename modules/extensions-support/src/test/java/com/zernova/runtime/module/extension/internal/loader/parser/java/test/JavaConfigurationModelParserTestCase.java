/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.test;

import static com.zernova.runtime.module.extension.internal.loader.parser.java.test.MinZernovaVersionTestUtils.ctxResolvingMinZernovaVersion;

import static java.lang.String.format;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.rules.ExpectedException.none;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.module.extension.api.loader.java.type.ConfigurationElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionElement;
import com.zernova.runtime.module.extension.internal.loader.java.type.runtime.ExtensionTypeWrapper;
import com.zernova.runtime.module.extension.internal.loader.parser.java.JavaConfigurationModelParser;
import com.zernova.runtime.module.extension.internal.loader.parser.java.JavaExtensionModelParser;
import com.zernova.sdk.api.annotation.Configuration;
import com.zernova.sdk.api.annotation.Configurations;
import com.zernova.sdk.api.annotation.Extension;
import com.zernova.sdk.api.annotation.NoImplicit;
import com.zernova.sdk.api.annotation.Operations;
import com.zernova.sdk.api.annotation.Sources;
import com.zernova.sdk.api.annotation.connectivity.ConnectionProviders;
import com.zernova.sdk.api.connectivity.ConnectionProvider;
import com.zernova.sdk.api.connectivity.ConnectionValidationResult;
import com.zernova.sdk.api.runtime.source.Source;
import com.zernova.sdk.api.runtime.source.SourceCallback;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JavaConfigurationModelParserTestCase {

  @Rule
  public ExpectedException expectedException = none();

  @Test
  public void getConfigurationNameFromConfigurationUsingSdkApi() {
    JavaConfigurationModelParser javaConfigurationModelParser = getParser(SimpleSdkExtension.class, SimpleSdkConfiguration.class);
    assertThat(javaConfigurationModelParser.getName(), is("newSdkConfiguration"));
  }

  @Test
  public void getConfigurationNameFromConfigurationUsingLegacyApi() {
    JavaConfigurationModelParser javaConfigurationModelParser =
        getParser(SimpleLegacyExtension.class, SimpleLegacyConfiguration.class);
    assertThat(javaConfigurationModelParser.getName(), is("oldLegacyConfiguration"));
  }

  @Test
  public void getConfigurationNameFromConfigurationUsingSdkAndLegacyApi() {
    expectedException.expect(instanceOf(IllegalModelDefinitionException.class));
    expectedException.expectMessage("Annotations com.zernova.runtime.extension.api.annotation.Configuration and " +
        "com.zernova.sdk.api.annotation.Configuration are both present at the same time on Configuration SimpleWronglyAnnotatedConfiguration");

    JavaConfigurationModelParser javaConfigurationModelParser =
        getParser(SimpleSdkExtensionWithWronglyAnnotatedConfiguration.class, SimpleWronglyAnnotatedConfiguration.class);
    javaConfigurationModelParser.getName();
  }

  @Test
  public void isForceNoImplicitOnConfigurationUsingTheSdkApi() {
    JavaConfigurationModelParser javaConfigurationModelParser = getParser(SimpleSdkExtension.class, SimpleSdkConfiguration.class);

    assertThat(javaConfigurationModelParser.isForceNoImplicit(), is(true));
  }

  @Test
  public void isForceNoImplicitOnConfigurationUsingTheLegacyApi() {
    JavaConfigurationModelParser javaConfigurationModelParser =
        getParser(SimpleLegacyExtension.class, SimpleLegacyConfiguration.class);

    assertThat(javaConfigurationModelParser.isForceNoImplicit(), is(true));
  }

  @Test
  public void getMMVForLegacyImplicitConfiguration() {
    JavaConfigurationModelParser javaConfigurationModelParser =
        getParser(LegacyAnnotationsExtension.class, NoImplicitLegacyConfiguration.class);
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getMinZernovaVersion().toString(), is("4.3"));
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getReason(),
               is("Configuration NoImplicitLegacyConfiguration has min mule version 4.3 because it is annotated with NoImplicit. NoImplicit was introduced in Mule 4.3."));
  }

  @Test
  public void getMMVForSdkImplicitConfiguration() {
    JavaConfigurationModelParser javaConfigurationModelParser =
        getParser(LegacyAnnotationsExtension.class, NoImplicitSdkConfiguration.class);
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getMinZernovaVersion().toString(), is("4.5.0"));
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getReason(),
               is("Configuration NoImplicitSdkConfiguration has min mule version 4.5.0 because it is annotated with NoImplicit. NoImplicit was introduced in Mule 4.5.0."));
  }

  @Test
  public void getMMVForLegacyAnnotationConfiguration() {
    JavaConfigurationModelParser javaConfigurationModelParser =
        getParser(LegacyAnnotationsExtension.class, LegacyAnnotationConfiguration.class);
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getMinZernovaVersion().toString(), is("4.1.1"));
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getReason(),
               is("Configuration LegacyAnnotationConfiguration has min mule version 4.1.1 because it is the default value."));
  }

  @Test
  public void getMMVForSdkAnnotationConfiguration() {
    JavaConfigurationModelParser javaConfigurationModelParser =
        getParser(LegacyAnnotationsExtension.class, SdkAnnotationConfiguration.class);
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getMinZernovaVersion().toString(), is("4.5.0"));
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getReason(),
               is("Configuration SdkAnnotationConfiguration has min mule version 4.5.0 because it is annotated with Configuration. Configuration was introduced in Mule 4.5.0."));
  }

  @Test
  public void getMMVForParameterizedConfiguration() {
    JavaConfigurationModelParser javaConfigurationModelParser =
        getParser(LegacyAnnotationsExtension.class, ParameterizedConfiguration.class);
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getMinZernovaVersion().toString(), is("4.4"));
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getReason(),
               is("Configuration ParameterizedConfiguration has min mule version 4.4 because of its field configField. Field configField has min mule version 4.4 because it is annotated with Parameter. Parameter was introduced in Mule 4.4."));
  }

  @Test
  public void getMMVForExtendsParameterizedConfiguration() {
    JavaConfigurationModelParser javaConfigurationModelParser =
        getParser(LegacyAnnotationsExtension.class, ExtendsParameterizedConfiguration.class);
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getMinZernovaVersion().toString(), is("4.4"));
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getReason(),
               is("Configuration ExtendsParameterizedConfiguration has min mule version 4.4 because of its super class ParameterizedConfiguration. Configuration ParameterizedConfiguration has min mule version 4.4 because of its field configField. Field configField has min mule version 4.4 because it is annotated with Parameter. Parameter was introduced in Mule 4.4."));
  }

  @Test
  public void getMMVForAnnotatedConfiguration() {
    JavaConfigurationModelParser javaConfigurationModelParser =
        getParser(LegacyAnnotationsExtension.class, AnnotatedConfiguration.class);
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getMinZernovaVersion().toString(), is("4.1.1"));
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getReason(),
               is("Configuration AnnotatedConfiguration has min mule version 4.1.1 because it is the default value."));
  }

  @Test
  public void getMMVForConfigurationFromExtensionWithSdkConfigurationsAnnotation() {
    JavaConfigurationModelParser javaConfigurationModelParser =
        getParser(SimpleLegacyExtension.class, SimpleLegacyConfiguration.class);
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getMinZernovaVersion().toString(), is("4.5.0"));
    assertThat(javaConfigurationModelParser.getResolvedMinZernovaVersion().get().getReason(),
               is("Configuration SimpleLegacyConfiguration has min mule version 4.5.0 because it was propagated from the annotation (either @Configurations or @Config) used to reference this configuration."));
  }

  protected JavaConfigurationModelParser getParser(Class<?> extension, Class<?> configuration) {
    ClassTypeLoader typeLoader =
        ExtensionsTypeLoaderFactory.getDefault().createTypeLoader(Thread.currentThread().getContextClassLoader());
    ExtensionElement extensionElement = new ExtensionTypeWrapper<>(extension, typeLoader);
    ConfigurationElement configurationElement = extensionElement.getConfigurations().stream()
        .filter(conf -> conf.getTypeName().equals(configuration.getName())).findFirst()
        .orElseThrow(() -> new IllegalStateException(format("Configuration %s was not found among the declared configuration in the extension",
                                                            configuration.getName(), extension.getName())));

    ExtensionLoadingContext ctx = ctxResolvingMinZernovaVersion();
    JavaExtensionModelParser javaExtensionModelParser = new JavaExtensionModelParser(extensionElement, ctx);
    return new JavaConfigurationModelParser(javaExtensionModelParser, extensionElement, configurationElement, ctx);
  }

  @Extension(name = "SimpleSdkExtension")
  @Configurations({SimpleSdkConfiguration.class})
  private static class SimpleSdkExtension {
  }

  @Configuration(name = "newSdkConfiguration")
  @NoImplicit
  private static class SimpleSdkConfiguration {
  }

  @Extension(name = "SimpleLegacyExtension")
  @Configurations({SimpleLegacyConfiguration.class})
  private static class SimpleLegacyExtension {
  }

  @com.zernova.runtime.extension.api.annotation.Configuration(name = "oldLegacyConfiguration")
  @com.zernova.runtime.extension.api.annotation.NoImplicit
  private static class SimpleLegacyConfiguration {
  }

  @com.zernova.runtime.extension.api.annotation.Extension(name = "SimpleLegacyExtensionWithOperations")
  @com.zernova.runtime.extension.api.annotation.Configurations({NoImplicitLegacyConfiguration.class,
      NoImplicitSdkConfiguration.class, LegacyAnnotationConfiguration.class, SdkAnnotationConfiguration.class,
      ParameterizedConfiguration.class, AnnotatedConfiguration.class, ExtendsParameterizedConfiguration.class})
  private static class LegacyAnnotationsExtension {
  }

  @com.zernova.runtime.extension.api.annotation.Configuration(name = "NoImplicitLegacyConfiguration")
  @com.zernova.runtime.extension.api.annotation.NoImplicit
  private static class NoImplicitLegacyConfiguration {
  }

  @com.zernova.runtime.extension.api.annotation.Configuration(name = "NoImplicitSdkConfiguration")
  @NoImplicit
  private static class NoImplicitSdkConfiguration {
  }

  @com.zernova.runtime.extension.api.annotation.Configuration(name = "LegacyAnnotationConfiguration")
  private static class LegacyAnnotationConfiguration {
  }

  @Configuration(name = "SdkAnnotationConfiguration")
  private static class SdkAnnotationConfiguration {
  }

  @com.zernova.runtime.extension.api.annotation.Configuration(name = "ParameterizedConfiguration")
  private static class ParameterizedConfiguration {

    @com.zernova.sdk.api.annotation.param.Parameter
    String configField;
  }

  @com.zernova.runtime.extension.api.annotation.Configuration(name = "ExtendsParameterizedConfiguration")
  private static class ExtendsParameterizedConfiguration extends ParameterizedConfiguration {
  }

  @com.zernova.runtime.extension.api.annotation.Configuration(name = "ParameterizedConfiguration")
  @Operations(SimpleOperations.class)
  @Sources(SdkSource.class)
  @ConnectionProviders(SdkConnectionProvider.class)
  private static class AnnotatedConfiguration {
  }

  private static class SimpleOperations {
  }

  private static class SdkConnectionProvider implements ConnectionProvider<String> {

    @Override
    public String connect() throws ConnectionException {
      return null;
    }

    @Override
    public void disconnect(String connection) {

    }

    @Override
    public ConnectionValidationResult validate(String connection) {
      return null;
    }
  }

  private static class SdkSource extends Source<String, String> {

    @Override
    public void onStart(SourceCallback<String, String> sourceCallback) throws ZernovaException {

    }

    @Override
    public void onStop() {

    }
  }

  @Extension(name = "SimpleSdkExtension")
  @Configurations({SimpleWronglyAnnotatedConfiguration.class})
  private static class SimpleSdkExtensionWithWronglyAnnotatedConfiguration {
  }

  @Configuration(name = "wronglySdkConfiguration")
  @com.zernova.runtime.extension.api.annotation.Configuration(name = "wronglyLegacyConfiguration")
  private static class SimpleWronglyAnnotatedConfiguration {
  }

}
