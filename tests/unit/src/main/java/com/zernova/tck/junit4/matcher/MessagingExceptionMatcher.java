/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.tck.junit4.matcher;

import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.core.privileged.exception.MessagingException;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * {@link Matcher} for {@link MessagingException} instances.
 *
 * @since 4.0
 */
public class MessagingExceptionMatcher extends TypeSafeMatcher<MessagingException> {

  private EventMatcher eventMatcher;
  private Matcher<Component> componentMatcher;

  public MessagingExceptionMatcher(EventMatcher eventMatcher, Matcher<Component> componentMatcher) {
    this.eventMatcher = eventMatcher;
    this.componentMatcher = componentMatcher;
  }

  public static MessagingExceptionMatcher withEventThat(EventMatcher eventMatcher) {
    return new MessagingExceptionMatcher(eventMatcher, null);
  }

  public static MessagingExceptionMatcher withFailingComponent(Matcher<Component> componentMatcher) {
    return new MessagingExceptionMatcher(null, componentMatcher);
  }

  @Override
  protected boolean matchesSafely(MessagingException item) {
    return (eventMatcher != null ? eventMatcher.matches(item.getEvent()) : true)
        && (componentMatcher != null ? componentMatcher.matches(item.getFailingComponent()) : true);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("a MessagingException with ");
    if (eventMatcher != null) {
      eventMatcher.describeTo(description);
    }
    if (componentMatcher != null) {
      componentMatcher.describeTo(description);
    }
  }

}
