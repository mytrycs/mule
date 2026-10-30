/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.config;

import static java.util.Arrays.asList;
import com.zernova.runtime.dsl.api.xml.XmlNamespaceInfo;
import com.zernova.runtime.dsl.api.xml.XmlNamespaceInfoProvider;

import java.util.Collection;

/**
 * {@link XmlNamespaceInfoProvider} for TEST module.
 *
 * @since 4.0
 */
public class TestXmlNamespaceInfoProvider implements XmlNamespaceInfoProvider {

  public static final String TEST_NAMESPACE = "test";

  @Override
  public Collection<XmlNamespaceInfo> getXmlNamespacesInfo() {
    return asList(new XmlNamespaceInfo() {

      @Override
      public String getNamespaceUriPrefix() {
        return "http://www.mulesoft.org/schema/mule/test";
      }

      @Override
      public String getNamespace() {
        return TEST_NAMESPACE;
      }
    });
  }
}
