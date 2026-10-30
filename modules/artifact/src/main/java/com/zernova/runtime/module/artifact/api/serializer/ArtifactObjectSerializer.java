/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.api.serializer;

import static java.util.Objects.requireNonNull;

import com.zernova.api.annotation.NoInstantiate;
import com.zernova.runtime.api.serialization.ObjectSerializer;
import com.zernova.runtime.api.serialization.SerializationProtocol;
import com.zernova.runtime.core.internal.serialization.JavaExternalSerializerProtocol;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderRepository;
import com.zernova.runtime.module.artifact.api.serializer.protocol.CustomJavaSerializationProtocol;

@NoInstantiate
public final class ArtifactObjectSerializer implements ObjectSerializer {

  private volatile JavaExternalSerializerProtocol javaExternalSerializerProtocol;
  private volatile CustomJavaSerializationProtocol javaInternalSerializerProtocol;

  public ArtifactObjectSerializer(ClassLoaderRepository classLoaderRepository, ClassLoader executionClassLoader) {
    requireNonNull(classLoaderRepository, "ClassLoaderRepository cannot be null");
    requireNonNull(executionClassLoader, "executionClassLoader cannot be null");

    javaExternalSerializerProtocol = new JavaExternalSerializerProtocol(executionClassLoader);
    javaInternalSerializerProtocol = new CustomJavaSerializationProtocol(classLoaderRepository, executionClassLoader);
  }

  @Override
  public SerializationProtocol getInternalProtocol() {
    return javaInternalSerializerProtocol;
  }

  @Override
  public SerializationProtocol getExternalProtocol() {
    return javaExternalSerializerProtocol;
  }

}
