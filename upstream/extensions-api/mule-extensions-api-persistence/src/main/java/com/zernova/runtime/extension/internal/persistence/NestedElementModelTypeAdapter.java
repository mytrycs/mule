/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.persistence;

import static java.lang.String.format;

import com.zernova.runtime.api.meta.model.nested.NestableElementModel;
import com.zernova.runtime.api.meta.model.nested.NestableElementModelVisitor;
import com.zernova.runtime.api.meta.model.nested.NestedChainModel;
import com.zernova.runtime.api.meta.model.nested.NestedComponentModel;
import com.zernova.runtime.api.meta.model.nested.NestedRouteModel;
import com.zernova.runtime.api.util.Reference;
import com.zernova.runtime.extension.api.model.nested.ImmutableNestedChainModel;
import com.zernova.runtime.extension.api.model.nested.ImmutableNestedComponentModel;
import com.zernova.runtime.extension.api.model.nested.ImmutableNestedRouteModel;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * A {@link TypeAdapter} for serializing instances of {@link NestableElementModel} and all its child interfaces
 *
 * @since 1.0
 */
public class NestedElementModelTypeAdapter extends KindEnrichedTypeAdapter<NestableElementModel> {

  private static final String COMPONENT_KIND = "component";
  private static final String CHAIN_KIND = "chain";
  private static final String ROUTE_KIND = "route";

  public NestedElementModelTypeAdapter(TypeAdapterFactory typeAdapterFactory, Gson gson) {
    super(typeAdapterFactory, gson);
  }

  @Override
  protected String getKind(NestableElementModel value) {
    Reference<String> kind = new Reference<>();
    value.accept(new NestableElementModelVisitor() {

      @Override
      public void visit(NestedComponentModel component) {
        kind.set(COMPONENT_KIND);
      }

      @Override
      public void visit(NestedChainModel component) {
        kind.set(CHAIN_KIND);
      }

      @Override
      public void visit(NestedRouteModel component) {
        kind.set(ROUTE_KIND);
      }
    });

    return kind.get();
  }

  @Override
  protected TypeAdapter<NestableElementModel> getDelegateAdapter(String kind) {
    Class clazz;
    switch (kind) {
      case ROUTE_KIND:
        TypeAdapter<NestableElementModel> delegateAdapter =
            (TypeAdapter) gson.getDelegateAdapter(typeAdapterFactory, TypeToken.get(ImmutableNestedRouteModel.class));
        return new LegacyNestedRouteModelTypeAdapter(delegateAdapter, gson);
      case CHAIN_KIND:
        clazz = ImmutableNestedChainModel.class;
        break;
      case COMPONENT_KIND:
        clazz = ImmutableNestedComponentModel.class;
        break;
      default:
        throw new IllegalArgumentException(format("Unknown kind [%s] for a NestedElementModel", kind));
    }

    return new LegacyNestedElementModelTypeAdapter(gson.getDelegateAdapter(typeAdapterFactory, TypeToken.get(clazz)));
  }
}
