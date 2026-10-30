/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.enricher;

import static com.zernova.runtime.api.meta.model.display.LayoutModel.builderFrom;
import static com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel.DEFAULT_GROUP_NAME;
import static com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase.LAYOUT;

import static java.lang.Integer.min;
import static java.util.Optional.of;

import com.zernova.runtime.api.meta.model.declaration.fluent.AbstractParameterDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ConfigurationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ConnectionProviderDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterGroupDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterizedDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.SourceDeclaration;
import com.zernova.runtime.api.meta.model.display.LayoutModel;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.extension.api.loader.DeclarationEnricherPhase;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.IdempotentDeclarationEnricherWalkDelegate;
import com.zernova.runtime.extension.api.loader.WalkingDeclarationEnricher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * {@link DeclarationEnricher} which walks through all the {@link ParameterDeclaration} of the entire extension and populates them
 * with the correspondent order.
 *
 * @since 1.5.0
 * @see LayoutModel
 */
public final class ParameterLayoutOrderDeclarationEnricher implements WalkingDeclarationEnricher {

  private static final int INITIAL_ORDER = 1;

  @Override
  public DeclarationEnricherPhase getExecutionPhase() {
    return LAYOUT;
  }

  @Override
  public Optional<DeclarationEnricherWalkDelegate> getWalkDelegate(ExtensionLoadingContext extensionLoadingContext) {
    return of(new IdempotentDeclarationEnricherWalkDelegate() {

      @Override
      public void onOperation(OperationDeclaration declaration) {
        establishOrder(declaration);
      }

      @Override
      public void onConfiguration(ConfigurationDeclaration declaration) {
        establishOrder(declaration);
      }

      @Override
      public void onSource(SourceDeclaration declaration) {
        establishOrder(declaration);
      }

      @Override
      protected void onConnectionProvider(ConnectionProviderDeclaration declaration) {
        establishOrder(declaration);
      }
    });
  }

  private void establishOrder(ParameterizedDeclaration declaration) {
    addMissingParameterOrders(declaration.getAllParameters());
    int minGroupOrder = addMissingParameterOrders(declaration.getParameterGroups());
    updateGeneralGroupOrder(declaration, minGroupOrder);
  }

  private void updateGeneralGroupOrder(ParameterizedDeclaration declaration, int minGroupOrder) {
    Optional<ParameterGroupDeclaration> general = declaration.getParameterGroups().stream()
        .filter(pg -> ((ParameterGroupDeclaration) pg).getName().equals(DEFAULT_GROUP_NAME)).findFirst();
    general.map(ParameterGroupDeclaration::getLayoutModel).flatMap(LayoutModel::getOrder).filter(order -> order > minGroupOrder)
        .ifPresent(order -> general.get()
            .setLayoutModel(builderFrom(general.get().getLayoutModel()).order(minGroupOrder - 1).build()));
  }

  private int addMissingParameterOrders(List<AbstractParameterDeclaration> declarations) {
    AtomicInteger minOrder = new AtomicInteger(INITIAL_ORDER);
    Set<Integer> takenOrders = new HashSet<>();
    List<AbstractParameterDeclaration> nonOrderedParams = new ArrayList<>();

    declarations.forEach(param -> {
      LayoutModel layoutModel = param.getLayoutModel();

      if (layoutModel != null && layoutModel.getOrder().isPresent()) {
        takenOrders.add(layoutModel.getOrder().get());
        minOrder.set(min(layoutModel.getOrder().get(), minOrder.get()));
      } else {
        nonOrderedParams.add(param);
      }

    });

    AtomicInteger currentOrder = new AtomicInteger(INITIAL_ORDER);
    nonOrderedParams.forEach(param -> param.setLayoutModel(
                                                           builderFrom(param.getLayoutModel())
                                                               .order(getNextOrder(currentOrder, takenOrders))
                                                               .build()));
    return minOrder.get();
  }

  private int getNextOrder(AtomicInteger init, Set<Integer> takenOrders) {
    while (takenOrders.contains(init.get())) {
      init.incrementAndGet();
    }
    return init.getAndIncrement();
  }
}
