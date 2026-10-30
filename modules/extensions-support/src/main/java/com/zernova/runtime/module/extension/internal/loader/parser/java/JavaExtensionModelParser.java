/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java;

import static com.zernova.metadata.api.utils.MetadataTypeUtils.getTypeId;
import static com.zernova.runtime.extension.api.loader.ExtensionDevelopmentFramework.JAVA_SDK;
import static com.zernova.runtime.module.extension.internal.loader.ModelLoaderDelegateUtils.requiresConfig;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.JavaExtensionModelParserUtils.getOperationParsers;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.JavaExtensionModelParserUtils.getRequiresEnterpriseLicenseInfo;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.JavaExtensionModelParserUtils.getRequiresEntitlementInfo;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.ZernovaExtensionAnnotationParser.mapReduceRepeatableAnnotation;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.ZernovaExtensionAnnotationParser.mapReduceSingleAnnotation;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.error.JavaErrorModelParserUtils.parseExtensionErrorModels;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.lib.JavaExternalLibModelParserUtils.parseExternalLibraryModels;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.notification.NotificationModelParserUtils.parseLegacyNotifications;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.notification.NotificationModelParserUtils.parseNotifications;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.utils.MinZernovaVersionUtils.resolveExtensionMinMuleVersion;
import static com.zernova.runtime.module.extension.internal.loader.utils.ExtensionNamespaceUtils.getExtensionsNamespace;
import static com.zernova.runtime.module.extension.internal.loader.utils.ModelLoaderUtils.getXmlDslModel;
import static com.zernova.sdk.api.meta.JavaVersion.valueOf;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import static net.bytebuddy.jar.asm.Opcodes.ASM5;
import static org.slf4j.LoggerFactory.getLogger;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.meta.Category;
import com.zernova.runtime.api.meta.model.ExternalLibraryModel;
import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;
import com.zernova.runtime.api.meta.model.notification.NotificationModel;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.extension.api.annotation.Export;
import com.zernova.runtime.extension.api.annotation.Import;
import com.zernova.runtime.extension.api.annotation.ImportedTypes;
import com.zernova.runtime.extension.api.annotation.PrivilegedExport;
import com.zernova.runtime.extension.api.annotation.SubTypeMapping;
import com.zernova.runtime.extension.api.annotation.SubTypesMapping;
import com.zernova.runtime.extension.api.annotation.dsl.xml.Xml;
import com.zernova.runtime.extension.api.annotation.notification.NotificationActions;
import com.zernova.runtime.extension.api.exception.IllegalModelDefinitionException;
import com.zernova.runtime.extension.api.loader.ExtensionDevelopmentFramework;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.parser.LicenseModelParser;
import com.zernova.runtime.extension.api.loader.parser.MinZernovaVersionParser;
import com.zernova.runtime.extension.api.runtime.exception.SdkExceptionHandlerFactory;
import com.zernova.runtime.module.extension.api.loader.java.type.AnnotationValueFetcher;
import com.zernova.runtime.module.extension.api.loader.java.type.ConfigurationElement;
import com.zernova.runtime.module.extension.api.loader.java.type.ExtensionElement;
import com.zernova.runtime.module.extension.api.loader.java.type.Type;
import com.zernova.runtime.module.extension.internal.loader.delegate.StereotypeModelLoaderDelegate;
import com.zernova.runtime.module.extension.internal.loader.java.property.ExportedClassNamesModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.property.ImplementingTypeModelProperty;
import com.zernova.runtime.module.extension.internal.loader.java.type.property.ExtensionTypeDescriptorModelProperty;
import com.zernova.runtime.extension.api.loader.parser.ConfigurationModelParser;
import com.zernova.runtime.extension.api.loader.parser.ConnectionProviderModelParser;
import com.zernova.runtime.extension.api.loader.parser.ErrorModelParser;
import com.zernova.runtime.extension.api.loader.parser.ExtensionModelParser;
import com.zernova.runtime.extension.api.loader.parser.FunctionModelParser;
import com.zernova.runtime.extension.api.loader.parser.OperationModelParser;
import com.zernova.runtime.extension.api.loader.parser.SourceModelParser;
import com.zernova.runtime.extension.api.loader.parser.XmlDslConfiguration;
import com.zernova.runtime.module.extension.internal.loader.parser.java.error.JavaErrorModelParserUtils;
import com.zernova.runtime.module.extension.internal.loader.parser.java.info.ExportInfo;
import com.zernova.runtime.module.extension.internal.loader.parser.java.info.RequiresEnterpriseLicenseInfo;
import com.zernova.runtime.module.extension.internal.loader.parser.java.info.RequiresEntitlementInfo;
import com.zernova.sdk.api.annotation.JavaVersionSupport;
import com.zernova.sdk.api.annotation.OnArtifactLifecycle;
import com.zernova.sdk.api.artifact.lifecycle.ArtifactLifecycleListener;
import com.zernova.sdk.api.meta.JavaVersion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.bytebuddy.jar.asm.AnnotationVisitor;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassVisitor;
import org.slf4j.Logger;

