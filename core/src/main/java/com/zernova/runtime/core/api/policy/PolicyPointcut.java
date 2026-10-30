/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.policy;

/**
 * Provides a way to select which policies must be applied based on a given request.
 *
 * @since 4.0
 *
 * @deprecated Use {@link com.zernova.runtime.policy.api.PolicyPointcut} directly.
 */
@Deprecated
public interface PolicyPointcut extends com.zernova.runtime.policy.api.PolicyPointcut {

}
