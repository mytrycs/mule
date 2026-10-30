/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static org.mockito.Mockito.verify;
import static com.zernova.tck.ZernovaTestUtils.spyInjector;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Test;

@SmallTest
public abstract class LifecycleAwareValueResolverWrapperTestCase<T> extends AbstractZernovaContextTestCase {

  protected LifecycleAwareValueResolverWrapper<T> wrapper;
  protected ValueResolver<T> delegate;

  @Before
  public void before() throws Exception {
    spyInjector(muleContext);
    delegate = createDelegate();
    wrapper = createWrapper(delegate);
    muleContext.getInjector().inject(wrapper);
  }

  protected abstract LifecycleAwareValueResolverWrapper<T> createWrapper(ValueResolver<T> delegate);

  protected abstract ValueResolver<T> createDelegate();

  @Test
  public void initialise() throws Exception {
    wrapper.initialise();

    if (delegate instanceof Initialisable) {
      verify((Initialisable) delegate).initialise();
    }

    verify(muleContext.getInjector()).inject(delegate);
  }

  @Test
  public void start() throws Exception {
    wrapper.start();
    if (delegate instanceof Startable) {
      verify((Startable) delegate).start();
    }
  }

  @Test
  public void stop() throws Exception {
    wrapper.stop();
    if (delegate instanceof Stoppable) {
      verify((Stoppable) delegate).stop();
    }
  }

  @Test
  public void dispose() throws Exception {
    wrapper.dispose();
    if (delegate instanceof Disposable) {
      verify((Disposable) delegate).dispose();
    }
  }
}
