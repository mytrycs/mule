/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.dsl.spring;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_EXTENSION_AUTH_CODE_HANDLER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_EXTENSION_CLIENT_CREDENTIALS_HANDLER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_EXTENSION_MANAGER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_EXTENSION_PLATFORM_MANAGED_HANDLER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_MULE_CONTEXT;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_POLICY_MANAGER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_POLICY_PROVIDER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_REGISTRY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_TIME_SUPPLIER;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.time.TimeSupplier;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.policy.PolicyProvider;
import com.zernova.runtime.core.internal.policy.PolicyManager;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.authcode.AuthorizationCodeOAuthHandler;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.clientcredentials.ClientCredentialsOAuthHandler;
import com.zernova.runtime.module.extension.internal.runtime.connectivity.oauth.ocs.PlatformManagedOAuthHandler;

import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap;

/**
 * Populates a reference to a Mule API injectable object into an object.
 *
 * @since 4.0
 */
class ObjectReferencePopulator {

  // TODO MULE-9682: Load this list dynamically and define the whole set of objects that can be injected.
  private static ImmutableMap<Class<?>, String> OBJECT_REFERENCES =
      new ImmutableMap.Builder()
          .put(Registry.class, OBJECT_REGISTRY)
          .put(ZernovaContext.class, OBJECT_MULE_CONTEXT)
          .put(TimeSupplier.class, OBJECT_TIME_SUPPLIER)
          .put(ExtensionManager.class, OBJECT_EXTENSION_MANAGER)
          .put(PolicyManager.class, OBJECT_POLICY_MANAGER)
          .put(PolicyProvider.class, OBJECT_POLICY_PROVIDER)
          .put(AuthorizationCodeOAuthHandler.class, OBJECT_EXTENSION_AUTH_CODE_HANDLER)
          .put(ClientCredentialsOAuthHandler.class, OBJECT_EXTENSION_CLIENT_CREDENTIALS_HANDLER)
          .put(PlatformManagedOAuthHandler.class, OBJECT_EXTENSION_PLATFORM_MANAGED_HANDLER)
          .build();


  /**
   * @param type           the type of the object to be popoulated with
   * @param typeIdConsumer a {@code Consumer} that will be provided with the identifier of the object to populate
   */
  public void populate(Class<?> type, Consumer<String> typeIdConsumer) {
    String referenceName = OBJECT_REFERENCES.get(type);
    if (referenceName == null) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not determine reference object of type: " + type.getName()));
    }
    typeIdConsumer.accept(referenceName);
  }

}
