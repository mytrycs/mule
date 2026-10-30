/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.queue;

import static com.zernova.runtime.api.message.Message.of;
import static com.zernova.runtime.core.api.util.queue.QueueConfiguration.MAXIMUM_CAPACITY;
import static com.zernova.tck.util.ZernovaContextUtils.eventBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import com.zernova.runtime.api.serialization.ObjectSerializer;
import com.zernova.runtime.api.serialization.SerializationProtocol;
import com.zernova.runtime.api.store.ObjectStoreException;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.serialization.JavaObjectSerializer;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.io.Serializable;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.hamcrest.core.Is;

public abstract class QueueStoreTestCase extends AbstractZernovaTestCase {

  public static final String VALUE = "value";
  public static final String ANOTHER_VALUE = "value2";
  public static final long OFFER_TIMEOUT = 10l;
  public static final int NUMBER_OF_ITEMS = 10;
  public static final int LONG_POLL_TIMEOUT = 100;
  public static final int SHORT_POLL_TIMEOUT = 100;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private ObjectSerializer serializer;

  @Before
  public void setUp() {
    serializer = new JavaObjectSerializer(this.getClass().getClassLoader());
  }

  @Test
  public void offerAndPollSingleValue() throws InterruptedException, ObjectStoreException {
    QueueStore queue = createQueue();
    queue.offer(VALUE, 0, OFFER_TIMEOUT);
    Serializable result = queue.poll(OFFER_TIMEOUT);
    assertThat((String) result, is(VALUE));
  }

  @Test
  public void offerAndPollTwice() throws Exception {
    QueueStore queue = createQueue();
    queue.putNow(VALUE);
    Serializable value = queue.poll(SHORT_POLL_TIMEOUT);
    assertThat((String) value, is(VALUE));
    assertThat(queue.poll(SHORT_POLL_TIMEOUT), nullValue());
  }

  @Test
  public void failIfThereIsNoCapacity() throws Exception {
    QueueStore queue = createQueueWithCapacity(1);
    assertThat(queue.offer(VALUE, 0, 10), is(true));
    assertThat(queue.offer(VALUE, 0, 10), is(false));
  }

  @Test
  public void allowOfferWhenThereIsCapacity() throws Exception {
    QueueStore queue = createQueueWithCapacity(1);
    assertThat(queue.offer(VALUE, 0, 10), is(true));
    queue.poll(10);
    assertThat(queue.offer(VALUE, 0, 10), is(true));
  }

  @Test
  public void untakeAddsElementFirst() throws Exception {
    QueueStore queue = createQueue();
    queue.offer(VALUE, 0, 10);
    queue.untake(ANOTHER_VALUE);
    assertThat((String) queue.poll(10), is(ANOTHER_VALUE));
  }

  @Test
  public void clearEmptiesTheQueue() throws Exception {
    QueueStore queue = createQueue();
    queue.putNow(VALUE);
    queue.putNow(ANOTHER_VALUE);
    queue.clear();
    assertThat(queue.poll(SHORT_POLL_TIMEOUT), nullValue());
  }

  @Test
  public void pollDoesNotReturnsUntilPollTimeout() throws Exception {
    QueueStore queue = createQueue();
    long initialTime = System.currentTimeMillis();
    queue.poll(LONG_POLL_TIMEOUT);
    assertThat(System.currentTimeMillis() - initialTime >= LONG_POLL_TIMEOUT, is(true));
  }

  @Test
  public void peekDoesNotRemoveElement() throws Exception {
    QueueStore queue = createQueue();
    queue.putNow(VALUE);
    assertThat((String) queue.peek(), Is.is(VALUE));
    assertThat((String) queue.poll(SHORT_POLL_TIMEOUT), Is.is(VALUE));
  }

  @Test
  public void offerSeveralRetrieveAll() throws Exception {
    QueueStore queue = createQueue();
    for (int i = 0; i < NUMBER_OF_ITEMS; i++) {
      queue.offer(String.valueOf(i), 0, NUMBER_OF_ITEMS);
    }
    for (int i = 0; i < NUMBER_OF_ITEMS; i++) {
      assertThat((String) queue.poll(NUMBER_OF_ITEMS), is(String.valueOf(i)));
    }
  }

  @Test
  public void offerSeveralRetrieveAllMuleEvents() throws Exception {
    QueueStore queue = createQueue();
    ArrayList<CoreEvent> events = new ArrayList<>(NUMBER_OF_ITEMS);
    for (int i = 0; i < NUMBER_OF_ITEMS; i++) {
      CoreEvent testEvent = eventBuilder().message(of("some data")).build();
      events.add(testEvent);
      queue.offer(testEvent, 0, NUMBER_OF_ITEMS);

    }
    for (int i = 0; i < NUMBER_OF_ITEMS; i++) {
      assertThat(((CoreEvent) queue.poll(NUMBER_OF_ITEMS)).getContext().getId().equals(events.get(i).getContext().getId()),
                 is(true));
    }
  }

  protected QueueStore createQueue() {
    return createQueueWithCapacity(MAXIMUM_CAPACITY);
  }

  protected QueueStore createQueueWithCapacity(int capacity) {
    QueueStore queue = createQueueInfoDelegate(capacity, temporaryFolder.getRoot().getAbsolutePath(),
                                               serializer.getInternalProtocol());
    return queue;
  }

  protected abstract QueueStore createQueueInfoDelegate(int capacity, String workingDirectory, SerializationProtocol serializer);
}
