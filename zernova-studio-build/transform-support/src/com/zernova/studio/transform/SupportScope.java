/* Copyright (c) 2026 ZerNova. Original lifecycle adapter for ZERNOVA scopes. */
package com.zernova.studio.transform;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.processor.*;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.privileged.processor.MessageProcessors;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import java.util.*;
import org.reactivestreams.Publisher;
public abstract class SupportScope extends AbstractMessageProcessorOwner implements Processor {
 protected List<Processor> children=List.of();protected MessageProcessorChain chain;
 public void setMessageProcessors(List<Processor> value){children=List.copyOf(value);}
 protected List<Processor> getOwnedMessageProcessors(){return chain==null?children:List.of(chain);}
 public void initialise() throws InitialisationException {if(children.isEmpty())throw new InitialisationException(new IllegalArgumentException("Scope needs child processors."),this);chain=MessageProcessors.newChain(Optional.empty(),children);super.initialise();}
 public CoreEvent process(CoreEvent event)throws ZernovaException{return MessageProcessors.processToApply(event,this);}
 public abstract Publisher<CoreEvent> apply(Publisher<CoreEvent> input);
}
