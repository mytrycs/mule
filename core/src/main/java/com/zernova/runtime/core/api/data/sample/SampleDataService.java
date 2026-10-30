/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.api.data.sample;

import com.zernova.api.annotation.Experimental;
import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationInstance;
import com.zernova.sdk.api.data.sample.SampleDataException;
import com.zernova.sdk.api.data.sample.SampleDataProvider;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Provides the capability of obtaining sample data for any component in a Mule app which supports providing sample data.
 *
 * <b>NOTE:</b> Experimental feature. Backwards compatibility not guaranteed.
 *
 * @since 4.4.0
 */
@NoImplement
@Experimental
public interface SampleDataService {

  /**
   * Key under which the {@link SampleDataService} can be found in the {@link com.zernova.runtime.api.artifact.Registry}
   */
  String SAMPLE_DATA_SERVICE_KEY = "_muleSampleDataService";

  /**
   * Returns a sample output {@link Message} for the component at the given {@link Location}.
   *
   * @param location The {@link Location} of the target component
   * @return a sample output {@link Message}
   * @throws SampleDataException if the resolution fails
   */
  Message getSampleData(Location location) throws SampleDataException;

  /**
   * Returns a sample output {@link Message} for a specific component referenced by its {@code extensionName} and
   * {@code componentName}.
   *
   * @param extensionName                 the name of the extension in which the component is defined
   * @param componentName                 the name of the component that supplies the sample
   * @param parameters                    the parameters supplied to the underlying {@link SampleDataProvider}
   * @param configurationInstanceSupplier supplies the config for underlying {@link SampleDataProvider} which requires
   *                                      connectivity or config
   * @return @return a sample output {@link Message}
   * @throws SampleDataException if the resolution fails
   */
  Message getSampleData(String extensionName,
                        String componentName,
                        Map<String, Object> parameters,
                        Supplier<Optional<ConfigurationInstance>> configurationInstanceSupplier)
      throws SampleDataException;
}
