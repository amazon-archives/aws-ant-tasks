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
import com.amazonaws.services.cloudformation.model.SetStackPolicyRequest;

public class SetStackPolicyTask extends AWSAntTask {

    private String stackName;
    private String stackPolicyBody;
    private String stackPolicyURL;

    /**
     * Set the body of a stack policy to apply. Must be well-formed, properly
     * escaped JSON if specified. If this is set, stackPolicyURL cannot be set.
     * If stackPolicyURL is set, this cannot be set. It is required that either
     * this or stackPolicyUrl be set.
     * 
     * @param stackPolicyBody
     *            Well formed, properly escaped JSON specifying a stack policy.
     */
    public void setStackPolicyBody(String stackPolicyBody) {
        this.stackPolicyBody = stackPolicyBody;
    }

    /**
     * Set the URL leading to the body of a stack policy to apply. If this is
     * set, stackPolicyBody cannot be set. If stackPolicyBody is set, this
     * cannot be set. It is required that either this or stackPolicyBody be set.
     * 
     * @param stackPolicyURL
     *            A valid URL pointing to a JSON object specifying a stack
     *            policy.
     */
    public void setStackPolicyURL(String stackPolicyURL) {
        this.stackPolicyURL = stackPolicyURL;
    }

    /**
     * Set the name of this stack. Required.
     * 
     * @param stackName
     *            The stack name
     */
    public void setStackName(String stackName) {
        this.stackName = stackName;
    }

    public void checkParams() {
        boolean areMalformedParams = false;
        StringBuilder errors = new StringBuilder("");

        if (stackName == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: stackName is required. \n");
        }
        if ((stackPolicyBody == null) == (stackPolicyURL == null)) {
            areMalformedParams = true;
            errors.append("Error in parameter configuration: You must set either stackPolicyBody or stackPolicyURL (But not both) \n");
        }
        if (areMalformedParams) {
            throw new BuildException(errors.toString());
        }
    }

    public void execute() {
        checkParams();
        AmazonCloudFormationClient client = getOrCreateClient(AmazonCloudFormationClient.class);
        SetStackPolicyRequest setStackPolicyRequest = new SetStackPolicyRequest()
                .withStackName(stackName).withStackPolicyBody(stackPolicyBody)
                .withStackPolicyURL(stackPolicyURL);
        try {
            client.setStackPolicy(setStackPolicyRequest);
            System.out.println("Successfully set stack policy");
        } catch (Exception e) {
            throw new BuildException("Could not set the stack policy "
                    + e.getMessage(), e);
        }
    }
}
