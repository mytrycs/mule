/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.extension.internal.loader.parser.java.notification;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.module.extension.internal.loader.parser.java.ZernovaExtensionAnnotationParser.mapReduceSingleAnnotation;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.runtime.api.exception.ZernovaRuntimeException;
import com.zernova.runtime.api.meta.model.declaration.fluent.ExecutableComponentDeclarer;
import com.zernova.runtime.api.meta.model.notification.NotificationModel;
import com.zernova.runtime.extension.api.annotation.notification.Fires;
import com.zernova.runtime.extension.api.model.notification.ImmutableNotificationModel;
import com.zernova.runtime.module.extension.api.loader.java.type.AnnotationValueFetcher;
import com.zernova.runtime.module.extension.api.loader.java.type.Type;
import com.zernova.runtime.module.extension.api.loader.java.type.WithAnnotations;
import com.zernova.runtime.extension.api.loader.parser.NotificationEmitterParser;
import com.zernova.sdk.api.annotation.notification.NotificationActionProvider;
import com.zernova.sdk.api.annotation.notification.NotificationActions;
import com.zernova.sdk.api.notification.NotificationActionDefinition;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utilities for parsing Java defined {@link NotificationModel notification models}
 *
 * @since 4.5.0
 */
public final class NotificationModelParserUtils {

  public static List<NotificationModel> parseNotifications(AnnotationValueFetcher<NotificationActions> value,
                                                           String extensionNamespace,
                                                           ClassTypeLoader typeLoader) {
    Class<Enum> enumType = (Class<Enum>) value.getClassValue(NotificationActions::value).getDeclaringClass().orElse(null);
    if (enumType == null) {
      return new LinkedList<>();
    }

    return Stream.of((NotificationActionDefinition<?>[]) enumType.getEnumConstants())
        .map(action -> new ImmutableNotificationModel(extensionNamespace, ((Enum) action).name(),
                                                      typeLoader.load(action.getDataType().getType())))
        .collect(toList());
  }

  public static List<NotificationModel> parseLegacyNotifications(AnnotationValueFetcher<com.zernova.runtime.extension.api.annotation.notification.NotificationActions> value,
                                                                 String extensionNamespace,
                                                                 ClassTypeLoader typeLoader) {
    Class<Enum> enumType =
        (Class<Enum>) value.getClassValue(com.zernova.runtime.extension.api.annotation.notification.NotificationActions::value)
            .getDeclaringClass().orElse(null);
    if (enumType == null) {
      return new LinkedList<>();
    }

    return Stream.of(
                     (com.zernova.runtime.extension.api.notification.NotificationActionDefinition<?>[]) enumType.getEnumConstants())
        .map(action -> new ImmutableNotificationModel(extensionNamespace, ((Enum) action).name(),
                                                      typeLoader.load(action.getDataType().getType())))
        .collect(toList());
  }

  public static List<String> getEmittedNotifications(WithAnnotations element,
                                                     String componentType,
                                                     String componentName) {
    return mapReduceSingleAnnotation(
                                     element,
                                     componentType,
                                     componentName,
                                     Fires.class,
                                     com.zernova.sdk.api.annotation.notification.Fires.class,
                                     value -> getEmittedNotifications(value.getClassArrayValue(Fires::value)),
                                     value -> getEmittedNotifications(value
                                         .getClassArrayValue(com.zernova.sdk.api.annotation.notification.Fires::value)))
                                             .orElse(emptyList());
  }

  public static void declareEmittedNotifications(NotificationEmitterParser parser,
                                                 ExecutableComponentDeclarer declarer,
                                                 Function<String, Optional<NotificationModel>> notificationMapper) {
    parser.getEmittedNotificationsStream(notificationMapper).forEach(declarer::withNotificationModel);
  }

  private static List<String> getEmittedNotifications(List<Type> types) {
    return types.stream()
        .filter(type -> type.getDeclaringClass().isPresent())
        .flatMap(type -> {
          Class<?> providerClass = type.getDeclaringClass().get();
          try {
            NotificationActionProvider provider = SdkNotificationActionProviderAdapter.from(providerClass.newInstance());
            return provider.getNotificationActions().stream().map(action -> ((Enum) action).name());
          } catch (InstantiationException | IllegalAccessException e) {
            throw new ZernovaRuntimeException(createStaticMessage("Could not create NotificationActionProvider of type "
                + providerClass.getName()), e);
          }
        }).collect(toList());
  }

  private NotificationModelParserUtils() {}
}
