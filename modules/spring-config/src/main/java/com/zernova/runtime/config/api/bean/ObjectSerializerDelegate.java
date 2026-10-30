/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.api.bean;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.serialization.ObjectSerializer;

/**
 * Wrapper for an {@link ObjectSerializer} which can be updated once the artifact configuration is loaded and applied.
 *
 * @since 4.10
 */
@NoImplement
public interface ObjectSerializerDelegate extends ObjectSerializer {

  /**
   * Changes the inner delegate of this serializer.
   *
   * @param delegate the new serializer to delegate to.
   */
  void setDelegate(ObjectSerializer delegate);

  /**
   * @return the serializer to delegate to.
   */
  ObjectSerializer getDelegate();

}
