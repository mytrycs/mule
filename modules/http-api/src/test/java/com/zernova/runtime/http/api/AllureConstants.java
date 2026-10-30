/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.http.api;

public interface AllureConstants {

  interface HttpFeature {

    String HTTP_SERVICE = "HTTP Service";

    interface HttpStory {

      String ERRORS = "Errors";
      String MULTI_MAP = "Multi Map";
      String PROXY_CONFIG_BUILDER = "Proxy Config Builder";
      String REQUEST_BUILDER = "Request Builder";
      String RESPONSE_BUILDER = "Response Builder";
      String TCP_BUILDER = "TCP Builders";

    }

  }

}
