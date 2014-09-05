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
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest;

public class DeleteBeanstalkApplicationTask extends AWSAntTask {

    private String applicationName;

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    private void checkParams() {
        if (applicationName == null) {
            throw new BuildException(
                    "Missing parameter: applicationName is required");
        }
    }

    public void execute() {
        checkParams();
        AWSElasticBeanstalkClient bcClient = getOrCreateClient(AWSElasticBeanstalkClient.class);
        bcClient.deleteApplication(new DeleteApplicationRequest(applicationName));
    }
}
