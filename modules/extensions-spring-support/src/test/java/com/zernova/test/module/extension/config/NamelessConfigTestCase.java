/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.config;

import com.zernova.runtime.extension.api.annotation.Configuration;
import com.zernova.runtime.extension.api.annotation.Configurations;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.module.extension.internal.loader.validator.JavaConfigurationModelValidator;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.test.module.extension.internal.util.ExtensionsTestUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NamelessConfigTestCase extends AbstractZernovaTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private ExtensionModelValidator validator = new JavaConfigurationModelValidator();

  @Test
  public void useDefaultConfigurationNameOnceAnnotated() throws Exception {
    validate(OneConfigAnnotatedExtension.class);
  }

  @Test
  public void useDefaultConfigurationNameTwiceAnnotated() throws Exception {
    expectedException.expect(IllegalModelDefinitionException.class);
    expectedException.expectMessage("contains 2 components 'config' which it's transformed DSL name is 'config'");
    validate(TwoConfigAnnotatedExtension.class);
  }

  @Test
  public void useDefaultConfigurationNameOnceNonAnnotated() throws Exception {
    validate(OneConfigNonAnnotatedExtension.class);
  }

  @Test
  public void useDefaultConfigurationNameTwiceNonAnnotated() throws Exception {
    expectedException.expect(IllegalModelDefinitionException.class);
    expectedException.expectMessage("contains 2 components 'config' which it's transformed DSL name is 'config'");
    validate(TwoConfigNonAnnotatedExtension.class);
  }

  @Test
  public void useDefaultConfigurationNameMixedAnnotation() throws Exception {
    expectedException.expect(IllegalModelDefinitionException.class);
    expectedException.expectMessage("contains 2 components 'config' which it's transformed DSL name is 'config'");
    validate(MixedConfigExtension.class);
  }

  private void validate(Class<?> connectorClass) {
    ExtensionsTestUtils.validate(connectorClass, validator);
  }

  @Extension(name = "once-annotated")
  @Configurations({AnnotatedNamelessConfig.class})
  public static class OneConfigAnnotatedExtension {
  }

  @Extension(name = "twice-annotated")
  @Configurations({AnnotatedNamelessConfig.class, AnotherAnnotatedNamelessConfig.class})
  public static class TwoConfigAnnotatedExtension {
  }

  @Extension(name = "once-non-annotated")
  @Configurations({NonAnnotatedNamelessConfig.class})
  public static class OneConfigNonAnnotatedExtension {
  }

  @Extension(name = "twice-non-annotated")
  @Configurations({NonAnnotatedNamelessConfig.class, AnotherNonAnnotatedNamelessConfig.class})
  public static class TwoConfigNonAnnotatedExtension {
  }

  @Extension(name = "twice-mixed-annotated")
  @Configurations({AnnotatedNamelessConfig.class, NonAnnotatedNamelessConfig.class})
  public static class MixedConfigExtension {
  }

  @Configuration
  public static class AnnotatedNamelessConfig {
  }

  @Configuration
  public static class AnotherAnnotatedNamelessConfig {
  }

  public static class NonAnnotatedNamelessConfig {
  }

  public static class AnotherNonAnnotatedNamelessConfig {
  }

}
