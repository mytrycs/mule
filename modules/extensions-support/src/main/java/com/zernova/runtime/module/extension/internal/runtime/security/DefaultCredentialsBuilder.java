/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.security;

import com.zernova.runtime.api.security.Credentials;
import com.zernova.runtime.api.security.CredentialsBuilder;
import com.zernova.runtime.core.api.security.DefaultZernovaCredentials;

/**
 * Builder for a {@link Credentials} implementation.
 *
 * @since 1.0
 */
public final class DefaultCredentialsBuilder implements CredentialsBuilder {

  private String username;
  private char[] password;
  private Object roles;

  DefaultCredentialsBuilder() {}

  /**
   * @param username the username of this {@link Credentials}
   * @return {@code this} builder
   */
  public DefaultCredentialsBuilder withUsername(String username) {
    this.username = username;
    return this;
  }

  /**
   * @param password the password of this {@link Credentials}
   * @return {@code this} builder
   */
  public DefaultCredentialsBuilder withPassword(char[] password) {
    this.password = password;
    return this;
  }

  /**
   * @param roles the enabled roles of this {@link Credentials}
   * @return {@code this} builder
   */
  public DefaultCredentialsBuilder withRoles(Object roles) {
    this.roles = roles;
    return this;
  }

  /**
   * @return an instance of a default implementation of {@link Credentials}
   */
  public Credentials build() {
    return new DefaultZernovaCredentials(username, password, roles);
  }
}
