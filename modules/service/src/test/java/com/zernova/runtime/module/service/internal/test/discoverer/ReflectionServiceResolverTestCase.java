/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.service.internal.test.discoverer;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.service.Service;
import com.zernova.runtime.api.service.ServiceProvider;
import com.zernova.runtime.module.service.api.discoverer.ServiceAssembly;
import com.zernova.runtime.module.service.api.discoverer.ServiceResolutionError;
import com.zernova.runtime.module.service.internal.discoverer.ReflectionServiceResolver;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import org.junit.Test;

import io.qameta.allure.Description;

public class ReflectionServiceResolverTestCase extends AbstractZernovaTestCase {

  @Test
  @Description("SOAP service apis are not loader for Java 21+. Assert that the presence of the service does not break the startup of Mule when running with Java 21+")
  public void resolveServiceNoContractClass() throws ServiceResolutionError {
    final var resolver = new ReflectionServiceResolver(null, null, (s, a) -> s);

    final var serviceAssembly = ServiceAssembly.lazyBuilder()
        .withName("Invalid Service")
        .withClassLoader(() -> this.getClass().getClassLoader())
        .withServiceProvider(() -> mock(ServiceProvider.class))
        .forContract("com.zernova.runtime.NotExists")
        .build();

    final var resolvedServices = resolver.resolveServices(asList(serviceAssembly));
    assertThat(resolvedServices, emptyIterable());
  }

  @Test
  public void resolveServiceException() throws ServiceResolutionError {
    final var expectedException = new ZernovaRuntimeException(createStaticMessage("Expected"));
    final var resolver = new ReflectionServiceResolver(null, null, (s, a) -> {
      throw expectedException;
    });

    final var serviceAssembly = ServiceAssembly.lazyBuilder()
        .withName("some Service")
        .withClassLoader(() -> this.getClass().getClassLoader())
        .withServiceProvider(() -> mock(ServiceProvider.class))
        .forContract(SomeService.class.getName())
        .build();

    var thrown = assertThrows(RuntimeException.class, () -> resolver.resolveServices(asList(serviceAssembly)));
    assertThat(thrown, sameInstance(expectedException));
  }

  public static interface SomeService extends Service {

  }
}
