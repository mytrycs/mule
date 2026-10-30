/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source.legacy;

import static java.lang.String.format;

import com.zernova.sdk.api.runtime.source.Source;

/**
 * Class in charge of adapting legacy {@link com.zernova.runtime.extension.api.runtime.source.Source}s into {@link Source}s.
 *
 * @since 4.4.0
 */
public class SdkSourceAdapterFactory {

  /**
   * Given an {@link Object}: if it is a {@link Source}, the same instance is returned. If it is a legacy
   * {@link com.zernova.runtime.extension.api.runtime.source.Source}, an adapter of it is return. Otherwise, this method fails.
   *
   * @param source a source that can either be a {@link Source} or a legacy
   *               {@link com.zernova.runtime.extension.api.runtime.source.Source}.
   * @return a {@link Source} that represents the given object.
   */
  public static Source createAdapter(Object source) {
    if (source instanceof Source) {
      return (Source) source;
    } else if (source instanceof com.zernova.runtime.extension.api.runtime.source.Source) {
      return SdkSourceAdapter.from((com.zernova.runtime.extension.api.runtime.source.Source) source);
    } else {
      throw new IllegalArgumentException(format("The given argument needs to either be an instance of %s or %s",
                                                Source.class.getCanonicalName(),
                                                com.zernova.runtime.extension.api.runtime.source.Source.class.getCanonicalName()));
    }
  }

}
