/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.internal.ast;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.core.api.config.ConfigurationException;

import java.util.Optional;
import java.util.Set;

/**
 * Allows for extracting an {@link ExtensionModel} which represents the {@link ArtifactAst} being parsed.
 *
 * @since 4.5.0
 */
public interface ZernovaSdkExtensionModelLoadingMediator {

  /**
   * @param ast         the artifact's AST
   * @param classLoader the artifact's classloader
   * @param extensions  the initial set of extensions the artifact depends on.
   * @return an {@link ExtensionModel} that represents the {@code ast}.
   * @throws ConfigurationException if the artifact couldn't be parsed.
   */
  Optional<ExtensionModel> loadExtensionModel(ArtifactAst ast, ClassLoader classLoader, Set<ExtensionModel> extensions)
      throws ConfigurationException;
}
