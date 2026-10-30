/* Copyright (c) 2026 ZerNova. */
package com.zernova.studio.transform;
import com.zernova.runtime.core.api.event.CoreEvent;
import org.reactivestreams.Publisher;
public final class BatchComplete extends SupportScope {
 public Publisher<CoreEvent> apply(Publisher<CoreEvent> input){return chain.apply(input);}
}
