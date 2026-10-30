/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.utils;

import static com.zernova.runtime.module.extension.internal.loader.parser.java.utils.ResolvedMinZernovaVersion.FIRST_MULE_VERSION;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.declaration.fluent.HasMinZernovaVersionDeclarer;
import com.zernova.runtime.api.meta.model.declaration.fluent.WithMinZernovaVersionDeclaration;
import com.zernova.runtime.extension.api.annotation.Configurations;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.loader.parser.MinZernovaVersionParser;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.streaming.PagingProvider;
import com.zernova.runtime.module.extension.api.loader.java.type.ConfigurationElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ConnectionProviderElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionParameter;
import com.zernova.runtime.module.extension.api.loader.java.type.FunctionElement;
import com.zernova.runtime.module.extension.api.loader.java.type.MethodElement;
import com.zernova.runtime.module.extension.api.loader.java.type.OperationContainerElement;
import com.zernova.runtime.module.extension.api.loader.java.type.OperationElement;
import com.zernova.runtime.module.extension.api.loader.java.type.SourceElement;
import com.zernova.runtime.module.extension.api.loader.java.type.Type;
import com.zernova.runtime.module.extension.api.loader.java.type.TypeGeneric;
import com.zernova.runtime.module.extension.api.loader.java.type.WithAnnotations;
import com.zernova.sdk.api.annotation.DoNotEnforceMinZernovaVersion;
import com.zernova.sdk.api.annotation.Extension;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;

import jakarta.inject.Inject;

/**
 * Utils class to create {@link ResolvedMinZernovaVersion}s from {@link Type}s.
 *
 * @since 4.5
 */
public final class MinZernovaVersionUtils {

  private static final Logger LOGGER = getLogger(MinZernovaVersionUtils.class);

  private static final ZernovaVersion SDK_EXTENSION_ANNOTATION_MIN_MULE_VERSION =
      new ZernovaVersion(Extension.class.getAnnotation(MinZernovaVersion.class).value());

  private MinZernovaVersionUtils() {}

  /**
   * Calculates the minimum {@link ZernovaVersion} for a given {@link ExtensionElement} by looking at the class annotation
   * {@link MinZernovaVersion} (if present) and at the new SDK API components it interacts with through its annotations, fields and
   * methods.
   *
   * @param extension the extension to calculate its min mule version
   * @return the minimum mule version of the given extension
   */
  public static ResolvedMinZernovaVersion resolveExtensionMinZernovaVersion(Type extension) {
    if (belongsToJavaPackages(extension.getTypeName())) {
      return resolveToDefaultMMV("Java type", extension.getTypeName());
    }
    ResolvedMinZernovaVersion extensionMMV;
    if (extension.isAnnotatedWith(Extension.class)) {
      extensionMMV = new ResolvedMinZernovaVersion(extension.getName(), SDK_EXTENSION_ANNOTATION_MIN_MULE_VERSION,
                                                "Extension " + extension.getName() + " has min mule version "
                                                    + SDK_EXTENSION_ANNOTATION_MIN_MULE_VERSION
                                                    + " because it if annotated with the new sdk api @Extension.");
    } else {
      extensionMMV = resolveToDefaultMMV("Extension", extension.getName());
    }
    Optional<ResolvedMinZernovaVersion> superExtensionMMV =
        extension.getSuperType().map(MinZernovaVersionUtils::resolveExtensionMinZernovaVersion);
    superExtensionMMV.ifPresent(resolvedMMV -> extensionMMV
        .updateIfHigherMMV(resolvedMMV, getReasonSuperClass("Extension", extension.getName(), resolvedMMV)));
    if (!(extension.isAnnotatedWith(Configurations.class)
        || extension.isAnnotatedWith(com.zernova.sdk.api.annotation.Configurations.class))) {
      Optional<ResolvedMinZernovaVersion> fieldMMV =
          extension.getFields().stream().map(f -> calculateFieldMinZernovaVersion(f, new HashSet<>()))
              .reduce(MinZernovaVersionUtils::max);
      fieldMMV.ifPresent(resolvedMMV -> extensionMMV
          .updateIfHigherMMV(resolvedMMV, getReasonField("Extension", extension.getName(), resolvedMMV)));
      Optional<ResolvedMinZernovaVersion> methodMMV =
          extension.getEnclosingMethods().map(MinZernovaVersionUtils::calculateMethodMinZernovaVersion)
              .reduce(MinZernovaVersionUtils::max);
      methodMMV.ifPresent(resolvedMMV -> extensionMMV
          .updateIfHigherMMV(resolvedMMV, getReasonMethod("Extension", extension.getName(), resolvedMMV)));
    }
    Optional<ZernovaVersion> classLevelMMV = getMinZernovaVersionFromAnnotations(extension);
    if (classLevelMMV.isPresent()) {
      if (classLevelMMV.get().priorTo(extensionMMV.getMinZernovaVersion())) {
        return new ResolvedMinZernovaVersion(extension.getName(), extensionMMV.getMinZernovaVersion(),
                                          getReasonOverride(extensionMMV.getMinZernovaVersion().toString(), "extension class",
                                                            classLevelMMV.get().toString(), extensionMMV.getReason()));
      } else {
        return new ResolvedMinZernovaVersion(extension.getName(), classLevelMMV.get(),
                                          getReasonClassLevelMMV("Extension", extension.getName(), classLevelMMV.get()));
      }
    }
    return extensionMMV;
  }

