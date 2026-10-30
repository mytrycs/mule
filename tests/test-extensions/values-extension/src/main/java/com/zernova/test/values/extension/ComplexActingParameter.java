/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.values.extension;

import java.util.List;
import java.util.Map;

public class ComplexActingParameter {

  private int intParam;
  private String stringParam;
  private List<String> listParam;
  private Map<String, String> mapParam;

  private InnerPojo innerPojoParam;
  private List<InnerPojo> complexListParam;
  private Map<String, InnerPojo> complexMapParam;

  public int getIntParam() {
    return intParam;
  }

  public String getStringParam() {
    return stringParam;
  }

  public List<String> getListParam() {
    return listParam;
  }

  public InnerPojo getInnerPojoParam() {
    return innerPojoParam;
  }

  public Map<String, String> getMapParam() {
    return mapParam;
  }

  public List<InnerPojo> getComplexListParam() {
    return complexListParam;
  }

  public Map<String, InnerPojo> getComplexMapParam() {
    return complexMapParam;
  }
}
