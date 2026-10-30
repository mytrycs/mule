/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.streaming;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.MULE_ENABLE_STREAMING_STATISTICS;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.util.ClassUtils.instantiateClass;
import static com.zernova.runtime.core.api.util.IOUtils.closeQuietly;
import static com.zernova.runtime.core.internal.event.EventUtils.getRoot;
import static com.zernova.runtime.core.internal.util.CompositeClassLoader.from;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.lang.System.getProperty;

import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.api.annotation.NoExtend;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.streaming.Cursor;
import com.zernova.runtime.api.streaming.CursorProvider;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.streaming.bytes.ByteBufferManager;
import com.zernova.runtime.core.api.streaming.bytes.ByteBufferManagerFactory;
import com.zernova.runtime.core.api.streaming.bytes.ByteStreamingManager;
import com.zernova.runtime.core.api.streaming.bytes.CursorStreamProviderFactory;
import com.zernova.runtime.core.api.streaming.object.CursorIteratorProviderFactory;
import com.zernova.runtime.core.api.streaming.object.ObjectStreamingManager;
import com.zernova.runtime.core.internal.event.DefaultEventContext;
import com.zernova.runtime.core.internal.streaming.AtomicStreamingStatistics;
import com.zernova.runtime.core.internal.streaming.CursorManager;
import com.zernova.runtime.core.internal.streaming.MutableStreamingStatistics;
import com.zernova.runtime.core.internal.streaming.NullStreamingStatistics;
import com.zernova.runtime.core.internal.streaming.StreamingGhostBuster;
import com.zernova.runtime.core.internal.streaming.bytes.DefaultByteStreamingManager;
import com.zernova.runtime.core.internal.streaming.bytes.factory.PoolingByteBufferManagerFactory;
import com.zernova.runtime.core.internal.streaming.object.DefaultObjectStreamingManager;
import com.zernova.runtime.core.internal.streaming.object.factory.NullCursorIteratorProviderFactory;
import com.zernova.runtime.core.internal.util.CompositeClassLoader;
import com.zernova.runtime.core.privileged.event.BaseEventContext;

import java.io.Closeable;
import java.io.InputStream;

import org.slf4j.Logger;

import jakarta.inject.Inject;

@NoExtend
public class DefaultStreamingManager implements StreamingManager, Initialisable, Disposable {

  private static final Logger LOGGER = getLogger(DefaultStreamingManager.class);

  private static final String BUFFER_MANAGER_FACTORY_CLASS = getProperty(ByteBufferManagerFactory.class.getName(),
                                                                         PoolingByteBufferManagerFactory.class.getName());

  private ByteBufferManager bufferManager;
  private ByteStreamingManager byteStreamingManager;
  private ObjectStreamingManager objectStreamingManager;
  private CursorManager cursorManager;
  private MutableStreamingStatistics statistics;
  private boolean initialised = false;

  @Inject
  private ZernovaContext muleContext;

  @Inject
  private FeatureFlaggingService ffService;

  @Inject
  private StreamingGhostBuster ghostBuster;

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialise() throws InitialisationException {
    if (!initialised) {
      statistics = createStatistics();

      cursorManager = new CursorManager(statistics, ghostBuster);
      bufferManager = createByteBufferManager();
      byteStreamingManager = createByteStreamingManager();
      objectStreamingManager = createObjectStreamingManager();

      initialiseIfNeeded(byteStreamingManager, true, muleContext);
      initialiseIfNeeded(objectStreamingManager, true, muleContext);
      initialised = true;
    }
  }

  private ByteBufferManager createByteBufferManager() throws InitialisationException {
    CompositeClassLoader classLoader = from(getClass().getClassLoader(), muleContext.getExecutionClassLoader());
    ByteBufferManagerFactory factory;
    try {
      factory = (ByteBufferManagerFactory) instantiateClass(BUFFER_MANAGER_FACTORY_CLASS, new Object[] {}, classLoader);
    } catch (Exception e) {
      throw new InitialisationException(createStaticMessage(format("Could not create %s of type %s",
                                                                   ByteBufferManagerFactory.class.getName(),
                                                                   BUFFER_MANAGER_FACTORY_CLASS)),
                                        e, this);
    }

    return factory.create();
  }

