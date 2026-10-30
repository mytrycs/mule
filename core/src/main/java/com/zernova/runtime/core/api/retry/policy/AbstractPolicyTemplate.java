/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.retry.policy;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.COMPUTE_CONNECTION_ERRORS_IN_STATS;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.management.stats.AllStatistics;
import com.zernova.runtime.core.api.retry.RetryCallback;
import com.zernova.runtime.core.api.retry.RetryContext;
import com.zernova.runtime.core.api.retry.RetryNotifier;
import com.zernova.runtime.core.internal.retry.DefaultRetryContext;

import java.io.InterruptedIOException;
import java.util.Map;
import java.util.concurrent.Executor;

import jakarta.inject.Inject;

import org.slf4j.Logger;

/**
 * Base class for RetryPolicyTemplate implementations. Uses ConnectNotifier as RetryNotifier by default.
 */
@NoExtend
public abstract class AbstractPolicyTemplate extends AbstractComponent implements RetryPolicyTemplate {

  protected RetryNotifier notifier = new ConnectNotifier();

  /** This data will be made available to the RetryPolicy via the RetryContext. */
  private Map<Object, Object> metaInfo;

  @Inject
  private NotificationDispatcher notificationFirer;

  @Inject
  protected ZernovaContext muleContext;

  @Inject
  private FeatureFlaggingService featureFlaggingService;

  private static final Logger LOGGER = getLogger(AbstractPolicyTemplate.class);

  @Override
  public RetryContext execute(RetryCallback callback, Executor workManager) throws Exception {
    PolicyStatus status = null;
    RetryPolicy policy = createRetryInstance();
    DefaultRetryContext context = new DefaultRetryContext(callback.getWorkDescription(), metaInfo, notificationFirer);

    try {
      Exception cause = null;
      do {
        try {
          callback.doWork(context);
          if (notifier != null) {
            notifier.onSuccess(context);
          }
          break;
        } catch (Exception e) {
          cause = e;

          computeStats();

          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Error executing policy", cause);
          }
          if (notifier != null) {
            notifier.onFailure(context, cause);
          }
          if (cause instanceof InterruptedException || cause instanceof InterruptedIOException) {
            LOGGER.error("Process was interrupted (InterruptedException), ceasing process");
            break;
          } else {
            status = policy.applyPolicy(cause);
          }
        }
      } while (status.isOk());

      if (status == null || status.isOk()) {
        return context;
      } else {
        context.setFailed(cause);
        throw new RetryPolicyExhaustedException(cause, callback.getWorkOwner());
      }
    } finally {
      if (status != null && status.getThrowable() != null) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Error executing policy", status.getThrowable());
        }
      }
    }
  }

  protected void computeStats() {
    AllStatistics statistics = muleContext.getStatistics();

    if (statistics != null && statistics.isEnabled() && computeConnectionErrorsInStats()) {
      statistics.getApplicationStatistics().incConnectionErrors();
    }
  }

  protected boolean computeConnectionErrorsInStats() {
    return featureFlaggingService.isEnabled(COMPUTE_CONNECTION_ERRORS_IN_STATS);
  }

  @Override
  public RetryNotifier getNotifier() {
    return notifier;
  }

  @Override
  public void setNotifier(RetryNotifier retryNotifier) {
    this.notifier = retryNotifier;
  }

  @Override
  public Map<Object, Object> getMetaInfo() {
    return metaInfo;
  }

  @Override
  public void setMetaInfo(Map<Object, Object> metaInfo) {
    this.metaInfo = metaInfo;
  }

  // For Spring IoC only
  public void setId(String id) {
    // ignore
  }

  public void setNotificationFirer(NotificationDispatcher notificationFirer) {
    this.notificationFirer = notificationFirer;
  }
}
