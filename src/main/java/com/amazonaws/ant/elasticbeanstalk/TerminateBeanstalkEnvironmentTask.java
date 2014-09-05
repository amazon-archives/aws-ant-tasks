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
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest;

/**
 * This task will terminate an Elastic Beanstalk environment
 */
public class TerminateBeanstalkEnvironmentTask extends AWSAntTask {

    private String environmentName;

    /**
     * Set the name of the environment. Required
     * 
     * @param environmentName
     *            The environment to terminate.
     */
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
        System.out
                .println("Terminating environment " + environmentName + "...");
        checkParams();
        AWSElasticBeanstalkClient bcClient = getOrCreateClient(AWSElasticBeanstalkClient.class);
        try {
            bcClient.terminateEnvironment(new TerminateEnvironmentRequest()
                    .withEnvironmentName(environmentName));
        } catch (Exception e) {
            throw new BuildException("Could not terminate environment "
                    + e.getMessage(), e);
        }
        System.out
                .println("The request to terminate the environment has been submitted.");
    }
}
