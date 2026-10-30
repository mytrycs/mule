/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import com.zernova.runtime.api.metadata.resolving.AttributesTypeResolver;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;
import com.zernova.runtime.module.extension.api.loader.java.type.WithAnnotations;
import com.zernova.runtime.extension.api.loader.parser.AttributesResolverModelParser;
import com.zernova.runtime.module.extension.internal.loader.parser.java.metadata.JavaOutputResolverModelParser;
import com.zernova.runtime.extension.api.loader.parser.metadata.OutputResolverModelParser;
import com.zernova.runtime.module.extension.internal.metadata.chain.AllOfRoutesOutputTypeResolver;
import com.zernova.runtime.module.extension.internal.metadata.chain.OneOfRoutesOutputTypeResolver;
import com.zernova.runtime.module.extension.internal.metadata.chain.PassThroughChainOutputTypeResolver;
import com.zernova.sdk.api.annotation.metadata.AllOfRoutesOutputChainResolver;
import com.zernova.sdk.api.annotation.metadata.OneOfRoutesOutputChainResolver;
import com.zernova.sdk.api.annotation.metadata.PassThroughOutputChainResolver;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Maps shorthand annotations to the corresponding built-in {@link OutputTypeResolver}/{@link AttributesTypeResolver}.
 */
public class JavaShortHandOutputResolver {

  private static final Iterable<JavaShortHandOutputResolver> SHORT_HAND_OUTPUT_RESOLVERS =
      asList(new JavaShortHandOutputResolver(PassThroughOutputChainResolver.class,
                                             PassThroughChainOutputTypeResolver.INSTANCE,
                                             PassThroughChainOutputTypeResolver.INSTANCE),
             new JavaShortHandOutputResolver(OneOfRoutesOutputChainResolver.class,
                                             OneOfRoutesOutputTypeResolver.INSTANCE,
                                             OneOfRoutesOutputTypeResolver.INSTANCE),
             new JavaShortHandOutputResolver(AllOfRoutesOutputChainResolver.class,
                                             AllOfRoutesOutputTypeResolver.INSTANCE));

  /**
   * @param annotatedElement an annotated element.
   * @return The {@link JavaShortHandOutputResolver} that applies to the given annotated element, if any.
   */
  public static Optional<JavaShortHandOutputResolver> findShortHandOutputResolver(WithAnnotations annotatedElement) {
    for (JavaShortHandOutputResolver shortHandResolver : SHORT_HAND_OUTPUT_RESOLVERS) {
      if (annotatedElement.getValueFromAnnotation(shortHandResolver.getAnnotation()).isPresent()) {
        return of(shortHandResolver);
      }
    }
    return empty();
  }

  private final Class<? extends Annotation> annotation;
  private final Optional<OutputResolverModelParser> outputResolverModelParser;
  private final Optional<AttributesResolverModelParser> attributesResolverModelParser;

  private JavaShortHandOutputResolver(Class<? extends Annotation> annotation,
                                      OutputTypeResolver<?> outputResolverModelParser,
                                      AttributesTypeResolver<?> attributesResolverModelParser) {
    this.annotation = annotation;
    this.outputResolverModelParser = ofNullable(outputResolverModelParser)
        .map(JavaOutputResolverModelParser::new);
    this.attributesResolverModelParser = ofNullable(attributesResolverModelParser)
        .map(JavaAttributesResolverModelParser::new);
  }

  private JavaShortHandOutputResolver(Class<? extends Annotation> annotation,
                                      OutputTypeResolver<?> outputResolverModelParser) {
    this(annotation, outputResolverModelParser, null);
  }

  private Class<? extends Annotation> getAnnotation() {
    return annotation;
  }

  public Optional<OutputResolverModelParser> getOutputResolverModelParser() {
    return outputResolverModelParser;
  }

  public Optional<AttributesResolverModelParser> getAttributesResolverModelParser() {
    return attributesResolverModelParser;
  }
}
