/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.customization.impl.provider;

import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.POLICY;
import static com.zernova.runtime.tracer.customization.impl.info.SpanInitialInfoUtils.getLocationAsString;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.ConfigurationProperties;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.tracer.api.span.info.InitialSpanInfo;
import com.zernova.runtime.tracer.customization.api.InitialSpanInfoProvider;
import com.zernova.runtime.tracer.customization.impl.info.ExecutionInitialSpanInfo;
import com.zernova.runtime.tracing.level.api.config.TracingLevelConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.inject.Inject;

/**
 * Default implementation of {@link InitialSpanInfoProvider}
 *
 * @since 4.5.0
 */
public class DefaultInitialSpanInfoProvider implements InitialSpanInfoProvider {

  // TODO: in the future, this can be exposed in the discovered service (new feature) to show what is being traced and how.
  private final Map<InitialSpanInfoIdentifier, ExecutionInitialSpanInfo> componentInitialSpanInfos = new ConcurrentHashMap<>();
  public static final String API_ID_CONFIGURATION_PROPERTIES_KEY = "apiId";

  ZernovaContext muleContext;

  ConfigurationProperties configurationProperties;

  TracingLevelConfiguration tracingLevelConfiguration;

  private String apiId;
  private boolean initialisedAttributes;

  @Override
  public InitialSpanInfo getInitialSpanInfo(Component component) {
    return new LazyInitialSpanInfo(() -> doGetInitialSpanInfo(component, null, null));
  }

  @Override
  public InitialSpanInfo getInitialSpanInfo(Component component, String suffix) {
    return new LazyInitialSpanInfo(() -> doGetInitialSpanInfo(component, suffix, null));
  }


  @Override
  public InitialSpanInfo getInitialSpanInfo(Component component, String overriddenName, String suffix) {
    return new LazyInitialSpanInfo(() -> doGetInitialSpanInfo(component, suffix, overriddenName));
  }

  private InitialSpanInfo doGetInitialSpanInfo(Component component, String suffix, String overriddenName) {
    // TODO: Verify initialisation order in mule context (W-12761329)
    if (!initialisedAttributes) {
      initialiseAttributes();
      initialisedAttributes = true;
    }

    // TODO: when to change the tracing level from other criteria than location is required, we will probably have to see this.
    if (component.getLocation() == null) {
      return new ExecutionInitialSpanInfo(component, apiId, overriddenName,
                                          suffix,
                                          tracingLevelConfiguration);
    }

    return componentInitialSpanInfos
        .computeIfAbsent(new InitialSpanInfoIdentifier(getLocationAsString(component.getLocation()), suffix, overriddenName),
                         identifier -> getExecutionInitialSpanInfo(component, suffix, overriddenName));
  }

  private ExecutionInitialSpanInfo getExecutionInitialSpanInfo(Component component, String suffix, String overriddenName) {
    ExecutionInitialSpanInfo executionInitialSpanInfo = new ExecutionInitialSpanInfo(component, apiId, overriddenName,
                                                                                     suffix,
                                                                                     tracingLevelConfiguration);

    tracingLevelConfiguration.onConfigurationChange(executionInitialSpanInfo::reconfigureInitialSpanInfo);

    return executionInitialSpanInfo;
  }

  public void initialiseAttributes() {
    if (muleContext.getArtifactType().equals(POLICY)) {
      apiId = configurationProperties.resolveStringProperty(API_ID_CONFIGURATION_PROPERTIES_KEY).orElse(null);
    }
  }

  @Inject
  public void setMuleContext(ZernovaContext muleContext) {
    this.muleContext = muleContext;
  }

  @Inject
  public void setConfigurationProperties(ConfigurationProperties configurationProperties) {
    this.configurationProperties = configurationProperties;
  }

  @Inject
  public void seTracingLevelConfiguration(TracingLevelConfiguration tracingLevelConfiguration) {
    this.tracingLevelConfiguration = tracingLevelConfiguration;
  }

  /**
   * @param initialSpanInfo the {@link InitialSpanInfo} to verify if it is dynamically configured, it may be affected by a change
   *                        in the configuration of tracing.
   * @return whether it is dynamically configurable.
   */
  public boolean isDynamicallyConfigurable(InitialSpanInfo initialSpanInfo) {
    return componentInitialSpanInfos.containsValue(initialSpanInfo);
  }
}
