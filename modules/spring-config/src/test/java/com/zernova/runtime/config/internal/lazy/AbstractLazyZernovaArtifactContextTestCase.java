/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.lazy;

import static com.zernova.runtime.api.meta.Category.COMMUNITY;
import static com.zernova.runtime.core.api.config.ZernovaProperties.OBJECT_REGISTRY;
import static com.zernova.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider.getExtensionModel;
import static com.zernova.runtime.core.internal.el.function.ZernovaFunctionsBindingContextProvider.CORE_FUNCTIONS_PROVIDER_REGISTRY_KEY;
import static com.zernova.tck.util.ZernovaContextUtils.mockContextWithServices;
import static com.zernova.tck.util.ZernovaContextUtils.registerIntoMockContext;
import static com.zernova.test.allure.AllureConstants.ConfigurationComponentLocatorFeature.CONFIGURATION_COMPONENT_LOCATOR;
import static com.zernova.test.allure.AllureConstants.ConfigurationComponentLocatorFeature.ComponentLifeCycle.COMPONENT_LIFE_CYCLE;
import static com.zernova.test.allure.AllureConstants.LazyInitializationFeature.LAZY_INITIALIZATION;

import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Optional.empty;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.api.memory.management.MemoryManagementService;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.XmlDslModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.metadata.ExpressionLanguageMetadataService;
import com.zernova.runtime.app.declaration.api.ArtifactDeclaration;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.serialization.ArtifactAstDeserializer;
import com.zernova.runtime.ast.api.serialization.ArtifactAstSerializerProvider;
import com.zernova.runtime.config.api.dsl.model.ComponentBuildingDefinitionRegistry;
import com.zernova.runtime.config.dsl.model.AbstractDslModelTestCase;
import com.zernova.runtime.config.internal.context.BaseConfigurationComponentLocator;
import com.zernova.runtime.config.internal.context.ObjectProviderAwareBeanFactory;
import com.zernova.runtime.config.internal.context.lazy.LazyZernovaArtifactContext;
import com.zernova.runtime.config.internal.model.DefaultComponentBuildingDefinitionRegistryFactory;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.DefaultZernovaConfiguration;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.internal.config.InternalCustomizationService;
import com.zernova.runtime.core.internal.el.function.ZernovaFunctionsBindingContextProvider;
import com.zernova.runtime.core.internal.exception.ContributedErrorTypeLocator;
import com.zernova.runtime.core.internal.exception.ContributedErrorTypeRepository;
import com.zernova.runtime.core.internal.registry.DefaultRegistry;
import com.zernova.runtime.core.privileged.processor.chain.DefaultMessageProcessorChainBuilder;
import com.zernova.runtime.extension.api.model.ImmutableExtensionModel;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.mockito.Mock;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.ResolvableType;

import io.qameta.allure.Feature;
import io.qameta.allure.Features;
import io.qameta.allure.Story;

@Features({@Feature(LAZY_INITIALIZATION), @Feature(CONFIGURATION_COMPONENT_LOCATOR)})
@Story(COMPONENT_LIFE_CYCLE)
public abstract class AbstractLazyZernovaArtifactContextTestCase extends AbstractDslModelTestCase {

  protected LazyZernovaArtifactContext lazyMuleArtifactContext;

  protected static final String MY_FLOW = "myFlow";

  @Mock(extraInterfaces = {Initialisable.class, Disposable.class, Startable.class, Stoppable.class})
  protected Processor targetProcessor;

  @Mock
  private ExtensionManager extensionManager;

  @Mock
  private InternalCustomizationService customizationService;

  protected TestObjectProviderAwareBeanFactory beanFactory;

