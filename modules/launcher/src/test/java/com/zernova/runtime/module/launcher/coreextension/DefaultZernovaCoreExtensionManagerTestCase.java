/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.launcher.coreextension;

import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.test.allure.AllureConstants.CoreExtensionsFeature.CORE_EXTENSIONS;
import static com.zernova.test.allure.AllureConstants.CoreExtensionsFeature.CoreExtensionsStory.CORE_EXTENSIONS_DEPENDENCY_INJECTION;
import static com.zernova.test.allure.AllureConstants.LockFactoryFeature.LOCK_FACTORY;
import static com.zernova.test.allure.AllureConstants.LockFactoryFeature.LockFactoryStory.SERVER_LOCK_FACTORY;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.service.Service;
import com.zernova.runtime.api.service.ServiceRepository;
import com.zernova.runtime.container.api.CoreExtensionsAware;
import com.zernova.runtime.container.api.ZernovaCoreExtension;
import com.zernova.runtime.core.api.config.bootstrap.ArtifactType;
import com.zernova.runtime.core.api.event.EventContextService;
import com.zernova.runtime.core.internal.lock.ServerLockFactory;
import com.zernova.runtime.module.artifact.api.classloader.ArtifactClassLoaderManager;
import com.zernova.runtime.module.deployment.api.ArtifactDeploymentListener;
import com.zernova.runtime.module.deployment.api.DeploymentListener;
import com.zernova.runtime.module.deployment.api.DeploymentService;
import com.zernova.runtime.module.deployment.api.DeploymentServiceAware;
import com.zernova.runtime.module.repository.api.RepositoryService;
import com.zernova.runtime.module.repository.api.RepositoryServiceAware;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.size.SmallTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.Test;

import org.mockito.InOrder;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

import jakarta.inject.Inject;

@SmallTest
@Feature(CORE_EXTENSIONS)
@Story(CORE_EXTENSIONS_DEPENDENCY_INJECTION)
public class DefaultZernovaCoreExtensionManagerTestCase extends AbstractZernovaTestCase {

  private final ZernovaCoreExtensionDiscoverer coreExtensionDiscoverer = mock(ZernovaCoreExtensionDiscoverer.class);
  private final ZernovaCoreExtensionDependencyResolver coreExtensionDependencyResolver =
      mock(ZernovaCoreExtensionDependencyResolver.class);
  private ZernovaCoreExtensionManagerServer coreExtensionManager =
      new DefaultZernovaCoreExtensionManagerServer(coreExtensionDiscoverer, coreExtensionDependencyResolver);

  @Test
  public void discoversMuleCoreExtension() throws Exception {
    coreExtensionManager.initialise();

    verify(coreExtensionDiscoverer).discover();
  }

  @Test
  public void injectsDeploymentServiceAwareCoreExtension() throws Exception {
    Consumer<DeploymentService> setServiceFunction = service -> coreExtensionManager.setDeploymentService(service);
    BiConsumer<List<TestDeploymentServiceAwareExtension>, DeploymentService> verificationFunction =
        (extensions, service) -> verify(extensions.get(0)).setDeploymentService(service);
    testServiceInjection(DeploymentService.class, TestDeploymentServiceAwareExtension.class, setServiceFunction,
                         verificationFunction);
  }

  @Test
  public void injectRepositoryServiceAwareCoreExtension() throws Exception {
    Consumer<RepositoryService> setServiceFunction = service -> coreExtensionManager.setRepositoryService(service);
    BiConsumer<List<TestRepositoryServiceAwareExtension>, RepositoryService> verificationFunction =
        (extensions, service) -> verify(extensions.get(0)).setRepositoryService(service);
    testServiceInjection(RepositoryService.class, TestRepositoryServiceAwareExtension.class, setServiceFunction,
                         verificationFunction);
  }

  @Test
  public void initializesDeploymentListenerCoreExtension() throws Exception {

    Consumer<DeploymentService> setServiceFunction = service -> coreExtensionManager.setDeploymentService(service);
    BiConsumer<List<TestDeploymentListenerExtension>, DeploymentService> verificationFunction =
        (extensions, service) -> verify(service).addDeploymentListener(extensions.get(0));
    testServiceInjection(DeploymentService.class, TestDeploymentListenerExtension.class, setServiceFunction,
                         verificationFunction);
  }

