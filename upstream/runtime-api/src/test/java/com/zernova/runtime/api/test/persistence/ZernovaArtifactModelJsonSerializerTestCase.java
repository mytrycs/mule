/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.test.persistence;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static com.zernova.runtime.api.deployment.meta.AbstractZernovaArtifactModel.BUNDLE_DESCRIPTOR_LOADER;
import static com.zernova.runtime.api.deployment.meta.AbstractZernovaArtifactModel.CLASS_LOADER_MODEL_LOADER_DESCRIPTOR;
import static com.zernova.runtime.api.deployment.meta.AbstractZernovaArtifactModel.ID;
import static com.zernova.runtime.api.deployment.meta.AbstractZernovaArtifactModel.MIN_MULE_VERSION;
import static com.zernova.runtime.api.deployment.meta.AbstractZernovaArtifactModel.NAME;
import static com.zernova.runtime.api.deployment.meta.AbstractZernovaArtifactModel.REQUIRED_PRODUCT;
import static com.zernova.runtime.api.deployment.meta.Product.ZERNOVA;

import com.zernova.runtime.api.deployment.meta.ZernovaDeployableModel;
import com.zernova.runtime.api.deployment.meta.ZernovaServerPluginModel;
import com.zernova.runtime.api.deployment.meta.ZernovaServiceModel;
import com.zernova.runtime.api.deployment.persistence.AbstractZernovaArtifactModelJsonSerializer;
import com.zernova.runtime.api.deployment.persistence.ZernovaApplicationModelJsonSerializer;
import com.zernova.runtime.api.deployment.persistence.ZernovaDomainModelJsonSerializer;
import com.zernova.runtime.api.deployment.persistence.ZernovaServerPluginModelJsonSerializer;
import com.zernova.runtime.api.deployment.persistence.ZernovaServiceModelJsonSerializer;

