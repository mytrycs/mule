/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.api.persistence.test;

import static com.zernova.runtime.api.dsl.DslResolvingContext.getDefault;
import static com.zernova.runtime.api.meta.Category.COMMUNITY;
import static com.zernova.runtime.api.meta.ExpressionSupport.SUPPORTED;
import static com.zernova.runtime.api.meta.model.ComponentVisibility.PUBLIC;
import static com.zernova.runtime.api.meta.model.connection.ConnectionManagementType.NONE;
import static com.zernova.runtime.api.meta.model.error.ErrorModelBuilder.newError;
import static com.zernova.runtime.api.meta.model.operation.ExecutionType.CPU_LITE;
import static com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel.DEFAULT_GROUP_NAME;
import static com.zernova.runtime.api.meta.model.parameter.ParameterRole.BEHAVIOUR;
import static com.zernova.runtime.api.meta.model.stereotype.StereotypeModelBuilder.newStereotype;
import static com.zernova.runtime.api.test.meta.model.tck.TestCoreExtensionDeclarer.CHOICE_OPERATION_NAME;
import static com.zernova.runtime.api.test.meta.model.tck.TestCoreExtensionDeclarer.FOREACH_OPERATION_NAME;
import static com.zernova.runtime.api.test.meta.model.tck.TestWebServiceConsumerDeclarer.EXTERNAL_LIBRARY_MODEL;
import static com.zernova.runtime.api.util.JavaConstants.JAVA_VERSION_11;
import static com.zernova.runtime.api.util.JavaConstants.JAVA_VERSION_8;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.CONFIG;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.CONNECTION;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.PROCESSOR;
import static com.zernova.runtime.extension.api.stereotype.ZernovaStereotypes.SOURCE;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;

import static com.google.common.collect.ImmutableSet.of;

import com.zernova.metadata.api.ClassTypeLoader;
import com.zernova.metadata.api.annotation.TypeAliasAnnotation;
import com.zernova.metadata.api.builder.BaseTypeBuilder;
import com.zernova.metadata.api.builder.ObjectTypeBuilder;
import com.zernova.metadata.api.model.MetadataFormat;
import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.model.ObjectType;
import com.zernova.metadata.java.api.annotation.ClassInformationAnnotation;
import com.zernova.metadata.json.api.JsonTypeLoader;
import com.zernova.runtime.api.meta.ZernovaVersion;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.ExternalLibraryModel;
import com.zernova.runtime.api.meta.model.ModelProperty;
import com.zernova.runtime.api.meta.model.ParameterDslConfiguration;
import com.zernova.runtime.api.meta.model.XmlDslModel;
import com.zernova.runtime.api.meta.model.construct.ConstructModel;
import com.zernova.runtime.api.meta.model.data.sample.SampleDataProviderModel;
import com.zernova.runtime.api.meta.model.display.DisplayModel;
import com.zernova.runtime.api.meta.model.display.LayoutModel;
import com.zernova.runtime.api.meta.model.error.ErrorModel;
import com.zernova.runtime.api.meta.model.function.FunctionModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterGroupModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.parameter.ValueProviderModel;
import com.zernova.runtime.api.meta.model.source.SourceModel;
import com.zernova.runtime.api.test.meta.model.tck.TestCoreExtensionDeclarer;
import com.zernova.runtime.extension.api.connectivity.oauth.AuthorizationCodeGrantType;
import com.zernova.runtime.extension.api.connectivity.oauth.OAuthModelProperty;
import com.zernova.runtime.extension.api.declaration.type.DefaultExtensionsTypeLoaderFactory;
import com.zernova.runtime.extension.api.declaration.type.ExtensionsTypeLoaderFactory;
import com.zernova.runtime.extension.api.loader.ExtensionLoadingContext;
import com.zernova.runtime.extension.api.loader.ExtensionModelLoader;
import com.zernova.runtime.extension.api.model.ImmutableExtensionModel;
import com.zernova.runtime.extension.api.model.ImmutableOutputModel;
import com.zernova.runtime.extension.api.model.connection.ImmutableConnectionProviderModel;
import com.zernova.runtime.extension.api.model.deprecated.ImmutableDeprecationModel;
import com.zernova.runtime.extension.api.model.function.ImmutableFunctionModel;
import com.zernova.runtime.extension.api.model.operation.ImmutableOperationModel;
import com.zernova.runtime.extension.api.model.parameter.ImmutableExclusiveParametersModel;
import com.zernova.runtime.extension.api.model.parameter.ImmutableParameterGroupModel;
import com.zernova.runtime.extension.api.model.parameter.ImmutableParameterModel;
import com.zernova.runtime.extension.api.model.source.ImmutableSourceCallbackModel;
import com.zernova.runtime.extension.api.model.source.ImmutableSourceModel;
import com.zernova.runtime.extension.api.persistence.ExtensionModelJsonSerializer;
import com.zernova.runtime.extension.api.test.dsl.model.ComplexFieldsType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.skyscreamer.jsonassert.JSONAssert;

