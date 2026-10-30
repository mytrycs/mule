/*
 * Copyright (c) 2026 ZerNova.
 * The software in this file is published under the terms of the CPAL v1.0
 * license, a copy of which is included in the root LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
/*
 * Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0
 * Internal bridge view. This is not an implementation of a nonexistent SDK API.
 */
package com.zernova.runtime.http.support.internal.message;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
public interface Part {
  InputStream getInputStream() throws IOException;
  String getContentType();
  String getName();
  long getSize();
  String getHeader(String name);
  Collection<String> getHeaders(String name);
  Collection<String> getHeaderNames();
  String getFileName();
}
