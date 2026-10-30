/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.test.persistence;


import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import com.zernova.runtime.api.deployment.meta.ZernovaArtifactLoaderDescriptor;
import com.zernova.runtime.api.deployment.meta.ZernovaPolicyModel;
import com.zernova.runtime.api.deployment.meta.ZernovaPolicyModel.ZernovaPolicyModelBuilder;
import com.zernova.runtime.api.deployment.persistence.ZernovaPolicyModelJsonSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;

import org.junit.Before;
import org.junit.Test;

public class ZernovaPolicyModelJsonSerializerTestCase extends AbstractZernovaArtifactModelJsonSerializerTestCase {

  private ZernovaPolicyModelJsonSerializer mulePolicyModelJsonSerializer;

  @Before
  public void setUp() {
    mulePolicyModelJsonSerializer = new ZernovaPolicyModelJsonSerializer();
  }

  @Test
  public void serializesModel() throws IOException {
    final String describerName = "policy-name";
    final String describerMinZernovaVersion = "4.2.3";
    final String describerClassLoaderModelId = "classLoaderModelLoaderID";

    Map<String, Object> bundleDescriptorAttributes = new HashMap<>();
    bundleDescriptorAttributes.put(BUNDLE_DESCRIPTOR_LOADER_KEY_1, BUNDLE_DESCRIPTOR_LOADER_VALUE_1);
    bundleDescriptorAttributes.put(BUNDLE_DESCRIPTOR_LOADER_KEY_2, BUNDLE_DESCRIPTOR_LOADER_VALUE_2);

    final ZernovaPolicyModelBuilder mulePolicyModelBuilder =
        new ZernovaPolicyModelBuilder()
            .setName(describerName)
            .setMinZernovaVersion(describerMinZernovaVersion)
            .withBundleDescriptorLoader(new ZernovaArtifactLoaderDescriptor(BUNDLE_DESCRIPTOR_LOADER_ID,
                                                                         bundleDescriptorAttributes));
    mulePolicyModelBuilder
        .withClassLoaderModelDescriptorLoader(new ZernovaArtifactLoaderDescriptor(describerClassLoaderModelId, emptyMap()));
    mulePolicyModelBuilder.setSupportedJavaVersions(singleton("17"));

    String actual = mulePolicyModelJsonSerializer.serialize(mulePolicyModelBuilder.build());
    final JsonObject actualElement = new JsonParser().parse(actual).getAsJsonObject();
    assertThat(actualElement.get("name").getAsString(), is(describerName));
    assertThat(actualElement.get("minZernovaVersion").getAsString(), is(describerMinZernovaVersion));

    JsonObject classLoaderModelDescriptor = actualElement.get("classLoaderModelLoaderDescriptor").getAsJsonObject();
    assertThat(classLoaderModelDescriptor.get(ID).getAsString(), is(describerClassLoaderModelId));
    assertThat(classLoaderModelDescriptor.get(ATTRIBUTES).getAsJsonObject().entrySet(), is(empty()));

    JsonObject bundleDescriptor = actualElement.get("bundleDescriptorLoader").getAsJsonObject();
    assertThat(bundleDescriptor.get("id").getAsString(), is(BUNDLE_DESCRIPTOR_LOADER_ID));
    assertThat(bundleDescriptor.get(ATTRIBUTES).getAsJsonObject().entrySet().size(), equalTo(2));
    assertThat(bundleDescriptor.get(ATTRIBUTES).getAsJsonObject().get(BUNDLE_DESCRIPTOR_LOADER_KEY_1).getAsString(),
               equalTo(BUNDLE_DESCRIPTOR_LOADER_VALUE_1));
    assertThat(bundleDescriptor.get(ATTRIBUTES).getAsJsonObject().get(BUNDLE_DESCRIPTOR_LOADER_KEY_2).getAsString(),
               equalTo(BUNDLE_DESCRIPTOR_LOADER_VALUE_2));

    final JsonArray supportedJavaVersions = actualElement.getAsJsonArray("supportedJavaVersions");
    assertThat(supportedJavaVersions.get(0).getAsString(), equalTo("17"));
  }

  @Test
  public void deserializesModel() throws IOException {
    InputStream resource = getClass().getResourceAsStream("/descriptor/policy-descriptor-with-classloader-model.json");

    ZernovaPolicyModel deserialized = mulePolicyModelJsonSerializer.deserialize(IOUtils.toString(resource));

    assertNameAndMinZernovaVersion(deserialized);
    assertClassLoaderModel(deserialized);
    assertBundleDescriptorLoader(deserialized.getBundleDescriptorLoader());
  }

  @Test
  public void deserializesModelWithSupportedJavaVersions() throws IOException {
    InputStream resource = getClass().getResourceAsStream("/descriptor/policy-descriptor-with-supported-java-versions.json");

    ZernovaPolicyModel deserialized = mulePolicyModelJsonSerializer.deserialize(IOUtils.toString(resource));

    assertThat(deserialized.getSupportedJavaVersions(), containsInAnyOrder("1.8", "11", "17"));
  }

  private void assertNameAndMinZernovaVersion(ZernovaPolicyModel deserialize) {
    assertThat(deserialize.getName(), is("plugin-name"));
    assertThat(deserialize.getMinZernovaVersion(), is("4.2.3"));
  }

  private void assertClassLoaderModel(ZernovaPolicyModel deserialize) {
    final ZernovaArtifactLoaderDescriptor extensionModelDescriptor = deserialize.getClassLoaderModelLoaderDescriptor();
    assertThat(extensionModelDescriptor.getId(), is("maven"));
    assertThat(extensionModelDescriptor.getAttributes().size(), is(2));
    final Object exportedPackages = extensionModelDescriptor.getAttributes().get("exportedPackages");
    assertThat(exportedPackages, is(instanceOf(List.class)));
    assertThat((List<String>) exportedPackages, containsInAnyOrder("com.zernova.extension.api", "com.zernova.extension.api.exception"));
    final Object exportedResources = extensionModelDescriptor.getAttributes().get("exportedResources");
    assertThat(exportedResources, is(instanceOf(List.class)));
    assertThat((List<String>) exportedResources, containsInAnyOrder("/META-INF/some.file", "/META-INF/other.file"));
  }

}
