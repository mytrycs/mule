/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.context.notification;

import static java.lang.Thread.currentThread;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.notification.AbstractServerNotification;
import com.zernova.runtime.api.notification.AsyncMessageNotification;
import com.zernova.runtime.api.notification.AsyncMessageNotificationListener;
import com.zernova.runtime.api.notification.ClusterNodeNotification;
import com.zernova.runtime.api.notification.ClusterNodeNotificationListener;
import com.zernova.runtime.api.notification.ConnectionNotification;
import com.zernova.runtime.api.notification.ConnectionNotificationListener;
import com.zernova.runtime.api.notification.ConnectorMessageNotification;
import com.zernova.runtime.api.notification.ConnectorMessageNotificationListener;
import com.zernova.runtime.api.notification.CustomNotification;
import com.zernova.runtime.api.notification.CustomNotificationListener;
import com.zernova.runtime.api.notification.ErrorHandlerNotification;
import com.zernova.runtime.api.notification.ErrorHandlerNotificationListener;
import com.zernova.runtime.api.notification.ExceptionNotification;
import com.zernova.runtime.api.notification.ExceptionNotificationListener;
import com.zernova.runtime.api.notification.ExtensionNotification;
import com.zernova.runtime.api.notification.ExtensionNotificationListener;
import com.zernova.runtime.api.notification.FlowConstructNotification;
import com.zernova.runtime.api.notification.FlowConstructNotificationListener;
import com.zernova.runtime.api.notification.ManagementNotification;
import com.zernova.runtime.api.notification.ManagementNotificationListener;
import com.zernova.runtime.api.notification.MessageProcessorNotification;
import com.zernova.runtime.api.notification.MessageProcessorNotificationListener;
import com.zernova.runtime.api.notification.Notification;
import com.zernova.runtime.api.notification.NotificationListener;
import com.zernova.runtime.api.notification.PipelineMessageNotification;
import com.zernova.runtime.api.notification.PipelineMessageNotificationListener;
import com.zernova.runtime.api.notification.PollingSourceItemNotification;
import com.zernova.runtime.api.notification.PollingSourceItemNotificationListener;
import com.zernova.runtime.api.notification.PollingSourceNotification;
import com.zernova.runtime.api.notification.PollingSourceNotificationListener;
import com.zernova.runtime.api.notification.RoutingNotification;
import com.zernova.runtime.api.notification.RoutingNotificationListener;
import com.zernova.runtime.api.notification.SecurityNotification;
import com.zernova.runtime.api.notification.SecurityNotificationListener;
import com.zernova.runtime.api.notification.TransactionNotification;
import com.zernova.runtime.api.notification.TransactionNotificationListener;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.scheduler.SchedulerConfig;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.api.util.ClassUtils;
import com.zernova.runtime.core.internal.context.notification.Configuration;
import com.zernova.runtime.core.internal.context.notification.OptimisedNotificationHandler;
import com.zernova.runtime.core.internal.context.notification.Policy;
import com.zernova.runtime.core.internal.profiling.notification.ProfilingNotification;
import com.zernova.runtime.core.internal.profiling.notification.ProfilingNotificationListener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.slf4j.Logger;

/**
 * A reworking of the event manager that allows efficient behaviour without global on/off switches in the config.
 *
 * <p>
 * The configuration and resulting policy are separate; the policy is a summary of the configuration that contains information to
 * decide whether a particular message can be handled, and which updates that with experience gained handling messages. When the
 * configuration is changed the policy is rebuilt. In this way we get a fairly efficient system without needing controls
 * elsewhere.
 *
 * <p>
 * However, measurements showed that there was still a small impact on speed in some cases. To improve behaviour further the
 * {@link OptimisedNotificationHandler} was added. This allows a service that generates notifications to cache locally a handler
 * optimised for a particular class.
 *
 * <p>
 * The dynamic flag stops this caching from occurring. This reduces efficiency slightly (about 15% cost on simple VM messages,
 * less on other transports)
 * </p>
 *
 * <p>
 * Note that, because of subclass relationships, we need to be very careful about exactly what is enabled and disabled:
 * <ul>
 * <li>Disabling an event or interface disables all uses of that class or any subclass.</li>
 * <li>Enquiring whether an event is enabled returns true if any subclass is enabled.</li>
 * </ul>
 */
@NoExtend
public class ServerNotificationManager implements ServerNotificationHandler, ZernovaContextAware {

  private static final Logger logger = getLogger(ServerNotificationManager.class);

