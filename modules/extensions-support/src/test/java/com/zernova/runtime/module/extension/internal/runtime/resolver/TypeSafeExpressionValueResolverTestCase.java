/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.runtime.resolver;

import static java.lang.Thread.currentThread;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.zernova.runtime.api.message.Message.of;
import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.getType;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.toDataType;
import static com.zernova.tck.util.ZernovaContextUtils.eventBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.java.api.JavaTypeLoader;
import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.el.ExpressionManagerSession;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.verification.VerificationMode;

public class TypeSafeExpressionValueResolverTestCase extends AbstractZernovaContextTestCase {

  private static final String HELLO_WORLD = "Hello World!";
  private static final MetadataType STRING = new JavaTypeLoader(currentThread().getContextClassLoader()).load(String.class);

  @Rule
  public ExpectedException expected = none();

  private ExtendedExpressionManager expressionManager;

  @Override
  protected void doSetUp() throws Exception {
    muleContext = spy(muleContext);
    expressionManager = spy(muleContext.getExpressionManager());

    when(muleContext.getExpressionManager()).thenReturn(expressionManager);
    ((ZernovaContextWithRegistry) muleContext).getRegistry().registerObject("_muleExpressionManager", expressionManager);
  }

  @Test
  public void expressionLanguageWithoutTransformation() throws Exception {
    ValueResolver<Object> resolver = getResolver("#['Hello ' ++ payload]", STRING);
    ValueResolvingContext ctx = ValueResolvingContext.builder(eventBuilder(muleContext).message(of("World!")).build())
        .withExpressionManager(expressionManager)
        .build();
    assertResolved(resolver.resolve(ctx), HELLO_WORLD, times(1));
  }

  @Test
  public void expressionTemplateWithoutTransformation() throws Exception {
    ValueResolver<Object> resolver = getResolver("#['Hello $(payload)']", STRING);
    ValueResolvingContext ctx = ValueResolvingContext.builder(eventBuilder(muleContext).message(of("World!")).build())
        .withExpressionManager(expressionManager)
        .build();
    assertResolved(resolver.resolve(ctx), HELLO_WORLD, times(1));
  }

  @Test
  public void constant() throws Exception {
    ValueResolver<Object> resolver = getResolver("Hello World!", STRING);

    ExpressionManager expressionManager = mock(ExpressionManager.class);
    ExpressionManagerSession session = mock(ExpressionManagerSession.class);
    when(expressionManager.openSession(any(), any(), any())).thenReturn(session);

    ValueResolvingContext ctx = ValueResolvingContext.builder(eventBuilder(muleContext).message(of(HELLO_WORLD)).build())
        .withExpressionManager(expressionManager)
        .build();

    Object resolvedValue = resolver.resolve(ctx);
    assertResolved(resolvedValue, HELLO_WORLD, never());
    verify(session, never()).evaluate(anyString());
  }

  @Test
  public void expressionWithTransformation() throws Exception {
    ValueResolver<Object> resolver = getResolver("#[true]", STRING);
    ValueResolvingContext ctx = ValueResolvingContext.builder(eventBuilder(muleContext).message(of(HELLO_WORLD)).build())
        .withExpressionManager(expressionManager)
        .build();
    assertResolved(resolver.resolve(ctx), "true", times(1));
  }

  @Test
  public void templateWithTransformation() throws Exception {
    ValueResolver<Object> resolver = getResolver("#['tru$('e')']", STRING);
    ValueResolvingContext ctx = ValueResolvingContext.builder(eventBuilder(muleContext).message(of(HELLO_WORLD)).build())
        .withExpressionManager(expressionManager)
        .build();
    assertResolved(resolver.resolve(ctx), "true", times(1));
  }

  @Test
  public void nullExpression() throws Exception {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage("Expression cannot be blank or null");
    getResolver(null, STRING);
  }

  @Test
  public void blankExpression() throws Exception {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage("Expression cannot be blank or null");
    getResolver(EMPTY, STRING);
  }

  @Test
  public void nullExpectedType() throws Exception {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage("expected type cannot be null");
    getResolver("#[payload]", null);
  }

  private void assertResolved(Object resolvedValue, Object expected, VerificationMode expressionManagerVerificationMode) {
    assertThat(resolvedValue, instanceOf(String.class));
    assertThat(resolvedValue, equalTo(expected));
    verify(expressionManager, expressionManagerVerificationMode).openSession(any(BindingContext.class));
  }

  private <T> ValueResolver<T> getResolver(String expression, MetadataType expectedType) throws Exception {
    TypeSafeExpressionValueResolver<T> valueResolver = new TypeSafeExpressionValueResolver(expression,
                                                                                           getType(expectedType).orElse(null),
                                                                                           toDataType(expectedType));
    muleContext.getInjector().inject(valueResolver);
    valueResolver.setExtendedExpressionManager(expressionManager);
    valueResolver.setTransformationService(muleContext.getTransformationService());
    valueResolver.initialise();
    return valueResolver;
  }
}
