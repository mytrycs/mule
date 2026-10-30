/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java;

import static com.zernova.runtime.api.meta.model.connection.ConnectionManagementType.CACHED;
import static com.zernova.runtime.api.meta.model.connection.ConnectionManagementType.NONE;
import static com.zernova.runtime.api.meta.model.connection.ConnectionManagementType.POOLING;
import static com.zernova.runtime.extension.privileged.semantic.SemanticTermsHelper.getConnectionTermsFromAnnotations;
import static com.zernova.runtime.module.extension.internal.ExtensionProperties.DEFAULT_CONNECTION_PROVIDER_NAME;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.JavaExtensionModelParserUtils.getParameterGroupParsers;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.ZernovaExtensionAnnotationParser.mapReduceSingleAnnotation;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.ParameterDeclarationContext.forConnectionProvider;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.connection.JavaConnectionProviderModelParserUtils.isCachedConnectionProvider;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.connection.JavaConnectionProviderModelParserUtils.isDefinedThroughSdkApi;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.connection.JavaConnectionProviderModelParserUtils.isPoolingConnectionProvider;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.connection.SdkCredentialPlacementUtils.from;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.lib.JavaExternalLibModelParserUtils.parseExternalLibraryModels;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.semantics.SemanticTermsParserUtils.addCustomTerms;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.stereotypes.JavaStereotypeModelParserUtils.resolveStereotype;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.utils.MinZernovaVersionUtils.resolveConnectionProviderMinMuleVersion;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getAnnotatedFieldsStream;

import static java.lang.String.format;
import static java.util.Objects.hash;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.ExternalLibraryModel;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.connection.ConnectionManagementType;
import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;
import com.zernova.runtime.api.meta.model.display.DisplayModel;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;
import com.zernova.runtime.extension.api.annotation.connectivity.oauth.AuthorizationCode;
import com.zernova.runtime.extension.api.annotation.connectivity.oauth.ClientCredentials;
import com.zernova.runtime.extension.api.annotation.connectivity.oauth.OAuthCallbackValue;
import com.zernova.runtime.extension.api.connectivity.oauth.AuthorizationCodeGrantType;
import com.zernova.runtime.extension.api.connectivity.oauth.ClientCredentialsGrantType;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthGrantType;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthModelProperty;
import com.zernova.runtime.extension.api.exception.IllegalConnectionProviderModelDefinitionException;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.parser.MinZernovaVersionParser;
import com.zernova.runtime.extension.api.runtime.connectivity.ConnectionProviderFactory;
import com.zernova.runtime.module.extension.api.loader.java.type.AnnotationValueFetcher;
import com.zernova.runtime.module.extension.api.loader.java.type.ConnectionProviderElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionElement;
import com.zernova.runtime.module.extension.api.loader.java.type.Type;
import com.zernova.runtime.module.extension.internal.loader.java.DefaultConnectionProviderFactory;
import com.zernova.runtime.module.extension.internal.loader.java.property.ConnectionTypeModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.property.ImplementingTypeModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.property.oauth.OAuthCallbackValuesModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.type.property.ExtensionTypeDescriptorModelProperty;
import com.zernova.runtime.extension.api.loader.parser.ConnectionProviderModelParser;
import com.zernova.runtime.extension.api.loader.parser.ParameterGroupModelParser;
import com.zernova.runtime.extension.api.loader.parser.StereotypeModelFactory;
import com.zernova.sdk.api.annotation.semantics.connectivity.ExcludeFromConnectivitySchema;
import com.zernova.sdk.api.connectivity.NoConnectivityTest;
import com.zernova.sdk.api.connectivity.XATransactionalConnectionProvider;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * {@link ConnectionProviderModelParser} for Java based syntax
 *
 * @since 4.5.0
 */
public class JavaConnectionProviderModelParser implements ConnectionProviderModelParser {

  private static final String CONNECTION_PROVIDER_NAME = "connection provider";

