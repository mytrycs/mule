/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.internal.maven;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.globalconfig.api.GlobalConfigLoader.getMavenConfig;
import static com.zernova.runtime.module.artifact.activation.api.deployable.ArtifactModelResolver.applicationModelResolver;
import static com.zernova.runtime.module.artifact.activation.api.deployable.ArtifactModelResolver.domainModelResolver;
import static com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor.MULE_APPLICATION_CLASSIFIER;
import static com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor.MULE_DOMAIN_CLASSIFIER;
import static com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorConstants.EXPORTED_PACKAGES;
import static com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorConstants.EXPORTED_RESOURCES;
import static com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptorConstants.INCLUDE_TEST_DEPENDENCIES;

import static java.lang.Boolean.parseBoolean;
import static java.nio.file.Files.find;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

import com.zernova.maven.pom.parser.api.MavenPomParser;
import com.zernova.runtime.api.artifact.ArtifactCoordinates;
import com.zernova.runtime.api.deployment.meta.ZernovaDeployableModel;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.module.artifact.activation.api.deployable.DeployableProjectModel;
import com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class LightweightDeployableProjectModelBuilder extends AbstractMavenDeployableProjectModelBuilder {

  private final boolean isDomain;
  private final Optional<ZernovaDeployableModel> model;


  public LightweightDeployableProjectModelBuilder(File projectFolder, boolean isDomain) {
    this(projectFolder, empty(), isDomain);
  }

  public LightweightDeployableProjectModelBuilder(File projectFolder, Optional<ZernovaDeployableModel> model, boolean isDomain) {
    super(getMavenConfig(), projectFolder);
    this.model = model;
    this.isDomain = isDomain;
  }

  @Override
  protected DeployableProjectModel doBuild(MavenPomParser parser, ArtifactCoordinates deployableArtifactCoordinates) {
    Supplier<ZernovaDeployableModel> deployableModelResolver = getDeployableModelResolver();
    ZernovaDeployableModel deployableModel = deployableModelResolver.get();

    List<String> exportedPackages =
        getAttribute(deployableModel.getClassLoaderModelLoaderDescriptor().getAttributes(),
                     EXPORTED_PACKAGES);
    List<String> exportedResources =
        getAttribute(deployableModel.getClassLoaderModelLoaderDescriptor().getAttributes(),
                     EXPORTED_RESOURCES);

    return new DeployableProjectModel(exportedPackages, exportedResources, emptyList(),
                                      buildBundleDescriptor(deployableArtifactCoordinates, isDomain),
                                      deployableModelResolver,
                                      empty(), projectFolder, deployableBundleDependencies,
                                      sharedDeployableBundleDescriptors, additionalPluginDependencies);
  }

  @Override
  protected File getPomFromFolder(File projectFolder) {
    File mavenFolder = new File(projectFolder, "META-INF/maven");
    try (Stream<Path> stream = find(mavenFolder.toPath(), 3, (p, m) -> p.getFileName().toString().equals("pom.xml"))) {
      List<Path> pomLists = stream.collect(toList());
      if (pomLists.size() != 1) {
        throw new ZernovaRuntimeException(createStaticMessage("Could find the pom in " + mavenFolder.toPath()));
      }
      return pomLists.get(0).toFile();
    } catch (IOException e) {
      throw new ZernovaRuntimeException(e);
    }
  }

  @Override
  protected boolean isIncludeTestDependencies() {
    return parseBoolean(getSimpleAttribute(getDeployableModelResolver().get().getClassLoaderModelLoaderDescriptor()
        .getAttributes(),
                                           INCLUDE_TEST_DEPENDENCIES, "false"));
  }

  private Supplier<ZernovaDeployableModel> getDeployableModelResolver() {
    return () -> model.orElseGet(() -> {
      if (isDomain) {
        return domainModelResolver().resolve(new File(projectFolder, "META-INF/zernova-artifact"));
      } else {
        return applicationModelResolver().resolve(new File(projectFolder, "META-INF/zernova-artifact"));
      }
    });
  }

  private BundleDescriptor buildBundleDescriptor(ArtifactCoordinates artifactCoordinates, boolean isDomain) {
    return new BundleDescriptor.Builder()
        .setArtifactId(artifactCoordinates.getArtifactId())
        .setGroupId(artifactCoordinates.getGroupId())
        .setVersion(artifactCoordinates.getVersion())
        .setBaseVersion(artifactCoordinates.getVersion())
        .setType(PACKAGE_TYPE)
        .setClassifier(isDomain ? MULE_DOMAIN_CLASSIFIER : MULE_APPLICATION_CLASSIFIER)
        .build();
  }

}
