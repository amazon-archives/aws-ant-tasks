/*
 * Copyright 2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.ant.opsworks;

import org.apache.tools.ant.BuildException;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.GetInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;

public class SetUpOpsWorksTestsTask extends AWSAntTask {

    private String instanceProfile;
    private String serviceRole;

    /**
     * Set the ARN of an IAM instance profile to assign to the "instanceProfile"
     * property.
     * 
     * @param instanceProfile
     *            The ARN of an IAM instance profile to assign to the
     *            "instanceProfile" property.
     */
    public void setInstanceProfile(String instanceProfile) {
        this.instanceProfile = instanceProfile;
    }

    /**
     * Set the ARN of an IAM role to assign to the "serviceRole" property
     * 
     * @param serviceRole
     *            The ARN of an IAM role to assign to the "serviceRole" property
     */
    public void setServiceRole(String serviceRole) {
        this.serviceRole = serviceRole;
    }

    private void checkParams() {
        StringBuilder errors = new StringBuilder("");
        boolean areMissingParams = false;
        if (instanceProfile == null) {
            areMissingParams = true;
            errors.append("Missing parameter: instanceProfile is required");
        }
        if (serviceRole == null) {
            areMissingParams = true;
            errors.append("Missing parameter: serviceRole is required");
        }

        if (areMissingParams) {
            throw new BuildException(errors.toString());
        }
    }

    /**
     * Sets the "instanceProfile" and "serviceRole" properties according to the
     * set parameters.
     */
    public void execute() {
        checkParams();
        AmazonIdentityManagementClient iamClient = getOrCreateClient(AmazonIdentityManagementClient.class);
        getProject()
                .setProperty(
                        "instanceProfileArn",
                        iamClient
                                .getInstanceProfile(
                                        new GetInstanceProfileRequest()
                                                .withInstanceProfileName(instanceProfile))
                                .getInstanceProfile().getArn());
        getProject()
                .setProperty(
                        "serviceRoleArn",
                        iamClient
                                .getRole(
                                        new GetRoleRequest()
                                                .withRoleName(serviceRole))
                                .getRole().getArn());

    }
}
