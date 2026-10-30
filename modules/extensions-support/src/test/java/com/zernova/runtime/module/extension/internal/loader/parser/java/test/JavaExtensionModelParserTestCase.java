/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.test;

import static com.zernova.runtime.module.extension.api.util.ZernovaExtensionUtils.loadExtension;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.test.MinZernovaVersionTestUtils.ctxResolvingMinZernovaVersion;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.utils.ResolvedMinZernovaVersion.FIRST_MULE_VERSION;

import static java.lang.Class.forName;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.ObjectFieldType;
import com.zernova.metadata.api.model.impl.DefaultDateTimeType;
import com.zernova.metadata.api.model.impl.DefaultObjectType;
import com.zernova.metadata.java.api.annotation.ClassInformationAnnotation;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.extension.api.annotation.SubTypeMapping;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import com.zernova.runtime.module.extension.internal.loader.java.type.runtime.ExtensionTypeWrapper;
import com.zernova.runtime.module.extension.internal.loader.parser.java.JavaExtensionModelParser;
import com.zernova.sdk.api.annotation.Configurations;
import com.zernova.sdk.api.annotation.Extension;
import com.zernova.sdk.api.annotation.Import;
import com.zernova.sdk.api.annotation.MinZernovaVersion;
import com.zernova.sdk.api.annotation.PrivilegedExport;
import com.zernova.sdk.api.runtime.parameter.Literal;
import com.zernova.test.heisenberg.extension.model.KnockeableDoor;
import com.zernova.test.vegan.extension.VeganCookBook;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.qameta.allure.Description;
import io.qameta.allure.Issue;

