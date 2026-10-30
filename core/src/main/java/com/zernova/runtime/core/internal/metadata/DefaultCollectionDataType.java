/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.metadata;

/**
 * This is kept for backwards compatibility with persistent serialized data.
 *
 * @since 3.0
 */
public class DefaultCollectionDataType extends com.zernova.runtime.core.privileged.metadata.DefaultCollectionDataType {

  private static final long serialVersionUID = 3600944898597616006L;

  DefaultCollectionDataType() {
    super(null, null, null, false);
  }

}
