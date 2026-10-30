/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.domain;

import static java.lang.String.format;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import com.zernova.runtime.api.exception.ZernovaException;

import java.util.Set;

/**
 * Exception type for representing a failure due to domain not found.
 */
public final class DomainNotFoundException extends ZernovaException {

  private final String domainName;

  public DomainNotFoundException(String domainName, Set<String> availableDomains) {
    super(createStaticMessage(format("The domain '%s' was not found. Available domains: [%s]", domainName,
                                     availableDomains.toString())));
    this.domainName = domainName;
  }

  public String getDomainName() {
    return domainName;
  }
}
