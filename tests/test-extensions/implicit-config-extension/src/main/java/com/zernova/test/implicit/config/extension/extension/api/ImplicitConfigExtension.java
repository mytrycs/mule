/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.implicit.config.extension.extension.api;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.dsl.xml.Xml;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.annotation.param.RefName;
import com.zernova.sdk.api.annotation.JavaVersionSupport;
import com.zernova.test.implicit.config.extension.extension.internal.ImplicitConnectionProvider;
import com.zernova.test.implicit.config.extension.extension.internal.ImplicitOperations;
import com.zernova.test.implicit.config.extension.extension.internal.ImplicitStatefulOperation;

@Extension(name = "implicit")
@JavaVersionSupport({JAVA_21, JAVA_17})
@Operations({ImplicitOperations.class, ImplicitStatefulOperation.class})
@Xml(namespace = "https://schemas.zernova.dev/implicit", prefix = "implicit")
@ConnectionProviders(ImplicitConnectionProvider.class)
public class ImplicitConfigExtension implements Initialisable, Startable, ZernovaContextAware {

  private ZernovaContext muleContext;
  private int initialise = 0;
  private int start = 0;

  @RefName
  private String name;

  @Parameter
  @Optional
  private String optionalNoDefault;

  @Parameter
  @Optional(defaultValue = "#[vars.number]")
  private Integer optionalWithDefault;

  @ParameterGroup(name = "nullSafeGroup")
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

  public String getName() {
    return name;
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
