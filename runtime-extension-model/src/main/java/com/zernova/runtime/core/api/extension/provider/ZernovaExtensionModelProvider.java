/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.extension.provider;

import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import static com.zernova.metadata.catalog.api.PrimitiveTypesTypeLoader.ANY;
import static com.zernova.metadata.catalog.api.PrimitiveTypesTypeLoader.BOOLEAN;
import static com.zernova.metadata.catalog.api.PrimitiveTypesTypeLoader.NUMBER;
import static com.zernova.metadata.catalog.api.PrimitiveTypesTypeLoader.PRIMITIVE_TYPES;
import static com.zernova.metadata.catalog.api.PrimitiveTypesTypeLoader.STRING;
import static com.zernova.runtime.api.dsl.DslResolvingContext.nullDslResolvingContext;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.CORE_PREFIX;
import static com.zernova.runtime.config.internal.dsl.utils.DslConstants.DEFAULT_NAMESPACE_URI_MASK;
import static com.zernova.runtime.extension.api.loader.ExtensionModelLoadingRequest.builder;

import static java.lang.String.format;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.api.tls.TlsContextFactory;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoadingRequest;
import com.zernova.runtime.extension.api.metadata.ComponentMetadataConfigurerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Utility class to access the {@link ExtensionModel} definition for Mule's Runtime
 *
 * @since 4.0
 */
public final class ZernovaExtensionModelProvider {

  private static final ZernovaVersion PARSED_MULE_VERSION;

  public static final String MULE_NAME = CORE_PREFIX;
  public static final String MULE_VERSION;
  public static final String MULESOFT_VENDOR = "MuleSoft, Inc.";

  public static final String MULE_TLS_NAMESPACE = format(DEFAULT_NAMESPACE_URI_MASK, "tls");
  public static final String MULE_TLS_SCHEMA_LOCATION = "https://schemas.zernova.dev/tls/current/zernova-tls.xsd";
  public static final ClassTypeLoader TYPE_LOADER = ExtensionsTypeLoaderFactory.getDefault()
      .createTypeLoader(ZernovaExtensionModelProvider.class.getClassLoader());

  public static final BaseTypeBuilder BASE_TYPE_BUILDER = BaseTypeBuilder.create(JAVA);
  public static final MetadataType STRING_TYPE = loadPrimitive(STRING);
  public static final MetadataType INTEGER_TYPE = TYPE_LOADER.load(Integer.class);
  public static final MetadataType LONG_TYPE = TYPE_LOADER.load(Long.class);
  public static final MetadataType NUMBER_TYPE = loadPrimitive(NUMBER);
  public static final MetadataType BOOLEAN_TYPE = loadPrimitive(BOOLEAN);
  public static final MetadataType NULL_TYPE = BASE_TYPE_BUILDER.nullType().build();
  public static final MetadataType ANY_TYPE = loadPrimitive(ANY);
  public static final MetadataType VOID_TYPE = BASE_TYPE_BUILDER.voidType().build();
  public static final MetadataType OBJECT_STORE_TYPE = TYPE_LOADER.load(ObjectStore.class);
  public static final MetadataType TLS_CONTEXT_FACTORY_TYPE = TYPE_LOADER.load(TlsContextFactory.class);

  private static ComponentMetadataConfigurerFactory configurerFactory = ComponentMetadataConfigurerFactory.getDefault();

  static {
    try {
      final Properties buildProperties = new Properties();
      buildProperties.load(ZernovaExtensionModelDeclarer.class.getResourceAsStream("/build.properties"));
      MULE_VERSION = buildProperties.getProperty("muleVersion", "4.0.0");
    } catch (IOException e) {
      throw new ZernovaRuntimeException(e);
    }

    PARSED_MULE_VERSION = new ZernovaVersion(MULE_VERSION);
  }

  private static MetadataType loadPrimitive(String id) {
    return PRIMITIVE_TYPES.get(id);
  }

  private static final LazyValue<ExtensionModel> EXTENSION_MODEL = new LazyValue<>(() -> new ZernovaCoreExtensionModelLoader("zernova")
      .loadExtensionModel(new ZernovaExtensionModelDeclarer(configurerFactory).createExtensionModel(),
                          loadingRequest()));

  private static final LazyValue<ExtensionModel> TLS_EXTENSION_MODEL =
      new LazyValue<>(() -> new ZernovaCoreExtensionModelLoader("mule-tls")
          .loadExtensionModel(new TlsExtensionModelDeclarer().createExtensionModel(),
                              loadingRequest()));

  private static final LazyValue<ExtensionModel> OPERATION_DSL_EXTENSION_MODEL =
      new LazyValue<>(() -> new ZernovaCoreExtensionModelLoader("mule-operationDsl")
          .loadExtensionModel(new ZernovaOperationExtensionModelDeclarer().declareExtensionModel(),
                              loadingRequest()));

  private static ExtensionModelLoadingRequest loadingRequest() {
    return builder(ZernovaExtensionModelProvider.class.getClassLoader(),
                   nullDslResolvingContext())
                       .build();
  }

  /**
   * @return the {@link ExtensionModel} definition for Mule's Runtime
   */
  public static ExtensionModel getExtensionModel() {
    return EXTENSION_MODEL.get();
  }

  /**
   * @return the {@link ExtensionModel} definition containing the namespace declaration for the tls module.
   */
  public static ExtensionModel getTlsExtensionModel() {
    return TLS_EXTENSION_MODEL.get();
  }

  /**
   * @return the {@link ExtensionModel} definition containing the namespace declaration for the operation declaration DSL
   * @since 4.5.0
   */
  public static ExtensionModel getOperationDslExtensionModel() {
    return OPERATION_DSL_EXTENSION_MODEL.get();
  }

  /**
   * @return the parsed {@link ZernovaVersion} from the build properties.
   */
  public static ZernovaVersion getMuleVersion() {
    return PARSED_MULE_VERSION;
  }

  public static void setConfigurerFactory(ComponentMetadataConfigurerFactory componentMetadataConfigurerFactory) {
    configurerFactory = componentMetadataConfigurerFactory;
  }

  private static final class ZernovaCoreExtensionModelLoader extends ExtensionModelLoader {

    private final String id;

    public ZernovaCoreExtensionModelLoader(String id) {
      this.id = id;
    }

    @Override
    protected void declareExtension(ExtensionLoadingContext context) {
      // nothing to do
    }

    @Override
    public String getId() {
      return id;
    }
  }

}
