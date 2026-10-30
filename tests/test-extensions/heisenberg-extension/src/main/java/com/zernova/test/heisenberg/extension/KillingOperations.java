/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension;

import static java.lang.String.format;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import static com.zernova.runtime.extension.api.annotation.param.Optional.PAYLOAD;
import static com.zernova.test.heisenberg.extension.HeisenbergOperations.KILL_WITH_GROUP;

import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.annotation.param.stereotype.Stereotype;
import com.zernova.test.heisenberg.extension.model.KillParameters;
import com.zernova.test.heisenberg.extension.model.Ricin;
import com.zernova.test.heisenberg.extension.model.Weapon;
import com.zernova.test.heisenberg.extension.model.types.WeaponType;
import com.zernova.test.heisenberg.extension.stereotypes.DrugKillingStereotype;
import com.zernova.test.heisenberg.extension.stereotypes.KillingStereotype;

import java.util.List;
import java.util.stream.Collectors;

@Stereotype(KillingStereotype.class)
public class KillingOperations {

  @MediaType(TEXT_PLAIN)
  public String killWithCustomMessage(@ParameterGroup(name = KILL_WITH_GROUP) KillParameters killParameters) {
    return format("%s, %s", killParameters.getGoodbyeMessage(), killParameters.getVictim());
  }

  @MediaType(TEXT_PLAIN)
  public String killWithWeapon(Weapon weapon, WeaponType type, Weapon.WeaponAttributes attributesOfWeapon) {
    return format("Killed with: %s , Type %s and attribute %s", weapon.kill(), type.name(), attributesOfWeapon.getBrand());
  }

  public List<String> killWithMultiplesWeapons(@Optional(defaultValue = PAYLOAD) List<Weapon> weapons) {
    return weapons.stream().map(Weapon::kill).collect(Collectors.toList());
  }

  public List<String> killWithMultipleWildCardWeapons(List<? extends Weapon> wildCardWeapons) {
    return wildCardWeapons.stream().map(Weapon::kill).collect(Collectors.toList());
  }

  public int killWithId(int id) {
    return id;
  }

  @Stereotype(DrugKillingStereotype.class)
  public List<Ricin> drugKilling(@Optional(defaultValue = PAYLOAD) List<Ricin> ricins) {
    return ricins;
  }

  public List<Ricin> killWithRicins(@Optional(defaultValue = PAYLOAD) List<Ricin> ricins) {
    return ricins;
  }
}
