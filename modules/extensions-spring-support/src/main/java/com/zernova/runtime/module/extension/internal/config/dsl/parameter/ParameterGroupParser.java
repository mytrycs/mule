/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.parameter;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition.Builder;
import com.zernova.runtime.extension.api.dsl.syntax.DslElementSyntax;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionDefinitionParser;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionParsingContext;

import java.util.Optional;

/**
 * A {@link ExtensionDefinitionParser} for parsing extension objects that are expressed as an inline {@link ParameterGroupModel}
 * <p>
 * These objects are parsed as {@link ValueResolver}s which are later resolved by a {@link TopLevelParameterObjectFactory}
 * instance
 *
 * @since 4.0
 */
public abstract class ParameterGroupParser extends ExtensionDefinitionParser {

  protected final ParameterGroupModel group;
  protected final ClassLoader classLoader;
  protected final DslElementSyntax groupDsl;
  protected final String name;
  protected final String namespace;

  public ParameterGroupParser(Builder definition, ParameterGroupModel group, ClassLoader classLoader, DslElementSyntax groupDsl,
                              DslSyntaxResolver dslResolver, ExtensionParsingContext context,
                              Optional<ClassTypeLoader> typeLoader) {
    super(definition, dslResolver, context, typeLoader);

    checkArgument(group.isShowInDsl(), "Cannot parse an implicit group");
    this.group = group;

    this.classLoader = classLoader;
    this.groupDsl = groupDsl;
    this.name = groupDsl.getElementName();
    this.namespace = groupDsl.getPrefix();
  }
}
