/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.nb;

import static com.zernova.test.module.extension.internal.util.extension.privileged.PrivilegedNonBlockingComponentExecutor.OUTPUT;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.zernova.functional.junit4.ExtensionFunctionalTestCase;
import com.zernova.test.module.extension.internal.util.extension.privileged.PrivilegedExtension;

import org.junit.Test;

public class NonBlockingPrivilegedOperationsTestCase extends ExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "privileged-non-blocking-config.xml";
  }

  @Override
  protected Class<?>[] getAnnotatedExtensionClasses() {
    return new Class<?>[] {PrivilegedExtension.class};
  }

  @Override
  protected void doSetUp() throws Exception {
    muleContext.getInjector().inject(this);
  }

  @Test
  public void privilegedNonBlockingOperation() throws Exception {
    String value = flowRunner("privilegedNonBlocking").run().getMessage().getPayload().getValue().toString();
    assertThat(value, equalTo(OUTPUT));
  }
}