public class JavaExtensionModelParserTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  @Issue("W-12622240")
  @Description("Verify that ExtensionModel for an Extension with java data types such as LocalDateTime, loads the correct MetadataType i.e. DateTimeType and works in Java 17")
  public void getParameterizedWithJavaFieldsExtensionUsingSdkApi() throws Exception {
    ExtensionModel extensionModel = loadExtension(ParameterizedWithJavaTypeExtension.class);
    ConfigurationModel configModel = extensionModel.getConfigurationModels().get(0);
    ParameterModel parameterModel = configModel.getAllParameterModels().get(0);
    DefaultObjectType objectType = (DefaultObjectType) parameterModel.getType();

    assertThat(forName(objectType.getAnnotation(ClassInformationAnnotation.class).get().getClassname()),
               is(equalTo(SimplePojoWithTime.class)));

    Map<String, ObjectFieldType> fieldMap = objectType.getFields().stream()
        .collect(toMap(f -> f.getKey().getName().getLocalPart(), identity()));

    assertThat(fieldMap.get("dateTime").getKey().getName().getLocalPart(), is("dateTime"));
    assertThat(fieldMap.get("dateTime").getValue(), instanceOf(DefaultDateTimeType.class));
  }

  @Test
  public void getImportedTypesFromExtensionUsingTheSdkApi() {
    List<MetadataType> importedTypes = getParser(SimpleExtensionUsingSdkApi.class).getImportedTypes();
    assertThat(importedTypes.size(), is(1));
    assertThat(importedTypes.get(0).getAnnotation(ClassInformationAnnotation.class).get().getClassname(),
               is("com.zernova.test.heisenberg.extension.model.KnockeableDoor"));
  }

  @Test
  public void getImportedTypesFromExtensionUsingTheLegacyApi() {
    List<MetadataType> importedTypes = getParser(SimpleExtensionUsingLegacyApi.class).getImportedTypes();
    assertThat(importedTypes.size(), is(1));
    assertThat(importedTypes.get(0).getAnnotation(ClassInformationAnnotation.class).get().getClassname(),
               is("com.zernova.test.heisenberg.extension.model.KnockeableDoor"));
  }

  @Test
  public void getImportedTypesFromExtensionUsingBothTheLegacyAndTheSdkApi() {
    List<MetadataType> importedTypes = getParser(SimpleMixedApiExtension.class).getImportedTypes();
    assertThat(importedTypes.size(), is(2));
    assertThat(importedTypes.get(1).getAnnotation(ClassInformationAnnotation.class).get().getClassname(),
               is("com.zernova.test.heisenberg.extension.model.KnockeableDoor"));
    assertThat(importedTypes.get(0).getAnnotation(ClassInformationAnnotation.class).get().getClassname(),
               is("com.zernova.test.vegan.extension.VeganCookBook"));
  }

  @Test
  @Ignore("W-12947325")
  public void getExportedTypesFromExtensionUsingTheSdkApi() {
    List<String> exportedTypes = getParser(SimpleExtensionUsingSdkApi.class).getPrivilegedExportedPackages();
    assertThat(exportedTypes.size(), is(1));
  }

  // This behavior needs to be supported to maintain backwards compatibility
  @Test
  public void getSubtypesFromExtensionWithDuplicatedBaseTypes() {
    Map<MetadataType, List<MetadataType>> subTypes = getParser(SubTypesDuplication.class).getSubTypes();
    assertThat(subTypes.size(), is(1));
    assertThat(subTypes.values().iterator().next(), hasSize(2));
  }

  @Test
  public void getMMVForLegacyExtension() {
    JavaExtensionModelParser parser = getParser(ExtensionUsingLegacyApi.class);
    assertThat(parser.getResolvedMinZernovaVersion().get().getMinZernovaVersion(), is(FIRST_MULE_VERSION));
    assertThat(parser.getResolvedMinZernovaVersion().get().getReason(),
               is("Extension SimpleExtension has min mule version 4.1.1 because it is the default value."));
  }

  @Test
  public void getMMVForSdkExtension() {
    JavaExtensionModelParser parser = getParser(ExtensionUsingSdkApi.class);
    assertThat(parser.getResolvedMinZernovaVersion().get().getMinZernovaVersion().toString(), is("4.5.0"));
    assertThat(parser.getResolvedMinZernovaVersion().get().getReason(),
               is("Extension SimpleExtension has min mule version 4.5.0 because it if annotated with the new sdk api @Extension."));
  }

  @Test
  public void getMMVForExtensionWithSuperExtension() {
    JavaExtensionModelParser parser = getParser(ExtensionWithSuperExtension.class);
    assertThat(parser.getResolvedMinZernovaVersion().get().getMinZernovaVersion().toString(), is("4.4"));
    assertThat(parser.getResolvedMinZernovaVersion().get().getReason(),
               is("Extension ExtensionWithSuperExtension has min mule version 4.4 because of its super class ParameterizedExtension. Extension ParameterizedExtension has min mule version 4.4 because of its field extensionParameter. Field extensionParameter has min mule version 4.4 because it is annotated with Parameter. Parameter was introduced in Mule 4.4."));
  }

  @Test
  public void getMMVForParameterizedExtension() {
    JavaExtensionModelParser parser = getParser(ParameterizedExtension.class);
    assertThat(parser.getResolvedMinZernovaVersion().get().getMinZernovaVersion().toString(), is("4.4"));
    assertThat(parser.getResolvedMinZernovaVersion().get().getReason(),
               is("Extension MixedConfigurationsAnnotationExtension has min mule version 4.4 because of its field extensionParameter. Field extensionParameter has min mule version 4.4 because it is annotated with Parameter. Parameter was introduced in Mule 4.4."));
  }

  @Test
  public void getMMVForExtensionWithConfiguration() {
    JavaExtensionModelParser parser = getParser(ExtensionWithConfiguration.class);
    assertThat(parser.getResolvedMinZernovaVersion().get().getMinZernovaVersion(), is(FIRST_MULE_VERSION));
    assertThat(parser.getResolvedMinZernovaVersion().get().getReason(),
               is("Extension MixedConfigurationsAnnotationExtension has min mule version 4.1.1 because it is the default value."));
  }

  @Test
  public void getMMVForExtensionAnnotatedWithMMV() {
    JavaExtensionModelParser parser = getParser(ExtensionAnnotatedWithMMV.class);
    assertThat(parser.getResolvedMinZernovaVersion().get().getMinZernovaVersion().toString(), is("4.4"));
    assertThat(parser.getResolvedMinZernovaVersion().get().getReason(),
               is("Calculated Min Mule Version is 4.4 which is greater than the one set at the extension class level 4.3. Overriding it. Extension ExtensionAnnotatedWithMMV has min mule version 4.4 because of its method extensionMethod. Method extensionMethod has min mule version 4.4 because it is the one set at the method level through the @MinZernovaVersion annotation."));
  }

  @Test
  public void getMMVForExtensionAnnotatedWithHighMMV() {
    JavaExtensionModelParser parser = getParser(ExtensionAnnotatedWithHighMMV.class);
    assertThat(parser.getResolvedMinZernovaVersion().get().getMinZernovaVersion().toString(), is("4.7"));
    assertThat(parser.getResolvedMinZernovaVersion().get().getReason(),
               is("Extension ExtensionAnnotatedWithHighMMV has min mule version 4.7 because it is the one set at the class level through the @MinZernovaVersion annotation."));
  }

  protected JavaExtensionModelParser getParser(Class<?> extensionClass) {
    ClassTypeLoader typeLoader =
        ExtensionsTypeLoaderFactory.getDefault().createTypeLoader(Thread.currentThread().getContextClassLoader());
    ExtensionTypeWrapper extensionTypeWrapper = new ExtensionTypeWrapper<>(extensionClass, typeLoader);
    return new JavaExtensionModelParser(extensionTypeWrapper, ctxResolvingMinZernovaVersion());
  }

  @Extension(name = "SimpleExtension")
  @Import(type = KnockeableDoor.class)
  @PrivilegedExport(packages = {"com.zernova.runtime.module.extension.internal.loader.parser.java"})
  private static class SimpleExtensionUsingSdkApi {
  }

  @Extension(name = "SimpleExtension")
  public static class ExtensionUsingSdkApi {
  }

  @com.zernova.runtime.extension.api.annotation.Extension(name = "SimpleExtension")
  @com.zernova.runtime.extension.api.annotation.Import(type = KnockeableDoor.class)
  @com.zernova.runtime.extension.api.annotation.PrivilegedExport(
      packages = {"com.zernova.runtime.module.extension.internal.loader.parser.java"})
  private static class SimpleExtensionUsingLegacyApi {
  }

  @com.zernova.runtime.extension.api.annotation.Extension(name = "SimpleExtension")
  public static class ExtensionUsingLegacyApi {
  }

  @Extension(name = "SimpleExtension")
  @Import(type = KnockeableDoor.class)
  @com.zernova.runtime.extension.api.annotation.Import(type = VeganCookBook.class)
  private static class SimpleMixedApiExtension {
  }

  @Extension(name = "SubTypesDuplication")
  @SubTypeMapping(baseType = BaseInterface.class,
      subTypes = {ImplementationOne.class, ImplementationTwo.class, ImplementationOne.class})
  private static class SubTypesDuplication {
  }

  private interface BaseInterface {

  }

  private static class ImplementationOne implements BaseInterface {

    @Parameter
    private String parameterOne;

  }

  private static class ImplementationTwo implements BaseInterface {

    @Parameter
    private String parameterTwo;

  }

  @com.zernova.runtime.extension.api.annotation.Extension(name = "MixedConfigurationsAnnotationExtension")
  @Configurations(ImplementationOne.class)
  private static class ExtensionWithConfiguration {

    Literal<String> extensionField;
  }

  @com.zernova.runtime.extension.api.annotation.Extension(name = "MixedConfigurationsAnnotationExtension")
  private static class ParameterizedExtension {

    @com.zernova.sdk.api.annotation.param.Parameter
    String extensionParameter;
  }

  @com.zernova.runtime.extension.api.annotation.Extension(name = "MixedConfigurationsAnnotationExtension")
  public static class ParameterizedWithJavaTypeExtension {

    @Parameter
    private SimplePojoWithTime simplePojoWithTime;

    public ParameterizedWithJavaTypeExtension() {
      this.simplePojoWithTime = simplePojoWithTime;
    }
  }

  @com.zernova.runtime.extension.api.annotation.Extension(name = "ExtensionWithSuperExtension")
  private static class ExtensionWithSuperExtension extends ParameterizedExtension {
  }

  @com.zernova.runtime.extension.api.annotation.Extension(name = "ExtensionAnnotatedWithMMV")
  @MinZernovaVersion("4.3")
  private static class ExtensionAnnotatedWithMMV {

    @MinZernovaVersion("4.4")
    public void extensionMethod() {}
  }

  @com.zernova.runtime.extension.api.annotation.Extension(name = "ExtensionAnnotatedWithHighMMV")
  @MinZernovaVersion("4.7")
  private static class ExtensionAnnotatedWithHighMMV {

    @com.zernova.sdk.api.annotation.param.Parameter
    String extensionParameter;
  }

  private static class SimplePojoWithTime {

    @Parameter
    private String name;

    @Parameter
    private Integer age;

    @Parameter
    private LocalDateTime dateTime;

    public SimplePojoWithTime() {}

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      SimplePojoWithTime that = (SimplePojoWithTime) o;
      return Objects.equals(name, that.name) &&
          Objects.equals(age, that.age) &&
          Objects.equals(dateTime, that.dateTime);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, age, dateTime);
    }
  }
}
