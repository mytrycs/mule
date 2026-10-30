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
  public static ResolvedMinZernovaVersion resolveExtensionMinMuleVersion(Type extension) {
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
        extension.getSuperType().map(MinZernovaVersionUtils::resolveExtensionMinMuleVersion);
    superExtensionMMV.ifPresent(resolvedMMV -> extensionMMV
        .updateIfHigherMMV(resolvedMMV, getReasonSuperClass("Extension", extension.getName(), resolvedMMV)));
    if (!(extension.isAnnotatedWith(Configurations.class)
        || extension.isAnnotatedWith(com.zernova.sdk.api.annotation.Configurations.class))) {
      Optional<ResolvedMinZernovaVersion> fieldMMV =
          extension.getFields().stream().map(f -> calculateFieldMinMuleVersion(f, new HashSet<>()))
              .reduce(MinZernovaVersionUtils::max);
      fieldMMV.ifPresent(resolvedMMV -> extensionMMV
          .updateIfHigherMMV(resolvedMMV, getReasonField("Extension", extension.getName(), resolvedMMV)));
      Optional<ResolvedMinZernovaVersion> methodMMV =
          extension.getEnclosingMethods().map(MinZernovaVersionUtils::calculateMethodMinMuleVersion)
              .reduce(MinZernovaVersionUtils::max);
      methodMMV.ifPresent(resolvedMMV -> extensionMMV
          .updateIfHigherMMV(resolvedMMV, getReasonMethod("Extension", extension.getName(), resolvedMMV)));
    }
    Optional<ZernovaVersion> classLevelMMV = getMinMuleVersionFromAnnotations(extension);
    if (classLevelMMV.isPresent()) {
      if (classLevelMMV.get().priorTo(extensionMMV.getMinMuleVersion())) {
        return new ResolvedMinZernovaVersion(extension.getName(), extensionMMV.getMinMuleVersion(),
                                          getReasonOverride(extensionMMV.getMinMuleVersion().toString(), "extension class",
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
   * @param propagatedMinMuleVersion the min mule version propagated from the config's container
   * @return the minimum mule version of the given configuration
   */
  public static ResolvedMinZernovaVersion resolveConfigurationMinMuleVersion(Type config, ZernovaVersion propagatedMinMuleVersion) {
    if (belongsToJavaPackages(config.getTypeName())) {
      return resolveToDefaultMMV("Java type", config.getTypeName());
    }
    ResolvedMinZernovaVersion configMMV = max(resolveToDefaultMMV("Configuration", config.getName()),
                                           new ResolvedMinZernovaVersion(config.getName(), propagatedMinMuleVersion, "Configuration "
                                               + config.getName() + " has min mule version " + propagatedMinMuleVersion
                                               + " because it was propagated from the annotation (either @Configurations or @Config) used to reference this configuration."));
    Optional<Type> superType = config.getSuperType();
    if (superType.isPresent()) {
      ResolvedMinZernovaVersion superConfigMMV = resolveConfigurationMinMuleVersion(superType.get(), propagatedMinMuleVersion);
      configMMV.updateIfHigherMMV(superConfigMMV, getReasonSuperClass("Configuration", config.getName(), superConfigMMV));
    }
    Optional<ResolvedMinZernovaVersion> annotationMMV =
        config.getAnnotations().map(MinZernovaVersionUtils::getEnforcedMinMuleVersion)
            .reduce(MinZernovaVersionUtils::max);
    annotationMMV.ifPresent(resolvedMMV -> configMMV
        .updateIfHigherMMV(resolvedMMV, getReasonAnnotated("Configuration", config.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> fieldMMV =
        config.getFields().stream().map(f -> calculateFieldMinMuleVersion(f, new HashSet<>()))
            .reduce(MinZernovaVersionUtils::max);
    fieldMMV.ifPresent(resolvedMMV -> configMMV
        .updateIfHigherMMV(resolvedMMV, getReasonField("Configuration", config.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> methodMMV =
        config.getEnclosingMethods().map(MinZernovaVersionUtils::calculateMethodMinMuleVersion)
            .reduce(MinZernovaVersionUtils::max);
    methodMMV.ifPresent(resolvedMMV -> configMMV
        .updateIfHigherMMV(resolvedMMV, getReasonMethod("Configuration", config.getName(), resolvedMMV)));
    Optional<ZernovaVersion> classLevelMMV = getMinMuleVersionFromAnnotations(config);
    if (classLevelMMV.isPresent()) {
      if (classLevelMMV.get().priorTo(configMMV.getMinMuleVersion())) {
        return new ResolvedMinZernovaVersion(config.getName(), configMMV.getMinMuleVersion(),
                                          getReasonOverride(configMMV.getMinMuleVersion().toString(), "configuration class",
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
   * @param propagatedMinMuleVersion the min mule version propagated from the function's container
   * @return the minimum mule version of the given function
   */
  public static ResolvedMinZernovaVersion resolveFunctionMinMuleVersion(MethodElement<?> function,
                                                                     ZernovaVersion propagatedMinMuleVersion) {
    ResolvedMinZernovaVersion resolvedMMV =
        max(resolveToDefaultMMV("Function", function.getName()),
            new ResolvedMinZernovaVersion(function.getName(), propagatedMinMuleVersion, "Function " + function.getName()
                + " has min mule version " + propagatedMinMuleVersion
                + " because it was propagated from the @Functions annotation at the extension class used to add the function."));
    resolvedMMV = max(resolvedMMV, calculateMethodMinMuleVersion(function));
    return resolvedMMV;
  }

  /**
   * Calculates the minimum {@link ZernovaVersion} for a given {@link OperationElement} by looking at the annotation
   * {@link MinZernovaVersion} (if present) and at the new SDK API components it interacts with through its annotations, parameters
   * and return type.
   *
   * @param operation                the operation, as a {@link MethodElement}, to calculate its min mule version
   * @param propagatedMinMuleVersion the min mule version propagated from the operation's container
   * @return the minimum mule version of the given operation
   */
  public static ResolvedMinZernovaVersion resolveOperationMinMuleVersion(MethodElement<?> operation,
                                                                      OperationContainerElement operationContainer,
                                                                      ZernovaVersion propagatedMinMuleVersion) {
    ResolvedMinZernovaVersion operationMMV = resolveToDefaultMMV("Operation", operation.getName());
    operationMMV = max(operationMMV, new ResolvedMinZernovaVersion(operation.getName(), propagatedMinMuleVersion, "Operation "
        + operation.getName() + " has min mule version " + propagatedMinMuleVersion
        + " because it was propagated from the @Operations annotation at the extension class used to add the operation's container "
        + operationContainer.getName() + "."));
    Optional<ResolvedMinZernovaVersion> parameterMMV = operationContainer.getParameters().stream()
        .map(MinZernovaVersionUtils::calculateMethodParameterMinMuleVersion)
        .reduce(MinZernovaVersionUtils::max);
    if (parameterMMV.isPresent()) {
      operationMMV.updateIfHigherMMV(parameterMMV.get(),
                                     getReasonParameter("Operation", operation.getName(), parameterMMV.get()));
    }
    operationMMV = max(operationMMV, calculateMethodMinMuleVersion(operation));
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
  public static ResolvedMinZernovaVersion resolveConnectionProviderMinMuleVersion(Type connectionProvider) {
    if (belongsToJavaPackages(connectionProvider.getTypeName())) {
      return resolveToDefaultMMV("Java type", connectionProvider.getTypeName());
    }
    ResolvedMinZernovaVersion connectionProviderMMV =
        resolveToDefaultMMV("Connection Provider", connectionProvider.getName());
    Optional<Type> superType = connectionProvider.getSuperType();
    if (superType.isPresent()) {
      if (superType.get().isAssignableTo(ConnectionProvider.class)
          || superType.get().isAssignableTo(com.zernova.sdk.api.connectivity.ConnectionProvider.class)) {
        ResolvedMinZernovaVersion superTypeMMV = resolveConnectionProviderMinMuleVersion(superType.get());
        connectionProviderMMV
            .updateIfHigherMMV(superTypeMMV,
                               getReasonSuperClass("Connection Provider", connectionProvider.getName(), superTypeMMV));
      }
    }
    Optional<ResolvedMinZernovaVersion> interfaceMMV = connectionProvider.getImplementingInterfaces()
        .map(MinZernovaVersionUtils::calculateInterfaceMinMuleVersion).reduce(MinZernovaVersionUtils::max);
    interfaceMMV.ifPresent(resolvedMMV -> connectionProviderMMV
        .updateIfHigherMMV(resolvedMMV, getReasonInterface("Connection Provider", connectionProvider.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> annotationMMV =
        connectionProvider.getAnnotations().map(MinZernovaVersionUtils::getEnforcedMinMuleVersion)
            .reduce(MinZernovaVersionUtils::max);
    annotationMMV.ifPresent(resolvedMMV -> connectionProviderMMV
        .updateIfHigherMMV(resolvedMMV, getReasonAnnotated("Connection Provider", connectionProvider.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> fieldMMV =
        connectionProvider.getFields().stream().map(f -> calculateFieldMinMuleVersion(f, new HashSet<>()))
            .reduce(MinZernovaVersionUtils::max);
    fieldMMV.ifPresent(resolvedMMV -> connectionProviderMMV
        .updateIfHigherMMV(resolvedMMV, getReasonField("Connection Provider", connectionProvider.getName(), resolvedMMV)));
    Optional<ZernovaVersion> classLevelMMV = getMinMuleVersionFromAnnotations(connectionProvider);
    if (classLevelMMV.isPresent()) {
      if (classLevelMMV.get().priorTo((connectionProviderMMV.getMinMuleVersion()))) {
        return new ResolvedMinZernovaVersion(connectionProvider.getName(), connectionProviderMMV.getMinMuleVersion(),
                                          getReasonOverride(connectionProviderMMV.getMinMuleVersion().toString(),
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
  public static ResolvedMinZernovaVersion resolveSourceMinMuleVersion(SourceElement source) {
    ResolvedMinZernovaVersion genericsMMV = calculateSourceGenericsMinMuleVersion(source.getSuperClassGenerics());
    ResolvedMinZernovaVersion sourceMMV = calculateSourceMinMuleVersion(source);
    sourceMMV.updateIfHigherMMV(genericsMMV, "Source " + source.getName() + " has min mule version "
        + genericsMMV.getMinMuleVersion() + " because it has a generic of type " + genericsMMV.getName() + ".");
    return sourceMMV;
  }

  private static ResolvedMinZernovaVersion calculateSourceMinMuleVersion(Type source) {
    if (belongsToJavaPackages(source.getTypeName())) {
      return resolveToDefaultMMV("Java type", source.getTypeName());
    }
    ResolvedMinZernovaVersion sourceMMV = resolveToDefaultMMV("Source", source.getName());
    Optional<Type> superType = source.getSuperType();
    if (superType.isPresent()) {
      ResolvedMinZernovaVersion superTypeMMV;
      if (superType.get().isSameType(Source.class) || superType.get().isSameType(com.zernova.sdk.api.runtime.source.Source.class)) {
        superTypeMMV = getEnforcedMinMuleVersion(superType.get());
      } else {
        superTypeMMV = calculateSourceMinMuleVersion(superType.get());
      }
      sourceMMV.updateIfHigherMMV(superTypeMMV, getReasonSuperClass("Source", source.getName(), superTypeMMV));
    }
    Optional<ResolvedMinZernovaVersion> interfaceMMV =
        source.getImplementingInterfaces().map(MinZernovaVersionUtils::calculateInterfaceMinMuleVersion)
            .reduce(MinZernovaVersionUtils::max);
    interfaceMMV.ifPresent(resolvedMMV -> sourceMMV
        .updateIfHigherMMV(resolvedMMV, getReasonInterface("Source", source.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> annotationMMV =
        source.getAnnotations().map(MinZernovaVersionUtils::getEnforcedMinMuleVersion)
            .reduce(MinZernovaVersionUtils::max);
    annotationMMV.ifPresent(resolvedMMV -> sourceMMV
        .updateIfHigherMMV(resolvedMMV, getReasonAnnotated("Source", source.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> fieldMMV =
        source.getFields().stream().map(f -> calculateFieldMinMuleVersion(f, new HashSet<>()))
            .reduce(MinZernovaVersionUtils::max);
    fieldMMV.ifPresent(resolvedMMV -> sourceMMV.updateIfHigherMMV(resolvedMMV,
                                                                  getReasonField("Source", source.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> methodMMV =
        source.getEnclosingMethods().map(MinZernovaVersionUtils::calculateMethodMinMuleVersion)
            .reduce(MinZernovaVersionUtils::max);
    methodMMV.ifPresent(resolvedMMV -> sourceMMV.updateIfHigherMMV(resolvedMMV,
                                                                   getReasonMethod("Source", source.getName(), resolvedMMV)));
    Optional<ZernovaVersion> classLevelMMV = getMinMuleVersionFromAnnotations(source);
    if (classLevelMMV.isPresent()) {
      if (classLevelMMV.get().priorTo(sourceMMV.getMinMuleVersion())) {
        return new ResolvedMinZernovaVersion(source.getName(), sourceMMV.getMinMuleVersion(),
                                          getReasonOverride(sourceMMV.getMinMuleVersion().toString(), "source class",
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
  public static <A extends Annotation> ZernovaVersion getContainerAnnotationMinMuleVersion(Type containerElement,
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

  private static ResolvedMinZernovaVersion calculateMethodMinMuleVersion(MethodElement<?> method) {
    ResolvedMinZernovaVersion methodMMV = resolveToDefaultMMV("Method", method.getName());
    Optional<ResolvedMinZernovaVersion> annotationMMV =
        method.getAnnotations().map(MinZernovaVersionUtils::getEnforcedMinMuleVersion)
            .reduce(MinZernovaVersionUtils::max);
    annotationMMV.ifPresent(resolvedMMV -> methodMMV
        .updateIfHigherMMV(resolvedMMV, getReasonAnnotated("Method", method.getName(), resolvedMMV)));
    Optional<ResolvedMinZernovaVersion> parameterMMV =
        method.getParameters().stream().map(MinZernovaVersionUtils::calculateMethodParameterMinMuleVersion)
            .reduce(MinZernovaVersionUtils::max);
    parameterMMV.ifPresent(resolvedMMV -> methodMMV
        .updateIfHigherMMV(resolvedMMV, getReasonParameter("Method", method.getName(), resolvedMMV)));
    ResolvedMinZernovaVersion outputMMV = calculateOutputMinMuleVersion(method.getReturnType());
    methodMMV.updateIfHigherMMV(outputMMV, "Method " + method.getName() + " has min mule version " + outputMMV.getMinMuleVersion()
        + " because of its output type " + outputMMV.getName() + ".");
    Optional<ZernovaVersion> operationLevelMMV = getMinMuleVersionFromAnnotations(method);
    if (operationLevelMMV.isPresent()) {
      if (operationLevelMMV.get().priorTo(methodMMV.getMinMuleVersion())) {
        return new ResolvedMinZernovaVersion(method.getName(), methodMMV.getMinMuleVersion(),
                                          getReasonOverride(methodMMV.getMinMuleVersion().toString(), "method",
                                                            operationLevelMMV.get().toString(), methodMMV.getReason()));
      } else {
        return new ResolvedMinZernovaVersion(method.getName(), operationLevelMMV.get(), "Method " + method.getName()
            + " has min mule version " + operationLevelMMV.get()
            + " because it is the one set at the method level through the @MinZernovaVersion annotation.");
      }
    }
    return methodMMV;
  }

  private static ResolvedMinZernovaVersion calculateInterfaceMinMuleVersion(Type interfaceType) {
    ResolvedMinZernovaVersion interfaceMMV = getEnforcedMinMuleVersion(interfaceType);
    if (belongsToSdkPackages(interfaceType.getTypeName())) {
      return interfaceMMV;
    }
    Optional<ResolvedMinZernovaVersion> superInterface = interfaceType.getImplementingInterfaces()
        .map(MinZernovaVersionUtils::calculateInterfaceMinMuleVersion).reduce(MinZernovaVersionUtils::max);
    superInterface.ifPresent(resolvedMMV -> interfaceMMV
        .updateIfHigherMMV(resolvedMMV, getReasonInterface("Interface", interfaceType.getName(), resolvedMMV)));
    return interfaceMMV;
  }

  private static ResolvedMinZernovaVersion calculateSourceGenericsMinMuleVersion(List<Type> sourceGenerics) {
    Type outputType = sourceGenerics.get(0);
    Type attributesType = sourceGenerics.get(1);
    return max(getEnforcedMinMuleVersion(attributesType), calculateOutputMinMuleVersion(outputType));
  }

  private static ResolvedMinZernovaVersion calculateOutputMinMuleVersion(Type outputType) {
    ResolvedMinZernovaVersion resolvedMMV = resolveToDefaultMMV("Output type", outputType.getName());
    if (outputType.isArray()) {
      resolvedMMV = max(resolvedMMV, calculateOutputMinMuleVersion(outputType.getArrayComponentType().get()));
    } else if (outputType.isAssignableTo(Iterable.class) || outputType.isAssignableTo(Iterator.class)) {
      for (TypeGeneric typeGeneric : outputType.getGenerics()) {
        resolvedMMV = max(resolvedMMV, calculateOutputMinMuleVersion(typeGeneric.getConcreteType()));
      }
    } else if (outputType.isSameType(Result.class) || outputType.isSameType(com.zernova.sdk.api.runtime.operation.Result.class)) {
      resolvedMMV = max(resolvedMMV, getEnforcedMinMuleVersion(outputType));
      for (TypeGeneric MMVTypeGeneric : outputType.getGenerics()) {
        resolvedMMV = max(resolvedMMV, getEnforcedMinMuleVersion(MMVTypeGeneric.getConcreteType()));
      }
    } else if (outputType.isSameType(PagingProvider.class)
        || outputType.isSameType(com.zernova.sdk.api.runtime.streaming.PagingProvider.class)) {
      resolvedMMV = max(resolvedMMV, getEnforcedMinMuleVersion(outputType));
      resolvedMMV =
          max(resolvedMMV, getEnforcedMinMuleVersion(outputType.getGenerics().get(1).getConcreteType()));
    } else {
      resolvedMMV = getEnforcedMinMuleVersion(outputType);
    }
    return resolvedMMV;
  }

  private static ResolvedMinZernovaVersion calculateFieldMinMuleVersion(ExtensionParameter field,
                                                                     Set<String> seenTypesForRecursionControl) {
    Type parameterType = field.getType();
    if (seenTypesForRecursionControl.contains(parameterType.getTypeName())) {
      // If it is a recursive type we only look at its annotations and cut the recursion.
      ResolvedMinZernovaVersion recursiveFieldMMV = resolveToDefaultMMV("Field", field.getName());
      Optional<ResolvedMinZernovaVersion> annotationMMV =
          field.getAnnotations().map(MinZernovaVersionUtils::getEnforcedMinMuleVersion)
              .reduce(MinZernovaVersionUtils::max);
      annotationMMV.ifPresent(resolvedMMV -> recursiveFieldMMV
          .updateIfHigherMMV(resolvedMMV, getReasonAnnotated("Field", field.getName(), resolvedMMV)));
      return recursiveFieldMMV;
    } else {
      seenTypesForRecursionControl.add(parameterType.getTypeName());
    }
    ResolvedMinZernovaVersion fieldMinMuleVersion = getMinMuleVersionFromAnnotations(field)
        .map(mmv -> new ResolvedMinZernovaVersion(field.getName(), mmv,
                                               "Field " + field.getName() + " has min mule version " + mmv
                                                   + " because it is annotated with @MinZernovaVersion."))
        .orElse(resolveToDefaultMMV("Field", field.getName()));
    // Parse sdk fields (this also catches automatically injected fields)
    if (belongsToSdkPackages(parameterType.getTypeName())) {
      ResolvedMinZernovaVersion typeMMV = getEnforcedMinMuleVersion(parameterType);
      fieldMinMuleVersion.updateIfHigherMMV(typeMMV, getReasonType("Field", field.getName(), typeMMV));
    }
    for (Type annotation : field.getAnnotations().collect(toList())) {
      calculateFieldAnnotationMinMuleVersion(field, seenTypesForRecursionControl, parameterType, fieldMinMuleVersion, annotation);
    }
    return fieldMinMuleVersion;
  }

  private static void calculateFieldAnnotationMinMuleVersion(ExtensionParameter field, Set<String> seenTypesForRecursionControl,
                                                             Type parameterType, ResolvedMinZernovaVersion fieldMinMuleVersion,
                                                             Type annotation) {
    if ((annotation.isSameType(Inject.class)
        // Still need to support javax.inject for the time being...
        || annotation.isSameType(javax.inject.Inject.class))
        && !parameterType.isSameType(Optional.class)) {
      // Parse injected classes but exclude Optionals (such as ForwardCompatibilityHelper)
      ResolvedMinZernovaVersion typeMMV = getEnforcedMinMuleVersion(parameterType);
      fieldMinMuleVersion.updateIfHigherMMV(typeMMV, getReasonType("Field", field.getName(), typeMMV));
    }
    if (isParameterOrParameterGroup(annotation)) {
      ResolvedMinZernovaVersion parameterContainerMMV =
          calculateParameterTypeMinMuleVersion(parameterType, seenTypesForRecursionControl);
      fieldMinMuleVersion.updateIfHigherMMV(parameterContainerMMV,
                                            getReasonFieldType(field.getName(),
                                                               parameterContainerMMV.getMinMuleVersion().toString(),
                                                               "parameter", parameterContainerMMV.getName()));
    }
    if (annotation.isSameType(Connection.class) || annotation.isSameType(com.zernova.sdk.api.annotation.param.Connection.class)) {
      // Sources inject the ConnectionProvider instead of the connection
      if (parameterType.isAssignableTo(ConnectionProvider.class)
          || parameterType.isAssignableTo(com.zernova.sdk.api.connectivity.ConnectionProvider.class)) {
        ResolvedMinZernovaVersion connectionProviderMMV = resolveConnectionProviderMinMuleVersion(parameterType);
        fieldMinMuleVersion.updateIfHigherMMV(connectionProviderMMV,
                                              getReasonFieldType(field.getName(),
                                                                 connectionProviderMMV.getMinMuleVersion().toString(),
                                                                 "connection provider", connectionProviderMMV.getName()));
      }
    }
    if (annotation.isSameType(Config.class) || annotation.isSameType(com.zernova.sdk.api.annotation.param.Config.class)) {
      ResolvedMinZernovaVersion configurationMMV =
          resolveConfigurationMinMuleVersion(parameterType, getEnforcedMinMuleVersion(annotation).getMinMuleVersion());
      fieldMinMuleVersion.updateIfHigherMMV(configurationMMV,
                                            getReasonFieldType(field.getName(), configurationMMV.getMinMuleVersion().toString(),
                                                               "configuration", configurationMMV.getName()));
    }
    ResolvedMinZernovaVersion annotationMMV = getEnforcedMinMuleVersion(annotation);
    fieldMinMuleVersion.updateIfHigherMMV(annotationMMV, getReasonAnnotated("Field", field.getName(), annotationMMV));
  }

  private static ResolvedMinZernovaVersion calculateMethodParameterMinMuleVersion(ExtensionParameter methodParameter) {
    ResolvedMinZernovaVersion methodParameterMMV = resolveToDefaultMMV("Parameter", methodParameter.getName());
    Optional<ResolvedMinZernovaVersion> configAnnotationMMV =
        methodParameter.getAnnotations()
            .filter(a -> a.isAssignableTo(Config.class) || a.isAssignableTo(com.zernova.sdk.api.annotation.param.Config.class))
            .findFirst().map(MinZernovaVersionUtils::getEnforcedMinMuleVersion);
    if (configAnnotationMMV.isPresent()) {
      ResolvedMinZernovaVersion configMMV =
          resolveConfigurationMinMuleVersion(methodParameter.getType(), configAnnotationMMV.get().getMinMuleVersion());
      methodParameterMMV
          .updateIfHigherMMV(configMMV, "Parameter " + methodParameter.getName() + " has min mule version "
              + configMMV.getMinMuleVersion() + " because it references a config of type " + configMMV.getName() + ".");
    }
    Optional<ResolvedMinZernovaVersion> annotationMMV =
        methodParameter.getAnnotations().map(MinZernovaVersionUtils::getEnforcedMinMuleVersion).reduce(MinZernovaVersionUtils::max);
    annotationMMV.ifPresent(resolvedMMV -> methodParameterMMV
        .updateIfHigherMMV(resolvedMMV, getReasonAnnotated("Parameter", methodParameter.getName(), resolvedMMV)));
    ResolvedMinZernovaVersion parameterTypeMMV;
    // Parse sdk fields (this also catches automatically injected fields)
    if (belongsToSdkPackages(methodParameter.getType().getTypeName())) {
      parameterTypeMMV = getEnforcedMinMuleVersion(methodParameter.getType());
    } else { // The parameter is of custom type
      if (methodParameter.getAnnotations()
          .noneMatch(a -> a.isSameType(Connection.class) || a.isSameType(com.zernova.sdk.api.annotation.param.Connection.class))) {
        parameterTypeMMV = calculateParameterTypeMinMuleVersion(methodParameter.getType(), new HashSet<>());
      } else {
        parameterTypeMMV = resolveToDefaultMMV("Type", methodParameter.getType().getName());
      }
    }
    methodParameterMMV.updateIfHigherMMV(parameterTypeMMV,
                                         getReasonType("Parameter", methodParameter.getName(), parameterTypeMMV));
    return methodParameterMMV;
  }

  private static ResolvedMinZernovaVersion calculateParameterTypeMinMuleVersion(Type parameterType,
                                                                             Set<String> seenTypesForRecursionControl) {
    Optional<ZernovaVersion> minMuleVersionAnnotation = getMinMuleVersionFromAnnotations(parameterType);
    if (minMuleVersionAnnotation.isPresent()) {
      String reason = "Type " + parameterType.getName() + " has min mule version " + minMuleVersionAnnotation.get()
          + " because it is annotated with @MinZernovaVersion.";
      return new ResolvedMinZernovaVersion(parameterType.getName(), minMuleVersionAnnotation.get(), reason);
    }
    ResolvedMinZernovaVersion parameterMMV = resolveToDefaultMMV("Type", parameterType.getName());

    Optional<ResolvedMinZernovaVersion> fieldMMV = empty();
    // TODO W-14749120 - properly handle provided types that are missing
    try {
      fieldMMV = parameterType.getFields().stream()
          .filter(MinZernovaVersionUtils::isAnnotatedWithParameterOrParameterGroup)
          .map(f -> calculateFieldMinMuleVersion(f, seenTypesForRecursionControl)).reduce(MinZernovaVersionUtils::max);
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
        parameterType.getAnnotations().map(MinZernovaVersionUtils::getEnforcedMinMuleVersion)
            .reduce(MinZernovaVersionUtils::max);
    annotationMMV.ifPresent(resolvedMMV -> parameterMMV
        .updateIfHigherMMV(resolvedMMV, getReasonAnnotated("Type", parameterType.getName(), resolvedMMV)));
    return parameterMMV;
  }

  public static void declarerWithMmv(HasMinZernovaVersionDeclarer<?> declarer, MinZernovaVersionParser resolvedMMV) {
    declarer.withMinMuleVersion(resolvedMMV.getMinMuleVersion());
    LOGGER.debug(resolvedMMV.getReason());
  }

  public static void declarationWithMmv(WithMinZernovaVersionDeclaration declaration, MinZernovaVersionParser resolvedMMV) {
    declaration.withMinMuleVersion(resolvedMMV.getMinMuleVersion());
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

  private static ResolvedMinZernovaVersion getEnforcedMinMuleVersion(Type type) {
    if (type.isAnnotatedWith(DoNotEnforceMinZernovaVersion.class)) {
      return new ResolvedMinZernovaVersion(type.getName(), FIRST_MULE_VERSION,
                                        type.getName() + " has the default base min mule version " + FIRST_MULE_VERSION
                                            + " because it is annotated with @DoNotEnforceMinZernovaVersion.");
    }
    Optional<ZernovaVersion> mmv = getMinMuleVersionFromAnnotations(type);
    return mmv
        .map(muleVersion -> new ResolvedMinZernovaVersion(type.getName(), muleVersion,
                                                       type.getName() + " was introduced in Mule " + muleVersion + "."))
        .orElseGet(() -> resolveToDefaultMMV("Type", type.getName()));
  }

  private static Optional<ZernovaVersion> getMinMuleVersionFromAnnotations(WithAnnotations type) {
    return type.getValueFromAnnotation(MinZernovaVersion.class)
        .map(fetcher -> new ZernovaVersion(fetcher.getStringValue(MinZernovaVersion::value)));
  }

  private static ResolvedMinZernovaVersion max(ResolvedMinZernovaVersion currentMax, ResolvedMinZernovaVersion candidate) {
    if (currentMax.getMinMuleVersion().atLeast(candidate.getMinMuleVersion())) {
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
    return component + " " + name + " has min mule version " + annotationMMV.getMinMuleVersion()
        + " because it is annotated with "
        + annotationMMV.getName() + ".";
  }

  private static String getReasonSuperClass(String component, String name, ResolvedMinZernovaVersion superClassMMV) {
    return component + " " + name + " has min mule version " + superClassMMV.getMinMuleVersion() + " because of its super class "
        + superClassMMV.getName() + ".";
  }

  private static String getReasonField(String component, String name, ResolvedMinZernovaVersion fieldMMV) {
    return component + " " + name + " has min mule version " + fieldMMV.getMinMuleVersion() + " because of its field "
        + fieldMMV.getName() + ".";
  }


  private static String getReasonFieldType(String fieldName, String mmv, String componentType, String type) {
    return "Field " + fieldName + " has min mule version " + mmv + " because it is a " + componentType + " of type " + type + ".";
  }

  private static String getReasonInterface(String component, String name, ResolvedMinZernovaVersion interfaceMMV) {
    return component + " " + name + " has min mule version " + interfaceMMV.getMinMuleVersion()
        + " because it implements interface " + interfaceMMV.getName() + ".";
  }

  private static String getReasonParameter(String component, String name, ResolvedMinZernovaVersion parameterMMV) {
    return component + " " + name + " has min mule version " + parameterMMV.getMinMuleVersion() + " because of its parameter "
        + parameterMMV.getName() + ".";
  }

  private static String getReasonType(String component, String name, ResolvedMinZernovaVersion typeMMV) {
    return component + " " + name + " has min mule version " + typeMMV.getMinMuleVersion() + " because it is of type "
        + typeMMV.getName() + ".";
  }

  private static String getReasonMethod(String component, String name, ResolvedMinZernovaVersion methodMMV) {
    return component + " " + name + " has min mule version " + methodMMV.getMinMuleVersion() + " because of its method "
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
