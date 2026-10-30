/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension;

import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.metadata.TypeResolver;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Content;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.annotation.values.OfValues;
import com.zernova.sdk.api.annotation.binding.Binding;
import com.zernova.sdk.api.annotation.values.FieldValues;
import com.zernova.test.values.extension.metadata.JsonTypeResolver;
import com.zernova.test.values.extension.metadata.XmlTypeResolver;
import com.zernova.test.values.extension.resolver.MultiLevelValueProvider;
import com.zernova.test.values.extension.resolver.SdkMultiLevelValueProvider;
import com.zernova.test.values.extension.resolver.SimpleValueProvider;
import com.zernova.test.values.extension.resolver.TrueFalseValueProvider;
import com.zernova.test.values.extension.resolver.WithComplexActingParameter;
import com.zernova.test.values.extension.resolver.WithConfigValueProvider;
import com.zernova.test.values.extension.resolver.WithConnectionValueProvider;
import com.zernova.test.values.extension.resolver.WithEnumParameterValueProvider;
import com.zernova.test.values.extension.resolver.WithErrorValueProvider;
import com.zernova.test.values.extension.resolver.WithFourActingParametersValueProvider;
import com.zernova.test.values.extension.resolver.WithListParameterValueProvider;
import com.zernova.test.values.extension.resolver.WithMapParameterValueProvider;
import com.zernova.test.values.extension.resolver.WithZernovaContextValueProvider;
import com.zernova.test.values.extension.resolver.WithOptionalParameterSdkValueProvider;
import com.zernova.test.values.extension.resolver.WithOptionalParametersValueProvider;
import com.zernova.test.values.extension.resolver.WithOptionalParametersWithDefaultValueProvider;
import com.zernova.test.values.extension.resolver.WithPojoParameterValueProvider;
import com.zernova.test.values.extension.resolver.WithRequiredAndOptionalParametersValueProvider;
import com.zernova.test.values.extension.resolver.WithRequiredParameterFromGroupValueProvider;
import com.zernova.test.values.extension.resolver.WithRequiredParameterSdkValueProvider;
import com.zernova.test.values.extension.resolver.WithRequiredParameterValueProvider;
import com.zernova.test.values.extension.resolver.WithRequiredParametersValueProvider;
import com.zernova.test.values.extension.resolver.WithReservedNameActingParameterValueProvider;
import com.zernova.test.values.extension.resolver.WithTwoActingParametersValueProvider;

import java.io.InputStream;
import java.util.List;

public class ValuesOperations {

  public void singleValuesEnabledParameter(@com.zernova.sdk.api.annotation.values.OfValues(SimpleValueProvider.class) String channels) {

  }

  public void singleValuesEnabledParameterWithConnection(@com.zernova.sdk.api.annotation.values.OfValues(WithConnectionValueProvider.class) String channels,
                                                         @Connection ValuesConnection connection) {}

  public void singleValuesEnabledParameterWithConfiguration(@com.zernova.sdk.api.annotation.values.OfValues(WithConfigValueProvider.class) String channels,
                                                            @Connection ValuesConnection connection) {}

  public void singleValuesEnabledParameterWithRequiredParameters(@OfValues(WithRequiredParametersValueProvider.class) String channels,
                                                                 String requiredString,
                                                                 boolean requiredBoolean,
                                                                 int requiredInteger,
                                                                 List<String> strings) {}

  public void singleValuesEnabledParameterInsideParameterGroup(@ParameterGroup(
      name = "ValuesGroup") GroupWithValuesParameter optionsParameter) {}

  public void singleValuesEnabledParameterRequiresValuesOfParameterGroup(@com.zernova.sdk.api.annotation.values.OfValues(WithRequiredParameterFromGroupValueProvider.class) String values,
                                                                         @ParameterGroup(
                                                                             name = "ValuesGroup") GroupWithValuesParameter optionsParameter) {}

  public void multiLevelValue(@OfValues(MultiLevelValueProvider.class) @ParameterGroup(
      name = "values") GroupAsMultiLevelValue optionsParameter) {

  }

  public void singleValuesWithRequiredParameterWithAlias(@ParameterGroup(
      name = "someGroup") WithRequiredParameterWithAliasGroup group) {}

  public void resolverGetsMuleContextInjection(@OfValues(WithZernovaContextValueProvider.class) String channel) {

  }

  public void valuesInsideShowInDslGroup(@com.zernova.sdk.api.annotation.values.OfValues(WithRequiredParameterFromGroupValueProvider.class) String values,
                                         @ParameterGroup(name = "ValuesGroup",
                                             showInDsl = true) GroupWithValuesParameter optionsParameter) {

  }

  @MediaType(value = "*/*", strict = false)
  public Object contentTypedValuesInsideShowInDslGroup(@ParameterGroup(name = "ContentTypedValuesGroup",
      showInDsl = true) GroupWithContentTypedValuesParameter optionsParameter) {
    return optionsParameter.getBody().getValue();
  }

  public void withErrorValueProvider(@com.zernova.sdk.api.annotation.values.OfValues(WithErrorValueProvider.class) String values,
                                     String errorCode) {

  }

