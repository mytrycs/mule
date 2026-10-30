/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java;

import static com.zernova.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static com.zernova.runtime.api.meta.ExpressionSupport.SUPPORTED;
import static com.zernova.runtime.core.api.util.StringUtils.isBlank;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.FLOW;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.OBJECT_STORE;
import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.isMap;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.NTLM_PROXY_CONFIGURATION;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.NTLM_PROXY_CONFIGURATION_PARAMETER;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.PROXY_CONFIGURATION_PARAMETER;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.PROXY_CONFIGURATION_TYPE;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.SCALAR_SECRET;
import static com.zernova.runtime.extension.privileged.semantic.ConnectivityVocabulary.SECRET;
import static com.zernova.runtime.extension.privileged.semantic.SemanticTermsHelper.getParameterTermsFromAnnotations;
import static com.zernova.runtime.module.extension.internal.loader.java.contributor.InfrastructureTypeResolver.getInfrastructureType;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.ZernovaExtensionAnnotationParser.mapReduceAnnotation;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.ZernovaExtensionAnnotationParser.mapReduceRepeatableAnnotation;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.ZernovaExtensionAnnotationParser.mapReduceSingleAnnotation;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.ZernovaExtensionAnnotationParser.parseLayoutAnnotations;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.semantics.SemanticTermsParserUtils.addCustomTerms;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.semantics.SemanticTermsParserUtils.addTermIfPresent;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.stereotypes.SdkStereotypeDefinitionAdapter.from;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.type.CustomStaticTypeUtils.getParameterType;
import static com.zernova.runtime.module.extension.internal.loader.utils.JavaMetadataKeyIdModelParserUtils.parseKeyIdResolverModelParser;
import static com.zernova.sdk.api.stereotype.ZernovaStereotypes.CONFIG;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

import com.zernova.metadata.api.model.ArrayType;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.metadata.api.visitor.BasicTypeMetadataVisitor;
import com.zernova.metadata.java.api.utils.JavaTypeUtils;
import com.zernova.runtime.api.meta.ExpressionSupport;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.ParameterDslConfiguration;
import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;
import com.zernova.runtime.api.meta.model.display.DisplayModel;
import com.zernova.runtime.api.meta.model.display.LayoutModel;
import com.zernova.runtime.api.meta.model.parameter.ExclusiveParametersModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterRole;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.extension.api.annotation.ConfigReferences;
import com.zernova.runtime.extension.api.annotation.connectivity.oauth.OAuthParameter;
import com.zernova.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import com.zernova.runtime.extension.api.annotation.param.ConfigOverride;
import com.zernova.runtime.extension.api.annotation.param.Content;
import com.zernova.runtime.extension.api.annotation.param.NullSafe;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.annotation.param.reference.ConfigReference;
import com.zernova.runtime.extension.api.annotation.param.reference.FlowReference;
import com.zernova.runtime.extension.api.annotation.param.reference.ObjectStoreReference;
import com.zernova.runtime.extension.api.annotation.param.stereotype.AllowedStereotypes;
import com.zernova.runtime.extension.api.annotation.param.stereotype.ComponentId;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthParameterModelProperty;
import com.zernova.runtime.extension.api.declaration.type.annotation.StereotypeTypeAnnotation;
import com.zernova.runtime.extension.api.exception.IllegalParameterModelDefinitionException;
import com.zernova.runtime.extension.api.loader.parser.MinZernovaVersionParser;
import com.zernova.runtime.extension.api.model.parameter.ImmutableExclusiveParametersModel;
import com.zernova.runtime.extension.api.property.DefaultImplementingTypeModelProperty;
import com.zernova.runtime.extension.api.property.InfrastructureParameterModelProperty;
import com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionParameter;
import com.zernova.runtime.module.extension.api.loader.java.type.FieldElement;
import com.zernova.runtime.module.extension.api.loader.java.type.Type;
import com.zernova.runtime.module.extension.internal.loader.java.property.DeclaringMemberModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.property.ExclusiveOptionalModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.property.ImplementingParameterModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.property.NullSafeModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.type.property.ExtensionParameterDescriptorModelProperty;
import com.zernova.runtime.extension.api.loader.parser.ParameterGroupModelParser.ExclusiveOptionalDescriptor;
import com.zernova.runtime.extension.api.loader.parser.ParameterModelParser;
import com.zernova.runtime.extension.api.loader.parser.StereotypeModelFactory;
import com.zernova.runtime.module.extension.internal.loader.parser.java.connection.SdkParameterPlacementUtils;
import com.zernova.runtime.extension.api.loader.parser.metadata.InputResolverModelParser;
import com.zernova.runtime.extension.api.loader.parser.metadata.MetadataKeyModelParser;
import com.zernova.runtime.module.extension.internal.loader.utils.JavaInputResolverModelParserUtils;
import com.zernova.runtime.module.extension.internal.loader.utils.JavaMetadataKeyIdModelParserUtils;
import com.zernova.runtime.module.extension.internal.util.IntrospectionUtils;
import com.zernova.sdk.api.annotation.semantics.connectivity.ExcludeFromConnectivitySchema;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * {@link ParameterModelParser} for Java based syntax
 *
 * @since 4.5.0
 */
