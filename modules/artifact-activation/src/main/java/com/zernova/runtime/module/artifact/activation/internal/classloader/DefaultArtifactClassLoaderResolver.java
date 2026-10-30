/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.activation.internal.classloader;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.module.artifact.activation.api.plugin.PluginDescriptorResolver.pluginDescriptorResolver;
import static com.zernova.runtime.module.artifact.activation.internal.PluginsDependenciesProcessor.process;
import static com.zernova.runtime.module.artifact.api.classloader.ChildOnlyLookupStrategy.CHILD_ONLY;
import static com.zernova.runtime.module.artifact.api.classloader.ParentFirstLookupStrategy.PARENT_FIRST;
import static com.zernova.runtime.module.artifact.api.descriptor.BundleDescriptor.MULE_PLUGIN_CLASSIFIER;
import static com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor.DEFAULT_DOMAIN_NAME;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.zernova.runtime.container.api.ModuleRepository;
import com.zernova.runtime.container.internal.ContainerOnlyLookupStrategy;
import com.zernova.runtime.jpms.api.ZernovaContainerModule;
import com.zernova.runtime.module.artifact.activation.api.ArtifactActivationException;
import com.zernova.runtime.module.artifact.activation.api.classloader.ArtifactClassLoaderResolver;
import com.zernova.runtime.module.artifact.activation.api.plugin.PluginClassLoaderResolver;
import com.zernova.runtime.module.artifact.activation.api.plugin.PluginDescriptorResolver;
import com.zernova.runtime.module.artifact.activation.internal.nativelib.NativeLibraryFinder;
import com.zernova.runtime.module.artifact.activation.internal.nativelib.NativeLibraryFinderFactory;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoaderFilter;
import com.zernova.runtime.module.artifact.api.classloader.ChildFirstLookupStrategy;
import com.zernova.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import com.zernova.runtime.module.artifact.api.classloader.DefaultArtifactClassLoaderFilter;
import com.zernova.runtime.module.artifact.api.classloader.DelegateOnlyLookupStrategy;
import com.zernova.runtime.module.artifact.api.classloader.LookupStrategy;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaDeployableArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ParentFirstLookupStrategy;
import com.zernova.runtime.module.artifact.api.classloader.RegionClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ApplicationDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.DeployableArtifactDescriptor;
import com.zernova.runtime.module.artifact.api.descriptor.DomainDescriptor;
import com.zernova.runtime.module.artifact.internal.classloader.ZernovaPluginClassLoader;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultArtifactClassLoaderResolver implements ArtifactClassLoaderResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultArtifactClassLoaderResolver.class);

  public static final String PLUGIN_CLASSLOADER_IDENTIFIER = "/plugin/";

  private final ArtifactClassLoader containerClassLoader;
  private final ModuleRepository moduleRepository;
  private final NativeLibraryFinderFactory nativeLibraryFinderFactory;
  private final ZernovaDeployableArtifactClassLoader defaultDomainClassloader;

  public DefaultArtifactClassLoaderResolver(ArtifactClassLoader containerClassLoader,
                                            ModuleRepository moduleRepository,
                                            NativeLibraryFinderFactory nativeLibraryFinderFactory) {
    this.containerClassLoader = containerClassLoader;
    this.moduleRepository = moduleRepository;
    this.nativeLibraryFinderFactory = nativeLibraryFinderFactory;
    defaultDomainClassloader = createDomainClassLoader(new DomainDescriptor(DEFAULT_DOMAIN_NAME));
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createDomainClassLoader(DomainDescriptor descriptor) {
    return createDomainClassLoader(descriptor, (ownerClassLoader, artifactPluginDescriptor) -> empty());
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createDomainClassLoader(DomainDescriptor descriptor,
                                                                   PluginClassLoaderResolver pluginClassLoaderResolver) {
    return createDomainClassLoader(descriptor, pluginClassLoaderResolver, emptyList());
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createDomainClassLoader(DomainDescriptor descriptor,
                                                                   PluginClassLoaderResolver pluginClassLoaderResolver,
                                                                   List<URL> additionalClassloaderUrls) {
    String artifactId = getDomainId(descriptor.getName());

    ClassLoaderLookupPolicy parentLookupPolicy = getDomainParentLookupPolicy(containerClassLoader);

    RegionClassLoader regionClassLoader = new RegionClassLoader(artifactId,
                                                                descriptor,
                                                                containerClassLoader.getClassLoader(),
                                                                parentLookupPolicy);

    ArtifactClassLoaderFilter artifactClassLoaderFilter = createArtifactClassLoaderFilter(descriptor, parentLookupPolicy);

    ZernovaSharedDomainClassLoader domainClassLoader;
    if (descriptor.getName().equals(DEFAULT_DOMAIN_NAME)) {
      domainClassLoader = getDefaultDomainClassLoader(regionClassLoader, regionClassLoader.getClassLoaderLookupPolicy());
    } else {
      NativeLibraryFinder nativeLibraryFinder =
          nativeLibraryFinderFactory.create(descriptor.getDataFolderName(), descriptor.getLoadedNativeLibrariesFolderName(),
                                            descriptor.getClassLoaderConfiguration().getUrls());
      domainClassLoader =
          getCustomDomainClassLoader(regionClassLoader, descriptor, nativeLibraryFinder, additionalClassloaderUrls);
    }

    regionClassLoader.addClassLoader(domainClassLoader, artifactClassLoaderFilter);

    // This is needed because although plugins must have been already ordered, they are in a Set, so here we guarantee the ordered
    // list needed for the class
    List<ArtifactPluginDescriptor> artifactPluginDescriptors = process(descriptor.getPlugins(), false, List::add);

    artifactPluginDescriptors
        .stream()
        .map(pluginDependencyDescriptor -> pluginClassLoaderResolver.resolve(domainClassLoader, pluginDependencyDescriptor)
            .orElse(() -> resolvePluginClassLoader(domainClassLoader, pluginDependencyDescriptor)).get())
        .forEach(artifactPluginClassLoader -> regionClassLoader
            .addClassLoader(artifactPluginClassLoader,
                            createPluginClassLoaderFilter(descriptor,
                                                          artifactPluginClassLoader.getArtifactDescriptor(),
                                                          parentLookupPolicy)));

    return domainClassLoader;
  }

  private ZernovaSharedDomainClassLoader getDefaultDomainClassLoader(ArtifactClassLoader parent,
                                                                  ClassLoaderLookupPolicy containerLookupPolicy) {
    return new ZernovaSharedDomainClassLoader(new DomainDescriptor(DEFAULT_DOMAIN_NAME), parent.getClassLoader(),
                                           containerLookupPolicy.extend(emptyMap()), emptyList());
  }

  private ZernovaSharedDomainClassLoader getCustomDomainClassLoader(ArtifactClassLoader parent, DomainDescriptor domain,
                                                                 NativeLibraryFinder nativeLibraryFinder,
                                                                 List<URL> additionalClassloaderUrls) {
    validateDomain(domain);

    List<URL> resourcesPath =
        concat(additionalClassloaderUrls.stream(), stream(domain.getClassLoaderConfiguration().getUrls())).collect(toList());

    return new ZernovaSharedDomainClassLoader(domain, parent.getClassLoader(),
                                           getArtifactClassLoaderLookupPolicy(parent, domain),
                                           resourcesPath,
                                           nativeLibraryFinder);
  }

  private void validateDomain(DomainDescriptor domainDescriptor) {
    File domainFolder = domainDescriptor.getRootFolder();
    if (!(domainFolder.exists() && domainFolder.isDirectory())) {
      throw new ArtifactActivationException(createStaticMessage(format("Domain %s does not exist", domainDescriptor.getName())));
    }
  }

  /**
   * @param domainName name of the domain. Non empty.
   * @return the unique identifier for the domain in the container.
   */
  public static String getDomainId(String domainName) {
    checkArgument(!isEmpty(domainName), "domainName cannot be empty");

    return "domain/" + domainName;
  }

  private ClassLoaderLookupPolicy getDomainParentLookupPolicy(ArtifactClassLoader parentClassLoader) {
    return parentClassLoader.getClassLoaderLookupPolicy();
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createApplicationClassLoader(ApplicationDescriptor descriptor) {
    return createApplicationClassLoader(descriptor, () -> defaultDomainClassloader);
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createApplicationClassLoader(ApplicationDescriptor descriptor,
                                                                        PluginClassLoaderResolver pluginClassLoaderResolver) {
    return createApplicationClassLoader(descriptor, () -> defaultDomainClassloader, pluginClassLoaderResolver);
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createApplicationClassLoader(ApplicationDescriptor descriptor,
                                                                        PluginClassLoaderResolver pluginClassLoaderResolver,
                                                                        List<URL> additionalClassloaderUrls) {
    return createApplicationClassLoader(descriptor, () -> defaultDomainClassloader, pluginClassLoaderResolver,
                                        additionalClassloaderUrls);
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createApplicationClassLoader(ApplicationDescriptor descriptor,
                                                                        Supplier<ArtifactClassLoader> domainClassLoader) {
    return createApplicationClassLoader(descriptor, domainClassLoader,
                                        (ownerClassLoader, artifactPluginDescriptor) -> empty());
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createApplicationClassLoader(ApplicationDescriptor descriptor,
                                                                        Supplier<ArtifactClassLoader> domainClassLoader,
                                                                        PluginClassLoaderResolver pluginClassLoaderResolver) {
    return createApplicationClassLoader(descriptor, domainClassLoader, pluginClassLoaderResolver, emptyList());
  }

  @Override
  public ZernovaDeployableArtifactClassLoader createApplicationClassLoader(ApplicationDescriptor descriptor,
                                                                        Supplier<ArtifactClassLoader> domainClassLoader,
                                                                        PluginClassLoaderResolver pluginClassLoaderResolver,
                                                                        List<URL> additionalClassloaderUrls) {
    ArtifactClassLoader parentClassLoader = domainClassLoader.get();
    String artifactId = getApplicationId(parentClassLoader.getArtifactId(), descriptor.getName());

    ClassLoaderLookupPolicy parentLookupPolicy = getApplicationParentLookupPolicy(parentClassLoader);

    RegionClassLoader regionClassLoader = new RegionClassLoader(artifactId,
                                                                descriptor,
                                                                parentClassLoader.getClassLoader(),
                                                                parentLookupPolicy);

    ArtifactClassLoaderFilter artifactClassLoaderFilter = createArtifactClassLoaderFilter(descriptor, parentLookupPolicy);

    final ClassLoaderLookupPolicy classLoaderLookupPolicy = getArtifactClassLoaderLookupPolicy(parentClassLoader, descriptor);

    List<URL> resourcesPath =
        concat(additionalClassloaderUrls.stream(), stream(descriptor.getClassLoaderConfiguration().getUrls())).collect(toList());

    ZernovaDeployableArtifactClassLoader appClassLoader =
        new ZernovaApplicationClassLoader(artifactId, descriptor, regionClassLoader,
                                       nativeLibraryFinderFactory.create(descriptor.getDataFolderName(),
                                                                         descriptor.getLoadedNativeLibrariesFolderName(),
                                                                         descriptor.getClassLoaderConfiguration().getUrls()),
                                       resourcesPath,
                                       classLoaderLookupPolicy);

    regionClassLoader.addClassLoader(appClassLoader, artifactClassLoaderFilter);

    // This is needed because although plugins must have been already ordered, they are in a Set, so here we guarantee the ordered
    // list needed for the class loaders' creation.
    List<ArtifactPluginDescriptor> artifactPluginDescriptors = process(descriptor.getPlugins(), false, List::add);

    artifactPluginDescriptors
        .stream()
        .map(pluginDependencyDescriptor -> pluginClassLoaderResolver.resolve(appClassLoader, pluginDependencyDescriptor)
            .orElse(() -> resolvePluginClassLoader(appClassLoader, pluginDependencyDescriptor)).get())
        .forEach(artifactPluginClassLoader -> regionClassLoader
            .addClassLoader(artifactPluginClassLoader,
                            createPluginClassLoaderFilter(descriptor,
                                                          artifactPluginClassLoader.getArtifactDescriptor(),
                                                          parentLookupPolicy)));

    return appClassLoader;
  }

  private ClassLoaderLookupPolicy getApplicationParentLookupPolicy(ArtifactClassLoader parentClassLoader) {
    ArtifactDescriptor descriptor = parentClassLoader.getArtifactDescriptor();
    List<String> packages = new ArrayList<>(descriptor.getClassLoaderConfiguration().getExportedPackages());

    if (descriptor instanceof DeployableArtifactDescriptor) {
      for (ArtifactPluginDescriptor artifactPluginDescriptor : ((DeployableArtifactDescriptor) descriptor).getPlugins()) {
        packages.addAll(artifactPluginDescriptor.getClassLoaderConfiguration().getExportedPackages());
      }
    }

    return parentClassLoader.getClassLoaderLookupPolicy().extend(packages.stream(), PARENT_FIRST);
  }

  /**
   * @param domainId      name of the domain where the application is deployed. Non empty.
   * @param applicationId id of the application. Non empty.
   * @return the unique identifier for the application in the container.
   */
  public static String getApplicationId(String domainId, String applicationId) {
    checkArgument(!isEmpty(domainId), "domainId cannot be empty");
    checkArgument(!isEmpty(applicationId), "applicationName cannot be empty");

    return domainId + "/app/" + applicationId;
  }

  //////////////////////////////////////////////////////////
  //
  // zernova-application + zernova-domain common
  //
  //////////////////////////////////////////////////////////

  private ClassLoaderLookupPolicy getArtifactClassLoaderLookupPolicy(ArtifactClassLoader parent,
                                                                     DeployableArtifactDescriptor descriptor) {
    final List<String> packages = new ArrayList<>();

    for (ArtifactPluginDescriptor artifactPluginDescriptor : descriptor.getPlugins()) {
      packages.addAll(artifactPluginDescriptor.getClassLoaderConfiguration().getExportedPackages());
    }

    return parent.getClassLoaderLookupPolicy().extend(packages.stream(), PARENT_FIRST);
  }

  private ArtifactClassLoaderFilter createArtifactClassLoaderFilter(DeployableArtifactDescriptor artifactDescriptor,
                                                                    ClassLoaderLookupPolicy classLoaderLookupPolicy) {
    Set<String> artifactExportedPackages = sanitizeExportedPackages(artifactDescriptor,
                                                                    classLoaderLookupPolicy,
                                                                    artifactDescriptor.getClassLoaderConfiguration()
                                                                        .getExportedPackages());

    return new DefaultArtifactClassLoaderFilter(artifactExportedPackages,
                                                artifactDescriptor.getClassLoaderConfiguration().getExportedResources());
  }

  private ArtifactClassLoaderFilter createPluginClassLoaderFilter(DeployableArtifactDescriptor artifactDescriptor,
                                                                  ArtifactPluginDescriptor pluginDescriptor,
                                                                  ClassLoaderLookupPolicy classLoaderLookupPolicy) {
    Set<String> sanitizedArtifactExportedPackages =
        sanitizeExportedPackages(artifactDescriptor, classLoaderLookupPolicy,
                                 pluginDescriptor.getClassLoaderConfiguration().getExportedPackages());

    Set<String> replacedPackages =
        artifactDescriptor.getClassLoaderConfiguration().getExportedPackages().stream()
            .filter(p -> sanitizedArtifactExportedPackages.contains(p)).collect(toSet());
    if (!replacedPackages.isEmpty()) {
      sanitizedArtifactExportedPackages.removeAll(replacedPackages);
      LOGGER.warn("Exported packages from plugin '" + pluginDescriptor.getName() + "' are provided by the artifact owner: "
          + replacedPackages);
    }
    return new DefaultArtifactClassLoaderFilter(sanitizedArtifactExportedPackages,
                                                pluginDescriptor.getClassLoaderConfiguration().getExportedResources());
  }

  private Set<String> sanitizeExportedPackages(DeployableArtifactDescriptor artifactDescriptor,
                                               ClassLoaderLookupPolicy classLoaderLookupPolicy,
                                               Set<String> artifactExportedPackages) {
    Set<String> sanitizedArtifactExportedPackages = new HashSet<>(artifactExportedPackages);

    Set<String> containerProvidedPackages = sanitizedArtifactExportedPackages.stream().filter(p -> {
      LookupStrategy lookupStrategy = classLoaderLookupPolicy.getPackageLookupStrategy(p);
      return !(lookupStrategy instanceof ChildFirstLookupStrategy);
    }).collect(toSet());
    if (!containerProvidedPackages.isEmpty()) {
      sanitizedArtifactExportedPackages.removeAll(containerProvidedPackages);
      LOGGER.warn("Exported packages from artifact '" + artifactDescriptor.getName() + "' are provided by parent class loader: "
          + containerProvidedPackages);
    }
    return sanitizedArtifactExportedPackages;
  }

  //////////////////////////////////////////////////////////
  //
  // zernova-plugin
  //
  //////////////////////////////////////////////////////////

  @Override
  public ZernovaArtifactClassLoader createMulePluginClassLoader(ZernovaDeployableArtifactClassLoader ownerArtifactClassLoader,
                                                             ArtifactPluginDescriptor descriptor,
                                                             PluginDescriptorResolver pluginDescriptorResolver) {
    return createMulePluginClassLoader(ownerArtifactClassLoader, descriptor, pluginDescriptorResolver,
                                       (ownerClassLoader, artifactPluginDescriptor) -> empty());

  }

  @Override
  public ZernovaArtifactClassLoader createMulePluginClassLoader(ZernovaDeployableArtifactClassLoader ownerArtifactClassLoader,
                                                             ArtifactPluginDescriptor descriptor,
                                                             PluginDescriptorResolver pluginDescriptorResolver,
                                                             PluginClassLoaderResolver pluginClassLoaderResolver) {
    RegionClassLoader regionClassLoader = (RegionClassLoader) ownerArtifactClassLoader.getParent();
    final String pluginArtifactId = getArtifactPluginId(regionClassLoader.getArtifactId(), descriptor.getName());

    ClassLoaderLookupPolicy pluginLookupPolicy = createPluginLookupPolicy(descriptor,
                                                                          ownerArtifactClassLoader,
                                                                          pluginDescriptorResolver,
                                                                          pluginClassLoaderResolver);

    ZernovaArtifactClassLoader pluginClassLoader =
        new ZernovaPluginClassLoader(pluginArtifactId, descriptor, descriptor.getClassLoaderConfiguration().getUrls(),
                                  regionClassLoader, pluginLookupPolicy);
    return pluginClassLoader;
  }

  protected ClassLoaderLookupPolicy createPluginLookupPolicy(ArtifactPluginDescriptor descriptor,
                                                             ZernovaDeployableArtifactClassLoader ownerArtifactClassLoader,
                                                             PluginDescriptorResolver pluginDescriptorResolver,
                                                             PluginClassLoaderResolver pluginClassLoaderResolver) {
    RegionClassLoader regionClassLoader = (RegionClassLoader) ownerArtifactClassLoader.getParent();
    ClassLoaderLookupPolicy baseLookupPolicy = regionClassLoader.getClassLoaderLookupPolicy()
        .extend(regionClassLoader.filterForClassLoader(regionClassLoader.getOwnerClassLoader())
            .getExportedClassPackages()
            .stream(), PARENT_FIRST);

    Set<ArtifactPluginDescriptor> pluginsDescriptors = ownerArtifactClassLoader.getArtifactPluginClassLoaders().stream()
        .map(p -> (ArtifactPluginDescriptor) (p.getArtifactDescriptor())).collect(toSet());
    Map<String, LookupStrategy> pluginsLookupPolicies = new HashMap<>();

    descriptor.getClassLoaderConfiguration().getDependencies()
        .stream()
        .filter(dependency -> dependency.getDescriptor().getClassifier()
            .map(MULE_PLUGIN_CLASSIFIER::equals)
            .orElse(false))
        .map(dependency -> pluginDescriptorResolver.resolve(pluginsDescriptors, dependency.getDescriptor()))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(dependencyPluginDescriptor -> {
          for (String exportedPackage : dependencyPluginDescriptor.getClassLoaderConfiguration().getExportedPackages()) {
            pluginsLookupPolicies.put(exportedPackage, PARENT_FIRST);
          }

          if (isPrivilegedPluginDependency(descriptor, dependencyPluginDescriptor)) {
            ArtifactClassLoader pluginClassLoader = pluginClassLoaderResolver
                .resolve(regionClassLoader.getOwnerClassLoader(), dependencyPluginDescriptor)
                .orElse(() -> regionClassLoader.getArtifactPluginClassLoaders()
                    .stream().filter(
                                     c -> c
                                         .getArtifactDescriptor()
                                         .getBundleDescriptor()
                                         .getArtifactId()
                                         .equals(dependencyPluginDescriptor
                                             .getBundleDescriptor()
                                             .getArtifactId()))
                    .findAny()
                    .orElseThrow(() -> new ArtifactActivationException(createStaticMessage("Cannot find classloader for plugin: "
                        + dependencyPluginDescriptor.getBundleDescriptor().getArtifactId()))))
                .get();
            LookupStrategy lookupStrategy = new DelegateOnlyLookupStrategy(pluginClassLoader.getClassLoader());

            for (String exportedPackage : dependencyPluginDescriptor.getClassLoaderConfiguration()
                .getPrivilegedExportedPackages()) {
              pluginsLookupPolicies.put(exportedPackage, lookupStrategy);
            }
          }
        });

    LookupStrategy containerOnlyLookupStrategy =
        containerClassLoader.getClassLoaderLookupPolicy().getClassLookupStrategy(ModuleRepository.class.getName());
    Set<String> muleModulesExportedPackages = new HashSet<>();

    for (ZernovaContainerModule module : moduleRepository.getModules()) {
      if (module.getPrivilegedArtifacts()
          .contains(descriptor.getBundleDescriptor().getGroupId() + ":" + descriptor.getBundleDescriptor().getArtifactId())) {
        for (String packageName : module.getPrivilegedExportedPackages()) {
          pluginsLookupPolicies.put(packageName, containerOnlyLookupStrategy);
        }
      }

      muleModulesExportedPackages.addAll(module.getExportedPackages());
    }

    List<String> pluginLocalPackages = new ArrayList<>();
    for (String localPackage : descriptor.getClassLoaderConfiguration().getLocalPackages()) {
      // packages exported from another artifact in the region will be ParentFirst,
      // even if they are also exported by the container.
      if (baseLookupPolicy.getPackageLookupStrategy(localPackage) instanceof ContainerOnlyLookupStrategy
          || (baseLookupPolicy.getPackageLookupStrategy(localPackage) instanceof ParentFirstLookupStrategy
              && muleModulesExportedPackages.contains(localPackage))) {
        LOGGER.debug("Plugin '" + descriptor.getName() + "' contains a local package '" + localPackage
            + "', but it will be ignored since it is already available from the container.");
      } else {
        pluginLocalPackages.add(localPackage);
      }
    }

    return baseLookupPolicy.extend(pluginsLookupPolicies).extend(pluginLocalPackages.stream(), CHILD_ONLY, true);
  }

  /**
   * @param parentArtifactId identifier of the artifact that owns the plugin. Non empty.
   * @param pluginName       name of the plugin. Non empty.
   * @return the unique identifier for the plugin inside the parent artifact.
   */
  private String getArtifactPluginId(String parentArtifactId, String pluginName) {
    checkArgument(!isEmpty(parentArtifactId), "parentArtifactId cannot be empty");
    checkArgument(!isEmpty(pluginName), "pluginName cannot be empty");

    return parentArtifactId + PLUGIN_CLASSLOADER_IDENTIFIER + pluginName;
  }

  private boolean isPrivilegedPluginDependency(ArtifactPluginDescriptor descriptor,
                                               ArtifactPluginDescriptor dependencyPluginDescriptor) {
    if (dependencyPluginDescriptor.getClassLoaderConfiguration().getPrivilegedExportedPackages().isEmpty()) {
      return false;
    }

    return dependencyPluginDescriptor.getClassLoaderConfiguration().getPrivilegedArtifacts()
        .stream()
        .filter(a -> a.startsWith(descriptor.getBundleDescriptor().getGroupId()
            + ":" + descriptor.getBundleDescriptor().getArtifactId()))
        .findFirst().isPresent();
  }

  private ZernovaArtifactClassLoader resolvePluginClassLoader(ArtifactClassLoader ownerClassLoader,
                                                           ArtifactPluginDescriptor descriptor) {
    return createMulePluginClassLoader((ZernovaDeployableArtifactClassLoader) ownerClassLoader,
                                       descriptor,
                                       pluginDescriptorResolver());
  }

}
