/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.semantics;

import static com.zernova.runtime.core.api.util.StringUtils.isBlank;

import com.zernova.runtime.module.extension.api.loader.java.type.WithAnnotations;
import com.zernova.sdk.api.annotation.semantics.SemanticTerms;

import java.util.Set;

public final class SemanticTermsParserUtils {

  public static void addCustomTerms(WithAnnotations annotated, Set<String> terms) {
    annotated.getValueFromAnnotation(SemanticTerms.class)
        .ifPresent(semanticTermsAnnotationValueFetcher -> semanticTermsAnnotationValueFetcher.getArrayValue(SemanticTerms::value)
            .forEach(term -> {
              if (!isBlank(term)) {
                terms.add(term.trim());
              }
            }));
  }

  public static void addTermIfPresent(Set<String> searchSpace, String searchTerm, String mappedTerm, Set<String> targetSpace) {
    if (searchSpace.contains(searchTerm)) {
      targetSpace.add(mappedTerm);
    }
  }

  private SemanticTermsParserUtils() {}
}