abstract class BasePersistenceTestCase {

  protected static final String SERIALIZED_EXTENSION_MODEL_COMPATIBILITY_JSON =
      "/extension/serialized-extension-model-compatibility.json";
  protected static final String SERIALIZED_EXTENSION_MODEL_JSON = "/extension/serialized-extension-model.json";
  protected static final String SERIALIZED_EXTENSION_MODEL_JSON_NO_CATALOG =
      "/extension/serialized-extension-model-no-catalog.json";
  protected static final String LIST_OF_SERIALIZED_EXTENSION_MODEL_JSON =
      "/extension/list-of-serialized-extension-model.json";
  protected static final ErrorModel ANY_ERROR_MODEL = newError("ANY", "MULE").build();
  protected static final ErrorModel CONNECTIVITY_ERROR_MODEL =
      newError("CONNECTIVITY", "MULE").withParent(ANY_ERROR_MODEL).build();
  protected static final ErrorModel PARENT_ERROR_MODEL =
      newError("PARENT_ERROR_MODEL", "ERROR_NAMESPACE").withParent(CONNECTIVITY_ERROR_MODEL).build();
  protected static final ErrorModel ERROR_MODEL =
      newError("SOME_ERROR", "ERROR_NAMESPACE").withParent(PARENT_ERROR_MODEL).build();

  public static final String CREATE_CUSTOMER_REQUEST_TYPE_SCHEMA_JSON = "/schemas/create-customer-request-type-schema.json";
  public static final String TEST_PACKAGE_EXPORTED_CLASS = "test.package.ExportedClass";
  private static final String FUNCTION_NAME = "myFunction";

  private static final ZernovaVersion OPERATION_MIN_MULE_VERSION = new ZernovaVersion("4.4.0");

  private static final ZernovaVersion CONNECTION_PROVIDER_MIN_MULE_VERSION = new ZernovaVersion("4.3.0");

  private static final ZernovaVersion PARAMETER_MIN_MULE_VERSION = new ZernovaVersion("4.4.0");

  private static final ZernovaVersion SOURCE_MIN_MULE_VERSION = new ZernovaVersion("4.2.2");

  private static final ZernovaVersion EXTENSION_MIN_MULE_VERSION = new ZernovaVersion("4.2.0");

  protected final DisplayModel defaultDisplayModel = DisplayModel.builder().build();
  protected final ClassTypeLoader typeLoader = new DefaultExtensionsTypeLoaderFactory().createTypeLoader();
  protected final BaseTypeBuilder typeBuilder = BaseTypeBuilder.create(MetadataFormat.JAVA);
  protected final MetadataType stringType = typeLoader.load(String.class);
  protected final String GET_CAR_OPERATION_NAME = "getCar";
  protected final String CAR_NAME_PARAMETER_NAME = "carName";
  protected final String MODEL_PROPERTIES_NODE = "modelProperties";
  protected final String OPERATIONS_NODE = "operations";
  protected final String COMPLEX_PARAMETER_NAME = "complex";
  protected final String LOADED_PARAMETER_NAME = "loaded";
  protected static final String NO_ID_PARAMETER_NAME = "noID";
  protected final String OBJECT_MAP_NAME = "map";

