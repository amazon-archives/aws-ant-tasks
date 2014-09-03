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
package com.amazonaws.ant.cloudformation;

import org.apache.tools.ant.Project;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.ant.cloudformation.CreateStackTask.StackParameter;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;

public class CloudFormationTests {

    private static final String UPDATE_COMPLETE = "UPDATE_COMPLETE";
    private static final String WEB_SERVER_CAPACITY_PARAMETER = "WebServerCapacity";
    private static final String UPDATE_ROLLBACK_COMPLETE = "UPDATE_ROLLBACK_COMPLETE";
    private static final String DB_NAME_PARAMETER = "DBName";
    private static final String CREATE_COMPLETE = "CREATE_COMPLETE";
    private static final String KEY_NAME_PARAMETER = "KeyName";
    private static final String KEY_NAME_PROPERTY = "generatedKeyName";
    private static final String TEMPLATE_URL = "https://s3-us-west-2.amazonaws.com/"
            + "cloudformation-templates-us-west-2/"
            + "WordPress_Multi_AZ.template";
    private static final String STACK_NAME = "AntTaskTestStack";
    private static AmazonCloudFormationClient client;
    private static Project project;

    @BeforeClass
    public static void setUp() {
        client = new AmazonCloudFormationClient();
        project = new Project();
        SetUpCloudFormationTestsTask setUpTask = new SetUpCloudFormationTestsTask();
        setUpTask.setProject(project);
        setUpTask.execute();
    }
    @Test
    public void testStackFlow() {
        CreateStackTask createStackTask = new CreateStackTask();
        createStackTask.setProject(project);
        createStackTask.setTemplateURL(TEMPLATE_URL);
        createStackTask.setStackName(STACK_NAME);
        createStackTask.setTimeoutInMinutes(100);
        StackParameter parameter = new StackParameter();
        parameter.setKey(KEY_NAME_PARAMETER);
        parameter.setValue(project.getProperty(KEY_NAME_PROPERTY));
        createStackTask.addConfiguredStackParameter(parameter);
        createStackTask.execute();
        Assert.assertTrue(WaitForStackToReachStateTask.waitForCloudFormationStackToReachStatus(client,
                STACK_NAME, CREATE_COMPLETE));
        
        SetStackPolicyTask policyTask = new SetStackPolicyTask();
        policyTask.setProject(project);
        policyTask.setStackName(STACK_NAME);
        policyTask.setStackPolicyBody(
        "{" +
         "\"Statement\" : [" +
                "{" +
                    "\"Effect\" : \"Deny\"," +
                    "\"Action\" : \"Update:*\"," +
                    "\"Principal\" : \"*\"," +
                    "\"Resource\" : \"LogicalResourceId/DBInstance\"" +
                    "}," +
                "{" +
                    "\"Effect\" : \"Allow\"," +
                    "\"Action\" : \"Update:*\"," +
                    "\"Principal\" : \"*\"," +
                    "\"Resource\" : \"*\"" +
                 "}" +
         "]" +
        "}");
        policyTask.execute();

        UpdateStackTask failedUpdate = new UpdateStackTask();
        failedUpdate.setProject(project);
        failedUpdate.setStackName(STACK_NAME);
        UpdateStackTask.StackParameter nameParameter = new UpdateStackTask.StackParameter();
        nameParameter.setKey(DB_NAME_PARAMETER);
        nameParameter.setValue("DoNotSetThis");
        failedUpdate.addConfiguredStackParameter(nameParameter);
        UpdateStackTask.StackParameter updateKeyPairParameter = new UpdateStackTask.StackParameter();
        updateKeyPairParameter.setKey(KEY_NAME_PARAMETER);
        updateKeyPairParameter.setValue(project.getProperty(KEY_NAME_PROPERTY));
        failedUpdate.addConfiguredStackParameter(updateKeyPairParameter);
        failedUpdate.setUsePreviousTemplate(true);
        failedUpdate.execute();
        //The update should be denied by the stack policy, so it should fail and roll back the stack.
        Assert.assertTrue(WaitForStackToReachStateTask
                .waitForCloudFormationStackToReachStatus(client, STACK_NAME,
                        UPDATE_ROLLBACK_COMPLETE));
        
        UpdateStackTask successfulUpdate = new UpdateStackTask();
        successfulUpdate.setProject(project);
        UpdateStackTask.StackParameter capacityParameter = new UpdateStackTask.StackParameter();
        capacityParameter.setKey(WEB_SERVER_CAPACITY_PARAMETER);
        capacityParameter.setValue("3");
        successfulUpdate.addConfiguredStackParameter(capacityParameter);
        successfulUpdate.addConfiguredStackParameter(updateKeyPairParameter);
        successfulUpdate.setUsePreviousTemplate(true);
        successfulUpdate.setStackName(STACK_NAME);
        successfulUpdate.execute();
        Assert.assertTrue(WaitForStackToReachStateTask
                .waitForCloudFormationStackToReachStatus(client, STACK_NAME,
                        UPDATE_COMPLETE));
    }

    @AfterClass
    public static void tearDown() {
        TearDownCloudFormationTestsTask tearDownTask = new TearDownCloudFormationTestsTask();
        tearDownTask.setProject(project);
        tearDownTask.setKeyName(project.getProperty(KEY_NAME_PROPERTY));
        tearDownTask.setStackName(STACK_NAME);
        tearDownTask.execute();
    }
}
