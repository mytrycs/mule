/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.validation;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.getType;
import static com.zernova.runtime.extension.api.util.NameUtils.getComponentModelTypeName;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.getFieldsWithGetters;

import com.zernova.metadata.api.model.ArrayType;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.ObjectFieldType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.metadata.api.visitor.MetadataTypeVisitor;
import com.zernova.runtime.api.meta.NamedObject;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.source.SourceCallbackModel;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.api.meta.model.util.IdempotentExtensionWalker;
import com.zernova.runtime.extension.api.declaration.type.TypeUtils;
import com.zernova.runtime.extension.api.declaration.type.annotation.StereotypeTypeAnnotation;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.loader.Problem;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;
import com.zernova.runtime.module.extension.internal.loader.java.property.ImplementingTypeModelProperty;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Validates the types used as:
 * <p>
 * <ul>
 * <li>Parameter of an {@link OperationModel}</li>
 * <li>Parameter of an {@link SourceCallbackModel}</li>
 * </ul>
 * <p>
 * These types should comply that for each parameter they contain, there must exist a getter method for that parameter. The vice
 * versa is also required, that implies the fact that for each getter method, there should exist a field named with the getter
 * convention.
 *
 * @since 4.0
 */
public final class JavaInputParametersTypeModelValidator implements ExtensionModelValidator {

  @Override
  public void validate(ExtensionModel extensionModel, ProblemsReporter problems) {
    final Set<Class<?>> validatedTypes = new HashSet<>();

    Optional<ImplementingTypeModelProperty> implementingType =
        extensionModel.getModelProperty(ImplementingTypeModelProperty.class);
    if (!implementingType.isPresent()) {
      return;
    }

    new IdempotentExtensionWalker() {

      @Override
      public void onOperation(OperationModel model) {
        model.getAllParameterModels()
            .forEach(parameterModel -> validateJavaType(model, parameterModel.getType(), problems, validatedTypes));
      }

      @Override
      public void onSource(SourceModel model) {
        validateCallback(model, model.getSuccessCallback());
        validateCallback(model, model.getErrorCallback());
        validateCallback(model, model.getTerminateCallback());
      }

      private void validateCallback(SourceModel model, Optional<SourceCallbackModel> callback) {
        callback.ifPresent(cb -> cb.getAllParameterModels()
            .forEach(parameterModel -> validateJavaType(model, parameterModel.getType(), problems, validatedTypes)));
      }
    }.walk(extensionModel);

    extensionModel.getSubTypes().forEach(subTypesModel -> getClassForValidation(subTypesModel.getBaseType())
        .filter(validatedTypes::contains).ifPresent(type -> {
          subTypesModel.getSubTypes()
              .forEach(subtype -> validateSubtypesHaveGetters(extensionModel, subtype, problems, validatedTypes));
        }));
  }

  private void validateJavaType(ComponentModel model, MetadataType type, ProblemsReporter problems,
                                Set<Class<?>> validatedTypes) {
    if (type.getMetadataFormat().equals(JAVA)) {
      validateParameterFieldsHaveGetters(model, type, problems, validatedTypes);
    }
  }

  private void validateSubtypesHaveGetters(ExtensionModel extensionModel, MetadataType subtype, ProblemsReporter problems,
                                           Set<Class<?>> validatedTypes) {
    validateType(format("Extension '%s' defines a subtype", extensionModel.getName()), extensionModel, subtype, problems,
                 validatedTypes);
  }

  private void validateParameterFieldsHaveGetters(ComponentModel model, MetadataType parameterMetadataType,
                                                  ProblemsReporter problems, Set<Class<?>> validatedTypes) {
    validateType(format("%s '%s' has an argument", getComponentModelTypeName(model), model.getName()), model,
                 parameterMetadataType, problems, validatedTypes);
  }

  private void validateType(String message, NamedObject namedObject, MetadataType type, ProblemsReporter problems,
                            Set<Class<?>> validatedTypes) {

    getClassForValidation(type).ifPresent(parameterType -> {

      if (Map.class.isAssignableFrom(parameterType)) {
        return;
      }

      ReflectionCache reflectionCache = new ReflectionCache();
      type.accept(new MetadataTypeVisitor() {

        @Override
        public void visitObject(ObjectType objectType) {
          if (validatedTypes.add(parameterType)) {
            Collection<ObjectFieldType> parameters = objectType.getFields();
            Set<String> fieldsWithGetters = getFieldsWithGetters(parameterType, reflectionCache).stream()
                .map(TypeUtils::getAlias)
                .map(String::toLowerCase)
                .collect(toSet());
            Set<String> parameterWithoutGetters =
                parameters.stream()
                    .filter(p -> {
                      StereotypeTypeAnnotation stereotypes = p.getAnnotation(StereotypeTypeAnnotation.class).orElse(null);
                      return stereotypes != null ? stereotypes.getAllowedStereotypes().isEmpty() : true;
                    })
                    .map(f -> f.getKey().getName().getLocalPart())
                    .filter(fieldName -> !fieldsWithGetters.contains(fieldName.toLowerCase()))
                    .collect(toSet());
            if (!parameterWithoutGetters.isEmpty()) {
              problems.addError(new Problem(namedObject,
                                            format(
                                                   "%s of type '%s' which contains fields (%s) that doesn't have the corresponding getter methods or getter methods that doesn't correspond to any of the present fields",
                                                   message, parameterType.getName(),
                                                   parameterWithoutGetters.stream().collect(joining(", ")))));
            }
          }
        }

        @Override
        public void visitArrayType(ArrayType arrayType) {
          validateType(message, namedObject, arrayType.getType(), problems, validatedTypes);
        }
      });
    });
  }

  private Optional<Class<Object>> getClassForValidation(MetadataType type) {
    if (!type.getMetadataFormat().equals(JAVA)) {
      return empty();
    }

    return getType(type);
  }
}
