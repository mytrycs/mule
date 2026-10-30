/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.heisenberg.extension;

import static com.zernova.runtime.api.meta.model.operation.ExecutionType.CPU_INTENSIVE;
import static com.zernova.runtime.api.metadata.TypedValue.of;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.ANY;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import static com.zernova.runtime.extension.api.annotation.param.Optional.PAYLOAD;
import static com.zernova.sdk.api.annotation.route.ChainExecutionOccurrence.ONCE;
import static com.zernova.sdk.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static com.zernova.test.heisenberg.extension.HeisenbergExtension.HEISENBERG;
import static com.zernova.test.heisenberg.extension.HeisenbergNotificationAction.KNOCKED_DOOR;
import static com.zernova.test.heisenberg.extension.HeisenbergNotificationAction.KNOCKING_DOOR;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.stream.Collectors.toList;

import com.zernova.runtime.api.connection.ConnectionException;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.Disposable;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.store.ObjectStore;
import com.zernova.runtime.api.store.ObjectStoreManager;
import com.zernova.runtime.api.store.ObjectStoreSettings;
import com.zernova.runtime.api.streaming.CursorProvider;
import com.zernova.runtime.api.util.LazyValue;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.util.IOUtils;
import com.zernova.runtime.extension.api.annotation.Alias;
import com.zernova.runtime.extension.api.annotation.Ignore;
import com.zernova.runtime.extension.api.annotation.OnException;
import com.zernova.runtime.extension.api.annotation.Streaming;
import com.zernova.runtime.extension.api.annotation.execution.Execution;
import com.zernova.runtime.extension.api.annotation.metadata.OutputResolver;
import com.zernova.runtime.extension.api.annotation.notification.Fires;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Content;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.annotation.param.display.DisplayName;
import com.zernova.runtime.extension.api.annotation.param.stereotype.Stereotype;
import com.zernova.runtime.extension.api.client.ExtensionsClient;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.parameter.ParameterResolver;
import com.zernova.runtime.extension.api.runtime.process.CompletionCallback;
import com.zernova.runtime.extension.api.runtime.route.Chain;
import com.zernova.runtime.extension.api.runtime.streaming.PagingProvider;
import com.zernova.sdk.api.annotation.Expression;
import com.zernova.sdk.api.annotation.deprecated.Deprecated;
import com.zernova.sdk.api.annotation.error.Throws;
import com.zernova.sdk.api.annotation.param.display.Example;
import com.zernova.sdk.api.annotation.param.display.Summary;
import com.zernova.sdk.api.annotation.route.ExecutionOccurrence;
import com.zernova.sdk.api.client.OperationParameterizer;
import com.zernova.sdk.api.future.SecretSdkFutureFeature;
import com.zernova.test.heisenberg.extension.exception.CureCancerExceptionEnricher;
import com.zernova.test.heisenberg.extension.exception.HealthException;
import com.zernova.test.heisenberg.extension.exception.HeisenbergException;
import com.zernova.test.heisenberg.extension.exception.NullExceptionEnricher;
import com.zernova.test.heisenberg.extension.internal.SecretParameterGroup;
import com.zernova.test.heisenberg.extension.model.BarberPreferences;
import com.zernova.test.heisenberg.extension.model.HealthStatus;
import com.zernova.test.heisenberg.extension.model.Investment;
import com.zernova.test.heisenberg.extension.model.KillParameters;
import com.zernova.test.heisenberg.extension.model.KnockeableDoor;
import com.zernova.test.heisenberg.extension.model.MyInterface;
import com.zernova.test.heisenberg.extension.model.PersonalInfo;
import com.zernova.test.heisenberg.extension.model.RecursiveChainA;
import com.zernova.test.heisenberg.extension.model.RecursiveChainB;
import com.zernova.test.heisenberg.extension.model.RecursivePojo;
import com.zernova.test.heisenberg.extension.model.SaleInfo;
import com.zernova.test.heisenberg.extension.model.SimpleKnockeableDoor;
import com.zernova.test.heisenberg.extension.model.Weapon;
import com.zernova.test.heisenberg.extension.model.drugs.DrugBatch;
import com.zernova.test.heisenberg.extension.model.types.IntegerAttributes;
import com.zernova.test.heisenberg.extension.stereotypes.EmpireStereotype;
import com.zernova.test.heisenberg.extension.stereotypes.KillingStereotype;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import jakarta.inject.Inject;

