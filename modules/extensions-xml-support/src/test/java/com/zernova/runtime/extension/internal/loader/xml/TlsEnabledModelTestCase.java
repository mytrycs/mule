/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.xml;


import static com.zernova.runtime.api.dsl.DslResolvingContext.getDefault;
import static com.zernova.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel.DEFAULT_GROUP_NAME;
import static com.zernova.runtime.api.meta.model.parameter.ParameterRole.BEHAVIOUR;
import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.getExtensionModel;
import static com.zernova.runtime.extension.api.loader.ExtensionModelLoadingRequest.builder;
import static com.zernova.runtime.extension.api.loader.util.InfrastructureTypeUtils.getMetadataTypeBasedInfrastructureType;
import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.getType;
import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.isInfrastructure;
import static com.zernova.runtime.extension.internal.loader.xml.XmlExtensionModelLoader.RESOURCE_XML;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.loadExtension;

import static java.util.Collections.singleton;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.ParameterDslConfiguration;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.tls.TlsContextFactory;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoadingRequest;
import com.zernova.runtime.extension.api.loader.util.InfrastructureTypeUtils.MetadataTypeBasedInfrastructureType;
import com.zernova.runtime.extension.api.property.InfrastructureParameterModelProperty;
import com.zernova.runtime.extension.api.property.QNameModelProperty;
import com.zernova.runtime.extension.api.property.SyntheticModelModelProperty;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.test.heisenberg.extension.HeisenbergExtension;
import com.zernova.test.petstore.extension.PetStoreConnector;

import java.util.HashSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the generation of the {@link ExtensionModel} for modules that contain elements marked as TLS-enabled.
 */
@SmallTest
public class TlsEnabledModelTestCase extends AbstractZernovaTestCase {

  private static final ExtensionModel PET_STORE_EXTENSION_MODEL = loadExtension(PetStoreConnector.class);
  private static final ExtensionModel HEISENBERG_EXTENSION_MODEL = loadExtension(HeisenbergExtension.class);
  private static final String EXPECTED_TLS_CONTEXT_PARAM_NAME = "tlsContext";

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void generatedModelHasTlsContextParameter() {
    ExtensionModel extensionModel = loadExtensionModelFrom("modules/module-tls-config.xml",
                                                           singleton(PET_STORE_EXTENSION_MODEL));
    assertModelHasTlsContextParameter(extensionModel, false);
  }

  @Test
  public void whenTargetRequiresTlsContextThenGeneratedModelHasRequiredTlsContextParameter() {
    ExtensionModel extensionModel = loadExtensionModelFrom("modules/module-tls-config-required.xml",
                                                           singleton(HEISENBERG_EXTENSION_MODEL));
    assertModelHasTlsContextParameter(extensionModel, true);
  }

  @Test
  public void whenTargetRequiresTlsContextAndProvidesOneThenGeneratedModelHasOptionalTlsContextParameter() {
    ExtensionModel extensionModel = loadExtensionModelFrom("modules/module-tls-config-required-with-default.xml",
                                                           singleton(HEISENBERG_EXTENSION_MODEL));
    assertModelHasTlsContextParameter(extensionModel, false);
  }

  @Test
  public void multipleElementsWithXmlnsTlsEnabledAttribute() {
    exception.expect(ZernovaRuntimeException.class);
    exception.expectMessage("There can only be one global element marked with [xmlns:tlsEnabled] but found [2], "
        + "offending global elements are: [petstore-config, unnamed@module-tls-config/2/connection]");
    loadExtensionModelFrom("validation/tls/module-tls-config-multiple-annotated-elems.xml",
                           singleton(PET_STORE_EXTENSION_MODEL));
  }

  @Test
  public void unsupportedTlsConfigurationTarget() {
    exception.expect(ZernovaRuntimeException.class);
    exception.expectMessage("The annotated element [unnamed@module-tls-config/2/1/0] with [xmlns:tlsEnabled] is not "
        + "valid to be configured for TLS (the component [petstore:pet] does not support it)");
    loadExtensionModelFrom("validation/tls/module-tls-config-unsupported-target.xml",
                           singleton(PET_STORE_EXTENSION_MODEL));
  }

