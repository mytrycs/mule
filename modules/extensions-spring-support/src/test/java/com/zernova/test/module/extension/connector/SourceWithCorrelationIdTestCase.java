/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.connector;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.tck.probe.PollingProber;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import org.junit.Before;
import org.junit.Test;

public class SourceWithCorrelationIdTestCase extends AbstractExtensionFunctionalTestCase {

  private static volatile String correlationId;

  @Override
  protected String getConfigFile() {
    return "source-with-correlation-id-config.xml";
  }

  @Override
  protected void doTearDown() throws Exception {
    correlationId = null;
  }

  @Test
  public void captureCorrelationId() throws Exception {
    PollingProber.check(5000, 100, () -> "Primate".equals(correlationId));
  }

  public static class TestProcessor implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws ZernovaException {
      correlationId = event.getCorrelationId();
      return event;
    }
  }

}
