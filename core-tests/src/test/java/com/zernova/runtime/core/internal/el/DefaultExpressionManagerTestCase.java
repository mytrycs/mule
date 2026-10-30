/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.el;

import static com.zernova.runtime.api.el.BindingContext.builder;
import static com.zernova.runtime.api.el.BindingContextUtils.NULL_BINDING_CONTEXT;
import static com.zernova.runtime.api.metadata.DataType.BYTE_ARRAY;
import static com.zernova.runtime.api.metadata.DataType.OBJECT;
import static com.zernova.runtime.api.metadata.DataType.STRING;
import static com.zernova.runtime.api.metadata.DataType.fromFunction;
import static com.zernova.runtime.api.metadata.DataType.fromType;
import static com.zernova.runtime.api.metadata.MediaType.XML;
import static com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation.fromSingleComponent;
import static com.zernova.tck.junit4.matcher.IsEqualIgnoringLineBreaks.equalToIgnoringLineBreaks;
import static com.zernova.test.allure.AllureConstants.ExpressionLanguageFeature.EXPRESSION_LANGUAGE;
import static com.zernova.test.allure.AllureConstants.ExpressionLanguageFeature.ExpressionLanguageStory.SUPPORT_DW;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.of;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.api.el.ExpressionFunction;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.FunctionParameter;
import com.zernova.runtime.api.metadata.MediaType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.api.el.ExpressionManagerSession;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(EXPRESSION_LANGUAGE)
@Story(SUPPORT_DW)
public class DefaultExpressionManagerTestCase extends AbstractZernovaContextTestCase {

  private static final String MY_VAR = "myVar";

  @Rule
  public MockitoRule mockitorule = MockitoJUnit.rule();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private StreamingManager streamingManager;

  private ExtendedExpressionManager expressionManager;

  @Before
  public void configureExpressionManager() throws ZernovaException {
    expressionManager = muleContext.getExpressionManager();
  }

  @Test
  @Description("Verifies that global bindings can be added.")
  public void globals() {
    DataType integerType = fromType(Integer.class);

    ExpressionFunction multiply = new ExpressionFunction() {

      @Override
      public Object call(Object[] objects, BindingContext bindingContext) {
        return ((Integer) objects[0]) * ((Integer) objects[1]);
      }

      @Override
      public Optional<DataType> returnType() {
        return of(integerType);
      }

      @Override
      public List<FunctionParameter> parameters() {
        return asList(new FunctionParameter("x", integerType),
                      new FunctionParameter("y", integerType));
      }
    };

    BindingContext globalContext = builder()
        .addBinding("aNum", new TypedValue<>(4, fromType(Integer.class)))
        .addBinding("times", new TypedValue<>(multiply, fromFunction(multiply)))
        .build();

    expressionManager.addGlobalBindings(globalContext);

    TypedValue result = expressionManager.evaluate("aNum times 5");
    assertThat(result.getValue(), is(20));

    expressionManager.addGlobalBindings(builder().addBinding("otherNum", new TypedValue(3, integerType)).build());

    result = expressionManager.evaluate("(times(7, 3) + otherNum) / aNum");
    assertThat(result.getValue(), is(6));
  }

  @Test
  @Description("Verifies that a simple literal expression is successful.")
  public void simple() {
    String expression = "\"wow\"";
    assertString(expression);
  }

  @Test
  @Description("Verifies that a simple literal expression is successful when using brackets.")
  public void simpleEnclosed() {
    assertString("#[\"wow\"]");
  }

  private void assertString(String expression) {
    assertThat(expressionManager.evaluate(expression).getValue(), is("wow"));
  }

  @Test
  @Description("Verifies that a map expression is successful.")
  public void map() {
    String expression = "{\'name\' : \'Sarah\', \'surname\' : \'Manning\'}";
    Object result = expressionManager.evaluate(expression).getValue();
    assertThat(result, is(instanceOf(Map.class)));
    assertThat((Map<String, String>) result, hasEntry("name", "Sarah"));
    assertThat((Map<String, String>) result, hasEntry("surname", "Manning"));
  }

  @Test
  @Description("Verifies that custom variables are considered.")
  public void simpleCustomVariable() {
    Object object = new Object();
    BindingContext context = builder().addBinding(MY_VAR, new TypedValue(object, OBJECT)).build();
    assertThat(expressionManager.evaluate("#[myVar]", context).getValue(), equalTo(object));
  }

  @Test
  @Description("Verifies that the flow variable exposing it's name works.")
  public void flowName() throws ZernovaException {
    FlowConstruct mockFlowConstruct = mock(FlowConstruct.class);
    when(mockFlowConstruct.getName()).thenReturn("myFlowName");

    String result = (String) expressionManager
        .evaluate("#[flow.name]", testEvent(), fromSingleComponent(mockFlowConstruct.getName())).getValue();
    assertThat(result, is(mockFlowConstruct.getName()));
  }

  @Test
  @Description("Verifies that payload variable works.")
  public void payloadVariable() throws ZernovaException {
    assertThat(expressionManager.evaluate("payload", testEvent()).getValue(), is(TEST_PAYLOAD));
  }

  @Test
  @Description("Verifies that flowVars work, returning null for non existent ones and it's value for those that do.")
  public void flowVars() throws ZernovaException {
    CoreEvent.Builder eventBuilder = CoreEvent.builder(testEvent());
    String flowVars = "vars.myVar";
    assertThat(expressionManager.evaluate(flowVars, eventBuilder.build()).getValue(), nullValue());
    String value = "Leda";
    eventBuilder.addVariable(MY_VAR, value);
    assertThat(expressionManager.evaluate(flowVars, eventBuilder.build()).getValue(), is(value));
  }