  @Before
  public void setup() throws Exception {
    ZernovaContext muleContext = mockContextWithServices();
    Set<ExtensionModel> extensions = ImmutableSet.<ExtensionModel>builder()
        .add(getExtensionModel())
        .add(mockExtension)
        .build();

    when(muleContext.getConfiguration()).thenReturn(new DefaultZernovaConfiguration());
    when(extensionManager.getExtensions()).thenReturn(extensions);
    when(muleContext.getExecutionClassLoader()).thenReturn(currentThread().getContextClassLoader());
    when(muleContext.getExtensionManager()).thenReturn(extensionManager);
    when(muleContext.getCustomizationService()).thenReturn(customizationService);

    registerIntoMockContext(muleContext, OBJECT_REGISTRY, new DefaultRegistry(muleContext));
    registerIntoMockContext(muleContext, CORE_FUNCTIONS_PROVIDER_REGISTRY_KEY, mock(ZernovaFunctionsBindingContextProvider.class));

    lazyMuleArtifactContext = createLazyMuleArtifactContextStub(muleContext);

    registerIntoMockContext(muleContext, MY_FLOW, new DefaultMessageProcessorChainBuilder().chain(targetProcessor));

    doAnswer(a -> {
      onProcessorInitialization();
      return null;
    }).when((Initialisable) targetProcessor).initialise();
  }

  protected abstract void onProcessorInitialization();

  protected abstract ArtifactDeclaration getArtifactDeclaration();

  private TestObjectProviderAwareBeanFactory createBeanFactoryMock(ZernovaContext muleContext) {
    return new TestObjectProviderAwareBeanFactory(muleContext);
  }

  @Override
  protected ExtensionModel createExtension(String name, XmlDslModel xmlDslModel, List<ConfigurationModel> configs,
                                           List<ConnectionProviderModel> connectionProviders) {
    return new ImmutableExtensionModel(EXTENSION_NAME,
                                       "",
                                       "1.0",
                                       "Mulesoft",
                                       COMMUNITY,
                                       emptyList(),
                                       emptyList(),
                                       emptyList(),
                                       emptyList(),
                                       emptyList(),
                                       emptyList(),
                                       null,
                                       xmlDslModel,
                                       emptySet(),
                                       emptySet(),
                                       emptySet(),
                                       emptySet(),
                                       emptySet(),
                                       emptySet(),
                                       emptySet(),
                                       emptySet(),
                                       emptySet(),
                                       emptySet());
  }

  private ArtifactAst loadAst(final String astName, Set<ExtensionModel> extensionModels) throws IOException {
    ArtifactAstDeserializer defaultArtifactAstDeserializer = new ArtifactAstSerializerProvider().getDeserializer();
    ArtifactAst deserializedArtifactAst = defaultArtifactAstDeserializer
        .deserialize(this.getClass().getResourceAsStream("/asts/" + astName + ".ast"),
                     name -> extensionModels.stream()
                         .filter(x -> x.getName().equals(name))
                         .findFirst()
                         .orElse(null));

    return deserializedArtifactAst;
  }

  private LazyZernovaArtifactContext createLazyMuleArtifactContextStub(ZernovaContext muleContext) throws IOException {
    final ArtifactAst artifactAst = loadAst(this.getClass().getSimpleName(), getExtensions(muleContext.getExtensionManager()));

    final ComponentBuildingDefinitionRegistry componentBuildingDefinitionRegistry =
        new DefaultComponentBuildingDefinitionRegistryFactory().create(artifactAst.dependencies(),
                                                                       artifactAst::dependenciesDsl);

    LazyZernovaArtifactContext muleArtifactContext =
        new LazyZernovaArtifactContext(muleContext,
                                    artifactAst,
                                    empty(),
                                    new BaseConfigurationComponentLocator(),
                                    new ContributedErrorTypeRepository(), new ContributedErrorTypeLocator(),
                                    emptyMap(), APP, empty(),
                                    componentBuildingDefinitionRegistry,
                                    mock(MemoryManagementService.class),
                                    mock(FeatureFlaggingService.class),
                                    mock(ExpressionLanguageMetadataService.class)) {

          @Override
          protected DefaultListableBeanFactory createBeanFactory() {
            beanFactory = createBeanFactoryMock(muleContext);
            return beanFactory;
          }
        };

    muleArtifactContext.refresh();

    return muleArtifactContext;

  }

