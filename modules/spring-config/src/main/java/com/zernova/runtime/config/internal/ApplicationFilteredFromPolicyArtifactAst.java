/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.ENABLE_POLICY_ISOLATION;
import static com.zernova.runtime.ast.api.ArtifactType.APPLICATION;
import static com.zernova.runtime.core.api.error.Errors.CORE_NAMESPACE_NAME;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Optional.empty;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ArtifactType;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.ImportedResource;
import com.zernova.runtime.ast.api.util.AstTraversalDirection;
import com.zernova.runtime.ast.api.util.BaseArtifactAst;
import com.zernova.runtime.core.internal.exception.FilteredErrorTypeRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class ApplicationFilteredFromPolicyArtifactAst extends BaseArtifactAst {

  private final ArtifactAst parentArtifactAst;
  private final FeatureFlaggingService featureFlaggingService;

  ApplicationFilteredFromPolicyArtifactAst(ArtifactAst parentArtifactAst,
                                           FeatureFlaggingService featureFlaggingService) {
    this.parentArtifactAst = parentArtifactAst;
    this.featureFlaggingService = featureFlaggingService;
  }

  @Override
  public String getArtifactName() {
    return parentArtifactAst.getArtifactName();
  }

  @Override
  public ArtifactType getArtifactType() {
    return APPLICATION;
  }

  @Override
  public Set<ExtensionModel> dependencies() {
    if (featureFlaggingService.isEnabled(ENABLE_POLICY_ISOLATION)) {
      return emptySet();
    } else {
      return parentArtifactAst.dependencies();
    }
  }

  @Override
  public Optional<ArtifactAst> getParent() {
    if (featureFlaggingService.isEnabled(ENABLE_POLICY_ISOLATION)) {
      return empty();
    } else {
      return parentArtifactAst.getParent();
    }
  }

  @Override
  public Stream<ComponentAst> recursiveStream(AstTraversalDirection direction) {
    if (featureFlaggingService.isEnabled(ENABLE_POLICY_ISOLATION)) {
      return Stream.empty();
    } else {
      return parentArtifactAst.recursiveStream(direction);
    }
  }

  @Override
  public List<ComponentAst> topLevelComponents() {
    if (featureFlaggingService.isEnabled(ENABLE_POLICY_ISOLATION)) {
      return emptyList();
    } else {
      return parentArtifactAst.topLevelComponents();
    }
  }

  @Override
  public void updatePropertiesResolver(UnaryOperator<String> newPropertiesResolver) {
    parentArtifactAst.updatePropertiesResolver(newPropertiesResolver);
  }

  @Override
  public ErrorTypeRepository getErrorTypeRepository() {
    // Since there is already a workaround to allow polices to use http connector without declaring the dependency
    // and relying on it provided by the app, this case has to be accounted for here when handling error codes as
    // well.
    return new FilteredErrorTypeRepository(parentArtifactAst.getErrorTypeRepository(),
                                           new HashSet<>(asList("HTTP", CORE_NAMESPACE_NAME)));
  }

  @Override
  public Collection<ImportedResource> getImportedResources() {
    if (featureFlaggingService.isEnabled(ENABLE_POLICY_ISOLATION)) {
      return emptySet();
    } else {
      return parentArtifactAst.getImportedResources();
    }
  }

  public static ArtifactAst applicationFilteredFromPolicyArtifactAst(ArtifactAst parentArtifactAst,
                                                                     FeatureFlaggingService featureFlaggingService) {
    return new ApplicationFilteredFromPolicyArtifactAst(parentArtifactAst, featureFlaggingService);
  }

}
