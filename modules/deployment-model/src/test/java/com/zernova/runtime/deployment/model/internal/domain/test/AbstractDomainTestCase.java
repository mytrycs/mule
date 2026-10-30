/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.deployment.model.internal.domain.test;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_HOME_DIRECTORY_PROPERTY;

import com.zernova.runtime.container.internal.ZernovaClassLoaderLookupPolicy;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.junit4.rule.SystemProperty;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class AbstractDomainTestCase extends AbstractZernovaTestCase {

  @ClassRule
  public static TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public final SystemProperty muleHomeSystemProperty =
      new SystemProperty(MULE_HOME_DIRECTORY_PROPERTY, temporaryFolder.getRoot().getCanonicalPath());
  protected final File muleHomeFolder;
  protected final ArtifactClassLoader containerClassLoader =
      new ZernovaArtifactClassLoader("mule", new ArtifactDescriptor("mule"), new URL[0],
                                  getClass().getClassLoader(),
                                  new ZernovaClassLoaderLookupPolicy(emptyMap(), emptySet()));

  public AbstractDomainTestCase() throws IOException {
    muleHomeFolder = temporaryFolder.getRoot();
  }

  protected File createDomainDir(String domainFolder, String domain) {
    final File file = new File(muleHomeFolder, domainFolder + File.separator + domain);
    assertThat(file.mkdirs(), is(true));
    return file;
  }
}
