/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.junit4;

import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_CLASSLOADER_REPOSITORY;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.getExtensionModel;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.manifest.api.ZernovaManifest.getMuleManifest;
import static com.zernova.runtime.module.extension.internal.loader.java.AbstractJavaExtensionModelLoader.TYPE_PROPERTY_NAME;
import static com.zernova.runtime.module.extension.internal.loader.java.AbstractJavaExtensionModelLoader.VERSION;

import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;

import static org.mockito.Mockito.mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.zernova.runtime.api.config.custom.CustomizationService;
import com.zernova.runtime.api.config.custom.ServiceConfigurator;
import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService;
import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.api.config.DefaultZernovaConfiguration;
import com.zernova.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import com.zernova.runtime.core.api.context.DefaultZernovaContextFactory;
import com.zernova.runtime.core.api.context.ZernovaContextBuilder;
import com.zernova.runtime.core.api.context.ZernovaContextFactory;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderRepository;
import com.zernova.runtime.module.extension.internal.loader.java.CraftedExtensionModelLoader;
import com.zernova.runtime.module.extension.internal.loader.java.DefaultJavaExtensionModelLoader;
import com.zernova.runtime.module.extension.internal.manager.DefaultExtensionManager;
import com.zernova.tck.config.TestNotificationListenerRegistryConfigurationBuilder;
import com.zernova.tck.config.TestPolicyProviderConfigurationBuilder;
import com.zernova.tck.config.TestServicesConfigurationBuilder;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.junit4.rule.SystemProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;

public abstract class AbstractConfigurationFailuresTestCase extends AbstractZernovaTestCase {

  @Rule
  public SystemProperty jvmVersionExtensionEnforcementLoose =
      new SystemProperty("mule.jvm.version.extension.enforcement", "LOOSE");

  @Rule
  public TestServicesConfigurationBuilder testServicesConfigurationBuilder =
      new TestServicesConfigurationBuilder(true, mockExpressionExecutor(), true);

  protected void loadConfiguration(String configuration) throws ZernovaException, InterruptedException {

    ZernovaContextFactory muleContextFactory = new DefaultZernovaContextFactory();
    List<ConfigurationBuilder> builders = new ArrayList<>();
    builders.add(new AbstractConfigurationBuilder() {

      @Override
      protected void doConfigure(ZernovaContext muleContext) throws Exception {
        DefaultExtensionManager defaultExtensionManager = new DefaultExtensionManager();
        initialiseIfNeeded(defaultExtensionManager, muleContext);
        getRequiredExtensions().forEach(defaultExtensionManager::registerExtension);
        muleContext.setExtensionManager(defaultExtensionManager);
      }
    });
    ConfigurationBuilder configurationBuilder = new ArtifactAstXmlParserConfigurationBuilder(emptyMap(),
                                                                                             disableXmlValidations(),
                                                                                             enableLazyInit(),
                                                                                             false,
                                                                                             false,
                                                                                             new String[] {configuration},
                                                                                             getExpressionLanguageMetadataService());
    configurationBuilder.addServiceConfigurator(testServicesConfigurationBuilder);
    builders.add(configurationBuilder);
    builders.add(testServicesConfigurationBuilder);
    configurationBuilder.addServiceConfigurator(new ServiceConfigurator() {

      @Override
      public void configure(CustomizationService customizationService) {
        customizationService.registerCustomServiceImpl(OBJECT_CLASSLOADER_REPOSITORY,
                                                       mock(ClassLoaderRepository.class),
                                                       true);
      }
    });
    builders.add(new TestPolicyProviderConfigurationBuilder());
    final Latch contextStartedLatch = new Latch();
    builders.add(new TestNotificationListenerRegistryConfigurationBuilder(contextStartedLatch));
    ZernovaContextBuilder contextBuilder = ZernovaContextBuilder.builder(APP);
    final DefaultZernovaConfiguration muleConfiguration = new DefaultZernovaConfiguration();
    muleConfiguration.setId(AbstractConfigurationFailuresTestCase.class.getSimpleName());
    applyConfiguration(muleConfiguration);
    contextBuilder.setMuleConfiguration(muleConfiguration);
    // Simulate the classloader for the app
    contextBuilder.setExecutionClassLoader(new ClassLoader(currentThread().getContextClassLoader()) {});
    ZernovaContextWithRegistry muleContext =
        (ZernovaContextWithRegistry) muleContextFactory.createMuleContext(builders, contextBuilder);
    muleContext.start();
    try {
      assertThat(contextStartedLatch.await(20, SECONDS), is(true));
    } finally {
      muleContext.stop();
      muleContext.dispose();
    }
  }

  protected ExpressionLanguageMetadataService getExpressionLanguageMetadataService() {
    return mock(ExpressionLanguageMetadataService.class);
  }

  protected void applyConfiguration(DefaultZernovaConfiguration muleConfiguration) {
    // nothing to do by default
  }

  protected boolean disableXmlValidations() {
    return false;
  }

  protected boolean enableLazyInit() {
    return false;
  }

  protected boolean mockExpressionExecutor() {
    return true;
  }

  protected List<ExtensionModel> getRequiredExtensions() {
    return singletonList(getExtensionModel());
  }

  protected ExtensionModel loadExtension(Class extension, Set<ExtensionModel> deps) {
    DefaultJavaExtensionModelLoader loader = new DefaultJavaExtensionModelLoader();
    return loadExtensionWithLoader(extension, deps, loader);
  }

  protected ExtensionModel loadExtensionWithDelegate(Class extension, Set<ExtensionModel> deps) {
    CraftedExtensionModelLoader loader = new CraftedExtensionModelLoader();
    return loadExtensionWithLoader(extension, deps, loader);
  }

  protected ExtensionModel loadExtensionWithLoader(Class extension, Set<ExtensionModel> deps,
                                                   ExtensionModelLoader extensionModelLoader) {
    Map<String, Object> ctx = new HashMap<>();
    ctx.put(TYPE_PROPERTY_NAME, extension.getName());
    ctx.put(VERSION, getMuleManifest().getProductVersion());
    ctx.putAll(getExtensionLoaderContextAdditionalParameters());
    return extensionModelLoader.loadExtensionModel(currentThread().getContextClassLoader(), DslResolvingContext.getDefault(deps),
                                                   ctx);
  }

  /**
   * Subclasses can override this method so that extension models are generated with an extension loading context that contains
   * the parameters returned by this method.
   *
   * @return a map with parameters to be added to the extension loader context.
   */
  protected Map<String, Object> getExtensionLoaderContextAdditionalParameters() {
    return emptyMap();
  }

}
