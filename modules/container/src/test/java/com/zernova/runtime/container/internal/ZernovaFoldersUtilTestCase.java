/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.container.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import com.zernova.runtime.container.api.ZernovaFoldersUtil;
import com.zernova.runtime.core.api.config.ZernovaProperties;
import com.zernova.tck.ZernovaTestUtils;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@SmallTest
public class ZernovaFoldersUtilTestCase extends AbstractZernovaTestCase {

  @Rule
  public TemporaryFolder muleHome = new TemporaryFolder();

  @Test
  public void getsMuleHome() throws Exception {
    ZernovaTestUtils.testWithSystemProperty(ZernovaProperties.MULE_HOME_DIRECTORY_PROPERTY, muleHome.getRoot().getAbsolutePath(),
                                         () -> {
                                           File folder = ZernovaFoldersUtil.getMuleHomeFolder();
                                           assertThat(folder.getAbsolutePath(),
                                                      equalTo(muleHome.getRoot().getAbsolutePath()));
                                         });
  }
}