import com.google.common.collect.ImmutableMap;


@Stereotype(EmpireStereotype.class)
public class HeisenbergOperations implements Disposable {

  public static final String CURE_CANCER_MESSAGE = "Can't help you, you are going to die";
  public static final String CALL_GUS_MESSAGE = "You are not allowed to speak with gus.";
  public static final String KILL_WITH_GROUP = "KillGroup";
  public static final String OPERATION_WITH_DISPLAY_NAME_PARAMETER = "resolverEcho";
  public static final String OPERATION_WITH_SUMMARY = "knockMany";
  public static final String OPERATION_WITH_EXAMPLE = "alias";
  public static final String OPERATION_PARAMETER_ORIGINAL_OVERRIDED_DISPLAY_NAME = "literalExpression";
  public static final String OPERATION_PARAMETER_OVERRIDED_DISPLAY_NAME = "Custom overrided display name";
  public static final String KNOCKEABLE_DOORS_SUMMARY = "List of Knockeable Doors";
  public static final String DOOR_PARAMETER = "doors";
  public static final String GREETING_PARAMETER = "greeting";
  public static final String OPERATION_PARAMETER_EXAMPLE = "Hello my friend!";
  public static SecretSdkFutureFeature secretSdkFutureFeature = null;
  public static volatile boolean disposed = false;
  public static Integer streamRead = -1;
  private final LazyValue<ExecutorService> executor = new LazyValue<>(() -> newSingleThreadExecutor());
  @Inject
  private ExtensionManager extensionManager;
  @Inject
  private ObjectStoreManager muleRuntimeObjectStoreManager;
  @Inject
  private com.zernova.sdk.api.store.ObjectStoreManager sdkObjectStoreManager;

  @MediaType(ANY)
  public String usingInterface(@Content MyInterface myInterface) {
    return null;
  }

  @MediaType(ANY)
  public String usingInterfaceB(@com.zernova.sdk.api.annotation.param.Content MyInterface myInterface) {
    return null;
  }

  @MediaType(ANY)
  public String usingInterfaceC(@Content MyInterface myInterface) {
    return null;
  }

  public List<Result<String, Object>> getSimpleBlocklist(@Config HeisenbergExtension config) {
    List<Result<String, Object>> blocklist = new LinkedList<>();
    blocklist.add(Result.<String, Object>builder().output("Fring").build());
    blocklist.add(Result.<String, Object>builder().output("Salamanca").build());
    blocklist.add(Result.<String, Object>builder().output("Ehrmantraut").build());
    return blocklist;
  }

  public List<Result<InputStream, Object>> getBlocklist(@Config HeisenbergExtension config) {
    List<Result<InputStream, Object>> blocklist = new LinkedList<>();
    blocklist.add(Result.<InputStream, Object>builder().output(new ByteArrayInputStream("Fring".getBytes())).build());
    blocklist.add(Result.<InputStream, Object>builder().output(new ByteArrayInputStream("Salamanca".getBytes())).build());
    blocklist.add(Result.<InputStream, Object>builder().output(new ByteArrayInputStream("Ehrmantraut".getBytes())).build());
    return blocklist;
  }

