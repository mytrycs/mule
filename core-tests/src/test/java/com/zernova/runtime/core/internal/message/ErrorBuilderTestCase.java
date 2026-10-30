/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.message;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.zernova.runtime.api.exception.ExceptionHelper.suppressIfPresent;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.message.Message.of;
import static com.zernova.runtime.core.api.util.ExceptionUtils.getMessagingExceptionCause;
import static com.zernova.runtime.core.internal.message.ErrorBuilder.builder;
import static com.zernova.tck.junit4.matcher.IsEqualIgnoringLineBreaks.equalToIgnoringLineBreaks;
import static com.zernova.test.allure.AllureConstants.ErrorHandlingFeature.ERROR_HANDLING;
import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.exception.ComposedErrorException;
import com.zernova.runtime.api.exception.DefaultZernovaException;
import com.zernova.runtime.api.exception.ErrorMessageAwareException;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.ast.internal.error.DefaultErrorTypeBuilder;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.privileged.exception.MessagingException;
import com.zernova.runtime.core.privileged.message.PrivilegedError;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SmallTest
@Feature(ERROR_HANDLING)
public class ErrorBuilderTestCase extends AbstractZernovaTestCase {

  private static final String EXCEPTION_MESSAGE = "message";
  private static final String SUPPRESSED_EXCEPTION_MESSAGE = "suppressed message";
  private final ErrorType mockErrorType = mock(ErrorType.class);
  private final ErrorType anyError = DefaultErrorTypeBuilder.builder().namespace("MULE").identifier("ANY").build();
  private final ErrorType testError =
      DefaultErrorTypeBuilder.builder().namespace("MULE").identifier("TEST").parentErrorType(anyError).build();

  @Rule
  public ExpectedException expectedException = none();