  @Test
  @Description("Verifies that a simple transformation works.")
  public void transformation() throws ZernovaException {
    String expression = "payload";
    TypedValue result = expressionManager.evaluate(expression, BYTE_ARRAY, builder().build(), testEvent());
    assertThat(result.getValue(), is(TEST_PAYLOAD.getBytes()));
    assertThat(result.getDataType(), is(BYTE_ARRAY));
  }

  @Test
  @Description("Verifies that a simple transformation works even when it's not required.")
  public void transformationNotNeeded() throws ZernovaException {
    String expression = "payload";
    TypedValue result = expressionManager.evaluate(expression, STRING, builder().build(), testEvent());
    assertThat(result.getValue(), is(TEST_PAYLOAD));
    assertThat(result.getDataType(), is(STRING));
  }

  @Test
  @Description("Verifies that parsing works only with regular ones in DW.")
  public void parseCompatibility() throws ZernovaException {
    assertThat(expressionManager.parse("#['this is ' ++ payload]", testEvent(), TEST_CONNECTOR_LOCATION),
               is(format("this is %s", TEST_PAYLOAD)));
    expectedException.expect(RuntimeException.class);
    expressionManager.parse("this is #[payload]", testEvent(), TEST_CONNECTOR_LOCATION);
  }

  @Test
  @Description("Verifies that parsing works for plain String scenarios.")
  public void parse() throws ZernovaException {
    String expression = "this is a test";
    assertThat(expressionManager.parse(expression, testEvent(), TEST_CONNECTOR_LOCATION), is(expression));
  }

  @Test
  @Description("Verifies that parsing works for log template scenarios for DW.")
  public void parseLog() throws ZernovaException {
    assertThat(expressionManager.parseLogTemplate("this is #[payload]", testEvent(), TEST_CONNECTOR_LOCATION,
                                                  NULL_BINDING_CONTEXT),
               is(format("this is %s", TEST_PAYLOAD)));
  }

  @Test
  @Description("Verifies that parsing works for log template scenarios for DW using the message.")
  public void parseLogMessage() throws ZernovaException {
    String expectedOutput =
        "current message is \norg.mule.runtime.core.internal.message.DefaultMessageBuilder$MessageImplementation\n{"
            + "\n  payload=test\n  mediaType=*/*\n  attributes=<not set>\n  attributesMediaType=*/*\n}";
    assertThat(expressionManager.parseLogTemplate("current message is #[message]", testEvent(), TEST_CONNECTOR_LOCATION,
                                                  NULL_BINDING_CONTEXT),
               is(equalToIgnoringLineBreaks(expectedOutput)));
  }

  @Test
  @Description("Verifies that XML content can be used for logging in DW.")
  public void parseLogXml() throws ZernovaException {
    CoreEvent event = getEventBuilder().message(Message.builder().value("<?xml version='1.0' encoding='US-ASCII'?>\n"
        + "<wsc_fields>\n"
        + "  <operation>echo</operation>\n"
        + "  <body_test>test</body_test>\n"
        + "</wsc_fields>")
        .mediaType(XML)
        .build())
        .build();
    assertThat(expressionManager.parseLogTemplate("this is #[payload.wsc_fields.operation]", event, TEST_CONNECTOR_LOCATION,
                                                  NULL_BINDING_CONTEXT),
               is("this is \"echo\""));
  }

  @Test
  @Description("Verifies that JSON content can be used for logging in DW.")
  public void parseLogJsonWithEscapedStrings() throws ZernovaException {
    System.out.println("{\"key1\": \"{\\\"key1\\\": \\\"value1\\\"}\"}");

    CoreEvent event = getEventBuilder().message(Message.builder()
        .value("{\"key1\": \"{\\\"key1\\\": \\\"value1\\\"}\"}")
        .mediaType(MediaType.JSON)
        .build())
        .build();
    assertThat(expressionManager.parseLogTemplate("this is #[payload]", event, TEST_CONNECTOR_LOCATION,
                                                  NULL_BINDING_CONTEXT),
               is("this is {\"key1\": \"{\\\"key1\\\": \\\"value1\\\"}\"}"));
  }

  @Test
  @Description("Verifies that streams are logged in DW.")
  public void parseLogStream() throws ZernovaException {
    ByteArrayInputStream stream = new ByteArrayInputStream("hello".getBytes());
    CoreEvent event = getEventBuilder().message(Message.of(stream)).build();
    assertThat(expressionManager.parseLogTemplate("this is #[payload]", event, TEST_CONNECTOR_LOCATION,
                                                  NULL_BINDING_CONTEXT),
               is("this is hello"));
  }

  @Test
  public void isValid() {
    String expression = "2*2";
    assertThat(expressionManager.isValid(expression), is(true));
  }

  @Test
  public void isInvalid() {
    String expression = "2*'2";
    assertThat(expressionManager.isValid(expression), is(false));
  }

  @Test
  public void isExpression() {
    assertThat(expressionManager.isExpression("2*2 + #[var]"), is(true));
    assertThat(expressionManager.isExpression("#[var]"), is(true));
    assertThat(expressionManager.isExpression("${var}"), is(false));
  }

  @Test
  public void melPrefixIsValidDw() throws ZernovaException {
    assertThat(expressionManager.isValid("#[mel:1]"), is(true));
    expressionManager.evaluate("#[mel:1]");
  }

  @Test
  public void session() throws ZernovaException {
    Object object = new Object();
    BindingContext context = builder().addBinding(MY_VAR, new TypedValue(object, OBJECT)).build();

    ExpressionManagerSession session = expressionManager.openSession(context);

    assertThat(session.evaluate("#[myVar]").getValue(), equalTo(object));
  }
}
