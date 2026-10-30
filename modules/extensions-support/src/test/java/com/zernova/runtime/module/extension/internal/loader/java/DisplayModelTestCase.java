/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static com.zernova.runtime.api.meta.model.display.PathModel.Type.ANY;
import static com.zernova.runtime.api.meta.model.display.PathModel.Type.DIRECTORY;
import static com.zernova.runtime.manifest.api.ZernovaManifest.getMuleManifest;
import static com.zernova.test.heisenberg.extension.HeisenbergExtension.LAB_ADDRESS_EXAMPLE;
import static com.zernova.test.heisenberg.extension.HeisenbergExtension.PARAMETER_ORIGINAL_OVERRIDED_DISPLAY_NAME;
import static com.zernova.test.heisenberg.extension.HeisenbergExtension.PARAMETER_OVERRIDED_DISPLAY_NAME;
import static com.zernova.test.heisenberg.extension.HeisenbergExtension.RICIN_PACKS_SUMMARY;
import static com.zernova.test.heisenberg.extension.HeisenbergOperations.DOOR_PARAMETER;
import static com.zernova.test.heisenberg.extension.HeisenbergOperations.GREETING_PARAMETER;
import static com.zernova.test.heisenberg.extension.HeisenbergOperations.KNOCKEABLE_DOORS_SUMMARY;
import static com.zernova.test.heisenberg.extension.HeisenbergOperations.OPERATION_PARAMETER_EXAMPLE;
import static com.zernova.test.heisenberg.extension.HeisenbergOperations.OPERATION_PARAMETER_ORIGINAL_OVERRIDED_DISPLAY_NAME;
import static com.zernova.test.heisenberg.extension.HeisenbergOperations.OPERATION_PARAMETER_OVERRIDED_DISPLAY_NAME;
import static com.zernova.test.module.extension.internal.util.ExtensionDeclarationTestUtils.declarerFor;

