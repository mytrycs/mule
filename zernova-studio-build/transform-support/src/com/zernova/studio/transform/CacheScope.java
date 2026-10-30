/* Copyright (c) 2026 ZerNova. Bounded application-local message cache. */
package com.zernova.studio.transform;
import com.zernova.runtime.api.message.Message;
import com.zernova.runtime.core.api.event.CoreEvent;
import com.zernova.runtime.core.api.el.ExtendedExpressionManager;
import com.zernova.runtime.api.lifecycle.InitialisationException;
import jakarta.inject.Inject;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
public final class CacheScope extends SupportScope {
 @Inject private ExtendedExpressionManager expressions;private String key="#[payload]";private long ttlMillis=60000;private int maxEntries=1000;private long bytes;
 private record Entry(long expires,byte[] message){}
 private final LinkedHashMap<String,Entry> entries=new LinkedHashMap<>(16,0.75f,true);
 public void setKey(String value){key=value;}public void setTtlMillis(long value){ttlMillis=value;}public void setMaxEntries(int value){maxEntries=value;}
 public void initialise()throws InitialisationException{if(ttlMillis<1||ttlMillis>86400000L||maxEntries<1||maxEntries>100000)throw new InitialisationException(new IllegalArgumentException("Cache TTL must be 1–86400000 ms and maxEntries 1–100000."),this);super.initialise();}
 private String key(CoreEvent event){try{String expression=key.startsWith("#[")&&key.endsWith("]")?key.substring(2,key.length()-1):key;Object serialized=expressions.evaluate("#[write(("+expression+"),'application/json')]",event,getLocation()).getValue();return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(TextValue.read(serialized).getBytes(StandardCharsets.UTF_8)));}catch(java.security.NoSuchAlgorithmException error){throw new IllegalStateException(error);}}
 public Publisher<CoreEvent> apply(Publisher<CoreEvent> input){return Flux.from(input).flatMap(event->Mono.defer(()->{
  String id=key(event);Entry cached;
  synchronized(entries){cached=entries.get(id);if(cached!=null&&cached.expires<System.currentTimeMillis()){entries.remove(id);bytes-=cached.message.length;cached=null;}}
  if(cached!=null){Message restored=muleContext.getObjectSerializer().getInternalProtocol().deserialize(cached.message);return Mono.just(CoreEvent.builder(event).message(restored).build());}
  return Mono.from(chain.apply(Mono.just(event))).doOnNext(output->{try{byte[] serialized=muleContext.getObjectSerializer().getInternalProtocol().serialize(output.getMessage());if(serialized.length>8388608)return;synchronized(entries){Entry old=entries.put(id,new Entry(System.currentTimeMillis()+ttlMillis,serialized));bytes+=serialized.length-(old==null?0:old.message.length);while(entries.size()>maxEntries||bytes>67108864){Iterator<Entry> iterator=entries.values().iterator();Entry evicted=iterator.next();bytes-=evicted.message.length;iterator.remove();}}}catch(RuntimeException unsupported){logger.warn("ZERNOVA Cache bypassed an unserializable message: {}",unsupported.getClass().getSimpleName());}});
 }));}
 public void dispose(){synchronized(entries){entries.clear();bytes=0;}super.dispose();}
}
