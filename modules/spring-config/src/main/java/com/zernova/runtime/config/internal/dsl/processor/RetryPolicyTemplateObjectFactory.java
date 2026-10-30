/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.dsl.processor;

import static com.zernova.runtime.core.api.retry.policy.SimpleRetryPolicyTemplate.DEFAULT_FREQUENCY;
import static com.zernova.runtime.core.api.retry.policy.SimpleRetryPolicyTemplate.DEFAULT_RETRY_COUNT;

import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.core.api.retry.RetryNotifier;
import com.zernova.runtime.core.api.retry.async.AsynchronousRetryTemplate;
import com.zernova.runtime.core.api.retry.policy.NoRetryPolicyTemplate;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.core.api.retry.policy.SimpleRetryPolicyTemplate;
import com.zernova.runtime.dsl.api.component.AbstractComponentFactory;
import com.zernova.runtime.dsl.api.component.ObjectFactory;

import jakarta.inject.Inject;

/**
 * {@link ObjectFactory} for reconnection configuration.
 *
 * It will return a {@link RetryPolicyTemplate} that may run synchronously or asynchronously based on the {@code blocking}
 * configuration.
 *
 * @since 4.0
 */
public class RetryPolicyTemplateObjectFactory extends AbstractComponentFactory<RetryPolicyTemplate> {

  private boolean blocking = true;
  private Integer count = DEFAULT_RETRY_COUNT;
  private Integer frequency = DEFAULT_FREQUENCY;
  private RetryNotifier retryNotifier;

  @Inject
  private NotificationDispatcher notificationFirer;

  /**
   * @param blocking true if the policy must run synchronously when invoked, false if it must run asynchronously.
   */
  public void setBlocking(boolean blocking) {
    this.blocking = blocking;
  }

  /**
   * @param count the number of retries to execute.
   */
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   * @param frequency time between retries.
   */
  public void setFrequency(Integer frequency) {
    this.frequency = frequency;
  }

  /**
   * @param retryNotifier the retry notifier to use when retrying in the template
   */
  public void setRetryNotifier(RetryNotifier retryNotifier) {
    this.retryNotifier = retryNotifier;
  }

  @Override
  public RetryPolicyTemplate doGetObject() throws Exception {
    RetryPolicyTemplate retryPolicyTemplate;
    if (count != 0) {
      retryPolicyTemplate = new SimpleRetryPolicyTemplate(frequency, count);
      ((SimpleRetryPolicyTemplate) retryPolicyTemplate).setNotificationFirer(notificationFirer);
    } else {
      retryPolicyTemplate = new NoRetryPolicyTemplate();
    }
    if (retryNotifier != null) {
      retryPolicyTemplate.setNotifier(retryNotifier);
    }
    if (!blocking) {
      return new AsynchronousRetryTemplate(retryPolicyTemplate);
    }
    return retryPolicyTemplate;
  }
}
