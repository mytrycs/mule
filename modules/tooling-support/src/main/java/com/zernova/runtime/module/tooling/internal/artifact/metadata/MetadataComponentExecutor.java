/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.artifact.metadata;

import static com.zernova.runtime.api.metadata.resolving.FailureCode.INVALID_METADATA_KEY;
import static com.zernova.runtime.api.metadata.resolving.MetadataFailure.Builder.newFailure;
import static com.zernova.runtime.api.metadata.resolving.MetadataResult.failure;
import static com.zernova.runtime.api.metadata.resolving.MetadataResult.success;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.CONFIG;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getClassLoader;

import static java.lang.String.format;

import static com.google.common.base.Throwables.propagateIfPossible;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.HasOutputModel;
import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.descriptor.ComponentMetadataTypesDescriptor;
import com.zernova.runtime.api.metadata.descriptor.InputMetadataDescriptor;
import com.zernova.runtime.api.metadata.descriptor.OutputMetadataDescriptor;
import com.zernova.runtime.api.metadata.resolving.MetadataFailure;
import com.zernova.runtime.api.metadata.resolving.MetadataResult;
import com.zernova.runtime.app.declaration.api.ComponentElementDeclaration;
import com.zernova.runtime.app.declaration.api.ElementDeclaration;
import com.zernova.runtime.core.api.connector.ConnectionManager;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.extension.api.property.TypeResolversInformationModelProperty;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.metadata.api.cache.MetadataCacheIdGenerator;
import com.zernova.runtime.metadata.internal.cache.MetadataCacheManager;
import com.zernova.runtime.module.extension.api.tooling.metadata.MetadataMediator;
import com.zernova.runtime.module.extension.internal.metadata.DefaultMetadataMediator;
import com.zernova.runtime.module.extension.internal.util.ReflectionCache;
import com.zernova.runtime.module.tooling.internal.artifact.ExecutorExceptionWrapper;
import com.zernova.runtime.module.tooling.internal.artifact.params.ExpressionNotSupportedException;
import com.zernova.runtime.module.tooling.internal.utils.ArtifactHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataComponentExecutor extends MetadataExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataComponentExecutor.class);

  public MetadataComponentExecutor(ConnectionManager connectionManager, ReflectionCache reflectionCache,
                                   ExpressionManager expressionManager, ArtifactHelper artifactHelper,
                                   MetadataCacheIdGenerator<ElementDeclaration> metadataCacheIdGenerator,
                                   MetadataCacheManager metadataCacheManager) {
    super(connectionManager, reflectionCache, expressionManager, artifactHelper, metadataCacheIdGenerator, metadataCacheManager);
    this.expressionManager = expressionManager;
  }

  public MetadataResult<ComponentMetadataTypesDescriptor> resolveComponentMetadata(ComponentModel componentModel,
                                                                                   ComponentElementDeclaration componentElementDeclaration) {
    try {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Resolve component metadata STARTED for component: {}", componentModel.getName());
      }
      Optional<ConfigurationInstance> optionalConfigurationInstance =
          getConfigurationInstance(componentModel, componentElementDeclaration);

      MetadataKeyResult metadataKeyResult =
          new MetadataKeyDeclarationResolver(componentModel, componentElementDeclaration, expressionManager).resolveKeyResult();
      if (!metadataKeyResult.isComplete()) {
        return failure(newFailure()
            .withMessage(metadataKeyResult.getPartialReason())
            .withFailureCode(INVALID_METADATA_KEY)
            .onComponent());
      }

      MetadataKey metadataKey = metadataKeyResult.getMetadataKey();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Invoking connector's component metadata resolver for component: {} with key: {}", componentModel.getName(),
                     metadataKey);
      }
      ClassLoader extensionClassLoader = getClassLoader(artifactHelper.getExtensionModel(componentElementDeclaration));

      return resolveMetadata(componentElementDeclaration, componentModel, optionalConfigurationInstance, metadataKey,
                             extensionClassLoader);
    } catch (MetadataResolvingException e) {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn(format("Resolve component metadata has FAILED with code: %s for component: %s", e.getFailure(),
                           componentModel.getName()),
                    e);
      }
      return failure(newFailure(e).withFailureCode(e.getFailure()).onComponent());
    } catch (ExpressionNotSupportedException e) {
      return failure(newFailure(e).withFailureCode(INVALID_METADATA_KEY).onKeys());
    } catch (ExecutorExceptionWrapper e) {
      Throwable cause = e.getCause();
      if (cause instanceof MetadataResolvingException) {
        MetadataResolvingException metadataResolvingException = (MetadataResolvingException) cause;
        LOGGER.warn(format("Resolve component metadata has FAILED with code: %s for component: %s",
                           metadataResolvingException.getFailure(), componentModel.getName()),
                    cause);
        return failure(newFailure(e).withFailureCode(metadataResolvingException.getFailure())
            .onKeys());
      }
      propagateIfPossible(cause, ZernovaRuntimeException.class);
      throw new ZernovaRuntimeException(cause);
    } catch (Exception e) {
      propagateIfPossible(e, ZernovaRuntimeException.class);
      throw new ZernovaRuntimeException(e);
    } finally {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Resolve component metadata FINISHED for component: {}", componentModel.getName());
      }
    }
  }

  @Override
  protected boolean resolverRequiresConfiguration(Optional<TypeResolversInformationModelProperty> typeResolversInformationModelProperty,
                                                  ComponentModel componentModel) {
    // TODO MULE-15638 it is not correct the information provided by the TypeResolversInformationModelProperty model property
    // return typeResolversInformationModelProperty
    // .map(mp -> {
    // if (mp.getOutputResolver().map(resolverInformation -> resolverInformation.isRequiresConfiguration()).orElse(false)) {
    // return true;
    // }
    // if (mp.getAttributesResolver().map(resolverInformation -> resolverInformation.isRequiresConfiguration()).orElse(false)) {
    // return true;
    // }
    // return componentModel.getAllParameterModels().stream().map(parameter -> mp.getParameterResolver(parameter.getName())
    // .map(resolverInformation -> resolverInformation.isRequiresConfiguration()).orElse(false)).findFirst().orElse(false);
    // }).orElse(false);
    return artifactHelper.hasParameterOfType(componentModel, CONFIG);
  }


  private MetadataResult<ComponentMetadataTypesDescriptor> resolveMetadata(ComponentElementDeclaration componentElementDeclaration,
                                                                           ComponentModel componentModel,
                                                                           Optional<ConfigurationInstance> configurationInstance,
                                                                           MetadataKey metadataKey,
                                                                           ClassLoader extensionClassLoader)
      throws MetadataResolvingException {
    MetadataMediator metadataMediator = new DefaultMetadataMediator<>(componentModel, reflectionCache);

    return withContextClassLoader(extensionClassLoader,
                                  () -> runWithMetadataContext(componentElementDeclaration, configurationInstance,
                                                               extensionClassLoader, metadataContext -> {
                                                                 MetadataResult<InputMetadataDescriptor> inputMetadata =
                                                                     metadataMediator
                                                                         .getInputMetadata(metadataContext, metadataKey);
                                                                 MetadataResult<OutputMetadataDescriptor> outputMetadata = null;
                                                                 if (componentModel instanceof HasOutputModel) {
                                                                   outputMetadata = metadataMediator
                                                                       .getOutputMetadata(metadataContext, metadataKey);
                                                                 }
                                                                 return collectMetadata(inputMetadata, outputMetadata);
                                                               }),
                                  MetadataResolvingException.class, e -> {
                                    throw new ExecutorExceptionWrapper(e);
                                  });
  }

  private MetadataResult<ComponentMetadataTypesDescriptor> collectMetadata(MetadataResult<InputMetadataDescriptor> inputMetadataResult,
                                                                           MetadataResult<OutputMetadataDescriptor> outputMetadataResult) {
    if (inputMetadataResult.isSuccess() && (outputMetadataResult == null || outputMetadataResult.isSuccess())) {
      ComponentMetadataTypesDescriptor.ComponentMetadataTypesDescriptorBuilder builder =
          ComponentMetadataTypesDescriptor.builder().withInputMetadataDescriptor(inputMetadataResult.get());
      if (outputMetadataResult != null) {
        builder.withOutputMetadataDescriptor(outputMetadataResult.get());
      }
      return success(builder.build());
    }
    List<MetadataFailure> failures = new ArrayList<>(inputMetadataResult.getFailures());
    if (outputMetadataResult != null) {
      failures.addAll(outputMetadataResult.getFailures());
    }
    return failure(failures);
  }

}
