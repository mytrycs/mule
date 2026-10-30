/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.functional;


import static com.zernova.tck.probe.PollingProber.check;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.zernova.functional.junit4.ZernovaArtifactFunctionalTestCase;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.test.functional.ByteArrayToInputStreamSdkTestCase.EventRecorder;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import io.qameta.allure.Issue;

public class ByteArrayToInputStreamSdkTestCase extends ZernovaArtifactFunctionalTestCase {

  private static final int POLL_TIMEOUT_MILLIS = 5000;
  private static final int POLL_DELAY_MILLIS = 1000;
  private static final int EXPECTED_EVENT_COUNT = 1;

  @Override
  protected String getConfigFile() {
    return "byte-array-to-input-stream-config.xml";
  }

  @Test
  public void byteArrayToInputStreamTransformationIsSuccessful() {
    check(POLL_TIMEOUT_MILLIS, POLL_DELAY_MILLIS, () -> EventRecorder.countCapturedEvents() == EXPECTED_EVENT_COUNT);
  }

  @Test
  @Issue("MULE-19279")
  public void byteArrayToInputStreamTransformationIsSuccessfulWithUTF16() throws Exception {
    // Test with UTF-16 character
    String testMessage = "Ź";
    CoreEvent event = flowRunner("transformer").withPayload(testMessage.getBytes("UTF-16")).run();
    TypedValue<String> value = event.getMessage().getPayload();
    assertThat(value.getValue(), is(testMessage));
  }

  public static class EventRecorder implements Processor, Startable {

    private static List<CoreEvent> capturedEvents = new LinkedList();

    public EventRecorder() {}

    @Override
    public CoreEvent process(CoreEvent event) throws ZernovaException {
      capturedEvents.add(event);
      return event;
    }

    @Override
    public void start() throws ZernovaException {
      capturedEvents.clear();
    }

    public static int countCapturedEvents() {
      return capturedEvents.size();
    }
  }
}
