/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension;

import static com.zernova.runtime.api.meta.ExternalLibraryType.NATIVE;
import static com.zernova.test.heisenberg.extension.HeisenbergExtension.HEISENBERG_LIB_CLASS_NAME;
import static com.zernova.test.heisenberg.extension.HeisenbergExtension.HEISENBERG_LIB_DESCRIPTION;
import static com.zernova.test.heisenberg.extension.HeisenbergExtension.HEISENBERG_LIB_FILE_NAME;
import static com.zernova.test.heisenberg.extension.HeisenbergExtension.HEISENBERG_LIB_NAME;
import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.tls.TlsContextFactory;
import com.zernova.runtime.extension.api.annotation.ExternalLib;
import com.zernova.runtime.extension.api.annotation.param.Parameter;
import com.zernova.sdk.api.annotation.Alias;

@Alias("secure")
@ExternalLib(name = HEISENBERG_LIB_NAME,
    description = HEISENBERG_LIB_DESCRIPTION,
    nameRegexpMatcher = HEISENBERG_LIB_FILE_NAME,
    requiredClassName = HEISENBERG_LIB_CLASS_NAME,
    type = NATIVE)
public class SecureHeisenbergConnectionProvider extends HeisenbergConnectionProvider {

  @Parameter
  private TlsContextFactory tlsContextFactory;


  @Override
  public HeisenbergConnection connect() throws ConnectionException {
    HeisenbergConnection connection = super.connect();
    connection.setTlsContextFactory(tlsContextFactory);

    return connection;
  }

  public TlsContextFactory getTlsContextFactory() {
    return tlsContextFactory;
  }
}
