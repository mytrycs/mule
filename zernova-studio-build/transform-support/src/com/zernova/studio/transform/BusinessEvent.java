/* Copyright (c) 2026 ZerNova. */
package com.zernova.studio.transform;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.api.metadata.DataType;
import com.zernova.runtime.api.el.BindingContext;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import jakarta.inject.Inject;
import org.slf4j.LoggerFactory;
public final class BusinessEvent extends AbstractComponent implements Processor {
 @Inject private ExtendedExpressionManager expressions;private String name="application.event",data="#[{}]";
 public void setName(String value){name=value;}public void setData(String value){data=value;}
 public CoreEvent process(CoreEvent event){TypedValue<?> fields=expressions.evaluate(data,event,getLocation());BindingContext context=BindingContext.builder().addBinding("zernovaBusinessName",new TypedValue<String>(name,DataType.STRING)).addBinding("zernovaBusinessFields",fields).build();
  Object json=expressions.evaluate("#[%dw 2.0\noutput application/java\n---\nwrite({name:zernovaBusinessName,correlationId:correlationId,data:zernovaBusinessFields},'application/json')]",event,getLocation(),context).getValue();
  LoggerFactory.getLogger("com.zernova.business.events").info("ZERNOVA_BUSINESS_EVENT {}",TextValue.read(json));return event;}
}
