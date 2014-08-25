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

import java.io.File;

import junit.framework.Assert;

import org.apache.tools.ant.Project;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.ant.AWSTestUtils;
import com.amazonaws.ant.opsworks.DeployAppTask.Command;
import com.amazonaws.ant.opsworks.DeployAppTask.InstanceId;
import com.amazonaws.ant.opsworks.IncrementalDeploymentTask.DeploymentGroup;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.GetInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.DeleteAppRequest;
import com.amazonaws.services.opsworks.model.DeleteInstanceRequest;
import com.amazonaws.services.opsworks.model.DeleteLayerRequest;
import com.amazonaws.services.opsworks.model.DeleteStackRequest;
import com.amazonaws.services.opsworks.model.Deployment;
import com.amazonaws.services.opsworks.model.DescribeDeploymentsRequest;
import com.amazonaws.services.opsworks.model.StopInstanceRequest;
import com.amazonaws.services.s3.AmazonS3Client;

public class OpsWorksDeploymentTests {

    private static final String ONLINE = "online";
    private static final String COMMAND = "deploy";
    private static final String S3_URL = "https://s3.amazonaws.com/";
    private static final String REPO_TYPE = "s3";
    private static final String APP_TYPE = "java";
    private static final String APP_NAME = "AntTaskTestApp";
    private static final String AVAILABILITY_ZONE = "us-east-1b";
    private static final String STACK_NAME = "antstack";
    private static final String LAYER_NAME = "AntTaskTestLayer";
    private static final String JAVA_APP = "java-app";
    private static AWSOpsWorksClient client;
    private static Project project;
    private static String BUCKET_NAME = "antopsworkstestbucket";
    private static String KEY = "test/test.war";
    private static final String WAR_FILE = System.getProperty("user.dir")
            + "/test.war";
    private static AmazonS3Client s3Client;
    private static AmazonIdentityManagementClient iamClient;

    @BeforeClass
    public static void setUp() {
        s3Client = new AmazonS3Client();
        client = new AWSOpsWorksClient();
        iamClient = new AmazonIdentityManagementClient();
    }

    private CreateStackTask readyStackTask() {
        CreateStackTask stackTask = new CreateStackTask();
        stackTask.setProject(project);
        stackTask.setName("AntTaskTestStack");
        stackTask.setRegion("us-east-1");
        stackTask
                .setDefaultInstanceProfileArn(iamClient
                        .getInstanceProfile(
                                new GetInstanceProfileRequest()
                                        .withInstanceProfileName("aws-opsworks-ec2-role"))
                        .getInstanceProfile().getArn());
        stackTask.setServiceRoleArn(iamClient
                .getRole(
                        new GetRoleRequest()
                                .withRoleName("aws-opsworks-service-role"))
                .getRole().getArn());

        return stackTask;
    }

    private CreateLayerTask readyLayerTask() {

        CreateLayerTask layerTask = new CreateLayerTask();
        layerTask.setProject(project);
        layerTask.setType(JAVA_APP);
        layerTask.setName(LAYER_NAME);
        layerTask.setShortname(STACK_NAME);

        return layerTask;
    }

    private CreateInstanceTask readyInstanceTask() {
        CreateInstanceTask instanceTask = new CreateInstanceTask();
        instanceTask.setProject(project);
        instanceTask.setInstanceType(InstanceType.M1Small.toString());
        instanceTask.setAvailabilityZone(AVAILABILITY_ZONE);

        return instanceTask;
    }

    private CreateAppTask readyAppTask() {
        CreateAppTask appTask = new CreateAppTask();

        appTask.setProject(project);
        appTask.setName(APP_NAME);
        appTask.setType(APP_TYPE);
        appTask.setRepoType(REPO_TYPE);
        appTask.setRepoUrl(S3_URL + BUCKET_NAME + "/" + KEY);
        appTask.setRepoUsername(new DefaultAWSCredentialsProviderChain()
                .getCredentials().getAWSAccessKeyId());
        appTask.setRepoPassword(new DefaultAWSCredentialsProviderChain()
                .getCredentials().getAWSSecretKey());

        return appTask;
    }

    private DeployAppTask readyDeployTask() {
        DeployAppTask deployTask = new DeployAppTask();
        deployTask.setProject(project);
        deployTask.setTaskName("deploy-opsworks-app");
        Command command = new Command();
        command.setName(COMMAND);
        deployTask.addConfiguredCommand(command);

        return deployTask;
    }

