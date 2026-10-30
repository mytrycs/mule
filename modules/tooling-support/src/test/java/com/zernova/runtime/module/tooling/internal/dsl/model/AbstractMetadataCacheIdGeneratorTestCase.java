/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.tooling.internal.dsl.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static com.zernova.runtime.api.component.TypedComponentIdentifier.ComponentType.CONFIG;
import static com.zernova.runtime.ast.api.util.ZernovaAstUtils.createComponentParameterizationFromComponentAst;
import static com.zernova.runtime.app.declaration.api.component.location.Location.builderFromStringRepresentation;
import static com.zernova.runtime.config.api.dsl.ArtifactDeclarationUtils.toArtifactast;
import static java.util.stream.Collectors.toList;

import com.zernova.runtime.api.component.location.Location;
import com.zernova.runtime.api.dsl.DslResolvingContext;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.app.declaration.api.ArtifactDeclaration;
import com.zernova.runtime.app.declaration.api.ElementDeclaration;
import com.zernova.runtime.app.declaration.api.fluent.ElementDeclarer;
import com.zernova.runtime.ast.api.ArtifactAst;
import com.zernova.runtime.ast.api.ComponentAst;
import com.zernova.runtime.config.api.dsl.model.DslElementModelFactory;
import com.zernova.runtime.metadata.api.cache.ConfigurationMetadataCacheIdGenerator;
import com.zernova.runtime.metadata.internal.cache.ComponentAstBasedMetadataCacheIdGenerator;
import com.zernova.runtime.metadata.internal.cache.ComponentParameterizationBasedMetadataCacheIdGenerator;
import com.zernova.runtime.metadata.internal.cache.DslElementBasedMetadataCacheIdGenerator;
import com.zernova.runtime.module.tooling.internal.metadata.model.DeclarationBasedMetadataCacheIdGenerator;
import com.zernova.runtime.module.tooling.internal.metadata.model.ModelBasedMetadataCacheIdGeneratorFactory;
import com.zernova.runtime.api.parameterization.ComponentParameterization;
import com.zernova.runtime.metadata.internal.cache.AstConfigurationMetadataCacheIdGenerator;
import com.zernova.runtime.core.api.extension.provider.ZernovaExtensionModelProvider;
import com.zernova.runtime.metadata.api.locator.ComponentLocator;
import com.zernova.runtime.metadata.api.cache.MetadataCacheId;
import com.zernova.runtime.metadata.api.cache.MetadataCacheIdGenerator;
import com.zernova.runtime.metadata.api.dsl.DslElementModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;

import com.google.common.collect.ImmutableSet;

public abstract class AbstractMetadataCacheIdGeneratorTestCase extends AbstractDslModelTestCase {

  protected Set<ExtensionModel> extensions;
  protected DslResolvingContext dslResolvingContext;
  protected ElementDeclarer declarer;
  protected DslElementModelFactory dslFactory;

  @Before
  public void setUp() throws Exception {
    extensions = ImmutableSet.<ExtensionModel>builder()
        .add(ZernovaExtensionModelProvider.getExtensionModel())
        .add(mockExtension)
        .build();

    dslResolvingContext = DslResolvingContext.getDefault(extensions);
    declarer = ElementDeclarer.forExtension(EXTENSION_NAME);
    dslFactory = DslElementModelFactory.getDefault(dslResolvingContext);
  }

  protected MetadataCacheId getIdForComponentOutputMetadata(ArtifactDeclaration declaration, String location) throws Exception {
    ArtifactAst app = loadApplicationModel(declaration);
    ComponentAst component = new Locator(app)
        .get(Location.builderFromStringRepresentation(location).build())
        .get();

    ElementDeclaration elementDeclaration = declaration.findElement(builderFromStringRepresentation(location).build()).get();

    MetadataCacheId astBasedId = createAstBasedGenerator(app).getIdForComponentOutputMetadata(component).get();
    MetadataCacheId modelBasedId = createModelBasedGenerator(app).getIdForComponentOutputMetadata(component).get();
    MetadataCacheId dslBasedId =
        createDslBasedGenerator(app, dslFactory).getIdForComponentOutputMetadata(dslFactory.create(component).get()).get();
    MetadataCacheId declarationBasedId =
        createDeclarationBasedGenerator(declaration).getIdForComponentOutputMetadata(elementDeclaration).get();
    MetadataCacheId parameterizationBasedId = createComponentParameterizationBasedGenerator(app)
        .getIdForComponentOutputMetadata(createComponentParameterizationFromComponentAst(component)).get();

    assertThat(parameterizationBasedId, equalTo(modelBasedId));
    assertThat(modelBasedId, equalTo(dslBasedId));
    assertThat(dslBasedId, equalTo(declarationBasedId));
    assertThat(declarationBasedId, equalTo(astBasedId));

    // Any should be fine
    return modelBasedId;
  }

