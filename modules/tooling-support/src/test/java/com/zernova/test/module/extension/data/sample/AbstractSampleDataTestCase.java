/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.data.sample;

import static com.zernova.runtime.api.metadata.MediaType.APPLICATION_JSON;
import static com.zernova.runtime.api.metadata.MediaType.APPLICATION_XML;
import static com.zernova.runtime.core.api.data.sample.SampleDataService.SAMPLE_DATA_SERVICE_KEY;
import static com.zernova.test.data.sample.extension.SampleDataExtension.EXTENSION_NAME;
import static com.zernova.test.module.extension.internal.util.extension.data.sample.SampleDataTestUtils.exceptionMatcher;

import static java.util.Optional.of;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.rules.ExpectedException.none;

import com.zernova.functional.junit4.ZernovaArtifactFunctionalTestCase;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.core.api.data.sample.SampleDataService;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.util.func.CheckedSupplier;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.sdk.api.data.sample.SampleDataException;
import com.zernova.test.runner.ArtifactClassLoaderRunnerConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@ArtifactClassLoaderRunnerConfig(applicationSharedRuntimeLibs = {"com.zernova.tests:mule-tests-model"})
public abstract class AbstractSampleDataTestCase extends ZernovaArtifactFunctionalTestCase {

  protected static final String EXPECTED_PAYLOAD = "my payload";
  protected static final String EXPECTED_ATTRIBUTES = "my attributes";
  protected static final String CONF_PREFIX = "from-conf-";
  protected static final String NULL_VALUE = "<<null>>";

  @Inject
  @Named(SAMPLE_DATA_SERVICE_KEY)
  private SampleDataService sampleDataService;

  @Inject
  private ExtensionManager extensionManager;

  @Rule
  public ExpectedException expectedException = none();

  @Override
  public boolean disableXmlValidations() {
    return true;
  }

  @Override
  public boolean addToolingObjectsToRegistry() {
    return true;
  }

  @Override
  protected boolean isDisposeContextPerClass() {
    return true;
  }

  protected void assertMessage(Message message, String payload, String attributes) {
    assertThat(message.getPayload().getValue(), equalTo(payload));
    assertThat(message.getPayload().getDataType().getMediaType().matches(APPLICATION_JSON), is(true));
    assertThat(message.getAttributes().getValue(), equalTo(attributes));
    assertThat(message.getAttributes().getDataType().getMediaType().matches(APPLICATION_XML), is(true));
  }

  protected Message getOperationSampleByLocation(String flowName) throws SampleDataException {
    Location location = Location.builder().globalName(flowName).addProcessorsPart().addIndexPart(0).build();
    return sampleDataService.getSampleData(location);
  }

  protected Message getSourceSampleByLocation(String flowName) throws SampleDataException {
    Location location = Location.builder().globalName(flowName).addSourcePart().build();
    return sampleDataService.getSampleData(location);
  }

  protected Message getSampleByComponentName(String componentName,
                                             Map<String, Object> parameters,
                                             String configName)
      throws SampleDataException {
    return sampleDataService.getSampleData(
                                           EXTENSION_NAME,
                                           componentName,
                                           parameters,
                                           getConfigurationSupplier(configName));
  }

  private Supplier<Optional<ConfigurationInstance>> getConfigurationSupplier(String configName) {
    if (configName == null) {
      return Optional::empty;
    }

    return (CheckedSupplier<Optional<ConfigurationInstance>>) () -> of(extensionManager.getConfiguration(configName,
                                                                                                         testEvent()));
  }

  protected void expectSampleDataException(String failureCode) {
    expectedException.expect(SampleDataException.class);
    expectedException.expect(exceptionMatcher(failureCode));
  }

  protected void expectSampleDataException(Class<? extends SampleDataException> sampleDataExceptionClass, String failureCode,
                                           String errorMessage, Optional<Class<? extends Throwable>> causeClass) {
    expectedException.expect(sampleDataExceptionClass);
    expectedException.expect(exceptionMatcher(failureCode));
    expectedException.expectMessage(errorMessage);
    if (causeClass.isPresent()) {
      expectedException.expectCause(instanceOf(causeClass.get()));
    } else {
      expectedException.expectCause(nullValue(Throwable.class));
    }
  }

  protected Map<String, Object> getDefaultParameters() {
    Map<String, Object> params = new HashMap<>();
    params.put("payload", "my payload");
    params.put("attributes", "my attributes");

    return params;
  }

  protected Map<String, Object> getGroupParameters() {
    Map<String, Object> params = new HashMap<>();
    params.put("groupParameter", "my payload");
    params.put("optionalParameter", "my attributes");

    return params;
  }
}
