/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.validation;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static com.zernova.runtime.api.connection.ConnectionValidationResult.success;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.api.connection.PoolingConnectionProvider;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.Configurations;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.connectivity.TransactionalConnection;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.module.extension.internal.loader.validator.JavaConnectionProviderModelValidator;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.tck.testmodels.fruit.Apple;
import com.zernova.test.module.extension.internal.util.ExtensionsTestUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SmallTest
public class JavaConnectionProviderModelValidatorTestCase extends AbstractZernovaTestCase {

  @Rule
  public ExpectedException expectedException = none();

  private ExtensionModelValidator validator = new JavaConnectionProviderModelValidator();

  @Test
  public void validModel() throws Exception {
    validate(ValidTestConnector.class);
  }

  @Test(expected = IllegalModelDefinitionException.class)
  public void invalidConnectedOperation() {
    validate(InvalidConfigConnectionProviderTestConnector.class);
  }

  @Test
  public void invalidConnectionTypeProviderTestConnector() {
    expectedException.expect(IllegalModelDefinitionException.class);
    expectedException.expectMessage(containsString("requires a connection of type"));
    validate(InvalidConnectionTypeProviderTestConnector.class);
  }

  @Test
  public void validTransactionalProvider() {
    validate(ValidTransactionalProviderConnector.class);
  }

  private void validate(Class<?> connectorClass) {
    ExtensionsTestUtils.validate(connectorClass, validator);
  }

  interface Config {

  }

  @Extension(name = "validatorTest")
  @Configurations({TestConfig.class, TestConfig2.class})
  @ConnectionProviders({TestConnectionProvider.class, TestConnectionProvider2.class})
  public static class ValidTestConnector {

  }

  public static class ValidTestOperations {

    public void foo(@Connection ValidatorTestConnection connection) {

    }

    public void bar(@Connection ValidatorTestConnection connection) {

    }
  }

  @Extension(name = "validatorTest")
  @Configurations({TestConfig.class, TestConfig2.class})
  @Operations({ValidTestOperations.class, InvalidConnectionOperation.class})
  public static class InvalidConnectedOperationTestConnector {

  }

  public static class InvalidConnectionOperation {

    public void invalid(@Connection Apple apple) {}
  }

  @Extension(name = "validatorTest")
  @Configurations({TestConfig.class, TestConfig2.class})
  @ConnectionProviders({TestConnectionProvider.class, TestConnectionProvider2.class, InvalidConfigConnectionProvider.class})
  public static class InvalidConfigConnectionProviderTestConnector {

  }

  @Extension(name = "validTransactionalProvider")
  @ConnectionProviders({TestConnectionProvider.class, TestConnectionProvider2.class, ValidTransactionalProvider.class})
  public static class ValidTransactionalProviderConnector {

  }

  @Extension(name = "validatorTest")
  @Configurations({TestConfig.class, TestConfig2.class})
  @ConnectionProviders({TestConnectionProvider.class, TestConnectionProvider2.class, InvalidTypeConnectionProvider.class})
  public static class InvalidConnectionTypeProviderTestConnector {

  }

  @Configuration(name = "config")
  @Operations(ValidTestOperations.class)
  public static class TestConfig implements Config {

  }

  @Configuration(name = "config2")
  @Operations(ValidTestOperations.class)
  public static class TestConfig2 implements Config {

  }

  @Alias("provider1")
  public static class TestConnectionProvider implements ConnectionProvider<ValidatorTestConnection> {

    @Override
    public ValidatorTestConnection connect() throws ConnectionException {
      return new ValidatorTestConnection();
    }

    @Override
    public void disconnect(ValidatorTestConnection connection) {

    }

    @Override
    public ConnectionValidationResult validate(ValidatorTestConnection validatorTestConnection) {
      return success();
    }
  }

  @Alias("provider2")
  public static class TestConnectionProvider2 extends TestConnectionProvider {

  }

  @Alias("invalidConfig")
  public static class InvalidConfigConnectionProvider implements ConnectionProvider<Apple> {

    @Override
    public Apple connect() throws ConnectionException {
      return new Apple();
    }

    @Override
    public void disconnect(Apple connection) {

    }

    @Override
    public ConnectionValidationResult validate(Apple validatorTestConnection) {
      return success();
    }
  }

  @Alias("invalidConnection")
  public static class InvalidTypeConnectionProvider implements ConnectionProvider<Apple> {

    @Override
    public Apple connect() throws ConnectionException {
      return new Apple();
    }

    @Override
    public void disconnect(Apple connection) {

    }

    @Override
    public ConnectionValidationResult validate(Apple apple) {
      return success();
    }
  }

  public static class ValidTransactionalProvider implements PoolingConnectionProvider<TransactionalConnection> {

    @Override
    public TransactionalConnection connect() throws ConnectionException {
      return mock(TransactionalConnection.class);
    }

    @Override
    public void disconnect(TransactionalConnection connection) {

    }

    @Override
    public ConnectionValidationResult validate(TransactionalConnection connection) {
      return success();
    }
  }

  public static class ValidatorTestConnection {

  }
}
