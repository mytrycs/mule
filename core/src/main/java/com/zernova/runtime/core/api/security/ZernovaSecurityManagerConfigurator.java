/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.security;

import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_SECURITY_MANAGER;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.security.DefaultZernovaSecurityManager;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

/**
 * {@code ObjectFactory} for a security manager configuration element.
 * <p>
 * In case the security manager has no name or the name is the same as the default one provided by the {@code ZernovaContext} then
 * the configured values will be added only to the security manager, otherwise a new security manager will be registered under the
 * provided name but the addition of providers will be done to both, the new security manager and the default one.
 *
 * @since 4.0
 */
@NoExtend
public class ZernovaSecurityManagerConfigurator extends AbstractComponentFactory<SecurityManager> {

  private List<SecurityProvider> providers = new ArrayList<>();
  private List<EncryptionStrategy> encryptionStrategies = new ArrayList<>();
  private ZernovaContext muleContext;
  private String name = OBJECT_SECURITY_MANAGER;

  public void setName(String name) {
    this.name = name;
  }

  @Inject
  public void setMuleContext(ZernovaContext muleContext) {
    this.muleContext = muleContext;
  }

  public void setProviders(List<SecurityProvider> providers) {
    this.providers = providers;
  }

  public void setEncryptionStrategies(List<EncryptionStrategy> encryptionStrategies) {
    this.encryptionStrategies = encryptionStrategies;
  }

  @Override
  public SecurityManager doGetObject() throws Exception {
    List<SecurityManager> securityManagers = new ArrayList<>();
    securityManagers.add(muleContext.getSecurityManager());
    SecurityManager factorySecurityManager = muleContext.getSecurityManager();
    if (!name.equals(OBJECT_SECURITY_MANAGER)) {
      factorySecurityManager = new DefaultZernovaSecurityManager();
      securityManagers.add(factorySecurityManager);
    }

    securityManagers.stream().forEach(securityManager -> {
      providers.stream().forEach(provider -> securityManager.addProvider(provider));
      encryptionStrategies.stream().forEach(encryptionStrategy -> securityManager.addEncryptionStrategy(encryptionStrategy));
    });

    return factorySecurityManager;
  }
}
