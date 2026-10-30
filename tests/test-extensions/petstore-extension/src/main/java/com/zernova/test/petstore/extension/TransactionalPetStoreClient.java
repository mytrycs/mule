/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.petstore.extension;

import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.tls.TlsContextFactory;
import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.extension.api.connectivity.TransactionalConnection;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class TransactionalPetStoreClient extends PetStoreClient implements TransactionalConnection {

  private boolean begun, commited, rolledback = false;

  public TransactionalPetStoreClient(String username, String password, TlsContextFactory tlsContextFactory,
                                     String configName, Date openingDate, List<Date> closedForHolidays,
                                     List<LocalDateTime> discountDates, ZernovaVersion muleVersion) {
    super(username, password, tlsContextFactory, configName, openingDate, closedForHolidays, discountDates, muleVersion);
  }

  @Override
  public void begin() throws TransactionException {
    begun = true;
  }

  @Override
  public void commit() throws TransactionException {
    commited = true;
  }

  @Override
  public void rollback() throws TransactionException {
    rolledback = true;
  }

  public boolean isBegun() {
    return begun;
  }

  public boolean isCommited() {
    return commited;
  }

  public boolean isRolledback() {
    return rolledback;
  }
}