  public void withComplexActingParameter(@Optional @OfValues(WithComplexActingParameter.class) String providedParameter,
                                         ComplexActingParameter complexActingParameter) {}

  public void withRequiredParameter(@OfValues(WithRequiredParameterValueProvider.class) String providedParameters,
                                    String requiredValue) {}

  public void withRequiredParameterAndOptionalParameterAsRequired(@OfValues(WithRequiredAndOptionalParametersValueProvider.class) String providedParameters,
                                                                  String requiredValue, String optionalValue) {}

  public void withRequiredAndOptionalParameters(@OfValues(WithRequiredAndOptionalParametersValueProvider.class) String providedParameters,
                                                String requiredValue, @Optional String optionalValue) {}

  public void withRequiredAndOptionalWithDefaultParameters(@OfValues(WithRequiredAndOptionalParametersValueProvider.class) String providedParameters,
                                                           String requiredValue, @Optional(
                                                               defaultValue = "OPERATION_DEFAULT_VALUE") String optionalValue) {}

  public void withOptionalParameterAsRequired(@OfValues(WithOptionalParametersValueProvider.class) String providedParameters,
                                              String optionalValue) {}

  public void withOptionalParameter(@OfValues(WithOptionalParametersValueProvider.class) String providedParameters,
                                    @Optional String optionalValue) {}

  public void withOptionalParameterWithDefault(@OfValues(WithOptionalParametersValueProvider.class) String providedParameters,
                                               @Optional(defaultValue = "OPERATION_DEFAULT_VALUE") String optionalValue) {}

  public void withVPOptionalParameterWithDefaultValue(@OfValues(WithOptionalParametersWithDefaultValueProvider.class) String providedParameters,
                                                      @Optional(defaultValue = "OPERATION_DEFAULT_VALUE") String optionalValue) {}