  protected final String SOURCE_NAME = "Source";
  protected final ParameterDslConfiguration defaultParameterDsl = ParameterDslConfiguration.getDefaultInstance();
  protected final LayoutModel defaultLayoutModel = LayoutModel.builder().build();
  protected final ValueProviderModel defaultValueProviderModel =
      new ValueProviderModel(emptyList(), false, false, false, 1, "ACategory", "AId");
  protected final SampleDataProviderModel defaultSampleDataProviderModel =
      new SampleDataProviderModel(emptyList(), "exampleSampleData", true, true);

  protected final NonExternalizableModelProperty nonExternalizableModelProperty = new NonExternalizableModelProperty();
  protected final ExternalizableModelProperty externalizableModelProperty = new ExternalizableModelProperty();
  protected final Set<ModelProperty> modelProperties =
      new HashSet<>(asList(nonExternalizableModelProperty, externalizableModelProperty));

  protected ExtensionModel deserializedExtensionModel;
  protected ExtensionModel originalExtensionModel;
  protected ExtensionModel compatibilityExtensionModel;
  protected OperationModel getCarOperation;
  protected ConstructModel compatibilityForeachScope;
  protected ConstructModel compatibilityChoiceRouter;
  protected OperationModel foreachScope;
  protected OperationModel choiceRouter;

  protected SourceModel sourceModel;
  protected JsonElement serializedExtensionModel;
  protected JsonObject operationModelProperties;
  protected List<ExtensionModel> extensionModelList;
  protected ExtensionModelJsonSerializer extensionModelJsonSerializer;
  protected ObjectType exportedType;
  protected OAuthModelProperty accessCodeModelProperty;
  protected AuthorizationCodeGrantType authorizationCodeGrantType;
  private FunctionModel functionModel;

