/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.data.sample.extension;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;

import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.Sources;
import com.zernova.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.annotation.JavaVersionSupport;
import com.zernova.sdk.api.annotation.dsl.xml.Xml;
import com.zernova.sdk.api.runtime.operation.Result;
import com.zernova.test.data.sample.extension.connection.SampleDataConnectionProvider;
import com.zernova.test.data.sample.extension.source.AliasedParameterGroupListener;
import com.zernova.test.data.sample.extension.source.ComplexParameterGroupListener;
import com.zernova.test.data.sample.extension.source.ConfigTestSampleDataListener;
import com.zernova.test.data.sample.extension.source.ConnectedTestSampleDataListener;
import com.zernova.test.data.sample.extension.source.FailingTestSampleDataListener;
import com.zernova.test.data.sample.extension.source.ZernovaContextAwareTestSampleDataListener;
import com.zernova.test.data.sample.extension.source.ParameterGroupListener;
import com.zernova.test.data.sample.extension.source.ShowInDslParameterGroupListener;
import com.zernova.test.data.sample.extension.source.SimpleTestSampleDataListener;
import com.zernova.test.data.sample.extension.source.SimpleTestSampleDataListenerWithTwoBoundActingParameters;

@Extension(name = SampleDataExtension.EXTENSION_NAME)
@JavaVersionSupport({JAVA_21, JAVA_17})
@Xml(namespace = "https://schemas.zernova.dev/sample-data", prefix = "sample-data")
@ConnectionProviders(SampleDataConnectionProvider.class)
@Operations(SampleDataOperations.class)
@Sources({SimpleTestSampleDataListener.class, ConnectedTestSampleDataListener.class, ConfigTestSampleDataListener.class,
    ParameterGroupListener.class, ShowInDslParameterGroupListener.class, AliasedParameterGroupListener.class,
    ComplexParameterGroupListener.class, ZernovaContextAwareTestSampleDataListener.class,
    SimpleTestSampleDataListenerWithTwoBoundActingParameters.class, FailingTestSampleDataListener.class
})
public class SampleDataExtension {

  public static final String NULL_VALUE = "<<null>>";
  public static final String EXTENSION_NAME = "SampleData";

  public static <T, A> Result<T, A> adaptLegacy(com.zernova.runtime.extension.api.runtime.operation.Result<T, A> result) {
    return Result.<T, A>builder()
        .output(result.getOutput())
        .mediaType(result.getMediaType().orElse(null))
        .attributes(result.getAttributes().orElse(null))
        .attributesMediaType(result.getAttributesMediaType().orElse(null))
        .build();
  }

  @Parameter
  private String prefix;

  public String getPrefix() {
    return prefix;
  }
}
