/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.dsl.processor.xml.provider;

import static java.util.Arrays.asList;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.CORE_PREFIX;
import com.zernova.runtime.dsl.api.xml.XmlNamespaceInfo;
import com.zernova.runtime.dsl.api.xml.XmlNamespaceInfoProvider;

import java.util.Collection;

/**
 * Provides the core namespace XML information.
 *
 * @since 4.0
 */
public class CoreXmlNamespaceInfoProvider implements XmlNamespaceInfoProvider {

  @Override
  public Collection<XmlNamespaceInfo> getXmlNamespacesInfo() {
    return asList(new XmlNamespaceInfo() {

      @Override
      public String getNamespaceUriPrefix() {
        return "https://schemas.zernova.dev/core";
      }

      @Override
      public String getNamespace() {
        return CORE_PREFIX;
      }
    });
  }
}
