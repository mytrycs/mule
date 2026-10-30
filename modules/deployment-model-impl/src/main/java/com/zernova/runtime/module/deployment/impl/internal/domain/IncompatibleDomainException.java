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
import com.zernova.runtime.deployment.model.api.domain.Domain;

/**
 * Exception type for representing a failure due to domain not compatible found.
 */
public final class IncompatibleDomainException extends ZernovaException {

  public IncompatibleDomainException(String domainName, Domain foundDomain) {
    super(createStaticMessage(format("A domain with name %s was found, but its bundle descriptor is not compatible with the application declared dependency. The found domain bundle descriptor is %s",
                                     domainName, foundDomain.getDescriptor().getBundleDescriptor())));
  }
}
