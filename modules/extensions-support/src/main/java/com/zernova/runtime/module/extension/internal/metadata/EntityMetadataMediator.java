/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.metadata;

import static com.zernova.runtime.api.metadata.resolving.FailureCode.INVALID_METADATA_KEY;
import static com.zernova.runtime.api.metadata.resolving.MetadataFailure.Builder.newFailure;
import static com.zernova.runtime.api.metadata.resolving.MetadataResult.failure;
import static com.zernova.runtime.api.metadata.resolving.MetadataResult.success;
import static com.zernova.runtime.module.extension.internal.metadata.MetadataResolverUtils.resolveWithOAuthRefresh;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.getMetadataResolverFactory;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.metadata.MetadataContext;
import com.zernova.runtime.api.metadata.MetadataKey;
import com.zernova.runtime.api.metadata.MetadataKeysContainer;
import com.zernova.runtime.api.metadata.MetadataKeysContainerBuilder;
import com.zernova.runtime.api.metadata.descriptor.ComponentMetadataDescriptor;
import com.zernova.runtime.api.metadata.descriptor.OutputMetadataDescriptor;
import com.zernova.runtime.api.metadata.descriptor.TypeMetadataDescriptor;
import com.zernova.runtime.api.metadata.resolving.InputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.MetadataResult;
import com.zernova.runtime.api.metadata.resolving.OutputTypeResolver;
import com.zernova.runtime.api.metadata.resolving.QueryEntityResolver;
import com.zernova.runtime.api.metadata.resolving.TypeKeysResolver;
import com.zernova.runtime.extension.api.metadata.MetadataResolverFactory;

import java.util.Set;

/**
 * Resolves a Component's Metadata by coordinating the several moving parts that are affected by the Metadata fetching process, so
 * that such pieces can remain decoupled.
 * <p/>
 * This mediator will coordinate the resolvers: {@link MetadataResolverFactory}, {@link TypeKeysResolver},
 * {@link InputTypeResolver} and {@link OutputTypeResolver}, and the descriptors that represent their results:
 * {@link ComponentMetadataDescriptor}, {@link OutputMetadataDescriptor} and {@link TypeMetadataDescriptor}
 *
 * @since 4.0
 */
public class EntityMetadataMediator {

  private final MetadataResolverFactory resolverFactory;

  public EntityMetadataMediator(OperationModel operationModel) {
    this.resolverFactory = getMetadataResolverFactory(operationModel);
  }

  public MetadataResult<MetadataKeysContainer> getEntityKeys(MetadataContext context) {
    try {
      QueryEntityResolver queryEntityResolver = resolverFactory.getQueryEntityResolver();
      Set<MetadataKey> entityKeys = resolveWithOAuthRefresh(context, () -> queryEntityResolver.getEntityKeys(context));
      final MetadataKeysContainerBuilder keyBuilder = MetadataKeysContainerBuilder.getInstance();
      if (entityKeys.stream().anyMatch(key -> key.getChilds().size() > 0)) {
        return failure(newFailure()
            .withMessage("Error retrieving entity keys, Only single level keys are supported for entity metadata")
            .withReason("There are at least one key that contains childs")
            .withFailureCode(INVALID_METADATA_KEY).onKeys());
      }
      return success(keyBuilder.add(queryEntityResolver.getResolverName(), entityKeys).build());
    } catch (Exception e) {
      return failure(newFailure(e).onKeys());
    }
  }

  public MetadataResult<TypeMetadataDescriptor> getEntityMetadata(MetadataContext context, MetadataKey entityKey) {
    try {
      MetadataType entityMetadata =
          resolveWithOAuthRefresh(context, () -> resolverFactory.getQueryEntityResolver().getEntityMetadata(context,
                                                                                                            entityKey.getId()));
      return success(TypeMetadataDescriptor.builder().withType(entityMetadata).build());
    } catch (Exception e) {
      return failure(newFailure(e).onEntity());
    }
  }

}
