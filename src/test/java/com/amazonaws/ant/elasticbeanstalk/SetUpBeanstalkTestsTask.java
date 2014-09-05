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
package com.amazonaws.ant.elasticbeanstalk;

import org.apache.tools.ant.BuildException;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AddRoleToInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.CreateInstanceProfileRequest;

public class SetUpBeanstalkTestsTask extends AWSAntTask {

    private static final String INSTANCEPROFILE_ROLE = "aws-elasticbeanstalk-ec2-role";
    private String instanceProfile;

    public void setInstanceProfile(String instanceProfile) {
        this.instanceProfile = instanceProfile;
    }

    public void checkParams() {
        if (instanceProfile == null) {
            throw new BuildException(
                    "Missingparameter: instanceProfile is required");
        }
    }

    public void execute() {
        AmazonIdentityManagementClient iamClient = getOrCreateClient(AmazonIdentityManagementClient.class);
        iamClient.createInstanceProfile(new CreateInstanceProfileRequest()
                .withInstanceProfileName(instanceProfile));
        iamClient
                .addRoleToInstanceProfile(new AddRoleToInstanceProfileRequest()
                        .withRoleName(INSTANCEPROFILE_ROLE)
                        .withInstanceProfileName(instanceProfile));
    }
}