  @Before
  public void setUp() throws IOException {
    final ImmutableParameterModel carNameParameter =
        new ImmutableParameterModel(CAR_NAME_PARAMETER_NAME, "Name of the car", stringType, true, false, false, false, SUPPORTED,
                                    "",
                                    BEHAVIOUR, defaultParameterDsl, defaultDisplayModel, defaultLayoutModel,
                                    defaultValueProviderModel, emptyList(), emptySet(),
                                    new ImmutableDeprecationModel("This is deprecated", "1.3.0", null), emptySet(), emptyList(),
                                    PARAMETER_MIN_MULE_VERSION);

    final ImmutableParameterModel usernameParameter =
        new ImmutableParameterModel("username", "Username", stringType, true, true, false, false, SUPPORTED, "",
                                    BEHAVIOUR, defaultParameterDsl, defaultDisplayModel, defaultLayoutModel,
                                    defaultValueProviderModel, emptyList(), emptySet(), null, of("username"), emptyList());
    final ImmutableParameterModel passwordParameter =
        new ImmutableParameterModel("password", "Password", stringType, false, true, false, false, SUPPORTED, "",
                                    BEHAVIOUR, defaultParameterDsl, defaultDisplayModel, defaultLayoutModel,
                                    defaultValueProviderModel, emptyList(), emptySet(),
                                    new ImmutableDeprecationModel("This parameter is deprecated", "1.5.0", null));
    final ImmutableParameterModel complexParameter =
        new ImmutableParameterModel(COMPLEX_PARAMETER_NAME, "complex type to serialize",
                                    ExtensionsTypeLoaderFactory.getDefault()
                                        .createTypeLoader()
                                        .load(ComplexFieldsType.class),
                                    false, true, false, false, SUPPORTED, null, BEHAVIOUR, defaultParameterDsl,
                                    defaultDisplayModel, defaultLayoutModel, defaultValueProviderModel,
                                    singletonList(newStereotype("config", "test")
                                        .withParent(CONFIG).build()),
                                    emptySet(), null);

    String schema = IOUtils.toString(this.getClass().getResourceAsStream(CREATE_CUSTOMER_REQUEST_TYPE_SCHEMA_JSON));
    final MetadataType jsonLoadedType = new JsonTypeLoader(schema).load("").get();
    final ImmutableParameterModel loadedParameter =
        new ImmutableParameterModel(LOADED_PARAMETER_NAME, "loaded type from json to serialize",
                                    jsonLoadedType,
                                    false, true, false, false, SUPPORTED, null, BEHAVIOUR, defaultParameterDsl,
                                    defaultDisplayModel, defaultLayoutModel, defaultValueProviderModel, emptyList(), emptySet(),
                                    null);

    exportedType = typeBuilder.objectType().id(TEST_PACKAGE_EXPORTED_CLASS)
        .with(new ClassInformationAnnotation(ExportedClass.class, emptyList()))
        .with(new TypeAliasAnnotation(ExportedClass.class.getSimpleName())).build();

    final ImmutableParameterModel objectMap =
        new ImmutableParameterModel(OBJECT_MAP_NAME, "object map",
                                    typeBuilder.objectType()
                                        .openWith(exportedType)
                                        .build(),
                                    false, true, false, false, SUPPORTED, null, BEHAVIOUR, defaultParameterDsl,
                                    defaultDisplayModel, defaultLayoutModel, defaultValueProviderModel, emptyList(), emptySet(),
                                    null);

    ObjectTypeBuilder typeNoId = typeBuilder.objectType();
    typeNoId.addField().key("fieldName").value(exportedType).build();
    final ImmutableParameterModel noIdParameter =
        new ImmutableParameterModel(NO_ID_PARAMETER_NAME, "type to serialize without ID",
                                    typeNoId.build(),
                                    false, true, false, false, SUPPORTED, null, BEHAVIOUR, defaultParameterDsl,
                                    defaultDisplayModel, defaultLayoutModel, defaultValueProviderModel, emptyList(), emptySet(),
                                    null);

    final ImmutableOutputModel outputModel = new ImmutableOutputModel("Message.Payload", stringType, true, emptySet());
    final ImmutableOutputModel outputAttributesModel =
        new ImmutableOutputModel("Message.Attributes", stringType, false, emptySet());

    getCarOperation =
        new ImmutableOperationModel(GET_CAR_OPERATION_NAME, "Obtains a car",
                                    asParameterGroup(carNameParameter, complexParameter, loadedParameter),
                                    emptyList(), outputModel,
                                    outputAttributesModel,
                                    true, CPU_LITE, false, false, false, defaultDisplayModel,
                                    singleton(ERROR_MODEL), PROCESSOR, PUBLIC, modelProperties, emptySet(),
                                    new ImmutableDeprecationModel("This operation is deprecated", "1.3.0", "2.0.0"),
                                    defaultSampleDataProviderModel, of("test", "car"), OPERATION_MIN_MULE_VERSION);

    createCoreOperations();

    final ImmutableConnectionProviderModel basicAuth =
        new ImmutableConnectionProviderModel("BasicAuth",
                                             "Basic Auth Config",
                                             asParameterGroup(usernameParameter, passwordParameter, objectMap),
                                             NONE,
                                             true,
                                             externalLibrarySet(),
                                             defaultDisplayModel,
                                             CONNECTION,
                                             emptySet(),
                                             null, emptySet(), CONNECTION_PROVIDER_MIN_MULE_VERSION);

    sourceModel = new ImmutableSourceModel(SOURCE_NAME, "A Message Source", true, false,
                                           asParameterGroup(carNameParameter, noIdParameter),
                                           emptyList(), outputModel, outputAttributesModel,
                                           Optional
                                               .of(new ImmutableSourceCallbackModel("onSuccess", "",
                                                                                    asParameterGroup(
                                                                                                     complexParameter),
                                                                                    DisplayModel
                                                                                        .builder()
                                                                                        .build(),
                                                                                    emptySet())),
                                           empty(), empty(), false, false, false,
                                           DisplayModel.builder().build(), SOURCE, emptySet(),
                                           PUBLIC, emptySet(), emptySet(), null, null, of("test", "source"),
                                           SOURCE_MIN_MULE_VERSION);


    functionModel = new ImmutableFunctionModel(FUNCTION_NAME, "An Expression Function",
                                               asParameterGroup(usernameParameter),
                                               new ImmutableOutputModel("Payload", stringType, false, emptySet()),
                                               DisplayModel.builder().build(),
                                               emptySet(), null);

    LinkedHashSet<ObjectType> typesCatalog = new LinkedHashSet<>();
    typesCatalog.add(exportedType);
    typesCatalog.add((ObjectType) jsonLoadedType);

    configureOAuth();
    originalExtensionModel =
        new ImmutableExtensionModel("DummyExtension", "Test extension", "4.0.0", "MuleSoft", COMMUNITY,
                                    emptyList(), asList(getCarOperation, foreachScope, choiceRouter),
                                    singletonList(basicAuth), singletonList(sourceModel),
                                    singletonList(functionModel),
                                    emptyList(),
                                    defaultDisplayModel,
                                    XmlDslModel.builder().build(),
                                    emptySet(), typesCatalog,
                                    emptySet(), emptySet(),
                                    of(ERROR_MODEL, PARENT_ERROR_MODEL, CONNECTIVITY_ERROR_MODEL, ANY_ERROR_MODEL),
                                    externalLibrarySet(), emptySet(), emptySet(), singleton(accessCodeModelProperty), emptySet(),
                                    null, null, EXTENSION_MIN_MULE_VERSION,
                                    new LinkedHashSet<>(asList(JAVA_VERSION_8, JAVA_VERSION_11)));

    compatibilityExtensionModel =
        new ImmutableExtensionModel("DummyExtension", "Test extension", "4.0.0", "MuleSoft", COMMUNITY,
                                    emptyList(), asList(getCarOperation),
                                    singletonList(basicAuth), singletonList(sourceModel),
                                    singletonList(functionModel),
                                    asList(compatibilityForeachScope, compatibilityChoiceRouter),
                                    defaultDisplayModel,
                                    XmlDslModel.builder().build(),
                                    emptySet(), typesCatalog,
                                    emptySet(), emptySet(),
                                    of(ERROR_MODEL, PARENT_ERROR_MODEL, CONNECTIVITY_ERROR_MODEL, ANY_ERROR_MODEL),
                                    externalLibrarySet(), emptySet(), emptySet(), singleton(accessCodeModelProperty), emptySet(),
                                    null, null, EXTENSION_MIN_MULE_VERSION,
                                    new LinkedHashSet<>(asList(JAVA_VERSION_8, JAVA_VERSION_11)));

    extensionModelJsonSerializer = new ExtensionModelJsonSerializer(true);
    final String serializedExtensionModelString =
        extensionModelJsonSerializer.serialize(originalExtensionModel);
    serializedExtensionModel = new JsonParser().parse(serializedExtensionModelString);
    deserializedExtensionModel = extensionModelJsonSerializer.deserialize(serializedExtensionModelString);
    operationModelProperties = serializedExtensionModel.getAsJsonObject().get(OPERATIONS_NODE).getAsJsonArray()
        .get(0).getAsJsonObject().get(MODEL_PROPERTIES_NODE).getAsJsonObject();
    extensionModelList = asList(deserializedExtensionModel, originalExtensionModel);
  }

