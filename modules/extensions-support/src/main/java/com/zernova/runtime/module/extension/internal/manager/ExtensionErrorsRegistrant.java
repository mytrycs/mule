/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.manager;

import static com.zernova.runtime.api.component.ComponentIdentifier.builder;
import static com.zernova.runtime.core.api.error.Errors.CORE_NAMESPACE_NAME;
import static com.zernova.runtime.core.api.error.Errors.Identifiers.CONNECTIVITY_ERROR_IDENTIFIER;
import static com.zernova.runtime.core.api.error.Errors.Identifiers.RETRY_EXHAUSTED_ERROR_IDENTIFIER;
import static com.zernova.runtime.module.extension.internal.loader.utils.ExtensionNamespaceUtils.getExtensionsNamespace;

import static java.util.stream.Stream.concat;

import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.meta.model.ConnectableComponentModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.error.ErrorModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.core.api.exception.ExceptionMapper;
import com.zernova.runtime.core.api.exception.ExceptionMapper.Builder;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyExhaustedException;
import com.zernova.runtime.core.privileged.exception.ErrorTypeLocator;
import com.zernova.runtime.extension.api.dsl.syntax.DslElementSyntax;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.SingleExtensionImportTypesStrategy;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Extension's {@link ErrorType} registrant.
 * <p>
 * For each {@link OperationModel} from an {@link ExtensionModel} consumes the declared {@link ErrorModel}s and registers an
 * {@link ExceptionMapper} for the operation.
 *
 * @see ErrorModel
 * @see ErrorType
 * @see ErrorTypeRepository
 * @see ErrorTypeLocator
 * @since 4.0
 */
public final class ExtensionErrorsRegistrant {

  public static void registerErrorMappings(final ErrorTypeRepository errorTypeRepository, final ErrorTypeLocator errorTypeLocator,
                                           final Set<ExtensionModel> dependencies,
                                           Function<ExtensionModel, Optional<DslSyntaxResolver>> dslSyntaxResolverLookup) {
    dependencies
        .stream()
        .forEach(extModel -> {
          DslSyntaxResolver syntaxResolver = dslSyntaxResolverLookup.apply(extModel)
              .orElseGet(() -> DslSyntaxResolver.getDefault(extModel, new SingleExtensionImportTypesStrategy()));
          String nsp = getExtensionsNamespace(extModel);

          final Builder mapperBuilder = ExceptionMapper.builder();

          errorTypeRepository.lookupErrorType(builder().namespace(nsp).name(CONNECTIVITY_ERROR_IDENTIFIER).build())
              .filter(err -> err.getParentErrorType().getNamespace().equals(CORE_NAMESPACE_NAME)
                  && err.getParentErrorType().getIdentifier().equals(CONNECTIVITY_ERROR_IDENTIFIER))
              .ifPresent(err -> mapperBuilder.addExceptionMapping(ConnectionException.class, err));

          errorTypeRepository.lookupErrorType(builder().namespace(nsp).name(RETRY_EXHAUSTED_ERROR_IDENTIFIER).build())
              .filter(err -> err.getParentErrorType().getNamespace().equals(CORE_NAMESPACE_NAME)
                  && err.getParentErrorType().getIdentifier().equals(RETRY_EXHAUSTED_ERROR_IDENTIFIER))
              .ifPresent(err -> mapperBuilder.addExceptionMapping(RetryPolicyExhaustedException.class, err));

          addComponentExceptionMappers(errorTypeLocator, extModel, syntaxResolver, mapperBuilder.build());
        });
  }

  private static void addComponentExceptionMappers(final ErrorTypeLocator errorTypeLocator, ExtensionModel extModel,
                                                   DslSyntaxResolver syntaxResolver, final ExceptionMapper mapping) {
    concat(extModel.getConfigurationModels().stream()
        .flatMap(config -> concat(config.getOperationModels().stream(),
                                  config.getSourceModels().stream())),
           concat(extModel.getOperationModels().stream(),
                  extModel.getSourceModels().stream()))

                      .map(model -> identifierFromModel(syntaxResolver, model))
                      .forEach(identifier -> errorTypeLocator.addComponentExceptionMapper(identifier, mapping));
  }

  private static ComponentIdentifier identifierFromModel(DslSyntaxResolver syntaxResolver, ConnectableComponentModel model) {
    final DslElementSyntax dsl = syntaxResolver.resolve(model);
    return ComponentIdentifier.builder()
        .name(dsl.getElementName())
        .namespace(dsl.getPrefix())
        .build();
  }
}
