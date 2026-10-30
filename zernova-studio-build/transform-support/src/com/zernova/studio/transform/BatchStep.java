/* Copyright (c) 2026 ZerNova. Original record-step lifecycle and acceptance policy. */
package com.zernova.studio.transform;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.api.exception.ZernovaException;
import com.zernova.runtime.core.api.processor.*;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.core.privileged.processor.MessageProcessors;
import com.zernova.runtime.core.privileged.processor.chain.MessageProcessorChain;
import jakarta.inject.Inject;
import java.util.*;
public final class BatchStep extends AbstractMessageProcessorOwner implements Processor {
 @Inject private ExtendedExpressionManager expressions;
 private String name="step",acceptExpression="#[true]",acceptPolicy="NO_FAILURES";private List<Processor> children=List.of();private MessageProcessorChain prefix;private BatchAggregator aggregator;
 public void setName(String value){name=value;}public String name(){return name;}
 public void setAcceptExpression(String value){acceptExpression=value;}public void setAcceptPolicy(String value){acceptPolicy=value;}
 public void setMessageProcessors(List<Processor> value){children=List.copyOf(value);}
 protected List<Processor> getOwnedMessageProcessors(){List<Processor> owned=new ArrayList<>();if(prefix!=null)owned.add(prefix);if(aggregator!=null)owned.add(aggregator);return owned;}
 public void initialise()throws InitialisationException{
  if(!Set.of("NO_FAILURES","ONLY_FAILURES","ALL").contains(acceptPolicy))throw new InitialisationException(new IllegalArgumentException("Unknown batch acceptance policy."),this);
  List<Processor> ordinary=new ArrayList<>();for(int index=0;index<children.size();index++){Processor child=children.get(index);if(child instanceof BatchAggregator group){if(aggregator!=null||index!=children.size()-1)throw new InitialisationException(new IllegalArgumentException("Use one Batch Aggregator as the final component in a step."),this);aggregator=group;}else ordinary.add(child);}
  if(!ordinary.isEmpty())prefix=MessageProcessors.newChain(Optional.empty(),ordinary);if(prefix==null&&aggregator==null)throw new InitialisationException(new IllegalArgumentException("Batch Step needs processors."),this);super.initialise();
 }
 boolean accepts(CoreEvent event,boolean failed){if(acceptPolicy.equals("NO_FAILURES")&&failed||acceptPolicy.equals("ONLY_FAILURES")&&!failed)return false;return Boolean.TRUE.equals(expressions.evaluate(acceptExpression,event,getLocation()).getValue());}
 CoreEvent applyRecord(CoreEvent event)throws ZernovaException{return prefix==null?event:MessageProcessors.processToApplyWithChildContext(event,prefix);}
 BatchAggregator aggregator(){return aggregator;}
 public CoreEvent process(CoreEvent event){throw new IllegalStateException("Batch Step can execute only inside Batch Job.");}
}
