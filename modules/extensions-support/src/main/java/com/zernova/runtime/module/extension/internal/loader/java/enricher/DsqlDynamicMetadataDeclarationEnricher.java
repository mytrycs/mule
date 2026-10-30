/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.java.enricher;

import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static com.zernova.runtime.api.meta.model.display.LayoutModel.builderFrom;
import static com.zernova.runtime.module.extension.internal.loader.utils.JavaMetadataKeyIdModelParserUtils.getMetadataKeyPart;
import static com.zernova.runtime.module.extension.internal.util.IntrospectionUtils.isASTMode;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.meta.model.declaration.fluent.BaseDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ComponentDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExecutableComponentDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExtensionDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.OperationDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterDeclarer;
import com.zernova.runtime.api.meta.model.declaration.fluent.ParameterizedDeclaration;
import com.zernova.runtime.api.meta.model.declaration.fluent.TypedDeclaration;
import com.zernova.runtime.api.metadata.resolving.AttributesTypeResolver;
import com.zernova.runtime.api.metadata.resolving.InputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.NamedTypeResolver;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;
import com.zernova.runtime.api.util.collection.Collectors;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataKeyId;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataKeyPart;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataScope;
import com.zernova.runtime.extension.api.annotation.param.Query;
import com.zernova.runtime.extension.api.exception.IllegalParameterModelDefinitionException;
import com.zernova.runtime.extension.api.loader.DeclarationEnricher;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.IdempotentDeclarationEnricherWalkDelegate;
import com.zernova.runtime.extension.api.loader.WalkingDeclarationEnricher;
import com.zernova.runtime.extension.api.metadata.MetadataResolverFactory;
import com.zernova.runtime.extension.api.metadata.NullMetadataResolver;
import com.zernova.runtime.extension.api.property.MetadataKeyIdModelProperty;
import com.zernova.runtime.extension.api.property.MetadataKeyPartModelProperty;
import com.zernova.runtime.extension.api.property.TypeResolversInformationModelProperty;
import com.zernova.runtime.module.extension.api.loader.java.type.AnnotationValueFetcher;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionParameter;
import com.zernova.runtime.module.extension.api.loader.java.type.MethodElement;
import com.zernova.runtime.module.extension.api.loader.java.type.Type;
import com.zernova.runtime.module.extension.api.loader.java.type.WithAnnotations;
import com.zernova.runtime.module.extension.internal.loader.java.property.ImplementingParameterModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.property.MetadataResolverFactoryModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.property.ParameterGroupModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.property.QueryParameterModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.type.property.ExtensionOperationDescriptorModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.type.property.ExtensionParameterDescriptorModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.type.property.ExtensionTypeDescriptorModelProperty;
import com.zernova.runtime.module.extension.internal.metadata.MetadataScopeAdapter;
import com.zernova.runtime.module.extension.internal.metadata.QueryMetadataResolverFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * {@link DeclarationEnricher} implementation that walks through a {@link ExtensionDeclaration} and looks for components
 * (Operations and Message sources) annotated with {@link MetadataScope} or {@link Query}. If a custom metadata scope is used, the
 * component will be considered of dynamic type.
 *
 * @since 4.0
 */
public class DsqlDynamicMetadataDeclarationEnricher implements WalkingDeclarationEnricher {

  private static final NullMetadataResolver NULL_METADATA_RESOLVER = new NullMetadataResolver();

