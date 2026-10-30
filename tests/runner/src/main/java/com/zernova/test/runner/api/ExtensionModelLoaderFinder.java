/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.runner.api;

import static com.zernova.maven.pom.parser.api.MavenPomParserProvider.discoverProvider;
import static com.zernova.runtime.module.artifact.activation.api.extension.discovery.boot.ExtensionLoaderUtils.getLoaderById;

import com.zernova.maven.pom.parser.api.MavenPomParser;
import com.zernova.maven.pom.parser.api.MavenPomParserProvider;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;
import com.zernova.runtime.module.extension.internal.loader.java.DefaultJavaExtensionModelLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * Finds the extension model loader for a given extension artifact
 *
 * @since 4.0
 */
class ExtensionModelLoaderFinder {

  public ExtensionModelLoader findLoaderFromMulePlugin(File extensionMulePluginJson) {
    try {
      ZernovaPluginBasedLoaderFinder finder = new ZernovaPluginBasedLoaderFinder(extensionMulePluginJson);
      return finder.getLoader();
    } catch (FileNotFoundException e) {
      // TODO: MULE-12295. make it work for soap connect extensions when running from IDE
      return new DefaultJavaExtensionModelLoader();
    }
  }

  /**
   * Searches in the plugin pom.xml for the {@code testExtensionModelLoaderId} property which specifies with which loader the
   * extension must be loaded. The main use of this is for Test Extensions that don't generate a mule-artifact.json.
   */
  public Optional<ExtensionModelLoader> findLoaderByProperty(Artifact plugin, DependencyResolver dependencyResolver,
                                                             List<RemoteRepository> rootArtifactRemoteRepositories) {
    DefaultArtifact artifact = new DefaultArtifact(plugin.getGroupId(), plugin.getArtifactId(), "pom", plugin.getVersion());
    try {
      MavenPomParserProvider provider = discoverProvider();
      ArtifactResult artifactResult = dependencyResolver.resolveArtifact(artifact, rootArtifactRemoteRepositories);
      File pomFile = artifactResult.getArtifact().getFile();
      MavenPomParser mavenProject = provider.createMavenPomParserClient(pomFile.toPath());
      String id = mavenProject.getProperties().getProperty("testExtensionModelLoaderId");
      return id != null ? Optional.ofNullable(getLoaderById(id)) : Optional.empty();
    } catch (ArtifactResolutionException e) {
      throw new RuntimeException("Cannot load extension, the artifact: [" + plugin + "] cannot be resolved", e);
    }
  }
}
