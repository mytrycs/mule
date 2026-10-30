/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.source;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static com.zernova.test.allure.AllureConstants.SourcesFeature.SOURCES;
import static com.zernova.test.heisenberg.extension.DEARadioSource.MESSAGES_PER_POLL;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.test.heisenberg.extension.model.types.DEAOfficerAttributes;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import io.qameta.allure.Feature;

@Feature(SOURCES)
public class ListOfMessagesSourceTestCase extends AbstractExtensionFunctionalTestCase {

  private static AtomicReference<List<Message>> capturedPayload = new AtomicReference<>(null);
  private static Latch latch = new Latch();

  @Override
  protected String getConfigFile() {
    return "source/heisenberg-source-list-config.xml";
  }

  @Override
  protected void doTearDown() throws Exception {
    capturedPayload = null;
    latch = null;
    super.doTearDown();
  }

  @Test
  public void listenMessages() throws Exception {
    assertThat(latch.await(5, SECONDS), is(true));
    List<Message> payload = capturedPayload.get();
    assertThat(payload, is(notNullValue()));
    assertThat(payload, hasSize(MESSAGES_PER_POLL));

    for (Message message : payload) {
      assertThat(message.getPayload().getValue(), is(instanceOf(String.class)));
      assertThat(message.getAttributes().getValue(), is(instanceOf(DEAOfficerAttributes.class)));
    }
  }

  public static class ListProcessor extends AbstractComponent implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws ZernovaException {
      List<Message> payload = (List<Message>) event.getMessage().getPayload().getValue();
      if (capturedPayload.compareAndSet(null, payload)) {
        latch.release();
      }
      return event;
    }
  }
}