public class JavaParameterModelParser implements ParameterModelParser, HasExtensionParameter {

  private final ExtensionParameter parameter;
  private final MetadataType type;
  private final Optional<ExclusiveOptionalDescriptor> exclusiveOptionals;
  private final List<ModelProperty> additionalModelProperties = new LinkedList<>();
  private final ParameterDeclarationContext context;

  private Optional<ParameterDslConfiguration> dslConfiguration;
  private ExpressionSupport expressionSupport;

  public JavaParameterModelParser(ExtensionParameter parameter,
                                  Optional<ExclusiveOptionalDescriptor> exclusiveOptionals,
                                  ParameterDeclarationContext context) {
    this.parameter = parameter;
    this.context = context;
    this.exclusiveOptionals = exclusiveOptionals;
    type = getParameterType(parameter);

    parserStructure();
    collectAdditionalModelProperties();
  }

  private void parserStructure() {
    parseExpressionSupport();
    parseExclusiveOptionals();
  }

  @Override
  public String getName() {
    return parameter.getAlias();
  }

  @Override
  public String getDescription() {
    return parameter.getDescription();
  }

  @Override
  public MetadataType getType() {
    return type;
  }

  @Override
  public boolean isRequired() {
    return parameter.isRequired();
  }

  @Override
  public Object getDefaultValue() {
    return parameter.defaultValue().orElse(null);
  }

  @Override
  public ParameterRole getRole() {
    return mapReduceSingleAnnotation(parameter, "parameter", parameter.getName(), Content.class,
                                     com.zernova.sdk.api.annotation.param.Content.class,
                                     legacyContentAnnotationValueFetcher -> legacyContentAnnotationValueFetcher
                                         .getBooleanValue(Content::primary) ? ParameterRole.PRIMARY_CONTENT
                                             : ParameterRole.CONTENT,
                                     sdkContentAnnotationValueFetcher -> sdkContentAnnotationValueFetcher
                                         .getBooleanValue(com.zernova.sdk.api.annotation.param.Content::primary)
                                             ? ParameterRole.PRIMARY_CONTENT
                                             : ParameterRole.CONTENT).orElse(ParameterRole.BEHAVIOUR);
  }

  @Override
  public ExpressionSupport getExpressionSupport() {
    return expressionSupport;
  }

  @Override
  public Optional<LayoutModel> getLayoutModel() {
    return parseLayoutAnnotations(parameter, LayoutModel.builder(), parameter.getName());
  }

  @Override
  public Optional<ParameterDslConfiguration> getDslConfiguration() {
    if (dslConfiguration == null) {
      dslConfiguration =
          mapReduceAnnotation(parameter, ParameterDsl.class, com.zernova.sdk.api.annotation.dsl.xml.ParameterDsl.class,
                              legacyAnnotationValueFetcher -> ParameterDslConfiguration.builder()
                                  .allowsInlineDefinition(legacyAnnotationValueFetcher
                                      .getBooleanValue(ParameterDsl::allowInlineDefinition))
                                  .allowsReferences(legacyAnnotationValueFetcher.getBooleanValue(ParameterDsl::allowReferences))
                                  .build(),
                              sdkAnnotationValueFetcher -> ParameterDslConfiguration.builder()
                                  .allowsInlineDefinition(sdkAnnotationValueFetcher
                                      .getBooleanValue(com.zernova.sdk.api.annotation.dsl.xml.ParameterDsl::allowInlineDefinition))
                                  .allowsReferences(sdkAnnotationValueFetcher
                                      .getBooleanValue(com.zernova.sdk.api.annotation.dsl.xml.ParameterDsl::allowReferences))
                                  .build(),
                              () -> new IllegalParameterModelDefinitionException(format("Parameter '%s' is annotated with '@%s' and '@%s' at the same time",
                                                                                        parameter.getName(),
                                                                                        ParameterDsl.class.getName(),
                                                                                        com.zernova.sdk.api.annotation.dsl.xml.ParameterDsl.class
                                                                                            .getName())));
    }
    return dslConfiguration;
  }

