/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.validation;

import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.Configurations;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.module.extension.internal.loader.validator.JavaConfigurationModelValidator;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.tck.testmodels.fruit.Apple;
import com.zernova.test.module.extension.internal.util.ExtensionsTestUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SmallTest
public class JavaConfigurationModelValidatorTestCase extends AbstractZernovaTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private ExtensionModelValidator validator = new JavaConfigurationModelValidator();

  @Test
  public void validConfigurationTypesForOperations() throws Exception {
    validate(ValidExtension.class);
  }

  @Test
  public void invalidConfigurationTypesForOperations() throws Exception {
    expectedException.expect(IllegalModelDefinitionException.class);
    expectedException.expectMessage("requires a configuration of type");
    validate(InvalidExtension.class);
  }

  @Test
  public void invalidConfigurationWithInvalidParameter() {
    expectedException.expect(IllegalModelDefinitionException.class);
    expectedException
        .expectMessage("Configuration 'configWithParameterWithNameNamedName' declares a parameter whose name is 'name', which is not allowed.");
    validate(ConfigNoNameExtension.class);
  }

  private void validate(Class<?> connectorClass) {
    ExtensionsTestUtils.validate(connectorClass, validator);
  }

  interface ConfigInterface {

  }


  @Extension(name = "invalidExtension")
  @Configurations({InvalidTestConfig.class})
  public static class InvalidExtension {

  }


  @Extension(name = "invalidExtension")
  @Configurations({ConfigWithNameParameter.class})
  public static class ConfigNoNameExtension {

  }


  @Extension(name = "validExtension")
  @Configurations({TestConfig.class, TestConfig2.class})
  public static class ValidExtension {

  }


  @Configuration(name = "config")
  @Operations(ValidTestOperations.class)
  public static class TestConfig implements ConfigInterface {

  }


  @Configuration(name = "config2")
  @Operations(ValidTestOperations.class)
  public static class TestConfig2 implements ConfigInterface {

  }


  @Configuration(name = "configWithParameterWithNameNamedName")
  public static class ConfigWithNameParameter implements ConfigInterface {

    @Parameter
    private String name;
  }

  @Operations(InvalidTestOperations.class)
  public static class InvalidTestConfig implements ConfigInterface {

  }



  public static class ValidTestOperations {

    public void foo(@Config ConfigInterface connection) {

    }

    public void bar(@Config ConfigInterface connection) {

    }
  }


  public static class InvalidTestOperations {

    public void foo(@Config ConfigInterface config) {

    }

    public void bar(@Config Apple config) {

    }
  }
}
