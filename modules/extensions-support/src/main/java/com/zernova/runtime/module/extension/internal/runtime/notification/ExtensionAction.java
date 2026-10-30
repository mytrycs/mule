/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.notification;

import com.zernova.runtime.api.notification.Notification.Action;

/**
 * {@link Action} produced by extensions.
 */
public class ExtensionAction implements Action {

  private final String namespace;
  private final String id;

  public ExtensionAction(String namespace, String id) {
    this.namespace = namespace;
    this.id = id;
  }

  @Override
  public String getNamespace() {
    return namespace;
  }

  @Override
  public String getIdentifier() {
    return id;
  }

}
