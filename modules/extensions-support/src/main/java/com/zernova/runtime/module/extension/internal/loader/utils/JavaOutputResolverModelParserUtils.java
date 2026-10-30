/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.utils;

import static com.zernova.runtime.module.extension.internal.loader.parser.java.ZernovaExtensionAnnotationParser.mapReduceSingleAnnotation;
import static com.zernova.runtime.module.extension.internal.loader.utils.JavaMetadataTypeResolverUtils.isStaticResolver;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.JavaShortHandOutputResolver.findShortHandOutputResolver;

import com.zernova.runtime.module.extension.api.loader.java.type.AnnotationValueFetcher;
import com.zernova.runtime.module.extension.api.loader.java.type.MethodElement;
import com.zernova.runtime.module.extension.api.loader.java.type.Type;
import com.zernova.runtime.module.extension.api.loader.java.type.WithAnnotations;
import com.zernova.runtime.extension.api.loader.parser.AttributesResolverModelParser;
import com.zernova.runtime.module.extension.internal.loader.parser.java.JavaShortHandOutputResolver;
import com.zernova.runtime.extension.api.loader.parser.metadata.OutputResolverModelParser;
import com.zernova.runtime.module.extension.internal.loader.parser.java.JavaAttributesResolverModelParser;
import com.zernova.runtime.module.extension.internal.loader.parser.java.metadata.JavaOutputResolverModelParser;
import com.zernova.sdk.api.annotation.metadata.MetadataScope;
import com.zernova.sdk.api.annotation.metadata.OutputResolver;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Function;

/**
 * Helper class for introspecting output and attributes metadata.
 *
 * @since 4.5
 */
public class JavaOutputResolverModelParserUtils {

  public static Optional<OutputResolverModelParser> parseOutputResolverModelParser(MethodElement<?> methodElement) {
    return extractFromAnnotation(methodElement,
                                 "operation",
                                 methodElement.getName(),
                                 com.zernova.runtime.extension.api.annotation.metadata.OutputResolver.class,
                                 OutputResolver.class,
                                 com.zernova.runtime.extension.api.annotation.metadata.OutputResolver::output,
                                 OutputResolver::output,
                                 cls -> getResolverModelParserFromType(cls, JavaOutputResolverModelParser::new),
                                 JavaShortHandOutputResolver::getOutputResolverModelParser);
  }

  public static Optional<OutputResolverModelParser> parseOutputResolverModelParser(Type extensionType,
                                                                                   WithAnnotations annotatedType) {
    return parseSourceResolverModelParser(extensionType, annotatedType,
                                          com.zernova.runtime.extension.api.annotation.metadata.MetadataScope::outputResolver,
                                          MetadataScope::outputResolver,
                                          cls -> getResolverModelParserFromType(cls, JavaOutputResolverModelParser::new));
  }

  public static Optional<AttributesResolverModelParser> parseAttributesResolverModelParser(MethodElement<?> methodElement) {
    return extractFromAnnotation(methodElement,
                                 "operation",
                                 methodElement.getName(),
                                 com.zernova.runtime.extension.api.annotation.metadata.OutputResolver.class,
                                 OutputResolver.class,
                                 com.zernova.runtime.extension.api.annotation.metadata.OutputResolver::attributes,
                                 OutputResolver::attributes,
                                 cls -> getResolverModelParserFromType(cls, JavaAttributesResolverModelParser::new),
                                 JavaShortHandOutputResolver::getAttributesResolverModelParser);
  }

  public static Optional<AttributesResolverModelParser> parseAttributesResolverModelParser(Type extensionType,
                                                                                           WithAnnotations annotatedType) {
    return parseSourceResolverModelParser(extensionType, annotatedType,
                                          com.zernova.runtime.extension.api.annotation.metadata.MetadataScope::attributesResolver,
                                          MetadataScope::attributesResolver,
                                          cls -> getResolverModelParserFromType(cls, JavaAttributesResolverModelParser::new));
  }

  private static <T> Optional<T> parseSourceResolverModelParser(Type extensionType,
                                                                WithAnnotations annotatedType,
                                                                Function<com.zernova.runtime.extension.api.annotation.metadata.MetadataScope, Class> legacyClassValueExtractor,
                                                                Function<MetadataScope, Class> classValueExtractor,
                                                                Function<Class<?>, T> mapper) {
    // Searches for the resolver on the annotated element itself first
    Optional<T> parserFromAnnotatedElement = extractFromAnnotation(annotatedType,
                                                                   "source",
                                                                   "",
                                                                   com.zernova.runtime.extension.api.annotation.metadata.MetadataScope.class,
                                                                   MetadataScope.class,
                                                                   legacyClassValueExtractor,
                                                                   classValueExtractor,
                                                                   mapper,
                                                                   null);
    if (parserFromAnnotatedElement.isPresent()) {
      return parserFromAnnotatedElement;
    }

    // Falls-back to the extension type and searches for a resolver there.
    return extractFromAnnotation(extensionType,
                                 "source",
                                 "",
                                 com.zernova.runtime.extension.api.annotation.metadata.MetadataScope.class,
                                 MetadataScope.class,
                                 legacyClassValueExtractor,
                                 classValueExtractor,
                                 mapper,
                                 null);
  }

  private static <A extends Annotation, L extends Annotation, T> Optional<T> extractFromAnnotation(WithAnnotations methodElement,
                                                                                                   String elementType,
                                                                                                   String elementName,
                                                                                                   Class<L> legacyAnnotationClass,
                                                                                                   Class<A> sdkAnnotationClass,
                                                                                                   Function<L, Class> legacyClassValueExtractor,
                                                                                                   Function<A, Class> classValueExtractor,
                                                                                                   Function<Class<?>, T> mapper,
                                                                                                   Function<JavaShortHandOutputResolver, Optional<T>> shortHandResolverMapper) {
    if (shortHandResolverMapper != null) {
      Optional<T> resolver = findShortHandOutputResolver(methodElement).flatMap(shortHandResolverMapper);
      if (resolver.isPresent()) {
        return resolver;
      }
    }

    return mapReduceSingleAnnotation(methodElement,
                                     elementType,
                                     elementName,
                                     legacyAnnotationClass,
                                     sdkAnnotationClass,
                                     getMappingFunction(legacyClassValueExtractor, mapper),
                                     getMappingFunction(classValueExtractor, mapper));
  }

  private static <A extends Annotation, T> Function<AnnotationValueFetcher<A>, T> getMappingFunction(Function<A, Class> classValueExtractor,
                                                                                                     Function<Class<?>, T> mapper) {
    return fetcher -> fetcher.getClassValue(classValueExtractor).getDeclaringClass().map(mapper).orElse(null);
  }

  private static <T> T getResolverModelParserFromType(Class<?> clazz, Function<Class<?>, T> resolveModelParserFactory) {
    if (!isStaticResolver(clazz)) {
      return resolveModelParserFactory.apply(clazz);
    }
    return null;
  }

}
