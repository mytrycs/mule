/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.exception;

import static java.lang.String.format;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.i18n.I18nMessage;
import com.zernova.runtime.api.meta.NameableObject;

/**
 * {@code LocatedZernovaException} is a general exception that adds context location about the Exception (i.e.: where it occurred in
 * the application).
 *
 * @since 1.0
 */
public class LocatedZernovaException extends ZernovaException {

  /**
   * Serial version
   */
  private static final long serialVersionUID = 5100364571184418132L;

  /**
   * @param component the object that failed during a lifecycle method call
   */
  public LocatedZernovaException(Object component) {
    super();
    setLocation(component);
  }

  /**
   * @param message   the exception message
   * @param component the object that failed during a lifecycle method call
   */
  public LocatedZernovaException(I18nMessage message, Object component) {
    super(message);
    setLocation(component);
  }

  /**
   * @param message   the exception message
   * @param cause     the exception that cause this exception to be thrown
   * @param component the object that failed during a lifecycle method call
   */
  public LocatedZernovaException(I18nMessage message, Throwable cause, Object component) {
    super(message, cause);
    setLocation(component);
  }

  /**
   * @param cause     the exception that cause this exception to be thrown
   * @param component the object that failed during a lifecycle method call
   */
  public LocatedZernovaException(Throwable cause, Object component) {
    super(cause);
    setLocation(component);
  }


  protected void setLocation(Object component) {
    if (component != null) {
      addInfo(INFO_LOCATION_KEY, resolveProcessorPath(component));
    }
  }

  protected String resolveProcessorPath(Object component) {
    return ((Component) component).getRepresentation();
  }

  public static String toString(Object obj) {
    if (obj == null) {
      return "";
    }

    String str = obj.getClass().getName();
    if (obj instanceof NameableObject) {
      str += format(" '%s'", ((NameableObject) obj).getName());
    }
    return str;
  }

}
