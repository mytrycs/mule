/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.provider;

import static java.util.ServiceLoader.load;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.meta.model.ExtensionModel;

import java.util.Objects;
import java.util.Set;

/**
 * Provider of an {@link ExtensionModel} of a Mule Runtime Component.
 *
 * @since 1.7
 */
@NoImplement
public interface RuntimeExtensionModelProvider {

  /**
   * @return the {@link ExtensionModel} corresponding to the Runtime Component.
   */
  ExtensionModel createExtensionModel();

}
