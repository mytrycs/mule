/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.api.deployment.meta;

import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import com.zernova.api.annotation.NoExtend;

/**
 * A builder to create instances of {@link AbstractZernovaArtifactModel} implementations.
 *
 * @param <T> type of the builder, used to enable builder pattern on subclasses methods
 * @param <M> type of the model being built, used to have type safety
 *
 * @since 1.0
 */
@NoExtend
public abstract class AbstractZernovaArtifactModelBuilder<T extends AbstractZernovaArtifactModelBuilder, M extends AbstractZernovaArtifactModel> {

  private String name;
  private String minZernovaVersion;
  private Product product;
  private ZernovaArtifactLoaderDescriptor bundleDescriptorLoader;
  private ZernovaArtifactLoaderDescriptor classLoaderModelDescriptorLoader;

  /**
   * Sets the describer's name
   *
   * @param name the name the describer will have
   * @return {@code this} builder
   */
  public T setName(String name) {
    this.name = name;

    return getThis();
  }

  /**
   * Sets the required product for the artifact.
   *
   * @param product the required product for the artifact.
   * @return {@code this} builder
   */
  public T setRequiredProduct(Product product) {
    this.product = product;
    return getThis();
  }

  public String getName() {
    return name;
  }

  public Product getRequiredProduct() {
    return product;
  }

  /**
   * @return the parametrized type of the builder
   */
  protected abstract T getThis();

  /**
   * Sets the describer's minimum Mule Runtime version that requires to work correctly
   *
   * @param muleVersion of the describer
   * @return {@code this} builder
   */
  public T setMinZernovaVersion(String muleVersion) {
    this.minZernovaVersion = muleVersion;
    return getThis();
  }

  public String getMinZernovaVersion() {
    return minZernovaVersion;
  }

  /**
   * Sets the bundle descriptor loader for the artifact
   *
   * @param bundleDescriptorLoader describes the loader for the bundle descriptor. Non null
   * @return a {@link ZernovaArtifactLoaderDescriptor} to populate the {@link ClassLoader} describer with the ID and any additional
   *         attributes
   */
  public T withBundleDescriptorLoader(ZernovaArtifactLoaderDescriptor bundleDescriptorLoader) {
    checkArgument(bundleDescriptorLoader != null, "bundleDescriptorLoader cannot be null");
    this.bundleDescriptorLoader = bundleDescriptorLoader;

    return getThis();
  }

  /**
   * Sets the bundle descriptor loader for the artifact
   *
   * @param classLoaderModelDescriptorLoader describes the loader for the classloader model descriptor. Non null
   * @return a {@link ZernovaArtifactLoaderDescriptor} to populate the {@link ClassLoader} describer with the ID and any additional
   *         attributes
   */
  public T withClassLoaderModelDescriptorLoader(ZernovaArtifactLoaderDescriptor classLoaderModelDescriptorLoader) {
    checkArgument(classLoaderModelDescriptorLoader != null, "classLoaderModelDescriptorLoader cannot be null");
    this.classLoaderModelDescriptorLoader = classLoaderModelDescriptorLoader;

    return getThis();
  }

  public ZernovaArtifactLoaderDescriptor getBundleDescriptorLoader() {
    return bundleDescriptorLoader;
  }


  public ZernovaArtifactLoaderDescriptor getClassLoaderModelDescriptorLoader() {
    return classLoaderModelDescriptorLoader;
  }

  /**
   * @return a well formed model of {@link M} type
   */
  public abstract M build();
}
