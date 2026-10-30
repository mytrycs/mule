/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.bean;

import com.zernova.runtime.api.artifact.ArtifactCoordinates;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.serialization.ObjectSerializer;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.CorrelationIdGenerator;
import com.zernova.runtime.core.api.config.DynamicConfigExpiration;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategyFactory;

import java.util.Optional;

import jakarta.inject.Inject;

public class ZernovaConfigurationDelegate implements ZernovaConfiguration {

  private ZernovaContext muleContext;

  @Override
  public int getDefaultResponseTimeout() {
    return getDelegate().getDefaultResponseTimeout();
  }

  @Override
  public String getWorkingDirectory() {
    return getDelegate().getWorkingDirectory();
  }

  @Override
  public String getMuleHomeDirectory() {
    return getDelegate().getMuleHomeDirectory();
  }

  @Override
  public int getDefaultTransactionTimeout() {
    return getDelegate().getDefaultTransactionTimeout();
  }

  @Override
  public boolean isClientMode() {
    return getDelegate().isClientMode();
  }

  @Override
  public String getDefaultEncoding() {
    return getDelegate().getDefaultEncoding();
  }

  @Override
  public String getId() {
    return getDelegate().getId();
  }

  @Override
  public String getDomainId() {
    return getDelegate().getDomainId();
  }

  @Override
  public String getSystemModelType() {
    return getDelegate().getSystemModelType();
  }

  @Override
  public String getSystemName() {
    return getDelegate().getSystemName();
  }

  @Override
  public boolean isAutoWrapMessageAwareTransform() {
    return getDelegate().isAutoWrapMessageAwareTransform();
  }

  @Override
  public boolean isCacheMessageAsBytes() {
    return getDelegate().isCacheMessageAsBytes();
  }

  @Override
  public boolean isEnableStreaming() {
    return getDelegate().isEnableStreaming();
  }

  @Override
  public boolean isValidateExpressions() {
    return getDelegate().isValidateExpressions();
  }

  @Override
  public boolean isLazyInit() {
    return getDelegate().isLazyInit();
  }

  @Override
  public int getDefaultQueueTimeout() {
    return getDelegate().getDefaultQueueTimeout();
  }

  @Override
  public long getShutdownTimeout() {
    return getDelegate().getShutdownTimeout();
  }

  @Override
  public int getMaxQueueTransactionFilesSizeInMegabytes() {
    return getDelegate().getMaxQueueTransactionFilesSizeInMegabytes();
  }

  @Override
  public boolean isContainerMode() {
    return getDelegate().isContainerMode();
  }

  @Override
  public boolean isStandalone() {
    return getDelegate().isStandalone();
  }

  @Override
  public String getDefaultErrorHandlerName() {
    return getDelegate().getDefaultErrorHandlerName();
  }

  @Override
  public boolean isDisableTimeouts() {
    return getDelegate().isDisableTimeouts();
  }

  @Override
  public <T> T getExtension(Class<T> extensionType) {
    return getDelegate().getExtension(extensionType);
  }

  @Override
  public ObjectSerializer getDefaultObjectSerializer() {
    return getDelegate().getDefaultObjectSerializer();
  }

  @Override
  public ProcessingStrategyFactory getDefaultProcessingStrategyFactory() {
    return getDelegate().getDefaultProcessingStrategyFactory();
  }

  @Override
  public DynamicConfigExpiration getDynamicConfigExpiration() {
    return getDelegate().getDynamicConfigExpiration();
  }

  @Override
  public boolean isInheritIterableRepeatability() {
    return getDelegate().isInheritIterableRepeatability();
  }

  @Override
  public Optional<ZernovaVersion> getMinZernovaVersion() {
    return getDelegate().getMinZernovaVersion();
  }

  @Override
  public Optional<CorrelationIdGenerator> getDefaultCorrelationIdGenerator() {
    return getDelegate().getDefaultCorrelationIdGenerator();
  }

  @Override
  public Optional<ArtifactCoordinates> getArtifactCoordinates() {
    return getDelegate().getArtifactCoordinates();
  }

  private ZernovaConfiguration getDelegate() {
    return muleContext.getConfiguration();
  }

  @Inject
  public void setMuleContext(ZernovaContext muleContext) {
    this.muleContext = muleContext;
  }

}
