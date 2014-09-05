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

import java.io.File;

import org.apache.tools.ant.BuildException;

import com.amazonaws.ant.AWSAntTask;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest;
import com.amazonaws.services.elasticbeanstalk.model.S3Location;
import com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

/**
 * A task for deploying an application to AWS Elastic Beanstalk. To use this app, you must
 * have an existing application and environment in AWS Elastic Beanstalk. This task will
 * create a new version of your application and update the environment.
 */
public class DeployAppToBeanstalkTask extends AWSAntTask {

    private String bucketName;
    private String key;
    private String versionLabel;
    private String versionDescription;
    private String applicationName;
    private String environmentName;
    private File file;

    /**
     * Specify the name of the bucket in S3 to upload your application file to.
     * Optional
     * 
     * @param bucketName
     *            The name of the bucket in S3 to upload your application file
     *            to. Must be a valid bucket that you have access permission to.
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Specify the application file (WAR, ZIP, etc) to upload to S3. This will
     * be the file attached to the new version of this application. Optional; if
     * not specified you must specify a file in S3 to use.
     * 
     * @param file
     *            The file containing your application
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Specify the key of the application file in S3. Conditionally required.
     * You can either upload a file to S3 to use as your application file, or
     * you can specify a file already in S3. If you are uploading a file, this
     * field is not required--if not set, the key will be the same as the file
     * name. If you are specifying a file in S3, this field must be set, and
     * must be a valid key in S3.
     * 
     * @param key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Specify the label of the new version. Required.
     * 
     * @param versionLabel
     *            What you want to name the new version of this application
     */
    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    /**
     * The description of the new version of the application. Required
     * 
     * @param versionDescription
     *            A description of the new application version
     */
    public void setVersionDescription(String versionDescription) {
        this.versionDescription = versionDescription;
    }

    /**
     * The name of the application you are updating. Must be an application in
     * beanstalk that exists. Required.
     * 
     * @param applicationName
     *            The name of the application in beanstalk that you want to
     *            update.
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * The nam eof the environment you are updating. Must be an environment in
     * beanstalk that exists and is in the "Ready" state. Required.
     * 
     * @param environmentName
     */
    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    private void checkParams() {
        StringBuilder errors = new StringBuilder("");
        boolean areMissingParams = false;
        if (bucketName == null) {
            areMissingParams = true;
            errors.append("Missing parameter: bucketName is required. \n");
        }

        if (key == null && file == null) {
            areMissingParams = true;
            errors.append("Error in parameter configuration: Either key or file must be set. You must specify an application in S3 or your own application to upload to S3.");
        }
        if (versionLabel == null) {
            areMissingParams = true;
            errors.append("Missing parameter: versionLabel is required \n");
        }
        if (versionDescription == null) {
            areMissingParams = true;
            errors.append("Missing parameter: versionDescription is required \n");
        }
        if (applicationName == null) {
            areMissingParams = true;
            errors.append("Missing parameter: applicationName is required \n");
        }
        if (environmentName == null) {
            areMissingParams = true;
            errors.append("Missing parameter: environmentName is required \n");
        }
        if (areMissingParams) {
            throw new BuildException(errors.toString());
        }
    }

    public void execute() {
        checkParams();
        AWSElasticBeanstalkClient client = getOrCreateClient(AWSElasticBeanstalkClient.class);

        CreateApplicationVersionRequest vRequest = new CreateApplicationVersionRequest(
                applicationName, versionLabel);
        vRequest.setDescription(versionDescription);
        String s3key = key == null ? file.getName() : key;
        if (file != null) {
            TransferManager tm;
            if (awsSecretKey != null && awsAccessKeyId != null) {
                tm = new TransferManager(new BasicAWSCredentials(
                        awsAccessKeyId, awsSecretKey));
            } else {
                tm = new TransferManager();
            }
            System.out.println("Uploading file " + file.getName() + " to S3");
            try {
                Upload u = tm.upload(bucketName, s3key, file);
                u.waitForCompletion();
            } catch (Exception e) {
                throw new BuildException("Error when trying to upload file: "
                        + e.getMessage(), e);
            }
            System.out.println("Upload successful");
        }
        vRequest.setSourceBundle(new S3Location(bucketName, s3key));
        System.out.println("Creating application version " + versionLabel
                + "...");
        try {
            client.createApplicationVersion(vRequest);
        } catch (Exception e) {
            throw new BuildException(
                    "Exception while attempting to create application version: "
                            + e.getMessage(), e);
        }
        System.out.println("Application version successfully created");

        System.out.println("Updating environment...");
        UpdateEnvironmentRequest updateRequest = new UpdateEnvironmentRequest()
                .withEnvironmentName(environmentName).withVersionLabel(
                        versionLabel);
        client.updateEnvironment(updateRequest);
        System.out.println("Update environment request submitted");
    }

}
