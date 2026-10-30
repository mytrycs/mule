/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.internal.singleapp;

import com.zernova.runtime.module.deployment.internal.DeploymentFileResolver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;

import static org.apache.commons.io.FileUtils.toFile;

/**
 * A {@link DeploymentFileResolver} for single app mode.
 */
public class SingleAppDeploymentFileResolver implements DeploymentFileResolver {

  @Override
  public File resolve(URI appArchiveUri) throws MalformedURLException {
    return toFile(appArchiveUri.toURL());
  }
}
