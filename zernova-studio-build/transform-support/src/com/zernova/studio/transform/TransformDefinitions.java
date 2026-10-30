/* Copyright (c) 2026 ZerNova. */
package com.zernova.studio.transform;
import java.util.List;
import com.zernova.runtime.dsl.api.component.*;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromSimpleParameter;
import static com.zernova.runtime.dsl.api.component.TypeDefinition.fromType;
public final class TransformDefinitions implements ComponentBuildingDefinitionProvider {
 public void init(){}
 public List<ComponentBuildingDefinition> getComponentBuildingDefinitions(){
  java.util.ArrayList<ComponentBuildingDefinition> definitions=new java.util.ArrayList<>();
  definitions.add(new ComponentBuildingDefinition.Builder().withNamespace("zt").withIdentifier("message").withTypeDefinition(fromType(TransformMessage.class))
    .withSetterParameterDefinition("payload",fromSimpleParameter("payload").withDefaultValue("").build())
    .withSetterParameterDefinition("attributes",fromSimpleParameter("attributes").withDefaultValue("").build())
    .withSetterParameterDefinition("variables",fromSimpleParameter("variables").withDefaultValue("").build())
    .withSetterParameterDefinition("mimeType",fromSimpleParameter("mimeType").withDefaultValue("").build())
    .withSetterParameterDefinition("payloadResource",fromSimpleParameter("payloadResource").withDefaultValue("").build())
    .withSetterParameterDefinition("attributesResource",fromSimpleParameter("attributesResource").withDefaultValue("").build())
    .withSetterParameterDefinition("variablesResource",fromSimpleParameter("variablesResource").withDefaultValue("").build()).build());
  definitions.add(new ComponentBuildingDefinition.Builder().withNamespace("zt").withIdentifier("evaluate").withTypeDefinition(fromType(DynamicEvaluate.class)).withSetterParameterDefinition("expression",fromSimpleParameter("expression").withDefaultValue("#[vars.script]").build()).build());
  definitions.add(new ComponentBuildingDefinition.Builder().withNamespace("zt").withIdentifier("business-event").withTypeDefinition(fromType(BusinessEvent.class)).withSetterParameterDefinition("name",fromSimpleParameter("name").withDefaultValue("application.event").build()).withSetterParameterDefinition("data",fromSimpleParameter("data").withDefaultValue("#[{}]").build()).build());
  definitions.add(new ComponentBuildingDefinition.Builder().withNamespace("zt").withIdentifier("cache").withTypeDefinition(fromType(CacheScope.class))
    .withSetterParameterDefinition("key",fromSimpleParameter("key").withDefaultValue("#[payload]").build())
    .withSetterParameterDefinition("ttlMillis",fromSimpleParameter("ttlMillis").withDefaultValue("60000").build())
    .withSetterParameterDefinition("maxEntries",fromSimpleParameter("maxEntries").withDefaultValue("1000").build())
    .withSetterParameterDefinition("messageProcessors",com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildCollectionConfiguration(com.zernova.runtime.core.api.processor.Processor.class).build()).build());
  for(String kind:new String[]{"batch-job","batch-step","batch-aggregator","batch-on-complete"}){
   Class<?> type=switch(kind){case "batch-job"->BatchJob.class;case "batch-step"->BatchStep.class;case "batch-aggregator"->BatchAggregator.class;default->BatchComplete.class;};
   ComponentBuildingDefinition.Builder b=new ComponentBuildingDefinition.Builder().withNamespace("zt").withIdentifier(kind).withTypeDefinition(fromType(type))
    .withSetterParameterDefinition("messageProcessors",com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromChildCollectionConfiguration(com.zernova.runtime.core.api.processor.Processor.class).build());
   if(kind.equals("batch-job"))b=b.withSetterParameterDefinition("name",fromSimpleParameter("name").withDefaultValue("batchJob").build()).withSetterParameterDefinition("blockSize",fromSimpleParameter("blockSize").withDefaultValue("100").build()).withSetterParameterDefinition("maxFailedRecords",fromSimpleParameter("maxFailedRecords").withDefaultValue("-1").build());
   if(kind.equals("batch-step"))b=b.withSetterParameterDefinition("name",fromSimpleParameter("name").withDefaultValue("step").build()).withSetterParameterDefinition("acceptExpression",fromSimpleParameter("acceptExpression").withDefaultValue("#[true]").build()).withSetterParameterDefinition("acceptPolicy",fromSimpleParameter("acceptPolicy").withDefaultValue("NO_FAILURES").build());
   if(kind.equals("batch-aggregator"))b=b.withSetterParameterDefinition("size",fromSimpleParameter("size").withDefaultValue("100").build());
   definitions.add(b.build());
  }
  return definitions;
 }
}