  public void withBoundActingParameter(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithRequiredParameterSdkValueProvider.class,
      bindings = {
          @Binding(actingParameter = "requiredValue", extractionExpression = "actingParameter")}) String parameterWithValues,
                                       String actingParameter) {}

  public void withBoundOptionalActingParameter(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithOptionalParameterSdkValueProvider.class,
      bindings = {
          @Binding(actingParameter = "optionalValue", extractionExpression = "actingParameter")}) String parameterWithValues,
                                               String actingParameter) {}

  public void withBoundActingParameterField(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithRequiredParameterSdkValueProvider.class,
      bindings = {@Binding(actingParameter = "requiredValue",
          extractionExpression = "actingParameter.field")}) String parameterWithValues,
                                            @TypeResolver(JsonTypeResolver.class) InputStream actingParameter) {}

  public void withBoundOptionalActingParameterField(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithOptionalParameterSdkValueProvider.class,
      bindings = {@Binding(actingParameter = "optionalValue",
          extractionExpression = "actingParameter.nested.field")}) String parameterWithValues,
                                                    @TypeResolver(JsonTypeResolver.class) InputStream actingParameter) {}

  public void withBoundActingParameterFieldWithDot(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithRequiredParameterSdkValueProvider.class,
      bindings = {
          @Binding(actingParameter = "requiredValue",
              extractionExpression = "actingParameter.\"field.with.dot\"")}) String parameterWithValues,
                                                   @TypeResolver(JsonTypeResolver.class) InputStream actingParameter) {}

  public void withTwoActingParameters(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithTwoActingParametersValueProvider.class,
      bindings = {@Binding(actingParameter = "requiredValue",
          extractionExpression = "actingParameter.field")}) String parameterWithValues,
                                      String scalarActingParameter,
                                      @TypeResolver(JsonTypeResolver.class) InputStream actingParameter) {}

  public void withTwoBoundActingParameters(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithTwoActingParametersValueProvider.class,
      bindings = {@Binding(actingParameter = "requiredValue", extractionExpression = "actingParameter.field"),
          @Binding(actingParameter = "scalarActingParameter",
              extractionExpression = "anotherParameter")}) String parameterWithValues,
                                           String anotherParameter,
                                           @TypeResolver(JsonTypeResolver.class) InputStream actingParameter) {}


  public void withBoundActingParameterToXmlTagContent(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithRequiredParameterSdkValueProvider.class,
      bindings = {
          @Binding(actingParameter = "requiredValue",
              extractionExpression = "actingParameter.nested.xmlTag")}) String parameterWithValues,
                                                      @TypeResolver(XmlTypeResolver.class) InputStream actingParameter) {}

  public void withBoundActingParameterToXmlTagAttribute(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithRequiredParameterSdkValueProvider.class,
      bindings = {@Binding(actingParameter = "requiredValue",
          extractionExpression = "actingParameter.nested.xmlTag.@attribute")}) String parameterWithValues,
                                                        @TypeResolver(XmlTypeResolver.class) InputStream actingParameter) {}

  public void withFourBoundActingParameters(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithFourActingParametersValueProvider.class,
      bindings = {@Binding(actingParameter = "requiredValue", extractionExpression = "actingParameter.field1"),
          @Binding(actingParameter = "anotherValue", extractionExpression = "actingParameter.nested.field2"),
          @Binding(actingParameter = "someValue", extractionExpression = "actingParameter.nested.field3"),
          @Binding(actingParameter = "optionalValue",
              extractionExpression = "actingParameter.anotherNested.field4")}) String parameterWithValues,
                                            @TypeResolver(JsonTypeResolver.class) InputStream actingParameter) {}

  public void withBoundActingParameterArray(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithListParameterValueProvider.class,
      bindings = {@Binding(actingParameter = "requiredValue",
          extractionExpression = "actingParameter.jsonArray")}) String parameterWithValues,
                                            @TypeResolver(JsonTypeResolver.class) InputStream actingParameter) {}

  public void withPojoBoundActingParameter(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithPojoParameterValueProvider.class,
      bindings = {@Binding(actingParameter = "requiredValue",
          extractionExpression = "actingParameter.pojoField")}) String parameterWithValues,
                                           @TypeResolver(JsonTypeResolver.class) InputStream actingParameter) {}

  public void withMapBoundActingParameter(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithMapParameterValueProvider.class,
      bindings = {@Binding(actingParameter = "requiredValue",
          extractionExpression = "actingParameter.mapField")}) String parameterWithValues,
                                          @TypeResolver(JsonTypeResolver.class) InputStream actingParameter) {}

  public void withPojoFieldBoundActingParameterField(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithRequiredParameterSdkValueProvider.class,
      bindings = {@Binding(actingParameter = "requiredValue",
          extractionExpression = "actingParameter.pojoId")}) String parameterWithValues,
                                                     MyPojo actingParameter) {}

  public void withBoundActingParameterEnum(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithEnumParameterValueProvider.class,
      bindings = {@Binding(actingParameter = "requiredValue",
          extractionExpression = "actingParameter.enumField")}) String parameterWithValues,
                                           @TypeResolver(JsonTypeResolver.class) InputStream actingParameter) {}

  public void withBoundActingParameterWithAlias(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithRequiredParameterSdkValueProvider.class,
      bindings = {
          @Binding(actingParameter = "requiredValue", extractionExpression = "parameterAlias")}) String parameterWithValues,
                                                @Alias("parameterAlias") String actingParameter) {}

  public void withBoundActingParameterFromContentField(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithRequiredParameterSdkValueProvider.class,
      bindings = {
          @Binding(actingParameter = "requiredValue", extractionExpression = "body.field")}) String parameterWithValues,
                                                       @TypeResolver(JsonTypeResolver.class) @Content InputStream body) {}

  public void withBoundActingParameterFromXmlContentField(@com.zernova.sdk.api.annotation.values.OfValues(
      value = WithRequiredParameterSdkValueProvider.class,
      bindings = {
          @Binding(actingParameter = "requiredValue", extractionExpression = "xmlBody.field")}) String parameterWithValues,
                                                          @TypeResolver(XmlTypeResolver.class) @Content InputStream xmlBody) {}

  public void singleValuesEnabledParameterWithOneFieldValues(@TypeResolver(JsonTypeResolver.class) @Content @FieldValues(
      targetSelectors = "simple.path",
      value = SimpleValueProvider.class) InputStream body) {}

  public void singleValuesEnabledParameterWithMoreThanOneFieldValues(@TypeResolver(JsonTypeResolver.class) @Content @FieldValues(
      targetSelectors = "simple.path",
      value = SimpleValueProvider.class) @FieldValues(targetSelectors = "another.simple.path",
          value = TrueFalseValueProvider.class) InputStream body) {}

  public void parameterWithMultilevelFieldValue(@TypeResolver(JsonTypeResolver.class) @Content @FieldValues(
      targetSelectors = "channel",
      value = SimpleValueProvider.class) @FieldValues(
          targetSelectors = {"location.continent", "location.country", "location.city"},
          value = SdkMultiLevelValueProvider.class) InputStream body) {}

  public void parameterWithTwoFieldWithSameValues(@TypeResolver(JsonTypeResolver.class) @Content @FieldValues(
      targetSelectors = "source.channelId",
      value = SimpleValueProvider.class) @FieldValues(
          targetSelectors = "target.channelId",
          value = SimpleValueProvider.class) InputStream body) {}

  public void actingParameterWithReservedName(String type,
                                              @OfValues(WithReservedNameActingParameterValueProvider.class) String parameterWithValues) {}

  public void parameterWithMultipleMultilevelFieldValueUsingSameProvider(@TypeResolver(JsonTypeResolver.class) @Content @FieldValues(
      targetSelectors = "channel",
      value = SimpleValueProvider.class) @FieldValues(
          targetSelectors = {"source.location.continent", "source.location.country", "source.location.city"},
          value = SdkMultiLevelValueProvider.class) @FieldValues(
              targetSelectors = {"target.location.continent", "target.location.country", "target.location.city"},
              value = SdkMultiLevelValueProvider.class) InputStream body) {}
}