  @Test
  public void injectsCoreExtensionsAwareCoreExtension() throws Exception {
    Consumer<DeploymentService> setServiceFunction = service -> {
    };
    BiConsumer<List<TestCoreExtensionsAwareExtension>, DeploymentService> verificationFunction =
        (extensions, service) -> verify(extensions.get(0)).setCoreExtensions(new ArrayList<>(extensions));
    testServiceInjection(DeploymentService.class, TestCoreExtensionsAwareExtension.class, setServiceFunction,
                         verificationFunction);
  }


  @Test
  public void injectsDeploymentServiceOnExtension() throws Exception {
    Consumer<DeploymentService> setServiceFunction = coreExtensionManager::setDeploymentService;
    BiConsumer<List<TestDeploymentServiceExtension>, DeploymentService> verificationFunction =
        (extensions, service) -> verify(extensions.get(0), atLeastOnce()).setDeploymentService(service);
    testServiceInjection(DeploymentService.class, TestDeploymentServiceExtension.class, setServiceFunction, verificationFunction);
  }

  @Test
  public void injectRepositoryServiceOnExtension() throws Exception {
    Consumer<RepositoryService> setServiceFunction = coreExtensionManager::setRepositoryService;
    BiConsumer<List<TestRepositoryServiceExtension>, RepositoryService> verificationFunction =
        (extensions, service) -> verify(extensions.get(0), atLeastOnce()).setRepositoryService(service);
    testServiceInjection(RepositoryService.class, TestRepositoryServiceExtension.class, setServiceFunction, verificationFunction);
  }

  @Test
  public void injectsCoreExtensionsOnExtension() throws Exception {
    Consumer<DeploymentService> setServiceFunction = service -> {
    };
    BiConsumer<List<TestCoreExtensionsExtension>, DeploymentService> verificationFunction =
        (extensions, service) -> verify(extensions.get(0), atLeastOnce()).setCoreExtensions(new ArrayList<>(extensions));
    testServiceInjection(DeploymentService.class, TestCoreExtensionsExtension.class, setServiceFunction, verificationFunction);
  }

  @Test
  public void injectsArtifactClassLoaderManagerOnExtension() throws Exception {
    Consumer<ArtifactClassLoaderManager> setServiceFunction =
        service -> coreExtensionManager.setArtifactClassLoaderManager(service);

    BiConsumer<List<TestArtifactClassLoaderManagerExtension>, ArtifactClassLoaderManager> verificationFunction =
        (extensions, service) -> verify(extensions.get(0), atLeastOnce()).setArtifactClassLoaderManager(same(service));

    testServiceInjection(ArtifactClassLoaderManager.class, TestArtifactClassLoaderManagerExtension.class, setServiceFunction,
                         verificationFunction);
  }

  @Test
  public void injectsServiceRepositoryCoreExtension() throws Exception {
    Consumer<ServiceRepository> setServiceFunction = coreExtensionManager::setServiceRepository;

    BiConsumer<List<TestServiceRepositoryExtension>, ServiceRepository> verificationFunction =
        (extensions, service) -> verify(extensions.get(0), atLeastOnce()).setServiceRepository(service);

    testServiceInjection(ServiceRepository.class, TestServiceRepositoryExtension.class, setServiceFunction,
                         verificationFunction);
  }

  @Test
  public void injectsEventContextServiceAwareCoreExtension() throws Exception {
    Consumer<EventContextService> setServiceFunction = coreExtensionManager::setEventContextService;

    BiConsumer<List<TestEventContextServiceExtension>, EventContextService> verificationFunction =
        (extensions, service) -> verify(extensions.get(0), atLeastOnce()).setEventContextService(service);
    testServiceInjection(EventContextService.class, TestEventContextServiceExtension.class, setServiceFunction,
                         verificationFunction);
  }

  @Test
  public void injectsServiceOnExtension() throws Exception {
    List<ZernovaCoreExtension> extensions = new LinkedList<>();
    InjectedTestServiceExtension extension = mock(InjectedTestServiceExtension.class);
    extensions.add(extension);

    TestService service = mock(TestService.class);
    when(service.getName()).thenReturn("testService");
    ServiceRepository serviceRepository = mock(ServiceRepository.class);
    when(serviceRepository.getServices()).thenReturn(Collections.singletonList(service));

    when(coreExtensionDiscoverer.discover()).thenReturn(extensions);
    when(coreExtensionDependencyResolver.resolveDependencies(extensions)).thenReturn(extensions);

    coreExtensionManager.setServiceRepository(serviceRepository);
    coreExtensionManager.initialise();

    verify(extension, atLeastOnce()).setService(service);
  }

