/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.execution;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.getParameter;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.getResolver;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.mockConfigurationInstance;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSet;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.loader.java.property.ParameterGroupModelProperty;
import com.zernova.runtime.module.extension.internal.runtime.config.ConfigurationObjectBuilder;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationObjectBuilderTestCase extends AbstractZernovaTestCase {

  private static final String NAME_VALUE = "name";
  private static final String DESCRIPTION_VALUE = "description";

  public static final ParameterModel nameParameterModel = getParameter("name", String.class);
  public static final ParameterModel descriptionParameterModel = getParameter("description", String.class);

  public static ResolverSet createResolverSet() throws Exception {
    ResolverSet resolverSet = new ResolverSet(mock(ZernovaContext.class));
    resolverSet.add(nameParameterModel.getName(), getResolver(NAME_VALUE));
    resolverSet.add(descriptionParameterModel.getName(), getResolver(DESCRIPTION_VALUE));

    return resolverSet;
  }

  @Mock(answer = RETURNS_DEEP_STUBS, lenient = true)
  private ConfigurationModel configurationModel;

  @Mock
  private CoreEvent event;

  @Mock
  private ExpressionManager expressionManager;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private ZernovaContext context;

  private ConfigurationObjectBuilder<TestConfig> configurationObjectBuilder;
  private ResolverSet resolverSet;


  @Before
  public void before() throws Exception {
    when(configurationModel.getAllParameterModels()).thenReturn(asList(nameParameterModel, descriptionParameterModel));
    mockConfigurationInstance(configurationModel, new TestConfig());
    when(configurationModel.getModelProperty(ParameterGroupModelProperty.class)).thenReturn(Optional.empty());

    resolverSet = createResolverSet();

    configurationObjectBuilder = new ConfigurationObjectBuilder<>(configurationModel, resolverSet, expressionManager, context);
  }

  @Test
  public void build() throws ZernovaException {
    TestConfig testConfig = configurationObjectBuilder.build(ValueResolvingContext.builder(event)
        .withExpressionManager(expressionManager)
        .build()).getFirst();
    assertThat(testConfig.getName(), is(NAME_VALUE));
    assertThat(testConfig.getDescription(), is(DESCRIPTION_VALUE));
  }

  public static class TestConfig {

    private String name;
    private String description;

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }
  }
}
