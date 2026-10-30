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
import com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor;

import java.util.Set;

public class AmbiguousDomainReferenceException extends ZernovaException {

  public AmbiguousDomainReferenceException(BundleDescriptor bundleDescriptor, Set<String> domainNames) {
    super(createStaticMessage(format("More than one compatible domain were found for bundle descriptor %s. Found domains were: %s",
                                     bundleDescriptor, domainNames)));
  }
}
