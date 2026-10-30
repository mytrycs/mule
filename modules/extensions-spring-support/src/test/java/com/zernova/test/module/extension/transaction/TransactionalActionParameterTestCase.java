/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.transaction;

import static com.zernova.runtime.extension.api.tx.OperationTransactionalAction.JOIN_IF_POSSIBLE;
import static com.zernova.runtime.extension.api.tx.OperationTransactionalAction.NOT_SUPPORTED;
import static com.zernova.runtime.extension.api.tx.SourceTransactionalAction.ALWAYS_BEGIN;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.zernova.runtime.api.util.Reference;
import com.zernova.runtime.core.api.construct.Flow;
import com.zernova.runtime.extension.api.tx.SourceTransactionalAction;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;
import com.zernova.test.runner.RunnerDelegateTo;
import com.zernova.test.transactional.TransactionalSourceWithTXParameters;

import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized;

@RunnerDelegateTo(Parameterized.class)
public class TransactionalActionParameterTestCase extends AbstractExtensionFunctionalTestCase {

  private boolean isSdkApi;
  private final String configFile;

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> configs() {
    return asList(new Object[][] {
        {"tx/transactional-parameters-injection-config.xml", false},
        {"tx/transactional-xa-parameters-injection-config.xml", true}
    });
  }

  public TransactionalActionParameterTestCase(String configFile, boolean isSdkApi) {
    this.isSdkApi = isSdkApi;
    this.configFile = configFile;
  }

  @Override
  protected String getConfigFile() {
    return configFile;
  }

  @Test
  public void injectAlwaysBeginSourceTransactionalAction() throws Exception {
    Reference<com.zernova.sdk.api.tx.SourceTransactionalAction> sdkSourceTransactionalAction = new Reference<>();
    Reference<SourceTransactionalAction> sourceTransactionalAction = new Reference<>();

    if (isSdkApi) {
      com.zernova.test.transactionalxa.TransactionalSourceWithTXParameters.responseCallback =
          tx -> sdkSourceTransactionalAction.set((com.zernova.sdk.api.tx.SourceTransactionalAction) tx);
    } else {
      TransactionalSourceWithTXParameters.responseCallback =
          tx -> sourceTransactionalAction.set((SourceTransactionalAction) tx);
    }

    startFlow("alwaysBeginTxAction");

    if (isSdkApi) {
      assertThat(sdkSourceTransactionalAction.get(), is(com.zernova.sdk.api.tx.SourceTransactionalAction.ALWAYS_BEGIN));
    } else {
      assertThat(sourceTransactionalAction.get(), is(ALWAYS_BEGIN));
    }
  }

  @Test
  public void injectDefaultOperationTransactionalAction() throws Exception {
    Enum value = (Enum) flowRunner("injectInOperationDefaultValue").run()
        .getMessage().getPayload().getValue();
    assertThat(value.name(), is(JOIN_IF_POSSIBLE.name()));
  }

  @Test
  public void injectInOperationJoinNotSupported() throws Exception {
    Enum value = (Enum) flowRunner("injectInOperationJoinNotSupported").run()
        .getMessage().getPayload().getValue();
    assertThat(value.name(), is(NOT_SUPPORTED.name()));
  }

  private void startFlow(String flowName) throws Exception {
    ((Flow) getFlowConstruct(flowName)).start();
  }
}
