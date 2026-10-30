/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.config;

import static com.zernova.runtime.api.el.BindingContextUtils.NULL_BINDING_CONTEXT;
import static com.zernova.runtime.api.scheduler.SchedulerConfig.config;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_SCHEDULER_BASE_CONFIG;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.tck.config.WeaveExpressionLanguageFactoryServiceProvider.provideDefaultExpressionLanguageFactoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import static org.hamcrest.collection.IsEmptyCollection.empty;

import static org.junit.Assert.assertThat;

import com.zernova.runtime.api.config.custom.CustomizationService;
import com.zernova.runtime.api.config.custom.ServiceConfigurator;
import com.zernova.runtime.api.el.DefaultExpressionLanguageFactoryService;
import com.zernova.runtime.api.el.DefaultValidationResult;
import com.zernova.runtime.api.el.ExpressionLanguage;
import com.zernova.runtime.api.el.ExpressionLanguageConfiguration;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService;
import com.zernova.runtime.api.scheduler.SchedulerView;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.registry.ZernovaRegistry;
import com.zernova.runtime.core.privileged.registry.RegistrationException;
import com.zernova.runtime.http.api.HttpService;
import com.zernova.runtime.http.api.server.HttpServer;
import com.zernova.runtime.http.api.server.HttpServerFactory;
import com.zernova.runtime.http.api.server.ServerCreationException;
import com.zernova.tck.SimpleUnitTestSupportSchedulerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Registers services instances into the {@link ZernovaRegistry} of a {@link ZernovaContext}.
 * <p>
 * This is to be used only in tests that do not leverage the service injection mechanism.
 *
 * @since 4.0
 */
public class TestServicesConfigurationBuilder extends AbstractConfigurationBuilder implements ServiceConfigurator, TestRule {

  private static final DefaultValidationResult SUCCESS_VALIDATION_RESULT = new DefaultValidationResult(true, null);
  private static final String MOCK_HTTP_SERVICE = "mockHttpService";
  private static final String MOCK_EXPR_EXECUTOR = "mockExpressionExecutor";
  private static final String MOCK_EXPRESSION_LANGUAGE_METADATA_SERVICE = "mockExpressionLanguageMetadataService";

  private static final ExpressionLanguageMetadataService expressionLanguageMetadataService =
      mock(ExpressionLanguageMetadataService.class);

  private static DefaultExpressionLanguageFactoryService cachedExprLanguageFactory;
  private static int cachedExprLanguageFactoryCounter = 0;

  private final SimpleUnitTestSupportSchedulerService schedulerService = new SimpleUnitTestSupportSchedulerService();

  private final boolean mockHttpService;
  private final boolean mockExpressionExecutor;
  private final boolean mockExpressionLanguageMetadataService;

  private final Map<String, Object> additionalMockedServices = new HashMap<>();

  private final Map<String, Object> overriddenDefaultServices = new HashMap<>();

  public TestServicesConfigurationBuilder() {
    this(true, true, true);
  }

  public TestServicesConfigurationBuilder(boolean mockHttpService, boolean mockExpressionExecutor,
                                          boolean mockExpressionLanguageMetadataService) {
    this.mockHttpService = mockHttpService;
    this.mockExpressionExecutor = mockExpressionExecutor;
    this.mockExpressionLanguageMetadataService = mockExpressionLanguageMetadataService;
  }

  @Override
  public void doConfigure(ZernovaContext muleContext) throws Exception {
    withContextClassLoader(TestServicesConfigurationBuilder.class.getClassLoader(), () -> {
      try {
        ZernovaRegistry registry = ((ZernovaContextWithRegistry) muleContext).getRegistry();
        registerServices(muleContext, registry);

        registry.registerObjects(additionalMockedServices);
      } catch (RegistrationException e) {
        throw new ZernovaRuntimeException(e);
      }
    });
  }

  @Override
  public void configure(CustomizationService customizationService) {
    customizationService.registerCustomServiceImpl(schedulerService.getName(),
                                                   spy(schedulerService),
                                                   true);
    if (mockExpressionExecutor) {
      customizationService.registerCustomServiceImpl(MOCK_EXPR_EXECUTOR,
                                                     createMockExpressionExecutor(),
                                                     true);
    } else {
      initCachedExprLanguageFactory();
      customizationService.registerCustomServiceImpl(MOCK_EXPR_EXECUTOR,
                                                     cachedExprLanguageFactory,
                                                     true);
    }
    if (mockHttpService) {
      customizationService.registerCustomServiceImpl(MOCK_HTTP_SERVICE, mockHttpService(),
                                                     true);
    }

    if (mockExpressionLanguageMetadataService) {
      customizationService.registerCustomServiceImpl(MOCK_EXPRESSION_LANGUAGE_METADATA_SERVICE,
                                                     expressionLanguageMetadataService,
                                                     true);
    }
  }

