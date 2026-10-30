/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.processor.chain;

import com.zernova.runtime.core.api.processor.Processor;

import java.util.List;

/**
 * Contract interface for a class which can return a list of {@link Processor}
 *
 * @since 4.0
 */
public interface HasMessageProcessors {

  List<Processor> getMessageProcessors();
}
