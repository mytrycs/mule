/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.privileged.exception;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.message.Error;

import static java.util.Objects.requireNonNull;

/**
 * Wraps a provided exception, suppressing a {@link ZernovaException} that is part of it's cause tree, meaning that the Mule Runtime
 * will not take it into account for the error handling. The suppressed cause and all its nested {@link Exception#getCause()} will
 * not be taken into account during the {@link Error} resolution. <br/>
 * <br/>
 * Example without suppression: <br/>
 *
 * <pre>
 * throw new {@link com.zernova.runtime.api.exception.TypedException}(new {@link com.zernova.runtime.api.connection.ConnectionException}(), {@link com.zernova.runtime.api.message.ErrorType customErrorType})
 * </pre>
 *
 * will resolve to an {@link Error} with {@link Error#getErrorType()} returning ConnectionException's error type (discarding the
 * top level customErrorType). <br/>
 * <br/>
 * Same example with suppression: <br/>
 *
 * <pre>
 * throw new {@link com.zernova.runtime.api.exception.TypedException}(new {@link SuppressedZernovaException}(new {@link com.zernova.runtime.api.connection.ConnectionException}()), {@link com.zernova.runtime.api.message.ErrorType customErrorType})
 * </pre>
 *
 * will resolve to an {@link Error} with {@link Error#getErrorType()} returning customErrorType (discarding the underlying
 * ConnectionException's error type}).
 *
 * @since 1.2.3, 1.3
 */
public class SuppressedZernovaException extends ZernovaException {

  private static final long serialVersionUID = -2810315886149183162L;

  private final ZernovaException suppressedException;

  /**
   * Constructs a new {@link SuppressedZernovaException}
   *
   * @param throwable       Exception that will be wrapped.
   * @param causeToSuppress The cause that wants to be suppressed. Cannot be null.
   */
  protected SuppressedZernovaException(Throwable throwable, ZernovaException causeToSuppress) {
    super(requireNonNull(throwable, "Exception cannot be null"));
    this.suppressedException = requireNonNull(causeToSuppress, "Cannot suppress a null cause");
    addSuppressionToMuleExceptionInfo(causeToSuppress);
  }

  /**
   * Completes the {@link com.zernova.runtime.api.exception.ZernovaExceptionInfo} of a {@link SuppressedZernovaException} by adding the
   * {@link ZernovaException} that is being suppressed and traversing its causes to include any other suppression found and also the
   * additional information from all the {@link ZernovaException}s.
   *
   * @param causeToSuppress Exception that is being suppressed.
   */
  private void addSuppressionToMuleExceptionInfo(ZernovaException causeToSuppress) {
    this.getExceptionInfo().addSuppressedCause(causeToSuppress);
    this.addAllInfo(causeToSuppress.getAdditionalInfo());
    Throwable nestedCause = causeToSuppress;
    while (nestedCause.getCause() != null && nestedCause.getCause() != nestedCause) {
      nestedCause = nestedCause.getCause();
      if (nestedCause instanceof ZernovaException) {
        this.addAllInfo(((ZernovaException) nestedCause).getAdditionalInfo());
        if (nestedCause instanceof SuppressedZernovaException) {
          this.getExceptionInfo().addSuppressedCause(((SuppressedZernovaException) nestedCause).getSuppressedException());
        }
      }
    }
  }

  /**
   * Unwraps a {@link SuppressedZernovaException}.
   *
   * @return First cause that is not an instance of {@link SuppressedZernovaException}.
   */
  public Throwable unwrap() {
    // There cannot be consecutive suppressions, so returning the cause is enough
    return getCause();
  }

  /**
   * @return {@link ZernovaException} that has been suppressed by this {@link SuppressedZernovaException}.
   */
  public ZernovaException getSuppressedException() {
    return suppressedException;
  }

  /**
   * Wraps the provided exception, suppressing the exception itself or the first cause that is an instance of the provided class.
   * The search will stop if a {@link SuppressedZernovaException} is found, making no suppression.
   *
   * @param exception       Exception that will be wrapped, suppressing the exception itself or one of it's causes.
   * @param causeToSuppress Class of the {@link ZernovaException} that has to be suppressed.
   * @return {@link SuppressedZernovaException} or provided exception if no cause to suppress is found.
   */
  public static Throwable suppressIfPresent(Throwable exception, Class<? extends ZernovaException> causeToSuppress) {
    Throwable cause = exception;
    while (cause != null && !(cause instanceof SuppressedZernovaException)) {
      if (causeToSuppress.isInstance(cause)) {
        return new SuppressedZernovaException(exception, (ZernovaException) cause);
      }
      if (cause.getCause() != cause) {
        cause = cause.getCause();
      } else {
        break;
      }
    }
    return exception;
  }

  @Override
  public String getVerboseMessage() {
    // SuppressedException is meant to marks as suppressed another exception that is part of the cause tree.
    return "Suppressed: " + super.getSummaryMessage();
  }

  @Override
  public String getSummaryMessage() {
    // SuppressedException is meant to marks as suppressed another exception that is part of the cause tree.
    return "Suppressed: " + super.getSummaryMessage();
  }
}
