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
import com.amazonaws.services.opsworks.AWSOpsWorksClient;

public class AssertSuccessfulDeploymentTask extends AWSAntTask {

    private String deploymentId;

    /**
     * Set the ID of the deployment that must be successful.
     * 
     * @param deploymentId
     *            The ID of the deployment that must be successful.
     */
    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    private void checkParams() {
        if (deploymentId == null) {
            throw new BuildException(
                    "Missing parameter: deploymentId is required");
        }
    }

    public void execute() {
        checkParams();
        if (!OpsWorksDeploymentTests.wasSuccessfulDeployment(
                createClient(AWSOpsWorksClient.class), deploymentId)) {
            throw new BuildException("deployment " + deploymentId + " failed");
        }
    }
}
