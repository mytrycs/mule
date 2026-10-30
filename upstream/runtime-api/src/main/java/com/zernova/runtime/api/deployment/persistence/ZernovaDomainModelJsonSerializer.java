/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.deployment.persistence;

import com.zernova.runtime.api.deployment.meta.ZernovaApplicationModel;
import com.zernova.runtime.api.deployment.meta.ZernovaDomainModel;

/**
 * {@link ZernovaApplicationModel} json serializer.
 *
 * @since 1.0
 */
public final class ZernovaDomainModelJsonSerializer extends AbstractZernovaArtifactModelJsonSerializer<ZernovaDomainModel> {

  @Override
  protected Class<ZernovaDomainModel> getParameterizedClass() {
    return ZernovaDomainModel.class;
  }

}
