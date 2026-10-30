/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.dsl;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.xml.namespace.QName.valueOf;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static com.zernova.runtime.api.component.Component.NS_MULE_DOCUMENTATION;
import static com.zernova.runtime.config.internal.model.ApplicationModel.DOC_NAMESPACE;

import java.util.Map.Entry;
import java.util.Optional;

import javax.xml.namespace.QName;

public final class XmlConstants {

  private XmlConstants() {}

  public static Optional<String> buildRawParamKeyForDocAttribute(Entry<String, String> docAttr) {
    final QName qName = valueOf(docAttr.getKey());

    if (NS_MULE_DOCUMENTATION.equals(qName.getNamespaceURI())) {
      return of(DOC_NAMESPACE + ":" + qName.getLocalPart());
    } else if (isEmpty(qName.getNamespaceURI())) {
      return of(DOC_NAMESPACE + ":" + docAttr.getKey());
    } else {
      return empty();
    }
  }
}
