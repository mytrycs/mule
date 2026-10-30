/*
 * Copyright (c) 2026 ZerNova.
 * The software in this file is published under the terms of the CPAL v1.0
 * license, a copy of which is included in the root LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/*
 * Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
 */
package com.zernova.runtime.http.support.internal;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class HttpProtocolCompatibilityTestCase {
  @Test void http1CallbackIsInvoked() {
    AtomicBoolean invoked = new AtomicBoolean();
    HttpProtocolCompatibility.http1(config -> { config.setEnabled(true); invoked.set(true); });
    assertTrue(invoked.get());
  }
  @Test void onlySupportedProtocolCannotBeDisabled() {
    assertThrows(IllegalArgumentException.class, () -> HttpProtocolCompatibility.http1(config -> config.setEnabled(false)));
  }
  @Test void explicitHttp2DisabledIsSupported() {
    AtomicBoolean invoked = new AtomicBoolean();
    HttpProtocolCompatibility.http2(config -> { config.setEnabled(false); invoked.set(true); });
    assertTrue(invoked.get());
  }
  @Test void http2DoesNotSilentlyFallBack() {
    assertThrows(UnsupportedOperationException.class, () -> HttpProtocolCompatibility.http2(config -> config.setEnabled(true)));
  }
  @Test void unsupportedHttp2SettingsAreNotIgnored() {
    assertThrows(UnsupportedOperationException.class, () -> HttpProtocolCompatibility.http2(config -> config.setHeaderTableSize(1L)));
    assertThrows(UnsupportedOperationException.class, () -> HttpProtocolCompatibility.http2(config -> config.setMaxConcurrentStreams(1L)));
    assertThrows(UnsupportedOperationException.class, () -> HttpProtocolCompatibility.http2(config -> config.setInitialWindowSize(1)));
    assertThrows(UnsupportedOperationException.class, () -> HttpProtocolCompatibility.http2(config -> config.setMaxFrameSize(1)));
    assertThrows(UnsupportedOperationException.class, () -> HttpProtocolCompatibility.http2(config -> config.setMaxHeaderListSize(1L)));
  }
  @Test void nullCallbacksFail() {
    assertThrows(NullPointerException.class, () -> HttpProtocolCompatibility.http1(null));
    assertThrows(NullPointerException.class, () -> HttpProtocolCompatibility.http2(null));
  }
}
