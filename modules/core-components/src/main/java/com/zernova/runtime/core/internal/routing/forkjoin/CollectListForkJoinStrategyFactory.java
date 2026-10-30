/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing.forkjoin;

import static com.zernova.runtime.api.component.AbstractComponent.LOCATION_KEY;
import static com.zernova.runtime.api.component.AbstractComponent.ROOT_CONTAINER_NAME_KEY;
import static com.zernova.runtime.api.message.Message.of;
import static com.zernova.runtime.api.metadata.DataType.MULE_MESSAGE_LIST;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.internal.routing.ForkJoinStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jakarta.inject.Inject;

import javax.xml.namespace.QName;

/**
 * {@link ForkJoinStrategy} that:
 * <ul>
 * <li>Performs parallel execution of route pairs subject to {@code maxConcurrency}.
 * <li>Merges variables using a last-wins strategy.
 * <li>Waits for the completion of all routes before emitting a result event, with an optional timeout.
 * <li>Collects results into a result {@link CoreEvent} with a {@link List} collection that contains
 * {@link com.zernova.runtime.api.message.Message} in the payload.
 * <li>Will processor all routes, regardless of errors, and propagating a composite exception where there were one or more errors.
 * </ul>
 */
public class CollectListForkJoinStrategyFactory extends AbstractForkJoinStrategyFactory implements Component {

  @Inject
  public CollectListForkJoinStrategyFactory(FeatureFlaggingService featureFlaggingService) {
    super(featureFlaggingService);
  }

  public CollectListForkJoinStrategyFactory(boolean mergeVariables, FeatureFlaggingService featureFlaggingService) {
    super(mergeVariables, featureFlaggingService);
  }

  @Override
  protected Function<List<CoreEvent>, CoreEvent> createResultEvent(CoreEvent original,
                                                                   CoreEvent.Builder resultBuilder) {
    return list -> resultBuilder.message(of(list.stream().map(event -> event.getMessage()).collect(toList()))).build();
  }

  @Override
  public DataType getResultDataType() {
    return MULE_MESSAGE_LIST;
  }

  //////////////////
  // From AbstractComponent
  //////////////////

  private volatile Map<QName, Object> annotations = emptyMap();

  private ComponentLocation location;
  private Location rootContainerLocation;

  @Override
  public Object getAnnotation(QName qName) {
    return annotations.get(qName);
  }

  @Override
  public Map<QName, Object> getAnnotations() {
    return unmodifiableMap(annotations);
  }

  @Override
  public void setAnnotations(Map<QName, Object> newAnnotations) {
    annotations = new HashMap<>(newAnnotations);
    location = (ComponentLocation) getAnnotation(LOCATION_KEY);
    rootContainerLocation = initRootContainerName();
  }

  protected Location initRootContainerName() {
    String rootContainerName = (String) getAnnotation(ROOT_CONTAINER_NAME_KEY);
    if (rootContainerName == null && getLocation() != null) {
      rootContainerName = getLocation().getRootContainerName();
    }
    return rootContainerName == null ? null : Location.builder().globalName(rootContainerName).build();
  }

  @Override
  public ComponentLocation getLocation() {
    return location;
  }

  @Override
  public Location getRootContainerLocation() {
    return rootContainerLocation;
  }

}
