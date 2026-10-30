/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.api;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.connectivity.ConnectivityTestingService;
import com.zernova.runtime.api.metadata.MetadataService;
import com.zernova.runtime.api.value.ValueProviderService;
import com.zernova.runtime.core.api.context.notification.ZernovaContextListener;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.runtime.deployment.model.api.plugin.ArtifactPlugin;
import com.zernova.runtime.module.artifact.api.Artifact;

import java.io.File;
import java.util.List;

/**
 * An Artifact is an abstract representation of a deployable unit within the mule container.
 *
 * @param <D> The type of the artifact's descriptor
 */
@NoImplement
public interface DeployableArtifact<D extends com.zernova.runtime.module.artifact.api.descriptor.DeployableArtifactDescriptor>
    extends Artifact<D> {

  /**
   * Install the artifact. Most commonly this includes the creation of the class loader and validation of resources.
   */
  void install() throws InstallException;

  /**
   * Initialise the artifact resources
   */
  void init();

  /**
   * Initialise the artifact resources for tooling
   */
  void initTooling();

  /**
   * Initialise the minimal resources required for this artifact to execute components. By default XML validations will not be
   * applied when doing lazy initialization. If needed use {@link #lazyInit(boolean)}.
   */
  void lazyInit();

  /**
   * Initialise the minimal resources required for this artifact to execute components.
   *
   * @param disableXmlValidations {@code true} validations when parsing the XML will happen, otherwise {@code false}.
   */
  void lazyInit(boolean disableXmlValidations);

  /**
   * Initialise the minimal resources required for this artifact to execute components and provide tooling cpabilities.
   *
   * @param disableXmlValidations {@code true} validations when parsing the XML will happen, otherwise {@code false}.
   */
  void lazyInitTooling(boolean disableXmlValidations);

  /**
   * Starts the artifact execution
   */
  void start() throws DeploymentStartException;

  /**
   * Stops the artifact execution
   */
  void stop();

  /**
   * @return the artifact descriptor
   */
  @Override
  D getDescriptor();

  /**
   * Dispose the artifact. Most commonly this includes the release of the resources held by the artifact
   */
  void dispose();

  /**
   * Do not use this method if the artifact initialization wasn't successful or the artifact has been destroyed.
   *
   * @return the registry of the artifact.
   */
  ArtifactContext getArtifactContext();

  /**
   * Do not use this method if the artifact initialization wasn't successful or the artifact has been destroyed.
   *
   * @return the registry of the artifact.
   * @deprecated Access the registry through {@link #getArtifactContext()}.
   */
  @Deprecated
  Registry getRegistry();

  /**
   * @return the directory where the artifact content is stored.
   */
  File getLocation();

  /**
   * Do not use this method if the artifact initialization wasn't successful or the artifact has been destroyed.
   *
   * @return a service to test connection over configuration components.
   */
  ConnectivityTestingService getConnectivityTestingService();

  /**
   * Do not use this method if the artifact initialization wasn't successful or the artifact has been destroyed.
   *
   * @return the {@link MetadataService} which can resolve the metadata of the components inside the current
   *         {@link DeployableArtifact}
   * @see MetadataService
   */
  MetadataService getMetadataService();

  /**
   * Do not use this method if the artifact initialization wasn't successful or the artifact has been destroyed.
   *
   * @return the {@link ValueProviderService} which can resolve possible values for a component configuration.
   */
  ValueProviderService getValueProviderService();

  /**
   * @return the plugins that are owned by the deployable artifact. Non null
   */
  List<ArtifactPlugin> getArtifactPlugins();

  /**
   * Sets a {@link ZernovaContextListener}.
   *
   * @param muleContextListener {@link ZernovaContextListener} to be set for this deployable artifact.
   */
  void setMuleContextListener(ZernovaContextListener muleContextListener);

}
