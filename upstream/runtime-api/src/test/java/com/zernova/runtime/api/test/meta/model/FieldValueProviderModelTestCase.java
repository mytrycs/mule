/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.test.meta.model;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.zernova.runtime.api.meta.model.parameter.ActingParameterModel;
import com.zernova.runtime.api.meta.model.parameter.FieldValueProviderModel;
import com.zernova.runtime.api.meta.model.parameter.ValueProviderModel;

import java.util.List;

import org.junit.Test;

public class FieldValueProviderModelTestCase {

  @Test
  public void testEqualsFieldValueProviderModel() {
    List<ActingParameterModel> actingParameterModels = singletonList(new ActingParameterModelImp());
    FieldValueProviderModel thisFieldValueProviderModel = new FieldValueProviderModel(
                                                                                      actingParameterModels, true, true, true, 1,
                                                                                      "providerName", "providerId", "targetPath");

    FieldValueProviderModel thatFieldValueProviderModel = new FieldValueProviderModel(
                                                                                      actingParameterModels, true, true, true, 1,
                                                                                      "providerName", "providerId", "targetPath");

    assertThat(thisFieldValueProviderModel.equals(thatFieldValueProviderModel), is(true));
  }

  @Test
  public void testNotEqualsFieldValueProviderModel() {
    List<ActingParameterModel> actingParameterModels = singletonList(new ActingParameterModelImp());
    FieldValueProviderModel thisFieldValueProviderModel = new FieldValueProviderModel(
                                                                                      actingParameterModels, true, true, true, 1,
                                                                                      "providerName", "providerId", "targetPath");

    FieldValueProviderModel thatFieldValueProviderModel = new FieldValueProviderModel(
                                                                                      actingParameterModels, false, false, false,
                                                                                      1, "providerName", "providerId",
                                                                                      "targetPath");

    assertThat(thisFieldValueProviderModel.equals(thatFieldValueProviderModel), is(false));
  }

  @Test
  public void testNotEqualsFieldValueProviderModelAndValueProviderModel() {
    List<ActingParameterModel> actingParameterModels1 = singletonList(new ActingParameterModelImp());
    FieldValueProviderModel thisFieldValueProviderModel = new FieldValueProviderModel(
                                                                                      actingParameterModels1, true, true, true, 1,
                                                                                      "providerName", "providerId", "targetPath");

    List<ActingParameterModel> actingParameterModels2 = singletonList(new ActingParameterModelImp());
    ValueProviderModel valueProviderModel =
        new ValueProviderModel(actingParameterModels2, true, true, true, 1, "providerId", "targetPath");

    assertThat(valueProviderModel.equals(thisFieldValueProviderModel), is(false));
  }


  private static class ActingParameterModelImp implements ActingParameterModel {

    @Override
    public String getName() {
      return "ActingParameterModel Test Implementation";
    }

    @Override
    public boolean isRequired() {
      return false;
    }

    @Override
    public String getExtractionExpression() {
      return "Extraction Expression";
    }
  }

}
