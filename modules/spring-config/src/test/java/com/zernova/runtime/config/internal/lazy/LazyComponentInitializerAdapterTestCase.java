/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.lazy;

import static com.zernova.runtime.api.component.location.Location.builderFromStringRepresentation;
import static com.zernova.runtime.app.declaration.api.fluent.ElementDeclarer.forExtension;
import static com.zernova.runtime.app.declaration.api.fluent.ElementDeclarer.newArtifact;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.FLOW_ELEMENT_IDENTIFIER;
import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.MULE_NAME;
import static com.zernova.test.allure.AllureConstants.ConfigurationComponentLocatorFeature.CONFIGURATION_COMPONENT_LOCATOR;
import static com.zernova.test.allure.AllureConstants.ConfigurationComponentLocatorFeature.ComponentLifeCycle.COMPONENT_LIFE_CYCLE;
import static com.zernova.test.allure.AllureConstants.LazyInitializationFeature.LAZY_INITIALIZATION;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.app.declaration.api.ArtifactDeclaration;
import com.zernova.runtime.config.api.LazyComponentInitializer.ComponentLocationFilter;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Ignore;
import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Features;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@Features({@Feature(LAZY_INITIALIZATION), @Feature(CONFIGURATION_COMPONENT_LOCATOR)})
@Story(COMPONENT_LIFE_CYCLE)
public class LazyComponentInitializerAdapterTestCase extends AbstractLazyZernovaArtifactContextTestCase {

  private final AtomicInteger initializations = new AtomicInteger(0);

  @Override
  protected void onProcessorInitialization() {
    initializations.incrementAndGet();
  }

  @Test
  @Issue("MULE-17400")
  public void shouldNotCreateBeansForSameLocationRequest() {
    Location location = builderFromStringRepresentation(MY_FLOW).build();

    lazyMuleArtifactContext.initializeComponent(location);
    lazyMuleArtifactContext.initializeComponent(location);

    assertThat(initializations.get(), is(1));

  }

  @Test
  @Issue("MULE-17400")
  public void shouldCreateBeansForSameLocationRequestIfDifferentPhaseApplied() {
    Location location = builderFromStringRepresentation(MY_FLOW).build();

    lazyMuleArtifactContext.initializeComponent(location, false);
    lazyMuleArtifactContext.initializeComponent(location);

    assertThat(initializations.get(), is(2));
  }

  @Test
  @Issue("MULE-17400")
  public void shouldNotCreateBeansForSameLocationFilterRequest() {
    ComponentLocationFilter filter = loc -> loc.getLocation().equals(MY_FLOW);

    lazyMuleArtifactContext.initializeComponents(filter);
    lazyMuleArtifactContext.initializeComponents(filter);

    assertThat(initializations.get(), is(1));
  }

  @Test
  @Issue("MULE-17400")
  public void shouldCreateBeansForSameLocationFilterRequestIfDifferentPhaseApplied() {
    ComponentLocationFilter filter = loc -> loc.getLocation().equals(MY_FLOW);

    lazyMuleArtifactContext.initializeComponents(filter, false);
    lazyMuleArtifactContext.initializeComponents(filter);

    assertThat(initializations.get(), is(2));
  }

  @Override
  protected ArtifactDeclaration getArtifactDeclaration() {
    return newArtifact()
        .withGlobalElement(forExtension(MULE_NAME)
            .newConstruct(FLOW_ELEMENT_IDENTIFIER)
            .withRefName(MY_FLOW)
            .getDeclaration())
        .getDeclaration();
  }
}
