/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.classloading;

import static java.lang.Thread.currentThread;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.runtime.process.CompletionCallback;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Named;

public class LoadClassOperation {

  @Inject
  @Named("completion.callbacks")
  private Map<String, CompletionCallback<Object, Object>> callbackMap;

  @MediaType(value = TEXT_PLAIN, strict = false)
  public void tryLoadClass(@Alias("class") String clazz) throws ClassNotFoundException {
    currentThread().getContextClassLoader().loadClass(clazz);
  }

  @MediaType(value = TEXT_PLAIN, strict = false)
  public void saveCompletionCallback(@Alias("callback-key") String key, CompletionCallback<Object, Object> completionCallback) {
    callbackMap.put(key, completionCallback);
  }

}
