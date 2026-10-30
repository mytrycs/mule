/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.test.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.zernova.runtime.module.deployment.internal.ActionTask;
import com.zernova.runtime.module.deployment.internal.NativeLibrariesFolderDeletionActionTask;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import io.qameta.allure.Issue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@Issue("W-15894519")
public class NativeLibrariesFolderDeletionActionTaskTestCase extends AbstractZernovaTestCase {

  private static final String ARTIFACT_ID = "application-test";

  @Rule
  public TemporaryFolder nativeLibrariesFolder = new TemporaryFolder();

  @Test
  public void nativeLibrariesFolderDeletionActionTaskDeletesTheAppNativeLibrariesFolderWhenExistsAndReturnsTrue() {
    ActionTask nativeLibrariesFolderDeletionActionTask =
        new NativeLibrariesFolderDeletionActionTask(ARTIFACT_ID, nativeLibrariesFolder.getRoot());

    assertTrue(nativeLibrariesFolder.getRoot().exists());
    assertTrue(nativeLibrariesFolderDeletionActionTask.tryAction());
    assertFalse(nativeLibrariesFolder.getRoot().exists());
  }
}
