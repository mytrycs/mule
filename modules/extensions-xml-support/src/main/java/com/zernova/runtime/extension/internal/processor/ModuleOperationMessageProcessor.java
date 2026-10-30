/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.processor;

import static com.zernova.metadata.api.model.MetadataFormat.JAVA;
import static com.zernova.runtime.api.config.ZernovaRuntimeFeature.ENABLE_XML_SDK_MDC_RESET;
import static com.zernova.runtime.api.el.BindingContextUtils.NULL_BINDING_CONTEXT;
import static com.zernova.runtime.api.el.BindingContextUtils.getTargetBindingContext;
import static com.zernova.runtime.api.meta.model.parameter.ParameterRole.CONTENT;
import static com.zernova.runtime.api.meta.model.parameter.ParameterRole.PRIMARY_CONTENT;
import static com.zernova.runtime.api.notification.EnrichedNotificationInfo.createInfo;
import static com.zernova.runtime.api.util.ZernovaSystemProperties.ENABLE_DYNAMIC_CONFIG_REF_PROPERTY;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.disposeIfNeeded;
import static com.zernova.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import static com.zernova.runtime.core.internal.el.ExpressionLanguageUtils.compile;
import static com.zernova.runtime.core.internal.message.InternalMessage.builder;
import static com.zernova.runtime.core.internal.util.rx.RxUtils.KEY_ON_NEXT_ERROR_STRATEGY;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.applyWithChildContext;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.buildNewChainWithListOfProcessors;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.getProcessingStrategy;
import static com.zernova.runtime.core.privileged.processor.MessageProcessors.processToApply;
import static com.zernova.runtime.extension.api.ExtensionConstants.TARGET_PARAMETER_NAME;
import static com.zernova.runtime.extension.api.ExtensionConstants.TARGET_VALUE_PARAMETER_NAME;
import static com.zernova.runtime.extension.internal.ast.MacroExpansionModuleModel.MODULE_CONFIG_GLOBAL_ELEMENT_NAME;
import static com.zernova.runtime.extension.internal.ast.MacroExpansionModuleModel.MODULE_CONNECTION_GLOBAL_ELEMENT_NAME;
import static com.zernova.runtime.extension.internal.ast.MacroExpansionModuleModel.MODULE_OPERATION_CONFIG_REF;
import static com.zernova.runtime.module.extension.internal.util.ZernovaExtensionUtils.isExpression;

import static java.lang.Boolean.getBoolean;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import static org.slf4j.LoggerFactory.getLogger;
import static reactor.core.publisher.Flux.from;

import com.zernova.metadata.api.model.MetadataType;
import com.zernova.metadata.api.utils.MetadataTypeUtils;
import com.zernova.runtime.api.component.Component;
import com.zernova.runtime.api.component.ComponentIdentifier;
import com.zernova.runtime.api.component.location.ComponentLocation;
import com.zernova.runtime.api.config.FeatureFlaggingService;
import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.api.el.CompiledExpression;
import com.zernova.runtime.api.el.ExpressionLanguageSession;
import com.zernova.runtime.api.event.EventContext;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.message.Error;
import com.zernova.runtime.api.message.ErrorType;
import com.zernova.runtime.api.meta.model.ExtensionModel;
import com.zernova.runtime.api.meta.model.config.ConfigurationModel;
import com.zernova.runtime.api.meta.model.operation.OperationModel;
import com.zernova.runtime.api.meta.model.parameter.ParameterModel;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.notification.EnrichedNotificationInfo;
import com.zernova.runtime.api.util.Pair;
import com.zernova.runtime.core.api.context.notification.FlowStackElement;
import com.zernova.runtime.core.api.el.ExpressionManager;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.exception.BaseExceptionHandler;
import com.zernova.runtime.core.api.execution.ExceptionContextProvider;
import com.zernova.runtime.core.api.extension.ExtensionManager;
import com.zernova.runtime.core.api.processor.AbstractMessageProcessorOwner;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.processor.strategy.ProcessingStrategy;
import com.zernova.runtime.core.internal.event.InternalEvent;
import com.zernova.runtime.core.internal.exception.EnrichedErrorMapping;
import com.zernova.runtime.core.internal.exception.ErrorMappingsAware;
import com.zernova.runtime.core.internal.message.ErrorBuilder;
import com.zernova.runtime.core.privileged.event.BaseEventContext;
import com.zernova.runtime.core.privileged.event.DefaultFlowCallStack;
import com.zernova.runtime.core.privileged.event.PrivilegedEvent;
import com.zernova.runtime.core.privileged.exception.MessagingException;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import com.zernova.runtime.extension.api.runtime.config.ConfigurationProvider;
import com.zernova.runtime.extension.internal.config.dsl.XmlSdkConfigurationProvider;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolver;
import com.zernova.runtime.module.extension.api.runtime.resolver.ValueResolvingContext;
import com.zernova.runtime.module.extension.internal.runtime.resolver.ConfigurationProviderValueResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.inject.Inject;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;

