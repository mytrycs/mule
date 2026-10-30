/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.event;

import static com.zernova.runtime.api.el.BindingContextUtils.NULL_BINDING_CONTEXT;
import static com.zernova.runtime.api.el.BindingContextUtils.addEventBindings;

import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.api.message.ItemSequenceInfo;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.security.Authentication;
import com.zernova.runtime.api.security.SecurityContext;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.core.api.context.notification.FlowCallStack;
import com.zernova.runtime.core.api.message.GroupCorrelation;
import com.zernova.runtime.core.internal.message.EventInternalContext;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.core.privileged.event.context.FlowProcessMediatorContext;

import java.util.Map;
import java.util.Optional;

abstract class BaseEventDecorator implements InternalEvent {

  private static final long serialVersionUID = 2264829044803742047L;

  private final InternalEvent event;

  private transient LazyValue<BindingContext> bindingContextBuilder =
      new LazyValue<>(() -> addEventBindings(this, NULL_BINDING_CONTEXT));

  public BaseEventDecorator(InternalEvent event) {
    this.event = event;
  }

  protected InternalEvent getEvent() {
    return event;
  }

  @Override
  public BaseEventContext getContext() {
    return event.getContext();
  }

  @Override
  public boolean isNotificationsEnabled() {
    return event.isNotificationsEnabled();
  }

  @Override
  public SecurityContext getSecurityContext() {
    return event.getSecurityContext();
  }

  @Override
  public Optional<GroupCorrelation> getGroupCorrelation() {
    return event.getGroupCorrelation();
  }

  @Override
  public FlowCallStack getFlowCallStack() {
    return event.getFlowCallStack();
  }

  @Override
  public Map<String, TypedValue<?>> getVariables() {
    return event.getVariables();
  }

  @Override
  public Map<String, TypedValue<?>> getParameters() {
    return event.getParameters();
  }

  @Override
  public Optional<Map<String, String>> getLoggingVariables() {
    return event.getLoggingVariables();
  }

  @Override
  public Message getMessage() {
    return event.getMessage();
  }

  @Override
  public Optional<Authentication> getAuthentication() {
    return event.getAuthentication();
  }

  @Override
  public Optional<Error> getError() {
    return event.getError();
  }

  @Override
  public Optional<ItemSequenceInfo> getItemSequenceInfo() {
    return event.getItemSequenceInfo();
  }

  @Override
  public String getCorrelationId() {
    return getLegacyCorrelationId() != null ? getLegacyCorrelationId() : getContext().getCorrelationId();
  }

  @Override
  public String getLegacyCorrelationId() {
    return event.getLegacyCorrelationId();
  }

  @Override
  public BindingContext asBindingContext() {
    return bindingContextBuilder.get();
  }

  @Override
  public Map<String, ?> getInternalParameters() {
    return event.getInternalParameters();
  }

  @Override
  public <T> T getInternalParameter(String key) {
    return event.getInternalParameter(key);
  }

  @Override
  public FlowProcessMediatorContext getFlowProcessMediatorContext() {
    return event.getFlowProcessMediatorContext();
  }

  @Override
  public void setFlowProcessMediatorContext(FlowProcessMediatorContext flowProcessMediatorContext) {
    event.setFlowProcessMediatorContext(flowProcessMediatorContext);
  }

  @Override
  public <T extends EventInternalContext> EventInternalContext<T> getSdkInternalContext() {
    return event.getSdkInternalContext();
  }

  @Override
  public <T extends EventInternalContext> void setSdkInternalContext(EventInternalContext<T> context) {
    event.setSdkInternalContext(context);
  }

  @Override
  public <T extends EventInternalContext> EventInternalContext<T> getForeachInternalContext() {
    return event.getForeachInternalContext();
  }

  @Override
  public <T extends EventInternalContext> void setForeachInternalContext(EventInternalContext<T> context) {
    event.setForeachInternalContext(context);
  }

  @Override
  public <T extends EventInternalContext> EventInternalContext<T> getSourcePolicyContext() {
    return event.getSourcePolicyContext();
  }

  @Override
  public <T extends EventInternalContext> void setSourcePolicyContext(EventInternalContext<T> context) {
    event.setSourcePolicyContext(context);
  }

  @Override
  public <T extends EventInternalContext> EventInternalContext<T> getOperationPolicyContext() {
    return event.getOperationPolicyContext();
  }

  @Override
  public <T extends EventInternalContext> void setOperationPolicyContext(EventInternalContext<T> context) {
    event.setOperationPolicyContext(context);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "->" + event.toString();
  }
}
