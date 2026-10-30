/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.util;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.ZernovaExtensionAnnotationParser.mapReduceSingleAnnotation;

import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionParameter;
import com.zernova.runtime.module.extension.internal.loader.utils.ParameterGroupInfo;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Utils class for handling cases where either {@link ParameterGroup} or {@link com.zernova.sdk.api.annotation.param.ParameterGroup}
 * could be used
 *
 * @since 4.5.0
 */
public class ParameterGroupUtils {

  /**
   * Given a set of annotation, this method returns whether they correspond to a parameter group or not.
   *
   * @param annotations a set of annotations
   * @return whether the set of annotations belong to a parameter group or not.
   */
  public static boolean hasParameterGroupAnnotation(Set<Class<? extends Annotation>> annotations) {
    return annotations.contains(ParameterGroup.class)
        || annotations.contains(com.zernova.sdk.api.annotation.param.ParameterGroup.class);
  }

  /**
   * Given a map that holds as key annotations classes, and as value an instance of that annotation class. This method looks for
   * parameter group annotations, and if present, it returns an {@link Optional} which holds a {@link Boolean} that says whether
   * the parameter group is showInDsl or not. If no corresponding annotation is present, it returns {@link Optional#empty()}
   *
   * @param annotations a map that holds as key annotations classes, and as value an instance of that annotation class.
   * @return whether the parameter group is showInDsl or not, or {@link Optional#empty()} if the given annotations do not
   *         correspond to a parameter group.
   */
  public static Optional<Boolean> isParameterGroupShowInDsl(Map<Class<? extends Annotation>, Annotation> annotations) {
    if (annotations.containsKey(ParameterGroup.class)) {
      return of(((ParameterGroup) annotations.get(ParameterGroup.class)).showInDsl());
    }
    if (annotations.containsKey(com.zernova.sdk.api.annotation.param.ParameterGroup.class)) {
      return of(((com.zernova.sdk.api.annotation.param.ParameterGroup) annotations
          .get(com.zernova.sdk.api.annotation.param.ParameterGroup.class)).showInDsl());
    }
    return empty();
  }

  /**
   * Given a map that holds as key annotations classes, and as value an instance of that annotation class. This method looks for
   * parameter group annotations, and if present, it returns an {@link Optional} with the {@link ParameterGroupInfo} that
   * represents the parameter group. If no corresponding annotation is present, it returns {@link Optional#empty()}
   *
   * @param annotations a map that holds as key annotations classes, and as value an instance of that annotation class.
   * @return {@link ParameterGroupInfo} that represents the parameter group, or {@link Optional#empty()} if the given annotations
   *         do not correspond to a parameter group.
   */
  public static Optional<ParameterGroupInfo> getParameterGroupInfo(ExtensionParameter extensionParameter) {
    return mapReduceSingleAnnotation(extensionParameter, "parameter group", extensionParameter.getName(),
                                     ParameterGroup.class,
                                     com.zernova.sdk.api.annotation.param.ParameterGroup.class,
                                     legacyParameterGroupAnnotationValueFetcher -> new ParameterGroupInfo(legacyParameterGroupAnnotationValueFetcher
                                         .getStringValue(ParameterGroup::name),
                                                                                                          legacyParameterGroupAnnotationValueFetcher
                                                                                                              .getBooleanValue(ParameterGroup::showInDsl)),
                                     sdkParameterGroupAnnotationValueFetcher -> new ParameterGroupInfo(sdkParameterGroupAnnotationValueFetcher
                                         .getStringValue(com.zernova.sdk.api.annotation.param.ParameterGroup::name),
                                                                                                       sdkParameterGroupAnnotationValueFetcher
                                                                                                           .getBooleanValue(com.zernova.sdk.api.annotation.param.ParameterGroup::showInDsl)));
  }
}
