/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import com.zernova.runtime.core.internal.message.InternalMessage;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationFactory;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.tck.testmodels.fruit.Apple;

import org.junit.Test;

@SmallTest
public class TypeAwareConfigurationFactoryTestCase extends AbstractZernovaTestCase {

  @Test
  public void instantiate() {
    ConfigurationFactory instantiator = new TypeAwareConfigurationFactory(Apple.class, Apple.class.getClassLoader(), true);
    Object object = instantiator.newInstance();
    assertThat(object, instanceOf(Apple.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void noDefaultConstructor() {
    new TypeAwareConfigurationFactory(TypeAwareConfigurationFactory.class,
                                      TypeAwareConfigurationFactory.class.getClassLoader(), true);
  }

  @Test(expected = NullPointerException.class)
  public void nullType() {
    new TypeAwareConfigurationFactory(null, getClass().getClassLoader(), true);
  }


  @Test(expected = NullPointerException.class)
  public void nullClassLoader() {
    new TypeAwareConfigurationFactory(Apple.class, null, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void interfaceType() {
    new TypeAwareConfigurationFactory(InternalMessage.class, getClass().getClassLoader(), true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void abstractClass() {
    new TypeAwareConfigurationFactory(AbstractZernovaTestCase.class, getClass().getClassLoader(), true);
  }
}
