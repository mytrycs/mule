/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.operation;

import static com.zernova.runtime.api.metadata.DataType.STRING;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.internal.util.rx.ImmediateScheduler.IMMEDIATE_SCHEDULER;
import static com.zernova.test.heisenberg.extension.HeisenbergExtension.HEISENBERG;
import static com.zernova.test.heisenberg.extension.model.HealthStatus.DEAD;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Mono.from;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.config.ArtifactEncoding;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.retry.policy.RetryPolicyTemplate;
import com.zernova.runtime.core.api.streaming.CursorProviderFactory;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.api.util.ClassUtils;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationState;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.module.extension.api.runtime.privileged.ExecutionContextAdapter;
import com.zernova.runtime.module.extension.api.runtime.resolver.ResolverSetResult;
import com.zernova.runtime.module.extension.internal.loader.java.property.ParameterGroupModelProperty;
import com.zernova.runtime.module.extension.internal.runtime.DefaultExecutionContext;
import com.zernova.runtime.module.extension.internal.runtime.config.LifecycleAwareConfigurationInstance;
import com.zernova.runtime.module.extension.internal.runtime.exception.SdkMethodInvocationException;
import com.zernova.runtime.module.extension.internal.runtime.execution.deprecated.ReactiveMethodOperationExecutor;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.size.SmallTest;
import com.zernova.test.heisenberg.extension.HeisenbergExtension;
import com.zernova.test.heisenberg.extension.HeisenbergOperations;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@SmallTest
public class NonCompletableMethodOperationExecutorTestCase extends AbstractZernovaContextTestCase {

  private static final String CONFIG_NAME = "config";
  private static final DataType DATA_TYPE = STRING;

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Mock(answer = RETURNS_DEEP_STUBS)
  private CoreEvent muleEvent;

  @Mock
  private ResolverSetResult parameters;

  @Mock
  private ExtensionModel extensionModel;

  @Mock
  private ConfigurationModel configurationModel;

  @Mock
  private OperationModel operationModel;

  @Mock
  private ArtifactEncoding artifactEncoding;

  @Mock
  private ExtensionManager extensionManager;

  @Mock
  private CursorProviderFactory cursorProviderFactory;

  @Mock
  private StreamingManager streamingManager;

  @Mock
  private Component component;

  @Mock
  private ConfigurationState configurationState;

  @Mock
  private RetryPolicyTemplate retryPolicyTemplate;

  private ReactiveMethodOperationExecutor executor;
  private ConfigurationInstance configurationInstance;
  private ExecutionContextAdapter operationContext;
  private HeisenbergExtension config;
  private HeisenbergOperations operations;
  private final PrimitiveTypesTestOperations primitiveTypesTestOperations = new PrimitiveTypesTestOperations();


  @Before
  public void init() throws Exception {
    initHeisenberg();
    configurationInstance =
        new LifecycleAwareConfigurationInstance(CONFIG_NAME,
                                                configurationModel,
                                                config,
                                                configurationState,
                                                empty());

    when(muleEvent.getMessage().getPayload()).thenReturn(new TypedValue<>(null, DATA_TYPE));
    when(operationModel.getModelProperty(ParameterGroupModelProperty.class)).thenReturn(empty());
    operationContext =
        new DefaultExecutionContext<>(extensionModel, of(configurationInstance), parameters.asMap(), operationModel,
                                      muleEvent,
                                      artifactEncoding,
                                      muleContext.getNotificationManager(),
                                      cursorProviderFactory,
                                      streamingManager, component,
                                      retryPolicyTemplate, IMMEDIATE_SCHEDULER, empty(),
                                      muleContext.getSecurityManager(),
                                      muleContext);
    operationContext = spy(operationContext);
  }

  @Test
  public void operationWithReturnValueAndWithoutParameters() throws Exception {
    Method method = ClassUtils.getMethod(HeisenbergOperations.class, "sayMyName", new Class<?>[] {HeisenbergExtension.class});
    executor = new ReactiveMethodOperationExecutor(operationModel, method, operations);
    initialiseIfNeeded(executor, true, muleContext);
    assertResult(execute(), HEISENBERG);
  }

  @Test
  public void exceptionIsPropagated() throws Exception {
    final RuntimeException exception = new RuntimeException();
    operations = mock(HeisenbergOperations.class);
    when(operations.sayMyName(any(HeisenbergExtension.class))).thenThrow(exception);

    var thrown = assertThrows(SdkMethodInvocationException.class, () -> operationWithReturnValueAndWithoutParameters());
    assertThat(thrown.getCause(), sameInstance(exception));
  }

  @Test
  public void voidOperationWithoutParameters() throws Exception {
    Method method = ClassUtils.getMethod(HeisenbergOperations.class, "die", new Class<?>[] {HeisenbergExtension.class});
    executor = new ReactiveMethodOperationExecutor(operationModel, method, operations);
    initialiseIfNeeded(executor, true, muleContext);
    assertThat(execute(), is(nullValue()));
    assertThat(config.getEndingHealth(), is(DEAD));
  }

