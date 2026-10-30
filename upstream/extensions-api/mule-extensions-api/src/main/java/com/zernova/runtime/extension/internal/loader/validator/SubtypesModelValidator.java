/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.validator;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.getId;
import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.getType;
import static com.zernova.runtime.extension.api.util.ExtensionModelUtils.toSubTypesMap;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.ImportedTypeModel;
import com.zernova.runtime.api.meta.model.SubTypesModel;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.loader.Problem;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;
import com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * {@link ExtensionModelValidator} which applies to {@link ExtensionModel}s.
 * <p>
 * This validator checks that all {@link ExtensionModel Extension} global elements declarations like {@link SubTypesModel
 * Subtypes} or {@link ImportedTypeModel Imported types} are valid.
 *
 * @since 1.0
 */
public final class SubtypesModelValidator implements ExtensionModelValidator {

  @Override
  public void validate(ExtensionModel model, ProblemsReporter problemsReporter) {
    final Map<ObjectType, Set<ObjectType>> typesMapping = toSubTypesMap(model.getSubTypes());
    validateBaseTypeNotFinal(model, typesMapping, problemsReporter);
    validateSubtypesExtendOrImplementBaseType(model, typesMapping, problemsReporter);
  }

  private void validateBaseTypeNotFinal(ExtensionModel model, Map<ObjectType, Set<ObjectType>> typesMapping,
                                        ProblemsReporter problemsReporter) {
    List<String> finalBaseTypes = typesMapping.keySet().stream()
        .filter(ExtensionMetadataTypeUtils::isFinal)
        .map(type -> getId(type).orElse(null))
        .filter(type -> type != null)
        .collect(toList());

    if (!finalBaseTypes.isEmpty()) {
      problemsReporter
          .addError(new Problem(model,
                                format("All the declared SubtypesMapping should have non final base types, but [%s] are final",
                                       Arrays.toString(finalBaseTypes.toArray()))));
    }
  }

  private void validateSubtypesExtendOrImplementBaseType(ExtensionModel model,
                                                         Map<ObjectType, Set<ObjectType>> typesMapping,
                                                         ProblemsReporter problemsReporter) {
    for (Map.Entry<ObjectType, Set<ObjectType>> subtypes : typesMapping.entrySet()) {
      Optional<Class<Object>> baseType = getType(subtypes.getKey());
      if (!baseType.isPresent()) {
        continue;
      }

      List<String> invalidTypes = subtypes.getValue().stream()
          .map(ExtensionMetadataTypeUtils::getType)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .filter(s -> !baseType.get().isAssignableFrom(s))
          .map(Class::getSimpleName)
          .collect(toList());

      if (!invalidTypes.isEmpty()) {
        problemsReporter
            .addError(new Problem(model,
                                  format("All the declared Subtypes should be concrete implementations of the give baseType,"
                                      + " but [%s] are not implementations of [%s]",
                                         Arrays.toString(invalidTypes.toArray()),
                                         baseType.get().getSimpleName())));
      }
    }
  }
}
