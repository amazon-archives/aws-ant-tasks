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
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

/**
 * Ant Task for uploading a fileset or filesets to S3.
 */
public class UploadFileSetToS3Task extends AWSAntTask {
    private Vector<FileSet> filesets = new Vector<FileSet>();
    private String bucketName;
    private String keyPrefix;
    private boolean printStatusUpdates = false;
    private boolean continueOnFail = false;
    private int statusUpdatePeriodInMs = 500;

    /**
     * Specify a fileset to be deployed. 
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
     * Specify whether to print updates about your upload. The update will
     * consist of how many bytes have been uploaded versus how many are to be
     * uploaded in total. Not required, default is false.
     * 
     * @param printStatusUpdates
     *            Whether you want the task to print status updates about your
     *            upload.
     */
    public void setPrintStatusUpdates(boolean printStatusUpdates) {
        this.printStatusUpdates = printStatusUpdates;
    }
    
    /**
     * Set how long to wait in between polls of your upload when printing
     * status. Not required, default is 500. Setting will do nothing unless
     * printStatusUpdates is true.
     * 
     * @param statusUpdatePeriodInMs
     *            How long to wait in between polls of your upload when printing
     *            status
     */
    public void setStatusUpdatePeriodInMs(int statusUpdatePeriodInMs) {
        this.statusUpdatePeriodInMs = statusUpdatePeriodInMs;
    }
    
    /**
     * Verifies that all necessary parameters were set
     */
    private void checkParameters() {
        StringBuilder errors = new StringBuilder("");
        boolean areMalformedParams = false;
        if (bucketName == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: bucketName is required \n");
        }
        if (filesets.size() < 1) {
            areMalformedParams = true;
            errors.append("Missing parameter: you must specify at least one fileset \n");
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
        TransferManager transferManager;
        if (awsSecretKey != null && awsAccessKeyId != null) {
            transferManager = new TransferManager(getOrCreateClient(AmazonS3Client.class));
        } else {
            transferManager = new TransferManager();
        }
        for (FileSet fileSet : filesets) {
            DirectoryScanner directoryScanner = fileSet.getDirectoryScanner(getProject());
            String[] includedFiles = directoryScanner.getIncludedFiles();
            try {
                for (String includedFile : includedFiles) {
                    File base = directoryScanner.getBasedir();
                    File file = new File(base, includedFile);
                    String key = keyPrefix + file.getName();
                    try {
                        System.out.println("Uploading file " + file.getName()
                                + "...");
                        Upload upload = transferManager.upload(bucketName, key, file);
                        if (printStatusUpdates) {
                            while (!upload.isDone()) {
                                System.out.print(upload.getProgress()
                                        .getBytesTransferred()
                                        + "/"
                                        + upload.getProgress()
                                                .getTotalBytesToTransfer()
                                        + " bytes transferred...\r");
                                Thread.sleep(statusUpdatePeriodInMs);
                            }
                            System.out.print(upload.getProgress()
                                        .getBytesTransferred()
                                        + "/"
                                        + upload.getProgress()
                                                .getTotalBytesToTransfer()
                                        + " bytes transferred...\n");
                        } else {
                            upload.waitForCompletion();
                        }
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
                transferManager.shutdownNow();
            }
        }
    }
}
