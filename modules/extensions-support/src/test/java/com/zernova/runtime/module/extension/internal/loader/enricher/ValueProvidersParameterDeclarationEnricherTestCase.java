/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.enricher;

import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static com.zernova.runtime.api.dsl.DslResolvingContext.getDefault;
import static com.zernova.runtime.manifest.api.ZernovaManifest.getMuleManifest;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getNamedObject;
import static com.zernova.test.module.extension.internal.util.ExtensionDeclarationTestUtils.declarerFor;

import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterDeclaration;
import com.zernova.runtime.api.meta.model.parameter.ActingParameterModel;
import com.zernova.runtime.api.meta.model.parameter.FieldValueProviderModel;
import com.zernova.runtime.api.meta.model.parameter.ValueProviderModel;
import com.zernova.runtime.extension.api.property.SinceZernovaVersionModelProperty;
import com.zernova.runtime.extension.internal.loader.DefaultExtensionLoadingContext;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.ValueProvidersParameterDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.property.DeclaringMemberModelProperty;
import com.zernova.test.values.extension.ValuesExtension;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class ValueProvidersParameterDeclarationEnricherTestCase {

  private ExtensionDeclaration declaration;

  @Before
  public void setUp() {
    ExtensionDeclarer declarer = declarerFor(ValuesExtension.class, getMuleManifest().getProductVersion());
    new ValueProvidersParameterDeclarationEnricher()
        .enrich(new DefaultExtensionLoadingContext(declarer, getClass().getClassLoader(), getDefault(emptySet())));
    this.declaration = declarer.getDeclaration();
  }

  @Test
  public void verifyValueProviderWithoutParameters() {
    ParameterDeclaration parameterDeclaration = getParameterByOperationAndName("singleValuesEnabledParameter", "channels");

    assertThat(parameterDeclaration, notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel(), notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel().getActingParameters(), hasSize(0));
    assertThat(parameterDeclaration.getValueProviderModel().getParameters(), hasSize(0));
  }

  @Test
  public void verifyValueProviderWithRequiredParameter() {
    ParameterDeclaration parameterDeclaration = getParameterByOperationAndName("withRequiredParameter", "providedParameters");
    assertWithRequiredParameter(parameterDeclaration, "requiredValue");
  }

  @Test
  public void verifyValueProviderWithRequiredParameterWithAlias() {
    OperationDeclaration operationDeclaration =
        getNamedObject(this.declaration.getOperations(), "singleValuesWithRequiredParameterWithAlias");
    ParameterDeclaration parameterDeclaration = getNamedObject(operationDeclaration.getAllParameters(), "channels");
    ParameterDeclaration aliasedParameterDeclaration = getNamedObject(operationDeclaration.getAllParameters(), "superString");
    Optional<DeclaringMemberModelProperty> modelProperty =
        aliasedParameterDeclaration.getModelProperty(DeclaringMemberModelProperty.class);

    assertThat(modelProperty.isPresent(), is(true));
    assertThat(modelProperty.get().getDeclaringField(), notNullValue());
    assertThat(modelProperty.get().getDeclaringField().getName(), is("requiredString"));
    assertWithRequiredParameter(parameterDeclaration, "superString");
  }

  @Test
  public void verifyValueProviderWithOptionalParameter() {
    ParameterDeclaration parameterDeclaration = getParameterByOperationAndName("withOptionalParameter", "providedParameters");

    assertThat(parameterDeclaration, notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel(), notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel().getActingParameters(), hasSize(0));

    assertThat(parameterDeclaration.getValueProviderModel().getParameters(), hasSize(1));
    assertThat(parameterDeclaration.getValueProviderModel().getParameters(), contains(item("optionalValue", false)));
  }

  @Test
  public void verifyValueProviderWithRequiredAndOptionalParameters() {
    ParameterDeclaration parameterDeclaration =
        getParameterByOperationAndName("withRequiredAndOptionalParameters", "providedParameters");

    assertThat(parameterDeclaration, notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel(), notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel().getActingParameters(), hasSize(1));
    assertThat(parameterDeclaration.getValueProviderModel().getActingParameters(), contains("requiredValue"));

    assertThat(parameterDeclaration.getValueProviderModel().getParameters(), hasSize(2));
    assertThat(parameterDeclaration.getValueProviderModel().getParameters(),
               contains(item("requiredValue", true), item("optionalValue", false)));
  }

  @Test
  public void verifyParametersOfValueProviderModel() {
    ParameterDeclaration parameterDeclaration = getParameterByOperationAndName("withRequiredParameter", "providedParameters");

    assertThat(parameterDeclaration, notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel(), notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel().getParameters(), hasSize(1));

    ActingParameterModel parameter = parameterDeclaration.getValueProviderModel().getParameters().get(0);
    assertThat(parameter, notNullValue());
    assertThat(parameter.getName(), is("requiredValue"));
    assertThat(parameter.isRequired(), is(true));
  }

  @Test
  public void verifyFieldValueProviderWithoutParametersForOperationParameterWithOneFieldValues() {
    ParameterDeclaration parameterDeclaration =
        getParameterByOperationAndName("singleValuesEnabledParameterWithOneFieldValues", "body");

    assertThat(parameterDeclaration, notNullValue());
    assertThat(parameterDeclaration.getFieldValueProviderModels(), hasSize(1));
    assertThat(parameterDeclaration.getFieldValueProviderModels().get(0).getTargetSelector(), is("simple.path"));
    assertThat(parameterDeclaration.getFieldValueProviderModels().get(0).getParameters(), hasSize(0));
  }

  @Test
  public void verifyFieldValueProviderWithoutParametersForOperationParameterWithMoreThanOneFieldValues() {
    ParameterDeclaration parameterDeclaration =
        getParameterByOperationAndName("singleValuesEnabledParameterWithMoreThanOneFieldValues", "body");

    assertThat(parameterDeclaration, notNullValue());
    assertThat(parameterDeclaration.getFieldValueProviderModels(), hasSize(2));
    assertThat(parameterDeclaration.getFieldValueProviderModels().get(0).getTargetSelector(), is("simple.path"));
    assertThat(parameterDeclaration.getFieldValueProviderModels().get(1).getTargetSelector(), is("another.simple.path"));
    assertThat(parameterDeclaration.getFieldValueProviderModels().get(0).getParameters(), hasSize(0));
  }

  private void assertWithRequiredParameter(ParameterDeclaration parameterDeclaration, String parameterName) {
    assertThat(parameterDeclaration, notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel(), notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel().getActingParameters(), hasSize(1));
    assertThat(parameterDeclaration.getValueProviderModel().getActingParameters(), contains(parameterName));

    assertThat(parameterDeclaration.getValueProviderModel().getParameters(), hasSize(1));
    assertThat(parameterDeclaration.getValueProviderModel().getParameters(), contains(item(parameterName, true)));
  }

  @Test
  public void verifyExtractinExpressionOfParametersOfValueProviderModelWithoutBinding() {
    ParameterDeclaration parameterDeclaration =
        getParameterByOperationAndName("withRequiredParameter", "providedParameters");

    assertThat(parameterDeclaration, notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel(), notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel().getParameters(), hasSize(1));

    ActingParameterModel parameter = parameterDeclaration.getValueProviderModel().getParameters().get(0);
    assertThat(parameter, notNullValue());
    assertThat(parameter.getName(), is("requiredValue"));
    assertThat(parameter.isRequired(), is(true));
    assertThat(parameter.getExtractionExpression(), is("requiredValue"));
  }

  @Test
  public void verifyExtractinExpressionOfParametersOfValueProviderModelWithBinding() {
    ParameterDeclaration parameterDeclaration = getParameterByOperationAndName("withBoundActingParameter", "parameterWithValues");

    assertThat(parameterDeclaration, notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel(), notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel().getParameters(), hasSize(1));

    ActingParameterModel parameter = parameterDeclaration.getValueProviderModel().getParameters().get(0);
    assertThat(parameter, notNullValue());
    assertThat(parameter.getName(), is("requiredValue"));
    assertThat(parameter.isRequired(), is(true));
    assertThat(parameter.getExtractionExpression(), is("actingParameter"));
  }

  @Test
  public void verifyExtractionExpressionOfParametersOfValueProviderModelWithBindingToField() {
    ParameterDeclaration parameterDeclaration =
        getParameterByOperationAndName("withBoundActingParameterField", "parameterWithValues");

    assertThat(parameterDeclaration, notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel(), notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel().getParameters(), hasSize(1));

    ActingParameterModel parameter = parameterDeclaration.getValueProviderModel().getParameters().get(0);
    assertThat(parameter, notNullValue());
    assertThat(parameter.getName(), is("requiredValue"));
    assertThat(parameter.isRequired(), is(true));
    assertThat(parameter.getExtractionExpression(), is("actingParameter.field"));
  }

  @Test
  public void verifyParameterWithFieldValueProviderWithFieldActingParameters() {
    ParameterDeclaration parameterDeclaration =
        getParameterByOperationAndName("tagContentAsActingForAttributeValue", "xmlBody");

    assertThat(parameterDeclaration, notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel(), nullValue());

    assertThat(parameterDeclaration.getFieldValueProviderModels(), notNullValue());
    assertThat(parameterDeclaration.getFieldValueProviderModels(), hasSize(1));

    FieldValueProviderModel fieldValueProviderModel = parameterDeclaration.getFieldValueProviderModels().get(0);

    assertThat(fieldValueProviderModel.getTargetSelector(), is("nested.tag.@customAttribute"));
    assertThat(fieldValueProviderModel.getParameters(), hasSize(1));

    ActingParameterModel parameter = fieldValueProviderModel.getParameters().get(0);

    assertThat(parameter, notNullValue());
    assertThat(parameter.getName(), is("requiredValue"));
    assertThat(parameter.isRequired(), is(true));
    assertThat(parameter.getExtractionExpression(), is("xmlBody.nested.someTag"));
  }

  @Test
  public void verifySinceMuleVersionModelPropertyAdded() {
    ParameterDeclaration parameterDeclaration =
        getParameterByOperationAndName("withBoundActingParameterField", "parameterWithValues");

    assertThat(parameterDeclaration, notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel(), notNullValue());

    ValueProviderModel valueProviderModel = parameterDeclaration.getValueProviderModel();

    assertThat(valueProviderModel.getModelProperty(SinceZernovaVersionModelProperty.class).isPresent(), is(true));

    SinceZernovaVersionModelProperty sinceMuleVersionModelProperty =
        valueProviderModel.getModelProperty(SinceZernovaVersionModelProperty.class).get();

    assertThat(sinceMuleVersionModelProperty.getVersion(), is(new ZernovaVersion("4.4.0")));
  }

  @Test
  public void verifySinceMuleVersionModelPropertyNotAdded() {
    ParameterDeclaration parameterDeclaration =
        getParameterByOperationAndName("singleValuesEnabledParameterWithRequiredParameters", "channels");

    assertThat(parameterDeclaration, notNullValue());
    assertThat(parameterDeclaration.getValueProviderModel(), notNullValue());

    ValueProviderModel valueProviderModel = parameterDeclaration.getValueProviderModel();

    assertThat(valueProviderModel.getModelProperty(SinceZernovaVersionModelProperty.class).isPresent(), is(false));
  }

  @Test
  public void verifyPartOrderIsAddedUsingBothApis() {
    ParameterDeclaration continentParameterDeclaration =
        getParameterByOperationAndName("multiLevelValue", "continent");

    assertThat(continentParameterDeclaration.getValueProviderModel().getPartOrder(), is(1));

    ParameterDeclaration countryParameterDeclaration =
        getParameterByOperationAndName("multiLevelValue", "country");

    assertThat(countryParameterDeclaration.getValueProviderModel().getPartOrder(), is(2));

    ParameterDeclaration cityParameterDeclaration =
        getParameterByOperationAndName("multiLevelValue", "city");

    assertThat(cityParameterDeclaration.getValueProviderModel().getPartOrder(), is(3));
  }

  private ParameterDeclaration getParameterByOperationAndName(String operationName, String parameterName) {
    OperationDeclaration operationDeclaration = getNamedObject(this.declaration.getOperations(), operationName);
    return getNamedObject(operationDeclaration.getAllParameters(), parameterName);
  }

  private org.hamcrest.Matcher<Object> item(String name, boolean required) {
    return allOf(hasProperty("name", is(name)), hasProperty("required", is(required)));
  }
}
