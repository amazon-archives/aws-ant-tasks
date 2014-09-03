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
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DeleteKeyPairRequest;

public class TearDownCloudFormationTestsTask extends AWSAntTask {

    private String keyName;
    private String stackName;

    /**
     * Set the name of the key to delete. Required.
     * 
     * @param keyName
     *            The name of the key to delete
     */
    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    /**
     * Set the name of the stack to delete. Required.
     * 
     * @param stackName
     */
    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    /**
     * Deletes the specified key and stack, which should have only existed for
     * testing purposes.
     */
    private void checkParams() {
        boolean areMissingParams = false;
        StringBuilder errors = new StringBuilder("");
        if (keyName == null) {
            areMissingParams = true;
            errors.append("Missing parameter: keyName is required. \n");
        }
        if (stackName == null) {
            areMissingParams = true;
            errors.append("Missing parameter: stackName is required. \n");
        }
        if (areMissingParams) {
            throw new BuildException(errors.toString());
        }
    }

    public void execute() {
        checkParams();
        AmazonEC2Client ec2Client = createClient(AmazonEC2Client.class);
        ec2Client
                .deleteKeyPair(new DeleteKeyPairRequest().withKeyName(keyName));
        AmazonCloudFormationClient cloudFormationClient = createClient(AmazonCloudFormationClient.class);
        cloudFormationClient.deleteStack(new DeleteStackRequest()
                .withStackName(stackName));
    }
}
