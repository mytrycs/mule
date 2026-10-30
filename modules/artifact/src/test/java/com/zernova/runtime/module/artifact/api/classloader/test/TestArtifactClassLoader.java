/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.api.classloader.test;

import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import com.zernova.runtime.module.artifact.api.classloader.ModuleLayerInformationSupplier;
import com.zernova.runtime.module.artifact.api.classloader.ShutdownListener;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.tck.classlaoder.TestClassLoader;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Optional;

import static java.util.Optional.empty;

public class TestArtifactClassLoader extends TestClassLoader implements ArtifactClassLoader {

  public TestArtifactClassLoader(ClassLoader parent) {
    super(parent);
  }

  @Override
  public String getArtifactId() {
    return null;
  }

  @Override
  public <T extends ArtifactDescriptor> T getArtifactDescriptor() {
    return null;
  }

  @Override
  public ClassLoader getClassLoader() {
    return this;
  }

  @Override
  public void addShutdownListener(ShutdownListener listener) {

  }

  @Override
  public void setModuleLayerInformationSupplier(ModuleLayerInformationSupplier moduleLayerInformationSupplier) {

  }

  @Override
  public Optional<ModuleLayerInformationSupplier> getModuleLayerInformation() {
    return empty();
  }

  @Override
  public ClassLoaderLookupPolicy getClassLoaderLookupPolicy() {
    return null;
  }

  @Override
  public void dispose() {}

  @Override
  public URL findLocalResource(String resourceName) {
    return null;
  }

  @Override
  public URL findResource(String s) {
    return super.findResource(s);
  }

  @Override
  public URL findInternalResource(String resource) {
    return findResource(resource);
  }

  @Override
  public Enumeration<URL> findResources(String name) throws IOException {
    return super.findResources(name);
  }

  @Override
  public Class<?> findLocalClass(String name) throws ClassNotFoundException {
    return super.findClass(name);
  }

  @Override
  public Class<?> loadInternalClass(String name) throws ClassNotFoundException {
    return super.loadClass(name);
  }
}
