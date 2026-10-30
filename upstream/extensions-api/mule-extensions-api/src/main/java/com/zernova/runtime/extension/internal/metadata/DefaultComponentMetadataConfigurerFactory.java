/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.metadata;

import static java.util.ServiceLoader.load;

import static com.zernova.runtime.api.util.classloader.ZernovaImplementationLoaderUtils.getMuleImplementationsLoader;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.api.util.classloader.ZernovaImplementationLoaderUtils.isResolveMuleImplementationLoadersDynamically;

import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.extension.api.metadata.ComponentMetadataConfigurer;
import com.zernova.runtime.extension.api.metadata.ComponentMetadataConfigurerFactory;
import com.zernova.runtime.extension.api.metadata.ComponentMetadataConfigurerFactoryDelegate;

import java.util.Iterator;

public class DefaultComponentMetadataConfigurerFactory extends ComponentMetadataConfigurerFactory {

  private static final LazyValue<ComponentMetadataConfigurerFactoryDelegate> DELEGATE =
      new LazyValue(DefaultComponentMetadataConfigurerFactory::loadFactoryDelegate);

  private static ComponentMetadataConfigurerFactoryDelegate loadFactoryDelegate() {
    Iterator<ComponentMetadataConfigurerFactoryDelegate> iter =
        load(ComponentMetadataConfigurerFactoryDelegate.class,
             getMuleImplementationsLoader()).iterator();
    if (!iter.hasNext()) {
      throw new ZernovaRuntimeException(createStaticMessage("There is no implementation available for %s.",
                                                         ComponentMetadataConfigurerFactoryDelegate.class.getName()));
    }
    return iter.next();
  }

  @Override
  public ComponentMetadataConfigurer create() {
    ComponentMetadataConfigurerFactoryDelegate delegate;
    if (isResolveMuleImplementationLoadersDynamically()) {
      delegate = loadFactoryDelegate();
    } else {
      delegate = DELEGATE.get();
    }

    return delegate.create();
  }
}