  private boolean dynamic = false;
  private Configuration configuration = new Configuration();
  private final AtomicInteger activeFires = new AtomicInteger();
  private final AtomicBoolean disposed = new AtomicBoolean(false);
  private final Latch disposeLatch = new Latch();
  private Scheduler notificationsLiteScheduler;
  private Scheduler notificationsIoScheduler;
  private ZernovaContext muleContext;
  private LazyValue<String> serverId = new LazyValue<>(() -> muleContext.getId());
  private LazyValue<SchedulerService> schedulerService = new LazyValue<>(() -> muleContext.getSchedulerService());
  private final Set<ServerNotificationConfigurationChangeListener> modificationListeners = new HashSet<>();

  public ServerNotificationManager() {}

  public ServerNotificationManager(LazyValue<SchedulerService> schedulerService, LazyValue<String> serverId) {
    this.schedulerService = schedulerService;
    this.serverId = serverId;
  }

  @Override
  public boolean isNotificationDynamic() {
    return dynamic;
  }

  public void setNotificationDynamic(boolean dynamic) {
    this.dynamic = dynamic;
  }

  /**
   * Do not make this object {@link com.zernova.runtime.api.lifecycle.Initialisable}. It needs to be initialised before every other
   * object to send notifications.
   */
  public void initialise() throws InitialisationException {
    notificationsLiteScheduler = schedulerService.get().cpuLightScheduler(SchedulerConfig.config().withName(toString()));
    notificationsIoScheduler = schedulerService.get().ioScheduler();
  }

  public void addInterfaceToType(Class<? extends NotificationListener> iface,
                                 Class<? extends Notification> event) {
    configuration.addInterfaceToType(iface, event);
    notifyIfDynamic();
  }

  public void setInterfaceToTypes(Map<Class<? extends NotificationListener>, Set<Class<? extends Notification>>> interfaceToEvents)
      throws ClassNotFoundException {
    configuration.addAllInterfaceToTypes(interfaceToEvents);
    notifyIfDynamic();
  }

  public void addListenerSubscriptionPair(ListenerSubscriptionPair pair) {
    configuration.addListenerSubscriptionPair(pair);
    notifyIfDynamic();
  }

  public void addListener(NotificationListener<?> listener) {
    configuration.addListenerSubscriptionPair(new ListenerSubscriptionPair(listener));
    notifyIfDynamic();
  }

  public <N extends Notification> void addListenerSubscription(NotificationListener<N> listener,
                                                               Predicate<N> selector) {
    configuration.addListenerSubscriptionPair(new ListenerSubscriptionPair(listener, selector));
    notifyIfDynamic();
  }

  /**
   * This removes *all* registrations that reference this listener
   */
  public void removeListener(NotificationListener<?> listener) {
    configuration.removeListener(listener);
    notifyIfDynamic();
  }

  public void disableInterface(Class<? extends NotificationListener> iface) {
    configuration.disableInterface(iface);
    notifyIfDynamic();
  }

  public void setDisabledInterfaces(Collection<Class<? extends NotificationListener>> interfaces)
      throws ClassNotFoundException {
    configuration.disabledAllInterfaces(interfaces);
    notifyIfDynamic();
  }

  private void notifyIfDynamic() {
    if (dynamic) {
      modificationListeners.forEach(ServerNotificationConfigurationChangeListener::onServerNotificationConfigurationChange);
    }
  }

  public void disableType(Class<? extends Notification> type) {
    configuration.disableType(type);
    notifyIfDynamic();
  }