/**
 * Creates a chain for any operation, where it parameterizes two type of values (parameter and property) to the inner processors
 * through the {@link CoreEvent}.
 * <p>
 * Both parameter and property could be simple literals or expressions that will be evaluated before passing the new
 * {@link CoreEvent} to the child processors.
 * <p>
 * Taking the following sample where the current event is processed using {@link ModuleOperationProcessorChain#apply(Publisher)},
 * has a variable under "person" with a value of "stranger!", the result of executing the above processor will be "howdy
 * stranger!":
 *
 * <pre>
 *  <module-operation-chain moduleName="a-module-name" moduleOperation="an-operation-name">
 *    <module-operation-properties/>
 *    <module-operation-parameters>
 *      <module-operation-parameter-entry value="howdy" key="value1"/>
 *      <module-operation-parameter-entry value="#[vars.person]" key="value2"/>
 *    </module-operation-parameters>
 *    <set-payload value="#[param.value1 ++ param.value2]"/>
 * </module-operation-chain>
 * </pre>
 */
public class ModuleOperationMessageProcessor extends AbstractMessageProcessorOwner implements Processor, ErrorMappingsAware {

  private static final Logger LOGGER = getLogger(ModuleOperationMessageProcessor.class);

  private static final String ORIGINAL_EVENT_KEY = "mule.xmlSdk.originalEvent";

  @Inject
  private ExpressionManager expressionManager;

  @Inject
  private Collection<ExceptionContextProvider> exceptionContextProviders;

  private FeatureFlaggingService featureFlaggingService;
  private final ExtensionManager extensionManager;
  private final OperationModel operationModel;

  private MessageProcessorChain nestedChain;
  private List<Processor> processors;

  private final List<ParameterModel> allProperties;
  private final Map<String, Pair<Object, MetadataType>> properties;
  private final Optional<ValueResolver<ConfigurationProvider>> configurationProviderResolver;
  private final Map<String, Pair<Object, MetadataType>> parameters;
  private final boolean returnsVoid;
  private final Optional<String> target;
  private final String targetValue;
  private final List<EnrichedErrorMapping> errorMappings;
  private CompiledExpression targetValueExpression;
  private final boolean isDynamicConfigRefEnabled = getBoolean(ENABLE_DYNAMIC_CONFIG_REF_PROPERTY);

  public ModuleOperationMessageProcessor(Map<String, Object> parameters,
                                         List<EnrichedErrorMapping> errorMappings,
                                         ExtensionManager extensionManager, ExtensionModel extensionModel,
                                         OperationModel operationModel) {
    this.extensionManager = extensionManager;

    allProperties = getAllProperties(extensionModel);
    this.properties = parseParameters(getProperties(parameters), allProperties);
    this.parameters = parseParameters(parameters, operationModel.getAllParameterModels());
    this.returnsVoid = MetadataTypeUtils.isVoid(operationModel.getOutput().getType());
    this.target = parameters.containsKey(TARGET_PARAMETER_NAME) ? of((String) parameters.remove(TARGET_PARAMETER_NAME)) : empty();
    this.targetValue = (String) parameters.remove(TARGET_VALUE_PARAMETER_NAME);
    this.errorMappings = errorMappings;
    this.configurationProviderResolver = getConfigurationProviderResolver(parameters);
    this.operationModel = operationModel;
  }

  public Map<String, String> getProperties(Map<String, Object> parameters) {
    // `properties` in the scope of a Xml-Sdk operation means the parameters of the config.
    String configRefParameter = (String) parameters.get(MODULE_OPERATION_CONFIG_REF);
    if (configRefParameter != null && !isExpression(configRefParameter)) {
      return createPropertiesFromConfigName(configRefParameter);
    }

    return emptyMap();
  }