  @Test
  public void withArgumentsAndReturnValue() throws Exception {
    Map<String, Object> parametersMap = new HashMap<>();
    parametersMap.put("index", 0);
    when(parameters.asMap()).thenReturn(parametersMap);
    init();

    Method method =
        ClassUtils.getMethod(HeisenbergOperations.class, "getEnemy", new Class<?>[] {HeisenbergExtension.class, int.class});
    executor = new ReactiveMethodOperationExecutor(operationModel, method, operations);
    initialiseIfNeeded(executor, true, muleContext);

    assertResult(((Result) execute()).getOutput(), "Hank");
  }

  @Test
  public void voidWithArguments() throws Exception {
    Method method = ClassUtils.getMethod(HeisenbergOperations.class, "die", new Class<?>[] {HeisenbergExtension.class});
    executor = new ReactiveMethodOperationExecutor(operationModel, method, operations);
    initialiseIfNeeded(executor, true, muleContext);
    assertThat(execute(), is(nullValue()));
  }

  @Test
  public void withPrimitiveTypeArgumentsWithoutValue() throws Exception {
    Object[][] primitiveOperations = {{"charOperation", char.class}, {"byteOperation", byte.class},
        {"shortOperation", short.class}, {"intOperation", int.class}, {"longOperation", long.class},
        {"floatOperation", float.class}, {"doubleOperation", double.class}, {"booleanOperation", boolean.class}};
    for (Object[] primitiveOperation : primitiveOperations) {
      Method method = ClassUtils.getMethod(PrimitiveTypesTestOperations.class, (String) primitiveOperation[0],
                                           new Class<?>[] {(Class<?>) primitiveOperation[1]});
      executor = new ReactiveMethodOperationExecutor(operationModel, method, primitiveTypesTestOperations);
      initialiseIfNeeded(executor, true, muleContext);
      execute();
    }
  }

  @Test
  public void withAllPrimitiveTypeArgumentsWithoutValue() throws Exception {
    Class<?>[] parameterTypes =
        {char.class, byte.class, short.class, int.class, long.class, float.class, double.class, boolean.class};
    Method method = ClassUtils.getMethod(PrimitiveTypesTestOperations.class, "allCombined", parameterTypes);
    executor = new ReactiveMethodOperationExecutor(operationModel, method, primitiveTypesTestOperations);
    initialiseIfNeeded(executor, true, muleContext);
    execute();
  }

  private void initHeisenberg() {
    config = new HeisenbergExtension();
    config.getPersonalInfo().setName(HEISENBERG);
    config.setEnemies(Arrays.asList("Hank"));
    operations = new HeisenbergOperations();
  }

  private void assertResult(Object value, Object expected) throws Exception {
    assertThat(value, is(expected));
  }

  private Object execute() {
    return from(executor.execute(operationContext)).block();
  }

  public static class PrimitiveTypesTestOperations {

    private char charValue;
    private byte byteValue;
    private short shortValue;
    private int intValue;
    private long longValue;
    private float floatValue;
    private double doubleValue;
    private boolean booleanValue;

    public void charOperation(@com.zernova.runtime.extension.api.annotation.param.Optional char value) {
      assertThat(value, is(charValue));
    }

    public void byteOperation(@com.zernova.runtime.extension.api.annotation.param.Optional byte value) {
      assertThat(value, is(byteValue));
    }

    public void shortOperation(@com.zernova.runtime.extension.api.annotation.param.Optional short value) {
      assertThat(value, is(shortValue));
    }

    public void intOperation(@com.zernova.runtime.extension.api.annotation.param.Optional int value) {
      assertThat(value, is(intValue));
    }

    public void longOperation(@com.zernova.runtime.extension.api.annotation.param.Optional long value) {
      assertThat(value, is(longValue));
    }

    public void floatOperation(@com.zernova.runtime.extension.api.annotation.param.Optional float value) {
      assertThat(value, is(floatValue));
    }

    public void doubleOperation(@com.zernova.runtime.extension.api.annotation.param.Optional double value) {
      assertThat(value, is(doubleValue));
    }

    public void booleanOperation(@com.zernova.runtime.extension.api.annotation.param.Optional boolean value) {
      assertThat(value, is(booleanValue));
    }

    public void allCombined(@com.zernova.runtime.extension.api.annotation.param.Optional char charValue,
                            @com.zernova.runtime.extension.api.annotation.param.Optional byte byteValue,
                            @com.zernova.runtime.extension.api.annotation.param.Optional short shortValue,
                            @com.zernova.runtime.extension.api.annotation.param.Optional int intValue,
                            @com.zernova.runtime.extension.api.annotation.param.Optional long longValue,
                            @com.zernova.runtime.extension.api.annotation.param.Optional float floatValue,
                            @com.zernova.runtime.extension.api.annotation.param.Optional double doubleValue,
                            @com.zernova.runtime.extension.api.annotation.param.Optional boolean booleanValue) {
      assertThat(charValue, is(this.charValue));
      assertThat(byteValue, is(this.byteValue));
      assertThat(shortValue, is(this.shortValue));
      assertThat(intValue, is(this.intValue));
      assertThat(longValue, is(this.longValue));
      assertThat(floatValue, is(this.floatValue));
      assertThat(doubleValue, is(this.doubleValue));
      assertThat(booleanValue, is(this.booleanValue));
    }
  }
}
