/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime;

import static com.zernova.functional.junit4.matchers.ThrowableRootCauseMatcher.hasRootCause;
import static com.zernova.runtime.api.artifact.ArtifactType.APP;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.api.retry.ReconnectionConfig.defaultReconnectionConfig;
import static com.zernova.runtime.core.internal.util.rx.ImmediateScheduler.IMMEDIATE_SCHEDULER;
import static com.zernova.runtime.module.extension.internal.ExtensionProperties.DO_NOT_RETRY;
import static com.zernova.tck.ZernovaTestUtils.stubComponentExecutor;
import static com.zernova.tck.ZernovaTestUtils.stubFailingComponentExecutor;
import static com.zernova.test.heisenberg.extension.HeisenbergErrors.CONNECTIVITY;
import static com.zernova.test.heisenberg.extension.HeisenbergErrors.HEALTH;
import static com.zernova.test.marvel.drstrange.DrStrangeErrorTypeDefinition.CUSTOM_ERROR;
import static com.zernova.test.module.extension.internal.util.ExtensionsTestUtils.mockExceptionEnricher;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.SECONDS;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.junit.MockitoJUnit.rule;
import static reactor.core.Exceptions.unwrap;

import com.zernova.runtime.api.artifact.ArtifactType;
import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.connection.ConnectionProvider;
import com.zernova.runtime.api.exception.ErrorTypeRepository;
import com.zernova.runtime.api.exception.TypedException;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.XmlDslModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.error.ImmutableErrorModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.notification.NotificationDispatcher;
import com.zernova.runtime.api.profiling.ProfilingDataProducer;
import com.zernova.runtime.api.profiling.type.context.ComponentThreadingProfilingEventContext;
import com.zernova.runtime.api.util.Reference;
import com.zernova.runtime.api.util.concurrent.Latch;
import com.zernova.runtime.ast.internal.error.DefaultErrorTypeBuilder;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.context.notification.ServerNotificationManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.retry.policy.NoRetryPolicyTemplate;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.core.api.retry.policy.SimpleRetryPolicyTemplate;
import com.zernova.runtime.core.api.security.SecurityManager;
import com.zernova.runtime.core.internal.connection.ConnectionManagerAdapter;
import com.zernova.runtime.core.internal.connection.ReconnectableConnectionProviderWrapper;
import com.zernova.runtime.extension.api.error.ErrorTypeDefinition;
import com.zernova.runtime.extension.api.error.ZernovaErrors;
import com.zernova.runtime.extension.api.exception.ModuleException;
import com.zernova.runtime.extension.api.property.ClassLoaderModelProperty;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationState;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutor;
import com.zernova.runtime.extension.api.runtime.operation.CompletableComponentExecutor.ExecutorCallback;
import com.zernova.runtime.extension.api.runtime.operation.Interceptor;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.internal.runtime.config.LifecycleAwareConfigurationInstance;
import com.zernova.runtime.module.extension.internal.runtime.config.MutableConfigurationStats;
import com.zernova.runtime.module.extension.internal.runtime.execution.interceptor.InterceptorChain;
import com.zernova.runtime.module.extension.internal.runtime.operation.DefaultExecutionMediator;
import com.zernova.runtime.module.extension.internal.runtime.operation.ExecutionMediator;
import com.zernova.runtime.module.extension.internal.runtime.operation.ResultTransformer;
import com.zernova.runtime.tracer.api.component.ComponentTracer;
import com.zernova.sdk.api.runtime.exception.ExceptionHandler;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.test.heisenberg.extension.exception.HeisenbergException;
import com.zernova.test.heisenberg.extension.exception.NullExceptionEnricher;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoRule;
import org.mockito.verification.VerificationMode;

import io.qameta.allure.Description;
import io.qameta.allure.Issue;

@SmallTest
@RunWith(Parameterized.class)
public class DefaultExecutionMediatorTestCase extends AbstractZernovaContextTestCase {

