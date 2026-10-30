/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.parameter.resolver;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.zernova.sdk.api.runtime.parameter.ParameterResolver;
import com.zernova.test.module.extension.AbstractExtensionFunctionalTestCase;

import org.hamcrest.Matcher;

public abstract class AbstractParameterResolverTestCase extends AbstractExtensionFunctionalTestCase {

  @Override
  protected boolean isDisposeContextPerClass() {
    return true;
  }

  protected <T> T getPayload(String flowName) throws Exception {
    return (T) flowRunner(flowName).run().getMessage().getPayload().getValue();
  }

  protected void assertParameterResolver(ParameterResolver resolver, java.util.Optional<String> expression, Matcher value) {
    assertThat(resolver.getExpression(), is(expression));
    assertThat(resolver.resolve(), value);
  }
}
