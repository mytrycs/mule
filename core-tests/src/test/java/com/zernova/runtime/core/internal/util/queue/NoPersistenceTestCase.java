/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.queue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.util.queue.DefaultQueueConfiguration;
import com.zernova.runtime.core.internal.serialization.JavaObjectSerializer;

public class NoPersistenceTestCase extends AbstractTransactionQueueManagerTestCase {

  @Override
  protected TransactionalQueueManager createQueueManager() throws Exception {
    TransactionalQueueManager mgr = new TransactionalQueueManager();
    ZernovaConfiguration mockConfiguration = mock(ZernovaConfiguration.class);
    when(mockConfiguration.getMaxQueueTransactionFilesSizeInMegabytes()).thenReturn(500);

    mgr.setMuleConfiguration(mockConfiguration);
    mgr.setObjectSerializer(new JavaObjectSerializer(this.getClass().getClassLoader()));
    mgr.initialise();
    mgr.setDefaultQueueConfiguration(new DefaultQueueConfiguration(0, false));
    return mgr;
  }

  @Override
  protected boolean isPersistent() {
    return false;
  }
}
