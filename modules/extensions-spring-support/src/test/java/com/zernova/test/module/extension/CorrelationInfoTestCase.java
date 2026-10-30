/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static com.zernova.runtime.api.message.ItemSequenceInfo.of;
import static com.zernova.tck.junit4.matcher.IsEmptyOptional.empty;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.extension.api.runtime.parameter.CorrelationInfo;
import com.zernova.test.runner.RunnerDelegateTo;

import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized;

@RunnerDelegateTo(Parameterized.class)
public class CorrelationInfoTestCase extends AbstractExtensionFunctionalTestCase {

  @Parameterized.Parameter(0)
  public String parameterizationName;

  @Parameterized.Parameter(1)
  public String configName;

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        {"Using Extensions API", "correlation-info-config.xml"},
        {"Using SDK API", "sdk-correlation-info-config.xml"},
    });
  }

  @Override
  protected String getConfigFile() {
    return configName;
  }

  @Test
  public void defaultCorrelationInfo() throws Exception {
    final CoreEvent event = flowRunner("correlate").run();
    CorrelationInfo correlationInfo = (CorrelationInfo) event.getMessage().getPayload().getValue();
    assertThat(correlationInfo.getEventId(), is(event.getContext().getId()));
    assertThat(correlationInfo.isOutboundCorrelationEnabled(), is(true));
    assertThat(correlationInfo.getCorrelationId(), is(correlationInfo.getCorrelationId()));
    assertThat(correlationInfo.getItemSequenceInfo(), is(empty()));
  }

  @Test
  public void customCorrelationId() throws Exception {
    final String correlationId = "correlateThis";
    final CoreEvent event =
        flowRunner("correlate").withSourceCorrelationId(correlationId).withItemSequenceInfo(of(43, 100)).run();
    CorrelationInfo correlationInfo = (CorrelationInfo) event.getMessage().getPayload().getValue();
    assertThat(correlationInfo.getEventId(), is(event.getContext().getId()));
    assertThat(correlationInfo.isOutboundCorrelationEnabled(), is(true));
    assertThat(correlationInfo.getCorrelationId(), is(correlationId));
    assertThat(correlationInfo.getItemSequenceInfo().get().getPosition(), is(43));
    assertThat(correlationInfo.getItemSequenceInfo().get().getSequenceSize().getAsInt(), is(100));
  }


}