  private Map<String, String> createPropertiesFromConfigName(final String configName) {
    return extensionManager.getConfigurationProvider(configName)
        .filter(cp -> cp instanceof XmlSdkConfigurationProvider)
        .map(cp -> ((XmlSdkConfigurationProvider) cp).getParameters())
        .orElse(emptyMap());
  }

  /**
   * Plains the complete list of configurations and connections for the parameterized {@link ExtensionModel}
   *
   * @param extensionModel looks for all the parameters of the configuration and connection.
   * @return a list of {@link ParameterModel} that will not repeat their {@link ParameterModel#getName()}s.
   */
  private List<ParameterModel> getAllProperties(ExtensionModel extensionModel) {
    List<ParameterModel> result = new ArrayList<>();
    extensionModel.getConfigurationModel(MODULE_CONFIG_GLOBAL_ELEMENT_NAME).ifPresent(configurationModel -> {
      result.addAll(configurationModel.getAllParameterModels());
      configurationModel.getConnectionProviderModel(MODULE_CONNECTION_GLOBAL_ELEMENT_NAME)
          .ifPresent(connectionProviderModel -> result.addAll(connectionProviderModel.getAllParameterModels()));
    });
    return result;
  }

  /**
   * To properly feed the {@link ExpressionManager#evaluate(String, DataType, BindingContext, CoreEvent)} we need to store the
   * {@link MetadataType} per parameter, so that the {@link DataType} can be generated.
   *
   * @param parameters      list of parameters taken from the XML
   * @param parameterModels collection of elements taken from the matching {@link ExtensionModel}
   * @return a collection of parameters to be later consumed in {@link #getEvaluatedValue(CoreEvent, String, MetadataType)}
   */
  private Map<String, Pair<Object, MetadataType>> parseParameters(Map<String, ?> parameters,
                                                                  List<ParameterModel> parameterModels) {
    final Map<String, Pair<Object, MetadataType>> result = new HashMap<>();

    for (ParameterModel parameterModel : parameterModels) {
      final String parameterName = parameterModel.getName();
      if (parameterName.equals(TARGET_PARAMETER_NAME) || parameterName.equals(TARGET_VALUE_PARAMETER_NAME)) {
        // nothing to do, these are not forwarded to the event for the inner chain
      } else if (parameters.containsKey(parameterName)) {
        result.put(parameterName, new Pair<>(getXmlParameterValue(parameters, parameterName), parameterModel.getType()));
      } else if (parameterModel.getDefaultValue() != null
          && (PRIMARY_CONTENT.equals(parameterModel.getRole())
              || CONTENT.equals(parameterModel.getRole()))) {
        result.put(parameterName, new Pair<>(parameterModel.getDefaultValue(), parameterModel.getType()));
      }
    }
    return result;
  }

  private Object getXmlParameterValue(Map<String, ?> parameters, String parameterName) {
    Object xmlValue = parameters.get(parameterName);
    return (xmlValue instanceof String ? ((String) xmlValue).trim() : xmlValue);
  }

  @Override
  public CoreEvent process(final CoreEvent event) throws ZernovaException {
    return processToApply(event, this);
  }

  /**
   * Given an {@code event}, it will consume from it ONLY the defined properties and parameters that were set when initializing
   * this class to provide scoping for the inner list of processors.
   */
  @Override
  public Publisher<CoreEvent> apply(Publisher<CoreEvent> publisher) {
    final String localStrategyCtxKey = "mule.xmlSdk." + getLocation().getLocation() + ".reactor.onNextError.localStrategy";

    return from(publisher)
        .map(this::createEventWithParameters)
        // 2. Restore the error handler, overriding the last one set by the inner chain. This one being set again is the one that
        // must be used for handling any errors in the mapper above.
        .contextWrite(ctx -> ctx.getOrEmpty(localStrategyCtxKey)
            .map(localErrorStr -> ctx.put(KEY_ON_NEXT_ERROR_STRATEGY, localErrorStr))
            .orElse(ctx))
        .transformDeferred(eventPub -> applyWithChildContext(eventPub,
                                                             p -> from(p)
                                                                 .doOnNext(this::pushFlowStackEntry)
                                                                 .transformDeferred(nestedChain)
                                                                 .doOnNext(event -> ((DefaultFlowCallStack) event
                                                                     .getFlowCallStack())
                                                                         .pop()),
                                                             ofNullable(getLocation()),
                                                             errorHandler()))
        // 1. Store the current error handler into the subscription context, so it can be retrieved later
        .contextWrite(ctx -> ctx.getOrEmpty(KEY_ON_NEXT_ERROR_STRATEGY)
            .map(onNextErrorStr -> ctx.put(localStrategyCtxKey, onNextErrorStr))
            .orElse(ctx))
        .map(eventResult -> processResult(getInternalParameter(ORIGINAL_EVENT_KEY, eventResult), eventResult));
  }

