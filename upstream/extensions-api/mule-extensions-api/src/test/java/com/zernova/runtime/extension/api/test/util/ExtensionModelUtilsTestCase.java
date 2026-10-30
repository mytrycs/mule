/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA compatibility addition Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.extension.api.test.util;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.extension.api.util.ExtensionModelUtils.componentHasAnImplicitConfiguration;
import static com.zernova.runtime.extension.api.util.ExtensionModelUtils.getDefaultValue;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.extension.api.property.NoImplicitModelProperty;

import java.lang.reflect.AccessibleObject;

import org.junit.Test;

public class ExtensionModelUtilsTestCase {

  private final static String DEFAULT_VALUE = "DEFAULT_VALUE";

  @Test
  public void componentWithImplicitAssociatedConfig() {
    ExtensionModel em = mock(ExtensionModel.class);
    OperationModel ope = mock(OperationModel.class);
    ConfigurationModel c = mock(ConfigurationModel.class);
    ConnectionProviderModel cp = mock(ConnectionProviderModel.class);
    ParameterModel param = mock(ParameterModel.class);
    when(param.isRequired()).thenReturn(false);
    when(cp.getAllParameterModels()).thenReturn(singletonList(param));
    when(c.getAllParameterModels()).thenReturn(emptyList());
    when(c.getOperationModels()).thenReturn(singletonList(ope));
    when(c.getConnectionProviders()).thenReturn(singletonList(cp));
    when(em.getConfigurationModels()).thenReturn(singletonList(c));

    assertThat(componentHasAnImplicitConfiguration(em, ope), is(true));
  }

  @Test
  public void componentWithConfigWithoutConnectionProviders() {
    ExtensionModel em = mock(ExtensionModel.class);
    SourceModel source = mock(SourceModel.class);
    ConfigurationModel c = mock(ConfigurationModel.class);
    when(c.getAllParameterModels()).thenReturn(emptyList());
    when(c.getSourceModels()).thenReturn(singletonList(source));
    when(em.getConfigurationModels()).thenReturn(singletonList(c));

    assertThat(componentHasAnImplicitConfiguration(em, source), is(true));
  }

  @Test
  public void componentWithImplicitConfigWithNonImplicitConnectionProviders() {
    ExtensionModel em = mock(ExtensionModel.class);
    OperationModel ope = mock(OperationModel.class);
    ConfigurationModel c = mock(ConfigurationModel.class);
    ConnectionProviderModel cp = mock(ConnectionProviderModel.class);
    ParameterModel param = mock(ParameterModel.class);
    when(param.isRequired()).thenReturn(true);
    when(cp.getAllParameterModels()).thenReturn(singletonList(param));
    when(c.getAllParameterModels()).thenReturn(emptyList());
    when(c.getOperationModels()).thenReturn(singletonList(ope));
    when(c.getConnectionProviders()).thenReturn(singletonList(cp));
    when(em.getConfigurationModels()).thenReturn(singletonList(c));

    assertThat(componentHasAnImplicitConfiguration(em, ope), is(false));
  }

  @Test
  public void componentWithNoImplicitConfigs() {
    ExtensionModel em = mock(ExtensionModel.class);
    SourceModel source = mock(SourceModel.class);
    ConfigurationModel c = mock(ConfigurationModel.class);
    ParameterModel param = mock(ParameterModel.class);
    when(param.isRequired()).thenReturn(true);
    when(c.getAllParameterModels()).thenReturn(singletonList(param));
    when(c.getSourceModels()).thenReturn(singletonList(source));
    when(em.getConfigurationModels()).thenReturn(singletonList(c));

    assertThat(componentHasAnImplicitConfiguration(em, source), is(false));
  }

  @Test
  public void componentWithImplicitConfigs() {
    ExtensionModel em = mock(ExtensionModel.class);
    SourceModel source = mock(SourceModel.class);
    ConfigurationModel c = mock(ConfigurationModel.class);
    ParameterModel param = mock(ParameterModel.class);
    when(param.isRequired()).thenReturn(false);
    when(c.getAllParameterModels()).thenReturn(singletonList(param));
    when(c.getSourceModels()).thenReturn(singletonList(source));
    when(em.getConfigurationModels()).thenReturn(singletonList(c));

    assertThat(componentHasAnImplicitConfiguration(em, source), is(true));
  }

