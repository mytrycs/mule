/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static com.zernova.runtime.core.api.util.ClassUtils.withContextClassLoader;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.JavaExtensionModelParserFactory.getExtensionElement;

import com.zernova.runtime.core.api.util.ClassUtils;
import com.zernova.runtime.extension.api.annotation.privileged.DeclarationEnrichers;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionElement;
import com.zernova.runtime.module.extension.internal.loader.AbstractExtensionModelLoader;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.DefaultEncodingDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.DsqlDynamicMetadataDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.ExtensionDescriptionsEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.JavaConfigurationDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.JavaMimeTypeParametersDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.JavaOAuthDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.JavaObjectStoreParameterDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.ParameterAllowedStereotypesDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.PollingSourceDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.RefNameDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.RequiredForMetadataDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.RuntimeVersionDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.SampleDataDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.enricher.ValueProvidersParameterDeclarationEnricher;
import com.zernova.runtime.module.extension.internal.loader.java.validation.ComponentLocationModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.IgnoredExtensionParameterModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.InjectedFieldsModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.JavaInputParametersTypeModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.JavaOAuthConnectionProviderModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.JavaParameterTypeModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.JavaSampleDataModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.JavaScopeModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.JavaSubtypesModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.JavaValueProviderModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.MediaTypeModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.MetadataComponentModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.NullSafeModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.OperationParametersTypeModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.OperationReturnTypeModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.PagedOperationModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.ParameterGroupModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.PojosModelValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.PrivilegedApiValidator;
import com.zernova.runtime.module.extension.internal.loader.java.validation.SourceCallbacksModelValidator;
import com.zernova.runtime.extension.api.loader.parser.ExtensionModelParserFactory;
import com.zernova.runtime.module.extension.internal.loader.parser.java.JavaExtensionModelParserFactory;
import com.zernova.runtime.module.extension.internal.loader.validator.JavaConfigurationModelValidator;
import com.zernova.runtime.module.extension.internal.loader.validator.JavaConnectionProviderModelValidator;
import com.zernova.runtime.module.extension.internal.loader.validator.DeprecationModelValidator;

import java.util.Collection;
import java.util.List;

public abstract class AbstractJavaExtensionModelLoader extends AbstractExtensionModelLoader {

  public static final String TYPE_PROPERTY_NAME = "type";
  public static final String EXTENSION_TYPE = "EXTENSION_TYPE";

  public static final String VERSION = "version";

  private final List<ExtensionModelValidator> customValidators = unmodifiableList(asList(
                                                                                         new JavaConfigurationModelValidator(),
                                                                                         new JavaConnectionProviderModelValidator(),
                                                                                         new PojosModelValidator(),
                                                                                         new DeprecationModelValidator(),
                                                                                         new JavaInputParametersTypeModelValidator(),
                                                                                         new JavaSubtypesModelValidator(),
                                                                                         new MediaTypeModelValidator(),
                                                                                         new MetadataComponentModelValidator(),
                                                                                         new NullSafeModelValidator(),
                                                                                         new OperationReturnTypeModelValidator(),
                                                                                         new OperationParametersTypeModelValidator(),
                                                                                         new SourceCallbacksModelValidator(),
                                                                                         new PagedOperationModelValidator(),
                                                                                         new ParameterGroupModelValidator(),
                                                                                         new JavaParameterTypeModelValidator(),
                                                                                         new JavaOAuthConnectionProviderModelValidator(),
                                                                                         new JavaValueProviderModelValidator(),
                                                                                         new JavaSampleDataModelValidator(),
                                                                                         new PrivilegedApiValidator(),
                                                                                         new ComponentLocationModelValidator(),
                                                                                         new InjectedFieldsModelValidator(),
                                                                                         new IgnoredExtensionParameterModelValidator(),
                                                                                         new JavaScopeModelValidator()));

  private final List<DeclarationEnricher> customDeclarationEnrichers = unmodifiableList(asList(
                                                                                               new RefNameDeclarationEnricher(),
                                                                                               new DefaultEncodingDeclarationEnricher(),
                                                                                               new RuntimeVersionDeclarationEnricher(),
                                                                                               // TODO: MOVE TO EXT_API when
                                                                                               // https://www.mulesoft.org/jira/browse/MULE-13070
                                                                                               new JavaMimeTypeParametersDeclarationEnricher(),
                                                                                               new DsqlDynamicMetadataDeclarationEnricher(),
                                                                                               new RequiredForMetadataDeclarationEnricher(),
                                                                                               new JavaConfigurationDeclarationEnricher(),
                                                                                               new JavaOAuthDeclarationEnricher(),
                                                                                               new ExtensionDescriptionsEnricher(),
                                                                                               new ValueProvidersParameterDeclarationEnricher(),
                                                                                               new SampleDataDeclarationEnricher(),
                                                                                               new ParameterAllowedStereotypesDeclarationEnricher(),
                                                                                               new JavaObjectStoreParameterDeclarationEnricher(),
                                                                                               new PollingSourceDeclarationEnricher()));

  private final String id;

  public AbstractJavaExtensionModelLoader(String id) {
    this.id = id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void configureContextBeforeDeclaration(ExtensionLoadingContext context) {
    super.configureContextBeforeDeclaration(context);

    context.addCustomValidators(customValidators);
    context.addCustomDeclarationEnrichers(customDeclarationEnrichers);
    context.addCustomDeclarationEnrichers(getPrivilegedDeclarationEnrichers(context));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ExtensionModelParserFactory getExtensionModelParserFactory(ExtensionLoadingContext context) {
    return new JavaExtensionModelParserFactory();
  }

  private Collection<DeclarationEnricher> getPrivilegedDeclarationEnrichers(ExtensionLoadingContext context) {
    ExtensionElement extensionType = getExtensionElement(context);
    if (extensionType.getDeclaringClass().isPresent()) {
      try {
        // TODO: MULE-12744. If this call throws an exception it means that the extension cannot access the privileged API.
        ClassLoader extensionClassLoader = context.getExtensionClassLoader();
        Class annotation = extensionClassLoader.loadClass(DeclarationEnrichers.class.getName());


        return (Collection<DeclarationEnricher>) extensionType.getValueFromAnnotation((Class<DeclarationEnrichers>) annotation)
            .map(value -> withContextClassLoader(extensionClassLoader,
                                                 () -> value.getClassArrayValue(DeclarationEnrichers::value)).stream()
                                                     .map(type -> instantiateOrFail(type.getDeclaringClass().get()))
                                                     .collect(toList()))
            .orElse(emptyList());
      } catch (ClassNotFoundException e) {
        // Do nothing
      }
    }
    return emptyList();
  }



  private <R> R instantiateOrFail(Class<R> clazz) {
    try {
      return ClassUtils.instantiateClass(clazz);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error instantiating class: [" + clazz + "].", e);
    }
  }
}
