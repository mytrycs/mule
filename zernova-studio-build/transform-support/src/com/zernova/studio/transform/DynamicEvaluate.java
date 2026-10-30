/* Copyright (c) 2026 ZerNova. */
package com.zernova.studio.transform;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import jakarta.inject.Inject;
public final class DynamicEvaluate extends AbstractComponent implements Processor {
 @Inject private ExtendedExpressionManager expressions;private String expression="#[vars.script]";
 public void setExpression(String value){expression=value;}
 public CoreEvent process(CoreEvent event){Object selected=expressions.evaluate(expression,event,getLocation()).getValue();if(!(selected instanceof String script)||script.isBlank()||script.length()>1048576)throw new IllegalArgumentException("Dynamic Evaluate requires a nonempty DataWeave script string, up to 1 MiB.");
  TypedValue<?> value=expressions.evaluate(script.startsWith("#[")?script:"#["+script+"]",event,getLocation());return CoreEvent.builder(event).message(Message.builder(event.getMessage()).payload(value).build()).build();}
}
