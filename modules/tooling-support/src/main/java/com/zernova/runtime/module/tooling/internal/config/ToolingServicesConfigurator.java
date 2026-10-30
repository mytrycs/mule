/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.config;

import static com.zernova.runtime.api.connectivity.ConnectivityTestingService.CONNECTIVITY_TESTING_SERVICE_KEY;
import static com.zernova.runtime.api.metadata.MetadataService.METADATA_SERVICE_KEY;
import static com.zernova.runtime.api.metadata.MetadataService.NON_LAZY_METADATA_SERVICE_KEY;
import static com.zernova.runtime.api.store.ObjectStoreManager.BASE_IN_MEMORY_OBJECT_STORE_KEY;
import static com.zernova.runtime.api.value.ValueProviderService.VALUE_PROVIDER_SERVICE_KEY;
import static com.zernova.runtime.core.api.config.ZernovaDeploymentProperties.MULE_ADD_TOOLING_OBJECTS_TO_REGISTRY;
import static com.zernova.runtime.core.api.config.ZernovaDeploymentProperties.MULE_LAZY_INIT_DEPLOYMENT_PROPERTY;
import static com.zernova.runtime.core.api.data.sample.SampleDataService.SAMPLE_DATA_SERVICE_KEY;
import static com.zernova.runtime.metadata.api.cache.MetadataCacheIdGeneratorFactory.METADATA_CACHE_ID_GENERATOR_KEY;
import static com.zernova.runtime.metadata.internal.cache.MetadataCacheManager.METADATA_CACHE_MANAGER_KEY;
import static com.zernova.runtime.module.tooling.internal.config.RuntimeLockFactoryUtil.getRuntimeLockFactory;
import static com.zernova.runtime.module.tooling.internal.connectivity.LazyConnectivityTestingService.NON_LAZY_CONNECTIVITY_TESTING_SERVICE;
import static com.zernova.runtime.module.tooling.internal.data.sample.LazySampleDataService.NON_LAZY_SAMPLE_DATA_SERVICE;
import static com.zernova.runtime.module.tooling.internal.store.SharedPartitionedPersistentObjectStore.SHARED_PERSISTENT_OBJECT_STORE_KEY;
import static com.zernova.runtime.module.tooling.internal.value.LazyValueProviderService.NON_LAZY_VALUE_PROVIDER_SERVICE;

import static java.lang.Boolean.parseBoolean;

import com.zernova.runtime.api.artifact.Registry;
import com.zernova.runtime.api.config.custom.CustomizationService;
import com.zernova.runtime.api.config.custom.ServiceConfigurator;
import com.zernova.runtime.api.connectivity.ConnectivityTestingService;
import com.zernova.runtime.api.metadata.MetadataService;
import com.zernova.runtime.api.store.ObjectStoreManager;
import com.zernova.runtime.api.value.ValueProviderService;
import com.zernova.runtime.core.api.data.sample.SampleDataService;
import com.zernova.runtime.core.internal.util.store.ZernovaObjectStoreManager;
import com.zernova.runtime.metadata.internal.ZernovaMetadataService;
import com.zernova.runtime.metadata.internal.cache.DefaultPersistentMetadataCacheManager;
import com.zernova.runtime.metadata.internal.cache.MetadataCacheManager;
import com.zernova.runtime.module.tooling.internal.connectivity.DefaultConnectivityTestingService;
import com.zernova.runtime.module.tooling.internal.connectivity.LazyConnectivityTestingService;
import com.zernova.runtime.module.tooling.internal.data.sample.LazySampleDataService;
import com.zernova.runtime.module.tooling.internal.data.sample.ZernovaSampleDataService;
import com.zernova.runtime.module.tooling.internal.metadata.LazyMetadataService;
import com.zernova.runtime.module.tooling.internal.metadata.cache.lazy.DelegateMetadataCacheIdGeneratorFactory;
import com.zernova.runtime.module.tooling.internal.metadata.cache.lazy.DelegateMetadataCacheManager;
import com.zernova.runtime.module.tooling.internal.metadata.model.ModelBasedMetadataCacheIdGeneratorFactory;
import com.zernova.runtime.module.tooling.internal.store.SharedPartitionedPersistentObjectStore;
import com.zernova.runtime.module.tooling.internal.value.LazyValueProviderService;
import com.zernova.runtime.module.tooling.internal.value.ZernovaValueProviderService;

import java.io.File;


public class ToolingServicesConfigurator implements ServiceConfigurator {

  public static final String SHARED_PARTITIONED_PERSISTENT_OBJECT_STORE_PATH = "_sharedPartitionatedPersistentObjectStorePath";

  private static final String DEFAULT_METADATA_CACHE_MANAGER_KEY = "_defaultPersistentMetadataCacheManager";
  private static final String DEFAULT_METADATA_CACHE_ID_GENERATOR_KEY = "_defaultMetadataCacheIdGenerator";
  private static final String LAZY_MULE_OBJECT_STORE_MANAGER = "_muleLazyObjectStoreManager";

