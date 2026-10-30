/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.container.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.mock;

import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.List;

import org.junit.Test;

@SmallTest
public class ContainerOnlyLookupStrategyTestCase extends AbstractZernovaTestCase {

  @Test
  public void returnsContainerClassLoader() throws Exception {
    ClassLoader containerClassLoader = getClass().getClassLoader();
    ContainerOnlyLookupStrategy lookupStrategy = new ContainerOnlyLookupStrategy(containerClassLoader);

    List<ClassLoader> classLoaders = lookupStrategy.getClassLoaders(mock(ClassLoader.class));

    assertThat(classLoaders, hasItem(containerClassLoader));
  }

  @Test(expected = NullPointerException.class)
  public void validatesContainerClassLoader() throws Exception {
    new ContainerOnlyLookupStrategy(null);
  }
}