  @Test
  public void exceptionCannotBeNull() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("exception cannot be null");
    builder().build();
  }

  @Test
  public void errorTypeCanNotBeNull() {
    RuntimeException exception = new RuntimeException(EXCEPTION_MESSAGE);
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("errorType cannot be null");
    builder(exception).build();
  }

  @Test
  @Issue("MULE-19408")
  public void errorCanBeSerializedByAnObjectOutputStreamEvenWhenFailingComponentIsNotSerializable() throws IOException {
    Component mockNonSerializableFailingComponent = mock(Component.class);
    when(mockNonSerializableFailingComponent.getRepresentation()).thenReturn("mock/representation");

    RuntimeException exception = new RuntimeException(EXCEPTION_MESSAGE);
    Error error = builder(exception).errorType(mockErrorType).failingComponent(mockNonSerializableFailingComponent).build();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(error);
    }

    assertThat(baos.toString(), containsString("mock/representation"));
  }

  @Test
  @Issue("MULE-19408")
  @Description("An instance of the deprecated ErrorImplementation could be already serialized. We should be able to " +
      "deserialize it")
  public void alreadySerializedErrorImplementationCanBeDeserialized() throws IOException, ClassNotFoundException {
    ErrorBuilder.ErrorImplementation deprecated = getDeprecatedImpl();

    // Serialize a deprecated object.
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(deprecated);
    }

    // Deserialize the object and check it's now a non-deprecated object.
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    try (ObjectInputStream ois = new ObjectInputStream(bais)) {
      Object o = ois.readObject();
      assertThat(o, instanceOf(ErrorBuilder.DeserializableErrorImplementation.class));
    }
  }

  private ErrorBuilder.ErrorImplementation getDeprecatedImpl() {
    Component mockSerializableFailingComponent = mock(SerializableComponent.class);
    RuntimeException exception = new RuntimeException(EXCEPTION_MESSAGE);
    Error anError = builder(exception).errorType(mockErrorType).build();
    return new ErrorBuilder.ErrorImplementation(anError.getCause(), anError.getDescription(), anError.getDetailedDescription(),
                                                mockSerializableFailingComponent, anError.getErrorType(),
                                                anError.getErrorMessage(), anError.getChildErrors(), emptyList());
  }

  @Test
  public void buildErrorFromException() {
    RuntimeException exception = new RuntimeException(EXCEPTION_MESSAGE);
    Error error = builder(exception).errorType(mockErrorType).build();
    assertThat(error.getCause(), is(exception));
    assertThat(error.getDescription(), is(EXCEPTION_MESSAGE));
    assertThat(error.getDetailedDescription(), is(EXCEPTION_MESSAGE));
    assertThat(error.getErrorType(), is(mockErrorType));
    assertThat(((PrivilegedError) error).getSuppressedErrors(), is(empty()));
    assertThat(error.getChildErrors(), is(empty()));
  }

  @Test
  public void buildErrorFromMuleException() {
    ZernovaException exception = new DefaultZernovaException(new RuntimeException(EXCEPTION_MESSAGE));
    Error error = builder(exception).errorType(mockErrorType).build();
    assertThat(error.getCause(), is(exception));
    assertThat(error.getDescription(), containsString(EXCEPTION_MESSAGE));
    assertThat(error.getDetailedDescription(), containsString(EXCEPTION_MESSAGE));
    assertThat(error.getErrorType(), is(mockErrorType));
    assertThat(((PrivilegedError) error).getSuppressedErrors(), is(empty()));
    assertThat(error.getChildErrors(), is(empty()));
  }

  @Test
  @Issue("MULE-18562")
  public void buildErrorFromMuleExceptionWithSuppression() {
    MessagingException messagingException = getMessagingException();
    Throwable exception =
        suppressIfPresent(new DefaultZernovaException(EXCEPTION_MESSAGE, messagingException), MessagingException.class);
    buildErrorAndAssertSuppression(exception, messagingException);
  }

  @Test
  @Issue("MULE-18562")
  public void buildErrorFromMuleExceptionWithNestedSuppression() {
    MessagingException messagingException = getMessagingException();
    Throwable exception =
        new DefaultZernovaException(EXCEPTION_MESSAGE, suppressIfPresent(messagingException, MessagingException.class));
    buildErrorAndAssertSuppression(exception, messagingException);
  }

  @Test
  @Issue("MULE-18562")
  public void buildErrorFromComposedErrorMessageAwareExceptionWithSuppression() {
    ComposedErrorMessageAwareException composedMessageAwareException =
        new ComposedErrorMessageAwareException(createStaticMessage(EXCEPTION_MESSAGE), testError, getMessagingException());
    // Adding a suppression in order to wrap the ComposedErrorMessageAwareException
    Error error = builder(suppressIfPresent(composedMessageAwareException, MessagingException.class))
        .errorType(testError)
        .build();
    assertThat(error.getDetailedDescription(), equalTo(EXCEPTION_MESSAGE));
    // Backwards compatibility demands that the description has to be set to the suppressed exception message
    assertThat(error.getDescription(), equalTo(SUPPRESSED_EXCEPTION_MESSAGE));
    assertThat(((PrivilegedError) error).getSuppressedErrors(), hasSize(1));
    Throwable suppressedErrorCause = ((PrivilegedError) error).getSuppressedErrors().get(0).getCause();
    assertThat(suppressedErrorCause, instanceOf(TestZernovaException.class));
    // Backwards compatibility demands that the error cause has to be the suppressed exception
    assertThat(error.getCause(), equalTo(suppressedErrorCause));
    // ComposedErrorMessageAwareException must be treated by the error builder
    assertThat(error.getErrorMessage().getPayload().getValue().toString(), equalTo(TEST_PAYLOAD));
    assertThat(error.getChildErrors(), contains(composedMessageAwareException.getErrors().toArray()));
  }

  @Test
  public void buildError() {
    String detailedDescription = "detailed description";
    String description = "description";
    ErrorType errorType = mockErrorType;
    Message errorMessage = of(null);
    IllegalArgumentException exception = new IllegalArgumentException("some message");
    Error error = builder()
        .errorType(errorType)
        .description(description)
        .detailedDescription(detailedDescription)
        .exception(exception)
        .errorMessage(errorMessage)
        .build();
    assertThat(error.getDescription(), is(description));
    assertThat(error.getDetailedDescription(), is(detailedDescription));
    assertThat(error.getCause(), is(exception));
    assertThat(error.getErrorType(), is(errorType));
    assertThat(error.getErrorMessage(), is(errorMessage));
    assertThat(error.getChildErrors(), is(empty()));
  }

  @Test
  public void buildErrorFromCustomException() {
    Error error = builder(new ComposedErrorMessageAwareException(createStaticMessage(EXCEPTION_MESSAGE)))
        .errorType(mockErrorType)
        .build();

    assertThat(error.getDescription(), is(EXCEPTION_MESSAGE));
    assertThat(error.getDetailedDescription(), is(EXCEPTION_MESSAGE));
    assertThat(error.getCause(), is(instanceOf(ComposedErrorMessageAwareException.class)));
    assertThat(error.getErrorType(), is(mockErrorType));
    assertThat(error.getErrorMessage().getPayload().getValue(), is(TEST_PAYLOAD));
    assertThat(((PrivilegedError) error).getSuppressedErrors(), is(empty()));
    List<Error> childErrors = error.getChildErrors();
    assertThat(childErrors, hasSize(2));
    assertThat(childErrors.get(0).getCause(), is(instanceOf(RuntimeException.class)));
    assertThat(childErrors.get(1).getCause(), is(instanceOf(IOException.class)));
  }

  @Test
  public void givesStringRepresentation() {
    Error error = getComposedErrorMessageAwareWithSuppressionException();
    assertThat(error.toString(),
               is(equalToIgnoringLineBreaks("\norg.mule.runtime.core.internal.message.ErrorBuilder$DeserializableErrorImplementation\n"
                   + "{\n"
                   + "  description=suppressed message\n"
                   + "  detailedDescription=message\n"
                   + "  errorType=MULE:TEST\n"
                   + "  cause=com.zernova.runtime.core.internal.message.ErrorBuilderTestCase$TestZernovaException\n"
                   + "  errorMessage=\n"
                   + "com.zernova.runtime.core.internal.message.DefaultMessageBuilder$MessageImplementation\n"
                   + "{\n"
                   + "  payload=test\n"
                   + "  mediaType=*/*\n"
                   + "  attributes=<not set>\n"
                   + "  attributesMediaType=*/*\n"
                   + "}\n"
                   + "  suppressedErrors=[\n"
                   + "com.zernova.runtime.core.internal.message.ErrorBuilder$DeserializableErrorImplementation\n"
                   + "{\n"
                   + "  description=suppressed message\n"
                   + "  detailedDescription=suppressed message\n"
                   + "  errorType=MULE:TEST\n"
                   + "  cause=com.zernova.runtime.core.internal.message.ErrorBuilderTestCase$TestZernovaException\n"
                   + "  errorMessage=-\n"
                   + "  suppressedErrors=[]\n"
                   + "  childErrors=[]\n"
                   + "}]\n"
                   + "  childErrors=[\n"
                   + "com.zernova.runtime.core.internal.message.ErrorBuilder$DeserializableErrorImplementation\n"
                   + "{\n"
                   + "  description=unknown description\n"
                   + "  detailedDescription=unknown description\n"
                   + "  errorType=MULE:TEST\n"
                   + "  cause=java.lang.RuntimeException\n"
                   + "  errorMessage=-\n"
                   + "  suppressedErrors=[]\n"
                   + "  childErrors=[]\n"
                   + "}, \n"
                   + "com.zernova.runtime.core.internal.message.ErrorBuilder$DeserializableErrorImplementation\n"
                   + "{\n"
                   + "  description=unknown description\n"
                   + "  detailedDescription=unknown description\n"
                   + "  errorType=MULE:TEST\n"
                   + "  cause=java.io.IOException\n"
                   + "  errorMessage=-\n"
                   + "  suppressedErrors=[]\n"
                   + "  childErrors=[]\n"
                   + "}]\n"
                   + "}")));
  }

  private Error getComposedErrorMessageAwareWithSuppressionException() {
    Throwable composedMessageAwareException =
        new ComposedErrorMessageAwareException(createStaticMessage(EXCEPTION_MESSAGE), testError, getMessagingException());
    composedMessageAwareException = suppressIfPresent(composedMessageAwareException, MessagingException.class);
    return builder(composedMessageAwareException)
        .errorType(testError)
        .build();
  }

  private void buildErrorAndAssertSuppression(Throwable exception, MessagingException suppressedCause) {
    Error error = builder(exception).errorType(mockErrorType).build();
    assertThat(error.getDescription(), containsString(suppressedCause.getMessage()));
    assertThat(error.getDetailedDescription(), containsString(EXCEPTION_MESSAGE));
    assertThat(error.getErrorType(), is(mockErrorType));
    assertThat(error.getCause(), is(getMessagingExceptionCause(suppressedCause)));
    assertThat(((PrivilegedError) error).getSuppressedErrors(), contains(suppressedCause.getEvent().getError().get()));
    assertThat(error.getChildErrors(), is(empty()));
  }

  private MessagingException getMessagingException() {
    CoreEvent suppressedErrorEvent = mock(CoreEvent.class);
    ZernovaException messagingExceptionCause = new TestZernovaException(createStaticMessage(SUPPRESSED_EXCEPTION_MESSAGE));
    Error suppressedError = builder(messagingExceptionCause).errorType(testError).build();
    MessagingException messagingException =
        new MessagingException(suppressedErrorEvent, messagingExceptionCause);
    when(suppressedErrorEvent.getError()).thenReturn(Optional.of(suppressedError));
    return messagingException;
  }

  private class TestZernovaException extends ZernovaException {

    public TestZernovaException(I18nMessage message) {
      super(message);
    }
  }

  private class ComposedErrorMessageAwareException extends ZernovaException implements ErrorMessageAwareException,
      ComposedErrorException {

    private static final long serialVersionUID = -5454799054976232938L;
    private final List<Error> errors;

    public ComposedErrorMessageAwareException(I18nMessage message) {
      this(message, mockErrorType);
    }

    public ComposedErrorMessageAwareException(I18nMessage message, ErrorType errorType) {
      super(message);
      errors = populateErrors(errorType);
    }

    public ComposedErrorMessageAwareException(I18nMessage message, ErrorType errorType, Throwable cause) {
      super(message, cause);
      errors = populateErrors(errorType);
    }

    private List<Error> populateErrors(ErrorType errorType) {
      return asList(builder(new RuntimeException()).errorType(errorType).build(),
                    builder(new IOException()).errorType(errorType).build());
    }

    @Override
    public List<Error> getErrors() {
      return errors;
    }

    @Override
    public Message getErrorMessage() {
      return of(TEST_PAYLOAD);
    }
  }

  private interface SerializableComponent extends Serializable, Component {
  }

}
