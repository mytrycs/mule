/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.capability.xml;

import static com.zernova.runtime.api.util.ZernovaSystemProperties.SYSTEM_PROPERTY_PREFIX;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.compareXML;

import static java.lang.Boolean.getBoolean;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.zernova.extension.test.extension.reconnection.ReconnectionExtension;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.extension.api.dsl.syntax.resources.spi.ExtensionSchemaGenerator;
import com.zernova.runtime.module.extension.internal.capability.xml.schema.DefaultExtensionSchemaGenerator;
import com.zernova.tck.size.SmallTest;
import com.zernova.test.function.extension.WeaveFunctionExtension;
import com.zernova.test.heisenberg.extension.HeisenbergExtension;
import com.zernova.test.implicit.config.extension.extension.api.ImplicitConfigExtension;
import com.zernova.test.marvel.MarvelExtension;
import com.zernova.test.metadata.extension.MetadataExtension;
import com.zernova.test.module.extension.internal.FileGenerationParameterizedExtensionModelTestCase;
import com.zernova.test.module.extension.internal.util.extension.connectivity.basic.GlobalInnerPojoConnector;
import com.zernova.test.module.extension.internal.util.extension.connectivity.basic.GlobalPojoConnector;
import com.zernova.test.module.extension.internal.util.extension.connectivity.basic.ListConnector;
import com.zernova.test.module.extension.internal.util.extension.connectivity.basic.MapConnector;
import com.zernova.test.module.extension.internal.util.extension.connectivity.basic.StringListConnector;
import com.zernova.test.module.extension.internal.util.extension.connectivity.basic.TestConnector;
import com.zernova.test.nonimplicit.config.extension.extension.api.NonImplicitConfigExtension;
import com.zernova.test.oauth.TestOAuthExtension;
import com.zernova.test.petstore.extension.PetStoreConnector;
import com.zernova.test.semantic.extension.SemanticTermsExtension;
import com.zernova.test.substitutiongroup.extension.SubstitutionGroupExtension;
import com.zernova.test.subtypes.extension.SubTypesMappingConnector;
import com.zernova.test.transactional.TransactionalExtension;
import com.zernova.test.typed.value.extension.extension.TypedValueExtension;
import com.zernova.test.values.extension.ValuesExtension;
import com.zernova.test.vegan.extension.VeganExtension;

import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@SmallTest
@RunWith(Parameterized.class)
public class DefaultExtensionSchemaGeneratorTestCase extends FileGenerationParameterizedExtensionModelTestCase {

  private static final boolean UPDATE_EXPECTED_FILES_ON_ERROR =
      getBoolean(SYSTEM_PROPERTY_PREFIX + "extensionSchemas.updateExpectedFilesOnError");

  private final ExtensionSchemaGenerator generator = new DefaultExtensionSchemaGenerator();

  @Parameterized.Parameters(name = "{2}")
  public static Collection<Object[]> data() {
    return asList(
                  new Object[] {JAVA_LOADER, MapConnector.class, "map.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, ListConnector.class, "list.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, TestConnector.class, "basic.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, StringListConnector.class, "string-list.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, GlobalPojoConnector.class, "global-pojo.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, GlobalInnerPojoConnector.class, "global-inner-pojo.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, VeganExtension.class, "vegan.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, PetStoreConnector.class, "petstore.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, MetadataExtension.class, "metadata.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, HeisenbergExtension.class, "heisenberg.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, SubstitutionGroupExtension.class, "substitutiongroup.xsd",
                      null, singletonList(HeisenbergExtension.class)},
                  new Object[] {JAVA_LOADER, TransactionalExtension.class, "tx-ext.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, SubTypesMappingConnector.class, "subtypes.xsd",
                      null, asList(HeisenbergExtension.class, MetadataExtension.class, VeganExtension.class)},
                  new Object[] {JAVA_LOADER, MarvelExtension.class, "marvel.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, TypedValueExtension.class, "typed-value.xsd",
                      null, singletonList(HeisenbergExtension.class)},
                  new Object[] {JAVA_LOADER, TestOAuthExtension.class, "test-oauth.xsd",
                      null, singletonList(ValuesExtension.class)},
                  new Object[] {JAVA_LOADER, WeaveFunctionExtension.class, "test-fn.xsd",
                      null, singletonList(HeisenbergExtension.class)},
                  new Object[] {JAVA_LOADER, ValuesExtension.class, "values.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, ImplicitConfigExtension.class, "implicit-config.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, NonImplicitConfigExtension.class, "non-implicit-config.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, SemanticTermsExtension.class, "semantic-terms-extension.xsd",
                      null, emptyList()},
                  new Object[] {JAVA_LOADER, ReconnectionExtension.class, "reconnection-extension.xsd",
                      null, emptyList()});
  }

  @Parameterized.Parameter(1)
  public Class extensionClass;

  @Override
  protected boolean shouldUpdateExpectedFilesOnError() {
    return UPDATE_EXPECTED_FILES_ON_ERROR;
  }

  @Override
  protected String getExpectedFilesDir() {
    return "schemas/";
  }

  @Override
  protected String doGenerate(ExtensionModel extensionUnderTest) throws Exception {
    return generator.generate(extensionUnderTest, dslResolvingContext);
  }

  @Override
  protected void assertEquals(String expectedContent, String actualContent) throws Exception {
    compareXML(expectedContent, actualContent);
  }

  @Override
  protected ExtensionModel doLoadExtension() {
    return loadExtension(extensionClass, loader, artifactCoordinates, dslResolvingContext);
  }
}
