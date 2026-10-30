/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.message;

import com.zernova.runtime.api.message.ItemSequenceInfo;
import com.zernova.runtime.core.api.message.GroupCorrelation;

public class ItemSequenceInfoUtils {


  public static ItemSequenceInfo fromGroupCorrelation(GroupCorrelation groupCorrelation) {
    ItemSequenceInfo itemSequenceInfo = null;
    if (groupCorrelation != null) {
      if (groupCorrelation.getGroupSize().isPresent()) {
        itemSequenceInfo = ItemSequenceInfo.of(groupCorrelation.getSequence(), groupCorrelation.getGroupSize().getAsInt());
      } else {
        itemSequenceInfo = ItemSequenceInfo.of(groupCorrelation.getSequence());
      }
    }
    return itemSequenceInfo;
  }

  public static GroupCorrelation toGroupCorrelation(ItemSequenceInfo itemSequenceInfo) {
    GroupCorrelation groupCorrelation = null;
    if (itemSequenceInfo != null) {
      if (itemSequenceInfo.getSequenceSize().isPresent()) {
        groupCorrelation = GroupCorrelation.of(itemSequenceInfo.getPosition(), itemSequenceInfo.getSequenceSize().getAsInt());
      } else {
        groupCorrelation = GroupCorrelation.of(itemSequenceInfo.getPosition());
      }
    }
    return groupCorrelation;
  }


}