  @Test
  public void componentWithForcedNoImplicit() {
    ExtensionModel em = mock(ExtensionModel.class);
    SourceModel source = mock(SourceModel.class);
    ConfigurationModel c = mock(ConfigurationModel.class);
    when(c.getModelProperty(NoImplicitModelProperty.class)).thenReturn(of(new NoImplicitModelProperty()));
    ParameterModel param = mock(ParameterModel.class);
    when(param.isRequired()).thenReturn(false);
    when(c.getAllParameterModels()).thenReturn(singletonList(param));
    when(c.getSourceModels()).thenReturn(singletonList(source));
    when(em.getConfigurationModels()).thenReturn(singletonList(c));

    assertThat(componentHasAnImplicitConfiguration(em, source), is(false));
  }

  @Test
  public void accesibleObjectDeprecatedOptionalDefaultValue() {
    com.zernova.runtime.extension.api.annotation.param.Optional optional =
        mock(com.zernova.runtime.extension.api.annotation.param.Optional.class);
    when(optional.defaultValue()).thenReturn(DEFAULT_VALUE);
    AccessibleObject object = mock(AccessibleObject.class);
    when(object.getAnnotation(com.zernova.runtime.extension.api.annotation.param.Optional.class)).thenReturn(optional);

    assertThat(getDefaultValue(object), is(DEFAULT_VALUE));
  }

  @Test
  public void accesibleObjectOptionalDefaultValue() {
    com.zernova.sdk.api.annotation.param.Optional optional = mock(com.zernova.sdk.api.annotation.param.Optional.class);
    when(optional.defaultValue()).thenReturn(DEFAULT_VALUE);
    AccessibleObject object = mock(AccessibleObject.class);
    when(object.getAnnotation(com.zernova.sdk.api.annotation.param.Optional.class)).thenReturn(optional);

    assertThat(getDefaultValue(object), is(DEFAULT_VALUE));
  }

  @Test
  public void accesibleObjectOptionalDefaultValueWithBothAnnotations() {
    com.zernova.sdk.api.annotation.param.Optional optional = mock(com.zernova.sdk.api.annotation.param.Optional.class);
    when(optional.defaultValue()).thenReturn(DEFAULT_VALUE);
    com.zernova.runtime.extension.api.annotation.param.Optional deprecatedOptional =
        mock(com.zernova.runtime.extension.api.annotation.param.Optional.class);
    when(deprecatedOptional.defaultValue()).thenReturn("SOMETHING");
    AccessibleObject object = mock(AccessibleObject.class);
    when(object.getAnnotation(com.zernova.sdk.api.annotation.param.Optional.class)).thenReturn(optional);
    when(object.getAnnotation(com.zernova.runtime.extension.api.annotation.param.Optional.class)).thenReturn(deprecatedOptional);

    assertThat(getDefaultValue(object), is(DEFAULT_VALUE));
  }

  @Test
  public void zernovaUnboundedRouteSupportsMultiple() {
    com.zernova.runtime.api.meta.model.nested.NestedRouteModel route = mock(com.zernova.runtime.api.meta.model.nested.NestedRouteModel.class);
    when(route.getMaxOccurs()).thenReturn(java.util.Optional.empty());
    assertThat(com.zernova.runtime.extension.api.util.ExtensionModelUtils.supportsMultiple(route), is(true));
  }

  @Test
  public void zernovaSingleRouteDoesNotSupportMultiple() {
    com.zernova.runtime.api.meta.model.nested.NestedRouteModel route = mock(com.zernova.runtime.api.meta.model.nested.NestedRouteModel.class);
    when(route.getMaxOccurs()).thenReturn(java.util.Optional.of(1));
    assertThat(com.zernova.runtime.extension.api.util.ExtensionModelUtils.supportsMultiple(route), is(false));
  }

  @Test
  public void zernovaZeroRouteDoesNotSupportMultiple() {
    com.zernova.runtime.api.meta.model.nested.NestedRouteModel route = mock(com.zernova.runtime.api.meta.model.nested.NestedRouteModel.class);
    when(route.getMaxOccurs()).thenReturn(java.util.Optional.of(0));
    assertThat(com.zernova.runtime.extension.api.util.ExtensionModelUtils.supportsMultiple(route), is(false));
  }

  @Test
  public void zernovaRepeatedRouteSupportsMultiple() {
    com.zernova.runtime.api.meta.model.nested.NestedRouteModel route = mock(com.zernova.runtime.api.meta.model.nested.NestedRouteModel.class);
    when(route.getMaxOccurs()).thenReturn(java.util.Optional.of(2));
    assertThat(com.zernova.runtime.extension.api.util.ExtensionModelUtils.supportsMultiple(route), is(true));
  }
}
