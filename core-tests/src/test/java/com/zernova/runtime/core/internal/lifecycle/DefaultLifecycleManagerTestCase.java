/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.lifecycle;

import static com.zernova.test.allure.AllureConstants.ErrorHandlingFeature.ERROR_HANDLING;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.zernova.runtime.api.exception.DefaultZernovaException;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.lifecycle.LifecycleException;
import com.zernova.runtime.core.api.lifecycle.LifecycleCallback;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Feature(ERROR_HANDLING)
@Issue("W-11090837")
public class DefaultLifecycleManagerTestCase extends AbstractZernovaTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testDefaultLifecycleManagerTestCaseWrapsDefaultMuleExceptionIntoLifecycleException() throws ZernovaException {
    Lifecycle configurationProviderToolingAdapter = mock(Lifecycle.class);
    LifecycleCallback lifecycleCallback = mock(LifecycleCallback.class);
    doThrow(new DefaultZernovaException("Test exception")).when(lifecycleCallback).onTransition(any(), any());

    DefaultLifecycleManager defaultLifecycleManager =
        new DefaultLifecycleManager("com.zernova.runtime.module.extension.internal.runtime.config.ConfigurationProviderToolingAdapter-lisConfig",
                                    configurationProviderToolingAdapter);
    DefaultLifecycleManager defaultLifecycleManagerSpy = spy(defaultLifecycleManager);
    doNothing().when(defaultLifecycleManagerSpy).checkPhase(any());

    expectedException.expect(LifecycleException.class);
    defaultLifecycleManagerSpy.fireStartPhase(lifecycleCallback);
  }
}
