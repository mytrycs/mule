/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.core.internal.event.InternalEvent;
import com.zernova.runtime.core.internal.message.EventInternalContext;
import com.zernova.runtime.core.internal.policy.SourcePolicyContext;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.CREATE_CHILD_POLICY_CONTEXT_FOR_PARALLEL_SCOPES;

/**
 * Util class for routers
 */
public final class RoutingUtils {

  private RoutingUtils() {

  }

  /**
   * Changes the {@link SourcePolicyContext} of the given event with a new instance (copy).
   *
   * @param event                  the event to change the {@link SourcePolicyContext}
   * @param featureFlaggingService The service to check if this change should be performed at all.
   */
  public static void setSourcePolicyChildContext(InternalEvent event, FeatureFlaggingService featureFlaggingService) {
    EventInternalContext<?> context = event.getSourcePolicyContext();
    if (!(context instanceof SourcePolicyContext)
        || !featureFlaggingService.isEnabled(CREATE_CHILD_POLICY_CONTEXT_FOR_PARALLEL_SCOPES)) {
      return;
    }
    SourcePolicyContext policyContext = (SourcePolicyContext) context;
    event.setSourcePolicyContext(policyContext.childContext());
  }

}