  @Test
  @Issue("W-14237711")
  @Feature(LOCK_FACTORY)
  @Story(SERVER_LOCK_FACTORY)
  public void injectsServerLockFactoryOnExtension() throws Exception {
    Consumer<ServerLockFactory> setServiceFunction = coreExtensionManager::setServerLockFactory;
    BiConsumer<List<TestLockFactoryExtension>, ServerLockFactory> verificationFunction =
        (extensions, service) -> verify(extensions.get(0), atLeastOnce()).setServerLockFactory(service);
    testServiceInjection(ServerLockFactory.class, TestLockFactoryExtension.class, setServiceFunction, verificationFunction);
  }

  @Test
  public void startsCoreExtensionsInOrder() throws Exception {
    List<ZernovaCoreExtension> extensions = new LinkedList<>();
    ZernovaCoreExtension extension1 = mock(ZernovaCoreExtension.class);
    ZernovaCoreExtension extension2 = mock(ZernovaCoreExtension.class);
    extensions.add(extension1);
    extensions.add(extension2);
    when(coreExtensionDiscoverer.discover()).thenReturn(extensions);

    List<ZernovaCoreExtension> orderedExtensions = new LinkedList<>();
    orderedExtensions.add(extension2);
    orderedExtensions.add(extension1);
    when(coreExtensionDependencyResolver.resolveDependencies(extensions)).thenReturn(orderedExtensions);
    coreExtensionManager.initialise();

    coreExtensionManager.start();

    InOrder ordered = inOrder(extension1, extension2);
    ordered.verify(extension2).start();
    ordered.verify(extension1).start();
  }


  @Test
  public void stopsCoreExtensionsInOrder() throws Exception {
    List<ZernovaCoreExtension> extensions = new LinkedList<>();
    ZernovaCoreExtension extension1 = mock(ZernovaCoreExtension.class);
    ZernovaCoreExtension extension2 = mock(ZernovaCoreExtension.class);
    extensions.add(extension1);
    extensions.add(extension2);
    when(coreExtensionDiscoverer.discover()).thenReturn(extensions);

    List<ZernovaCoreExtension> orderedExtensions = new LinkedList<>();
    orderedExtensions.add(extension1);
    orderedExtensions.add(extension2);
    when(coreExtensionDependencyResolver.resolveDependencies(extensions)).thenReturn(orderedExtensions);
    coreExtensionManager.initialise();
    coreExtensionManager.start();

    coreExtensionManager.stop();

    InOrder ordered = inOrder(extension1, extension2);
    ordered.verify(extension2).stop();
    ordered.verify(extension1).stop();
  }

  @Test
  public void stopsCoreExtensionsAfterStartFail() throws Exception {
    List<ZernovaCoreExtension> extensions = new LinkedList<>();
    ZernovaCoreExtension extension1 = mock(ZernovaCoreExtension.class);
    ZernovaCoreExtension extension2 = mock(ZernovaCoreExtension.class);

    Exception firstStartFailure = new RuntimeException();
    doThrow(firstStartFailure).when(extension1).start();

    when(coreExtensionDiscoverer.discover()).thenReturn(extensions);

    extensions.add(extension1);
    extensions.add(extension2);
    List<ZernovaCoreExtension> orderedExtensions = new LinkedList<>();
    orderedExtensions.add(extension1);
    orderedExtensions.add(extension2);
    when(coreExtensionDependencyResolver.resolveDependencies(extensions)).thenReturn(orderedExtensions);

    coreExtensionManager.initialise();
    try {
      coreExtensionManager.start();
      fail();
    } catch (Exception e) {
      assertThat(e, sameInstance(firstStartFailure));
    }
    coreExtensionManager.stop();

    verify(extension2, never()).stop();
  }

  @Test
  public void initializesCoreExtensionsInOrder() throws Exception {
    List<ZernovaCoreExtension> extensions = new LinkedList<>();
    ZernovaCoreExtension extension1 = mock(ZernovaCoreExtension.class);
    ZernovaCoreExtension extension2 = mock(ZernovaCoreExtension.class);
    extensions.add(extension1);
    extensions.add(extension2);
    when(coreExtensionDiscoverer.discover()).thenReturn(extensions);

    List<ZernovaCoreExtension> orderedExtensions = new LinkedList<>();
    orderedExtensions.add(extension2);
    orderedExtensions.add(extension1);
    when(coreExtensionDependencyResolver.resolveDependencies(extensions)).thenReturn(orderedExtensions);
    coreExtensionManager.initialise();

    InOrder ordered = inOrder(extension1, extension2);
    ordered.verify(extension2).initialise();
    ordered.verify(extension1).initialise();
  }