  private void pushFlowStackEntry(CoreEvent event) {
    final DefaultFlowCallStack flowCallStack = (DefaultFlowCallStack) event.getFlowCallStack();

    flowCallStack.push(createFlowStackEntry(flowCallStack.peek()));
  }

  private FlowStackElement createFlowStackEntry(FlowStackElement top) {
    final ComponentIdentifier identifier = getIdentifier();
    if (identifier.getNamespace() == null || "tns".equals(identifier.getNamespace())) {
      final String[] peekedWithNamespace = top.getFlowName().split("\\:");
      String peekedNamespace = peekedWithNamespace[0];

      return new FlowStackElement(peekedNamespace + ":" + identifier.getName(), identifier, null, getLocation(),
                                  getAnnotations());
    } else {
      return new FlowStackElement(identifier.getNamespace() + ":" + identifier.getName(), identifier, null, getLocation(),
                                  getAnnotations());
    }
  }

  private BaseExceptionHandler errorHandler() {
    return new BaseExceptionHandler() {

      @Override
      protected void onError(Exception exception) {
        final MessagingException me = (MessagingException) exception;
        final CoreEvent event = me.getEvent();

        // If any of the internals of an <operation/> throws an {@link ZernovaException}, this method will be responsible of altering
        // the current location of that exception will change to target the call invocation of the smart connector's operation.
        // <p>
        // By doing so, later processing (such as {@link ZernovaException#getDetailedMessage()}) will keep digging for the prime
        // cause of the exception, which means the Mule application will <b>only see</b> the logs of the application's call to the
        // smart connector's <operation/>, rather than the internals of the smart connector's internals.
        EnrichedNotificationInfo notificationInfo = createInfo(event, me, null);
        for (ExceptionContextProvider exceptionContextProvider : exceptionContextProviders) {
          exceptionContextProvider.putContextInfo(me.getExceptionInfo(), notificationInfo, ModuleOperationMessageProcessor.this);
        }

        ((DefaultFlowCallStack) event.getFlowCallStack()).pop();
        handleSubChainException(me,
                                getInternalParameter(ORIGINAL_EVENT_KEY, event,
                                                     ((BaseEventContext) event.getContext()).getParentContext().get()));
      }

      /**
       * Unlike other {@link MessageProcessorChain MessageProcessorChains}, modules could contain error mappings that need to be
       * considered when resolving exceptions.
       */
      private void handleSubChainException(MessagingException messagingException, CoreEvent originalRequest) {
        final CoreEvent.Builder builder = CoreEvent.builder(messagingException.getEvent().getContext(), originalRequest)
            .error(messagingException.getEvent().getError().get());
        List<EnrichedErrorMapping> errorMappings = getErrorMappings();
        if (!errorMappings.isEmpty()) {
          Error error = messagingException.getEvent().getError().get();
          ErrorType errorType = error.getErrorType();
          ErrorType resolvedType = errorMappings.stream()
              .filter(m -> m.match(errorType))
              .findFirst()
              .map(EnrichedErrorMapping::getTarget)
              .orElse(errorType);
          if (!resolvedType.equals(errorType)) {
            builder.error(ErrorBuilder.builder(error).errorType(resolvedType).build());
          }
        }
        messagingException.setProcessedEvent(builder.build());
      }

      @Override
      public String toString() {
        return ModuleOperationMessageProcessor.class.getSimpleName() + ".errorHandler @ " + getLocation().getLocation();
      }
    };
  }

  private String getParameterId(String keyPrefix, CoreEvent event) {
    return getParameterId(keyPrefix, event.getContext());
  }

  private String getParameterId(String keyPrefix, final EventContext context) {
    return keyPrefix + context.getId();
  }

  private <T> T getInternalParameter(String keyPrefix, CoreEvent event) {
    return ((InternalEvent) event).getInternalParameter(getParameterId(keyPrefix, event));
  }

