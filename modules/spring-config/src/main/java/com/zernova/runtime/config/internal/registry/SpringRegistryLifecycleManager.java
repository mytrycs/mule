/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.config.internal.registry;

import static com.zernova.runtime.config.internal.context.ZernovaArtifactContext.INNER_BEAN_PREFIX;

import com.zernova.runtime.api.el.ExpressionLanguage;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.LifecycleException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.api.lock.LockFactory;
import com.zernova.runtime.api.service.Service;
import com.zernova.runtime.api.store.ObjectStoreManager;
import com.zernova.runtime.config.internal.DependencyGraphLifecycleObjectSorter;
import com.zernova.runtime.config.internal.resolvers.AutoDiscoveredDependencyResolver;
import com.zernova.runtime.config.internal.resolvers.ConfigurationDependencyResolver;
import com.zernova.runtime.config.internal.resolvers.DeclaredDependencyResolver;
import com.zernova.runtime.config.internal.resolvers.DependencyGraphBeanDependencyResolver;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.Config;
import com.zernova.runtime.core.api.config.ZernovaConfiguration;
import com.zernova.runtime.core.api.construct.FlowConstruct;
import com.zernova.runtime.core.api.context.notification.ServerNotificationManager;
import com.zernova.runtime.core.api.exception.FlowExceptionHandler;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.security.SecurityManager;
import com.zernova.runtime.core.api.streaming.StreamingManager;
import com.zernova.runtime.core.api.transformer.Transformer;
import com.zernova.runtime.core.api.util.queue.QueueManager;
import com.zernova.runtime.core.internal.exception.GlobalErrorHandler;
import com.zernova.runtime.core.internal.lifecycle.EmptyLifecycleCallback;
import com.zernova.runtime.core.internal.lifecycle.LifecycleInterceptor;
import com.zernova.runtime.core.internal.lifecycle.RegistryLifecycleCallback;
import com.zernova.runtime.core.internal.lifecycle.RegistryLifecycleManager;
import com.zernova.runtime.core.internal.lifecycle.phases.LifecycleObjectSorter;
import com.zernova.runtime.core.internal.lifecycle.phases.ZernovaContextDisposePhase;
import com.zernova.runtime.core.internal.lifecycle.phases.ZernovaContextInitialisePhase;
import com.zernova.runtime.core.internal.lifecycle.phases.ZernovaContextStartPhase;
import com.zernova.runtime.core.internal.lifecycle.phases.ZernovaContextStopPhase;
import com.zernova.runtime.core.internal.lifecycle.phases.NotInLifecyclePhase;
import com.zernova.runtime.core.internal.registry.Registry;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;

import java.util.Map;

public class SpringRegistryLifecycleManager extends RegistryLifecycleManager {

  public SpringRegistryLifecycleManager(String id, Registry springRegistry, ZernovaContext muleContext,
                                        LifecycleInterceptor lifecycleInterceptor) {
    super(id, springRegistry, muleContext, lifecycleInterceptor);
  }

  @Override
  protected void registerPhases(Registry registry) {
    final RegistryLifecycleCallback<Registry> callback = new RegistryLifecycleCallback<>(this);

    registerPhase(NotInLifecyclePhase.PHASE_NAME, new NotInLifecyclePhase(), new EmptyLifecycleCallback<>());
    registerPhase(Initialisable.PHASE_NAME, new SpringContextInitialisePhase(), callback);
    registerPhase(Startable.PHASE_NAME, new ZernovaContextStartPhase(), callback);
    registerPhase(Stoppable.PHASE_NAME, new ZernovaContextStopPhase(), callback);
    registerPhase(Disposable.PHASE_NAME, new SpringContextDisposePhase(), callback);
  }

