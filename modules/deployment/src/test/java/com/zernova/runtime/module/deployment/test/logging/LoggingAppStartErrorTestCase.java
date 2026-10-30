/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.test.logging;

import static com.zernova.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static com.zernova.runtime.deployment.model.api.application.ApplicationStatus.DEPLOYMENT_FAILED;
import static com.zernova.test.allure.AllureConstants.Logging.LOGGING;
import static com.zernova.test.allure.AllureConstants.Logging.LoggingStory.ERROR_REPORTING;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import static com.github.valfirst.slf4jtest.TestLoggerFactory.getTestLogger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThrows;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.zernova.runtime.api.lifecycle.LifecycleException;
import com.zernova.runtime.api.lifecycle.Startable;
import com.zernova.runtime.api.lifecycle.Stoppable;
import com.zernova.runtime.api.memory.management.MemoryManagementService;
import com.zernova.runtime.api.notification.NotificationListenerRegistry;
import com.zernova.runtime.api.service.ServiceRepository;
import com.zernova.runtime.core.api.ZernovaContext;
import com.zernova.runtime.core.api.source.MessageSource;
import com.zernova.runtime.core.internal.context.ZernovaContextWithRegistry;
import com.zernova.runtime.core.internal.registry.DefaultRegistry;
import com.zernova.runtime.deployment.model.api.DeploymentStartException;
import com.zernova.runtime.deployment.model.api.application.ApplicationDescriptor;
import com.zernova.runtime.deployment.model.api.application.ApplicationStatus;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactConfigurationProcessor;
import com.zernova.runtime.deployment.model.api.artifact.ArtifactContext;
import com.zernova.runtime.module.artifact.activation.api.extension.discovery.ExtensionModelLoaderRepository;
import com.zernova.runtime.module.artifact.activation.internal.classloader.ZernovaApplicationClassLoader;
import com.zernova.runtime.module.artifact.api.classloader.ZernovaArtifactClassLoader;
import com.zernova.runtime.module.artifact.api.descriptor.ClassLoaderConfiguration.ClassLoaderConfigurationBuilder;
import com.zernova.runtime.module.deployment.impl.internal.application.DefaultZernovaApplication;
import com.zernova.runtime.module.deployment.internal.DefaultArtifactDeployer;
import com.zernova.runtime.module.deployment.test.internal.AbstractApplicationDeploymentTestCase;
import com.zernova.tck.probe.JUnitProbe;
import com.zernova.tck.probe.PollingProber;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;

@Feature(LOGGING)
@Story(ERROR_REPORTING)
@Issue("W-11090837")
public class LoggingAppStartErrorTestCase extends AbstractApplicationDeploymentTestCase {

  private static final int PROBER_TIMEOUT = 1000;
  private static final int PROBER_INTERVAL = 100;

  private DefaultZernovaApplication application;
  private ArtifactContext mockArtifactContext;

  private final File appLocation = new File("fakeLocation");

  private final TestLogger loggerDefaultMuleApplication = getTestLogger(DefaultZernovaApplication.class);
  private final TestLogger loggerDefaultArtifactDeployer = getTestLogger(DefaultArtifactDeployer.class);

  @Parameterized.Parameters(name = "Parallel: {0}")
  public static List<Boolean> params() {
    return asList(false);
  }

  public LoggingAppStartErrorTestCase(boolean parallelDeployment) {
    super(parallelDeployment);
  }

  @Before
  public void doSetUp() throws Exception {
    ZernovaApplicationClassLoader parentArtifactClassLoader = mock(ZernovaApplicationClassLoader.class);
    mockArtifactContext = mock(ArtifactContext.class);
    ZernovaContextWithRegistry muleContext = mock(ZernovaContextWithRegistry.class);
    when(mockArtifactContext.getMuleContext()).thenReturn(muleContext);

    DefaultRegistry defaultRegistryMock = mock(DefaultRegistry.class);
    when(mockArtifactContext.getRegistry()).thenReturn(defaultRegistryMock);

    NotificationListenerRegistry notificationListenerRegistryMock = mock(NotificationListenerRegistry.class);
    when(defaultRegistryMock.lookupByType(any())).thenReturn(Optional.of(notificationListenerRegistryMock));

    ApplicationDescriptor applicationDescriptorMock = mock(ApplicationDescriptor.class, RETURNS_DEEP_STUBS);
    when(applicationDescriptorMock.getClassLoaderConfiguration())
        .thenReturn(new ClassLoaderConfigurationBuilder().containing(new URL("file:/target/classes")).build());
    application = new DefaultZernovaApplication(applicationDescriptorMock, parentArtifactClassLoader, emptyList(),
                                             null, mock(ServiceRepository.class),
                                             mock(ExtensionModelLoaderRepository.class),
                                             appLocation, null, null,
                                             mock(MemoryManagementService.class),
                                             mock(ArtifactConfigurationProcessor.class));

    Method setArtifactContext = application.getClass().getDeclaredMethod("setArtifactContext", ArtifactContext.class);
    setArtifactContext.setAccessible(true);
    setArtifactContext.invoke(application, mockArtifactContext);
  }