  /**
   * Calculates the minimum {@link ZernovaVersion} for a given {@link ConfigurationElement} by looking at the class annotation
   * {@link MinZernovaVersion} (if present) and at the new SDK API components it interacts with through its annotations, fields,
   * methods and class inheritance.
   *
   * @param config                   the configuration, as a {@link Type}, to calculate its min mule version
   * @param propagatedMinZernovaVersion the min mule version propagated from the config's container
   * @return the minimum mule version of the given configuration
   */
  public static ResolvedMinZernovaVersion resolveConfigurationMinZernovaVersion(Type config, ZernovaVersion propagatedMinZernovaVersion) {
    if (belongsToJavaPackages(config.getTypeName())) {
      return resolveToDefaultMMV("Java type", config.getTypeName());
    }
    ResolvedMinZernovaVersion configMMV = max(resolveToDefaultMMV("Configuration", config.getName()),
                                           new ResolvedMinZernovaVersion(config.getName(), propagatedMinZernovaVersion, "Configuration "
                                               + config.getName() + " has min mule version " + propagatedMinZernovaVersion
                                               + " because it was propagated from the annotation (either @Configurations or @Config) used to reference this configuration."));
    Optional<Type> superType = config.getSuperType();
    if (superType.isPresent()) {
      ResolvedMinZernovaVersion superConfigMMV = resolveConfigurationMinZernovaVersion(superType.get(), propagatedMinZernovaVersion);
      configMMV.updateIfHigherMMV(superConfigMMV, getReasonSuperClass("Configuration", config.getName(), superConfigMMV));
    }
    Optional<ResolvedMinZernovaVersion> annotationMMV =
        config.getAnnotations().map(MinZernovaVersionUtils::getEnforcedMinZernovaVersion)
            .reduce(MinZernovaVersionUtils::max);
    annotationMMV.ifPresent(resolvedMMV -> configMMV
        .updateIfHigherMMV(resolvedMMV, getReasonAnnotated("Configuration", config.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> fieldMMV =
        config.getFields().stream().map(f -> calculateFieldMinZernovaVersion(f, new HashSet<>()))
            .reduce(MinZernovaVersionUtils::max);
    fieldMMV.ifPresent(resolvedMMV -> configMMV
        .updateIfHigherMMV(resolvedMMV, getReasonField("Configuration", config.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> methodMMV =
        config.getEnclosingMethods().map(MinZernovaVersionUtils::calculateMethodMinZernovaVersion)
            .reduce(MinZernovaVersionUtils::max);
    methodMMV.ifPresent(resolvedMMV -> configMMV
        .updateIfHigherMMV(resolvedMMV, getReasonMethod("Configuration", config.getName(), resolvedMMV)));
    Optional<ZernovaVersion> classLevelMMV = getMinZernovaVersionFromAnnotations(config);
    if (classLevelMMV.isPresent()) {
      if (classLevelMMV.get().priorTo(configMMV.getMinZernovaVersion())) {
        return new ResolvedMinZernovaVersion(config.getName(), configMMV.getMinZernovaVersion(),
                                          getReasonOverride(configMMV.getMinZernovaVersion().toString(), "configuration class",
                                                            classLevelMMV.get().toString(), configMMV.getReason()));
      } else {
        return new ResolvedMinZernovaVersion(config.getName(), classLevelMMV.get(),
                                          getReasonClassLevelMMV("Configuration", config.getName(), classLevelMMV.get()));
      }
    }
    return configMMV;
  }

  /**
   * Calculates the minimum {@link ZernovaVersion} for a given {@link FunctionElement} by looking at the annotation
   * {@link MinZernovaVersion} (if present) and at the new SDK API components it interacts with through its annotations, parameters
   * and return type.
   *
   * @param function                 the function, as a {@link MethodElement}, to calculate its min mule version
   * @param propagatedMinZernovaVersion the min mule version propagated from the function's container
   * @return the minimum mule version of the given function
   */
  public static ResolvedMinZernovaVersion resolveFunctionMinZernovaVersion(MethodElement<?> function,
                                                                     ZernovaVersion propagatedMinZernovaVersion) {
    ResolvedMinZernovaVersion resolvedMMV =
        max(resolveToDefaultMMV("Function", function.getName()),
            new ResolvedMinZernovaVersion(function.getName(), propagatedMinZernovaVersion, "Function " + function.getName()
                + " has min mule version " + propagatedMinZernovaVersion
                + " because it was propagated from the @Functions annotation at the extension class used to add the function."));
    resolvedMMV = max(resolvedMMV, calculateMethodMinZernovaVersion(function));
    return resolvedMMV;
  }

  /**
   * Calculates the minimum {@link ZernovaVersion} for a given {@link OperationElement} by looking at the annotation
   * {@link MinZernovaVersion} (if present) and at the new SDK API components it interacts with through its annotations, parameters
   * and return type.
   *
   * @param operation                the operation, as a {@link MethodElement}, to calculate its min mule version
   * @param propagatedMinZernovaVersion the min mule version propagated from the operation's container
   * @return the minimum mule version of the given operation
   */
  public static ResolvedMinZernovaVersion resolveOperationMinZernovaVersion(MethodElement<?> operation,
                                                                      OperationContainerElement operationContainer,
                                                                      ZernovaVersion propagatedMinZernovaVersion) {
    ResolvedMinZernovaVersion operationMMV = resolveToDefaultMMV("Operation", operation.getName());
    operationMMV = max(operationMMV, new ResolvedMinZernovaVersion(operation.getName(), propagatedMinZernovaVersion, "Operation "
        + operation.getName() + " has min mule version " + propagatedMinZernovaVersion
        + " because it was propagated from the @Operations annotation at the extension class used to add the operation's container "
        + operationContainer.getName() + "."));
    Optional<ResolvedMinZernovaVersion> parameterMMV = operationContainer.getParameters().stream()
        .map(MinZernovaVersionUtils::calculateMethodParameterMinZernovaVersion)
        .reduce(MinZernovaVersionUtils::max);
    if (parameterMMV.isPresent()) {
      operationMMV.updateIfHigherMMV(parameterMMV.get(),
                                     getReasonParameter("Operation", operation.getName(), parameterMMV.get()));
    }
    operationMMV = max(operationMMV, calculateMethodMinZernovaVersion(operation));
    return operationMMV;
  }

  /**
   * Calculates the minimum {@link ZernovaVersion} for a given {@link ConnectionProviderElement} by looking at the annotation
   * {@link MinZernovaVersion} (if present) and at the new SDK API components it interacts with through its annotations, fields and
   * class inheritance.
   *
   * @param connectionProvider the connection provider, as a {@link Type}, to calculate its min mule version
   * @return the minimum mule version of the given connection provider
   */
  public static ResolvedMinZernovaVersion resolveConnectionProviderMinZernovaVersion(Type connectionProvider) {
    if (belongsToJavaPackages(connectionProvider.getTypeName())) {
      return resolveToDefaultMMV("Java type", connectionProvider.getTypeName());
    }
    ResolvedMinZernovaVersion connectionProviderMMV =
        resolveToDefaultMMV("Connection Provider", connectionProvider.getName());
    Optional<Type> superType = connectionProvider.getSuperType();
    if (superType.isPresent()) {
      if (superType.get().isAssignableTo(ConnectionProvider.class)
          || superType.get().isAssignableTo(com.zernova.sdk.api.connectivity.ConnectionProvider.class)) {
        ResolvedMinZernovaVersion superTypeMMV = resolveConnectionProviderMinZernovaVersion(superType.get());
        connectionProviderMMV
            .updateIfHigherMMV(superTypeMMV,
                               getReasonSuperClass("Connection Provider", connectionProvider.getName(), superTypeMMV));
      }
    }
    Optional<ResolvedMinZernovaVersion> interfaceMMV = connectionProvider.getImplementingInterfaces()
        .map(MinZernovaVersionUtils::calculateInterfaceMinZernovaVersion).reduce(MinZernovaVersionUtils::max);
    interfaceMMV.ifPresent(resolvedMMV -> connectionProviderMMV
        .updateIfHigherMMV(resolvedMMV, getReasonInterface("Connection Provider", connectionProvider.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> annotationMMV =
        connectionProvider.getAnnotations().map(MinZernovaVersionUtils::getEnforcedMinZernovaVersion)
            .reduce(MinZernovaVersionUtils::max);
    annotationMMV.ifPresent(resolvedMMV -> connectionProviderMMV
        .updateIfHigherMMV(resolvedMMV, getReasonAnnotated("Connection Provider", connectionProvider.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> fieldMMV =
        connectionProvider.getFields().stream().map(f -> calculateFieldMinZernovaVersion(f, new HashSet<>()))
            .reduce(MinZernovaVersionUtils::max);
    fieldMMV.ifPresent(resolvedMMV -> connectionProviderMMV
        .updateIfHigherMMV(resolvedMMV, getReasonField("Connection Provider", connectionProvider.getName(), resolvedMMV)));
    Optional<ZernovaVersion> classLevelMMV = getMinZernovaVersionFromAnnotations(connectionProvider);
    if (classLevelMMV.isPresent()) {
      if (classLevelMMV.get().priorTo((connectionProviderMMV.getMinZernovaVersion()))) {
        return new ResolvedMinZernovaVersion(connectionProvider.getName(), connectionProviderMMV.getMinZernovaVersion(),
                                          getReasonOverride(connectionProviderMMV.getMinZernovaVersion().toString(),
                                                            "connection provider class", classLevelMMV.get().toString(),
                                                            connectionProviderMMV.getReason()));
      } else {
        return new ResolvedMinZernovaVersion(connectionProvider.getName(), classLevelMMV
            .get(), getReasonClassLevelMMV("Connection Provider", connectionProvider.getName(), classLevelMMV.get()));
      }
    }
    return connectionProviderMMV;
  }

  /**
   * Calculates the minimum {@link ZernovaVersion} for a given {@link SourceElement} by looking at the annotation
   * {@link MinZernovaVersion} (if present) and at the new SDK API components it interacts with through its annotations, generics,
   * fields, methods and class inheritance.
   *
   * @param source the connection provider, as a {@link Type}, to calculate its min mule version
   * @return the minimum mule version of the given source
   */
  public static ResolvedMinZernovaVersion resolveSourceMinZernovaVersion(SourceElement source) {
    ResolvedMinZernovaVersion genericsMMV = calculateSourceGenericsMinZernovaVersion(source.getSuperClassGenerics());
    ResolvedMinZernovaVersion sourceMMV = calculateSourceMinZernovaVersion(source);
    sourceMMV.updateIfHigherMMV(genericsMMV, "Source " + source.getName() + " has min mule version "
        + genericsMMV.getMinZernovaVersion() + " because it has a generic of type " + genericsMMV.getName() + ".");
    return sourceMMV;
  }

  private static ResolvedMinZernovaVersion calculateSourceMinZernovaVersion(Type source) {
    if (belongsToJavaPackages(source.getTypeName())) {
      return resolveToDefaultMMV("Java type", source.getTypeName());
    }
    ResolvedMinZernovaVersion sourceMMV = resolveToDefaultMMV("Source", source.getName());
    Optional<Type> superType = source.getSuperType();
    if (superType.isPresent()) {
      ResolvedMinZernovaVersion superTypeMMV;
      if (superType.get().isSameType(Source.class) || superType.get().isSameType(com.zernova.sdk.api.runtime.source.Source.class)) {
        superTypeMMV = getEnforcedMinZernovaVersion(superType.get());
      } else {
        superTypeMMV = calculateSourceMinZernovaVersion(superType.get());
      }
      sourceMMV.updateIfHigherMMV(superTypeMMV, getReasonSuperClass("Source", source.getName(), superTypeMMV));
    }
    Optional<ResolvedMinZernovaVersion> interfaceMMV =
        source.getImplementingInterfaces().map(MinZernovaVersionUtils::calculateInterfaceMinZernovaVersion)
            .reduce(MinZernovaVersionUtils::max);
    interfaceMMV.ifPresent(resolvedMMV -> sourceMMV
        .updateIfHigherMMV(resolvedMMV, getReasonInterface("Source", source.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> annotationMMV =
        source.getAnnotations().map(MinZernovaVersionUtils::getEnforcedMinZernovaVersion)
            .reduce(MinZernovaVersionUtils::max);
    annotationMMV.ifPresent(resolvedMMV -> sourceMMV
        .updateIfHigherMMV(resolvedMMV, getReasonAnnotated("Source", source.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> fieldMMV =
        source.getFields().stream().map(f -> calculateFieldMinZernovaVersion(f, new HashSet<>()))
            .reduce(MinZernovaVersionUtils::max);
    fieldMMV.ifPresent(resolvedMMV -> sourceMMV.updateIfHigherMMV(resolvedMMV,
                                                                  getReasonField("Source", source.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> methodMMV =
        source.getEnclosingMethods().map(MinZernovaVersionUtils::calculateMethodMinZernovaVersion)
            .reduce(MinZernovaVersionUtils::max);
    methodMMV.ifPresent(resolvedMMV -> sourceMMV.updateIfHigherMMV(resolvedMMV,
                                                                   getReasonMethod("Source", source.getName(), resolvedMMV)));
    Optional<ZernovaVersion> classLevelMMV = getMinZernovaVersionFromAnnotations(source);
    if (classLevelMMV.isPresent()) {
      if (classLevelMMV.get().priorTo(sourceMMV.getMinZernovaVersion())) {
        return new ResolvedMinZernovaVersion(source.getName(), sourceMMV.getMinZernovaVersion(),
                                          getReasonOverride(sourceMMV.getMinZernovaVersion().toString(), "source class",
                                                            classLevelMMV.get().toString(), sourceMMV.getReason()));
      } else {
        return new ResolvedMinZernovaVersion(source.getName(), classLevelMMV.get(),
                                          getReasonClassLevelMMV("Source", source.getName(), classLevelMMV.get()));
      }
    }
    return sourceMMV;
  }

  /**
   * For a given sdk component, it checks in its containing element if the component was declared in the given annotation, and, if
   * true, returns the annotation's {@link MinZernovaVersion}.
   *
   * @param element          the component to look for
   * @param containerElement the containing element where the component is declared through an annotation
   * @param annotationClass  the annotation class to check
   * @param mapper           a function to extract the values of the annotation class
   * @return the min mule version of the container annotation, if the component was declared there, otherwise a 4.1.1 Mule
   *         Version.
   */
  public static <A extends Annotation> ZernovaVersion getContainerAnnotationMinZernovaVersion(Type containerElement,
                                                                                        Class<A> annotationClass,
                                                                                        Function<A, Class[]> mapper,
                                                                                        Type element) {
    List<Type> sdkConfigurations =
        containerElement.getValueFromAnnotation(annotationClass).map(vf -> vf.getClassArrayValue(mapper)).orElse(emptyList());
    if (sdkConfigurations.stream().anyMatch(element::isSameType)) {
      return new ZernovaVersion(annotationClass.getAnnotation(MinZernovaVersion.class).value());
    }
    return FIRST_MULE_VERSION;
  }

  private static ResolvedMinZernovaVersion calculateMethodMinZernovaVersion(MethodElement<?> method) {
    ResolvedMinZernovaVersion methodMMV = resolveToDefaultMMV("Method", method.getName());
    Optional<ResolvedMinZernovaVersion> annotationMMV =
        method.getAnnotations().map(MinZernovaVersionUtils::getEnforcedMinZernovaVersion)
            .reduce(MinZernovaVersionUtils::max);
    annotationMMV.ifPresent(resolvedMMV -> methodMMV
        .updateIfHigherMMV(resolvedMMV, getReasonAnnotated("Method", method.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> parameterMMV =
        method.getParameters().stream().map(MinZernovaVersionUtils::calculateMethodParameterMinZernovaVersion)
            .reduce(MinZernovaVersionUtils::max);
    parameterMMV.ifPresent(resolvedMMV -> methodMMV
        .updateIfHigherMMV(resolvedMMV, getReasonParameter("Method", method.getName(), resolvedMMV)));
    ResolvedMinZernovaVersion outputMMV = calculateOutputMinZernovaVersion(method.getReturnType());
    methodMMV.updateIfHigherMMV(outputMMV, "Method " + method.getName() + " has min mule version " + outputMMV.getMinZernovaVersion()
        + " because of its output type " + outputMMV.getName() + ".");
    Optional<ZernovaVersion> operationLevelMMV = getMinZernovaVersionFromAnnotations(method);
    if (operationLevelMMV.isPresent()) {
      if (operationLevelMMV.get().priorTo(methodMMV.getMinZernovaVersion())) {
        return new ResolvedMinZernovaVersion(method.getName(), methodMMV.getMinZernovaVersion(),
                                          getReasonOverride(methodMMV.getMinZernovaVersion().toString(), "method",
                                                            operationLevelMMV.get().toString(), methodMMV.getReason()));
      } else {
        return new ResolvedMinZernovaVersion(method.getName(), operationLevelMMV.get(), "Method " + method.getName()
            + " has min mule version " + operationLevelMMV.get()
            + " because it is the one set at the method level through the @MinZernovaVersion annotation.");
      }
    }
    return methodMMV;
  }

  private static ResolvedMinZernovaVersion calculateInterfaceMinZernovaVersion(Type interfaceType) {
    ResolvedMinZernovaVersion interfaceMMV = getEnforcedMinZernovaVersion(interfaceType);
    if (belongsToSdkPackages(interfaceType.getTypeName())) {
      return interfaceMMV;
    }
    Optional<ResolvedMinZernovaVersion> superInterface = interfaceType.getImplementingInterfaces()
        .map(MinZernovaVersionUtils::calculateInterfaceMinZernovaVersion).reduce(MinZernovaVersionUtils::max);
    superInterface.ifPresent(resolvedMMV -> interfaceMMV
        .updateIfHigherMMV(resolvedMMV, getReasonInterface("Interface", interfaceType.getName(), resolvedMMV)));
    return interfaceMMV;
  }

  private static ResolvedMinZernovaVersion calculateSourceGenericsMinZernovaVersion(List<Type> sourceGenerics) {
    Type outputType = sourceGenerics.get(0);
    Type attributesType = sourceGenerics.get(1);
    return max(getEnforcedMinZernovaVersion(attributesType), calculateOutputMinZernovaVersion(outputType));
  }

  private static ResolvedMinZernovaVersion calculateOutputMinZernovaVersion(Type outputType) {
    ResolvedMinZernovaVersion resolvedMMV = resolveToDefaultMMV("Output type", outputType.getName());
    if (outputType.isArray()) {
      resolvedMMV = max(resolvedMMV, calculateOutputMinZernovaVersion(outputType.getArrayComponentType().get()));
    } else if (outputType.isAssignableTo(Iterable.class) || outputType.isAssignableTo(Iterator.class)) {
      for (TypeGeneric typeGeneric : outputType.getGenerics()) {
        resolvedMMV = max(resolvedMMV, calculateOutputMinZernovaVersion(typeGeneric.getConcreteType()));
      }
    } else if (outputType.isSameType(Result.class) || outputType.isSameType(com.zernova.sdk.api.runtime.operation.Result.class)) {
      resolvedMMV = max(resolvedMMV, getEnforcedMinZernovaVersion(outputType));
      for (TypeGeneric MMVTypeGeneric : outputType.getGenerics()) {
        resolvedMMV = max(resolvedMMV, getEnforcedMinZernovaVersion(MMVTypeGeneric.getConcreteType()));
      }
    } else if (outputType.isSameType(PagingProvider.class)
        || outputType.isSameType(com.zernova.sdk.api.runtime.streaming.PagingProvider.class)) {
      resolvedMMV = max(resolvedMMV, getEnforcedMinZernovaVersion(outputType));
      resolvedMMV =
          max(resolvedMMV, getEnforcedMinZernovaVersion(outputType.getGenerics().get(1).getConcreteType()));
    } else {
      resolvedMMV = getEnforcedMinZernovaVersion(outputType);
    }
    return resolvedMMV;
  }

  private static ResolvedMinZernovaVersion calculateFieldMinZernovaVersion(ExtensionParameter field,
                                                                     Set<String> seenTypesForRecursionControl) {
    Type parameterType = field.getType();
    if (seenTypesForRecursionControl.contains(parameterType.getTypeName())) {
      // If it is a recursive type we only look at its annotations and cut the recursion.
      ResolvedMinZernovaVersion recursiveFieldMMV = resolveToDefaultMMV("Field", field.getName());
      Optional<ResolvedMinZernovaVersion> annotationMMV =
          field.getAnnotations().map(MinZernovaVersionUtils::getEnforcedMinZernovaVersion)
              .reduce(MinZernovaVersionUtils::max);
      annotationMMV.ifPresent(resolvedMMV -> recursiveFieldMMV
          .updateIfHigherMMV(resolvedMMV, getReasonAnnotated("Field", field.getName(), resolvedMMV)));
      return recursiveFieldMMV;
    } else {
      seenTypesForRecursionControl.add(parameterType.getTypeName());
    }
    ResolvedMinZernovaVersion fieldMinZernovaVersion = getMinZernovaVersionFromAnnotations(field)
        .map(mmv -> new ResolvedMinZernovaVersion(field.getName(), mmv,
                                               "Field " + field.getName() + " has min mule version " + mmv
                                                   + " because it is annotated with @MinZernovaVersion."))
        .orElse(resolveToDefaultMMV("Field", field.getName()));
    // Parse sdk fields (this also catches automatically injected fields)
    if (belongsToSdkPackages(parameterType.getTypeName())) {
      ResolvedMinZernovaVersion typeMMV = getEnforcedMinZernovaVersion(parameterType);
      fieldMinZernovaVersion.updateIfHigherMMV(typeMMV, getReasonType("Field", field.getName(), typeMMV));
    }
    for (Type annotation : field.getAnnotations().collect(toList())) {
      calculateFieldAnnotationMinZernovaVersion(field, seenTypesForRecursionControl, parameterType, fieldMinZernovaVersion, annotation);
    }
    return fieldMinZernovaVersion;
  }

  private static void calculateFieldAnnotationMinZernovaVersion(ExtensionParameter field, Set<String> seenTypesForRecursionControl,
                                                             Type parameterType, ResolvedMinZernovaVersion fieldMinZernovaVersion,
                                                             Type annotation) {
    if ((annotation.isSameType(Inject.class)
        // Still need to support javax.inject for the time being...
        || annotation.isSameType(javax.inject.Inject.class))
        && !parameterType.isSameType(Optional.class)) {
      // Parse injected classes but exclude Optionals (such as ForwardCompatibilityHelper)
      ResolvedMinZernovaVersion typeMMV = getEnforcedMinZernovaVersion(parameterType);
      fieldMinZernovaVersion.updateIfHigherMMV(typeMMV, getReasonType("Field", field.getName(), typeMMV));
    }
    if (isParameterOrParameterGroup(annotation)) {
      ResolvedMinZernovaVersion parameterContainerMMV =
          calculateParameterTypeMinZernovaVersion(parameterType, seenTypesForRecursionControl);
      fieldMinZernovaVersion.updateIfHigherMMV(parameterContainerMMV,
                                            getReasonFieldType(field.getName(),
                                                               parameterContainerMMV.getMinZernovaVersion().toString(),
                                                               "parameter", parameterContainerMMV.getName()));
    }
    if (annotation.isSameType(Connection.class) || annotation.isSameType(com.zernova.sdk.api.annotation.param.Connection.class)) {
      // Sources inject the ConnectionProvider instead of the connection
      if (parameterType.isAssignableTo(ConnectionProvider.class)
          || parameterType.isAssignableTo(com.zernova.sdk.api.connectivity.ConnectionProvider.class)) {
        ResolvedMinZernovaVersion connectionProviderMMV = resolveConnectionProviderMinZernovaVersion(parameterType);
        fieldMinZernovaVersion.updateIfHigherMMV(connectionProviderMMV,
                                              getReasonFieldType(field.getName(),
                                                                 connectionProviderMMV.getMinZernovaVersion().toString(),
                                                                 "connection provider", connectionProviderMMV.getName()));
      }
    }
    if (annotation.isSameType(Config.class) || annotation.isSameType(com.zernova.sdk.api.annotation.param.Config.class)) {
      ResolvedMinZernovaVersion configurationMMV =
          resolveConfigurationMinZernovaVersion(parameterType, getEnforcedMinZernovaVersion(annotation).getMinZernovaVersion());
      fieldMinZernovaVersion.updateIfHigherMMV(configurationMMV,
                                            getReasonFieldType(field.getName(), configurationMMV.getMinZernovaVersion().toString(),
                                                               "configuration", configurationMMV.getName()));
    }
    ResolvedMinZernovaVersion annotationMMV = getEnforcedMinZernovaVersion(annotation);
    fieldMinZernovaVersion.updateIfHigherMMV(annotationMMV, getReasonAnnotated("Field", field.getName(), annotationMMV));
  }

  private static ResolvedMinZernovaVersion calculateMethodParameterMinZernovaVersion(ExtensionParameter methodParameter) {
    ResolvedMinZernovaVersion methodParameterMMV = resolveToDefaultMMV("Parameter", methodParameter.getName());
    Optional<ResolvedMinZernovaVersion> configAnnotationMMV =
        methodParameter.getAnnotations()
            .filter(a -> a.isAssignableTo(Config.class) || a.isAssignableTo(com.zernova.sdk.api.annotation.param.Config.class))
            .findFirst().map(MinZernovaVersionUtils::getEnforcedMinZernovaVersion);
    if (configAnnotationMMV.isPresent()) {
      ResolvedMinZernovaVersion configMMV =
          resolveConfigurationMinZernovaVersion(methodParameter.getType(), configAnnotationMMV.get().getMinZernovaVersion());
      methodParameterMMV
          .updateIfHigherMMV(configMMV, "Parameter " + methodParameter.getName() + " has min mule version "
              + configMMV.getMinZernovaVersion() + " because it references a config of type " + configMMV.getName() + ".");
    }
    Optional<ResolvedMinZernovaVersion> annotationMMV =
        methodParameter.getAnnotations().map(MinZernovaVersionUtils::getEnforcedMinZernovaVersion).reduce(MinZernovaVersionUtils::max);
    annotationMMV.ifPresent(resolvedMMV -> methodParameterMMV
        .updateIfHigherMMV(resolvedMMV, getReasonAnnotated("Parameter", methodParameter.getName(), resolvedMMV)));
    ResolvedMinZernovaVersion parameterTypeMMV;
    // Parse sdk fields (this also catches automatically injected fields)
    if (belongsToSdkPackages(methodParameter.getType().getTypeName())) {
      parameterTypeMMV = getEnforcedMinZernovaVersion(methodParameter.getType());
    } else { // The parameter is of custom type
      if (methodParameter.getAnnotations()
          .noneMatch(a -> a.isSameType(Connection.class) || a.isSameType(com.zernova.sdk.api.annotation.param.Connection.class))) {
        parameterTypeMMV = calculateParameterTypeMinZernovaVersion(methodParameter.getType(), new HashSet<>());
      } else {
        parameterTypeMMV = resolveToDefaultMMV("Type", methodParameter.getType().getName());
      }
    }
    methodParameterMMV.updateIfHigherMMV(parameterTypeMMV,
                                         getReasonType("Parameter", methodParameter.getName(), parameterTypeMMV));
    return methodParameterMMV;
  }

  private static ResolvedMinZernovaVersion calculateParameterTypeMinZernovaVersion(Type parameterType,
                                                                             Set<String> seenTypesForRecursionControl) {
    Optional<ZernovaVersion> minZernovaVersionAnnotation = getMinZernovaVersionFromAnnotations(parameterType);
    if (minZernovaVersionAnnotation.isPresent()) {
      String reason = "Type " + parameterType.getName() + " has min mule version " + minZernovaVersionAnnotation.get()
          + " because it is annotated with @MinZernovaVersion.";
      return new ResolvedMinZernovaVersion(parameterType.getName(), minZernovaVersionAnnotation.get(), reason);
    }
    ResolvedMinZernovaVersion parameterMMV = resolveToDefaultMMV("Type", parameterType.getName());

    Optional<ResolvedMinZernovaVersion> fieldMMV = empty();
    // TODO W-14749120 - properly handle provided types that are missing
    try {
      fieldMMV = parameterType.getFields().stream()
          .filter(MinZernovaVersionUtils::isAnnotatedWithParameterOrParameterGroup)
          .map(f -> calculateFieldMinZernovaVersion(f, seenTypesForRecursionControl)).reduce(MinZernovaVersionUtils::max);
    } catch (Throwable t) {
      if (isLinkageError(t)) {
        LOGGER
            .warn(format("Skipping MMV calculation for fields of type '%s', at least one of the fields' types couldn't be loaded",
                         parameterType.getName()));
      } else {
        throw t;
      }
    }

    fieldMMV.ifPresent(resolvedMMV -> parameterMMV
        .updateIfHigherMMV(resolvedMMV, getReasonField("Type", parameterType.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> annotationMMV =
        parameterType.getAnnotations().map(MinZernovaVersionUtils::getEnforcedMinZernovaVersion)
            .reduce(MinZernovaVersionUtils::max);
    annotationMMV.ifPresent(resolvedMMV -> parameterMMV
        .updateIfHigherMMV(resolvedMMV, getReasonAnnotated("Type", parameterType.getName(), resolvedMMV)));
    return parameterMMV;
  }

  public static void declarerWithMmv(HasMinZernovaVersionDeclarer<?> declarer, MinZernovaVersionParser resolvedMMV) {
    declarer.withMinZernovaVersion(resolvedMMV.getMinZernovaVersion());
    LOGGER.debug(resolvedMMV.getReason());
  }

  public static void declarationWithMmv(WithMinZernovaVersionDeclaration declaration, MinZernovaVersionParser resolvedMMV) {
    declaration.withMinZernovaVersion(resolvedMMV.getMinZernovaVersion());
    LOGGER.debug(resolvedMMV.getReason());
  }

  private static boolean isLinkageError(Throwable t) {
    Set<Throwable> seen = new HashSet<>();
    while (t != null && seen.add(t)) {
      if (t instanceof LinkageError) {
        return true;
      }

      t = t.getCause();
    }

    return false;
  }

  private static ResolvedMinZernovaVersion getEnforcedMinZernovaVersion(Type type) {
    if (type.isAnnotatedWith(DoNotEnforceMinZernovaVersion.class)) {
      return new ResolvedMinZernovaVersion(type.getName(), FIRST_MULE_VERSION,
                                        type.getName() + " has the default base min mule version " + FIRST_MULE_VERSION
                                            + " because it is annotated with @DoNotEnforceMinZernovaVersion.");
    }
    Optional<ZernovaVersion> mmv = getMinZernovaVersionFromAnnotations(type);
    return mmv
        .map(muleVersion -> new ResolvedMinZernovaVersion(type.getName(), muleVersion,
                                                       type.getName() + " was introduced in Mule " + muleVersion + "."))
        .orElseGet(() -> resolveToDefaultMMV("Type", type.getName()));
  }

  private static Optional<ZernovaVersion> getMinZernovaVersionFromAnnotations(WithAnnotations type) {
    return type.getValueFromAnnotation(MinZernovaVersion.class)
        .map(fetcher -> new ZernovaVersion(fetcher.getStringValue(MinZernovaVersion::value)));
  }

  private static ResolvedMinZernovaVersion max(ResolvedMinZernovaVersion currentMax, ResolvedMinZernovaVersion candidate) {
    if (currentMax.getMinZernovaVersion().atLeast(candidate.getMinZernovaVersion())) {
      return currentMax;
    }
    return candidate;
  }

  private static ResolvedMinZernovaVersion resolveToDefaultMMV(String componentDescription, String componentName) {
    return new ResolvedMinZernovaVersion(componentName, FIRST_MULE_VERSION, componentDescription + " " + componentName
        + " has min mule version " + FIRST_MULE_VERSION + " because it is the default value.");
  }

  private static boolean isParameterOrParameterGroup(Type annotation) {
    return (annotation.isSameType(Parameter.class)
        || annotation.isSameType(com.zernova.sdk.api.annotation.param.Parameter.class)
        || annotation.isSameType(ParameterGroup.class)
        || annotation.isSameType(com.zernova.sdk.api.annotation.param.ParameterGroup.class));
  }

  private static boolean isAnnotatedWithParameterOrParameterGroup(WithAnnotations type) {
    return (type.isAnnotatedWith(Parameter.class)
        || type.isAnnotatedWith(com.zernova.sdk.api.annotation.param.Parameter.class)
        || type.isAnnotatedWith(ParameterGroup.class)
        || type.isAnnotatedWith(com.zernova.sdk.api.annotation.param.ParameterGroup.class));
  }

  private static boolean belongsToSdkPackages(String fullyQualifiedName) {
    return belongsToExtensionsApiPackages(fullyQualifiedName) || belongsToSdkApiPackages(fullyQualifiedName);
  }

  private static boolean belongsToExtensionsApiPackages(String fullyQualifiedName) {
    return fullyQualifiedName.startsWith("com.zernova.runtime.extension.api");
  }

  private static boolean belongsToSdkApiPackages(String fullyQualifiedName) {
    return fullyQualifiedName.startsWith("com.zernova.sdk.api");
  }

  private static boolean belongsToJavaPackages(String fullyQualifiedName) {
    return fullyQualifiedName.startsWith("java.");
  }

  private static String getReasonAnnotated(String component, String name, ResolvedMinZernovaVersion annotationMMV) {
    return component + " " + name + " has min mule version " + annotationMMV.getMinZernovaVersion()
        + " because it is annotated with "
        + annotationMMV.getName() + ".";
  }

  private static String getReasonSuperClass(String component, String name, ResolvedMinZernovaVersion superClassMMV) {
    return component + " " + name + " has min mule version " + superClassMMV.getMinZernovaVersion() + " because of its super class "
        + superClassMMV.getName() + ".";
  }

  private static String getReasonField(String component, String name, ResolvedMinZernovaVersion fieldMMV) {
    return component + " " + name + " has min mule version " + fieldMMV.getMinZernovaVersion() + " because of its field "
        + fieldMMV.getName() + ".";
  }


  private static String getReasonFieldType(String fieldName, String mmv, String componentType, String type) {
    return "Field " + fieldName + " has min mule version " + mmv + " because it is a " + componentType + " of type " + type + ".";
  }

  private static String getReasonInterface(String component, String name, ResolvedMinZernovaVersion interfaceMMV) {
    return component + " " + name + " has min mule version " + interfaceMMV.getMinZernovaVersion()
        + " because it implements interface " + interfaceMMV.getName() + ".";
  }

  private static String getReasonParameter(String component, String name, ResolvedMinZernovaVersion parameterMMV) {
    return component + " " + name + " has min mule version " + parameterMMV.getMinZernovaVersion() + " because of its parameter "
        + parameterMMV.getName() + ".";
  }

  private static String getReasonType(String component, String name, ResolvedMinZernovaVersion typeMMV) {
    return component + " " + name + " has min mule version " + typeMMV.getMinZernovaVersion() + " because it is of type "
        + typeMMV.getName() + ".";
  }

  private static String getReasonMethod(String component, String name, ResolvedMinZernovaVersion methodMMV) {
    return component + " " + name + " has min mule version " + methodMMV.getMinZernovaVersion() + " because of its method "
        + methodMMV.getName() + ".";
  }

  private static String getReasonClassLevelMMV(String component, String name, ZernovaVersion mmv) {
    return component + " " + name + " has min mule version " + mmv
        + " because it is the one set at the class level through the @MinZernovaVersion annotation.";
  }

  private static String getReasonOverride(String resolvedMMV, String componentType, String classLevelMMV, String reason) {
    return "Calculated Min Mule Version is " + resolvedMMV + " which is greater than the one set at the " + componentType
        + " level " + classLevelMMV + ". Overriding it. " + reason;
  }
}