import com.zernova.runtime.api.meta.model.declaration.fluent.ConfigurationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ConnectedDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ConnectionProviderDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.WithOperationsDeclaration;
import com.zernova.runtime.api.meta.model.display.ClassValueModel;
import com.zernova.runtime.api.meta.model.display.DisplayModel;
import com.zernova.runtime.api.meta.model.display.PathModel;
import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.Configurations;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.display.ClassValue;
import com.zernova.runtime.extension.api.annotation.param.display.DisplayName;
import com.zernova.runtime.extension.api.annotation.param.display.Example;
import com.zernova.runtime.extension.api.annotation.param.display.Summary;
import com.zernova.runtime.extension.api.exception.IllegalParameterModelDefinitionException;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.test.heisenberg.extension.HeisenbergExtension;
import com.zernova.test.heisenberg.extension.HeisenbergOperations;
import com.zernova.test.marvel.MarvelExtension;
import com.zernova.test.marvel.MissileProvider;
import com.zernova.test.marvel.ironman.IronMan;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class DisplayModelTestCase extends AbstractZernovaTestCase {

  private static final String PARAMETER_GROUP_DISPLAY_NAME = "Date of decease";
  private static final String PARAMETER_GROUP_ORIGINAL_DISPLAY_NAME = "dateOfDeath";
  private ExtensionDeclarer heisenbergDeclarer;
  private ExtensionDeclarer marvelDeclarer;

  @Before
  public void setUp() {
    String version = getMuleManifest().getProductVersion();
    heisenbergDeclarer = declarerFor(HeisenbergExtension.class, version);
    marvelDeclarer = declarerFor(MarvelExtension.class, version);
  }

  @Test
  public void parseDisplayAnnotationsOnParameter() {
    ExtensionDeclaration extensionDeclaration = heisenbergDeclarer.getDeclaration();
    List<ParameterDeclaration> parameters = extensionDeclaration.getConfigurations().get(0).getAllParameters();

    assertParameterDisplayName(findParameter(parameters, PARAMETER_ORIGINAL_OVERRIDED_DISPLAY_NAME),
                               PARAMETER_OVERRIDED_DISPLAY_NAME);
  }

  @Test
  public void parseDisplayNameAnnotationOnParameterGroup() {
    ExtensionDeclaration extensionDeclaration = heisenbergDeclarer.getDeclaration();
    List<ParameterDeclaration> parameters = extensionDeclaration.getConfigurations().get(0).getAllParameters();

    assertParameterDisplayName(findParameter(parameters, PARAMETER_GROUP_ORIGINAL_DISPLAY_NAME), PARAMETER_GROUP_DISPLAY_NAME);
  }

  @Test
  public void parseDisplayNameAnnotationOnOperationParameter() {
    ExtensionDeclaration extensionDeclaration = heisenbergDeclarer.getDeclaration();
    OperationDeclaration operation =
        getOperation(extensionDeclaration, HeisenbergOperations.OPERATION_WITH_DISPLAY_NAME_PARAMETER);

    assertThat(operation, is(notNullValue()));
    List<ParameterDeclaration> parameters = operation.getAllParameters();

    assertParameterDisplayName(findParameter(parameters, OPERATION_PARAMETER_ORIGINAL_OVERRIDED_DISPLAY_NAME),
                               OPERATION_PARAMETER_OVERRIDED_DISPLAY_NAME);
  }

  @Test
  public void parseSummaryAnnotationOnConfigParameter() {
    ExtensionDeclaration extensionDeclaration = heisenbergDeclarer.getDeclaration();
    List<ParameterDeclaration> parameters = extensionDeclaration.getConfigurations().get(0).getAllParameters();

    assertParameterSummary(findParameter(parameters, "ricinPacks"), RICIN_PACKS_SUMMARY);
  }

  @Test
  public void parseSummaryAnnotationOnOperationParameter() {
    ExtensionDeclaration extensionDeclaration = heisenbergDeclarer.getDeclaration();
    OperationDeclaration operation =
        getOperation(extensionDeclaration, HeisenbergOperations.OPERATION_WITH_SUMMARY);

    assertThat(operation, is(notNullValue()));
    List<ParameterDeclaration> parameters = operation.getAllParameters();

    assertParameterSummary(findParameter(parameters, DOOR_PARAMETER), KNOCKEABLE_DOORS_SUMMARY);
  }

  @Test
  public void parseExampleAnnotationOnConfigParameter() {
    ExtensionDeclaration extensionDeclaration = heisenbergDeclarer.getDeclaration();
    List<ParameterDeclaration> parameters = extensionDeclaration.getConfigurations().get(0).getAllParameters();

    assertParameterExample(findParameter(parameters, "labAddress"), LAB_ADDRESS_EXAMPLE);
  }

  @Test
  public void parseExampleAnnotationOnOperationParameter() {
    ExtensionDeclaration extensionDeclaration = heisenbergDeclarer.getDeclaration();
    OperationDeclaration operation =
        getOperation(extensionDeclaration, HeisenbergOperations.OPERATION_WITH_EXAMPLE);

    assertThat(operation, is(notNullValue()));
    List<ParameterDeclaration> parameters = operation.getAllParameters();

    assertParameterExample(findParameter(parameters, GREETING_PARAMETER), OPERATION_PARAMETER_EXAMPLE);
  }

  @Test
  public void parseClassParameterInOperation() {
    ExtensionDeclaration declaration = marvelDeclarer.getDeclaration();
    OperationDeclaration findInstructionsOperation = getOperation(declaration, "findInstructions");
    List<ParameterDeclaration> params = findInstructionsOperation.getAllParameters();
    assertThat(params, hasSize(2));

    ParameterDeclaration pathParam = params.get(1);
    Optional<ClassValueModel> classValueModel = pathParam.getDisplayModel().getClassValueModel();
    assertThat(classValueModel.isPresent(), is(true));
    assertThat(classValueModel.get().getAssignableFrom(), hasSize(1));
    assertThat(classValueModel.get().getAssignableFrom().get(0), equalTo("com.starkindustries.Reader"));
  }

  @Test
  public void parsePathParameterWithFileExtensions() {
    ExtensionDeclaration declaration = marvelDeclarer.getDeclaration();
    OperationDeclaration findInstructionsOperation = getOperation(declaration, "findInstructions");
    List<ParameterDeclaration> params = findInstructionsOperation.getAllParameters();
    assertThat(params, hasSize(2));

    ParameterDeclaration pathParam = params.get(0);
    Optional<PathModel> pathModel = pathParam.getDisplayModel().getPathModel();
    assertThat(pathModel.isPresent(), is(true));
    assertThat(pathModel.get().getType(), is(ANY));
    assertThat(pathModel.get().acceptsUrls(), is(false));
    assertThat(pathModel.get().getFileExtensions(), hasItem("xml"));
  }

  @Test
  public void parsePathParameterThatIsDirectory() {
    ExtensionDeclaration declaration = marvelDeclarer.getDeclaration();
    ConfigurationDeclaration config = findConfigByName(declaration, IronMan.CONFIG_NAME);
    ConnectionProviderDeclaration missileProvider = findProviderByName(config, MissileProvider.NAME);

    List<ParameterDeclaration> params = missileProvider.getAllParameters();
    assertThat(params, hasSize(1));

    ParameterDeclaration pathParam = params.get(0);
    Optional<PathModel> pathModel = pathParam.getDisplayModel().getPathModel();
    assertThat(pathModel.isPresent(), is(true));
    assertThat(pathModel.get().getType(), is(DIRECTORY));
    assertThat(pathModel.get().acceptsUrls(), is(false));
    assertThat(pathModel.get().getFileExtensions(), empty());
  }

  @Test
  public void parseSimplePathParameter() {
    ExtensionDeclaration declaration = marvelDeclarer.getDeclaration();
    ConfigurationDeclaration config = findConfigByName(declaration, IronMan.CONFIG_NAME);

    List<ParameterDeclaration> params = config.getAllParameters();
    assertThat(params, hasSize(2));

    ParameterDeclaration pathParam = params.get(0);
    Optional<PathModel> pathModel = pathParam.getDisplayModel().getPathModel();
    assertThat(pathModel.isPresent(), is(true));
    assertThat(pathModel.get().getType(), is(ANY));
    assertThat(pathModel.get().acceptsUrls(), is(true));
    assertThat(pathModel.get().getFileExtensions(), empty());
  }

  @Test
  public void parseClassParameter() {
    ExtensionDeclaration declaration = marvelDeclarer.getDeclaration();
    ConfigurationDeclaration config = findConfigByName(declaration, IronMan.CONFIG_NAME);

    List<ParameterDeclaration> params = config.getAllParameters();
    assertThat(params, hasSize(2));

    ParameterDeclaration pathParam = params.get(1);
    Optional<ClassValueModel> classValueModel = pathParam.getDisplayModel().getClassValueModel();
    assertThat(classValueModel.isPresent(), is(true));
    assertThat(classValueModel.get().getAssignableFrom(), hasSize(1));
    assertThat(classValueModel.get().getAssignableFrom().get(0), equalTo("com.starkindustries.AIEngine"));
  }

  @Test(expected = IllegalParameterModelDefinitionException.class)
  public void parseLegacyAndSdkDisplayNameAnnotationsOnParameter() {
    declarerFor(ExtensionWithInvalidUseOfDisplayNameAnnotation.class, "1.0.0-dev");
  }

  @Test(expected = IllegalParameterModelDefinitionException.class)
  public void parseLegacyAndSdkExampleAnnotationsOnParameter() {
    declarerFor(ExtensionWithInvalidUseOfExampleAnnotation.class, "1.0.0-dev");
  }

  @Test(expected = IllegalParameterModelDefinitionException.class)
  public void parseLegacyAndSdkSummaryAnnotationsOnParameter() {
    declarerFor(ExtensionWithInvalidUseOfSummaryAnnotation.class, "1.0.0-dev");
  }

  @Test(expected = IllegalParameterModelDefinitionException.class)
  public void parseLegacyAndSdkClassValueAnnotationsOnParameter() {
    declarerFor(ExtensionWithInvalidUseOfClassValueAnnotation.class, "1.0.0-dev");
  }

  @Test
  public void parseConfigurationWithDisplayName() {
    ExtensionDeclarer extensionDeclarer = declarerFor(ExtensionWithConfigWithDisplayName.class, "1.0.0-dev");
    ConfigurationDeclaration configurationDeclaration =
        findConfigByName(extensionDeclarer.getDeclaration(), "configurationWithDisplayName");

    assertThat(configurationDeclaration.getDisplayModel(), is(notNullValue()));
    assertThat(configurationDeclaration.getDisplayModel().getDisplayName(), is("Config display name"));
  }

  private ConfigurationDeclaration findConfigByName(ExtensionDeclaration declaration, String name) {
    return declaration.getConfigurations().stream().filter(c -> c.getName().equals(name)).findAny().get();
  }

  private ConnectionProviderDeclaration findProviderByName(ConnectedDeclaration<?> declaration, String name) {
    return declaration.getConnectionProviders().stream().filter(c -> c.getName().equals(name)).findAny().get();
  }

  private void assertParameterDisplayName(ParameterDeclaration param, String displayName) {
    DisplayModel display = param.getDisplayModel();
    assertThat(display.getDisplayName(), is(displayName));
  }

  private void assertParameterSummary(ParameterDeclaration param, String summary) {
    DisplayModel display = param.getDisplayModel();
    assertThat(display.getSummary(), is(summary));
  }

  private void assertParameterExample(ParameterDeclaration param, String example) {
    DisplayModel display = param.getDisplayModel();
    assertThat(display.getExample(), is(example));
  }

  private OperationDeclaration getOperation(WithOperationsDeclaration declaration, final String operationName) {
    List<OperationDeclaration> operations = declaration.getOperations();
    return operations.stream()
        .filter(operation -> operation.getName().equals(operationName))
        .findAny()
        .orElse(null);
  }

  private ParameterDeclaration findParameter(List<ParameterDeclaration> parameters, final String name) {
    return parameters.stream()
        .filter(param -> param.getName().equals(name))
        .findAny()
        .orElse(null);
  }

  @Extension(name = "extensionWithInvalidUseOfDisplayNameAnnotation")
  public static class ExtensionWithInvalidUseOfDisplayNameAnnotation {

    @Parameter
    @DisplayName("niceFirstParameter")
    @com.zernova.sdk.api.annotation.param.display.DisplayName("niceFirstParameter")
    public String firstParameter;
  }

  @Extension(name = "extensionWithInvalidUseOfExampleAnnotation")
  public static class ExtensionWithInvalidUseOfExampleAnnotation {

    @Parameter
    @Example("parameter")
    @com.zernova.sdk.api.annotation.param.display.Example("parameter")
    public String firstParameter;
  }

  @Extension(name = "extensionWithInvalidUseOfSummaryAnnotation")
  public static class ExtensionWithInvalidUseOfSummaryAnnotation {

    @Parameter
    @Summary("This is a test parameter")
    @com.zernova.sdk.api.annotation.param.display.Summary("This is a test parameter")
    public String firstParameter;
  }

  @Extension(name = "extensionWithInvalidUseOfClassValueAnnotation")
  public static class ExtensionWithInvalidUseOfClassValueAnnotation {

    @Parameter
    @ClassValue
    @com.zernova.sdk.api.annotation.param.display.ClassValue
    public String firstParameter;
  }

  @Extension(name = "extensionWithConfigWithDisplayName")
  @Configurations({ConfigurationWithDisplayName.class})
  public static class ExtensionWithConfigWithDisplayName {
  }

  @Configuration(name = "configurationWithDisplayName")
  @DisplayName("Config display name")
  public static class ConfigurationWithDisplayName {
  }
}
