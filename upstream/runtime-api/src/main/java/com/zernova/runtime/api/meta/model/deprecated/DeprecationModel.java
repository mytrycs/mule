/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model.deprecated;


import java.util.Optional;

/**
 * A model that fully describes the deprecation of a part of the extension model that is a {@link DeprecableModel}.
 *
 * @since 1.2
 */
public interface DeprecationModel {

  /**
   * @return a {@link String} that describes why something was deprecated, what can be used as substitute, or both.
   */
  String getMessage();

  /**
   * @return a {@link String} which is the version of the extension in which the annotated member was deprecated.
   */
  String getDeprecatedSince();

  /**
   * @return a {@link String} which is the version of the extension in which the annotated member will be removed or was removed.
   */
  Optional<String> getToRemoveIn();

}
