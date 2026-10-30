/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.internal.util;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;

import java.io.File;
import java.io.IOException;

public class FileUtils {

  /**
   * Workaround for JDK bug <a href="http://bugs.sun.com/bugdatabase/view_bug.do;:YfiG?bug_id=4117557"> 4117557</a>. More
   * in-context information at <a href="http://mule.mulesoft.org/jira/browse/MULE-1112">MULE-1112</a>
   * <p/>
   * Factory methods correspond to constructors of the <code>java.io.File class</code>. No physical file created in this method.
   *
   * @see File
   */
  public static File newFile(String pathName) {
    try {
      return new File(pathName).getCanonicalFile();
    } catch (IOException e) {
      throw new ZernovaRuntimeException(createStaticMessage("Unable to create a canonical file for " + pathName),
                                     e);
    }
  }

}
