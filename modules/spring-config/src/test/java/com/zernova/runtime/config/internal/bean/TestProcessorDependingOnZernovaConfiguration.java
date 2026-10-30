/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.bean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;

import jakarta.inject.Inject;

/**
 * A simple {@link Processor} that has the {@link ZernovaConfiguration} as an injected dependency.
 * <p>
 * Needs to also implement {@link Component} in order to avoid an error when creating a dynamic subclass to add the annotations,
 * because this package is not exported and the dynamic subclass is created with a different module (currently the unnamed).
 */
public class TestProcessorDependingOnZernovaConfiguration extends AbstractComponent implements Processor {

  @javax.inject.Inject
  public ZernovaConfiguration muleConfigurationJavax;

  @Inject
  public ZernovaConfiguration muleConfiguration;

  @Override
  public CoreEvent process(CoreEvent event) {
    assertThat(muleConfiguration, sameInstance(muleConfigurationJavax));

    return CoreEvent.builder(event).message(Message.of(muleConfiguration.getDefaultErrorHandlerName())).build();
  }
}