  private final ConnectionProviderElement element;
  private final ExtensionLoadingContext loadingContext;
  private final List<ModelProperty> additionalModelProperties = new LinkedList<>();
  private final ClassLoader extensionClassLoader;

  public JavaConnectionProviderModelParser(ExtensionElement extensionElement,
                                           ConnectionProviderElement element,
                                           ExtensionLoadingContext loadingContext) {
    this.element = element;
    this.loadingContext = loadingContext;
    extensionClassLoader = extensionElement.getDeclaringClass()
        .map(Class::getClassLoader)
        .orElse(ExtensionModel.class.getClassLoader());

    collectAdditionalModelProperties();
  }

  @Override
  public String getName() {
    if (element.getName().equals(element.getAlias())) {
      return DEFAULT_CONNECTION_PROVIDER_NAME;
    }

    return element.getAlias();
  }

  @Override
  public String getDescription() {
    return element.getDescription();
  }

  @Override
  public List<ParameterGroupModelParser> getParameterGroupModelParsers() {
    return getParameterGroupParsers(element.getParameters(), forConnectionProvider(getName(), loadingContext));
  }

  @Override
  public List<ExternalLibraryModel> getExternalLibraryModels() {
    return parseExternalLibraryModels(element);
  }

  @Override
  public ConnectionManagementType getConnectionManagementType() {
    ConnectionManagementType managementType = NONE;
    if (isPoolingConnectionProvider(element)) {
      managementType = POOLING;
    } else if (isCachedConnectionProvider(element)) {
      managementType = CACHED;
    }

    return managementType;
  }

  @Override
  public Optional<ConnectionProviderFactory<?>> getConnectionProviderFactory() {
    return element.getDeclaringClass()
        .map(declaringClass -> new DefaultConnectionProviderFactory<>(declaringClass, extensionClassLoader));
  }

  @Override
  public boolean supportsConnectivityTesting() {
    return !element.isAssignableTo(NoConnectivityTest.class)
        && !element.isAssignableTo(com.zernova.runtime.extension.api.connectivity.NoConnectivityTest.class);
  }

  @Override
  public boolean supportsXa() {
    return element.isAssignableTo(XATransactionalConnectionProvider.class);
  }

  @Override
  public boolean isExcludedFromConnectivitySchema() {
    return element.isAnnotatedWith(ExcludeFromConnectivitySchema.class);
  }

  @Override
  public Optional<OAuthModelProperty> getOAuthModelProperty() {
    List<OAuthGrantType> grantTypes = new LinkedList<>();
    mapReduceSingleAnnotation(element, CONNECTION_PROVIDER_NAME, element.getName(), AuthorizationCode.class,
                              com.zernova.sdk.api.annotation.connectivity.oauth.AuthorizationCode.class,
                              this::getAuthorizationCodeGrantType,
                              this::getAuthorizationCodeGrantTypeFromSdk)
                                  .ifPresent(grantTypes::add);

    mapReduceSingleAnnotation(element, CONNECTION_PROVIDER_NAME, element.getName(), ClientCredentials.class,
                              com.zernova.sdk.api.annotation.connectivity.oauth.ClientCredentials.class,
                              this::getClientCredentialsGrantType,
                              this::getClientCredentialsGrantTypeFromSdk)
                                  .ifPresent(grantTypes::add);

    return grantTypes.isEmpty() ? empty() : of(new OAuthModelProperty(grantTypes));
  }

  @Override
  public Optional<StereotypeModel> getStereotype(StereotypeModelFactory factory) {
    return resolveStereotype(element, "Connection Provider", getName(), factory);
  }

  @Override
  public List<ModelProperty> getAdditionalModelProperties() {
    return additionalModelProperties;
  }