  @Override
  public Optional<DeclarationEnricherWalkDelegate> getWalkDelegate(ExtensionLoadingContext extensionLoadingContext) {
    BaseDeclaration declaration = extensionLoadingContext.getExtensionDeclarer().getDeclaration();
    // TODO MULE-14397 - Improve Dynamic Metadata Enricher to enrich without requiring Classes
    if (isASTMode(declaration)) {
      return empty();
    }

    Optional<ExtensionTypeDescriptorModelProperty> property =
        declaration.getModelProperty(ExtensionTypeDescriptorModelProperty.class);

    return property.map(extensionTypeDescriptorModelProperty -> new IdempotentDeclarationEnricherWalkDelegate() {

      private Type extensionType = extensionTypeDescriptorModelProperty.getType();

      @Override
      public void onOperation(OperationDeclaration declaration) {
        enrichOperationMetadata(declaration);
      }

      private void enrichOperationMetadata(OperationDeclaration declaration) {
        declaration.getModelProperty(ExtensionOperationDescriptorModelProperty.class)
            .map(ExtensionOperationDescriptorModelProperty::getOperationElement)
            .ifPresent(operation -> {
              if (operation.isAnnotatedWith(Query.class)) {
                enrichWithDsql(declaration, operation);
              }
            });
      }

      private void enrichMetadataKeyParameters(ParameterizedDeclaration<?> declaration,
                                               MetadataScopeAdapter metadataScope) {
        declaration.getAllParameters()
            .forEach(paramDeclaration -> paramDeclaration.getModelProperty(ExtensionParameterDescriptorModelProperty.class)
                .ifPresent(modelProperty -> parseMetadataKeyAnnotations(modelProperty.getExtensionParameter(),
                                                                        paramDeclaration,
                                                                        metadataScope)));
      }

      private void declareResolversInformation(ExecutableComponentDeclaration<? extends ComponentDeclaration> declaration,
                                               MetadataScopeAdapter metadataScope, String categoryName) {
        declareResolversInformation(declaration, metadataScope, categoryName, declaration.isRequiresConnection());
      }

      private void declareResolversInformation(BaseDeclaration declaration,
                                               MetadataScopeAdapter metadataScope, String categoryName,
                                               boolean requiresConnection) {
        if (metadataScope.isCustomScope()) {
          Map<String, String> inputResolversByParam = metadataScope.getInputResolvers()
              .entrySet().stream()
              .collect(Collectors.toImmutableMap(Map.Entry::getKey,
                                                 e -> e.getValue().get().getResolverName()));
          String outputResolver = metadataScope.getOutputResolver().getResolverName();
          String attributesResolver = metadataScope.getAttributesResolver().getResolverName();
          String keysResolver = metadataScope.getKeysResolver().getResolverName();
          boolean isPartialKeyResolver = metadataScope.isPartialKeyResolver();

          // TODO MULE-15638 - Once Metadata API 2.0 is implemented we will know better if the resolver requires or not a
          // connection
          // of config.
          declaration.addModelProperty(new TypeResolversInformationModelProperty(categoryName,
                                                                                 inputResolversByParam,
                                                                                 outputResolver,
                                                                                 attributesResolver,
                                                                                 keysResolver,
                                                                                 requiresConnection,
                                                                                 requiresConnection,
                                                                                 isPartialKeyResolver));
        }
      }

      private void enrichWithDsql(OperationDeclaration declaration,
                                  MethodElement method) {
        AnnotationValueFetcher<Query> queryAnnotationValueFetcher = method.getValueFromAnnotation(Query.class).get();

        Class nativeOutputResolverType = getClassFromAnnotationFetcher(queryAnnotationValueFetcher, Query::nativeOutputResolver);
        Class entityResolverType = getClassFromAnnotationFetcher(queryAnnotationValueFetcher, Query::entityResolver);
        Class translatorType = getClassFromAnnotationFetcher(queryAnnotationValueFetcher, Query::translator);

        final MetadataResolverFactory resolverFactory = new QueryMetadataResolverFactory(
                                                                                         nativeOutputResolverType,
                                                                                         entityResolverType);
        declaration.addModelProperty(new MetadataResolverFactoryModelProperty(() -> resolverFactory));

        final MetadataScopeAdapter metadataScope = new MetadataScopeAdapter() {

          private OutputTypeResolver outputResolver = resolverFactory.getOutputResolver();

          @Override
          public boolean hasKeysResolver() {
            return false;
          }

          @Override
          public boolean hasInputResolvers() {
            return false;
          }

          @Override
          public boolean hasOutputResolver() {
            return true;
          }

          @Override
          public boolean hasAttributesResolver() {
            return false;
          }

          @Override
          public boolean isPartialKeyResolver() {
            return false;
          }

          @Override
          public TypeKeysResolver getKeysResolver() {
            return NULL_METADATA_RESOLVER;
          }

          @Override
          public Map<String, Supplier<? extends InputTypeResolver>> getInputResolvers() {
            return emptyMap();
          }

          @Override
          public OutputTypeResolver getOutputResolver() {
            return outputResolver;
          }

          @Override
          public AttributesTypeResolver getAttributesResolver() {
            return NULL_METADATA_RESOLVER;
          }

          @Override
          public MetadataType getKeyResolverMetadataType() {
            return null;
          }

          @Override
          public String getKeyResolverParameterName() {
            return null;
          }
        };
        addQueryModelProperties(declaration, translatorType);
        declareDynamicType(declaration.getOutput());
        declareMetadataKeyId(declaration, null);
        enrichMetadataKeyParameters(declaration, metadataScope);
        declareResolversInformation(declaration, metadataScope, getCategoryName(metadataScope));
      }

      private Class getClassFromAnnotationFetcher(AnnotationValueFetcher<Query> queryAnnotationValueFetcher,
                                                  Function<Query, Class> classMapper) {
        Type resolvedType = queryAnnotationValueFetcher.getClassValue(classMapper);
        if (resolvedType == null) {
          return null;
        }
        return resolvedType.getDeclaringClass().orElse(null);
      }


      private void addQueryModelProperties(OperationDeclaration declaration, Class translatorType) {
        ParameterDeclaration parameterDeclaration = declaration.getAllParameters()
            .stream()
            .filter(p -> p.getModelProperty(ImplementingParameterModelProperty.class).isPresent())
            .filter(p -> p.getModelProperty(ImplementingParameterModelProperty.class).get()
                .getParameter().isAnnotationPresent(MetadataKeyId.class))
            .findFirst()
            .orElseThrow(() -> new IllegalParameterModelDefinitionException(
                                                                            "Query operation must have a parameter annotated with @MetadataKeyId"));

        parameterDeclaration.addModelProperty(new QueryParameterModelProperty(translatorType));
        parameterDeclaration.setLayoutModel(builderFrom(parameterDeclaration.getLayoutModel()).asQuery().build());
      }


      private void declareDynamicType(TypedDeclaration component) {
        component.setType(component.getType(), true);
      }

      private void declareMetadataKeyId(ComponentDeclaration<? extends ComponentDeclaration> component,
                                        String categoryName) {
        getMetadataKeyModelProperty(component, categoryName).ifPresent(component::addModelProperty);
      }

      private Optional<MetadataKeyIdModelProperty> getMetadataKeyModelProperty(ComponentDeclaration<? extends ComponentDeclaration> component,
                                                                               String categoryName) {
        Optional<MetadataKeyIdModelProperty> keyId = findMetadataKeyIdInGroups(component, categoryName);
        return keyId.isPresent() ? keyId : findMetadataKeyIdInParameters(component, categoryName);
      }

      private Optional<MetadataKeyIdModelProperty> findMetadataKeyIdInGroups(
                                                                             ComponentDeclaration<? extends ComponentDeclaration> component,
                                                                             String categoryName) {
        return component.getParameterGroups().stream()
            .map(group -> group.getModelProperty(ParameterGroupModelProperty.class).orElse(null))
            .filter(Objects::nonNull)
            .filter(group -> hasKeyId(group.getDescriptor().getAnnotatedContainer()))
            .map(group -> new MetadataKeyIdModelProperty(group.getDescriptor().getMetadataType(),
                                                         group.getDescriptor().getName(), categoryName))
            .findFirst();
      }


      private Optional<MetadataKeyIdModelProperty> findMetadataKeyIdInParameters(ComponentDeclaration<? extends ComponentDeclaration> component,
                                                                                 String categoryName) {
        return component.getParameterGroups().stream()
            .flatMap(g -> g.getParameters().stream())
            .filter(p -> getExtensionParameter(p).map(this::hasKeyId).orElse(false))
            .map(p -> new MetadataKeyIdModelProperty(p.getType(), p.getName(), categoryName))
            .findFirst();
      }

      private Optional<ExtensionParameter> getExtensionParameter(ParameterDeclaration parameterDeclaration) {
        return parameterDeclaration
            .getModelProperty(ExtensionParameterDescriptorModelProperty.class)
            .map(ExtensionParameterDescriptorModelProperty::getExtensionParameter);
      }

      /**
       * Enriches the {@link ParameterDeclarer} with a {@link MetadataKeyPartModelProperty} if the parsedParameter is annotated
       * either as {@link MetadataKeyId} or {@link MetadataKeyPart}
       *
       * @param element         the method annotated parameter parsed
       * @param baseDeclaration the {@link ParameterDeclarer} associated to the parsed parameter
       */
      private void parseMetadataKeyAnnotations(ExtensionParameter element, BaseDeclaration baseDeclaration,
                                               MetadataScopeAdapter metadataScope) {
        if (hasKeyId(element)) {
          baseDeclaration.addModelProperty(new MetadataKeyPartModelProperty(1, metadataScope.hasKeysResolver()));
        }

        getMetadataKeyPart(element).ifPresent(metadataKeyPart -> baseDeclaration
            .addModelProperty(new MetadataKeyPartModelProperty(metadataKeyPart.getFirst(), metadataKeyPart.getSecond())));
      }

      private String getCategoryName(MetadataScopeAdapter metadataScopeAdapter) {
        NamedTypeResolver resolver = metadataScopeAdapter.getKeysResolver();
        if (metadataScopeAdapter.hasKeysResolver()) {
          return resolver.getCategoryName();
        } else if (metadataScopeAdapter.hasInputResolvers()) {
          return metadataScopeAdapter.getInputResolvers().values().iterator().next().get().getCategoryName();
        } else if (metadataScopeAdapter.hasOutputResolver()) {
          return metadataScopeAdapter.getOutputResolver().getCategoryName();
        } else {
          return null;
        }
      }

      private boolean hasKeyId(WithAnnotations withAnnotations) {
        return withAnnotations.isAnnotatedWith(MetadataKeyId.class);
      }
    });

  }
}
