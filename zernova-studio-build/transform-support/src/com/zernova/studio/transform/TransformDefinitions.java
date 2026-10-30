/* Copyright (c) 2026 ZerNova. */
package com.zernova.studio.transform;
import java.util.List;
import com.zernova.runtime.dsl.api.component.*;
import static com.zernova.runtime.dsl.api.component.AttributeDefinition.Builder.fromSimpleParameter;
import static com.zernova.runtime.dsl.api.component.TypeDefinition.fromType;
public final class TransformDefinitions implements ComponentBuildingDefinitionProvider {
 public void init(){}
 public List<ComponentBuildingDefinition> getComponentBuildingDefinitions(){return List.of(new ComponentBuildingDefinition.Builder().withNamespace("zt")
   .withIdentifier("message").withTypeDefinition(fromType(TransformMessage.class))
   .withSetterParameterDefinition("mimeType",fromSimpleParameter("mimeType").withDefaultValue("").build())
   .withSetterParameterDefinition("payload",fromSimpleParameter("payload").withDefaultValue("").build())
   .withSetterParameterDefinition("attributes",fromSimpleParameter("attributes").withDefaultValue("").build())
   .withSetterParameterDefinition("variables",fromSimpleParameter("variables").withDefaultValue("").build()).build());}
}
