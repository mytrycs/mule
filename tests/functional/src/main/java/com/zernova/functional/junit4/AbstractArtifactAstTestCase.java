/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.junit4;

import static com.zernova.runtime.api.component.TypedComponentIdentifier.ComponentType.CONNECTION;
import static com.zernova.runtime.ast.api.ArtifactType.APPLICATION;
import static com.zernova.runtime.ast.api.util.ZernovaAstUtils.emptyArtifact;
import static com.zernova.runtime.module.extension.internal.loader.java.AbstractJavaExtensionModelLoader.TYPE_PROPERTY_NAME;
import static com.zernova.runtime.module.extension.internal.loader.java.AbstractJavaExtensionModelLoader.VERSION;
import static com.zernova.sdk.api.stereotype.ZernovaStereotypes.CONFIG;

import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;

import com.zernova.runtime.api.component.TypedComponentIdentifier.ComponentType;
import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;
import com.zernova.runtime.module.extension.internal.loader.java.DefaultJavaExtensionModelLoader;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;

/**
 * Case class for test cases that need to load the AST for a config but do not need the whole app to be deployed/started.
 */
public abstract class AbstractArtifactAstTestCase extends AbstractZernovaContextTestCase {

  private static final CachingAstXmlParser AST_PARSER =
      new CachingAstXmlParser(true, false, emptyMap(), APPLICATION, emptyArtifact());

  private ArtifactAst appAst;

  @Before
  public void loadAst() {
    appAst = AST_PARSER.parse(this.getClass().getName(),
                              getRequiredExtensions(),
                              this.getClass().getClassLoader(),
                              empty(),
                              null,
                              new String[] {getConfigFile()});
  }

  protected abstract String getConfigFile();

  protected abstract Set<ExtensionModel> getRequiredExtensions();

  protected ExtensionModel loadExtension(Class extension, Set<ExtensionModel> deps) {
    DefaultJavaExtensionModelLoader loader = new DefaultJavaExtensionModelLoader();
    return loadExtensionWithLoader(extension, deps, loader);
  }

  protected ExtensionModel loadExtensionWithLoader(Class extension, Set<ExtensionModel> deps,
                                                   ExtensionModelLoader extensionModelLoader) {
    Map<String, Object> ctx = new HashMap<>();
    ctx.put(TYPE_PROPERTY_NAME, extension.getName());
    ctx.put(VERSION, "4.10.0");
    ctx.putAll(getExtensionLoaderContextAdditionalParameters());
    return extensionModelLoader.loadExtensionModel(currentThread().getContextClassLoader(), DslResolvingContext.getDefault(deps),
                                                   ctx);
  }

  /**
   * Subclasses can override this method so that extension models are generated with an extension loading context that contains
   * the parameters returned by this method.
   *
   * @return a map with parameters to be added to the extension loader context.
   */
  protected Map<String, Object> getExtensionLoaderContextAdditionalParameters() {
    return emptyMap();
  }

  public ArtifactAst getAppAst() {
    return appAst;
  }

  protected ComponentAst getTopLevelComponent(String configName) {
    return getAppAst().topLevelComponentsStream()
        .filter(f -> componentIdEquals(f, configName))
        .findFirst().orElseThrow();
  }

  protected ComponentAst getConnectionProvider(ComponentAst configAst) {
    return configAst.directChildrenStream()
        .filter(c -> CONNECTION.equals(c.getComponentType()))
        .findAny()
        .orElseThrow();
  }

  protected ComponentAst getFlowComponent(String flowName, ComponentType componentType) {
    return getAppAst().topLevelComponentsStream()
        .filter(f -> componentIdEquals(f, flowName))
        .flatMap(flowAst -> flowAst.directChildrenStream()
            .filter(comp -> componentType.equals(comp.getComponentType())))
        .findFirst().orElseThrow();
  }

  private boolean componentIdEquals(ComponentAst component, String componentId) {
    return component.getComponentId()
        .map(componentId::equals)
        .orElse(false);
  }

  protected Optional<String> configNameFromComponent(ComponentAst comp) {
    return comp.getParameters()
        .stream()
        .filter(p -> p.getModel().getAllowedStereotypes()
            .stream()
            .anyMatch(as -> as.isAssignableTo(CONFIG)))
        .map(p -> (String) p.getValue().getRight())
        .findAny();
  }

}
