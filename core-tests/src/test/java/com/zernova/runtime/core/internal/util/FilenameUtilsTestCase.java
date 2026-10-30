/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util;

import static com.zernova.runtime.core.internal.util.FilenameUtils.normalizeDecodedPath;

import static java.io.File.separator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import org.junit.Test;

@SmallTest
public class FilenameUtilsTestCase extends AbstractZernovaTestCase {

  @Test
  public void doesNotAddSlashAtBeginningInWindows() {
    String path = "file:/C:/ProgramFiles/zaraza";
    assertThat(normalizeDecodedPath(path, true), is("C:/ProgramFiles/zaraza"));
  }

  @Test
  public void addsSlashAtBeginningInUnix() {
    String path = "file:/etc/zaraza";
    assertThat(normalizeDecodedPath(path, false),
               is(separator + "etc/zaraza"));
  }

  @Test
  public void addsSlashAtBeginningInWindowsWhenSharedPath() {
    String path = "file://SERVER/zaraza";
    assertThat(normalizeDecodedPath(path, true), is(separator + "/SERVER/zaraza"));
  }

}
