/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.registry;

import static com.zernova.test.allure.AllureConstants.RegistryFeature.REGISTRY;
import static com.zernova.test.allure.AllureConstants.RegistryFeature.TransfromersStory.TRANSFORMERS;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.transformer.Converter;
import com.zernova.runtime.core.api.transformer.Transformer;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.transformer.DefaultTransformersRegistry;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;

import org.mockito.ArgumentCaptor;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(REGISTRY)
@Story(TRANSFORMERS)
public class ConvertersNotRegisteredTwiceTestCase extends AbstractZernovaContextTestCase {

  private final DefaultTransformersRegistry transformersRegistry = spy(new DefaultTransformersRegistry());

  @Test
  public void noDuplicates() throws InitialisationException {
    transformersRegistry
        .setTransformers(new ArrayList<>(((ZernovaContextWithRegistry) muleContext).getRegistry().lookupObjects(Transformer.class)));
    transformersRegistry.initialise();

    ArgumentCaptor<Converter> converterArgumentCaptor = forClass(Converter.class);
    verify(transformersRegistry, atLeastOnce()).notifyTransformerResolvers(converterArgumentCaptor.capture());
    assertNoDuplicatesNorEmpty(converterArgumentCaptor.getAllValues());
  }

  private <T> void assertNoDuplicatesNorEmpty(Collection<T> collection) {
    assertThat(collection, is(not(empty())));

    assertThat(new HashSet<>(collection), hasSize(collection.size()));
  }
}