    @Test
    public void testDeploymentFlow() throws InterruptedException {
        project = new Project();
        s3Client.createBucket(BUCKET_NAME);
        s3Client.putObject(BUCKET_NAME, KEY, new File(WAR_FILE));
        CreateStackTask stackTask = readyStackTask();
        CreateLayerTask layerTask = readyLayerTask();
        layerTask.setPropertyNameForLayerId("layerId1");
        CreateInstanceTask instanceTask = readyInstanceTask();
        instanceTask.setPropertyNameForInstanceId("instanceId1");
        CreateAppTask appTask = readyAppTask();
        DeployAppTask deployTask = readyDeployTask();
        deployTask.setPropertyNameForDeploymentId("deploymentId1");
        InstanceId instanceId = new InstanceId();

        stackTask.execute();
        layerTask.execute();
        instanceTask.execute();

        instanceId.setValue(project.getProperty("instanceId1"));
        AWSTestUtils.waitForOpsworksInstanceToReachState(client,
                instanceId.getValue(), ONLINE);
        deployTask.addConfiguredInstanceId(instanceId);

        appTask.execute();
        deployTask.execute();

        Assert.assertTrue(wasSuccessfulDeployment(client,
                deployTask.getDeploymentId()));
    }

    @Test
    public void testIncrementalDeployments() throws InterruptedException {
        project = new Project();
        s3Client.createBucket(BUCKET_NAME);
        s3Client.putObject(BUCKET_NAME, KEY, new File(WAR_FILE));
        CreateStackTask stackTask = readyStackTask();
        CreateLayerTask layerTask = readyLayerTask();
        layerTask.setPropertyNameForLayerId("layerId1");
        CreateAppTask appTask = readyAppTask();
        CreateInstanceTask instanceTask1 = readyInstanceTask();
        instanceTask1.setPropertyNameForInstanceId("instanceId1");
        CreateInstanceTask instanceTask2 = readyInstanceTask();
        instanceTask2.setPropertyNameForInstanceId("instanceId2");
        CreateInstanceTask instanceTask3 = readyInstanceTask();
        instanceTask3.setPropertyNameForInstanceId("instanceId3");
        CreateInstanceTask instanceTask4 = readyInstanceTask();
        instanceTask4.setPropertyNameForInstanceId("instanceId4");
        CreateInstanceTask instanceTask5 = readyInstanceTask();
        instanceTask5.setPropertyNameForInstanceId("instanceId5");

        stackTask.execute();
        layerTask.execute();
        appTask.execute();
        instanceTask1.execute();
        instanceTask2.execute();
        instanceTask3.execute();
        instanceTask4.execute();
        instanceTask5.execute();

        IncrementalDeploymentTask incrementalDeploymentTask = new IncrementalDeploymentTask();
        DeploymentGroup deploymentGroup1 = new DeploymentGroup();

        DeployAppTask deployTask1 = readyDeployTask();
        deployTask1.setPropertyNameForDeploymentId("deploymentId1");
        InstanceId instanceId1 = new InstanceId();
        instanceId1.setValue(project.getProperty("instanceId1"));
        deployTask1.addConfiguredInstanceId(instanceId1);

        DeployAppTask deployTask2 = readyDeployTask();
        deployTask2.setPropertyNameForDeploymentId("deploymentId2");
        InstanceId instanceId2 = new InstanceId();
        instanceId2.setValue(project.getProperty("instanceId2"));
        deployTask2.addConfiguredInstanceId(instanceId2);

        deploymentGroup1.addTask(deployTask1);
        deploymentGroup1.addTask(deployTask2);

        DeploymentGroup deploymentGroup2 = new DeploymentGroup();
        DeployAppTask deployTask3 = readyDeployTask();
        deployTask3.setPropertyNameForDeploymentId("deploymentId3");
        InstanceId instanceId3 = new InstanceId();
        instanceId3.setValue(project.getProperty("instanceId3"));
        deployTask3.addConfiguredInstanceId(instanceId3);

        DeployAppTask deployTask4 = readyDeployTask();
        deployTask4.setPropertyNameForDeploymentId("deploymentId4");
        InstanceId instanceId4 = new InstanceId();
        instanceId4.setValue(project.getProperty("instanceId4"));
        deployTask4.addConfiguredInstanceId(instanceId4);

        deploymentGroup2.addTask(deployTask3);
        deploymentGroup2.addTask(deployTask4);

        DeploymentGroup deploymentGroup3 = new DeploymentGroup();
        DeployAppTask deployTask5 = readyDeployTask();
        deployTask5.setPropertyNameForDeploymentId("deploymentId5");
        InstanceId instanceId5 = new InstanceId();
        instanceId5.setValue(project.getProperty("instanceId5"));
        deployTask5.addConfiguredInstanceId(instanceId5);

        deploymentGroup3.addTask(deployTask5);

        incrementalDeploymentTask
                .addConfiguredDeploymentGroup(deploymentGroup1);
        incrementalDeploymentTask
                .addConfiguredDeploymentGroup(deploymentGroup2);
        incrementalDeploymentTask
                .addConfiguredDeploymentGroup(deploymentGroup3);

        AWSTestUtils.waitForOpsworksInstanceToReachState(client,
                instanceId1.getValue(), ONLINE);
        AWSTestUtils.waitForOpsworksInstanceToReachState(client,
                instanceId2.getValue(), ONLINE);
        AWSTestUtils.waitForOpsworksInstanceToReachState(client,
                instanceId3.getValue(), ONLINE);
        AWSTestUtils.waitForOpsworksInstanceToReachState(client,
                instanceId4.getValue(), ONLINE);
        AWSTestUtils.waitForOpsworksInstanceToReachState(client,
                instanceId5.getValue(), ONLINE);

        incrementalDeploymentTask.execute();

        Assert.assertTrue(wasSuccessfulDeployment(client,
                deployTask1.getDeploymentId()));
        Assert.assertTrue(wasSuccessfulDeployment(client,
                deployTask2.getDeploymentId()));
        Assert.assertTrue(wasSuccessfulDeployment(client,
                deployTask3.getDeploymentId()));
        Assert.assertTrue(wasSuccessfulDeployment(client,
                deployTask4.getDeploymentId()));
        Assert.assertTrue(wasSuccessfulDeployment(client,
                deployTask5.getDeploymentId()));
    }

