/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.process;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.route.Route;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * This callback is how a Router receiving {@link Route}s notify their outcome.
 * <p>
 * In order to implement a Router (that is, an operation that receives one or more {@link Route}s), the method needs to:
 * <p>
 * <ul>
 * <li>Have a void return type</li>
 * <li>Have at least one argument of {@link Route} type</li>
 * <li>Have an argument of {@link RouterCompletionCallback} type</li>
 * </ul>
 * <p>
 * When the execution of the Router has finished, it has to notify the {@link Result} either by invoking the
 * {@link #success(Result)} or {@link #error(Throwable)} methods. Only then will the execution of the Router be considered as
 * completed and the next processor in the pipeline will be executed. <b>If the {@link #success(Result)} or
 * {@link #error(Throwable)} methods are invoked before any of the nested {@link Route}s is completed, the {@link Result} of the
 * nested execution will be lost and never propagated.</b>
 *
 * For example, a Router can be declared as:
 * <p>
 *
 * <pre>
 *
 * public void twoRoutesRouter(WhenRoute when, @Optional OtherwiseRoute other, RouterCompletionCallback callback) {
 *   if (when.shouldExecute()) {
 *     when.getChain().process(routeResult -> callback.success(routeResult), (e, r) -> callback.error(e));
 *   } else if (other != null && other.shouldExecute()) {
 *     other.getChain().process(callback::success, (e, r) -> callback.error(e));
 *   } else {
 *     callback.error(new IllegalArgumentException("No route could be executed"));
 *   }
 * }
 * </pre>
 * <p>
 *
 * @since 1.0
 * @deprecated since 1.7.0. Starting with Mule 4.7, routers can now have random outputs, use
 *             {@link com.zernova.sdk.api.runtime.process.CompletionCallback} instead
 */
@MinZernovaVersion("4.1")
@NoImplement
@Deprecated
public interface RouterCompletionCallback extends CompletionCallback<Object, Object> {


}
