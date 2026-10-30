/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.api.descriptor;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.artifact.ArtifactType;

import java.util.Optional;

/**
 * Maintains the registered {@link DescriptorLoader}
 */
@NoImplement
public interface DescriptorLoaderRepository {

  /**
   * Gets a descriptor loader from the repository
   *
   * @param id          identifies the loader to obtain. Non empty.
   * @param loaderClass class of {@link DescriptorLoader} to search for. No null.
   * @param <T>         type of descriptor loader to return
   * @returns a non null {@link Optional} loader of the given class and ID
   * @throws LoaderNotFoundException if there is no registered loader of type {@link T} with the provided ID.
   */
  <T extends DescriptorLoader> T get(String id, ArtifactType artifactType, Class<T> loaderClass) throws LoaderNotFoundException;
}
