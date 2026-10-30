/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.api.loader.java.type;

import com.zernova.api.annotation.NoImplement;

/**
 * A generic contract for any kind of component that could describer who is their owner
 * <p>
 * This is for informative usage
 *
 * @since 4.0
 */
@NoImplement
interface WithOwner {

  String getOwnerDescription();
}
