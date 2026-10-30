/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.serialization;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;

import com.zernova.runtime.api.serialization.ObjectSerializer;
import com.zernova.runtime.api.serialization.SerializationProtocol;

/**
 * Serializes objects using the default Java serialization mechanism provided by writeObject and readObject methods.
 */
public class JavaObjectSerializer implements ObjectSerializer {

  private volatile JavaExternalSerializerProtocol javaSerializerProtocol;

  public JavaObjectSerializer(ClassLoader executionClassLoader) {
    checkArgument(executionClassLoader != null, "executionClassLoader cannot be null");

    javaSerializerProtocol = new JavaExternalSerializerProtocol(executionClassLoader);
  }

  @Override
  public SerializationProtocol getInternalProtocol() {
    return javaSerializerProtocol;
  }

  @Override
  public SerializationProtocol getExternalProtocol() {
    return javaSerializerProtocol;
  }

}
