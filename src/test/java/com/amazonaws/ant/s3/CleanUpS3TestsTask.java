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

import java.io.File;

import org.apache.tools.ant.BuildException;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.ant.AWSTestUtils;
import com.amazonaws.services.s3.AmazonS3Client;

public class CleanUpS3TestsTask extends AWSAntTask {
    private String firstFile;

    public void setFirstFile(String firstFile) {
        this.firstFile = firstFile;
    }

    private String secondFile;

    public void setSecondFile(String secondFile) {
        this.secondFile = secondFile;
    }

    private String bucketName;

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    private void checkParams() {
        if (firstFile == null) {
            throw new BuildException("File must be set");
        }
        if (secondFile == null) {
            throw new BuildException("Second file must be set");
        }
        if (bucketName == null) {
            throw new BuildException("Bucket name must be set");
        }
    }

    @Override
	public void execute() {
        checkParams();
        File file1 = new File(firstFile);
        if (file1.exists()) {
            file1.delete();
        }
        File file2 = new File(secondFile);
        if (file2.exists()) {
            file2.delete();
        }
        AmazonS3Client client = getOrCreateClient(AmazonS3Client.class);

        AWSTestUtils.emptyAndDeleteBucket(client, bucketName);
    }
}
