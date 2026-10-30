/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.embedded.impl;

import com.zernova.runtime.module.embedded.api.ContainerInfo;
import com.zernova.runtime.module.embedded.api.controller.EmbeddedController;

/**
 * Controller class for the runtime. It spins up a new container instance using a temporary folder and dynamically loading the
 * container libraries.
 *
 * @since 4.6
 */
public class DefaultEmbeddedController extends CommonsEmbeddedController implements EmbeddedController {

  public DefaultEmbeddedController(ContainerInfo containerInfo) {
    super(containerInfo);
  }

}
