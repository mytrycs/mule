/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source.legacy;

import com.zernova.runtime.extension.api.runtime.source.PollContext;

/**
 * Utility class for using legacy PollItemStatus.
 *
 * @since 4.4.0
 */
public class LegacyPollItemStatusUtils {

  /**
   * Gives the correspondent {@link PollContext.PollItemStatus} to the give
   * {@link com.zernova.sdk.api.runtime.source.PollContext.PollItemStatus}
   *
   * @param pollItemStatus a {@link com.zernova.sdk.api.runtime.source.PollContext.PollItemStatus}
   * @return the correspondent {@link PollContext.PollItemStatus}
   */
  public static PollContext.PollItemStatus from(com.zernova.sdk.api.runtime.source.PollContext.PollItemStatus pollItemStatus) {
    switch (pollItemStatus) {
      case SOURCE_STOPPING:
        return PollContext.PollItemStatus.SOURCE_STOPPING;
      case ALREADY_IN_PROCESS:
        return PollContext.PollItemStatus.ALREADY_IN_PROCESS;
      case FILTERED_BY_WATERMARK:
        return PollContext.PollItemStatus.FILTERED_BY_WATERMARK;
      case ACCEPTED:
        return PollContext.PollItemStatus.ACCEPTED;
      default:
        throw new IllegalArgumentException();
    }
  }

}
