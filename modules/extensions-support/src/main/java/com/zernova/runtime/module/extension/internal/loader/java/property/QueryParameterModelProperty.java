/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.property;

import com.zernova.runtime.extension.api.annotation.param.Query;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.extension.api.dsql.QueryTranslator;
import com.zernova.runtime.api.meta.model.operation.OperationModel;


/**
 * A {@link ModelProperty} for a {@link OperationModel} parameter that indicates that the parameter it's a {@link Query}.
 *
 * @since 4.0
 */
public final class QueryParameterModelProperty implements ModelProperty {

  private final Class<? extends QueryTranslator> queryTranslator;

  /**
   * Creates a new instance.
   *
   * @param queryTranslator a {@link QueryTranslator} class, for translating from dsql to native.
   */
  public QueryParameterModelProperty(Class<? extends QueryTranslator> queryTranslator) {
    this.queryTranslator = queryTranslator;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return "queryParameter";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isPublic() {
    return false;
  }

  /**
   * @return a {@link QueryTranslator} instance to translate from DSQL to Native Query Language.
   */
  public Class<? extends QueryTranslator> getQueryTranslator() {
    return queryTranslator;
  }
}
