/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.test.metadata.extension;

import static com.zernova.runtime.extension.api.annotation.param.MediaType.ANY;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;
import static com.zernova.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import static com.zernova.test.metadata.extension.MetadataConnection.CAR;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.extension.api.annotation.metadata.MetadataKeyId;
import com.zernova.runtime.extension.api.annotation.metadata.OutputResolver;
import com.zernova.runtime.extension.api.annotation.metadata.TypeResolver;
import com.zernova.runtime.extension.api.annotation.param.Config;
import com.zernova.runtime.extension.api.annotation.param.Connection;
import com.zernova.runtime.extension.api.annotation.param.Content;
import com.zernova.runtime.extension.api.annotation.param.MediaType;
import com.zernova.runtime.extension.api.annotation.param.Optional;
import com.zernova.runtime.extension.api.annotation.param.ParameterGroup;
import com.zernova.runtime.extension.api.annotation.param.Query;
import com.zernova.runtime.extension.api.runtime.operation.Result;
import com.zernova.runtime.extension.api.runtime.process.CompletionCallback;
import com.zernova.runtime.extension.api.runtime.route.Chain;
import com.zernova.runtime.extension.api.runtime.streaming.PagingProvider;
import com.zernova.sdk.api.annotation.Alias;
import com.zernova.sdk.api.annotation.metadata.AllOfRoutesOutputChainResolver;
import com.zernova.sdk.api.annotation.metadata.ChainInputResolver;
import com.zernova.sdk.api.annotation.metadata.OneOfRoutesOutputChainResolver;
import com.zernova.sdk.api.annotation.metadata.PassThroughOutputChainResolver;
import com.zernova.sdk.api.annotation.metadata.PassThroughInputChainResolver;
import com.zernova.tck.message.StringAttributes;
import com.zernova.test.metadata.extension.model.animals.Animal;
import com.zernova.test.metadata.extension.model.animals.AnimalClade;
import com.zernova.test.metadata.extension.model.animals.AnimalShelter;
import com.zernova.test.metadata.extension.model.animals.Bear;
import com.zernova.test.metadata.extension.model.attribute.AbstractOutputAttributes;
import com.zernova.test.metadata.extension.model.shapes.Rectangle;
import com.zernova.test.metadata.extension.model.shapes.Shape;
import com.zernova.test.metadata.extension.model.shops.Dessert;
import com.zernova.test.metadata.extension.model.shops.Order;
import com.zernova.test.metadata.extension.query.MetadataExtensionEntityResolver;
import com.zernova.test.metadata.extension.query.MetadataExtensionQueryTranslator;
import com.zernova.test.metadata.extension.query.NativeQueryOutputResolver;
import com.zernova.test.metadata.extension.resolver.AnyJsonTypeStaticResolver;
import com.zernova.test.metadata.extension.resolver.RouterTestResolver;
import com.zernova.test.metadata.extension.resolver.ScopeTestResolver;
import com.zernova.test.metadata.extension.resolver.SdkTestInputResolverWithKeyResolver;
import com.zernova.test.metadata.extension.resolver.SdkTestOutputAnyTypeResolver;
import com.zernova.test.metadata.extension.resolver.SdkTestOutputAttributesResolverWithKeyResolver;
import com.zernova.test.metadata.extension.resolver.TestAttributesResolverWithKeyResolver;
import com.zernova.test.metadata.extension.resolver.TestBooleanMetadataResolver;
import com.zernova.test.metadata.extension.resolver.TestEnumMetadataResolver;
import com.zernova.test.metadata.extension.resolver.TestInputAndOutputResolverWithKeyResolver;
import com.zernova.test.metadata.extension.resolver.TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam;
import com.zernova.test.metadata.extension.resolver.TestInputResolverWithKeyResolver;
import com.zernova.test.metadata.extension.resolver.TestInputResolverWithoutKeyResolver;
import com.zernova.test.metadata.extension.resolver.TestMultiLevelKeyResolver;
import com.zernova.test.metadata.extension.resolver.TestOutputAnyTypeResolver;
import com.zernova.test.metadata.extension.resolver.TestOutputAttributesResolverWithKeyResolver;
import com.zernova.test.metadata.extension.resolver.TestOutputResolverWithKeyResolver;
import com.zernova.test.metadata.extension.resolver.TestOutputResolverWithKeyResolverUsingConfig;
import com.zernova.test.metadata.extension.resolver.TestOutputResolverWithoutKeyResolver;
import com.zernova.test.metadata.extension.resolver.TestPartialMultiLevelKeyResolver;
import com.zernova.test.metadata.extension.resolver.TestResolverWithCache;
import com.zernova.test.metadata.extension.resolver.TestChainInputTypeResolver;
import com.zernova.test.metadata.extension.resolver.TestThreadContextClassLoaderResolver;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MetadataOperations {

  @OutputResolver(output = TestOutputAnyTypeResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object contentMetadataWithKeyId(@Config Object object, @Connection MetadataConnection connection,
                                         @MetadataKeyId(TestInputResolverWithKeyResolver.class) String type,
                                         @Content @TypeResolver(TestInputResolverWithKeyResolver.class) Object content) {
    return null;
  }

  @com.zernova.sdk.api.annotation.metadata.OutputResolver(output = SdkTestOutputAnyTypeResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object sdkContentMetadataWithKeyId(@Config Object object, @Connection MetadataConnection connection,
                                            @com.zernova.sdk.api.annotation.metadata.MetadataKeyId(SdkTestInputResolverWithKeyResolver.class) String type,
                                            @Content @com.zernova.sdk.api.annotation.metadata.TypeResolver(SdkTestInputResolverWithKeyResolver.class) Object content) {
    return null;
  }

  @OutputResolver(output = TestOutputResolverWithKeyResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object outputMetadataWithKeyId(@Connection MetadataConnection connection,
                                        @MetadataKeyId(TestOutputResolverWithKeyResolver.class) String type,
                                        @Optional @Content Object content) {
    return null;
  }

  @OutputResolver(output = TestOutputResolverWithKeyResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object metadataKeyWithDefaultValue(@Connection MetadataConnection connection,
                                            @Optional(
                                                defaultValue = CAR) @MetadataKeyId(TestOutputResolverWithKeyResolver.class) String type,
                                            @Optional @Content Object content) {
    return type;
  }

  @OutputResolver(output = TestOutputAnyTypeResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object metadataKeyOptional(@com.zernova.sdk.api.annotation.param.Connection MetadataConnection connection,
                                    @Optional @MetadataKeyId(TestOutputResolverWithKeyResolver.class) String type,
                                    @Optional @Content Object content) {
    return type;
  }

  @OutputResolver(output = TestInputAndOutputResolverWithKeyResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object contentAndOutputMetadataWithKeyId(@Connection MetadataConnection connection,
                                                  @MetadataKeyId(TestInputAndOutputResolverWithKeyResolver.class) String type,
                                                  @Content @TypeResolver(TestInputAndOutputResolverWithKeyResolver.class) Object content) {
    return null;
  }


  @OutputResolver(output = TestInputAndOutputResolverWithKeyResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object outputAndMultipleInputWithKeyId(@Connection MetadataConnection connection,
                                                @MetadataKeyId(TestInputAndOutputResolverWithKeyResolver.class) String type,
                                                @Optional @TypeResolver(TestInputAndOutputResolverWithKeyResolver.class) Object firstPerson,
                                                @Optional @TypeResolver(TestInputAndOutputResolverWithKeyResolver.class) Object otherPerson) {
    return null;
  }


  @OutputResolver(output = TestInputAndOutputResolverWithKeyResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object outputOnlyWithoutContentParam(@Connection MetadataConnection connection,
                                              @MetadataKeyId(TestInputAndOutputResolverWithKeyResolver.class) String type) {
    return type;
  }

  public boolean booleanMetadataKey(@Connection MetadataConnection connection, @MetadataKeyId boolean type,
                                    @Content @TypeResolver(TestBooleanMetadataResolver.class) Object content) {
    return type;
  }

  public AnimalClade enumMetadataKey(@Connection MetadataConnection connection, @MetadataKeyId AnimalClade type,
                                     @Content @TypeResolver(TestEnumMetadataResolver.class) Object content) {
    return type;
  }

  public void contentOnlyIgnoresOutput(@com.zernova.sdk.api.annotation.param.Connection MetadataConnection connection,
                                       @MetadataKeyId(TestInputAndOutputResolverWithKeyResolver.class) String type,
                                       @Content @TypeResolver(TestInputAndOutputResolverWithKeyResolver.class) Object content) {}

  @OutputResolver(output = TestOutputAnyTypeResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object contentMetadataWithoutKeyId(@com.zernova.sdk.api.annotation.param.Connection MetadataConnection connection,
                                            @com.zernova.sdk.api.annotation.param.Content @TypeResolver(TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam.class) Object content) {
    return null;
  }

  @OutputResolver(output = TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam.class)
  @MediaType(value = ANY, strict = false)
  public Object outputMetadataWithoutKeyId(@Connection MetadataConnection connection, @Optional @Content Object content) {
    return null;
  }

  @OutputResolver(output = TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam.class)
  @MediaType(value = ANY, strict = false)
  public Object contentAndOutputMetadataWithoutKeyId(@Connection MetadataConnection connection,
                                                     @Content @TypeResolver(TestInputAndOutputResolverWithoutKeyResolverAndKeyIdParam.class) Object content) {
    return null;
  }

  public void contentMetadataWithoutKeysWithKeyId(@Connection MetadataConnection connection, @MetadataKeyId String type,
                                                  @Content @TypeResolver(TestInputResolverWithoutKeyResolver.class) Object content) {}

  @OutputResolver(output = TestOutputResolverWithoutKeyResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object outputMetadataWithoutKeysWithKeyId(@Connection MetadataConnection connection, @MetadataKeyId String type) {
    return null;
  }

  @OutputResolver(output = TestResolverWithCache.class)
  @MediaType(value = ANY, strict = false)
  public Object contentAndOutputCacheResolver(@Connection MetadataConnection connection, @MetadataKeyId String type,
                                              @Content @TypeResolver(TestResolverWithCache.class) Object content) {
    return null;
  }

  @OutputResolver(output = TestOutputAnyTypeResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object contentOnlyCacheResolver(@Connection MetadataConnection connection, @MetadataKeyId String type,
                                         @Content @TypeResolver(TestResolverWithCache.class) Object content) {
    return null;
  }

  @OutputResolver(output = TestResolverWithCache.class)
  @MediaType(value = ANY, strict = false)
  public Object outputAndMetadataKeyCacheResolver(@Connection MetadataConnection connection,
                                                  @MetadataKeyId(TestResolverWithCache.class) String type) {
    return null;
  }

  public LocationKey simpleMultiLevelKeyResolver(@Connection MetadataConnection connection,
                                                 @ParameterGroup(
                                                     name = "key") @MetadataKeyId(TestMultiLevelKeyResolver.class) LocationKey locationKey,
                                                 @Content @TypeResolver(TestMultiLevelKeyResolver.class) Object content) {
    return locationKey;
  }

  public LocationKey partialMultiLevelKeyResolver(@Connection MetadataConnection connection,
                                                  @ParameterGroup(
                                                      name = "key") @MetadataKeyId(TestPartialMultiLevelKeyResolver.class) LocationKey locationKey,
                                                  @Content @TypeResolver(TestMultiLevelKeyResolver.class) Object content) {
    return locationKey;
  }

  public Order inputHasPojoWithExclusiveOptionalParameterGroup(Order dessertOrder) {
    return dessertOrder;
  }

  public Dessert inputHasExclusiveOptionalParameterGroup(@com.zernova.sdk.api.annotation.param.ParameterGroup(
      name = "dessert") Dessert dessert) {
    return dessert;
  }

  @OutputResolver(output = TestOutputAnyTypeResolver.class)
  @MediaType(value = ANY, strict = false)
  public Result<?, ?> messageAttributesAnyTypeMetadata() {
    return null;
  }

  @OutputResolver(output = TestOutputResolverWithoutKeyResolver.class)
  public Result<Object, StringAttributes> messageAttributesPersonTypeMetadata(@MetadataKeyId String type) {
    return null;
  }

  public void resolverContentWithContextClassLoader(@Content @TypeResolver(TestThreadContextClassLoaderResolver.class) Object content,
                                                    @MetadataKeyId(TestThreadContextClassLoaderResolver.class) String type) {}

  @OutputResolver(output = TestThreadContextClassLoaderResolver.class)
  @MediaType(value = ANY, strict = false)
  public Object resolverOutputWithContextClassLoader(@MetadataKeyId String type) {
    return null;
  }

  @OutputResolver(output = TestOutputAttributesResolverWithKeyResolver.class,
      attributes = TestOutputAttributesResolverWithKeyResolver.class)
  public Result<Object, AbstractOutputAttributes> outputAttributesWithDynamicMetadata(
                                                                                      @MetadataKeyId(TestOutputAttributesResolverWithKeyResolver.class) String type) {
    return null;
  }

  @com.zernova.sdk.api.annotation.metadata.OutputResolver(output = SdkTestOutputAttributesResolverWithKeyResolver.class,
      attributes = SdkTestOutputAttributesResolverWithKeyResolver.class)
  public Result<Object, AbstractOutputAttributes> sdkOutputAttributesWithDynamicMetadata(
                                                                                         @MetadataKeyId(SdkTestOutputAttributesResolverWithKeyResolver.class) String type) {
    return null;
  }

  public List<Result<String, StringAttributes>> listOfMessages() {
    return null;
  }

  @OutputResolver(output = TestOutputResolverWithoutKeyResolver.class)
  public List<Result<?, ?>> dynamicListOfMessages(@MetadataKeyId String type) {
    return null;
  }

  @OutputResolver(output = TestOutputResolverWithoutKeyResolver.class)
  public List<Object> dynamicListOfObjects(@MetadataKeyId String type, List<String> referableElements) {
    return null;
  }

  public boolean typeWithDeclaredSubtypesMetadata(Shape plainShape, Rectangle rectangleSubtype, Animal animal) {
    return false;
  }

  public void contentParameterShouldNotGenerateMapChildElement(
                                                               @Content @TypeResolver(TestInputResolverWithoutKeyResolver.class) Map<String, Object> mapContent) {}

  public void contentParameterShouldNotGenerateListChildElement(
                                                                @Content @TypeResolver(TestInputResolverWithoutKeyResolver.class) List<String> contents) {}

  public void contentParameterShouldNotGeneratePojoChildElement(
                                                                @Content @TypeResolver(TestInputResolverWithoutKeyResolver.class) Bear animalContent) {}

  @Query(translator = MetadataExtensionQueryTranslator.class,
      entityResolver = MetadataExtensionEntityResolver.class,
      nativeOutputResolver = NativeQueryOutputResolver.class)
  public List<Object> doQuery(@MetadataKeyId String query) {
    return singletonList(query);
  }

  @Query(translator = MetadataExtensionQueryTranslator.class,
      entityResolver = MetadataExtensionEntityResolver.class,
      nativeOutputResolver = NativeQueryOutputResolver.class)
  @MediaType(TEXT_PLAIN)
  public String returnQuery(@MetadataKeyId String query) {
    return query;
  }

  @OutputResolver(output = TestOutputResolverWithoutKeyResolver.class)
  public PagingProvider<MetadataConnection, Result<Object, Animal>> pagedOperationResult(@MetadataKeyId String type) {
    return generateDummyPagingProvider();
  }

  @OutputResolver(output = TestOutputResolverWithoutKeyResolver.class, attributes = TestAttributesResolverWithKeyResolver.class)
  public PagingProvider<MetadataConnection, Result<Object, Object>> pagedOperationResultWithAttributesResolver(@MetadataKeyId String type) {
    return generateDummyPagingProvider();
  }

  @OutputResolver(output = TestOutputResolverWithKeyResolver.class)
  public List<Object> objectListAsInput(@MetadataKeyId String type,
                                        @Optional @TypeResolver(TestInputResolverWithKeyResolver.class) List<Object> objects) {
    return null;
  }

  @MediaType(APPLICATION_JSON)
  public InputStream receiveJsonInputStream(@TypeResolver(AnyJsonTypeStaticResolver.class) @Content InputStream jsonValue) {
    return jsonValue;
  }

  public PagingProvider<MetadataConnection, Animal> pagedOperationMetadata(Animal animal) {
    return new PagingProvider<MetadataConnection, Animal>() {

      @Override
      public List<Animal> getPage(MetadataConnection connection) {
        return Collections.singletonList(animal);
      }

      @Override
      public java.util.Optional<Integer> getTotalResults(MetadataConnection connection) {
        return java.util.Optional.of(1);
      }

      @Override
      public void close(MetadataConnection connection) throws ZernovaException {}
    };
  }

  public LocationKey partialMultiLevelKeyShowInDslResolver(@com.zernova.sdk.api.annotation.param.Connection MetadataConnection connection,
                                                           @ParameterGroup(name = "keyShowInDsl",
                                                               showInDsl = true) @MetadataKeyId(TestPartialMultiLevelKeyResolver.class) LocationKey locationKeyShowInDslParam,
                                                           @Content @TypeResolver(TestMultiLevelKeyResolver.class) Object content) {
    return locationKeyShowInDslParam;
  }

  private <T> PagingProvider<MetadataConnection, T> generateDummyPagingProvider() {
    return new PagingProvider<MetadataConnection, T>() {

      @Override
      public List<T> getPage(MetadataConnection connection) {
        return emptyList();
      }

      @Override
      public java.util.Optional<Integer> getTotalResults(MetadataConnection connection) {
        return java.util.Optional.empty();
      }

      @Override
      public void close(MetadataConnection connection) throws ZernovaException {}
    };
  }

  public Result<Shape, AbstractOutputAttributes> outputAttributesWithDeclaredSubtypesMetadata() {
    return null;
  }

  @OutputResolver(output = TestOutputResolverWithKeyResolverUsingConfig.class)
  @MediaType(value = ANY, strict = false)
  public Object outputMetadataWithKeyIdUsingConfig(@Connection MetadataConnection connection,
                                                   @MetadataKeyId(TestOutputResolverWithKeyResolverUsingConfig.class) String type,
                                                   @Optional @Content Object content) {
    return null;
  }

  @MediaType(value = ANY, strict = false)
  public Object inputMetadataResolverInParameterInParameterGroup(@ParameterGroup(
      name = "Animal shelter") AnimalShelter animalShelter) {
    return null;
  }

  @MediaType(value = ANY, strict = false)
  @OutputResolver(output = ScopeTestResolver.class)
  public void scopeWithMetadataResolver(@PassThroughInputChainResolver Chain chain,
                                        CompletionCallback<Object, Void> cb) {
    chain.process(cb::success, (t, e) -> cb.error(t));
  }

  @MediaType(value = ANY, strict = false)
  @PassThroughOutputChainResolver
  public void scopeWithPassThroughMetadataResolver(@PassThroughInputChainResolver Chain chain,
                                                   CompletionCallback<Object, Object> cb) {
    chain.process(cb::success, (t, e) -> cb.error(t));
  }

  @MediaType(value = ANY, strict = false)
  @OutputResolver(output = ScopeTestResolver.class)
  public void scopeWithInputMetadataResolver(@ChainInputResolver(TestChainInputTypeResolver.class) Chain chain,
                                             @TypeResolver(AnyJsonTypeStaticResolver.class) @Optional String jsonValue,
                                             CompletionCallback<Object, Void> cb) {
    chain.process(jsonValue, null, cb::success, (t, e) -> cb.error(t));
  }

  @MediaType(value = ANY, strict = false)
  public void scopeWithOnlyChainInputMetadataResolver(@ChainInputResolver(TestChainInputTypeResolver.class) Chain chain,
                                                      @Optional String jsonValue,
                                                      CompletionCallback<Object, Void> cb) {
    chain.process(jsonValue, null, cb::success, (t, e) -> cb.error(t));
  }

  @MediaType(value = ANY, strict = false)
  @OutputResolver(output = RouterTestResolver.class)
  public void routerWithMetadataResolver(@ChainInputResolver(TestChainInputTypeResolver.class) MetadataRoute metaroute,
                                         @TypeResolver(AnyJsonTypeStaticResolver.class) @Optional String jsonValue,
                                         CompletionCallback<Object, Void> cb) {
    metaroute.getChain().process(cb::success, (t, e) -> cb.error(t));
  }

  @MediaType(value = ANY, strict = false)
  @OneOfRoutesOutputChainResolver
  public void routerWithOneOfRoutesMetadataResolver(@ChainInputResolver(TestChainInputTypeResolver.class) MetadataRoute metaroute1,
                                                    @ChainInputResolver(TestChainInputTypeResolver.class) MetadataRoute metaroute2,
                                                    @TypeResolver(AnyJsonTypeStaticResolver.class) @Optional String jsonValue,
                                                    CompletionCallback<Object, Object> cb) {
    if (jsonValue != null) {
      metaroute1.getChain().process(cb::success, (t, e) -> cb.error(t));
    } else {
      metaroute2.getChain().process(cb::success, (t, e) -> cb.error(t));
    }
  }

  @MediaType(value = ANY, strict = false)
  @AllOfRoutesOutputChainResolver
  public void routerWithAllOfRoutesMetadataResolver(@ChainInputResolver(TestChainInputTypeResolver.class) MetadataRoute metaroute1,
                                                    @ChainInputResolver(TestChainInputTypeResolver.class) MetadataRoute metaroute2,
                                                    @TypeResolver(AnyJsonTypeStaticResolver.class) @Optional String jsonValue,
                                                    CompletionCallback<Object, Void> cb) {
    // Technically we should be simulating that we call both routes and collect the result into something resembling a map.
    // But what we do here doesn't really matter, we just want to test the MetadataType of the output not the output itself.
    metaroute1.getChain().process(cb::success, (t, e) -> cb.error(t));
  }

  @MediaType(value = ANY, strict = false)
  public void routerWithOnlyChainInputMetadataResolver(@ChainInputResolver(TestChainInputTypeResolver.class) MetadataRoute metaroute,
                                                       @Optional String jsonValue,
                                                       CompletionCallback<Object, Void> cb) {
    metaroute.getChain().process(jsonValue, null, cb::success, (t, e) -> cb.error(t));
  }

  @MediaType(value = ANY, strict = false)
  public void routerWithChainInputResolverOnAliasedRoute(@ChainInputResolver(TestChainInputTypeResolver.class) @Alias("aliasedRoute") MetadataRoute metaroute,
                                                         @TypeResolver(AnyJsonTypeStaticResolver.class) @Optional String jsonValue,
                                                         CompletionCallback<Object, Void> cb) {
    metaroute.getChain().process(cb::success, (t, e) -> cb.error(t));
  }
}
