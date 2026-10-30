/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.api.descriptor;

import com.zernova.api.annotation.NoExtend;
import com.zernova.api.annotation.NoInstantiate;
import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.module.artifact.activation.api.ArtifactActivationException;

/**
 * Indicates that an error occurred while resolving a domain descriptor for an application descriptor.
 *
 * @since 4.5
 */
@NoExtend
@NoInstantiate
public class DomainDescriptorResolutionException extends ArtifactActivationException {

  private static final long serialVersionUID = 8465366672674298173L;

  public DomainDescriptorResolutionException(I18nMessage message) {
    super(message);
  }

  public DomainDescriptorResolutionException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }

}