  private Set<ExtensionModel> getExtensions(ExtensionManager extensionManager) {
    return extensionManager == null ? emptySet() : extensionManager.getExtensions();
  }

  /**
   * A {@link ObjectProviderAwareBeanFactory} to test lazy configuration.
   */
  protected static class TestObjectProviderAwareBeanFactory extends ObjectProviderAwareBeanFactory {

    private final ZernovaContext muleContext;
    private boolean beanFactoryMustThrow;

    private final Map<String, BeanDefinition> registeredBeans = new HashMap<>();

    private final Map<String, BeanDefinition> registeredBeansByType = new HashMap<>();

    public TestObjectProviderAwareBeanFactory(ZernovaContext muleContext) {
      super(mock(ListableBeanFactory.class));
      this.muleContext = muleContext;
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
      return new String[0];
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
      registeredBeans.put(beanName, beanDefinition);
      registeredBeansByType.put(beanDefinition.getBeanClassName(), beanDefinition);
      super.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
      return getBean(requiredType);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
      if (beanFactoryMustThrow) {
        throw new NoSuchBeanDefinitionException(requiredType);
      }

      if (registeredBeans.containsKey(requiredType)) {
        return mock(requiredType);
      } else {
        return null;
      }
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
      return getBeanProvider(requiredType, false);
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType, boolean allowEagerInit) {
      if (beanFactoryMustThrow) {
        throw new NoSuchBeanDefinitionException(requiredType);
      } else {
        T bean = mock(requiredType);
        return new ObjectProvider<>() {

          @Override
          public T getObject() throws BeansException {
            return bean;
          }
        };
      }
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
      if (beanFactoryMustThrow) {
        throw new NoSuchBeanDefinitionException(requiredType);
      } else {
        T bean = null;
        try {
          bean = (T) mock(Class.forName(requiredType.getType().getTypeName()));
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
        T finalBean = bean;
        return new ObjectProvider<>() {

          @Override
          public T getObject() throws BeansException {
            return finalBean;
          }
        };
      }
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType, boolean allowEagerInit) {
      return getBeanProvider(requiredType, false);
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
      return getBean(name);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
      return (T) registeredBeans.get(name);
    }

    @Override
    public Object getBean(String name) throws BeansException {
      if (beanFactoryMustThrow) {
        if (registeredBeans.containsKey(name)) {
          return registeredBeans.get(name);
        } else {
          throw new NoSuchBeanDefinitionException(name);
        }
      }

      if (name.equals("_muleContext")) {
        return muleContext;
      }

      if (registeredBeans.containsKey(name)) {
        try {
          return mock(Class.forName(registeredBeans.get(name).getBeanClassName()));
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      } else {
        return null;
      }
    }

    @Override
    public void setSerializationId(String serializationId) {
      // Nothing to do.
    }

    @Override
    protected Map<String, Object> findAutowireCandidates(String beanName, Class<?> requiredType,
                                                         DependencyDescriptor descriptor) {
      if (requiredType.equals(ZernovaContext.class)) {
        return Collections.singletonMap("_muleContext", muleContext);
      }

      if (requiredType.equals(com.zernova.runtime.api.artifact.Registry.class)) {
        return Collections.singletonMap("_registry", new DefaultRegistry(muleContext));
      }

      return Collections.singletonMap(beanName, mock(requiredType));
    }

    public void setBeanFactoryMustThrow(boolean beanFactoryMustThrow) {
      this.beanFactoryMustThrow = beanFactoryMustThrow;
    }

    public boolean isRegisteredBeanDefiniion(String name) {
      return registeredBeans.containsKey(name);
    }

    @Override
    public boolean containsBean(String name) {
      return registeredBeans.containsKey(name);
    }

    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
      return false;
    }

    @Override
    public String[] getAliases(String name) {
      return null;
    }
  }
}
