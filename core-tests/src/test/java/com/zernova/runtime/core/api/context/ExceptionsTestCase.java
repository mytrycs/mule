/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.i18n.I18nMessageFactory;
import com.zernova.runtime.api.exception.DefaultZernovaException;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.internal.routing.result.RoutingException;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.Test;

public class ExceptionsTestCase extends AbstractZernovaTestCase {

  @Test
  public void testExceptionChaining() {
    String rootMsg = "Root Test Exception Message";
    String msg = "Test Exception Message";

    Exception e = new ZernovaContextException(I18nMessageFactory.createStaticMessage(msg),
                                           new DefaultZernovaException(I18nMessageFactory.createStaticMessage(rootMsg)));

    assertEquals(rootMsg, e.getCause().getMessage());
    assertEquals(msg, e.getMessage());
    assertEquals(e.getClass().getName() + ": " + msg, e.toString());
  }

  @Test
  public final void testRoutingExceptionNullMessageValidProcessor() throws ZernovaException {
    Processor processor = mock(Processor.class);
    RoutingException rex = new RoutingException(processor);
    assertSame(processor, rex.getRoute());
  }

}
