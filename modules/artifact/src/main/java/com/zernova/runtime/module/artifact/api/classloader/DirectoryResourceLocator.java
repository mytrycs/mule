/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.api.classloader;

import static java.lang.String.format;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.api.annotation.NoInstantiate;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

@NoInstantiate
public final class DirectoryResourceLocator implements LocalResourceLocator {

  private String[] directories;

  public DirectoryResourceLocator(String... directories) {
    this.directories = directories;
  }

  @Override
  public URL findLocalResource(String resourceName) {
    if (directories != null && resourceName != null) {
      for (String directory : directories) {
        File resourceFile = new File(directory, resourceName);
        if (resourceFile.exists()) {
          try {
            return resourceFile.toURI().toURL();
          } catch (MalformedURLException e) {
            throw new ZernovaRuntimeException(createStaticMessage(format("Can not load resource with name %s.", resourceName)), e);
          }
        }
      }
    }
    return null;
  }
}
