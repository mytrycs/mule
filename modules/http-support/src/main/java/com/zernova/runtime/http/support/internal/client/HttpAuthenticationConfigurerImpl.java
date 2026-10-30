/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/* ZERNOVA modifications Copyright (c) 2026 ZerNova. CPAL-1.0. */
package com.zernova.runtime.http.support.internal.client;

import com.zernova.runtime.http.api.client.auth.HttpAuthentication;
import com.zernova.sdk.api.http.client.auth.HttpAuthenticationConfigurer;

public class HttpAuthenticationConfigurerImpl implements HttpAuthenticationConfigurer {

  private HttpAuthentication result;

  @Override
  public void basic(String username, String password, boolean preemptive) {
    result = HttpAuthentication.basic(username, password).preemptive(preemptive).build();
  }

  @Override
  public void digest(String username, String password, boolean preemptive) {
    result = HttpAuthentication.digest(username, password).preemptive(preemptive).build();
  }

  @Override
  public void ntlm(String username, String password, boolean preemptive, String domain, String workstation) {
    result = HttpAuthentication.ntlm(username, password).preemptive(preemptive).domain(domain).workstation(workstation).build();
  }

  public HttpAuthentication build() {
    return result;
  }
}