/**
 * {@link ExtensionModelParser} for Java based syntax
 *
 * @since 4.5.0
 */
public class JavaExtensionModelParser extends AbstractJavaModelParser implements ExtensionModelParser {

  private static final Logger LOGGER = getLogger(JavaExtensionModelParser.class);

  private Optional<XmlDslConfiguration> xmlDslConfiguration;
  private List<ErrorModelParser> errorModelParsers;
  private final List<MetadataType> exportedTypes = new LinkedList<>();
  private final List<String> exportedResources = new LinkedList<>();
  private final StereotypeModelLoaderDelegate stereotypeLoaderDelegate;

  private List<MetadataType> importedTypes = new LinkedList<>();
  private List<String> privilegedExportedArtifacts = new LinkedList<>();
  private List<String> privilegedExportedPackages = new LinkedList<>();
  private List<NotificationModel> notificationModels = new LinkedList<>();
  private final Map<MetadataType, List<MetadataType>> subTypes = new LinkedHashMap<>();
  private String namespace;
  private Set<String> supportedJavaVersions;

  public JavaExtensionModelParser(ExtensionElement extensionElement, ExtensionLoadingContext loadingContext) {
    this(extensionElement, new StereotypeModelLoaderDelegate(loadingContext), loadingContext);
  }

  public JavaExtensionModelParser(ExtensionElement extensionElement,
                                  StereotypeModelLoaderDelegate stereotypeLoaderDelegate,
                                  ExtensionLoadingContext loadingContext) {
    super(extensionElement, loadingContext);
    this.stereotypeLoaderDelegate = stereotypeLoaderDelegate;
    parseStructure(extensionElement);
  }

  private void parseStructure(ExtensionElement extensionElement) {
    xmlDslConfiguration = parseXmlDslConfiguration();

    // use dummy version since this is just for obtaining the namespace
    namespace = getExtensionsNamespace(getXmlDslModel(extensionElement, "1.0.0", xmlDslConfiguration));
    stereotypeLoaderDelegate.setNamespace(namespace);
    errorModelParsers = fetchErrorModelParsers();

    additionalModelProperties.add(new ExtensionTypeDescriptorModelProperty(extensionElement));
    extensionElement.getDeclaringClass()
        .ifPresent(extensionClass -> additionalModelProperties.add(new ImplementingTypeModelProperty(extensionClass)));

    parseExported();
    parseImportedTypes();
    parseSubtypes();
    parseNotificationModels();

    supportedJavaVersions = parseSupportedJavaVersions(extensionElement);
  }

  private Set<String> parseSupportedJavaVersions(ExtensionElement extensionElement) {
    return extensionElement.getValueFromAnnotation(JavaVersionSupport.class)
        .map(a -> a
            .getEnumArrayValue(javaVersionSupport -> getJavaVersionSupport(javaVersionSupport, extensionElement))
            .stream()
            .map(JavaVersion::version)
            .collect(toCollection(() -> (Set<String>) new LinkedHashSet<String>())))
        .orElse(emptySet());
  }

