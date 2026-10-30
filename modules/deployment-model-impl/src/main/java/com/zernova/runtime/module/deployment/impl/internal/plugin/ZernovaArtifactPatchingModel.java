/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.impl.internal.plugin;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.core.internal.util.jar.JarLoadingUtils.loadFileContentFrom;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.zernova.runtime.api.artifact.ArtifactCoordinates;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ZernovaArtifactPatchingModel {

  private static final String MULE_ARTIFACT_PATCH_JSON_FILE_NAME = "mule-artifact-patch.json";

  private static final Map<String, ZernovaArtifactPatchingModel> loadedModelByJar = new HashMap<>();

  private ArtifactCoordinates artifactCoordinates;
  private List<String> affectedVersions;

  public static synchronized ZernovaArtifactPatchingModel loadModel(File pluginPatchJarFile) throws IOException {
    final String key = pluginPatchJarFile.toString();
    if (!loadedModelByJar.containsKey(key)) {
      Optional<byte[]> muleArtifactPatchContent =
          loadFileContentFrom(pluginPatchJarFile, MULE_ARTIFACT_PATCH_JSON_FILE_NAME);
      muleArtifactPatchContent.map(bytes -> {
        ZernovaArtifactPatchingModel artifactPatchingModel = deserialize(bytes);
        loadedModelByJar.put(key, artifactPatchingModel);
        return artifactPatchingModel;
      })
          .orElseThrow(() -> new ZernovaRuntimeException(createStaticMessage(format("Invalid jar file '%s'. It does not contain descriptor '%s'",
                                                                                 pluginPatchJarFile,
                                                                                 MULE_ARTIFACT_PATCH_JSON_FILE_NAME))));
    }
    return loadedModelByJar.get(key);
  }

  private static ZernovaArtifactPatchingModel deserialize(byte[] muleArtifactPatchBytes) {
    Gson gson = (new GsonBuilder()).enableComplexMapKeySerialization().setPrettyPrinting()
        .registerTypeAdapter(ArtifactCoordinates.class, new ArtifactCoordinatesAdapter())
        .create();
    return gson.fromJson(new String(muleArtifactPatchBytes), ZernovaArtifactPatchingModel.class);
  }

  public ArtifactCoordinates getArtifactCoordinates() {
    return artifactCoordinates;
  }

  public void setArtifactCoordinates(ArtifactCoordinates artifactCoordinates) {
    this.artifactCoordinates = artifactCoordinates;
  }

  public List<String> getAffectedVersions() {
    return affectedVersions;
  }

  public void setAffectedVersions(List<String> affectedVersions) {
    this.affectedVersions = affectedVersions;
  }

  public static class ArtifactCoordinatesAdapter implements JsonDeserializer<ArtifactCoordinates> {

    public ArtifactCoordinates deserialize(JsonElement jsonElement, Type type,
                                           JsonDeserializationContext jsonDeserializationContext)
        throws JsonParseException {

      JsonObject jsonObject = jsonElement.getAsJsonObject();
      String groupId = jsonObject.get("groupId").getAsString();
      String artifactId = jsonObject.get("artifactId").getAsString();
      Optional<String> classifier = ofNullable(jsonObject.get("classifier")).map(JsonElement::getAsString);

      return new ArtifactCoordinates() {

        @Override
        public String getGroupId() {
          return groupId;
        }

        @Override
        public String getArtifactId() {
          return artifactId;
        }

        @Override
        public String getVersion() {
          return null;
        }

        @Override
        public Optional<String> getClassifier() {
          return classifier;
        }
      };
    }
  }
}

