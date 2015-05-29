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
import java.io.IOException;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * Ant task for Downloading a file from a specified bucket in S3. You have two
 * main options for downloading. You may either specify a key and download the
 * file in your bucket in S3 with that key (To a target file if you wish to
 * specify it), or specify a prefix and download all files from your bucket in
 * S3 with that prefix to a specified directory.
 */
public class DownloadFileFromS3Task extends AWSAntTask {
    private String bucketName;
    private String dir;
    private String key;
    private String keyPrefix;
    private File file;

    /**
     * Specify the name of your S3 bucket
     * 
     * @param bucketName
     *            The name of the bucket in S3 to download the file from. An
     *            exception will be thrown if it doesn't exist.
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Specify the key to download
     * 
     * @param key
     *            The key of the file you want to download
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Specify your key prefix. If set, you must also specify a directory.
     * 
     * @param keyPrefix
     *            All files in your bucket in S3 whose keys begin with these
     *            prefix will be downloaded to a specified directory
     */
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    /**
     * Set the name of the file to download to
     * 
     * @param file
     *            Optional. If set, the data from S3 will be stored in a file
     *            with this name. Otherwise, the name of the file will be the
     *            same as the key. The file should not already exist.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * If you download files based on a prefix, then you must specify a
     * directory to download the files to.
     * 
     * @param dir
     *            The directory to download your files to
     */
    public void setDir(String dir) {
        this.dir = dir;
    }

    public void checkParams() {
        boolean areMalformedParams = false;
        StringBuilder errors = new StringBuilder("");
        if (bucketName == null) {
            areMalformedParams = true;
            errors.append("Missing parameter: bucketName is required. \n");
        }
        if (key != null && keyPrefix != null) {
            areMalformedParams = true;
            errors.append("key and keyPrefix cannot both be set. You either want to download a single file, or all files with a certain prefix. \n");
        }
        if (key == null && keyPrefix == null) {
            areMalformedParams = true;
            errors.append("Either key or keyPrefix must be set \n");
        }
        if (keyPrefix != null && file != null) {
            areMalformedParams = true;
            errors.append("keyPrefix and fileName cannot both be set. If KeyPrefix is set, all files with your prefix will be downloaded as their keys. \n");
        }
        if (keyPrefix != null && dir == null) {
            areMalformedParams = true;
            errors.append("If keyPrefix is set, then dir must be set to specify what directory to download the files to. \n");
        }
        if (areMalformedParams) {
            throw new BuildException(errors.toString());
        }
    }

    private void downloadObjectToFile(AmazonS3Client client, File file, String key) {
        System.out.println("Downloading S3Object with key " + key
                + " from bucket " + bucketName + " to file " + file + "...");
        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        } catch (IOException e) {
            throw new BuildException(
                    "IOException while attempting to create new file " + file + ": "
                            + e.getMessage());
        }
        try {
            client.getObject(new GetObjectRequest(bucketName, key), file);
        } catch (Exception e) {
            throw new BuildException(
                    "Exception while trying to download object: " + bucketName + "/" + key + ": " + " to file " + file
                            + e.getMessage(), e);
        }
        System.out.println("Download successful");
    }

    public void execute() {
        AmazonS3Client client = getOrCreateClient(AmazonS3Client.class);
        if (key != null) {
            File targetFile = file == null ? new File(key) : file;
            downloadObjectToFile(client, targetFile, key);
        } else {
            ObjectListing objectListing = client.listObjects(bucketName);

            while (true) {
                for (Iterator<?> iterator = objectListing.getObjectSummaries()
                        .iterator(); iterator.hasNext();) {
                    S3ObjectSummary objectSummary = (S3ObjectSummary) iterator
                            .next();
                    String key = objectSummary.getKey();
                    if (key.startsWith(keyPrefix)) {
                        downloadObjectToFile(client, new File(dir
                                + File.pathSeparator + key), key);
                    }
                }

                if (objectListing.isTruncated()) {
                    objectListing = client
                            .listNextBatchOfObjects(objectListing);
                } else {
                    break;
                }
            }
        }
    }
}
