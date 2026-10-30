/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension;

import com.zernova.runtime.extension.api.annotation.metadata.TypeResolver;
import com.zernova.runtime.extension.api.annotation.param.Content;
import com.zernova.sdk.api.annotation.binding.Binding;
import com.zernova.sdk.api.annotation.values.FieldValues;
import com.zernova.test.values.extension.metadata.XmlTypeResolver;
import com.zernova.test.values.extension.resolver.WithRequiredParameterSdkValueProvider;

import java.io.InputStream;

public class XmlBasedOperations {

  public void textAsActingForAttributeValue(@FieldValues(value = WithRequiredParameterSdkValueProvider.class,
      targetSelectors = "nested.tag.@customAttribute", bindings = @Binding(actingParameter = "requiredValue",
          extractionExpression = "xmlBody.nested.tag.\"__text\"")) @Content @TypeResolver(XmlTypeResolver.class) InputStream xmlBody) {}

  public void attributeAsActingForTagContentValue(@FieldValues(value = WithRequiredParameterSdkValueProvider.class,
      targetSelectors = "nested.tag", bindings = @Binding(actingParameter = "requiredValue",
          extractionExpression = "xmlBody.nested.anotherTag.@customAttribute")) @Content @TypeResolver(XmlTypeResolver.class) InputStream xmlBody) {}

  public void tagContentAsActingForAttributeValue(@FieldValues(value = WithRequiredParameterSdkValueProvider.class,
      targetSelectors = "nested.tag.@customAttribute", bindings = @Binding(actingParameter = "requiredValue",
          extractionExpression = "xmlBody.nested.someTag")) @com.zernova.sdk.api.annotation.param.Content @TypeResolver(XmlTypeResolver.class) InputStream xmlBody) {}

}
