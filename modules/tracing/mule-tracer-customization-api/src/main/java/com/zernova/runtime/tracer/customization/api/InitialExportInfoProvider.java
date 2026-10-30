/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.customization.api;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.tracer.api.span.info.InitialExportInfo;

public interface InitialExportInfoProvider {

  /**
   * @param component the {@link Component} the component.
   *
   * @return {@link InitialExportInfo} for a component.
   */
  InitialExportInfo getInitialExportInfo(Component component);

  /**
   * @param component      the {@link Component} the component.
   * @param spanNameSuffix the span name suffix
   *
   * @return {@link InitialExportInfo} for a component.
   */
  InitialExportInfo getInitialExportInfo(Component component, String spanNameSuffix);

  /**
   * @param spanName the span name.
   *
   * @return {@link InitialExportInfo} for a component.
   */
  InitialExportInfo getInitialExportInfo(String spanName);

  /**
   * @param spanName   the span name.
   *
   * @param debugLevel indicates if it is debug level.
   *
   * @return {@link InitialExportInfo} for a component.
   */
  InitialExportInfo getInitialExportInfo(String spanName, boolean debugLevel);
}
