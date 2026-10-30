/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.ast.api.validation.Validation.Level.ERROR;

import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.core.privileged.extension.SingletonModelProperty;

import java.util.function.Predicate;

/**
 * Elements declared as singletons via the 'SingletonModelProperty' with 'appliesToFile' as true are not repeated in a config
 * file.
 */
public class SingletonsPerFileAreNotRepeated extends SingletonsAreNotRepeated {

  @Override
  public String getName() {
    return "Singletons per file are not repeated";
  }

  @Override
  public String getDescription() {
    return "Elements declared as singletons via the 'SingletonModelProperty' with 'appliesToFile' as true are not repeated in a config file.";
  }

  @Override
  public Level getLevel() {
    return ERROR;
  }

  @Override
  protected boolean isApplicable(SingletonModelProperty smp) {
    return !super.isApplicable(smp);
  }

  @Override
  protected Predicate<? super ComponentAst> additionalFilter(ComponentAst component) {
    return comp -> comp.getMetadata().getFileName().equals(component.getMetadata().getFileName());
  }

}
