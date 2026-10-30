/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.internal.processor;

import static com.zernova.runtime.config.api.ArtifactContextFactory.createArtifactContextFactory;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;

import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.config.api.ArtifactContextFactory;
import com.zernova.runtime.config.internal.ArtifactAstConfigurationBuilder;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.internal.context.DefaultZernovaContext;
import com.zernova.runtime.core.internal.context.NullDomainZernovaContextLifecycleStrategy;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactConfigurationProcessor;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContextConfiguration;
import com.zernova.runtime.deployment.model.internal.artifact.ImmutableArtifactContext;

/**
 * Base class providing a reusable template method that contains an {@link ArtifactAst} and delegates to
 * {@link ArtifactAstConfigurationBuilder} to create registry and populate the {@link ZernovaContext}.
 *
 * @since 4.5
 */
abstract class AbstractAstConfigurationProcessor implements ArtifactConfigurationProcessor {

  @Override
  public ArtifactContext createArtifactContext(ArtifactContextConfiguration artifactContextConfiguration)
      throws ConfigurationException {
    if (isEmpty(artifactContextConfiguration.getConfigResources())) {
      return contextForEmptyArtifact(artifactContextConfiguration);
    }

    ArtifactAst artifactAst = obtainArtifactAst(artifactContextConfiguration);

    ArtifactContextFactory configurationBuilder = createArtifactContextFactory(artifactAst,
                                                                               artifactContextConfiguration
                                                                                   .getArtifactProperties(),
                                                                               artifactContextConfiguration
                                                                                   .getArtifactType(),
                                                                               artifactContextConfiguration
                                                                                   .isEnableLazyInitialization(),
                                                                               artifactContextConfiguration
                                                                                   .isAddToolingObjectsToRegistry(),
                                                                               artifactContextConfiguration
                                                                                   .getServiceConfigurators(),
                                                                               artifactContextConfiguration
                                                                                   .getParentArtifactContext());

    configurationBuilder.configure(artifactContextConfiguration.getMuleContext());
    return configurationBuilder.createArtifactContext();
  }

  protected ArtifactContext contextForEmptyArtifact(ArtifactContextConfiguration artifactContextConfiguration) {
    ((DefaultZernovaContext) artifactContextConfiguration.getMuleContext())
        .setLifecycleStrategy(new NullDomainZernovaContextLifecycleStrategy());
    return new ImmutableArtifactContext(artifactContextConfiguration.getMuleContext());
  }

  protected abstract ArtifactAst obtainArtifactAst(ArtifactContextConfiguration artifactContextConfiguration)
      throws ConfigurationException;
}
