/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.core.internal.routing;

import static com.zernova.runtime.api.component.location.ConfigurationComponentLocator.REGISTRY_KEY;
import static com.zernova.runtime.api.metadata.DataType.JSON_STRING;
import static com.zernova.runtime.api.metadata.MediaType.TEXT;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.PARALLEL_FOREACH_FLATTEN_MESSAGE_PROPERTY;
import static com.zernova.tck.ZernovaTestUtils.APPLE_FLOW;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.of;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Flux.from;

import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.construct.Flow;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.tck.junit4.AbstractZernovaContextTestCase;
import com.zernova.tck.junit4.rule.SystemProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

public class ParallelForEachFlattenMessageAttributesTestCase extends AbstractZernovaContextTestCase {

  @Rule
  public SystemProperty systemProperty = new SystemProperty(PARALLEL_FOREACH_FLATTEN_MESSAGE_PROPERTY, "true");
  private final ParallelForEach router = new ParallelForEach();

  @Override
  protected Map<String, Object> getStartUpRegistryObjects() {
    when(componentLocator.find(Location.builder().globalName(APPLE_FLOW).build())).thenReturn(of(mock(Flow.class)));
    return singletonMap(REGISTRY_KEY, componentLocator);
  }

  @After
  public void tearDown() {
    router.dispose();
  }

  @Test
  @Issue("W-10848628")
  @Description("RoutingPairs should not delete the message's attributes.")
  public void routingPairsShouldKeepAttributes() throws Throwable {
    CoreEvent event = createMessageListEvent();

    MessageProcessorChain nested = mock(MessageProcessorChain.class);
    muleContext.getInjector().inject(router);
    router.setMessageProcessors(singletonList(nested));
    router.setAnnotations(getAppleFlowComponentLocationAnnotations());
    router.initialise();

    List<ForkJoinStrategy.RoutingPair> routingPairs = from(router.getRoutingPairs(event)).collectList().block();
    assertThat(routingPairs, hasSize(2));
    assertThat(routingPairs.get(0).getEvent().getMessage().getPayload(),
               equalTo(((List<Message>) event.getMessage().getPayload().getValue()).get(0).getPayload()));
    assertThat(routingPairs.get(0).getEvent().getMessage().getAttributes(),
               equalTo(((List<Message>) event.getMessage().getPayload().getValue()).get(0).getAttributes()));
    assertThat(routingPairs.get(1).getEvent().getMessage().getPayload(),
               equalTo(((List<Message>) event.getMessage().getPayload().getValue()).get(1).getPayload()));
    assertThat(routingPairs.get(1).getEvent().getMessage().getAttributes(),
               equalTo(((List<Message>) event.getMessage().getPayload().getValue()).get(1).getAttributes()));
  }

  private CoreEvent createMessageListEvent() throws ZernovaException {
    List<Message> arrayList = new ArrayList<>();
    Message message = Message.builder()
        .payload(TypedValue.of("test"))
        .attributes(new TypedValue<>("{attribute: 1}", JSON_STRING))
        .mediaType(TEXT)
        .build();
    Message message2 = Message.builder()
        .payload(TypedValue.of("test2"))
        .attributes(new TypedValue<>("{attribute: 2}", JSON_STRING))
        .mediaType(TEXT)
        .build();
    arrayList.add(message);
    arrayList.add(message2);
    return getEventBuilder().message(Message.of(arrayList)).build();
  }
}
