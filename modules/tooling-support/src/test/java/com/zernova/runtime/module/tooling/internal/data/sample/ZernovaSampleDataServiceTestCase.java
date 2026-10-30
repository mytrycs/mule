/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.data.sample;

import static com.zernova.sdk.api.data.sample.SampleDataException.INVALID_LOCATION;
import static com.zernova.sdk.api.data.sample.SampleDataException.NOT_SUPPORTED;
import static com.zernova.sdk.api.data.sample.SampleDataException.NO_DATA_AVAILABLE;
import static com.zernova.sdk.api.data.sample.SampleDataException.UNKNOWN;
import static com.zernova.test.allure.AllureConstants.SdkToolingSupport.SDK_TOOLING_SUPPORT;
import static com.zernova.test.allure.AllureConstants.SdkToolingSupport.SampleDataStory.SAMPLE_DATA_SERVICE;
import static com.zernova.test.module.extension.internal.util.extension.data.sample.SampleDataTestUtils.exceptionMatcher;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.location.ConfigurationComponentLocator;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.extension.api.data.sample.ComponentSampleDataProvider;
import com.zernova.sdk.api.data.sample.SampleDataException;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@SmallTest
@Feature(SDK_TOOLING_SUPPORT)
@Story(SAMPLE_DATA_SERVICE)
public class ZernovaSampleDataServiceTestCase extends AbstractZernovaTestCase {

  @Rule
  public ExpectedException expectedException = none();

  @Rule
  public MockitoRule mockitorule = MockitoJUnit.rule();

  @Mock
  private Location location;

  @Mock
  private ConfigurationComponentLocator componentLocator;

  @Mock(extraInterfaces = {Component.class})
  private ComponentSampleDataProvider componentSampleDataProvider;

  @Mock
  private Message message;

  private final ZernovaSampleDataService sampleDataService = new ZernovaSampleDataService();

  @Before
  public void before() throws SampleDataException {
    sampleDataService.setComponentLocator(componentLocator);
    when(componentSampleDataProvider.getSampleData()).thenReturn(message);
    when(componentLocator.find(location)).thenReturn(of((Component) componentSampleDataProvider));
  }

  @Test
  public void getSampleData() throws SampleDataException {
    assertThat(sampleDataService.getSampleData(location), is(sameInstance(message)));
    verify(componentSampleDataProvider).getSampleData();
  }

  @Test
  public void noDataFound() throws SampleDataException {
    expectSampleDataException(NO_DATA_AVAILABLE);
    when(componentSampleDataProvider.getSampleData()).thenReturn(null);
    sampleDataService.getSampleData(location);
  }

  @Test
  public void invalidLocation() throws SampleDataException {
    expectSampleDataException(INVALID_LOCATION);
    when(componentLocator.find(location)).thenReturn(empty());

    sampleDataService.getSampleData(location);
  }

  @Test
  public void resolutionException() throws SampleDataException {
    SampleDataException e = new SampleDataException("some message", UNKNOWN);
    expectedException.expect(sameInstance(e));

    when(componentSampleDataProvider.getSampleData()).thenThrow(e);
    sampleDataService.getSampleData(location);
  }

  @Test
  public void unsupportedComponent() throws SampleDataException {
    when(componentLocator.find(location)).thenReturn(of(mock(Component.class)));
    expectSampleDataException(NOT_SUPPORTED);

    sampleDataService.getSampleData(location);
  }

  private void expectSampleDataException(String failureCode) {
    expectedException.expect(SampleDataException.class);
    expectedException.expect(exceptionMatcher(failureCode));
  }
}
