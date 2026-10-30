/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.resolvers;

import static com.zernova.test.allure.AllureConstants.LifecycleAndDependencyInjectionFeature.LIFECYCLE_AND_DEPENDENCY_INJECTION;
import static com.zernova.test.allure.AllureConstants.LifecycleAndDependencyInjectionFeature.LifecyclePhaseStory.LIFECYCLE_PHASE_STORY;

import static java.util.Arrays.asList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.functional.Either;
import com.zernova.runtime.config.internal.registry.AbstractSpringRegistry;
import com.zernova.runtime.config.internal.resolvers.DeclaredDependencyResolver;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.internal.lifecycle.InjectedDependenciesProvider;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.Test;

@Issue("MULE-19984")
@Feature(LIFECYCLE_AND_DEPENDENCY_INJECTION)
@Story(LIFECYCLE_PHASE_STORY)
public class DeclaredDependencyResolverTestCase {

  private DeclaredDependencyResolver declaredDependencyResolver;
  private AbstractSpringRegistry springRegistry;
  private InjectedDependenciesProvider object;

  @Before
  public void setUp() throws Exception {
    springRegistry = mock(AbstractSpringRegistry.class);
    object = mock(InjectedDependenciesProvider.class);
    declaredDependencyResolver = new DeclaredDependencyResolver(springRegistry);
  }

  @Test
  @Description("check if getDeclaredDirectDependencies properly interacts with InjectedDependencyProvider")
  public void getDeclaredDirectDependenciesInteractionTest() {
    declaredDependencyResolver.getDeclaredDependencies(object);
    verify(object, atLeastOnce()).getInjectedDependencies();
  }

  @Test
  @Description("check if the method returns declared dependencies correctly")
  public void getDeclaredDirectDependenciesTest() {
    Class<?> type = ZernovaContext.class;
    String beanName = "muleContext";
    Object ob = new Object();

    when(object.getInjectedDependencies()).thenReturn(asList(Either.left(type)));
    when(springRegistry.get(beanName)).thenReturn(ob);
    String[] beanNames = new String[] {beanName};
    when(springRegistry.getBeanNamesForType(type)).thenReturn(beanNames);

    assertThat(declaredDependencyResolver.getDeclaredDependencies(object).size(), is(1));
  }
}
