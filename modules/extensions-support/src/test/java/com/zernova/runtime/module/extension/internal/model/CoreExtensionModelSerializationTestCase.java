/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider;
import com.zernova.runtime.extension.api.persistence.ExtensionModelJsonSerializer;

import org.junit.Test;

public class CoreExtensionModelSerializationTestCase {

  @Test
  public void serializeDeserialize() throws Exception {
    ExtensionModel extensionModel = ZernovaExtensionModelProvider.getExtensionModel();
    ExtensionModelJsonSerializer jsonSerializer = new ExtensionModelJsonSerializer(true);
    String serialized = jsonSerializer.serialize(extensionModel);
    ExtensionModel deserialized = jsonSerializer.deserialize(serialized);
    assertThat(extensionModel, equalTo(deserialized));
  }

}
