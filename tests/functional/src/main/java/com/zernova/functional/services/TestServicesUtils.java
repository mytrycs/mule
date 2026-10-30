/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.services;

import static java.lang.System.getProperty;

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

import com.zernova.runtime.api.el.DefaultExpressionLanguageFactoryService;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService;
import com.zernova.runtime.api.scheduler.SchedulerService;
import com.zernova.runtime.http.api.HttpService;
import com.zernova.runtime.module.service.builder.ServiceFileBuilder;
import com.zernova.tck.util.CompilerUtils;

import java.io.File;
import java.io.IOException;

public class TestServicesUtils {

  private TestServicesUtils() {
    // Nothing to do
  }

  private static File getResourceFile(String resource, File tempFolder) {
    return getResourceFile(resource, tempFolder, resource);
  }

  private static File getResourceFile(String resource, File tempFolder, String targetFilePath) {
    final File targetFile = new File(tempFolder, targetFilePath);
    try {
      copyInputStreamToFile(TestServicesUtils.class.getResourceAsStream(resource), targetFile);
    } catch (IOException e) {
      throw new ZernovaRuntimeException(e);
    }

    return targetFile;
  }

  /**
   * Provides a packaged mock {@link SchedulerService} implementation.
   *
   * @param tempFolder where to generate temporary files needed for compilation of the service classes.
   * @return the zip service file
   */
  public static File buildSchedulerServiceFile(File tempFolder) {
    final File defaulServiceSchedulerJarFile = new CompilerUtils.JarCompiler()
        .targetJavaVersion(17)
        .compiling(getResourceFile("/com/zernova/service/scheduler/MockScheduler.java", tempFolder),
                   getResourceFile("/com/zernova/service/scheduler/MockSchedulerService.java", tempFolder),
                   getResourceFile("/com/zernova/service/scheduler/MockSchedulerServiceProvider.java", tempFolder),
                   getResourceFile("/com/zernova/service/scheduler/MockSchedulerView.java", tempFolder),
                   getResourceFile("/com/zernova/service/scheduler/module-info.java", tempFolder, "module-info.java"))
        .including(getResourceFile("/com/zernova/service/scheduler/MANIFEST.MF", tempFolder),
                   "META-INF/MANIFEST.MF")
        .dependingOn(new File(getProperty("apiAnnotationsLib")))
        .compile("mule-module-service-mock-scheduler-1.0-SNAPSHOT.jar");

    return new ServiceFileBuilder("schedulerService")
        .withServiceProviderClass("com.zernova.service.scheduler.MockSchedulerServiceProvider")
        .forContract("com.zernova.runtime.api.scheduler.SchedulerService")
        .usingLibrary(defaulServiceSchedulerJarFile.getAbsolutePath())
        .unpack(true)
        .getArtifactFile();
  }

  /**
   * Provides a packaged mock {@link DefaultExpressionLanguageFactoryService} implementation.
   *
   * @param tempFolder where to generate temporary files needed for compilation of the service classes.
   * @return the zip service file
   */
  public static File buildExpressionLanguageServiceFile(File tempFolder) {
    final File defaulServiceSchedulerJarFile = new CompilerUtils.JarCompiler()
        .targetJavaVersion(17)
        .compiling(getResourceFile("/com/zernova/service/el/MockExpressionLanguage.java", tempFolder),
                   getResourceFile("/com/zernova/service/el/MockExpressionLanguageFactoryService.java", tempFolder),
                   getResourceFile("/com/zernova/service/el/MockExpressionLanguageFactoryServiceProvider.java", tempFolder),
                   getResourceFile("/com/zernova/service/el/module-info.java", tempFolder, "module-info.java"))
        .including(getResourceFile("/com/zernova/service/el/MANIFEST.MF", tempFolder),
                   "META-INF/MANIFEST.MF")
        .dependingOn(new File(getProperty("apiAnnotationsLib")))
        .compile("mule-module-service-mock-expression-language-1.0-SNAPSHOT.jar");

    return new ServiceFileBuilder("expressionLanguageService")
        .withServiceProviderClass("com.zernova.service.el.MockExpressionLanguageFactoryServiceProvider")
        .forContract("com.zernova.runtime.api.el.DefaultExpressionLanguageFactoryService")
        .usingLibrary(defaulServiceSchedulerJarFile.getAbsolutePath())
        .unpack(true)
        .getArtifactFile();
  }


  /**
   * Provides a packaged mock {@link ExpressionLanguageMetadataService} implementation.
   *
   * @param tempFolder where to generate temporary files needed for compilation of the service classes.
   * @return the zip service file
   */
  public static File buildExpressionLanguageMetadataServiceFile(File tempFolder) {
    final File defaultServiceJarFile = new CompilerUtils.JarCompiler()
        .targetJavaVersion(17)
        .compiling(getResourceFile("/com/zernova/service/el/metadata/MockExpressionLanguageMetadataService.java", tempFolder),
                   getResourceFile("/com/zernova/service/el/metadata/MockExpressionLanguageMetadataServiceProvider.java",
                                   tempFolder),
                   getResourceFile("/com/zernova/service/el/metadata/module-info.java", tempFolder, "module-info.java"))
        .including(getResourceFile("/com/zernova/service/el/metadata/MANIFEST.MF", tempFolder),
                   "META-INF/MANIFEST.MF")
        .dependingOn(new File(getProperty("apiAnnotationsLib")))
        .compile("mule-module-service-mock-expression-language-metadata-1.0-SNAPSHOT.jar");

    return new ServiceFileBuilder("expressionLanguageMetadataService")
        .withServiceProviderClass("com.zernova.service.el.metadata.MockExpressionLanguageMetadataServiceProvider")
        .forContract("com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService")
        .usingLibrary(defaultServiceJarFile.getAbsolutePath())
        .unpack(true)
        .getArtifactFile();
  }

  /**
   * Provides a packaged mock {@link HttpService} implementation.
   *
   * @param tempFolder where to generate temporary files needed for compilation of the service classes.
   * @return the zip service file
   */
  public static File buildHttpServiceFile(File tempFolder) {
    final File defaulServiceSchedulerJarFile = new CompilerUtils.JarCompiler()
        .targetJavaVersion(17)
        .compiling(getResourceFile("/com/zernova/service/http/mock/MockHttpService.java", tempFolder),
                   getResourceFile("/com/zernova/service/http/mock/MockHttpServiceProvider.java", tempFolder),
                   getResourceFile("/com/zernova/service/http/mock/module-info.java", tempFolder, "module-info.java"))
        .including(getResourceFile("/com/zernova/service/http/mock/MANIFEST.MF", tempFolder),
                   "META-INF/MANIFEST.MF")
        .dependingOn(new File(getProperty("apiAnnotationsLib")))
        .compile("mule-module-service-mock-http-1.0-SNAPSHOT.jar");

    return new ServiceFileBuilder("http-service")
        .withServiceProviderClass("com.zernova.service.http.mock.MockHttpServiceProvider")
        .forContract("com.zernova.runtime.http.api.HttpService")
        .unpack(true)
        .usingLibrary(defaulServiceSchedulerJarFile.getAbsolutePath()).getArtifactFile();
  }
}
