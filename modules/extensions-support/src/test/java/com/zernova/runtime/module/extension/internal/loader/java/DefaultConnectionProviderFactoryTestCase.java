/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.extension.api.runtime.connectivity.ConnectionProviderFactory;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.test.petstore.extension.SimplePetStoreConnectionProvider;

import org.junit.Test;

@SmallTest
public class DefaultConnectionProviderFactoryTestCase extends AbstractZernovaTestCase {

  private ConnectionProviderFactory factory =
      new DefaultConnectionProviderFactory<>(SimplePetStoreConnectionProvider.class,
                                             SimplePetStoreConnectionProvider.class.getClassLoader());

  @Test
  public void getObjectType() {
    assertThat(factory.getObjectType(), equalTo(SimplePetStoreConnectionProvider.class));
  }

  @Test
  public void newInstance() throws Exception {
    assertThat(factory.newInstance(), is(instanceOf(SimplePetStoreConnectionProvider.class)));
  }

  @Test
  public void returnsDifferentInstances() throws Exception {
    assertThat(factory.newInstance(), is(not(sameInstance(factory.newInstance()))));
  }

  @Test(expected = IllegalModelDefinitionException.class)
  public void notProviderClass() {
    new DefaultConnectionProviderFactory<>(Object.class, getClass().getClassLoader());
  }

  @Test(expected = IllegalArgumentException.class)
  public void notInstantiable() {
    new DefaultConnectionProviderFactory<>(ConnectionProvider.class, ConnectionProvider.class.getClassLoader());
  }
}
