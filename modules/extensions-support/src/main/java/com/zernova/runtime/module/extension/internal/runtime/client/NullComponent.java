/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.client;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;

/**
 * Null object pattern implementation for the {@link AbstractComponent} class
 *
 * @since 4.5.0
 */
public class NullComponent extends AbstractComponent implements Initialisable {

  public static final NullComponent NULL_COMPONENT = new NullComponent();

  @Override
  public void initialise() throws InitialisationException {

  }
}