  @Test
  public void disposesCoreExtensions() throws Exception {
    List<ZernovaCoreExtension> extensions = new LinkedList<>();
    TestDeploymentServiceAwareExtension extension1 = mock(TestDeploymentServiceAwareExtension.class);
    ZernovaCoreExtension extension2 = mock(ZernovaCoreExtension.class);
    extensions.add(extension1);
    extensions.add(extension2);
    when(coreExtensionDiscoverer.discover()).thenReturn(extensions);

    List<ZernovaCoreExtension> orderedExtensions = new LinkedList<>();
    orderedExtensions.add(extension1);
    orderedExtensions.add(extension2);
    when(coreExtensionDependencyResolver.resolveDependencies(extensions)).thenReturn(orderedExtensions);
    coreExtensionManager.initialise();

    coreExtensionManager.dispose();

    InOrder inOrder = inOrder(extension1, extension2);
    inOrder.verify(extension1).dispose();
    inOrder.verify(extension2).dispose();
  }

  @Test
  public void disposesCoreExtensionsAfterStartFail() throws Exception {
    List<ZernovaCoreExtension> extensions = new LinkedList<>();
    ZernovaCoreExtension extension1 = mock(ZernovaCoreExtension.class);
    ZernovaCoreExtension extension2 = mock(ZernovaCoreExtension.class);

    Exception firstStartFailure = new RuntimeException();
    doThrow(firstStartFailure).when(extension1).initialise();

    when(coreExtensionDiscoverer.discover()).thenReturn(extensions);

    extensions.add(extension1);
    extensions.add(extension2);
    List<ZernovaCoreExtension> orderedExtensions = new LinkedList<>();
    orderedExtensions.add(extension1);
    orderedExtensions.add(extension2);
    when(coreExtensionDependencyResolver.resolveDependencies(extensions)).thenReturn(orderedExtensions);

    try {
      coreExtensionManager.initialise();
      fail();
    } catch (Exception e) {
      assertThat(e.getCause(), sameInstance(firstStartFailure));
    }
    coreExtensionManager.dispose();

    verify(extension2, never()).dispose();
  }

  @Test
  public void resolvesCoreExtensionDependencies() throws Exception {

    List<ZernovaCoreExtension> extensions = new LinkedList<>();
    ZernovaCoreExtension extension = mock(ZernovaCoreExtension.class);
    extensions.add(extension);
    when(coreExtensionDiscoverer.discover()).thenReturn(extensions);

    coreExtensionManager.initialise();

    verify(coreExtensionDependencyResolver).resolveDependencies(extensions);
  }

  @Test
  public void testAllCoreExtensionsAreStoppedAfterRuntimeException() throws Exception {
    TestDeploymentServiceAwareExtension extensionFailsStops = mock(TestDeploymentServiceAwareExtension.class);
    TestDeploymentServiceAwareExtension extensionStopsOk = mock(TestDeploymentServiceAwareExtension.class);
    InOrder stopsInOrder = inOrder(extensionFailsStops, extensionStopsOk);
    List<ZernovaCoreExtension> extensions = new LinkedList<>();
    when(coreExtensionDiscoverer.discover()).thenReturn(extensions);
    when(coreExtensionDependencyResolver.resolveDependencies(extensions)).thenReturn(extensions);
    doThrow(RuntimeException.class).when(extensionFailsStops).stop();
    extensions.add(extensionStopsOk);
    extensions.add(extensionFailsStops);
    coreExtensionManager.initialise();
    coreExtensionManager.start();
    try {
      coreExtensionManager.stop();
    } finally {
      stopsInOrder.verify(extensionFailsStops).stop();
      stopsInOrder.verify(extensionStopsOk).stop();
    }
  }

  @Test
  public void initializesArtifactDeploymentListenerCoreExtension() throws Exception {
    assertArtifactDeploymentListener(mock(TestArtifactDeploymentListenerExtension.class));
  }

