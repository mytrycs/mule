/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.extension.test.extension.reconnection;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;

import com.zernova.runtime.extension.api.annotation.Configurations;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.sdk.api.annotation.JavaVersionSupport;


/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations and
 * sources are going to be declared.
 */
@Extension(name = "reconnection")
@JavaVersionSupport({JAVA_21, JAVA_17})
@Configurations({ReconnectionConfiguration.class, SynchronizableConfiguration.class, FailingConnectionConfiguration.class})
public class ReconnectionExtension {

}