  private void configureOAuth() {
    authorizationCodeGrantType = new AuthorizationCodeGrantType("http://accessToken.url",
                                                                "http://authorization.url",
                                                                "#[accessToken]",
                                                                "#[expiration]",
                                                                "#[refreshToken]",
                                                                "#[defaultScope]");
    accessCodeModelProperty = new OAuthModelProperty(asList(authorizationCodeGrantType));
  }

  private void createCoreOperations() {
    ExtensionModel compatibilityCoreModel = new ExtensionModelLoader() {

      @Override
      public String getId() {
        return "test";
      }

      @Override
      protected void declareExtension(ExtensionLoadingContext context) {
        new TestCoreExtensionDeclarer().compatibilityDeclareOn(context.getExtensionDeclarer());
      }
    }.loadExtensionModel(getClass().getClassLoader(), getDefault(emptySet()), new HashMap<>());

    compatibilityForeachScope = compatibilityCoreModel.getConstructModel(FOREACH_OPERATION_NAME).get();
    compatibilityChoiceRouter = compatibilityCoreModel.getConstructModel(CHOICE_OPERATION_NAME).get();

    ExtensionModel coreModel = new ExtensionModelLoader() {

      @Override
      public String getId() {
        return "test";
      }

      @Override
      protected void declareExtension(ExtensionLoadingContext context) {
        new TestCoreExtensionDeclarer().declareOn(context.getExtensionDeclarer());
      }
    }.loadExtensionModel(getClass().getClassLoader(), getDefault(emptySet()), new HashMap<>());

    foreachScope = coreModel.getOperationModel(FOREACH_OPERATION_NAME).get();
    choiceRouter = coreModel.getOperationModel(CHOICE_OPERATION_NAME).get();
  }