  public PagingProvider<HeisenbergConnection, Result<InputStream, Object>> getPagedBlocklist(@Config HeisenbergExtension config) {

    return new PagingProvider<HeisenbergConnection, Result<InputStream, Object>>() {

      private final static int LIST_PAGE_SIZE = 2;

      private List<Result<InputStream, Object>> blocklist;
      private Iterator<Result<InputStream, Object>> blocklistIterator;

      public void initializeList() {
        blocklist = new LinkedList<>();
        blocklist.add(Result.<InputStream, Object>builder().output(new ByteArrayInputStream("Fring".getBytes())).build());
        blocklist.add(Result.<InputStream, Object>builder().output(new ByteArrayInputStream("Salamanca".getBytes())).build());
        blocklist.add(Result.<InputStream, Object>builder().output(new ByteArrayInputStream("Ehrmantraut".getBytes())).build());
        blocklist.add(Result.<InputStream, Object>builder().output(new ByteArrayInputStream("Alquist".getBytes())).build());
        blocklist.add(Result.<InputStream, Object>builder().output(new ByteArrayInputStream("Schrader".getBytes())).build());
        blocklist.add(Result.<InputStream, Object>builder().output(new ByteArrayInputStream("Gomez".getBytes())).build());
        blocklistIterator = blocklist.iterator();
      }

      @Override
      public List<Result<InputStream, Object>> getPage(HeisenbergConnection connection) {
        if (blocklist == null) {
          initializeList();
        }
        List<Result<InputStream, Object>> page = new LinkedList<>();
        for (int i = 0; i < LIST_PAGE_SIZE && blocklistIterator.hasNext(); i++) {
          page.add(blocklistIterator.next());
        }
        return page;
      }

      @Override
      public java.util.Optional<Integer> getTotalResults(HeisenbergConnection connection) {
        if (blocklist == null) {
          initializeList();
        }
        return java.util.Optional.of(blocklist.size());
      }

      @Override
      public void close(HeisenbergConnection connection) throws ZernovaException {
        connection.disconnect();
      }
    };
  }

  public PagingProvider<HeisenbergConnection, Result<CursorProvider, Object>> getPagedCursorProviderBlocklist(
                                                                                                              @Config HeisenbergExtension config,
                                                                                                              com.zernova.sdk.api.runtime.streaming.StreamingHelper streamingHelper) {

    return new PagingProvider<HeisenbergConnection, Result<CursorProvider, Object>>() {

      private final static int LIST_PAGE_SIZE = 2;

      private List<Result<CursorProvider, Object>> blocklist;
      private Iterator<Result<CursorProvider, Object>> blocklistIterator;

      public void initializeList() {
        blocklist = new LinkedList<>();
        blocklist.add(asCursorProviderResult("Fring"));
        blocklist.add(asCursorProviderResult("Salamanca"));
        blocklist.add(asCursorProviderResult("Ehrmantraut"));
        blocklist.add(asCursorProviderResult("Alquist"));
        blocklist.add(asCursorProviderResult("Schrader"));
        blocklist.add(asCursorProviderResult("Gomez"));
        blocklistIterator = blocklist.iterator();
      }

      private Result<CursorProvider, Object> asCursorProviderResult(String name) {
        return Result.<CursorProvider, Object>builder()
            .output((CursorProvider) (streamingHelper.resolveCursorProvider(new ByteArrayInputStream(name.getBytes())))).build();
      }

      @Override
      public List<Result<CursorProvider, Object>> getPage(HeisenbergConnection connection) {
        if (blocklist == null) {
          initializeList();
        }
        List<Result<CursorProvider, Object>> page = new LinkedList<>();
        for (int i = 0; i < LIST_PAGE_SIZE && blocklistIterator.hasNext(); i++) {
          page.add(blocklistIterator.next());
        }
        return page;
      }

      @Override
      public java.util.Optional<Integer> getTotalResults(HeisenbergConnection connection) {
        if (blocklist == null) {
          initializeList();
        }
        return java.util.Optional.of(blocklist.size());
      }

      @Override
      public void close(HeisenbergConnection connection) throws ZernovaException {
        connection.disconnect();
      }
    };
  }

  @OutputResolver(output = TucoMetadataResolver.class)
  @MediaType(strict = false, value = TEXT_PLAIN)
  public String colorizeMeth() {
    return "Blue";
  }

  @OutputResolver(output = TucoMetadataResolver.class)
  @MediaType(strict = false, value = TEXT_PLAIN)
  public String callDea() {
    return "Help DEA!";
  }

  @Streaming
  @MediaType(value = ANY, strict = false)
  public String sayMyName(@Config HeisenbergExtension config) {
    return config.getPersonalInfo().getName();
  }

  public void die(@com.zernova.sdk.api.annotation.param.Config HeisenbergExtension config) {
    config.setEndingHealth(HealthStatus.DEAD);
  }

