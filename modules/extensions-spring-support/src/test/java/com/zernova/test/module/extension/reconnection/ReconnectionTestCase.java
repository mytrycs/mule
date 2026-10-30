/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.reconnection;

import static com.zernova.extension.test.extension.reconnection.ReconnectableConnectionProvider.disconnectCalls;
import static com.zernova.extension.test.extension.reconnection.ReconnectionOperations.closePagingProviderCalls;
import static com.zernova.extension.test.extension.reconnection.ReconnectionOperations.getPageCalls;
import static com.zernova.runtime.extension.api.error.ZernovaErrors.CONNECTIVITY;
import static com.zernova.runtime.extension.api.error.ZernovaErrors.VALIDATION;
import static com.zernova.tck.probe.PollingProber.check;
import static com.zernova.tck.probe.PollingProber.checkNot;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import com.zernova.extension.test.extension.reconnection.FailingConnection;
import com.zernova.extension.test.extension.reconnection.FallibleReconnectableSource;
import com.zernova.extension.test.extension.reconnection.NonReconnectableSource;
import com.zernova.extension.test.extension.reconnection.ReconnectableConnection;
import com.zernova.extension.test.extension.reconnection.ReconnectableConnectionProvider;
import com.zernova.extension.test.extension.reconnection.SynchronizableConnection;
import com.zernova.extension.test.extension.reconnection.SynchronizableSource;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.notification.ExceptionNotification;
import com.zernova.runtime.api.notification.ExceptionNotificationListener;
import com.zernova.runtime.api.streaming.object.CursorIterator;
import com.zernova.runtime.api.streaming.object.CursorIteratorProvider;
import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.retry.policy.RetryPolicy;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.extension.api.error.ZernovaErrors;
import com.zernova.runtime.extension.api.exception.ModuleException;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class ReconnectionTestCase extends AbstractExtensionFunctionalTestCase {

  private final static long TIMEOUT = 5000;
  private final static long POLL_DELAY = 500;

  private static List<CoreEvent> capturedEvents;

  public static void resetCounters() {
    closePagingProviderCalls = 0;
    getPageCalls = 0;
    disconnectCalls = 0;
    SynchronizableSource.first = true;
    SynchronizableConnection.disconnectionWaitedFullTimeout = false;
  }

  @Override
  protected String getConfigFile() {
    return "reconnection-config.xml";
  }

  @Override
  protected void doSetUp() throws Exception {
    capturedEvents = new LinkedList<>();
    ReconnectableConnectionProvider.fail = false;
    FallibleReconnectableSource.fail = false;
    NonReconnectableSource.fail = false;
  }

  @Override
  protected void doTearDown() throws Exception {
    capturedEvents = null;
    ReconnectableConnectionProvider.fail = false;
    FallibleReconnectableSource.fail = false;
    NonReconnectableSource.fail = false;
  }

  @Test
  public void reconnectSource() throws Exception {
    ((Startable) getFlowConstruct("reconnectForever")).start();
    check(5000, 1000, () -> !capturedEvents.isEmpty());
    switchConnection();

    check(10000, 1000, () -> {
      synchronized (capturedEvents) {
        return capturedEvents.stream()
            .map(event -> (ReconnectableConnection) event.getMessage().getPayload().getValue())
            .filter(c -> c.getReconnectionAttempts() >= 3)
            .findAny()
            .isPresent();
      }
    });
  }

  @Test
  public void sendNotificationOnReconnection() throws Exception {
    final Latch latch = new Latch();
    final List<ExceptionNotification> notifications = new ArrayList<>();
    final ExceptionNotificationListener listener = notification -> {
      notifications.add(notification);
      latch.release();
    };
    notificationListenerRegistry.registerListener(listener);
    try {
      this.reconnectSource();
      latch.await(TIMEOUT, MILLISECONDS);
      assertThat(notifications.size(), greaterThanOrEqualTo(1));
      assertThat(notifications.get(0).getInfo(), notNullValue());
      assertThat(notifications.get(0).getInfo().getException(), notNullValue());
      assertThat(notifications.get(0).getInfo().getException(), instanceOf(ConnectionException.class));
    } finally {
      notificationListenerRegistry.unregisterListener(listener);
    }
  }

  @Test
  public void noReconnectSource() throws Exception {
    ((Startable) getFlowConstruct("noReconnect")).start();
    check(TIMEOUT, POLL_DELAY, () -> !capturedEvents.isEmpty());
    NonReconnectableSource.fail = true;
    check(TIMEOUT, POLL_DELAY, () -> NonReconnectableSource.attempts.get() > 0);
    clear(capturedEvents);
    checkNot(TIMEOUT, POLL_DELAY, () -> size(capturedEvents) > 0);
  }

  @Test
  public void doNotStartSourceTwiceAfterExceptionOnReconnection() throws Exception {
    ((Startable) getFlowConstruct("reconnectAfterFailure")).start();
    check(TIMEOUT, POLL_DELAY, () -> !capturedEvents.isEmpty());
    FallibleReconnectableSource.fail = true;
    checkNot(TIMEOUT, POLL_DELAY, () -> FallibleReconnectableSource.simultaneouslyStartedSources);
    FallibleReconnectableSource.release();
    checkNot(TIMEOUT, POLL_DELAY, () -> FallibleReconnectableSource.simultaneouslyStartedSources);
  }

  @Test
  @Ignore("W-14161245")
  public void getInlineRetryPolicyTemplate() throws Exception {
    RetryPolicyTemplate template = (RetryPolicyTemplate) flowRunner("getInlineReconnection").run()
        .getMessage().getPayload().getValue();

    assertRetryTemplate(template, true, 30, 50);
  }

  @Test
  @Ignore("W-14161245")
  public void getInlineRetryPolicyBlockingTemplate() throws Exception {
    RetryPolicyTemplate template = (RetryPolicyTemplate) flowRunner("getInlineReconnectionBlocking").run()
        .getMessage().getPayload().getValue();

    assertRetryTemplate(template, false, 30, 50);
  }

  @Test
  public void reconnectAfterConnectionExceptionOnFirstPage() throws Exception {
    resetCounters();
    Iterator<ReconnectableConnection> iterator = getCursor("pagedOperation", 1, CONNECTIVITY);
    iterator.next();
    assertThat("Connection was not disconnected.", disconnectCalls, is(1));
    assertThat("Paging provider was not closed.", closePagingProviderCalls, is(1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void doNotReconnectAfterOtherExceptionOnFirstPage() throws Throwable {
    resetCounters();
    Iterator<ReconnectableConnection> iterator;
    try {
      iterator = getCursor("pagedOperation", 1, VALIDATION);
      iterator.next();
    } catch (Exception e) {
      assertThat(e.getCause(), instanceOf(IllegalArgumentException.class));
      assertThat(e.getMessage(), is("An illegal argument was received."));
      assertThat("Paging provider was not closed.", closePagingProviderCalls, is(1));
      assertThat("Connection was disconnected.", disconnectCalls, is(0));
      throw e.getCause();
    }
  }

  @Test
  public void reconnectionDuringConnectionExceptionOnSecondPage() throws Exception {
    resetCounters();
    Iterator<ReconnectableConnection> iterator = getCursor("pagedOperation", 2, CONNECTIVITY);

    iterator.next();
    assertThat("Connection was disconnected.", disconnectCalls, is(0));
    assertThat("Paging provider was closed.", closePagingProviderCalls, is(0));

    iterator.next();
    assertThat("Connection was not disconnected.", disconnectCalls, is(1));
    assertThat("Paging provider was closed.", closePagingProviderCalls, is(0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void doNotReconnectAfterOtherExceptionOnSecondPage() throws Exception {
    resetCounters();
    Iterator<ReconnectableConnection> iterator;
    try {
      iterator = getCursor("pagedOperation", 2, VALIDATION);
      iterator.next();
      iterator.next();
    } catch (Exception e) {
      assertThat(e, instanceOf(IllegalArgumentException.class));
      assertThat(e.getMessage(), is("An illegal argument was received."));
      assertThat("Paging provider was not closed.", closePagingProviderCalls, is(0));
      assertThat("Connection was disconnected.", disconnectCalls, is(0));
      throw e;
    }
  }

  @Test
  public void stickyConnectionIsClosedAndReconnectedDuringConnectionExceptionOnFirstPage() throws Exception {
    resetCounters();
    Iterator<ReconnectableConnection> iterator = getCursor("stickyPagedOperation", 1, CONNECTIVITY);
    iterator.next();
    assertThat("Connection was not disconnected.", disconnectCalls, is(1));
    assertThat("Paging provider was not closed.", closePagingProviderCalls, is(1));
  }

  @Test(expected = ModuleException.class)
  public void stickyConnectionIsNotReconnectedDuringConnectionExceptionOnSecondPage() throws Exception {
    resetCounters();
    try {
      Iterator<ReconnectableConnection> iterator = getCursor("stickyPagedOperation", 2, CONNECTIVITY);
      iterator.next();
      iterator.next();
    } catch (Exception e) {
      assertThat(e, instanceOf(ModuleException.class));
      assertThat(e.getCause(), instanceOf(ConnectionException.class));
      assertThat(e.getCause().getMessage(), is("Failed to retrieve Page"));
      assertThat("Paging provider was not closed.", closePagingProviderCalls, is(0));
      assertThat("Connection was not disconnected.", disconnectCalls, is(1));
      throw e;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void stickyConnectionIsNotReconnectedDuringOtherExceptionOnSecondPage() throws Exception {
    resetCounters();
    try {
      Iterator<ReconnectableConnection> iterator = getCursor("stickyPagedOperation", 2, VALIDATION);
      iterator.next();
      iterator.next();
    } catch (Exception e) {
      assertThat(e, instanceOf(IllegalArgumentException.class));
      assertThat(e.getMessage(), is("An illegal argument was received."));
      assertThat("Paging provider was not closed.", closePagingProviderCalls, is(0));
      assertThat("Connection was not disconnected.", disconnectCalls, is(0));
      throw e;
    }
  }

  @Test
  public void connectionInvalidatedOnCallback() throws Exception {
    resetCounters();
    ((Startable) getFlowConstruct("synchronizableSource")).start();
    check(TIMEOUT, POLL_DELAY, () -> SynchronizableConnection.disconnectionWaitedFullTimeout);
  }


  @Test
  public void connectionInvalidatedAndRecreatedIfConnectionExceptionOnStart() throws Exception {
    ((Startable) getFlowConstruct("invalidateConnectionIfConnectionExceptionOnStart")).start();
    check(10000, 1000, () -> {
      synchronized (capturedEvents) {
        return capturedEvents.stream()
            .flatMap(event -> ((List<FailingConnection>) event.getMessage().getPayload().getValue()).stream())
            .distinct()
            .count() == 5;
      }
    });
  }

  private void assertRetryTemplate(RetryPolicyTemplate template, boolean async, int count, long freq) throws Exception {
    assertThat(template.isAsync(), is(async));

    RetryPolicy policy = template.createRetryInstance();

    // Needs to use reflection because SimpleRetryPolicy is an internal class on the container ClassLoader which is not visible
    // from the TestCase's ClassLoader. Still, both methods are accessible (public in this case).
    Method getCountMethod = policy.getClass().getMethod("getCount");
    Method getFrequencyMethod = policy.getClass().getMethod("getFrequency");

    assertThat(getCountMethod.invoke(policy), is(count));
    assertThat(((Duration) getFrequencyMethod.invoke(policy)).toMillis(), is(freq));
  }

  private void switchConnection() throws Exception {
    flowRunner("switchConnection").run();
  }

  private <T> CursorIterator<T> getCursor(String flowName, Integer failOn, ZernovaErrors errorType) throws Exception {
    CursorIteratorProvider provider =
        (CursorIteratorProvider) flowRunner(flowName).withPayload(failOn).withVariable("errorType", errorType).keepStreamsOpen()
            .run().getMessage().getPayload()
            .getValue();

    return provider.openCursor();
  }

  private void clear(List<CoreEvent> list) {
    synchronized (list) {
      list.clear();
    }
  }

  private int size(List<CoreEvent> list) {
    synchronized (list) {
      return list.size();
    }
  }


  public static class CaptureProcessor implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws ZernovaException {
      synchronized (capturedEvents) {
        capturedEvents.add(event);
      }
      return event;
    }
  }
}
