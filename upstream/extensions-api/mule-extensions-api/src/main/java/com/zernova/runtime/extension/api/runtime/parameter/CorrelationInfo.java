/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.parameter;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.event.Event;
import com.zernova.runtime.api.message.ItemSequenceInfo;
import com.zernova.runtime.extension.api.annotation.execution.OnError;
import com.zernova.runtime.extension.api.annotation.execution.OnSuccess;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.util.Optional;

/**
 * Provides message correlation information. Operations and source callbacks (such as methods annotated with {@link OnSuccess}
 * or @{@link OnError}) can obtain an instance in order to obtain information regarding how the message being processed correlates
 * to its context.
 *
 * @since 1.1
 */
@MinZernovaVersion("4.1")
@NoImplement
public interface CorrelationInfo extends com.zernova.sdk.api.runtime.parameter.CorrelationInfo {
}
