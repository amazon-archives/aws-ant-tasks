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
import com.amazonaws.ant.AWSTestUtils;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus;

public class TestSuccessfulBeanstalkDeploymentTask extends AWSAntTask {

    private String environmentName;

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    private void checkParams() {
        if (environmentName == null) {
            throw new BuildException(
                    "Missing parameter: environmentName is required");
        }
    }

    public void execute() {
        checkParams();
        AWSElasticBeanstalkClient bcClient = createClient(AWSElasticBeanstalkClient.class);
        DescribeEnvironmentsRequest deRequest = new DescribeEnvironmentsRequest()
                .withEnvironmentNames(environmentName);
        DescribeEnvironmentsResult result = bcClient
                .describeEnvironments(deRequest);
        if (result.getEnvironments().size() < 1) {
            throw new BuildException(
                    "No environments found with the specified name "
                            + environmentName);
        }
        try {
            AWSTestUtils.waitForEnvironmentToTransitionToStateAndHealth(
                    environmentName, EnvironmentStatus.Ready, null, bcClient);
        } catch (InterruptedException e) {
            throw new BuildException(e.getMessage());
        }
    }
}
