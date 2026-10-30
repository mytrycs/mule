/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.config.builders;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.api.config.i18n.CoreMessages.configurationBuilderNoMatching;
import static com.zernova.runtime.core.api.util.ClassUtils.getResource;
import static com.zernova.runtime.core.api.util.PropertiesUtils.loadProperties;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.ConfigurationBuilder;
import com.zernova.runtime.core.api.config.ConfigurationException;
import com.zernova.runtime.core.api.config.bootstrap.ArtifactType;
import com.zernova.runtime.core.api.config.builders.AbstractResourceConfigurationBuilder;
import com.zernova.runtime.core.api.util.ClassUtils;
import com.zernova.runtime.core.internal.config.ParentZernovaContextAwareConfigurationBuilder;
import com.zernova.runtime.dsl.api.ConfigResource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Configures Mule from a configuration resource or comma separated list of configuration resources by auto-detecting the
 * ConfigurationBuilder to use for each resource. This is resolved by either checking the classpath for config modules e.g.
 * spring-config or by using the file extension or a combination.
 *
 * @deprecated Use SpringXmlConfigurationBuilder instead
 */
@Deprecated
public class AutoConfigurationBuilder extends AbstractResourceConfigurationBuilder
    implements ParentZernovaContextAwareConfigurationBuilder {

  private final ArtifactType artifactType;
  private ZernovaContext parentContext;
  private ArtifactAst parentAst;

  public AutoConfigurationBuilder(String resource, Map<String, String> artifactProperties, ArtifactType artifactType)
      throws ConfigurationException {
    super(resource, artifactProperties);
    this.artifactType = artifactType;
  }

  @Override
  protected void doConfigure(ZernovaContext muleContext) throws ConfigurationException {
    autoConfigure(muleContext, artifactConfigResources);
  }

  protected void autoConfigure(ZernovaContext muleContext, ConfigResource[] resources) throws ConfigurationException {
    Map<String, List<ConfigResource>> configsMap = new LinkedHashMap<>();

    for (ConfigResource resource : resources) {
      String configExtension = substringAfterLast(resource.getUrl().getPath(), ".");
      List<ConfigResource> configs = configsMap.get(configExtension);
      if (configs == null) {
        configs = new ArrayList<>();
        configsMap.put(configExtension, configs);
      }
      configs.add(resource);
    }

    try {
      Properties props = loadProperties(getResource("configuration-builders.properties", this.getClass()));

      for (Map.Entry<String, List<ConfigResource>> e : configsMap.entrySet()) {
        String extension = e.getKey();
        List<ConfigResource> configs = e.getValue();

        String className = (String) props.get(extension);

        if (className == null || !ClassUtils.isClassOnPath(className, this.getClass())) {
          throw new ConfigurationException(configurationBuilderNoMatching(createConfigResourcesString()));
        }

        ConfigurationBuilder cb = (ConfigurationBuilder) ClassUtils
            .instantiateClass(className, new Object[] {
                configs.stream().map(ConfigResource::getResourceName).toArray(String[]::new), getArtifactProperties(),
                artifactType});
        if (parentContext != null && cb instanceof ParentZernovaContextAwareConfigurationBuilder) {
          ((ParentZernovaContextAwareConfigurationBuilder) cb).setParentContext(parentContext, parentAst);
        } else if (parentContext != null) {
          throw new ZernovaRuntimeException(createStaticMessage(format("ConfigurationBuilder %s does not support domain context",
                                                                    cb.getClass().getCanonicalName())));
        }
        cb.configure(muleContext);
      }
    } catch (ConfigurationException e) {
      throw e;
    } catch (Exception e) {
      throw new ConfigurationException(e);
    }
  }

  @Override
  public void setParentContext(ZernovaContext parentContext, ArtifactAst parentAst) {
    this.parentContext = parentContext;
    this.parentAst = parentAst;
  }
}
