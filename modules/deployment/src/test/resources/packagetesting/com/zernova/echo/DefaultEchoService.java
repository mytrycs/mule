/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.echo;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.service.test.api.EchoService;

public class DefaultEchoService implements EchoService, Startable, Stoppable {
    public DefaultEchoService() {
    }

    public String echo(String message) {
        return message;
    }

    public void start() throws ZernovaException {
        System.out.println("Starting " + this.getClass().getName());
    }

    public void stop() throws ZernovaException {
        System.out.println("Stopping " + this.getClass().getName());
    }

    public String getName() {
        return "DefaultEchoService";
    }
}
