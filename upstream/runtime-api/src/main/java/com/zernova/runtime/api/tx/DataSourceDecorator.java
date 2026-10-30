/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.tx;

import com.zernova.runtime.api.config.DatabasePoolingProfile;

import javax.sql.DataSource;

/**
 * Decorates {@link DataSource} if required in order to work with XA transactions
 *
 * @since 1.0
 * @deprecated since 4.10 use the new XA connection provider introduced in 4.10 instead.
 */
@Deprecated // (since = "4.10", forRemoval = true)
public interface DataSourceDecorator {

  /**
   * Decorates a dataSource
   *
   * @param dataSource       dataSource to decorate. Non null
   * @param dataSourceName   dataSource bean name
   * @param dbPoolingProfile pooling profile use to create the wrapped dataSource
   * @return
   */
  DataSource decorate(DataSource dataSource, String dataSourceName, DatabasePoolingProfile dbPoolingProfile);

  /**
   * Indicates whether or not this decorator can decorate a given datasource
   *
   * @param dataSource dataSource to check
   * @return whether the {@code dataSource} can be decorated by {@code this} instance
   */
  boolean appliesTo(DataSource dataSource);

}
