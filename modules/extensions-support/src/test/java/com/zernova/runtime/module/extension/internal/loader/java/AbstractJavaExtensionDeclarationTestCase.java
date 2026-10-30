/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.CONFIG_ATTRIBUTE_NAME;
import static com.zernova.runtime.extension.api.ExtensionConstants.REDELIVERY_POLICY_PARAMETER_NAME;
import static com.zernova.runtime.manifest.api.ZernovaManifest.getMuleManifest;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.assertType;

import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.api.meta.model.declaration.fluent.ConfigurationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterizedDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.WithOperationsDeclaration;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.extension.api.declaration.type.RedeliveryPolicyTypeBuilder;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.test.module.extension.internal.util.ExtensionDeclarationTestUtils;

import java.util.List;

public abstract class AbstractJavaExtensionDeclarationTestCase extends AbstractZernovaTestCase {

  private ExtensionDeclarer declarer;

  protected ExtensionDeclarer getDeclarer() {
    return declarer;
  }

  protected void setDeclarer(ExtensionDeclarer declarer) {
    this.declarer = declarer;
  }

  protected ExtensionDeclarer declarerFor(final Class<?> type) {
    return ExtensionDeclarationTestUtils.declarerFor(type, getMuleManifest().getProductVersion());
  }

  protected ExtensionDeclarer declarerFor(final Class<?> type, String version) {
    return ExtensionDeclarationTestUtils.declarerFor(type, version);
  }

  protected ExtensionDeclarer declarerFor(Class<?> type, ExtensionLoadingContext ctx) {
    return declarerFor(type, getMuleManifest().getProductVersion(), ctx);
  }

  protected ExtensionDeclarer declarerFor(Class<?> type, String version, ExtensionLoadingContext ctx) {
    return ExtensionDeclarationTestUtils.declarerFor(type, version, ctx);
  }

  protected ExtensionDeclarer declareExtension() {
    return declarer;
  }

  protected ConfigurationDeclaration getConfiguration(ExtensionDeclaration extensionDeclaration, final String configurationName) {
    return extensionDeclaration.getConfigurations()
        .stream()
        .filter(config -> config.getName().equals(configurationName))
        .findAny()
        .orElse(null);
  }

  protected OperationDeclaration getOperation(WithOperationsDeclaration declaration, final String operationName) {
    List<OperationDeclaration> operations = declaration.getOperations();
    return operations.stream()
        .filter(operation -> operation.getName().equals(operationName))
        .findAny()
        .orElse(null);
  }

  protected Pair<ParameterGroupDeclaration, ParameterDeclaration> findParameterInGroup(ParameterizedDeclaration<?> declaration,
                                                                                       String name) {
    return declaration.getParameterGroups().stream()
        .map(group -> {
          ParameterDeclaration parameter = findParameter(group.getParameters(), name);
          return parameter != null ? new Pair<>(group, parameter) : null;
        })
        .filter(pair -> pair != null)
        .findFirst().orElse(null);
  }

  protected ParameterDeclaration findParameter(List<ParameterDeclaration> parameters, final String name) {
    return parameters.stream()
        .filter(param -> param.getName().equals(name))
        .findAny()
        .orElse(null);
  }

  protected void assertConfigRefParam(ParameterModel configRef) {
    assertThat(configRef.getName(), is(CONFIG_ATTRIBUTE_NAME));
    assertType(configRef.getType(), ConfigurationProvider.class, ObjectType.class);
  }

  protected void assertRedeliveryPolicyParameter(ParameterModel redelivery) {
    assertThat(redelivery.getName(), is(REDELIVERY_POLICY_PARAMETER_NAME));
    assertThat(redelivery.getType(), is(equalTo(new RedeliveryPolicyTypeBuilder().buildRedeliveryPolicyType())));
  }
}
