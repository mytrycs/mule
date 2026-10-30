/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static com.zernova.runtime.core.internal.policy.DefaultPolicyManager.noPolicyOperation;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.policy.NoOpPolicyManager;
import com.zernova.runtime.core.internal.policy.OperationParametersProcessor;
import com.zernova.runtime.core.internal.policy.OperationPolicy;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import io.qameta.allure.Issue;
import org.junit.Test;

public class NoOpPolicyManagerTestCase extends AbstractZernovaTestCase {

  private NoOpPolicyManager noOpPolicyManager = new NoOpPolicyManager();

  @Test
  @Issue("MULE-18442")
  public void noOpPolicyManagerCreatesNoPolicyOperation() {
    Component operation = mock(Component.class);
    CoreEvent operationEvent = mock(CoreEvent.class);
    OperationParametersProcessor operationParameters = mock(OperationParametersProcessor.class);

    OperationPolicy policy = noOpPolicyManager.createOperationPolicy(operation, operationEvent, operationParameters);

    assertThat(policy, is(noPolicyOperation()));
  }
}
