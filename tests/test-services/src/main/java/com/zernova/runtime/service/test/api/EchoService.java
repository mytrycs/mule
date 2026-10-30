/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.service.test.api;

import com.zernova.runtime.api.service.Service;

/**
 * Service API used for testing purposes.
 */
public interface EchoService extends Service {

  /**
   * Echoes the input message.
   *
   * @param message text to be echoed.
   * @return the echoed message.
   */
  String echo(String message);
}
