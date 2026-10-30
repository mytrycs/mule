/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.domain.wrapper;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.connectivity.ConnectivityTestingService;
import com.zernova.runtime.api.metadata.MetadataService;
import com.zernova.runtime.api.value.ValueProviderService;
import com.zernova.runtime.core.api.context.notification.ZernovaContextListener;
import com.zernova.runtime.deployment.model.api.DeploymentStartException;
import com.zernova.runtime.deployment.model.api.InstallException;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.runtime.deployment.model.api.domain.Domain;
import com.zernova.runtime.deployment.model.api.plugin.ArtifactPlugin;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor;

import java.io.File;
import java.util.List;

public class TestDomainWrapper implements Domain {

  private final Domain delegate;
  private boolean failOnPurpose;
  private boolean failOnDispose;

  public TestDomainWrapper(Domain delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean containsSharedResources() {
    return delegate.containsSharedResources();
  }

  @Override
  public Registry getRegistry() {
    return delegate.getArtifactContext() == null ? null : delegate.getArtifactContext().getRegistry();
  }

  @Override
  public ArtifactContext getArtifactContext() {
    return delegate.getArtifactContext();
  }

  @Override
  public File getLocation() {
    return delegate.getLocation();
  }

  @Override
  public ConnectivityTestingService getConnectivityTestingService() {
    return delegate.getConnectivityTestingService();
  }

  @Override
  public MetadataService getMetadataService() {
    return delegate.getMetadataService();
  }

  @Override
  public ValueProviderService getValueProviderService() {
    return delegate.getValueProviderService();
  }

  @Override
  public List<ArtifactPlugin> getArtifactPlugins() {
    return delegate.getArtifactPlugins();
  }

  @Override
  public void setMuleContextListener(ZernovaContextListener muleContextListener) {
    delegate.setMuleContextListener(muleContextListener);
  }

  @Override
  public void install() throws InstallException {
    delegate.install();
  }

  @Override
  public void init() {
    delegate.init();
  }

  @Override
  public void initTooling() {
    delegate.initTooling();
  }

  @Override
  public void lazyInit() {
    delegate.lazyInit();
  }

  @Override
  public void lazyInit(boolean disableXmlValidations) {
    delegate.lazyInit(disableXmlValidations);
  }

  @Override
  public void lazyInitTooling(boolean disableXmlValidations) {
    delegate.lazyInitTooling(disableXmlValidations);
  }

  @Override
  public void start() throws DeploymentStartException {
    delegate.start();
  }

  @Override
  public void stop() {
    if (failOnPurpose) {
      fail();
    }
    delegate.stop();
  }

  private void fail() {
    throw new RuntimeException("fail on purpose");
  }

  @Override
  public void dispose() {
    if (failOnDispose) {
      fail();
    }
    delegate.dispose();
  }

  @Override
  public String getArtifactName() {
    return delegate.getArtifactName();
  }

  @Override
  public String getArtifactId() {
    return delegate.getArtifactId();
  }

  @Override
  public DomainDescriptor getDescriptor() {
    return delegate.getDescriptor();
  }

  @Override
  public File[] getResourceFiles() {
    return delegate.getResourceFiles();
  }

  @Override
  public ArtifactClassLoader getArtifactClassLoader() {
    return delegate.getArtifactClassLoader();
  }

  public void setFailOnStop() {
    this.failOnPurpose = true;
  }

  public void setFailOnDispose() {
    this.failOnDispose = true;
  }
}
