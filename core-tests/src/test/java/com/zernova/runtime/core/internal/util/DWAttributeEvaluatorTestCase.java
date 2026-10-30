/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util;

import static com.zernova.runtime.api.metadata.DataType.BOOLEAN;
import static com.zernova.runtime.api.metadata.DataType.INPUT_STREAM;
import static com.zernova.runtime.api.metadata.DataType.NUMBER;
import static com.zernova.runtime.api.metadata.DataType.OBJECT;
import static com.zernova.runtime.api.metadata.DataType.STRING;
import static com.zernova.runtime.api.metadata.MediaType.APPLICATION_JAVA;
import static com.zernova.runtime.api.metadata.MediaType.APPLICATION_JSON;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.mock;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.MediaType;
import com.zernova.runtime.api.streaming.CursorProvider;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.expression.ExpressionRuntimeException;
import com.zernova.runtime.core.api.util.IOUtils;
import com.zernova.runtime.core.internal.util.attribute.AttributeEvaluator;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.size.SmallTest;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@SmallTest
public class DWAttributeEvaluatorTestCase extends AbstractZernovaContextTestCase {

  private static final String HOST_PORT_JSON = "{\"host\":\"0.0.0.0\", \"port\" : 8081}";
  private static final String JSON_CAR = "{\n  \"color\": \"RED\",\n  \"price\": 1000\n}";
  private static final String DW_CAR = "#[{color : 'RED', price: 1000}]";
  private static final String DW_CAR_LIST = "#[[{color : 'RED', price: 1000}]]";
  private static final DataType CAR_DATA_TYPE = DataType.fromType(Car.class);
  private static final DataType CAR_LIST_DATA_TYPE = DataType.builder().collectionType(List.class).itemType(Car.class).build();

  @Rule
  public MockitoRule mockitorule = MockitoJUnit.rule();

  private final CoreEvent mockMuleEvent = mock(CoreEvent.class);
  private ExtendedExpressionManager expressionManager;

  @Before
  public void setUp() throws ZernovaException {
    expressionManager = muleContext.getExpressionManager();
  }

  @Test
  public void plainTextValue() {
    String staticValue = "attributeEvaluator";
    AttributeEvaluator attributeEvaluator = getAttributeEvaluator(staticValue);
    assertThat(attributeEvaluator.resolveValue(mockMuleEvent), is(staticValue));
  }

  @Test
  public void getJavaStringFromIntJsonProperty() throws ZernovaException {
    AttributeEvaluator attributeEvaluator = getAttributeEvaluator("#[payload.port]", STRING);
    CoreEvent event = newEvent(HOST_PORT_JSON, APPLICATION_JSON);
    Object port = attributeEvaluator.resolveValue(event);
    assertThat(port, is("8081"));
  }

  @Test
  public void getJavaIntFromIntJsonProperty() throws ZernovaException {
    AttributeEvaluator attributeEvaluator = getAttributeEvaluator("#[payload.port]", NUMBER);
    CoreEvent event = newEvent(HOST_PORT_JSON, APPLICATION_JSON);
    Object port = attributeEvaluator.resolveValue(event);
    assertThat(port, is(8081));
  }

  @Test
  public void getJavaStringFromStringJsonProperty() throws ZernovaException {
    AttributeEvaluator attributeEvaluator = getAttributeEvaluator("#[payload.host]", STRING);
    CoreEvent event = newEvent(HOST_PORT_JSON, APPLICATION_JSON);
    Object host = attributeEvaluator.resolveValue(event);
    assertThat(host, is("0.0.0.0"));
  }

  @Test
  public void getJavaObjectFromStringJsonProperty() throws ZernovaException {
    AttributeEvaluator attributeEvaluator = getAttributeEvaluator("#[payload.host]", OBJECT);
    CoreEvent event = newEvent(HOST_PORT_JSON, APPLICATION_JSON);
    Object resolveValue = attributeEvaluator.resolveValue(event);
    assertThat(IOUtils.toString((InputStream) ((CursorProvider) resolveValue).openCursor()), is("\"0.0.0.0\""));
  }

  @Test
  public void getJavaInputStreamFromStringJsonProperty() throws ZernovaException {
    AttributeEvaluator attributeEvaluator = getAttributeEvaluator("#[payload.host]", INPUT_STREAM);
    CoreEvent event = newEvent(HOST_PORT_JSON, APPLICATION_JSON);
    Object resolveValue = attributeEvaluator.resolveValue(event);
    assertThat(IOUtils.toString((InputStream) ((CursorProvider) resolveValue).openCursor()), is("\"0.0.0.0\""));
  }

  @Test
  public void getJavaPojo() throws ZernovaException {
    AttributeEvaluator attributeEvaluator =
        getAttributeEvaluator(DW_CAR, DataType.fromType(Car.class));
    Object car = attributeEvaluator.resolveValue(newEvent());
    assertThat(car, is(allOf(hasProperty("color", is("RED")), hasProperty("price", is(1000)))));
  }

