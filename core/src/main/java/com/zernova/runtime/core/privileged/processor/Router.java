/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.privileged.processor;

import com.zernova.runtime.core.api.processor.Processor;

/**
 * A {@link Processor} that routes messages to zero or more destination message processors. Implementations determine exactly how
 * this is done by making decisions about which route(s) should be used and if the message should be copied or not.
 */
public interface Router extends Scope {

}
