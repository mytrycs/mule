/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.api.classloader.net;

import com.zernova.api.annotation.NoInstantiate;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Registers a handler for the Mule Artifact protocol, so that every time an URL has {@link #PROTOCOL} it will be handled by
 * {@link ZernovaArtifactUrlConnection}.
 *
 * @since 4.0
 */
@NoInstantiate
public final class ZernovaArtifactUrlStreamHandler extends URLStreamHandler {

  /**
   * Mule Protocol that will be used to reference artifacts.
   */
  public final static String PROTOCOL = "muleartifact";

  /**
   * Registers the Mule Artifact protocol {@link #PROTOCOL} into the
   * {@link URL#setURLStreamHandlerFactory(URLStreamHandlerFactory)} through the {@link ZernovaUrlStreamHandlerFactory}.
   */
  public static void register() {
    ZernovaUrlStreamHandlerFactory.registerHandler(PROTOCOL, new ZernovaArtifactUrlStreamHandler());
  }

  /**
   * Opens a connection to the object referenced by the {@code url} argument if the protocol of it it's {@link #PROTOCOL}.
   *
   * @param url that represents a Mule Artifact to connect with.
   * @return a {@link URLConnection} object for the {@code url}.
   * @throws IOException if an I/O error occurs while opening the {@link URL#openConnection()}.
   */
  @Override
  protected URLConnection openConnection(URL url) throws IOException {
    return new ZernovaArtifactUrlConnection(url);
  }
}
