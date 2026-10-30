/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.util.Preconditions;
import com.zernova.runtime.extension.api.annotation.deprecated.Deprecated;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.stereotype.AllowedStereotypes;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.process.CompletionCallback;
import com.zernova.runtime.extension.api.runtime.route.Chain;
import com.zernova.runtime.extension.api.stereotype.ValidatorStereotype;
import com.zernova.sdk.api.annotation.dsl.xml.ParameterDsl;

import java.util.Map;

public class HeisenbergScopes implements Initialisable {

  private int initialiasedCounter = 0;

  @Parameter
  @Optional(defaultValue = "0")
  private int fieldParam;

  @Override
  public void initialise() throws InitialisationException {
    initialiasedCounter++;
  }

  public int getCounter() {
    return initialiasedCounter;
  }

  public void getChain(@AllowedStereotypes(ValidatorStereotype.class) Chain chain, CompletionCallback<Chain, Void> cb) {
    cb.success(Result.<Chain, Void>builder().output(chain).build());
  }

  @Deprecated(message = "All usages of this scope are covered by the payload-modifier scope.", since = "1.3.0",
      toRemoveIn = "2.0.0")
  public void executeAnything(Chain chain, CompletionCallback<Void, Void> cb) {
    chain.process(cb::success, (t, e) -> cb.error(t));
  }

  @MediaType(value = TEXT_PLAIN, strict = false)
  public void payloadModifier(Chain chain,
                              CompletionCallback<Object, Object> cb,
                              Object payload,
                              @ParameterDsl(allowInlineDefinition = false) Map<String, String> attributes) {
    chain.process(payload, attributes, cb::success, (t, e) -> cb.error(t));
  }

  public void exceptionOnCallbacks(Chain processors, CompletionCallback<Void, Void> callback) {
    processors
        .process(result -> {
          throw new IllegalArgumentException("ON_SUCCESS_EXCEPTION");
        }, (error, previous) -> {
          throw new IllegalArgumentException("ON_ERROR_EXCEPTION");
        });
  }

  public void alwaysFailsWrapper(Chain processors, CompletionCallback<Void, Void> callback) {
    processors.process(result -> callback.error(new IllegalArgumentException("ON_SUCCESS_ERROR")),
                       (error, previous) -> callback.error(new IllegalArgumentException("ON_ERROR_ERROR")));
  }

  @MediaType(TEXT_PLAIN)
  public void neverFailsWrapper(@Optional Chain optionalProcessors,
                                CompletionCallback<String, Object> callback) {
    if (optionalProcessors == null) {
      callback.success(Result.<String, Object>builder().output("EMPTY").build());
    } else {
      optionalProcessors.process(result -> callback.success(result.copy().output("SUCCESS").attributes(result).build()),
                                 (error, previous) -> callback
                                     .success(previous.copy().output("ERROR").attributes(error).build()));
    }
  }

  public int scopeField(int expected, int newValue) {
    Preconditions.checkArgument(expected == fieldParam, "Expected " + expected + " but was " + fieldParam);
    fieldParam = newValue;
    return fieldParam;
  }

}