  private void collectAdditionalModelProperties() {
    Class<?> baseInterface = isDefinedThroughSdkApi(element)
        ? com.zernova.sdk.api.connectivity.ConnectionProvider.class
        : ConnectionProvider.class;

    List<Type> providerGenerics = element.getSuperTypeGenerics(baseInterface);

    if (providerGenerics.size() != 1) {
      // TODO: MULE-9220: Add a syntax validator for this
      throw new IllegalConnectionProviderModelDefinitionException(
                                                                  format("Connection provider class '%s' was expected to have 1 generic type "
                                                                      + "(for the connection type) but %d were found",
                                                                         element.getName(), providerGenerics.size()));
    }

    additionalModelProperties.add(new ConnectionTypeModelProperty(providerGenerics.get(0)));
    element.getDeclaringClass().ifPresent(clazz -> additionalModelProperties.add(new ImplementingTypeModelProperty(clazz)));
    element.getDeclaringClass()
        .ifPresent(clazz -> getOAuthCallbackValuesModelProperty(clazz).ifPresent(additionalModelProperties::add));
    additionalModelProperties.add(new ExtensionTypeDescriptorModelProperty(element));
  }

  private Optional<OAuthCallbackValuesModelProperty> getOAuthCallbackValuesModelProperty(Class<?> clazz) {
    Map<Field, String> values = getAnnotatedFieldsStream(clazz, OAuthCallbackValue.class)
        .collect(toMap(identity(), field -> field.getAnnotation(OAuthCallbackValue.class).expression()));

    values.putAll(getAnnotatedFieldsStream(clazz, com.zernova.sdk.api.annotation.connectivity.oauth.OAuthCallbackValue.class)
        .collect(toMap(identity(), field -> field
            .getAnnotation(com.zernova.sdk.api.annotation.connectivity.oauth.OAuthCallbackValue.class).expression())));

    if (!values.isEmpty()) {
      return of(new OAuthCallbackValuesModelProperty(values));
    }
    return empty();
  }

  @Override
  public Optional<DeprecationModel> getDeprecationModel() {
    return JavaExtensionModelParserUtils.getDeprecationModel(element);
  }

  @Override
  public Optional<DisplayModel> getDisplayModel() {
    return JavaExtensionModelParserUtils.getDisplayModel(element, "connection provider", element.getName());
  }

  @Override
  public Optional<MinZernovaVersionParser> getResolvedMinMuleVersion() {
    return of(resolveConnectionProviderMinMuleVersion(element));
  }

  @Override
  public Set<String> getSemanticTerms() {
    Set<String> terms = new LinkedHashSet<>();
    terms.addAll(getConnectionTermsFromAnnotations(element::isAnnotatedWith));
    addCustomTerms(element, terms);

    return terms;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof JavaConnectionProviderModelParser) {
      return element.equals(((JavaConnectionProviderModelParser) o).element);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return hash(element);
  }

  private AuthorizationCodeGrantType getAuthorizationCodeGrantType(AnnotationValueFetcher<AuthorizationCode> authorizationCodeAnnotationValueFetcher) {
    return new AuthorizationCodeGrantType(
                                          authorizationCodeAnnotationValueFetcher
                                              .getStringValue(AuthorizationCode::accessTokenUrl),
                                          authorizationCodeAnnotationValueFetcher
                                              .getStringValue(AuthorizationCode::authorizationUrl),
                                          authorizationCodeAnnotationValueFetcher
                                              .getStringValue(AuthorizationCode::accessTokenExpr),
                                          authorizationCodeAnnotationValueFetcher
                                              .getStringValue(AuthorizationCode::expirationExpr),
                                          authorizationCodeAnnotationValueFetcher
                                              .getStringValue(AuthorizationCode::refreshTokenExpr),
                                          authorizationCodeAnnotationValueFetcher
                                              .getStringValue(AuthorizationCode::defaultScopes),
                                          authorizationCodeAnnotationValueFetcher
                                              .getEnumValue(AuthorizationCode::credentialsPlacement),
                                          authorizationCodeAnnotationValueFetcher
                                              .getBooleanValue(AuthorizationCode::includeRedirectUriInRefreshTokenRequest));
  }

