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
package com.amazonaws.ant.cloudformation;

import org.apache.tools.ant.BuildException;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;

public class WaitForStackToReachStateTask extends AWSAntTask {

    private static final String FAILED = "FAILED";
    private String stackName;
    private String status;

    /**
     * Set the name of this stack. Required.
     * 
     * @param stackName
     *            The stack name
     */
    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    /**
     * Set the status to wait for this stack to reach. Should not contain
     * "FAILED"
     * 
     * @param status
     *            The status to wait for this stack to reach.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Waits for the specified stack to reach the specified status. Returns true
     * if it does, returns false if it reaches a status with "FAILED", or if 30
     * minutes pass without reaching the desired status.
     */
    private void checkParams() {
        boolean areMissingParams = false;
        StringBuilder errors = new StringBuilder("");

        if (stackName == null) {
            areMissingParams = true;
            errors.append("Missing parameter: stackName is required \n");
        }

        if (status == null) {
            areMissingParams = true;
            errors.append("Missing parameter: stackName is required \n");
        }

        if (areMissingParams) {
            throw new BuildException(errors.toString());
        }
    }

    public void execute() {
        checkParams();
        AmazonCloudFormationClient client = createClient(AmazonCloudFormationClient.class);
        if (!waitForCloudFormationStackToReachStatus(client, stackName, status)) {
            throw new BuildException("The stack update or creation failed");
        }
    }

    public static boolean waitForCloudFormationStackToReachStatus(
            AmazonCloudFormationClient client, String stackName, String status) {
        int count = 0;
        while (true) {
            if (count++ == 100) {
                System.out
                        .println(stackName + " never reached state " + status);
                return false;
            }
            try {
                Thread.sleep(1000 * 30);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                return false;
            }
            String stackStatus = client
                    .describeStacks(
                            new DescribeStacksRequest()
                                    .withStackName(stackName)).getStacks()
                    .get(0).getStackStatus();
            if (stackStatus.equals(status)) {
                return true;
            } else if (stackStatus.contains(FAILED)) {
                System.out.println("The process failed with status " + stackStatus);
                return false;
            }
            System.out.println(stackName + " is in status " + stackStatus);
        }
    }
}
