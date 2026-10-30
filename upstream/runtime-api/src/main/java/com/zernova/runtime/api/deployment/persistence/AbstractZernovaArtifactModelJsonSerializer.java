/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.deployment.persistence;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.deployment.meta.AbstractZernovaArtifactModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Base class for marshalling an {@link com.zernova.runtime.api.deployment.meta.AbstractZernovaArtifactModel} instance to {@code JSON}
 * format and back
 *
 * TODO MULE-11007: these classes should be moved to a persistence module
 *
 * @since 1.0
 */
@NoExtend
public abstract class AbstractZernovaArtifactModelJsonSerializer<T extends AbstractZernovaArtifactModel> {

  private final Gson gson;

  /**
   * Creates a new serializer
   */
  public AbstractZernovaArtifactModelJsonSerializer() {
    gson = new GsonBuilder().setPrettyPrinting().create();
  }

  /**
   * Serializes the given {@code muleArtifactModel} to JSON
   *
   * @param muleArtifactModel the describer to be marshaled
   * @return the resulting {@code JSON} in {@link String} format
   */
  public String serialize(T muleArtifactModel) {
    return gson.toJson(muleArtifactModel);
  }

  /**
   * Deserializes the given {@code JSON} back into the corresponding Java object
   *
   * @param json a describer {@code JSON} in {@link String} format
   * @return an instance of the parameterized type
   */
  public T deserialize(String json) {
    return gson.fromJson(json, getParameterizedClass());
  }

  /**
   * @return the class for the parameterized type {@link T}
   */
  protected abstract Class<T> getParameterizedClass();
}
