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
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

/**
 * ANT Task for deploying a fileset or filesets to S3.
 * 
 * @author jesduart
 * 
 */
public class UploadFileSetToS3Task extends AWSAntTask {
    private Vector<FileSet> filesets = new Vector<FileSet>();
    private String bucketName;
    private String keyPrefix;
    private boolean continueOnFail = false;

    /**
     * Specify a fileset to be deployed
     * 
     * @param fileset
     *            A fileset, whose files will all be deployed to S3
     */
    public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }

    /**
     * Specify the name of your S3 bucket
     * 
     * @param bucketName
     *            The name of the bucket in S3 to store the files in. An
     *            exception will be thrown if it doesn't exist.
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Specify the prefix to your files in this upload. This is optional
     * 
     * @param keyPrefix
     *            If specified, all of your files in the fileset will have this
     *            prefixed to their key. For example, you can name this
     *            "myfiles/", and if you upload myfile.txt, its key in S3 will
     *            be myfiles/myfile.txt.
     */
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    /**
     * Specify whether you want to continue uploading your fileset if the upload
     * of one file fails. False by default.
     * 
     * @param continueOnFail
     *            If true, the task will continue to upload files from the
     *            fileset if any single files fails to upload. Otherwise, one
     *            file
     */
    public void setContinueOnFail(String continueOnFail) {
        this.continueOnFail = new Boolean(continueOnFail);
    }

    /**
     * Verifies that all necessary parameters were set
     */
    private void checkParameters() {
        StringBuilder errors = new StringBuilder("");
        boolean areMalformedParams = false;
        if (bucketName == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: bucketname is required");
        }
        if (filesets.size() < 1) {
            areMalformedParams = true;
            errors.append("Missing parameter: you must specify at least one fileset");
        }
        if (areMalformedParams) {
            throw new BuildException(errors.toString());
        }
    }

    /**
     * Uploads files to S3
     */
    public void execute() {
        checkParameters();
        TransferManager tm;
        if (awsSecretKey != null && awsAccessKeyId != null) {
            tm = new TransferManager(new BasicAWSCredentials(awsAccessKeyId,
                    awsSecretKey));
        } else {
            tm = new TransferManager();
        }
        for (FileSet fs : filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] includedFiles = ds.getIncludedFiles();
            try {
                for (String s : includedFiles) {
                    File base = ds.getBasedir();
                    File file = new File(base, s);
                    String key = keyPrefix + file.getName();
                    try {
                        System.out.println("Uploading file " + file.getName()
                                + "...");
                        Upload u = tm.upload(bucketName, key, file);
                        u.waitForCompletion();
                        System.out.println("Upload succesful");
                    } catch (Exception e) {
                        if (!continueOnFail) {
                            throw new BuildException(
                                    "Error. The file that failed to upload was: "
                                            + file.getName(), e);
                        } else {
                            System.err.println("The file " + file.getName()
                                    + " failed to upload. Continuing...");
                        }
                    }
                }
            } finally {
                tm.shutdownNow();
            }
        }
    }
}