  private <T> T getInternalParameter(String keyPrefix, CoreEvent event, EventContext context) {
    return ((InternalEvent) event).getInternalParameter(getParameterId(keyPrefix, context));
  }

  private CoreEvent processResult(CoreEvent originalEvent, CoreEvent chainEvent) {
    if (!returnsVoid) {
      originalEvent = createNewEventFromJustMessage(originalEvent, chainEvent);
    }

    // Avoid leaking the MDC context outside the XML SDK operation by clearing it out using the chainEvent
    if (featureFlaggingService.isEnabled(ENABLE_XML_SDK_MDC_RESET)) {
      PrivilegedEvent.builder(chainEvent).clearLoggingVariables();
    }

    return originalEvent;
  }

  private CoreEvent createNewEventFromJustMessage(CoreEvent request, CoreEvent response) {
    final CoreEvent.Builder builder = CoreEvent.builder(request);
    if (target.isPresent()) {
      try (ExpressionLanguageSession session = expressionManager.openSession(getTargetBindingContext(response.getMessage()))) {
        builder.addVariable(target.get(), session.evaluate(targetValueExpression));
      }
    } else {
      builder.message(builder(response.getMessage()).build());
    }
    return builder.build();
  }

  private CoreEvent createEventWithParameters(CoreEvent event) {
    InternalEvent.Builder builder = InternalEvent.builder(event.getContext());
    builder.message(builder().nullValue().build());

    // The properties may not have been resolved yet if the config-ref was an expression, so we resolve them now
    Map<String, Pair<Object, MetadataType>> resolvedProperties = getResolvedProperties(event);

    // If this operation is called from an outer operation, we need to obtain the config from the previous caller in order to
    // populate the event variables as expected.
    TypedValue<?> configRef = event.getVariables().get(MODULE_OPERATION_CONFIG_REF);
    if (configRef != null) {
      builder.addVariable(MODULE_OPERATION_CONFIG_REF, configRef.getValue());

      if (properties.isEmpty()) {
        resolvedProperties = parseParameters(createPropertiesFromConfigName((String) configRef.getValue()), allProperties);
      }
    }

    addVariables(event, builder, resolvedProperties);
    addVariables(event, builder, parameters);

    builder.internalParameters(((InternalEvent) event).getInternalParameters());
    builder.addInternalParameter(getParameterId(ORIGINAL_EVENT_KEY, event), event);
    builder.securityContext(event.getSecurityContext());
    InternalEvent newEvent = builder.build();
    newEvent.setSourcePolicyContext(((InternalEvent) event).getSourcePolicyContext());
    return newEvent;
  }

  /**
   *
   * @param event A {@link CoreEvent} to resolve expression from.
   * @return The resolved properties for the processing of the given event. Note that if the config reference was not an
   *         expression, then the properties will not need any resolution at this point, we just return {@link #properties}.
   */
  private Map<String, Pair<Object, MetadataType>> getResolvedProperties(CoreEvent event) {
    if (configurationProviderResolver.isPresent()) {
      // Resolves the configuration provider and validates it
      ConfigurationProvider cp = resolveConfigurationProvider(event);
      validateConfigurationProvider(cp);

      // Gets the properties from the configuration provider
      if (cp instanceof XmlSdkConfigurationProvider) {
        return parseParameters(((XmlSdkConfigurationProvider) cp).getParameters(), allProperties);
      }
    }

    // No runtime resolution needed, we just return the properties resolved at the instantiation
    return properties;
  }

  /**
   * @param parameters The operation's parameters.
   * @return An optional {@link ConfigurationProvider} {@link ValueResolver} only present if the config-ref is an expression.
   */
  private Optional<ValueResolver<ConfigurationProvider>> getConfigurationProviderResolver(Map<String, Object> parameters) {
    if (!isDynamicConfigRefEnabled) {
      return empty();
    }

    String configRefParameter = (String) parameters.get(MODULE_OPERATION_CONFIG_REF);
    if (isExpression(configRefParameter)) {
      return of(new ConfigurationProviderValueResolver(configRefParameter));
    }

    return empty();
  }

