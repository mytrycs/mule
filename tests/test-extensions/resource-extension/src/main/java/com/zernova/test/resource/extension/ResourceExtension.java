/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.resource.extension;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;

import com.zernova.runtime.extension.api.annotation.Export;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.dsl.xml.Xml;
import com.zernova.sdk.api.annotation.JavaVersionSupport;

/**
 * This extension exposes operations to validate resource access from a plugin and service, but also exposes a resource to
 * validate exported ones can in fact be accessed.
 */
@Extension(name = "ResourceExtension")
@JavaVersionSupport({JAVA_21, JAVA_17})
@Xml(prefix = "rs")
@Operations(ResourceOps.class)
@Export(resources = {"stuff.json"})
public class ResourceExtension {

}
