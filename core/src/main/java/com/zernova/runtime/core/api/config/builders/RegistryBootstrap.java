/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.config.builders;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.lifecycle.Initialisable;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.config.bootstrap.ArtifactType;
import com.zernova.runtime.core.api.config.bootstrap.BootstrapServiceDiscoverer;
import com.zernova.runtime.core.api.context.ZernovaContextAware;
import com.zernova.runtime.core.internal.config.bootstrap.SimpleRegistryBootstrap;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.privileged.registry.RegistrationException;

import java.util.function.BiConsumer;

/**
 * Loads objects defined in a file called <code>registry-bootstrap.properties</code> into the local registry. This allows modules
 * and transports to make certain objects available by default. The most common use case is for a module or transport to load
 * stateless transformers into the registry. For this file to be located it must be present in the modules META-INF directory
 * under
 *
 * <pre>
 * META-INF/com/zernova/config/
 * </pre>
 * <p/>
 * The format of this file is a simple key / value pair. i.e.
 *
 * <pre>
 * myobject = org.foo.MyObject
 * </pre>
 * <p>
 * Will register an instance of MyObject with a key of 'myobject'. If you don't care about the object name and want to ensure that
 * the ojbect gets a unique name you can use -
 *
 * <pre>
 * object.1=org.foo.MyObject
 * object.2=org.bar.MyObject
 * </pre>
 * <p>
 * or
 *
 * <pre>
 * myFoo=org.foo.MyObject
 * myBar=org.bar.MyObject
 * </pre>
 * <p>
 * It's also possible to define if the entry must be applied to a domain, an application, or both by using the parameter
 * applyToArtifactType.
 *
 * <pre>
 * myFoo=org.foo.MyObject will be applied to any mule application since the parameter applyToArtifactType default value is app
 * myFoo=org.foo.MyObject;applyToArtifactType=app will be applied to any mule application
 * myFoo=org.foo.MyObject;applyToArtifactType=domain will be applied to any mule domain
 * myFoo=org.foo.MyObject;applyToArtifactType=app/domain will be applied to any mule application and any mule domain
 * </pre>
 * <p>
 * Loading transformers has a slightly different notation since you can define the 'returnClass' with optional mime type, and
 * 'name' of the transformer as parameters i.e.
 *
 * <pre>
 * transformer.1=com.zernova.compatibility.core.transport.jms.transformers.JMSMessageToObject,returnClass=byte[]
 * transformer.2=com.zernova.compatibility.core.transport.jms.transformers.JMSMessageToObject,returnClass=java.lang.String:text/xml, name=JMSMessageToString
 * transformer.3=com.zernova.compatibility.core.transport.jms.transformers.JMSMessageToObject,returnClass=java.util.Hashtable)
 * </pre>
 * <p>
 * Note that the key used for transformers must be 'transformer.x' where 'x' is a sequential number. The transformer name will be
 * automatically generated as JMSMessageToXXX where XXX is the return class name i.e. JMSMessageToString unless a 'name' parameter
 * is specified. If no 'returnClass' is specified the default in the transformer will be used.
 * <p/>
 * Note that all objects defined have to have a default constructor. They can implement injection interfaces such as
 * {@link ZernovaContextAware} and lifecycle interfaces such as {@link Initialisable}.
 *
 * @since 4.5.0
 */
public interface RegistryBootstrap extends Initialisable {

  /**
   * Returns a default instance for the given parameters
   *
   * @param supportedArtifactType the type of artifact being configured
   * @param muleContext           the configured {@link ZernovaContext}
   * @return a {@link RegistryBootstrap} instance
   *
   * @deprecated Use {@link #defaultRegistryBoostrap(ArtifactType, BootstrapServiceDiscoverer, Registrer)} instead.
   */
  @Deprecated
  static RegistryBootstrap defaultRegistryBoostrap(com.zernova.runtime.core.api.config.bootstrap.ArtifactType supportedArtifactType,
                                                   ZernovaContext muleContext) {
    return defaultRegistryBoostrap(ArtifactType.valueOf(supportedArtifactType.name()),
                                   muleContext.getRegistryBootstrapServiceDiscoverer(),
                                   ((ZernovaContextWithRegistry) muleContext).getRegistry()::registerObject);
  }

  /**
   * Returns a default instance for the given parameters
   *
   * @param supportedArtifactType the type of artifact being configured
   * @param muleContext           the configured {@link ZernovaContext}
   * @param registrer             adds an object to the registry, with any applicable rules (i.e.: service overrides)
   * @return a {@link RegistryBootstrap} instance
   * @since 4.8
   */
  static RegistryBootstrap defaultRegistryBoostrap(ArtifactType supportedArtifactType,
                                                   BootstrapServiceDiscoverer bootstrapServiceDiscoverer,
                                                   Registrer registrer) {
    return new SimpleRegistryBootstrap(supportedArtifactType, bootstrapServiceDiscoverer, registrer);
  }

  /**
   * Performs the bootstrap operations
   *
   * @throws InitialisationException
   */
  @Override
  void initialise() throws InitialisationException;

  @FunctionalInterface
  public static interface Registrer extends BiConsumer<String, Object> {

    @Override
    default void accept(String serviceId, Object defaultServiceImpl) {
      try {
        register(serviceId, defaultServiceImpl);
      } catch (ZernovaException e) {
        throw new ZernovaRuntimeException(e);
      }
    }

    /**
     * Registers an object in the registry with a key.
     *
     * @param key   the key to store the value against. This is a non-null value
     * @param value the object to store in the registry. This is a non-null value
     * @throws RegistrationException if an object with the same key already exists
     */
    void register(String serviceId, Object defaultServiceImpl) throws ZernovaException;
  }
}
