/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.el.dataweave;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Optional.of;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.el.DefaultExpressionLanguageFactoryService;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.weave.v2.el.provider.WeaveDefaultExpressionLanguageFactoryService;

import org.junit.Before;

public abstract class AbstractWeaveExpressionLanguageTestCase extends AbstractZernovaContextTestCase {

  protected DataWeaveExpressionLanguageAdaptor expressionLanguage;

  private DefaultExpressionLanguageFactoryService weaveExpressionExecutor;
  protected Registry registry = mock(Registry.class);

  @Before
  public void setUp() throws InitialisationException {
    weaveExpressionExecutor = new WeaveDefaultExpressionLanguageFactoryService(null);
    when(registry.lookupByType(DefaultExpressionLanguageFactoryService.class)).thenReturn(of(weaveExpressionExecutor));
    expressionLanguage =
        new DataWeaveExpressionLanguageAdaptor(muleContext, registry, muleContext.getConfiguration(),
                                               () -> defaultCharset(),
                                               weaveExpressionExecutor, getFeatureFlaggingService());
    expressionLanguage.initialise();
  }

}
