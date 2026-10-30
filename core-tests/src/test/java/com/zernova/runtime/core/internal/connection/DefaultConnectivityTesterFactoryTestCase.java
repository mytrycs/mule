/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.connection;

import static com.zernova.test.allure.AllureConstants.JavaSdk.JAVA_SDK;
import static com.zernova.test.allure.AllureConstants.JavaSdk.ConnectivityTestingStory.CONNECTIVITY_TEST;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lock.LockFactory;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.probe.JUnitLambdaProbe;
import com.zernova.tck.probe.PollingProber;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import jakarta.inject.Inject;

@Feature(JAVA_SDK)
@Story(CONNECTIVITY_TEST)
public class DefaultConnectivityTesterFactoryTestCase extends AbstractZernovaContextTestCase {

  public static final PollingProber prober = new PollingProber();
  private static final ExecutorService executorService = newSingleThreadExecutor();

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Mock
  public LockFactory lockFactory;

  @Mock
  public ConnectionProvider provider;

  @Mock
  public ConfigurationInstance configurationInstance;

  @Inject
  public ConnectionManagerAdapter connectionManager;

  private DefaultConnectivityTesterFactory connectivityTesterFactory;

  private Lock lock;

  @Before
  public void setUp() throws ZernovaException {
    connectivityTesterFactory = muleContext.getInjector().inject(new DefaultConnectivityTesterFactory());

    lock = spy(new ReentrantLock());
    when(lockFactory.createLock(anyString())).thenReturn(lock);
    connectivityTesterFactory.setLockFactory(lockFactory);

    connectionManager = spy(connectionManager);
    connectivityTesterFactory.setConnectionManager(connectionManager);
  }

  @Override
  protected boolean doTestClassInjection() {
    return true;
  }

  @Test
  public void connectivityTesterWaitsForTheLock() {
    ConnectivityTester connectivityTester = connectivityTesterFactory.create("TestConnectivityTester");

    // The lock is acquired so that connectivityTester's tryLock gets blocked
    lock.lock();

    // Run the testConnectivity in other thread
    executorService.execute(() -> {
      try {
        connectivityTester.testConnectivity(provider, configurationInstance);
      } catch (ZernovaException e) {
        fail(e.getMessage());
      }
    });

    // Eventually, the tryLock is called by the testConnectivity. We expect it to get blocked
    prober.check(new JUnitLambdaProbe(() -> {
      verify(lock).tryLock(anyLong(), any(TimeUnit.class));
      return true;
    }));

    // Release the lock so that tryLock returns true
    lock.unlock();

    // And eventually, the testConnectivity is delegated to the connectionManager
    prober.check(new JUnitLambdaProbe(() -> {
      verify(connectionManager).testConnectivity(same(configurationInstance));
      return true;
    }));
  }
}
