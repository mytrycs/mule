/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.loader.validator;

import static com.zernova.runtime.extension.api.util.ExtensionModelUtils.isRouter;
import static com.zernova.runtime.extension.api.util.ExtensionModelUtils.isScope;
import static com.zernova.runtime.extension.api.util.NameUtils.getComponentModelTypeName;
import static com.zernova.runtime.extension.internal.util.ExtensionValidationUtils.validateNoInlineParameters;
import static com.zernova.runtime.extension.privileged.util.ComponentDeclarationUtils.isConnectionProvisioningRequired;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import com.zernova.runtime.api.meta.model.ComponentModel;
import com.zernova.runtime.api.meta.model.ComposableModel;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.connection.HasConnectionProviderModels;
import com.zernova.runtime.api.meta.model.construct.ConstructModel;
import com.zernova.runtime.api.meta.model.construct.HasConstructModels;
import com.zernova.runtime.api.meta.model.error.ErrorModel;
import com.zernova.runtime.api.meta.model.nested.NestableElementModelVisitor;
import com.zernova.runtime.api.meta.model.nested.NestedChainModel;
import com.zernova.runtime.api.meta.model.nested.NestedComponentModel;
import com.zernova.runtime.api.meta.model.nested.NestedRouteModel;
import com.zernova.runtime.api.meta.model.operation.HasOperationModels;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.meta.model.util.ExtensionWalker;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.DslSyntaxResolver;
import com.zernova.runtime.extension.api.dsl.syntax.resolver.SingleExtensionImportTypesStrategy;
import com.zernova.runtime.extension.api.loader.ExtensionModelValidator;
import com.zernova.runtime.extension.api.loader.Problem;
import com.zernova.runtime.extension.api.loader.ProblemsReporter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Validates {@link ParameterModel parameters} with rules specific to {@link OperationModel}.
 *
 * @since 1.0
 */
public final class OperationModelValidator implements ExtensionModelValidator {

  @Override
  public void validate(ExtensionModel extensionModel, ProblemsReporter problemsReporter) {
    new ValidatorDelegate().validate(extensionModel,
                                     DslSyntaxResolver.getDefault(extensionModel, new SingleExtensionImportTypesStrategy()),
                                     problemsReporter);
  }

  @Override
  public void validate(ExtensionModel extensionModel, DslSyntaxResolver syntaxResolver, ProblemsReporter problemsReporter) {
    new ValidatorDelegate().validate(extensionModel, syntaxResolver, problemsReporter);
  }

  private static class ValidatorDelegate {

    private ProblemsReporter problemsReporter;
    private DslSyntaxResolver dsl;

    void validate(ExtensionModel extensionModel, DslSyntaxResolver syntaxResolver, ProblemsReporter problemsReporter) {
      this.problemsReporter = problemsReporter;
      this.dsl = syntaxResolver;
      final boolean hasGlobalConnectionProviders = !extensionModel.getConnectionProviders().isEmpty();
      final boolean extensionWithoutErrors = extensionModel.getErrorModels().isEmpty();

      new ExtensionWalker() {

        @Override
        protected void onConstruct(HasConstructModels owner, ConstructModel model) {
          validateErrors(extensionModel, model, problemsReporter);
          if (isScope(model)) {
            validateScope(model);
          } else if (isRouter(model)) {
            validateRouter(model);
          }
        }

        @Override
        protected void onOperation(HasOperationModels owner, OperationModel model) {
          validateErrors(extensionModel, model, problemsReporter);
          validateOutput(model);
          validateConnection(owner, model, hasGlobalConnectionProviders);

          if (isScope(model)) {
            validateScope(model);
          } else if (isRouter(model)) {
            validateRouter(model);
          }
        }

        private void validateErrors(ExtensionModel extensionModel, ComponentModel componentModel,
                                    ProblemsReporter problemsReporter) {
          if (extensionWithoutErrors && !componentModel.getErrorModels().isEmpty()) {
            problemsReporter.addError(new Problem(componentModel,
                                                  format("%s '%s' declares error types but the Extension declares none",
                                                         getComponentModelTypeName(componentModel),
                                                         componentModel.getName())));
          }

          List<ErrorModel> undeclared = componentModel.getErrorModels().stream()
              .filter(error -> !extensionModel.getErrorModels().contains(error))
              .collect(toList());

          if (!undeclared.isEmpty()) {
            problemsReporter.addError(new Problem(componentModel,
                                                  format("%s '%s' declares error types which are not defined in the extension. Offending errors are [%s]",
                                                         getComponentModelTypeName(componentModel),
                                                         componentModel.getName(),
                                                         undeclared.stream().map(ErrorModel::getType).collect(joining(", ")))));
          }
        }
      }.walk(extensionModel);
    }

