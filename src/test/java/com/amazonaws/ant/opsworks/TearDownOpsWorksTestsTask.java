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
import com.amazonaws.services.opsworks.model.DeleteAppRequest;
import com.amazonaws.services.opsworks.model.DeleteStackRequest;
import com.amazonaws.services.s3.AmazonS3Client;

public class TearDownOpsWorksTestsTask extends AWSAntTask {

    private String bucketName;

    /**
     * Set the name of the bucket to be emptied and deleted.
     * 
     * @param bucketName
     *            The name of the bucket to be emptied and deleted.
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void checkParams() {
        if (bucketName == null) {
            throw new BuildException(
                    "Missing parameter: bucketName is required");
        }
    }

    public void execute() {
        AmazonS3Client s3Client = getOrCreateClient(AmazonS3Client.class);
        AWSOpsWorksClient client = getOrCreateClient(AWSOpsWorksClient.class);
        AWSTestUtils.emptyAndDeleteBucket(s3Client, bucketName);
        client.deleteApp(new DeleteAppRequest().withAppId(getProject()
                .getProperty("appId")));
        try {
            OpsWorksDeploymentTests.stopAllInstances(getProject(), client);
        } catch (InterruptedException e) {
            throw new BuildException(e.getMessage(), e);
        }
        OpsWorksDeploymentTests.deleteAllInstances(getProject(), client);
        OpsWorksDeploymentTests.deleteAllLayers(getProject(), client);
        client.deleteStack(new DeleteStackRequest().withStackId(getProject()
                .getProperty("stackId")));
    }
}
