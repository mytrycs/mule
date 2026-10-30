/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.artifact;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.module.tooling.internal.artifact.params.ParameterExtractor.asDataWeaveExpression;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.NamedObject;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.app.declaration.api.ParameterElementDeclaration;
import com.zernova.runtime.app.declaration.api.ParameterGroupElementDeclaration;
import com.zernova.runtime.app.declaration.api.ParameterizedElementDeclaration;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.module.extension.api.runtime.resolver.ParameterValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.internal.runtime.config.ResolverSetBasedParameterResolver;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ParametersResolver;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;
import com.zernova.runtime.module.tooling.internal.artifact.params.ExpressionNotSupportedException;
import com.zernova.runtime.module.tooling.internal.utils.ArtifactHelper;

import java.util.HashMap;
import java.util.Map;

public class AbstractParameterResolverExecutor {

  protected final ZernovaContext muleContext;
  protected final ExpressionManager expressionManager;
  protected final ReflectionCache reflectionCache;
  protected final ArtifactHelper artifactHelper;

  public static final String INVALID_PARAMETER_VALUE = "INVALID_PARAMETER_VALUE";

  public AbstractParameterResolverExecutor(ZernovaContext muleContext, ExpressionManager expressionManager,
                                           ReflectionCache reflectionCache, ArtifactHelper artifactHelper) {
    this.muleContext = muleContext;
    this.expressionManager = expressionManager;
    this.reflectionCache = reflectionCache;
    this.artifactHelper = artifactHelper;
  }

  protected ParameterValueResolver parameterValueResolver(ParameterizedElementDeclaration parameterizedElementDeclaration,
                                                          ParameterizedModel parameterizedModel)
      throws ExpressionNotSupportedException {
    Map<String, Object> parametersMap =
        parametersMap(parameterizedElementDeclaration, parameterizedModel);

    try {
      final ResolverSet resolverSet =
          ParametersResolver.fromValues(parametersMap,
                                        muleContext,
                                        reflectionCache,
                                        expressionManager,
                                        parameterizedModel.getName())
              .getParametersAsResolverSet(parameterizedModel, muleContext);
      return new ResolverSetBasedParameterResolver(resolverSet, parameterizedModel, reflectionCache, expressionManager);
    } catch (ConfigurationException e) {
      throw new ZernovaRuntimeException(createStaticMessage("Error resolving parameters values from declaration"), e);
    }
  }

  protected Map<String, Object> parametersMap(ParameterizedElementDeclaration parameterizedElementDeclaration,
                                              ParameterizedModel parameterizedModel)
      throws ExpressionNotSupportedException {
    Map<String, Object> parametersMap = new HashMap<>();

    Map<String, ParameterGroupModel> parameterGroups =
        parameterizedModel.getParameterGroupModels().stream().collect(toMap(NamedObject::getName, identity()));

    for (ParameterGroupElementDeclaration parameterGroupElement : parameterizedElementDeclaration.getParameterGroups()) {
      final String parameterGroupName = parameterGroupElement.getName();
      final ParameterGroupModel parameterGroupModel = parameterGroups.get(parameterGroupName);
      if (parameterGroupModel == null) {
        throw new ZernovaRuntimeException(createStaticMessage("Could not find parameter group with name: '%s' in model",
                                                           parameterGroupName));
      }

      for (ParameterElementDeclaration parameterElement : parameterGroupElement.getParameters()) {
        final String parameterName = parameterElement.getName();
        final ParameterModel parameterModel = parameterGroupModel.getParameter(parameterName)
            .orElseThrow(() -> new ZernovaRuntimeException(createStaticMessage("Could not find parameter with name: '%s' in parameter group: '%s'",
                                                                            parameterName, parameterGroupName)));
        parametersMap.put(parameterName, asDataWeaveExpression(parameterElement.getValue(), parameterModel.getType()));
      }
    }
    return parametersMap;
  }

}
