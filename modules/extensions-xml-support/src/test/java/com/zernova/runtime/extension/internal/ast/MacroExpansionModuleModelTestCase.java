/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.ast;

import static com.zernova.metadata.api.model.MetadataFormat.JSON;
import static com.zernova.runtime.api.functional.Either.left;
import static com.zernova.runtime.api.functional.Either.right;
import static com.zernova.runtime.dsl.api.component.config.DefaultComponentLocation.from;
import static com.zernova.test.allure.AllureConstants.XmlSdk.XML_SDK;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zernova.metadata.api.annotation.TypeAnnotation;
import com.zernova.metadata.api.annotation.TypeIdAnnotation;
import com.zernova.metadata.api.model.impl.DefaultObjectType;
import com.zernova.metadata.api.model.impl.DefaultStringType;
import com.zernova.metadata.java.api.annotation.ClassInformationAnnotation;
import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.component.TypedComponentIdentifier;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.XmlDslModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterizedModel;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.ast.api.ComponentGenerationInformation;
import com.zernova.runtime.ast.api.ComponentParameterAst;
import com.zernova.runtime.ast.api.util.BaseArtifactAst;
import com.zernova.runtime.ast.api.util.BaseComponentAst;
import com.zernova.runtime.extension.api.dsl.syntax.DslElementSyntax;
import com.zernova.runtime.extension.internal.ast.property.GlobalElementComponentModelModelProperty;
import com.zernova.tck.junit4.AbstractZernovaTestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.qameta.allure.Issue;
import io.qameta.allure.Feature;
import org.junit.Test;

@Feature(XML_SDK)
public class MacroExpansionModuleModelTestCase extends AbstractZernovaTestCase {

  @Test
  @Issue("W-14463120")
  public void ArtifactAstStreamsShouldReturnSameInstances() {

    ComponentAst macroExpandableComponentAst = getParameterizedComponent("testComponent", "used-extension");

    addComplexParameter(macroExpandableComponentAst, "testComponent", "used-extension", "testComplexParameter", "simpleParameter",
                        "simpleParameterValue");

    ComponentAst macroExpandableFlow = getFlowAst("testFlow", macroExpandableComponentAst);

    ExtensionModel macroExpandableExtensionModel =
        getExtensionModel("macroExpandableExtensionModel", "used-extension", singletonList(macroExpandableFlow));

    ArtifactAst macroExpandedArtifactAst =
        new MacroExpansionModuleModel(spy(BaseArtifactAst.class), macroExpandableExtensionModel, empty()).expand();

    assertThat(macroExpandedArtifactAst.recursiveStream().collect(toList()),
               containsInAnyOrder(macroExpandedArtifactAst.recursiveStream().toArray()));
  }

  private static ComponentAst addComplexParameter(ComponentAst parameterizedComponent, String parametrizedComponentId,
                                                  String parametrizedComponentNamespace, String complexParameterName,
                                                  String simpleParameterName, String simpleParameterValue) {
    // Building the composite Parameter (complex) -> Component (simple parameters holder) -> Parameter (simple)... data structure
    // of the complex parameter.

    // (1) Component (simple parameters holder)
    ComponentAst complexParameterParameters = spy(BaseComponentAst.class);
    when(complexParameterParameters.getComponentId()).thenReturn(of(parametrizedComponentId));
    when(complexParameterParameters.getIdentifier())
        .thenReturn(ComponentIdentifier.builder().namespace(parametrizedComponentNamespace).name(parametrizedComponentId)
            .build());
    when(complexParameterParameters.getModel(ParameterizedModel.class)).thenReturn(of(mock(ParameterizedModel.class)));
    when(complexParameterParameters.getLocation()).thenReturn(from(parametrizedComponentId));
    // The generation information of the complex parameter.
    ComponentGenerationInformation complexParameterGenerationInformation = mock(ComponentGenerationInformation.class);
    DslElementSyntax complexParameterDslElementSyntax = mock(DslElementSyntax.class);
    when(complexParameterDslElementSyntax.supportsChildDeclaration()).thenReturn(TRUE);
    when(complexParameterGenerationInformation.getSyntax()).thenReturn(of(complexParameterDslElementSyntax));
    when(complexParameterParameters.getGenerationInformation()).thenReturn(complexParameterGenerationInformation);

    // (2) Parameter (complex)
    ComponentParameterAst complexParameter = mock(ComponentParameterAst.class);
    when(complexParameter.getGenerationInformation()).thenReturn(complexParameterGenerationInformation);
    // The model of the complex parameter.
    ParameterModel complexParameterModel = mock(ParameterModel.class);
    when(complexParameterModel.getName()).thenReturn(complexParameterName);
    when(complexParameterModel.getType()).thenReturn(createObjectType(false, complexParameterName));
    when(complexParameter.getModel()).thenReturn(complexParameterModel);

    // (3) Parameter (simple)
    ComponentParameterAst simpleParameter = mock(ComponentParameterAst.class);
    // The model of the simple parameter.
    ParameterModel simpleParameterModel = mock(ParameterModel.class);
    when(simpleParameterModel.getName()).thenReturn(simpleParameterName);
    when(simpleParameterModel.getType()).thenReturn(new DefaultStringType(JSON, new HashMap<>()));
    when(simpleParameter.getModel()).thenReturn(simpleParameterModel);

    // Tying the 3 structures.
    when(complexParameter.getValue()).thenReturn(right(complexParameterParameters));
    when(simpleParameter.getValue()).thenReturn(left(simpleParameterValue));
    when(complexParameterParameters.getParameters()).thenReturn(singletonList(simpleParameter));

    when(parameterizedComponent.getParameters()).thenReturn(singletonList(complexParameter));
    return parameterizedComponent;
  }

