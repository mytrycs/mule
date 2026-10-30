/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.api.artifact;

import com.zernova.runtime.module.tooling.api.ArtifactAgnosticServiceBuilder;

/**
 * Provides all required steps to configure and build a new {@link DeclarationSession}
 *
 * @since 4.4.0
 */
public interface DeclarationSessionBuilder
    extends ArtifactAgnosticServiceBuilder<DeclarationSessionBuilder, DeclarationSession> {

}
