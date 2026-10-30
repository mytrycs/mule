/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.deployment.meta;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.Map;

/**
 * A builder to create instances of {@link ZernovaArtifactLoaderDescriptor}.
 * <p>
 * A new instance of this class should be used per each manifest to be created. The created instances will be immutable.
 *
 * @since 1.0
 */
public final class ZernovaArtifactLoaderDescriptorBuilder {

  private String id;
  private final Map<String, Object> properties = new HashMap<>();

  /**
   * Sets the describer's ID
   *
   * @param id the ID to be set
   * @return {@code this} builder
   */
  public ZernovaArtifactLoaderDescriptorBuilder setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * Sets the given property on the describer.
   * <p>
   * If a value is already associated with the {@code key}, then it is overridden with the new {@code value}
   *
   * @param key   the property's key
   * @param value the property's value
   * @return {@code this} builder
   */
  public ZernovaArtifactLoaderDescriptorBuilder addProperty(String key, Object value) {
    checkArgument(!isBlank(key), "key cannot be a blank key");
    properties.put(key, value);
    return this;
  }

  /**
   * Creates and returns a new {@link ZernovaArtifactLoaderDescriptor} according to the values set
   *
   * @return a {@link ZernovaArtifactLoaderDescriptor}
   */
  public ZernovaArtifactLoaderDescriptor build() {
    checkArgument(!isBlank(id), "ID cannot be a blank");
    return new ZernovaArtifactLoaderDescriptor(id, properties);
  }
}
