/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.meta.model.function;

import com.zernova.api.annotation.NoImplement;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.meta.model.EnrichableModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.OutputModel;
import com.zernova.runtime.api.meta.model.deprecated.DeprecableModel;
import com.zernova.runtime.api.meta.model.display.HasDisplayModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.api.meta.model.version.HasMinZernovaVersion;

/**
 * A definition of an function in a {@link ExtensionModel}.
 * <p>
 * Function models implement the flyweight pattern. This means that a given function should only be represented by only one
 * instance of this class. Thus, if the same function is contained by different {@link HasFunctionModels} instances, then each of
 * those containers should reference the same function model instance.
 *
 * @since 1.0
 */
@NoImplement
public interface FunctionModel extends ParameterizedModel, EnrichableModel, HasDisplayModel, DeprecableModel, HasMinZernovaVersion {

  /**
   * Returns a {@link MetadataType} for the value that this component sets on the output {@link Message#getPayload()} field.
   *
   * @return a {@link MetadataType} representing the content type for the output messages
   */
  OutputModel getOutput();

}