  @Test
  public void whenAppThrowsLifecycleExceptionWhileStartingTheErrorShouldBeLogged() throws Exception {
    ZernovaContext mockedMuleContext = mock(ZernovaContext.class);
    when(mockArtifactContext.getMuleContext()).thenReturn(mockedMuleContext);
    String expectedLogMessage = "Could not start Test App";

    MessageSource mockMessageSource = mock(MessageSource.class, withSettings().extraInterfaces(Startable.class, Stoppable.class));
    doThrow(new LifecycleException(createStaticMessage(expectedLogMessage), mockMessageSource)).when(mockedMuleContext).start();

    assertThrows(DeploymentStartException.class, () -> application.start());

    assertStatus(DEPLOYMENT_FAILED);
    assertThat(loggerDefaultMuleApplication.getAllLoggingEvents(),
               hasItem(new LoggingEventMatcher(containsString(expectedLogMessage))));
  }

  private void assertStatus(final ApplicationStatus status) {
    PollingProber prober = new PollingProber(PROBER_TIMEOUT, PROBER_INTERVAL);
    prober.check(new JUnitProbe() {

      @Override
      protected boolean test() throws Exception {
        assertThat(application.getStatus(), is(status));
        return true;
      }

      @Override
      public String describeFailure() {
        return format("Application remained at status %s instead of moving to %s", application.getStatus().name(),
                      status.name());
      }
    });

  }

  @Test
  public void whenAppFailsWhileStartingTheErrorLogShouldBeCreatedWithTheAppClassloader() throws Exception {
    String expectedLogMessageDefaultMuleApplication = "Failing processor error";
    String expectedLogMessageDefaultArtifactDeployer = "Failed to deploy artifact";
    addPackedAppFromBuilder(dummyErrorAppOnStartDescriptorFileBuilder);

    startDeployment();

    assertDeploymentFailure(applicationDeploymentListener, dummyErrorAppOnStartDescriptorFileBuilder.getId());

    assertThat(loggerDefaultArtifactDeployer.getAllLoggingEvents(),
               hasItem(new LoggingEventMatcher(containsString(expectedLogMessageDefaultArtifactDeployer))));

    assertThat(loggerDefaultMuleApplication.getAllLoggingEvents(),
               hasItem(new LoggingEventMatcher(containsString(expectedLogMessageDefaultMuleApplication),
                                               containsString("dummy-error-app-start"))));
  }

  private static class LoggingEventMatcher extends TypeSafeMatcher<LoggingEvent> {

    private final Matcher<String> messageMatcher;

    private final Optional<Matcher<String>> artifactIdMatcher;

    LoggingEventMatcher(Matcher<String> messageMatcher, Matcher<String> artifactIdMatcher) {
      this.messageMatcher = messageMatcher;
      this.artifactIdMatcher = ofNullable(artifactIdMatcher);
    }

    LoggingEventMatcher(Matcher<String> messageMatcher) {
      this(messageMatcher, null);
    }

    @Override
    protected boolean matchesSafely(LoggingEvent loggingEvent) {
      return messageMatcher.matches(loggingEvent.getMessage()) &&
          artifactIdMatcher
              .map(m -> m.matches(getArtifactId(loggingEvent)))
              .orElse(true);
    }

    private String getArtifactId(LoggingEvent loggingEvent) {
      return ((ZernovaArtifactClassLoader) loggingEvent.getThreadContextClassLoader()).getArtifactId();
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("a logging event with a message matching ").appendDescriptionOf(messageMatcher);
      artifactIdMatcher
          .ifPresent(m -> description.appendText("\n\tand with artifact id matching ").appendDescriptionOf(m));
    }

    @Override
    protected void describeMismatchSafely(LoggingEvent loggingEvent, Description mismatchDescription) {
      mismatchDescription.appendText("got a logging event with a message that ");
      messageMatcher.describeMismatch(loggingEvent.getMessage(), mismatchDescription);
      if (artifactIdMatcher.isPresent()) {
        mismatchDescription.appendText("and artifact id that ");
        artifactIdMatcher.get().describeMismatch(getArtifactId(loggingEvent), mismatchDescription);
      }
    }
  }

}
