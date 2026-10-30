/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.api.deployable;

import com.zernova.api.annotation.NoImplement;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Represents the basic structure of a Mule project in the filesystem.
 *
 * @since 4.8
 */
@NoImplement
public interface ZernovaProjectStructure {

  /**
   * @return the root folder of the project
   */
  Path getProjectFolder();

  /**
   * @return the path of the directory containing the java sources of the project
   */
  Path getJavaDirectory();

  /**
   * @return the path of the directory containing the mule sources of the project
   */
  Path getMuleResourcesDirectory();

  /**
   * @return the paths of the directories containing the java sources of the project
   */
  Collection<Path> getResourcesDirectories();

}
