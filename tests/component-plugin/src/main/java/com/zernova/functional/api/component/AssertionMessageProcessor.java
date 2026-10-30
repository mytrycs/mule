/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.api.component;

import static com.zernova.tck.junit4.AbstractZernovaContextTestCase.RECEIVE_TIMEOUT;
import static com.zernova.tck.processor.FlowAssert.addAssertion;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import static org.junit.Assert.fail;

import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.el.ValidationResult;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.expression.InvalidExpressionException;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.tck.processor.FlowAssertion;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;

public class AssertionMessageProcessor extends AbstractComponent implements FlowAssertion, Processor, Startable {

  protected String expression = "#[true]";
  protected String message = "?";
  private int count = 1;
  private final AtomicInteger invocationCount = new AtomicInteger(0);
  protected boolean needToMatchCount = false;

  public void setExpression(String expression) {
    this.expression = expression;
  }

  private CountDownLatch latch;

  @Inject
  protected ExpressionManager expressionManager;
  private boolean result = true;

  @Override
  public void start() throws InitialisationException {
    ValidationResult result = this.expressionManager.validate(expression);
    if (!result.isSuccess()) {
      throw new InvalidExpressionException(expression, result.errorMessage().orElse("Invalid exception"));
    }
    latch = new CountDownLatch(count);
    addAssertion(getRootContainerLocation().toString(), this);
  }

  @Override
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    if (event == null) {
      return null;
    }
    result = result
        && expressionManager.evaluateBoolean(expression, event, getLocation(), false, true);
    increaseCount();
    latch.countDown();
    return event;
  }

  /**
   * If result evaluated to false in some processed event or the last processed event was null, then assert fails, otherwise:
   * <li>count was set & count processes were done => ok</li>
   * <li>count was set & count processes were not done => fail</li>
   * <li>count was not set & at least one processing were done => ok</li>
   *
   * @throws InterruptedException
   */
  @Override
  public void verify() throws InterruptedException {
    if (countFailOrNullEvent()) {
      if (needToMatchCount) {
        fail(format("%sExpected count of %d but got %d.", failureMessagePrefix(), count, invocationCount.get()));
      } else {
        fail(format("%sNo event was received.", failureMessagePrefix()));
      }
    } else if (expressionFailed()) {
      fail(failureMessagePrefix() + "Expression " + expression + " evaluated false.");
    }
  }

  protected String failureMessagePrefix() {
    String processorPath = this.getLocation().getLocation();
    return "Flow assertion '" + message + "' failed @ '" + processorPath + "'. ";
  }

  public Boolean countFailOrNullEvent() throws InterruptedException // added for testing (cant assert on asserts)
  {
    return !isProcessesCountCorrect();
  }

  public Boolean expressionFailed() // added for testing (cant assert on asserts)
  {
    return !result;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setCount(int count) {
    this.count = count;
    needToMatchCount = true;
  }

  private void increaseCount() {
    invocationCount.incrementAndGet();
  }

  /**
   * The semantics of the count are as follows: - count was set & count processes were done => ok - count was set & count
   * processes were not done => fail - count was not set & at least one processing were done => ok
   *
   * @return
   * @throws InterruptedException
   */
  synchronized private boolean isProcessesCountCorrect() throws InterruptedException {
    boolean countReached = latch.await(RECEIVE_TIMEOUT, MILLISECONDS);
    if (needToMatchCount) {
      return count == invocationCount.get();
    } else {
      return countReached;
    }
  }

  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }
}
