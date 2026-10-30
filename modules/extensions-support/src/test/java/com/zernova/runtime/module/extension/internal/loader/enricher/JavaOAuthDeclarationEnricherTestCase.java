/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.enricher;

import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static com.zernova.runtime.api.dsl.DslResolvingContext.getDefault;
import static com.zernova.runtime.extension.api.loader.ExtensionModelLoadingRequest.builder;
import static com.zernova.runtime.manifest.api.ZernovaManifest.getMuleManifest;
import static com.zernova.runtime.module.extension.internal.loader.enricher.EnricherTestUtils.getDeclaration;
import static com.zernova.test.module.extension.internal.util.ExtensionDeclarationTestUtils.declarerFor;

import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.extension.internal.loader.DefaultExtensionLoadingContext;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.JavaOAuthDeclarationEnricher;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.test.oauth.TestOAuthExtension;

import org.junit.Before;
import org.junit.Test;

public class JavaOAuthDeclarationEnricherTestCase extends AbstractZernovaTestCase {

  private ExtensionDeclaration declaration = null;

  @Before
  public void setUp() {
    ExtensionDeclarer declarer = declarerFor(TestOAuthExtension.class, getMuleManifest().getProductVersion());
    new JavaOAuthDeclarationEnricher().enrich(
                                              new DefaultExtensionLoadingContext(declarer,
                                                                                 builder(getClass().getClassLoader(),
                                                                                         getDefault(emptySet())).build()));
    declaration = declarer.getDeclaration();
  }

  @Test
  public void verifyUnauthorizeOperationIsAddedToAllConfigs() {
    declaration.getConfigurations().stream().forEach(configurationDeclaration -> {
      OperationDeclaration operationDeclaration = getDeclaration(configurationDeclaration.getOperations(), "unauthorize");
      assertThat(operationDeclaration, is(not(nullValue())));
    });
  }

}
