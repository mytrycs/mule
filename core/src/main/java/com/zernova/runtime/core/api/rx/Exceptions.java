/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.rx;

import static reactor.core.Exceptions.isBubbling;
import static reactor.core.Exceptions.isErrorCallbackNotImplemented;
import static reactor.core.Exceptions.propagate;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.exception.DefaultZernovaException;
import com.zernova.runtime.api.exception.ZernovaFatalException;
import com.zernova.runtime.core.api.util.func.CheckedBiConsumer;
import com.zernova.runtime.core.api.util.func.CheckedBiFunction;
import com.zernova.runtime.core.api.util.func.CheckedBiPredicate;
import com.zernova.runtime.core.api.util.func.CheckedConsumer;
import com.zernova.runtime.core.api.util.func.CheckedFunction;
import com.zernova.runtime.core.api.util.func.CheckedPredicate;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utilities for working with checked exceptions with project reactor.
 */
public class Exceptions {

  private static final String REACTIVE_EXCEPTION_CLASS_NAME = "reactor.core.Exceptions$ReactiveException";

  /**
   * Adapt a {@link CheckedConsumer} to a {@link Consumer} propagating any exceptions thrown by the {@link CheckedConsumer} using
   * {@link reactor.core.Exceptions#propagate(Throwable)}. Useful when using existing methods which throw checked exceptions to
   * avoid try/catch blocks and improve readability.
   *
   * {@code flux.subscribe(Exceptions.checked(v -> checkedMethod(v)}
   *
   * @param checkedConsumer the consumer that throws a checked exception
   * @param <T>             the type of the input to the operation
   * @return consumer that adapts {@link CheckedConsumer} and {@link reactor.core.Exceptions#propagate(Throwable)}'s checked
   *         exceptions
   * @see reactor.core.Exceptions#propagate(Throwable)
   */
  public static <T> Consumer<T> checkedConsumer(CheckedConsumer<T> checkedConsumer) {
    return t -> checkedConsumer.accept(t);
  }

  /**
   * Adapt a {@link CheckedBiConsumer} to a {@link BiConsumer} propagating any exceptions thrown by the {@link CheckedBiConsumer}
   * using {@link reactor.core.Exceptions#propagate(Throwable)}. Useful when using existing methods which throw checked exceptions
   * to avoid try/catch blocks and improve readability.
   *
   * @param checkedBiConsumer the biconsumer that throws a chedked exception
   * @param <T>               the type of the first argument to the operation
   * @param <U>               the type of the second argument to the operation
   * @return biconsumer that adapts {@link CheckedBiConsumer} and {@link reactor.core.Exceptions#propagate(Throwable)}'s checked
   *         exceptions
   * @see reactor.core.Exceptions#propagate(Throwable)
   */
  public static <T, U> BiConsumer<T, U> checkedConsumer(CheckedBiConsumer<T, U> checkedBiConsumer) {
    return (t, u) -> checkedBiConsumer.accept(t, u);
  }

  /**
   * Adapt a {@link CheckedFunction} to a {@link Function} propagating any exceptions thrown by the {@link CheckedFunction} using
   * {@link reactor.core.Exceptions#propagate(Throwable)}. Useful when using existing methods which throw checked exceptions to
   * avoid try/catch blocks and improve readability.
   *
   * {@code flux.map(Exceptions.checked(v -> checkedMethod(v)).subscribe(Subscribers.unbounded())}
   *
   * @param checkedFunction the function that throws a checked exception
   * @param <T>             the type of the input to the function
   * @param <R>             the type of the result of the function
   * @return function that adapts {@link CheckedFunction} and {@link reactor.core.Exceptions#propagate(Throwable)}'s checked
   *         exceptions
   * @see reactor.core.Exceptions#propagate(Throwable)
   */
  public static <T, R> Function<T, R> checkedFunction(CheckedFunction<T, R> checkedFunction) {
    return t -> checkedFunction.apply(t);
  }

  /**
   * Adapt a {@link CheckedBiFunction} to a {@link BiFunction} propagating any exceptions thrown by the {@link CheckedBiFunction}
   * using {@link reactor.core.Exceptions#propagate(Throwable)}. Useful when using existing methods which throw checked exceptions
   * to avoid try/catch blocks and improve readability.
   *
   * @param checkedBiFunction the bifunction that throws a checked exception
   * @param <T>               the type of the first argument to the function
   * @param <U>               the type of the second argument to the function
   * @param <R>               the type of the result of the function
   * @return bifunction that adapts {@link CheckedBiFunction} and {@link reactor.core.Exceptions#propagate(Throwable)}'s checked
   *         exceptions
   * @see reactor.core.Exceptions#propagate(Throwable)
   */
  public static <T, U, R> BiFunction<T, U, R> checkedFunction(CheckedBiFunction<T, U, R> checkedBiFunction) {
    return (t, u) -> checkedBiFunction.apply(t, u);
  }

