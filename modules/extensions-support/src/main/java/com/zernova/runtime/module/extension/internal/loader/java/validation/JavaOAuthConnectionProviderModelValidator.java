/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.validation;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static com.zernova.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static com.zernova.runtime.module.extension.internal.loader.utils.JavaParserUtils.getExpressionSupport;
import static com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ExtensionsOAuthUtils.AUTHORIZATION_CODE_STATE_INTERFACES;
import static com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ExtensionsOAuthUtils.CLIENT_CREDENTIALS_STATE_INTERFACES;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getFields;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getImplementingType;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.util.IdempotentExtensionWalker;
import com.zernova.runtime.extension.api.connectivity.oauth.AuthorizationCodeGrantType;
import com.zernova.runtime.extension.api.connectivity.oauth.ClientCredentialsGrantType;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthModelProperty;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthParameterModelProperty;
import com.zernova.runtime.extension.api.exception.IllegalConnectionProviderModelDefinitionException;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.loader.Problem;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;
import com.zernova.runtime.module.extension.internal.loader.java.property.DeclaringMemberModelProperty;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Validates that OAuth enabled {@link ConnectionProvider}s are well formed
 *
 * @since 4.0
 */
public class JavaOAuthConnectionProviderModelValidator implements ExtensionModelValidator {

  @Override
  public void validate(ExtensionModel model, ProblemsReporter problemsReporter) {
    new IdempotentExtensionWalker() {

      @Override
      protected void onConnectionProvider(ConnectionProviderModel model) {
        Class<?> implementingType = getImplementingType(model).orElse(null);

        if (implementingType == null) {
          return;
        }

        boolean supportsAuthCode = supportsAuthorizationCode(model);

        boolean supportsClientCredentials = supportsClientCredentials(model);

        if (supportsAuthCode && supportsClientCredentials) {
          throw new IllegalConnectionProviderModelDefinitionException(format(
                                                                             "Connection Provider of class '%s' is attempting to support both authorization code and client credentials "
                                                                                 + "grant types. Each connection provider can only support one grant type at a time.",
                                                                             implementingType));
        }

        if (supportsAuthCode) {
          validateStateField(implementingType, AUTHORIZATION_CODE_STATE_INTERFACES, "authorization code");
        }

        if (supportsClientCredentials) {
          validateStateField(implementingType, CLIENT_CREDENTIALS_STATE_INTERFACES, "client credentials");
        }

        validateOAuthParameters(model, problemsReporter);
      }
    }.walk(model);
  }

  private void validateOAuthParameters(ConnectionProviderModel connectionProviderModel, ProblemsReporter problemsReporter) {
    connectionProviderModel
        .getAllParameterModels().stream().filter(
                                                 parameterModel -> parameterModel
                                                     .getModelProperty(OAuthParameterModelProperty.class).isPresent())
        .forEach(
                 parameterModel -> parameterModel.getModelProperty(DeclaringMemberModelProperty.class)
                     .map(DeclaringMemberModelProperty::getDeclaringField)
                     .ifPresent(field -> validateExpressionSupport(connectionProviderModel,
                                                                   parameterModel,
                                                                   field,
                                                                   problemsReporter)));
  }

  private void validateExpressionSupport(ConnectionProviderModel provider,
                                         ParameterModel parameter,
                                         Field field,
                                         ProblemsReporter problemsReporter) {
    if (getExpressionSupport(field)
        .filter(expression -> expression != NOT_SUPPORTED)
        .isPresent()) {
      problemsReporter.addError(new Problem(parameter, format(
                                                              "Parameter '%s' in Connection Provider '%s' is marked as supporting expressions. Expressions are not supported "
                                                                  + "in OAuth parameters",
                                                              parameter.getName(),
                                                              provider.getName())));
    }
  }

  private void validateStateField(Class<?> implementingType, List<Class<?>> stateFieldTypes, String grantType) {
    List<Field> stateFields = getFields(implementingType).stream()
        .filter(f -> stateFieldTypes.stream().anyMatch(stateFieldType -> f.getType().equals(stateFieldType)))
        .collect(toList());

    if (stateFields.size() != 1) {
      throw new IllegalConnectionProviderModelDefinitionException(
                                                                  format("Connection Provider of class '%s' uses OAuth2 %s grant type and thus should contain "
                                                                      + "one (and only one) field of type %s. %d were found",
                                                                         implementingType,
                                                                         grantType,
                                                                         stateFieldTypes.get(0).getName(),
                                                                         stateFields.size()));
    }
  }

  private boolean supportsAuthorizationCode(ConnectionProviderModel connectionProviderModel) {
    return connectionProviderModel.getModelProperty(OAuthModelProperty.class)
        .map(oAuthModelProperty -> oAuthModelProperty.getGrantTypes()
            .stream().filter(oAuthGrantType -> oAuthGrantType instanceof AuthorizationCodeGrantType).findFirst().isPresent())
        .orElse(false);
  }

  private boolean supportsClientCredentials(ConnectionProviderModel connectionProviderModel) {
    return connectionProviderModel.getModelProperty(OAuthModelProperty.class)
        .map(oAuthModelProperty -> oAuthModelProperty.getGrantTypes().stream()
            .filter(oAuthGrantType -> oAuthGrantType instanceof ClientCredentialsGrantType).findFirst().isPresent())
        .orElse(false);
  }

}
