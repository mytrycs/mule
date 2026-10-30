/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.source;

import static com.zernova.tck.probe.PollingProber.check;
import static com.zernova.test.petstore.extension.PetStoreSimpleSourceWithSdkApi.ON_ERROR_CALL_COUNT;
import static com.zernova.test.petstore.extension.PetStoreSimpleSourceWithSdkApi.ON_SUCCESS_CALL_COUNT;
import static com.zernova.test.petstore.extension.PetStoreSimpleSourceWithSdkApi.ON_TERMINATE_CALL_COUNT;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class SourceExecutionSdkApiTestCase extends AbstractExtensionFunctionalTestCase {

  private static final int TIMEOUT = 5000;
  private static final int DELAY = 100;

  private static final List<CoreEvent> EVENTS = new LinkedList<>();

  @Override
  protected void doTearDown() throws Exception {
    EVENTS.clear();
  }

  @Override
  protected String getConfigFile() {
    return "source/source-notification-sdk-api-config.xml";
  }

  @Test
  public void onSuccessMethodCall() throws Exception {
    startFlow("onSuccessMethodCallFlow");
    check(TIMEOUT, DELAY, () -> ON_SUCCESS_CALL_COUNT > 0);
    check(TIMEOUT, DELAY, () -> ON_TERMINATE_CALL_COUNT > 0);
  }

  @Test
  public void onErrorMethodCall() throws Exception {
    startFlow("onErrorMethodCallFlow");
    check(TIMEOUT, DELAY, () -> ON_ERROR_CALL_COUNT > 0);
    check(TIMEOUT, DELAY, () -> ON_TERMINATE_CALL_COUNT > 0);
  }

  private void startFlow(String flowName) throws Exception {
    ((Startable) getFlowConstruct(flowName)).start();
  }


  public static class SourceProcessor implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws ZernovaException {
      synchronized (EVENTS) {
        EVENTS.add(event);
      }
      return event;
    }
  }

}
