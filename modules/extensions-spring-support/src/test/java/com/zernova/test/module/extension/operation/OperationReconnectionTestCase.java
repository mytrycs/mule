/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.operation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.getConfigurationInstanceFromRegistry;

import com.zernova.test.marvel.ironman.IronMan;
import com.zernova.test.marvel.model.Villain;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import org.junit.Test;

public class OperationReconnectionTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "operation-reconnection-config.xml";
  }

  @Test
  public void operationReconnection() throws Exception {
    final Villain villain = new Villain();
    flowRunner("operationReconnection").withPayload(villain).run();
    assertThat(villain.isAlive(), is(false));
    IronMan stark = getIronMan();
    assertThat(stark.getMissilesFired(), is(2));
  }

  @Test
  public void defaultReconnection() throws Exception {
    final Villain villain = new Villain();

    flowRunner("defaultReconnection").withPayload(villain).runExpectingException();

    assertThat(villain.isAlive(), is(true));
    IronMan stark = getIronMan();
    assertThat(stark.getMissilesFired(), is(1));
  }

  private IronMan getIronMan() throws Exception {
    return (IronMan) getConfigurationInstanceFromRegistry("ironMan", testEvent(), muleContext).getValue();
  }
}
