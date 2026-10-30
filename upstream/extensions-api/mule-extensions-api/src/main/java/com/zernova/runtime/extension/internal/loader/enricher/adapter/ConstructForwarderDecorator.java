/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.enricher.adapter;

import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.ComponentVisibility;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.declaration.fluent.*;
import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;
import com.zernova.runtime.api.meta.model.display.DisplayModel;
import com.zernova.runtime.api.meta.model.error.ErrorModel;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of {@link ConstructDeclaration} that proxies to the {@link OperationDeclaration} that it actually models, after
 * making the Routers be modeled as Operations instead of Constructs. This is only to be used to ensure Backward Compatibility
 * with preexistent privilege extensions that have routers, as Aggregators.
 *
 * @since 1.7
 */
public class ConstructForwarderDecorator extends ConstructDeclaration {

  private final OperationDeclaration delegate;

  public ConstructForwarderDecorator(OperationDeclaration delegate) {
    super(delegate.getName());
    this.delegate = delegate;
  }

  @Override
  public List<NestableElementDeclaration> getNestedComponents() {
    return delegate.getNestedComponents();
  }

  @Override
  public ConstructDeclaration addNestedComponent(NestableElementDeclaration nestedComponentDeclaration) {
    this.delegate.addNestedComponent(nestedComponentDeclaration);
    return this;
  }

  @Override
  public void addErrorModel(ErrorModel errorModel) {
    this.delegate.addErrorModel(errorModel);
  }

  @Override
  public Set<ErrorModel> getErrorModels() {
    return this.delegate.getErrorModels();
  }

  @Override
  public ComponentVisibility getVisibility() {
    return delegate.getVisibility();
  }

  @Override
  public void setVisibility(ComponentVisibility visibility) {
    this.delegate.setVisibility(visibility);
  }

  @Override
  public StereotypeModel getStereotype() {
    return this.delegate.getStereotype();
  }

  @Override
  public void withStereotype(StereotypeModel stereotype) {
    this.delegate.withStereotype(stereotype);
  }

  @Override
  public Optional<DeprecationModel> getDeprecation() {
    return this.delegate.getDeprecation();
  }

  @Override
  public void withDeprecation(DeprecationModel deprecation) {
    this.delegate.withDeprecation(deprecation);
  }

  @Override
  public Optional<ZernovaVersion> getMinZernovaVersion() {
    return this.delegate.getMinZernovaVersion();
  }

  @Override
  public void withMinZernovaVersion(ZernovaVersion minZernovaVersion) {
    this.delegate.withMinZernovaVersion(minZernovaVersion);
  }

  @Override
  public List<ParameterGroupDeclaration> getParameterGroups() {
    return this.delegate.getParameterGroups();
  }

  @Override
  public ParameterGroupDeclaration getParameterGroup(String groupName) {
    return this.delegate.getParameterGroup(groupName);
  }

  @Override
  public ParameterGroupDeclaration getDefaultParameterGroup() {
    return this.delegate.getDefaultParameterGroup();
  }

  @Override
  public List<ParameterDeclaration> getAllParameters() {
    return this.delegate.getAllParameters();
  }

  @Override
  public void addSemanticTerm(String semanticTerm) {
    this.delegate.addSemanticTerm(semanticTerm);
  }

  @Override
  public Set<String> getSemanticTerms() {
    return this.delegate.getSemanticTerms();
  }

  @Override
  public String getName() {
    return this.delegate.getName();
  }

  @Override
  public Set<ModelProperty> getModelProperties() {
    return this.delegate.getModelProperties();
  }

  @Override
  public <P extends ModelProperty> Optional<P> getModelProperty(Class<P> propertyType) {
    return this.delegate.getModelProperty(propertyType);
  }

  @Override
  public ConstructDeclaration addModelProperty(ModelProperty modelProperty) {
    this.delegate.addModelProperty(modelProperty);
    return this;
  }

  @Override
  public String getDescription() {
    return this.delegate.getDescription();
  }

  @Override
  public void setDescription(String description) {
    this.delegate.setDescription(description);
  }

  @Override
  public DisplayModel getDisplayModel() {
    return this.delegate.getDisplayModel();
  }

  @Override
  public void setDisplayModel(DisplayModel displayModel) {
    this.delegate.setDisplayModel(displayModel);
  }
}
