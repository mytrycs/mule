/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.queue;

import static com.zernova.tck.junit4.matcher.Eventually.eventually;
import static com.zernova.tck.util.CollectableReference.collectedByGc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.config.DefaultZernovaConfiguration;
import com.zernova.runtime.core.api.util.queue.DefaultQueueConfiguration;
import com.zernova.runtime.core.api.util.queue.Queue;
import com.zernova.runtime.core.api.util.queue.QueueConfiguration;
import com.zernova.runtime.core.api.util.queue.QueueSession;
import com.zernova.runtime.core.internal.serialization.JavaObjectSerializer;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.util.CollectableReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TransactionalQueueManagerTestCase extends AbstractZernovaTestCase {

  private static final String TEST_QUEUE_NAME = "queue1";

  private TransactionalQueueManager queueManager;

  @Before
  public void setUp() throws ZernovaException {
    queueManager = new TransactionalQueueManager();
    queueManager.setMuleConfiguration(new DefaultZernovaConfiguration());
    queueManager.setObjectSerializer(new JavaObjectSerializer(this.getClass().getClassLoader()));
    queueManager.initialise();
    queueManager.start();
  }

  @After
  public void teardown() throws ZernovaException {
    queueManager.stop();
    queueManager.dispose();
  }

  @Test
  public void allowChangingConfigurationOnDisposedQueue() throws Exception {
    queueManager.setQueueConfiguration(TEST_QUEUE_NAME, new DefaultQueueConfiguration(0, true));
    QueueSession queueSession = queueManager.getQueueSession();
    Queue queue = queueSession.getQueue(TEST_QUEUE_NAME);
    queue.dispose();
    queueManager.setQueueConfiguration(TEST_QUEUE_NAME, new DefaultQueueConfiguration(0, false));
  }

  @Test
  public void clearRecoveryQueuesAfterRecovery() throws Exception {
    createDanglingTx();

    QueueSession queueSession = queueManager.getQueueSession();
    queueSession.getQueue(TEST_QUEUE_NAME).dispose();

    queueManager.setQueueConfiguration(TEST_QUEUE_NAME, new DefaultQueueConfiguration());
    queueManager.start();
  }

  @Test
  public void doNotCreateTwiceTheSameRecoveryQueue() {
    final RecoverableQueueStore recoveryQueue = queueManager.getRecoveryQueue(TEST_QUEUE_NAME);
    assertThat(recoveryQueue, is(queueManager.getRecoveryQueue(TEST_QUEUE_NAME)));
  }

  @Test
  public void doNotLeakQueueConfigurationAfterQueueDispose() {
    CollectableReference<QueueConfiguration> collectableReference = new CollectableReference<>(new DefaultQueueConfiguration());

    QueueStore queueStore = queueManager.getQueue(TEST_QUEUE_NAME);
    queueManager.setQueueConfiguration(TEST_QUEUE_NAME, collectableReference.get());
    queueManager.disposeQueueStore(queueStore);

    assertThat(collectableReference, is(eventually(collectedByGc())));
  }

  private void createDanglingTx() throws InterruptedException, ZernovaException {
    queueManager.setDefaultQueueConfiguration(new DefaultQueueConfiguration(0, true));
    QueueSession queueSession = queueManager.getQueueSession();
    queueSession.getQueue(TEST_QUEUE_NAME).put("value");
    queueSession.begin();
    queueSession.getQueue(TEST_QUEUE_NAME).poll(10);
    queueManager.stop();
  }
}
