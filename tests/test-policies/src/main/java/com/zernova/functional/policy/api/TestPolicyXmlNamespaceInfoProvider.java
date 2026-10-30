/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.policy.api;


import static java.util.Collections.singletonList;

import com.zernova.runtime.dsl.api.xml.XmlNamespaceInfo;
import com.zernova.runtime.dsl.api.xml.XmlNamespaceInfoProvider;

import java.util.Collection;

/**
 * Defines the {@link XmlNamespaceInfoProvider} for the test policy module
 */
public class TestPolicyXmlNamespaceInfoProvider implements XmlNamespaceInfoProvider {

  public static final String TEST_POLICY_NAMESPACE = "http://www.mulesoft.org/schema/mule/test-policy";
  public static final String TEST_POLICY_PREFIX = "test-policy";

  @Override
  public Collection<XmlNamespaceInfo> getXmlNamespacesInfo() {
    return singletonList(new XmlNamespaceInfo() {

      @Override
      public String getNamespaceUriPrefix() {
        return TEST_POLICY_NAMESPACE;
      }

      @Override
      public String getNamespace() {
        return TEST_POLICY_PREFIX;
      }
    });
  }
}
