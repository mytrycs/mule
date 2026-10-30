/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.config.factory;

import static java.lang.String.format;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.findOperation;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.module.extension.mule.internal.construct.Operation;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.dsl.api.component.ObjectFactory;
import com.zernova.runtime.extension.api.exception.IllegalOperationModelDefinitionException;
import com.zernova.runtime.module.extension.mule.internal.construct.ZernovaOperation;
import com.zernova.runtime.module.extension.mule.internal.config.provider.OperationDslBuildingDefinitionProvider.OperationBody;

/**
 * {@link ObjectFactory} for creating {@link Operation} instances
 *
 * @since 4.5.0
 */
public class DefaultOperationObjectFactory extends AbstractComponent implements ObjectFactory<Operation> {

  private final String name;
  private final ExtensionManager extensionManager;
  private final ZernovaContext muleContext;
  private OperationBody body;

  public DefaultOperationObjectFactory(String name,
                                       ExtensionManager extensionManager,
                                       ZernovaContext muleContext) {
    this.name = name;
    this.extensionManager = extensionManager;
    this.muleContext = muleContext;
  }

  @Override
  public Operation getObject() {
    Operation operation = ZernovaOperation.builder()
        .processors(body.getProcessors())
        .setOperationModel(locateOperationModel())
        .setMuleContext(muleContext)
        .build();

    operation.setAnnotations(getAnnotations());
    return operation;
  }

  private OperationModel locateOperationModel() {
    ExtensionModel extensionModel = extensionManager.getExtension(muleContext.getConfiguration().getId())
        .orElseThrow(() -> new IllegalOperationModelDefinitionException(
                                                                        format("Cannot parse operation '%s'. Application ExtensionModel not found",
                                                                               name)));

    return findOperation(extensionModel, name)
        .orElseThrow(() -> new IllegalOperationModelDefinitionException(
                                                                        format("Cannot parse operation '%s'. OperationModel not found",
                                                                               name)));
  }

  public void setBody(OperationBody body) {
    this.body = body;
  }
}