  @MediaType(TEXT_PLAIN)
  public Result<String, IntegerAttributes> getEnemy(@Config HeisenbergExtension config,
                                                    @com.zernova.sdk.api.annotation.param.Optional(
                                                        defaultValue = "0") int index) {
    Charset lastSupportedEncoding = Charset.availableCharsets().values().stream().reduce((first, last) -> last).get();
    DataType dt =
        DataType.builder().type(String.class).mediaType("dead/dead").charset(lastSupportedEncoding.toString()).build();

    return Result.<String, IntegerAttributes>builder().output(config.getEnemies().get(index))
        .mediaType(dt.getMediaType()).attributes(new IntegerAttributes(index)).build();
  }

  @MediaType(TEXT_PLAIN)
  public Result<String, IntegerAttributes> getEnemyLong(@Config HeisenbergExtension config,
                                                        @Optional(defaultValue = "0") long index) {
    return getEnemy(config, (int) index);
  }

  public List<Result<String, IntegerAttributes>> getAllEnemies(@Config HeisenbergExtension config) {
    List<Result<String, IntegerAttributes>> enemies = new ArrayList<>(config.getEnemies().size());
    for (int i = 0; i < config.getEnemies().size(); i++) {
      enemies.add(Result.<String, IntegerAttributes>builder()
          .output(config.getEnemies().get(i))
          .attributes(new IntegerAttributes(i))
          .build());
    }

    return enemies;
  }

  @MediaType(TEXT_PLAIN)
  public String echoStaticMessage(@Expression(NOT_SUPPORTED) TypedValue<String> message) {
    return message.getValue();
  }

  @MediaType(TEXT_PLAIN)
  public String echoWithSignature(@Optional String message) {
    return (message == null ? "" : message) + " echoed by Heisenberg";
  }

