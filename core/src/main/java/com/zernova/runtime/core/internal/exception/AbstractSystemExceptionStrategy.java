/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.exception;

import static com.zernova.runtime.core.privileged.event.PrivilegedEvent.getCurrentEvent;
import static com.zernova.runtime.core.privileged.event.PrivilegedEvent.setCurrentEvent;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.core.api.connector.ConnectException;
import com.zernova.runtime.core.api.exception.RollbackSourceCallback;
import com.zernova.runtime.core.api.exception.SystemExceptionHandler;
import com.zernova.runtime.core.internal.message.InternalMessage;
import com.zernova.runtime.core.privileged.event.PrivilegedEvent;
import com.zernova.runtime.core.privileged.exception.AbstractExceptionListener;
import com.zernova.runtime.core.privileged.exception.MessagingException;

import jakarta.inject.Inject;

/**
 * Fire a notification, log exception, clean up transaction if any, and trigger reconnection strategy if this is a
 * <code>ConnectException</code>.
 */
public abstract class AbstractSystemExceptionStrategy extends AbstractExceptionListener implements SystemExceptionHandler {

  @Inject
  private SchedulerService schedulerService;

  protected Scheduler retryScheduler;

  @Override
  public void handleException(Exception ex, RollbackSourceCallback rollbackMethod) {
    doHandleException(ex, rollbackMethod, null);
  }

  private void rollback(Exception ex, RollbackSourceCallback rollbackMethod) {
    rollback(ex);
    if (rollbackMethod != null) {
      rollbackMethod.rollback();
    }
  }

  @Override
  public void handleException(Exception ex) {
    doHandleException(ex, null, null);
  }

  @Override
  public void handleException(Exception ex, ComponentLocation componentLocation) {
    doHandleException(ex, null, componentLocation);
  }

  private void doHandleException(Exception ex, RollbackSourceCallback rollbackMethod, ComponentLocation componentLocation) {
    fireNotification(ex, getCurrentEvent(), componentLocation);

    if ((ex instanceof MessagingException) && !(((MessagingException) ex).getExceptionInfo().isAlreadyLogged())) {
      resolveAndLogException(ex);
    }

    logger.debug("Rolling back transaction");
    rollback(ex, rollbackMethod);

    if (getCurrentEvent() != null) {
      PrivilegedEvent currentEvent = getCurrentEvent();
      currentEvent = PrivilegedEvent.builder(currentEvent)
          .message(InternalMessage.builder(currentEvent.getMessage()).build()).build();
      setCurrentEvent(currentEvent);
    }

    if (ex instanceof ConnectException) {
      ((ConnectException) ex).handleReconnection(retryScheduler);
    }
  }

  @Override
  protected void doInitialise() throws InitialisationException {
    retryScheduler = schedulerService.ioScheduler(muleContext.getSchedulerBaseConfig().withShutdownTimeout(0, MILLISECONDS));
    super.doInitialise();
  }

  @Override
  public void dispose() {
    super.dispose();
    if (retryScheduler != null) {
      retryScheduler.stop();
      retryScheduler = null;
    }
  }
}
