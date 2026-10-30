/*
 * Copyright (c) 2026 ZerNova.
 * The software in this file is published under the terms of the CPAL v1.0
 * license, a copy of which is included in the root LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/*
 * Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
 * The published SDK exposes bytes/streams; this internal extension preserves
 * structured parts when an engine entity makes a round trip through that SDK.
 */
package com.zernova.runtime.http.support.internal.message;
import java.io.IOException;
import java.util.Collection;
public interface MultipartEntityView extends com.zernova.sdk.api.http.domain.entity.HttpEntity {
  boolean isComposed();
  Collection<Part> getParts() throws IOException;
}
