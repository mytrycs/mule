/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.store;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.runtime.api.store.ObjectStoreSettings;

/**
 * Utility class to perform conversions between components of the SDK API and the Mule API
 *
 * @since 4.5.0
 */
public class SdkObjectStoreUtils {

  /**
   * Converts the SDK api OS settings {@link com.zernova.sdk.api.store.ObjectStoreSettings} into the corresponding Mule api OS
   * settings {@code ObjectStoreSettings}
   *
   * @param sdkObjectStoreSettings the SDK api OS settings to be converted
   * @return the converted Mule api OS settings
   */
  public static ObjectStoreSettings convertToMuleObjectStoreSettings(
                                                                     com.zernova.sdk.api.store.ObjectStoreSettings sdkObjectStoreSettings) {
    checkArgument(sdkObjectStoreSettings != null, "Cannot convert null value");
    return convertSettings(sdkObjectStoreSettings);
  }

  /**
   * Converts the Mule object store settings {@code ObjectStoreSettings} into the corresponding SDK api OS settings
   * {@link com.zernova.sdk.api.store.ObjectStoreSettings}.
   *
   * @param muleObjectStoreSettings the Mule api OS settings to be converted
   * @return the converted SDK api OS settings
   */
  public static com.zernova.sdk.api.store.ObjectStoreSettings convertToSdkObjectStoreSettings(
                                                                                           ObjectStoreSettings muleObjectStoreSettings) {
    checkArgument(muleObjectStoreSettings != null, "Cannot convert null value");
    return convertSettings(muleObjectStoreSettings);
  }

  private static ObjectStoreSettings convertSettings(com.zernova.sdk.api.store.ObjectStoreSettings objectStoreSettings) {
    return ObjectStoreSettings.builder()
        .persistent(objectStoreSettings.isPersistent())
        .maxEntries(objectStoreSettings.getMaxEntries().orElse(null))
        .entryTtl(objectStoreSettings.getEntryTTL().orElse(null))
        .expirationInterval(objectStoreSettings.getExpirationInterval())
        .build();
  }

  private static com.zernova.sdk.api.store.ObjectStoreSettings convertSettings(ObjectStoreSettings objectStoreSettings) {
    return com.zernova.sdk.api.store.ObjectStoreSettings.builder()
        .persistent(objectStoreSettings.isPersistent())
        .maxEntries(objectStoreSettings.getMaxEntries().orElse(null))
        .entryTtl(objectStoreSettings.getEntryTTL().orElse(null))
        .expirationInterval(objectStoreSettings.getExpirationInterval())
        .build();
  }
}
