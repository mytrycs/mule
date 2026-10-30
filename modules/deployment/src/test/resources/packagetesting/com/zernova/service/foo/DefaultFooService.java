/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.service.foo;

import com.zernova.runtime.service.test.api.EchoService;
import com.zernova.runtime.service.test.api.FooService;

import jakarta.inject.Inject;

public class DefaultFooService implements FooService {
    @Inject
    private EchoService echoService;

    public DefaultFooService(EchoService echoService) {
        this.echoService = echoService;
    }

    public String doFoo(String foo) {
        return this.echoService.echo(foo);
    }

    public String getName() {
        return "DefaultFooService";
    }
}
