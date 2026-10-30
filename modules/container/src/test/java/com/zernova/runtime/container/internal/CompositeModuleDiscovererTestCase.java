/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.container.internal;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.container.api.ZernovaModule;
import com.zernova.runtime.container.api.discoverer.ModuleDiscoverer;
import com.zernova.runtime.jpms.api.ZernovaContainerModule;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CompositeModuleDiscovererTestCase extends AbstractZernovaTestCase {

  @Test
  public void delegatesToComposedDiscovers() throws Exception {
    final ModuleDiscoverer discoverer1 = mock(ModuleDiscoverer.class);
    ZernovaModule module1 = new ZernovaModule("module1", emptySet(), emptySet(), emptySet(), emptySet(), emptyList());
    final List<ZernovaContainerModule> modules1 = new ArrayList<>();
    modules1.add(module1);
    when(discoverer1.discover()).thenReturn(modules1);
    ZernovaModule module2 = new ZernovaModule("module1", emptySet(), emptySet(), emptySet(), emptySet(), emptyList());
    final List<ZernovaContainerModule> modules2 = new ArrayList<>();
    modules2.add(module2);
    final ModuleDiscoverer discoverer2 = mock(ModuleDiscoverer.class);
    when(discoverer2.discover()).thenReturn(modules2);

    final CompositeModuleDiscoverer composite = new CompositeModuleDiscoverer(discoverer1, discoverer2);
    final List<ZernovaContainerModule> discovered = composite.discover();

    assertThat(discovered.size(), equalTo(2));
    assertThat(discovered, contains(module1, module2));
  }
}
