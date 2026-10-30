/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.route;

import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.runtime.extension.api.annotation.param.stereotype.AllowedStereotypes;
import com.zernova.runtime.extension.api.runtime.process.RouterCompletionCallback;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * A {@link Route} allows a given {@link ComponentModel Operation} to receive a chain of message processors along with a set of
 * parameters, to be executed as part of the operation's execution.
 * <p>
 * Routes should be used for receiving multiple possible paths of execution, where the {@link ParameterModel parameters} of the
 * {@link Route} can be used to decide whether or not the contained processors should be executed. {@link StereotypeModel
 * Stereotypes} can be used in the {@link Route} declaration to define which are the <b>allowed stereotypes</b> of the received
 * processors.
 * <p>
 * This {@link Route} will not allow complex parameters to be defined inline, since the DSL content of the element will be
 * exclusive of the parameterized processors.
 *
 * @since 1.0
 * @see Chain
 * @see AllowedStereotypes
 * @see RouterCompletionCallback
 */
@MinZernovaVersion("4.1")
public abstract class Route {

  @Parameter
  private Chain processors;

  public Chain getChain() {
    return processors;
  }

  /**
   * This is for call from the runtime when building this component. Additional calls to this will fail with
   * {@link IllegalStateException}.
   *
   * @param processors the processors of this route
   *
   * @since 1.8
   */
  public final void setProcessors(Chain processors) {
    if (this.processors != null) {
      throw new IllegalStateException("'processors' had been already set in route '" + this.toString() + "'");
    }
    this.processors = processors;
  }

}
