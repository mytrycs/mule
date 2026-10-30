/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.callback;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import static com.zernova.runtime.core.api.util.ClassUtils.instantiateClass;
import static com.zernova.runtime.extension.api.annotation.param.Optional.PAYLOAD;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.module.deployment.test.api.EventCallback;

public class CallbackOperation {

  @MediaType(value = TEXT_PLAIN, strict = false)
  public void callback(@Alias("class") String clazz, @Optional(defaultValue = PAYLOAD) Object payload) throws ClassNotFoundException {
    try {
      EventCallback eventCallback = (EventCallback) instantiateClass(clazz);
      eventCallback.eventReceived((String) payload);
    } catch (ClassNotFoundException cnfe) {
      throw cnfe;
    } catch (Exception e) {
      throw new ZernovaRuntimeException(e);
    }
  }

  @MediaType(value = TEXT_PLAIN, strict = false)
  public void throwException(@Alias("exceptionClassName") String exceptionClassName, @Alias("error") String error) throws Exception {
    throw (Exception) instantiateClass(exceptionClassName);
  }
}