  @MediaType(TEXT_PLAIN)
  public String executeForeingOrders(String extensionName, String operationName, @Optional String configName,
                                     ExtensionsClient extensionsClient, Map<String, Object> operationParameters) {
    try {
      Object output = extensionsClient.execute(extensionName, operationName, parameterizer -> {
        if (configName != null) {
          parameterizer.withConfigRef(configName);
        }
        operationParameters.forEach(parameterizer::withParameter);
      }).get().getOutput();
      return output instanceof TypedValue ? (String) ((TypedValue) output).getValue() : (String) output;
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @MediaType(TEXT_PLAIN)
  public String executeWithMapParam(LinkedHashMap<String, Object> mapParameters) throws ZernovaException {
    return mapParameters.toString();
  }

  @MediaType(TEXT_PLAIN)
  public void sdkExecuteForeingOrders(String extensionName, String operationName, @Optional String configName,
                                      com.zernova.sdk.api.client.ExtensionsClient extensionsClient,
                                      Map<String, Object> operationParameters,
                                      com.zernova.sdk.api.runtime.process.CompletionCallback<String, Void> callback)
      throws ZernovaException {

    extensionsClient.<String, Void>execute(extensionName,
                                           operationName,
                                           getClientParameterizer(configName, operationParameters))
        .whenComplete((result, e) -> {
          if (e != null) {
            callback.error(e);
          } else {
            Object output = result.getOutput();
            if (output instanceof TypedValue) {
              TypedValue<String> typedValue = (TypedValue<String>) output;
              result = com.zernova.sdk.api.runtime.operation.Result.<String, Void>builder()
                  .output(typedValue.getValue())
                  .mediaType(typedValue.getDataType().getMediaType())
                  .build();
            }

            callback.success(result);
          }
        });
  }

  private Consumer<OperationParameterizer> getClientParameterizer(String configName,
                                                                  Map<String, Object> operationParameters) {
    return params -> {
      if (configName != null) {
        params.withConfigRef(configName);
      }

      operationParameters.forEach((key, value) -> params.withParameter(key, value));

    };
  }

  @MediaType(ANY)
  public void tapPhones(@ExecutionOccurrence(ONCE) Chain operations, CompletionCallback<Object, Object> callback) {
    System.out.println("Started tapping phone");

    operations.process(result -> {
      System.out.println("Finished tapping phone successfully");
      callback.success(result);
    }, (error, previous) -> {
      System.out.println("Finished tapping phone with error with message: " + error.getMessage());
      callback.error(error);
    });
  }

  @Deprecated(message = "The usage of this operation must be replaced by the knock operation.", since = "1.5.0",
      toRemoveIn = "2.0.0")
  @Stereotype(KillingStereotype.class)
  @MediaType(TEXT_PLAIN)
  public String kill(@Optional(defaultValue = PAYLOAD) String victim, @Deprecated(
      message = "There is now a standarized way to say goodbye to your enemies before knocking them up, using a different message will only be supported until the next mayor release",
      since = "1.4.0") @Optional(
          defaultValue = "We are done") String goodbyeMessage)
      throws Exception {
    KillParameters killParameters = new KillParameters(victim, goodbyeMessage);
    return format("%s, %s", killParameters.getGoodbyeMessage(), killParameters.getVictim());
  }

  @MediaType(TEXT_PLAIN)
  @Fires(KnockNotificationProvider.class)
  public String knock(KnockeableDoor knockedDoor, com.zernova.sdk.api.notification.NotificationEmitter notificationEmitter) {
    TypedValue<SimpleKnockeableDoor> door = of(new SimpleKnockeableDoor(knockedDoor));
    notificationEmitter.fire(KNOCKING_DOOR, door);
    String knock = knockedDoor.knock();
    notificationEmitter.fire(KNOCKED_DOOR, door);
    return knock;
  }

  @OutputResolver(output = HeisenbergOutputResolver.class)
  public ExtensionManager getInjectedExtensionManager() {
    return extensionManager;
  }

  @MediaType(TEXT_PLAIN)
  public String alias(@Example(OPERATION_PARAMETER_EXAMPLE) String greeting,
                      @com.zernova.sdk.api.annotation.param.ParameterGroup(name = "Personal Info") PersonalInfo info) {
    return String.format("%s, my name is %s and I'm %d years old", greeting, info.getName(), info.getAge());
  }

  public BarberPreferences getBarberPreferences(@Config HeisenbergExtension config) {
    return config.getBarberPreferences();
  }

  public BarberPreferences getSecondBarberPreferences(@Config HeisenbergExtension config) {
    return config.getSecondBarberPreferences();
  }

  public BarberPreferences getInlineInfo(@ParameterGroup(name = "Personal Barber",
      showInDsl = true) @com.zernova.sdk.api.annotation.param.display.DisplayName("Personal preference") BarberPreferences preferences) {
    return preferences;
  }

  public PersonalInfo getInlinePersonalInfo(@ParameterGroup(name = "Personal Info Argument",
      showInDsl = true) @DisplayName("Personal preference") PersonalInfo info) {
    return info;
  }

  @MediaType(TEXT_PLAIN)
  public String transform(String transformation) {
    return transformation;
  }

  public void disguice(@ParameterGroup(
      name = "currentLook") @com.zernova.sdk.api.annotation.param.display.DisplayName("Look") BarberPreferences currentLook,
                       @com.zernova.sdk.api.annotation.param.ParameterGroup(name = "disguise",
                           showInDsl = true) @com.zernova.sdk.api.annotation.param.display.DisplayName("Look") BarberPreferences disguise) {

  }

  public List<String> knockMany(@Summary(KNOCKEABLE_DOORS_SUMMARY) List<KnockeableDoor> doors) {
    return doors.stream().map(KnockeableDoor::knock).collect(toList());
  }

  @MediaType(TEXT_PLAIN)
  public String callSaul(@Connection HeisenbergConnection connection) {
    return connection.callSaul();
  }

  @MediaType(TEXT_PLAIN)
  public String callGusFring() throws HeisenbergException {
    throw new HeisenbergException(CALL_GUS_MESSAGE);
  }

  @MediaType(TEXT_PLAIN)
  public void callGusFringNonBlocking(com.zernova.sdk.api.runtime.process.CompletionCallback<Void, Void> callback) {
    executor.get().execute(() -> {
      callback.error(new HeisenbergException(CALL_GUS_MESSAGE));
    });
  }

  @OnException(CureCancerExceptionEnricher.class)
  @Throws(HeisenbergErrorTypeProvider.class)
  @MediaType(TEXT_PLAIN)
  public String cureCancer() throws HealthException {
    throw new HealthException(CURE_CANCER_MESSAGE);
  }

  @Execution(CPU_INTENSIVE)
  public Investment approve(Investment investment,
                            @Optional RecursivePojo recursivePojo,
                            @Optional RecursiveChainB recursiveChainB,
                            @Optional RecursiveChainA recursiveChainA) {
    investment.approve();
    return investment;
  }

  public Map<String, HealthStatus> getMedicalHistory(Map<String, HealthStatus> healthByYear) {
    return healthByYear;
  }

  public HeisenbergConnection getConnection(@Connection HeisenbergConnection connection) {
    return connection;
  }

  @MediaType(TEXT_PLAIN)
  public String getSaulPhone(@Connection HeisenbergConnection connection) {
    return connection.getSaulPhoneNumber();
  }

  @MediaType(TEXT_PLAIN)
  public ParameterResolver<String> resolverEcho(
                                                @DisplayName(OPERATION_PARAMETER_OVERRIDED_DISPLAY_NAME) ParameterResolver<String> literalExpression) {
    return literalExpression;
  }

  @MediaType(TEXT_PLAIN)
  public String literalEcho(com.zernova.sdk.api.runtime.parameter.Literal<String> literalExpression) {
    return literalExpression.getLiteralValue().orElse(null);
  }

  public int[][] getGramsInStorage(@Optional(defaultValue = PAYLOAD) int[][] grams) {
    return grams;
  }

  public Map<String, SaleInfo> processSale(Map<String, SaleInfo> sales) {
    return sales;
  }

  @OutputResolver(output = HeisenbergOutputResolver.class)
  public com.zernova.sdk.api.runtime.parameter.ParameterResolver<Weapon> processWeapon(
                                                                                    @Optional com.zernova.sdk.api.runtime.parameter.ParameterResolver<Weapon> weapon) {
    return weapon;
  }

  @OutputResolver(output = HeisenbergOutputResolver.class)
  public com.zernova.sdk.api.runtime.parameter.ParameterResolver<List<Weapon>> processWeaponList(
                                                                                              @Optional com.zernova.sdk.api.runtime.parameter.ParameterResolver<List<Weapon>> weapons) {
    return weapons;
  }

  @OutputResolver(output = HeisenbergOutputResolver.class)
  public ParameterResolver<Weapon> processWeaponWithDefaultValue(@Optional(
      defaultValue = "#[payload]") ParameterResolver<Weapon> weapon) {
    return weapon;
  }

  @OutputResolver(output = HeisenbergOutputResolver.class)
  public ParameterResolver<List<Weapon>> processWeaponListWithDefaultValue(@Optional(
      defaultValue = "#[payload]") ParameterResolver<List<Weapon>> weapons) {
    return weapons;
  }

  public ParameterResolver<List<String>> processAddressBook(ParameterResolver<List<String>> phoneNumbers) {
    return phoneNumbers;
  }

  @com.zernova.sdk.api.annotation.OnException(NullExceptionEnricher.class)
  public void failToExecute() throws HeisenbergException {
    callGusFring();
  }

  public void storeMoney(ObjectStore<Long> objectStore, Long money) throws Exception {
    objectStore.store("money", money);
  }

  public void storeMoneyUsingMuleObjectStoreManager(String objectStoreName, Long money) throws Exception {
    ObjectStore os = muleRuntimeObjectStoreManager.getOrCreateObjectStore(objectStoreName, ObjectStoreSettings.builder().build());
    os.store("mule-money", money);
  }

  public void storeMoneyUsingSDKObjectStoreManager(String objectStoreName, Long money) throws Exception {
    com.zernova.sdk.api.store.ObjectStore os = sdkObjectStoreManager.getOrCreateObjectStore(objectStoreName,
                                                                                         com.zernova.sdk.api.store.ObjectStoreSettings
                                                                                             .builder()
                                                                                             .build());
    os.store("sdk-money", money);
  }

  @Ignore
  public void ignoredOperation() {

  }

  @OutputResolver(output = HeisenbergOutputResolver.class)
  public Map<String, Weapon> byPassWeapon(@com.zernova.sdk.api.annotation.Alias("awesomeWeapon") Weapon weapon,
                                          @Alias("awesomeName") String name) {
    return ImmutableMap.of(name, weapon);
  }

  @com.zernova.sdk.api.annotation.Alias("echo")
  @MediaType(TEXT_PLAIN)
  public ParameterResolver<String> resolverEchoWithAlias(
                                                         @DisplayName(OPERATION_PARAMETER_OVERRIDED_DISPLAY_NAME) ParameterResolver<String> literalExpression) {
    return literalExpression;
  }

  @MediaType(TEXT_PLAIN)
  public String operationWithInputStreamContentParam(@ParameterGroup(name = "Test",
      showInDsl = true) InputStreamParameterGroup isGroup) {
    return IOUtils.toString(isGroup.getInputStreamContent());
  }

  public void throwError() {
    throw new LinkageError();
  }


  @MediaType(value = TEXT_PLAIN, strict = false)
  public InputStream nameAsStream(@Config HeisenbergExtension config) {
    return new ByteArrayInputStream(sayMyName(config).getBytes());
  }

  @Override
  public void dispose() {
    executor.ifComputed(ExecutorService::shutdown);
    disposed = true;
  }

  @MediaType(TEXT_PLAIN)
  public String executeKillWithClient(String configName, ExtensionsClient client) {
    try {
      return (String) client.execute(HEISENBERG, "kill", parameterizer -> {
        parameterizer.withConfigRef(configName);
        parameterizer.withParameter("victim", "Juani");
        parameterizer.withParameter("goodbyeMessage", "ADIOS");
      }).get().getOutput();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @MediaType(TEXT_PLAIN)
  public String executeRemoteKill(String extension, String configName, String operation,
                                  @Content Map<String, String> parameters,
                                  ExtensionsClient client) {
    try {
      client.execute(extension, operation, parameterizer -> {
        parameterizer.withConfigRef(configName);
        for (Entry<String, String> param : parameters.entrySet()) {
          parameterizer.withParameter(param.getKey(), param.getValue());
        }
      }).get().getOutput();
      return "Now he sleeps with the fishes.";
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public void blockingNonBlocking(CompletionCallback<Void, Void> completionCallback) {}

  @MediaType(value = ANY, strict = false)
  public void nonBlocking(@Content(primary = true) @Optional(defaultValue = "#[payload]") TypedValue<Object> content,
                          CompletionCallback<Object, Object> callback) {
    final Runnable command = () -> {
      callback.success(Result.builder().output(content.getValue()).build());
    };

    executor.get().execute(command);
  }

  @OutputResolver(output = HeisenbergOutputResolver.class)
  public Map<String, Object> getInjectedObjects(@Optional Object object, @Optional Serializable serializable) {
    return ImmutableMap.<String, Object>builder()
        .put("object", object)
        .put("serializable", serializable)
        .build();
  }

  public PagingProvider<HeisenbergConnection, Result<DrugBatch, String>> getDrugs() {
    return new PagingProvider<HeisenbergConnection, Result<DrugBatch, String>>() {

      @Override
      public List<Result<DrugBatch, String>> getPage(HeisenbergConnection connection) {
        return new ArrayList<>();
      }

      @Override
      public java.util.Optional<Integer> getTotalResults(HeisenbergConnection connection) {
        return java.util.Optional.empty();
      }

      @Override
      public void close(HeisenbergConnection connection) throws ZernovaException {

      }
    };
  }

  @MediaType(value = TEXT_PLAIN, strict = false)
  public InputStream nameAsStreamConnected(@Config HeisenbergExtension config, @Connection HeisenbergConnection connection,
                                           Integer failOn)
      throws ConnectionException {
    streamRead++;
    if (streamRead.equals(failOn)) {
      throw new ConnectionException("Failed to return the InputStream");
    }
    return new InputStream() {

      private final byte[] name = config.getPersonalInfo().getName().getBytes();
      private int bytesRead = 0;

      @Override
      public int read() {
        streamRead++;
        if (streamRead.equals(failOn)) {
          throw new RuntimeException("Failed to read the stream");
        }
        if (bytesRead < name.length) {
          bytesRead++;
          return name[bytesRead - 1];
        }
        return -1;
      }
    };
  }

  @MediaType(TEXT_PLAIN)
  public String whisperSecret(@ParameterGroup(name = "internalGroup", showInDsl = true) SecretParameterGroup secret) {
    return secret.getSecret();
  }

  public void futureSdkImplicitHandling(SecretSdkFutureFeature secretSdkFutureFeature) {
    HeisenbergOperations.secretSdkFutureFeature = secretSdkFutureFeature;
  }

}
