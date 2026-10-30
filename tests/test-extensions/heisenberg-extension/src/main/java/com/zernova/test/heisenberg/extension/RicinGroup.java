/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension;

import static com.zernova.test.heisenberg.extension.HeisenbergExtension.RICIN_PACKS_SUMMARY;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.display.Summary;
import com.zernova.sdk.api.annotation.param.display.Placement;
import com.zernova.test.heisenberg.extension.model.KnockeableDoor;
import com.zernova.test.heisenberg.extension.model.Ricin;

import java.util.Map;
import java.util.Set;

public class RicinGroup {

  @Parameter
  @Optional
  @Placement(order = 1)
  private Map<String, Ricin> labeledRicin;

  @Parameter
  @Optional
  private KnockeableDoor nextDoor;

  @Parameter
  @Optional
  @Placement(order = 2)
  @Summary(RICIN_PACKS_SUMMARY)
  private Set<Ricin> ricinPacks;

  public Map<String, Ricin> getLabeledRicin() {
    return labeledRicin;
  }

  public KnockeableDoor getNextDoor() {
    return nextDoor;
  }

  public Set<Ricin> getRicinPacks() {
    return ricinPacks;
  }
}
