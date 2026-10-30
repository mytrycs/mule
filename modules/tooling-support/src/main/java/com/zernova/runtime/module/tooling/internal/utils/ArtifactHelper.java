/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.utils;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static com.zernova.metadata.java.api.utils.JavaTypeUtils.getType;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.internal.event.NullEventFactory.getNullEvent;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getClassLoader;

import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.EnrichableModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;
import com.zernova.runtime.api.util.Reference;
import com.zernova.runtime.app.declaration.api.ArtifactDeclaration;
import com.zernova.runtime.app.declaration.api.ComponentElementDeclaration;
import com.zernova.runtime.app.declaration.api.ConfigurationElementDeclaration;
import com.zernova.runtime.app.declaration.api.ElementDeclaration;
import com.zernova.runtime.app.declaration.api.GlobalElementDeclarationVisitor;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;

import java.util.List;
import java.util.Optional;

public class ArtifactHelper {

  private ExtensionManager extensionManager;
  private ArtifactDeclaration artifactDeclaration;
  private ConfigurationComponentLocator componentLocator;

  public ArtifactHelper(ExtensionManager extensionManager,
                        ConfigurationComponentLocator componentLocator,
                        ArtifactDeclaration artifactDeclaration) {
    this.extensionManager = extensionManager;
    this.componentLocator = componentLocator;
    this.artifactDeclaration = artifactDeclaration;
  }

  public ExtensionModel getExtensionModel(ElementDeclaration declaration) {
    return findExtension(declaration)
        .orElseThrow(() -> new ZernovaRuntimeException(createStaticMessage("There is no extensionModel for extension: %s",
                                                                        declaration.getDeclaringExtension())));
  }

  public Optional<ExtensionModel> findExtension(ElementDeclaration declaration) {
    return extensionManager.getExtension(declaration.getDeclaringExtension());
  }

  public <T> Class<T> getParameterClass(ParameterModel parameterModel, ElementDeclaration containerDeclaration) {
    return getType(parameterModel.getType(), getClassLoader(getExtensionModel(containerDeclaration)));
  }

  public <T extends ParameterizedModel & EnrichableModel> Optional<T> findModel(ExtensionModel extensionModel,
                                                                                ElementDeclaration elementDeclaration) {
    return ArtifactHelperUtils.findModel(extensionModel, elementDeclaration);
  }

  public Optional<? extends ComponentModel> findComponentModel(ExtensionModel extensionModel,
                                                               ComponentElementDeclaration<?> componentDeclaration) {
    return findModel(extensionModel, componentDeclaration).filter(m -> m instanceof ComponentModel).map(m -> (ComponentModel) m);
  }

  public boolean hasParameterOfType(ComponentModel componentModel,
                                    StereotypeModel referenceStereotype) {
    return componentModel.getAllParameterModels()
        .stream()
        .filter(paramModel -> paramModel.getAllowedStereotypes()
            .stream()
            .anyMatch(allowed -> allowed.isAssignableTo(referenceStereotype)))
        .findAny().isPresent();
  }

  public Optional<ConfigurationElementDeclaration> findConfigurationDeclaration(String configName) {
    final Reference<ConfigurationElementDeclaration> configDeclaration = new Reference<>();
    final GlobalElementDeclarationVisitor visitor = new GlobalElementDeclarationVisitor() {

      @Override
      public void visit(ConfigurationElementDeclaration declaration) {
        if (declaration.getRefName().equals(configName)) {
          configDeclaration.set(declaration);
        }
      }
    };
    artifactDeclaration.getGlobalElements().forEach(gld -> gld.accept(visitor));
    return ofNullable(configDeclaration.get());
  }

  private Optional<ConfigurationProvider> findConfigurationProvider(String configName) {
    return findConfigurationDeclaration(configName)
        .map(ced -> Location.builder().globalName(ced.getRefName()).build())
        .flatMap(cloc -> componentLocator.find(cloc))
        .filter(cp -> cp instanceof ConfigurationProvider)
        .map(cp -> (ConfigurationProvider) cp);
  }

  public Optional<ConfigurationInstance> getConfigurationInstance(String configName) {
    return findConfigurationProvider(configName).map(cp -> {
      CoreEvent fakeEvent = getNullEvent();
      try {
        return cp.get(fakeEvent);
      } finally {
        if (fakeEvent != null) {
          ((BaseEventContext) fakeEvent.getContext()).success();
        }
      }
    });
  }

  public List<String> getExtensions() {
    return extensionManager.getExtensions().stream().map(extensionModel -> extensionModel.getName()).collect(toList());
  }

}
