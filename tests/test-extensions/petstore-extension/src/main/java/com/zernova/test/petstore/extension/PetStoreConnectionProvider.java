/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import static com.zernova.runtime.api.connection.ConnectionValidationResult.failure;
import static com.zernova.runtime.api.connection.ConnectionValidationResult.success;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.lifecycle.Lifecycle;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.tls.TlsContextFactory;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.RefName;
import com.zernova.runtime.extension.api.annotation.param.display.Password;
import com.zernova.sdk.api.annotation.param.RuntimeVersion;
import com.zernova.sdk.api.annotation.semantics.security.Username;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.inject.Inject;

public abstract class PetStoreConnectionProvider<T extends PetStoreClient> implements ConnectionProvider<T>, Lifecycle {

  private static final String USERNAME = "john";

  @Inject
  protected ZernovaContext muleContext;

  @RefName
  protected String configName;

  @Parameter
  @Username
  protected String username;

  @Parameter
  @Password
  protected String password;

  @Parameter
  @Optional
  protected TlsContextFactory tls;

  @Optional
  @Parameter
  protected Date openingDate;

  @Optional
  @Parameter
  protected List<Date> closedForHolidays;

  @Optional
  @Parameter
  protected List<LocalDateTime> discountDates;

  @RuntimeVersion
  protected ZernovaVersion muleVersion;

  private int initialise, start, stop, dispose = 0;

  @Override
  public T connect() throws ConnectionException {
    if (!username.equals(USERNAME)) {
      throw new ConnectionException("We only know john");
    }
    return (T) new PetStoreClient(username, password, tls, configName, openingDate, closedForHolidays, discountDates,
                                  muleVersion);
  }

  @Override
  public void disconnect(PetStoreClient connection) {
    if (connection != null) {
      connection.disconnect();
    }
  }

  @Override
  public ConnectionValidationResult validate(PetStoreClient connection) {
    if (connection.getUsername().equals("john") && connection.getPassword().equals("doe")) {
      return success();
    } else {
      return failure("Invalid credentials", new Exception("Invalid credentials"));
    }
  }

  @Override
  public void dispose() {
    dispose++;
  }

  @Override
  public void initialise() throws InitialisationException {
    initialise++;
  }

  @Override
  public void start() throws ZernovaException {
    start++;
  }

  @Override
  public void stop() throws ZernovaException {
    stop++;
  }

  public int getInitialise() {
    return initialise;
  }

  public int getStart() {
    return start;
  }

  public int getStop() {
    return stop;
  }

  public int getDispose() {
    return dispose;
  }

  public ZernovaContext getMuleContext() {
    return muleContext;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Date getOpeningDate() {
    return openingDate;
  }
}
