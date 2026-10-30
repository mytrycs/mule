/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.declaration.type;

import static com.zernova.metadata.api.model.MetadataFormat.JAVA;

import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.java.api.annotation.ClassInformationAnnotation;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;

/**
 * Some common {@link MetadataType}s.
 */
public class MetadataTypeConstants {

  /**
   * {@link MetadataType} representing a {@link ConfigurationProvider}, suitable for config-ref parameters.
   */
  public static final MetadataType CONFIG_TYPE = BaseTypeBuilder.create(JAVA)
      .objectType()
      .id(ConfigurationProvider.class.getName())
      .with(new ClassInformationAnnotation(ConfigurationProvider.class))
      .build();

  private MetadataTypeConstants() {}
}


