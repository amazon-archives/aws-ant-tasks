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
package com.amazonaws.ant.s3;

import org.apache.tools.ant.BuildException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * ANT Task for creating a bucket in S3. Specify the name of your bucket, and it
 * will be created.
 */
public class CreateS3BucketTask extends AWSAntTask {

    private String bucketName;

    /**
     * Specify the name of the S3 bucket to create
     * 
     * @param bucketName
     *            The desired named of the S3 bucket. Must be valid and not
     *            taken.
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void checkParams() {
        if (bucketName == null) {
            throw new BuildException(
                    "Missing parameter: bucketName must be set.");
        }
    }

    public void execute() {
        AmazonS3Client client = getOrCreateClient(AmazonS3Client.class);
        try {
            System.out.println("Creating bucket with name " + bucketName
                    + "...");
            client.createBucket(bucketName);
            System.out
                    .println("Bucket " + bucketName + " successfuly created.");
        } catch (AmazonServiceException ase) {
            throw new BuildException(
                    "AmazonServiceException: Errors in S3 while processing request."
                            + ase.getMessage());
        } catch (AmazonClientException ace) {
            throw new BuildException(
                    "AmazonClientException: Errors encountered in the client while"
                            + " making the request or handling the response. "
                            + ace.getMessage());
        } catch (Exception e) {
            throw new BuildException(e.getMessage());
        }
    }
}
