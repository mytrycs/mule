/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.extension;

import com.zernova.runtime.api.meta.model.ModelProperty;

/**
 * Declares that parameters with this property may have its value set to an expression without the {@code #[...]} markers and
 * still be considered an expression.
 * <p>
 *
 * @since 1.4
 * @deprecated This exists for backwards compatibility with previously existing components.
 */
@Deprecated
public class AllowsExpressionWithoutMarkersModelProperty implements ModelProperty {

  public static final String NAME = "allowsExpressionWithoutMarkers";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isPublic() {
    return false;
  }

}
