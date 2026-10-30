/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.APPLICATION_XML;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.metadata.fixed.AttributesXmlType;
import com.zernova.runtime.extension.api.annotation.metadata.fixed.OutputXmlType;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.runtime.source.Source;
import com.zernova.runtime.extension.api.runtime.source.SourceCallback;

import java.io.InputStream;

@Alias("xml-static-metadata")
@AttributesXmlType(schema = "order.xsd", qname = "shiporder")
@OutputXmlType(schema = "order.xsd", qname = "shiporder")
@MediaType(value = APPLICATION_XML, strict = false)
public class CustomXmlStaticMetadataSource extends Source<InputStream, InputStream> {

  @Override
  public void onStart(SourceCallback sourceCallback) throws ZernovaException {

  }

  @Override
  public void onStop() {

  }
}