  @Override
  public Optional<DeprecationModel> getDeprecationModel() {
    return JavaExtensionModelParserUtils.getDeprecationModel(parameter);
  }

  @Override
  public Optional<DisplayModel> getDisplayModel() {
    return JavaExtensionModelParserUtils.getDisplayModel(parameter, "parameter", parameter.getName());
  }

  @Override
  public boolean isConfigOverride() {
    return parameter.isAnnotatedWith(ConfigOverride.class) ||
        parameter.isAnnotatedWith(com.zernova.sdk.api.annotation.param.ConfigOverride.class);
  }

  @Override
  public boolean isComponentId() {
    return parameter.isAnnotatedWith(ComponentId.class)
        || parameter.isAnnotatedWith(com.zernova.sdk.api.annotation.param.stereotype.ComponentId.class);
  }

  @Override
  public List<StereotypeModel> getAllowedStereotypes(StereotypeModelFactory factory) {
    if (parameter.isAnnotatedWith(FlowReference.class)
        || parameter.isAnnotatedWith(com.zernova.sdk.api.annotation.param.reference.FlowReference.class)) {
      return singletonList(FLOW);
    }

    if (parameter.isAnnotatedWith(ObjectStoreReference.class)
        || parameter.isAnnotatedWith(com.zernova.sdk.api.annotation.param.reference.ObjectStoreReference.class)) {
      return singletonList(OBJECT_STORE);
    }

    List<StereotypeModel> stereotypes =
        mapReduceRepeatableAnnotation(
                                      parameter,
                                      ConfigReference.class,
                                      com.zernova.sdk.api.annotation.param.reference.ConfigReference.class,
                                      container -> ((ConfigReferences) container).value(),
                                      container -> ((com.zernova.sdk.api.annotation.ConfigReferences) container).value(),
                                      value -> factory.createStereotype(value.getStringValue(ConfigReference::name),
                                                                        value.getStringValue(ConfigReference::namespace), CONFIG),
                                      value -> factory.createStereotype(
                                                                        value
                                                                            .getStringValue(com.zernova.sdk.api.annotation.param.reference.ConfigReference::name),
                                                                        value
                                                                            .getStringValue(com.zernova.sdk.api.annotation.param.reference.ConfigReference::namespace),
                                                                        CONFIG))
                                                                            .collect(toList());

    if (stereotypes.isEmpty()) {
      stereotypes = mapReduceSingleAnnotation(
                                              parameter,
                                              "parameter",
                                              parameter.getName(),
                                              AllowedStereotypes.class,
                                              com.zernova.sdk.api.annotation.param.stereotype.AllowedStereotypes.class,
                                              value -> value.getClassArrayValue(AllowedStereotypes::value).stream()
                                                  .filter(type -> type.getDeclaringClass().isPresent())
                                                  .map(type -> from(type.getDeclaringClass().get())),
                                              value -> value
                                                  .getClassArrayValue(com.zernova.sdk.api.annotation.param.stereotype.AllowedStereotypes::value)
                                                  .stream()
                                                  .filter(type -> type.getDeclaringClass().isPresent())
                                                  .map(type -> from(type.getDeclaringClass().get())))
                                                      .map(stream -> stream.map(def -> factory.createStereotype(def))
                                                          .collect(toList()))
                                                      .orElse(new LinkedList<>());
    }

    stereotypes.addAll(type.getAnnotation(StereotypeTypeAnnotation.class)
        .map(StereotypeTypeAnnotation::getAllowedStereotypes)
        .orElse(emptyList()));

    return stereotypes;
  }

  @Override
  public List<ModelProperty> getAdditionalModelProperties() {
    return additionalModelProperties;
  }

  @Override
  public boolean isExcludedFromConnectivitySchema() {
    return parameter.isAnnotatedWith(ExcludeFromConnectivitySchema.class);
  }

  @Override
  public Set<String> getSemanticTerms() {
    Set<String> terms = new LinkedHashSet<>();
    terms.addAll(getParameterTermsFromAnnotations(parameter::isAnnotatedWith));

    Set<String> typeTerms = new LinkedHashSet<>(ExtensionMetadataTypeUtils.getSemanticTerms(getType()));

    addTermIfPresent(typeTerms, PROXY_CONFIGURATION_TYPE, PROXY_CONFIGURATION_PARAMETER, terms);
    addTermIfPresent(typeTerms, NTLM_PROXY_CONFIGURATION, NTLM_PROXY_CONFIGURATION_PARAMETER, terms);

    if (typeTerms.contains(SECRET)) {
      getType().accept(new BasicTypeMetadataVisitor() {

        @Override
        protected void visitBasicType(MetadataType metadataType) {
          typeTerms.remove(SECRET);
          typeTerms.add(SCALAR_SECRET);
        }
      });
    }

    addCustomTerms(parameter, terms);

    return terms;
  }