  private ConfigurationProvider resolveConfigurationProvider(CoreEvent event) {
    ValueResolvingContext valueResolvingContext = ValueResolvingContext.builder(event)
        .withExpressionManager(expressionManager)
        .build();
    try {
      // We should have already checked if configurationProviderResolver is present before calling this method
      return configurationProviderResolver.get().resolve(valueResolvingContext);
    } catch (ZernovaException e) {
      throw new IllegalArgumentException(format("Error resolving configuration for component '%s'",
                                                getLocation().getRootContainerName()),
                                         e);
    }
  }

  private void validateConfigurationProvider(ConfigurationProvider configurationProvider) {
    ConfigurationModel configurationModel = configurationProvider.getConfigurationModel();
    if (!configurationModel.getOperationModel(operationModel.getName()).isPresent() &&
        !configurationProvider.getExtensionModel().getOperationModel(operationModel.getName()).isPresent()) {
      throw new IllegalArgumentException(format(
                                                "Root component '%s' defines an usage of operation '%s' which points to configuration '%s'. "
                                                    + "The selected config does not support that operation.",
                                                getLocation().getRootContainerName(), operationModel.getName(),
                                                configurationProvider.getName()));
    }
  }

  private void addVariables(CoreEvent event, CoreEvent.Builder builder,
                            Map<String, Pair<Object, MetadataType>> unevaluatedMap) {
    unevaluatedMap.entrySet().stream()
        .forEach(entry -> {
          final boolean isExpression = expressionManager.isExpression(entry.getValue().getFirst().toString());
          if (isExpression) {
            final TypedValue<?> evaluatedValue =
                getEvaluatedValue(event, entry.getValue().getFirst().toString(), entry.getValue().getSecond());
            builder.addVariable(entry.getKey(), evaluatedValue);
          } else {
            builder.addVariable(entry.getKey(), entry.getValue().getFirst());
          }
        });
  }

  private TypedValue<?> getEvaluatedValue(CoreEvent event, String value, MetadataType metadataType) {
    ComponentLocation headLocation;
    final Processor head = nestedChain.getMessageProcessors().get(0);
    headLocation = ((Component) head).getLocation();

    TypedValue<?> evaluatedResult;
    if (JAVA.equals(metadataType.getMetadataFormat())) {
      evaluatedResult = expressionManager.evaluate(value, event, headLocation);
    } else {
      final String mediaType = metadataType.getMetadataFormat().getValidMimeTypes().iterator().next();
      final DataType expectedOutputType =
          DataType.builder()
              .type(String.class)
              .mediaType(mediaType)
              .charset(UTF_8)
              .build();
      evaluatedResult = expressionManager
          .evaluate(value, expectedOutputType, NULL_BINDING_CONTEXT, event, headLocation, false);
    }
    return evaluatedResult;
  }

  /**
   * Configure the nested {@link Processor}'s of the XML SDK operation.
   *
   * @param processors
   */
  public void setMessageProcessors(List<Processor> processors) {
    this.processors = processors;
  }

  @Override
  public void initialise() throws InitialisationException {
    final Optional<ProcessingStrategy> processingStrategy = getProcessingStrategy(locator, this);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Initializing {} {} with processing strategy {}...", this.getClass().getSimpleName(),
                   getLocation().getLocation(), processingStrategy);
    }
    this.nestedChain = buildNewChainWithListOfProcessors(processingStrategy, processors);
    super.initialise();
    if (targetValue != null) {
      targetValueExpression = compile(targetValue, expressionManager);
    }
    initialiseIfNeeded(configurationProviderResolver, muleContext);
  }

  @Override
  public void dispose() {
    LOGGER.debug("Disposing {} {}...", this.getClass().getSimpleName(), getLocation().getLocation());
    disposeIfNeeded(configurationProviderResolver, LOGGER);
    super.dispose();
  }

  @Override
  public void start() throws ZernovaException {
    LOGGER.debug("Starting {} {}...", this.getClass().getSimpleName(), getLocation().getLocation());
    super.start();
  }

  @Override
  public void stop() throws ZernovaException {
    LOGGER.debug("Stopping {} {}...", this.getClass().getSimpleName(), getLocation().getLocation());
    super.stop();
  }

  @Override
  protected List<Processor> getOwnedMessageProcessors() {
    return singletonList(nestedChain);
  }

  @Override
  public List<EnrichedErrorMapping> getErrorMappings() {
    return errorMappings;
  }

  @Inject
  public void setFeatureFlaggingService(FeatureFlaggingService featureFlaggingService) {
    this.featureFlaggingService = featureFlaggingService;
  }
}
