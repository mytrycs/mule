/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.transactionalxa;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;

import com.zernova.sdk.api.annotation.Configurations;
import com.zernova.sdk.api.annotation.Export;
import com.zernova.sdk.api.annotation.Extension;
import com.zernova.sdk.api.annotation.JavaVersionSupport;
import com.zernova.sdk.api.annotation.dsl.xml.Xml;

@Extension(name = "transactional-xa")
@JavaVersionSupport({JAVA_17, JAVA_21})
@Xml(prefix = "tx-xa")
@Configurations(TransactionalConfig.class)
@Export(classes = com.zernova.test.transactionalxa.TransactionalOperations.class)
public class TransactionalExtension {

}