  @Test
  public void getJavaCarFromJsonCar() throws ZernovaException {
    AttributeEvaluator attributeEvaluator = getAttributeEvaluator("#[payload]", CAR_DATA_TYPE);
    Object car = attributeEvaluator.resolveValue(newEvent(JSON_CAR, APPLICATION_JSON));
    assertThat(car, is(allOf(hasProperty("color", is("RED")), hasProperty("price", is(1000)))));
  }

  @Test
  public void getMapFromJsonCar() throws ZernovaException {
    AttributeEvaluator attributeEvaluator = getAttributeEvaluator("#[payload]", DataType.fromType(Map.class));
    Map<String, String> car = attributeEvaluator.resolveValue(newEvent(JSON_CAR, APPLICATION_JSON));
    assertThat(car, hasEntry(is("price"), is(1000)));
    assertThat(car, hasEntry(is("color"), is("RED")));
  }

  @Test
  public void getListOfCarsFromJsonCar() throws ZernovaException {
    AttributeEvaluator attributeEvaluator =
        getAttributeEvaluator("#[[payload]]", CAR_LIST_DATA_TYPE);
    List<Car> cars = attributeEvaluator.resolveValue(newEvent(JSON_CAR, APPLICATION_JSON));
    Car car = cars.get(0);
    assertThat(car, is(allOf(hasProperty("color", is("RED")), hasProperty("price", is(1000)))));
  }

  @Test
  public void getListOfCarsFromExpression() throws ZernovaException {
    AttributeEvaluator attributeEvaluator =
        getAttributeEvaluator(DW_CAR_LIST, CAR_LIST_DATA_TYPE);
    List<Car> cars = attributeEvaluator.resolveValue(newEvent(JSON_CAR, APPLICATION_JSON));
    Car car = cars.get(0);
    assertThat(car, is(allOf(hasProperty("color", is("RED")), hasProperty("price", is(1000)))));
  }

  @Test
  public void getListOfMapsFromJsonCar() throws ZernovaException {
    AttributeEvaluator attributeEvaluator = getAttributeEvaluator("#[[payload as Object]]", DataType.fromType(List.class));
    List<Map<String, String>> cars = attributeEvaluator.resolveValue(newEvent(JSON_CAR, APPLICATION_JSON));
    Map<String, String> car = cars.get(0);
    assertThat(car, hasEntry(is("price"), is(1000)));
    assertThat(car, hasEntry(is("color"), is("RED")));
  }

  @Test(expected = ExpressionRuntimeException.class)
  public void parseExpressionAreNotSupported() throws ZernovaException {
    AttributeEvaluator attributeEvaluator = getAttributeEvaluator("invalid #['expression']");
    attributeEvaluator.resolveValue(newEvent());
  }

  @Test
  public void resolveIntegerValueFromJsonObject() throws ZernovaException {
    AttributeEvaluator attributeEvaluator = getAttributeEvaluator("#[payload.port]", NUMBER);
    Integer port = attributeEvaluator.resolveValue(newEvent(HOST_PORT_JSON, APPLICATION_JSON));
    assertThat(port, is(8081));
  }

  @Test
  public void resolveIntegerValueFromJavaString() throws ZernovaException {
    AttributeEvaluator attributeEvaluator = getAttributeEvaluator("#[payload]", DataType.NUMBER);
    Object port = attributeEvaluator.resolveValue(newEvent("12", APPLICATION_JAVA));
    assertThat(port, is(12));
  }

  @Test
  public void resolveStringValue() throws ZernovaException {
    AttributeEvaluator attributeEvaluator = getAttributeEvaluator("#[payload.port]", STRING);
    String port = attributeEvaluator.resolveValue(newEvent(HOST_PORT_JSON, APPLICATION_JSON));
    assertThat(port, is("8081"));
  }

  @Test
  public void getBooleanValue() throws ZernovaException {
    AttributeEvaluator attributeEvaluator = getAttributeEvaluator("#[payload.ok]", BOOLEAN);
    Boolean bool = attributeEvaluator.resolveValue(newEvent("{\"ok\" : true}", APPLICATION_JSON));
    assertThat(bool, is(true));
  }

  private CoreEvent newEvent(Object payload, MediaType applicationJson) throws ZernovaException {
    return CoreEvent.builder(this.<CoreEvent>newEvent())
        .message(Message.builder()
            .value(payload)
            .mediaType(applicationJson)
            .build())
        .build();
  }

  private AttributeEvaluator getAttributeEvaluator(String expression) {
    return getAttributeEvaluator(expression, null);
  }

  private AttributeEvaluator getAttributeEvaluator(String expression, DataType expectedDataType) {
    AttributeEvaluator attributeEvaluator = new AttributeEvaluator(expression, expectedDataType);
    attributeEvaluator.initialize(expressionManager);
    return attributeEvaluator;
  }
}
