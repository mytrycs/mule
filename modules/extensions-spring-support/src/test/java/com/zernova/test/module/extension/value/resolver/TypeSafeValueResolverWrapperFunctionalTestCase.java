/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.value.resolver;

import org.junit.Test;
import com.zernova.functional.junit4.ZernovaArtifactFunctionalTestCase;
import com.zernova.runtime.api.message.Message;
import com.zernova.test.runner.ArtifactClassLoaderRunnerConfig;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ArtifactClassLoaderRunnerConfig(applicationSharedRuntimeLibs = {"com.zernova.tests:mule-tests-model"})
public class TypeSafeValueResolverWrapperFunctionalTestCase extends ZernovaArtifactFunctionalTestCase {

  @Override
  protected String getConfigFile() {
    return "heisenberg-stream-parameter.xml";
  }

  @Test
  public void testContentConvertedToStream() throws Exception {
    Message firstMessage = flowRunner("streamContent").run().getMessage();
    Message secondMessage = flowRunner("streamContent").run().getMessage();
    assertThat(firstMessage.getPayload().getValue(), is(secondMessage.getPayload().getValue()));
  }

}
