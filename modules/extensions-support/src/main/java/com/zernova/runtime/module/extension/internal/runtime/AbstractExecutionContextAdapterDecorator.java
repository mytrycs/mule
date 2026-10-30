/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.security.SecurityContext;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.notification.ServerNotificationManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.core.api.security.SecurityManager;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.privileged.transaction.TransactionConfig;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;

import java.util.Map;
import java.util.Optional;

/**
 * Base helper class to use when decorating {@link ExecutionContextAdapter}s.
 *
 * @since 4.0
 */
public abstract class AbstractExecutionContextAdapterDecorator<M extends ComponentModel> implements ExecutionContextAdapter<M> {

  private final ExecutionContextAdapter<M> decorated;

  public AbstractExecutionContextAdapterDecorator(ExecutionContextAdapter<M> decorated) {
    this.decorated = decorated;
  }

  @Override
  public CoreEvent getEvent() {
    return decorated.getEvent();
  }

  @Override
  public void changeEvent(CoreEvent updated) {
    decorated.changeEvent(updated);
  }

  @Override
  public SecurityContext getSecurityContext() {
    return decorated.getSecurityContext();
  }

  @Override
  public void setSecurityContext(SecurityContext securityContext) {
    decorated.setSecurityContext(securityContext);
  }

  @Override
  public Scheduler getCurrentScheduler() {
    return decorated.getCurrentScheduler();
  }

  @Override
  public void setCurrentScheduler(Scheduler currentScheduler) {
    decorated.setCurrentScheduler(currentScheduler);
  }

  @Override
  public M getComponentModel() {
    return decorated.getComponentModel();
  }

  @Override
  public Optional<ConfigurationInstance> getConfiguration() {
    return decorated.getConfiguration();
  }

  @Override
  public ExtensionModel getExtensionModel() {
    return decorated.getExtensionModel();
  }

  @Override
  public <T> T getParameter(String parameterName) {
    return decorated.getParameter(parameterName);
  }

  @Override
  public <T> T getParameterOrDefault(String parameterName, T defaultValue) {
    return decorated.getParameterOrDefault(parameterName, defaultValue);
  }

  @Override
  public Map<String, Object> getParameters() {
    return decorated.getParameters();
  }

  @Override
  public boolean hasParameter(String arg0) {
    return decorated.hasParameter(arg0);
  }

  @Override
  public <T> T getVariable(String key) {
    return decorated.getVariable(key);
  }

  @Override
  public Object setVariable(String key, Object value) {
    return decorated.setVariable(key, value);
  }

  @Override
  public <T> T removeVariable(String key) {
    return decorated.removeVariable(key);
  }

  @Override
  public Optional<TransactionConfig> getTransactionConfig() {
    return decorated.getTransactionConfig();
  }

  @Override
  public ZernovaContext getMuleContext() {
    return decorated.getMuleContext();
  }

  @Override
  public ArtifactEncoding getArtifactEncoding() {
    return decorated.getArtifactEncoding();
  }

  @Override
  public ServerNotificationManager getNotificationManager() {
    return decorated.getNotificationManager();
  }

  @Override
  public CursorProviderFactory getCursorProviderFactory() {
    return decorated.getCursorProviderFactory();
  }

  @Override
  public StreamingManager getStreamingManager() {
    return decorated.getStreamingManager();
  }

  @Override
  public Component getComponent() {
    return decorated.getComponent();
  }

  @Override
  public ComponentLocation getComponentLocation() {
    return decorated.getComponent().getLocation();
  }

  @Override
  public Optional<RetryPolicyTemplate> getRetryPolicyTemplate() {
    return decorated.getRetryPolicyTemplate();
  }

  @Override
  public SecurityManager getSecurityManager() {
    return decorated.getSecurityManager();
  }
}

