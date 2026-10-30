/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.util.store;

import static com.zernova.runtime.core.api.util.FileUtils.openDirectory;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.store.ObjectDoesNotExistException;
import com.zernova.runtime.api.store.ObjectStoreException;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.internal.serialization.JavaObjectSerializer;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@SmallTest
public class PersistentObjectStorePartitionTestCase extends AbstractZernovaTestCase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Rule
  public TemporaryFolder objectStoreFolder = new TemporaryFolder();

  @Mock
  private ZernovaConfiguration muleConfiguration;

  private File workingDirectory;

  private PersistentObjectStorePartition partition;

  @Before
  public void setUp() throws Exception {
    workingDirectory = objectStoreFolder.getRoot().getParentFile();
    when(muleConfiguration.getWorkingDirectory()).thenReturn(workingDirectory.getPath());
    partition = new PersistentObjectStorePartition(muleConfiguration, new JavaObjectSerializer(this.getClass().getClassLoader()),
                                                   "test", objectStoreFolder.getRoot());
    partition.open();
  }

  @Test
  public void indicatesUnexistentKeyOnRetrieveError() throws ObjectStoreException {
    final String nonExistentKey = "nonExistentKey";

    try {
      partition.retrieve(nonExistentKey);
      fail("Supposed to thrown an exception as key is not valid");
    } catch (ObjectDoesNotExistException e) {
      assertTrue(e.getMessage().contains(nonExistentKey));
    }
  }

  @Test
  public void skipAndMoveCorruptedOrUnreadableFiles() {
    final String KEY = "key";
    final String VALUE = "value";
    try {
      File.createTempFile("temp", ".obj", objectStoreFolder.getRoot());
      File corruptedFolder = openDirectory(workingDirectory.getAbsolutePath()
          + File.separator + PersistentObjectStorePartition.CORRUPTED_FOLDER);
      int corruptedBefore = corruptedFolder.list().length;

      partition.store(KEY, VALUE);
      // Expect the new stored object, and the partition-descriptor file
      assertEquals(2, objectStoreFolder.getRoot().listFiles().length);

      // Expect to have one more corrupted file in the corrupted folder
      assertEquals(corruptedBefore + 1, corruptedFolder.list().length);
    } catch (Exception e) {
      fail("Supposed to have skipped corrupted or unreadable files");
    }
  }

  @Test
  public void skipAndMoveCorruptedOrUnreadableFilesWithoutCreatingDir() throws Exception {
    final String KEY = "key";
    final String VALUE = "value";
    File corruptedFile = File.createTempFile("temp", ".obj", objectStoreFolder.getRoot());
    File corruptedFolder = openDirectory(workingDirectory.getAbsolutePath()
        + File.separator + PersistentObjectStorePartition.CORRUPTED_FOLDER);
    deleteDirectory(corruptedFolder);

    partition.store(KEY, VALUE);
    // Expect the new stored object, and the partition-descriptor file
    assertThat(objectStoreFolder.getRoot().listFiles().length, is(2));
    // Expect to have one more corrupted file in the corrupted folder
    assertThat(corruptedFolder.list().length, is(1));
    File directory = new File(corruptedFolder.getAbsolutePath() + "/" + corruptedFolder.list()[0]);
    assertThat(directory.isDirectory(), is(true));
    assertThat(directory.listFiles().length, is(1));
    assertThat(directory.listFiles()[0].getName(), is(corruptedFile.getName()));
  }

  @Test
  public void clear() throws Exception {
    final String KEY = "key";
    final String VALUE = "value";


    partition.store(KEY, VALUE);
    assertThat(partition.contains(KEY), is(true));
    assertThat(VALUE, is(partition.retrieve(KEY)));

    partition.clear();
    assertThat(partition.contains(KEY), is(false));
    assertThat("Partition descriptor doesn't exists", new File(objectStoreFolder.getRoot(), "partition-descriptor").exists(),
               is(true));
  }

  @Test
  public void clearBeforeLoading() throws Exception {
    partition.clear();
    assertEquals(0, partition.allKeys().size());
  }
}
