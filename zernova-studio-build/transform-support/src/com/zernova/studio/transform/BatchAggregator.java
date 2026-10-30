/* Copyright (c) 2026 ZerNova. */
package com.zernova.studio.transform;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import com.zernova.runtime.core.api.event.CoreEvent;
import org.reactivestreams.Publisher;
public final class BatchAggregator extends SupportScope {
 private int size=100;public void setSize(int value){size=value;}public int size(){return size;}
 public void initialise()throws InitialisationException{if(size<1||size>10000)throw new InitialisationException(new IllegalArgumentException("Batch aggregation size must be 1–10000."),this);super.initialise();}
 public Publisher<CoreEvent> apply(Publisher<CoreEvent> input){return chain.apply(input);}
}
