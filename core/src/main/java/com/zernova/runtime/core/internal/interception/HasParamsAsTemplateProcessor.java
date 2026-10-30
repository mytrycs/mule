/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.interception;

import com.zernova.runtime.core.api.el.ExpressionManager;

/**
 * Marker interface used by <b>Interception API</b> that indicates that the expression parameters of the implementing components
 * must be interpreted as templates.
 *
 * @see ExpressionManager#parseLogTemplate(String, com.zernova.runtime.core.api.event.CoreEvent,
 *      com.zernova.runtime.api.component.location.ComponentLocation, com.zernova.runtime.api.el.BindingContext)
 *
 * @since 4.5
 */
public interface HasParamsAsTemplateProcessor {

}
