/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import static java.util.Collections.emptyList;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.internal.routing.ChoiceRouter;
import com.zernova.runtime.core.internal.routing.ProcessorExpressionRoute;
import com.zernova.runtime.core.internal.routing.ProcessorRoute;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

import java.util.Collection;

import jakarta.inject.Inject;

public class ChoiceRouterObjectFactory extends AbstractComponentFactory<ChoiceRouter> {

  @Inject
  private ZernovaContext muleContext;

  @Inject
  private ComponentTracerFactory componentTracerFactory;

  private Processor defaultProcessor;
  private Collection<ProcessorExpressionRoute> conditionalMessageProcessors = emptyList();

  public ChoiceRouterObjectFactory() {
    super();
  }

  public Class<?> getObjectType() {
    return ChoiceRouter.class;
  }

  public void setDefaultRoute(ProcessorRoute conditionalProcessor) {
    defaultProcessor = conditionalProcessor.getProcessor();
  }

  public void setRoutes(Collection<ProcessorExpressionRoute> conditionalMessageProcessors) {
    this.conditionalMessageProcessors = conditionalMessageProcessors;
  }

  @Override
  public ChoiceRouter doGetObject() throws Exception {
    final ChoiceRouter router = new ChoiceRouter(componentTracerFactory);
    router.setAnnotations(getAnnotations());
    router.setDefaultRoute(defaultProcessor);
    router.setMuleContext(muleContext);

    for (final ProcessorExpressionRoute mper : conditionalMessageProcessors) {
      router.addRoute(mper.getExpression(), mper.getProcessor());
    }

    return router;
  }

}
