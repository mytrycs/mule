/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal;

import static com.zernova.maven.pom.parser.api.model.BundleScope.valueOf;
import static com.zernova.maven.pom.parser.api.model.MavenModelBuilderProvider.discoverProvider;
import static com.zernova.runtime.api.deployment.meta.Product.MULE;
import static com.zernova.runtime.api.util.Preconditions.checkState;
import static com.zernova.runtime.container.api.ZernovaFoldersUtil.getExecutionFolder;
import static com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor.META_INF;
import static com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor.MULE_ARTIFACT;
import static com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor.MULE_PLUGIN_CLASSIFIER;

import static java.nio.file.Files.createDirectories;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.io.IOUtils.copy;

import com.zernova.maven.client.api.MavenClient;
import com.zernova.maven.client.api.MavenClientProvider;
import com.zernova.maven.pom.parser.api.model.ArtifactCoordinates;
import com.zernova.maven.pom.parser.api.model.BundleDependency;
import com.zernova.maven.pom.parser.api.model.MavenModelBuilder;
import com.zernova.maven.pom.parser.api.model.MavenModelBuilderProvider;
import com.zernova.runtime.api.artifact.ArtifactType;
import com.zernova.runtime.api.deployment.meta.ZernovaApplicationModel;
import com.zernova.runtime.api.deployment.meta.ZernovaArtifactLoaderDescriptor;
import com.zernova.runtime.api.deployment.persistence.ZernovaApplicationModelJsonSerializer;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.app.declaration.api.ArtifactDeclaration;
import com.zernova.runtime.core.api.util.UUID;
import com.zernova.runtime.globalconfig.api.GlobalConfigLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ClassLoaderConfiguration;
import com.zernova.runtime.module.deployment.impl.internal.application.DefaultApplicationFactory;
import com.zernova.runtime.module.deployment.impl.internal.application.DeployableMavenClassLoaderConfigurationLoader;
import com.zernova.runtime.module.tooling.api.ArtifactAgnosticServiceBuilder;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

