/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.policy;

import com.zernova.runtime.deployment.model.api.application.Application;
import com.zernova.runtime.deployment.model.api.policy.PolicyTemplate;
import com.zernova.runtime.deployment.model.api.policy.PolicyTemplateDescriptor;

/**
 * Creates {@link PolicyTemplate} instances
 */
public interface PolicyTemplateFactory {

  /**
   * Creates a new policy template artifact
   *
   * @param application class loader where the policy template's class loader will be included. Non null.
   * @param descriptor  describes how to build a policy template artifact. Non null
   * @return a {@link PolicyTemplate} artifact from the provided descriptor as a member of the region.
   * @throws PolicyTemplateCreationException when the artifact cannot be created
   */
  PolicyTemplate createArtifact(Application application, PolicyTemplateDescriptor descriptor);
}
