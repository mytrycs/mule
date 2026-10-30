/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.registry;

import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_TRANSACTION_MANAGER;
import static com.zernova.runtime.core.privileged.registry.LegacyRegistryUtils.registerObject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import org.junit.Test;

import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;

public class TransactionManagerInjectTestCase extends AbstractZernovaContextTestCase {

  @Test
  public void injectTransactionManager() throws Exception {
    TransactionManager manager = mock(TransactionManager.class);
    registerObject(muleContext, OBJECT_TRANSACTION_MANAGER, manager);
    TransactionClient txClient = new TransactionClient();
    muleContext.getInjector().inject(txClient);

    assertThat(txClient.getTxMgr(), is(manager));
  }

  public static class TransactionClient {

    private TransactionManager txMgr;

    public TransactionManager getTxMgr() {
      return txMgr;
    }

    @Inject
    public void setTxMgr(TransactionManager txMgr) {
      this.txMgr = txMgr;
    }
  }

}