public abstract class AbstractArtifactAgnosticServiceBuilder<T extends ArtifactAgnosticServiceBuilder, S>
    implements ArtifactAgnosticServiceBuilder<T, S> {

  private static final String TMP_APP_ARTIFACT_ID = "temp-artifact-id";
  private static final String TMP_APP_GROUP_ID = "temp-group-id";
  private static final String TMP_APP_VERSION = "temp-version";
  private static final String TMP_APP_MODEL_VERSION = "4.0.0";

  private final DefaultApplicationFactory defaultApplicationFactory;

  private ArtifactDeclaration artifactDeclaration;

  private MavenModelBuilder model;
  private Map<String, String> artifactProperties = emptyMap();

  protected AbstractArtifactAgnosticServiceBuilder(DefaultApplicationFactory defaultApplicationFactory) {
    this.defaultApplicationFactory = defaultApplicationFactory;
    createTempMavenModel();
  }

  @Override
  public T setArtifactProperties(Map<String, String> artifactProperties) {
    checkState(artifactProperties != null, "artifactProperties cannot be null");
    this.artifactProperties = artifactProperties;
    return getThis();
  }

  @Override
  public T setArtifactDeclaration(ArtifactDeclaration artifactDeclaration) {
    checkState(artifactDeclaration != null, "artifactDeclaration cannot be null");
    this.artifactDeclaration = artifactDeclaration;
    return getThis();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T addDependency(String groupId, String artifactId, String artifactVersion,
                         String classifier, String type) {
    com.zernova.maven.pom.parser.api.model.BundleDescriptor bundleDescriptor =
        new com.zernova.maven.pom.parser.api.model.BundleDescriptor.Builder()
            .setGroupId(groupId)
            .setArtifactId(artifactId)
            .setVersion(artifactVersion)
            .setType(type)
            .setClassifier(classifier).build();

    BundleDependency bundleDependency = new BundleDependency.Builder().setBundleDescriptor(bundleDescriptor).build();

    addMavenModelDependency(bundleDependency);
    return getThis();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T addDependency(Dependency dependency) {
    com.zernova.maven.pom.parser.api.model.BundleDescriptor bundleDescriptor =
        new com.zernova.maven.pom.parser.api.model.BundleDescriptor.Builder()
            .setGroupId(dependency.getGroupId())
            .setArtifactId(dependency.getArtifactId())
            .setVersion(dependency.getVersion())
            .setType(dependency.getType())
            .setClassifier(dependency.getClassifier())
            .setOptional(dependency.getOptional())
            .setSystemPath(dependency.getSystemPath())
            .setExclusions(dependency.getExclusions().stream()
                .map(exclusion -> new ArtifactCoordinates(exclusion.getGroupId(), exclusion.getArtifactId())).collect(toList()))
            .build();


    BundleDependency.Builder bundleDependencyBuilder = new BundleDependency.Builder().setBundleDescriptor(bundleDescriptor);
    if (dependency.getScope() != null) {
      bundleDependencyBuilder.setScope(valueOf(dependency.getScope().toUpperCase())).build();
    }

    addMavenModelDependency(bundleDependencyBuilder.build());
    return getThis();
  }

  private void addMavenModelDependency(BundleDependency bundleDependency) {
    com.zernova.maven.pom.parser.api.model.BundleDescriptor descriptor = bundleDependency.getDescriptor();
    if (!MULE_PLUGIN_CLASSIFIER.equals(descriptor.getClassifier().orElse(null))) {
      model.addSharedLibraryDependency(descriptor.getGroupId(), descriptor.getArtifactId());
    }
    model.addDependency(bundleDependency);
  }

  @Override
  public S build() {
    checkState(artifactDeclaration != null, "artifact configuration cannot be null");
    return createService(() -> {
      String applicationName = UUID.getUUID() + "-artifact-temp-app";
      File applicationFolder = new File(getExecutionFolder(), applicationName);
      Properties deploymentProperties = new Properties();
      deploymentProperties.putAll(forcedDeploymentProperties());
      Set<String> configs = singleton("empty-app.xml");

      model.createDeployablePomFile(applicationFolder.toPath());
      model.updateArtifactPom(applicationFolder.toPath());

      MavenClientProvider mavenClientProvider =
          MavenClientProvider.discoverProvider(AbstractArtifactAgnosticServiceBuilder.class.getClassLoader());
      ClassLoaderConfiguration classLoaderConfiguration;
      try (MavenClient mavenClient = mavenClientProvider.createMavenClient(GlobalConfigLoader.getMavenConfig())) {
        classLoaderConfiguration =
            new DeployableMavenClassLoaderConfigurationLoader(of(mavenClient))
                .load(applicationFolder, singletonMap(BundleDescriptor.class.getName(),
                                                      createTempBundleDescriptor()),
                      ArtifactType.APP);
      }

      for (String config : configs) {
        copy(this.getClass().getClassLoader().getResource(config), new File(applicationFolder, config));
      }

      File destinationFolder =
          applicationFolder.toPath().resolve(META_INF).resolve(MULE_ARTIFACT).toFile();
      createDirectories(destinationFolder.toPath());

      ZernovaVersion muleVersion = new ZernovaVersion("4.4.0");
      String artifactJson =
          new ZernovaApplicationModelJsonSerializer().serialize(serializeModel(applicationName, classLoaderConfiguration,
                                                                            configs,
                                                                            muleVersion.toCompleteNumericVersion()));

      try (FileWriter fileWriter = new FileWriter(new File(destinationFolder, "mule-artifact.json"))) {
        fileWriter.write(artifactJson);
      }

      ApplicationDescriptor artifactDescriptor =
          defaultApplicationFactory.createArtifactDescriptor(applicationFolder, of(deploymentProperties));
      artifactDescriptor.setMinMuleVersion(muleVersion);
      artifactDescriptor.setArtifactDeclaration(artifactDeclaration);
      artifactDescriptor.setAppProperties(artifactProperties);
      return defaultApplicationFactory.createArtifact(artifactDescriptor);
    });
  }

  private ZernovaApplicationModel serializeModel(String appName,
                                              ClassLoaderConfiguration classLoaderConfiguration,
                                              Set<String> configs, String muleVersion) {
    Map<String, Object> attributes = ImmutableMap.of("exportedResources",
                                                     newArrayList(classLoaderConfiguration.getExportedResources()),
                                                     "exportedPackages",
                                                     newArrayList(classLoaderConfiguration.getExportedPackages()));
    ZernovaArtifactLoaderDescriptor muleArtifactLoaderDescriptor = new ZernovaArtifactLoaderDescriptor("mule", attributes);
    ZernovaApplicationModel.ZernovaApplicationModelBuilder builder = new ZernovaApplicationModel.ZernovaApplicationModelBuilder();
    builder.setName(appName)
        .setMinMuleVersion(muleVersion)
        .setRequiredProduct(MULE)
        .withBundleDescriptorLoader(new ZernovaArtifactLoaderDescriptor("mule", emptyMap()))
        .withClassLoaderModelDescriptorLoader(muleArtifactLoaderDescriptor)
        .setConfigs(configs);
    return builder.build();
  }



  protected Map<String, String> forcedDeploymentProperties() {
    return emptyMap();
  }

  protected abstract S createService(ApplicationSupplier applicationSupplier);

  private void createTempMavenModel() {
    MavenModelBuilderProvider mavenModelBuilderProvider = discoverProvider();
    model = mavenModelBuilderProvider
        .createMavenModelBuilder(TMP_APP_GROUP_ID, TMP_APP_ARTIFACT_ID, "4.4.0", of(TMP_APP_MODEL_VERSION), empty());
  }

  private BundleDescriptor createTempBundleDescriptor() {
    return new BundleDescriptor.Builder().setArtifactId(TMP_APP_ARTIFACT_ID).setGroupId(TMP_APP_GROUP_ID)
        .setVersion(TMP_APP_VERSION).setClassifier("mule-application").build();
  }

  private T getThis() {
    return (T) this;
  }

  // Used in test cases
  MavenModelBuilder getModel() {
    return model;
  }

  // Used in test cases
  void setModel(MavenModelBuilder model) {
    this.model = model;
  }

}
