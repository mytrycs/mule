/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.dsl.model;

import static java.lang.Thread.currentThread;
import static java.util.Collections.list;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.zernova.runtime.config.internal.model.dsl.ClassLoaderResourceProvider;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.Test;

public class ClassLoaderResourceProviderTestCase extends AbstractZernovaTestCase {

  private ClassLoaderResourceProvider resourceProvider;

  @Before
  public void setUp() {
    resourceProvider = new ClassLoaderResourceProvider(currentThread().getContextClassLoader());
  }

  @Test
  public void sameJarResourceGetsLoaded() {
    verifyResourceGetsLoadedSuccessfully("META-INF/mule-core.xsd");
  }

  @Test
  public void differentJarResourceGetsLoaded() {
    verifyResourceGetsLoadedSuccessfully("javax/inject/Inject.class");
  }

  @Test
  public void absolutePathResourceGetsLoaded() throws IOException {
    final Optional<URL> muleModulePropertiesUrl = list(getClass().getClassLoader()
        .getResources("META-INF/mule-module.properties"))
            .stream()
            .filter(url -> {
              try {
                return url.toURI().getScheme().equals("file") && !url.toURI().getPath().contains(".jar!");
              } catch (URISyntaxException e) {
                throw new RuntimeException(e);
              }
            })
            .findAny();

    File file = FileUtils.toFile(muleModulePropertiesUrl.get());
    verifyResourceGetsLoadedSuccessfully(file.getAbsolutePath());
  }

  private void verifyResourceGetsLoadedSuccessfully(String resource) {
    try (InputStream content = resourceProvider.getResourceAsStream(resource)) {
      assertThat(content, notNullValue());
    } catch (IOException e) {
      fail();
    }
  }

}
