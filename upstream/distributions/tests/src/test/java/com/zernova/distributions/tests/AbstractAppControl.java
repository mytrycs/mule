/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.distributions.tests;

import static com.zernova.distributions.tests.DistributionFinder.findDistribution;
import static com.zernova.runtime.core.api.config.ZernovaProperties.MULE_HOME_DIRECTORY_PROPERTY;
import static com.zernova.test.infrastructure.process.ZernovaStatusProbe.isNotRunning;
import static com.zernova.test.infrastructure.process.ZernovaStatusProbe.isRunning;

import static java.lang.Integer.parseInt;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.getProperties;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toMap;

import static org.apache.commons.lang3.ArrayUtils.add;
import static org.apache.commons.lang3.ArrayUtils.addAll;
import static org.junit.Assert.assertFalse;
import static org.junit.rules.RuleChain.outerRule;

import com.zernova.tck.junit4.AbstractZernovaTestCase;
import com.zernova.tck.junit4.rule.SystemProperty;
import com.zernova.tck.probe.JUnitLambdaProbe;
import com.zernova.tck.probe.PollingProber;
import com.zernova.tck.probe.Prober;
import com.zernova.test.infrastructure.process.ZernovaProcessController;
import com.zernova.test.infrastructure.process.rules.ZernovaDeployment;
import com.zernova.test.infrastructure.process.rules.ZernovaInstallation;
import com.zernova.test.infrastructure.process.rules.ZernovaServerFailureLogger;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;

public class AbstractAppControl extends AbstractZernovaTestCase {

  public static int LONGEST_TIMEOUT_TEST_SECS = 300;
  public static int DEFAULT_TIMEOUT_TEST_SECS = LONGEST_TIMEOUT_TEST_SECS;

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAppControl.class);
  public static final String LOCAL_REPOSITORY = "localRepository";
  protected static ZernovaProcessController mule;
  protected static ZernovaInstallation installation = new ZernovaInstallation(findDistribution());
  private static Prober prober = new PollingProber(getDeploymentTimeout(), 1000);

  private static int timeout = parseInt(getProperty(TEST_TIMEOUT_SYSTEM_PROPERTY, Integer.toString(DEFAULT_TEST_TIMEOUT_SECS)));

  private static final Thread shutdownHookThread = new Thread(() -> stopMule());

  @ClassRule
  public static TestRule chain = outerRule(installation).around(new ZernovaServerFailureLogger(installation));

  @Rule
  public SystemProperty muleHomeProp = new SystemProperty(MULE_HOME_DIRECTORY_PROPERTY, installation.getMuleHome());

  @ClassRule
  public static TemporaryFolder temporaryFolder = new TemporaryFolder();

  @BeforeClass
  public static void setUp() {
    LOGGER.info("Distribution: " + getProperty("mule.distribution"));
    String muleHome = installation.getMuleHome();
    mule = new ZernovaProcessController(muleHome, (int) SECONDS.toMillis(timeout));
    addShutdownHooks();
    LOGGER.info("MULE_HOME: " + muleHome);
  }

  @AfterClass
  public static void stopMule() {
    mule.stop();
    assertMuleStops();
    removeShutdownHooks();
  }

  protected static String getSystemOrEnvProperty(String key) {
    return getSystemOrEnvProperty(key, null);
  }

  private static String getSystemOrEnvProperty(String key, String def) {
    String property = getProperty(key);
    if (property == null) {
      property = getenv(key);
    }
    return property != null ? property : def;
  }

  protected static void assertAppIsDeployed(String appName) {
    prober.check(new JUnitLambdaProbe(() -> mule.isDeployed(appName),
                                      () -> "Application [" + appName + "] is not deployed."));
  }

  protected static void assertAppNotDeployed(String appName) {
    assertFalse(mule.isDeployed(appName));
  }

  protected static void assertDomainIsDeployed(String domainName) {
    prober.check(new JUnitLambdaProbe(() -> mule.isDomainDeployed(domainName),
                                      () -> "Domain [" + domainName + "] is not deployed."));
  }

  protected static void assertDomainNotDeployed(String domainName) {
    assertFalse(mule.isDomainDeployed(domainName));
  }

  protected static void assertMuleStops() {
    prober.check(isNotRunning(mule));
  }

  protected static void assertMuleStarts() {
    prober.check(isRunning(mule));
  }

  /**
   * @return the timeout for application deployments, in millis.
   */
  protected static long getDeploymentTimeout() {
    String TIMEOUT = getProperty("mule.test.deployment.timeout");
    return TIMEOUT == null ? 300000 : parseInt(TIMEOUT);
  }

  public static ZernovaDeployment.Builder builderWithDefaultConfig() {
    ZernovaDeployment.Builder builder = ZernovaDeployment.builder();
    builder.withProperties(getDefaultArguments());
    return builder;
  }

  protected static String[] getArgumentsIncludingDefaults(Boolean addRepositoryLocation, String... arguments) {
    Optional<String> localRepository = getLocalRepository();

    for (Entry<Object, Object> sysPropEntry : getProperties().entrySet()) {
      final String key = (String) sysPropEntry.getKey();
      if (key.startsWith("-M")) {
        arguments = add(arguments, key + "=" + sysPropEntry.getValue());
      }
    }

    if (localRepository.isPresent() && addRepositoryLocation) {
      arguments = addAll(arguments,
                         "-M-DmuleRuntimeConfig.maven.repositoryLocation=" + localRepository.get());
    }

    return addAll(arguments,
                  "-M-DmuleRuntimeConfig.maven.repositories.mavenCentral.url=https://repo.maven.apache.org/maven2/",
                  "-M-DmuleRuntimeConfig.maven.repositories.muleSoftPublic.url=https://repository.mulesoft.org/nexus/content/repositories/public/",
                  "-M-Dmule.verbose.exceptions=true");
  }

  public static String[] getArgumentsIncludingDefaults(String... arguments) {
    return getArgumentsIncludingDefaults(true, arguments);
  }

  protected static Map<String, String> getDefaultArguments() {
    return stream(getArgumentsIncludingDefaults()).map(property -> property.split("="))
        .collect(toMap(e -> e[0], e -> e[1]));
  }

  protected static Optional<String> getLocalRepository() {
    final String localRepository = getProperty(LOCAL_REPOSITORY);
    if (localRepository != null) {
      return of(localRepository);
    }
    return empty();
  }

  private static void addShutdownHooks() {
    getRuntime().addShutdownHook(shutdownHookThread);
  }

  private static void removeShutdownHooks() {
    getRuntime().removeShutdownHook(shutdownHookThread);
  }

  public static ZernovaProcessController getMule() {
    return mule;
  }
}