  @Override
  public Optional<OAuthParameterModelProperty> getOAuthParameterModelProperty() {
    return mapReduceSingleAnnotation(parameter,
                                     "parameter",
                                     parameter.getName(),
                                     OAuthParameter.class,
                                     com.zernova.sdk.api.annotation.connectivity.oauth.OAuthParameter.class,
                                     oAuthParameterAnnotationValueFetcher -> new OAuthParameterModelProperty(oAuthParameterAnnotationValueFetcher
                                         .getStringValue(OAuthParameter::requestAlias), oAuthParameterAnnotationValueFetcher.getEnumValue(OAuthParameter::placement)),
                                     oAuthParameterAnnotationValueFetcher -> new OAuthParameterModelProperty(oAuthParameterAnnotationValueFetcher
                                         .getStringValue(com.zernova.sdk.api.annotation.connectivity.oauth.OAuthParameter::requestAlias),
                                                                                                             SdkParameterPlacementUtils
                                                                                                                 .from(oAuthParameterAnnotationValueFetcher
                                                                                                                     .getEnumValue(com.zernova.sdk.api.annotation.connectivity.oauth.OAuthParameter::placement))));
  }

  @Override
  public Optional<MinZernovaVersionParser> getResolvedMinZernovaVersion() {
    return empty();
  }

  @Override
  public Optional<InputResolverModelParser> getInputResolverModelParser() {
    return JavaInputResolverModelParserUtils.getResolverParser(parameter);
  }

  @Override
  public Optional<Pair<Integer, Boolean>> getMetadataKeyPart() {
    Optional<MetadataKeyModelParser> metadataKeyModelParser = parseKeyIdResolverModelParser(parameter, null, null);
    if (metadataKeyModelParser.isPresent()) {
      return of(new Pair(1, metadataKeyModelParser.get().hasKeyIdResolver() || context.isKeyResolverAvailable()));
    }
    return JavaMetadataKeyIdModelParserUtils.getMetadataKeyPart(parameter);
  }

  private void collectAdditionalModelProperties() {
    additionalModelProperties.add(new ExtensionParameterDescriptorModelProperty(parameter));
    collectImplementingTypeProperties();
    collectNullSafeProperties();
    collectInfrastructureModelProperties();
    collectStackableTypesModelProperty();
  }

  private void collectStackableTypesModelProperty() {
    additionalModelProperties.addAll(context.resolveStackableTypes(parameter));
  }

  private void collectInfrastructureModelProperties() {
    if (parameter instanceof FieldElement) {
      getInfrastructureType(parameter.getType()).ifPresent(infrastructureType -> {
        if (!isBlank(infrastructureType.getName())) {
          additionalModelProperties.add(new InfrastructureParameterModelProperty(infrastructureType.getSequence()));
          expressionSupport = NOT_SUPPORTED;
          infrastructureType.getQNameModelProperty().ifPresent(additionalModelProperties::add);
          infrastructureType.getDslConfiguration().ifPresent(dsl -> dslConfiguration = of(dsl));
        }
      });
    }
  }

  private void collectImplementingTypeProperties() {
    parameter.getDeclaringElement().ifPresent(element -> {
      if (element instanceof Field) {
        additionalModelProperties.add(new DeclaringMemberModelProperty(((Field) element)));
      } else {
        additionalModelProperties.add(new ImplementingParameterModelProperty((java.lang.reflect.Parameter) element));
      }
    });
  }

  private void parseExpressionSupport() {
    expressionSupport = IntrospectionUtils.getExpressionSupport(parameter, "parameter", getName()).orElse(SUPPORTED);
  }

  private void parseExclusiveOptionals() {
    exclusiveOptionals.ifPresent(exclusive -> {
      ExclusiveParametersModel exclusiveParametersModel =
          new ImmutableExclusiveParametersModel(exclusive.getExclusiveOptionals(), exclusive.isOneRequired());
      additionalModelProperties.add(new ExclusiveOptionalModelProperty(exclusiveParametersModel));
    });
  }

