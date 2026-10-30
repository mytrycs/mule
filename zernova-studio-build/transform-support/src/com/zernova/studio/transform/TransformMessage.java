/* Copyright (c) 2026 ZerNova. Original message transformation processor. */
package com.zernova.studio.transform;
import com.zernova.runtime.api.component.AbstractComponent;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.api.metadata.TypedValue;
import com.zernova.runtime.core.api.processor.Processor;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import jakarta.inject.Inject;
import java.util.Map;
public final class TransformMessage extends AbstractComponent implements Processor {
  @Inject private ExtendedExpressionManager expressions;
  private String mimeType="";
  public void setMimeType(String value){mimeType=value;}
  private String payload="", attributes="", variables="";
  public void setPayload(String value){payload=value;}
  public void setAttributes(String value){attributes=value;}
  public void setVariables(String value){variables=value;}
  private TypedValue<?> evaluate(String script,CoreEvent event){return expressions.evaluate("#["+script+"]",event,getLocation());}
  public CoreEvent process(CoreEvent event) throws ZernovaException {
    // All outputs evaluate against the original input; no partial event escapes on failure.
    TypedValue<?> p=payload.isBlank()?null:evaluate(payload,event);
    TypedValue<?> a=attributes.isBlank()?null:evaluate(attributes,event);
    TypedValue<?> v=variables.isBlank()?null:evaluate(variables,event);
    if(v!=null && !(v.getValue() instanceof Map))throw new IllegalArgumentException("Transform variables must produce an object keyed by variable name.");
    Message.Builder message=Message.builder(event.getMessage());
    if(p!=null){message.payload(p);if(!mimeType.isBlank())message.mediaType(com.zernova.runtime.api.metadata.MediaType.parse(mimeType));}
    if(a!=null)message.attributes(a);
    CoreEvent.Builder out=CoreEvent.builder(event).message(message.build());
    if(v!=null)for(Map.Entry<?,?> entry:((Map<?,?>)v.getValue()).entrySet()){
      if(!(entry.getKey() instanceof String) || !((String)entry.getKey()).matches("[A-Za-z_][A-Za-z0-9_]*"))throw new IllegalArgumentException("Invalid Transform variable name.");
      out.addVariable((String)entry.getKey(),entry.getValue());
    }
    return out.build();
  }
}
