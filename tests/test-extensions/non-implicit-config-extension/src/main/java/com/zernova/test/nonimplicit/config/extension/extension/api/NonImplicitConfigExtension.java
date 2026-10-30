/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.nonimplicit.config.extension.extension.api;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.NoImplicit;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.dsl.xml.Xml;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.sdk.api.annotation.JavaVersionSupport;
import com.zernova.test.nonimplicit.config.extension.extension.internal.NonImplicitConnectionProvider;
import com.zernova.test.nonimplicit.config.extension.extension.internal.NonImplicitOperations;

@Extension(name = "Non Implicit")
@JavaVersionSupport({JAVA_21, JAVA_17})
@Operations({NonImplicitOperations.class})
@Xml(namespace = "http://www.mulesoft.org/schema/mule/implicit", prefix = "non-implicit")
@ConnectionProviders(NonImplicitConnectionProvider.class)
@NoImplicit
public class NonImplicitConfigExtension implements Initialisable, Startable, ZernovaContextAware {

  private ZernovaContext muleContext;
  private int initialise = 0;
  private int start = 0;

  @Parameter
  @Optional
  private String optionalNoDefault;

  @Parameter
  @Optional(defaultValue = "#[vars.number]")
  private Integer optionalWithDefault;

  @com.zernova.sdk.api.annotation.param.ParameterGroup(name = "nullSafeGroup")
  private NullSafeParameterGroup nullSafeGroup;

  @ParameterGroup(name = "nullSafeGroupShowInDsl", showInDsl = true)
  private NullSafeParameterGroupShowInDsl nullSafeGroupShowInDsl;

  @Override
  public void initialise() throws InitialisationException {
    initialise++;
  }

  @Override
  public void setMuleContext(ZernovaContext context) {
    muleContext = context;
  }

  @Override
  public void start() throws ZernovaException {
    start++;
  }

  public ZernovaContext getMuleContext() {
    return muleContext;
  }

  public int getInitialise() {
    return initialise;
  }

  public int getStart() {
    return start;
  }

  public String getOptionalNoDefault() {
    return optionalNoDefault;
  }

  public Integer getOptionalWithDefault() {
    return optionalWithDefault;
  }

  public NullSafeParameterGroup getNullSafeGroup() {
    return nullSafeGroup;
  }

  public void setNullSafeGroup(NullSafeParameterGroup nullSafeGroup) {
    this.nullSafeGroup = nullSafeGroup;
  }

  public NullSafeParameterGroupShowInDsl getNullSafeGroupShowInDsl() {
    return nullSafeGroupShowInDsl;
  }

  public void setNullSafeGroupShowInDsl(NullSafeParameterGroupShowInDsl nullSafeGroupShowInDsl) {
    this.nullSafeGroupShowInDsl = nullSafeGroupShowInDsl;
  }
}
