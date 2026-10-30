/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension.model;

import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.annotation.dsl.xml.TypeDsl;

import java.io.Serializable;

@Alias("door")
@TypeDsl(allowTopLevelDefinition = true)
public class KnockeableDoor implements Serializable {

  @Parameter
  private String victim;

  @Parameter
  private String address;

  @Parameter
  @Optional
  private KnockeableDoor previous;

  public KnockeableDoor() {}

  public KnockeableDoor(String victim) {
    this.victim = victim;
  }

  public KnockeableDoor(String victim, String address) {
    this.victim = victim;
    this.address = address;
  }

  public static String knock(String value) {
    return "Knocked on " + value;
  }

  public String knock() {
    return knock(victim);
  }

  public void setVictim(String victim) {
    this.victim = victim;
  }

  public String getVictim() {
    return victim;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getAddress() {
    return address;
  }

  public void setPrevious(KnockeableDoor previous) {
    this.previous = previous;
  }

  public KnockeableDoor getPrevious() {
    return previous;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof KnockeableDoor) {
      return victim.equals(((KnockeableDoor) obj).victim);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return victim.hashCode();
  }
}
