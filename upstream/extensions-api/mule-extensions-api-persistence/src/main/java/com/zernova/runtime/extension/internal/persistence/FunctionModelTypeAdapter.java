/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.persistence;

import com.zernova.runtime.api.meta.model.function.FunctionModel;
import com.zernova.runtime.extension.api.model.function.ImmutableFunctionModel;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * A {@link TypeAdapter} for serializing instances of {@link FunctionModel} and all its child interfaces
 *
 * @since 1.0
 */
public class FunctionModelTypeAdapter extends KindEnrichedTypeAdapter<FunctionModel> {

  public FunctionModelTypeAdapter(TypeAdapterFactory typeAdapterFactory, Gson gson) {
    super(typeAdapterFactory, gson);
  }

  @Override
  protected String getKind(FunctionModel value) {
    return "function";
  }

  @Override
  protected TypeAdapter<FunctionModel> getDelegateAdapter(String kind) {
    Class functionModelClass = ImmutableFunctionModel.class;
    return gson.getDelegateAdapter(typeAdapterFactory, TypeToken.get(functionModelClass));
  }
}
