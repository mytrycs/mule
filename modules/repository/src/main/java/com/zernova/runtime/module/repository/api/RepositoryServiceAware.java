/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.repository.api;

/**
 * Enables {@link RepositoryService} injection.
 *
 * @deprecated on 4.1, use @Inject on a field or setter method of type {@link RepositoryService}
 */
@Deprecated
public interface RepositoryServiceAware {

  void setRepositoryService(RepositoryService repositoryService);

}
