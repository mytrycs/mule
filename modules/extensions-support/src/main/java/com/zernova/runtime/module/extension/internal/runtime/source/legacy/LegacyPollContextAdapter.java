/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.source.legacy;

import static com.zernova.runtime.module.extension.internal.runtime.source.legacy.LegacyPollItemStatusUtils.from;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.core.internal.util.message.SdkResultAdapter;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.PollContext;
import com.zernova.runtime.extension.api.runtime.source.SourceCallbackContext;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Adapts a {@link com.zernova.sdk.api.runtime.source.PollContext} into a legacy {@link PollContext}
 *
 * @param <T> the generic type of the output value
 * @param <A> the generic type of the message attributes
 * @since 4.4.0
 */
public class LegacyPollContextAdapter<T, A> implements PollContext<T, A> {

  private final com.zernova.sdk.api.runtime.source.PollContext delegate;

  public LegacyPollContextAdapter(com.zernova.sdk.api.runtime.source.PollContext<T, A> delegate) {
    this.delegate = delegate;
  }

  @Override
  public PollItemStatus accept(Consumer<PollItem<T, A>> consumer) {
    return from(delegate.accept(new PollItemConsumerAdapter(consumer)));
  }

  @Override
  public Optional<Serializable> getWatermark() {
    return delegate.getWatermark();
  }

  @Override
  public boolean isSourceStopping() {
    return delegate.isSourceStopping();
  }

  @Override
  public void setWatermarkComparator(Comparator<? extends Serializable> comparator) {
    delegate.setWatermarkComparator(comparator);
  }

  @Override
  public void onConnectionException(ConnectionException e) {
    delegate.onConnectionException(e);
  }

  private static class PollItemConsumerAdapter<T, A>
      implements Consumer<com.zernova.sdk.api.runtime.source.PollContext.PollItem<T, A>> {

    Consumer<PollItem<T, A>> delegate;

    PollItemConsumerAdapter(Consumer<PollItem<T, A>> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void accept(com.zernova.sdk.api.runtime.source.PollContext.PollItem pollItem) {
      delegate.accept(new SdkToLegacyPollItemAdapter(pollItem));
    }

    private static class SdkToLegacyPollItemAdapter implements PollContext.PollItem {

      com.zernova.sdk.api.runtime.source.PollContext.PollItem delegate;

      public SdkToLegacyPollItemAdapter(com.zernova.sdk.api.runtime.source.PollContext.PollItem delegate) {
        this.delegate = delegate;
      }

      @Override
      public SourceCallbackContext getSourceCallbackContext() {
        return new LegacySourceCallbackContextAdapter(delegate.getSourceCallbackContext());
      }

      @Override
      public PollItem setResult(Result result) {
        delegate.setResult(SdkResultAdapter.from(result));
        return this;
      }

      @Override
      public PollItem setWatermark(Serializable watermark) {
        delegate.setWatermark(watermark);
        return this;
      }

      @Override
      public PollItem setId(String id) {
        delegate.setId(id);
        return this;
      }
    }
  }
}
