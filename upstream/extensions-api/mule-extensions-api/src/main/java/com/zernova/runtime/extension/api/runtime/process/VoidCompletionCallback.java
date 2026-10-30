/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.runtime.process;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.extension.api.runtime.route.Route;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

/**
 * This callback is how Routers notify the end of its execution when no result is produced.
 * <p>
 * In order to implement a Void Router, the method needs to:
 * <p>
 * <ul>
 * <li>Have a void return type</li>
 * <li>Have at least one argument of {@link Route} type</li>
 * <li>Have an argument of {@link VoidCompletionCallback} type</li>
 * </ul>
 * <p>
 * When the processing performed by the Router finishes, it has to notify its completion either by invoking the {@link #success()}
 * or {@link #error(Throwable)} methods. Only then will the execution of the Router be considered as completed and the next
 * processor in the pipeline will be executed.
 * <p>
 * For example, a Void Router can be declared as:
 * <p>
 *
 * <pre>
 *
 * public void enricher(WhenRoute when, @Optional DefaultRoute defaultRoute, VoidCompletionCallback callback) {
 *   if (when.shouldExecute()) {
 *     when.getChain().process(r -> callback.success(),
 *                             (e, r) -> callback.error(e));
 *   } else if (other != null && other.shouldExecute()) {
 *     other.getChain().process(r -> callback.success(),
 *                              (e, r) -> callback.error(e));
 *   } else {
 *     callback.error(new IllegalArgumentException("No route executed"));
 *   }
 * }
 * </pre>
 * <p>
 * As you can see, the result of the Route being executed is ignored, and the {@link VoidCompletionCallback callback} is notified
 * with a {@link VoidCompletionCallback#success()} or {@link VoidCompletionCallback#error(Throwable)}
 *
 * @since 1.1
 */
@MinZernovaVersion("4.1")
@NoImplement
public interface VoidCompletionCallback extends com.zernova.sdk.api.runtime.process.VoidCompletionCallback {

}
