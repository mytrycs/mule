/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.function.extension;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.extension.api.annotation.param.Optional.PAYLOAD;

import static com.google.common.collect.Lists.partition;

import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.Ignore;
import com.zernova.runtime.extension.api.annotation.deprecated.Deprecated;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.test.heisenberg.extension.model.KnockeableDoor;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;

import com.google.common.collect.ImmutableMap;

import org.w3c.dom.Document;

public class GlobalWeaveFunction implements Initialisable {

  private XPathFactory xPathFactory;

  @Inject
  ExpressionManager manager;

  @Override
  public void initialise() throws InitialisationException {
    if (xPathFactory == null) {
      xPathFactory = XPathFactory.newInstance();
    }
    if (manager == null) {
      throw new InitialisationException(createStaticMessage("Expected ExpressionManager to be injected but was null"), this);
    }
  }

  public String customEcho(String echo) {
    return echo;
  }

  public TypedValue<InputStream> typedInputStream(@Optional TypedValue<InputStream> stream) {
    return stream;
  }

  public List<Object> withTypedValues(@Alias("notObject") @Optional TypedValue<Object> object,
                                      @com.zernova.sdk.api.annotation.param.Optional TypedValue<String> string,
                                      @Optional TypedValue<InputStream> stream,
                                      @Optional TypedValue<KnockeableDoor> door) {
    LinkedList<Object> values = new LinkedList<>();
    values.add(object);
    values.add(string);
    values.add(stream);
    values.add(door);
    return values;
  }

  public String defaultPrimitives(@Optional boolean bool, @Optional int number,
                                  @Optional double doubles, @Optional float floats,
                                  @Optional long longs, @Optional short shorts) {
    return "SUCCESS";
  }

  public Object echoWithDefault(@Optional(defaultValue = PAYLOAD) Object payload,
                                @Optional(defaultValue = "prefix_") String prefix) {
    return prefix.concat(String.valueOf(payload));
  }

  public Map<String, String> toMap(SimplePojo pojo) {
    return ImmutableMap.of("user", pojo.getUser(), "pass", pojo.getPass());
  }

  @Deprecated(message = "The case of use of this function is covered within the echoWithDefault function", since = "2.2.0",
      toRemoveIn = "3.0.0")
  public Object xpath(String expression,
                      @Optional(defaultValue = PAYLOAD) InputStream item,
                      @Optional String returnType) {
    try {

      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(item);
      if (returnType == null) {
        return xPathFactory.newXPath().evaluate(expression, document);
      } else {
        return xPathFactory.newXPath().evaluate(expression, document, asQname(returnType));
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Alias("partition")
  public List<List<Object>> aliasedFunction(List<Object> listToSplit, int groupSize) {
    return partition(listToSplit, groupSize);
  }

  @Ignore
  public String invalidIgnoredFunction(@Config String theConfiguration) {
    return null;
  }

  private QName asQname(String name) {
    return new QName("http://www.w3.org/1999/XSL/Transform", name.toUpperCase());
  }

}
