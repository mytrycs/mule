/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.api.classloader;

import static com.zernova.runtime.core.api.util.ClassUtils.getResource;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.apache.commons.io.IOUtils.readLines;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;

import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

public class ZernovaArtifactClassLoaderTestCase extends AbstractZernovaTestCase {

  private static final String API_RESOURCE_NAME = "test-api.raml";
  private static final String JAR_RESOURCE_NAME = "hello.txt";
  private static final String API_LINE = "#%RAML 1.0";

  private final URL apiLocation;
  private final URL apiLibraryLocation;
  private final URL jarLocation;
  private final URL testsJarLocation;

  public ZernovaArtifactClassLoaderTestCase() {
    apiLocation = getResource("com/organization/test-artifact/1.0.0/test-artifact-1.0.0-raml.zip", this.getClass());
    apiLibraryLocation =
        getResource("com/organization/test-artifact/1.0.0/test-artifact-1.0.0-raml-fragment.zip", this.getClass());
    jarLocation = getResource("com/organization/test-artifact/1.0.0/test-artifact-1.0.0.jar", this.getClass());
    testsJarLocation = getResource("com/organization/test-artifact/1.0.0/test-artifact-1.0.0-tests.jar", this.getClass());
  }

  @Test
  public void findsResourcesFromSpecificInnerZipArtifacts() throws Exception {
    ZernovaArtifactClassLoader classLoader = createClassLoader();

    findAndValidate(classLoader, "resource::com.organization:test-artifact:1.0.0:raml:zip", API_RESOURCE_NAME, apiLocation,
                    API_LINE);
    findAndValidate(classLoader, "resource::com.organization:test-artifact:1.0.0:raml-fragment:zip", "reused-fragment.raml",
                    apiLibraryLocation, "#%RAML 1.0 Library");
  }

  @Test
  public void findsResourcesFromSpecificInnerJarArtifacts() throws Exception {
    ZernovaArtifactClassLoader classLoader = createClassLoader();

    findAndValidate(classLoader, "resource::com.organization:test-artifact:1.0.0::jar", JAR_RESOURCE_NAME, jarLocation,
                    "Hello, world!");
    findAndValidate(classLoader, "resource::com.organization:test-artifact:1.0.0:tests:jar", JAR_RESOURCE_NAME, testsJarLocation,
                    "Aloha, world!");
  }

  @Test
  public void findsResourceFromSpecificInnerArtifactWithoutVersion() throws Exception {
    ZernovaArtifactClassLoader classLoader = createClassLoader();
    findAndValidate(classLoader, "resource::com.organization:test-artifact:*:raml:zip", API_RESOURCE_NAME, apiLocation, API_LINE);
  }

  @Test
  public void cannotFindResourceFromSpecificInnerArtifactWithWrongVersion() {
    ZernovaArtifactClassLoader classLoader = createClassLoader();
    assertThat(classLoader.findResource("resource::com.organization:test-artifact:1.3:raml:zip:test-api.raml"), is(nullValue()));
  }

  @Test
  public void findsResourceFromSpecificInnerArtifactAfterWildcardRequest() throws Exception {
    ZernovaArtifactClassLoader classLoader = createClassLoader();
    findAndValidate(classLoader, "resource::com.organization:test-artifact:*:raml:zip", API_RESOURCE_NAME, apiLocation, API_LINE);
    findAndValidate(classLoader, "resource::com.organization:test-artifact:1.0.0:raml:zip", API_RESOURCE_NAME, apiLocation,
                    API_LINE);
  }

  @Test
  public void findsResourceFromSpecificInnerArtifactAfterFullRequest() throws Exception {
    ZernovaArtifactClassLoader classLoader = createClassLoader();
    findAndValidate(classLoader, "resource::com.organization:test-artifact:1.0.0:raml:zip", API_RESOURCE_NAME, apiLocation,
                    API_LINE);
    findAndValidate(classLoader, "resource::com.organization:test-artifact:*:raml:zip", API_RESOURCE_NAME, apiLocation, API_LINE);
  }

  private ZernovaArtifactClassLoader createClassLoader() {
    return new ZernovaArtifactClassLoader("test", mock(ArtifactDescriptor.class),
                                       new URL[] {apiLocation, apiLibraryLocation, jarLocation, testsJarLocation},
                                       null, mock(ClassLoaderLookupPolicy.class));
  }

  private void findAndValidate(ZernovaArtifactClassLoader classLoader, String request, String resourceName,
                               URL expectedArtifactLocation, String expectedLine)
      throws IOException {
    URL resource = classLoader.findResource(request + ":" + resourceName);
    assertThat(resource, is(notNullValue()));
    assertThat(resource, is(equalTo(new URL("jar:" + expectedArtifactLocation.toString() + "!/" + resourceName))));
    assertThat(resource.openConnection().getUseCaches(), is(false));
    assertThat(readLines(resource.openStream(), UTF_8).get(0), is(expectedLine));
  }

}
