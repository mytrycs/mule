/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.config.dsl;

import static com.zernova.runtime.core.privileged.processor.MessageProcessors.newChain;
import static com.zernova.runtime.core.privileged.processor.chain.UnnamedComponent.getUnnamedComponent;
import static com.zernova.runtime.extension.api.annotation.param.Optional.PAYLOAD;
import static com.zernova.runtime.module.extension.api.runtime.privileged.ChildContextChain.CHAIN_OWNER_LOCATION_KEY;
import static com.zernova.runtime.tracer.customization.api.InternalSpanNames.MESSAGE_PROCESSORS_SPAN_NAME;

import static java.util.Optional.empty;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.nested.NestedChainModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.module.extension.internal.runtime.ExtensionComponent;
import com.zernova.runtime.module.extension.internal.runtime.operation.ComponentMessageProcessor;
import com.zernova.runtime.module.extension.internal.runtime.operation.ComponentMessageProcessorBuilder;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ProcessorChainValueResolver;
import com.zernova.runtime.tracer.api.component.ComponentTracerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import javax.xml.namespace.QName;

/**
 * A base {@link AbstractExtensionObjectFactory} for producers of {@link ExtensionComponent} instances
 *
 * @since 4.0
 */
public abstract class ComponentMessageProcessorObjectFactory<M extends ComponentModel, P extends ComponentMessageProcessor>
    extends AbstractExtensionObjectFactory<P> {

  @Inject
  private ComponentTracerFactory<CoreEvent> componentTracerFactory;

  @Inject
  private StreamingManager streamingManager;

  protected final ExtensionModel extensionModel;
  protected final M componentModel;
  protected ConfigurationProvider configurationProvider;
  protected String target = EMPTY;
  protected String targetValue = PAYLOAD;
  protected CursorProviderFactory cursorProviderFactory;
  protected RetryPolicyTemplate retryPolicyTemplate;
  protected List<Processor> nestedProcessors;

  public ComponentMessageProcessorObjectFactory(ExtensionModel extensionModel,
                                                M componentModel,
                                                ZernovaContext muleContext) {
    super(muleContext);
    this.extensionModel = extensionModel;
    this.componentModel = componentModel;
  }

  @Override
  public P doGetObject() {
    final MessageProcessorChain chain;

    if (nestedProcessors != null) {
      chain = newChain(empty(), nestedProcessors,
                       componentTracerFactory
                           .fromComponent(getUnnamedComponent(), MESSAGE_PROCESSORS_SPAN_NAME, ""));
      componentModel.getNestedComponents().stream()
          .filter(NestedChainModel.class::isInstance)
          .findFirst()
          .ifPresent(chainModel -> parameters.put(chainModel.getName(),
                                                  new ProcessorChainValueResolver(chainModel, chain, streamingManager)));

      // For MULE-18771 we need access to the chain's location to create a new event and sdk context
      // Update for W-15158118: since the scope and the chain were having the same location, the chain was overriding the
      // registration of the scope in the ComponentLocator. To avoid such issues, we are not adding the location as the location
      // key but as another annotation.
      Map<QName, Object> annotations = new HashMap<>(this.getAnnotations());
      ComponentLocation chainLocation = this.getLocation();
      annotations.put(LOCATION_KEY, ((DefaultComponentLocation) chainLocation).appendProcessorsPart());
      annotations.put(CHAIN_OWNER_LOCATION_KEY, chainLocation);
      chain.setAnnotations(annotations);
    } else {
      chain = null;
    }

    return getMessageProcessorBuilder()
        .setConfigurationProvider(configurationProvider)
        .setParameters(parameters)
        .setTarget(target)
        .setTargetValue(targetValue)
        .setCursorProviderFactory(cursorProviderFactory)
        .setRetryPolicyTemplate(retryPolicyTemplate)
        .setNestedChain(chain)
        .setClassLoader(Thread.currentThread().getContextClassLoader())
        .build();
  }

  protected abstract ComponentMessageProcessorBuilder<M, P> getMessageProcessorBuilder();

  public void setConfigurationProvider(ConfigurationProvider configuration) {
    this.configurationProvider = configuration;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public void setTargetValue(String targetValue) {
    this.targetValue = targetValue;
  }

  public void setCursorProviderFactory(CursorProviderFactory cursorProviderFactory) {
    this.cursorProviderFactory = cursorProviderFactory;
  }

  public void setRetryPolicyTemplate(RetryPolicyTemplate retryPolicyTemplate) {
    this.retryPolicyTemplate = retryPolicyTemplate;
  }

  public void setNestedProcessors(List<Processor> processors) {
    this.nestedProcessors = processors;
  }

}