import java.util.Collection;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ZernovaArtifactModelJsonSerializerTestCase {

  private static final String DESCRIPTOR_NAME = "descriptor";
  private static final JsonObject VALID_JSON = new JsonObject();
  private static final JsonParser parser = new JsonParser();
  private static JsonObject json;

  private final AbstractZernovaArtifactModelJsonSerializer<ZernovaDeployableModel> serializer;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @BeforeClass
  public static void setUp() {
    VALID_JSON.addProperty(NAME, "someName");
    VALID_JSON.addProperty(REQUIRED_PRODUCT, ZERNOVA.toString());
    VALID_JSON.addProperty(MIN_MULE_VERSION, "someVersion");
    JsonObject id = new JsonObject();
    id.addProperty(ID, "someId");
    VALID_JSON.add(CLASS_LOADER_MODEL_LOADER_DESCRIPTOR, id);
    VALID_JSON.add(BUNDLE_DESCRIPTOR_LOADER, id);
  }

  @Before
  public void resetJson() {
    json = parser.parse(VALID_JSON.toString()).getAsJsonObject();
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        {new ZernovaApplicationModelJsonSerializer()},
        {new ZernovaDomainModelJsonSerializer()}
    });
  }

  public ZernovaArtifactModelJsonSerializerTestCase(AbstractZernovaArtifactModelJsonSerializer serializer) {
    this.serializer = serializer;
  }

  @Test
  public void redeploymentEnabledDefaultIfNotPresentIsTrue() {
    ZernovaDeployableModel muleArtifactModel = this.serializer.deserialize("{}");
    assertThat(muleArtifactModel.isRedeploymentEnabled(), is(true));
  }

  @Test
  public void configsAttributeIsNullIfNotPresent() throws Exception {
    ZernovaDeployableModel muleArtifactModel = this.serializer.deserialize("{}");
    assertThat(muleArtifactModel.getConfigs(), is(nullValue()));
  }

  @Test
  public void configsAttributeIsEmptyIfEmptyInSerialization() throws Exception {
    ZernovaDeployableModel muleArtifactModel = this.serializer.deserialize("{\'configs\':[]}");
    assertThat(muleArtifactModel.getConfigs(), is(notNullValue()));
    assertThat(muleArtifactModel.getConfigs(), is(empty()));
  }

  @Test
  public void logConfigFileAttributeIsDeserializedCorrectly() throws Exception {
    String logConfigFile = "../../../test-classes/resources/logging/custom-log4j2.xml";
    ZernovaDeployableModel muleArtifactModel = this.serializer.deserialize("{\"logConfigFile\":\"" + logConfigFile + "\"}");
    assertThat(muleArtifactModel.getLogConfigFile(), is(logConfigFile));
  }

  @Test
  public void mandatoryFieldsCheckIsTrueIfAllPresent() throws Exception {
    ZernovaDeployableModel muleArtifactModel = this.serializer.deserialize(json.toString());
    muleArtifactModel.validateModel(DESCRIPTOR_NAME);
  }

  @Test
  public void securePropertiesDeserialization() {
    ZernovaDeployableModel muleArtifactModel = this.serializer.deserialize("{" +
        "\"secureProperties\": [\"db.username\", \"db.password\"]" +
        "}");
    List<String> secretProperties = muleArtifactModel.getSecureProperties();
    assertThat(secretProperties, hasSize(2));
    assertThat(secretProperties, contains("db.username", "db.password"));
  }

  @Test
  public void mandatoryFieldsCheckIsFalseIfNameNotSet() throws Exception {
    json.remove("name");
    ZernovaDeployableModel muleArtifactModel = this.serializer.deserialize(json.toString());
    expectedException.expect(IllegalStateException.class);
    muleArtifactModel.validateModel(DESCRIPTOR_NAME);
  }

  @Test
  public void mandatoryFieldsCheckIsFalseIfMinZernovaVersionNotSet() throws Exception {
    json.remove("minZernovaVersion");
    ZernovaDeployableModel muleArtifactModel = this.serializer.deserialize(json.toString());
    expectedException.expect(IllegalStateException.class);
    muleArtifactModel.validateModel(DESCRIPTOR_NAME);
  }

  @Test
  public void mandatoryFieldsCheckIsFalseIfClassLoaderModelLoaderDescriptorNotSet() throws Exception {
    json.remove("classLoaderModelLoaderDescriptor");
    ZernovaDeployableModel muleArtifactModel = this.serializer.deserialize(json.toString());
    expectedException.expect(IllegalStateException.class);
    muleArtifactModel.validateModel(DESCRIPTOR_NAME);
  }

  @Test
  public void mandatoryFieldsCheckIsFalseIfBundleDescriptorLoaderNotSet() throws Exception {
    json.remove("bundleDescriptorLoader");
    ZernovaDeployableModel muleArtifactModel = this.serializer.deserialize(json.toString());
    expectedException.expect(IllegalStateException.class);
    muleArtifactModel.validateModel(DESCRIPTOR_NAME);
  }

  @Test
  public void mandatoryFieldsCheckIsFalseIfClassLoaderModelLoaderDescriptorWithNoId() throws Exception {
    json.remove("classLoaderModelLoaderDescriptor");
    json.add("classLoaderModelLoaderDescriptor", new JsonObject());
    ZernovaDeployableModel muleArtifactModel = this.serializer.deserialize(json.toString());
    expectedException.expect(IllegalStateException.class);
    muleArtifactModel.validateModel(DESCRIPTOR_NAME);
  }

  @Test
  public void mandatoryFieldsCheckIsFalseIfBundleDescriptorWithNoId() throws Exception {
    json.remove("bundleDescriptorLoader");
    json.add("bundleDescriptorLoader", new JsonObject());
    ZernovaDeployableModel muleArtifactModel = this.serializer.deserialize(json.toString());
    expectedException.expect(IllegalStateException.class);
    muleArtifactModel.validateModel(DESCRIPTOR_NAME);
  }

  @Test
  public void validateServerPluginModelMandatoryFields() throws Exception {
    ZernovaServerPluginModelJsonSerializer serializer = new ZernovaServerPluginModelJsonSerializer();
    ZernovaServerPluginModel muleServerPluginModel = serializer.deserialize(json.toString());
    try {
      muleServerPluginModel.validateModel(DESCRIPTOR_NAME);
      fail("Descriptor validation should have failed because of missing pluginClassName");
    } catch (Exception e) {
    }
    json.addProperty("pluginClassName", "somePluginClassName");
    muleServerPluginModel = serializer.deserialize(json.toString());
    muleServerPluginModel.validateModel(DESCRIPTOR_NAME);
  }

  @Test
  public void validateServiceModelMandatoryFields() throws Exception {
    ZernovaServiceModelJsonSerializer serializer = new ZernovaServiceModelJsonSerializer();
    ZernovaServiceModel muleServiceModel = serializer.deserialize(json.toString());
    try {
      muleServiceModel.validateModel(DESCRIPTOR_NAME);
      fail("Descriptor validation should have failed because of missing contract");
    } catch (Exception expected) {
    }
    JsonObject contract = new JsonObject();
    JsonArray contracts = new JsonArray();
    contracts.add(contract);
    json.add("contracts", contracts);
    muleServiceModel = serializer.deserialize(json.toString());
    try {
      muleServiceModel.validateModel(DESCRIPTOR_NAME);
      fail("Descriptor validation should have failed because of contract missing properties");
    } catch (Exception expected) {
    }

    contract.addProperty("serviceProviderClassName", "FooProvider");
    muleServiceModel = serializer.deserialize(json.toString());

    try {
      muleServiceModel.validateModel(DESCRIPTOR_NAME);
      fail("Descriptor validation should still have failed because of contract missing properties");
    } catch (Exception expected) {
    }

    contract.addProperty("contractClassName", "FooService");
    muleServiceModel = serializer.deserialize(json.toString());
    muleServiceModel.validateModel(DESCRIPTOR_NAME);
  }
}
