/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.api.classloader;

import com.zernova.api.annotation.NoImplement;

/**
 * This class has the constants of the mule extension plugin used to package artifacts.
 * <p>
 * During deployment with maven packaged artifacts, the mule extensions maven plugin coordinates are used to locate the plugin
 * configuration within the artifact pom to search for the declared additional dependencies that will be added to other plugins.
 *
 * @since 4.1
 */
@NoImplement
public interface ZernovaExtensionsMavenPlugin {

  String MULE_EXTENSIONS_PLUGIN_GROUP_ID = "com.zernova.runtime.plugins";
  String MULE_EXTENSIONS_PLUGIN_ARTIFACT_ID = "mule-extensions-maven-plugin";
}
