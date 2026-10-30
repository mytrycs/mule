/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.validation;

import static com.zernova.runtime.api.component.ComponentIdentifier.builder;
import static com.zernova.runtime.ast.api.util.ComponentAstPredicatesFactory.currentElemement;
import static com.zernova.runtime.ast.api.validation.Validation.Level.WARN;
import static com.zernova.runtime.ast.api.validation.ValidationResultItem.create;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.TLS_PREFIX;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.TLS_TRUST_STORE_ELEMENT_IDENTIFIER;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.ComponentParameterAst;
import com.zernova.runtime.ast.api.validation.Validation;
import com.zernova.runtime.ast.api.validation.ValidationResultItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class InsecureTLSValidation implements Validation {

  private static final ComponentIdentifier TLS_IDENTIFIER =
      builder().namespace(TLS_PREFIX).name(TLS_TRUST_STORE_ELEMENT_IDENTIFIER).build();

  @Override
  public String getName() {
    return "Insecure TLS validation";
  }

  @Override
  public String getDescription() {
    return "Warning for insecure communication";
  }

  @Override
  public Level getLevel() {
    return WARN;
  }

  @Override
  public Predicate<List<ComponentAst>> applicable() {
    return currentElemement(component -> component.getIdentifier().equals(TLS_IDENTIFIER));
  }

  @Override
  public Optional<ValidationResultItem> validate(ComponentAst component, ArtifactAst artifact) {
    final ComponentParameterAst param = component.getParameter("TrustStore", "insecure");
    Object insecure = param.getValue().getRight();
    if (insecure instanceof Boolean && (Boolean) insecure) {
      return of(create(component, param, this,
                       "Setting insecure to true renders connections vulnerable to attack. Use it only for prototyping or testing. Never use it in production environments."));
    } else {
      return empty();
    }
  }

}
