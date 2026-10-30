/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.client.source;

import com.zernova.api.annotation.Experimental;
import com.zernova.runtime.extension.api.client.params.Parameterizer;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * Parameterizes a success/error callback from a message source
 * <p>
 * <b>NOTE:</b> Experimental feature. Backwards compatibility not guaranteed.
 *
 * @since 1.5.0
 */
@Experimental
@MinZernovaVersion("4.5.0")
public interface SourceCallbackParameterizer extends Parameterizer<SourceCallbackParameterizer> {

}
