/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.plugin;

import static com.zernova.maven.client.api.VersionUtils.discoverVersionUtils;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.container.api.ZernovaFoldersUtil.getMuleHomeFolder;
import static com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor.MULE_PLUGIN_CLASSIFIER;

import static java.lang.String.format;
import static java.nio.file.Files.walk;
import static java.util.stream.Collectors.toList;

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.maven.client.api.VersionUtils;
import com.zernova.runtime.api.artifact.ArtifactCoordinates;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.module.artifact.activation.internal.plugin.PluginPatchesResolver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;

/**
 * Default implementation of {@link PluginPatchesResolver}.
 *
 * @since 4.5
 */
public class DefaultPluginPatchesResolver implements PluginPatchesResolver {

  private static final Logger LOGGER = getLogger(DefaultPluginPatchesResolver.class);

  private static final String MULE_ARTIFACT_PATCHES_LOCATION = "lib/patches/zernova-artifact-patches";
  private static final String PATCH_FILES_EXTENSION = "jar";

  @Override
  public List<URL> resolve(ArtifactCoordinates pluginArtifactCoordinates) {
    List<URL> patches = new ArrayList<>();

    String artifactId = pluginArtifactCoordinates.getGroupId() + ":"
        + pluginArtifactCoordinates.getArtifactId() + ":" + pluginArtifactCoordinates.getVersion();

    File muleArtifactPatchesFolder = new File(getMuleHomeFolder(), MULE_ARTIFACT_PATCHES_LOCATION);
    try {
      if (muleArtifactPatchesFolder.exists()) {
        final VersionUtils versionUtils = discoverVersionUtils(this.getClass().getClassLoader());

        patches = walk(muleArtifactPatchesFolder.toPath())
            .filter(patchFilePath -> getExtension(patchFilePath.toString()).endsWith(PATCH_FILES_EXTENSION))
            .map(patchFilePath -> {
              try {
                ZernovaArtifactPatchingModel muleArtifactPatchingModel = ZernovaArtifactPatchingModel.loadModel(patchFilePath.toFile());
                ArtifactCoordinates patchedArtifactCoordinates = muleArtifactPatchingModel.getArtifactCoordinates();

                if (patchedArtifactCoordinates.getGroupId().equals(pluginArtifactCoordinates.getGroupId()) &&
                    patchedArtifactCoordinates.getArtifactId().equals(pluginArtifactCoordinates.getArtifactId()) &&
                    patchedArtifactCoordinates.getClassifier().map(MULE_PLUGIN_CLASSIFIER::equals).orElse(false)) {
                  boolean versionContained;
                  try {
                    versionContained = versionUtils
                        .containsVersion(pluginArtifactCoordinates.getVersion(), muleArtifactPatchingModel.getAffectedVersions());
                  } catch (IllegalArgumentException e) {
                    throw new ZernovaRuntimeException(createStaticMessage(e.getMessage()
                        + ", patches against this artifact will not be applied"), e);
                  }

                  if (versionContained) {
                    LOGGER.info("Patching artifact '{}' with patch file '{}'", artifactId, patchFilePath);

                    return patchFilePath.toFile().toURI().toURL();
                  }
                }
              } catch (IOException e) {
                throw new ZernovaRuntimeException(createStaticMessage(format("There was an error processing the patch in '%s' file",
                                                                          patchFilePath)),
                                               e);
              }

              return null;
            })
            .filter(Objects::nonNull)
            .collect(toList());
      }
    } catch (Exception e) {
      throw new ZernovaRuntimeException(createStaticMessage(format("There was an error processing the patches in '%s'",
                                                                muleArtifactPatchesFolder)),
                                     e);
    }

    return patches;
  }

}
