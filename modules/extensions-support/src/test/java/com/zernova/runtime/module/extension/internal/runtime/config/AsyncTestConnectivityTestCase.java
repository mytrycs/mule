/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.config;

import static com.zernova.runtime.api.connection.ConnectionValidationResult.success;
import static com.zernova.test.allure.AllureConstants.JavaSdk.JAVA_SDK;
import static com.zernova.test.allure.AllureConstants.JavaSdk.ConnectivityTestingStory.CONNECTIVITY_TEST;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singletonMap;
import static java.util.Optional.of;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.config.PoolingProfile;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionHandler;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.lock.LockFactory;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.core.api.Injector;
import com.zernova.runtime.core.api.retry.ReconnectionConfig;
import com.zernova.runtime.core.api.retry.RetryNotifier;
import com.zernova.runtime.core.api.retry.async.AsynchronousRetryTemplate;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.core.api.retry.policy.SimpleRetryPolicyTemplate;
import com.zernova.runtime.core.internal.connection.ConnectionManagerAdapter;
import com.zernova.runtime.core.internal.connection.ConnectivityTesterFactory;
import com.zernova.runtime.core.internal.connection.DefaultConnectivityTesterFactory;
import com.zernova.runtime.core.privileged.registry.RegistrationException;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationState;
import com.zernova.tck.SimpleUnitTestSupportSchedulerService;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.probe.JUnitLambdaProbe;
import com.zernova.tck.probe.PollingProber;
import com.zernova.tck.size.SmallTest;
import com.zernova.tck.util.TestTimeSupplier;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@SmallTest
@Feature(JAVA_SDK)
@Story(CONNECTIVITY_TEST)
public class AsyncTestConnectivityTestCase extends AbstractZernovaTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  protected static final int RECONNECTION_MAX_ATTEMPTS = 5;
  private static final int RECONNECTION_FREQ = 100;
  private static final String NAME = "name";
  private static final int TEST_TIMEOUT = 2000;
  private static final int TEST_POLL_DELAY = 10;

  public AsyncTestConnectivityTestCase() {}

  @Mock
  private ConfigurationModel configurationModel;

  @Mock
  private ConfigurationState configurationState;

  protected Lifecycle value = mock(Lifecycle.class, withSettings().extraInterfaces(Component.class));
  protected AsyncConnectionManagerAdapter connectionManager;
  protected RetryPolicyTemplate retryPolicyTemplate;
  protected LifecycleAwareConfigurationInstance configurationInstance;
  private final TestTimeSupplier timeSupplier = new TestTimeSupplier(currentTimeMillis());
  private final Optional<ConnectionProvider> connectionProvider =
      of(mock(ConnectionProvider.class, withSettings().extraInterfaces(Lifecycle.class)));

  @Before
  public void setUp() throws Exception {
    retryPolicyTemplate = createRetryTemplate();
    retryPolicyTemplate.setNotifier(mock(RetryNotifier.class));
    connectionManager = spy(new AsyncConnectionManagerAdapter(retryPolicyTemplate));

    configurationInstance = createConfigurationInstance();
  }

  protected RetryPolicyTemplate createRetryTemplate() {
    return new AsynchronousRetryTemplate(new SimpleRetryPolicyTemplate(RECONNECTION_FREQ, RECONNECTION_MAX_ATTEMPTS));
  }

  @After
  public void after() {
    configurationInstance.dispose();
  }

  protected LifecycleAwareConfigurationInstance createConfigurationInstance() throws ZernovaException {
    final var lifecycleAwareConfigurationInstance = new LifecycleAwareConfigurationInstance(NAME,
                                                                                            configurationModel,
                                                                                            value,
                                                                                            configurationState,
                                                                                            connectionProvider);
    lifecycleAwareConfigurationInstance.setTimeSupplier(timeSupplier);
    lifecycleAwareConfigurationInstance.setConnectionManager(connectionManager);
    lifecycleAwareConfigurationInstance.setInjector(mock(Injector.class));

    final var defaultConnectivityTesterFactory = new DefaultConnectivityTesterFactory();
    defaultConnectivityTesterFactory.setLockFactory(lockId -> new ReentrantLock());
    defaultConnectivityTesterFactory.setSchedulerService(new SimpleUnitTestSupportSchedulerService());
    defaultConnectivityTesterFactory.setConnectionManager(connectionManager);
    lifecycleAwareConfigurationInstance.setConnectivityTesterFactory(defaultConnectivityTesterFactory);

    return lifecycleAwareConfigurationInstance;
  }


  @Test
  @Description("Checks that the test connectivity test is not interrupted")
  public void testConnectivityIsNotInterruptedWhenAsyncRetryTemplate() throws Exception {
    configurationInstance.initialise();
    configurationInstance.start();
    new PollingProber(TEST_TIMEOUT, TEST_POLL_DELAY).check(new JUnitLambdaProbe(() -> {
      verify(connectionManager).testConnectivity(configurationInstance);
      assertThat(connectionManager.wasInterruptedBeforeTestingConnectivity(), is(false));
      return true;
    }));
  }

  /**
   * This class is used to verify that the testConnectivity is performed before the thread pool used for connectivity testing
   * purposes is interrupted.
   */
  private static class AsyncConnectionManagerAdapter implements ConnectionManagerAdapter {

    private final RetryPolicyTemplate retryPolicyTemplate;

    public AsyncConnectionManagerAdapter(RetryPolicyTemplate retryPolicyTemplate) {
      this.retryPolicyTemplate = retryPolicyTemplate;
    }

    private boolean interrupted = false;

    @Override
    public <C> void bind(Object config, ConnectionProvider<C> connectionProvider) {}

    @Override
    public boolean hasBinding(Object config) {
      return false;
    }

    @Override
    public void unbind(Object config) {}

    @Override
    public <C> ConnectionHandler<C> getConnection(Object config) throws ConnectionException {
      return null;
    }

    @Override
    public <C> ConnectionValidationResult testConnectivity(ConnectionProvider<C> connectionProvider) {
      if (Thread.currentThread().isInterrupted()) {
        interrupted = true;
      }

      return success();
    }

    @Override
    public <C> ConnectionValidationResult testConnectivity(C connection, ConnectionHandler<C> connectionHandler) {
      return success();
    }

    @Override
    public ConnectionValidationResult testConnectivity(ConfigurationInstance configurationInstance)
        throws IllegalArgumentException {
      return success();
    }

    @Override
    public ConnectionValidationResult testConnectivity(ConfigurationInstance configurationInstance, boolean force)
        throws IllegalArgumentException {
      return success();
    }

    @Override
    public void initialise() throws InitialisationException {}

    @Override
    public void start() throws ZernovaException {}

    @Override
    public void stop() throws ZernovaException {}

    @Override
    public void dispose() {}

    @Override
    public <C> RetryPolicyTemplate getRetryTemplateFor(ConnectionProvider<C> connectionProvider) {
      return retryPolicyTemplate;
    }

    @Override
    public <C> ReconnectionConfig getReconnectionConfigFor(ConnectionProvider<C> connectionProvider) {
      return null;
    }

    @Override
    public PoolingProfile getDefaultPoolingProfile() {
      return null;
    }

    public boolean wasInterruptedBeforeTestingConnectivity() {
      return interrupted;
    }

  }
}
