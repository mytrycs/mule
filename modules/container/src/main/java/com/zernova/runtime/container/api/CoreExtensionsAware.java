/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.container.api;

import java.util.List;

/**
 * Enables injection of all available {@link ZernovaCoreExtension}.
 * <p/>
 * Classes implementing this interface cannot contain methods annotated with {@link ZernovaCoreExtensionDependency} as that will
 * create an unresolvable dependency on runtime.
 *
 * @deprecated on 4.1, use @Inject on a field or setter method of type {@link List<ZernovaCoreExtension>}
 */
@Deprecated
public interface CoreExtensionsAware {

  void setCoreExtensions(List<ZernovaCoreExtension> coreExtensions);
}
