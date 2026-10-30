/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source.legacy;

import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.sdk.api.runtime.operation.Result;
import com.zernova.sdk.api.runtime.source.PollContext;
import com.zernova.sdk.api.runtime.source.PollingSource;
import com.zernova.sdk.api.runtime.source.Source;
import com.zernova.sdk.api.runtime.source.SourceCallback;
import com.zernova.sdk.api.runtime.source.SourceCallbackContext;

import org.slf4j.Logger;

/**
 * Adapts a legacy {@link com.zernova.runtime.extension.api.runtime.source.Source} into a {@link Source}
 *
 * @param <T> the generic type of the output value
 * @param <A> the generic type of the message attributes
 * @since 4.4.0
 */
public class SdkSourceAdapter<T, A> extends Source<T, A> implements LegacySourceWrapper, Initialisable, Disposable {

  private static final Logger LOGGER = getLogger(SdkSourceAdapter.class);

  private final com.zernova.runtime.extension.api.runtime.source.Source<T, A> delegate;

  public static <T, A> Source<T, A> from(com.zernova.runtime.extension.api.runtime.source.Source<T, A> delegate) {
    if (delegate instanceof com.zernova.runtime.extension.api.runtime.source.PollingSource) {
      return new LegacyPollingSourceAdapter<>((com.zernova.runtime.extension.api.runtime.source.PollingSource) delegate);
    }

    return new SdkSourceAdapter<>(delegate);
  }

  private SdkSourceAdapter(com.zernova.runtime.extension.api.runtime.source.Source<T, A> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void onStart(SourceCallback<T, A> sourceCallback) throws ZernovaException {
    delegate.onStart(new LegacySourceCallbackAdapter(sourceCallback));
  }

  @Override
  public void onStop() {
    delegate.onStop();
  }

  @Override
  public com.zernova.runtime.extension.api.runtime.source.Source getDelegate() {
    return delegate;
  }

  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(delegate);
  }

  @Override
  public void dispose() {
    disposeIfNeeded(delegate, LOGGER);
  }

  private static class LegacyPollingSourceAdapter<T, A> extends PollingSource<T, A>
      implements LegacySourceWrapper, Initialisable, Disposable {

    private static final Logger LOGGER = getLogger(LegacyPollingSourceAdapter.class);

    private final com.zernova.runtime.extension.api.runtime.source.PollingSource<T, A> delegate;

    private LegacyPollingSourceAdapter(com.zernova.runtime.extension.api.runtime.source.PollingSource<T, A> delegate) {
      this.delegate = delegate;
    }

    @Override
    protected void doStart() throws ZernovaException {
      delegate.onStart(null);
    }

    @Override
    protected void doStop() {
      delegate.onStop();
    }

    @Override
    public void poll(PollContext<T, A> pollContext) {
      delegate.poll(new LegacyPollContextAdapter<>(pollContext));
    }

    @Override
    public void onRejectedItem(Result<T, A> result, SourceCallbackContext callbackContext) {
      delegate.onRejectedItem(LegacyResultAdapter.from(result), new LegacySourceCallbackContextAdapter(callbackContext));
    }

    @Override
    public com.zernova.runtime.extension.api.runtime.source.Source getDelegate() {
      return delegate;
    }

    @Override
    public void initialise() throws InitialisationException {
      initialiseIfNeeded(delegate);
    }

    @Override
    public void dispose() {
      disposeIfNeeded(delegate, LOGGER);
    }
  }
}
