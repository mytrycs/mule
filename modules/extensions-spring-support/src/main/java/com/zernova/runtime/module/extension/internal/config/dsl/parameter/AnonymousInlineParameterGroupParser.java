/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl.parameter;

import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromFixedValue;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromReferenceObject;
import static com.zernova.runtime.dsl.api.component.TypeDefinition.fromType;

import static java.util.Collections.singletonList;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.dsl.api.component.ComponentBuildingDefinition.Builder;
import com.zernova.runtime.extension.api.dsl.syntax.DslElementSyntax;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.internal.config.dsl.AbstractExtensionObjectFactory;
import com.zernova.runtime.module.extension.internal.config.dsl.ExtensionParsingContext;
import com.zernova.runtime.module.extension.internal.runtime.resolver.MapValueResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.StaticValueResolver;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link ParameterGroupParser} which returns the values of the parameters in the group as a {@link Map}
 *
 * @since 4.0
 */
public class AnonymousInlineParameterGroupParser extends ParameterGroupParser {

  public AnonymousInlineParameterGroupParser(Builder definition,
                                             ParameterGroupModel group,
                                             ClassLoader classLoader,
                                             DslElementSyntax groupDsl,
                                             DslSyntaxResolver dslResolver,
                                             ExtensionParsingContext context,
                                             Optional<ClassTypeLoader> typeLoader) {
    super(definition, group, classLoader, groupDsl, dslResolver, context, typeLoader);
  }

  @Override
  protected Builder doParse(Builder definitionBuilder) throws ConfigurationException {
    Builder finalBuilder =
        definitionBuilder.withIdentifier(name).withNamespace(namespace).asNamed()
            .withTypeDefinition(fromType(Map.class))
            .withObjectFactoryType(AnonymousGroupObjectFactory.class)
            .withConstructorParameterDefinition(fromReferenceObject(ZernovaContext.class).build())
            .withConstructorParameterDefinition(fromFixedValue(group).build());

    this.parseParameters(group.getParameterModels());

    return finalBuilder;
  }

  public static class AnonymousGroupObjectFactory extends AbstractExtensionObjectFactory<Object> {

    private final ParameterGroupModel group;

    public AnonymousGroupObjectFactory(ZernovaContext muleContext, ParameterGroupModel group) {
      super(muleContext);
      this.group = group;
    }

    @Override
    public Object getObject() throws Exception {
      ResolverSet resolverSet =
          this.getParametersResolver().getParametersAsResolverSet(singletonList(group), group.getParameterModels(), muleContext);

      List<ValueResolver<Object>> keyResolvers = new LinkedList<>();
      List<ValueResolver<Object>> valueResolvers = new LinkedList<>();
      resolverSet.getResolvers().forEach((key, value) -> {
        keyResolvers.add(new StaticValueResolver<>(key));
        valueResolvers.add((ValueResolver<Object>) value);
      });
      return MapValueResolver.of(HashMap.class, keyResolvers, valueResolvers, reflectionCache, muleContext);
    }

    @Override
    public Map doGetObject() throws Exception {
      throw new UnsupportedOperationException("This factory returns a simple Java Map. We can't have annotations on a Map");
    }
  }

}
