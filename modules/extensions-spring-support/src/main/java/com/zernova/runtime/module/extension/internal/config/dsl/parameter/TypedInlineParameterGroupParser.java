/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.parameter;

import static com.zernova.metadata.api.utils.MetadataTypeUtils.isObjectType;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromFixedValue;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromReferenceObject;
import static com.zernova.runtime.dsl.api.component.TypeDefinition.fromType;
import static com.zernova.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory.getDefault;

import static java.lang.String.format;

import static com.google.common.base.Preconditions.checkArgument;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.runtime.api.meta.NamedObject;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterRole;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition.Builder;
import com.zernova.runtime.extension.api.dsl.syntax.DslElementSyntax;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionParsingContext;
import com.zernova.runtime.module.extension.internal.loader.ParameterGroupDescriptor;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A {@link ParameterGroupParser} which returns the values of the parameters in the group in fields of an object of a given type
 *
 * @since 4.0
 */
public class TypedInlineParameterGroupParser extends ParameterGroupParser {

  private final ObjectType metadataType;
  private final Map<String, ParameterRole> parametersRole;

  public TypedInlineParameterGroupParser(Builder definition,
                                         ParameterGroupModel group,
                                         ParameterGroupDescriptor groupDescriptor,
                                         ClassLoader classLoader, DslElementSyntax groupDsl,
                                         DslSyntaxResolver dslResolver,
                                         ExtensionParsingContext context,
                                         Optional<ClassTypeLoader> typeLoader) {
    super(definition, group, classLoader, groupDsl, dslResolver, context, typeLoader);
    MetadataType type = typeLoader.orElseGet(() -> getDefault().createTypeLoader(classLoader))
        .load(groupDescriptor.getType().getDeclaringClass().get());

    checkArgument(isObjectType(type), format("Only an ObjectType can be parsed as a TypedParameterGroup, found [%s] instead",
                                             type.getClass().getName()));
    metadataType = (ObjectType) type;
    parametersRole = group.getParameterModels().stream().collect(Collectors.toMap(NamedObject::getName, ParameterModel::getRole));
  }


  @Override
  protected Builder doParse(Builder definitionBuilder) throws ConfigurationException {
    Builder finalBuilder = definitionBuilder.withIdentifier(name).withNamespace(namespace).asNamed()
        .withTypeDefinition(fromType(ValueResolver.class))
        .withObjectFactoryType(InlineParameterGroupObjectFactory.class)
        .withConstructorParameterDefinition(fromFixedValue(metadataType).build())
        .withConstructorParameterDefinition(fromFixedValue(classLoader).build())
        .withConstructorParameterDefinition(fromReferenceObject(ZernovaContext.class).build());

    parseFields(metadataType, groupDsl, parametersRole);

    return finalBuilder;
  }
}
