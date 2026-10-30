/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.boot.api;

/**
 * Decouples the creation of the {@link ZernovaContainer} from the module requiring it.
 *
 * @since 4.6
 */
public interface ZernovaContainerProvider {

  /**
   * Creates an instance of a {@link ZernovaContainer}.
   *
   * @return a newly created {@link ZernovaContainer}.
   * @throws Exception
   */
  ZernovaContainer provide() throws Exception;

}