  /**
   * Adapt a {@link CheckedBiConsumer} to a {@link BiConsumer} propagating any exceptions thrown by the {@code checkedBiConsumer}
   * using {@link reactor.core.Exceptions#propagate(Throwable)}. Useful when using existing methods which throw checked exceptions
   * to avoid try/catch blocks and improve readability.
   *
   * @param checkedBiConsumer the biconsumer that throws a checked exception
   * @param <T>               the type of the first argument to the function
   * @param <U>               the type of the second argument to the function
   * @return a biconsumer that adapts {@code checkedBiConsumer} and {@link reactor.core.Exceptions#propagate(Throwable)}'s checked
   *         exceptions
   * @see reactor.core.Exceptions#propagate(Throwable)
   * @since 4.3.0
   */
  public static <T, U> BiConsumer<T, U> checkedBiConsumer(CheckedBiConsumer<T, U> checkedBiConsumer) {
    return checkedBiConsumer::accept;
  }

  /**
   * Adapt a {@link CheckedPredicate} to a {@link Predicate} propagating any exceptions thrown by the {@link CheckedPredicate}
   * using {@link reactor.core.Exceptions#propagate(Throwable)}. Useful when using existing methods which throw checked exceptions
   * to avoid try/catch blocks and improve readability.
   *
   * {@code flux.filter(Exceptions.checked(v -> checkedMethod(v)).subscribe(Subscribers.unbounded())}
   *
   * @param checkedPredicate the predicate that throws a checked exception
   * @param <T>              the type of the input to the predicate
   * @return predicate that adapts {@link CheckedPredicate} and {@link reactor.core.Exceptions#propagate(Throwable)}'s checked
   *         exceptions
   * @see reactor.core.Exceptions#propagate(Throwable)
   */
  public static <T> Predicate<T> checkedPredicate(CheckedPredicate<T> checkedPredicate) {
    return t -> checkedPredicate.test(t);
  }

  /**
   * Adapt a {@link CheckedBiPredicate} to a {@link BiPredicate} propagating any exceptions thrown by the
   * {@link CheckedBiPredicate} using {@link reactor.core.Exceptions#propagate(Throwable)}. Useful when using existing methods
   * which throw checked exceptions to avoid try/catch blocks and improve readability.
   *
   * @param checkedBiPredicate the bipredicate that throws a checked exception
   * @param <T>                the type of the first argument to the predicate
   * @param <U>                the type of the second argument the predicate
   * @return bipredicate that adapts {@link CheckedBiPredicate} and {@link reactor.core.Exceptions#propagate(Throwable)}'s checked
   *         exceptions
   * @see reactor.core.Exceptions#propagate(Throwable)
   */
  public static <T, U> BiPredicate<T, U> checkedPredicate(CheckedBiPredicate<T, U> checkedBiPredicate) {
    return (t, u) -> checkedBiPredicate.test(t, u);
  }

  /**
   * Unwrap reactive exception and rethrow instances {@link ZernovaException} or {@link RuntimeException}. Other exception types are
   * wrapped in a instance of {@link DefaultZernovaException}.
   *
   * @param throwable
   * @return a {@link ZernovaException}
   * @throws ZernovaException
   */
  public static ZernovaException rxExceptionToMuleException(Throwable throwable)
      throws ZernovaException {
    throwable = unwrap(throwable);
    if (throwable instanceof ZernovaException) {
      return (ZernovaException) throwable;
    } else if (throwable instanceof RuntimeException) {
      throw (RuntimeException) throwable;
    } else {
      throw new DefaultZernovaException(throwable);
    }
  }

  /**
   * Unwrap a particular {@code Throwable}.
   *
   * @param throwable the exception to wrap
   * @return the unwrapped exception
   */
  public static Throwable unwrap(Throwable throwable) {
    while (throwable.getClass().getName().equals(REACTIVE_EXCEPTION_CLASS_NAME)) {
      throwable = throwable.getCause();
    }
    return throwable;
  }

  /**
   * Propagate an exception through streams even if it's considered fatal by Reactor.
   *
   * @param t throwable.
   * @return exception wrapped by Reactor, after possibly wrapping with {@link ZernovaFatalException}.
   */
  public static RuntimeException propagateWrappingFatal(Throwable t) {
    return propagate(wrapFatal(t));
  }

  /**
   * Wrap an exception with {@link ZernovaFatalException} so that Reactor can handle it safely instead of always bubbling it up.
   *
   * @param t throwable.
   * @return possibly wrapped exception.
   */
  public static Throwable wrapFatal(Throwable t) {
    if (t instanceof LinkageError) {
      return new ZernovaFatalException(t);
    } else if (t instanceof VirtualMachineError) {
      return new ZernovaFatalException(t);
    } else if (isBubbling(t)) {
      return new ZernovaRuntimeException(unwrap(t));
    } else if (isErrorCallbackNotImplemented(t)) {
      // This type of fatal Reactor exceptions was added later see MULE-19593 for more details
      return new ZernovaRuntimeException(unwrap(t));
    } else {
      return t;
    }
  }

  private Exceptions() {}
}
