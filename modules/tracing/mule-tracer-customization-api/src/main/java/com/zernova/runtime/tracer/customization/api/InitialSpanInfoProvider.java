/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.tracer.customization.api;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.tracer.api.span.info.InitialSpanInfo;

/**
 * A provider for builders for {@link InitialSpanInfo}
 *
 * @since 4.5.0
 */
public interface InitialSpanInfoProvider {

  /**
   * @param component the {@link Component} to generate the {@link InitialSpanInfo} for.
   *
   * @return a {@link InitialSpanInfo} based on a {@link Component}.
   */
  InitialSpanInfo getInitialSpanInfo(Component component);

  /**
   * @param component the {@link Component} to generate the {@link InitialSpanInfo} for.
   * @param suffix    the suffix.
   * @return
   */
  InitialSpanInfo getInitialSpanInfo(Component component, String suffix);


  /**
   * @param component      the {@link Component} to generate the {@link InitialSpanInfo} for.
   * @param overriddenName the overridden name.
   * @param suffix         the suffix.
   * @return
   */
  InitialSpanInfo getInitialSpanInfo(Component component, String overriddenName, String suffix);
}
