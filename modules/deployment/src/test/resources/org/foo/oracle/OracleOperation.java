/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo.oracle;

import static java.lang.Thread.sleep;
import static org.mockito.Mockito.mock;
import static org.slf4j.LoggerFactory.getLogger;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.sql.DriverManager.getConnection;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.Class;
import java.lang.Thread;
import java.sql.Connection;

import oracle.jdbc.OracleDriver;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.test.runner.ArtifactClassLoaderRunnerConfig;

public class OracleOperation {

    public OracleOperation() {}

    @MediaType(value = TEXT_PLAIN, strict = false)
    public String printMessage(@Config OracleExtension config) {
        return "Test plugin extension says hello!";
    }

    @MediaType(value = TEXT_PLAIN, strict = false)
    public String connect(@Config OracleExtension config) {
        try {
            //Register test oracle driver to trick JdbcResourceReleaser
            //into releasing resources.
            Class.forName("oracle.jdbc.OracleDriver");
            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
            //Mimic oracle driver behaviour,
            //spawn timer threads.
            Timer timerThread = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    getLogger(OracleOperation.class).info("This is timer task reporting for duty.");
                }
            };

            timerThread.schedule(task, 10, 1000);

            return "Connection success!";

        } catch (Exception e) {
            return "Exception ocurred while attempting to load test driver: " + e.getMessage();
        }
    }
}