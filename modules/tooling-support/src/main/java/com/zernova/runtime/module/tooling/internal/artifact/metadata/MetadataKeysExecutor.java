/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.artifact.metadata;

import static com.zernova.runtime.api.metadata.resolving.MetadataResult.failure;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.CONFIG;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getClassLoader;

import static java.lang.String.format;

import static com.google.common.base.Throwables.propagateIfPossible;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.MetadataKeysContainer;
import com.zernova.runtime.api.metadata.MetadataResolvingException;
import com.zernova.runtime.api.metadata.resolving.FailureCode;
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
import com.zernova.runtime.module.tooling.internal.artifact.AbstractParameterResolverExecutor;
import com.zernova.runtime.module.tooling.internal.artifact.ExecutorExceptionWrapper;
import com.zernova.runtime.module.tooling.internal.artifact.params.ExpressionNotSupportedException;
import com.zernova.runtime.module.tooling.internal.utils.ArtifactHelper;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataKeysExecutor extends MetadataExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataKeysExecutor.class);

  private static final FailureCode INVALID_PARAMETER_VALUE =
      new FailureCode(AbstractParameterResolverExecutor.INVALID_PARAMETER_VALUE);

  public MetadataKeysExecutor(ConnectionManager connectionManager, ReflectionCache reflectionCache,
                              ExpressionManager expressionManager, ArtifactHelper artifactHelper,
                              MetadataCacheIdGenerator<ElementDeclaration> metadataCacheIdGenerator,
                              MetadataCacheManager metadataCacheManager) {
    super(connectionManager, reflectionCache, expressionManager, artifactHelper, metadataCacheIdGenerator,
          metadataCacheManager);
  }

  public MetadataResult<MetadataKeysContainer> resolveMetadataKeys(ComponentModel componentModel,
                                                                   ComponentElementDeclaration componentElementDeclaration) {
    try {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Resolve metadata keys STARTED for component: {}", componentModel.getName());
      }
      Optional<ConfigurationInstance> optionalConfigurationInstance =
          getConfigurationInstance(componentModel, componentElementDeclaration);

      MetadataKey metadataKey =
          new MetadataKeyDeclarationResolver(componentModel, componentElementDeclaration, expressionManager).resolvePartialKey();

      ClassLoader extensionClassLoader = getClassLoader(artifactHelper.getExtensionModel(componentElementDeclaration));

      MetadataMediator metadataMediator = new DefaultMetadataMediator<>(componentModel, reflectionCache);

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Invoking connector's metadata key resolver for component: {}", componentModel.getName());
      }
      return withContextClassLoader(extensionClassLoader,
                                    () -> runWithMetadataContext(componentElementDeclaration, optionalConfigurationInstance,
                                                                 extensionClassLoader,
                                                                 (metadataContext) -> metadataMediator
                                                                     .getMetadataKeys(metadataContext, metadataKey)),
                                    MetadataResolvingException.class, e -> {
                                      throw new ExecutorExceptionWrapper(e);
                                    });
    } catch (MetadataResolvingException e) {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn(format("Resolve metadata keys has FAILED with code: %s for component: %s", e.getFailure(),
                           componentModel.getName()),
                    e);
      }
      return failure(MetadataFailure.Builder.newFailure(e).withFailureCode(e.getFailure()).onKeys());
    } catch (ExpressionNotSupportedException e) {
      return failure(MetadataFailure.Builder.newFailure(e).withFailureCode(INVALID_PARAMETER_VALUE).onKeys());
    } catch (ExecutorExceptionWrapper e) {
      Throwable cause = e.getCause();
      if (cause instanceof MetadataResolvingException) {
        MetadataResolvingException metadataResolvingException = (MetadataResolvingException) cause;
        if (LOGGER.isWarnEnabled()) {
          LOGGER.warn(format("Resolve metadata keys has FAILED with code: %s for component: %s",
                             metadataResolvingException.getFailure(), componentModel.getName()),
                      cause);
        }
        return failure(MetadataFailure.Builder.newFailure(e).withFailureCode(metadataResolvingException.getFailure())
            .onKeys());
      }
      propagateIfPossible(cause, ZernovaRuntimeException.class);
      throw new ZernovaRuntimeException(cause);
    } catch (Exception e) {
      propagateIfPossible(e, ZernovaRuntimeException.class);
      throw new ZernovaRuntimeException(e);
    } finally {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Resolve metadata keys FINISHED for component: {}", componentModel.getName());
      }
    }
  }

  @Override
  protected boolean resolverRequiresConfiguration(Optional<TypeResolversInformationModelProperty> typeResolversInformationModelProperty,
                                                  ComponentModel componentModel) {
    // TODO MULE-15638 it is not correct the information provided by the TypeResolversInformationModelProperty model property
    // return typeResolversInformationModelProperty.map(mp -> mp.getKeysResolver()
    // .map(resolverInformation -> resolverInformation.isRequiresConfiguration()).orElse(false))
    // .orElse(false);
    return artifactHelper.hasParameterOfType(componentModel, CONFIG);
  }

}
