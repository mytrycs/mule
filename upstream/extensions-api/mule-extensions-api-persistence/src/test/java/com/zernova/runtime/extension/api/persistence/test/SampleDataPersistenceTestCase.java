/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.persistence.test;

import static java.util.Arrays.asList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.zernova.runtime.api.meta.model.data.sample.SampleDataProviderModel;
import com.zernova.runtime.api.meta.model.parameter.ActingParameterModel;
import com.zernova.runtime.extension.api.model.parameter.ImmutableActingParameterModel;
import com.zernova.runtime.extension.internal.persistence.DefaultImplementationTypeAdapterFactory;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;

import org.junit.Test;

public class SampleDataPersistenceTestCase {

  private static final SampleDataProviderModel SAMPLE_DATA_PERSISTENCE_MODEL =
      new SampleDataProviderModel(asList(buildActingParameterModel("param1", true), buildActingParameterModel("param2", true),
                                         buildActingParameterModel("param3", false)),
                                  "sample data", true, true);

  private final JsonParser jsonParser = new JsonParser();
  private final Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(new DefaultImplementationTypeAdapterFactory<>(ActingParameterModel.class,
                                                                                ImmutableActingParameterModel.class))
      .create();

  @Test
  public void serializeSampleDataProvider() throws IOException {
    JsonElement serialized = gson.toJsonTree(SAMPLE_DATA_PERSISTENCE_MODEL);
    JsonElement expected = loadAsJson("/data/sample/sample-data-provider-model.json");
    assertThat(serialized, is(expected));
  }

  @Test
  public void deserializedPartModelProperty() throws IOException {
    SampleDataProviderModel model =
        gson.fromJson(loadAsJson("/data/sample/sample-data-provider-model.json"), SampleDataProviderModel.class);
    assertThat(model, equalTo(SAMPLE_DATA_PERSISTENCE_MODEL));
  }

  private JsonElement loadAsJson(String name) throws IOException {
    return jsonParser.parse(loadAsString(name));
  }

  private String loadAsString(String name) throws IOException {
    return IOUtils.toString(this.getClass().getResourceAsStream(name));
  }

  private static ActingParameterModel buildActingParameterModel(String name, boolean required) {
    return new ImmutableActingParameterModel(name, required);
  }
}
