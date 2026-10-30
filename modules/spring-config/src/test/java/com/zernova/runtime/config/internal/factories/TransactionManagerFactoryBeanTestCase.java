/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.factories;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import com.zernova.runtime.core.api.context.DefaultZernovaContextFactory;
import com.zernova.runtime.core.internal.context.DefaultZernovaContext;
import com.zernova.tck.config.TestServicesConfigurationBuilder;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.testmodels.mule.TestTransactionManagerFactory;

import org.junit.Test;

import jakarta.transaction.TransactionManager;

public class TransactionManagerFactoryBeanTestCase extends AbstractZernovaTestCase {

  @Test
  public void registerTransactionManager() throws Exception {
    DefaultZernovaContext context =
        (DefaultZernovaContext) new DefaultZernovaContextFactory().createMuleContext(new TestServicesConfigurationBuilder());

    TransactionManagerFactoryBean txMgrFB = new TransactionManagerFactoryBean();
    txMgrFB.setMuleContext(context);
    txMgrFB.setTxManagerFactory(new TestTransactionManagerFactory());
    TransactionManager transactionManager = txMgrFB.getObject();
    assertThat(transactionManager, not(is(nullValue())));
  }

}
