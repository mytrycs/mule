/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.transformer.type;

import static com.zernova.runtime.api.util.classloader.ZernovaImplementationLoaderUtils.isResolveMuleImplementationLoadersDynamically;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mockStatic;

import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.DataType.DynamicDelegateDataType;
import com.zernova.runtime.api.util.classloader.ZernovaImplementationLoaderUtils;
import com.zernova.runtime.core.privileged.metadata.DefaultDataTypeBuilder;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.io.Serializable;
import java.util.function.Supplier;

import io.qameta.allure.Issue;
import org.junit.Test;
import org.mockito.MockedStatic;

@SmallTest
@Issue("W-16584126")
public class DynamicDelegateDataTypeSerializationTestCase extends AbstractZernovaTestCase {

  @Test
  public void serializeDynamicDelegateDataType() {
    DataType delegate = new DefaultDataTypeBuilder().build();
    DynamicDelegateDataType original = new DynamicDelegateDataType((Supplier<DataType> & Serializable) () -> delegate);
    byte[] serialized = serialize(original);
    DynamicDelegateDataType deserialized = deserialize(serialized);
    assertThat(deserialized, is(original));

    try (final MockedStatic<ZernovaImplementationLoaderUtils> mockedLoaderUtils = mockStatic(ZernovaImplementationLoaderUtils.class)) {
      mockedLoaderUtils.when(ZernovaImplementationLoaderUtils::isResolveMuleImplementationLoadersDynamically).thenReturn(true);
      mockedLoaderUtils.when(ZernovaImplementationLoaderUtils::getMuleImplementationsLoader)
          .thenReturn(this.getClass().getClassLoader());
      assertThat(isResolveMuleImplementationLoadersDynamically(), is(true));

      assertThat(deserialized.getDelegate(), is(original.getDelegate()));
    }
  }

}
