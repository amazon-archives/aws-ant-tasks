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
import com.amazonaws.ant.AWSTestUtils;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;

public class WaitForInstanceToReachStateTask extends AWSAntTask {
    private String instanceId;
    private String state;

    /**
     * Set the ID of the instance to wait for.
     * 
     * @param instanceId
     *            The ID of the instance to wait for.
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Set the state to wait for the instance to reach.
     * 
     * @param state
     *            The state to wait for the instance to reach.
     */
    public void setState(String state) {
        this.state = state;
    }

    public void checkParams() {
        StringBuilder errors = new StringBuilder("");
        boolean areMissingParams = false;

        if (instanceId == null) {
            areMissingParams = true;
            errors.append("Missing parameter: instanceId is required \n");
        }

        if (state == null) {
            areMissingParams = true;
            errors.append("Missing parameter: state is required \n");
        }

        if (areMissingParams) {
            throw new BuildException(errors.toString());
        }
    }

    public void execute() {
        AWSOpsWorksClient client = getOrCreateClient(AWSOpsWorksClient.class);
        try {
            AWSTestUtils.waitForOpsworksInstanceToReachState(client,
                    instanceId, state);
        } catch (Exception e) {
            throw new BuildException(e.getMessage(), e);
        }
    }
}
