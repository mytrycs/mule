/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java;

import static com.zernova.runtime.api.dsl.DslResolvingContext.getDefault;
import static com.zernova.runtime.api.meta.ExpressionSupport.SUPPORTED;
import static com.zernova.runtime.manifest.api.ZernovaManifest.getMuleManifest;
import static com.zernova.test.module.extension.internal.util.ExtensionDeclarationTestUtils.declarerFor;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.validate;

import static java.util.Collections.emptySet;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.extension.api.annotation.Expression;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.connectivity.oauth.AuthorizationCode;
import com.zernova.runtime.extension.api.annotation.connectivity.oauth.OAuthParameter;
import com.zernova.runtime.extension.api.connectivity.oauth.AuthorizationCodeState;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.extension.internal.loader.DefaultExtensionLoadingContext;
import com.zernova.runtime.extension.internal.loader.ExtensionModelFactory;
import com.zernova.runtime.module.extension.internal.loader.java.validation.JavaOAuthConnectionProviderModelValidator;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.test.oauth.TestOAuthConnection;

import io.qameta.allure.Issue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JavaOAuthConnectionProviderModelValidatorTestCase extends AbstractZernovaTestCase {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private final JavaOAuthConnectionProviderModelValidator validator = new JavaOAuthConnectionProviderModelValidator();

  @Test
  @Issue("W-14180661")
  public void verifyExpressionsNotSupportedInOAuthParameters() {
    exception.expect(IllegalModelDefinitionException.class);
    exception.expectMessage("is marked as supporting expressions. Expressions are not supported in OAuth parameters");

    Class<?> extensionClass = OAuthWithParameterSupportingExpressions.class;
    DefaultExtensionLoadingContext ctx = new DefaultExtensionLoadingContext(
                                                                            extensionClass.getClassLoader(),
                                                                            getDefault(emptySet()));

    declarerFor(OAuthWithParameterSupportingExpressions.class, getMuleManifest().getProductVersion(), ctx);

    ExtensionModel extensionModel = new ExtensionModelFactory().create(ctx);

    validate(extensionModel, validator);
  }

  @Extension(name = "OAuth extension with parameter supporting expressions")
  @ConnectionProviders({OAuthConnectionProviderWithParameterSupportingExpressions.class})
  public static class OAuthWithParameterSupportingExpressions {
  }

  @AuthorizationCode(accessTokenUrl = "tokenUrl",
      authorizationUrl = "authorizationUrl",
      defaultScopes = "defaultScope")
  private static class OAuthConnectionProviderWithParameterSupportingExpressions
      implements ConnectionProvider<TestOAuthConnection> {

    private AuthorizationCodeState oauthState;

    @Expression(SUPPORTED)
    @OAuthParameter
    private String state;

    public OAuthConnectionProviderWithParameterSupportingExpressions() {}

    @Override
    public TestOAuthConnection connect() throws ConnectionException {
      return null;
    }

    public void disconnect(TestOAuthConnection connection) {

    }

    @Override
    public ConnectionValidationResult validate(TestOAuthConnection connection) {
      return null;
    }

  }

}