  protected MetadataCacheId getIdForComponentAttributesMetadata(ArtifactDeclaration declaration, String location)
      throws Exception {
    ArtifactAst app = loadApplicationModel(declaration);
    ComponentAst component = new Locator(app)
        .get(Location.builderFromStringRepresentation(location).build())
        .get();

    ElementDeclaration elementDeclaration = declaration.findElement(builderFromStringRepresentation(location).build()).get();

    MetadataCacheId astBasedId = createAstBasedGenerator(app).getIdForComponentAttributesMetadata(component).get();
    MetadataCacheId modelBasedId = createModelBasedGenerator(app).getIdForComponentAttributesMetadata(component).get();
    MetadataCacheId dslBasedId =
        createDslBasedGenerator(app, dslFactory).getIdForComponentAttributesMetadata(dslFactory.create(component).get()).get();
    MetadataCacheId declarationBasedId =
        createDeclarationBasedGenerator(declaration).getIdForComponentAttributesMetadata(elementDeclaration).get();
    MetadataCacheId parameterizationBasedId = createComponentParameterizationBasedGenerator(app)
        .getIdForComponentAttributesMetadata(createComponentParameterizationFromComponentAst(component)).get();

    assertThat(parameterizationBasedId, equalTo(modelBasedId));
    assertThat(modelBasedId, equalTo(dslBasedId));
    assertThat(dslBasedId, equalTo(declarationBasedId));
    assertThat(declarationBasedId, equalTo(astBasedId));

    // Any should be fine
    return modelBasedId;
  }

  protected MetadataCacheId getIdForComponentInputMetadata(ArtifactDeclaration declaration, String location, String parameterName)
      throws Exception {
    ArtifactAst app = loadApplicationModel(declaration);
    ComponentAst component = new Locator(app)
        .get(Location.builderFromStringRepresentation(location).build())
        .get();

    ElementDeclaration elementDeclaration = declaration.findElement(builderFromStringRepresentation(location).build()).get();

    MetadataCacheId astBasedId = createAstBasedGenerator(app).getIdForComponentInputMetadata(component, parameterName).get();
    MetadataCacheId modelBasedId = createModelBasedGenerator(app).getIdForComponentInputMetadata(component, parameterName).get();
    MetadataCacheId dslBasedId = createDslBasedGenerator(app, dslFactory)
        .getIdForComponentInputMetadata(dslFactory.create(component).get(), parameterName).get();
    MetadataCacheId declarationBasedId =
        createDeclarationBasedGenerator(declaration).getIdForComponentInputMetadata(elementDeclaration, parameterName).get();
    MetadataCacheId parameterizationBasedId = createComponentParameterizationBasedGenerator(app)
        .getIdForComponentInputMetadata(createComponentParameterizationFromComponentAst(component), parameterName).get();

    assertThat(parameterizationBasedId, equalTo(modelBasedId));
    assertThat(modelBasedId, equalTo(dslBasedId));
    assertThat(dslBasedId, equalTo(declarationBasedId));
    assertThat(declarationBasedId, equalTo(astBasedId));

    // Any should be fine
    return modelBasedId;
  }

  protected MetadataCacheId getIdForComponentMetadata(ArtifactDeclaration declaration, String location) throws Exception {
    ArtifactAst app = loadApplicationModel(declaration);
    ComponentAst component = new Locator(app)
        .get(Location.builderFromStringRepresentation(location).build())
        .get();

    ElementDeclaration elementDeclaration = declaration.findElement(builderFromStringRepresentation(location).build()).get();

    MetadataCacheId astBasedId = createAstBasedGenerator(app).getIdForComponentMetadata(component).get();
    MetadataCacheId modelBasedId = createModelBasedGenerator(app).getIdForComponentMetadata(component).get();
    MetadataCacheId dslBasedId =
        createDslBasedGenerator(app, dslFactory).getIdForComponentMetadata(dslFactory.create(component).get()).get();
    MetadataCacheId declarationBasedId =
        createDeclarationBasedGenerator(declaration).getIdForComponentMetadata(elementDeclaration).get();
    MetadataCacheId parameterizationBasedId = createComponentParameterizationBasedGenerator(app)
        .getIdForComponentMetadata(createComponentParameterizationFromComponentAst(component)).get();

    assertThat(parameterizationBasedId, equalTo(modelBasedId));
    assertThat(modelBasedId, equalTo(dslBasedId));
    assertThat(dslBasedId, equalTo(declarationBasedId));
    assertThat(declarationBasedId, equalTo(astBasedId));

    // Any should be fine
    return dslBasedId;
  }