  private void collectNullSafeProperties() {
    Optional<Type> nullSafeAnnotationType = mapReduceSingleAnnotation(parameter,
                                                                      "parameter",
                                                                      parameter.getName(),
                                                                      NullSafe.class,
                                                                      com.zernova.sdk.api.annotation.param.NullSafe.class,
                                                                      value -> value
                                                                          .getClassValue(NullSafe::defaultImplementingType),
                                                                      value -> value
                                                                          .getClassValue(com.zernova.sdk.api.annotation.param.NullSafe::defaultImplementingType));

    if (nullSafeAnnotationType.isPresent()) {
      if (isConfigOverride()) {
        throw new IllegalParameterModelDefinitionException(
                                                           format("Parameter '%s' is annotated with '@%s' and also marked as a config override, which is redundant. "
                                                               + "The default value for this parameter will come from the configuration parameter",
                                                                  parameter.getName(), NullSafe.class.getSimpleName()));
      }
      if (parameter.isRequired() && !parameter.isAnnotatedWith(ParameterGroup.class)) {
        throw new IllegalParameterModelDefinitionException(
                                                           format("Parameter '%s' is required but annotated with '@%s', which is redundant",
                                                                  parameter.getName(), NullSafe.class.getSimpleName()));
      }

      final boolean hasDefaultOverride = !nullSafeAnnotationType.get().isSameType(Object.class);

      MetadataType nullSafeType =
          hasDefaultOverride ? nullSafeAnnotationType.get().asMetadataType() : type;

      boolean isInstantiable =
          hasDefaultOverride ? nullSafeAnnotationType.get().isInstantiable() : parameter.getType().isInstantiable();

      type.accept(new BasicTypeMetadataVisitor() {

        @Override
        protected void visitBasicType(MetadataType metadataType) {
          throw new IllegalParameterModelDefinitionException(
                                                             format("Parameter '%s' is annotated with '@%s' but is of type '%s'. That annotation can only be "
                                                                 + "used with complex types (Pojos, Lists, Maps)",
                                                                    parameter.getName(), NullSafe.class.getSimpleName(),
                                                                    parameter.getType().getName()));
        }

        @Override
        public void visitArrayType(ArrayType arrayType) {
          if (hasDefaultOverride) {
            throw new IllegalParameterModelDefinitionException(format("Parameter '%s' is annotated with '@%s' is of type '%s'"
                + " but a 'defaultImplementingType' was provided."
                + " Type override is not allowed for Collections",
                                                                      parameter.getName(),
                                                                      NullSafe.class.getSimpleName(),
                                                                      parameter.getType().getName()));
          }
        }

        @Override
        public void visitObject(ObjectType objectType) {
          if (hasDefaultOverride && isMap(objectType)) {
            throw new IllegalParameterModelDefinitionException(format("Parameter '%s' is annotated with '@%s' is of type '%s'"
                + " but a 'defaultImplementingType' was provided."
                + " Type override is not allowed for Maps",
                                                                      parameter.getName(),
                                                                      NullSafe.class.getSimpleName(),
                                                                      parameter.getType().getName()));
          }

          if (hasDefaultOverride && parameter.getType().isInstantiable()) {
            throw new IllegalParameterModelDefinitionException(
                                                               format("Parameter '%s' is annotated with '@%s' is of concrete type '%s',"
                                                                   + " but a 'defaultImplementingType' was provided."
                                                                   + " Type override is not allowed for concrete types",
                                                                      parameter.getName(),
                                                                      NullSafe.class.getSimpleName(),
                                                                      parameter.getType().getName()));
          }

          if (!isInstantiable && !isMap(nullSafeType)) {
            throw new IllegalParameterModelDefinitionException(
                                                               format("Parameter '%s' is annotated with '@%s' but is of type '%s'. That annotation can only be "
                                                                   + "used with complex instantiable types (Pojos, Lists, Maps)",
                                                                      parameter.getName(),
                                                                      NullSafe.class.getSimpleName(),
                                                                      parameter.getType().getName()));
          }

          if (hasDefaultOverride && !parameter.getType().isAssignableFrom(nullSafeAnnotationType.get())) {
            throw new IllegalParameterModelDefinitionException(
                                                               format("Parameter '%s' is annotated with '@%s' of type '%s', but provided type '%s"
                                                                   + " is not a subtype of the parameter's type",
                                                                      parameter.getName(),
                                                                      NullSafe.class.getSimpleName(),
                                                                      parameter.getType().getName(),
                                                                      JavaTypeUtils.getType(nullSafeType).getName()));
          }
        }
      });

      additionalModelProperties.add(new NullSafeModelProperty(nullSafeType));
      if (hasDefaultOverride) {
        additionalModelProperties.add(new DefaultImplementingTypeModelProperty(nullSafeType));
      }
    }
  }

  @Override
  public ExtensionParameter getExtensionParameter() {
    return parameter;
  }

}