  private AuthorizationCodeGrantType getAuthorizationCodeGrantTypeFromSdk(AnnotationValueFetcher<com.zernova.sdk.api.annotation.connectivity.oauth.AuthorizationCode> authorizationCodeAnnotationValueFetcher) {
    return new AuthorizationCodeGrantType(
                                          authorizationCodeAnnotationValueFetcher
                                              .getStringValue(com.zernova.sdk.api.annotation.connectivity.oauth.AuthorizationCode::accessTokenUrl),
                                          authorizationCodeAnnotationValueFetcher
                                              .getStringValue(com.zernova.sdk.api.annotation.connectivity.oauth.AuthorizationCode::authorizationUrl),
                                          authorizationCodeAnnotationValueFetcher
                                              .getStringValue(com.zernova.sdk.api.annotation.connectivity.oauth.AuthorizationCode::accessTokenExpr),
                                          authorizationCodeAnnotationValueFetcher
                                              .getStringValue(com.zernova.sdk.api.annotation.connectivity.oauth.AuthorizationCode::expirationExpr),
                                          authorizationCodeAnnotationValueFetcher
                                              .getStringValue(com.zernova.sdk.api.annotation.connectivity.oauth.AuthorizationCode::refreshTokenExpr),
                                          authorizationCodeAnnotationValueFetcher
                                              .getStringValue(com.zernova.sdk.api.annotation.connectivity.oauth.AuthorizationCode::defaultScopes),
                                          authorizationCodeAnnotationValueFetcher
                                              .getEnumValue(authorizationCode -> from(authorizationCode
                                                  .credentialsPlacement())),
                                          authorizationCodeAnnotationValueFetcher
                                              .getBooleanValue(com.zernova.sdk.api.annotation.connectivity.oauth.AuthorizationCode::includeRedirectUriInRefreshTokenRequest));
  }

  private ClientCredentialsGrantType getClientCredentialsGrantType(AnnotationValueFetcher<ClientCredentials> clientCredentialsAnnotationValueFetcher) {
    return new ClientCredentialsGrantType(clientCredentialsAnnotationValueFetcher
        .getStringValue(ClientCredentials::tokenUrl),
                                          clientCredentialsAnnotationValueFetcher
                                              .getStringValue(ClientCredentials::accessTokenExpr),
                                          clientCredentialsAnnotationValueFetcher
                                              .getStringValue(ClientCredentials::expirationExpr),
                                          clientCredentialsAnnotationValueFetcher
                                              .getStringValue(ClientCredentials::defaultScopes),
                                          clientCredentialsAnnotationValueFetcher
                                              .getEnumValue(ClientCredentials::credentialsPlacement));
  }

  private ClientCredentialsGrantType getClientCredentialsGrantTypeFromSdk(AnnotationValueFetcher<com.zernova.sdk.api.annotation.connectivity.oauth.ClientCredentials> clientCredentialsAnnotationValueFetcher) {
    return new ClientCredentialsGrantType(clientCredentialsAnnotationValueFetcher
        .getStringValue(com.zernova.sdk.api.annotation.connectivity.oauth.ClientCredentials::tokenUrl),
                                          clientCredentialsAnnotationValueFetcher
                                              .getStringValue(com.zernova.sdk.api.annotation.connectivity.oauth.ClientCredentials::accessTokenExpr),
                                          clientCredentialsAnnotationValueFetcher
                                              .getStringValue(com.zernova.sdk.api.annotation.connectivity.oauth.ClientCredentials::expirationExpr),
                                          clientCredentialsAnnotationValueFetcher
                                              .getStringValue(com.zernova.sdk.api.annotation.connectivity.oauth.ClientCredentials::defaultScopes),
                                          clientCredentialsAnnotationValueFetcher
                                              .getEnumValue(clientCredentials -> from(clientCredentials
                                                  .credentialsPlacement())));
  }

}
