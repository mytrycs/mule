/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.api.utils;

import static com.github.benmanes.caffeine.cache.Caffeine.newBuilder;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;

import com.github.benmanes.caffeine.cache.Cache;

import java.net.URI;

/**
 * Cache to avoid recalculating URIs more than once
 *
 * @since 4.0
 */
public class UriCache {

  private static final int MAX_CACHE_SIZE = 2000;
  private static volatile UriCache instance;

  private Cache<String, URI> cache = newBuilder().maximumSize(MAX_CACHE_SIZE).build();

  private UriCache() {}

  public static UriCache getInstance() {
    if (instance == null) {
      synchronized (UriCache.class) {
        if (instance == null) {
          instance = new UriCache();
        }
      }
    }
    return instance;
  }

  public static URI getUriFromString(String uri) {
    try {
      return getInstance().cache.get(uri, u -> URI.create(u));
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage("Could not create URI for " + uri, e));
    }
  }
}
