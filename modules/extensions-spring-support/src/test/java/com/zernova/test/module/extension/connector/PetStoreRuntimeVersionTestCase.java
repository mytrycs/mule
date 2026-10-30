/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.connector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static com.zernova.test.module.extension.connector.PetStoreSourceRetryPolicyProviderTestCase.POLL_DELAY_MILLIS;
import static com.zernova.test.module.extension.connector.PetStoreSourceRetryPolicyProviderTestCase.TIMEOUT_MILLIS;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.util.Reference;
import com.zernova.runtime.core.api.construct.Flow;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.tck.probe.JUnitLambdaProbe;
import com.zernova.tck.probe.PollingProber;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;
import com.zernova.test.petstore.extension.PetStoreClient;
import com.zernova.test.petstore.extension.PetStoreConnector;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class PetStoreRuntimeVersionTestCase extends AbstractExtensionFunctionalTestCase {

  private static final Reference<Message> messageHolder = new Reference<>();

  @Override
  protected String getConfigFile() {
    return "petstore-runtime-version.xml";
  }

  @Before
  public void setUp() {
    messageHolder.set(null);
  }

  @Test
  public void configWithRuntimeVersionField() throws Exception {
    PetStoreConnector config =
        (PetStoreConnector) flowRunner("configWithRuntimeVersion").run().getMessage().getPayload().getValue();

    assertRuntimeVersion(config.getRuntimeVersion());
  }

  @Test
  public void sourceWithRuntimeVersionField() throws Exception {
    startFlow("sourceWithRuntimeVersion");
    ZernovaVersion muleVersion = (ZernovaVersion) ((Map) listen().getAttributes().getValue()).get("muleRuntime");

    assertRuntimeVersion(muleVersion);
  }

  @Test
  public void connectionProviderWithRuntimeVersionField() throws Exception {
    PetStoreClient petStoreClient =
        (PetStoreClient) flowRunner("connectionProviderWithRuntimeVersion").run().getMessage().getPayload().getValue();

    assertRuntimeVersion(petStoreClient.getMuleVersion());
  }

  @Test
  public void operationClassWithRuntimeVersionField() throws Exception {
    ZernovaVersion muleVersion =
        (ZernovaVersion) flowRunner("operationClassWithRuntimeVersion").run().getMessage().getPayload().getValue();

    assertRuntimeVersion(muleVersion);
  }

  private void assertRuntimeVersion(ZernovaVersion muleVersion) {
    assertThat(muleVersion, notNullValue());
  }

  private void startFlow(String flowName) throws Exception {
    ((Flow) getFlowConstruct(flowName)).start();
  }

  public static class TestProcessor implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws ZernovaException {
      messageHolder.set(event.getMessage());
      return event;
    }
  }

  private Message listen() {
    PollingProber prober = new PollingProber(TIMEOUT_MILLIS, POLL_DELAY_MILLIS);
    prober.check(new JUnitLambdaProbe(() -> messageHolder.get() != null));

    return messageHolder.get();
  }
}
