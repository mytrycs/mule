/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.dsl.processor;

import static com.zernova.runtime.api.metadata.MediaType.parseDefinedInApp;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.DataTypeParamsBuilder;
import com.zernova.runtime.config.api.dsl.ObjectFactoryCommonConfigurator;
import com.zernova.runtime.core.privileged.processor.simple.AbstractAddVariablePropertyProcessor;

import java.util.Map;

/**
 * {@link ObjectFactoryCommonConfigurator} for transformers in Mule.
 *
 * The transformer class that will be configured setting the returnType, mimeType, name, encoding and ignoreBadInput
 * configuration.
 *
 * @since 4.0
 */
public final class AddVariablePropertyConfigurator
    implements ObjectFactoryCommonConfigurator<AbstractAddVariablePropertyProcessor> {

  /**
   * Configures the common parameters of every transformer.
   *
   * @param propVarSetterInstance the transformar instance
   * @param parameters            the set of parameters configured in the component model according to the
   *                              {@link com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition}
   */
  @Override
  public void configure(AbstractAddVariablePropertyProcessor propVarSetterInstance, Map<String, Object> parameters) {
    String mimeType = (String) parameters.get("mimeType");
    String encoding = (String) parameters.get("encoding");
    if (mimeType != null) {
      DataTypeParamsBuilder builder = DataType.builder();
      if (isNotEmpty(mimeType)) {
        builder.mediaType(parseDefinedInApp(mimeType));
      }
      propVarSetterInstance.setReturnDataType(builder.charset(encoding).build());
    }
  }

}
