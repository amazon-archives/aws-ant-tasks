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
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;

public class SetUpCloudFormationTestsTask extends AWSAntTask {

    private String propertyNameForKeyName = "generatedKeyName";

    /**
     * Set the property to set to the generated key-pair name. Not required, has
     * a default of "generatedKeyName"
     * 
     * @param propertyNameForKeyName
     */
    public void setPropertyNameForKeyName(String propertyNameForKeyName) {
        this.propertyNameForKeyName = propertyNameForKeyName;
    }

    private void checkParams() {
        if (getProject().getProperty(propertyNameForKeyName) != null) {
            throw new BuildException("The property " + propertyNameForKeyName
                    + " was already defined");
        }
    }

    /**
     * Creates a key-pair with a randomly generated name, and sets the generated
     * name to the specified property.
     */
    public void execute() {
        checkParams();
        AmazonEC2Client client = getOrCreateClient(AmazonEC2Client.class);
        try {
            String keyName = Long.toString(System.currentTimeMillis());
            client.createKeyPair(new CreateKeyPairRequest()
                    .withKeyName(keyName));
            getProject().setProperty(propertyNameForKeyName, keyName);
        } catch (Exception e) {
            throw new BuildException(e.getMessage(), e);
        }
    }
}
