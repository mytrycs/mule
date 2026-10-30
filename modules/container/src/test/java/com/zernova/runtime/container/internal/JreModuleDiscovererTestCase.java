/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.container.internal;

import static com.zernova.runtime.container.internal.JreModuleDiscoverer.JRE_MODULE_NAME;

import static org.apache.commons.lang3.JavaVersion.JAVA_17;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtLeast;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.IsEqual.equalTo;

import com.zernova.runtime.container.api.ZernovaModule;
import com.zernova.runtime.jpms.api.ZernovaContainerModule;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.qameta.allure.Issue;

public class JreModuleDiscovererTestCase extends AbstractZernovaTestCase {

  @Rule
  public ExpectedException expected = ExpectedException.none();

  private final JreModuleDiscoverer moduleDiscoverer = new JreModuleDiscoverer();

  @Test
  public void discoversJreModule() throws Exception {
    final List<ZernovaContainerModule> muleModules = moduleDiscoverer.discover();

    assertThat(muleModules.size(), equalTo(1));
    final ZernovaModule muleModule = (ZernovaModule) muleModules.get(0);
    assertThat(muleModule.getName(), equalTo(JRE_MODULE_NAME));
    assertThat("exportedPackages", muleModule.getExportedPackages(), is(not(empty())));
    if (isJavaVersionAtLeast(JAVA_17)) {
      // Respect the encapsulation of the java modules
      assertThat("exportedPaths", muleModule.getExportedPaths(), is(empty()));
      assertThat("exportedServices", muleModule.getExportedServices(), is(empty()));
    } else {
      assertThat("exportedPaths", muleModule.getExportedPaths(), is(not(empty())));
      assertThat("exportedServices", muleModule.getExportedServices(), is(not(empty())));
    }
  }

  @Test
  @Issue("MULE-19398")
  public void discoveryResultCached() throws Exception {
    final List<ZernovaContainerModule> discovered = moduleDiscoverer.discover();
    final List<ZernovaContainerModule> discoveredFromCache = moduleDiscoverer.discover();

    assertThat(discoveredFromCache, sameInstance(discovered));
  }
}
