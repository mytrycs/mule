/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.exception;

import static com.zernova.runtime.api.message.Message.of;
import static com.zernova.runtime.core.api.event.EventContextFactory.create;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.alert.AlertingSupport;
import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.exception.ZernovaExceptionInfo;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.ast.internal.error.DefaultErrorTypeBuilder;
import com.zernova.runtime.ast.privileged.error.DefaultErrorType;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.construct.Flow;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.privileged.exception.AbstractDeclaredExceptionListener;
import com.zernova.runtime.core.privileged.exception.MessagingException;
import com.zernova.runtime.core.privileged.exception.TemplateOnErrorHandler;
import com.zernova.runtime.core.privileged.message.PrivilegedError;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.junit4.rule.VerboseExceptions;

import java.util.Collection;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import io.qameta.allure.Issue;

@RunWith(Parameterized.class)
public abstract class AbstractErrorHandlerTestCase extends AbstractZernovaContextTestCase {

  @Rule
  public VerboseExceptions verbose;

  protected AlertingSupport alertingSupport;

  public AbstractErrorHandlerTestCase(VerboseExceptions verbose) {
    this.verbose = verbose;
  }

  @Parameters(name = "{0}")
  public static Collection<Object> data() {
    return asList(new VerboseExceptions(true),
                  new VerboseExceptions(false));
  }

  protected MessagingException mockException = mock(MessagingException.class);

  protected Flow flow;

  protected EventContext context;

  protected CoreEvent muleEvent;

  protected abstract AbstractDeclaredExceptionListener getErrorHandler();

  @Before
  public void before() throws Exception {
    flow = getTestFlow(muleContext);

    context = create(flow, TEST_CONNECTOR_LOCATION);
    muleEvent = CoreEvent.builder(context).message(of("")).build();

    when(mockException.getExceptionInfo()).thenReturn(new ZernovaExceptionInfo());

    alertingSupport = mock(AlertingSupport.class);
  }

  @Test
  @Issue("MULE-18562")
  public void testUnsuppressedErrorMustBeAccepted() throws InitialisationException {
    TemplateOnErrorHandler onErrorHandler = (TemplateOnErrorHandler) getErrorHandler();
    CoreEvent event = getCoreEventWithSuppressedError(onErrorHandler.getMuleContext());
    onErrorHandler.setErrorType("ZERNOVA:UNSUPPRESSED");
    initialiseIfNeeded(onErrorHandler, onErrorHandler.getMuleContext());
    assertThat(onErrorHandler.accept(event), is(true));
  }

  @Test
  @Issue("MULE-18562")
  public void testSuppressedErrorMustBeAccepted() throws InitialisationException {
    TemplateOnErrorHandler onErrorHandler = (TemplateOnErrorHandler) getErrorHandler();
    CoreEvent event = getCoreEventWithSuppressedError(onErrorHandler.getMuleContext());
    onErrorHandler.setErrorType("ZERNOVA:SUPPRESSED");
    initialiseIfNeeded(onErrorHandler, onErrorHandler.getMuleContext());
    assertThat(onErrorHandler.accept(event), is(true));
  }

  private CoreEvent getCoreEventWithSuppressedError(ZernovaContext muleContext) {
    ErrorType anyErrorType = DefaultErrorTypeBuilder.builder().namespace("ZERNOVA").identifier("ANY").build();
    ErrorType suppressedErrorType =
        DefaultErrorTypeBuilder.builder().namespace("ZERNOVA").identifier("SUPPRESSED").parentErrorType(anyErrorType).build();
    ErrorType unsuppressedErrorType =
        DefaultErrorTypeBuilder.builder().namespace("ZERNOVA").identifier("UNSUPPRESSED").parentErrorType(anyErrorType).build();
    ErrorType sourceResponseErrorType =
        DefaultErrorTypeBuilder.builder().namespace("ZERNOVA").identifier("SOURCE_RESPONSE").parentErrorType(anyErrorType).build();
    Error errorWithSuppression = mock(PrivilegedError.class);
    Error suppressedError = mock(Error.class);
    CoreEvent event = mock(CoreEvent.class);
    when(((PrivilegedError) errorWithSuppression).getSuppressedErrors()).thenReturn(singletonList(suppressedError));
    when(errorWithSuppression.getErrorType()).thenReturn(unsuppressedErrorType);
    when(suppressedError.getErrorType()).thenReturn(suppressedErrorType);
    when(event.getError()).thenReturn(Optional.of(errorWithSuppression));

    final ErrorTypeRepository errorTypeRepository = mock(ErrorTypeRepository.class);
    ((ContributedErrorTypeRepository) muleContext.getErrorTypeRepository()).setDelegate(errorTypeRepository);

    when(errorTypeRepository
        .lookupErrorType(ComponentIdentifier.buildFromStringRepresentation("ZERNOVA:SUPPRESSED")))
            .thenReturn(Optional.of(suppressedErrorType));
    when(errorTypeRepository
        .lookupErrorType(ComponentIdentifier.buildFromStringRepresentation("ZERNOVA:UNSUPPRESSED")))
            .thenReturn(Optional.of(unsuppressedErrorType));
    when(errorTypeRepository
        .getSourceResponseErrorType()).thenReturn(sourceResponseErrorType);

    return event;
  }

  protected void addErrorType(ComponentIdentifier errorTypeIdentifier) {
    final ZernovaExceptionInfo exceptionInfo = mock(ZernovaExceptionInfo.class);
    when(exceptionInfo.getErrorType())
        .thenReturn(new DefaultErrorType(errorTypeIdentifier.getName(), errorTypeIdentifier.getNamespace(), null));
    when(mockException.getExceptionInfo()).thenReturn(exceptionInfo);
  }

}
