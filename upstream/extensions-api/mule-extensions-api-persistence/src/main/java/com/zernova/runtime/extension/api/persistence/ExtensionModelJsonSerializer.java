/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.persistence;

import static java.util.Collections.emptySet;
import static com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils.getId;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.metadata.persistence.MetadataTypeGsonTypeAdapter;
import com.zernova.metadata.persistence.ObjectTypeReferenceHandler;
import com.zernova.metadata.persistence.SerializationContext;
import com.zernova.metadata.persistence.type.adapter.OptionalTypeAdapterFactory;
import com.zernova.runtime.api.artifact.ArtifactCoordinates;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.EnrichableModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.ImportedTypeModel;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.OutputModel;
import com.zernova.runtime.api.meta.model.ParameterDslConfiguration;
import com.zernova.runtime.api.meta.model.SubTypesModel;
import com.zernova.runtime.api.meta.model.XmlDslModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.connection.ConnectionProviderModel;
import com.zernova.runtime.api.meta.model.deprecated.DeprecationModel;
import com.zernova.runtime.api.meta.model.display.LayoutModel;
import com.zernova.runtime.api.meta.model.error.ErrorModel;
import com.zernova.runtime.api.meta.model.notification.NotificationModel;
import com.zernova.runtime.api.meta.model.parameter.ActingParameterModel;
import com.zernova.runtime.api.meta.model.parameter.ExclusiveParametersModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.source.SourceCallbackModel;
import com.zernova.runtime.api.meta.model.stereotype.ImmutableStereotypeModel;
import com.zernova.runtime.api.meta.model.stereotype.StereotypeModel;
import com.zernova.runtime.extension.api.model.ImmutableExtensionModel;
import com.zernova.runtime.extension.api.model.ImmutableOutputModel;
import com.zernova.runtime.extension.api.model.config.ImmutableConfigurationModel;
import com.zernova.runtime.extension.api.model.connection.ImmutableConnectionProviderModel;
import com.zernova.runtime.extension.api.model.deprecated.ImmutableDeprecationModel;
import com.zernova.runtime.extension.api.model.parameter.ImmutableActingParameterModel;
import com.zernova.runtime.extension.api.model.parameter.ImmutableExclusiveParametersModel;
import com.zernova.runtime.extension.api.model.parameter.ImmutableParameterGroupModel;
import com.zernova.runtime.extension.api.model.parameter.ImmutableParameterModel;
import com.zernova.runtime.extension.api.model.source.ImmutableSourceCallbackModel;
import com.zernova.runtime.extension.api.util.ExtensionMetadataTypeUtils;
import com.zernova.runtime.extension.internal.persistence.ArtifactCoordinatesTypeAdapter;
import com.zernova.runtime.extension.internal.persistence.ConstructModelTypeAdapterFactory;
import com.zernova.runtime.extension.internal.persistence.DefaultImplementationTypeAdapterFactory;
import com.zernova.runtime.extension.internal.persistence.ElementDslModelTypeAdapter;
import com.zernova.runtime.extension.internal.persistence.ErrorModelToIdentifierTypeAdapter;
import com.zernova.runtime.extension.internal.persistence.ExtensionModelTypeAdapter;
import com.zernova.runtime.extension.internal.persistence.FunctionModelTypeAdapterFactory;
import com.zernova.runtime.extension.internal.persistence.ImportedTypesModelTypeAdapter;
import com.zernova.runtime.extension.internal.persistence.MetadataKeyPartModelPropertyTypeAdapterFactory;
import com.zernova.runtime.extension.internal.persistence.ModelPropertyMapTypeAdapterFactory;
import com.zernova.runtime.extension.internal.persistence.ZernovaVersionTypeAdapter;
import com.zernova.runtime.extension.internal.persistence.NestableElementModelTypeAdapterFactory;
import com.zernova.runtime.extension.internal.persistence.NotificationModelToIdentifierTypeAdapter;
import com.zernova.runtime.extension.internal.persistence.OAuthGrantTypeTypeAdapterFactory;
import com.zernova.runtime.extension.internal.persistence.OperationModelTypeAdapterFactory;
import com.zernova.runtime.extension.internal.persistence.RestrictedTypesObjectTypeReferenceHandler;
import com.zernova.runtime.extension.internal.persistence.SourceModelTypeAdapterFactory;
import com.zernova.runtime.extension.internal.persistence.SubTypesModelTypeAdapter;
import com.zernova.runtime.extension.internal.persistence.XmlDslModelTypeAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * Serializer that can convert a {@link ExtensionModel} into a readable and processable JSON representation and from a JSON
 * {@link String} to an {@link ExtensionModel} instance
 * <p>
 * <b>Considerations:</b>
 * <ul>
 * <li>Only {@link ModelProperty}s that are considered as <b>externalizable</b>, the ones that {@link ModelProperty#isPublic()}
 * returns {@code true}, will be serialized</li>
 * <li>Due to the nature of {@link ModelProperty}, that can be dynamically attached to any {@link EnrichableModel}, only the
 * already know set of {@link ModelProperty} will be tagged with a friendly name, example: {@link LayoutModel} is going to be
 * identified with the {@code display} name. Otherwise, the {@link ModelProperty} will be serialized tagging it with the full
 * qualifier name of the class.</li>
 * <li>When deserializing {@link ModelProperty}s, their full qualified name will be used, if the class is not found in the
 * ClassLoader the {@link ModelProperty} object will be discarded</li>
 * </ul>
 *
 * @since 1.0
 */
public class ExtensionModelJsonSerializer {

  private final boolean prettyPrint;
  private Set<ObjectType> registeredTypes = emptySet();
  private Set<ObjectType> importedTypes = emptySet();

  /**
   * Creates a new instance of the {@link ExtensionModelJsonSerializer}. This serializer is capable of serializing and
   * deserializing {@link ExtensionModel} from JSON ({@link #deserialize(String)} and to JSON ( {@link #serialize(ExtensionModel)}
   */
  public ExtensionModelJsonSerializer() {
    this(false);
  }

  /**
   * Creates a new instance of the {@link ExtensionModelJsonSerializer}.
   *
   * @param prettyPrint boolean indicating if the serialization of the {@link ExtensionModel} should be printed in a human
   *                    readable or into compact and more performable format
   */
  public ExtensionModelJsonSerializer(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  private Gson buildGson() {
    final SerializationContext serializationContext = new SerializationContext();

    Map<String, ErrorModel> errorModelRepository = new HashMap<>();
    Map<String, NotificationModel> notificationModelRepository = new HashMap<>();
    Gson gsonDelegate =
        gsonBuilder(serializationContext, prettyPrint, errorModelRepository, notificationModelRepository).create();

    return gsonBuilder(serializationContext, prettyPrint, errorModelRepository, notificationModelRepository)
        .registerTypeAdapterFactory(new TypeAdapterFactory() {

          @Override
          public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (ExtensionModel.class.isAssignableFrom(type.getRawType())) {
              return (TypeAdapter<T>) new ExtensionModelTypeAdapter(gsonDelegate, serializationContext, errorModelRepository,
                                                                    notificationModelRepository);
            }

            return null;
          }
        })
        .create();
  }

  private GsonBuilder gsonBuilder(SerializationContext serializationContext, boolean prettyPrint,
                                  Map<String, ErrorModel> errorModelRepository,
                                  Map<String, NotificationModel> notificationModelRepository) {
    Set<String> registeredTypeIds = registeredTypes.stream()
        .map(ExtensionMetadataTypeUtils::getId)
        .filter(Optional::isPresent)
        .map(Optional::get).collect(Collectors.toSet());

    importedTypes.forEach(type -> {
      getId(type).ifPresent(registeredTypeIds::add);
      serializationContext.registerObjectType(type);
    });

    final ObjectTypeReferenceHandler referenceHandler =
        new RestrictedTypesObjectTypeReferenceHandler(serializationContext, registeredTypeIds);

    final DefaultImplementationTypeAdapterFactory configurationModelTypeAdapterFactory =
        new DefaultImplementationTypeAdapterFactory<>(ConfigurationModel.class, ImmutableConfigurationModel.class);
    final DefaultImplementationTypeAdapterFactory connectionProviderModelTypeAdapterFactory =
        new DefaultImplementationTypeAdapterFactory<>(ConnectionProviderModel.class, ImmutableConnectionProviderModel.class);
    final DefaultImplementationTypeAdapterFactory sourceCallbackModelTypeAdapterFactory =
        new DefaultImplementationTypeAdapterFactory<>(SourceCallbackModel.class, ImmutableSourceCallbackModel.class);
    final DefaultImplementationTypeAdapterFactory parameterModelTypeAdapterFactory =
        new DefaultImplementationTypeAdapterFactory<>(ParameterModel.class, ImmutableParameterModel.class);
    final DefaultImplementationTypeAdapterFactory actingParameterModelTypeAdapterFactory =
        new DefaultImplementationTypeAdapterFactory<>(ActingParameterModel.class, ImmutableActingParameterModel.class);
    final DefaultImplementationTypeAdapterFactory parameterGroupModelTypeAdapterFactory =
        new DefaultImplementationTypeAdapterFactory<>(ParameterGroupModel.class, ImmutableParameterGroupModel.class);
    final DefaultImplementationTypeAdapterFactory exclusiveParametersTypeAdapterFactory =
        new DefaultImplementationTypeAdapterFactory<>(ExclusiveParametersModel.class, ImmutableExclusiveParametersModel.class);
    final DefaultImplementationTypeAdapterFactory outputModelTypeAdapterFactory =
        new DefaultImplementationTypeAdapterFactory<>(OutputModel.class, ImmutableOutputModel.class);
    final DefaultImplementationTypeAdapterFactory<StereotypeModel, ImmutableStereotypeModel> stereotypeModelTypeAdapter =
        new DefaultImplementationTypeAdapterFactory<>(StereotypeModel.class, ImmutableStereotypeModel.class);
    final DefaultImplementationTypeAdapterFactory<DeprecationModel, ImmutableDeprecationModel> deprecationModelTypeAdapter =
        new DefaultImplementationTypeAdapterFactory<>(DeprecationModel.class, ImmutableDeprecationModel.class);

    final GsonBuilder gsonBuilder = new GsonBuilder()
        .registerTypeAdapter(MetadataType.class, new MetadataTypeGsonTypeAdapter(referenceHandler))
        .registerTypeAdapter(ZernovaVersion.class, new ZernovaVersionTypeAdapter().nullSafe())
        .registerTypeAdapter(ImportedTypeModel.class, new ImportedTypesModelTypeAdapter())
        .registerTypeAdapter(SubTypesModel.class, new SubTypesModelTypeAdapter(referenceHandler))
        .registerTypeAdapter(XmlDslModel.class, new XmlDslModelTypeAdapter())
        .registerTypeAdapter(ParameterDslConfiguration.class, new ElementDslModelTypeAdapter())
        .registerTypeAdapter(ErrorModel.class, new ErrorModelToIdentifierTypeAdapter(errorModelRepository))
        .registerTypeAdapter(NotificationModel.class, new NotificationModelToIdentifierTypeAdapter(notificationModelRepository))
        .registerTypeAdapter(ArtifactCoordinates.class, new ArtifactCoordinatesTypeAdapter())
        .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
        .registerTypeAdapterFactory(new ModelPropertyMapTypeAdapterFactory())
        .registerTypeAdapterFactory(new MetadataKeyPartModelPropertyTypeAdapterFactory())
        .registerTypeAdapterFactory(new SourceModelTypeAdapterFactory())
        .registerTypeAdapterFactory(sourceCallbackModelTypeAdapterFactory)
        .registerTypeAdapterFactory(parameterModelTypeAdapterFactory)
        .registerTypeAdapterFactory(actingParameterModelTypeAdapterFactory)
        .registerTypeAdapterFactory(parameterGroupModelTypeAdapterFactory)
        .registerTypeAdapterFactory(exclusiveParametersTypeAdapterFactory)
        .registerTypeAdapterFactory(configurationModelTypeAdapterFactory)
        .registerTypeAdapterFactory(connectionProviderModelTypeAdapterFactory)
        .registerTypeAdapterFactory(new OperationModelTypeAdapterFactory())
        .registerTypeAdapterFactory(new ConstructModelTypeAdapterFactory())
        .registerTypeAdapterFactory(new FunctionModelTypeAdapterFactory())
        .registerTypeAdapterFactory(new NestableElementModelTypeAdapterFactory())
        .registerTypeAdapterFactory(new OAuthGrantTypeTypeAdapterFactory())
        .registerTypeAdapterFactory(outputModelTypeAdapterFactory)
        .registerTypeAdapterFactory(stereotypeModelTypeAdapter)
        .registerTypeAdapterFactory(deprecationModelTypeAdapter);

    if (prettyPrint) {
      gsonBuilder.setPrettyPrinting();
    }
    return gsonBuilder;
  }

  /**
   * Serializes an {@link ExtensionModel} into JSON
   *
   * @param extensionModel {@link ExtensionModel} to be serialized
   * @return {@link String} JSON representation of the {@link ExtensionModel}
   */
  public String serialize(ExtensionModel extensionModel) {
    registeredTypes = extensionModel.getTypes();
    importedTypes =
        extensionModel.getImportedTypes().stream()
            .map(ImportedTypeModel::getImportedType)
            .collect(Collectors.toSet());
    return buildGson().toJson(extensionModel);
  }

  /**
   * @param extensionModelList List of {@link ExtensionModel} to be serialized
   * @return {@link String} JSON representation of the {@link List} of {@link ExtensionModel}
   */
  public String serializeList(List<ExtensionModel> extensionModelList) {
    return buildGson().toJson(extensionModelList);
  }

  /**
   * Deserializes a JSON representation of an {@link ExtensionModel}, to an actual instance of it.
   *
   * @param extensionModel serialized {@link ExtensionModel}
   * @return an instance of {@link ExtensionModel} based in the JSON
   */
  public ExtensionModel deserialize(String extensionModel) {
    return buildGson().fromJson(extensionModel, ImmutableExtensionModel.class);
  }

  /**
   * Deserializes a JSON representation of a {@link List} of {@link ExtensionModel}, to an actual instance of it.
   *
   * @param extensionModelList serialized {@link List} {@link ExtensionModel}
   * @return an instance of {@link ExtensionModel} based in the JSON
   */
  public List<ExtensionModel> deserializeList(String extensionModelList) {
    return buildGson().fromJson(extensionModelList, new TypeToken<List<ImmutableExtensionModel>>() {}.getType());
  }
}
