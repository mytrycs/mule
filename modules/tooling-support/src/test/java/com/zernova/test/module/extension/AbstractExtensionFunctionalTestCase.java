/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension;

import com.zernova.functional.junit4.ZernovaArtifactFunctionalTestCase;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.test.runner.ArtifactClassLoaderRunnerConfig;

import java.util.Optional;

@ArtifactClassLoaderRunnerConfig(applicationSharedRuntimeLibs = {"com.zernova.tests:mule-tests-model"})
public abstract class AbstractExtensionFunctionalTestCase extends ZernovaArtifactFunctionalTestCase {

  protected Optional<ExtensionModel> getExtensionModel(String name) {
    return muleContext.getExtensionManager().getExtension(name);
  }
}
