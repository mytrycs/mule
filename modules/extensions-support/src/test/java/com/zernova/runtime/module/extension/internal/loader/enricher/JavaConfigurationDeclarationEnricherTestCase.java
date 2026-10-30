/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.enricher;

import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static com.zernova.runtime.api.dsl.DslResolvingContext.getDefault;
import static com.zernova.runtime.manifest.api.ZernovaManifest.getMuleManifest;
import static com.zernova.runtime.module.extension.internal.loader.enricher.EnricherTestUtils.checkIsPresent;
import static com.zernova.runtime.module.extension.internal.loader.enricher.EnricherTestUtils.getDeclaration;
import static com.zernova.test.module.extension.internal.util.ExtensionDeclarationTestUtils.declarerFor;

import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.SourceDeclaration;
import com.zernova.runtime.extension.internal.loader.DefaultExtensionLoadingContext;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.JavaConfigurationDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.property.ConfigTypeModelProperty;
import com.zernova.test.heisenberg.extension.HeisenbergExtension;

import org.junit.Before;
import org.junit.Test;

public class JavaConfigurationDeclarationEnricherTestCase {

  private static final String GET_ENEMY = "getEnemy";
  private static final String LISTEN_PAYMENTS = "ListenPayments";
  private ExtensionDeclaration declaration = null;

  @Before
  public void setUp() {
    ExtensionDeclarer declarer = declarerFor(HeisenbergExtension.class, getMuleManifest().getProductVersion());
    new JavaConfigurationDeclarationEnricher()
        .enrich(new DefaultExtensionLoadingContext(declarer, getClass().getClassLoader(), getDefault(emptySet())));
    declaration = declarer.getDeclaration();
  }

  @Test
  public void verifyConfigurationModelPropertyOnOperation() {
    OperationDeclaration operationDeclaration = getDeclaration(declaration.getConfigurations().get(0).getOperations(), GET_ENEMY);
    final ConfigTypeModelProperty configTypeModelProperty = checkIsPresent(operationDeclaration, ConfigTypeModelProperty.class);

    assertType(configTypeModelProperty);
  }

  @Test
  public void verifyConfigurationModelPropertyOnSource() {
    SourceDeclaration sourceDeclaration =
        getDeclaration(declaration.getConfigurations().get(0).getMessageSources(), LISTEN_PAYMENTS);
    final ConfigTypeModelProperty configTypeModelProperty = checkIsPresent(sourceDeclaration, ConfigTypeModelProperty.class);

    assertType(configTypeModelProperty);
  }

  private void assertType(ConfigTypeModelProperty configTypeModelProperty) {
    assertThat(configTypeModelProperty.getConfigType(), equalTo(HeisenbergExtension.class));
  }
}
