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
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest;

/**
 * Ant Task for creating an Elastic Beanstalk application.
 */
public class CreateBeanstalkApplicationTask extends AWSAntTask {
    String applicationDescription;
    String applicationName;

    /**
     * Specify name of this application
     * 
     * @param applicationName
     *            Name of this application
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Specify the description of your beanstalk application
     * 
     * @param applicationDescription
     *            Description of this application
     */
    public void setApplicationDescription(String applicationDescription) {
        this.applicationDescription = applicationDescription;
    }

    public void checkParams() {
        boolean areMissingParams = false;
        StringBuilder errors = new StringBuilder("");
        if (applicationDescription == null) {
            areMissingParams = true;
            errors.append("Missing parameter: applicationDescription is required \n");
        }
        if (applicationName == null) {
            areMissingParams = true;
            errors.append("Missing parameter: applicationName is required \n");
        }
        if (areMissingParams) {
            throw new BuildException(errors.toString());
        }
    }

    public void execute() {
        checkParams();
        AWSElasticBeanstalkClient client = getOrCreateClient(AWSElasticBeanstalkClient.class);
        CreateApplicationRequest request = new CreateApplicationRequest(
                applicationName).withDescription(applicationDescription);
        System.out.println("Creating application " + applicationName + "...");
        try {
            client.createApplication(request);
        } catch (Exception e) {
            throw new BuildException(
                    "Exception while attempting to create application: "
                            + e.getMessage(), e);
        }
        System.out.println("Application created successfully");
    }
}
