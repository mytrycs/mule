/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.domain.test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.module.deployment.impl.internal.domain.DomainZernovaContextBuilder;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.Test;

@SmallTest
public class DomainZernovaContextBuilderTestCase extends AbstractZernovaTestCase {

  @Test
  public void createsContainerConfiguration() throws Exception {
    DomainZernovaContextBuilder builder = new DomainZernovaContextBuilder("test");

    ZernovaConfiguration muleConfiguration = builder.getMuleConfiguration();

    assertThat(muleConfiguration.isContainerMode(), is(true));
  }
}
