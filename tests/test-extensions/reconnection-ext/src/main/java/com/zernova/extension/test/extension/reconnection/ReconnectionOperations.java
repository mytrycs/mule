/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.extension.test.extension.reconnection;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static com.zernova.extension.test.extension.reconnection.ReconnectableConnectionProvider.fail;
import static com.zernova.runtime.extension.api.error.ZernovaErrors.CONNECTIVITY;

import com.zernova.extension.test.extension.reconnection.metadata.RetryPolicyOutputResolver;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.extension.api.annotation.metadata.OutputResolver;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.error.ZernovaErrors;
import com.zernova.runtime.extension.api.exception.ModuleException;
import com.zernova.runtime.extension.api.runtime.streaming.PagingProvider;

import java.util.List;
import java.util.Optional;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class ReconnectionOperations {

  public static volatile int closePagingProviderCalls = 0;
  public static volatile int getPageCalls = 0;

  /**
   * Example of a simple operation that receives a string parameter and returns a new string message that will be set on the
   * payload.
   */
  public void switchConnection() {
    fail = !fail;
  }

  @OutputResolver(output = RetryPolicyOutputResolver.class)
  public RetryPolicyTemplate getRetryPolicyTemplate(@Connection ReconnectableConnection connection,
                                                    RetryPolicyTemplate template) {
    return template;
  }

  public PagingProvider<ReconnectableConnection, ReconnectableConnection> pagedOperation(Integer failOn,
                                                                                         ZernovaErrors withErrorType) {
    return new PagingProvider<ReconnectableConnection, ReconnectableConnection>() {

      @Override
      public List<ReconnectableConnection> getPage(ReconnectableConnection connection) {
        getPageCalls++;
        if (getPageCalls == failOn) {
          if (withErrorType.equals(CONNECTIVITY)) {
            throw new ModuleException(withErrorType, new ConnectionException("Failed to retrieve Page"));
          }
          throw new IllegalArgumentException("An illegal argument was received.");
        }
        return singletonList(connection);
      }

      @Override
      public Optional<Integer> getTotalResults(ReconnectableConnection connection) {
        return empty();
      }

      @Override
      public void close(ReconnectableConnection connection) {
        closePagingProviderCalls++;
      }
    };
  }

  public PagingProvider<ReconnectableConnection, ReconnectableConnection> stickyPagedOperation(Integer failOn,
                                                                                               ZernovaErrors withErrorType) {
    return new PagingProvider<ReconnectableConnection, ReconnectableConnection>() {

      @Override
      public List<ReconnectableConnection> getPage(ReconnectableConnection connection) {
        getPageCalls++;
        if (getPageCalls == failOn) {
          if (withErrorType.equals(CONNECTIVITY)) {
            throw new ModuleException(withErrorType, new ConnectionException("Failed to retrieve Page"));
          }
          throw new IllegalArgumentException("An illegal argument was received.");
        }
        return singletonList(connection);
      }

      @Override
      public Optional<Integer> getTotalResults(ReconnectableConnection connection) {
        return empty();
      }

      @Override
      public void close(ReconnectableConnection connection) {
        closePagingProviderCalls++;
      }

      @Override
      public boolean useStickyConnections() {
        return true;
      }
    };
  }
}
