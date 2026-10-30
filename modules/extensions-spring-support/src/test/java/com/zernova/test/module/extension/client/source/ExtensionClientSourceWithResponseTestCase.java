/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.client.source;

import static com.zernova.runtime.extension.api.util.ExtensionModelUtils.getExtensionClassLoader;
import static com.zernova.test.allure.AllureConstants.ExtensionsClientFeature.EXTENSIONS_CLIENT;
import static com.zernova.test.allure.AllureConstants.ExtensionsClientFeature.ExtensionsClientStory.MESSAGE_SOURCE;
import static com.zernova.test.marvel.MarvelExtension.MARVEL_EXTENSION;
import static com.zernova.test.marvel.xmen.MagnetoMutantSummon.CLASSLOADER_NOTIFICATION_ACTION;
import static com.zernova.test.marvel.xmen.MagnetoMutantSummon.ERROR_NOTIFICATION_ACTION;
import static com.zernova.test.marvel.xmen.MagnetoMutantSummon.MESSAGE;

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.notification.CustomNotification;
import com.zernova.runtime.api.notification.CustomNotificationListener;
import com.zernova.runtime.api.notification.NotificationListenerRegistry;
import com.zernova.runtime.api.util.Reference;
import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.util.IOUtils;
import com.zernova.runtime.extension.api.client.source.SourceResultHandler;

import java.io.InputStream;
import java.util.function.Consumer;

import jakarta.inject.Inject;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(EXTENSIONS_CLIENT)
@Story(MESSAGE_SOURCE)
public class ExtensionClientSourceWithResponseTestCase extends BaseExtensionClientSourceTestCase {

  @Override
  protected String[] getConfigFiles() {
    return new String[] {};
  }

  @Inject
  private NotificationListenerRegistry notificationListenerRegistry;

  @Inject
  private ExtensionManager extensionManager;

  private ClassLoader sourceCallbackContextClassLoader;
  private final Latch sourceCallbackContextClassLoaderLatch = new Latch();

  @Override
  protected void doSetUp() throws Exception {
    notificationListenerRegistry.registerListener((CustomNotificationListener<CustomNotification>) notification -> {
      if (notification.getAction().getActionId() == CLASSLOADER_NOTIFICATION_ACTION) {
        sourceCallbackContextClassLoader = (ClassLoader) notification.getSource();
        sourceCallbackContextClassLoaderLatch.release();
      }
    });
  }

  @Test
  public void sendSuccessfulResponse() throws Exception {
    Latch latch = new Latch();
    InputStream responseStream = mock(InputStream.class);

    Consumer<SourceResultHandler<InputStream, Void>> handlerConsumer = handler -> {
      String message = IOUtils.toString(handler.getResult().getOutput());
      assertThat(message, equalTo(MESSAGE));

      handler.completeWithSuccess(params -> params.withParameter("body", responseStream))
          .whenComplete((v, e) -> latch.release());
    };

    handler = extensionsClient.createSource("Marvel",
                                            "MagnetoMutantSummon",
                                            handlerConsumer,
                                            parameters -> {
                                            });

    handler.start();
    assertThat(latch.await(5, SECONDS), is(true));
    verify(responseStream).read(any(byte[].class));

    assertSourceCallbackContextClassLoader();
  }

  @Test
  public void sendErrorResponse() throws Exception {
    final Latch latch = new Latch();
    final Reference<Error> capturedError = new Reference<>();
    final String errorMessage = "Long Live Professor X";

    notificationListenerRegistry.registerListener((CustomNotificationListener<CustomNotification>) notification -> {
      if (notification.getAction().getActionId() == ERROR_NOTIFICATION_ACTION) {
        capturedError.set((Error) notification.getSource());
        latch.release();
      }
    });

    Consumer<SourceResultHandler<InputStream, Void>> handlerConsumer = handler -> {
      String message = IOUtils.toString(handler.getResult().getOutput());
      assertThat(message, equalTo(MESSAGE));

      handler.completeWithError(new RuntimeException(errorMessage), params -> {
      });
    };

    handler = extensionsClient.createSource("Marvel",
                                            "MagnetoMutantSummon",
                                            handlerConsumer,
                                            parameters -> {
                                            });

    handler.start();
    assertThat(latch.await(5, SECONDS), is(true));
    Error error = capturedError.get();

    assertThat(error.getDescription(), equalTo(errorMessage));
    assertThat(error.getErrorType().getIdentifier(), equalTo("UNKNOWN"));
    assertThat(error.getErrorType().getNamespace(), equalTo("ZERNOVA"));

    assertSourceCallbackContextClassLoader();
  }

  private void assertSourceCallbackContextClassLoader() throws Exception {
    ExtensionModel marvelModel = extensionManager.getExtension(MARVEL_EXTENSION).get();
    assertThat(sourceCallbackContextClassLoaderLatch.await(5, SECONDS), is(true));
    assertThat(sourceCallbackContextClassLoader, is(sameInstance(getExtensionClassLoader(marvelModel).get())));
  }
}
