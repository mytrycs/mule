/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.splash;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static com.zernova.runtime.core.api.config.i18n.CoreMessages.getArtifactTypeLoggableName;

import com.zernova.runtime.core.api.ZernovaContext;

public class ArtifactStartupSplashScreen extends SplashScreen {

  @Override
  protected void doHeader(ZernovaContext context) {
    header.add(getArtifactTypeLoggableName(context.getArtifactType()) + ": " + context.getConfiguration().getId());
    header.add(format("OS encoding: %s, Mule encoding: %s",
                      defaultCharset().name(),
                      context.getConfiguration().getDefaultEncoding()));
    header.add(" ");
  }

}
