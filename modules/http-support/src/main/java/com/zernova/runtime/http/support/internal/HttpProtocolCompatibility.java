/*
 * Copyright (c) 2026 ZerNova.
 * The software in this file is published under the terms of the CPAL v1.0
 * license, a copy of which is included in the root LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/*
 * Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
 * Compatibility addition to the CPAL-covered HTTP bridge. See LICENSE.txt.
 */
package com.zernova.runtime.http.support.internal;

import java.util.Objects;
import java.util.function.Consumer;
import com.zernova.sdk.api.http.protocol.Http1ProtocolConfigurer;
import com.zernova.sdk.api.http.protocol.Http2ProtocolConfigurer;

/** Explicit capabilities of this HTTP/1.1 engine; never silently downgrade HTTP/2. */
public final class HttpProtocolCompatibility {
  private HttpProtocolCompatibility() {}

  public static void http1(Consumer<Http1ProtocolConfigurer> callback) {
    Objects.requireNonNull(callback, "HTTP/1 configuration callback").accept(new Http1());
  }

  public static void http2(Consumer<Http2ProtocolConfigurer> callback) {
    Objects.requireNonNull(callback, "HTTP/2 configuration callback").accept(new Http2());
  }

  private static UnsupportedOperationException unavailable() {
    return new UnsupportedOperationException("ZERNOVA 4.10 source baseline supports HTTP/1.1, not HTTP/2. No protocol downgrade was performed.");
  }

  private static final class Http1 implements Http1ProtocolConfigurer {
    @Override
    public Http1ProtocolConfigurer setEnabled(boolean enabled) {
      if (!enabled) {
        throw new IllegalArgumentException("HTTP/1.1 cannot be disabled: it is the supported protocol in this ZERNOVA baseline.");
      }
      return this;
    }
  }

  private static final class Http2 implements Http2ProtocolConfigurer {
    @Override
    public Http2ProtocolConfigurer setEnabled(boolean enabled) {
      if (enabled) { throw unavailable(); }
      return this;
    }
    @Override public Http2ProtocolConfigurer setHeaderTableSize(Long value) { throw unavailable(); }
    @Override public Http2ProtocolConfigurer setMaxConcurrentStreams(Long value) { throw unavailable(); }
    @Override public Http2ProtocolConfigurer setInitialWindowSize(Integer value) { throw unavailable(); }
    @Override public Http2ProtocolConfigurer setMaxFrameSize(Integer value) { throw unavailable(); }
    @Override public Http2ProtocolConfigurer setMaxHeaderListSize(Long value) { throw unavailable(); }
  }
}
