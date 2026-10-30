/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.api.artifact;


import com.zernova.api.annotation.NoImplement;
import com.zernova.runtime.api.connection.ConnectionValidationResult;
import com.zernova.runtime.api.metadata.MetadataKeysContainer;
import com.zernova.runtime.api.metadata.descriptor.ComponentMetadataTypesDescriptor;
import com.zernova.runtime.api.metadata.resolving.MetadataResult;
import com.zernova.runtime.api.sampledata.SampleDataResult;
import com.zernova.runtime.api.value.ValueResult;
import com.zernova.runtime.app.declaration.api.ComponentElementDeclaration;
import com.zernova.runtime.app.declaration.api.ParameterizedElementDeclaration;
import com.zernova.runtime.module.repository.api.BundleNotFoundException;

/**
 * It is in charge of resolving connector's operations and retrieving metadata and sample data for all components related to the
 * same session configuration. The session configuration should be defined by multiple global elements, including Configurations,
 * Connections, etc.
 * <p/>
 * This session provides the possibility to avoid having a full artifact configuration before being able to gather metadata from
 * the connector.
 * <p/>
 *
 * @since 4.4.0
 */
@NoImplement
public interface DeclarationSession {

  /**
   * Test connectivity for the connection associated to the configuration with the provided name.
   *
   * @param configName The name of the config for which to test connection.
   * @return a {@link ConnectionValidationResult} with the result of the connectivity testing
   * @throws BundleNotFoundException if any of the dependencies defined for the session could not be resolved
   */
  ConnectionValidationResult testConnection(String configName);

  /**
   * Retrieve all {@link com.zernova.runtime.api.value.Value} that can be configured for the given parameter.
   *
   * @param parameterizedElementDeclaration a {@link ParameterizedElementDeclaration} for the component from which the available
   *                                        values can be used on the parameter {@param providerName}. In case the value provider
   *                                        requires any acting parameters to be able to resolve this values, those parameters
   *                                        should be populated in this declaration. Also, if the Component requires values from a
   *                                        Configuration, then its reference name should be specified in the declaration.
   * @param providerName                    the name of the value provider for which to resolve the
   *                                        {@link com.zernova.runtime.api.value.Value}s
   * @return a {@link ValueResult} with the accepted parameter values to use
   * @throws BundleNotFoundException if any of the dependencies defined for the session could not be resolved
   */
  ValueResult getValues(ParameterizedElementDeclaration parameterizedElementDeclaration, String providerName);

  /**
   * Retrieve all {@link com.zernova.runtime.api.value.Value} that can be configured for the field in the given parameter.
   *
   * @param parameterizedElementDeclaration a {@link ParameterizedElementDeclaration} for the component from which the available
   *                                        values can be used on the parameter {@param providerName}. In case the value provider
   *                                        requires any acting parameters to be able to resolve this values, those parameters
   *                                        should be populated in this declaration. Also, if the Component requires values from a
   *                                        Configuration, then its reference name should be specified in the declaration.
   * @param providerName                    the name of the value provider for which to resolve the
   *                                        {@link com.zernova.runtime.api.value.Value}s
   * @param targetSelector                  The path to locate the field within the given parameter
   * @return a {@link ValueResult} with the accepted parameter values to use
   * @throws BundleNotFoundException if any of the dependencies defined for the session could not be resolved
   */
  ValueResult getFieldValues(ParameterizedElementDeclaration parameterizedElementDeclaration, String providerName,
                             String targetSelector);

  /**
   * Returns the list of keys that can be resolved associated to the specified component.
   *
   * @param component the location of the {@link com.zernova.runtime.api.metadata.MetadataKeyProvider} component to query for its
   *                  available keys
   * @return Successful {@link MetadataResult} if the keys are successfully resolved Failure {@link MetadataResult} if there is an
   *         error while resolving the keys
   * @throws BundleNotFoundException if any of the dependencies defined for the session could not be resolved
   */
  MetadataResult<MetadataKeysContainer> getMetadataKeys(ComponentElementDeclaration component);

  /**
   * Retrieve all the dynamic metadata for the given component. It includes input parameters, output (payload) and output
   * attributes. If a metadata key is needed all parts of the key must be provided.
   *
   * @param component the component whose dynamic metadata types are required
   * @return a {@link MetadataResult} of {@link ComponentMetadataTypesDescriptor} containing all the dynamic types
   * @throws BundleNotFoundException if any of the dependencies defined for the session could not be resolved
   */
  MetadataResult<ComponentMetadataTypesDescriptor> resolveComponentMetadata(ComponentElementDeclaration component);

  /**
   * Disposes the {@link com.zernova.runtime.api.metadata.MetadataCache} associated to the component metadata resolution which is
   * provided in order to store resources cross resolution by resolvers.
   *
   * @param component the component whose has a dynamic metadata types.
   */
  void disposeMetadataCache(ComponentElementDeclaration component);

  /**
   * Retrieves any sample data available for the component.
   *
   * @param component the component whose sample data is required
   * @return a {@link SampleDataResult} with the sample data message
   * @throws BundleNotFoundException if any of the dependencies defined for the session could not be resolved
   */
  SampleDataResult getSampleData(ComponentElementDeclaration component);

  /**
   * Stops and disposes all resources used by this {@link DeclarationSession}.
   */
  void dispose();

}