    private void validateScope(ComponentModel model) {
      validateSingleNestedChain(model, model, "Scope");
    }

    private void validateRouter(ComponentModel model) {
      model.getNestedComponents().stream()
          .forEach(nested -> nested.accept(new NestableElementModelVisitor() {

            @Override
            public void visit(NestedComponentModel component) {
              // Not supported yet
            }

            @Override
            public void visit(NestedChainModel component) {
              problemsReporter.addError(new Problem(model, "A Chain component was found along with one or more Route components. "
                  + "Mixed content is not allowed, either use all Routes or a single Chain"));
            }

            @Override
            public void visit(NestedRouteModel route) {
              validateRoute(route, model);
            }
          }));
    }

    private void validateRoute(NestedRouteModel route, ComponentModel model) {
      validateNoInlineParameters(route, "Route", problemsReporter, dsl);
      validateSingleNestedChain(route, model, "Route");
    }

    private void validateSingleNestedChain(ComposableModel container, ComponentModel model, String kind) {
      AtomicInteger numberOfChains = new AtomicInteger(0);
      container.getNestedComponents().forEach(nestedComponent -> nestedComponent.accept(new NestableElementModelVisitor() {

        @Override
        public void visit(NestedComponentModel component) {
          // Not supported yet
        }

        @Override
        public void visit(NestedChainModel component) {
          numberOfChains.incrementAndGet();
        }

        @Override
        public void visit(NestedRouteModel nestedRoute) {
          problemsReporter.addError(new Problem(model, format("Routes are not supported inside %s, but found Route"
              + " [%s] nested as part of the %s [%s]",
                                                              kind, nestedRoute.getName(), kind, container.getName())));
        }
      }));

      if (numberOfChains.get() == 0) {
        problemsReporter
            .addError(new Problem(model,
                                  format("A Chain component is required as part of the %s [%s] in operation [%s]",
                                         kind, container.getName(), model.getName())));
      } else if (numberOfChains.get() > 1) {
        problemsReporter.addError(new Problem(model, format("Only a single Chain component is supported as part of the %s [%s], "
            + "remove redundant declarations", kind, container.getName())));
      }
    }

    private void validateConnection(HasOperationModels owner, OperationModel model,
                                    boolean hasGlobalConnectionProviders) {
      if (isConnectionProvisioningRequired(model)) {
        boolean connectable;
        if (owner instanceof HasConnectionProviderModels) {
          connectable =
              hasGlobalConnectionProviders || !((HasConnectionProviderModels) owner).getConnectionProviders().isEmpty();
        } else {
          connectable = hasGlobalConnectionProviders;
        }

        if (!connectable) {
          problemsReporter
              .addError(new Problem(model, format("Operation '%s' requires a connection but no connection provider was "
                  + "defined at either the configuration or extension level",
                                                  model.getName())));
        }
      }
    }

    private void validateOutput(OperationModel model) {
      if (model.getOutput().getType() == null) {
        problemsReporter.addError(new Problem(model, format("Operation '%s' does not define an output type", model.getName())));
      }

      if (model.getOutputAttributes().getType() == null) {
        problemsReporter
            .addError(new Problem(model, format("Operation '%s' does not define an attributes output type", model.getName())));
      }
    }
  }
}
