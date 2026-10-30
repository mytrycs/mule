/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.config;

import static com.zernova.runtime.api.meta.Category.SELECT;
import static com.zernova.runtime.api.meta.ExternalLibraryType.NATIVE;
import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.getExtensionModel;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.getConfigurationInstanceFromRegistry;

import static java.util.Collections.singleton;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.zernova.functional.junit4.ExtensionFunctionalTestCase;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import com.zernova.runtime.extension.api.annotation.Export;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.ExternalLib;
import com.zernova.runtime.extension.api.annotation.OnException;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.Sources;
import com.zernova.runtime.extension.api.annotation.SubTypeMapping;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.sdk.api.annotation.error.ErrorTypes;
import com.zernova.test.heisenberg.extension.AsyncHeisenbergSource;
import com.zernova.test.heisenberg.extension.DEARadioSource;
import com.zernova.test.heisenberg.extension.HeisenbergConnectionProvider;
import com.zernova.test.heisenberg.extension.HeisenbergErrors;
import com.zernova.test.heisenberg.extension.HeisenbergExtension;
import com.zernova.test.heisenberg.extension.HeisenbergOperations;
import com.zernova.test.heisenberg.extension.HeisenbergRouters;
import com.zernova.test.heisenberg.extension.HeisenbergScopes;
import com.zernova.test.heisenberg.extension.HeisenbergSource;
import com.zernova.test.heisenberg.extension.KillingOperations;
import com.zernova.test.heisenberg.extension.MoneyLaunderingOperation;
import com.zernova.test.heisenberg.extension.ReconnectableHeisenbergSource;
import com.zernova.test.heisenberg.extension.SecureHeisenbergConnectionProvider;
import com.zernova.test.heisenberg.extension.exception.HeisenbergConnectionExceptionEnricher;
import com.zernova.test.heisenberg.extension.exception.HeisenbergException;
import com.zernova.test.heisenberg.extension.model.CarDealer;
import com.zernova.test.heisenberg.extension.model.CarWash;
import com.zernova.test.heisenberg.extension.model.Investment;
import com.zernova.test.heisenberg.extension.model.Ricin;
import com.zernova.test.heisenberg.extension.model.Weapon;

import java.util.List;

import jakarta.inject.Inject;

import org.junit.Test;

public class ConfigurationInjectionLifecycleTestCase extends ExtensionFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "heisenberg-io-config.xml";
  }

  @Override
  protected Class<?>[] getAnnotatedExtensionClasses() {
    return new Class<?>[] {DIHeisenbergExtension.class};
  }

  @Override
  protected void addBuilders(List<ConfigurationBuilder> builders) {
    builders.add(new AbstractConfigurationBuilder() {

      @Override
      protected void doConfigure(ZernovaContext muleContext) throws Exception {
        muleContext.getCustomizationService().registerCustomServiceClass("lifecycleTracker", LifecycleTracker.class);
      }
    });
    builders.add(extensionManagerWithMuleExtModelBuilder(singleton(getExtensionModel())));
    super.addBuilders(builders);
  }

  @Test
  public void injectedDependencyLifecycle() throws Exception {
    DIHeisenbergExtension heisenberg =
        (DIHeisenbergExtension) getConfigurationInstanceFromRegistry("heisenberg", testEvent(), muleContext).getValue();
    LifecycleTracker tracker = heisenberg.getLifecycleTracker();

    assertThat(tracker.initialise, is(1));
    assertThat(tracker.start, is(1));
  }

  public static class LifecycleTracker implements Initialisable, Startable {

    private int initialise, start;

    @Override
    public void initialise() throws InitialisationException {
      initialise++;
    }

    @Override
    public void start() throws ZernovaException {
      start++;
    }
  }

  @Extension(name = HeisenbergExtension.HEISENBERG, category = SELECT)
  @Operations({HeisenbergOperations.class, MoneyLaunderingOperation.class,
      KillingOperations.class, HeisenbergScopes.class, HeisenbergRouters.class})
  @OnException(HeisenbergConnectionExceptionEnricher.class)
  @ConnectionProviders({HeisenbergConnectionProvider.class, SecureHeisenbergConnectionProvider.class})
  @Sources({HeisenbergSource.class, DEARadioSource.class, AsyncHeisenbergSource.class})
  @com.zernova.sdk.api.annotation.Sources({ReconnectableHeisenbergSource.class})
  @Export(classes = {HeisenbergExtension.class, HeisenbergException.class}, resources = "methRecipe.json")
  @SubTypeMapping(baseType = Weapon.class, subTypes = {Ricin.class})
  @SubTypeMapping(baseType = Investment.class, subTypes = {CarWash.class, CarDealer.class})
  @ExternalLib(name = HeisenbergExtension.HEISENBERG_LIB_NAME, description = HeisenbergExtension.HEISENBERG_LIB_DESCRIPTION,
      nameRegexpMatcher = HeisenbergExtension.HEISENBERG_LIB_FILE_NAME,
      requiredClassName = HeisenbergExtension.HEISENBERG_LIB_CLASS_NAME, type = NATIVE,
      coordinates = "com.zernova.libs:this-is-a-lib:dll:1.0.0")
  @ErrorTypes(HeisenbergErrors.class)
  public static class DIHeisenbergExtension extends HeisenbergExtension {

    @Inject
    private LifecycleTracker lifecycleTracker;

    @Override
    public void initialise() throws InitialisationException {
      if (lifecycleTracker.initialise != 1) {
        throw new IllegalStateException("initialise was " + lifecycleTracker.initialise);
      }
      super.initialise();
    }

    public LifecycleTracker getLifecycleTracker() {
      return lifecycleTracker;
    }
  }
}