  public static final int RETRY_COUNT = 10;
  private static final String DUMMY_NAME = "dummyName";
  private static final String ERROR = "Error";

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return asList(new Object[] {"With simple retry", new SimpleRetryPolicyTemplate(10, RETRY_COUNT)},
                  new Object[] {"With no retry", new NoRetryPolicyTemplate()});
  }

  public DefaultExecutionMediatorTestCase(String name, RetryPolicyTemplate retryPolicy) {
    this.name = name;
    this.retryPolicy = retryPolicy;
  }

  @Rule
  public MockitoRule mockito = rule().silent();

  @Mock(answer = RETURNS_DEEP_STUBS)
  private ExecutionContextAdapter operationContext;

  @Mock(lenient = true)
  private ConfigurationInstance configurationInstance;

  @Mock
  private MutableConfigurationStats configurationStats;

  @Mock(lenient = true)
  private CompletableComponentExecutor operationExecutor;

  @Mock(lenient = true)
  private ExecutorCallback executorCallback;

  @Mock
  private CompletableComponentExecutor operationExceptionExecutor;

  @Mock
  private Interceptor interceptor1;

  @Mock
  private Interceptor interceptor2;

  @Mock
  private ExceptionHandler exceptionEnricher;

  @Mock
  private ConfigurationModel configurationModel;

  @Mock(lenient = true)
  private ExtensionModel extensionModel;

  @Mock
  private OperationModel operationModel;

  @Mock
  private NotificationDispatcher notificationDispatcher;

  @Mock
  private ArtifactEncoding artifactEncoding;

  @Mock
  private ServerNotificationManager notificationManager;

  @Mock
  private SecurityManager securityManager;

  @Mock
  private ConnectionManagerAdapter connectionManagerAdapter;

  @Mock
  private ProfilingDataProducer<ComponentThreadingProfilingEventContext, CoreEvent> threadReleaseDataProducer;

  @Mock
  private ComponentTracer<CoreEvent> operationExecutionTracer;

  private final String name;
  private final Object result = new Object();
  private final RetryPolicyTemplate retryPolicy;
  private final ConnectionException connectionException = new ConnectionException("Connection failure");
  private final Exception exception = new Exception();
  private final Exception exceptionWithMessage = new Exception(ERROR);
  private InOrder inOrder;
  private List<Interceptor> orderedInterceptors;
  private ExecutionMediator mediator;
  private InterceptorChain interceptorChain;

  private enum TestErrorTypes implements ErrorTypeDefinition<TestErrorTypes> {

    UNREGISTERED_ERROR_TYPE;

    @Override
    public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
      return of(ZernovaErrors.ANY);
    }
  }

  @Before
  public void before() throws Exception {
    when(configurationInstance.getStatistics()).thenReturn(configurationStats);
    when(configurationInstance.getName()).thenReturn(DUMMY_NAME);
    when(configurationInstance.getModel()).thenReturn(configurationModel);
    when(extensionModel.getName()).thenReturn(DUMMY_NAME);
    when(extensionModel.getModelProperty(ClassLoaderModelProperty.class)).thenReturn(empty());
    mockExceptionEnricher(extensionModel, null);
    mockExceptionEnricher(operationModel, null);

    stubComponentExecutor(operationExecutor, result);
    stubFailingComponentExecutor(operationExceptionExecutor, exception);

    when(operationContext.getConfiguration()).thenReturn(of(configurationInstance));
    when(operationContext.getExtensionModel()).thenReturn(extensionModel);
    when(operationContext.getTransactionConfig()).thenReturn(empty());
    when(operationContext.getRetryPolicyTemplate()).thenReturn(ofNullable(retryPolicy));
    when(operationContext.getCurrentScheduler()).thenReturn(IMMEDIATE_SCHEDULER);
    when(operationContext.getArtifactEncoding()).thenReturn(artifactEncoding);
    when(operationContext.getNotificationManager()).thenReturn(notificationManager);
    when(operationContext.getSecurityManager()).thenReturn(securityManager);

    when(extensionModel.getXmlDslModel()).thenReturn(XmlDslModel.builder().setPrefix("test-extension").build());

    interceptorChain = InterceptorChain.builder()
        .addInterceptor(interceptor1)
        .addInterceptor(interceptor2)
        .build();

    when(interceptor1.onError(any(), any())).thenAnswer(inv -> inv.getArgument(1));
    when(interceptor2.onError(any(), any())).thenAnswer(inv -> inv.getArgument(1));

    mediator = new DefaultExecutionMediator(extensionModel,
                                            operationModel,
                                            interceptorChain,
                                            mock(ErrorTypeRepository.class),
                                            this.getClass().getClassLoader(),
                                            mock(ZernovaConfiguration.class),
                                            APP,
                                            notificationDispatcher,
                                            null,
                                            threadReleaseDataProducer, operationExecutionTracer, true);

    final ReconnectableConnectionProviderWrapper<?> connectionProviderWrapper =
        new ReconnectableConnectionProviderWrapper<>(null,
                                                     defaultReconnectionConfig(true, retryPolicy));
    initialiseIfNeeded(connectionProviderWrapper, true, muleContext);
    Optional<ConnectionProvider<?>> connectionProvider = of(connectionProviderWrapper);

    doReturn(connectionProvider).when(configurationInstance).getConnectionProvider();
    when(exceptionEnricher.enrichException(any())).thenAnswer(inv -> {
      final Throwable toEnrich = inv.getArgument(0);
      if (toEnrich == exception || toEnrich.getCause() == exception) {
        return new HeisenbergException(ERROR, toEnrich);
      } else {
        return toEnrich;
      }
    });

    defineOrder(interceptor1, interceptor2);
  }

  @Test
  public void interceptorsInvokedOnSuccess() throws Throwable {
    Object result = execute();

    assertBefore();
    assertOnSuccess(times(1));
    assertOnError(never());
    assertResult(result);
  }

  @Test
  public void interceptorsInvokedOnError() throws Throwable {
    stubException();
    assertException(e -> {
      assertThat(e, is(instanceOf(ConnectionException.class)));

      try {
        assertBefore();
      } catch (Exception e2) {
        throw new RuntimeException(e2);
      }
      assertOnSuccess(never());
      assertOnError(times(1));
    });
  }

  @Test
  public void decoratedException() throws Throwable {
    stubException();
    assertException(e -> {
      assertThat(e, is(sameInstance(connectionException)));
      assertAfter(null);
    });
  }

  @Test
  public void exceptionOnBefore() throws Throwable {
    stubExceptionOnBeforeInterceptor();
    assertException(e -> {
      try {
        assertThat(e, is(sameInstance(exception)));
        assertBefore();
        assertOnError(never());
        verify(operationExecutor, never()).execute(same(operationContext), any());
      } catch (Exception e2) {
        throw new RuntimeException(e2);
      }
    });
  }

  @Test
  public void configurationStatsOnSuccessfulOperation() throws Throwable {
    execute();
    assertStatistics();
  }

  @Test
  public void configurationStatsOnFailedOperation() throws Throwable {
    stubException();
    assertException(e -> assertStatistics());
  }

  @Test
  public void configurationStatsOnFailedBeforeInterceptor() throws Throwable {
    stubExceptionOnBeforeInterceptor();
    assertException(e -> assertStatistics());
  }

  @Test
  public void enrichThrownException() throws Throwable {
    mockExceptionEnricher(operationModel, () -> exceptionEnricher);
    stubFailingComponentExecutor(operationExecutor, exception);

    mediator = new DefaultExecutionMediator(extensionModel,
                                            operationModel,
                                            interceptorChain,
                                            mock(ErrorTypeRepository.class),
                                            this.getClass().getClassLoader(),
                                            mock(ZernovaConfiguration.class),
                                            APP,
                                            notificationDispatcher,
                                            null,
                                            threadReleaseDataProducer, operationExecutionTracer, true);

    var thrown = assertThrows(HeisenbergException.class, this::execute);
    assertThat(thrown, hasRootCause(sameInstance(exception)));
    assertThat(thrown.getMessage(), is(ERROR));
  }

  @Test
  public void notEnrichThrownException() throws Throwable {
    mockExceptionEnricher(operationModel, NullExceptionEnricher::new);
    stubFailingComponentExecutor(operationExecutor, exception);

    mediator = new DefaultExecutionMediator(extensionModel,
                                            operationModel,
                                            interceptorChain,
                                            mock(ErrorTypeRepository.class),
                                            this.getClass().getClassLoader(),
                                            mock(ZernovaConfiguration.class),
                                            APP,
                                            notificationDispatcher,
                                            null,
                                            threadReleaseDataProducer, operationExecutionTracer, true);

    var thrown = assertThrows(Exception.class, this::execute);
    assertThat(thrown, sameInstance(exception));
  }

  @Test
  public void enrichThrownExceptionInValueTransformer() throws Throwable {
    final Exception exceptionToThrow = new RuntimeException(ERROR, exception);
    mockExceptionEnricher(operationModel, () -> exceptionEnricher);
    final ResultTransformer failingTransformer = mock(ResultTransformer.class);
    when(failingTransformer.apply(any(), any())).thenThrow(exceptionToThrow);

    mediator = new DefaultExecutionMediator(extensionModel,
                                            operationModel,
                                            interceptorChain,
                                            mock(ErrorTypeRepository.class),
                                            this.getClass().getClassLoader(),
                                            mock(ZernovaConfiguration.class),
                                            APP,
                                            notificationDispatcher,
                                            failingTransformer,
                                            threadReleaseDataProducer, operationExecutionTracer, true);

    var thrown = assertThrows(HeisenbergException.class, this::execute);
    assertThat(thrown, hasRootCause(sameInstance(exception)));
    assertThat(thrown.getMessage(), is(ERROR));
  }

  @Test
  public void enrichThrownSdkModuleExceptionInValueTransformer() throws Throwable {
    final com.zernova.sdk.api.exception.ModuleException moduleExceptionToThrow =
        new com.zernova.sdk.api.exception.ModuleException(ERROR, HEALTH, exception);
    mockExceptionEnricher(operationModel, () -> exceptionEnricher);
    final ResultTransformer failingTransformer = mock(ResultTransformer.class);
    when(failingTransformer.apply(any(), any())).thenThrow(moduleExceptionToThrow);

    ErrorTypeRepository errorTypeRepository = mockErrorModel();

    mediator = new DefaultExecutionMediator(extensionModel,
                                            operationModel,
                                            interceptorChain,
                                            errorTypeRepository,
                                            this.getClass().getClassLoader(),
                                            mock(ZernovaConfiguration.class),
                                            APP,
                                            notificationDispatcher,
                                            failingTransformer,
                                            threadReleaseDataProducer, operationExecutionTracer, true);

    var thrown = assertThrows(HeisenbergException.class, this::execute);
    assertThat(thrown.getCause(), sameInstance(moduleExceptionToThrow));
  }

  @Test
  public void enrichThrownLegacyModuleExceptionInValueTransformer() throws Throwable {
    final ModuleException moduleExceptionToThrow = new ModuleException(ERROR, CUSTOM_ERROR, exception);
    mockExceptionEnricher(operationModel, () -> exceptionEnricher);
    final ResultTransformer failingTransformer = mock(ResultTransformer.class);
    when(failingTransformer.apply(any(), any())).thenThrow(moduleExceptionToThrow);

    ErrorTypeRepository errorTypeRepository = mockErrorModel();

    mediator = new DefaultExecutionMediator(extensionModel,
                                            operationModel,
                                            interceptorChain,
                                            errorTypeRepository,
                                            this.getClass().getClassLoader(),
                                            mock(ZernovaConfiguration.class),
                                            APP,
                                            notificationDispatcher,
                                            failingTransformer,
                                            threadReleaseDataProducer, operationExecutionTracer, true);

    var thrown = assertThrows(HeisenbergException.class, this::execute);
    assertThat(thrown.getCause(), sameInstance(moduleExceptionToThrow));
  }

  @Test
  public void notEnrichThrownExceptionInValueTransformer() throws Throwable {
    final Exception exceptionToThrow = new RuntimeException(ERROR, exception);
    mockExceptionEnricher(operationModel, NullExceptionEnricher::new);
    final ResultTransformer failingTransformer = mock(ResultTransformer.class);
    when(failingTransformer.apply(any(), any())).thenThrow(exceptionToThrow);

    mediator = new DefaultExecutionMediator(extensionModel,
                                            operationModel,
                                            interceptorChain,
                                            mock(ErrorTypeRepository.class),
                                            this.getClass().getClassLoader(),
                                            mock(ZernovaConfiguration.class),
                                            APP,
                                            notificationDispatcher,
                                            failingTransformer,
                                            threadReleaseDataProducer, operationExecutionTracer, true);

    var thrown = assertThrows(Exception.class, this::execute);
    assertThat(thrown.getCause(), sameInstance(exception));
    assertThat(thrown.getMessage(), is(ERROR));
  }

  @Test
  public void notEnrichThrownSdkModuleExceptionInValueTransformer() throws Throwable {
    final com.zernova.sdk.api.exception.ModuleException moduleExceptionToThrow =
        new com.zernova.sdk.api.exception.ModuleException(ERROR, HEALTH, exception);
    mockExceptionEnricher(operationModel, NullExceptionEnricher::new);
    final ResultTransformer failingTransformer = mock(ResultTransformer.class);
    when(failingTransformer.apply(any(), any())).thenThrow(moduleExceptionToThrow);

    ErrorTypeRepository errorTypeRepository = mockErrorModel();

    mediator = new DefaultExecutionMediator(extensionModel,
                                            operationModel,
                                            interceptorChain,
                                            errorTypeRepository,
                                            this.getClass().getClassLoader(),
                                            mock(ZernovaConfiguration.class),
                                            APP,
                                            notificationDispatcher,
                                            failingTransformer,
                                            threadReleaseDataProducer, operationExecutionTracer, true);

    var thrown = assertThrows(TypedException.class, this::execute);
    assertThat(thrown, hasRootCause(sameInstance(exception)));
  }

  @Test
  public void notEnrichThrownSdkModuleExceptionWithMessageInValueTransformer() throws Throwable {
    final com.zernova.sdk.api.exception.ModuleException moduleExceptionToThrow =
        new com.zernova.sdk.api.exception.ModuleException(ERROR, HEALTH, exceptionWithMessage);
    mockExceptionEnricher(operationModel, NullExceptionEnricher::new);
    final ResultTransformer failingTransformer = mock(ResultTransformer.class);
    when(failingTransformer.apply(any(), any())).thenThrow(moduleExceptionToThrow);

    ErrorTypeRepository errorTypeRepository = mockErrorModel();

    mediator = new DefaultExecutionMediator(extensionModel,
                                            operationModel,
                                            interceptorChain,
                                            errorTypeRepository,
                                            this.getClass().getClassLoader(),
                                            mock(ZernovaConfiguration.class),
                                            APP,
                                            notificationDispatcher,
                                            failingTransformer,
                                            threadReleaseDataProducer, operationExecutionTracer, true);

    var thrown = assertThrows(TypedException.class, this::execute);
    assertThat(thrown, hasRootCause(sameInstance(exceptionWithMessage)));
  }

  @Test
  public void notEnrichThrownLegacyModuleExceptionInValueTransformer() throws Throwable {
    final ModuleException moduleExceptionToThrow = new ModuleException(ERROR, CUSTOM_ERROR, exception);
    mockExceptionEnricher(operationModel, NullExceptionEnricher::new);
    final ResultTransformer failingTransformer = mock(ResultTransformer.class);
    when(failingTransformer.apply(any(), any())).thenThrow(moduleExceptionToThrow);

    ErrorTypeRepository errorTypeRepository = mockErrorModel();

    mediator = new DefaultExecutionMediator(extensionModel,
                                            operationModel,
                                            interceptorChain,
                                            errorTypeRepository,
                                            this.getClass().getClassLoader(),
                                            mock(ZernovaConfiguration.class),
                                            APP,
                                            notificationDispatcher,
                                            failingTransformer,
                                            threadReleaseDataProducer, operationExecutionTracer, true);

    var thrown = assertThrows(TypedException.class, this::execute);
    assertThat(thrown, hasRootCause(sameInstance(exception)));
  }

  @Test
  @Issue("MULE-19707")
  public void invalidEnrichment() throws Throwable {
    final ResultTransformer failingTransformer = mock(ResultTransformer.class);
    ModuleException moduleException = new ModuleException(ERROR, TestErrorTypes.UNREGISTERED_ERROR_TYPE);
    mockExceptionEnricher(operationModel, () -> exceptionEnricher);
    stubFailingComponentExecutor(operationExecutor, moduleException);
    mediator = new DefaultExecutionMediator<>(extensionModel,
                                              operationModel,
                                              interceptorChain,
                                              mock(ErrorTypeRepository.class),
                                              this.getClass().getClassLoader(),
                                              mock(ZernovaConfiguration.class),
                                              APP,
                                              notificationDispatcher,
                                              failingTransformer,
                                              threadReleaseDataProducer, operationExecutionTracer, true);
    var thrown = assertThrows(ModuleException.class, this::execute);
    verify(executorCallback, times(1)).error(moduleException);
  }

  @Test
  public void notReconnectInValueTransformerWhenVariableIsSet() {
    int expectedRetries = retryPolicy.isEnabled() ? 1 : 0;
    final com.zernova.sdk.api.exception.ModuleException moduleExceptionToThrow =
        new com.zernova.sdk.api.exception.ModuleException(ERROR, CONNECTIVITY, connectionException);
    when(operationContext.getVariable(DO_NOT_RETRY)).thenReturn("true");
    clearInvocations(operationContext);
    mockExceptionEnricher(operationModel, NullExceptionEnricher::new);
    final ResultTransformer failingTransformer = mock(ResultTransformer.class);
    when(failingTransformer.apply(eq(operationContext), any())).thenThrow(moduleExceptionToThrow);

    mediator = new DefaultExecutionMediator(extensionModel,
                                            operationModel,
                                            interceptorChain,
                                            mockErrorModel(),
                                            this.getClass().getClassLoader(),
                                            mock(ZernovaConfiguration.class),
                                            APP,
                                            notificationDispatcher,
                                            failingTransformer,
                                            threadReleaseDataProducer, operationExecutionTracer, true);

    try {
      execute();
    } catch (Exception e) {
      assertThat(e, is(instanceOf(ConnectionException.class)));
      assertThat(e.getMessage(), is("Connection failure"));
      verify(failingTransformer, times(1)).apply(any(), any());
      verify(operationContext, times(expectedRetries)).getVariable(DO_NOT_RETRY);
    }
  }

  private ErrorTypeRepository mockErrorModel() {
    final ErrorType parentErrorType = mock(ErrorType.class);
    ErrorTypeRepository errorTypeRepository = mock(ErrorTypeRepository.class);
    when(errorTypeRepository.lookupErrorType(any())).thenReturn(of(DefaultErrorTypeBuilder.builder()
        .namespace("testNs")
        .identifier("test")
        .parentErrorType(parentErrorType)
        .build()));

    when(operationModel.getErrorModels()).thenReturn(singleton(new ImmutableErrorModel("test", "testNs", true, null)));
    return errorTypeRepository;
  }

  @Test
  public void retry() throws Throwable {
    stubException();

    int expectedRetries = retryPolicy instanceof SimpleRetryPolicyTemplate
        ? RETRY_COUNT + 1
        : 1;

    assertException(exception -> {
      assertThat(exception, instanceOf(ConnectionException.class));
      try {
        verify(interceptor1, times(expectedRetries)).before(operationContext);
        verify(interceptor2, times(expectedRetries)).before(operationContext);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      verify(interceptor1, times(expectedRetries)).onError(same(operationContext), any());
      verify(interceptor1, times(expectedRetries)).after(operationContext, null);

      verify(interceptor2, times(expectedRetries)).onError(same(operationContext), any());
      verify(interceptor2, times(expectedRetries)).after(operationContext, null);
    });
  }

  @Test
  @Description("Tests the exception raised when trying to get the statistics from a configuration instance before " +
      "initializing it is correctly propagated through the executor's error callback")
  @Issue("W-10742153")
  public void getStatisticsFromNotInitializedConfigurationInstance() throws Throwable {
    ConfigurationInstance configurationInstance =
        new LifecycleAwareConfigurationInstance("name", mock(ConfigurationModel.class), null, mock(ConfigurationState.class),
                                                of(mock(ConnectionProvider.class)));
    when(operationContext.getConfiguration()).thenReturn(of(configurationInstance));

    assertThrows(IllegalStateException.class, this::execute);
  }

  private void assertException(Consumer<Throwable> assertion) throws Throwable {
    try {
      execute();
      fail("was expecting a exception");
    } catch (Exception e) {
      assertion.accept(unwrap(e));
    }
  }

  private void stubExceptionOnBeforeInterceptor() throws Exception {
    doThrow(exception).when(interceptor2).before(operationContext);
  }

  private void assertStatistics() {
    verify(configurationStats).addInflightOperation();
    verify(configurationStats).discountInflightOperation();
  }

  private void assertBefore() throws Exception {
    verifyInOrder(interceptor -> {
      try {
        interceptor.before(operationContext);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    });
  }

  private void assertOnSuccess(VerificationMode verificationMode) {
    verifyInOrder(interceptor -> {
      interceptor.onSuccess(operationContext, result);
      interceptor.after(operationContext, result);
    }, verificationMode);
  }

  private void assertOnError(VerificationMode verificationMode) {
    verifyInOrder(interceptor -> {
      interceptor.onError(same(operationContext), same(connectionException));
      interceptor.after(operationContext, null);
    },
                  verificationMode);
  }

  private void assertAfter(Object expected) {
    verifyInOrder(interceptor -> interceptor.after(operationContext, expected));
  }

  private void assertResult(Object result) {
    assertThat(result, is(sameInstance(this.result)));
  }

  private void stubException() throws Exception {
    stubFailingComponentExecutor(operationExecutor, connectionException);
  }

  private void defineOrder(Interceptor... interceptors) {
    inOrder = inOrder(interceptors);
    orderedInterceptors = ImmutableList.copyOf(interceptors);
  }

  private void verifyInOrder(Consumer<Interceptor> consumer) {
    verifyInOrder(consumer, times(1));
  }

  private void verifyInOrder(Consumer<Interceptor> consumer, VerificationMode verificationMode) {
    for (Interceptor interceptor : orderedInterceptors) {
      consumer.accept(inOrder.verify(interceptor, verificationMode));
    }
  }

  private Object execute() throws Exception {
    Latch latch = new Latch();
    Reference<Object> result = new Reference<>();
    Reference<Exception> exception = new Reference<>();

    doAnswer(invocation -> {
      result.set(invocation.getArgument(0));
      latch.release();

      return null;
    }).when(executorCallback).complete(any());

    doAnswer(invocation -> {
      exception.set(invocation.getArgument(0));
      latch.release();

      return null;
    }).when(executorCallback).error(any());

    try {
      mediator.execute(operationExecutor, operationContext, executorCallback);
    } catch (Throwable e) {
      fail("Uncaught throwable in execution mediator");
    }

    latch.await(5, SECONDS);

    if (exception.get() != null) {
      throw exception.get();
    }

    return result.get();
  }
}
