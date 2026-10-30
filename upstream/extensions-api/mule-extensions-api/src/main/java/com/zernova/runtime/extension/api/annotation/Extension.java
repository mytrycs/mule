/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.annotation;

import com.zernova.runtime.api.meta.Category;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation defines a class that will export its functionality as a Mule module.
 * <p/>
 * There are a few restrictions as to which types as valid for this annotation:
 * <ul>
 * <li>It cannot be an interface</li>
 * <li>It must be public</li>
 * <li>It cannot have a typed parameter (no generic)</li>
 * </ul>
 *
 * @since 1.0
 */
@MinZernovaVersion("4.1")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Extension {

  /**
   * A name consistent with the definition on {@link ExtensionModel#getName()}
   */
  String name();

  /**
   * Name of the extension's vendor
   */
  String vendor() default MULESOFT;

  /**
   * Extension's {@link Category}
   */
  Category category() default Category.COMMUNITY;

  String MULESOFT = "Mulesoft";

  String DEFAULT_CONFIG_NAME = "config";

  String DEFAULT_CONFIG_DESCRIPTION = "Default configuration";
}