  @Override
  public void configure(CustomizationService customizationService) {
    if (!parseBoolean(customizationService.getArtifactProperties().get(MULE_ADD_TOOLING_OBJECTS_TO_REGISTRY))) {
      return;
    }

    customizationService.registerCustomServiceClass(METADATA_SERVICE_KEY,
                                                    ZernovaMetadataService.class,
                                                    false);
    customizationService.registerCustomServiceClass(VALUE_PROVIDER_SERVICE_KEY,
                                                    ZernovaValueProviderService.class,
                                                    false);
    customizationService.registerCustomServiceClass(SAMPLE_DATA_SERVICE_KEY,
                                                    ZernovaSampleDataService.class,
                                                    false);
    customizationService.registerCustomServiceClass(CONNECTIVITY_TESTING_SERVICE_KEY,
                                                    DefaultConnectivityTestingService.class,
                                                    false);
    customizationService.registerCustomServiceClass(METADATA_CACHE_MANAGER_KEY,
                                                    DefaultPersistentMetadataCacheManager.class,
                                                    false);
    customizationService.registerCustomServiceClass(METADATA_CACHE_ID_GENERATOR_KEY,
                                                    ModelBasedMetadataCacheIdGeneratorFactory.class,
                                                    false);

    if (parseBoolean(customizationService.getArtifactProperties().get(MULE_LAZY_INIT_DEPLOYMENT_PROPERTY))) {
      configureLazyToolingServices(customizationService);
    }
  }

  private void configureLazyToolingServices(CustomizationService customizationService) {
    customizationService.registerCustomServiceImpl(CONNECTIVITY_TESTING_SERVICE_KEY,
                                                   new LazyConnectivityTestingService(registry -> registry
                                                       .<ConnectivityTestingService>lookupByName(NON_LAZY_CONNECTIVITY_TESTING_SERVICE)
                                                       .get()),
                                                   false);
    customizationService.registerCustomServiceClass(NON_LAZY_CONNECTIVITY_TESTING_SERVICE,
                                                    DefaultConnectivityTestingService.class, false);

    customizationService.registerCustomServiceImpl(METADATA_SERVICE_KEY,
                                                   new LazyMetadataService(registry -> registry
                                                       .<MetadataService>lookupByName(NON_LAZY_METADATA_SERVICE_KEY)
                                                       .get()),
                                                   false);
    customizationService.registerCustomServiceClass(NON_LAZY_METADATA_SERVICE_KEY, ZernovaMetadataService.class, false);

    customizationService.registerCustomServiceImpl(VALUE_PROVIDER_SERVICE_KEY,
                                                   new LazyValueProviderService(registry -> registry
                                                       .<ValueProviderService>lookupByName(NON_LAZY_VALUE_PROVIDER_SERVICE)
                                                       .get()),
                                                   false);
    customizationService.registerCustomServiceClass(NON_LAZY_VALUE_PROVIDER_SERVICE, ZernovaValueProviderService.class, false);

    customizationService.registerCustomServiceImpl(SAMPLE_DATA_SERVICE_KEY,
                                                   new LazySampleDataService(registry -> registry
                                                       .<SampleDataService>lookupByName(NON_LAZY_SAMPLE_DATA_SERVICE)
                                                       .get()),
                                                   false);
    customizationService.registerCustomServiceClass(NON_LAZY_SAMPLE_DATA_SERVICE, ZernovaSampleDataService.class, false);

    customizationService.registerCustomServiceClass(DEFAULT_METADATA_CACHE_ID_GENERATOR_KEY,
                                                    ModelBasedMetadataCacheIdGeneratorFactory.class, false);
    customizationService.registerCustomServiceImpl(METADATA_CACHE_ID_GENERATOR_KEY,
                                                   new DelegateMetadataCacheIdGeneratorFactory(registry -> registry
                                                       .<ModelBasedMetadataCacheIdGeneratorFactory>lookupByName(DEFAULT_METADATA_CACHE_ID_GENERATOR_KEY)
                                                       .get()),
                                                   false);

    final var sharedPartitionedPersistentObjectStorePath =
        customizationService.getArtifactProperties().get(SHARED_PARTITIONED_PERSISTENT_OBJECT_STORE_PATH);
    if (sharedPartitionedPersistentObjectStorePath != null) {
      // We need to first define this service so it would be later initialized
      customizationService.registerCustomServiceClass(SHARED_PERSISTENT_OBJECT_STORE_KEY,
                                                      SharedPartitionedPersistentObjectStore.class);
      customizationService.registerCustomServiceImpl(SHARED_PERSISTENT_OBJECT_STORE_KEY,
                                                     new SharedPartitionedPersistentObjectStore<>(new File(sharedPartitionedPersistentObjectStorePath),
                                                                                                  getRuntimeLockFactory()));
      ZernovaObjectStoreManager osm = new ZernovaObjectStoreManager();
      osm.setBasePersistentStoreKey(SHARED_PERSISTENT_OBJECT_STORE_KEY);
      osm.setBaseTransientStoreKey(BASE_IN_MEMORY_OBJECT_STORE_KEY);
      customizationService.registerCustomServiceImpl(LAZY_MULE_OBJECT_STORE_MANAGER, osm);

      customizationService.registerCustomServiceClass(DEFAULT_METADATA_CACHE_MANAGER_KEY,
                                                      DefaultPersistentMetadataCacheManager.class,
                                                      false);
      customizationService.registerCustomServiceImpl(METADATA_CACHE_MANAGER_KEY,
                                                     new DelegateMetadataCacheManager(this::lookupMetadataCacheManager),
                                                     false);
    }
  }

  private MetadataCacheManager lookupMetadataCacheManager(Registry registry) {
    DefaultPersistentMetadataCacheManager defaultPersistentMetadataCacheManager = registry
        .<DefaultPersistentMetadataCacheManager>lookupByName(DEFAULT_METADATA_CACHE_MANAGER_KEY)
        .get();
    defaultPersistentMetadataCacheManager.setLockFactory(getRuntimeLockFactory());
    defaultPersistentMetadataCacheManager.setObjectStoreManager(registry
        .<ObjectStoreManager>lookupByName(LAZY_MULE_OBJECT_STORE_MANAGER)
        .get());

    return defaultPersistentMetadataCacheManager;
  }

}
