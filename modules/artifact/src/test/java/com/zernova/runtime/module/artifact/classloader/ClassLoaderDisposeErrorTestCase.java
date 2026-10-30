/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.classloader;

import static com.zernova.runtime.module.artifact.classloader.SimpleClassLoaderLookupPolicy.PARENT_FIRST_CLASSLOADER_LOOKUP_POLICY;
import static com.zernova.test.allure.AllureConstants.LeakPrevention.LEAK_PREVENTION;
import static com.zernova.test.allure.AllureConstants.LeakPrevention.LeakPreventionMetaspace.METASPACE_LEAK_PREVENTION_ON_REDEPLOY;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.zernova.runtime.module.artifact.api.classloader.ZernovaArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ShutdownListener;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.probe.JUnitLambdaProbe;
import com.zernova.tck.probe.PollingProber;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.net.URL;

import org.junit.Test;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@Feature(LEAK_PREVENTION)
@Story(METASPACE_LEAK_PREVENTION_ON_REDEPLOY)
@Issue("W-12512289")
public class ClassLoaderDisposeErrorTestCase extends AbstractZernovaTestCase {

  private static final int PROBER_POLLING_INTERVAL = 100;
  private static final int PROBER_POLLING_TIMEOUT = 5000;

  @Test
  public void disposingContinuesAfterErrorWhileReleasingResources() throws ClassNotFoundException {
    ZernovaArtifactClassLoader artifactClassLoader =
        new TestArtifactClassLoader(ClassLoaderDisposeErrorTestCase.class.getClassLoader());

    artifactClassLoader.loadClass(TestDriver.class.getName());

    ShutdownListener shutdownListener = new ShutdownListener() {

      @Override
      public void execute() {
        // Nothing to do
      }
    };

    artifactClassLoader.addShutdownListener(shutdownListener);
    PhantomReference<ShutdownListener> shutdownListenerRef = new PhantomReference<>(shutdownListener, new ReferenceQueue<>());

    artifactClassLoader.dispose();

    shutdownListener = null;

    new PollingProber(PROBER_POLLING_TIMEOUT, PROBER_POLLING_INTERVAL).check(new JUnitLambdaProbe(() -> {
      System.gc();
      assertThat(shutdownListenerRef.isEnqueued(), is(true));
      return true;
    }));
  }

  private static class TestArtifactClassLoader extends ZernovaArtifactClassLoader {

    public TestArtifactClassLoader(ClassLoader parentCl) {
      super("testId", new ArtifactDescriptor("test"), new URL[0], parentCl, PARENT_FIRST_CLASSLOADER_LOOKUP_POLICY);
    }

  }

}
