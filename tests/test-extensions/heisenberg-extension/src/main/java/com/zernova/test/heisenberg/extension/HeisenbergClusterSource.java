/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import static com.zernova.runtime.extension.api.annotation.source.SourceClusterSupport.DEFAULT_PRIMARY_NODE_ONLY;
import static com.zernova.runtime.extension.api.runtime.source.BackPressureMode.DROP;
import static com.zernova.runtime.extension.api.runtime.source.BackPressureMode.FAIL;

import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.Streaming;
import com.zernova.runtime.extension.api.annotation.notification.Fires;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.source.BackPressure;
import com.zernova.runtime.extension.api.annotation.source.ClusterSupport;
import com.zernova.runtime.extension.api.annotation.source.EmitsResponse;

@Alias("listen-payments-cluster")
@EmitsResponse
@Fires(SourceNotificationProvider.class)
@Streaming
@MediaType(TEXT_PLAIN)
@BackPressure(defaultMode = FAIL, supportedModes = {FAIL, DROP})
@ClusterSupport(DEFAULT_PRIMARY_NODE_ONLY)
public class HeisenbergClusterSource extends SdkHeisenbergSource {
}