  protected void registerServices(ZernovaContext muleContext, ZernovaRegistry registry) throws RegistrationException {
    registry.registerObject(schedulerService.getName(), spy(schedulerService));
    registry.registerObject(OBJECT_SCHEDULER_BASE_CONFIG, config());

    if (mockExpressionExecutor) {
      registry.registerObject(MOCK_EXPR_EXECUTOR, createMockExpressionExecutor());
    } else {
      initCachedExprLanguageFactory();
      registry.registerObject(cachedExprLanguageFactory.getName(), cachedExprLanguageFactory);
    }

    if (mockHttpService) {
      registry.registerObject(MOCK_HTTP_SERVICE, mockHttpService());
    }

    if (mockExpressionLanguageMetadataService) {
      registry.registerObject(MOCK_EXPRESSION_LANGUAGE_METADATA_SERVICE, mock(ExpressionLanguageMetadataService.class));
    }

    overriddenDefaultServices.forEach((serviceId, serviceImpl) -> {
      muleContext.getCustomizationService().overrideDefaultServiceImpl(serviceId, serviceImpl);
    });
  }

  protected void initCachedExprLanguageFactory() {
    // Avoid doing the DW warm-up for every test, reusing the ExpressionLanguage implementation
    // Still have to recreate ever once in a while so global bindings added for each test are accumulated.
    if (cachedExprLanguageFactory == null || cachedExprLanguageFactoryCounter > 20) {
      cachedExprLanguageFactoryCounter = 0;
      final DefaultExpressionLanguageFactoryService exprExecutor = provideDefaultExpressionLanguageFactoryService();
      ExpressionLanguage exprLanguage = exprExecutor.create();
      // Force initialization of internal DataWeave stuff
      // This way we avoid doing some heavy initialization on the test itself,
      // which may cause trouble when evaluation expressions in places with small timeouts
      exprLanguage.evaluate("{dataWeave: 'is'} ++ {zernova: 'default EL'}", NULL_BINDING_CONTEXT);

      cachedExprLanguageFactory = new DefaultExpressionLanguageFactoryService() {

        @Override
        public ExpressionLanguage create() {
          return exprLanguage;
        }

        @Override
        public ExpressionLanguage create(ExpressionLanguageConfiguration configuration) {
          return exprLanguage;
        }

        @Override
        public String getName() {
          return exprExecutor.getName();
        }
      };
    } else {
      cachedExprLanguageFactoryCounter++;
    }
  }

  protected DefaultExpressionLanguageFactoryService createMockExpressionExecutor() {
    ExpressionLanguage lang = mock(ExpressionLanguage.class, RETURNS_DEEP_STUBS);
    when(lang.validate(anyString())).thenReturn(SUCCESS_VALIDATION_RESULT);

    DefaultExpressionLanguageFactoryService languageFactoryService =
        mock(DefaultExpressionLanguageFactoryService.class, RETURNS_DEEP_STUBS);
    when(languageFactoryService.create()).thenReturn(lang);
    when(languageFactoryService.create(any())).thenReturn(lang);

    return languageFactoryService;
  }

  protected HttpService mockHttpService() {
    try {
      HttpServerFactory httpServerFactory = mock(HttpServerFactory.class);
      HttpServer httpServer = mock(HttpServer.class);
      when(httpServerFactory.create(any())).thenReturn(httpServer);
      HttpService service = mock(HttpService.class);
      when(service.getServerFactory()).thenReturn(httpServerFactory);
      return service;
    } catch (ServerCreationException e) {
      return null;
    }
  }

  public void stopServices() throws ZernovaException {
    final List<SchedulerView> schedulers = schedulerService.getSchedulers();
    try {
      assertThat(schedulers, empty());
    } finally {
      schedulerService.stop();
    }
  }

  public void registerAdditionalService(String name, Object service) {
    this.additionalMockedServices.put(name, service);
  }

  public void registerOverriddenService(String name, Object service) {
    this.overriddenDefaultServices.put(name, service);
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        try {
          base.evaluate();
        } finally {
          stopServices();
        }
      }
    };
  }

}
