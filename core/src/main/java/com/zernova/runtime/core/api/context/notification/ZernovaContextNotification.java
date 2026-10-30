/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.context.notification;

import com.zernova.api.annotation.NoInstantiate;
import com.zernova.runtime.api.notification.AbstractServerNotification;
import com.zernova.runtime.core.api.ZernovaContext;

/**
 * <code>ZernovaContextNotification</code> is fired when an event such as the mule context starting occurs. The payload of this event
 * will always be a reference to the muleContext.
 */
@NoInstantiate
public final class ZernovaContextNotification extends AbstractServerNotification {

  /**
   * Serial version
   */
  private static final long serialVersionUID = -3246036188011581121L;

  public static final int CONTEXT_INITIALISING = CONTEXT_EVENT_ACTION_START_RANGE + 1;
  public static final int CONTEXT_INITIALISED = CONTEXT_EVENT_ACTION_START_RANGE + 2;
  public static final int CONTEXT_STARTING = CONTEXT_EVENT_ACTION_START_RANGE + 3;
  public static final int CONTEXT_STARTED = CONTEXT_EVENT_ACTION_START_RANGE + 4;
  public static final int CONTEXT_STOPPING = CONTEXT_EVENT_ACTION_START_RANGE + 5;
  public static final int CONTEXT_STOPPED = CONTEXT_EVENT_ACTION_START_RANGE + 6;
  public static final int CONTEXT_DISPOSING = CONTEXT_EVENT_ACTION_START_RANGE + 7;
  public static final int CONTEXT_DISPOSED = CONTEXT_EVENT_ACTION_START_RANGE + 8;

  static {
    registerAction("mule context initialising", CONTEXT_INITIALISING);
    registerAction("mule context initialised", CONTEXT_INITIALISED);
    registerAction("mule context starting", CONTEXT_STARTING);
    registerAction("mule context started", CONTEXT_STARTED);
    registerAction("mule context stopping", CONTEXT_STOPPING);
    registerAction("mule context stopped", CONTEXT_STOPPED);
    registerAction("mule context disposing", CONTEXT_DISPOSING);
    registerAction("mule context disposed", CONTEXT_DISPOSED);
  }

  private String clusterId;
  private String domain;
  private transient ZernovaContext muleContext;

  public ZernovaContextNotification(ZernovaContext context, String action) {
    this(context, getActionId(action));
  }

  public ZernovaContextNotification(ZernovaContext context, int action) {
    super(context.getId(), action);
    this.muleContext = context;
    this.resourceIdentifier = context.getConfiguration().getId();
    this.clusterId = context.getClusterId();
    this.domain = context.getConfiguration().getDomainId();
  }

  public String getClusterId() {
    return clusterId;
  }

  public String getDomain() {
    return domain;
  }

  public ZernovaContext getMuleContext() {
    return this.muleContext;
  }

  @Override
  public String toString() {
    return getEventName() + "{" + "action=" + getActionName(action) + ", resourceId=" + resourceIdentifier + ", timestamp="
        + timestamp + "}";
  }

  @Override
  public boolean isSynchronous() {
    return true;
  }

  @Override
  public String getEventName() {
    return "ZernovaContextNotification";
  }
}
