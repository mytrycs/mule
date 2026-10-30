/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.classloading;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataKeyId;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataScope;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;

import java.util.List;

@MetadataScope(keysResolver = CLKeysResolver.class, outputResolver = CLKeysResolver.class)
public class CLOperations {

  @MetadataKeyId
  @Parameter
  @Optional
  private String key;

  public void someOperation(@Connection String connection) {}

  public List<String> getMethods(String clazzName) {
    try {
      return stream(currentThread().getContextClassLoader().loadClass(clazzName).getMethods())
          .map(method -> method.getName()).collect(toList());
    } catch (ClassNotFoundException e) {
      throw new ZernovaRuntimeException(createStaticMessage("Class was not found!"), e);
    }
  }
}