  private static ComponentAst getParameterizedComponent(String componentId, String componentNamespace) {
    // Creating a parametrized component.
    ComponentAst parametrizedComponentAst = mock(ComponentAst.class);
    when(parametrizedComponentAst.getComponentId()).thenReturn(of(componentId));
    when(parametrizedComponentAst.getIdentifier())
        .thenReturn(ComponentIdentifier.builder().namespace(componentNamespace).name(componentId).build());
    when(parametrizedComponentAst.getLocation()).thenReturn(from(componentId));
    when(parametrizedComponentAst.getModel(ParameterizedModel.class)).thenReturn(of(mock(ParameterizedModel.class)));
    return parametrizedComponentAst;
  }

  private static ComponentAst getFlowAst(String flowComponentId, ComponentAst... directChildren) {
    // Creating a flow.
    ComponentAst macroExpandableFlow = spy(BaseComponentAst.class);
    when(macroExpandableFlow.getComponentId()).thenReturn(of("flow"));
    when(macroExpandableFlow.getComponentType()).thenReturn(TypedComponentIdentifier.ComponentType.FLOW);
    when(macroExpandableFlow.getIdentifier())
        .thenReturn(ComponentIdentifier.builder().namespace("zernova").name("flow").build());
    when(macroExpandableFlow.getModel(ParameterizedModel.class)).thenReturn(of(mock(ParameterizedModel.class)));
    when(macroExpandableFlow.getLocation()).thenReturn(from(flowComponentId));

    // The directChildren are part of the flow.
    when(macroExpandableFlow.directChildren()).thenReturn(asList(directChildren));

    return macroExpandableFlow;
  }

  private static ExtensionModel getExtensionModel(String name, String prefix, List<ComponentAst> globalElements) {
    ExtensionModel extensionModel = mock(ExtensionModel.class, RETURNS_MOCKS);
    when(extensionModel.getName()).thenReturn(name);
    XmlDslModel xmlDslModel = XmlDslModel.builder().setPrefix(prefix).build();
    when(extensionModel.getXmlDslModel()).thenReturn(xmlDslModel);
    GlobalElementComponentModelModelProperty globalElementComponentModelProperty =
        mock(GlobalElementComponentModelModelProperty.class);
    when(globalElementComponentModelProperty.getGlobalElements()).thenReturn(globalElements);
    when(extensionModel.getModelProperty(GlobalElementComponentModelModelProperty.class))
        .thenReturn(of(globalElementComponentModelProperty));
    return extensionModel;
  }

  private static DefaultObjectType createObjectType(boolean isInstantiable, String className) {
    Map<Class<? extends TypeAnnotation>, TypeAnnotation> extensions = new HashMap<>();
    extensions.put(TypeIdAnnotation.class, new TypeIdAnnotation(className));
    extensions.put(ClassInformationAnnotation.class,
                   new ClassInformationAnnotation(className, false, false, isInstantiable,
                                                  false, false, emptyList(),
                                                  null, emptyList(), false));

    return new DefaultObjectType(emptySet(), false, null, null, extensions);
  }

}