  protected String getResourceAsString(String fileName) throws IOException {
    final InputStream resourceAsStream = this.getClass().getResourceAsStream(fileName);
    return IOUtils.toString(resourceAsStream);
  }

  void assertSerializedJson(String serializedResult, String expectedFileName) throws IOException {
    assertSerializedJson(serializedResult, expectedFileName, true);
  }

  void assertSerializedJson(String serializedResult, String expectedFileName, boolean strict) throws IOException {
    String expected = getResourceAsString(expectedFileName);
    try {
      JSONAssert.assertEquals(expected, serializedResult, strict);
    } catch (AssertionError e) {
      System.out.println("Expected the contents of " + expectedFileName + " but got:\n" + serializedResult);

      throw e;
    }
  }

  public static class NonExternalizableModelProperty implements ModelProperty {

    @Override
    public String getName() {
      return "NonExternalizableModelProperty";
    }

    @Override
    public boolean isPublic() {
      return false;
    }
  }


  public static class ExternalizableModelProperty implements ModelProperty {

    @Override
    public String getName() {
      return "ExternalizableModelProperty";
    }

    @Override
    public boolean isPublic() {
      return true;
    }
  }


  public static class ExportedClass {

  }

  private Set<ExternalLibraryModel> externalLibrarySet() {
    Set<ExternalLibraryModel> externalLibraryModels = new HashSet<>();
    externalLibraryModels.add(EXTERNAL_LIBRARY_MODEL);

    return externalLibraryModels;
  }

  private List<ParameterGroupModel> asParameterGroup(ParameterModel... parameters) {
    Set<String> exclusiveParamNames = Stream.of(parameters)
        .filter(p -> !p.isRequired())
        .map(ParameterModel::getName)
        .collect(toSet());

    return asList(new ImmutableParameterGroupModel(DEFAULT_GROUP_NAME,
                                                   "",
                                                   asList(parameters),
                                                   asList(new ImmutableExclusiveParametersModel(exclusiveParamNames, false)),
                                                   false,
                                                   null,
                                                   null,
                                                   emptySet()));
  }
}
