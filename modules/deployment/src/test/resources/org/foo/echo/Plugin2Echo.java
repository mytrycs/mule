/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.echo;

import com.zernova.runtime.module.deployment.test.api.EventCallback;

import org.bar.BarUtils;

public class Plugin2Echo implements EventCallback {

  @Override
  public void eventReceived(String payload) throws Exception {
    new BarUtils().doStuff();
  }
}
