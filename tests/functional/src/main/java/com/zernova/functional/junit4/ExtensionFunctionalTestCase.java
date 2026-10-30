/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.junit4;

import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.getExtensionModel;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.util.IOUtils.getResourceAsUrl;

import static com.google.common.collect.ImmutableList.copyOf;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.ReflectionUtils.findMethod;

import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;
import com.zernova.runtime.extension.api.resources.GeneratedResource;
import com.zernova.runtime.extension.api.resources.ResourcesGenerator;
import com.zernova.runtime.module.extension.internal.loader.java.DefaultJavaExtensionModelLoader;
import com.zernova.runtime.module.extension.internal.manager.DefaultExtensionManager;
import com.zernova.tck.junit4.rule.SystemProperty;
import com.zernova.test.runner.infrastructure.ExtensionsTestInfrastructureDiscoverer;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.junit.Rule;

/**
 * Base test class for {@link FunctionalTestCase}s that make use of components generated through the extensions API.
 * <p/>
 * The value added by this class in comparison to a traditional {@link FunctionalTestCase} is that before creating the
 * {@link ZernovaContext}, it creates a {@link ExtensionManager} and automatically registers extensions pointed by
 * {@link #getAnnotatedExtensionClasses()} methods.
 * <p/>
 * Once extensions are registered, a {@link ResourcesGenerator} is used to automatically generate any backing resources needed
 * (XSD schemas, spring bundles, etc).
 * <p/>
 * In this way, the user experience is greatly simplified when running the test either through an IDE or build tool such as maven
 * or gradle.
 * <p/>
 * Since this class extends {@link FunctionalTestCase}, a new {@link ZernovaContext} is created per each test. That also means that a
 * new {@link ExtensionManager} is created per test.
 *
 * @since 3.7.0
 */
public abstract class ExtensionFunctionalTestCase extends FunctionalTestCase {

  @Rule
  public SystemProperty jvmVersionExtensionEnforcementLoose =
      new SystemProperty("mule.jvm.version.extension.enforcement", "LOOSE");

  private ExtensionManager extensionManager;

  /**
   * Implement this method to limit the amount of extensions initialised by providing the annotated classes which define the
   * extensions that you actually want to use for this test. Returning a {@code null} or empty array forces the
   * {@link ExtensionManager} to perform a full classpath discovery. Default implementation of this method returns {@code null}.
   */
  protected Class<?>[] getAnnotatedExtensionClasses() {
    return null;
  }

  /**
   * Adds a {@link ConfigurationBuilder} that sets the {@link #extensionManager} into the {@link #muleContext}. This
   * {@link ConfigurationBuilder} is set as the first element of the {@code builders} {@link List}
   *
   * @param builders the list of {@link ConfigurationBuilder}s that will be used to initialise the {@link #muleContext}
   */
  @Override
  // TODO - MULE-11119: Make final again once we can add the HTTP service injection as the scehduler's is
  protected void addBuilders(List<ConfigurationBuilder> builders) {
    super.addBuilders(builders);
    builders.add(0, new AbstractConfigurationBuilder() {

      @Override
      protected void doConfigure(ZernovaContext muleContext) throws Exception {
        createExtensionsManager(muleContext);
      }
    });
  }

  private void createExtensionsManager(ZernovaContext muleContext) throws Exception {
    extensionManager = new DefaultExtensionManager();
    File generatedResourcesDirectory = getGenerationTargetDirectory();

    muleContext.setExtensionManager(extensionManager);
    initialiseIfNeeded(extensionManager, muleContext);
    extensionManager.registerExtension(getExtensionModel());

    ExtensionsTestInfrastructureDiscoverer discoverer = new ExtensionsTestInfrastructureDiscoverer(extensionManager);

    for (Class<?> annotatedClass : getAnnotatedExtensionClasses()) {
      discoverer.discoverExtension(annotatedClass, getExtensionModelLoader());
    }
    generateResourcesAndAddToClasspath(generatedResourcesDirectory,
                                       copyOf(discoverer
                                           .generateDslResources(generatedResourcesDirectory)));
  }

  private void generateResourcesAndAddToClasspath(File generatedResourcesDirectory, List<GeneratedResource> resources)
      throws Exception {
    ClassLoader cl = getClass().getClassLoader();
    Method method = findMethod(cl.getClass(), "addURL", URL.class);

    if (method != null) {
      method.setAccessible(true);

      for (GeneratedResource resource : resources) {
        URL generatedResourceURL = new File(generatedResourcesDirectory, resource.getPath()).toURI().toURL();
        method.invoke(cl, generatedResourceURL);
      }
    } else {
      for (GeneratedResource resource : resources) {
        URL generatedResourceURL = new File(generatedResourcesDirectory, resource.getPath()).toURI().toURL();
        URLClassLoader.newInstance(new URL[] {generatedResourceURL}, cl);
      }

    }
  }

  private File getGenerationTargetDirectory() {
    URL url = getResourceAsUrl(getEffectiveConfigFile(), getClass(), true, true);
    File targetDirectory = new File(FileUtils.toFile(url).getParentFile(), "META-INF");

    if (!targetDirectory.exists() && !targetDirectory.mkdir()) {
      throw new RuntimeException("Could not create target directory " + targetDirectory.getAbsolutePath());
    }

    return targetDirectory;
  }

  private String getEffectiveConfigFile() {
    String configFile = getConfigFile();
    if (!isBlank(configFile)) {
      return configFile;
    }

    configFile = getConfigFileFromSplittable(getConfigurationResources());
    if (!isBlank(configFile)) {
      return configFile;
    }

    configFile = getConfigFileFromSplittable(getConfigResources());
    if (!isBlank(configFile)) {
      return configFile;
    }

    String[] configFiles = getConfigFiles();
    if (!isEmpty(configFiles)) {
      return configFiles[0].trim();
    }

    throw new IllegalArgumentException("No valid config file was specified");
  }

  private String getConfigFileFromSplittable(String configFile) {
    if (!isBlank(configFile)) {
      return configFile.split(",")[0].trim();
    }

    return null;
  }

  protected ExtensionModelLoader getExtensionModelLoader() {
    return new DefaultJavaExtensionModelLoader();
  }
}
