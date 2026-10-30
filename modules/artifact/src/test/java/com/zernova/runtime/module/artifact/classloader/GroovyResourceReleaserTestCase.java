/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.artifact.classloader;

import static com.zernova.runtime.module.artifact.classloader.DependencyResolver.getDependencyFromMaven;
import static com.zernova.runtime.module.artifact.classloader.SimpleClassLoaderLookupPolicy.CHILD_FIRST_CLASSLOADER_LOOKUP_POLICY;
import static com.zernova.tck.junit4.matcher.Eventually.eventually;
import static com.zernova.tck.util.CollectableReference.collectedByGc;
import static com.zernova.test.allure.AllureConstants.LeakPrevention.LEAK_PREVENTION;
import static com.zernova.test.allure.AllureConstants.LeakPrevention.LeakPreventionMetaspace.METASPACE_LEAK_PREVENTION_ON_REDEPLOY;

import static java.lang.Class.forName;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

import com.zernova.runtime.module.artifact.api.classloader.ZernovaArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.util.CollectableReference;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import org.aspectj.weaver.loadtime.Aj;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@Feature(LEAK_PREVENTION)
@RunWith(Parameterized.class)
@Story(METASPACE_LEAK_PREVENTION_ON_REDEPLOY)
public class GroovyResourceReleaserTestCase extends AbstractZernovaTestCase {

  private static List<String> oldAjLoadersToSkip;

  @BeforeClass
  public static void avoidAspectjAgentLeak() {
    oldAjLoadersToSkip = Aj.loadersToSkip;
    Aj.loadersToSkip = asList(ZernovaArtifactClassLoader.class.getName(),
                              "groovy.lang.GroovyClassLoader$InnerLoader");
  }

  @AfterClass
  public static void restoreAjLoadersToSkip() {
    Aj.loadersToSkip = oldAjLoadersToSkip;
  }

  private static final String GROOVY_ARTIFACT_ID = "groovy";
  private static final String GROOVY_GROUP_ID = "org.codehaus.groovy";
  private static final String GROOVY_SCRIPT_ENGINE = "groovy.util.GroovyScriptEngine";
  private static final String GROOVY_LANG_BINDING = "groovy.lang.Binding";

  private final URL groovyUrl;

  public GroovyResourceReleaserTestCase(String groovyVersion) {
    groovyUrl = getDependencyFromMaven(GROOVY_GROUP_ID, GROOVY_ARTIFACT_ID, groovyVersion);
  }

  @Parameterized.Parameters(name = "Testing artifact {0}")
  public static String[] data() throws NoSuchFieldException, IllegalAccessException {
    return new String[] {
        "2.4.21",
        "2.5.22",
        "3.0.19"
    };
  }

  @Test
  public void runGroovyScriptAndDispose() throws ReflectiveOperationException {
    createAppClassLoaderRunAndAssert(currentThread().getContextClassLoader(), new URL[] {groovyUrl});
  }

  @Test
  @Issue("W-15750766")
  public void runGroovyScriptAndDisposeWhenGroovyIsInDomain() throws ReflectiveOperationException {
    CollectableReference<ZernovaArtifactClassLoader> domainClassLoader = createClassLoader("Domain",
                                                                                        currentThread().getContextClassLoader(),
                                                                                        new URL[] {groovyUrl});
    // This is to make the hierarchy more similar to reality, in particular, this affects the way in which classloaders are
    // reachable through the parents.
    RegionClassLoader regionClassLoader = new RegionClassLoader(domainClassLoader.get());

    createAppClassLoaderRunAndAssert(regionClassLoader);
    createAppClassLoaderRunAndAssert(regionClassLoader);

    regionClassLoader = null;
    disposeClassLoaderAndAssertCollected(domainClassLoader);
  }

  private void createAppClassLoaderRunAndAssert(ClassLoader parentClassLoader) throws ReflectiveOperationException {
    createAppClassLoaderRunAndAssert(parentClassLoader, new URL[] {});
  }

  private CollectableReference<ZernovaArtifactClassLoader> createClassLoader(String name, ClassLoader parentClassLoader,
                                                                          URL[] classPaths) {
    return new CollectableReference<>(new ZernovaArtifactClassLoader(name,
                                                                  mock(ArtifactDescriptor.class),
                                                                  classPaths,
                                                                  parentClassLoader,
                                                                  CHILD_FIRST_CLASSLOADER_LOOKUP_POLICY));
  }

  private void createAppClassLoaderRunAndAssert(ClassLoader parentClassLoader, URL[] classPaths)
      throws ReflectiveOperationException {
    CollectableReference<ZernovaArtifactClassLoader> collectableReference =
        createClassLoader("Application", parentClassLoader, classPaths);

    assertThat(runScript(collectableReference.get()), is("hello"));
    disposeClassLoaderAndAssertCollected(collectableReference);
  }

  private void disposeClassLoaderAndAssertCollected(CollectableReference<ZernovaArtifactClassLoader> collectableReference) {
    collectableReference.get().dispose();
    assertThat(collectableReference, is(eventually(collectedByGc())));
  }

  private static String runScript(ZernovaArtifactClassLoader artifactClassLoader) throws ReflectiveOperationException {
    URL[] roots = new URL[] {artifactClassLoader.getResource("groovy/example.groovy")};
    Class<?> groovyScriptEngineClass = forName(GROOVY_SCRIPT_ENGINE, true, artifactClassLoader);
    Object scriptEngine =
        groovyScriptEngineClass.getConstructor(URL[].class, ClassLoader.class).newInstance(roots, artifactClassLoader);
    Class<?> groovyBinding = forName(GROOVY_LANG_BINDING, true, artifactClassLoader);
    Method runMethod = groovyScriptEngineClass.getMethod("run", String.class, groovyBinding);
    String scriptBody = "example.groovy";
    return (String) runMethod.invoke(scriptEngine, scriptBody, groovyBinding.getConstructor().newInstance());
  }

  private static class RegionClassLoader extends URLClassLoader {

    private final ZernovaArtifactClassLoader registeredClassLoader;

    public RegionClassLoader(ZernovaArtifactClassLoader registeredClassLoader) {
      super(new URL[] {});
      this.registeredClassLoader = registeredClassLoader;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
      return registeredClassLoader.loadClass(name);
    }
  }
}
