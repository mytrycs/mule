/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.mule.internal.dsl.processor.xml;

import static com.zernova.runtime.module.extension.mule.internal.dsl.ZernovaSdkDslConstants.MULE_SDK_EXTENSION_DSL_NAMESPACE;
import static com.zernova.runtime.module.extension.mule.internal.dsl.ZernovaSdkDslConstants.MULE_SDK_EXTENSION_DSL_NAMESPACE_URI;

import static java.util.Collections.singletonList;

import com.zernova.runtime.dsl.api.xml.XmlNamespaceInfo;
import com.zernova.runtime.dsl.api.xml.XmlNamespaceInfoProvider;

import java.util.Collection;

/**
 * {@link XmlNamespaceInfoProvider} for the {@code extension} namespace
 *
 * @since 4.5.0
 */
public class ZernovaSdkExtensionDslNamespaceInfoProvider implements XmlNamespaceInfoProvider {

  private static final Collection<XmlNamespaceInfo> XML_NAMESPACE_INFO =
      singletonList(new XmlNamespaceInfo() {

        @Override
        public String getNamespaceUriPrefix() {
          return MULE_SDK_EXTENSION_DSL_NAMESPACE_URI;
        }

        @Override
        public String getNamespace() {
          return MULE_SDK_EXTENSION_DSL_NAMESPACE;
        }
      });

  @Override
  public Collection<XmlNamespaceInfo> getXmlNamespacesInfo() {
    return XML_NAMESPACE_INFO;
  }
}