  @Override
  protected Map<String, Object> lookupObjectsForLifecycle() {
    if (Initialisable.PHASE_NAME.equals(getExecutingPhase())) {
      // Initialise has already been done for the objects in the base registry
      return getSpringRegistry().lookupEntriesForLifecycle(Object.class);
    } else {
      return getSpringRegistry().lookupEntriesForLifecycleIncludingAncestors(Object.class);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////
  // Spring custom lifecycle phases
  // ///////////////////////////////////////////////////////////////////////////////////


  class SpringContextInitialisePhase extends ZernovaContextInitialisePhase {

    public SpringContextInitialisePhase() {
      super();

      setOrderedLifecycleTypes(new Class<?>[] {
          LockFactory.class,
          ObjectStoreManager.class,
          ExpressionLanguage.class,
          QueueManager.class,
          StreamingManager.class,
          ConfigurationProvider.class,
          Config.class,
          SecurityManager.class,
          FlowConstruct.class,
          ZernovaConfiguration.class,
          Initialisable.class
      });

      setIgnoredObjectTypes(new Class[] {
          ExtensionManager.class,
          AbstractSpringRegistry.class,
          SpringRegistryBootstrap.class,
          FlowExceptionHandler.class,
          MessageProcessorChain.class,
          ZernovaContext.class,
          Service.class
      });
    }


    @Override
    public void applyLifecycle(Object o) throws LifecycleException {
      if (o instanceof Transformer) {
        String name = ((Transformer) o).getName();
        if (isNamedBean(name)) {
          super.applyLifecycle(o);
        }
      } else {
        super.applyLifecycle(o);
      }
    }

    @Override
    public LifecycleObjectSorter newLifecycleObjectSorter() {
      AutoDiscoveredDependencyResolver autoDiscoveredDependencyResolver =
          new AutoDiscoveredDependencyResolver(getSpringRegistry());
      DeclaredDependencyResolver declaredDependencyResolver = new DeclaredDependencyResolver(getSpringRegistry());
      ConfigurationDependencyResolver configurationDependencyResolver =
          getSpringRegistry().getConfigurationDependencyResolver();
      DependencyGraphBeanDependencyResolver dependencyGraphBeanDependencyResolver =
          new DependencyGraphBeanDependencyResolver(configurationDependencyResolver, declaredDependencyResolver,
                                                    autoDiscoveredDependencyResolver, getSpringRegistry());
      return new DependencyGraphLifecycleObjectSorter(dependencyGraphBeanDependencyResolver, orderedLifecycleTypes);
    }
  }

  /**
   * Detects if a bean is an inner bean to prevent applying lifecycle to it since lifecycle is already applied by the owner, i.e.:
   * a flow
   *
   * @param name bean name.
   * @return true if contains inner bean as prefix of the bean name, false otherwise.
   */
  private boolean isNamedBean(String name) {
    return name != null && !name.startsWith(INNER_BEAN_PREFIX);
  }

  /**
   * A lifecycle phase that will delegate to the {@link AbstractSpringRegistry#doDispose()} method which in turn will destroy the
   * application context managed by this registry
   */
  class SpringContextDisposePhase extends ZernovaContextDisposePhase {

    public SpringContextDisposePhase() {
      super();
      Class<?>[] ignoredObjects = new Class[] {
          ZernovaContext.class,
          ServerNotificationManager.class,
          Service.class,
          GlobalErrorHandler.class
      };
      setIgnoredObjectTypes(ignoredObjects);
    }

    @Override
    public void applyLifecycle(Object o) throws LifecycleException {
      if (o instanceof AbstractSpringRegistry) {
        ((AbstractSpringRegistry) o).doDispose();
      } else if (o instanceof Transformer) {
        String name = ((Transformer) o).getName();
        if (isNamedBean(name)) {
          super.applyLifecycle(o);
        }
      } else {
        super.applyLifecycle(o);
      }
    }

    @Override
    public LifecycleObjectSorter newLifecycleObjectSorter() {
      AutoDiscoveredDependencyResolver autoDiscoveredDependencyResolver =
          new AutoDiscoveredDependencyResolver(getSpringRegistry());
      DeclaredDependencyResolver declaredDependencyResolver = new DeclaredDependencyResolver(getSpringRegistry());
      ConfigurationDependencyResolver configurationDependencyResolver =
          getSpringRegistry().getConfigurationDependencyResolver();
      DependencyGraphBeanDependencyResolver dependencyGraphBeanDependencyResolver =
          new DependencyGraphBeanDependencyResolver(configurationDependencyResolver, declaredDependencyResolver,
                                                    autoDiscoveredDependencyResolver, getSpringRegistry());
      return new DependencyGraphLifecycleObjectSorter(dependencyGraphBeanDependencyResolver, orderedLifecycleTypes);
    }
  }

  private AbstractSpringRegistry getSpringRegistry() {
    return (AbstractSpringRegistry) getLifecycleObject();
  }
}
