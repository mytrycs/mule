/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.dsl.model.internal.config;

import static com.zernova.tck.junit4.matcher.IsEqualIgnoringLineBreaks.equalToIgnoringLineBreaks;
import static com.zernova.test.allure.AllureConstants.ConfigurationProperties.CONFIGURATION_PROPERTIES;
import static com.zernova.test.allure.AllureConstants.ConfigurationProperties.ComponentConfigurationAttributesStory.CONFIGURATION_PROPERTIES_RESOLVER_STORY;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;

import com.zernova.runtime.properties.api.ResourceProvider;
import com.zernova.runtime.config.api.properties.ConfigurationPropertiesHierarchyBuilder;
import com.zernova.runtime.config.api.properties.ConfigurationPropertiesResolver;
import com.zernova.runtime.config.internal.model.dsl.ClassLoaderResourceProvider;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Feature(CONFIGURATION_PROPERTIES)
@Story(CONFIGURATION_PROPERTIES_RESOLVER_STORY)
public class FileConfigurationPropertiesProviderTestCase extends AbstractZernovaTestCase {

  private ConfigurationPropertiesResolver resolver;

  @Rule
  public ExpectedException expectedException = none();

  @Before
  public void createResolver() {
    ResourceProvider externalResourceProvider = new ClassLoaderResourceProvider(Thread.currentThread().getContextClassLoader());
    resolver = new ConfigurationPropertiesHierarchyBuilder().withPropertiesFile(externalResourceProvider).build();
  }

  @Test
  public void fileIsResolved() {
    assertThat((String) resolver.resolveValue("${file::dummy.xml}"),
               is(equalToIgnoringLineBreaks("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<foo/>\n")));
  }

  @Test
  public void fileDoesNotExist() {
    expectedException.expectMessage(is("Couldn't find configuration property value for key ${file::non-existing-file}"));
    resolver.resolveValue("${file::non-existing-file}");
  }
}
