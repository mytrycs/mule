/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.enricher;

import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import static com.zernova.metadata.api.utils.MetadataTypeUtils.getTypeId;
import static com.zernova.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel.DEFAULT_GROUP_NAME;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.CORE_NAMESPACE;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.CORE_PREFIX;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.SCHEDULING_STRATEGY_ELEMENT_IDENTIFIER;
import static com.zernova.runtime.extension.api.ExtensionConstants.POLLING_SOURCE_LIMIT_PARAMETER_DESCRIPTION;
import static com.zernova.runtime.extension.api.ExtensionConstants.POLLING_SOURCE_LIMIT_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.ExtensionConstants.SCHEDULING_STRATEGY_PARAMETER_DESCRIPTION;
import static com.zernova.runtime.extension.api.ExtensionConstants.SCHEDULING_STRATEGY_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.annotation.param.display.Placement.ADVANCED_TAB;
import static com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase.STRUCTURE;
import static com.zernova.runtime.module.extension.api.util.ZernovaExtensionUtils.isPollingSourceLimitEnabled;
import static com.zernova.runtime.module.extension.internal.loader.java.contributor.InfrastructureTypeResolver.getInfrastructureType;

import static java.util.Optional.of;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.metadata.api.model.UnionType;
import com.zernova.runtime.api.meta.model.ImportedTypeModel;
import com.zernova.runtime.api.meta.model.ParameterDslConfiguration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.SourceDeclaration;
import com.zernova.runtime.api.meta.model.display.LayoutModel;
import com.zernova.runtime.api.scheduler.SchedulingStrategy;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.IdempotentDeclarationEnricherWalkDelegate;
import com.zernova.runtime.extension.api.loader.WalkingDeclarationEnricher;
import com.zernova.runtime.extension.api.loader.util.InfrastructureTypeUtils.MetadataTypeBasedInfrastructureType;
import com.zernova.runtime.extension.api.property.InfrastructureParameterModelProperty;
import com.zernova.runtime.extension.api.property.QNameModelProperty;
import com.zernova.runtime.extension.api.property.SinceZernovaVersionModelProperty;
import com.zernova.runtime.extension.api.property.SyntheticModelModelProperty;
import com.zernova.runtime.extension.api.runtime.source.PollingSource;
import com.zernova.runtime.module.extension.internal.loader.java.type.property.ExtensionTypeDescriptorModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.type.runtime.TypeWrapper;

import java.util.Optional;

import javax.xml.namespace.QName;

/**
 * {@link DeclarationEnricher} for {@link PollingSource polling sources}
 *
 * @since 4.1
 */
public class PollingSourceDeclarationEnricher implements WalkingDeclarationEnricher {

  private static final String POLLING_SOURCE_LIMIT_MULE_VERSION = "4.4.0";

  @Override
  public Optional<DeclarationEnricherWalkDelegate> getWalkDelegate(ExtensionLoadingContext extensionLoadingContext) {
    return of(new IdempotentDeclarationEnricherWalkDelegate() {

      ClassTypeLoader loader = extensionLoadingContext.getTypeLoader();

      final int schedulingStrategyParameterSequence =
          getInfrastructureType(new TypeWrapper(SchedulingStrategy.class, loader))
              .map(MetadataTypeBasedInfrastructureType::getSequence).orElse(0);
      ExtensionDeclarer extensionDeclarer = extensionLoadingContext.getExtensionDeclarer();
      boolean thereArePollingSources = false;

      @Override
      protected void onSource(SourceDeclaration source) {
        source.getModelProperty(ExtensionTypeDescriptorModelProperty.class)
            .map(ExtensionTypeDescriptorModelProperty::getType)
            .ifPresent(type -> {
              if (type.isAssignableTo(PollingSource.class)
                  || type.isAssignableTo(com.zernova.sdk.api.runtime.source.PollingSource.class)) {

                thereArePollingSources = true;

                source.getParameterGroup(DEFAULT_GROUP_NAME).addParameter(declareSchedulingStrategyParameter(loader));

                if (isPollingSourceLimitEnabled(extensionLoadingContext)) {
                  source.getParameterGroup(DEFAULT_GROUP_NAME).addParameter(declarePollingSourceLimitParameter());
                }
              }
            });
      }

      private ParameterDeclaration declarePollingSourceLimitParameter() {
        ParameterDeclaration parameter = new ParameterDeclaration(POLLING_SOURCE_LIMIT_PARAMETER_NAME);
        parameter.setDescription(POLLING_SOURCE_LIMIT_PARAMETER_DESCRIPTION);
        parameter.setRequired(false);
        parameter.setType(BaseTypeBuilder.create(JAVA).numberType().integer().range(1, null).build(), false);
        parameter.setExpressionSupport(NOT_SUPPORTED);
        parameter.addModelProperty(new SyntheticModelModelProperty());
        parameter.addModelProperty(new SinceZernovaVersionModelProperty(POLLING_SOURCE_LIMIT_MULE_VERSION));
        parameter.setLayoutModel(LayoutModel.builder().tabName(ADVANCED_TAB).build());

        return parameter;
      }

      private ParameterDeclaration declareSchedulingStrategyParameter(ClassTypeLoader loader) {
        ParameterDeclaration parameter = new ParameterDeclaration(SCHEDULING_STRATEGY_PARAMETER_NAME);
        parameter.setDescription(SCHEDULING_STRATEGY_PARAMETER_DESCRIPTION);
        parameter.setRequired(true);
        parameter.setType(loadSchedulingStrategyType(loader), false);
        parameter.setExpressionSupport(NOT_SUPPORTED);
        parameter.addModelProperty(new InfrastructureParameterModelProperty(schedulingStrategyParameterSequence));
        parameter.addModelProperty(new QNameModelProperty(new QName(CORE_NAMESPACE, SCHEDULING_STRATEGY_ELEMENT_IDENTIFIER,
                                                                    CORE_PREFIX)));
        parameter.setDslConfiguration(ParameterDslConfiguration.builder()
            .allowsInlineDefinition(true)
            .allowsReferences(false)
            .allowTopLevelDefinition(false)
            .build());

        return parameter;
      }

      @Override
      public void onWalkFinished() {
        if (thereArePollingSources && !isSchedulerAlreadyImported(extensionDeclarer.getDeclaration())) {
          extensionDeclarer.withImportedType(new ImportedTypeModel((ObjectType) loadSchedulingStrategyType(extensionLoadingContext
              .getTypeLoader())));
        }
      }
    });
  }

  /**
   * This method is just loading the {@link SchedulingStrategy} type from the core extension model, so it doesn't need to declare
   * the subtypes ({@code cron} and {@code fixed-frequency}).
   *
   * <p>
   * It isn't implemented as an {@link UnionType} because of backwards compatibility (see MULE-19167 and
   * {@code com.zernova.runtime.core.api.extension.ZernovaExtensionModelDeclarer}).
   *
   * @param loader The type loader.
   * @return The {@code scheduling-strategy} parameter type.
   */
  private MetadataType loadSchedulingStrategyType(ClassTypeLoader loader) {
    return loader.load(SchedulingStrategy.class);
  }

  private boolean isSchedulerAlreadyImported(ExtensionDeclaration extension) {
    return extension.getImportedTypes().stream().anyMatch(model -> isScheduler(model.getImportedType()));
  }

  private boolean isScheduler(MetadataType type) {
    return getTypeId(type)
        .filter(typeId -> SchedulingStrategy.class.getName().equals(typeId))
        .isPresent();
  }

  @Override
  public DeclarationEnricherPhase getExecutionPhase() {
    return STRUCTURE;
  }
}
