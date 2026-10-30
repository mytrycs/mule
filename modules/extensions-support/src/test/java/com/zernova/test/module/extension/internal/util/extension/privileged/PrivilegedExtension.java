/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.internal.util.extension.privileged;

import com.zernova.runtime.extension.api.annotation.Extension;
import com.zernova.runtime.extension.api.annotation.Operations;
import com.zernova.runtime.extension.api.annotation.privileged.DeclarationEnrichers;

@DeclarationEnrichers({ChangeNameDeclarationEnricher.class, TestNonBlockingOperationDeclarationEnricher.class})
@Extension(name = "privileged")
@Operations(PrivilegedOperations.class)
public class PrivilegedExtension {
}