  private JavaVersion[] getJavaVersionSupport(JavaVersionSupport javaVersionSupport, ExtensionElement extensionElement) {
    try {
      return javaVersionSupport.value();
    } catch (EnumConstantNotPresentException e) {
      // An enum value not present in the Mule container version of `JavaVersion` is in use, we need to introspect the annotation
      try {
        Class<?> annotatedClass = extensionElement.getDeclaringClass().orElseThrow();

        ClassReader reader = new ClassReader(currentThread().getContextClassLoader()
            .getResourceAsStream(annotatedClass.getName().replace(".", "/") + ".class"));

        List<JavaVersion> javaVersions = new ArrayList<>();
        int asmVersion = ASM5;
        reader.accept(new ClassVisitor(asmVersion) {

          @Override
          public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            return new AnnotationVisitor(asmVersion) {

              @Override
              public AnnotationVisitor visitArray(String name) {
                return new AnnotationVisitor(asmVersion) {

                  @Override
                  public void visitEnum(String name, String desc, String value) {
                    try {
                      if (descriptor.endsWith(JavaVersionSupport.class.getSimpleName() + ";")) {
                        javaVersions.add(valueOf(value));
                      }
                    } catch (IllegalArgumentException e) {
                      LOGGER.debug("Found unknown value '{}' in JavaVersionSupport annotation", value, e);
                    }
                  }
                };
              }
            };
          }
        }, 0);

        return javaVersions.toArray(new JavaVersion[javaVersions.size()]);
      } catch (Exception e2) {
        e.addSuppressed(e2);
        throw e;
      }
    }
  }

  private void parseSubtypes() {
    List<Pair<Type, List<Type>>> pairs = mapReduceRepeatableAnnotation(
                                                                       extensionElement,
                                                                       SubTypeMapping.class,
                                                                       com.zernova.sdk.api.annotation.SubTypeMapping.class,
                                                                       container -> ((SubTypesMapping) container).value(),
                                                                       container -> ((com.zernova.sdk.api.annotation.SubTypesMapping) container)
                                                                           .value(),
                                                                       value -> new Pair<>(value
                                                                           .getClassValue(sub -> sub.baseType()),
                                                                                           value.getClassArrayValue(sub -> sub
                                                                                               .subTypes())),
                                                                       value -> new Pair<>(value
                                                                           .getClassValue(sub -> sub.baseType()),
                                                                                           value.getClassArrayValue(sub -> sub
                                                                                               .subTypes()))).collect(toList());

    Set<Object> baseTypes = new HashSet<>();
    pairs.forEach(mapping -> {

      final Type baseType = mapping.getFirst();
      baseTypes.add(baseType);

      MetadataType baseMetadataType = baseType.asMetadataType();
      Map<Type, MetadataType> subTypesMetadataTypes = mapping.getSecond()
          .stream()
          .collect(toMap(identity(), Type::asMetadataType, (u, v) -> {
            LOGGER.debug("Definition of type {} for base type {} is duplicated. It will be defined only once.",
                         getTypeId(u).orElse("Unknown"),
                         getTypeId(baseMetadataType).orElse("Unknown"));
            return u;
          }, LinkedHashMap::new));

      subTypes.put(baseMetadataType, new ArrayList<>(subTypesMetadataTypes.values()));
    });
  }

  private void parseNotificationModels() {
    notificationModels = mapReduceSingleAnnotation(extensionElement,
                                                   NotificationActions.class,
                                                   com.zernova.sdk.api.annotation.notification.NotificationActions.class,
                                                   value -> parseLegacyNotifications(value, namespace,
                                                                                     loadingContext.getTypeLoader()),
                                                   value -> parseNotifications(value, namespace,
                                                                               loadingContext.getTypeLoader()))
                                                                                   .orElse(new LinkedList<>());
  }

  private void parseImportedTypes() {
    List<Type> types = mapReduceRepeatableAnnotation(extensionElement,
                                                     Import.class,
                                                     com.zernova.sdk.api.annotation.Import.class,
                                                     container -> ((ImportedTypes) container).value(),
                                                     container -> ((com.zernova.sdk.api.annotation.ImportedTypes) container).value(),
                                                     value -> value.getClassValue(Import::type),
                                                     value -> value.getClassValue(com.zernova.sdk.api.annotation.Import::type))
                                                         .collect(toList());

    importedTypes = types.stream()
        .distinct()
        .map(Type::asMetadataType)
        .collect(toList());

    if (types.size() != importedTypes.size()) {
      throw new IllegalModelDefinitionException(
                                                format("There should be only one Import declaration for any given type in extension [%s]."
                                                    + " Multiple imports of the same type are not allowed", getName()));
    }
  }

  private void parseExported() {
    ExportInfo info = mapReduceSingleAnnotation(extensionElement,
                                                Export.class,
                                                com.zernova.sdk.api.annotation.Export.class,
                                                export -> ExportInfo.fromLegacy(export),
                                                export -> ExportInfo.fromSdkApi(export)).orElse(null);

    if (info == null) {
      return;
    }

    Set<String> exportedClassNames = new LinkedHashSet<>();
    info.getExportedTypes().forEach(type -> {
      exportedClassNames.add(type.getClassInformation().getClassname());
      exportedTypes.add(type.asMetadataType());
    });
    exportedResources.addAll(info.getExportedResources());

    if (!exportedClassNames.isEmpty()) {
      additionalModelProperties.add(new ExportedClassNamesModelProperty(exportedClassNames));
    }

    parsePrivilegeExport();
  }

  private void parsePrivilegeExport() {
    mapReduceSingleAnnotation(
                              extensionElement,
                              PrivilegedExport.class,
                              com.zernova.sdk.api.annotation.PrivilegedExport.class,
                              value -> new Pair<>(value.getArrayValue(PrivilegedExport::artifacts),
                                                  value.getArrayValue(PrivilegedExport::packages)),
                              value -> new Pair<>(value.getArrayValue(com.zernova.sdk.api.annotation.PrivilegedExport::artifacts),
                                                  value.getArrayValue(com.zernova.sdk.api.annotation.PrivilegedExport::packages)))
                                                      .ifPresent(exported -> {
                                                        privilegedExportedArtifacts = exported.getFirst();
                                                        privilegedExportedPackages = exported.getSecond();
                                                      });
  }

  @Override
  public String getName() {
    return extensionElement.getName();
  }

  @Override
  public Category getCategory() {
    return extensionElement.getCategory();
  }

  @Override
  public String getVendor() {
    return extensionElement.getVendor();
  }

  @Override
  public List<ConfigurationModelParser> getConfigurationParsers() {
    List<ConfigurationElement> configurations = extensionElement.getConfigurations();
    if (configurations.isEmpty()) {
      return singletonList(new JavaConfigurationModelParser(this, extensionElement, extensionElement, loadingContext));
    } else {
      return configurations.stream()
          .map(config -> new JavaConfigurationModelParser(this, extensionElement, config, loadingContext))
          .collect(toList());
    }
  }

  @Override
  public List<OperationModelParser> getOperationModelParsers() {
    return getOperationParsers(this,
                               extensionElement,
                               extensionElement,
                               loadingContext)
                                   .filter(operation -> !requiresConfig(getDevelopmentFramework(), operation))
                                   .collect(toList());
  }

  @Override
  public List<SourceModelParser> getSourceModelParsers() {
    return JavaExtensionModelParserUtils.getSourceParsers(extensionElement,
                                                          extensionElement.getSources(),
                                                          loadingContext)
        .filter(source -> !requiresConfig(source))
        .collect(toList());
  }

  @Override
  public List<ConnectionProviderModelParser> getConnectionProviderModelParsers() {
    return JavaExtensionModelParserUtils.getConnectionProviderModelParsers(extensionElement,
                                                                           extensionElement.getConnectionProviders(),
                                                                           loadingContext);
  }

  @Override
  public List<FunctionModelParser> getFunctionModelParsers() {
    return JavaExtensionModelParserUtils.getFunctionModelParsers(extensionElement,
                                                                 extensionElement.getFunctionContainers(),
                                                                 loadingContext);
  }

  @Override
  public List<ErrorModelParser> getErrorModelParsers() {
    return errorModelParsers;
  }

  private List<ErrorModelParser> fetchErrorModelParsers() {
    return parseExtensionErrorModels(extensionElement, getNamespace());
  }

  @Override
  public LicenseModelParser getLicensingParser() {
    Optional<RequiresEntitlementInfo> requiresEntitlementOptional = getRequiresEntitlementInfo(extensionElement);
    Optional<RequiresEnterpriseLicenseInfo> requiresEnterpriseLicenseOptional =
        getRequiresEnterpriseLicenseInfo(extensionElement);
    boolean requiresEnterpriseLicense = requiresEnterpriseLicenseOptional.isPresent();
    boolean allowsEvaluationLicense =
        requiresEnterpriseLicenseOptional.map(RequiresEnterpriseLicenseInfo::isAllowEvaluationLicense).orElse(true);
    Optional<String> requiredEntitlement = requiresEntitlementOptional.map(RequiresEntitlementInfo::getName);

    return new JavaLicenseModelParser(requiresEnterpriseLicense, allowsEvaluationLicense, requiredEntitlement);
  }

  @Override
  public List<ExternalLibraryModel> getExternalLibraryModels() {
    return parseExternalLibraryModels(extensionElement);
  }

  @Override
  public Optional<SdkExceptionHandlerFactory> getExceptionHandlerFactory() {
    return JavaErrorModelParserUtils.getExceptionHandlerFactory(extensionElement, "Extension", getName());
  }

  @Override
  public Optional<DeprecationModel> getDeprecationModel() {
    return JavaExtensionModelParserUtils.getDeprecationModel(extensionElement);
  }

  @Override
  public Optional<XmlDslConfiguration> getXmlDslConfiguration() {
    return xmlDslConfiguration;
  }

  @Override
  public List<MetadataType> getExportedTypes() {
    return exportedTypes;
  }

  @Override
  public Map<MetadataType, List<MetadataType>> getSubTypes() {
    return subTypes;
  }

  @Override
  public List<String> getExportedResources() {
    return exportedResources;
  }

  @Override
  public List<String> getPrivilegedExportedArtifacts() {
    return privilegedExportedArtifacts;
  }

  @Override
  public List<String> getPrivilegedExportedPackages() {
    return privilegedExportedPackages;
  }

  @Override
  public List<MetadataType> getImportedTypes() {
    return importedTypes;
  }

  @Override
  public List<NotificationModel> getNotificationModels() {
    return notificationModels;
  }

  @Override
  public String getNamespace() {
    return namespace;
  }

  @Override
  public Optional<MinZernovaVersionParser> getResolvedMinMuleVersion() {
    return of(resolveExtensionMinMuleVersion(extensionElement));
  }

  @Override
  public ExtensionDevelopmentFramework getDevelopmentFramework() {
    return JAVA_SDK;
  }

  @Override
  public Set<String> getSupportedJavaVersions() {
    return supportedJavaVersions;
  }

  @Override
  public Optional<Class<? extends ArtifactLifecycleListener>> getArtifactLifecycleListenerClass() {
    return parseArtifactLifecycleListener(extensionElement);
  }

  public StereotypeModelLoaderDelegate getStereotypeLoaderDelegate() {
    return stereotypeLoaderDelegate;
  }

  private Optional<XmlDslConfiguration> parseXmlDslConfiguration() {
    return mapReduceSingleAnnotation(
                                     extensionElement,
                                     Xml.class,
                                     com.zernova.sdk.api.annotation.dsl.xml.Xml.class,
                                     xml -> new XmlDslConfiguration(
                                                                    xml.getStringValue(Xml::prefix),
                                                                    xml.getStringValue(Xml::namespace)),
                                     xml -> new XmlDslConfiguration(
                                                                    xml.getStringValue(com.zernova.sdk.api.annotation.dsl.xml.Xml::prefix),
                                                                    xml.getStringValue(com.zernova.sdk.api.annotation.dsl.xml.Xml::namespace)));
  }

  private Optional<Class<? extends ArtifactLifecycleListener>> parseArtifactLifecycleListener(ExtensionElement extensionElement) {
    return extensionElement.getValueFromAnnotation(OnArtifactLifecycle.class)
        .flatMap(this::parseArtifactLifecycleListener);
  }

  private Optional<Class<? extends ArtifactLifecycleListener>> parseArtifactLifecycleListener(AnnotationValueFetcher<OnArtifactLifecycle> annotationValueFetcher) {
    return annotationValueFetcher.getClassValue(OnArtifactLifecycle::value)
        .getDeclaringClass()
        .map(cls -> (Class<? extends ArtifactLifecycleListener>) cls);
  }

  private class JavaLicenseModelParser implements LicenseModelParser {

    private final boolean requiresEnterpriseLicense;
    private final boolean allowsEvaluationLicense;
    private final Optional<String> requiredEntitlement;

    public JavaLicenseModelParser(boolean requiresEnterpriseLicense, boolean allowsEvaluationLicense,
                                  Optional<String> requiredEntitlement) {
      this.requiresEnterpriseLicense = requiresEnterpriseLicense;
      this.allowsEvaluationLicense = allowsEvaluationLicense;
      this.requiredEntitlement = requiredEntitlement;
    }

    @Override
    public boolean requiresEeLicense() {
      return requiresEnterpriseLicense;
    }

    @Override
    public boolean isAllowsEvaluationLicense() {
      return allowsEvaluationLicense;
    }

    @Override
    public Optional<String> getRequiredEntitlement() {
      return requiredEntitlement;
    }
  }
}
