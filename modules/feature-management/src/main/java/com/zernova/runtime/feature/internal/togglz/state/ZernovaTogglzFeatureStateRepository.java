/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.feature.internal.togglz.state;

import static com.zernova.runtime.feature.internal.togglz.scope.type.ZernovaTogglzFeatureScopeType.ARTIFACT_SCOPE_TYPE;
import static com.zernova.runtime.feature.internal.togglz.user.ZernovaFeatureUser.FEATURE_SCOPE_ATTRIBUTE_KEY;
import static java.util.Collections.singletonMap;
import static org.togglz.core.context.FeatureContext.getFeatureManager;

import com.zernova.runtime.feature.internal.togglz.provider.ZernovaTogglzFeatureProvider;
import com.zernova.runtime.feature.internal.togglz.scope.ZernovaTogglzFeatureScope;
import com.zernova.runtime.feature.internal.togglz.scope.type.ZernovaTogglzFeatureScopeType;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.FeatureUser;

import java.util.Map;

/**
 * A Togglz {@link StateRepository} for the Mule runtime
 *
 * @since 4.5.0
 */
public class ZernovaTogglzFeatureStateRepository implements StateRepository {

  private final Map<ZernovaTogglzFeatureScopeType, ZernovaTogglzFeatureStateResolver> featureResolvers =
      singletonMap(ARTIFACT_SCOPE_TYPE, new ZernovaTogglzApplicationFeatureStateResolver(ZernovaTogglzFeatureStateRepository.this));

  public static final String INVALID_FEATURE_SCOPE_ATTRIBUTE = "Invalid feature scope attribute";
  public static final String FEATURE_IS_NOT_REGISTERED = "Feature '%s' is not registered";

  private final ZernovaTogglzFeatureStateResolver defaultFeatureStateResolver =
      new ZernovaTogglzRuntimeFeatureStateResolver(ZernovaTogglzFeatureStateRepository.this);
  private final ZernovaTogglzFeatureProvider featureProvider;

  public ZernovaTogglzFeatureStateRepository(ZernovaTogglzFeatureProvider featureProvider) {
    this.featureProvider = featureProvider;
  }

  @Override
  public FeatureState getFeatureState(Feature feature) {
    if (featureProvider.getFeature(feature.name()) == null) {
      throw new IllegalArgumentException(String.format(FEATURE_IS_NOT_REGISTERED, feature.name()));
    }

    ZernovaTogglzFeatureScope scope = getScope();

    return getFeatureStateResolver(scope).getFeatureState(feature, scope);
  }

  @Override
  public void setFeatureState(FeatureState featureState) {
    ZernovaTogglzFeatureScope scope = getScope();
    getFeatureStateResolver(scope).setFeatureState(scope, featureState);
  }

  private ZernovaTogglzFeatureScope getScope() {
    FeatureUser featureUser = getFeatureManager().getCurrentFeatureUser();
    if (featureUser == null) {
      return null;
    }

    Object scope = featureUser.getAttribute(FEATURE_SCOPE_ATTRIBUTE_KEY);

    if (scope != null && !(scope instanceof ZernovaTogglzFeatureScope)) {
      throw new IllegalStateException(INVALID_FEATURE_SCOPE_ATTRIBUTE);
    }

    return (ZernovaTogglzFeatureScope) scope;
  }

  public void removeFeatureState(FeatureState muleFeatureState) {
    if (!(muleFeatureState instanceof ZernovaTogglzFeatureState)) {
      throw new IllegalArgumentException("The Mule Feature State Repository can only remove Mule Feature States");
    }

    ZernovaTogglzFeatureState muleTogglzFeatureState = (ZernovaTogglzFeatureState) muleFeatureState;
    getFeatureStateResolver(muleTogglzFeatureState.getScope()).removeFeatureFeature(muleTogglzFeatureState);
  }

  private ZernovaTogglzFeatureStateResolver getFeatureStateResolver(ZernovaTogglzFeatureScope scope) {
    if (scope == null) {
      return defaultFeatureStateResolver;
    } else {
      return featureResolvers.getOrDefault(scope.getScopeType(), defaultFeatureStateResolver);
    }
  }
}
