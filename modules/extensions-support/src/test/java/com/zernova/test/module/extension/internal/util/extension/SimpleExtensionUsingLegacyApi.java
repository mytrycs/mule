/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.internal.util.extension;

import com.zernova.sdk.api.annotation.Export;
import com.zernova.sdk.api.annotation.Extension;
import com.zernova.sdk.api.annotation.Import;
import com.zernova.test.heisenberg.extension.model.KnockeableDoor;

@Extension(name = "SimpleExtension")
@Export(classes = {SimpleExportedType.class}, resources = "simpleResource.json")
@Import(type = KnockeableDoor.class)
public class SimpleExtensionUsingLegacyApi {
}
