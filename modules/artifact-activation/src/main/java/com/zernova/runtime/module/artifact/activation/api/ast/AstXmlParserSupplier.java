/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.api.ast;

import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.ast.api.xml.AstXmlParser;

import java.util.Set;

/**
 * A component capable of supplying {@link AstXmlParser} instances
 *
 * @since 4.5.0
 */
@FunctionalInterface
@NoImplement
public interface AstXmlParserSupplier {

  /**
   * Returns an {@link AstXmlParser}. Each invocation might return either new instances or reuse previously yielded ones, but must
   * always honour the supplied arguments.
   *
   * @param extensions         the {@link ExtensionModel} instances configured in the parser.
   * @param disableValidations whether to disable validations.
   * @return an {@link AstXmlParser}
   */
  AstXmlParser getParser(Set<ExtensionModel> extensions, boolean disableValidations);
}
