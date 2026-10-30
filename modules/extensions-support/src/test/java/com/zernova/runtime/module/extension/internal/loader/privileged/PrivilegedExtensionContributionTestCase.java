/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.privileged;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static com.zernova.runtime.module.extension.api.util.ZernovaExtensionUtils.loadExtension;
import static com.zernova.test.module.extension.internal.util.extension.privileged.ChangeNameDeclarationEnricher.NEW_NAME;

import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.test.module.extension.internal.util.extension.privileged.PrivilegedExtension;

import org.junit.Test;

public class PrivilegedExtensionContributionTestCase extends AbstractZernovaTestCase {

  @Test
  public void loadPrivilegedExtension() {
    ExtensionModel extension = loadExtension(PrivilegedExtension.class);
    assertThat(extension.getName(), is(NEW_NAME));
  }
}