  private MutableStreamingStatistics createStatistics() {
    return parseBoolean(getProperty(MULE_ENABLE_STREAMING_STATISTICS))
        ? new AtomicStreamingStatistics()
        : new NullStreamingStatistics();
  }

  protected ByteStreamingManager createByteStreamingManager() {
    return new DefaultByteStreamingManager(bufferManager, this, getFeatureFlaggingService());
  }

  protected ObjectStreamingManager createObjectStreamingManager() {
    return new DefaultObjectStreamingManager(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose() {
    disposeIfNeeded(byteStreamingManager, LOGGER);
    disposeIfNeeded(objectStreamingManager, LOGGER);
    disposeIfNeeded(bufferManager, LOGGER);
    disposeIfNeeded(cursorManager, LOGGER);

    initialised = false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ByteStreamingManager forBytes() {
    return byteStreamingManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ObjectStreamingManager forObjects() {
    return objectStreamingManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CursorProvider manage(CursorProvider provider, EventContext creatorEventContext) {
    if (provider.isManaged()) {
      return provider;
    }
    return cursorManager.manage(provider, (DefaultEventContext) ((BaseEventContext) creatorEventContext).getRootContext());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void manage(InputStream stream, EventContext creatorEventContext) {
    manage((Closeable) stream, creatorEventContext);
  }

  @Override
  public void manage(Closeable closeable, EventContext creatorEventContext) {
    if (closeable instanceof Cursor) {
      return;
    }

    ((BaseEventContext) creatorEventContext).onTerminated((response, throwable) -> closeQuietly(closeable));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CursorProvider manage(CursorProvider provider, CoreEvent creatorEvent) {
    return manage(provider, getRoot(creatorEvent.getContext()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void manage(InputStream stream, CoreEvent creatorEvent) {
    manage(stream, getRoot(creatorEvent.getContext()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Pair<CursorStreamProviderFactory, CursorIteratorProviderFactory> getPairFor(CursorProviderFactory provider) {
    CursorStreamProviderFactory cursorStreamProviderFactory;
    CursorIteratorProviderFactory cursorIteratorProviderFactory;

    if (provider instanceof CursorIteratorProviderFactory) {
      cursorIteratorProviderFactory = (CursorIteratorProviderFactory) provider;
      if (provider instanceof NullCursorIteratorProviderFactory
          && muleContext.getConfiguration().isInheritIterableRepeatability()) {
        cursorStreamProviderFactory = forBytes().getNullCursorProviderFactory();
      } else {
        cursorStreamProviderFactory = forBytes().getDefaultCursorProviderFactory();
      }
    } else if (provider instanceof CursorStreamProviderFactory) {
      cursorStreamProviderFactory = (CursorStreamProviderFactory) provider;
      cursorIteratorProviderFactory = forObjects().getDefaultCursorProviderFactory();
    } else if (provider instanceof NullCursorProviderFactory) {
      cursorStreamProviderFactory = forBytes().getNullCursorProviderFactory();
      cursorIteratorProviderFactory = forObjects().getNullCursorProviderFactory();
    } else {
      throw new IllegalArgumentException("The given cursor provider factory must be either a CursorStreamProviderFactory or a CursorIteratorProviderFactory");
    }
    return new Pair<>(cursorStreamProviderFactory, cursorIteratorProviderFactory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StreamingStatistics getStreamingStatistics() {
    return statistics;
  }

  protected ByteBufferManager getBufferManager() {
    return bufferManager;
  }

  protected FeatureFlaggingService getFeatureFlaggingService() {
    return ffService;
  }
}
