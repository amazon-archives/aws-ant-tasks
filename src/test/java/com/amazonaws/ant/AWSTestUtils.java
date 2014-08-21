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
package com.amazonaws.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClient;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentHealth;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentStatus;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AWSTestUtils {

    public static void emptyAndDeleteBucket(AmazonS3Client client,
            String bucketName) {
        ObjectListing objectListing = client.listObjects(bucketName);

        while (true) {
            for (Iterator<?> iterator = objectListing.getObjectSummaries()
                    .iterator(); iterator.hasNext();) {
                S3ObjectSummary objectSummary = (S3ObjectSummary) iterator
                        .next();
                client.deleteObject(bucketName, objectSummary.getKey());
            }

            if (objectListing.isTruncated()) {
                objectListing = client.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }
        client.deleteBucket(bucketName);
    }

    public static File createRandomFile(String prefix, String suffix)
            throws IOException {
        File file = File.createTempFile(prefix, suffix);
        writeRandomLinesToFile(file);
        return file;
    }

    public static void writeRandomLinesToFile(File file)
            throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(file);
        Random random = new Random();
        int randInt = random.nextInt(100) + 1;
        for (int i = 0; i <= randInt; i++) {
            writer.println(random.nextInt(100) + 1);
        }
        writer.close();
    }

    public static void waitForEnvironmentToTransitionToStateAndHealth(
            String environmentName, EnvironmentStatus state,
            EnvironmentHealth health, AWSElasticBeanstalkClient bcClient)
            throws InterruptedException {
        System.out.println("Waiting for instance " + environmentName
                + " to transition to " + state + "/" + health);

        int count = 0;
        while (true) {
            Thread.sleep(1000 * 30);
            if (count++ > 100) {
                throw new RuntimeException("Environment " + environmentName
                        + " never transitioned to " + state + "/" + health);
            }

            List<EnvironmentDescription> environments = bcClient
                    .describeEnvironments(
                            new DescribeEnvironmentsRequest()
                                    .withEnvironmentNames(environmentName))
                    .getEnvironments();

            if (environments.size() == 0) {
                System.out
                        .println("No environments with that name were found.");
                return;
            }

            EnvironmentDescription environment = environments.get(0);
            System.out.println(" - " + environment.getStatus() + "/"
                    + environment.getHealth());
            if (environment.getStatus().equalsIgnoreCase(state.toString()) == false)
                continue;
            if (health != null
                    && environment.getHealth().equalsIgnoreCase(
                            health.toString()) == false)
                continue;
            return;
        }
    }
}
