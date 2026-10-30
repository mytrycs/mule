/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.parameter.resolver;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;

import com.zernova.runtime.extension.api.runtime.parameter.ParameterResolver;
import com.zernova.test.heisenberg.extension.model.DifferedKnockableDoor;

import org.junit.Test;

public class ParameterResolverOnPojoTestCase extends AbstractParameterResolverTestCase {

  @Override
  protected String getConfigFile() {
    return "parameter/parameter-resolver-on-pojo-config.xml";
  }

  @Test
  public void operationWithChildElement() throws Exception {
    DifferedKnockableDoor door = getPayload("operationWithChildElement");

    ParameterResolver<String> someExpression = door.getVictim();
    assertParameterResolver(someExpression, of("#[payload]"), is("this is the payload"));
  }

  @Test
  public void operationWithDynamicReferenceElement() throws Exception {
    DifferedKnockableDoor door = getPayload("operationWithDynamicReferenceElement");

    ParameterResolver<String> someExpression = door.getVictim();
    assertParameterResolver(someExpression, of("#[payload]"), is("this is the payload"));
  }

  @Test
  public void operationWithStaticReferenceElement() throws Exception {
    DifferedKnockableDoor door = getPayload("operationWithStaticReferenceElement");

    ParameterResolver<String> someExpression = door.getVictim();
    assertParameterResolver(someExpression, empty(), is("this is not an expression"));
  }


}
