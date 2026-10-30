/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.internal.test.artifact;

import static org.hamcrest.CoreMatchers.containsString;
import static com.zernova.runtime.module.service.internal.artifact.ModuleLayerGraph.setModuleLayerId;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.zernova.runtime.module.service.internal.artifact.ModuleLayerGraph;
import org.junit.Before;
import org.junit.Test;

public class ModuleLayerGraphTestCase {

  private static final String GRAPH_REPR = "--------------\n" +
      "| testLayer  |\n" +
      "--------------\n" +
      "       |\n" +
      "       |\n" +
      "       V\n" +
      "--------------\n" +
      "|testParentLayer|\n" +
      "--------------\n";

  private ModuleLayerGraph graph;

  @Before
  public void setUp() throws Exception {
    ModuleLayer layer = this.getClass().getModule().getLayer();
    setModuleLayerId(this.getClass().getModule().getLayer(), "testLayer");
    setModuleLayerId(layer.parents().get(0), "testParentLayer");
    graph = new ModuleLayerGraph(layer);
  }

  @Test
  public void graphString() {
    assertThat(graph.graphString(), is(GRAPH_REPR));
  }

  @Test
  public void moduleLayerModules() {
    String info = graph.moduleLayerModules();
    // We don't assert for the exact string because it may come in different order, and depending on the modifications of
    // dependencies of this module, then this test may start failing even if we sort those dependencies' modules.
    assertThat(info, containsString("com.zernova.runtime.metadata.model.api"));
    assertThat(info, containsString("com.zernova.runtime.jpms.utils"));
    assertThat(info, containsString("com.zernova.runtime.metrics.api"));
    assertThat(info, containsString("com.zernova.runtime.tracer.api"));
    assertThat(info, containsString("com.zernova.runtime.manifest"));
    assertThat(info, containsString("com.zernova.runtime.dsl.api"));
    assertThat(info, containsString("com.zernova.runtime.jar.handling.utils"));
    assertThat(info, containsString("com.zernova.runtime.container"));
    assertThat(info, containsString("com.zernova.runtime.metadata.model.message"));
    assertThat(info, containsString("com.zernova.runtime.profiling.api"));
    assertThat(info, containsString("com.zernova.runtime.tracer.customization.api"));
    assertThat(info, containsString("com.zernova.runtime.tracer.exporter.configuration.api"));
    assertThat(info, containsString("com.zernova.runtime.policy.api"));
    assertThat(info, containsString("com.zernova.runtime.policy.api"));
    assertThat(info, containsString("com.zernova.runtime.core"));
    assertThat(info, containsString("com.zernova.runtime.metadata.model.java"));
    assertThat(info, containsString("com.zernova.runtime.artifact.declaration"));
    assertThat(info, containsString("com.zernova.runtime.errors"));
    assertThat(info, containsString("com.zernova.sdk.api"));
  }

}
