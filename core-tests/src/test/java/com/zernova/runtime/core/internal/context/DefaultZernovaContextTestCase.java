/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.context;

import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_CLUSTER_CONFIGURATION;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_CONVERTER_RESOLVER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_MULE_STREAM_CLOSER_SERVICE;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_POLLING_CONTROLLER;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_SECURITY_MANAGER;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.store.ObjectStoreManager;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import com.zernova.runtime.core.api.context.DefaultZernovaContextFactory;
import com.zernova.runtime.core.api.context.ZernovaContextFactory;
import com.zernova.runtime.core.api.context.notification.ServerNotificationManager;
import com.zernova.runtime.core.api.exception.RollbackSourceCallback;
import com.zernova.runtime.core.api.exception.SystemExceptionHandler;
import com.zernova.runtime.core.api.security.SecurityManager;
import com.zernova.runtime.core.api.transformer.DataTypeConversionResolver;
import com.zernova.runtime.core.api.util.StreamCloserService;
import com.zernova.runtime.core.internal.config.ClusterConfiguration;
import com.zernova.runtime.core.internal.config.builders.MinimalConfigurationBuilder;
import com.zernova.runtime.core.internal.connector.SchedulerController;
import com.zernova.runtime.core.internal.lifecycle.ZernovaContextLifecycleManager;
import com.zernova.runtime.core.internal.registry.ZernovaRegistry;
import com.zernova.runtime.core.internal.registry.ZernovaRegistryHelper;
import com.zernova.runtime.core.internal.transformer.DynamicDataTypeConversionResolver;
import com.zernova.runtime.core.internal.util.store.ZernovaObjectStoreManager;
import com.zernova.runtime.core.privileged.exception.MessagingException;
import com.zernova.tck.config.TestServicesConfigurationBuilder;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.slf4j.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DefaultZernovaContextTestCase extends AbstractZernovaTestCase {

  private static final Logger LOGGER = getLogger(DefaultZernovaContextTestCase.class);

  private final SystemExceptionHandler mockSystemExceptionHandler = mock(SystemExceptionHandler.class);
  private final MessagingException mockMessagingException = mock(MessagingException.class);
  @Rule
  public TestServicesConfigurationBuilder testServicesConfigurationBuilder = new TestServicesConfigurationBuilder();
  private ZernovaContextFactory muleContextFactory;

  private ZernovaContext context;

  @Before
  public void before() {
    muleContextFactory = new DefaultZernovaContextFactory();
  }

  @After
  public void after() throws ZernovaException {
    if (context != null) {
      if (context.isStarted()) {
        context.stop();
      }
      context.dispose();
    }
  }

  @Test
  public void callDisposeIfInitFails() throws Exception {
    ServerNotificationManager mockNotificationManager = mock(ServerNotificationManager.class);
    doThrow(ZernovaRuntimeException.class).when(mockNotificationManager).initialise();

    DefaultZernovaContextBuilder muleContextBuilder = new DefaultZernovaContextBuilder(APP);
    muleContextBuilder.setLifecycleManager(new ZernovaContextLifecycleManager());
    muleContextBuilder.setNotificationManager(mockNotificationManager);
    DefaultZernovaContext defaultMuleContext = (DefaultZernovaContext) muleContextBuilder.buildMuleContext();

    try {
      defaultMuleContext.initialise();
      fail("exception expected");
    } catch (Exception e) {
      verify(mockNotificationManager).dispose();
    }
  }

  @Test
  public void callSystemExceptionHandlerWhenExceptionIsMessagingException() throws Exception {
    createMuleContext();
    disposeIfNeeded(context.getExceptionListener(), LOGGER);
    context.setExceptionListener(mockSystemExceptionHandler);
    context.handleException(mockMessagingException);
    verify(mockSystemExceptionHandler, times(1)).handleException(mockMessagingException, (RollbackSourceCallback) null);
  }

  @Test
  public void getObjectStoreManager() throws Exception {
    createMuleContext();
    Object osManager = ((ZernovaContextWithRegistry) context).getRegistry().lookupObject(ObjectStoreManager.class);
    assertThat(osManager, instanceOf(ZernovaObjectStoreManager.class));
  }

  @Test
  public void defaultMuleClusterConfiguration() throws Exception {
    createMuleContext();
    context.start();
    assertThat(context.getClusterId(), is(""));
    assertThat(context.getClusterNodeId(), is(0));
  }

  @Test
  public void overriddenClusterConfiguration() throws Exception {
    final int clusterNodeId = 22;
    final String clusterId = "some-id";
    createMuleContext();
    ((ZernovaContextWithRegistry) context).getRegistry().registerObject(OBJECT_CLUSTER_CONFIGURATION, new ClusterConfiguration() {

      @Override
      public String getClusterId() {
        return clusterId;
      }

      @Override
      public int getClusterNodeId() {

        return clusterNodeId;
      }
    });
    context.start();
    assertThat(context.getClusterId(), is(clusterId));
    assertThat(context.getClusterNodeId(), is(clusterNodeId));
  }

  @Test
  public void defaultMulePollingController() throws Exception {
    createMuleContext();
    context.start();
    assertThat(context.isPrimaryPollingInstance(), is(true));
  }

  @Test
  public void overriddenMulePollingController() throws Exception {
    createMuleContext();
    ((ZernovaContextWithRegistry) context).getRegistry().registerObject(OBJECT_POLLING_CONTROLLER,
                                                                     (SchedulerController) () -> false);
    context.start();
    assertThat(context.isPrimaryPollingInstance(), is(false));
  }

  @Test
  public void getStreamCloserService() throws Exception {
    createMuleContext();
    StreamCloserService serviceFromRegistry =
        ((ZernovaContextWithRegistry) context).getRegistry().lookupObject(OBJECT_MULE_STREAM_CLOSER_SERVICE);
    ZernovaRegistryHelper registry = spy((ZernovaRegistryHelper) ((ZernovaContextWithRegistry) context).getRegistry());
    ((DefaultZernovaContext) context).setRegistry(registry);

    StreamCloserService streamCloserService = context.getStreamCloserService();
    assertThat(streamCloserService, not(nullValue()));

    assertThat(streamCloserService, is(sameInstance(serviceFromRegistry)));

    // test that subsequent invocations consistently returns the same object
    assertThat(context.getStreamCloserService(), is(sameInstance(streamCloserService)));

    // verify we're not fetching from registry many times
    verify(registry, times(1)).lookupObject(OBJECT_MULE_STREAM_CLOSER_SERVICE);
  }

  @Test
  public void cachesDataTypeConversionResolver() throws Exception {
    createMuleContext();
    ZernovaRegistryHelper muleRegistry = spy((ZernovaRegistryHelper) ((ZernovaContextWithRegistry) context).getRegistry());
    ((DefaultZernovaContext) context).setRegistry(muleRegistry);

    DataTypeConversionResolver dataTypeConverterResolver1 = context.getDataTypeConverterResolver();
    DataTypeConversionResolver dataTypeConverterResolver2 = context.getDataTypeConverterResolver();

    assertThat(dataTypeConverterResolver1, instanceOf(DynamicDataTypeConversionResolver.class));
    assertThat(dataTypeConverterResolver2, sameInstance(dataTypeConverterResolver1));
    verify(muleRegistry).lookupObject(OBJECT_CONVERTER_RESOLVER);
  }

  @Test
  public void securityManagerOnlyFetchedOnce() throws Exception {
    createMuleContext();
    ZernovaRegistry registry = ((DefaultZernovaContext) context).getRegistry();
    SecurityManager securityManager = mock(SecurityManager.class);
    registry.registerObject(OBJECT_SECURITY_MANAGER, securityManager);
    ZernovaRegistry muleRegistry = spy(registry);

    ((DefaultZernovaContext) context).setRegistry(muleRegistry);
    for (int i = 0; i < 2; i++) {
      assertThat(context.getSecurityManager(), is(sameInstance(securityManager)));
    }

    verify(muleRegistry).lookupObject(OBJECT_SECURITY_MANAGER);
  }

  @Test
  public void securityManagerIsRegisteredAndCached() throws Exception {
    SecurityManager securityManager = mock(SecurityManager.class);
    context = muleContextFactory.createMuleContext(testServicesConfigurationBuilder,
                                                   new MinimalConfigurationBuilder(),
                                                   new AbstractConfigurationBuilder() {

                                                     @Override
                                                     protected void doConfigure(ZernovaContext muleContext) throws Exception {
                                                       muleContext.setSecurityManager(securityManager);
                                                     }
                                                   });

    ZernovaRegistry registry = ((DefaultZernovaContext) context).getRegistry();
    ZernovaRegistry muleRegistry = spy(registry);

    assertThat(context.getSecurityManager(), is(sameInstance(securityManager)));
    verify(muleRegistry, never()).lookupObject(OBJECT_SECURITY_MANAGER);
  }

  protected void createMuleContext() throws ZernovaException {
    context = muleContextFactory.createMuleContext(testServicesConfigurationBuilder, new MinimalConfigurationBuilder());
  }
}
