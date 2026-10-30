/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.internal.memory.management;

import static com.zernova.runtime.api.memory.provider.type.ByteBufferType.HEAP;
import static com.zernova.test.allure.AllureConstants.MemoryManagement.MEMORY_MANAGEMENT;
import static com.zernova.test.allure.AllureConstants.MemoryManagement.MemoryManagementServiceStory.BYTE_BUFFER_PROVIDER;

import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.junit.MockitoJUnit.rule;

import com.zernova.runtime.api.memory.management.MemoryManagementService;
import com.zernova.runtime.api.profiling.ProfilingDataProducer;
import com.zernova.runtime.api.profiling.ProfilingService;
import com.zernova.runtime.internal.memory.bytebuffer.HeapByteBufferProvider;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoRule;

@Feature(MEMORY_MANAGEMENT)
@Story(BYTE_BUFFER_PROVIDER)
public class ArtifactMemoryManagementTestCase extends AbstractZernovaTestCase {

  public static final String TEST_BYTE_BUFFER = "testByteBuffer";
  public static final String ANOTHER_BYTE_BUFFER = "AnothertestByteBuffer";
  public static final String BYTE_BUFFER_NAME = "BYTE_BUFFER_NAME";

  @Mock
  MemoryManagementService containerMemoryManagementService;

  @Rule
  public ExpectedException expectedException = none();

  @Rule
  public MockitoRule rule = rule();

  @After
  public void after() {
    containerMemoryManagementService.dispose();
  }

  @Test
  @Description("When an artifact memory management service is disposed, only its managed byte buffers are disposed")
  public void containerByteBufferNotDisposedWhenArtifactByteBufferDisposed() {
    MemoryManagementService artifactMemoryManagementService =
        new ArtifactMemoryManagementService(containerMemoryManagementService);
    MemoryManagementService anotherArtifactMemoryManagementService =
        new ArtifactMemoryManagementService(containerMemoryManagementService);

    ProfilingService mockedProfilingService = mock(ProfilingService.class);
    when(mockedProfilingService.getProfilingDataProducer(any(), any())).thenReturn(mock(ProfilingDataProducer.class));
    HeapByteBufferProvider byteBufferProvider = new HeapByteBufferProvider(
                                                                           BYTE_BUFFER_NAME,
                                                                           mockedProfilingService);
    when(containerMemoryManagementService.getByteBufferProvider(any(), any())).thenReturn(byteBufferProvider);

    artifactMemoryManagementService.getByteBufferProvider(TEST_BYTE_BUFFER, HEAP);
    anotherArtifactMemoryManagementService.getByteBufferProvider(ANOTHER_BYTE_BUFFER, HEAP);

    artifactMemoryManagementService.dispose();

    verify(containerMemoryManagementService).disposeByteBufferProvider(TEST_BYTE_BUFFER);
    verify(containerMemoryManagementService, never()).disposeByteBufferProvider(ANOTHER_BYTE_BUFFER);
  }

}
