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
import static org.hamcrest.MatcherAssert.assertThat;
import static com.zernova.runtime.api.dsl.DslResolvingContext.getDefault;
import static com.zernova.runtime.manifest.api.ZernovaManifest.getMuleManifest;
import static com.zernova.runtime.module.extension.internal.loader.enricher.EnricherTestUtils.getDeclaration;
import static com.zernova.test.module.extension.internal.util.ExtensionDeclarationTestUtils.declarerFor;

import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.extension.api.property.TypeResolversInformationModelProperty;
import com.zernova.runtime.extension.internal.loader.DefaultExtensionLoadingContext;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.DsqlDynamicMetadataDeclarationEnricher;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.test.metadata.extension.MetadataExtension;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class DsqlDynamicMetadataDeclarationEnricherTestCase extends AbstractZernovaTestCase {

  private ExtensionDeclaration declaration;

  @Before
  public void setUp() {
    ExtensionDeclarer declarer = declarerFor(MetadataExtension.class, getMuleManifest().getProductVersion());
    new DsqlDynamicMetadataDeclarationEnricher()
        .enrich(new DefaultExtensionLoadingContext(declarer, getClass().getClassLoader(), getDefault(emptySet())));
    declaration = declarer.getDeclaration();
  }

  @Test
  public void outputTypeResolverInformationSetForDsql() throws Exception {
    OperationDeclaration query = getDeclaration(declaration.getOperations(), "doQuery");
    Optional<TypeResolversInformationModelProperty> info = query
        .getModelProperty(TypeResolversInformationModelProperty.class);

    assertThat(info.isPresent(), is(true));
    assertThat(info.get().getCategoryName(), is("QueryResolverCategory"));
    assertThat(info.get().getOutputResolver().get().getResolverName(), is("QUERY_OUTPUT_RESOLVER-ENTITY-NativeQueryResolver"));
  }

}
