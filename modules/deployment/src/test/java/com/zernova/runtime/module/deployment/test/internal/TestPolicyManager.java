/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.module.deployment.test.internal;

import static java.util.Optional.empty;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static com.zernova.runtime.api.util.Preconditions.checkArgument;
import static com.zernova.runtime.container.api.ZernovaFoldersUtil.getExecutionFolder;
import static com.zernova.runtime.core.api.util.FileUtils.unzip;

import com.zernova.runtime.core.api.policy.Policy;
import com.zernova.runtime.core.api.policy.PolicyParametrization;
import com.zernova.runtime.core.api.policy.PolicyProvider;
import com.zernova.runtime.deployment.model.api.application.Application;
import com.zernova.runtime.deployment.model.api.application.ApplicationPolicyManager;
import com.zernova.runtime.deployment.model.api.policy.PolicyRegistrationException;
import com.zernova.runtime.deployment.model.api.policy.PolicyTemplateDescriptor;
import com.zernova.runtime.module.deployment.api.DeploymentListener;
import com.zernova.runtime.module.deployment.api.DeploymentService;
import com.zernova.runtime.module.deployment.impl.internal.policy.PolicyTemplateDescriptorFactory;
import com.zernova.runtime.policy.api.PolicyPointcutParameters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Applies policies to each deployed application to simulate the real manager provided by API Gateway.
 */
public class TestPolicyManager implements DeploymentListener {

  private final DeploymentService deploymentService;
  private final List<PolicyTemplateDescriptor> policyTemplateDescriptors = new ArrayList<>();
  private final PolicyTemplateDescriptorFactory policyTemplateDescriptorFactory;

  /**
   * Creates a new manager
   *
   * @param deploymentService               service that deploys applications in the container. Non null.
   * @param policyTemplateDescriptorFactory creates descriptors for the policy templates. Non null
   */
  public TestPolicyManager(DeploymentService deploymentService, PolicyTemplateDescriptorFactory policyTemplateDescriptorFactory) {
    checkArgument(deploymentService != null, "deploymentService cannot be null");
    checkArgument(policyTemplateDescriptorFactory != null, "policyTemplateDescriptorFactory cannot be null");

    this.deploymentService = deploymentService;
    this.policyTemplateDescriptorFactory = policyTemplateDescriptorFactory;
  }

  /**
   * Registers a policy template that can be later used to register a parameterized policy
   *
   * @param policyTemplate policy artifact file. Non null.
   */
  public PolicyTemplateDescriptor registerPolicyTemplate(File policyTemplate) {
    final File tempFolder = new File(getPoliciesTempFolder(), getBaseName(policyTemplate.getName()));
    try {
      unzip(policyTemplate, tempFolder);
    } catch (IOException e) {
      throw new IllegalStateException("Error processing policy ZIP file: " + policyTemplate, e);
    }

    final PolicyTemplateDescriptor policyTemplateDescriptor = policyTemplateDescriptorFactory.create(tempFolder, empty());
    policyTemplateDescriptors.add(policyTemplateDescriptor);
    return policyTemplateDescriptor;
  }

  /**
   * Adds a parameterized policy
   *
   * @param appName               application where the policy must be applied. Non empty.
   * @param policyTemplateName    template that must be used to instantiate the parametrized policy. Non empty.
   * @param policyParametrization parametrization to instantiate the policy. Non null.
   */
  public void addPolicy(String appName, String policyTemplateName, PolicyParametrization policyParametrization)
      throws PolicyRegistrationException {

    checkArgument(!isEmpty(appName), "appName cannot be empty");
    checkArgument(!isEmpty(policyTemplateName), "policyTemplateName cannot be empty");
    checkArgument(policyParametrization != null, "policyParametrization cannot be ull");

    Optional<PolicyTemplateDescriptor> policyTemplateDescriptor =
        policyTemplateDescriptors.stream().filter(template -> template.getName().equals(policyTemplateName)).findFirst();

    if (!policyTemplateDescriptor.isPresent()) {
      throw new IllegalStateException("Cannot find policy template descriptor with name: " + policyTemplateName);
    }

    addPolicy(appName, policyTemplateDescriptor.get(), policyParametrization);
  }

  /**
   * Adds a parameterized policy
   *
   * @param appName                  application where the policy must be applied. Non empty.
   * @param policyTemplateDescriptor template that must be used to instantiate the parametrized policy. Non empty.
   * @param policyParametrization    parametrization to instantiate the policy. Non null.
   */
  public void addPolicy(String appName, PolicyTemplateDescriptor policyTemplateDescriptor,
                        PolicyParametrization policyParametrization)
      throws PolicyRegistrationException {

    checkArgument(!isEmpty(appName), "appName cannot be empty");
    checkArgument(policyTemplateDescriptor != null, "policyTemplateDescriptor cannot be empty");
    checkArgument(policyParametrization != null, "policyParametrization cannot be ull");

    Application application = deploymentService.findApplication(appName);
    ApplicationPolicyManager policyManager = application.getPolicyManager();
    policyManager.addPolicy(policyTemplateDescriptor, policyParametrization);
  }

  /**
   * Removes a parameterized policy
   *
   * @param appName  application where the policy is applied. Non empty.
   * @param policyId identifies the policy parametrization. Non empty.
   * @return true if the poclicy was removed, false if the policy is not applied in the application.
   * @throws IllegalArgumentException if the application does not exists or there is an invalid parameter value.
   */
  public boolean removePolicy(String appName, String policyId) {
    checkArgument(!isEmpty(appName), "appName cannot be empty");
    checkArgument(!isEmpty(policyId), "policyId cannot be empty");

    Application application = deploymentService.findApplication(appName);
    if (application == null) {
      throw new IllegalArgumentException("Cannot find application named: " + appName);
    }

    return application.getPolicyManager().removePolicy(policyId);
  }

  /**
   * Retrieves the source policies that match the given {@link PolicyPointcutParameters}
   *
   * @param appName                  application from which to look for the policies. Non empty.
   * @param policyPointcutParameters parameters to match against existing policies
   * @return List of source {@link Policy}s that matched the parameters
   */
  public List<Policy> findSourcePolicies(String appName, PolicyPointcutParameters policyPointcutParameters) {
    return getPolicyProvider(appName).findSourceParameterizedPolicies(policyPointcutParameters);
  }

  /**
   * Retrieves the operation policies that match the given {@link PolicyPointcutParameters}Retrieves the source policies that
   * match the given {@link PolicyPointcutParameters}
   *
   * @param appName                  application from which to look for the policies. Non empty.
   * @param policyPointcutParameters parameters to match against existing policies
   * @return List of operation {@link Policy}s that matched the parameters
   */
  public List<Policy> findOperationPolicies(String appName, PolicyPointcutParameters policyPointcutParameters) {
    return getPolicyProvider(appName).findOperationParameterizedPolicies(policyPointcutParameters);
  }

  private PolicyProvider getPolicyProvider(String appName) {
    Application application = deploymentService.findApplication(appName);
    if (application == null) {
      throw new IllegalArgumentException("Cannot find application named: " + appName);
    }

    return application.getArtifactContext().getRegistry().lookupByType(PolicyProvider.class)
        .orElseThrow(() -> new IllegalArgumentException("Cannot find a Polict Provider in application named: " + appName));
  }

  private File getPoliciesTempFolder() {
    return new File(getExecutionFolder(), "policies");
  }

}
