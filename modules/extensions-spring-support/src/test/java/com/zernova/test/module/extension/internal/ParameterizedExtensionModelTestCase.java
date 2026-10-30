/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.module.extension.internal;

import static com.zernova.runtime.api.util.collection.SmallMap.of;
import static com.zernova.runtime.core.api.util.IOUtils.toByteArray;
import static com.zernova.runtime.extension.api.loader.ExtensionModelLoadingRequest.builder;
import static com.zernova.runtime.manifest.api.ZernovaManifest.getMuleManifest;
import static com.zernova.runtime.module.extension.api.resources.BaseExtensionResourcesGeneratorAnnotationProcessor.COMPILATION_MODE;
import static com.zernova.runtime.module.extension.internal.loader.java.AbstractJavaExtensionModelLoader.TYPE_PROPERTY_NAME;
import static com.zernova.runtime.module.extension.internal.loader.java.AbstractJavaExtensionModelLoader.VERSION;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toUnmodifiableSet;

import com.zernova.runtime.api.artifact.ArtifactCoordinates;
import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoadingRequest;
import com.zernova.runtime.module.extension.internal.loader.java.DefaultJavaExtensionModelLoader;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.function.TriFunction;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@SmallTest
@RunWith(Parameterized.class)
public abstract class ParameterizedExtensionModelTestCase extends AbstractZernovaTestCase {

  protected static Map<String, ExtensionModel> EXTENSION_MODELS = new HashMap<>();

  protected static final ExtensionModelLoader JAVA_LOADER = new DefaultJavaExtensionModelLoader();

  @AfterClass
  public static void cleanUp() {
    EXTENSION_MODELS = new HashMap<>();
  }

  protected static ExtensionModel loadExtension(Class<?> clazz, ExtensionModelLoader loader, ArtifactCoordinates coordinates,
                                                DslResolvingContext dslResolvingContext) {
    Map<String, Object> params = of(TYPE_PROPERTY_NAME, clazz.getName(),
                                    VERSION, getMuleManifest().getProductVersion(),
                                    // TODO MULE-14517: This workaround should be replaced for a better and more complete
                                    // mechanism
                                    COMPILATION_MODE, true);

    final String basePackage = clazz.getPackage().toString();
    final ClassLoader pluginClassLoader = new ClassLoader(clazz.getClassLoader()) {

      @Override
      protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.startsWith(basePackage)) {
          byte[] classBytes;
          try {
            classBytes =
                toByteArray(this.getClass().getResourceAsStream("/" + name.replaceAll("\\.", "/") + ".class"));
            return this.defineClass(null, classBytes, 0, classBytes.length);
          } catch (Exception e) {
            return super.loadClass(name);
          }
        } else {
          return super.loadClass(name, resolve);
        }
      }
    };

    ExtensionModelLoadingRequest.Builder builder = builder(pluginClassLoader, dslResolvingContext)
        .setResolveMinMuleVersion(true)
        .addParameters(params);
    if (coordinates != null) {
      builder.setArtifactCoordinates(coordinates);
    }

    return loader.loadExtensionModel(builder.build());
  }

  public static class ExtensionUnitTest {

    final ExtensionModelLoader loader;
    final Class<?> extensionClass;
    final ArtifactCoordinates artifactCoordinates;

    protected ExtensionUnitTest(ExtensionModelLoader loader, Class<?> extensionClass, ArtifactCoordinates artifactCoordinates) {
      this.loader = loader;
      this.extensionClass = extensionClass;
      this.artifactCoordinates = artifactCoordinates;
    }

    ExtensionModelLoader getLoader() {
      return loader;
    }

    Class<?> getExtensionClass() {
      return extensionClass;
    }

    ArtifactCoordinates getArtifactCoordinates() {
      return artifactCoordinates;
    }

    public final Object[] toTestParams(TriFunction<Class<?>, ExtensionModelLoader, ArtifactCoordinates, ExtensionModel> createExtensionModel) {
      final ExtensionModel extensionModel =
          createExtensionModel.apply(getExtensionClass(), getLoader(), getArtifactCoordinates());
      return buildTestParams(extensionModel);
    }

    protected Object[] buildTestParams(ExtensionModel extensionModel) {
      return new Object[] {extensionModel};
    }
  }
}
