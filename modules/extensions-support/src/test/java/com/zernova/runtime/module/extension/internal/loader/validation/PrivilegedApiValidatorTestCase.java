/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.validation;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.module.extension.internal.loader.java.validation.PrivilegedApiValidator.NO_PRIVILEGED_ARTIFACTS_ERROR;
import static com.zernova.runtime.module.extension.internal.loader.java.validation.PrivilegedApiValidator.NO_PRIVILEGED_PACKAGES_ERROR;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.validate;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.module.extension.internal.loader.java.validation.PrivilegedApiValidator;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class PrivilegedApiValidatorTestCase extends AbstractZernovaTestCase {

  private static final String EXTENSION_NAME = "my extension";

  @Rule
  public ExpectedException expectedException = none();

  @Before
  public void before() {
    when(extensionModel.getName()).thenReturn(EXTENSION_NAME);
  }

  @Mock(answer = RETURNS_DEEP_STUBS, lenient = true)
  private ExtensionModel extensionModel;

  private PrivilegedApiValidator validator = new PrivilegedApiValidator();

  @Test
  public void valid() {
    when(extensionModel.getPrivilegedArtifacts()).thenReturn(singleton("com.zernova.test:foo"));
    when(extensionModel.getPrivilegedPackages()).thenReturn(singleton("org.foo"));
    validate(extensionModel, validator);
  }

  @Test
  public void noPrivilegedExportedPackages() {
    when(extensionModel.getPrivilegedPackages()).thenReturn(emptySet());
    when(extensionModel.getPrivilegedArtifacts()).thenReturn(singleton("com.zernova.test:foo"));

    expectedException.expect(IllegalModelDefinitionException.class);
    expectedException.expectMessage(containsString(NO_PRIVILEGED_PACKAGES_ERROR));

    validate(extensionModel, validator);
  }

  @Test
  public void noPrivilegedArtifacts() {
    when(extensionModel.getPrivilegedPackages()).thenReturn(singleton("org.foo"));
    when(extensionModel.getPrivilegedArtifacts()).thenReturn(emptySet());

    expectedException.expect(IllegalModelDefinitionException.class);
    expectedException.expectMessage(containsString(NO_PRIVILEGED_ARTIFACTS_ERROR));

    validate(extensionModel, validator);
  }

  @Test
  public void noPrivilegedApi() throws Exception {
    when(extensionModel.getPrivilegedPackages()).thenReturn(emptySet());
    when(extensionModel.getPrivilegedArtifacts()).thenReturn(emptySet());
    validate(extensionModel, validator);
  }
}
