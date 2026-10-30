/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.config;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.mockConfigurationInstance;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.source.SourceCallbackModel;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.module.extension.api.runtime.resolver.ConnectionProviderValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.internal.loader.java.property.ConnectivityModelProperty;
import com.zernova.runtime.module.extension.internal.runtime.execution.ConfigurationObjectBuilderTestCase;
import com.zernova.runtime.module.extension.internal.runtime.execution.ConfigurationObjectBuilderTestCase.TestConfig;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.tck.testmodels.fruit.Banana;
import com.zernova.tck.testmodels.fruit.Kiwi;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationInstanceFactoryTestCase extends AbstractZernovaTestCase {

  private static final String CONFIG_NAME = "config";
  private static final String ENCODING = "UTF-8";

  @Mock(answer = RETURNS_DEEP_STUBS, lenient = true)
  private ConfigurationModel configurationModel;

  @Mock(lenient = true)
  private OperationModel operationModel;

  @Mock(lenient = true)
  private ExtensionModel extensionModel;

  @Mock(lenient = true)
  private SourceModel sourceModel;

  @Mock(lenient = true)
  private SourceCallbackModel sourceCallbackModel;

  @Mock(lenient = true)
  private ComponentModel componentModel;

  @Mock(answer = RETURNS_DEEP_STUBS, lenient = true)
  private CoreEvent event;

  @Mock(lenient = true)
  private ConnectionProviderValueResolver<Object> connectionProviderValueResolver;

  @Mock(answer = RETURNS_DEEP_STUBS, lenient = true)
  private ExpressionManager expressionManager;

  @Mock(answer = RETURNS_DEEP_STUBS, lenient = true)
  private ZernovaContext muleContext;

  private ResolverSet resolverSet;
  private ConfigurationInstanceFactory<TestConfig> factory;

  @Before
  public void before() throws Exception {
    mockConfigurationInstance(configurationModel, new TestConfig());
    when(configurationModel.getOperationModels()).thenReturn(ImmutableList.of());
    when(configurationModel.getSourceModels()).thenReturn(ImmutableList.of());
    when(extensionModel.getOperationModels()).thenReturn(asList(operationModel));
    when(extensionModel.getSourceModels()).thenReturn(asList(sourceModel));
    when(operationModel.getModelProperty(ConnectivityModelProperty.class))
        .thenReturn(of(new ConnectivityModelProperty(Banana.class)));
    when(sourceModel.getModelProperty(ConnectivityModelProperty.class))
        .thenReturn(of(new ConnectivityModelProperty(Banana.class)));
    when(sourceModel.getErrorCallback()).thenReturn(of(sourceCallbackModel));
    when(sourceModel.getSuccessCallback()).thenReturn(of(sourceCallbackModel));
    when(muleContext.getConfiguration().getDefaultEncoding()).thenReturn(ENCODING);

    resolverSet = ConfigurationObjectBuilderTestCase.createResolverSet();
    factory = new ConfigurationInstanceFactory<>(extensionModel, configurationModel, resolverSet, expressionManager, muleContext);
  }

  @Test
  public void createFromEvent() throws Exception {
    ConfigurationInstance configurationInstance =
        factory.createConfiguration(CONFIG_NAME, event, connectionProviderValueResolver);

    assertConfiguration(configurationInstance);
  }

  @Test
  public void createFromResolverSetResult() throws Exception {
    ResolverSetResult result = ResolverSetResult.newBuilder().build();
    ConfigurationInstance configurationInstance = factory.createConfiguration(CONFIG_NAME, result, event, empty());

    assertConfiguration(configurationInstance);
    assertThat(configurationInstance.getConnectionProvider().isPresent(), is(false));
  }

  private void assertConfiguration(ConfigurationInstance configurationInstance) {
    assertThat(configurationInstance, is(notNullValue()));
    assertThat(configurationInstance.getName(), is(CONFIG_NAME));
    assertThat(configurationInstance.getModel(), is(sameInstance(configurationModel)));
    assertThat(configurationInstance.getValue(), is(instanceOf(TestConfig.class)));
  }

  public static class InvalidConfigTestConnectionProvider implements ConnectionProvider<Banana> {

    @Override
    public Banana connect() throws ConnectionException {
      return new Banana();
    }

    @Override
    public void disconnect(Banana banana) {

    }

    @Override
    public ConnectionValidationResult validate(Banana banana) {
      return ConnectionValidationResult.success();
    }
  }


  public static class InvalidConnectionTypeProvider implements ConnectionProvider<Kiwi> {

    @Override
    public Kiwi connect() throws ConnectionException {
      return new Kiwi();
    }

    @Override
    public void disconnect(Kiwi kiwi) {

    }

    @Override
    public ConnectionValidationResult validate(Kiwi kiwi) {
      return ConnectionValidationResult.success();
    }
  }
}