  @Override
  public boolean isListenerRegistered(NotificationListener listener) {
    for (ListenerSubscriptionPair pair : configuration.getListeners()) {
      if (pair.getListener().equals(listener)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void fireNotification(Notification notification) {
    if (disposed.get()) {
      logger.warn("Notification not enqueued after ServerNotificationManager disposal: " + notification);
      return;
    }

    activeFires.incrementAndGet();
    try {
      if (notification instanceof AbstractServerNotification) {
        ((AbstractServerNotification) notification).setServerId(serverId.get());
      }
      if (notification.isSynchronous()) {
        notifyListeners(notification, (listener, nfn) -> listener.onNotification(nfn));
      } else {
        notifyListeners(notification, (listener, nfn) -> {
          if (listener.isBlocking()) {
            notificationsIoScheduler.submit(() -> listener.onNotification(nfn));
          } else {
            notificationsLiteScheduler.submit(() -> listener.onNotification(nfn));
          }
        });
      }
    } finally {
      if (0 == activeFires.decrementAndGet() && disposed.get()) {
        disposeLatch.countDown();
      }
    }
  }

  protected void notifyListeners(Notification notification, NotifierCallback notifier) {
    configuration.getPolicy().dispatch(notification, notifier);
  }

  @Override
  public boolean isNotificationEnabled(Class<? extends Notification> type) {
    boolean enabled = false;
    if (configuration != null) {
      Policy policy = configuration.getPolicy();
      if (policy != null) {
        enabled = policy.isNotificationEnabled(type);
      }
    }
    return enabled;
  }

  /**
   * Do not make this object {@link com.zernova.runtime.api.lifecycle.Disposable}. It needs to be alive after everything else has
   * died
   */
  public void dispose() {
    disposed.set(true);

    if (activeFires.get() > 0) {
      try {
        disposeLatch.await();
      } catch (InterruptedException e) {
        // Continue with the disposal after interrupt
        currentThread().interrupt();
      }
    }

    if (notificationsLiteScheduler != null) {
      notificationsLiteScheduler.stop();
      notificationsLiteScheduler = null;
    }
    if (notificationsIoScheduler != null) {
      notificationsIoScheduler.stop();
      notificationsIoScheduler = null;
    }

    configuration = null;
  }

  /**
   * Support string or class parameters
   */
  public static Class toClass(Object value) throws ClassNotFoundException {
    Class clazz;
    if (value instanceof String) {
      clazz = ClassUtils.loadClass(value.toString(), value.getClass());
    } else if (value instanceof Class) {
      clazz = (Class) value;
    } else {
      throw new IllegalArgumentException("Notification types and listeners must be a Class with fully qualified class name. Value is: "
          + value);
    }
    return clazz;
  }

  // for tests -------------------------------------------------------

  Policy getPolicy() {
    return configuration.getPolicy();
  }

  public Map<Class<? extends NotificationListener>, Set<Class<? extends Notification>>> getInterfaceToTypes() {
    return unmodifiableMap(configuration.getInterfaceToTypes());
  }

  public Set<ListenerSubscriptionPair> getListeners() {
    return unmodifiableSet(configuration.getListeners());
  }

  public boolean isDisposed() {
    return disposed.get();
  }

  /**
   * @return a {@link ServerNotificationManager} with the default configuration for Mule notifications
   */
  public static ServerNotificationManager createDefaultNotificationManager() {
    return registerNotifications(new ServerNotificationManager());
  }

  public static ServerNotificationManager createDefaultNotificationManager(LazyValue<SchedulerService> schedulerService,
                                                                           LazyValue<String> serverId) {
    return registerNotifications(new ServerNotificationManager(schedulerService, serverId));
  }

  private static ServerNotificationManager registerNotifications(ServerNotificationManager manager) {
    manager.addInterfaceToType(ZernovaContextNotificationListener.class, ZernovaContextNotification.class);
    manager.addInterfaceToType(RoutingNotificationListener.class, RoutingNotification.class);
    manager.addInterfaceToType(SecurityNotificationListener.class, SecurityNotification.class);
    manager.addInterfaceToType(ManagementNotificationListener.class, ManagementNotification.class);
    manager.addInterfaceToType(CustomNotificationListener.class, CustomNotification.class);
    manager.addInterfaceToType(ConnectionNotificationListener.class, ConnectionNotification.class);
    manager.addInterfaceToType(MessageProcessorNotificationListener.class, MessageProcessorNotification.class);
    manager.addInterfaceToType(ExceptionNotificationListener.class, ExceptionNotification.class);
    manager.addInterfaceToType(ErrorHandlerNotificationListener.class, ErrorHandlerNotification.class);
    manager.addInterfaceToType(TransactionNotificationListener.class, TransactionNotification.class);
    manager.addInterfaceToType(PipelineMessageNotificationListener.class, PipelineMessageNotification.class);
    manager.addInterfaceToType(AsyncMessageNotificationListener.class, AsyncMessageNotification.class);
    manager.addInterfaceToType(ClusterNodeNotificationListener.class, ClusterNodeNotification.class);
    manager.addInterfaceToType(ConnectorMessageNotificationListener.class, ConnectorMessageNotification.class);
    manager.addInterfaceToType(FlowConstructNotificationListener.class, FlowConstructNotification.class);
    manager.addInterfaceToType(ExtensionNotificationListener.class, ExtensionNotification.class);
    manager.addInterfaceToType(ProfilingNotificationListener.class, ProfilingNotification.class);
    manager.addInterfaceToType(PollingSourceNotificationListener.class, PollingSourceNotification.class);
    manager.addInterfaceToType(PollingSourceItemNotificationListener.class, PollingSourceItemNotification.class);
    return manager;
  }

  @Override
  public void setMuleContext(ZernovaContext muleContext) {
    this.muleContext = muleContext;
  }

  @Override
  public void registerServerNotificationConfigurationChangeListener(ServerNotificationConfigurationChangeListener serverNotificationConfigurationChangeListener) {
    modificationListeners.add(serverNotificationConfigurationChangeListener);
  }
}
