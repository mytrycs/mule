/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.repository.internal;

import static com.zernova.maven.client.api.MavenClientProvider.discoverProvider;
import static com.zernova.maven.client.api.model.MavenConfiguration.newMavenConfigurationBuilder;
import static com.zernova.maven.client.api.model.RemoteRepository.newRemoteRepositoryBuilder;
import static com.zernova.runtime.core.internal.util.ZernovaContainerUtils.getMuleLibDir;

import com.zernova.maven.client.api.MavenClient;
import com.zernova.maven.client.api.MavenClientProvider;
import com.zernova.maven.client.api.model.MavenConfiguration;
import com.zernova.maven.client.api.model.RemoteRepository;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.module.repository.api.RepositoryService;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryServiceFactory {

  /**
   * System property key to specify a custom repository folder. By default, the container will use
   * {@code $MULE_HOME/lib/repository}
   */
  public static final String MULE_REPOSITORY_FOLDER_PROPERTY = "mule.repository.folder";

  /**
   * System property key to specify the remote repositories to use. Multiple values must be comma separated.
   * <p>
   * If no value is provided then the repository will be disabled causing a {@code RepositoryServiceDisabledException} if any
   * method is called.
   */
  public static final String MULE_REMOTE_REPOSITORIES_PROPERTY = "mule.repository.repositories";

  private static final String REPOSITORY_FOLDER = "repository";

  private static final Logger logger = LoggerFactory.getLogger(RepositoryServiceFactory.class);

  public RepositoryService createRepositoryService() {
    File repositoryFolder = createRepositoryFolderIfDoesNotExists();
    List<RemoteRepository> remoteRepositories = collectRemoteRepositories();

    MavenClientProvider mavenClientProvider = discoverProvider(RepositoryServiceFactory.class.getClassLoader());
    MavenClient mavenClient = mavenClientProvider
        .createMavenClient(getMavenConfiguration(repositoryFolder, remoteRepositories));

    return new DefaultRepositoryService(mavenClient);
  }

  private MavenConfiguration getMavenConfiguration(File localRepositoryFolder, List<RemoteRepository> remoteRepositories) {
    final MavenConfiguration.MavenConfigurationBuilder mavenConfigurationBuilder = newMavenConfigurationBuilder()
        .localMavenRepositoryLocation(localRepositoryFolder);

    remoteRepositories.forEach(mavenConfigurationBuilder::remoteRepository);

    return mavenConfigurationBuilder.build();
  }

  private List<RemoteRepository> collectRemoteRepositories() {
    String[] remoteRepositoriesArray = System.getProperty(MULE_REMOTE_REPOSITORIES_PROPERTY, "").split(",");
    List<RemoteRepository> remoteRepositories = new ArrayList<>();
    for (String remoteRepository : remoteRepositoriesArray) {
      if (!remoteRepository.trim().equals("")) {
        try {
          remoteRepositories
              .add(newRemoteRepositoryBuilder().id(remoteRepository).url(new URL(remoteRepository.trim())).build());
        } catch (MalformedURLException e) {
          throw new ZernovaRuntimeException(e);
        }
      }
    }
    return remoteRepositories;
  }

  private File createRepositoryFolderIfDoesNotExists() {
    File repositoryFolder = resolveRepositoryFolder();
    createRepositoryFolder(repositoryFolder);
    return repositoryFolder;
  }

  private void createRepositoryFolder(File repositoryFolder) {
    if (!repositoryFolder.exists() && !repositoryFolder.mkdirs()) {
      throw new RuntimeException("Could not create dependencies folder with path " + repositoryFolder.getAbsolutePath());
    }
  }

  private File resolveRepositoryFolder() {
    String userDefinedDependenciesFolder = System.getProperty(MULE_REPOSITORY_FOLDER_PROPERTY);
    File repositoryFolder;
    if (userDefinedDependenciesFolder != null) {
      repositoryFolder = new File(userDefinedDependenciesFolder);
    } else {
      repositoryFolder = new File(getMuleLibDir(), REPOSITORY_FOLDER);
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Using dependencies folder " + repositoryFolder.getAbsolutePath());
    }
    return repositoryFolder;
  }

}