  private <ServiceType, CoreExtensionType extends ZernovaCoreExtension> void testServiceInjection(Class<ServiceType> serviceType,
                                                                                               Class<CoreExtensionType> coreExtensionType,
                                                                                               Consumer<ServiceType> setServiceFunction,
                                                                                               BiConsumer<List<CoreExtensionType>, ServiceType> verificationFunction)
      throws ZernovaException {
    List<ZernovaCoreExtension> extensions = new LinkedList<>();
    CoreExtensionType extension = mock(coreExtensionType);
    extensions.add(extension);
    ServiceType service = mock(serviceType);
    when(coreExtensionDiscoverer.discover()).thenReturn(extensions);
    when(coreExtensionDependencyResolver.resolveDependencies(extensions)).thenReturn(extensions);
    setServiceFunction.accept(service);
    coreExtensionManager.initialise();
    verificationFunction.accept((List<CoreExtensionType>) extensions, service);
  }

  private void assertArtifactDeploymentListener(ArtifactDeploymentListener extension) throws Exception {
    List<ZernovaCoreExtension> extensions = new LinkedList<>();
    extensions.add((ZernovaCoreExtension) extension);

    when(coreExtensionDiscoverer.discover()).thenReturn(extensions);
    when(coreExtensionDependencyResolver.resolveDependencies(extensions)).thenReturn(extensions);
    DeploymentService deploymentService = mock(DeploymentService.class);

    TestZernovaCoreExtensionManager testCoreExtensionManager =
        new TestZernovaCoreExtensionManager(coreExtensionDiscoverer, coreExtensionDependencyResolver);
    testCoreExtensionManager.setDeploymentService(deploymentService);
    testCoreExtensionManager.initialise();

    verify(deploymentService).addDomainDeploymentListener(testCoreExtensionManager.domainDeploymentListener);
    verify(deploymentService).addDeploymentListener(testCoreExtensionManager.applicationDeploymentListener);
  }

  public interface TestDeploymentServiceAwareExtension extends ZernovaCoreExtension, DeploymentServiceAware {

  }

  public interface TestDeploymentServiceExtension extends ZernovaCoreExtension {

    @Inject
    void setDeploymentService(DeploymentService deploymentService);
  }

  public interface TestLockFactoryExtension extends ZernovaCoreExtension {

    @Inject
    void setServerLockFactory(ServerLockFactory serverLockFactory);
  }

  public interface TestRepositoryServiceAwareExtension extends ZernovaCoreExtension, RepositoryServiceAware {

  }

  public interface TestRepositoryServiceExtension extends ZernovaCoreExtension {

    @Inject
    void setRepositoryService(RepositoryService repositoryService);
  }

  public interface TestDeploymentListenerExtension extends ZernovaCoreExtension, DeploymentListener {

  }

  public interface TestCoreExtensionsAwareExtension extends ZernovaCoreExtension, CoreExtensionsAware {

  }

  public interface TestCoreExtensionsExtension extends ZernovaCoreExtension {

    @Inject
    void setCoreExtensions(List<ZernovaCoreExtension> coreExtensions);
  }

  public interface TestArtifactDeploymentListenerExtension extends ZernovaCoreExtension, ArtifactDeploymentListener {

  }

  public interface TestEventContextServiceExtension extends ZernovaCoreExtension {

    @Inject
    void setEventContextService(EventContextService eventContextService);
  }

  public interface TestServiceRepositoryExtension extends ZernovaCoreExtension {

    @Inject
    void setServiceRepository(ServiceRepository serviceRepository);
  }

  public interface TestArtifactClassLoaderManagerExtension extends ZernovaCoreExtension {

    @Inject
    void setArtifactClassLoaderManager(ArtifactClassLoaderManager artifactClassLoaderManager);
  }

  public interface InjectedTestServiceExtension extends ZernovaCoreExtension {

    @Inject
    void setService(TestService service);
  }

  public interface TestService extends Service {

  }

  private static class TestZernovaCoreExtensionManager extends DefaultZernovaCoreExtensionManagerServer {

    DeploymentListener applicationDeploymentListener;
    DeploymentListener domainDeploymentListener;

    public TestZernovaCoreExtensionManager(ZernovaCoreExtensionDiscoverer coreExtensionDiscoverer,
                                        ZernovaCoreExtensionDependencyResolver coreExtensionDependencyResolver) {
      super(coreExtensionDiscoverer, coreExtensionDependencyResolver);
    }

    @Override
    DeploymentListener createDeploymentListenerAdapter(ArtifactDeploymentListener artifactDeploymentListener, ArtifactType type) {
      if (type == APP) {
        applicationDeploymentListener = super.createDeploymentListenerAdapter(artifactDeploymentListener, type);
        return applicationDeploymentListener;
      }

      domainDeploymentListener = super.createDeploymentListenerAdapter(artifactDeploymentListener, type);

      return domainDeploymentListener;
    }
  }

}
