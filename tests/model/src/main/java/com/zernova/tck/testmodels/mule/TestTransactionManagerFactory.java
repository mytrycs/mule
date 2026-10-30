/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.testmodels.mule;

import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.internal.transaction.TransactionManagerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import jakarta.transaction.TransactionManager;

/**
 * Provides a transaction factory for testing purposes
 */
public class TestTransactionManagerFactory implements TransactionManagerFactory {

  private Map<Object, Object> environment;

  @Override
  public TransactionManager create(ZernovaConfiguration config) throws Exception {
    return (TransactionManager) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {TransactionManager.class},
                                                       new InternalInvocationHandler());
  }

  public Map getEnvironment() {
    return environment;
  }

  public void setEnvironment(Map<Object, Object> environment) {
    this.environment = environment;
  }

  public class InternalInvocationHandler implements InvocationHandler {

    public TestTransactionManagerFactory getParent() {
      return TestTransactionManagerFactory.this;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (Object.class.equals(method.getDeclaringClass())) {
        try {
          return method.invoke(this, args);
        } catch (InvocationTargetException e) {
          throw e.getCause();
        }
      } else {
        return null;
      }
    }

  }
}
