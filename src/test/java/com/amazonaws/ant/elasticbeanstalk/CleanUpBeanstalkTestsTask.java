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
import com.amazonaws.ant.AWSTestUtils;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.DeleteInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.RemoveRoleFromInstanceProfileRequest;
import com.amazonaws.services.s3.AmazonS3Client;

public class CleanUpBeanstalkTestsTask extends AWSAntTask {

    private static final String INSTANCEPROFILE_ROLE = "aws-elasticbeanstalk-ec2-role";
    private String instanceProfile;
    private String bucketName;

    public void setInstanceProfile(String instanceProfile) {
        this.instanceProfile = instanceProfile;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void checkParams() {
        boolean areMissingParams = false;
        StringBuffer errors = new StringBuffer("");
        if (instanceProfile == null) {
            areMissingParams = true;
            errors.append("Missing parameter: instanceProfile must be set. \n");
        }
        if (bucketName == null) {
            areMissingParams = true;
            errors.append("Missing parameter: bucketName must be set. \n");
        }
        if (areMissingParams) {
            throw new BuildException(errors.toString());
        }
    }

    public void execute() {
        AmazonIdentityManagementClient iamClient = getOrCreateClient(AmazonIdentityManagementClient.class);
        iamClient
                .removeRoleFromInstanceProfile(new RemoveRoleFromInstanceProfileRequest()
                        .withRoleName(INSTANCEPROFILE_ROLE)
                        .withInstanceProfileName(instanceProfile));
        iamClient.deleteInstanceProfile(new DeleteInstanceProfileRequest()
                .withInstanceProfileName(instanceProfile));
        AmazonS3Client client = getOrCreateClient(AmazonS3Client.class);

        AWSTestUtils.emptyAndDeleteBucket(client, bucketName);
    }
}
