/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.annotation.param.stereotype;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.zernova.runtime.api.meta.model.nested.NestedComponentModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.extension.api.runtime.route.Chain;
import com.zernova.runtime.extension.api.runtime.route.Route;
import com.zernova.runtime.extension.api.stereotype.StereotypeDefinition;
import com.zernova.sdk.api.annotation.MinZernovaVersion;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Declares an array of {@link Class classes} of an {@link StereotypeDefinition}, to communicate and declare which
 * {@link StereotypeDefinition} the {@link NestedComponentModel nested component} allows to be injected with.
 * <p>
 * This annotation can be applied to {@link ParameterModel parameters} of type {@link Chain}, or to {@link Route} implementations.
 *
 * @since 1.0
 * @see StereotypeDefinition
 * @see Chain
 * @see Route
 */
@MinZernovaVersion("4.1")
@Target({FIELD, PARAMETER, TYPE})
@Retention(RUNTIME)
@Documented
public @interface AllowedStereotypes {

  Class<? extends StereotypeDefinition>[] value();
}
