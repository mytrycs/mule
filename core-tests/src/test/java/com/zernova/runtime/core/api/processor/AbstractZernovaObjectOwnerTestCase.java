/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.processor;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.tck.size.SmallTest;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class AbstractZernovaObjectOwnerTestCase {

  @Mock
  private TestClass mockObject1;

  @Mock
  private TestClass mockObject2;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private ZernovaContext mockMuleContext;

  @Mock
  private FlowConstruct mockFlowConstruct;

  private AbstractZernovaObjectOwner<TestClass> abstractMuleObjectOwner;

  @Before
  public void before() {
    abstractMuleObjectOwner = new AbstractZernovaObjectOwner<TestClass>() {

      @Override
      protected List<TestClass> getOwnedObjects() {
        return Arrays.asList(mockObject1, mockObject2);
      }
    };
    abstractMuleObjectOwner.setMuleContext(mockMuleContext);
  }

  @Test
  public void testInitialise() throws Exception {
    abstractMuleObjectOwner.initialise();
    verify(mockObject1).initialise();
    verify(mockObject2).initialise();
    // TODO TMULE-10764 Injection should only happen once
    verify(mockObject1, times(3)).setMuleContext(mockMuleContext);
    verify(mockObject2, times(3)).setMuleContext(mockMuleContext);
  }

  @Test
  public void testDispose() throws Exception {
    abstractMuleObjectOwner.dispose();
    verify(mockObject1).dispose();
    verify(mockObject2).dispose();
  }

  @Test
  public void testStart() throws Exception {
    abstractMuleObjectOwner.start();
    verify(mockObject1).start();
    verify(mockObject2).start();
  }

  @Test
  public void testStop() throws Exception {
    abstractMuleObjectOwner.stop();
    verify(mockObject1).stop();
    verify(mockObject2).stop();
  }

  public class TestClass implements Lifecycle, ZernovaContextAware {

    @Override
    public void dispose() {}

    @Override
    public void initialise() throws InitialisationException {}

    @Override
    public void setMuleContext(ZernovaContext context) {}

    @Override
    public void start() throws ZernovaException {}

    @Override
    public void stop() throws ZernovaException {}
  }
}
