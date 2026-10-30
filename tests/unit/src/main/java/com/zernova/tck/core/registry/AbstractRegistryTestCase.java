/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.core.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.zernova.runtime.core.internal.registry.Registry;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public abstract class AbstractRegistryTestCase extends AbstractZernovaTestCase {

  public abstract Registry getRegistry() throws Exception;

  @Test
  public void testNotFoundCalls() throws Exception {
    Registry r = getRegistry();
    Map<String, IOException> map = r.lookupByType(IOException.class);
    assertNotNull(map);
    assertEquals(0, map.size());

    IOException object = r.lookupObject(IOException.class);
    assertNull(object);

    object = r.lookupObject("foooooo");
    assertNull(object);

    Collection<IOException> list = r.lookupObjects(IOException.class);
    assertNotNull(list);
    assertEquals(0, list.size());
  }
}
