/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.marvel.drstrange;

import static com.zernova.runtime.api.component.location.Location.builderFromStringRepresentation;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.scheduler.Scheduler;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.stereotype.AllowedStereotypes;
import com.zernova.runtime.extension.api.annotation.param.stereotype.ComponentId;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import jakarta.inject.Inject;

import javax.xml.namespace.QName;

@Alias("bytes-caster")
@MediaType(TEXT_PLAIN)
public class DrStrangeBytesSource extends Source<InputStream, Void> {

  @Inject
  private SchedulerService schedulerService;
  private Scheduler scheduler;
  private ScheduledFuture sourceCallbackHandleTask;

  @Inject
  private ConfigurationComponentLocator locator;

  @Parameter
  private long castFrequencyInMillis;

  @Parameter
  private String spell;

  @Parameter
  @Optional(defaultValue = "1")
  private int spellSize;

  @Parameter
  @com.zernova.sdk.api.annotation.param.Optional
  @AllowedStereotypes(ReferableOperationStereotypeDefinition.class)
  private String nextOperationReference;

  @Parameter
  @ComponentId
  private String listenerId;

  @Config
  private DrStrange config;


  private ComponentLocation location;
  private QName ANNOTATION_PARAMETERS = new QName("config", "componentParameters");

  @Override
  public void onStart(SourceCallback<InputStream, Void> sourceCallback) throws ZernovaException {
    if (nextOperationReference != null) {
      Component proc = locator.find(builderFromStringRepresentation(location.getParts().get(0).getPartPath()).addProcessorsPart()
          .addIndexPart(0).build())
          .orElseThrow(() -> new IllegalArgumentException("Missing processor after this source"));

      Map<String, Object> parameters = (Map<String, Object>) proc.getAnnotation(ANNOTATION_PARAMETERS);
      if (!parameters.get("name").equals(nextOperationReference)) {
        throw new IllegalArgumentException(String.format("Next processor %s does not match the expected operation %s",
                                                         parameters.get("name"), nextOperationReference));
      }
    }

    scheduler = schedulerService.cpuLightScheduler();
    sourceCallbackHandleTask = scheduler.scheduleAtFixedRate(() -> sourceCallback.handle(Result.<InputStream, Void>builder()
        .output(new ByteArrayInputStream(getSpellBytes(spell)))
        .build()), 0, castFrequencyInMillis, MILLISECONDS);
  }

  private byte[] getSpellBytes(String spell) {
    while (spell.length() < spellSize) {
      spell += spell;
    }
    return spell.getBytes();
  }

  @Override
  public void onStop() {
    if (sourceCallbackHandleTask != null) {
      sourceCallbackHandleTask.cancel(false);
    }
    if (scheduler != null) {
      scheduler.stop();
    }
  }
}