  protected MetadataCacheId getIdForMetadataKeys(ArtifactDeclaration declaration, String location) throws Exception {
    ArtifactAst app = loadApplicationModel(declaration);
    ComponentAst component = new Locator(app)
        .get(Location.builderFromStringRepresentation(location).build())
        .get();

    ElementDeclaration elementDeclaration = declaration.findElement(builderFromStringRepresentation(location).build()).get();

    MetadataCacheId astBasedId = createAstBasedGenerator(app).getIdForMetadataKeys(component).get();
    MetadataCacheId modelBasedId = createModelBasedGenerator(app).getIdForMetadataKeys(component).get();
    MetadataCacheId dslBasedId =
        createDslBasedGenerator(app, dslFactory).getIdForMetadataKeys(dslFactory.create(component).get()).get();
    MetadataCacheId declarationBasedId =
        createDeclarationBasedGenerator(declaration).getIdForMetadataKeys(elementDeclaration).get();
    MetadataCacheId parameterizationBasedId = createComponentParameterizationBasedGenerator(app)
        .getIdForMetadataKeys(createComponentParameterizationFromComponentAst(component)).get();

    assertThat(parameterizationBasedId, equalTo(modelBasedId));
    assertThat(modelBasedId, equalTo(dslBasedId));
    assertThat(dslBasedId, equalTo(declarationBasedId));
    assertThat(declarationBasedId, equalTo(astBasedId));

    // Any should be fine
    return modelBasedId;
  }

  protected MetadataCacheId getIdForGlobalMetadata(ArtifactDeclaration declaration, String location) throws Exception {
    ArtifactAst app = loadApplicationModel(declaration);
    ComponentAst component = new Locator(app)
        .get(Location.builderFromStringRepresentation(location).build())
        .get();

    ElementDeclaration elementDeclaration = declaration.findElement(builderFromStringRepresentation(location).build()).get();

    MetadataCacheId astBasedId = createAstBasedGenerator(app).getIdForGlobalMetadata(component).get();
    MetadataCacheId modelBasedId = createModelBasedGenerator(app).getIdForGlobalMetadata(component).get();
    MetadataCacheId dslBasedId =
        createDslBasedGenerator(app, dslFactory).getIdForGlobalMetadata(dslFactory.create(component).get()).get();
    MetadataCacheId declarationBasedId =
        createDeclarationBasedGenerator(declaration).getIdForGlobalMetadata(elementDeclaration).get();
    MetadataCacheId parameterizationBasedId = createComponentParameterizationBasedGenerator(app)
        .getIdForGlobalMetadata(createComponentParameterizationFromComponentAst(component)).get();

    assertThat(parameterizationBasedId, equalTo(modelBasedId));
    assertThat(modelBasedId, equalTo(dslBasedId));
    assertThat(dslBasedId, equalTo(declarationBasedId));
    assertThat(declarationBasedId, equalTo(astBasedId));

    // Any should be fine
    return modelBasedId;
  }

  private MetadataCacheIdGenerator<ComponentAst> createAstBasedGenerator(ArtifactAst app) {
    return new ComponentAstBasedMetadataCacheIdGenerator(new Locator(app));
  }

  private MetadataCacheIdGenerator<ComponentAst> createModelBasedGenerator(ArtifactAst app) {
    return new ModelBasedMetadataCacheIdGeneratorFactory()
        .create(dslResolvingContext, new ModelBasedTypeMetadataCacheKeyGeneratorTestCase.Locator(app));
  }

  private MetadataCacheIdGenerator<DslElementModel<?>> createDslBasedGenerator(ArtifactAst app,
                                                                               DslElementModelFactory factory) {
    ComponentLocator<ComponentAst> astLocator = new ModelBasedTypeMetadataCacheKeyGeneratorTestCase.Locator(app);
    ComponentLocator<DslElementModel<?>> dslLocator = l -> astLocator.get(l).map(e -> factory.create(e).orElse(null));
    return new DslElementBasedMetadataCacheIdGenerator(dslLocator);
  }

  private MetadataCacheIdGenerator<ComponentParameterization<?>> createComponentParameterizationBasedGenerator(ArtifactAst app) {
    ConfigurationMetadataCacheIdGenerator configGenerator = new AstConfigurationMetadataCacheIdGenerator();
    configGenerator.addConfigurations(app.topLevelComponentsStream()
        .filter(potentialConfig -> potentialConfig.getComponentType().equals(CONFIG)).collect(toList()));
    return new ComponentParameterizationBasedMetadataCacheIdGenerator(configGenerator);
  }

  private MetadataCacheIdGenerator<ElementDeclaration> createDeclarationBasedGenerator(ArtifactDeclaration app) {
    ComponentLocator<ElementDeclaration> declarationLocator =
        l -> app.findElement(builderFromStringRepresentation(l.toString()).build());
    return new DeclarationBasedMetadataCacheIdGenerator(dslResolvingContext, declarationLocator);
  }

  protected ArtifactAst loadApplicationModel(ArtifactDeclaration declaration) throws Exception {
    return toArtifactast(declaration, extensions);
  }

  protected static class Locator implements ComponentLocator<ComponentAst> {

    private final Map<Location, ComponentAst> components = new HashMap<>();

    Locator(ArtifactAst app) {
      app.topLevelComponentsStream().forEach(this::addComponent);
    }

    @Override
    public Optional<ComponentAst> get(Location location) {
      return Optional.ofNullable(components.get(location));
    }

    private Location getLocation(ComponentAst component) {
      return Location.builderFromStringRepresentation(component.getLocation().getLocation()).build();
    }

    private void addComponent(ComponentAst component) {
      components.put(getLocation(component), component);
      component.directChildrenStream().forEach(this::addComponent);
    }

  }

}
