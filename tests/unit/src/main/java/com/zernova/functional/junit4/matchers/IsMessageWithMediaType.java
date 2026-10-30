/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.functional.junit4.matchers;

import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.MediaType;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Verifies a {@link Message}'s media type.
 *
 * @since 4.0
 */
public class IsMessageWithMediaType extends TypeSafeMatcher<Message> {

  private final MediaType expectedMediaType;

  public IsMessageWithMediaType(MediaType mediaType) {
    this.expectedMediaType = mediaType;
  }


  @Override
  protected boolean matchesSafely(Message message) {
    return expectedMediaType.equals(message.getPayload().getDataType().getMediaType());
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("message with media type ").appendValue(expectedMediaType);
  }

  @Override
  protected void describeMismatchSafely(Message message, Description mismatchDescription) {
    mismatchDescription.appendText("got a message with media type ");
    mismatchDescription.appendValue(message.getPayload().getDataType().getMediaType());
  }
}
