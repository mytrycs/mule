/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension;

import static java.lang.Thread.currentThread;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.APPLICATION_CSV;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.APPLICATION_XML;

import com.zernova.runtime.core.api.util.IOUtils;
import com.zernova.runtime.extension.api.annotation.metadata.OutputResolver;
import com.zernova.runtime.extension.api.annotation.metadata.TypeResolver;
import com.zernova.runtime.extension.api.annotation.metadata.fixed.AttributesJsonType;
import com.zernova.runtime.extension.api.annotation.metadata.fixed.InputJsonType;
import com.zernova.runtime.extension.api.annotation.metadata.fixed.OutputJsonType;
import com.zernova.runtime.extension.api.annotation.metadata.fixed.OutputXmlType;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.streaming.PagingProvider;
import com.zernova.sdk.api.annotation.metadata.fixed.AttributesXmlType;
import com.zernova.sdk.api.annotation.metadata.fixed.InputXmlType;
import com.zernova.tck.testmodels.fruit.Banana;
import com.zernova.test.metadata.extension.resolver.CsvInputStaticTypeResolver;
import com.zernova.test.metadata.extension.resolver.JavaOutputStaticTypeResolver;
import com.zernova.test.metadata.extension.resolver.JsonInputStaticIntersectionTypeResolver;
import com.zernova.test.metadata.extension.resolver.JsonInputStaticTypeResolver;
import com.zernova.test.metadata.extension.resolver.JsonOutputStaticIntersectionTypeResolver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomStaticMetadataOperations {

  public static final String CSV_VALUE = "Name,LastName\\njuan,desimoni\\nesteban,wasinger";
  public static final String JSON_VALUE = "{\"age\":12,\"dni\": 1478231}";
  public static final String JSON_ARRAY_VALUE = "[{\"age\":12,\"dni\": 1478231}, {\"age\":25,\"dni\": 37562148}]";
  private static final ClassLoader cl = currentThread().getContextClassLoader();
  public static final String XML_VALUE = IOUtils.toString(cl.getResourceAsStream("order.xml"));

  @OutputXmlType(schema = "order.xsd", qname = "shiporder")
  @MediaType(value = APPLICATION_XML, strict = false)
  public InputStream xmlOutput() {
    return cl.getResourceAsStream("order.xml");
  }

  @MediaType(value = APPLICATION_XML, strict = false)
  @com.zernova.sdk.api.annotation.metadata.fixed.OutputXmlType(schema = "orderWithImport.xsd", qname = "shiporder")
  public InputStream xmlOutputSchemaWithImport() {
    return cl.getResourceAsStream("order.xml");
  }

  @com.zernova.sdk.api.annotation.metadata.fixed.OutputXmlType(schema = "order.xsd", qname = "shiporder")
  public List<InputStream> xmlOutputList() {
    ArrayList xmlList = new ArrayList();
    xmlList.add(cl.getResourceAsStream("order.xml"));
    xmlList.add(cl.getResourceAsStream("order.xml"));
    return xmlList;
  }

  @OutputXmlType(schema = "order.xsd", qname = "shiporder")
  @MediaType(value = APPLICATION_XML, strict = false)
  public String xmlInput(@InputXmlType(schema = "order.xsd", qname = "shiporder") InputStream xml) {
    return XML_VALUE;
  }

  @OutputJsonType(schema = "person-schema.json")
  @MediaType(value = APPLICATION_JSON, strict = false)
  public InputStream jsonOutput() {
    return new ByteArrayInputStream(JSON_VALUE.getBytes());
  }

  @MediaType(value = APPLICATION_JSON, strict = false)
  @com.zernova.sdk.api.annotation.metadata.fixed.OutputJsonType(schema = "persons-schema.json")
  public InputStream jsonOutputArray() {
    return new ByteArrayInputStream(JSON_ARRAY_VALUE.getBytes());
  }

  @OutputJsonType(schema = "person-schema.json")
  public List<String> jsonOutputList() {
    return new ArrayList<>();
  }

  @com.zernova.sdk.api.annotation.metadata.fixed.OutputJsonType(schema = "person-schema.json")
  public PagingProvider<MetadataConnection, String> jsonOutputPagingProvider() {
    return new PagingProvider<MetadataConnection, String>() {

      @Override
      public List<String> getPage(MetadataConnection connection) {
        ArrayList jsonArrayList = new ArrayList<>();
        jsonArrayList.add(JSON_VALUE);
        jsonArrayList.add(JSON_VALUE);
        return jsonArrayList;
      }

      @Override
      public java.util.Optional<Integer> getTotalResults(MetadataConnection connection) {
        return java.util.Optional.empty();
      }

      @Override
      public void close(MetadataConnection connection) {

      }
    };
  }

  @OutputJsonType(schema = "persons-schema.json")
  public List<String> jsonArrayOutputList() {
    ArrayList jsonArrayList = new ArrayList();
    jsonArrayList.add(JSON_ARRAY_VALUE);
    jsonArrayList.add(JSON_ARRAY_VALUE);
    return jsonArrayList;
  }

  @MediaType(value = APPLICATION_JSON)
  public String jsonInputStream(@com.zernova.sdk.api.annotation.metadata.fixed.InputJsonType(
      schema = "person-schema.json") InputStream json) {
    return IOUtils.toString(json);
  }

  public List<InputStream> jsonInputList(@com.zernova.sdk.api.annotation.metadata.fixed.InputJsonType(
      schema = "person-schema.json") List<InputStream> persons) {
    return persons;
  }

  @OutputResolver(output = CsvInputStaticTypeResolver.class)
  public List<Object> customTypeListOutput() {
    ArrayList<Object> csvList = new ArrayList();
    csvList.add(CSV_VALUE);
    csvList.add(CSV_VALUE);
    return csvList;
  }

  @MediaType(value = APPLICATION_JSON)
  public int jsonInputMap(@InputJsonType(schema = "person-schema.json") Map<String, Object> json) {
    return (int) json.get("age");
  }

  @OutputJsonType(schema = "person-schema.json")
  @MediaType(value = APPLICATION_JSON, strict = false)
  public Result<InputStream, Banana> customTypeOutputWithStaticAttributes() {
    return Result.<InputStream, Banana>builder()
        .output(new ByteArrayInputStream(new byte[] {}))
        .build();
  }

  @OutputResolver(output = CsvInputStaticTypeResolver.class)
  @MediaType(value = APPLICATION_CSV, strict = false)
  public Object customTypeOutput() {
    return CSV_VALUE;
  }

  @MediaType(APPLICATION_JSON)
  public String customTypeInput(@TypeResolver(JsonInputStaticTypeResolver.class) InputStream type) {
    return IOUtils.toString(type);
  }

  @MediaType(APPLICATION_JSON)
  public String customIntersectionTypeInput(@com.zernova.sdk.api.annotation.metadata.TypeResolver(JsonInputStaticIntersectionTypeResolver.class) InputStream data) {
    return IOUtils.toString(data);
  }

  @MediaType(value = APPLICATION_JSON, strict = false)
  @OutputResolver(output = JsonOutputStaticIntersectionTypeResolver.class)
  public String customIntersectionTypeOutput() {
    return JSON_VALUE;
  }

  @AttributesXmlType(schema = "order.xsd", qname = "shiporder")
  public Result<Integer, InputStream> xmlAttributes() {
    return Result.<Integer, InputStream>builder().output(1).build();
  }

  @OutputResolver(output = CsvInputStaticTypeResolver.class, attributes = JsonStaticAttributesTypeResolver.class)
  public Result<Integer, InputStream> customAttributesOutput() {
    return Result.<Integer, InputStream>builder().output(1).build();
  }

  @com.zernova.sdk.api.annotation.metadata.fixed.AttributesJsonType(schema = "person-schema.json")
  public Result<Integer, InputStream> jsonAttributes() {
    return Result.<Integer, InputStream>builder().output(1).build();
  }

  @com.zernova.sdk.api.annotation.metadata.fixed.AttributesJsonType(schema = "persons-schema.json")
  public Result<Integer, InputStream> jsonArrayAttributes() {
    return Result.<Integer, InputStream>builder().output(1).build();
  }

  @AttributesJsonType(schema = "person-schema.json")
  public Result<Integer, List<InputStream>> jsonAttributesList() {
    return Result.<Integer, List<InputStream>>builder().output(1).build();
  }

  @AttributesJsonType(schema = "persons-schema.json")
  public Result<Integer, ArrayList<InputStream>> jsonArrayAttributesList() {
    return Result.<Integer, ArrayList<InputStream>>builder().output(1).build();
  }

  @AttributesJsonType(schema = "person-schema.json")
  public PagingProvider<MetadataConnection, Result<Integer, InputStream>> jsonAttributesPagingProviderWithResult() {
    return new PagingProvider<MetadataConnection, Result<Integer, InputStream>>() {

      @Override
      public List<Result<Integer, InputStream>> getPage(MetadataConnection connection) {
        return new ArrayList<>();
      }

      @Override
      public java.util.Optional<Integer> getTotalResults(MetadataConnection connection) {
        return java.util.Optional.empty();
      }

      @Override
      public void close(MetadataConnection connection) {

      }
    };
  }

  @OutputXmlType(schema = "", qname = "shiporder")
  @MediaType(value = APPLICATION_XML)
  public List<InputStream> xmlOutputListWithEmptySchema() {
    ArrayList<InputStream> xmlList = new ArrayList();
    xmlList.add(cl.getResourceAsStream("order.xml"));
    xmlList.add(cl.getResourceAsStream("order.xml"));
    return xmlList;
  }

  @OutputResolver(output = JavaOutputStaticTypeResolver.class)
  @MediaType(value = "application/java", strict = false)
  public Object customInputAndOutput(@TypeResolver(JsonInputStaticTypeResolver.class) InputStream type) {
    return null;
  }
}
