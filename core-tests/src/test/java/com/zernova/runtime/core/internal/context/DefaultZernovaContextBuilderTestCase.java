/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.context;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;

import com.zernova.runtime.api.notification.AsyncMessageNotification;
import com.zernova.runtime.api.notification.AsyncMessageNotificationListener;
import com.zernova.runtime.api.notification.ClusterNodeNotification;
import com.zernova.runtime.api.notification.ClusterNodeNotificationListener;
import com.zernova.runtime.api.notification.ConnectionNotification;
import com.zernova.runtime.api.notification.ConnectionNotificationListener;
import com.zernova.runtime.api.notification.CustomNotification;
import com.zernova.runtime.api.notification.CustomNotificationListener;
import com.zernova.runtime.api.notification.ExceptionNotification;
import com.zernova.runtime.api.notification.ExceptionNotificationListener;
import com.zernova.runtime.api.notification.ManagementNotification;
import com.zernova.runtime.api.notification.ManagementNotificationListener;
import com.zernova.runtime.api.notification.Notification;
import com.zernova.runtime.api.notification.NotificationListener;
import com.zernova.runtime.api.notification.PipelineMessageNotification;
import com.zernova.runtime.api.notification.PipelineMessageNotificationListener;
import com.zernova.runtime.api.notification.RoutingNotification;
import com.zernova.runtime.api.notification.RoutingNotificationListener;
import com.zernova.runtime.api.notification.SecurityNotification;
import com.zernova.runtime.api.notification.SecurityNotificationListener;
import com.zernova.runtime.api.notification.TransactionNotification;
import com.zernova.runtime.api.notification.TransactionNotificationListener;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.DefaultZernovaConfiguration;
import com.zernova.runtime.core.api.context.notification.ZernovaContextNotification;
import com.zernova.runtime.core.api.context.notification.ZernovaContextNotificationListener;
import com.zernova.runtime.core.api.context.notification.ServerNotificationManager;
import com.zernova.runtime.core.internal.lifecycle.ZernovaContextLifecycleManager;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class DefaultZernovaContextBuilderTestCase extends AbstractZernovaTestCase {

  @Test
  public void testBuildMuleContextDefault() {
    // Build
    DefaultZernovaContextBuilder builder = new DefaultZernovaContextBuilder(APP);
    ZernovaContext muleContext = builder.buildMuleContext();

    // Assert
    assertThat(muleContext, notNullValue());
    assertThat(muleContext.getConfiguration(), instanceOf(DefaultZernovaConfiguration.class));
    assertThat(muleContext.getLifecycleManager(), instanceOf(ZernovaContextLifecycleManager.class));
    assertThat(muleContext.getNotificationManager(), instanceOf(ServerNotificationManager.class));
  }

  @Test
  public void testBuildMuleContextCustom() {
    // Build
    ZernovaContext muleContext = build();

    // Assert
    assertThat(muleContext, notNullValue());
    assertThat(muleContext.getConfiguration(), instanceOf(MyZernovaConfiguration.class));
    assertThat(muleContext.getLifecycleManager(), instanceOf(MyLifeCycleManager.class));
    assertThat(muleContext.getNotificationManager(), instanceOf(MyServerNotificationManager.class));
  }

  /**
   * <p>
   * After the mule context is built it has to contain the proper notification interfaces in the notification manager
   * </p>
   */
  @Test
  public void notificationManagerContainsTheCorrectInterfaces() {
    DefaultZernovaContextBuilder builder = new DefaultZernovaContextBuilder(APP);
    builder.setMuleConfiguration(new MyZernovaConfiguration());
    builder.setLifecycleManager(new MyLifeCycleManager());
    ZernovaContext muleContext = builder.buildMuleContext();

    Map<Class<? extends NotificationListener>, Set<Class<? extends Notification>>> interfaces =
        muleContext.getNotificationManager().getInterfaceToTypes();

    assertEquals(ZernovaContextNotification.class, interfaces.get(ZernovaContextNotificationListener.class).toArray()[0]);
    assertEquals(RoutingNotification.class, interfaces.get(RoutingNotificationListener.class).toArray()[0]);
    assertEquals(SecurityNotification.class, interfaces.get(SecurityNotificationListener.class).toArray()[0]);
    assertEquals(ManagementNotification.class, interfaces.get(ManagementNotificationListener.class).toArray()[0]);
    assertEquals(CustomNotification.class, interfaces.get(CustomNotificationListener.class).toArray()[0]);
    assertEquals(ConnectionNotification.class, interfaces.get(ConnectionNotificationListener.class).toArray()[0]);
    assertEquals(ExceptionNotification.class, interfaces.get(ExceptionNotificationListener.class).toArray()[0]);
    assertEquals(TransactionNotification.class, interfaces.get(TransactionNotificationListener.class).toArray()[0]);
    assertEquals(PipelineMessageNotification.class, interfaces.get(PipelineMessageNotificationListener.class).toArray()[0]);
    assertEquals(AsyncMessageNotification.class, interfaces.get(AsyncMessageNotificationListener.class).toArray()[0]);
    assertEquals(ClusterNodeNotification.class, interfaces.get(ClusterNodeNotificationListener.class).toArray()[0]);


  }

  private ZernovaContext build() {
    DefaultZernovaContextBuilder builder = new DefaultZernovaContextBuilder(APP);
    builder.setMuleConfiguration(new MyZernovaConfiguration());
    builder.setLifecycleManager(new MyLifeCycleManager());
    builder.setNotificationManager(new MyServerNotificationManager());
    return builder.buildMuleContext();
  }

  /**
   * Override, we don't want a {@link ZernovaContext} created for this test case.
   */
  protected ZernovaContext createMuleContext() throws Exception {
    return null;
  }

  static class MyLifeCycleManager extends ZernovaContextLifecycleManager {
    // subclass just for testing
  }

  static class MyZernovaConfiguration extends DefaultZernovaConfiguration {
    // subclass just for testing
  }

  static class MyServerNotificationManager extends ServerNotificationManager {
    // subclass just for testing
  }
}
