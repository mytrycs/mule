/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/**
 * Persistence API for Mule Extensions.
 *
 * @moduleGraph
 * @since 1.5
 */
module com.zernova.runtime.extensions.api.persistence {

  requires com.zernova.runtime.api;
  requires com.zernova.runtime.extensions.api;
  requires com.zernova.runtime.metadata.model.api;
  requires com.zernova.runtime.metadata.model.persistence;

  requires com.google.common;
  requires com.google.gson;
  requires org.apache.commons.lang3;

  exports com.zernova.runtime.extension.api.persistence;
  exports com.zernova.runtime.extension.api.persistence.metadata;
  exports com.zernova.runtime.extension.api.persistence.value;

  exports com.zernova.runtime.extension.internal.persistence to
      com.zernova.runtime.extensions.api.persistence.test;

  provides com.zernova.metadata.persistence.api.TypeAnnotationSerializerExtender
      with com.zernova.runtime.extension.internal.persistence.ExtensionTypeAnnotationSerializerExtender;

  // Allow introspection for serialization/deserialization by Gson
  opens com.zernova.runtime.extension.api.persistence.metadata to
      com.google.gson;
  opens com.zernova.runtime.extension.internal.persistence.metadata to
      com.google.gson;

}