  @Test
  public void unsupportedTlsConfigurationTargetOperation() {
    exception.expect(ZernovaRuntimeException.class);
    exception.expectMessage("The annotated element [set-payload-hardcoded-value] with [xmlns:tlsEnabled] is not "
        + "valid to be configured for TLS (the component [module:operation] does not support it)");
    loadExtensionModelFrom("validation/tls/module-tls-config-unsupported-target-operation.xml",
                           singleton(PET_STORE_EXTENSION_MODEL));
  }

  @Test
  public void unsupportedTlsConfigurationTargetModule() {
    exception.expect(ZernovaRuntimeException.class);
    exception.expectMessage("The annotated element [module-tls-config] with [xmlns:tlsEnabled] is not "
        + "valid to be configured for TLS (the component [module:module] does not support it)");
    loadExtensionModelFrom("validation/tls/module-tls-config-unsupported-target-module.xml",
                           singleton(PET_STORE_EXTENSION_MODEL));
  }

  private ExtensionModel loadExtensionModelFrom(String modulePath, Set<ExtensionModel> dependencyExtensions) {
    Set<ExtensionModel> allExtensions = new HashSet<>(dependencyExtensions);
    allExtensions.add(getExtensionModel());
    ExtensionModelLoadingRequest request = builder(getClass().getClassLoader(), getDefault(allExtensions))
        .addParameter(RESOURCE_XML, modulePath)
        .build();

    return new XmlExtensionModelLoader().loadExtensionModel(request);
  }

  private void assertModelHasTlsContextParameter(ExtensionModel extensionModel, boolean expectRequired) {
    ConfigurationModel configurationModel = extensionModel.getConfigurationModel("config").get();
    ParameterGroupModel defaultParameterGroupModel = configurationModel.getParameterGroupModels().stream()
        .filter(pgm -> pgm.getName().equals(DEFAULT_GROUP_NAME))
        .findFirst()
        .get();

    ParameterModel tlsContextParameterModel = defaultParameterGroupModel.getParameter(EXPECTED_TLS_CONTEXT_PARAM_NAME).get();
    assertThat(getType(tlsContextParameterModel.getType()).get(), is(TlsContextFactory.class));
    assertThat(isInfrastructure(tlsContextParameterModel.getType()), is(true));
    assertThat(tlsContextParameterModel.isRequired(), is(expectRequired));
    assertThat(tlsContextParameterModel.getRole(), is(BEHAVIOUR));
    assertThat(tlsContextParameterModel.getExpressionSupport(), is(NOT_SUPPORTED));
    assertThat(tlsContextParameterModel.getModelProperty(SyntheticModelModelProperty.class).isPresent(), is(true));
    assertThat(tlsContextParameterModel.getModelProperty(InfrastructureParameterModelProperty.class).isPresent(), is(true));
    MetadataTypeBasedInfrastructureType tlsInfrastructureType = getMetadataTypeBasedInfrastructureType(TlsContextFactory.class);
    assertThat(tlsContextParameterModel.getModelProperty(QNameModelProperty.class),
               is(tlsInfrastructureType.getQNameModelProperty()));

    ParameterDslConfiguration actualDslConfiguration = tlsContextParameterModel.getDslConfiguration();
    ParameterDslConfiguration expectedDslConfiguration = tlsInfrastructureType.getDslConfiguration().get();
    assertThat(actualDslConfiguration.allowTopLevelDefinition(), is(expectedDslConfiguration.allowTopLevelDefinition()));
    assertThat(actualDslConfiguration.allowsReferences(), is(expectedDslConfiguration.allowsReferences()));
    assertThat(actualDslConfiguration.allowsInlineDefinition(), is(expectedDslConfiguration.allowsInlineDefinition()));
  }
}
