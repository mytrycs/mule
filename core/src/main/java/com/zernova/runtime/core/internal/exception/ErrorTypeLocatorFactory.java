/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.exception;

import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.ERROR_AND_ROLLBACK_TX_WHEN_TIMEOUT;
import static com.zernova.runtime.ast.api.error.ErrorTypeBuilder.builder;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.ANY;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.CLIENT_SECURITY;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.COMPOSITE_ROUTING;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.CONNECTIVITY;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.DUPLICATE_MESSAGE;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.EXPRESSION;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.NOT_PERMITTED;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.REDELIVERY_EXHAUSTED;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.RETRY_EXHAUSTED;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.ROUTING;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.SECURITY;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.SERVER_SECURITY;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.STREAM_MAXIMUM_SIZE_EXCEEDED;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.TRANSACTION;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.TRANSFORMATION;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.UNKNOWN;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Handleable.VALIDATION;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Unhandleable.FATAL;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Unhandleable.FLOW_BACK_PRESSURE;
import static com.zernova.runtime.core.api.error.Errors.ComponentIdentifiers.Unhandleable.OVERLOAD;
import static java.util.Optional.empty;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.exception.ZernovaFatalException;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.security.ClientSecurityException;
import com.zernova.runtime.api.security.NotPermittedException;
import com.zernova.runtime.api.security.SecurityException;
import com.zernova.runtime.api.security.ServerSecurityException;
import com.zernova.runtime.api.streaming.exception.StreamingBufferSizeExceededException;
import com.zernova.runtime.api.tx.TransactionException;
import com.zernova.runtime.core.api.exception.ExceptionMapper;
import com.zernova.runtime.core.api.expression.ExpressionRuntimeException;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyExhaustedException;
import com.zernova.runtime.core.api.transformer.MessageTransformerException;
import com.zernova.runtime.core.api.transformer.TransformerException;
import com.zernova.runtime.core.internal.construct.FlowBackPressureException;
import com.zernova.runtime.core.internal.event.EventContextDeepNestingException;
import com.zernova.runtime.core.privileged.routing.CompositeRoutingException;
import com.zernova.runtime.core.internal.routing.result.RoutingException;
import com.zernova.runtime.core.internal.routing.split.DuplicateMessageException;
import com.zernova.runtime.core.privileged.exception.ErrorTypeLocator;
import com.zernova.runtime.core.privileged.exception.MessageRedeliveredException;

import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;

/**
 * Factory for {@link ErrorTypeLocator}.
 *
 * @since 4.0
 */
public class ErrorTypeLocatorFactory {

  public static ErrorTypeLocator createDefaultErrorTypeLocator(ErrorTypeRepository errorTypeRepository) {
    return createDefaultErrorTypeLocator(errorTypeRepository, empty());
  }

  /**
   * Creates the default {@link ErrorTypeLocator} to use in mule.
   *
   * @param errorTypeRepository error type repository. Commonly created using {@link ErrorTypeRepositoryFactory}.
   * @return a new {@link ErrorTypeLocator}.
   */
  public static ErrorTypeLocator createDefaultErrorTypeLocator(ErrorTypeRepository errorTypeRepository,
                                                               Optional<FeatureFlaggingService> featureFlaggingService) {
    ErrorType unknown = errorTypeRepository.getErrorType(UNKNOWN).get();
    return ErrorTypeLocator.builder(errorTypeRepository)
        .defaultExceptionMapper(ExceptionMapper.builder()
            .addExceptionMapping(MessageTransformerException.class, errorTypeRepository.lookupErrorType(TRANSFORMATION).get())
            .addExceptionMapping(TransformerException.class, errorTypeRepository.lookupErrorType(TRANSFORMATION).get())
            .addExceptionMapping(ExpressionRuntimeException.class, errorTypeRepository.lookupErrorType(EXPRESSION).get())
            .addExceptionMapping(RoutingException.class, errorTypeRepository.lookupErrorType(ROUTING).get())
            .addExceptionMapping(CompositeRoutingException.class, errorTypeRepository.getErrorType(COMPOSITE_ROUTING).get())
            .addExceptionMapping(ConnectionException.class, errorTypeRepository.lookupErrorType(CONNECTIVITY).get())
            .addExceptionMapping(ValidationException.class, errorTypeRepository.lookupErrorType(VALIDATION).get())
            .addExceptionMapping(DuplicateMessageException.class, errorTypeRepository.lookupErrorType(DUPLICATE_MESSAGE).get())
            .addExceptionMapping(RetryPolicyExhaustedException.class, errorTypeRepository.lookupErrorType(RETRY_EXHAUSTED).get())
            .addExceptionMapping(SecurityException.class, errorTypeRepository.lookupErrorType(SECURITY).get())
            .addExceptionMapping(ClientSecurityException.class, errorTypeRepository.lookupErrorType(CLIENT_SECURITY).get())
            .addExceptionMapping(ServerSecurityException.class, errorTypeRepository.lookupErrorType(SERVER_SECURITY).get())
            .addExceptionMapping(NotPermittedException.class, errorTypeRepository.lookupErrorType(NOT_PERMITTED).get())
            .addExceptionMapping(RejectedExecutionException.class, errorTypeRepository.getErrorType(OVERLOAD).get())
            .addExceptionMapping(FlowBackPressureException.class, errorTypeRepository.getErrorType(FLOW_BACK_PRESSURE).get())
            .addExceptionMapping(MessageRedeliveredException.class,
                                 errorTypeRepository.lookupErrorType(REDELIVERY_EXHAUSTED).get())
            .addExceptionMapping(Exception.class, unknown)
            .addExceptionMapping(EventContextDeepNestingException.class, errorTypeRepository.getCriticalErrorType())
            .addExceptionMapping(Error.class, errorTypeRepository.getCriticalErrorType())
            .addExceptionMapping(StreamingBufferSizeExceededException.class,
                                 errorTypeRepository.lookupErrorType(STREAM_MAXIMUM_SIZE_EXCEEDED).get())
            .addExceptionMapping(ZernovaFatalException.class, errorTypeRepository.getErrorType(FATAL).get())
            .addExceptionMapping(TransactionException.class,
                                 useTransactionErrorIfEnabled(errorTypeRepository.lookupErrorType(TRANSACTION)
                                     .orElseGet(ErrorTypeLocatorFactory::createTransactionError), unknown,
                                                              featureFlaggingService))
            .build())
        .defaultError(unknown)
        .build();
  }

  private static ErrorType createTransactionError() {
    ErrorType anyError = builder().namespace(ANY.getNamespace()).identifier(ANY.getName()).build();
    return builder().namespace(TRANSACTION.getNamespace()).identifier(TRANSACTION.getName()).parentErrorType(anyError).build();
  }

  private static ErrorType useTransactionErrorIfEnabled(ErrorType txError, ErrorType unknown,
                                                        Optional<FeatureFlaggingService> featureFlaggingService) {
    return featureFlaggingService.map(service -> service.isEnabled(ERROR_AND_ROLLBACK_TX_WHEN_TIMEOUT) ? txError : unknown)
        .orElse(txError);
  }
}
