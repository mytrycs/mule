/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension;

import static com.zernova.sdk.api.meta.JavaVersion.JAVA_17;
import static com.zernova.sdk.api.meta.JavaVersion.JAVA_21;

import com.zernova.runtime.extension.api.annotation.Configurations;
import com.zernova.runtime.extension.api.annotation.Export;
import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Sources;
import com.zernova.runtime.extension.api.annotation.dsl.xml.Xml;
import com.zernova.sdk.api.annotation.JavaVersionSupport;
import com.zernova.test.values.extension.config.ChatConfiguration;
import com.zernova.test.values.extension.config.ConfigWithBoundValueParameter;
import com.zernova.test.values.extension.config.ConfigWithFailureErrorProvider;
import com.zernova.test.values.extension.config.ConfigWithParameterWithFieldValues;
import com.zernova.test.values.extension.config.ConfigWithValueAndRequiredParam;
import com.zernova.test.values.extension.config.ConfigWithValueParameter;
import com.zernova.test.values.extension.config.ConfigWithValuesWithRequiredParamsFromParamGroup;
import com.zernova.test.values.extension.config.ConfigWithValuesWithRequiredParamsFromShowInDslGroup;
import com.zernova.test.values.extension.config.SimpleConfig;
import com.zernova.test.values.extension.source.SimpleSource;
import com.zernova.test.values.extension.source.SourceWithTwoBoundActingParameters;

@Extension(name = "Values")
@JavaVersionSupport({JAVA_21, JAVA_17})
@Configurations({SimpleConfig.class, ConfigWithValueParameter.class,
    ConfigWithValueAndRequiredParam.class, ConfigWithValuesWithRequiredParamsFromParamGroup.class,
    ConfigWithValuesWithRequiredParamsFromShowInDslGroup.class, ConfigWithFailureErrorProvider.class,
    ConfigWithBoundValueParameter.class, ConfigWithParameterWithFieldValues.class, ChatConfiguration.class, XmlBasedConfig.class})
@Sources({SimpleSource.class, SourceWithTwoBoundActingParameters.class})
@Export(classes = MyPojo.class)
@Xml(namespace = "http://www.mulesoft.org/schema/mule/values", prefix = "values")
public class ValuesExtension {

}
