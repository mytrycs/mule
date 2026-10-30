/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.api.util.ZernovaSystemProperties.MULE_MEL_AS_DEFAULT;
import static com.zernova.runtime.ast.api.validation.Validation.Level.WARN;
import static com.zernova.runtime.ast.api.validation.ValidationResultItem.create;

import static java.lang.Boolean.valueOf;
import static java.lang.System.getProperty;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.validation.ArtifactValidation;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Properties or configs used to enable MEL support are not present.
 *
 * @since 4.7
 */
public class MelNotEnabled implements ArtifactValidation {

  @Override
  public String getName() {
    return "MEL not enabled";
  }

  @Override
  public String getDescription() {
    return "Properties or configs used to enable MEL support are not present";
  }

  @Override
  public Level getLevel() {
    // WARN for now because the rest of the usages of the compatibility features (not just MEL) will be disabled in the future.
    return WARN;
  }

  @Override
  public List<ValidationResultItem> validateMany(ArtifactAst artifact) {
    if (isMelDefault()) {
      return singletonList(create(emptyList(), this,
                                  "Runtime has the '" + MULE_MEL_AS_DEFAULT + "' system property set to true."));
    } else {
      return emptyList();
    }
  }

  private boolean isMelDefault() {
    return valueOf(getProperty(MULE_MEL_AS_DEFAULT, "false"));
  }

  @Override
  public Predicate<List<ComponentAst>> applicable() {
    return h -> true;
  }

  @Override
  public Optional<ValidationResultItem> validate(ComponentAst component, ArtifactAst artifact) {
    return this.validate(artifact);
  }

}
