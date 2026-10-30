/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package org.foo;

import com.zernova.runtime.module.artifact.api.classloader.ResourceReleaser;
import com.zernova.sdk.api.artifact.lifecycle.ArtifactDisposalContext;

import java.util.function.Consumer;

public class ArtifactDisposalTracker implements ResourceReleaser {

  private static Runnable onLegacyReleaser;
  private static Consumer<ArtifactDisposalContext> onArtifactDisposal;

  public static void setOnLegacyReleaser(Runnable onLegacyReleaser) {
    ArtifactDisposalTracker.onLegacyReleaser = onLegacyReleaser;
  }

  public static void setOnArtifactDisposalCallback(Consumer<ArtifactDisposalContext> onArtifactDisposal) {
    ArtifactDisposalTracker.onArtifactDisposal = onArtifactDisposal;
  }

  public static void onArtifactDisposal(ArtifactDisposalContext artifactDisposalContext) {
    if (onArtifactDisposal != null) {
      onArtifactDisposal.accept(artifactDisposalContext);
    }
  }

  public void release() {
    if (onLegacyReleaser != null) {
      onLegacyReleaser.run();
    }
  }
}
