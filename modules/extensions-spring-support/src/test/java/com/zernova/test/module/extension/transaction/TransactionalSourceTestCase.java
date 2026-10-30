/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.transaction;

import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.core.api.construct.Flow;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.util.func.CheckedSupplier;
import com.zernova.tck.probe.JUnitLambdaProbe;
import com.zernova.tck.probe.PollingProber;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;
import com.zernova.test.runner.RunnerDelegateTo;
import com.zernova.test.transactional.SdkTransactionalSource;
import com.zernova.test.transactional.TransactionalSource;
import com.zernova.test.transactional.connection.SdkTestTransactionalConnection;
import com.zernova.test.transactional.connection.TestTransactionalConnection;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

@RunnerDelegateTo(Parameterized.class)
public class TransactionalSourceTestCase extends AbstractExtensionFunctionalTestCase {

  private final Supplier<Boolean> successSource;
  private final String configFile;

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        {
            "Using Extensions API",
            (Supplier<Boolean>) () -> TransactionalSource.isSuccess,
            "tx/source-transaction-config.xml"
        },
        {
            "Using SDK API, legacy XA",
            (Supplier<Boolean>) () -> SdkTransactionalSource.isSuccess,
            "tx/sdk-source-transaction-config.xml"
        },
        {
            "Using SDK API",
            (Supplier<Boolean>) () -> com.zernova.test.transactionalxa.TransactionalSource.isSuccess,
            "tx/source-transaction-xa-config.xml"
        }
    });
  }

  public TransactionalSourceTestCase(String parametrizationName, Supplier<Boolean> successSource, String configFile) {
    this.successSource = successSource;
    this.configFile = configFile;
  }

  @Override
  protected String getConfigFile() {
    return configFile;
  }

  @Before
  public void setUp() throws Exception {
    MessageStorage.clean();
    TransactionalSource.isSuccess = null;
    SdkTransactionalSource.isSuccess = null;
    com.zernova.test.transactionalxa.TransactionalSource.isSuccess = null;
  }

  @After
  public void tearDown() {
    MessageStorage.clean();
    TransactionalSource.isSuccess = null;
    SdkTransactionalSource.isSuccess = null;
    com.zernova.test.transactionalxa.TransactionalSource.isSuccess = null;
  }

  @Test
  public void sourceStartsALocalTxAndGetsCommitted() throws Exception {
    startFlow("sourceStartsALocalTxAndGetsCommitted");
    validate(() -> !MessageStorage.messages.isEmpty());

    validateFlow(true);
    validateCommittedTransaction(MessageStorage.messages.poll());
  }

  @Test
  public void sourceStartsALocalTxAndGetsRollBacked() throws Exception {
    startFlow("sourceStartsALocalTxAndGetsRollBacked");
    validate(() -> !MessageStorage.messages.isEmpty());

    validateFlow(false);
    validateRolledBackedTransaction(MessageStorage.messages.poll());
  }

  @Test
  public void sourceStartsALocalTxAndOperationsCanJointIt() throws Exception {
    startFlow("sourceStartsALocalTxAndOperationsCanJointIt");
    validate(() -> MessageStorage.messages.size() == 2);

    validateFlow(true);
    validateCommittedTransaction(MessageStorage.messages.peek());
  }

  @Test
  public void sourceStartsALocalTxAndOperationsWithDifferentConnectionCanTJoinIt() throws Exception {
    startFlow("sourceStartsALocalTxAndOperationsWithDifferentConnectionCanTJoinIt");
    validate(() -> MessageStorage.exception != null);
    assertThat(MessageStorage.exception, is(instanceOf(TransactionException.class)));

    validateFlow(false);
    validateRolledBackedTransaction(MessageStorage.messages.poll());
  }

  @Test
  public void nonTxSourceDoesntBeginTx() throws Exception {
    startFlow("nonTxSourceDoesntBeginTx");
    validate(() -> !MessageStorage.messages.isEmpty());

    validateFlow(true);
    validateNonTxConnection(MessageStorage.messages.poll());
  }

  @Test
  public void nonTxSourceWithNonTxOperation() throws Exception {
    startFlow("nonTxSourceWithNonTxOperation");
    validate(() -> !MessageStorage.messages.isEmpty());

    validateFlow(true);
    validateNonTxConnection(MessageStorage.messages.poll());
  }

  @Test
  public void nonTxSourceWithTxInside() throws Exception {
    startFlow("nonTxSourceWithTxInside");
    validate(() -> !MessageStorage.messages.isEmpty());

    validateFlow(true);
    validateNonTxConnection(MessageStorage.messages.poll());
  }

  @Test
  public void sourceWithTxAndTimeout() throws Exception {
    startFlow("sourceWithTimeout");

    validateFlow(false);
    validateRolledBackedTransaction(MessageStorage.messages.poll());
  }

  private void startFlow(String flowName) throws Exception {
    ((Flow) getFlowConstruct(flowName)).start();
  }

  private void validate(CheckedSupplier<Boolean> validation) {
    new PollingProber(10000, 100).check(new JUnitLambdaProbe(validation));
  }

  private void validateCommittedTransaction(Object connection) {
    assertTransactionConnectionState(connection, true, true, false);
  }

  private void validateRolledBackedTransaction(Object connection) {
    assertTransactionConnectionState(connection, true, false, true);
  }

  private void validateNonTxConnection(Object connection) {
    assertTransactionConnectionState(connection, false, false, false);
  }

  private void validateFlow(boolean succeeded) {
    validate(() -> successSource.get() != null);
    assertThat(successSource.get(), is(succeeded));
  }

  private void assertTransactionConnectionState(Object connection, boolean begun, boolean committed, boolean rolledBack) {
    if (connection instanceof TestTransactionalConnection conn) {
      assertThat(conn.isTransactionBegun(), is(begun));
      assertThat(conn.isTransactionCommited(), is(committed));
      assertThat(conn.isTransactionRolledback(), is(rolledBack));
    } else if (connection instanceof SdkTestTransactionalConnection conn) {
      assertThat(conn.isTransactionBegun(), is(begun));
      assertThat(conn.isTransactionCommited(), is(committed));
      assertThat(conn.isTransactionRolledback(), is(rolledBack));
    } else if (connection instanceof com.zernova.test.transactionalxa.connection.TestTransactionalConnection conn) {
      assertThat(conn.isTransactionBegun(), is(begun));
      assertThat(conn.isTransactionCommited(), is(committed));
      assertThat(conn.isTransactionRolledback(), is(rolledBack));
    } else {
      throw new RuntimeException("Stored object is not a valid type of connection");
    }
  }


  public static class SleepProcessor implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws ZernovaException {
      try {
        sleep(3000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      return event;
    }
  }

  public static class MessageStorage extends AbstractComponent implements Processor {

    public static Queue<Object> messages = new ConcurrentLinkedQueue<>();

    public static Throwable exception;

    @Override
    public CoreEvent process(CoreEvent event) throws ZernovaException {
      event.getError().ifPresent(theError -> exception = theError.getCause());
      TypedValue<Object> payload = event.getMessage().getPayload();
      if (payload.getValue() != null) {
        messages.add(payload.getValue());
      }
      return event;
    }

    public static void clean() {
      exception = null;
      messages = new ConcurrentLinkedQueue<>();
    }
  }
}
