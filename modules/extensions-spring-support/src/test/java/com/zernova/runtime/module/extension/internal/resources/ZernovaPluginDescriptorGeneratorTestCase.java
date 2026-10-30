/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.resources;

import static com.zernova.runtime.module.extension.api.util.ZernovaExtensionUtils.loadExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.extension.api.resources.GeneratedResource;
import com.zernova.runtime.extension.api.resources.spi.GeneratedResourceFactory;
import com.zernova.runtime.module.extension.internal.resources.test.AbstractGeneratedResourceFactoryTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.test.heisenberg.extension.HeisenbergExtension;

import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.skyscreamer.jsonassert.JSONAssert;

import org.junit.Before;
import org.junit.Test;

@SmallTest
public class ZernovaPluginDescriptorGeneratorTestCase extends AbstractGeneratedResourceFactoryTestCase {

  private ZernovaPluginDescriptorGenerator generator = new ZernovaPluginDescriptorGenerator();

  private ExtensionModel extensionModel;

  @Before
  public void before() {
    extensionModel = loadExtension(HeisenbergExtension.class);
  }

  @Override
  protected Class<? extends GeneratedResourceFactory>[] getResourceFactoryTypes() {
    return new Class[] {ZernovaPluginDescriptorGenerator.class};
  }

  @Test
  public void generate() throws Exception {
    InputStream in = getClass().getResourceAsStream("/heisenberg-test-mule-artifact.json");
    assertThat(in, is(notNullValue()));
    String expectedDescriptor = IOUtils.toString(in);
    Optional<GeneratedResource> resource = generator.generateResource(extensionModel);
    assertThat(resource.isPresent(), is(true));

    String actualDescriptor = new String(resource.get().getContent());
    JSONAssert.assertEquals(expectedDescriptor, actualDescriptor, true);
  }
}
