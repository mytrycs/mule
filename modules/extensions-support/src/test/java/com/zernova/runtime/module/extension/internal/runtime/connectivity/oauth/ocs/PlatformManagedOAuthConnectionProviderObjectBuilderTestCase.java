/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ocs;

import static com.zernova.runtime.extension.api.ocs.OCSConstants.OCS_API_VERSION;
import static com.zernova.runtime.extension.api.ocs.OCSConstants.OCS_CLIENT_ID;
import static com.zernova.runtime.extension.api.ocs.OCSConstants.OCS_CLIENT_SECRET;
import static com.zernova.runtime.extension.api.ocs.OCSConstants.OCS_ORG_ID;
import static com.zernova.runtime.extension.api.ocs.OCSConstants.OCS_PLATFORM_AUTH_URL;
import static com.zernova.runtime.extension.api.ocs.OCSConstants.OCS_SERVICE_URL;
import static com.zernova.runtime.module.extension.internal.ExtensionProperties.CONFIGURATION_MODEL_PROPERTY_NAME;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.retry.ReconnectionConfig;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthGrantType;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthGrantTypeVisitor;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthModelProperty;
import com.zernova.runtime.extension.api.connectivity.oauth.PlatformManagedOAuthGrantType;
import com.zernova.runtime.extension.api.runtime.connectivity.ConnectionProviderFactory;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.loader.java.property.ConnectionProviderFactoryModelProperty;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PlatformManagedOAuthConnectionProviderObjectBuilderTestCase {

  private PlatformManagedOAuthConnectionProviderObjectBuilder<Object> builder;

  @Mock
  private ResolverSet resolverSet;

  @Mock
  private ConnectionProviderModel providerModel;

  @Mock
  private PoolingProfile poolingProfile;

  @Mock
  private ReconnectionConfig reconnectionConfig;

  @Mock
  private PlatformManagedOAuthHandler platformManagedOAuthHandler;

  @Mock
  private PlatformManagedOAuthGrantType grantType;

  @Mock
  private ConfigurationProperties properties;

  @Mock
  private ExtensionModel extensionModel;

  @Mock
  private ExpressionManager expressionManager;

  @Mock
  private ZernovaContext muleContext;

  @Mock
  private ResolverSetResult resolverSetResult;

  @Mock
  private ValueResolvingContext valueResolvingContext;

  @Mock
  private ConnectionProviderFactory connectionProviderFactory;

  @Mock
  private TestConnectionProvider connectionProvider;

  private static final String CLIENT_ID = "client_id";
  private static final String SECRET_ID = "secret_id";
  private static final String ORG_ID = "org_id";
  private static final String SERVICE_URL = "service_url";
  private static final String PLATFORM_AUTH_URL = "http://localhost/accounts";

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    when(providerModel.getModelProperty(ConnectionProviderFactoryModelProperty.class))
        .thenReturn(Optional.of(new ConnectionProviderFactoryModelProperty(connectionProviderFactory)));
    when(connectionProviderFactory.newInstance()).thenReturn(connectionProvider);

    builder = new PlatformManagedOAuthConnectionProviderObjectBuilder<>(
                                                                        providerModel,
                                                                        resolverSet,
                                                                        poolingProfile,
                                                                        reconnectionConfig,
                                                                        grantType,
                                                                        platformManagedOAuthHandler,
                                                                        properties,
                                                                        extensionModel,
                                                                        expressionManager,
                                                                        muleContext);
  }

  @Test
  public void testBuild() throws Exception {
    builder = spy(builder);

    ConfigurationModel mockConfigModel = mock(ConfigurationModel.class);
    Pair delegateModel = mock(Pair.class);
    when(delegateModel.getFirst()).thenReturn(providerModel);
    when(delegateModel.getSecond()).thenReturn(grantType);
    doReturn(delegateModel).when(builder).getDelegateOAuthConnectionProviderModel(mockConfigModel);
    when(valueResolvingContext.getProperty(CONFIGURATION_MODEL_PROPERTY_NAME)).thenReturn(mockConfigModel);
    when(resolverSet.resolve(valueResolvingContext)).thenReturn(resolverSetResult);

    when(properties.resolveStringProperty(OCS_CLIENT_SECRET)).thenReturn(of(SECRET_ID));
    when(properties.resolveStringProperty(OCS_CLIENT_ID)).thenReturn(of(CLIENT_ID));
    when(properties.resolveStringProperty(OCS_ORG_ID)).thenReturn(of(ORG_ID));
    when(properties.resolveStringProperty(OCS_SERVICE_URL)).thenReturn(of(SERVICE_URL));
    when(properties.resolveStringProperty(OCS_PLATFORM_AUTH_URL)).thenReturn(of(PLATFORM_AUTH_URL));
    when(properties.resolveStringProperty(OCS_API_VERSION)).thenReturn(empty());

    Pair<ConnectionProvider<Object>, ResolverSetResult> result = builder.build(valueResolvingContext);

    assertThat(result, is(notNullValue()));
    assertThat(result.getFirst(), is(notNullValue()));
    assertThat(result.getFirst(), isA(PlatformManagedOAuthConnectionProvider.class));
    assertThat(result.getSecond(), is(resolverSetResult));
  }

  @Test
  public void testGetDelegateOAuthConnectionProviderModel() {
    ConfigurationModel mockConfigModel = mock(ConfigurationModel.class);
    ConnectionProviderModel mockConnectionProviderModel = mock(ConnectionProviderModel.class);
    OAuthModelProperty mockOAuthModelProperty = mock(OAuthModelProperty.class);
    OAuthGrantType mockGrantType = mock(OAuthGrantType.class);
    when(mockOAuthModelProperty.getGrantTypes()).thenReturn(List.of(mockGrantType));
    when(mockConnectionProviderModel.getModelProperty(OAuthModelProperty.class)).thenReturn(
                                                                                            Optional.of(mockOAuthModelProperty));
    when(mockConfigModel.getConnectionProviders()).thenReturn(List.of(mockConnectionProviderModel));

    builder.getDelegateOAuthConnectionProviderModel(mockConfigModel);

    verify(mockGrantType, times(1)).accept(any(OAuthGrantTypeVisitor.class));
  }

  class TestConnectionProvider implements ConnectionProvider {

    @Override
    public Object connect() throws ConnectionException {
      return null;
    }

    @Override
    public void disconnect(Object connection) {

    }

    @Override
    public ConnectionValidationResult validate(Object connection) {
      return null;
    }
  }
}