    @After
    public void tearDown() throws InterruptedException {

        AWSTestUtils.emptyAndDeleteBucket(s3Client, BUCKET_NAME);
        client.deleteApp(new DeleteAppRequest().withAppId(project
                .getProperty(Constants.APP_ID_PROPERTY)));
        stopAllInstances(project, client);
        deleteAllInstances(project, client);
        deleteAllLayers(project, client);
        client.deleteStack(new DeleteStackRequest().withStackId(project
                .getProperty(Constants.STACK_ID_PROPERTY)));
    }

    public static boolean wasSuccessfulDeployment(AWSOpsWorksClient opsClient,
            String deploymentId) {
        while (true) {
            Deployment deployment = opsClient
                    .describeDeployments(
                            new DescribeDeploymentsRequest()
                                    .withDeploymentIds(deploymentId))
                    .getDeployments().get(0);
            if (deployment.getStatus().equalsIgnoreCase("successful")) {
                System.out.println("Deployment " + deploymentId
                        + " was successful");
                return true;
            } else if (deployment.getStatus().equalsIgnoreCase("failed")) {
                return false;
            }
        }
    }

    public static void deleteAllLayers(Project project, AWSOpsWorksClient client) {
        for (String layerId : project.getProperty(Constants.LAYER_IDS_PROPERTY)
                .split(",")) {
            client.deleteLayer(new DeleteLayerRequest().withLayerId(layerId));
        }
    }

    public static void stopAllInstances(Project project,
            AWSOpsWorksClient client) throws InterruptedException {
        String[] instanceIds = project.getProperty(Constants.INSTANCE_IDS_PROPERTY)
                .split(",");
        for (String instanceId : instanceIds) {
            client.stopInstance(new StopInstanceRequest()
                    .withInstanceId(instanceId));
        }
        for (String instanceId : instanceIds) {
            AWSTestUtils.waitForOpsworksInstanceToReachState(client,
                    instanceId, "stopped");
        }
    }

    public static void deleteAllInstances(Project project,
            AWSOpsWorksClient client) {
        for (String instanceId : project.getProperty(Constants.INSTANCE_IDS_PROPERTY)
                .split(",")) {
            client.deleteInstance(new DeleteInstanceRequest()
                    .withInstanceId(instanceId));
        }
    }
}
