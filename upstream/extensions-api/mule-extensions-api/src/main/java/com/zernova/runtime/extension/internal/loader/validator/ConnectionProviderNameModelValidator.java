/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.validator;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import com.zernova.runtime.api.meta.NamedObject;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.meta.model.util.IdempotentExtensionWalker;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.loader.Problem;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.util.HashSet;
import java.util.Set;

/**
 * Validates that there's no name clashing among the extension's connection providers
 *
 * @since 1.0
 */
public class ConnectionProviderNameModelValidator implements ExtensionModelValidator {

  @Override
  public void validate(ExtensionModel model, ProblemsReporter problemsReporter) throws IllegalModelDefinitionException {
    Multiset<String> names = HashMultiset.create();
    Set<ConnectionProviderModel> models = new HashSet<>();
    new IdempotentExtensionWalker() {

      @Override
      public void onConnectionProvider(ConnectionProviderModel model) {
        models.add(model);
        names.add(model.getName());
      }
    }.walk(model);

    Set<ConnectionProviderModel> repeatedNameModels =
        models.stream().filter(cp -> names.count(cp.getName()) > 1).collect(toSet());

    if (!repeatedNameModels.isEmpty()) {
      problemsReporter.addError(new Problem(model,
                                            format("There are %d connection providers with repeated names. Offending names are: [%s]",
                                                   repeatedNameModels.size(),
                                                   repeatedNameModels.stream().map(NamedObject::getName).collect(joining(",")))));
    }
  }
}
